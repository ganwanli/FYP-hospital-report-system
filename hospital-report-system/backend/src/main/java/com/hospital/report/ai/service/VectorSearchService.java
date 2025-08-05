package com.hospital.report.ai.service;

import com.hospital.report.ai.entity.DatabaseSchema;
import com.hospital.report.ai.entity.TableRelation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 向量搜索服务
 */
@Service
@Slf4j  
public class VectorSearchService {

    @Autowired
    private DatabaseSchemaService databaseSchemaService;
    
    @Autowired
    private EmbeddingService embeddingService;

    /**
     * 根据自然语言查询搜索相关的数据库schema
     */
    public List<DatabaseSchema> searchRelevantSchemas(String naturalLanguageQuery, Long datasourceId, int topK) {
        log.info("开始向量搜索，查询: {}, 数据源ID: {}, 返回前{}个结果", naturalLanguageQuery, datasourceId, topK);
        
        // 1. 为查询生成嵌入向量
        List<Double> queryEmbedding = embeddingService.generateQueryEmbedding(naturalLanguageQuery);
        
        // 2. 获取指定数据源的所有schema
        List<DatabaseSchema> allSchemas = databaseSchemaService.getSchemasByDatasourceId(datasourceId);
        
        // 3. 计算相似度并排序
        List<SchemaWithSimilarity> scoredSchemas = new ArrayList<>();
        
        for (DatabaseSchema schema : allSchemas) {
            if (schema.getEmbedding() != null) {
                List<Double> schemaEmbedding = embeddingService.parseEmbeddingFromJson(schema.getEmbedding());
                if (schemaEmbedding != null) {
                    double similarity = embeddingService.calculateCosineSimilarity(queryEmbedding, schemaEmbedding);
                    scoredSchemas.add(new SchemaWithSimilarity(schema, similarity));
                }
            }
        }
        
        // 4. 按相似度降序排序，取前topK个结果
        List<DatabaseSchema> results = scoredSchemas.stream()
            .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
            .limit(topK)
            .map(SchemaWithSimilarity::getSchema)
            .collect(Collectors.toList());
        
        log.info("向量搜索完成，返回 {} 个相关schema", results.size());
        
        // 打印调试信息
        if (log.isDebugEnabled()) {
            scoredSchemas.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                .limit(topK)
                .forEach(item -> log.debug("相似度: {:.4f}, Schema: {}", 
                    item.getSimilarity(), 
                    item.getSchema().getFullDescription()));
        }
        
        return results;
    }

    /**
     * 搜索相关的表（只返回表概览，不包含字段详情）
     */
    public List<DatabaseSchema> searchRelevantTables(String naturalLanguageQuery, Long datasourceId, int topK) {
        List<DatabaseSchema> allResults = searchRelevantSchemas(naturalLanguageQuery, datasourceId, topK * 2);
        
        // 过滤出表概览（columnName为null的记录）
        return allResults.stream()
            .filter(schema -> schema.getColumnName() == null)
            .limit(topK)
            .collect(Collectors.toList());
    }

    /**
     * 搜索相关的字段
     */
    public List<DatabaseSchema> searchRelevantColumns(String naturalLanguageQuery, Long datasourceId, int topK) {
        List<DatabaseSchema> allResults = searchRelevantSchemas(naturalLanguageQuery, datasourceId, topK * 2);
        
        // 过滤出字段记录（columnName不为null的记录）
        return allResults.stream()
            .filter(schema -> schema.getColumnName() != null)
            .limit(topK)
            .collect(Collectors.toList());
    }

    /**
     * 根据表名获取相关联的表
     */
    public List<String> getRelatedTables(String tableName, Long datasourceId) {
        List<TableRelation> relations = databaseSchemaService.getTableRelations(datasourceId);
        
        Set<String> relatedTables = new HashSet<>();
        
        for (TableRelation relation : relations) {
            if (relation.getPrimaryTable().equals(tableName)) {
                relatedTables.add(relation.getForeignTable());
            } else if (relation.getForeignTable().equals(tableName)) {
                relatedTables.add(relation.getPrimaryTable());
            }
        }
        
        return new ArrayList<>(relatedTables);
    }

    /**
     * 构建查询上下文，包含相关表和字段信息
     */
    public QueryContext buildQueryContext(String naturalLanguageQuery, Long datasourceId) {
        log.info("构建查询上下文: {}", naturalLanguageQuery);
        
        QueryContext context = new QueryContext();
        context.setOriginalQuery(naturalLanguageQuery);
        context.setDatasourceId(datasourceId);
        
        // 搜索相关表
        List<DatabaseSchema> relevantTables = searchRelevantTables(naturalLanguageQuery, datasourceId, 5);
        context.setRelevantTables(relevantTables);
        
        // 搜索相关字段
        List<DatabaseSchema> relevantColumns = searchRelevantColumns(naturalLanguageQuery, datasourceId, 10);
        context.setRelevantColumns(relevantColumns);
        
        // 获取表关系
        Set<String> tableNames = relevantTables.stream()
            .map(DatabaseSchema::getTableName)
            .collect(Collectors.toSet());
        
        Map<String, List<String>> tableRelations = new HashMap<>();
        for (String tableName : tableNames) {
            List<String> relatedTables = getRelatedTables(tableName, datasourceId);
            if (!relatedTables.isEmpty()) {
                tableRelations.put(tableName, relatedTables);
            }
        }
        context.setTableRelations(tableRelations);
        
        log.info("查询上下文构建完成，包含 {} 个相关表，{} 个相关字段", 
            relevantTables.size(), relevantColumns.size());
        
        return context;
    }

    /**
     * Schema与相似度的包装类
     */
    private static class SchemaWithSimilarity {
        private DatabaseSchema schema;
        private double similarity;
        
        public SchemaWithSimilarity(DatabaseSchema schema, double similarity) {
            this.schema = schema;
            this.similarity = similarity;
        }
        
        public DatabaseSchema getSchema() { return schema; }
        public double getSimilarity() { return similarity; }
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