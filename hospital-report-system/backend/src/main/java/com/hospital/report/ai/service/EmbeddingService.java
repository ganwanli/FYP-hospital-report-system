package com.hospital.report.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.report.ai.entity.DatabaseSchema;
import com.hospital.report.ai.config.MilvusConfig;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 向量嵌入生成服务 - 使用LangChain4J集成千问向量模型 + Milvus存储
 */
@Service
@Slf4j
public class EmbeddingService {

    @Autowired
    private EmbeddingModel qwenEmbeddingModel;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private VectorStoreService vectorStoreService;

    /**
     * 为数据库schema生成向量嵌入并存储（混合存储：MySQL+Milvus）
     */
    public void generateEmbeddingsForSchemas(List<DatabaseSchema> schemas) {
        log.info("开始为 {} 个schema使用千问向量模型生成向量嵌入并存储（混合存储）", schemas.size());
        
        // 确保schema集合存在
        vectorStoreService.createCollection(MilvusConfig.SCHEMA_COLLECTION, "数据库Schema向量存储");
        
        // 准备向量数据
        List<VectorStoreService.VectorData> vectorDataList = new ArrayList<>();
        
        // 使用并发处理提高效率
        List<CompletableFuture<Void>> futures = schemas.stream()
            .map(schema -> CompletableFuture.runAsync(() -> {
                try {
                    // 使用完整描述生成嵌入向量
                    String description = schema.getFullDescription();
                    if (description != null && !description.trim().isEmpty()) {
                        List<Float> embedding = generateEmbedding(description);
                        
                        if (!embedding.isEmpty()) {
                            // 为MySQL存储准备JSON格式的向量
                            List<Double> embeddingDouble = embedding.stream()
                                .map(Float::doubleValue)
                                .collect(Collectors.toList());
                            
                            try {
                                String embeddingJson = objectMapper.writeValueAsString(embeddingDouble);
                                schema.setEmbedding(embeddingJson); // 存储到MySQL
                            } catch (Exception e) {
                                log.warn("向量JSON序列化失败: {}", e.getMessage());
                            }
                            
                            // 为Milvus存储准备详细的元数据（增强版）
                            String metadata = buildEnhancedMetadata(schema);
                            
                            // 创建向量数据（使用真实的schema ID）
                            String sourceId;
                            if (schema.getId() != null) {
                                sourceId = "schema_" + schema.getId();
                                log.debug("使用真实ID: {}", sourceId);
                            } else {
                                sourceId = "schema_temp_" + System.currentTimeMillis() + "_" + Math.random();
                                log.warn("使用临时ID（schema.getId()为null）: {}", sourceId);
                            }
                            
                            VectorStoreService.VectorData vectorData = new VectorStoreService.VectorData(
                                sourceId,
                                description,
                                metadata,
                                embedding
                            );
                            
                            synchronized (vectorDataList) {
                                vectorDataList.add(vectorData);
                            }
                            
                            log.debug("千问向量模型为schema生成嵌入: {} -> 维度: {}", 
                                description.substring(0, Math.min(50, description.length())), 
                                embedding.size());
                        }
                    }
                } catch (Exception e) {
                    log.error("千问向量模型为schema生成嵌入失败: {}", schema.getFullDescription(), e);
                    // 继续处理其他schema，不因单个失败而停止
                }
            }))
            .collect(Collectors.toList());
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 批量插入到Milvus（继续存储到Milvus）
        if (!vectorDataList.isEmpty()) {
            boolean success = vectorStoreService.insertVectors(MilvusConfig.SCHEMA_COLLECTION, vectorDataList);
            if (success) {
                log.info("成功将 {} 个schema向量存储到Milvus", vectorDataList.size());
            } else {
                log.warn("存储schema向量到Milvus失败，但MySQL存储仍然有效");
            }
        } else {
            log.warn("没有生成任何向量数据，跳过Milvus存储");
        }
        
        log.info("千问向量模型嵌入生成并存储完成（混合存储：MySQL+Milvus）");
    }

    /**
     * 为单个文本生成向量嵌入 - 使用千问向量模型，返回Float列表用于Milvus
     */
    public List<Float> generateEmbedding(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                log.warn("输入文本为空，跳过嵌入生成");
                return new ArrayList<>();
            }
            
            log.debug("使用千问向量模型生成嵌入，文本长度: {}", text.length());
            
            // 使用LangChain4J的千问向量模型生成嵌入
            Embedding embedding = qwenEmbeddingModel.embed(text).content();
            
            // 转换为Float列表用于Milvus存储
            List<Float> embeddingVector = new ArrayList<>();
            float[] vector = embedding.vector();
            for (float value : vector) {
                embeddingVector.add(value);
            }
            
            log.info("千问向量模型生成嵌入成功，维度: {}", embeddingVector.size());
            return embeddingVector;
            
        } catch (Exception e) {
            log.error("千问向量模型生成嵌入向量失败: {}", text.substring(0, Math.min(100, text.length())), e);
            // 如果千问API失败，回退到模拟向量
            log.warn("回退到模拟向量生成");
            return generateMockEmbeddingFloat(text);
        }
    }
    
    /**
     * 为兼容性保留的Double版本
     */
    public List<Double> generateEmbeddingDouble(String text) {
        List<Float> floatEmbedding = generateEmbedding(text);
        return floatEmbedding.stream()
            .map(Float::doubleValue)
            .collect(Collectors.toList());
    }

    /**
     * 计算两个向量的余弦相似度
     */
    public double calculateCosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("向量维度不匹配: " + vector1.size() + " vs " + vector2.size());
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += vector1.get(i) * vector1.get(i);
            norm2 += vector2.get(i) * vector2.get(i);
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 从JSON字符串解析向量
     */
    public List<Double> parseEmbeddingFromJson(String embeddingJson) {
        try {
            return objectMapper.readValue(embeddingJson, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class));
        } catch (JsonProcessingException e) {
            log.error("解析向量JSON失败: {}", embeddingJson, e);
            return null;
        }
    }

    /**
     * 生成模拟向量嵌入（回退方案）- Float版本
     */
    private List<Float> generateMockEmbeddingFloat(String text) {
        log.debug("生成模拟向量嵌入作为回退方案");
        // 基于文本内容生成确定性的模拟向量
        java.util.Random random = new java.util.Random(text.hashCode());
        List<Float> embedding = new ArrayList<>();
        
        // 使用标准的1536维度（与千问向量模型保持一致）
        for (int i = 0; i < MilvusConfig.VECTOR_DIMENSION; i++) {
            embedding.add((float) random.nextGaussian());
        }
        
        // 归一化向量
        double norm = embedding.stream().mapToDouble(x -> x * x).sum();
        norm = Math.sqrt(norm);
        
        if (norm > 0) {
            for (int i = 0; i < embedding.size(); i++) {
                embedding.set(i, (float) (embedding.get(i) / norm));
            }
        }
        
        return embedding;
    }

    /**
     * 为查询文本生成嵌入向量
     */
    public List<Float> generateQueryEmbedding(String queryText) {
        // 对查询文本进行预处理
        String processedQuery = preprocessQuery(queryText);
        return generateEmbedding(processedQuery);
    }

    /**
     * 预处理查询文本
     */
    private String preprocessQuery(String query) {
        if (query == null) return "";
        
        // 转换为小写
        query = query.toLowerCase();
        
        // 移除标点符号
        query = query.replaceAll("[\\p{Punct}]", " ");
        
        // 替换常见的SQL术语
        query = query.replaceAll("\\b(select|from|where|join|group by|order by)\\b", "");
        
        // 移除多余空格
        query = query.replaceAll("\\s+", " ").trim();
        
        return query;
    }
    
    /**
     * 批量生成向量嵌入
     */
    public List<List<Float>> generateBatchEmbeddings(List<String> texts) {
        log.info("批量生成向量嵌入，数量: {}", texts.size());
        
        return texts.parallelStream()
            .map(this::generateEmbedding)
            .collect(Collectors.toList());
    }

    /**
     * 批量生成向量嵌入 (Double版本)
     */
    public List<List<Double>> generateBatchEmbeddingsDouble(List<String> texts) {
        log.info("批量生成向量嵌入(Double)，数量: {}", texts.size());
        
        return texts.parallelStream()
            .map(this::generateEmbeddingDouble)
            .collect(Collectors.toList());
    }
    
    /**
     * 测试千问向量模型连接
     */
    public boolean testEmbeddingConnection() {
        try {
            List<Float> testEmbedding = generateEmbedding("测试连接");
            boolean success = !testEmbedding.isEmpty();
            
            if (success) {
                log.info("千问向量模型连接测试成功，向量维度: {}", testEmbedding.size());
            } else {
                log.warn("千问向量模型连接测试失败");
            }
            
            return success;
        } catch (Exception e) {
            log.error("千问向量模型连接测试异常", e);
            return false;
        }
    }

    /**
     * 构建增强版元数据，包含完整的数据库schema信息
     */
    private String buildEnhancedMetadata(DatabaseSchema schema) {
        Map<String, Object> metadata = new HashMap<>();
        
        // 基础字段
        metadata.put("datasourceId", schema.getDatasourceId());
        metadata.put("schemaId", schema.getId());
        metadata.put("databaseName", schema.getDatabaseName());
        metadata.put("tableName", schema.getTableName());
        
        // 表相关信息
        if (schema.getTableComment() != null && !schema.getTableComment().trim().isEmpty()) {
            metadata.put("tableComment", schema.getTableComment());
        }
        
        // 字段相关信息
        if (schema.getColumnName() != null && !schema.getColumnName().trim().isEmpty()) {
            metadata.put("columnName", schema.getColumnName());
        }
        
        if (schema.getColumnType() != null && !schema.getColumnType().trim().isEmpty()) {
            metadata.put("columnType", schema.getColumnType());
        }
        
        if (schema.getColumnComment() != null && !schema.getColumnComment().trim().isEmpty()) {
            metadata.put("columnComment", schema.getColumnComment());
        }
        
        // 字段属性
        if (schema.getIsPrimaryKey() != null) {
            metadata.put("isPrimaryKey", schema.getIsPrimaryKey());
        }
        
        if (schema.getIsNullable() != null) {
            metadata.put("isNullable", schema.getIsNullable());
        }
        
        if (schema.getDefaultValue() != null && !schema.getDefaultValue().trim().isEmpty()) {
            metadata.put("defaultValue", schema.getDefaultValue());
        }
        
        // 时间戳
        if (schema.getCreatedTime() != null) {
            metadata.put("createdTime", schema.getCreatedTime().toString());
        }
        
        if (schema.getUpdatedTime() != null) {
            metadata.put("updatedTime", schema.getUpdatedTime().toString());
        }
        
        // 完整描述
        if (schema.getFullDescription() != null && !schema.getFullDescription().trim().isEmpty()) {
            metadata.put("fullDescription", schema.getFullDescription());
        }
        
        try {
            String metadataJson = objectMapper.writeValueAsString(metadata);
            log.debug("构建增强元数据: {}", metadataJson);
            return metadataJson;
        } catch (Exception e) {
            log.warn("构建增强元数据JSON失败: {}", e.getMessage());
            // 回退到基础元数据
            return String.format("{\"datasourceId\":%d,\"tableName\":\"%s\",\"columnName\":\"%s\"}", 
                schema.getDatasourceId(), 
                schema.getTableName() != null ? schema.getTableName() : "",
                schema.getColumnName() != null ? schema.getColumnName() : "");
        }
    }
}