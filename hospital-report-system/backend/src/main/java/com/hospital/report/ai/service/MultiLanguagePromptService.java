package com.hospital.report.ai.service;

import com.hospital.report.ai.enums.AnalysisType;
import com.hospital.report.entity.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 多语言提示词服务
 * 根据用户输入的语言自动选择对应的提示词模板
 */
@Service
@Slf4j
public class MultiLanguagePromptService {
    
    // 中文字符检测正则
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fff]");
    
    /**
     * 语言枚举
     */
    public enum Language {
        CHINESE, ENGLISH
    }
    
    /**
     * 检测文本语言
     */
    public Language detectLanguage(String text) {
        if (!StringUtils.hasText(text)) {
            return Language.CHINESE; // 默认中文
        }
        
        // 检查是否包含中文字符
        if (CHINESE_PATTERN.matcher(text).find()) {
            return Language.CHINESE;
        }
        
        return Language.ENGLISH;
    }
    
    /**
     * 构建系统提示词（根据语言）
     */
    public String buildSystemPrompt(Language language, DataSource dataSource) {
        if (language == Language.ENGLISH) {
            return buildEnglishSystemPrompt(dataSource);
        } else {
            return buildChineseSystemPrompt(dataSource);
        }
    }
    
    /**
     * 构建数据库分析系统提示词
     */
    public String buildDatabaseAnalysisSystemPrompt(Language language) {
        if (language == Language.ENGLISH) {
            return buildEnglishDatabaseAnalysisPrompt();
        } else {
            return buildChineseDatabaseAnalysisPrompt();
        }
    }
    
    /**
     * 构建SQL分析系统提示词
     */
    public String buildSqlAnalysisSystemPrompt(Language language) {
        if (language == Language.ENGLISH) {
            return buildEnglishSqlAnalysisPrompt();
        } else {
            return buildChineseSqlAnalysisPrompt();
        }
    }
    
    /**
     * 构建RAG提示词
     */
    public String buildRAGPrompt(Language language, String naturalLanguageQuery, String context) {
        if (language == Language.ENGLISH) {
            return buildEnglishRAGPrompt(naturalLanguageQuery, context);
        } else {
            return buildChineseRAGPrompt(naturalLanguageQuery, context);
        }
    }
    
    /**
     * 构建SQL分析提示词
     */
    public String buildSqlAnalysisPrompt(Language language, String sqlContent, String technicalAnalysis, AnalysisType analysisType) {
        if (language == Language.ENGLISH) {
            return buildEnglishSqlAnalysisPrompt(sqlContent, technicalAnalysis, analysisType);
        } else {
            return buildChineseSqlAnalysisPrompt(sqlContent, technicalAnalysis, analysisType);
        }
    }
    
    // ================== 中文提示词模板 ==================
    
    private String buildChineseSystemPrompt(DataSource dataSource) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个专业的数据库和SQL专家助手，具备以下能力：\n");
        prompt.append("1. 分析和解释SQL查询语句，提供详细的执行逻辑说明\n");
        prompt.append("2. 提供具体可行的SQL优化建议和改进方案\n");
        prompt.append("3. 分析数据库结构设计，识别潜在问题并提供改进建议\n");
        prompt.append("4. 解答各种数据库相关技术问题\n");
        prompt.append("5. 提供数据查询的最佳实践指导\n\n");
        
        // 添加数据库上下文
        if (dataSource != null) {
            prompt.append("当前连接的数据库环境：\n");
            prompt.append("- 数据库类型: ").append(dataSource.getDatabaseType()).append("\n");
            prompt.append("- 数据库名称: ").append(dataSource.getDatabaseName()).append("\n");
            if (StringUtils.hasText(dataSource.getDescription())) {
                prompt.append("- 描述: ").append(dataSource.getDescription()).append("\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("请用专业、清晰、易懂的语言回答用户问题，在适当时提供具体的代码示例和实用建议。");
        prompt.append("对于SQL优化建议，请给出具体的改进方案和原因说明。");
        
        return prompt.toString();
    }
    
    private String buildChineseDatabaseAnalysisPrompt() {
        return "你是一个资深的数据库架构师和DBA专家。请基于提供的数据库结构信息，从以下维度进行专业分析：\n" +
               "1. 表结构设计评估 - 分析表设计的合理性\n" +
               "2. 索引使用分析 - 评估索引的配置和效率\n" +
               "3. 数据关系梳理 - 分析表之间的关联关系\n" +
               "4. 性能优化建议 - 提供具体的优化方案\n" +
               "5. 规范性检查 - 检查命名规范和设计模式\n" +
               "6. 扩展性评估 - 分析系统的可扩展性\n\n" +
               "请提供详细、实用的分析报告，包含具体的改进建议和实施方案。";
    }
    
    private String buildChineseSqlAnalysisPrompt() {
        return "你是一个SQL优化专家，请基于提供的技术分析结果，从以下角度给出专业建议：\n" +
               "1. SQL执行逻辑解释 - 用通俗易懂的语言解释SQL的执行过程\n" +
               "2. 性能分析 - 分析查询的性能特点和潜在瓶颈\n" +
               "3. 优化建议 - 提供具体的优化方案和改进代码\n" +
               "4. 最佳实践 - 给出相关的编程最佳实践建议\n" +
               "5. 风险提示 - 识别潜在的安全或性能风险\n\n" +
               "请提供实用、可操作的建议，包含具体的SQL改进代码示例。";
    }
    
    private String buildChineseRAGPrompt(String naturalLanguageQuery, String context) {
        return String.format("""
            你是一个专业的SQL生成助手。请基于提供的数据库结构信息，为用户的自然语言查询生成准确的SQL语句。
            
            %s
            
            用户查询: %s
            
            请生成对应的SQL语句，并提供简要说明。要求：
            1. 只使用上述提供的表和字段
            2. 生成标准的SQL语法
            3. 使用反引号包围表名和字段名
            4. 如果查询涉及多个表，使用适当的JOIN语句
            5. 添加合理的LIMIT限制
            
            请按以下格式回复：
            
            ```sql
            -- 生成的SQL语句
            ```
            
            **说明:** 简要解释SQL逻辑和使用的表/字段。
            """, context, naturalLanguageQuery);
    }
    
    private String buildChineseSqlAnalysisPrompt(String sqlContent, String technicalAnalysis, AnalysisType analysisType) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("请分析以下SQL语句：\n\n");
        prompt.append("```sql\n").append(sqlContent).append("\n```\n\n");
        
        if (StringUtils.hasText(technicalAnalysis)) {
            prompt.append("技术分析结果：\n").append(technicalAnalysis).append("\n\n");
        }
        
        prompt.append("请基于以上技术分析结果，用专业且通俗易懂的语言：\n");
        prompt.append("1. 解释SQL的执行逻辑和工作原理\n");
        prompt.append("2. 分析潜在的性能问题和优化空间\n");
        prompt.append("3. 提供具体的优化方案，包含改进后的SQL代码\n");
        prompt.append("4. 给出相关的最佳实践建议\n");
        
        return prompt.toString();
    }
    
    // ================== 英文提示词模板 ==================
    
    private String buildEnglishSystemPrompt(DataSource dataSource) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are a professional database and SQL expert assistant with the following capabilities:\n");
        prompt.append("1. Analyze and explain SQL query statements, providing detailed execution logic explanations\n");
        prompt.append("2. Provide specific and feasible SQL optimization suggestions and improvement plans\n");
        prompt.append("3. Analyze database structure design, identify potential issues and provide improvement suggestions\n");
        prompt.append("4. Answer various database-related technical questions\n");
        prompt.append("5. Provide best practice guidance for data querying\n\n");
        
        // Add database context
        if (dataSource != null) {
            prompt.append("Current connected database environment:\n");
            prompt.append("- Database Type: ").append(dataSource.getDatabaseType()).append("\n");
            prompt.append("- Database Name: ").append(dataSource.getDatabaseName()).append("\n");
            if (StringUtils.hasText(dataSource.getDescription())) {
                prompt.append("- Description: ").append(dataSource.getDescription()).append("\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("Please answer user questions with professional, clear, and understandable language, providing specific code examples and practical suggestions when appropriate. ");
        prompt.append("For SQL optimization suggestions, please provide specific improvement plans and explanations for the reasons.");
        
        return prompt.toString();
    }
    
    private String buildEnglishDatabaseAnalysisPrompt() {
        return "You are a senior database architect and DBA expert. Please conduct professional analysis based on the provided database structure information from the following dimensions:\n" +
               "1. Table Structure Design Assessment - Analyze the rationality of table design\n" +
               "2. Index Usage Analysis - Evaluate the configuration and efficiency of indexes\n" +
               "3. Data Relationship Analysis - Analyze the relationships between tables\n" +
               "4. Performance Optimization Recommendations - Provide specific optimization solutions\n" +
               "5. Compliance Check - Check naming conventions and design patterns\n" +
               "6. Scalability Assessment - Analyze system scalability\n\n" +
               "Please provide detailed and practical analysis reports, including specific improvement suggestions and implementation plans.";
    }
    
    private String buildEnglishSqlAnalysisPrompt() {
        return "You are a SQL optimization expert. Please provide professional advice based on the provided technical analysis results from the following perspectives:\n" +
               "1. SQL Execution Logic Explanation - Explain the SQL execution process in easy-to-understand language\n" +
               "2. Performance Analysis - Analyze query performance characteristics and potential bottlenecks\n" +
               "3. Optimization Suggestions - Provide specific optimization solutions and improved code\n" +
               "4. Best Practices - Provide related programming best practice suggestions\n" +
               "5. Risk Warnings - Identify potential security or performance risks\n\n" +
               "Please provide practical and actionable suggestions, including specific SQL improvement code examples.";
    }
    
    private String buildEnglishRAGPrompt(String naturalLanguageQuery, String context) {
        return String.format("""
            You are a professional SQL generation assistant. Please generate accurate SQL statements for the user's natural language query based on the provided database structure information.
            
            %s
            
            User Query: %s
            
            Please generate the corresponding SQL statement and provide a brief explanation. Requirements:
            1. Only use the tables and fields provided above
            2. Generate standard SQL syntax
            3. Use backticks around table names and field names
            4. If the query involves multiple tables, use appropriate JOIN statements
            5. Add reasonable LIMIT restrictions
            
            Please reply in the following format:
            
            ```sql
            -- Generated SQL statement
            ```
            
            **Explanation:** Briefly explain the SQL logic and the tables/fields used.
            """, context, naturalLanguageQuery);
    }
    
    private String buildEnglishSqlAnalysisPrompt(String sqlContent, String technicalAnalysis, AnalysisType analysisType) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Please analyze the following SQL statement:\n\n");
        prompt.append("```sql\n").append(sqlContent).append("\n```\n\n");
        
        if (StringUtils.hasText(technicalAnalysis)) {
            prompt.append("Technical Analysis Results:\n").append(technicalAnalysis).append("\n\n");
        }
        
        prompt.append("Based on the above technical analysis results, please use professional yet easy-to-understand language to:\n");
        prompt.append("1. Explain the execution logic and working principles of the SQL\n");
        prompt.append("2. Analyze potential performance issues and optimization opportunities\n");
        prompt.append("3. Provide specific optimization solutions, including improved SQL code\n");
        prompt.append("4. Give related best practice suggestions\n");
        
        return prompt.toString();
    }
    
    /**
     * 获取错误消息（多语言）
     */
    public String getErrorMessage(Language language, String errorKey) {
        if (language == Language.ENGLISH) {
            return switch (errorKey) {
                case "ai_service_error" -> "AI assistant service error: ";
                case "sql_generation_failed" -> "Sorry, SQL generation failed. Please check your query or data source configuration. Error: ";
                case "no_relevant_schema" -> "No relevant database structure information found, unable to generate SQL.";
                case "rag_generation_error" -> "Sorry, an error occurred while generating SQL: ";
                default -> "An error occurred: ";
            };
        } else {
            return switch (errorKey) {
                case "ai_service_error" -> "AI助手服务异常: ";
                case "sql_generation_failed" -> "抱歉，SQL生成失败。请检查您的查询或数据源配置。错误信息：";
                case "no_relevant_schema" -> "未找到相关的数据库结构信息，无法生成SQL。";
                case "rag_generation_error" -> "抱歉，生成SQL时发生错误: ";
                default -> "发生错误: ";
            };
        }
    }
    
    /**
     * 获取成功消息（多语言）
     */
    public String getSuccessMessage(Language language, String messageKey) {
        if (language == Language.ENGLISH) {
            return switch (messageKey) {
                case "sql_generation_complete" -> "SQL generation completed";
                case "natural_language_query" -> "**Natural Language Query:** %s\n\n**Generated SQL:**\n```sql\n%s\n```\n\n**Explanation:** %s\n\n**Tables Used:** %s";
                case "rag_complete" -> "RAG SQL generation completed";
                default -> "Operation completed";
            };
        } else {
            return switch (messageKey) {
                case "sql_generation_complete" -> "SQL生成完成";
                case "natural_language_query" -> "**自然语言查询：** %s\n\n**生成的SQL：**\n```sql\n%s\n```\n\n**说明：** %s\n\n**使用的表：** %s";
                case "rag_complete" -> "RAG生成SQL完成";
                default -> "操作完成";
            };
        }
    }
}