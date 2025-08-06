package com.hospital.report.ai.controller;

import com.hospital.report.ai.service.EmbeddingService;
import com.hospital.report.ai.service.RAGService;
import com.hospital.report.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LangChain4J测试控制器
 */
@RestController
@RequestMapping("/langchain4j")
@Tag(name = "LangChain4J测试", description = "LangChain4J集成测试接口")
@Slf4j
public class LangChain4JTestController {

    @Autowired
    private EmbeddingService embeddingService;
    
    @Autowired
    private RAGService ragService;

    /**
     * 测试千问向量模型连接
     */
    @GetMapping("/test-embedding")
    @Operation(summary = "测试千问向量模型")
    public Result<Map<String, Object>> testEmbedding() {
        try {
            log.info("测试千问向量模型连接");
            
            // 测试生成向量
            List<Double> embedding = embeddingService.generateEmbeddingDouble("这是一个测试文本，用于验证千问向量模型是否正常工作");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", !embedding.isEmpty());
            result.put("dimension", embedding.size());
            result.put("sampleVector", embedding.subList(0, Math.min(5, embedding.size())));
            
            if (embedding.isEmpty()) {
                return Result.error("千问向量模型测试失败");
            }
            
            return Result.success("千问向量模型测试成功", result);
            
        } catch (Exception e) {
            log.error("测试千问向量模型失败", e);
            return Result.error("千问向量模型测试异常: " + e.getMessage());
        }
    }

    /**
     * 测试RAG功能
     */
    @PostMapping("/test-rag/{datasourceId}")
    @Operation(summary = "测试RAG功能")
    public Result<Map<String, Object>> testRAG(@PathVariable Long datasourceId) {
        try {
            log.info("测试RAG功能，数据源ID: {}", datasourceId);
            
            boolean success = ragService.testRAGFunction(datasourceId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("datasourceId", datasourceId);
            
            if (success) {
                return Result.success("RAG功能测试成功", result);
            } else {
                return Result.error("RAG功能测试失败");
            }
            
        } catch (Exception e) {
            log.error("RAG功能测试失败", e);
            return Result.error("RAG功能测试异常: " + e.getMessage());
        }
    }

    /**
     * 测试RAG生成SQL
     */
    @PostMapping("/test-rag-sql")
    @Operation(summary = "测试RAG生成SQL")
    public Result<String> testRAGSQL(@RequestParam String query, @RequestParam Long datasourceId) {
        try {
            log.info("测试RAG生成SQL，查询: {}, 数据源: {}", query, datasourceId);
            
            String sqlResult = ragService.generateSQLWithRAG(query, datasourceId);
            
            return Result.success("RAG生成SQL成功", sqlResult);
            
        } catch (Exception e) {
            log.error("RAG生成SQL失败", e);
            return Result.error("RAG生成SQL异常: " + e.getMessage());
        }
    }

    /**
     * 批量测试向量生成性能
     */
    @PostMapping("/test-batch-embedding")
    @Operation(summary = "测试批量向量生成")
    public Result<Map<String, Object>> testBatchEmbedding(@RequestBody List<String> texts) {
        try {
            log.info("测试批量向量生成，文本数量: {}", texts.size());
            
            long startTime = System.currentTimeMillis();
            List<List<Double>> embeddings = embeddingService.generateBatchEmbeddingsDouble(texts);
            long endTime = System.currentTimeMillis();
            
            Map<String, Object> result = new HashMap<>();
            result.put("inputCount", texts.size());
            result.put("outputCount", embeddings.size());
            result.put("duration", endTime - startTime);
            result.put("averageTime", (double)(endTime - startTime) / texts.size());
            
            if (!embeddings.isEmpty()) {
                result.put("dimension", embeddings.get(0).size());
            }
            
            return Result.success("批量向量生成测试成功", result);
            
        } catch (Exception e) {
            log.error("批量向量生成测试失败", e);
            return Result.error("批量向量生成测试异常: " + e.getMessage());
        }
    }

    /**
     * 测试向量相似度计算
     */
    @PostMapping("/test-similarity")
    @Operation(summary = "测试向量相似度计算")
    public Result<Map<String, Object>> testSimilarity(@RequestParam String text1, @RequestParam String text2) {
        try {
            log.info("测试向量相似度计算: '{}' vs '{}'", text1, text2);
            
            List<Double> embedding1 = embeddingService.generateEmbeddingDouble(text1);
            List<Double> embedding2 = embeddingService.generateEmbeddingDouble(text2);
            
            if (embedding1.isEmpty() || embedding2.isEmpty()) {
                return Result.error("向量生成失败");
            }
            
            double similarity = embeddingService.calculateCosineSimilarity(embedding1, embedding2);
            
            Map<String, Object> result = new HashMap<>();
            result.put("text1", text1);
            result.put("text2", text2);
            result.put("similarity", similarity);
            result.put("dimension", embedding1.size());
            
            return Result.success("向量相似度计算成功", result);
            
        } catch (Exception e) {
            log.error("向量相似度计算失败", e);
            return Result.error("向量相似度计算异常: " + e.getMessage());
        }
    }
}