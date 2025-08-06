package com.hospital.report.ai.service;

import com.hospital.report.ai.config.MilvusConfig;
import com.hospital.report.ai.entity.DatabaseSchema;
import com.hospital.report.entity.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据库Schema管理服务 - 纯Milvus向量存储架构
 */
@Service
@Slf4j
public class DatabaseSchemaService {

    @Autowired
    private DatabaseMetadataExtractor metadataExtractor;
    
    @Autowired
    private EmbeddingService embeddingService;
    
    @Autowired
    private VectorStoreService vectorStoreService;

    /**
     * 为数据源创建schema向量数据（纯Milvus存储）
     */
    @Transactional
    public void createOrUpdateSchemaVectors(DataSource dataSource) {
        log.info("开始为数据源 {} 创建schema向量数据（纯Milvus存储）", dataSource.getDatasourceName());
        
        try {
            // 1. 清除Milvus中的现有数据
            clearExistingDataFromMilvus(dataSource.getId());
            
            // 2. 提取数据库元数据
            List<DatabaseSchema> schemas = metadataExtractor.extractDatabaseSchema(dataSource);
            
            if (schemas.isEmpty()) {
                log.warn("数据源 {} 没有提取到schema信息", dataSource.getDatasourceName());
                return;
            }
            
            log.info("提取到 {} 个schema记录", schemas.size());
            
            // 3. 生成向量嵌入并直接存储到Milvus
            embeddingService.generateEmbeddingsForSchemas(schemas);
            
            log.info("成功为数据源 {} 创建了 {} 个schema向量记录（纯Milvus存储）", 
                dataSource.getDatasourceName(), schemas.size());
                
        } catch (Exception e) {
            log.error("为数据源 {} 创建schema向量数据失败: {}", dataSource.getDatasourceName(), e.getMessage(), e);
            throw new RuntimeException("Failed to create schema vectors for datasource: " + dataSource.getDatasourceName(), e);
        }
    }
    
    /**
     * 清除指定数据源在Milvus中的现有schema数据
     */
    private void clearExistingDataFromMilvus(Long datasourceId) {
        log.info("清除数据源 {} 在Milvus中的现有schema数据", datasourceId);
        
        try {
            // 查询所有相关数据的source_id
            List<VectorStoreService.SearchResult> existingData = 
                vectorStoreService.searchByDatasourceId(MilvusConfig.SCHEMA_COLLECTION, datasourceId, 10000);
            
            // 逐个删除
            for (VectorStoreService.SearchResult result : existingData) {
                if (result.getSourceId() != null) {
                    vectorStoreService.deleteVectors(MilvusConfig.SCHEMA_COLLECTION, result.getSourceId());
                }
            }
            
            log.info("清除了 {} 条数据源 {} 的旧schema数据（Milvus）", existingData.size(), datasourceId);
            
        } catch (Exception e) {
            log.warn("清除Milvus中的旧数据时出现异常：{}", e.getMessage());
        }
    }

    /**
     * 获取指定数据源的所有schema - 从Milvus获取
     */
    public List<DatabaseSchema> getSchemasByDatasourceId(Long datasourceId) {
        try {
            List<VectorStoreService.SearchResult> results = 
                vectorStoreService.searchByDatasourceId(MilvusConfig.SCHEMA_COLLECTION, datasourceId, 10000);
            
            return results.stream()
                .map(this::convertSearchResultToSchema)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
                
        } catch (Exception e) {
            log.error("从Milvus获取schema失败: {}", e.getMessage());
            return java.util.Collections.emptyList();
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
     * 转换Milvus搜索结果为DatabaseSchema对象
     */
    private DatabaseSchema convertSearchResultToSchema(VectorStoreService.SearchResult result) {
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
            String pattern = "\"" + key + "\":\"([^\"]*)\"";
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
}