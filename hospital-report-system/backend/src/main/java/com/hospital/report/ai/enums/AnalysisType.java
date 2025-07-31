package com.hospital.report.ai.enums;

public enum AnalysisType {
    SQL_EXPLAIN("SQL执行计划分析"),
    SQL_OPTIMIZE("SQL优化建议"),
    PERFORMANCE_ANALYZE("性能分析"),
    SCHEMA_ANALYZE("数据库结构分析"),
    SECURITY_ANALYZE("安全分析");
    
    private final String description;
    
    AnalysisType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}