package com.hospital.report.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.report.ai.config.MilvusConfig;
import com.hospital.report.entity.SqlTemplate;
import com.hospital.report.service.SqlTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * SQL知识库向量化服务
 * 负责将SQL Asset中的SQL模板向量化并存储到Milvus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SqlKnowledgeBaseService {
    
    private final VectorStoreService vectorStoreService;
    private final EmbeddingService embeddingService;
    private final SqlTemplateService sqlTemplateService;
    private final ObjectMapper objectMapper;
    
    /**
     * 初始化SQL知识库 - 将所有SQL Asset向量化存储
     */
    public void initializeSqlKnowledgeBase() {
        log.info("开始初始化SQL知识库向量数据");
        
        try {
            // 确保SQL集合存在
            boolean created = vectorStoreService.createCollection(
                MilvusConfig.SQL_COLLECTION, 
                "SQL Asset知识库向量存储"
            );
            
            if (!created) {
                log.error("创建SQL集合失败，停止初始化");
                return;
            }
            
            // 获取所有SQL模板 - 使用分页获取所有数据
            List<SqlTemplate> sqlTemplates = sqlTemplateService.searchTemplates("");
            log.info("获取到 {} 个SQL模板", sqlTemplates.size());
            
            if (sqlTemplates.isEmpty()) {
                log.info("没有SQL模板可以向量化");
                return;
            }
            
            // 向量化SQL模板
            vectorizeSqlTemplates(sqlTemplates);
            
        } catch (Exception e) {
            log.error("初始化SQL知识库失败", e);
        }
    }
    
    /**
     * 向量化SQL模板列表
     */
    public void vectorizeSqlTemplates(List<SqlTemplate> sqlTemplates) {
        log.info("开始向量化 {} 个SQL模板", sqlTemplates.size());
        
        // 准备向量数据
        List<VectorStoreService.VectorData> vectorDataList = new ArrayList<>();
        
        // 并发处理SQL模板
        List<CompletableFuture<Void>> futures = sqlTemplates.stream()
            .map(template -> CompletableFuture.runAsync(() -> {
                try {
                    // 构建SQL描述文本
                    String sqlDescription = buildSqlDescription(template);
                    
                    if (sqlDescription != null && !sqlDescription.trim().isEmpty()) {
                        // 生成向量嵌入
                        List<Float> embedding = embeddingService.generateEmbedding(sqlDescription);
                        
                        if (!embedding.isEmpty()) {
                            // 准备元数据
                            String metadata = buildSqlMetadata(template);
                            
                            // 创建向量数据
                            VectorStoreService.VectorData vectorData = new VectorStoreService.VectorData(
                                "sql_" + template.getId(),
                                sqlDescription,
                                metadata,
                                embedding
                            );
                            
                            synchronized (vectorDataList) {
                                vectorDataList.add(vectorData);
                            }
                            
                            log.debug("SQL模板向量化完成: {} -> 维度: {}", 
                                template.getTemplateName(), embedding.size());
                        }
                    }
                } catch (Exception e) {
                    log.error("SQL模板向量化失败: {}", template.getTemplateName(), e);
                }
            }))
            .collect(Collectors.toList());
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 批量插入到Milvus
        if (!vectorDataList.isEmpty()) {
            boolean success = vectorStoreService.insertVectors(MilvusConfig.SQL_COLLECTION, vectorDataList);
            if (success) {
                log.info("成功将 {} 个SQL模板向量存储到Milvus", vectorDataList.size());
            } else {
                log.error("存储SQL模板向量到Milvus失败");
            }
        }
        
        log.info("SQL模板向量化完成");
    }
    
    /**
     * 构建SQL描述文本用于向量化
     */
    private String buildSqlDescription(SqlTemplate template) {
        StringBuilder description = new StringBuilder();
        
        // 添加模板名称
        if (template.getTemplateName() != null) {
            description.append("SQL模板名称: ").append(template.getTemplateName()).append("\n");
        }
        
        // 添加描述
        if (template.getDescription() != null) {
            description.append("描述: ").append(template.getDescription()).append("\n");
        }
        
        // 添加SQL语句
        if (template.getSqlContent() != null) {
            description.append("SQL语句: ").append(template.getSqlContent()).append("\n");
        }
        
        // 添加业务类型
        if (template.getBusinessType() != null) {
            description.append("业务类型: ").append(template.getBusinessType()).append("\n");
        }
        
        // 添加标签
        if (template.getTags() != null && !template.getTags().trim().isEmpty()) {
            description.append("标签: ").append(template.getTags()).append("\n");
        }
        
        // 添加参数信息
        if (template.getParameters() != null && !template.getParameters().isEmpty()) {
            description.append("参数信息: ").append(template.getParameters()).append("\n");
        }
        
        // 添加字段信息（如果有）
        if (template.getFields() != null && !template.getFields().isEmpty()) {
            description.append("字段信息: ");
            template.getFields().forEach(field -> {
                description.append(field.getFieldName()).append(" (")
                          .append(field.getFieldType()).append(") ");
                if (field.getDescription() != null) {
                    description.append(field.getDescription()).append(" ");
                }
            });
            description.append("\n");
        }
        
        return description.toString().trim();
    }
    
    /**
     * 构建SQL元数据JSON
     */
    private String buildSqlMetadata(SqlTemplate template) {
        try {
            SqlMetadata metadata = new SqlMetadata();
            metadata.setTemplateId(template.getId());
            metadata.setTemplateName(template.getTemplateName());
            metadata.setBusinessType(template.getBusinessType());
            metadata.setDatasourceId(template.getDatasourceId());
            metadata.setTags(template.getTags());
            metadata.setComplexity(template.getComplexity() != null ? template.getComplexity().toString() : "");
            metadata.setExecuteCount(template.getExecuteCount() != null ? template.getExecuteCount().intValue() : 0);
            metadata.setAvgExecuteTime(template.getAvgExecuteTime() != null ? template.getAvgExecuteTime().longValue() : 0L);
            
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.error("构建SQL元数据失败: {}", template.getTemplateName(), e);
            return "{}";
        }
    }
    
    /**
     * 根据自然语言查询搜索相似的SQL
     */
    public List<SqlSearchResult> searchSimilarSql(String naturalLanguageQuery, int topK) {
        log.info("搜索相似SQL，查询: {}, topK: {}", naturalLanguageQuery, topK);
        
        try {
            // 生成查询向量
            List<Float> queryVector = embeddingService.generateQueryEmbedding(naturalLanguageQuery);
            
            if (queryVector.isEmpty()) {
                log.warn("查询向量生成失败");
                return new ArrayList<>();
            }
            
            // 在Milvus中搜索
            List<VectorStoreService.SearchResult> searchResults = vectorStoreService.searchSimilarVectors(
                MilvusConfig.SQL_COLLECTION, queryVector, topK
            );
            
            // 转换搜索结果
            List<SqlSearchResult> results = new ArrayList<>();
            for (VectorStoreService.SearchResult result : searchResults) {
                try {
                    SqlSearchResult sqlResult = new SqlSearchResult();
                    sqlResult.setScore(result.getScore());
                    sqlResult.setContent(result.getContent());
                    
                    // 解析元数据
                    if (result.getMetadata() != null) {
                        SqlMetadata metadata = objectMapper.readValue(result.getMetadata(), SqlMetadata.class);
                        sqlResult.setTemplateId(metadata.getTemplateId());
                        sqlResult.setTemplateName(metadata.getTemplateName());
                        sqlResult.setBusinessType(metadata.getBusinessType());
                        sqlResult.setDatasourceId(metadata.getDatasourceId());
                        sqlResult.setTags(metadata.getTags());
                    }
                    
                    results.add(sqlResult);
                } catch (Exception e) {
                    log.error("解析搜索结果失败", e);
                }
            }
            
            log.info("搜索完成，返回 {} 个SQL结果", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("搜索相似SQL失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 添加新的SQL模板到知识库
     */
    public void addSqlToKnowledgeBase(SqlTemplate template) {
        log.info("添加SQL模板到知识库: {}", template.getTemplateName());
        
        try {
            // 确保集合存在
            vectorStoreService.createCollection(MilvusConfig.SQL_COLLECTION, "SQL Asset知识库向量存储");
            
            // 向量化单个SQL模板
            List<SqlTemplate> templates = List.of(template);
            vectorizeSqlTemplates(templates);
            
        } catch (Exception e) {
            log.error("添加SQL模板到知识库失败: {}", template.getTemplateName(), e);
        }
    }
    
    /**
     * 从知识库删除SQL模板
     */
    public void removeSqlFromKnowledgeBase(Long templateId) {
        log.info("从知识库删除SQL模板: {}", templateId);
        
        try {
            boolean success = vectorStoreService.deleteVectors(
                MilvusConfig.SQL_COLLECTION, 
                "sql_" + templateId
            );
            
            if (success) {
                log.info("成功删除SQL模板向量: {}", templateId);
            } else {
                log.error("删除SQL模板向量失败: {}", templateId);
            }
            
        } catch (Exception e) {
            log.error("从知识库删除SQL模板失败: {}", templateId, e);
        }
    }
    
    /**
     * 重建整个SQL知识库
     */
    public void rebuildSqlKnowledgeBase() {
        log.info("开始重建SQL知识库");
        
        try {
            // 删除现有集合
            vectorStoreService.dropCollection(MilvusConfig.SQL_COLLECTION);
            
            // 重新初始化
            initializeSqlKnowledgeBase();
            
            log.info("SQL知识库重建完成");
            
        } catch (Exception e) {
            log.error("重建SQL知识库失败", e);
        }
    }
    
    /**
     * SQL元数据DTO
     */
    public static class SqlMetadata {
        private Long templateId;
        private String templateName;
        private String businessType;
        private Long datasourceId;
        private String tags;
        private String complexity;
        private Integer executeCount;
        private Long avgExecuteTime;
        
        // Getters and Setters
        public Long getTemplateId() { return templateId; }
        public void setTemplateId(Long templateId) { this.templateId = templateId; }
        
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        
        public Long getDatasourceId() { return datasourceId; }
        public void setDatasourceId(Long datasourceId) { this.datasourceId = datasourceId; }
        
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
        
        public String getComplexity() { return complexity; }
        public void setComplexity(String complexity) { this.complexity = complexity; }
        
        public Integer getExecuteCount() { return executeCount; }
        public void setExecuteCount(Integer executeCount) { this.executeCount = executeCount; }
        
        public Long getAvgExecuteTime() { return avgExecuteTime; }
        public void setAvgExecuteTime(Long avgExecuteTime) { this.avgExecuteTime = avgExecuteTime; }
    }
    
    /**
     * SQL搜索结果DTO
     */
    public static class SqlSearchResult {
        private Long templateId;
        private String templateName;
        private String content;
        private String businessType;
        private Long datasourceId;
        private String tags;
        private Float score;
        
        // Getters and Setters
        public Long getTemplateId() { return templateId; }
        public void setTemplateId(Long templateId) { this.templateId = templateId; }
        
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        
        public Long getDatasourceId() { return datasourceId; }
        public void setDatasourceId(Long datasourceId) { this.datasourceId = datasourceId; }
        
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
        
        public Float getScore() { return score; }
        public void setScore(Float score) { this.score = score; }
    }
}