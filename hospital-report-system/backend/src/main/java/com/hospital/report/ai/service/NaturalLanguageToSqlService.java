package com.hospital.report.ai.service;

import com.hospital.report.ai.client.DeepSeekClient;
import com.hospital.report.ai.entity.DatabaseSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自然语言转SQL服务
 */
@Service
@Slf4j
public class NaturalLanguageToSqlService {

    @Autowired
    private VectorSearchService vectorSearchService;
    
    @Autowired
    private DeepSeekClient deepSeekClient;

    /**
     * 将自然语言转换为SQL查询
     */
    public SqlGenerationResult generateSql(String naturalLanguageQuery, Long datasourceId) {
        log.info("开始生成SQL，查询: {}, 数据源ID: {}", naturalLanguageQuery, datasourceId);
        
        try {
            // 1. 构建查询上下文
            VectorSearchService.QueryContext context = vectorSearchService.buildQueryContext(
                naturalLanguageQuery, datasourceId);
            
            // 2. 构建AI提示词
            String prompt = buildSqlGenerationPrompt(context);
            
            // 3. 调用AI生成SQL
            String aiResponse = deepSeekClient.chat(
                List.of(com.hospital.report.ai.client.dto.ChatRequest.ChatMessage.user(prompt))
            ).map(response -> response.getChoices().get(0).getMessage().getContent())
             .block();
            
            // 4. 解析AI响应，提取SQL和解释
            SqlGenerationResult result = parseAiResponse(aiResponse, context);
            
            log.info("SQL生成完成: {}", result.getGeneratedSql().substring(0, 
                Math.min(100, result.getGeneratedSql().length())));
            
            return result;
            
        } catch (Exception e) {
            log.error("生成SQL失败: {}", e.getMessage(), e);
            return createErrorResult(naturalLanguageQuery, e.getMessage());
        }
    }

    /**
     * 构建SQL生成的提示词
     */
    private String buildSqlGenerationPrompt(VectorSearchService.QueryContext context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个专业的SQL生成助手。请根据用户的自然语言查询生成准确的SQL语句。\n\n");
        
        // 添加数据库schema信息
        prompt.append("## 数据库结构信息\n\n");
        
        // 相关表信息
        if (!context.getRelevantTables().isEmpty()) {
            prompt.append("### 相关表:\n");
            for (DatabaseSchema table : context.getRelevantTables()) {
                prompt.append(String.format("- **%s**: %s\n", 
                    table.getTableName(), 
                    table.getTableComment() != null ? table.getTableComment() : "无描述"));
            }
            prompt.append("\n");
        }
        
        // 相关字段信息
        if (!context.getRelevantColumns().isEmpty()) {
            prompt.append("### 相关字段:\n");
            
            // 按表分组显示字段
            Map<String, List<DatabaseSchema>> columnsByTable = context.getRelevantColumns().stream()
                .collect(Collectors.groupingBy(DatabaseSchema::getTableName));
            
            for (Map.Entry<String, List<DatabaseSchema>> entry : columnsByTable.entrySet()) {
                prompt.append(String.format("**表 %s:**\n", entry.getKey()));
                for (DatabaseSchema column : entry.getValue()) {
                    prompt.append(String.format("  - %s (%s)", 
                        column.getColumnName(), column.getColumnType()));
                    if (column.getIsPrimaryKey()) {
                        prompt.append(" [主键]");
                    }
                    if (column.getColumnComment() != null && !column.getColumnComment().trim().isEmpty()) {
                        prompt.append(String.format(" - %s", column.getColumnComment()));
                    }
                    prompt.append("\n");
                }
                prompt.append("\n");
            }
        }
        
        // 表关系信息
        if (!context.getTableRelations().isEmpty()) {
            prompt.append("### 表关系:\n");
            for (Map.Entry<String, List<String>> entry : context.getTableRelations().entrySet()) {
                prompt.append(String.format("- %s 关联表: %s\n", 
                    entry.getKey(), String.join(", ", entry.getValue())));
            }
            prompt.append("\n");
        }
        
        // SQL生成规则
        prompt.append("## SQL生成规则\n");
        prompt.append("1. 只生成SELECT查询语句，不要生成增删改语句\n");
        prompt.append("2. 使用标准SQL语法，兼容MySQL\n");
        prompt.append("3. 字段名和表名使用反引号包围\n");
        prompt.append("4. 如需JOIN，优先使用INNER JOIN\n");
        prompt.append("5. 添加适当的WHERE条件进行数据过滤\n");
        prompt.append("6. 如果涉及统计，使用GROUP BY和聚合函数\n");
        prompt.append("7. 限制结果数量，默认添加LIMIT 100\n\n");
        
        // 用户查询
        prompt.append("## 用户查询\n");
        prompt.append(context.getOriginalQuery()).append("\n\n");
        
        // 要求输出格式
        prompt.append("## 请按以下格式回复\n");
        prompt.append("```sql\n");
        prompt.append("-- 这里是生成的SQL语句\n");
        prompt.append("```\n\n");
        prompt.append("**解释:** 简要说明SQL语句的逻辑和涉及的表/字段。\n");
        
        return prompt.toString();
    }

    /**
     * 解析AI响应
     */
    private SqlGenerationResult parseAiResponse(String aiResponse, VectorSearchService.QueryContext context) {
        SqlGenerationResult result = new SqlGenerationResult();
        result.setOriginalQuery(context.getOriginalQuery());
        result.setDatasourceId(context.getDatasourceId());
        result.setUsedTables(context.getRelevantTables().stream()
            .map(DatabaseSchema::getTableName)
            .distinct()
            .collect(Collectors.toList()));
        
        // 提取SQL语句（在```sql和```之间的内容）
        String sql = extractSqlFromResponse(aiResponse);
        result.setGeneratedSql(sql);
        
        // 提取解释
        String explanation = extractExplanationFromResponse(aiResponse);
        result.setExplanation(explanation);
        
        result.setSuccess(true);
        
        return result;
    }

    /**
     * 从AI响应中提取SQL语句
     */
    private String extractSqlFromResponse(String response) {
        // 查找```sql和```之间的内容
        String sqlPattern = "```sql\\s*\\n([\\s\\S]*?)\\n\\s*```";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(sqlPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(response);
        
        if (matcher.find()) {
            String sql = matcher.group(1).trim();
            // 移除注释行
            return java.util.Arrays.stream(sql.split("\n"))
                .filter(line -> !line.trim().startsWith("--"))
                .collect(Collectors.joining("\n"))
                .trim();
        }
        
        // 如果没有找到代码块，尝试提取看起来像SQL的部分
        String[] lines = response.split("\n");
        StringBuilder sqlBuilder = new StringBuilder();
        boolean inSql = false;
        
        for (String line : lines) {
            String trimmed = line.trim().toUpperCase();
            if (trimmed.startsWith("SELECT")) {
                inSql = true;
                sqlBuilder.append(line).append("\n");
            } else if (inSql) {
                if (trimmed.isEmpty() || trimmed.startsWith("**") || trimmed.startsWith("解释")) {
                    break;
                }
                sqlBuilder.append(line).append("\n");
            }
        }
        
        return sqlBuilder.toString().trim();
    }

    /**
     * 从AI响应中提取解释
     */
    private String extractExplanationFromResponse(String response) {
        // 查找"解释:"或"**解释:**"后的内容
        String explanationPattern = "\\*?\\*?解释\\*?\\*?:?\\s*([\\s\\S]*)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(explanationPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(response);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // 如果没有找到解释标识，返回SQL代码块后的内容
        String[] parts = response.split("```");
        if (parts.length > 2) {
            return parts[2].trim();
        }
        
        return "AI生成了SQL语句，但未提供详细解释。";
    }

    /**
     * 创建错误结果
     */
    private SqlGenerationResult createErrorResult(String originalQuery, String errorMessage) {
        SqlGenerationResult result = new SqlGenerationResult();
        result.setOriginalQuery(originalQuery);
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        result.setGeneratedSql("-- SQL生成失败: " + errorMessage);
        result.setExplanation("抱歉，无法为您的查询生成SQL语句。错误信息: " + errorMessage);
        return result;
    }

    /**
     * SQL生成结果类
     */
    public static class SqlGenerationResult {
        private String originalQuery;
        private Long datasourceId;
        private String generatedSql;
        private String explanation;
        private List<String> usedTables;
        private boolean success;
        private String errorMessage;
        
        // Getters and Setters
        public String getOriginalQuery() { return originalQuery; }
        public void setOriginalQuery(String originalQuery) { this.originalQuery = originalQuery; }
        
        public Long getDatasourceId() { return datasourceId; }
        public void setDatasourceId(Long datasourceId) { this.datasourceId = datasourceId; }
        
        public String getGeneratedSql() { return generatedSql; }
        public void setGeneratedSql(String generatedSql) { this.generatedSql = generatedSql; }
        
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
        
        public List<String> getUsedTables() { return usedTables; }
        public void setUsedTables(List<String> usedTables) { this.usedTables = usedTables; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}