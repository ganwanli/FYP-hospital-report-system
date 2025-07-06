package com.hospital.report.executor;

import com.hospital.report.entity.SqlExecutionLog;
import com.hospital.report.entity.SqlTemplate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class SqlExecutor {

    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<ExecutionResult>> asyncExecutions = new ConcurrentHashMap<>();
    private final ParameterProcessor parameterProcessor;
    private final ResultConverter resultConverter;
    private final PerformanceMonitor performanceMonitor;
    private final SecurityChecker securityChecker;
    private final CacheManager cacheManager;

    public SqlExecutor(ParameterProcessor parameterProcessor, 
                      ResultConverter resultConverter,
                      PerformanceMonitor performanceMonitor,
                      SecurityChecker securityChecker,
                      CacheManager cacheManager) {
        this.parameterProcessor = parameterProcessor;
        this.resultConverter = resultConverter;
        this.performanceMonitor = performanceMonitor;
        this.securityChecker = securityChecker;
        this.cacheManager = cacheManager;
    }

    public ExecutionResult executeQuery(SqlTemplate template, Map<String, Object> parameters, Long userId) {
        return executeQuery(template.getTemplateContent(), parameters, template.getDatabaseType(), userId, template.getExecutionTimeout());
    }

    public ExecutionResult executeQuery(String sqlContent, Map<String, Object> parameters, String databaseType, Long userId) {
        return executeQuery(sqlContent, parameters, databaseType, userId, 300);
    }

    public ExecutionResult executeQuery(String sqlContent, Map<String, Object> parameters, String databaseType, Long userId, Integer timeoutSeconds) {
        SqlExecutionLog executionLog = new SqlExecutionLog();
        executionLog.setUserId(userId);
        executionLog.setSqlContent(sqlContent);
        executionLog.setParameterValues(parameterProcessor.serializeParameters(parameters));
        executionLog.setStartTime(LocalDateTime.now());
        executionLog.setDatabaseName(databaseType);
        executionLog.setIsAsync(false);

        try {
            String cacheKey = cacheManager.generateCacheKey(sqlContent, parameters);
            ExecutionResult cachedResult = cacheManager.getFromCache(cacheKey);
            if (cachedResult != null) {
                executionLog.setCacheHit(true);
                executionLog.setCacheKey(cacheKey);
                executionLog.setExecutionStatus("SUCCESS");
                executionLog.setEndTime(LocalDateTime.now());
                executionLog.setExecutionDuration(0L);
                return cachedResult;
            }

            SecurityCheckResult securityResult = securityChecker.checkSql(sqlContent, parameters);
            if (!securityResult.isValid()) {
                executionLog.setExecutionStatus("SECURITY_VIOLATION");
                executionLog.setErrorMessage(securityResult.getErrorMessage());
                executionLog.setEndTime(LocalDateTime.now());
                throw new SecurityException(securityResult.getErrorMessage());
            }

            String processedSql = parameterProcessor.processParameters(sqlContent, parameters);
            
            DataSource dataSource = getDataSource(databaseType);
            if (dataSource == null) {
                throw new RuntimeException("DataSource not found for database type: " + databaseType);
            }

            PerformanceMetrics metrics = performanceMonitor.startMonitoring();
            
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false);
                
                if (timeoutSeconds != null && timeoutSeconds > 0) {
                    connection.setNetworkTimeout(null, timeoutSeconds * 1000);
                }

                ExecutionResult result = executeWithConnection(connection, processedSql, parameters, metrics);
                
                connection.commit();
                
                result.setExecutionTime(metrics.getExecutionTime());
                result.setMemoryUsage(metrics.getMemoryUsage());
                result.setCpuUsage(metrics.getCpuUsage());
                
                cacheManager.putInCache(cacheKey, result);
                
                executionLog.setExecutionStatus("SUCCESS");
                executionLog.setResultRows((long) result.getRowCount());
                executionLog.setAffectedRows(result.getAffectedRows());
                executionLog.setExecutionDuration(metrics.getExecutionTime());
                executionLog.setMemoryUsage(metrics.getMemoryUsage());
                executionLog.setCpuUsage(metrics.getCpuUsage());
                executionLog.setCacheHit(false);
                executionLog.setCacheKey(cacheKey);
                
                return result;
                
            } catch (SQLException e) {
                executionLog.setExecutionStatus("FAILED");
                executionLog.setErrorMessage(e.getMessage());
                executionLog.setErrorCode(String.valueOf(e.getErrorCode()));
                throw new RuntimeException("SQL execution failed: " + e.getMessage(), e);
            } finally {
                performanceMonitor.stopMonitoring(metrics);
                executionLog.setEndTime(LocalDateTime.now());
                // TODO: Save execution log to database
            }

        } catch (Exception e) {
            executionLog.setExecutionStatus("ERROR");
            executionLog.setErrorMessage(e.getMessage());
            executionLog.setEndTime(LocalDateTime.now());
            throw new RuntimeException("Query execution failed: " + e.getMessage(), e);
        }
    }

    public String executeQueryAsync(String sqlContent, Map<String, Object> parameters, String databaseType, Long userId) {
        String taskId = UUID.randomUUID().toString();
        
        CompletableFuture<ExecutionResult> future = CompletableFuture.supplyAsync(() -> {
            try {
                return executeQuery(sqlContent, parameters, databaseType, userId);
            } catch (Exception e) {
                ExecutionResult errorResult = new ExecutionResult();
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                return errorResult;
            }
        });

        asyncExecutions.put(taskId, future);
        
        future.orTimeout(600, TimeUnit.SECONDS)
               .whenComplete((result, throwable) -> {
                   if (throwable != null) {
                       log.error("Async execution failed for task: {}", taskId, throwable);
                   }
                   asyncExecutions.remove(taskId);
               });

        return taskId;
    }

    public ExecutionResult getAsyncResult(String taskId) {
        CompletableFuture<ExecutionResult> future = asyncExecutions.get(taskId);
        if (future == null) {
            return null;
        }

        if (future.isDone()) {
            try {
                return future.get();
            } catch (Exception e) {
                ExecutionResult errorResult = new ExecutionResult();
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                return errorResult;
            }
        }

        return null;
    }

    public boolean isAsyncExecutionComplete(String taskId) {
        CompletableFuture<ExecutionResult> future = asyncExecutions.get(taskId);
        return future != null && future.isDone();
    }

    public void cancelAsyncExecution(String taskId) {
        CompletableFuture<ExecutionResult> future = asyncExecutions.get(taskId);
        if (future != null) {
            future.cancel(true);
            asyncExecutions.remove(taskId);
        }
    }

    public Map<String, Object> getExecutionStatus(String taskId) {
        CompletableFuture<ExecutionResult> future = asyncExecutions.get(taskId);
        Map<String, Object> status = new HashMap<>();
        
        if (future == null) {
            status.put("status", "NOT_FOUND");
            status.put("message", "Task not found");
        } else if (future.isDone()) {
            status.put("status", "COMPLETED");
            status.put("message", "Execution completed");
        } else if (future.isCancelled()) {
            status.put("status", "CANCELLED");
            status.put("message", "Execution cancelled");
        } else {
            status.put("status", "RUNNING");
            status.put("message", "Execution in progress");
        }
        
        return status;
    }

    private ExecutionResult executeWithConnection(Connection connection, String sql, Map<String, Object> parameters, PerformanceMetrics metrics) throws SQLException {
        ExecutionResult result = new ExecutionResult();
        result.setSuccess(true);
        result.setSql(sql);
        result.setParameters(parameters);
        result.setStartTime(LocalDateTime.now());

        String queryType = determineQueryType(sql);
        result.setQueryType(queryType);

        if (queryType.equals("SELECT")) {
            return executeSelectQuery(connection, sql, result, metrics);
        } else {
            return executeUpdateQuery(connection, sql, result, metrics);
        }
    }

    private ExecutionResult executeSelectQuery(Connection connection, String sql, ExecutionResult result, PerformanceMetrics metrics) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> rows = resultConverter.convertResultSet(resultSet);
                
                result.setData(rows);
                result.setRowCount(rows.size());
                result.setColumns(resultConverter.getColumnMetadata(resultSet));
                
                if (rows.size() > 1000) {
                    result.setTruncated(true);
                    result.setTotalRows(rows.size());
                    result.setData(rows.subList(0, 1000));
                    result.setRowCount(1000);
                }
                
                return result;
            }
        }
    }

    private ExecutionResult executeUpdateQuery(Connection connection, String sql, ExecutionResult result, PerformanceMetrics metrics) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int affectedRows = statement.executeUpdate();
            
            result.setAffectedRows((long) affectedRows);
            result.setRowCount(0);
            result.setData(new ArrayList<>());
            
            return result;
        }
    }

    private String determineQueryType(String sql) {
        String upperSql = sql.trim().toUpperCase();
        if (upperSql.startsWith("SELECT") || upperSql.startsWith("WITH")) {
            return "SELECT";
        } else if (upperSql.startsWith("INSERT")) {
            return "INSERT";
        } else if (upperSql.startsWith("UPDATE")) {
            return "UPDATE";
        } else if (upperSql.startsWith("DELETE")) {
            return "DELETE";
        } else if (upperSql.startsWith("CALL") || upperSql.startsWith("EXEC")) {
            return "PROCEDURE";
        } else {
            return "OTHER";
        }
    }

    private DataSource getDataSource(String databaseType) {
        return dataSourceMap.get(databaseType);
    }

    public void registerDataSource(String databaseType, DataSource dataSource) {
        dataSourceMap.put(databaseType, dataSource);
    }

    @Data
    public static class ExecutionResult {
        private boolean success;
        private String sql;
        private Map<String, Object> parameters;
        private Object data;
        private List<Map<String, Object>> columns;
        private int rowCount;
        private Long affectedRows;
        private String queryType;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long executionTime;
        private Long memoryUsage;
        private Double cpuUsage;
        private boolean truncated;
        private Integer totalRows;
        private String errorMessage;
        private String errorCode;
        private String cacheKey;
        private boolean fromCache;
    }

    @Data
    public static class SecurityCheckResult {
        private boolean valid;
        private String errorMessage;
        private String riskLevel;
        private List<String> violations;
    }

    @Data
    public static class PerformanceMetrics {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long executionTime;
        private Long memoryUsage;
        private Double cpuUsage;
        private String executionPlan;
    }
}