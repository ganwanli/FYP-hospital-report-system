package com.hospital.report.executor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

@Component
@Slf4j
public class SecurityChecker {

    private static final List<String> DANGEROUS_KEYWORDS = Arrays.asList(
        "DROP", "TRUNCATE", "DELETE", "ALTER", "CREATE", "GRANT", "REVOKE",
        "EXEC", "EXECUTE", "SHUTDOWN", "KILL", "LOAD_FILE", "INTO OUTFILE",
        "INTO DUMPFILE", "BENCHMARK", "SLEEP", "WAITFOR", "DELAY"
    );

    private static final List<String> SYSTEM_TABLES = Arrays.asList(
        "INFORMATION_SCHEMA", "MYSQL", "PERFORMANCE_SCHEMA", "SYS",
        "MASTER", "MSDB", "TEMPDB", "MODEL"
    );

    private static final List<Pattern> SQL_INJECTION_PATTERNS = Arrays.asList(
        Pattern.compile("('|(\\-\\-)|(;)|(\\|)|(\\*)|(%))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(union|select|insert|delete|update|create|drop|exec|execute)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(script|javascript|vbscript|onload|onerror)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\<|\\>|\\&lt;|\\&gt;)", Pattern.CASE_INSENSITIVE)
    );

    private static final List<Pattern> XSS_PATTERNS = Arrays.asList(
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
    );

    public SecurityCheckResult checkSql(String sql, Map<String, Object> parameters) {
        SecurityCheckResult result = new SecurityCheckResult();
        result.setValid(true);
        result.setViolations(new ArrayList<>());
        result.setRiskLevel("LOW");

        if (!StringUtils.hasText(sql)) {
            result.setValid(false);
            result.setErrorMessage("SQL content is empty");
            return result;
        }

        // Check for dangerous keywords
        checkDangerousKeywords(sql, result);

        // Check for system table access
        checkSystemTableAccess(sql, result);

        // Check for SQL injection patterns
        checkSqlInjectionPatterns(sql, result);

        // Check parameters for malicious content
        if (parameters != null) {
            checkParameterSecurity(parameters, result);
        }

        // Check SQL structure
        checkSqlStructure(sql, result);

        // Check for suspicious patterns
        checkSuspiciousPatterns(sql, result);

        // Determine overall risk level
        determineRiskLevel(result);

        return result;
    }

    public boolean isParameterSafe(String paramName, Object paramValue) {
        if (paramValue == null) {
            return true;
        }

        String stringValue = paramValue.toString();

        // Check for XSS patterns
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(stringValue).find()) {
                return false;
            }
        }

        // Check for SQL injection in parameter values
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(stringValue).find()) {
                return false;
            }
        }

        // Check for excessively long values
        if (stringValue.length() > 10000) {
            return false;
        }

        return true;
    }

    public String sanitizeParameter(Object paramValue) {
        if (paramValue == null) {
            return null;
        }

        String stringValue = paramValue.toString();

        // Remove potentially dangerous characters
        stringValue = stringValue.replaceAll("[<>\"'&]", "");
        
        // Limit length
        if (stringValue.length() > 1000) {
            stringValue = stringValue.substring(0, 1000);
        }

        return stringValue;
    }

    public List<String> extractTableNames(String sql) {
        List<String> tableNames = new ArrayList<>();
        
        // Simple regex to extract table names (this is a basic implementation)
        Pattern tablePattern = Pattern.compile("(?i)(?:FROM|JOIN|UPDATE|INTO)\\s+([a-zA-Z_][a-zA-Z0-9_]*)", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = tablePattern.matcher(sql);
        
        while (matcher.find()) {
            String tableName = matcher.group(1);
            if (!tableNames.contains(tableName.toUpperCase())) {
                tableNames.add(tableName.toUpperCase());
            }
        }
        
        return tableNames;
    }

    public boolean isReadOnlyQuery(String sql) {
        String upperSql = sql.trim().toUpperCase();
        return upperSql.startsWith("SELECT") || upperSql.startsWith("WITH") || upperSql.startsWith("SHOW") || upperSql.startsWith("DESCRIBE");
    }

    public SecurityCheckResult validateQueryComplexity(String sql) {
        SecurityCheckResult result = new SecurityCheckResult();
        result.setValid(true);
        result.setViolations(new ArrayList<>());

        // Check for nested queries depth
        int nestedDepth = countNestedQueries(sql);
        if (nestedDepth > 5) {
            result.getViolations().add("Query nesting depth too high: " + nestedDepth);
            result.setRiskLevel("HIGH");
        }

        // Check for excessive joins
        int joinCount = countJoins(sql);
        if (joinCount > 10) {
            result.getViolations().add("Too many joins: " + joinCount);
            result.setRiskLevel("MEDIUM");
        }

        // Check for Cartesian products
        if (hasCartesianProduct(sql)) {
            result.getViolations().add("Potential Cartesian product detected");
            result.setRiskLevel("HIGH");
        }

        if (!result.getViolations().isEmpty()) {
            result.setValid(false);
            result.setErrorMessage("Query complexity validation failed");
        }

        return result;
    }

    private void checkDangerousKeywords(String sql, SecurityCheckResult result) {
        String upperSql = sql.toUpperCase();
        
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                result.getViolations().add("Dangerous keyword detected: " + keyword);
                result.setRiskLevel("CRITICAL");
            }
        }
    }

    private void checkSystemTableAccess(String sql, SecurityCheckResult result) {
        String upperSql = sql.toUpperCase();
        
        for (String systemTable : SYSTEM_TABLES) {
            if (upperSql.contains(systemTable)) {
                result.getViolations().add("System table access detected: " + systemTable);
                result.setRiskLevel("HIGH");
            }
        }
    }

    private void checkSqlInjectionPatterns(String sql, SecurityCheckResult result) {
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(sql).find()) {
                result.getViolations().add("Potential SQL injection pattern detected");
                result.setRiskLevel("HIGH");
                break;
            }
        }
    }

    private void checkParameterSecurity(Map<String, Object> parameters, SecurityCheckResult result) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String paramName = entry.getKey();
            Object paramValue = entry.getValue();
            
            if (!isParameterSafe(paramName, paramValue)) {
                result.getViolations().add("Unsafe parameter detected: " + paramName);
                result.setRiskLevel("HIGH");
            }
        }
    }

    private void checkSqlStructure(String sql, SecurityCheckResult result) {
        // Check for multiple statements
        if (sql.contains(";") && !sql.trim().endsWith(";")) {
            result.getViolations().add("Multiple SQL statements detected");
            result.setRiskLevel("HIGH");
        }

        // Check for comments that might hide malicious code
        if (sql.contains("/*") || sql.contains("--")) {
            result.getViolations().add("SQL comments detected - potential code hiding");
            result.setRiskLevel("MEDIUM");
        }

        // Check for unusual characters
        if (sql.matches(".*[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F].*")) {
            result.getViolations().add("Unusual control characters detected");
            result.setRiskLevel("HIGH");
        }
    }

    private void checkSuspiciousPatterns(String sql, SecurityCheckResult result) {
        String lowerSql = sql.toLowerCase();

        // Check for time-based attack patterns
        if (lowerSql.contains("sleep(") || lowerSql.contains("waitfor") || lowerSql.contains("benchmark(")) {
            result.getViolations().add("Time-based attack pattern detected");
            result.setRiskLevel("CRITICAL");
        }

        // Check for file operation patterns
        if (lowerSql.contains("load_file") || lowerSql.contains("into outfile") || lowerSql.contains("into dumpfile")) {
            result.getViolations().add("File operation pattern detected");
            result.setRiskLevel("CRITICAL");
        }

        // Check for union-based injection
        if (lowerSql.contains("union") && (lowerSql.contains("select") || lowerSql.contains("null"))) {
            result.getViolations().add("Union-based injection pattern detected");
            result.setRiskLevel("HIGH");
        }

        // Check for blind injection patterns
        if (lowerSql.matches(".*\\b(and|or)\\s+\\d+\\s*=\\s*\\d+.*")) {
            result.getViolations().add("Blind injection pattern detected");
            result.setRiskLevel("HIGH");
        }
    }

    private void determineRiskLevel(SecurityCheckResult result) {
        if (result.getViolations().isEmpty()) {
            result.setRiskLevel("LOW");
            return;
        }

        // If already set to CRITICAL, keep it
        if ("CRITICAL".equals(result.getRiskLevel())) {
            result.setValid(false);
            result.setErrorMessage("Critical security violations detected");
            return;
        }

        // Count high-risk violations
        long highRiskCount = result.getViolations().stream()
                .filter(v -> v.contains("injection") || v.contains("attack") || v.contains("Dangerous"))
                .count();

        if (highRiskCount > 0) {
            result.setRiskLevel("HIGH");
            result.setValid(false);
            result.setErrorMessage("High-risk security violations detected");
        } else if (result.getViolations().size() > 2) {
            result.setRiskLevel("MEDIUM");
        }
    }

    private int countNestedQueries(String sql) {
        int count = 0;
        int depth = 0;
        int maxDepth = 0;
        
        for (char c : sql.toCharArray()) {
            if (c == '(') {
                depth++;
                maxDepth = Math.max(maxDepth, depth);
            } else if (c == ')') {
                depth--;
            }
        }
        
        return maxDepth;
    }

    private int countJoins(String sql) {
        String upperSql = sql.toUpperCase();
        int count = 0;
        
        String[] joinTypes = {"JOIN", "INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL JOIN", "CROSS JOIN"};
        for (String joinType : joinTypes) {
            int index = 0;
            while ((index = upperSql.indexOf(joinType, index)) != -1) {
                count++;
                index += joinType.length();
            }
        }
        
        return count;
    }

    private boolean hasCartesianProduct(String sql) {
        String upperSql = sql.toUpperCase();
        
        // Simple check: multiple FROM clauses without proper JOIN conditions
        int fromCount = 0;
        int joinCount = countJoins(sql);
        
        String[] parts = upperSql.split("\\s+");
        for (String part : parts) {
            if ("FROM".equals(part)) {
                fromCount++;
            }
        }
        
        // If there are multiple tables in FROM but no joins, it's likely a Cartesian product
        return fromCount > 1 && joinCount == 0 && upperSql.contains(",");
    }

    @Data
    public static class SecurityCheckResult {
        private boolean valid;
        private String errorMessage;
        private String riskLevel;
        private List<String> violations;
        private Map<String, Object> details = new HashMap<>();
    }
}