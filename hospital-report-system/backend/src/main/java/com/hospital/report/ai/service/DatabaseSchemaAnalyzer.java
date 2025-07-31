package com.hospital.report.ai.service;

import com.hospital.report.ai.entity.dto.DatabaseSchemaInfo;
import com.hospital.report.entity.DataSource;
import com.hospital.report.service.DataSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.util.*;

@Service
@Slf4j
public class DatabaseSchemaAnalyzer {
    
    private final DataSourceService dataSourceService;
    
    public DatabaseSchemaAnalyzer(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }
    
    public DatabaseSchemaInfo analyzeDatabaseSchema(Long datasourceId) {
        DataSource dataSource = dataSourceService.getById(datasourceId);
        if (dataSource == null) {
            throw new RuntimeException("数据源不存在，ID: " + datasourceId);
        }
        
        try (Connection connection = createConnection(dataSource)) {
            DatabaseSchemaInfo schemaInfo = new DatabaseSchemaInfo();
            
            // 获取数据库基本信息
            DatabaseMetaData metaData = connection.getMetaData();
            schemaInfo.setDatabaseName(dataSource.getDatabaseName());
            schemaInfo.setDatabaseType(dataSource.getDatabaseType());
            schemaInfo.setVersion(metaData.getDatabaseProductVersion());
            
            // 获取所有表信息
            List<DatabaseSchemaInfo.TableInfo> tables = getTableInfos(metaData, connection, dataSource);
            schemaInfo.setTables(tables);
            
            // 获取表之间的关系
            List<DatabaseSchemaInfo.RelationshipInfo> relationships = getRelationships(metaData, dataSource);
            schemaInfo.setRelationships(relationships);
            
            // 统计信息
            schemaInfo.setTotalTables(tables.size());
            schemaInfo.setTotalColumns(tables.stream().mapToInt(t -> t.getColumns().size()).sum());
            schemaInfo.setTotalIndexes(tables.stream().mapToInt(t -> t.getIndexes().size()).sum());
            
            log.info("成功分析数据库结构，数据源ID: {}, 表数量: {}", datasourceId, tables.size());
            return schemaInfo;
            
        } catch (SQLException e) {
            log.error("分析数据库结构失败，数据源ID: {}", datasourceId, e);
            throw new RuntimeException("数据库结构分析失败: " + e.getMessage(), e);
        }
    }
    
    private List<DatabaseSchemaInfo.TableInfo> getTableInfos(DatabaseMetaData metaData, Connection connection, DataSource dataSource) throws SQLException {
        List<DatabaseSchemaInfo.TableInfo> tables = new ArrayList<>();
        
        try (ResultSet tableRs = metaData.getTables(dataSource.getDatabaseName(), null, null, new String[]{"TABLE"})) {
            while (tableRs.next()) {
                DatabaseSchemaInfo.TableInfo tableInfo = new DatabaseSchemaInfo.TableInfo();
                String tableName = tableRs.getString("TABLE_NAME");
                
                tableInfo.setTableName(tableName);
                tableInfo.setTableComment(tableRs.getString("REMARKS"));
                tableInfo.setTableType(tableRs.getString("TABLE_TYPE"));
                
                // 获取表行数（仅对MySQL有效）
                if ("MySQL".equalsIgnoreCase(dataSource.getDatabaseType())) {
                    tableInfo.setRowCount(getTableRowCount(connection, tableName));
                }
                
                // 获取列信息
                List<DatabaseSchemaInfo.ColumnInfo> columns = getColumnInfos(metaData, dataSource, tableName);
                tableInfo.setColumns(columns);
                
                // 获取主键信息
                List<String> primaryKeys = getPrimaryKeys(metaData, dataSource, tableName);
                tableInfo.setPrimaryKeys(primaryKeys);
                
                // 获取索引信息
                List<DatabaseSchemaInfo.IndexInfo> indexes = getIndexInfos(metaData, dataSource, tableName);
                tableInfo.setIndexes(indexes);
                
                tables.add(tableInfo);
            }
        }
        
        return tables;
    }
    
    private List<DatabaseSchemaInfo.ColumnInfo> getColumnInfos(DatabaseMetaData metaData, DataSource dataSource, String tableName) throws SQLException {
        List<DatabaseSchemaInfo.ColumnInfo> columns = new ArrayList<>();
        
        try (ResultSet columnRs = metaData.getColumns(dataSource.getDatabaseName(), null, tableName, null)) {
            while (columnRs.next()) {
                DatabaseSchemaInfo.ColumnInfo columnInfo = new DatabaseSchemaInfo.ColumnInfo();
                
                columnInfo.setColumnName(columnRs.getString("COLUMN_NAME"));
                columnInfo.setDataType(columnRs.getString("TYPE_NAME"));
                columnInfo.setColumnSize(columnRs.getInt("COLUMN_SIZE"));
                columnInfo.setDecimalDigits(columnRs.getInt("DECIMAL_DIGITS"));
                columnInfo.setNullable(columnRs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                columnInfo.setDefaultValue(columnRs.getString("COLUMN_DEF"));
                columnInfo.setComment(columnRs.getString("REMARKS"));
                
                // 检查是否为自增列（MySQL特定）
                String isAutoIncrement = columnRs.getString("IS_AUTOINCREMENT");
                columnInfo.setAutoIncrement("YES".equalsIgnoreCase(isAutoIncrement));
                
                columns.add(columnInfo);
            }
        }
        
        return columns;
    }
    
    private List<String> getPrimaryKeys(DatabaseMetaData metaData, DataSource dataSource, String tableName) throws SQLException {
        List<String> primaryKeys = new ArrayList<>();
        
        try (ResultSet pkRs = metaData.getPrimaryKeys(dataSource.getDatabaseName(), null, tableName)) {
            while (pkRs.next()) {
                primaryKeys.add(pkRs.getString("COLUMN_NAME"));
            }
        }
        
        return primaryKeys;
    }
    
    private List<DatabaseSchemaInfo.IndexInfo> getIndexInfos(DatabaseMetaData metaData, DataSource dataSource, String tableName) throws SQLException {
        List<DatabaseSchemaInfo.IndexInfo> indexes = new ArrayList<>();
        Map<String, DatabaseSchemaInfo.IndexInfo> indexMap = new HashMap<>();
        
        try (ResultSet indexRs = metaData.getIndexInfo(dataSource.getDatabaseName(), null, tableName, false, false)) {
            while (indexRs.next()) {
                String indexName = indexRs.getString("INDEX_NAME");
                if (indexName == null || "PRIMARY".equals(indexName)) {
                    continue; // 跳过主键索引
                }
                
                DatabaseSchemaInfo.IndexInfo indexInfo = indexMap.computeIfAbsent(indexName, k -> {
                    DatabaseSchemaInfo.IndexInfo info = new DatabaseSchemaInfo.IndexInfo();
                    info.setIndexName(k);
                    try {
                        info.setUnique(!indexRs.getBoolean("NON_UNIQUE"));
                        info.setIndexType(String.valueOf(indexRs.getShort("TYPE")));
                    } catch (SQLException e) {
                        log.warn("读取索引信息失败: {}", k, e);
                    }
                    info.setColumns(new ArrayList<>());
                    return info;
                });
                
                String columnName = indexRs.getString("COLUMN_NAME");
                if (columnName != null) {
                    indexInfo.getColumns().add(columnName);
                }
            }
        }
        
        indexes.addAll(indexMap.values());
        return indexes;
    }
    
    private List<DatabaseSchemaInfo.RelationshipInfo> getRelationships(DatabaseMetaData metaData, DataSource dataSource) throws SQLException {
        List<DatabaseSchemaInfo.RelationshipInfo> relationships = new ArrayList<>();
        
        try (ResultSet tables = metaData.getTables(dataSource.getDatabaseName(), null, null, new String[]{"TABLE"})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                
                try (ResultSet foreignKeys = metaData.getImportedKeys(dataSource.getDatabaseName(), null, tableName)) {
                    while (foreignKeys.next()) {
                        DatabaseSchemaInfo.RelationshipInfo relationship = new DatabaseSchemaInfo.RelationshipInfo();
                        
                        relationship.setFromTable(foreignKeys.getString("FKTABLE_NAME"));
                        relationship.setFromColumn(foreignKeys.getString("FKCOLUMN_NAME"));
                        relationship.setToTable(foreignKeys.getString("PKTABLE_NAME"));
                        relationship.setToColumn(foreignKeys.getString("PKCOLUMN_NAME"));
                        relationship.setConstraintName(foreignKeys.getString("FK_NAME"));
                        relationship.setRelationshipType("MANY_TO_ONE");
                        
                        relationships.add(relationship);
                    }
                }
            }
        }
        
        return relationships;
    }
    
    private Long getTableRowCount(Connection connection, String tableName) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM `" + tableName + "`")) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            log.debug("获取表行数失败: {}", tableName, e);
        }
        return null;
    }
    
    public String generateSchemaDescription(DatabaseSchemaInfo schemaInfo) {
        StringBuilder description = new StringBuilder();
        
        description.append("# 数据库结构分析报告\n\n");
        description.append("## 基本信息\n");
        description.append("- **数据库名称**: ").append(schemaInfo.getDatabaseName()).append("\n");
        description.append("- **数据库类型**: ").append(schemaInfo.getDatabaseType()).append("\n");
        if (StringUtils.hasText(schemaInfo.getVersion())) {
            description.append("- **版本**: ").append(schemaInfo.getVersion()).append("\n");
        }
        description.append("- **表数量**: ").append(schemaInfo.getTotalTables()).append("\n");
        description.append("- **总列数**: ").append(schemaInfo.getTotalColumns()).append("\n");
        description.append("- **总索引数**: ").append(schemaInfo.getTotalIndexes()).append("\n\n");
        
        description.append("## 表结构详情\n");
        
        for (DatabaseSchemaInfo.TableInfo table : schemaInfo.getTables()) {
            description.append("\n### ").append(table.getTableName());
            if (StringUtils.hasText(table.getTableComment())) {
                description.append(" (").append(table.getTableComment()).append(")");
            }
            description.append("\n\n");
            
            if (table.getRowCount() != null) {
                description.append("**行数**: ").append(table.getRowCount()).append("\n\n");
            }
            
            description.append("**字段列表**:\n");
            for (DatabaseSchemaInfo.ColumnInfo column : table.getColumns()) {
                description.append("- **").append(column.getColumnName()).append("**");
                description.append(" `").append(column.getDataType());
                
                if (column.getColumnSize() > 0) {
                    description.append("(").append(column.getColumnSize());
                    if (column.getDecimalDigits() > 0) {
                        description.append(",").append(column.getDecimalDigits());
                    }
                    description.append(")");
                }
                description.append("`");
                
                List<String> attributes = new ArrayList<>();
                if (!column.isNullable()) attributes.add("NOT NULL");
                if (column.isAutoIncrement()) attributes.add("AUTO_INCREMENT");
                if (table.getPrimaryKeys().contains(column.getColumnName())) attributes.add("PRIMARY KEY");
                
                if (!attributes.isEmpty()) {
                    description.append(" ").append(String.join(", ", attributes));
                }
                
                if (StringUtils.hasText(column.getComment())) {
                    description.append(" - ").append(column.getComment());
                }
                
                description.append("\n");
            }
            
            if (!table.getIndexes().isEmpty()) {
                description.append("\n**索引**:\n");
                for (DatabaseSchemaInfo.IndexInfo index : table.getIndexes()) {
                    description.append("- ").append(index.getIndexName());
                    if (index.isUnique()) {
                        description.append(" (UNIQUE)");
                    }
                    description.append(": ").append(String.join(", ", index.getColumns())).append("\n");
                }
            }
            
            description.append("\n");
        }
        
        if (!schemaInfo.getRelationships().isEmpty()) {
            description.append("## 表关系\n");
            for (DatabaseSchemaInfo.RelationshipInfo rel : schemaInfo.getRelationships()) {
                description.append("- **").append(rel.getFromTable()).append(".").append(rel.getFromColumn())
                    .append("** → **").append(rel.getToTable()).append(".").append(rel.getToColumn()).append("**");
                if (StringUtils.hasText(rel.getConstraintName())) {
                    description.append(" (").append(rel.getConstraintName()).append(")");
                }
                description.append("\n");
            }
        }
        
        return description.toString();
    }
    
    private Connection createConnection(DataSource dataSource) throws SQLException {
        // 这里需要根据你的DataSource实体结构来实现连接创建
        // 由于我无法看到DataSource实体的具体实现，这里提供一个通用的实现框架
        try {
            String url = buildConnectionUrl(dataSource);
            return DriverManager.getConnection(url, dataSource.getUsername(), dataSource.getPassword());
        } catch (SQLException e) {
            log.error("创建数据库连接失败: {}", dataSource.getDatasourceName(), e);
            throw e;
        }
    }
    
    private String buildConnectionUrl(DataSource dataSource) {
        // 根据数据库类型构建连接URL
        String host = dataSource.getHost();
        Integer port = dataSource.getPort();
        String databaseName = dataSource.getDatabaseName();
        
        switch (dataSource.getDatabaseType().toUpperCase()) {
            case "MYSQL":
                return String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai", 
                    host, port != null ? port : 3306, databaseName);
            case "POSTGRESQL":
                return String.format("jdbc:postgresql://%s:%d/%s", 
                    host, port != null ? port : 5432, databaseName);
            case "ORACLE":
                return String.format("jdbc:oracle:thin:@%s:%d:%s", 
                    host, port != null ? port : 1521, databaseName);
            case "SQLSERVER":
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s", 
                    host, port != null ? port : 1433, databaseName);
            default:
                throw new RuntimeException("不支持的数据库类型: " + dataSource.getDatabaseType());
        }
    }
}