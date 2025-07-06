package com.hospital.report.controller;

import com.hospital.report.common.Result;
import com.hospital.report.service.SqlExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sql-execution")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SqlExecutionController {

    private final SqlExecutionService sqlExecutionService;

    @PostMapping("/execute")
    public Result<Map<String, Object>> executeQuery(@RequestBody Map<String, Object> request) {
        try {
            Long templateId = request.get("templateId") != null ? Long.valueOf(request.get("templateId").toString()) : null;
            String sqlContent = (String) request.get("sqlContent");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
            String databaseType = (String) request.get("databaseType");
            Long userId = Long.valueOf(request.get("userId").toString());

            Map<String, Object> result;
            if (templateId != null) {
                result = sqlExecutionService.executeQuery(templateId, parameters, userId);
            } else {
                result = sqlExecutionService.executeQuery(sqlContent, parameters, databaseType, userId);
            }

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("SQL execution failed: " + e.getMessage());
        }
    }

    @PostMapping("/execute-async")
    public Result<String> executeQueryAsync(@RequestBody Map<String, Object> request) {
        try {
            Long templateId = request.get("templateId") != null ? Long.valueOf(request.get("templateId").toString()) : null;
            String sqlContent = (String) request.get("sqlContent");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
            String databaseType = (String) request.get("databaseType");
            Long userId = Long.valueOf(request.get("userId").toString());

            String taskId;
            if (templateId != null) {
                taskId = sqlExecutionService.executeQueryAsync(templateId, parameters, userId);
            } else {
                taskId = sqlExecutionService.executeQueryAsync(sqlContent, parameters, databaseType, userId);
            }

            return Result.success(taskId);
        } catch (Exception e) {
            return Result.error("Failed to start async execution: " + e.getMessage());
        }
    }

    @GetMapping("/async/{taskId}/result")
    public Result<Map<String, Object>> getAsyncExecutionResult(@PathVariable String taskId) {
        try {
            Map<String, Object> result = sqlExecutionService.getAsyncExecutionResult(taskId);
            if (result != null) {
                return Result.success(result);
            } else {
                return Result.error("Task not found or not completed");
            }
        } catch (Exception e) {
            return Result.error("Failed to get async result: " + e.getMessage());
        }
    }

    @GetMapping("/async/{taskId}/status")
    public Result<Map<String, Object>> getAsyncExecutionStatus(@PathVariable String taskId) {
        try {
            Map<String, Object> status = sqlExecutionService.getAsyncExecutionStatus(taskId);
            return Result.success(status);
        } catch (Exception e) {
            return Result.error("Failed to get async status: " + e.getMessage());
        }
    }

    @PostMapping("/async/{taskId}/cancel")
    public Result<Void> cancelAsyncExecution(@PathVariable String taskId) {
        try {
            sqlExecutionService.cancelAsyncExecution(taskId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to cancel async execution: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getExecutionHistory(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "50") Integer limit) {
        try {
            List<Map<String, Object>> history = sqlExecutionService.getExecutionHistory(userId, limit);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error("Failed to get execution history: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getExecutionStatistics(@RequestParam Long userId) {
        try {
            Map<String, Object> statistics = sqlExecutionService.getExecutionStatistics(userId);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error("Failed to get execution statistics: " + e.getMessage());
        }
    }

    @DeleteMapping("/history")
    public Result<Void> clearExecutionHistory(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "30") Integer daysOld) {
        try {
            sqlExecutionService.clearExecutionHistory(userId, daysOld);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to clear execution history: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    public Result<Map<String, Object>> validateSqlBeforeExecution(@RequestBody Map<String, String> request) {
        try {
            String sqlContent = request.get("sqlContent");
            String databaseType = request.get("databaseType");
            
            Map<String, Object> validationResult = sqlExecutionService.validateSqlBeforeExecution(sqlContent, databaseType);
            return Result.success(validationResult);
        } catch (Exception e) {
            return Result.error("SQL validation failed: " + e.getMessage());
        }
    }

    @PostMapping("/explain")
    public Result<Map<String, Object>> explainQuery(@RequestBody Map<String, String> request) {
        try {
            String sqlContent = request.get("sqlContent");
            String databaseType = request.get("databaseType");
            
            Map<String, Object> explanation = sqlExecutionService.explainQuery(sqlContent, databaseType);
            return Result.success(explanation);
        } catch (Exception e) {
            return Result.error("Query explanation failed: " + e.getMessage());
        }
    }

    @GetMapping("/slow-queries")
    public Result<List<Map<String, Object>>> getSlowQueries(@RequestParam(defaultValue = "20") Integer limit) {
        try {
            List<Map<String, Object>> slowQueries = sqlExecutionService.getSlowQueries(limit);
            return Result.success(slowQueries);
        } catch (Exception e) {
            return Result.error("Failed to get slow queries: " + e.getMessage());
        }
    }

    @PostMapping("/optimize")
    public Result<Void> optimizeQuery(@RequestBody Map<String, String> request) {
        try {
            String sqlContent = request.get("sqlContent");
            String databaseType = request.get("databaseType");
            
            sqlExecutionService.optimizeQuery(sqlContent, databaseType);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Query optimization failed: " + e.getMessage());
        }
    }

    @GetMapping("/cache/statistics")
    public Result<Map<String, Object>> getCacheStatistics() {
        try {
            Map<String, Object> statistics = sqlExecutionService.getCacheStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error("Failed to get cache statistics: " + e.getMessage());
        }
    }

    @PostMapping("/cache/clear")
    public Result<Void> clearQueryCache(@RequestParam(required = false) String pattern) {
        try {
            if (pattern != null && !pattern.trim().isEmpty()) {
                sqlExecutionService.clearQueryCache(pattern);
            } else {
                sqlExecutionService.clearQueryCache();
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to clear query cache: " + e.getMessage());
        }
    }

    @GetMapping("/active")
    public Result<List<Map<String, Object>>> getActiveExecutions() {
        try {
            List<Map<String, Object>> activeExecutions = sqlExecutionService.getActiveExecutions();
            return Result.success(activeExecutions);
        } catch (Exception e) {
            return Result.error("Failed to get active executions: " + e.getMessage());
        }
    }

    @GetMapping("/execution/{executionId}")
    public Result<Map<String, Object>> getExecutionDetails(@PathVariable Long executionId) {
        try {
            Map<String, Object> details = sqlExecutionService.getExecutionDetails(executionId);
            return Result.success(details);
        } catch (Exception e) {
            return Result.error("Failed to get execution details: " + e.getMessage());
        }
    }

    @PostMapping("/export/{executionId}")
    public Result<Void> exportExecutionResults(
            @PathVariable Long executionId,
            @RequestParam String format) {
        try {
            sqlExecutionService.exportExecutionResults(executionId, format);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to export execution results: " + e.getMessage());
        }
    }
}