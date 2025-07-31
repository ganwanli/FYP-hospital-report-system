package com.hospital.report.ai.service;

import com.hospital.report.ai.entity.dto.SqlAnalysisResult;
import com.hospital.report.ai.enums.AnalysisType;
import com.hospital.report.entity.DataSource;
import com.hospital.report.service.DataSourceService;
import com.hospital.report.service.SqlExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SqlAnalyzer {
    
    private final SqlExecutionService sqlExecutionService;
    private final DataSourceService dataSourceService;
    
    // SQL关键字和模式
    private static final Set<String> RISKY_KEYWORDS = Set.of(
        "DROP", "DELETE", "TRUNCATE", "ALTER", "CREATE", "INSERT", "UPDATE"
    );
    
    private static final Set<String> FUNCTION_KEYWORDS = Set.of(
        "SUBSTRING", "CONCAT", "UPPER", "LOWER", "LENGTH", "TRIM"
    );
    
    private static final Pattern SELECT_STAR_PATTERN = Pattern.compile("SELECT\\s+\\*", Pattern.CASE_INSENSITIVE);
    private static final Pattern LIKE_WILDCARD_PATTERN = Pattern.compile("LIKE\\s+'%[^']*'", Pattern.CASE_INSENSITIVE);
    private static final Pattern OR_PATTERN = Pattern.compile("\\bOR\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern FUNCTION_IN_WHERE_PATTERN = Pattern.compile("WHERE.*?\\b(" + String.join("|", FUNCTION_KEYWORDS) + ")\\s*\\(", Pattern.CASE_INSENSITIVE);
    
    public SqlAnalyzer(SqlExecutionService sqlExecutionService, 
                      DataSourceService dataSourceService) {
        this.sqlExecutionService = sqlExecutionService;
        this.dataSourceService = dataSourceService;
    }
    
    public SqlAnalysisResult analyzeSql(String sqlContent, Long datasourceId, AnalysisType analysisType) {
        long startTime = System.currentTimeMillis();
        
        DataSource dataSource = dataSourceService.getById(datasourceId);
        if (dataSource == null) {
            throw new RuntimeException("数据源不存在，ID: " + datasourceId);
        }
        
        SqlAnalysisResult result = new SqlAnalysisResult();
        result.setSqlContent(sqlContent);
        result.setAnalysisType(analysisType);
        result.setDatabaseType(dataSource.getDatabaseType());
        
        try {
            switch (analysisType) {
                case SQL_EXPLAIN:
                    result = explainSql(sqlContent, dataSource, result);
                    break;
                case SQL_OPTIMIZE:
                    result = optimizeSql(sqlContent, dataSource, result);
                    break;
                case PERFORMANCE_ANALYZE:
                    result = performanceAnalyze(sqlContent, dataSource, result);
                    break;
                case SECURITY_ANALYZE:
                    result = securityAnalyze(sqlContent, dataSource, result);
                    break;
                default:
                    throw new RuntimeException("不支持的分析类型: " + analysisType);
            }
            
            result.setSuccess(true);
            
        } catch (Exception e) {
            log.error("SQL分析失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        return result;
    }
    
    private SqlAnalysisResult explainSql(String sqlContent, DataSource dataSource, SqlAnalysisResult result) {
        try {
            // 使用现有的explainQuery方法
            Map<String, Object> explainResult = sqlExecutionService.explainQuery(sqlContent, dataSource.getDatabaseType());
            result.setExecutionPlan(explainResult);
            
            // 分析执行计划
            List<String> insights = analyzeExecutionPlan(explainResult, dataSource.getDatabaseType());
            result.setInsights(insights);
            
        } catch (Exception e) {
            log.error("SQL解释失败", e);
            throw new RuntimeException("SQL执行计划分析失败: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    private SqlAnalysisResult optimizeSql(String sqlContent, DataSource dataSource, SqlAnalysisResult result) {
        List<SqlAnalysisResult.OptimizationSuggestion> suggestions = new ArrayList<>();
        
        // 1. 语法分析
        suggestions.addAll(analyzeSyntax(sqlContent));
        
        // 2. 性能分析
        suggestions.addAll(analyzePerformance(sqlContent, dataSource));
        
        // 3. 最佳实践检查
        suggestions.addAll(checkBestPractices(sqlContent));
        
        result.setOptimizationSuggestions(suggestions);
        return result;
    }
    
    private SqlAnalysisResult performanceAnalyze(String sqlContent, DataSource dataSource, SqlAnalysisResult result) {
        try {
            // 1. 获取执行计划
            Map<String, Object> explainResult = sqlExecutionService.explainQuery(sqlContent, dataSource.getDatabaseType());
            result.setExecutionPlan(explainResult);
            
            // 2. 分析性能指标
            SqlAnalysisResult.PerformanceMetrics metrics = analyzePerformanceMetrics(explainResult, dataSource.getDatabaseType());
            result.setPerformanceMetrics(metrics);
            
            // 3. 识别性能瓶颈
            List<String> bottlenecks = identifyBottlenecks(explainResult, metrics, sqlContent);
            result.setBottlenecks(bottlenecks);
            
        } catch (Exception e) {
            log.error("性能分析失败", e);
            throw new RuntimeException("SQL性能分析失败: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    private SqlAnalysisResult securityAnalyze(String sqlContent, DataSource dataSource, SqlAnalysisResult result) {
        List<SqlAnalysisResult.SecurityIssue> securityIssues = new ArrayList<>();
        
        // 检查危险操作
        securityIssues.addAll(checkDangerousOperations(sqlContent));
        
        // 检查SQL注入风险
        securityIssues.addAll(checkSqlInjectionRisks(sqlContent));
        
        // 检查权限相关问题
        securityIssues.addAll(checkPrivilegeIssues(sqlContent));
        
        result.setSecurityIssues(securityIssues);
        return result;
    }
    
    private List<SqlAnalysisResult.OptimizationSuggestion> analyzeSyntax(String sqlContent) {
        List<SqlAnalysisResult.OptimizationSuggestion> suggestions = new ArrayList<>();
        String upperSql = sqlContent.toUpperCase();
        
        // 检查 SELECT *
        if (SELECT_STAR_PATTERN.matcher(sqlContent).find()) {
            SqlAnalysisResult.OptimizationSuggestion suggestion = new SqlAnalysisResult.OptimizationSuggestion();
            suggestion.setType("SYNTAX");
            suggestion.setSeverity("MEDIUM");
            suggestion.setTitle("避免使用 SELECT *");
            suggestion.setDescription("检测到使用了 SELECT *，这可能影响性能");
            suggestion.setSuggestion("明确指定需要的列名，避免传输和处理不必要的数据");
            suggestion.setReason("SELECT * 会返回所有列，增加网络传输量和内存使用");
            suggestions.add(suggestion);
        }
        
        // 检查是否缺少LIMIT
        if (upperSql.contains("SELECT") && !upperSql.contains("LIMIT") && !upperSql.contains("TOP")) {
            SqlAnalysisResult.OptimizationSuggestion suggestion = new SqlAnalysisResult.OptimizationSuggestion();
            suggestion.setType("SYNTAX");
            suggestion.setSeverity("LOW");
            suggestion.setTitle("建议添加LIMIT限制");
            suggestion.setDescription("查询没有LIMIT限制，可能返回大量数据");
            suggestion.setSuggestion("为查询添加合适的LIMIT限制，如 LIMIT 1000");
            suggestion.setReason("无限制的查询可能返回大量数据，影响性能和用户体验");
            suggestions.add(suggestion);
        }
        
        // 检查子查询
        long selectCount = upperSql.chars().mapToObj(c -> String.valueOf((char) c))
            .filter(s -> upperSql.indexOf("SELECT") != -1).count();
        if (selectCount > 1) {
            SqlAnalysisResult.OptimizationSuggestion suggestion = new SqlAnalysisResult.OptimizationSuggestion();
            suggestion.setType("SYNTAX");
            suggestion.setSeverity("MEDIUM");
            suggestion.setTitle("考虑优化子查询");
            suggestion.setDescription("检测到子查询，可能可以优化为JOIN");
            suggestion.setSuggestion("评估是否可以将子查询重写为JOIN操作");
            suggestion.setReason("JOIN通常比子查询有更好的性能");
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    private List<SqlAnalysisResult.OptimizationSuggestion> analyzePerformance(String sqlContent, DataSource dataSource) {
        List<SqlAnalysisResult.OptimizationSuggestion> suggestions = new ArrayList<>();
        
        // 检查LIKE通配符
        if (LIKE_WILDCARD_PATTERN.matcher(sqlContent).find()) {
            SqlAnalysisResult.OptimizationSuggestion suggestion = new SqlAnalysisResult.OptimizationSuggestion();
            suggestion.setType("PERFORMANCE");
            suggestion.setSeverity("HIGH");
            suggestion.setTitle("LIKE查询性能问题");
            suggestion.setDescription("LIKE查询以通配符开头会导致全表扫描");
            suggestion.setSuggestion("考虑使用全文索引或重构查询条件");
            suggestion.setReason("以%开头的LIKE查询无法使用索引，会进行全表扫描");
            suggestions.add(suggestion);
        }
        
        // 检查OR条件
        if (OR_PATTERN.matcher(sqlContent).find()) {
            SqlAnalysisResult.OptimizationSuggestion suggestion = new SqlAnalysisResult.OptimizationSuggestion();
            suggestion.setType("PERFORMANCE");
            suggestion.setSeverity("MEDIUM");
            suggestion.setTitle("OR条件性能影响");
            suggestion.setDescription("OR条件可能影响索引使用效率");
            suggestion.setSuggestion("考虑使用UNION替代OR，或者重构查询逻辑");
            suggestion.setReason("OR条件可能导致索引失效或选择性较差的执行计划");
            suggestions.add(suggestion);
        }
        
        // 检查WHERE子句中的函数
        if (FUNCTION_IN_WHERE_PATTERN.matcher(sqlContent).find()) {
            SqlAnalysisResult.OptimizationSuggestion suggestion = new SqlAnalysisResult.OptimizationSuggestion();
            suggestion.setType("PERFORMANCE");
            suggestion.setSeverity("HIGH");
            suggestion.setTitle("WHERE子句中使用函数");
            suggestion.setDescription("WHERE子句中对列使用函数会阻止索引使用");
            suggestion.setSuggestion("尽量避免在WHERE子句中对列使用函数，考虑重构查询");
            suggestion.setReason("对列使用函数会导致索引失效，需要进行全表扫描");
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    private List<SqlAnalysisResult.OptimizationSuggestion> checkBestPractices(String sqlContent) {
        List<SqlAnalysisResult.OptimizationSuggestion> suggestions = new ArrayList<>();
        String upperSql = sqlContent.toUpperCase();
        
        // 检查是否有WHERE条件
        if (upperSql.contains("SELECT") && !upperSql.contains("WHERE") && !upperSql.contains("LIMIT")) {
            SqlAnalysisResult.OptimizationSuggestion suggestion = new SqlAnalysisResult.OptimizationSuggestion();
            suggestion.setType("BEST_PRACTICE");
            suggestion.setSeverity("MEDIUM");
            suggestion.setTitle("缺少过滤条件");
            suggestion.setDescription("查询没有WHERE条件，可能返回全表数据");
            suggestion.setSuggestion("添加适当的WHERE条件来过滤数据");
            suggestion.setReason("无条件查询可能返回大量不必要的数据");
            suggestions.add(suggestion);
        }
        
        // 检查DISTINCT用法
        if (upperSql.contains("SELECT DISTINCT") && !upperSql.contains("ORDER BY")) {
            SqlAnalysisResult.OptimizationSuggestion suggestion = new SqlAnalysisResult.OptimizationSuggestion();
            suggestion.setType("BEST_PRACTICE");
            suggestion.setSeverity("LOW");
            suggestion.setTitle("DISTINCT查询建议");
            suggestion.setDescription("DISTINCT查询建议添加ORDER BY保证结果一致性");
            suggestion.setSuggestion("为DISTINCT查询添加ORDER BY子句");
            suggestion.setReason("没有ORDER BY的DISTINCT查询结果顺序可能不一致");
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    private List<String> analyzeExecutionPlan(Map<String, Object> explainResult, String databaseType) {
        List<String> insights = new ArrayList<>();
        
        if (explainResult == null || explainResult.isEmpty()) {
            insights.add("无法获取执行计划信息");
            return insights;
        }
        
        // MySQL执行计划分析
        if ("MySQL".equalsIgnoreCase(databaseType)) {
            analyzeMySQLExecutionPlan(explainResult, insights);
        }
        // 可以添加其他数据库的执行计划分析
        
        return insights;
    }
    
    private void analyzeMySQLExecutionPlan(Map<String, Object> explainResult, List<String> insights) {
        // 分析MySQL执行计划
        Object rows = explainResult.get("rows");
        if (rows != null && rows instanceof Number) {
            long rowCount = ((Number) rows).longValue();
            if (rowCount > 10000) {
                insights.add("预计处理行数较多(" + rowCount + ")，可能需要优化");
            }
        }
        
        Object type = explainResult.get("type");
        if (type != null) {
            String scanType = type.toString();
            if ("ALL".equals(scanType)) {
                insights.add("检测到全表扫描，建议检查索引使用");
            } else if ("index".equals(scanType)) {
                insights.add("使用了索引扫描，性能较好");
            }
        }
        
        Object extra = explainResult.get("Extra");
        if (extra != null && extra.toString().contains("Using filesort")) {
            insights.add("检测到文件排序，考虑添加相应的索引优化ORDER BY");
        }
        
        if (extra != null && extra.toString().contains("Using temporary")) {
            insights.add("检测到使用临时表，可能影响性能");
        }
    }
    
    private SqlAnalysisResult.PerformanceMetrics analyzePerformanceMetrics(Map<String, Object> explainResult, String databaseType) {
        SqlAnalysisResult.PerformanceMetrics metrics = new SqlAnalysisResult.PerformanceMetrics();
        
        if (explainResult != null) {
            // 提取通用性能指标
            Object rows = explainResult.get("rows");
            if (rows instanceof Number) {
                metrics.setEstimatedRows(((Number) rows).longValue());
            }
            
            Object type = explainResult.get("type");
            if (type != null) {
                metrics.setAccessMethod(type.toString());
                metrics.setHasFullTableScan("ALL".equals(type.toString()));
            }
            
            Object key = explainResult.get("key");
            if (key != null && !key.toString().isEmpty()) {
                metrics.setIndexesUsed(Arrays.asList(key.toString().split(",")));
            }
            
            metrics.setAdditionalMetrics(explainResult);
        }
        
        return metrics;
    }
    
    private List<String> identifyBottlenecks(Map<String, Object> explainResult, SqlAnalysisResult.PerformanceMetrics metrics, String sqlContent) {
        List<String> bottlenecks = new ArrayList<>();
        
        if (metrics.getEstimatedRows() != null && metrics.getEstimatedRows() > 100000) {
            bottlenecks.add("预计处理行数过多(" + metrics.getEstimatedRows() + ")，建议添加更严格的过滤条件");
        }
        
        if (metrics.isHasFullTableScan()) {
            bottlenecks.add("存在全表扫描，建议检查WHERE条件和索引使用");
        }
        
        if (metrics.getIndexesUsed() == null || metrics.getIndexesUsed().isEmpty()) {
            bottlenecks.add("查询没有使用索引，可能需要创建合适的索引");
        }
        
        return bottlenecks;
    }
    
    private List<SqlAnalysisResult.SecurityIssue> checkDangerousOperations(String sqlContent) {
        List<SqlAnalysisResult.SecurityIssue> issues = new ArrayList<>();
        String upperSql = sqlContent.toUpperCase();
        
        for (String keyword : RISKY_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                SqlAnalysisResult.SecurityIssue issue = new SqlAnalysisResult.SecurityIssue();
                issue.setType("DANGEROUS_OPERATION");
                issue.setSeverity("HIGH");
                issue.setDescription("检测到危险操作: " + keyword);
                issue.setRecommendation("请确认此操作的必要性，并做好数据备份");
                issue.setAffectedPart(keyword + " 语句");
                issues.add(issue);
            }
        }
        
        return issues;
    }
    
    private List<SqlAnalysisResult.SecurityIssue> checkSqlInjectionRisks(String sqlContent) {
        List<SqlAnalysisResult.SecurityIssue> issues = new ArrayList<>();
        
        // 检查动态SQL拼接的潜在风险
        if (sqlContent.contains("'") && (sqlContent.contains("OR") || sqlContent.contains("AND"))) {
            SqlAnalysisResult.SecurityIssue issue = new SqlAnalysisResult.SecurityIssue();
            issue.setType("SQL_INJECTION");
            issue.setSeverity("HIGH");
            issue.setDescription("可能存在SQL注入风险");
            issue.setRecommendation("使用参数化查询或预编译语句");
            issue.setAffectedPart("WHERE条件");
            issues.add(issue);
        }
        
        return issues;
    }
    
    private List<SqlAnalysisResult.SecurityIssue> checkPrivilegeIssues(String sqlContent) {
        List<SqlAnalysisResult.SecurityIssue> issues = new ArrayList<>();
        String upperSql = sqlContent.toUpperCase();
        
        // 检查可能的权限提升操作
        if (upperSql.contains("GRANT") || upperSql.contains("REVOKE")) {
            SqlAnalysisResult.SecurityIssue issue = new SqlAnalysisResult.SecurityIssue();
            issue.setType("PRIVILEGE_ESCALATION");
            issue.setSeverity("HIGH");
            issue.setDescription("检测到权限操作语句");
            issue.setRecommendation("确保只有授权用户才能执行权限操作");
            issue.setAffectedPart("权限管理语句");
            issues.add(issue);
        }
        
        return issues;
    }
}