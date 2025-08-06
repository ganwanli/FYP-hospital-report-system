package com.hospital.report.ai.controller;

import com.hospital.report.ai.service.DatabaseSchemaService;
import com.hospital.report.ai.service.NaturalLanguageToSqlService;
import com.hospital.report.ai.service.VectorSearchService;
import com.hospital.report.ai.service.VectorStoreService;
import com.hospital.report.common.Result;
import com.hospital.report.entity.DataSource;
import com.hospital.report.service.DataSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI向量化数据库Schema控制器
 */
@RestController
@RequestMapping("/ai-schema")
@Slf4j
public class DatabaseSchemaController {

    @Autowired
    private DatabaseSchemaService databaseSchemaService;
    
    @Autowired
    private DataSourceService dataSourceService;
    
    @Autowired
    private VectorSearchService vectorSearchService;
    
    @Autowired
    private NaturalLanguageToSqlService nlToSqlService;

    /**
     * 为指定数据源创建或更新schema向量
     */
    @PostMapping("/build-vectors/{datasourceId}")
    public Result<String> buildSchemaVectors(@PathVariable Long datasourceId) {
        try {
            log.info("开始为数据源 {} 构建schema向量", datasourceId);
            
            DataSource dataSource = dataSourceService.getById(datasourceId);
            if (dataSource == null) {
                return Result.error("数据源不存在: " + datasourceId);
            }
            
            databaseSchemaService.createOrUpdateSchemaVectors(dataSource);
            
            return Result.success("Schema向量构建成功");
            
        } catch (Exception e) {
            log.error("构建schema向量失败: {}", e.getMessage(), e);
            return Result.error("构建schema向量失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据源的schema统计信息
     */
    @GetMapping("/statistics/{datasourceId}")
    public Result<VectorStoreService.SchemaStatistics> getSchemaStatistics(@PathVariable Long datasourceId) {
        try {
            VectorStoreService.SchemaStatistics stats = databaseSchemaService.getSchemaStatistics(datasourceId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取schema统计信息失败: {}", e.getMessage(), e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查数据源是否已有schema数据
     */
    @GetMapping("/check/{datasourceId}")
    public Result<Boolean> checkSchemaExists(@PathVariable Long datasourceId) {
        try {
            boolean exists = databaseSchemaService.hasSchemaData(datasourceId);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查schema数据失败: {}", e.getMessage(), e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }

    /**
     * 向量搜索相关schema
     */
    @PostMapping("/search")
    public Result<?> searchSchemas(@RequestBody Map<String, Object> request) {
        try {
            String query = (String) request.get("query");
            Long datasourceId = Long.valueOf(request.get("datasourceId").toString());
            Integer topK = request.containsKey("topK") ? 
                Integer.valueOf(request.get("topK").toString()) : 10;
            
            if (query == null || query.trim().isEmpty()) {
                return Result.error("查询内容不能为空");
            }
            
            var results = vectorSearchService.searchRelevantSchemas(query, datasourceId, topK);
            return Result.success(results);
            
        } catch (Exception e) {
            log.error("向量搜索失败: {}", e.getMessage(), e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 自然语言转SQL
     */
    @PostMapping("/nl-to-sql")
    public Result<NaturalLanguageToSqlService.SqlGenerationResult> naturalLanguageToSql(
            @RequestBody Map<String, Object> request) {
        try {
            String query = (String) request.get("query");
            Long datasourceId = Long.valueOf(request.get("datasourceId").toString());
            
            if (query == null || query.trim().isEmpty()) {
                return Result.error("查询内容不能为空");
            }
            
            if (!databaseSchemaService.hasSchemaData(datasourceId)) {
                return Result.error("该数据源尚未构建schema向量，请先执行向量构建");
            }
            
            NaturalLanguageToSqlService.SqlGenerationResult result = 
                nlToSqlService.generateSql(query, datasourceId);
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("自然语言转SQL失败: {}", e.getMessage(), e);
            return Result.error("转换失败: " + e.getMessage());
        }
    }
}