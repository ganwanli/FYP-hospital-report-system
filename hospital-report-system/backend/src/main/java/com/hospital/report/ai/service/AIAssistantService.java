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
    private final SqlAnalysisLogMapper sqlAnalysisLogMapper;
    private final ObjectMapper objectMapper;
    
    public AIAssistantService(DeepSeekClient deepSeekClient,
                             DatabaseSchemaAnalyzer schemaAnalyzer,
                             SqlAnalyzer sqlAnalyzer,
                             ConversationService conversationService,
                             DataSourceService dataSourceService,
                             SqlAnalysisLogMapper sqlAnalysisLogMapper) {
        this.deepSeekClient = deepSeekClient;
        this.schemaAnalyzer = schemaAnalyzer;
        this.sqlAnalyzer = sqlAnalyzer;
        this.conversationService = conversationService;
        this.dataSourceService = dataSourceService;
        this.sqlAnalysisLogMapper = sqlAnalysisLogMapper;
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
                
                // 2. 构建上下文消息
                List<ChatRequest.ChatMessage> messages = buildContextMessages(conversation, request);
                
                // 3. 保存用户消息
                conversationService.saveMessage(conversation.getId(), MessageType.USER, request.getMessage());
                
                // 4. 调用流式AI接口
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
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 分析数据库结构
            DatabaseSchemaInfo schemaInfo = schemaAnalyzer.analyzeDatabaseSchema(datasourceId);
            String schemaDescription = schemaAnalyzer.generateSchemaDescription(schemaInfo);
            
            // 2. 构建AI分析请求
            List<ChatRequest.ChatMessage> messages = Arrays.asList(
                ChatRequest.ChatMessage.system(buildDatabaseAnalysisSystemPrompt()),
                ChatRequest.ChatMessage.user("请分析以下数据库结构并提供专业的评估报告：\n\n" + schemaDescription)
            );
            
            // 3. 调用AI分析
            ChatResponse response = deepSeekClient.chat(messages).block();
            
            if (response == null || response.getChoices().isEmpty()) {
                return AIAssistantResponse.error("AI数据库分析服务异常");
            }
            
            String analysis = response.getChoices().get(0).getMessage().getContent();
            Integer tokensUsed = (response.getUsage() != null && response.getUsage().getTotalTokens() != null) 
                ? response.getUsage().getTotalTokens() : 0;
            
            // 4. 保存分析结果
            if (conversationId != null) {
                conversationService.saveMessage(conversationId, MessageType.SYSTEM, "数据库结构分析");
                conversationService.saveMessage(conversationId, MessageType.ASSISTANT, analysis, null, tokensUsed);
            }
            
            // 5. 准备响应数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("schemaInfo", schemaInfo);
            metadata.put("analysisType", "DATABASE_SCHEMA");
            metadata.put("executionTime", System.currentTimeMillis() - startTime);
            
            AIAssistantResponse assistantResponse = AIAssistantResponse.success(conversationId, analysis);
            assistantResponse.setMetadata(metadata);
            assistantResponse.setTokenUsed(tokensUsed);
            
            log.info("数据库结构分析完成，数据源ID: {}, 执行时间: {}ms", datasourceId, System.currentTimeMillis() - startTime);
            return assistantResponse;
            
        } catch (Exception e) {
            log.error("数据库结构分析失败，数据源ID: {}", datasourceId, e);
            return AIAssistantResponse.error("数据库结构分析失败: " + e.getMessage());
        }
    }
    
    public AIAssistantResponse analyzeSql(Long conversationId, String sqlContent, Long datasourceId, AnalysisType analysisType) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. SQL技术分析
            SqlAnalysisResult analysisResult = sqlAnalyzer.analyzeSql(sqlContent, datasourceId, analysisType);
            
            // 2. 构建AI分析请求
            String analysisPrompt = buildSqlAnalysisPrompt(sqlContent, analysisResult, analysisType);
            
            List<ChatRequest.ChatMessage> messages = Arrays.asList(
                ChatRequest.ChatMessage.system(buildSqlAnalysisSystemPrompt()),
                ChatRequest.ChatMessage.user(analysisPrompt)
            );
            
            // 3. 调用AI分析
            ChatResponse response = deepSeekClient.chat(messages).block();
            
            if (response == null || response.getChoices().isEmpty()) {
                return AIAssistantResponse.error("AI SQL分析服务异常");
            }
            
            String aiAnalysis = response.getChoices().get(0).getMessage().getContent();
            Integer tokensUsed = (response.getUsage() != null && response.getUsage().getTotalTokens() != null) 
                ? response.getUsage().getTotalTokens() : 0;
            
            // 4. 保存分析日志
            saveSqlAnalysisLog(conversationId, sqlContent, analysisType, analysisResult, aiAnalysis, datasourceId, System.currentTimeMillis() - startTime);
            
            // 5. 保存对话记录
            if (conversationId != null) {
                conversationService.saveMessage(conversationId, MessageType.USER, "SQL分析: " + sqlContent);
                conversationService.saveMessage(conversationId, MessageType.ASSISTANT, aiAnalysis, null, tokensUsed);
            }
            
            // 6. 准备响应数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sqlAnalysis", analysisResult);
            metadata.put("analysisType", analysisType.name());
            metadata.put("sqlContent", sqlContent);
            metadata.put("executionTime", System.currentTimeMillis() - startTime);
            
            AIAssistantResponse assistantResponse = AIAssistantResponse.success(conversationId, aiAnalysis);
            assistantResponse.setMetadata(metadata);
            assistantResponse.setTokenUsed(tokensUsed);
            
            log.info("SQL分析完成，类型: {}, 执行时间: {}ms", analysisType, System.currentTimeMillis() - startTime);
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
        
        // 1. 添加系统提示
        messages.add(ChatRequest.ChatMessage.system(buildSystemPrompt(conversation)));
        
        // 2. 添加历史对话（最近10条）
        List<AIMessage> recentMessages = conversationService.getRecentMessages(conversation.getId(), 10);
        for (AIMessage message : recentMessages) {
            if (message.getMessageType() == MessageType.USER) {
                messages.add(ChatRequest.ChatMessage.user(message.getContent()));
            } else if (message.getMessageType() == MessageType.ASSISTANT) {
                messages.add(ChatRequest.ChatMessage.assistant(message.getContent()));
            }
        }
        
        // 3. 添加当前用户消息
        messages.add(ChatRequest.ChatMessage.user(request.getMessage()));
        
        return messages;
    }
    
    private String buildSystemPrompt(AIConversation conversation) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个专业的数据库和SQL专家助手，具备以下能力：\n");
        prompt.append("1. 分析和解释SQL查询语句，提供详细的执行逻辑说明\n");
        prompt.append("2. 提供具体可行的SQL优化建议和改进方案\n");
        prompt.append("3. 分析数据库结构设计，识别潜在问题并提供改进建议\n");
        prompt.append("4. 解答各种数据库相关技术问题\n");
        prompt.append("5. 提供数据查询的最佳实践指导\n\n");
        
        // 添加数据库上下文
        if (conversation.getDatasourceId() != null) {
            try {
                DataSource dataSource = dataSourceService.getById(conversation.getDatasourceId());
                if (dataSource != null) {
                    prompt.append("当前连接的数据库环境：\n");
                    prompt.append("- 数据库类型: ").append(dataSource.getDatabaseType()).append("\n");
                    prompt.append("- 数据库名称: ").append(dataSource.getDatabaseName()).append("\n");
                    if (StringUtils.hasText(dataSource.getDescription())) {
                        prompt.append("- 描述: ").append(dataSource.getDescription()).append("\n");
                    }
                    prompt.append("\n");
                }
            } catch (Exception e) {
                log.warn("获取数据源信息失败", e);
            }
        }
        
        prompt.append("请用专业、清晰、易懂的语言回答用户问题，在适当时提供具体的代码示例和实用建议。");
        prompt.append("对于SQL优化建议，请给出具体的改进方案和原因说明。");
        
        return prompt.toString();
    }
    
    private String buildDatabaseAnalysisSystemPrompt() {
        return "你是一个资深的数据库架构师和DBA专家。请基于提供的数据库结构信息，从以下维度进行专业分析：\n" +
               "1. 表结构设计评估 - 分析表设计的合理性\n" +
               "2. 索引使用分析 - 评估索引的配置和效率\n" +
               "3. 数据关系梳理 - 分析表之间的关联关系\n" +
               "4. 性能优化建议 - 提供具体的优化方案\n" +
               "5. 规范性检查 - 检查命名规范和设计模式\n" +
               "6. 扩展性评估 - 分析系统的可扩展性\n\n" +
               "请提供详细、实用的分析报告，包含具体的改进建议和实施方案。";
    }
    
    private String buildSqlAnalysisSystemPrompt() {
        return "你是一个SQL优化专家，请基于提供的技术分析结果，从以下角度给出专业建议：\n" +
               "1. SQL执行逻辑解释 - 用通俗易懂的语言解释SQL的执行过程\n" +
               "2. 性能分析 - 分析查询的性能特点和潜在瓶颈\n" +
               "3. 优化建议 - 提供具体的优化方案和改进代码\n" +
               "4. 最佳实践 - 给出相关的编程最佳实践建议\n" +
               "5. 风险提示 - 识别潜在的安全或性能风险\n\n" +
               "请提供实用、可操作的建议，包含具体的SQL改进代码示例。";
    }
    
    private String buildSqlAnalysisPrompt(String sqlContent, SqlAnalysisResult analysisResult, AnalysisType analysisType) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("请分析以下SQL语句：\n\n");
        prompt.append("```sql\n").append(sqlContent).append("\n```\n\n");
        
        prompt.append("技术分析结果：\n");
        
        if (analysisType == AnalysisType.SQL_EXPLAIN && analysisResult.getExecutionPlan() != null) {
            prompt.append("## 执行计划\n");
            prompt.append("```json\n").append(formatJson(analysisResult.getExecutionPlan())).append("\n```\n\n");
            
            if (analysisResult.getInsights() != null && !analysisResult.getInsights().isEmpty()) {
                prompt.append("## 关键洞察\n");
                analysisResult.getInsights().forEach(insight -> prompt.append("- ").append(insight).append("\n"));
                prompt.append("\n");
            }
        }
        
        if (analysisType == AnalysisType.SQL_OPTIMIZE && analysisResult.getOptimizationSuggestions() != null) {
            prompt.append("## 优化建议\n");
            analysisResult.getOptimizationSuggestions().forEach(suggestion -> 
                prompt.append("### ").append(suggestion.getTitle()).append(" [").append(suggestion.getSeverity()).append("]\n")
                     .append("- **类型**: ").append(suggestion.getType()).append("\n")
                     .append("- **描述**: ").append(suggestion.getDescription()).append("\n")
                     .append("- **建议**: ").append(suggestion.getSuggestion()).append("\n\n"));
        }
        
        if (analysisType == AnalysisType.PERFORMANCE_ANALYZE) {
            if (analysisResult.getPerformanceMetrics() != null) {
                prompt.append("## 性能指标\n");
                SqlAnalysisResult.PerformanceMetrics metrics = analysisResult.getPerformanceMetrics();
                if (metrics.getEstimatedRows() != null) {
                    prompt.append("- 预计处理行数: ").append(metrics.getEstimatedRows()).append("\n");
                }
                if (metrics.getAccessMethod() != null) {
                    prompt.append("- 访问方法: ").append(metrics.getAccessMethod()).append("\n");
                }
                if (metrics.getIndexesUsed() != null && !metrics.getIndexesUsed().isEmpty()) {
                    prompt.append("- 使用的索引: ").append(String.join(", ", metrics.getIndexesUsed())).append("\n");
                }
                prompt.append("\n");
            }
            
            if (analysisResult.getBottlenecks() != null && !analysisResult.getBottlenecks().isEmpty()) {
                prompt.append("## 性能瓶颈\n");
                analysisResult.getBottlenecks().forEach(bottleneck -> prompt.append("- ").append(bottleneck).append("\n"));
                prompt.append("\n");
            }
        }
        
        if (analysisType == AnalysisType.SECURITY_ANALYZE && analysisResult.getSecurityIssues() != null) {
            prompt.append("## 安全问题\n");
            analysisResult.getSecurityIssues().forEach(issue ->
                prompt.append("### ").append(issue.getDescription()).append(" [").append(issue.getSeverity()).append("]\n")
                     .append("- **类型**: ").append(issue.getType()).append("\n")
                     .append("- **建议**: ").append(issue.getRecommendation()).append("\n\n"));
        }
        
        prompt.append("请基于以上技术分析结果，用专业且通俗易懂的语言：\n");
        prompt.append("1. 解释SQL的执行逻辑和工作原理\n");
        prompt.append("2. 分析潜在的性能问题和优化空间\n");
        prompt.append("3. 提供具体的优化方案，包含改进后的SQL代码\n");
        prompt.append("4. 给出相关的最佳实践建议\n");
        
        return prompt.toString();
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
    
    private String formatJson(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
}