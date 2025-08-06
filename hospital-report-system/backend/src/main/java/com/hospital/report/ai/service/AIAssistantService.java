package com.hospital.report.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.report.ai.client.DeepSeekClient;
import com.hospital.report.ai.client.dto.ChatRequest;
import com.hospital.report.ai.client.dto.ChatResponse;
import com.hospital.report.ai.entity.AIConversation;
import com.hospital.report.ai.entity.AIMessage;
import com.hospital.report.ai.entity.SqlAnalysisLog;
import com.hospital.report.ai.entity.dto.*;
import com.hospital.report.ai.enums.AnalysisType;
import com.hospital.report.ai.enums.MessageType;
import com.hospital.report.ai.mapper.SqlAnalysisLogMapper;
import com.hospital.report.entity.DataSource;
import com.hospital.report.service.DataSourceService;
import com.hospital.report.ai.service.NaturalLanguageToSqlService;
import com.hospital.report.ai.service.MultiLanguagePromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@Transactional
public class AIAssistantService {
    
    private final DeepSeekClient deepSeekClient;
    private final DatabaseSchemaAnalyzer schemaAnalyzer;
    private final SqlAnalyzer sqlAnalyzer;
    private final ConversationService conversationService;
    private final DataSourceService dataSourceService;
    private final NaturalLanguageToSqlService naturalLanguageToSqlService;
    private final SqlAnalysisLogMapper sqlAnalysisLogMapper;
    private final MultiLanguagePromptService multiLanguagePromptService;
    private final ObjectMapper objectMapper;
    
    public AIAssistantService(DeepSeekClient deepSeekClient,
                             DatabaseSchemaAnalyzer schemaAnalyzer,
                             SqlAnalyzer sqlAnalyzer,
                             ConversationService conversationService,
                             DataSourceService dataSourceService,
                             NaturalLanguageToSqlService naturalLanguageToSqlService,
                             SqlAnalysisLogMapper sqlAnalysisLogMapper,
                             MultiLanguagePromptService multiLanguagePromptService) {
        this.deepSeekClient = deepSeekClient;
        this.schemaAnalyzer = schemaAnalyzer;
        this.sqlAnalyzer = sqlAnalyzer;
        this.conversationService = conversationService;
        this.dataSourceService = dataSourceService;
        this.naturalLanguageToSqlService = naturalLanguageToSqlService;
        this.sqlAnalysisLogMapper = sqlAnalysisLogMapper;
        this.multiLanguagePromptService = multiLanguagePromptService;
        this.objectMapper = new ObjectMapper();
    }
    
    public AIAssistantResponse chat(AIAssistantRequest request) {
        try {
            // 1. 获取或创建对话
            AIConversation conversation = getOrCreateConversation(request);
            
            // 2. 构建上下文消息
            List<ChatRequest.ChatMessage> messages = buildContextMessages(conversation, request);
            
            // 3. 调用AI接口
            ChatResponse response = deepSeekClient.chat(messages).block();
            
            if (response == null || response.getChoices().isEmpty()) {
                return AIAssistantResponse.error("AI服务响应异常");
            }
            
            String aiResponse = response.getChoices().get(0).getMessage().getContent();
            Integer tokensUsed = (response.getUsage() != null && response.getUsage().getTotalTokens() != null) 
                ? response.getUsage().getTotalTokens() : 0;
            
            // 4. 保存对话记录
            conversationService.saveMessage(conversation.getId(), MessageType.USER, request.getMessage());
            conversationService.saveMessage(conversation.getId(), MessageType.ASSISTANT, aiResponse, null, tokensUsed);
            
            // 5. 构建响应
            AIAssistantResponse assistantResponse = AIAssistantResponse.success(conversation.getId(), aiResponse);
            assistantResponse.setTokenUsed(tokensUsed);
            
            log.info("AI对话成功，对话ID: {}, Token使用: {}", conversation.getId(), tokensUsed);
            return assistantResponse;
            
        } catch (Exception e) {
            log.error("AI助手聊天失败", e);
            return AIAssistantResponse.error("AI助手服务异常: " + e.getMessage());
        }
    }
    
    public Flux<String> chatStream(AIAssistantRequest request) {
        return Flux.create(sink -> {
            try {
                // 1. 获取或创建对话
                AIConversation conversation = getOrCreateConversation(request);
                
                // 2. 检查是否是自然语言转SQL请求
                if ("NATURAL_LANGUAGE_TO_SQL".equals(request.getAnalysisType()) && 
                    request.getOriginalQuery() != null && 
                    request.getDatasourceId() != null) {
                    
                    handleNaturalLanguageToSqlStream(conversation, request, sink);
                    return;
                }
                
                // 3. 构建上下文消息
                List<ChatRequest.ChatMessage> messages = buildContextMessages(conversation, request);
                
                // 4. 保存用户消息
                conversationService.saveMessage(conversation.getId(), MessageType.USER, request.getMessage());
                
                // 5. 调用流式AI接口
                StringBuilder fullResponse = new StringBuilder();
                
                deepSeekClient.chatStream(messages)
                    .doOnNext(chunk -> {
                        fullResponse.append(chunk);
                        sink.next(chunk);
                    })
                    .doOnComplete(() -> {
                        // 保存完整的AI响应
                        conversationService.saveMessage(conversation.getId(), MessageType.ASSISTANT, fullResponse.toString());
                        sink.complete();
                        log.info("流式AI对话完成，对话ID: {}", conversation.getId());
                    })
                    .doOnError(error -> {
                        log.error("流式AI对话失败", error);
                        sink.error(error);
                    })
                    .subscribe();
                    
            } catch (Exception e) {
                log.error("流式聊天初始化失败", e);
                sink.error(e);
            }
        });
    }
    
    public AIAssistantResponse analyzeDatabaseSchema(Long conversationId, Long datasourceId) {
        return analyzeDatabaseSchema(conversationId, datasourceId, null);
    }
    
    public AIAssistantResponse analyzeDatabaseSchema(Long conversationId, Long datasourceId, String userQuery) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 检测语言
            MultiLanguagePromptService.Language language = MultiLanguagePromptService.Language.CHINESE; // 默认中文
            if (StringUtils.hasText(userQuery)) {
                language = multiLanguagePromptService.detectLanguage(userQuery);
            }
            
            // 2. 分析数据库结构
            DatabaseSchemaInfo schemaInfo = schemaAnalyzer.analyzeDatabaseSchema(datasourceId);
            String schemaDescription = schemaAnalyzer.generateSchemaDescription(schemaInfo);
            
            // 3. 构建多语言AI分析请求
            String systemPrompt = multiLanguagePromptService.buildDatabaseAnalysisSystemPrompt(language);
            String userPrompt = language == MultiLanguagePromptService.Language.ENGLISH 
                ? "Please analyze the following database structure and provide a professional evaluation report:\n\n" + schemaDescription
                : "请分析以下数据库结构并提供专业的评估报告：\n\n" + schemaDescription;
            
            List<ChatRequest.ChatMessage> messages = Arrays.asList(
                ChatRequest.ChatMessage.system(systemPrompt),
                ChatRequest.ChatMessage.user(userPrompt)
            );
            
            // 4. 调用AI分析
            ChatResponse response = deepSeekClient.chat(messages).block();
            
            if (response == null || response.getChoices().isEmpty()) {
                String errorMsg = multiLanguagePromptService.getErrorMessage(language, "ai_service_error") + "数据库分析服务异常";
                return AIAssistantResponse.error(errorMsg);
            }
            
            String analysis = response.getChoices().get(0).getMessage().getContent();
            Integer tokensUsed = (response.getUsage() != null && response.getUsage().getTotalTokens() != null) 
                ? response.getUsage().getTotalTokens() : 0;
            
            // 5. 保存分析结果
            if (conversationId != null) {
                String systemMessage = language == MultiLanguagePromptService.Language.ENGLISH 
                    ? "Database structure analysis" : "数据库结构分析";
                conversationService.saveMessage(conversationId, MessageType.SYSTEM, systemMessage);
                conversationService.saveMessage(conversationId, MessageType.ASSISTANT, analysis, null, tokensUsed);
            }
            
            // 6. 准备响应数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("schemaInfo", schemaInfo);
            metadata.put("analysisType", "DATABASE_SCHEMA");
            metadata.put("language", language.name());
            metadata.put("executionTime", System.currentTimeMillis() - startTime);
            
            AIAssistantResponse assistantResponse = AIAssistantResponse.success(conversationId, analysis);
            assistantResponse.setMetadata(metadata);
            assistantResponse.setTokenUsed(tokensUsed);
            
            log.info("数据库结构分析完成，数据源ID: {}, 语言: {}, 执行时间: {}ms", datasourceId, language, System.currentTimeMillis() - startTime);
            return assistantResponse;
            
        } catch (Exception e) {
            log.error("数据库结构分析失败，数据源ID: {}", datasourceId, e);
            return AIAssistantResponse.error("数据库结构分析失败: " + e.getMessage());
        }
    }
    
    public AIAssistantResponse analyzeSql(Long conversationId, String sqlContent, Long datasourceId, AnalysisType analysisType) {
        return analyzeSql(conversationId, sqlContent, datasourceId, analysisType, null);
    }
    
    public AIAssistantResponse analyzeSql(Long conversationId, String sqlContent, Long datasourceId, AnalysisType analysisType, String userQuery) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 检测语言
            MultiLanguagePromptService.Language language = MultiLanguagePromptService.Language.CHINESE; // 默认中文
            if (StringUtils.hasText(userQuery)) {
                language = multiLanguagePromptService.detectLanguage(userQuery);
            } else if (StringUtils.hasText(sqlContent)) {
                // 如果没有用户查询，尝试从SQL注释中检测语言
                language = multiLanguagePromptService.detectLanguage(sqlContent);
            }
            
            // 2. SQL技术分析
            SqlAnalysisResult analysisResult = sqlAnalyzer.analyzeSql(sqlContent, datasourceId, analysisType);
            
            // 3. 构建多语言AI分析请求
            String systemPrompt = multiLanguagePromptService.buildSqlAnalysisSystemPrompt(language);
            String technicalAnalysisText = buildTechnicalAnalysisText(analysisResult, analysisType, language);
            String analysisPrompt = multiLanguagePromptService.buildSqlAnalysisPrompt(language, sqlContent, technicalAnalysisText, analysisType);
            
            List<ChatRequest.ChatMessage> messages = Arrays.asList(
                ChatRequest.ChatMessage.system(systemPrompt),
                ChatRequest.ChatMessage.user(analysisPrompt)
            );
            
            // 4. 调用AI分析
            ChatResponse response = deepSeekClient.chat(messages).block();
            
            if (response == null || response.getChoices().isEmpty()) {
                String errorMsg = multiLanguagePromptService.getErrorMessage(language, "ai_service_error") + "SQL分析服务异常";
                return AIAssistantResponse.error(errorMsg);
            }
            
            String aiAnalysis = response.getChoices().get(0).getMessage().getContent();
            Integer tokensUsed = (response.getUsage() != null && response.getUsage().getTotalTokens() != null) 
                ? response.getUsage().getTotalTokens() : 0;
            
            // 5. 保存分析日志
            saveSqlAnalysisLog(conversationId, sqlContent, analysisType, analysisResult, aiAnalysis, datasourceId, System.currentTimeMillis() - startTime);
            
            // 6. 保存对话记录
            if (conversationId != null) {
                String userMessage = language == MultiLanguagePromptService.Language.ENGLISH 
                    ? "SQL Analysis: " + sqlContent : "SQL分析: " + sqlContent;
                conversationService.saveMessage(conversationId, MessageType.USER, userMessage);
                conversationService.saveMessage(conversationId, MessageType.ASSISTANT, aiAnalysis, null, tokensUsed);
            }
            
            // 7. 准备响应数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sqlAnalysis", analysisResult);
            metadata.put("analysisType", analysisType.name());
            metadata.put("sqlContent", sqlContent);
            metadata.put("language", language.name());
            metadata.put("executionTime", System.currentTimeMillis() - startTime);
            
            AIAssistantResponse assistantResponse = AIAssistantResponse.success(conversationId, aiAnalysis);
            assistantResponse.setMetadata(metadata);
            assistantResponse.setTokenUsed(tokensUsed);
            
            log.info("SQL分析完成，类型: {}, 语言: {}, 执行时间: {}ms", analysisType, language, System.currentTimeMillis() - startTime);
            return assistantResponse;
            
        } catch (Exception e) {
            log.error("SQL分析失败，类型: {}", analysisType, e);
            
            // 保存失败日志
            try {
                SqlAnalysisLog errorLog = new SqlAnalysisLog();
                errorLog.setConversationId(conversationId);
                errorLog.setSqlContent(sqlContent);
                errorLog.setAnalysisType(analysisType);
                errorLog.setStatus("FAILED");
                errorLog.setErrorMessage(e.getMessage());
                errorLog.setDatasourceId(datasourceId);
                errorLog.setExecutionTime(System.currentTimeMillis() - startTime);
                errorLog.setCreatedTime(LocalDateTime.now());
                sqlAnalysisLogMapper.insert(errorLog);
            } catch (Exception logError) {
                log.error("保存SQL分析失败日志时出错", logError);
            }
            
            return AIAssistantResponse.error("SQL分析失败: " + e.getMessage());
        }
    }
    
    private AIConversation getOrCreateConversation(AIAssistantRequest request) {
        if (request.getConversationId() != null) {
            return conversationService.getConversation(request.getConversationId());
        } else {
            String title = conversationService.generateConversationTitle(request.getMessage());
            return conversationService.createConversation(request.getUserId(), title, request.getDatasourceId());
        }
    }
    
    private List<ChatRequest.ChatMessage> buildContextMessages(AIConversation conversation, AIAssistantRequest request) {
        List<ChatRequest.ChatMessage> messages = new ArrayList<>();
        
        // 1. 检测用户消息的语言
        MultiLanguagePromptService.Language language = multiLanguagePromptService.detectLanguage(request.getMessage());
        
        // 2. 添加多语言系统提示
        DataSource dataSource = null;
        if (conversation.getDatasourceId() != null) {
            try {
                dataSource = dataSourceService.getById(conversation.getDatasourceId());
            } catch (Exception e) {
                log.warn("获取数据源信息失败", e);
            }
        }
        
        String systemPrompt = multiLanguagePromptService.buildSystemPrompt(language, dataSource);
        messages.add(ChatRequest.ChatMessage.system(systemPrompt));
        
        // 3. 添加历史对话（最近10条）
        List<AIMessage> recentMessages = conversationService.getRecentMessages(conversation.getId(), 10);
        for (AIMessage message : recentMessages) {
            if (message.getMessageType() == MessageType.USER) {
                messages.add(ChatRequest.ChatMessage.user(message.getContent()));
            } else if (message.getMessageType() == MessageType.ASSISTANT) {
                messages.add(ChatRequest.ChatMessage.assistant(message.getContent()));
            }
        }
        
        // 4. 添加当前用户消息
        messages.add(ChatRequest.ChatMessage.user(request.getMessage()));
        
        return messages;
    }
    
    private String formatJson(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
    
    private void saveSqlAnalysisLog(Long conversationId, String sqlContent, AnalysisType analysisType, 
                                   SqlAnalysisResult analysisResult, String aiSuggestions, Long datasourceId, Long executionTime) {
        try {
            SqlAnalysisLog log = new SqlAnalysisLog();
            log.setConversationId(conversationId);
            log.setSqlContent(sqlContent);
            log.setAnalysisType(analysisType);
            log.setAnalysisResult(objectMapper.writeValueAsString(analysisResult));
            log.setAiSuggestions(aiSuggestions);
            log.setDatasourceId(datasourceId);
            log.setExecutionTime(executionTime);
            log.setStatus("SUCCESS");
            log.setCreatedTime(LocalDateTime.now());
            
            sqlAnalysisLogMapper.insert(log);
        } catch (Exception e) {
            log.error("保存SQL分析日志失败", e);
        }
    }
    
    /**
     * 处理自然语言转SQL的流式响应
     */
    private void handleNaturalLanguageToSqlStream(AIConversation conversation, AIAssistantRequest request, 
                                                  reactor.core.publisher.FluxSink<String> sink) {
        try {
            log.info("开始处理自然语言转SQL请求，对话ID: {}, 数据源ID: {}", conversation.getId(), request.getDatasourceId());
            
            // 1. 检测语言
            MultiLanguagePromptService.Language language = multiLanguagePromptService.detectLanguage(request.getOriginalQuery());
            
            // 2. 保存用户消息（使用原始查询）
            conversationService.saveMessage(conversation.getId(), MessageType.USER, request.getOriginalQuery());
            
            // 3. 调用自然语言转SQL服务，获取结果
            var sqlResult = naturalLanguageToSqlService.generateSql(request.getOriginalQuery(), request.getDatasourceId());
            
            // 4. 构建多语言流式响应内容
            String formattedResponse;
            if (language == MultiLanguagePromptService.Language.ENGLISH) {
                formattedResponse = String.format(
                    "**Natural Language Query:** %s\n\n**Generated SQL:**\n```sql\n%s\n```\n\n**Explanation:** %s\n\n**Tables Used:** %s",
                    sqlResult.getOriginalQuery(),
                    sqlResult.getGeneratedSql(),
                    sqlResult.getExplanation(),
                    sqlResult.getUsedTables() != null ? String.join(", ", sqlResult.getUsedTables()) : "None"
                );
            } else {
                formattedResponse = String.format(
                    multiLanguagePromptService.getSuccessMessage(language, "natural_language_query"),
                    sqlResult.getOriginalQuery(),
                    sqlResult.getGeneratedSql(),
                    sqlResult.getExplanation(),
                    sqlResult.getUsedTables() != null ? String.join(", ", sqlResult.getUsedTables()) : "无"
                );
            }
            
            // 5. 优化的流式输出 - 更明显的流式效果
            StringBuilder responseBuilder = new StringBuilder();
            
            // 先发送开始标识
            sink.next("**自然语言查询：** " + sqlResult.getOriginalQuery() + "\n\n");
            Thread.sleep(200);
            
            sink.next("**生成的SQL：**\n```sql\n");
            Thread.sleep(150);
            
            // 逐行发送SQL内容
            String[] sqlLines = sqlResult.getGeneratedSql().split("\n");
            for (String line : sqlLines) {
                sink.next(line + "\n");
                Thread.sleep(120); // SQL每行间隔
            }
            
            sink.next("```\n\n");
            Thread.sleep(150);
            
            sink.next("**解释：** ");
            Thread.sleep(100);
            
            // 逐句发送解释内容
            String explanation = sqlResult.getExplanation();
            String[] sentences = explanation.split("(?<=[。！？；])|(?<=\\. )|(?<=! )|(?<=\\? )");
            for (String sentence : sentences) {
                if (!sentence.trim().isEmpty()) {
                    sink.next(sentence);
                    Thread.sleep(150); // 每句话间隔
                }
            }
            
            sink.next("\n\n**使用的表：** " + 
                (sqlResult.getUsedTables() != null ? String.join(", ", sqlResult.getUsedTables()) : "无"));
            
            // 构建完整响应用于保存
            String fullResponse = String.format(
                "**自然语言查询：** %s\n\n**生成的SQL：**\n```sql\n%s\n```\n\n**解释：** %s\n\n**使用的表：** %s",
                sqlResult.getOriginalQuery(),
                sqlResult.getGeneratedSql(),
                sqlResult.getExplanation(),
                sqlResult.getUsedTables() != null ? String.join(", ", sqlResult.getUsedTables()) : "无"
            );
            
            // 6. 保存完整的AI响应
            conversationService.saveMessage(conversation.getId(), MessageType.ASSISTANT, fullResponse);
            
            // 7. 完成流式响应
            sink.complete();
            log.info("自然语言转SQL流式响应完成，对话ID: {}, 语言: {}", conversation.getId(), language);
            
        } catch (Exception e) {
            log.error("处理自然语言转SQL流式请求失败", e);
            
            // 检测语言以提供合适的错误消息
            MultiLanguagePromptService.Language language = multiLanguagePromptService.detectLanguage(request.getOriginalQuery());
            String errorMessage = multiLanguagePromptService.getErrorMessage(language, "sql_generation_failed") + e.getMessage();
            
            sink.next(errorMessage);
            
            // 保存错误响应
            conversationService.saveMessage(conversation.getId(), MessageType.ASSISTANT, errorMessage);
            
            sink.complete();
        }
    }
    
    /**
     * 构建技术分析文本（多语言）
     */
    private String buildTechnicalAnalysisText(SqlAnalysisResult analysisResult, AnalysisType analysisType, MultiLanguagePromptService.Language language) {
        StringBuilder text = new StringBuilder();
        
        String executionPlanLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "## Execution Plan" : "## 执行计划";
        String keyInsightsLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "## Key Insights" : "## 关键洞察";
        String optimizationLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "## Optimization Suggestions" : "## 优化建议";
        String performanceLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "## Performance Metrics" : "## 性能指标";
        String bottlenecksLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "## Performance Bottlenecks" : "## 性能瓶颈";
        String securityLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "## Security Issues" : "## 安全问题";
        
        if (analysisType == AnalysisType.SQL_EXPLAIN && analysisResult.getExecutionPlan() != null) {
            text.append(executionPlanLabel).append("\n");
            text.append("```json\n").append(formatJson(analysisResult.getExecutionPlan())).append("\n```\n\n");
            
            if (analysisResult.getInsights() != null && !analysisResult.getInsights().isEmpty()) {
                text.append(keyInsightsLabel).append("\n");
                analysisResult.getInsights().forEach(insight -> text.append("- ").append(insight).append("\n"));
                text.append("\n");
            }
        }
        
        if (analysisType == AnalysisType.SQL_OPTIMIZE && analysisResult.getOptimizationSuggestions() != null) {
            text.append(optimizationLabel).append("\n");
            String typeLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "**Type**:" : "**类型**:";
            String descLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "**Description**:" : "**描述**:";
            String suggLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "**Suggestion**:" : "**建议**:";
            
            analysisResult.getOptimizationSuggestions().forEach(suggestion -> 
                text.append("### ").append(suggestion.getTitle()).append(" [").append(suggestion.getSeverity()).append("]\n")
                     .append("- ").append(typeLabel).append(" ").append(suggestion.getType()).append("\n")
                     .append("- ").append(descLabel).append(" ").append(suggestion.getDescription()).append("\n")
                     .append("- ").append(suggLabel).append(" ").append(suggestion.getSuggestion()).append("\n\n"));
        }
        
        if (analysisType == AnalysisType.PERFORMANCE_ANALYZE) {
            if (analysisResult.getPerformanceMetrics() != null) {
                text.append(performanceLabel).append("\n");
                SqlAnalysisResult.PerformanceMetrics metrics = analysisResult.getPerformanceMetrics();
                String estimatedRowsLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "- Estimated rows: " : "- 预计处理行数: ";
                String accessMethodLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "- Access method: " : "- 访问方法: ";
                String indexesUsedLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "- Indexes used: " : "- 使用的索引: ";
                
                if (metrics.getEstimatedRows() != null) {
                    text.append(estimatedRowsLabel).append(metrics.getEstimatedRows()).append("\n");
                }
                if (metrics.getAccessMethod() != null) {
                    text.append(accessMethodLabel).append(metrics.getAccessMethod()).append("\n");
                }
                if (metrics.getIndexesUsed() != null && !metrics.getIndexesUsed().isEmpty()) {
                    text.append(indexesUsedLabel).append(String.join(", ", metrics.getIndexesUsed())).append("\n");
                }
                text.append("\n");
            }
            
            if (analysisResult.getBottlenecks() != null && !analysisResult.getBottlenecks().isEmpty()) {
                text.append(bottlenecksLabel).append("\n");
                analysisResult.getBottlenecks().forEach(bottleneck -> text.append("- ").append(bottleneck).append("\n"));
                text.append("\n");
            }
        }
        
        if (analysisType == AnalysisType.SECURITY_ANALYZE && analysisResult.getSecurityIssues() != null) {
            text.append(securityLabel).append("\n");
            String typeLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "**Type**:" : "**类型**:";
            String recommendLabel = language == MultiLanguagePromptService.Language.ENGLISH ? "**Recommendation**:" : "**建议**:";
            
            analysisResult.getSecurityIssues().forEach(issue ->
                text.append("### ").append(issue.getDescription()).append(" [").append(issue.getSeverity()).append("]\n")
                     .append("- ").append(typeLabel).append(" ").append(issue.getType()).append("\n")
                     .append("- ").append(recommendLabel).append(" ").append(issue.getRecommendation()).append("\n\n"));
        }
        
        return text.toString();
    }
}