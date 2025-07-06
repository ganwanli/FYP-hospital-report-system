package com.hospital.report.service;

import java.util.List;
import java.util.Map;

public interface SqlExecutionService {

    Map<String, Object> executeQuery(Long templateId, Map<String, Object> parameters, Long userId);

    Map<String, Object> executeQuery(String sqlContent, Map<String, Object> parameters, String databaseType, Long userId);

    String executeQueryAsync(Long templateId, Map<String, Object> parameters, Long userId);

    String executeQueryAsync(String sqlContent, Map<String, Object> parameters, String databaseType, Long userId);

    Map<String, Object> getAsyncExecutionResult(String taskId);

    Map<String, Object> getAsyncExecutionStatus(String taskId);

    void cancelAsyncExecution(String taskId);

    List<Map<String, Object>> getExecutionHistory(Long userId, Integer limit);

    Map<String, Object> getExecutionStatistics(Long userId);

    void clearExecutionHistory(Long userId, Integer daysOld);

    Map<String, Object> validateSqlBeforeExecution(String sqlContent, String databaseType);

    Map<String, Object> explainQuery(String sqlContent, String databaseType);

    List<Map<String, Object>> getSlowQueries(Integer limit);

    void optimizeQuery(String sqlContent, String databaseType);

    Map<String, Object> getCacheStatistics();

    void clearQueryCache();

    void clearQueryCache(String pattern);

    List<Map<String, Object>> getActiveExecutions();

    Map<String, Object> getExecutionDetails(Long executionId);

    void exportExecutionResults(Long executionId, String format);
}