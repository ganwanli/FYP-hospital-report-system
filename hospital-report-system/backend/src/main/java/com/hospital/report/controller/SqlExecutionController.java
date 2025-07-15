package com.hospital.report.controller;

import com.hospital.report.common.Result;
import com.hospital.report.service.SqlExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/sql-execution")
@Tag(name = "SQL执行管理", description = "SQL执行相关接口")
@CrossOrigin(origins = "*")
public class SqlExecutionController {

    @Autowired
    private SqlExecutionService sqlExecutionService;

    @PostMapping("/execute")
    @Operation(summary = "执行SQL查询", description = "同步执行SQL查询并返回结果")
    public Result<Map<String, Object>> executeQuery(@RequestBody Map<String, Object> request) {
        try {
            String sqlContent = (String) request.get("sqlContent");
            String databaseType = (String) request.get("databaseType");
            Map<String, Object> parameters = (Map<String, Object>) request.getOrDefault("parameters", Map.of());
            Long userId = request.get("userId") != null ? 
                Long.valueOf(request.get("userId").toString()) : 1L;

            log.info("Executing SQL query: {}, database: {}, user: {}", 
                    sqlContent, databaseType, userId);

            Map<String, Object> result = sqlExecutionService.executeQuery(
                    sqlContent, parameters, databaseType, userId);

            if ((Boolean) result.getOrDefault("success", false)) {
                return Result.success(result);
            } else {
                return Result.error((String) result.get("message"));
            }
        } catch (Exception e) {
            log.error("Failed to execute SQL query", e);
            return Result.error("SQL执行失败: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "验证SQL语句", description = "验证SQL语句的语法正确性")
    public Result<Map<String, Object>> validateSql(@RequestBody Map<String, Object> request) {
        try {
            String sqlContent = (String) request.get("sqlContent");
            String databaseType = (String) request.get("databaseType");

            log.info("Validating SQL: {}, database: {}", sqlContent, databaseType);

            Map<String, Object> result = sqlExecutionService.validateSqlBeforeExecution(sqlContent, databaseType);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to validate SQL", e);
            return Result.error("SQL验证失败: " + e.getMessage());
        }
    }
}
