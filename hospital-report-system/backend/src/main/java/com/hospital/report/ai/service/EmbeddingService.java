package com.hospital.report.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.report.ai.client.DeepSeekClient;
import com.hospital.report.ai.entity.DatabaseSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 向量嵌入生成服务
 */
@Service
@Slf4j
public class EmbeddingService {

    @Autowired
    private DeepSeekClient deepSeekClient;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 为数据库schema生成向量嵌入
     */
    public void generateEmbeddingsForSchemas(List<DatabaseSchema> schemas) {
        log.info("开始为 {} 个schema生成向量嵌入", schemas.size());
        
        for (DatabaseSchema schema : schemas) {
            try {
                // 使用完整描述生成嵌入向量
                String description = schema.getFullDescription();
                if (description != null && !description.trim().isEmpty()) {
                    List<Double> embedding = generateEmbedding(description);
                    
                    // 将向量转换为JSON字符串存储
                    String embeddingJson = objectMapper.writeValueAsString(embedding);
                    schema.setEmbedding(embeddingJson);
                    
                    log.debug("为schema生成嵌入: {} -> 维度: {}", 
                        description.substring(0, Math.min(50, description.length())), 
                        embedding.size());
                }
            } catch (Exception e) {
                log.error("为schema生成嵌入失败: {}", schema.getFullDescription(), e);
                // 继续处理其他schema，不因单个失败而停止
            }
        }
        
        log.info("向量嵌入生成完成");
    }

    /**
     * 为单个文本生成向量嵌入
     */
    public List<Double> generateEmbedding(String text) {
        try {
            // 使用DeepSeek生成嵌入向量
            // 注意：这里假设DeepSeek支持embedding，如果不支持，可以使用其他embedding服务
            // 比如OpenAI的text-embedding-ada-002或者本地的sentence-transformers
            
            // 简化版本：使用AI模型生成语义特征
            String prompt = String.format(
                "请为以下数据库schema信息生成语义特征向量。返回一个包含256个浮点数的数组，" +
                "用于表示该schema的语义特征。文本内容：%s", text);
            
            // 这里应该调用专门的embedding API
            // 暂时返回模拟向量
            return generateMockEmbedding(text);
            
        } catch (Exception e) {
            log.error("生成嵌入向量失败: {}", text, e);
            return generateMockEmbedding(text);
        }
    }

    /**
     * 计算两个向量的余弦相似度
     */
    public double calculateCosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("向量维度不匹配");
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
     * 生成模拟向量嵌入（用于测试）
     * 实际项目中应该替换为真实的embedding服务
     */
    private List<Double> generateMockEmbedding(String text) {
        // 基于文本内容生成确定性的模拟向量
        java.util.Random random = new java.util.Random(text.hashCode());
        List<Double> embedding = new java.util.ArrayList<>();
        
        for (int i = 0; i < 256; i++) {
            embedding.add(random.nextGaussian());
        }
        
        // 归一化向量
        double norm = embedding.stream().mapToDouble(x -> x * x).sum();
        norm = Math.sqrt(norm);
        
        if (norm > 0) {
            for (int i = 0; i < embedding.size(); i++) {
                embedding.set(i, embedding.get(i) / norm);
            }
        }
        
        return embedding;
    }

    /**
     * 为查询文本生成嵌入向量
     */
    public List<Double> generateQueryEmbedding(String queryText) {
        // 对查询文本进行预处理
        String processedQuery = preprocessQuery(queryText);
        return generateEmbedding(processedQuery);
    }

    /**
     * 预处理查询文本
     */
    private String preprocessQuery(String query) {
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
}