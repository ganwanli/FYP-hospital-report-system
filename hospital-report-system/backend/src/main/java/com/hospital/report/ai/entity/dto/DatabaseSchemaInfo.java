package com.hospital.report.ai.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class DatabaseSchemaInfo {
    
    private String databaseName;
    private String databaseType;
    private String version;
    private List<TableInfo> tables;
    private List<RelationshipInfo> relationships;
    private Integer totalTables;
    private Integer totalColumns;
    private Integer totalIndexes;
    
    @Data
    public static class TableInfo {
        private String tableName;
        private String tableComment;
        private String tableType;
        private Long rowCount;
        private String engine;
        private List<ColumnInfo> columns;
        private List<IndexInfo> indexes;
        private List<String> primaryKeys;
    }
    
    @Data
    public static class ColumnInfo {
        private String columnName;
        private String dataType;
        private Integer columnSize;
        private Integer decimalDigits;
        private boolean nullable;
        private boolean autoIncrement;
        private String defaultValue;
        private String comment;
        private boolean isPrimaryKey;
        private boolean isForeignKey;
    }
    
    @Data
    public static class IndexInfo {
        private String indexName;
        private boolean unique;
        private String indexType;
        private List<String> columns;
        private String comment;
    }
    
    @Data
    public static class RelationshipInfo {
        private String fromTable;
        private String fromColumn;
        private String toTable;
        private String toColumn;
        private String constraintName;
        private String relationshipType; // ONE_TO_ONE, ONE_TO_MANY, MANY_TO_MANY
    }
}