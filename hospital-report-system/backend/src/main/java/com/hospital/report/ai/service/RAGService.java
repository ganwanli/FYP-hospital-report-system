package com.hospital.report.ai.service;

import com.hospital.report.ai.entity.DatabaseSchema;
import com.hospital.report.ai.service.MultiLanguagePromptService;
import com.hospital.report.ai.config.MilvusConfig;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * RAG服务 - 使用LangChain4J + Milvus实现检索增强生成
 */
@Service
@Slf4j
public class RAGService {

    @Autowired
    private EmbeddingModel qwenEmbeddingModel;
    
    @Autowired
    private ChatLanguageModel qwenChatLanguageModel;
    
    @Autowired
    private DatabaseSchemaService databaseSchemaService;
    
    @Autowired
    private EmbeddingService embeddingService;
    
    @Autowired
    private MultiLanguagePromptService multiLanguagePromptService;
    
    @Autowired
    private VectorStoreService vectorStoreService;
    
    @Autowired
    private SqlKnowledgeBaseService sqlKnowledgeBaseService;

    /**
     * 基于查询从Milvus检索相关的数据库schema信息
     */
    public List<DatabaseSchema> retrieveRelevantSchemas(String query, Long datasourceId, int maxResults, double minScore) {
        try {
            log.info("从Milvus检索相关schema，查询: {}, 最大结果数: {}", query, maxResults);
            
            // 生成查询向量
            List<Float> queryEmbedding = embeddingService.generateQueryEmbedding(query);
            
            if (queryEmbedding.isEmpty()) {
                log.warn("查询向量生成失败，使用传统方式检索");
                return retrieveRelevantSchemasTraditional(query, datasourceId, maxResults, minScore);
            }
            
            // 从Milvus搜索相似向量
            List<VectorStoreService.SearchResult> searchResults = vectorStoreService.searchSimilarVectors(
                MilvusConfig.SCHEMA_COLLECTION, queryEmbedding, maxResults
            );
            
            // 转换为DatabaseSchema对象
            List<DatabaseSchema> results = new ArrayList<>();
            for (VectorStoreService.SearchResult result : searchResults) {
                if (result.getScore() >= minScore) {
                    // 从sourceId提取schema ID
                    String sourceId = result.getSourceId();
                    if (sourceId != null && sourceId.startsWith("schema_")) {
                        try {
                            Long schemaId = Long.parseLong(sourceId.substring(7));
                            DatabaseSchema schema = databaseSchemaService.getSchemasByDatasourceId(datasourceId)
                                .stream()
                                .filter(s -> s.getId().equals(schemaId))
                                .findFirst()
                                .orElse(null);
                            if (schema != null && schema.getDatasourceId().equals(datasourceId)) {
                                results.add(schema);
                            }
                        } catch (NumberFormatException e) {
                            log.warn("解析schema ID失败: {}", sourceId);
                        }
                    }
                }
            }
            
            log.info("从Milvus检索到 {} 个相关schema", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("从Milvus检索相关schema失败，回退到传统方式", e);
            return retrieveRelevantSchemasTraditional(query, datasourceId, maxResults, minScore);
        }
    }
    
    /**
     * 传统方式检索schema（回退方案）
     */
    private List<DatabaseSchema> retrieveRelevantSchemasTraditional(String query, Long datasourceId, int maxResults, double minScore) {
        try {
            log.info("使用传统方式检索相关schema");
            
            // 获取所有schema
            List<DatabaseSchema> allSchemas = databaseSchemaService.getSchemasByDatasourceId(datasourceId);
            
            // 生成查询向量
            List<Float> queryEmbedding = embeddingService.generateQueryEmbedding(query);
            
            if (queryEmbedding.isEmpty()) {
                log.warn("查询向量生成失败，返回默认schema");
                return allSchemas.stream().limit(maxResults).collect(Collectors.toList());
            }
            
            // 转换为Double向量用于兼容
            List<Double> queryEmbeddingDouble = queryEmbedding.stream()
                .map(Float::doubleValue)
                .collect(Collectors.toList());
            
            // 计算相似度并排序
            return allSchemas.stream()
                .filter(schema -> schema.getEmbedding() != null)
                .map(schema -> {
                    List<Double> schemaEmbedding = embeddingService.parseEmbeddingFromJson(schema.getEmbedding());
                    if (schemaEmbedding != null && schemaEmbedding.size() == queryEmbeddingDouble.size()) {
                        double similarity = embeddingService.calculateCosineSimilarity(queryEmbeddingDouble, schemaEmbedding);
                        return new SchemaWithScore(schema, similarity);
                    }
                    return new SchemaWithScore(schema, 0.0);
                })
                .filter(item -> item.score >= minScore)
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(maxResults)
                .map(item -> item.schema)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("传统方式检索相关schema失败", e);
            return List.of();
        }
    }

    /**
     * 使用RAG生成自然语言到SQL的回答（增强版：结合Schema + SQL知识库）
     */
    public String generateSQLWithRAG(String naturalLanguageQuery, Long datasourceId) {
        try {
            log.info("使用RAG生成SQL，查询: {}", naturalLanguageQuery);
            
            // 1. 检测语言
            MultiLanguagePromptService.Language language = multiLanguagePromptService.detectLanguage(naturalLanguageQuery);
            
            // 2. 并行检索相关信息
            CompletableFuture<List<DatabaseSchema>> schemaFuture = CompletableFuture.supplyAsync(() -> 
                retrieveRelevantSchemas(naturalLanguageQuery, datasourceId, 10, 0.3)
            );
            
            CompletableFuture<List<SqlKnowledgeBaseService.SqlSearchResult>> sqlFuture = CompletableFuture.supplyAsync(() -> 
                sqlKnowledgeBaseService.searchSimilarSql(naturalLanguageQuery, 5)
            );
            
            // 3. 等待检索完成
            List<DatabaseSchema> relevantSchemas = schemaFuture.join();
            List<SqlKnowledgeBaseService.SqlSearchResult> relevantSqls = sqlFuture.join();
            
            // 4. 构建增强上下文
            String context = buildEnhancedRAGContext(relevantSchemas, relevantSqls, language);
            
            // 5. 构建多语言提示词
            String prompt = multiLanguagePromptService.buildRAGPrompt(language, naturalLanguageQuery, context);
            
            // 6. 使用千问生成回答
            String response = qwenChatLanguageModel.generate(prompt);
            
            log.info("RAG生成SQL完成，语言: {}，使用了 {} 个schema和 {} 个SQL示例", 
                language, relevantSchemas.size(), relevantSqls.size());
            return response;
            
        } catch (Exception e) {
            log.error("RAG生成SQL失败", e);
            MultiLanguagePromptService.Language language = multiLanguagePromptService.detectLanguage(naturalLanguageQuery);
            return multiLanguagePromptService.getErrorMessage(language, "rag_generation_error") + e.getMessage();
        }
    }

    /**
     * 构建增强RAG上下文（Schema + SQL示例）
     */
    private String buildEnhancedRAGContext(List<DatabaseSchema> schemas, List<SqlKnowledgeBaseService.SqlSearchResult> sqlResults, MultiLanguagePromptService.Language language) {
        StringBuilder context = new StringBuilder();
        
        // 数据库结构信息
        if (!schemas.isEmpty()) {
            String schemaHeaderText = language == MultiLanguagePromptService.Language.ENGLISH 
                ? "Relevant database structure information:\n\n" 
                : "相关数据库结构信息:\n\n";
            context.append(schemaHeaderText);
            
            String tableLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "- Table: " : "- 表: ";
            String fieldLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "  Field: " : "  字段: ";
            
            for (DatabaseSchema schema : schemas) {
                context.append(tableLabel).append(schema.getTableName());
                if (schema.getTableComment() != null) {
                    context.append(" (").append(schema.getTableComment()).append(")");
                }
                context.append("\n");
                
                if (schema.getColumnName() != null) {
                    context.append(fieldLabel).append(schema.getColumnName())
                           .append(" (").append(schema.getColumnType()).append(")");
                    if (schema.getColumnComment() != null) {
                        context.append(" - ").append(schema.getColumnComment());
                    }
                    context.append("\n");
                }
            }
            context.append("\n");
        }
        
        // SQL示例信息
        if (!sqlResults.isEmpty()) {
            String sqlHeaderText = language == MultiLanguagePromptService.Language.ENGLISH 
                ? "Relevant SQL examples:\n\n" 
                : "相关SQL示例:\n\n";
            context.append(sqlHeaderText);
            
            for (int i = 0; i < sqlResults.size(); i++) {
                SqlKnowledgeBaseService.SqlSearchResult result = sqlResults.get(i);
                String exampleLabel = language == MultiLanguagePromptService.Language.ENGLISH 
                    ? String.format("Example %d (similarity: %.2f):\n", i + 1, result.getScore())
                    : String.format("示例 %d (相似度: %.2f):\n", i + 1, result.getScore());
                
                context.append(exampleLabel);
                context.append("Template: ").append(result.getTemplateName()).append("\n");
                if (result.getContent() != null) {
                    // 只显示SQL部分，去掉其他描述信息
                    String[] lines = result.getContent().split("\n");
                    for (String line : lines) {
                        if (line.trim().toLowerCase().startsWith("sql") && line.contains(":")) {
                            context.append(line.trim()).append("\n");
                            break;
                        }
                    }
                }
                context.append("\n");
            }
        }
        
        return context.toString();
    }

    /**
     * 构建RAG上下文（保持向后兼容）
     */
    private String buildRAGContext(List<DatabaseSchema> schemas) {
        return buildEnhancedRAGContext(schemas, new ArrayList<>(), MultiLanguagePromptService.Language.CHINESE);
    }
    
    /**
     * 测试RAG功能（增强版）
     */
    public boolean testRAGFunction(Long datasourceId) {
        try {
            // 测试Schema检索
            List<DatabaseSchema> schemas = retrieveRelevantSchemas("用户信息", datasourceId, 5, 0.2);
            
            // 测试SQL检索
            List<SqlKnowledgeBaseService.SqlSearchResult> sqlResults = 
                sqlKnowledgeBaseService.searchSimilarSql("查询用户信息", 3);
            
            boolean success = !schemas.isEmpty() || !sqlResults.isEmpty();
            log.info("RAG功能测试{}，检索到 {} 个相关schema和 {} 个SQL示例", 
                success ? "成功" : "失败", schemas.size(), sqlResults.size());
            
            return success;
            
        } catch (Exception e) {
            log.error("RAG功能测试失败", e);
            return false;
        }
    }
    
    /**
     * Schema与评分的内部类
     */
    private static class SchemaWithScore {
        final DatabaseSchema schema;
        final double score;
        
        SchemaWithScore(DatabaseSchema schema, double score) {
            this.schema = schema;
            this.score = score;
        }
    }
}