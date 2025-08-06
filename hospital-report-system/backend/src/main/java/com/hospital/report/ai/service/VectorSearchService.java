package com.hospital.report.ai.service;

import com.hospital.report.ai.config.MilvusConfig;
import com.hospital.report.ai.entity.DatabaseSchema;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 向量搜索服务 - 基于Milvus的纯向量搜索架构
 */
@Service
@Slf4j  
public class VectorSearchService {

    @Autowired
    private EmbeddingService embeddingService;
    
    @Autowired
    private VectorStoreService vectorStoreService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据自然语言查询搜索相关的数据库schema - 仅使用Milvus
     */
    public List<DatabaseSchema> searchRelevantSchemas(String naturalLanguageQuery, Long datasourceId, int topK) {
        log.info("开始Milvus向量搜索，查询: {}, 数据源ID: {}, 返回前{}个结果", naturalLanguageQuery, datasourceId, topK);
        
        try {
            // 仅从Milvus搜索
            List<DatabaseSchema> milvusResults = searchFromMilvus(naturalLanguageQuery, datasourceId, topK);
            log.info("从Milvus获取到 {} 个结果", milvusResults.size());
            return milvusResults;
            
        } catch (Exception e) {
            log.error("Milvus向量搜索失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 从Milvus搜索向量数据
     */
    private List<DatabaseSchema> searchFromMilvus(String naturalLanguageQuery, Long datasourceId, int topK) {
        try {
            // 1. 生成查询向量
            List<Float> queryEmbedding = embeddingService.generateQueryEmbedding(naturalLanguageQuery);
            if (queryEmbedding.isEmpty()) {
                log.warn("查询向量生成失败，无法使用Milvus搜索");
                return Collections.emptyList();
            }
            
            // 2. 从Milvus搜索相似向量
            List<VectorStoreService.SearchResult> searchResults = 
                vectorStoreService.searchSimilarVectors(MilvusConfig.SCHEMA_COLLECTION, queryEmbedding, topK * 2);
            
            if (searchResults.isEmpty()) {
                log.info("Milvus搜索结果为空");
                return Collections.emptyList();
            }
            
            // 3. 转换结果并过滤数据源
            List<DatabaseSchema> results = searchResults.stream()
                .filter(result -> belongsToDatasource(result, datasourceId))
                .limit(topK)
                .map(this::convertToSchema)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            log.info("Milvus搜索完成，筛选后返回{}个相关结果", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("Milvus搜索异常: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 搜索相关的表（只返回表概览，不包含字段详情）
     */
    public List<DatabaseSchema> searchRelevantTables(String naturalLanguageQuery, Long datasourceId, int topK) {
        List<DatabaseSchema> allResults = searchRelevantSchemas(naturalLanguageQuery, datasourceId, topK * 2);
        
        // 过滤出表概览（columnName为null或空的记录）
        return allResults.stream()
            .filter(schema -> schema.getColumnName() == null || schema.getColumnName().trim().isEmpty())
            .limit(topK)
            .collect(Collectors.toList());
    }

    /**
     * 搜索相关的字段
     */
    public List<DatabaseSchema> searchRelevantColumns(String naturalLanguageQuery, Long datasourceId, int topK) {
        List<DatabaseSchema> allResults = searchRelevantSchemas(naturalLanguageQuery, datasourceId, topK * 2);
        
        // 过滤出字段记录（columnName不为null且不为空的记录）
        return allResults.stream()
            .filter(schema -> schema.getColumnName() != null && !schema.getColumnName().trim().isEmpty())
            .limit(topK)
            .collect(Collectors.toList());
    }

    /**
     * 根据表名获取相关联的表 - 从Milvus数据推断
     */
    public List<String> getRelatedTables(String tableName, Long datasourceId) {
        try {
            // 从Milvus中搜索相关表信息
            List<VectorStoreService.SearchResult> results = 
                vectorStoreService.searchByDatasourceId(MilvusConfig.SCHEMA_COLLECTION, datasourceId, 1000);
            
            Set<String> relatedTables = new HashSet<>();
            for (VectorStoreService.SearchResult result : results) {
                String metadata = result.getMetadata();
                if (metadata != null) {
                    String resultTableName = parseJsonStringValue(metadata, "tableName");
                    if (resultTableName != null && !resultTableName.equals(tableName) && 
                        areTablesRelated(tableName, resultTableName)) {
                        relatedTables.add(resultTableName);
                    }
                }
            }
            
            return new ArrayList<>(relatedTables);
            
        } catch (Exception e) {
            log.error("获取相关表失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * 简单判断两个表是否可能相关
     */
    private boolean areTablesRelated(String table1, String table2) {
        String t1 = table1.toLowerCase();
        String t2 = table2.toLowerCase();
        
        // 如果一个表名是另一个表名的一部分
        if (t1.contains(t2) || t2.contains(t1)) {
            return true;
        }
        
        // 如果都包含常见的关联词
        String[] commonWords = {"patient", "user", "order", "record", "info", "detail", "sys_"};
        for (String word : commonWords) {
            if (t1.contains(word) && t2.contains(word)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 构建查询上下文，包含相关表和字段信息 - 仅基于Milvus
     */
    public QueryContext buildQueryContext(String naturalLanguageQuery, Long datasourceId) {
        log.info("构建Milvus查询上下文: {}", naturalLanguageQuery);
        
        QueryContext context = new QueryContext();
        context.setOriginalQuery(naturalLanguageQuery);
        context.setDatasourceId(datasourceId);
        
        // 搜索相关表
        List<DatabaseSchema> relevantTables = searchRelevantTables(naturalLanguageQuery, datasourceId, 5);
        context.setRelevantTables(relevantTables);
        
        // 搜索相关字段
        List<DatabaseSchema> relevantColumns = searchRelevantColumns(naturalLanguageQuery, datasourceId, 20);
        context.setRelevantColumns(relevantColumns);
        
        // 获取表关系 - 基于搜索到的表
        Set<String> tableNames = relevantTables.stream()
            .map(DatabaseSchema::getTableName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        Map<String, List<String>> tableRelations = new HashMap<>();
        for (String tableName : tableNames) {
            List<String> relatedTables = getRelatedTables(tableName, datasourceId);
            if (!relatedTables.isEmpty()) {
                tableRelations.put(tableName, relatedTables);
            }
        }
        context.setTableRelations(tableRelations);
        
        log.info("Milvus查询上下文构建完成，包含 {} 个相关表，{} 个相关字段", 
            relevantTables.size(), relevantColumns.size());
        
        return context;
    }
    
    /**
     * 获取指定数据源的所有schema - 从Milvus获取
     */
    public List<DatabaseSchema> getSchemasByDatasourceId(Long datasourceId) {
        try {
            List<VectorStoreService.SearchResult> results = 
                vectorStoreService.searchByDatasourceId(MilvusConfig.SCHEMA_COLLECTION, datasourceId, 10000);
            
            return results.stream()
                .map(this::convertToSchema)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("从Milvus获取schema失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * 检查数据源是否已有schema数据 - 从Milvus检查
     */
    public boolean hasSchemaData(Long datasourceId) {
        return vectorStoreService.hasDataForDatasource(MilvusConfig.SCHEMA_COLLECTION, datasourceId);
    }
    
    /**
     * 获取schema统计信息 - 从Milvus获取
     */
    public VectorStoreService.SchemaStatistics getSchemaStatistics(Long datasourceId) {
        return vectorStoreService.getSchemaStatistics(MilvusConfig.SCHEMA_COLLECTION, datasourceId);
    }

    /**
     * 判断搜索结果是否属于指定数据源
     */
    private boolean belongsToDatasource(VectorStoreService.SearchResult result, Long datasourceId) {
        try {
            String metadata = result.getMetadata();
            if (metadata == null) return false;
            
            // 简单的JSON解析，检查datasourceId
            return metadata.contains("\"datasourceId\":" + datasourceId);
            
        } catch (Exception e) {
            log.debug("解析metadata失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 将Milvus搜索结果转换为DatabaseSchema
     */
    private DatabaseSchema convertToSchema(VectorStoreService.SearchResult result) {
        try {
            DatabaseSchema schema = new DatabaseSchema();
            
            // 解析source_id获取schema ID
            String sourceId = result.getSourceId();
            if (sourceId != null && sourceId.startsWith("schema_")) {
                try {
                    schema.setId(Long.parseLong(sourceId.substring(7)));
                } catch (NumberFormatException e) {
                    log.debug("解析source_id失败: {}", sourceId);
                }
            }
            
            // 设置描述内容
            schema.setFullDescription(result.getContent());
            
            // 解析metadata获取详细信息
            if (result.getMetadata() != null) {
                parseMetadataToSchema(result.getMetadata(), schema);
            }
            
            return schema;
            
        } catch (Exception e) {
            log.warn("转换搜索结果为Schema失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析metadata并填充到schema对象
     */
    private void parseMetadataToSchema(String metadata, DatabaseSchema schema) {
        try {
            // 简单的JSON解析，提取关键字段
            schema.setDatasourceId(parseJsonLongValue(metadata, "datasourceId"));
            
            String tableName = parseJsonStringValue(metadata, "tableName");
            if (tableName != null && !tableName.trim().isEmpty()) {
                schema.setTableName(tableName);
            }
            
            String columnName = parseJsonStringValue(metadata, "columnName");
            if (columnName != null && !columnName.trim().isEmpty()) {
                schema.setColumnName(columnName);
            }
            
            String columnType = parseJsonStringValue(metadata, "columnType");
            if (columnType != null && !columnType.trim().isEmpty()) {
                schema.setColumnType(columnType);
            }
            
        } catch (Exception e) {
            log.debug("解析metadata失败: {}", e.getMessage());
        }
    }
    
    /**
     * 从JSON字符串中解析Long值
     */
    private Long parseJsonLongValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":([0-9]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return Long.parseLong(m.group(1));
            }
        } catch (Exception e) {
            log.debug("JSON Long解析失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 从JSON字符串中解析String值
     */
    private String parseJsonStringValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":\"([^\"]*)\";";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            log.debug("JSON String解析失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 查询上下文类
     */
    public static class QueryContext {
        private String originalQuery;
        private Long datasourceId;
        private List<DatabaseSchema> relevantTables;
        private List<DatabaseSchema> relevantColumns;
        private Map<String, List<String>> tableRelations;
        
        // Getters and Setters
        public String getOriginalQuery() { return originalQuery; }
        public void setOriginalQuery(String originalQuery) { this.originalQuery = originalQuery; }
        
        public Long getDatasourceId() { return datasourceId; }
        public void setDatasourceId(Long datasourceId) { this.datasourceId = datasourceId; }
        
        public List<DatabaseSchema> getRelevantTables() { return relevantTables; }
        public void setRelevantTables(List<DatabaseSchema> relevantTables) { this.relevantTables = relevantTables; }
        
        public List<DatabaseSchema> getRelevantColumns() { return relevantColumns; }
        public void setRelevantColumns(List<DatabaseSchema> relevantColumns) { this.relevantColumns = relevantColumns; }
        
        public Map<String, List<String>> getTableRelations() { return tableRelations; }
        public void setTableRelations(Map<String, List<String>> tableRelations) { this.tableRelations = tableRelations; }
    }
}