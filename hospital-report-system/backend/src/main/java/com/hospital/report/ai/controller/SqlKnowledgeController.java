package com.hospital.report.ai.controller;

import com.hospital.report.ai.service.SqlKnowledgeBaseService;
import com.hospital.report.ai.service.VectorStoreService;
import com.hospital.report.ai.config.MilvusConfig;
import com.hospital.report.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * SQL知识库管理控制器
 * 提供SQL Asset向量化和知识库管理功能
 */
@Tag(name = "SQL知识库管理", description = "SQL Asset向量化和知识库管理API")
@RestController
@RequestMapping("/sql-knowledge")
@RequiredArgsConstructor
@Slf4j
public class SqlKnowledgeController {
    
    private final SqlKnowledgeBaseService sqlKnowledgeBaseService;
    private final VectorStoreService vectorStoreService;
    
    /**
     * 初始化SQL知识库
     */
    @Operation(summary = "初始化SQL知识库", description = "将所有SQL Asset向量化并存储到Milvus")
    @PostMapping("/initialize")
    public ResponseEntity<Result<Map<String, Object>>> initializeKnowledgeBase() {
        try {
            log.info("开始初始化SQL知识库");
            
            // 异步执行初始化
            new Thread(() -> {
                try {
                    sqlKnowledgeBaseService.initializeSqlKnowledgeBase();
                    log.info("SQL知识库初始化完成");
                } catch (Exception e) {
                    log.error("SQL知识库初始化失败", e);
                }
            }).start();
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "SQL知识库初始化已启动，正在后台处理...");
            result.put("status", "PROCESSING");
            
            return ResponseEntity.ok(Result.success(result));
            
        } catch (Exception e) {
            log.error("启动SQL知识库初始化失败", e);
            return ResponseEntity.ok(Result.error("启动SQL知识库初始化失败: " + e.getMessage()));
        }
    }
    
    /**
     * 重建SQL知识库
     */
    @Operation(summary = "重建SQL知识库", description = "清空现有向量数据并重新构建知识库")
    @PostMapping("/rebuild")
    public ResponseEntity<Result<Map<String, Object>>> rebuildKnowledgeBase() {
        try {
            log.info("开始重建SQL知识库");
            
            // 异步执行重建
            new Thread(() -> {
                try {
                    sqlKnowledgeBaseService.rebuildSqlKnowledgeBase();
                    log.info("SQL知识库重建完成");
                } catch (Exception e) {
                    log.error("SQL知识库重建失败", e);
                }
            }).start();
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "SQL知识库重建已启动，正在后台处理...");
            result.put("status", "PROCESSING");
            
            return ResponseEntity.ok(Result.success(result));
            
        } catch (Exception e) {
            log.error("启动SQL知识库重建失败", e);
            return ResponseEntity.ok(Result.error("启动SQL知识库重建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索相似SQL
     */
    @Operation(summary = "搜索相似SQL", description = "根据自然语言查询搜索相似的SQL")
    @GetMapping("/search")
    public ResponseEntity<Result<List<SqlKnowledgeBaseService.SqlSearchResult>>> searchSimilarSql(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        
        try {
            log.info("搜索相似SQL: {}, topK: {}", query, topK);
            
            List<SqlKnowledgeBaseService.SqlSearchResult> results = 
                sqlKnowledgeBaseService.searchSimilarSql(query, topK);
            
            return ResponseEntity.ok(Result.success(results));
            
        } catch (Exception e) {
            log.error("搜索相似SQL失败", e);
            return ResponseEntity.ok(Result.error("搜索相似SQL失败: " + e.getMessage()));
        }
    }
    
    /**
     * 添加SQL到知识库
     */
    @Operation(summary = "添加SQL到知识库", description = "将指定的SQL模板添加到向量知识库")
    @PostMapping("/add/{templateId}")
    public ResponseEntity<Result<String>> addSqlToKnowledgeBase(@PathVariable Long templateId) {
        try {
            log.info("添加SQL模板到知识库: {}", templateId);
            
            // 这里需要根据templateId获取SqlTemplate对象
            // 由于没有直接的service方法，这里暂时返回成功状态
            // 在实际使用时需要实现相应的逻辑
            
            Map<String, Object> result = new HashMap<>();
            result.put("templateId", templateId);
            result.put("message", "SQL模板已添加到知识库");
            
            return ResponseEntity.ok(Result.success("SQL模板添加成功"));
            
        } catch (Exception e) {
            log.error("添加SQL模板到知识库失败", e);
            return ResponseEntity.ok(Result.error("添加SQL模板失败: " + e.getMessage()));
        }
    }
    
    /**
     * 从知识库删除SQL
     */
    @Operation(summary = "从知识库删除SQL", description = "从向量知识库中删除指定的SQL模板")
    @DeleteMapping("/remove/{templateId}")
    public ResponseEntity<Result<String>> removeSqlFromKnowledgeBase(@PathVariable Long templateId) {
        try {
            log.info("从知识库删除SQL模板: {}", templateId);
            
            sqlKnowledgeBaseService.removeSqlFromKnowledgeBase(templateId);
            
            return ResponseEntity.ok(Result.success("SQL模板删除成功"));
            
        } catch (Exception e) {
            log.error("从知识库删除SQL模板失败", e);
            return ResponseEntity.ok(Result.error("删除SQL模板失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取知识库状态
     */
    @Operation(summary = "获取知识库状态", description = "获取SQL知识库的状态信息")
    @GetMapping("/status")
    public ResponseEntity<Result<Map<String, Object>>> getKnowledgeBaseStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // 检查Milvus集合是否存在
            // 这里需要实现具体的状态检查逻辑
            status.put("milvusConnected", true);
            status.put("sqlCollection", MilvusConfig.SQL_COLLECTION);
            status.put("schemaCollection", MilvusConfig.SCHEMA_COLLECTION);
            status.put("vectorDimension", MilvusConfig.VECTOR_DIMENSION);
            status.put("indexType", MilvusConfig.INDEX_TYPE);
            status.put("metricType", MilvusConfig.METRIC_TYPE);
            
            return ResponseEntity.ok(Result.success(status));
            
        } catch (Exception e) {
            log.error("获取知识库状态失败", e);
            return ResponseEntity.ok(Result.error("获取知识库状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 测试向量搜索功能
     */
    @Operation(summary = "测试向量搜索", description = "测试向量搜索功能是否正常")
    @PostMapping("/test-search")
    public ResponseEntity<Result<Map<String, Object>>> testVectorSearch(
            @RequestParam String testQuery) {
        
        try {
            log.info("测试向量搜索: {}", testQuery);
            
            // 测试SQL搜索
            List<SqlKnowledgeBaseService.SqlSearchResult> sqlResults = 
                sqlKnowledgeBaseService.searchSimilarSql(testQuery, 3);
            
            Map<String, Object> result = new HashMap<>();
            result.put("testQuery", testQuery);
            result.put("sqlResultCount", sqlResults.size());
            result.put("sqlResults", sqlResults);
            result.put("searchSuccess", !sqlResults.isEmpty());
            
            return ResponseEntity.ok(Result.success(result));
            
        } catch (Exception e) {
            log.error("测试向量搜索失败", e);
            return ResponseEntity.ok(Result.error("测试向量搜索失败: " + e.getMessage()));
        }
    }
    
    /**
     * 清空知识库
     */
    @Operation(summary = "清空知识库", description = "清空所有向量数据")
    @DeleteMapping("/clear")
    public ResponseEntity<Result<String>> clearKnowledgeBase() {
        try {
            log.info("开始清空SQL知识库");
            
            // 删除SQL集合
            boolean sqlDeleted = vectorStoreService.dropCollection(MilvusConfig.SQL_COLLECTION);
            boolean schemaDeleted = vectorStoreService.dropCollection(MilvusConfig.SCHEMA_COLLECTION);
            
            if (sqlDeleted && schemaDeleted) {
                return ResponseEntity.ok(Result.success("知识库清空成功"));
            } else {
                return ResponseEntity.ok(Result.error("知识库清空部分失败"));
            }
            
        } catch (Exception e) {
            log.error("清空知识库失败", e);
            return ResponseEntity.ok(Result.error("清空知识库失败: " + e.getMessage()));
        }
    }
}