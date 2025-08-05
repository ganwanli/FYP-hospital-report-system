package com.hospital.report.ai.service;

import com.hospital.report.ai.entity.DatabaseSchema;
import com.hospital.report.ai.entity.TableRelation;
import com.hospital.report.entity.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据库元数据提取服务
 */
@Service
@Slf4j
public class DatabaseMetadataExtractor {

    @Autowired
    private DatabaseConnectionService connectionService;

    /**
     * 提取数据库的所有表和字段信息
     */
    public List<DatabaseSchema> extractDatabaseSchema(DataSource dataSource) {
        List<DatabaseSchema> schemas = new ArrayList<>();
        
        try (Connection connection = connectionService.getConnection(dataSource)) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseName = connection.getCatalog();
            
            log.info("开始提取数据库 {} 的元数据", databaseName);
            
            // 获取所有表
            try (ResultSet tables = metaData.getTables(databaseName, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    String tableComment = tables.getString("REMARKS");
                    
                    log.debug("处理表: {}", tableName);
                    
                    // 为每个表创建一个概览记录
                    DatabaseSchema tableSchema = createTableOverview(dataSource, databaseName, tableName, tableComment);
                    schemas.add(tableSchema);
                    
                    // 获取表的所有字段
                    schemas.addAll(extractColumnsForTable(dataSource, databaseName, tableName, tableComment, metaData));
                }
            }
            
            log.info("成功提取 {} 个schema记录", schemas.size());
            
        } catch (SQLException e) {
            log.error("提取数据库元数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract database metadata", e);
        }
        
        return schemas;
    }

    /**
     * 创建表概览记录
     */
    private DatabaseSchema createTableOverview(DataSource dataSource, String databaseName, 
                                             String tableName, String tableComment) {
        DatabaseSchema schema = new DatabaseSchema();
        schema.setDatasourceId(dataSource.getId());
        schema.setDatabaseName(databaseName);
        schema.setTableName(tableName);
        schema.setTableComment(tableComment);
        schema.setColumnName(null); // 表概览不关联具体字段
        
        // 构建表的完整描述
        StringBuilder description = new StringBuilder();
        description.append("表名: ").append(tableName);
        if (tableComment != null && !tableComment.trim().isEmpty()) {
            description.append(", 描述: ").append(tableComment);
        }
        description.append(", 数据库: ").append(databaseName);
        
        schema.setFullDescription(description.toString());
        schema.setCreatedTime(LocalDateTime.now());
        schema.setUpdatedTime(LocalDateTime.now());
        
        return schema;
    }

    /**
     * 提取表的所有字段信息
     */
    private List<DatabaseSchema> extractColumnsForTable(DataSource dataSource, String databaseName,
                                                       String tableName, String tableComment, 
                                                       DatabaseMetaData metaData) throws SQLException {
        List<DatabaseSchema> columnSchemas = new ArrayList<>();
        
        // 获取主键信息
        Set<String> primaryKeys = getPrimaryKeys(metaData, databaseName, tableName);
        
        // 获取字段信息
        try (ResultSet columns = metaData.getColumns(databaseName, null, tableName, "%")) {
            while (columns.next()) {
                DatabaseSchema columnSchema = new DatabaseSchema();
                columnSchema.setDatasourceId(dataSource.getId());
                columnSchema.setDatabaseName(databaseName);
                columnSchema.setTableName(tableName);
                columnSchema.setTableComment(tableComment);
                
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                String columnComment = columns.getString("REMARKS");
                int nullable = columns.getInt("NULLABLE");
                String defaultValue = columns.getString("COLUMN_DEF");
                
                columnSchema.setColumnName(columnName);
                columnSchema.setColumnType(columnType);
                columnSchema.setColumnComment(columnComment);
                columnSchema.setIsPrimaryKey(primaryKeys.contains(columnName));
                columnSchema.setIsNullable(nullable == DatabaseMetaData.columnNullable);
                columnSchema.setDefaultValue(defaultValue);
                
                // 构建字段的完整描述
                StringBuilder description = new StringBuilder();
                description.append("表: ").append(tableName);
                description.append(", 字段: ").append(columnName);
                description.append(", 类型: ").append(columnType);
                
                if (columnComment != null && !columnComment.trim().isEmpty()) {
                    description.append(", 描述: ").append(columnComment);
                }
                
                if (primaryKeys.contains(columnName)) {
                    description.append(", 主键");
                }
                
                if (nullable != DatabaseMetaData.columnNullable) {
                    description.append(", 非空");
                }
                
                if (tableComment != null && !tableComment.trim().isEmpty()) {
                    description.append(", 表描述: ").append(tableComment);
                }
                
                columnSchema.setFullDescription(description.toString());
                columnSchema.setCreatedTime(LocalDateTime.now());
                columnSchema.setUpdatedTime(LocalDateTime.now());
                
                columnSchemas.add(columnSchema);
            }
        }
        
        return columnSchemas;
    }

    /**
     * 获取表的主键字段
     */
    private Set<String> getPrimaryKeys(DatabaseMetaData metaData, String databaseName, String tableName) 
            throws SQLException {
        Set<String> primaryKeys = new HashSet<>();
        
        try (ResultSet rs = metaData.getPrimaryKeys(databaseName, null, tableName)) {
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        }
        
        return primaryKeys;
    }

    /**
     * 提取表之间的关系
     */
    public List<TableRelation> extractTableRelations(DataSource dataSource) {
        List<TableRelation> relations = new ArrayList<>();
        
        try (Connection connection = connectionService.getConnection(dataSource)) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseName = connection.getCatalog();
            
            log.info("开始提取数据库 {} 的表关系", databaseName);
            
            // 获取所有表
            try (ResultSet tables = metaData.getTables(databaseName, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    
                    // 获取该表作为外键表的所有关系
                    try (ResultSet foreignKeys = metaData.getImportedKeys(databaseName, null, tableName)) {
                        while (foreignKeys.next()) {
                            TableRelation relation = new TableRelation();
                            relation.setDatasourceId(dataSource.getId());
                            relation.setPrimaryTable(foreignKeys.getString("PKTABLE_NAME"));
                            relation.setForeignTable(foreignKeys.getString("FKTABLE_NAME"));
                            relation.setPrimaryColumn(foreignKeys.getString("PKCOLUMN_NAME"));
                            relation.setForeignColumn(foreignKeys.getString("FKCOLUMN_NAME"));
                            relation.setRelationType("ONE_TO_MANY"); // 默认一对多关系
                            
                            // 构建关系描述
                            String description = String.format("表 %s 的字段 %s 引用表 %s 的字段 %s",
                                relation.getForeignTable(), relation.getForeignColumn(),
                                relation.getPrimaryTable(), relation.getPrimaryColumn());
                            relation.setRelationDescription(description);
                            relation.setCreatedTime(LocalDateTime.now());
                            
                            relations.add(relation);
                        }
                    }
                }
            }
            
            log.info("成功提取 {} 个表关系", relations.size());
            
        } catch (SQLException e) {
            log.error("提取表关系失败: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract table relations", e);
        }
        
        return relations;
    }
}