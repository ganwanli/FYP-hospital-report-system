package com.hospital.report.sync;

import com.hospital.report.annotation.DataSource;
import com.hospital.report.config.DynamicDataSourceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataExtractor {

    private final DynamicDataSourceManager dataSourceManager;

    public List<Map<String, Object>> extractData(SyncContext context) throws SQLException {
        List<Map<String, Object>> data = new ArrayList<>();
        
        String dataSourceKey = "ds_" + context.getSourceDatasourceId();
        try (Connection connection = dataSourceManager.getDataSource(dataSourceKey).getConnection()) {
            
            String sql = buildExtractSql(context);
            log.info("执行数据抽取SQL: {}", sql);
            
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (rs.next()) {
                    if (context.isCancelled()) {
                        log.info("同步任务已取消，停止数据抽取");
                        break;
                    }
                    
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    data.add(row);
                    
                    // 批量抽取，避免内存溢出
                    if (data.size() >= context.getBatchSize()) {
                        break;
                    }
                }
            }
        }
        
        log.info("抽取数据完成，共 {} 条记录", data.size());
        return data;
    }

    public long countSourceData(SyncContext context) throws SQLException {
        String dataSourceKey = "ds_" + context.getSourceDatasourceId();
        try (Connection connection = dataSourceManager.getDataSource(dataSourceKey).getConnection()) {
            
            String sql = buildCountSql(context);
            log.debug("执行数据统计SQL: {}", sql);
            
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    private String buildExtractSql(SyncContext context) {
        StringBuilder sql = new StringBuilder();
        
        if (context.getSourceSql() != null && !context.getSourceSql().trim().isEmpty()) {
            // 使用自定义SQL
            sql.append(context.getSourceSql());
        } else {
            // 使用表名构建SQL
            sql.append("SELECT * FROM ").append(context.getSourceTable());
        }
        
        // 添加增量条件
        String incrementalCondition = buildIncrementalCondition(context);
        if (incrementalCondition != null) {
            if (sql.toString().toUpperCase().contains("WHERE")) {
                sql.append(" AND ").append(incrementalCondition);
            } else {
                sql.append(" WHERE ").append(incrementalCondition);
            }
        }
        
        // 添加过滤条件
        if (context.getFilterCondition() != null && !context.getFilterCondition().trim().isEmpty()) {
            if (sql.toString().toUpperCase().contains("WHERE")) {
                sql.append(" AND (").append(context.getFilterCondition()).append(")");
            } else {
                sql.append(" WHERE ").append(context.getFilterCondition());
            }
        }
        
        // 添加排序
        if (context.getIncrementalColumn() != null) {
            sql.append(" ORDER BY ").append(context.getIncrementalColumn());
        }
        
        // 添加限制
        if (context.getBatchSize() != null && context.getBatchSize() > 0) {
            sql.append(" LIMIT ").append(context.getBatchSize());
        }
        
        return sql.toString();
    }

    private String buildCountSql(SyncContext context) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        
        if (context.getSourceSql() != null && !context.getSourceSql().trim().isEmpty()) {
            // 使用自定义SQL的子查询
            sql.append("(").append(context.getSourceSql()).append(") AS temp_table");
        } else {
            // 使用表名
            sql.append(context.getSourceTable());
            
            // 添加增量条件
            String incrementalCondition = buildIncrementalCondition(context);
            if (incrementalCondition != null) {
                sql.append(" WHERE ").append(incrementalCondition);
            }
            
            // 添加过滤条件
            if (context.getFilterCondition() != null && !context.getFilterCondition().trim().isEmpty()) {
                if (incrementalCondition != null) {
                    sql.append(" AND (").append(context.getFilterCondition()).append(")");
                } else {
                    sql.append(" WHERE ").append(context.getFilterCondition());
                }
            }
        }
        
        return sql.toString();
    }

    private String buildIncrementalCondition(SyncContext context) {
        if ("FULL".equals(context.getSyncMode()) || 
            context.getIncrementalColumn() == null || 
            context.getLastSyncValue() == null) {
            return null;
        }
        
        String column = context.getIncrementalColumn();
        String lastValue = context.getLastSyncValue();
        String incrementalType = context.getIncrementalType();
        
        if ("TIMESTAMP".equals(incrementalType)) {
            return column + " > '" + lastValue + "'";
        } else if ("NUMBER".equals(incrementalType)) {
            return column + " > " + lastValue;
        } else {
            return column + " > '" + lastValue + "'";
        }
    }

    public void writeData(SyncContext context, List<Map<String, Object>> data) throws SQLException {
        if (data == null || data.isEmpty()) {
            return;
        }
        
        String dataSourceKey = "ds_" + context.getTargetDatasourceId();
        try (Connection connection = dataSourceManager.getDataSource(dataSourceKey).getConnection()) {
            
            if (context.getEnableTransaction()) {
                connection.setAutoCommit(false);
            }
            
            try {
                if (context.getTargetSql() != null && !context.getTargetSql().trim().isEmpty()) {
                    // 使用自定义SQL
                    executeCustomSql(connection, context, data);
                } else {
                    // 使用INSERT语句
                    executeInsertSql(connection, context, data);
                }
                
                if (context.getEnableTransaction()) {
                    connection.commit();
                }
                
                context.setSuccessCount((context.getSuccessCount() == null ? 0 : context.getSuccessCount()) + data.size());
                
            } catch (SQLException e) {
                if (context.getEnableTransaction()) {
                    connection.rollback();
                }
                throw e;
            }
        }
    }

    private void executeInsertSql(Connection connection, SyncContext context, List<Map<String, Object>> data) throws SQLException {
        if (data.isEmpty()) return;
        
        Map<String, Object> firstRow = data.get(0);
        Set<String> columns = firstRow.keySet();
        
        // 应用字段映射
        Set<String> targetColumns = applyFieldMapping(columns, context.getFieldMapping());
        
        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(context.getTargetTable())
                .append(" (")
                .append(String.join(", ", targetColumns))
                .append(") VALUES (")
                .append(String.join(", ", Collections.nCopies(targetColumns.size(), "?")))
                .append(")");
        
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (Map<String, Object> row : data) {
                if (context.isCancelled()) {
                    break;
                }
                
                int paramIndex = 1;
                for (String column : columns) {
                    Object value = row.get(column);
                    stmt.setObject(paramIndex++, value);
                }
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }

    private void executeCustomSql(Connection connection, SyncContext context, List<Map<String, Object>> data) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(context.getTargetSql())) {
            for (Map<String, Object> row : data) {
                if (context.isCancelled()) {
                    break;
                }
                
                // 设置参数（需要根据SQL中的占位符设置）
                // 这里简化处理，实际应用中需要更复杂的参数映射
                int paramIndex = 1;
                for (Object value : row.values()) {
                    stmt.setObject(paramIndex++, value);
                }
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }

    private Set<String> applyFieldMapping(Set<String> sourceColumns, Map<String, String> fieldMapping) {
        if (fieldMapping == null || fieldMapping.isEmpty()) {
            return sourceColumns;
        }
        
        Set<String> targetColumns = new LinkedHashSet<>();
        for (String sourceColumn : sourceColumns) {
            String targetColumn = fieldMapping.getOrDefault(sourceColumn, sourceColumn);
            targetColumns.add(targetColumn);
        }
        return targetColumns;
    }
}