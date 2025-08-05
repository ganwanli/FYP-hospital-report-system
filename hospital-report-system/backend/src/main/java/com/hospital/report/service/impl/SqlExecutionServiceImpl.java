package com.hospital.report.service.impl;

import com.hospital.report.config.DynamicDataSourceManager;
import com.hospital.report.entity.DataSource;
import com.hospital.report.service.DataSourceService;
import com.hospital.report.service.SqlExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.util.Date;

@Service
@Slf4j
public class SqlExecutionServiceImpl implements SqlExecutionService {

    @Autowired
    private DynamicDataSourceManager dataSourceManager;

    @Autowired
    private DataSourceService dataSourceService;

    @Override
    public Map<String, Object> executeQuery(Long templateId, Map<String, Object> parameters, Long userId) {
        log.info("Executing SQL query for template: {}, user: {}", templateId, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", List.of());
        result.put("total", 0);
        result.put("message", "SQL execution not implemented yet");
        return result;
    }

    @Override
    public Map<String, Object> executeQuery(String sqlContent, Map<String, Object> parameters, String databaseType, Long userId) {
        log.info("Executing SQL query: {}, database: {}, user: {}", sqlContent, databaseType, userId);

        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            // 根据数据源代码获取数据源配置
            DataSource dataSourceConfig = dataSourceService.findByCode(databaseType);
            if (dataSourceConfig == null) {
                dataSourceConfig = dataSourceService.findById(databaseType);
                if (dataSourceConfig == null) {
                    log.error("Data source configuration not found: {}", databaseType);
                    result.put("success", false);
                    result.put("message", "Data source configuration not found: " + databaseType);
                    result.put("data", new ArrayList<>());
                    result.put("columns", new ArrayList<>());
                    result.put("rowCount", 0);
                    result.put("executionTime", "0.000s");
                    return result;
                }
            }

            // 获取实际的数据源连接
            javax.sql.DataSource actualDataSource = dataSourceManager.getDataSource(dataSourceConfig.getDatasourceCode());
            if (actualDataSource == null) {
                    log.error("Data source connection not found: {}", databaseType);
                    result.put("success", false);
                    result.put("message", "Data source connection not found: " + databaseType);
                    result.put("data", new ArrayList<>());
                    result.put("columns", new ArrayList<>());
                    result.put("rowCount", 0);
                    result.put("executionTime", "0.000s");
                    return result;

            }

            // 执行SQL查询
            try (Connection connection = actualDataSource.getConnection()) {
                // 处理参数化查询
                String processedSql = processSqlParameters(sqlContent, parameters);
                log.info("Processed SQL: {}", processedSql);

                try (PreparedStatement statement = connection.prepareStatement(processedSql)) {
                    // 设置参数
                    setStatementParameters(statement, parameters);

                    // 执行查询
                    boolean hasResultSet = statement.execute();

                    if (hasResultSet) {
                        // 处理查询结果
                        try (ResultSet resultSet = statement.getResultSet()) {
                            Map<String, Object> queryResult = processResultSet(resultSet);

                            long endTime = System.currentTimeMillis();
                            double executionTimeSeconds = (endTime - startTime) / 1000.0;

                            result.put("success", true);
                            result.put("data", queryResult.get("data"));
                            result.put("columns", queryResult.get("columns"));
                            result.put("rowCount", queryResult.get("rowCount"));
                            result.put("executionTime", String.format("%.3fs", executionTimeSeconds));
                            result.put("message", "Query executed successfully");

                            log.info("Query executed successfully, rows: {}, time: {}s",
                                    queryResult.get("rowCount"), String.format("%.3f", executionTimeSeconds));
                        }
                    } else {
                        // 处理更新操作
                        int updateCount = statement.getUpdateCount();
                        long endTime = System.currentTimeMillis();
                        double executionTimeSeconds = (endTime - startTime) / 1000.0;

                        result.put("success", true);
                        result.put("data", new ArrayList<>());
                        result.put("columns", new ArrayList<>());
                        result.put("rowCount", updateCount);
                        result.put("executionTime", String.format("%.3fs", executionTimeSeconds));
                        result.put("message", "Update executed successfully, affected rows: " + updateCount);

                        log.info("Update executed successfully, affected rows: {}, time: {}s",
                                updateCount, String.format("%.3f", executionTimeSeconds));
                    }
                }
            }

        } catch (SQLException e) {
            log.error("SQL execution failed", e);
            long endTime = System.currentTimeMillis();
            double executionTimeSeconds = (endTime - startTime) / 1000.0;

            result.put("success", false);
            result.put("message", "SQL execution failed: " + e.getMessage());
            result.put("data", new ArrayList<>());
            result.put("columns", new ArrayList<>());
            result.put("rowCount", 0);
            result.put("executionTime", String.format("%.3fs", executionTimeSeconds));
        } catch (Exception e) {
            log.error("Unexpected error during SQL execution", e);
            long endTime = System.currentTimeMillis();
            double executionTimeSeconds = (endTime - startTime) / 1000.0;

            result.put("success", false);
            result.put("message", "Unexpected error: " + e.getMessage());
            result.put("data", new ArrayList<>());
            result.put("columns", new ArrayList<>());
            result.put("rowCount", 0);
            result.put("executionTime", String.format("%.3fs", executionTimeSeconds));
        }

        return result;
    }

    @Override
    public String executeQueryAsync(Long templateId, Map<String, Object> parameters, Long userId) {
        log.info("Executing async SQL query for template: {}, user: {}", templateId, userId);
        return "task-" + System.currentTimeMillis();
    }

    @Override
    public String executeQueryAsync(String sqlContent, Map<String, Object> parameters, String databaseType, Long userId) {
        log.info("Executing async SQL query: {}, database: {}, user: {}", sqlContent, databaseType, userId);
        // TODO: Implement actual async execution
        return "task-" + System.currentTimeMillis();
    }

    @Override
    public Map<String, Object> getAsyncExecutionResult(String taskId) {
        log.info("Getting async execution result for task: {}", taskId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("status", "COMPLETED");
        result.put("data", List.of());
        return result;
    }

    @Override
    public Map<String, Object> getAsyncExecutionStatus(String taskId) {
        log.info("Getting async execution status for task: {}", taskId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("status", "COMPLETED");
        result.put("progress", 100);
        return result;
    }

    @Override
    public void cancelAsyncExecution(String taskId) {
        log.info("Cancelling async execution for task: {}", taskId);
    }

    @Override
    public List<Map<String, Object>> getExecutionHistory(Long userId, Integer limit) {
        log.info("Getting execution history for user: {}, limit: {}", userId, limit);
        return List.of();
    }

    @Override
    public Map<String, Object> getExecutionStatistics(Long userId) {
        log.info("Getting execution statistics for user: {}", userId);
        Map<String, Object> result = new HashMap<>();
        result.put("totalExecutions", 0);
        result.put("successfulExecutions", 0);
        result.put("failedExecutions", 0);
        result.put("averageExecutionTime", 0.0);
        return result;
    }

    @Override
    public void clearExecutionHistory(Long userId, Integer daysOld) {
        log.info("Clearing execution history for user: {}, days old: {}", userId, daysOld);
    }

    @Override
    public Map<String, Object> validateSqlBeforeExecution(String sqlContent, String databaseType) {
        log.info("Validating SQL: {}, database: {}", sqlContent, databaseType);
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("message", "SQL validation not implemented yet");
        return result;
    }

    @Override
    public Map<String, Object> explainQuery(String sqlContent, String databaseType) {
        log.info("Explaining query: {}, database: {}", sqlContent, databaseType);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("explanation", "Query explanation not implemented yet");
        return result;
    }

    @Override
    public Map<String, Object> explainSql(String sqlContent, String databaseType) {
        log.info("Explaining SQL: {}, database: {}", sqlContent, databaseType);
        Map<String, Object> result = new HashMap<>();
        result.put("plan", "SQL执行计划功能暂未实现");
        result.put("cost", 0);
        result.put("rows", 0);
        return result;
    }

    @Override
    public List<Map<String, Object>> getSlowQueries(Integer limit) {
        log.info("Getting slow queries, limit: {}", limit);
        return List.of();
    }

    @Override
    public void optimizeQuery(String sqlContent, String databaseType) {
        log.info("Optimizing query: {}, database: {}", sqlContent, databaseType);
    }

    @Override
    public Map<String, Object> getCacheStatistics() {
        log.info("Getting cache statistics");
        Map<String, Object> result = new HashMap<>();
        result.put("cacheHits", 0);
        result.put("cacheMisses", 0);
        result.put("cacheSize", 0);
        return result;
    }

    @Override
    public void clearQueryCache() {
        log.info("Clearing query cache");
    }

    @Override
    public void clearQueryCache(String pattern) {
        log.info("Clearing query cache with pattern: {}", pattern);
    }

    @Override
    public List<Map<String, Object>> getActiveExecutions() {
        log.info("Getting active executions");
        return List.of();
    }

    @Override
    public Map<String, Object> getExecutionDetails(Long executionId) {
        log.info("Getting execution details for: {}", executionId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("executionId", executionId);
        result.put("status", "COMPLETED");
        return result;
    }

    @Override
    public void exportExecutionResults(Long executionId, String format) {
        log.info("Exporting execution results: {}, format: {}", executionId, format);
    }

    /**
     * 处理SQL参数
     */
    private String processSqlParameters(String sqlContent, Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return cleanSqlContent(sqlContent);
        }

        String processedSql = cleanSqlContent(sqlContent);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String placeholder = "#{" + entry.getKey() + "}";
            if (processedSql.contains(placeholder)) {
                processedSql = processedSql.replace(placeholder, "?");
            }
        }

        return processedSql;
    }

    /**
     * 清理SQL内容，移除不必要的转义字符和格式化问题
     */
    private String cleanSqlContent(String sqlContent) {
        if (sqlContent == null || sqlContent.trim().isEmpty()) {
            return sqlContent;
        }

        String cleanedSql = sqlContent;

        // 移除常见的转义字符和格式化问题
        cleanedSql = cleanedSql.replace("\\ n", "\n");  // 修复换行符
        cleanedSql = cleanedSql.replace("\\n", "\n");   // 修复换行符
        cleanedSql = cleanedSql.replace("\\ t", "\t");  // 修复制表符
        cleanedSql = cleanedSql.replace("\\t", "\t");   // 修复制表符
        cleanedSql = cleanedSql.replace("\\ r", "\r");  // 修复回车符
        cleanedSql = cleanedSql.replace("\\r", "\r");   // 修复回车符

        // 移除字段名前的反斜杠
        cleanedSql = cleanedSql.replaceAll("\\\\ ([a-zA-Z_][a-zA-Z0-9_]*\\.[a-zA-Z_][a-zA-Z0-9_]*)", " $1");  // 修复 \ table.column
        cleanedSql = cleanedSql.replaceAll("\\\\ ([a-zA-Z_][a-zA-Z0-9_]*)", " $1");  // 修复 \ column

        // 移除多余的空格和格式化字符
        cleanedSql = cleanedSql.replaceAll("\\s+", " ");  // 将多个空格替换为单个空格
        cleanedSql = cleanedSql.trim();  // 移除首尾空格

        // 修复SQL关键字格式
        cleanedSql = cleanedSql.replaceAll("(?i)\\bFROM\\s+\\\\ n\\s+\\\\", "FROM ");
        cleanedSql = cleanedSql.replaceAll("(?i)\\bLEFT\\s+JOIN", "LEFT JOIN");
        cleanedSql = cleanedSql.replaceAll("(?i)\\bINNER\\s+JOIN", "INNER JOIN");
        cleanedSql = cleanedSql.replaceAll("(?i)\\bRIGHT\\s+JOIN", "RIGHT JOIN");

        return cleanedSql;
    }

    /**
     * 设置PreparedStatement参数
     */
    private void setStatementParameters(PreparedStatement statement, Map<String, Object> parameters) throws SQLException {
        if (parameters == null || parameters.isEmpty()) {
            return;
        }

        int paramIndex = 1;
        for (Object value : parameters.values()) {
            if (value == null) {
                statement.setNull(paramIndex, Types.NULL);
            } else if (value instanceof String) {
                statement.setString(paramIndex, (String) value);
            } else if (value instanceof Integer) {
                statement.setInt(paramIndex, (Integer) value);
            } else if (value instanceof Long) {
                statement.setLong(paramIndex, (Long) value);
            } else if (value instanceof Double) {
                statement.setDouble(paramIndex, (Double) value);
            } else if (value instanceof Boolean) {
                statement.setBoolean(paramIndex, (Boolean) value);
            } else if (value instanceof java.util.Date) {
                statement.setTimestamp(paramIndex, new Timestamp(((java.util.Date) value).getTime()));
            } else {
                statement.setString(paramIndex, value.toString());
            }
            paramIndex++;
        }
    }

    /**
     * 处理ResultSet并转换为Map格式
     */
    private Map<String, Object> processResultSet(ResultSet resultSet) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        List<String> columns = new ArrayList<>();
        List<List<Object>> data = new ArrayList<>();

        // 获取列信息
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnLabel(i));
        }

        // 获取数据行
        int rowCount = 0;
        while (resultSet.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                Object value = resultSet.getObject(i);
                // 处理特殊类型
                if (value instanceof Timestamp) {
                    value = value.toString();
                } else if (value instanceof java.sql.Date) {
                    value = value.toString();
                } else if (value instanceof Time) {
                    value = value.toString();
                }
                row.add(value);
            }
            data.add(row);
            rowCount++;

            // 限制返回行数，防止内存溢出
            if (rowCount >= 10000) {
                log.warn("Query result exceeds 10000 rows, truncating...");
                break;
            }
        }

        result.put("columns", columns);
        result.put("data", data);
        result.put("rowCount", rowCount);

        return result;
    }
}