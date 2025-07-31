package com.hospital.report.ai.entity.dto;

import com.hospital.report.ai.enums.AnalysisType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class SqlAnalysisResult {
    
    private String sqlContent;
    private AnalysisType analysisType;
    private String databaseType;
    private boolean success;
    private String errorMessage;
    
    // 执行计划相关
    private Map<String, Object> executionPlan;
    private List<String> insights;
    
    // 优化建议相关
    private List<OptimizationSuggestion> optimizationSuggestions;
    
    // 性能分析相关
    private PerformanceMetrics performanceMetrics;
    private List<String> bottlenecks;
    
    // 安全分析相关
    private List<SecurityIssue> securityIssues;
    
    private LocalDateTime analyzedAt = LocalDateTime.now();
    private Long executionTimeMs;
    
    @Data
    public static class OptimizationSuggestion {
        private String type; // SYNTAX, PERFORMANCE, BEST_PRACTICE, SECURITY
        private String severity; // HIGH, MEDIUM, LOW
        private String title;
        private String description;
        private String suggestion;
        private String optimizedSql;
        private String reason;
    }
    
    @Data
    public static class PerformanceMetrics {
        private Double estimatedCost;
        private Long estimatedRows;
        private String accessMethod;
        private List<String> indexesUsed;
        private List<String> indexesMissing;
        private String joinType;
        private boolean hasFullTableScan;
        private Map<String, Object> additionalMetrics;
    }
    
    @Data
    public static class SecurityIssue {
        private String type; // SQL_INJECTION, PRIVILEGE_ESCALATION, DATA_EXPOSURE
        private String severity; // HIGH, MEDIUM, LOW
        private String description;
        private String recommendation;
        private String affectedPart;
    }
}