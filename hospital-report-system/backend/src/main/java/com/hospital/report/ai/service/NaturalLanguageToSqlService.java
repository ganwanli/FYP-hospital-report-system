package com.hospital.report.ai.service;

import com.hospital.report.ai.client.DeepSeekClient;
import com.hospital.report.ai.entity.DatabaseSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 自然语言转SQL服务
 */
@Service
@Slf4j
public class NaturalLanguageToSqlService {

    @Autowired
    private VectorStoreService vectorStoreService;
    
    @Autowired
    private EmbeddingService embeddingService;
    
    @Autowired
    private DeepSeekClient deepSeekClient;

    /**
     * 将自然语言转换为SQL查询
     */
    public SqlGenerationResult generateSql(String naturalLanguageQuery, Long datasourceId) {
        log.info("开始生成SQL，查询: {}, 数据源ID: {}", naturalLanguageQuery, datasourceId);
        
        try {
            // 1. 构建查询上下文（只使用Milvus向量搜索）
            MilvusQueryContext context = buildMilvusQueryContext(
                naturalLanguageQuery, datasourceId);
            
            // 2. 构建AI提示词（基于Milvus结果）
            String prompt = buildMilvusSqlGenerationPrompt(context);
            
            // 3. 调用AI生成SQL
            String aiResponse = deepSeekClient.chat(
                List.of(com.hospital.report.ai.client.dto.ChatRequest.ChatMessage.user(prompt))
            ).map(response -> response.getChoices().get(0).getMessage().getContent())
             .block();
            
            // 4. 解析AI响应，提取SQL和解释
            SqlGenerationResult result = parseAiResponse(aiResponse, context);
            
            // 5. 验证生成的SQL中的字段是否存在
            List<String> invalidFields = validateSqlFields(result.getGeneratedSql(), context);
            if (!invalidFields.isEmpty()) {
                log.warn("发现无效字段: {}, 重新生成SQL", invalidFields);
                result = regenerateWithMilvusConstraints(naturalLanguageQuery, context, invalidFields);
            }
            
            log.info("基于Milvus的SQL生成完成: {}", result.getGeneratedSql().substring(0, 
                Math.min(100, result.getGeneratedSql().length())));
            
            return result;
            
        } catch (Exception e) {
            log.error("生成SQL失败: {}", e.getMessage(), e);
            return createErrorResult(naturalLanguageQuery, e.getMessage());
        }
    }

    /**
     * 构建基于Milvus的查询上下文（优化版）
     */
    private MilvusQueryContext buildMilvusQueryContext(String query, Long datasourceId) {
        log.info("开始构建优化的Milvus查询上下文，查询: {}, 数据源ID: {}", query, datasourceId);
        
        try {
            // 1. 生成查询向量
            List<Float> queryEmbedding = embeddingService.generateQueryEmbedding(query);
            if (queryEmbedding.isEmpty()) {
                log.warn("查询向量生成失败");
                return createEmptyMilvusContext(query, datasourceId);
            }
            
            // 2. 从Milvus搜索相关的schema向量（减少搜索数量提高性能）
            List<VectorStoreService.SearchResult> searchResults = vectorStoreService.searchSimilarVectors(
                com.hospital.report.ai.config.MilvusConfig.SCHEMA_COLLECTION, 
                queryEmbedding, 
                30  // 减少搜索数量从50到30
            );
            
            if (searchResults.isEmpty()) {
                log.warn("Milvus搜索结果为空");
                return createEmptyMilvusContext(query, datasourceId);
            }
            
            // 3. 批量转换搜索结果（优化转换过程）
            List<DatabaseSchema> relevantSchemas = searchResults.parallelStream()
                .filter(result -> belongsToDatasource(result, datasourceId))
                .map(this::convertSearchResultToSchemaOptimized)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            if (relevantSchemas.isEmpty()) {
                log.warn("没有找到数据源 {} 的相关schema", datasourceId);
                return createEmptyMilvusContext(query, datasourceId);
            }
            
            // 4. 快速分离表和字段
            Map<Boolean, List<DatabaseSchema>> partitioned = relevantSchemas.stream()
                .collect(Collectors.partitioningBy(schema -> 
                    schema.getColumnName() == null || schema.getColumnName().trim().isEmpty()));
            
            List<DatabaseSchema> tables = partitioned.get(true);
            List<DatabaseSchema> columns = partitioned.get(false);
            
            // 5. 构建查询上下文
            MilvusQueryContext context = new MilvusQueryContext();
            context.setOriginalQuery(query);
            context.setDatasourceId(datasourceId);
            context.setRelevantTables(tables);
            context.setRelevantColumns(columns);
            context.setTableRelations(buildSimpleTableRelations(tables));
            
            log.info("优化的Milvus查询上下文构建完成 - 相关表: {}, 相关字段: {}", 
                tables.size(), columns.size());
            
            return context;
            
        } catch (Exception e) {
            log.error("构建Milvus查询上下文失败: {}", e.getMessage(), e);
            return createEmptyMilvusContext(query, datasourceId);
        }
    }
    
    /**
     * 判断搜索结果是否属于指定数据源
     */
    private boolean belongsToDatasource(VectorStoreService.SearchResult result, Long datasourceId) {
        try {
            String metadata = result.getMetadata();
            if (metadata == null) return false;
            
            return metadata.contains("\"datasourceId\":" + datasourceId);
            
        } catch (Exception e) {
            log.debug("解析metadata失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 优化的搜索结果转换方法
     */
    private DatabaseSchema convertSearchResultToSchemaOptimized(VectorStoreService.SearchResult result) {
        try {
            DatabaseSchema schema = new DatabaseSchema();
            
            // 快速解析source_id
            String sourceId = result.getSourceId();
            if (sourceId != null && sourceId.startsWith("schema_")) {
                try {
                    schema.setId(Long.parseLong(sourceId.substring(7)));
                } catch (NumberFormatException e) {
                    log.debug("解析source_id失败: {}", sourceId);
                }
            }
            
            // 设置内容
            schema.setFullDescription(result.getContent());
            
            // 优化的metadata解析 - 使用缓存的模式
            String metadata = result.getMetadata();
            if (metadata != null) {
                parseMetadataOptimized(metadata, schema);
            }
            
            return schema;
            
        } catch (Exception e) {
            log.warn("转换搜索结果失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 优化的元数据解析方法
     */
    private void parseMetadataOptimized(String metadata, DatabaseSchema schema) {
        try {
            // 批量解析所有需要的字段，减少正则表达式调用次数
            schema.setDatasourceId(parseJsonLongValue(metadata, "datasourceId"));
            schema.setDatabaseName(parseJsonStringValue(metadata, "databaseName"));
            schema.setTableName(parseJsonStringValue(metadata, "tableName"));
            schema.setColumnName(parseJsonStringValue(metadata, "columnName"));
            schema.setColumnType(parseJsonStringValue(metadata, "columnType"));
            schema.setColumnComment(parseJsonStringValue(metadata, "columnComment"));
            schema.setTableComment(parseJsonStringValue(metadata, "tableComment"));
            
            Boolean isPrimaryKey = parseJsonBooleanValue(metadata, "isPrimaryKey");
            if (isPrimaryKey != null) {
                schema.setIsPrimaryKey(isPrimaryKey);
            }
            
        } catch (Exception e) {
            log.debug("优化metadata解析失败: {}", e.getMessage());
        }
    }
    
    /**
     * 简化的表关系构建方法
     */
    private Map<String, List<String>> buildSimpleTableRelations(List<DatabaseSchema> tables) {
        Map<String, List<String>> relations = new HashMap<>();
        
        if (tables.size() < 2) {
            return relations; // 没有足够的表来构建关系
        }
        
        // 简化的关系推断：基于表名相似性
        Set<String> tableNames = tables.stream()
            .map(DatabaseSchema::getTableName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        for (String tableName : tableNames) {
            List<String> relatedTables = tableNames.stream()
                .filter(other -> !other.equals(tableName))
                .filter(other -> areTablesRelatedSimple(tableName, other))
                .limit(3) // 最多关联3个表，避免复杂度过高
                .collect(Collectors.toList());
            
            if (!relatedTables.isEmpty()) {
                relations.put(tableName, relatedTables);
            }
        }
        
        return relations;
    }
    
    /**
     * 简化的表关联判断
     */
    private boolean areTablesRelatedSimple(String table1, String table2) {
        String t1 = table1.toLowerCase();
        String t2 = table2.toLowerCase();
        
        // 快速判断：共同前缀或包含关系
        if (t1.startsWith(t2.substring(0, Math.min(3, t2.length()))) || 
            t2.startsWith(t1.substring(0, Math.min(3, t1.length())))) {
            return true;
        }
        
        // 常见关联词检查（简化版）
        return (t1.contains("patient") && t2.contains("patient")) ||
               (t1.contains("user") && t2.contains("user")) ||
               (t1.contains("sys_") && t2.contains("sys_"));
    }
    
    /**
     * 从JSON字符串中解析Long值
     */
    private Long parseJsonLongValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":([0-9]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return Long.parseLong(m.group(1));
            }
        } catch (Exception e) {
            log.debug("JSON Long解析失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 从JSON字符串中解析String值
     */
    private String parseJsonStringValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":\"([^\"]*)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            log.debug("JSON String解析失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 从JSON字符串中解析Boolean值
     */
    private Boolean parseJsonBooleanValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":(true|false)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return Boolean.parseBoolean(m.group(1));
            }
        } catch (Exception e) {
            log.debug("JSON Boolean解析失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 创建空的Milvus查询上下文
     */
    private MilvusQueryContext createEmptyMilvusContext(String query, Long datasourceId) {
        MilvusQueryContext context = new MilvusQueryContext();
        context.setOriginalQuery(query);
        context.setDatasourceId(datasourceId);
        context.setRelevantTables(new ArrayList<>());
        context.setRelevantColumns(new ArrayList<>());
        context.setTableRelations(new HashMap<>());
        return context;
    }

    /**
     * 构建基于Milvus的SQL生成提示词
     */
    private String buildMilvusSqlGenerationPrompt(MilvusQueryContext context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个专业的SQL生成助手。请根据用户的自然语言查询生成准确的SQL语句。\n\n");
        
        prompt.append("🚨 **重要约束条件：**\n");
        prompt.append("1. **严格限制：只能使用下面明确列出的表名和字段名，不得创造任何字段**\n");
        prompt.append("2. **字段验证：每个字段必须在下面的字段列表中存在**\n");
        prompt.append("3. **表名验证：每个表必须在下面的表列表中存在**\n");
        prompt.append("4. **数据来源：所有信息均来自Milvus向量搜索结果**\n");
        prompt.append("5. **如果找不到合适的字段，明确说明无法生成准确的SQL**\n\n");
        
        // 添加数据库schema信息
        prompt.append("## 📋 可用的数据库结构（来自Milvus向量搜索）\n\n");
        
        // 相关表信息
        if (!context.getRelevantTables().isEmpty()) {
            prompt.append("### 📊 可用表清单:\n");
            for (DatabaseSchema table : context.getRelevantTables()) {
                prompt.append(String.format("✅ **%s**", table.getTableName()));
                if (table.getTableComment() != null && !table.getTableComment().trim().isEmpty()) {
                    prompt.append(String.format(" - %s", table.getTableComment()));
                }
                prompt.append("\n");
            }
            prompt.append("\n");
        }
        
        // 相关字段信息 - 按表分组并详细展示
        if (!context.getRelevantColumns().isEmpty()) {
            prompt.append("### 🏷️ 可用字段清单（按表分组）:\n");
            
            Map<String, List<DatabaseSchema>> columnsByTable = context.getRelevantColumns().stream()
                .collect(Collectors.groupingBy(DatabaseSchema::getTableName));
            
            for (Map.Entry<String, List<DatabaseSchema>> entry : columnsByTable.entrySet()) {
                prompt.append(String.format("\n**📋 表 `%s` 的字段:**\n", entry.getKey()));
                for (DatabaseSchema column : entry.getValue()) {
                    prompt.append("  ✅ `").append(column.getColumnName()).append("`");
                    if (column.getColumnType() != null) {
                        prompt.append(" (").append(column.getColumnType()).append(")");
                    }
                    if (column.getIsPrimaryKey() != null && column.getIsPrimaryKey()) {
                        prompt.append(" 🔑[主键]");
                    }
                    if (column.getColumnComment() != null && !column.getColumnComment().trim().isEmpty()) {
                        prompt.append(" - ").append(column.getColumnComment());
                    }
                    prompt.append("\n");
                }
            }
            prompt.append("\n");
        }
        
        // 表关系信息
        if (!context.getTableRelations().isEmpty()) {
            prompt.append("### 🔗 表关系:\n");
            for (Map.Entry<String, List<String>> entry : context.getTableRelations().entrySet()) {
                prompt.append(String.format("- `%s` 关联表: %s\n", 
                    entry.getKey(), entry.getValue().stream()
                        .map(t -> "`" + t + "`")
                        .collect(Collectors.joining(", "))));
            }
            prompt.append("\n");
        }
        
        // 添加Few-Shot Learning示例
        prompt.append("## 💡 SQL生成示例\n");
        prompt.append("```\n");
        prompt.append("用户查询: 查询患者姓名和年龄\n");
        prompt.append("正确做法: 首先在字段清单中找到 patient_name 和 patient_age 字段\n");
        prompt.append("生成SQL: SELECT `patient_name`, `patient_age` FROM `patients`;\n");
        prompt.append("\n");
        prompt.append("用户查询: 查询订单信息\n");
        prompt.append("错误做法: SELECT order_id, order_date FROM orders; (如果字段清单中没有这些字段)\n");
        prompt.append("正确做法: 根据实际字段清单，如 SELECT `id`, `created_time` FROM `order_table`;\n");
        prompt.append("```\n\n");
        
        // SQL生成规则
        prompt.append("## 📝 SQL生成规则\n");
        prompt.append("1. ✅ 只生成SELECT查询语句，不要生成增删改语句\n");
        prompt.append("2. ✅ 使用标准SQL语法，兼容MySQL\n");
        prompt.append("3. ✅ 字段名和表名必须用反引号包围\n");
        prompt.append("4. ✅ 所有字段必须在上述字段清单中存在\n");
        prompt.append("5. ✅ 所有表名必须在上述表清单中存在\n");
        prompt.append("6. ✅ 如需JOIN，优先使用INNER JOIN\n");
        prompt.append("7. ❌ 不要随意添加WHERE条件，除非用户明确指定过滤条件\n");
        prompt.append("8. ❌ 不要添加ORDER BY子句，除非用户明确要求排序\n");
        prompt.append("9. ❌ 不要假设业务类型、状态等字段的值，除非在字段清单中明确存在\n");
        prompt.append("10. ✅ 如果涉及统计，使用GROUP BY和聚合函数\n");
        prompt.append("11. ✅ 限制结果数量，默认添加LIMIT 100\n\n");
        
        // 用户查询
        prompt.append("## ❓ 用户查询\n");
        prompt.append("**查询内容：** ").append(context.getOriginalQuery()).append("\n\n");
        
        // 要求输出格式
        prompt.append("## 📤 要求的回复格式\n");
        prompt.append("请严格按照以下格式回复：\n\n");
        prompt.append("```sql\n");
        prompt.append("-- 这里是生成的SQL语句（只使用上述字段清单中的字段）\n");
        prompt.append("```\n\n");
        prompt.append("**解释:** 简要说明SQL语句的逻辑，列出使用的表和字段，确认所有字段都在字段清单中存在。\n\n");
        prompt.append("⚠️ **再次提醒：如果在字段清单中找不到合适的字段来满足查询需求，请直接说明无法生成准确的SQL，不要创造字段！**\n");
        
        return prompt.toString();
    }

    /**
     * 解析AI响应
     */
    private SqlGenerationResult parseAiResponse(String aiResponse, MilvusQueryContext context) {
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
     * 验证生成的SQL中的字段是否存在
     */
    private List<String> validateSqlFields(String sql, MilvusQueryContext context) {
        List<String> invalidFields = new ArrayList<>();
        
        try {
            // 获取所有可用的字段名
            Set<String> availableFields = context.getRelevantColumns().stream()
                .map(DatabaseSchema::getColumnName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
            
            // 使用正则表达式提取SQL中的字段名
            // 匹配反引号包围的字段名和表名.字段名的模式
            String fieldPattern = "`([^`]+)`|\\b([a-zA-Z_][a-zA-Z0-9_]*)\\.([a-zA-Z_][a-zA-Z0-9_]*)\\b";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(fieldPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher matcher = pattern.matcher(sql);
            
            Set<String> sqlFields = new HashSet<>();
            while (matcher.find()) {
                String field = null;
                if (matcher.group(1) != null) {
                    // 反引号包围的字段
                    field = matcher.group(1).toLowerCase();
                } else if (matcher.group(3) != null) {
                    // 表名.字段名模式中的字段名
                    field = matcher.group(3).toLowerCase();
                }
                
                if (field != null && !field.equals("*") && 
                    !isReservedKeyword(field) && !isAggregateFunction(field)) {
                    sqlFields.add(field);
                }
            }
            
            // 检查每个字段是否在可用字段列表中
            for (String field : sqlFields) {
                if (!availableFields.contains(field)) {
                    invalidFields.add(field);
                }
            }
            
            log.info("字段验证结果 - SQL字段: {}, 可用字段: {}, 无效字段: {}", 
                sqlFields.size(), availableFields.size(), invalidFields.size());
            
        } catch (Exception e) {
            log.warn("字段验证过程中出现异常: {}", e.getMessage());
        }
        
        return invalidFields;
    }
    
    /**
     * 检查是否为SQL保留关键字
     */
    private boolean isReservedKeyword(String word) {
        Set<String> keywords = Set.of(
            "select", "from", "where", "join", "inner", "left", "right", "on", 
            "group", "by", "order", "having", "limit", "offset", "and", "or", 
            "not", "in", "like", "between", "is", "null", "as", "distinct",
            "count", "sum", "avg", "max", "min", "case", "when", "then", "else", "end"
        );
        return keywords.contains(word.toLowerCase());
    }
    
    /**
     * 检查是否为聚合函数
     */
    private boolean isAggregateFunction(String word) {
        Set<String> functions = Set.of(
            "count", "sum", "avg", "max", "min", "group_concat", "concat", 
            "upper", "lower", "trim", "substring", "now", "date", "year", "month", "day"
        );
        return functions.contains(word.toLowerCase());
    }
    
    /**
     * 基于字段约束重新生成SQL (Milvus版本)
     */
    private SqlGenerationResult regenerateWithMilvusConstraints(String naturalLanguageQuery, 
                                                              MilvusQueryContext context, 
                                                              List<String> invalidFields) {
        try {
            log.info("开始重新生成SQL，限制无效字段: {}", invalidFields);
            
            // 构建更严格的提示词
            String constrainedPrompt = buildMilvusConstrainedRegenerationPrompt(context, invalidFields);
            
            // 调用AI重新生成
            String aiResponse = deepSeekClient.chat(
                List.of(com.hospital.report.ai.client.dto.ChatRequest.ChatMessage.user(constrainedPrompt))
            ).map(response -> response.getChoices().get(0).getMessage().getContent())
             .block();
            
            // 解析新的响应
            SqlGenerationResult result = parseAiResponse(aiResponse, context);
            
            // 再次验证（最多重试一次）
            List<String> stillInvalidFields = validateSqlFields(result.getGeneratedSql(), context);
            if (!stillInvalidFields.isEmpty()) {
                log.warn("重新生成后仍有无效字段: {}, 返回错误结果", stillInvalidFields);
                return createMilvusFieldValidationErrorResult(naturalLanguageQuery, stillInvalidFields, context);
            }
            
            log.info("SQL重新生成成功，已通过字段验证");
            return result;
            
        } catch (Exception e) {
            log.error("重新生成SQL失败: {}", e.getMessage(), e);
            return createMilvusFieldValidationErrorResult(naturalLanguageQuery, invalidFields, context);
        }
    }
    
    /**
     * 构建Milvus约束重新生成的提示词
     */
    private String buildMilvusConstrainedRegenerationPrompt(MilvusQueryContext context, 
                                                      List<String> invalidFields) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("🚨 **字段验证失败，需要重新生成SQL** 🚨\n\n");
        prompt.append("上一次生成的SQL中发现了以下无效字段（这些字段在数据库中不存在）：\n");
        for (String field : invalidFields) {
            prompt.append("❌ `").append(field).append("`\n");
        }
        prompt.append("\n");
        
        prompt.append("**🔍 严格要求：**\n");
        prompt.append("1. 绝对不能使用上述无效字段\n");
        prompt.append("2. 只能使用下面明确列出的字段\n");
        prompt.append("3. 不要添加用户未明确要求的WHERE条件\n");
        prompt.append("4. 不要添加ORDER BY子句，除非用户明确要求排序\n");
        prompt.append("5. 不要假设任何字段的具体值（如business_type、status等）\n");
        prompt.append("6. 如果无法找到合适的字段满足查询需求，请明确说明\n\n");
        
        // 重新显示可用字段（更加强调）
        prompt.append("## ✅ **唯一可用的字段清单**\n\n");
        
        Map<String, List<DatabaseSchema>> columnsByTable = context.getRelevantColumns().stream()
            .collect(Collectors.groupingBy(DatabaseSchema::getTableName));
        
        for (Map.Entry<String, List<DatabaseSchema>> entry : columnsByTable.entrySet()) {
            prompt.append(String.format("### 📋 表 `%s`:\n", entry.getKey()));
            for (DatabaseSchema column : entry.getValue()) {
                prompt.append("  ✅ **`").append(column.getColumnName()).append("`**");
                prompt.append(" (").append(column.getColumnType()).append(")");
                if (column.getColumnComment() != null && !column.getColumnComment().trim().isEmpty()) {
                    prompt.append(" - ").append(column.getColumnComment());
                }
                prompt.append("\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("## ❓ 用户原始查询\n");
        prompt.append("**查询内容：** ").append(context.getOriginalQuery()).append("\n\n");
        
        prompt.append("## 📝 重新生成要求\n");
        prompt.append("请严格基于上述字段清单重新生成SQL，格式如下：\n\n");
        prompt.append("```sql\n");
        prompt.append("-- 重新生成的SQL语句\n");
        prompt.append("```\n\n");
        prompt.append("**解释:** 说明使用的字段和逻辑。\n\n");
        prompt.append("⚠️ **最后提醒：绝对不能创造字段，只能使用上述字段清单中的字段！**\n");
        
        return prompt.toString();
    }
    
    /**
     * 创建Milvus字段验证错误结果
     */
    private SqlGenerationResult createMilvusFieldValidationErrorResult(String originalQuery, 
                                                               List<String> invalidFields, 
                                                               MilvusQueryContext context) {
        SqlGenerationResult result = new SqlGenerationResult();
        result.setOriginalQuery(originalQuery);
        result.setSuccess(false);
        
        String errorMessage = String.format(
            "抱歉，无法为您的查询生成准确的SQL。原因：查询中需要的字段 [%s] 在当前数据库中不存在。\n\n" +
            "建议：\n" +
            "1. 请检查字段名是否正确\n" +
            "2. 可用的字段包括：%s\n" +
            "3. 请重新描述您的查询需求",
            String.join(", ", invalidFields),
            context.getRelevantColumns().stream()
                .map(DatabaseSchema::getColumnName)
                .distinct()
                .collect(Collectors.joining(", "))
        );
        
        result.setErrorMessage(errorMessage);
        result.setGeneratedSql("-- 由于字段验证失败，无法生成SQL");
        result.setExplanation(errorMessage);
        result.setUsedTables(context.getRelevantTables().stream()
            .map(DatabaseSchema::getTableName)
            .distinct()
            .collect(Collectors.toList()));
        
        return result;
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

    /**
     * Milvus查询上下文类（替代VectorSearchService.QueryContext）
     */
    public static class MilvusQueryContext {
        private String originalQuery;
        private Long datasourceId;
        private List<DatabaseSchema> relevantTables;
        private List<DatabaseSchema> relevantColumns;
        private Map<String, List<String>> tableRelations;

        // Getters and Setters
        public String getOriginalQuery() { return originalQuery; }
        public void setOriginalQuery(String originalQuery) { this.originalQuery = originalQuery; }

        public Long getDatasourceId() { return datasourceId; }
        public void setDatasourceId(Long datasourceId) { this.datasourceId = datasourceId; }

        public List<DatabaseSchema> getRelevantTables() { return relevantTables; }
        public void setRelevantTables(List<DatabaseSchema> relevantTables) { this.relevantTables = relevantTables; }

        public List<DatabaseSchema> getRelevantColumns() { return relevantColumns; }
        public void setRelevantColumns(List<DatabaseSchema> relevantColumns) { this.relevantColumns = relevantColumns; }

        public Map<String, List<String>> getTableRelations() { return tableRelations; }
        public void setTableRelations(Map<String, List<String>> tableRelations) { this.tableRelations = tableRelations; }
    }
}