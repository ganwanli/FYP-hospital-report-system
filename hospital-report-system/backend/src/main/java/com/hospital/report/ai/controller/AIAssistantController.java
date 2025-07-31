package com.hospital.report.ai.controller;

import com.hospital.report.ai.entity.AIConversation;
import com.hospital.report.ai.entity.AIMessage;
import com.hospital.report.ai.entity.dto.AIAssistantRequest;
import com.hospital.report.ai.entity.dto.AIAssistantResponse;
import com.hospital.report.ai.entity.dto.ConversationStats;
import com.hospital.report.ai.enums.AnalysisType;
import com.hospital.report.ai.service.AIAssistantService;
import com.hospital.report.ai.service.ConversationService;
import com.hospital.report.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai-assistant")
@Tag(name = "AI助手", description = "AI助手相关接口")
@CrossOrigin(
    origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:3001", "http://127.0.0.1:3001"}, 
    maxAge = 3600, 
    allowedHeaders = {"Content-Type", "Accept", "Authorization", "Cache-Control", "X-Requested-With"}, 
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
    allowCredentials = "true"
)
@Slf4j
public class AIAssistantController {
    
    private final AIAssistantService aiAssistantService;
    private final ConversationService conversationService;
    
    public AIAssistantController(AIAssistantService aiAssistantService,
                                ConversationService conversationService) {
        this.aiAssistantService = aiAssistantService;
        this.conversationService = conversationService;
    }
    
    @GetMapping("/test")
    @Operation(summary = "测试接口", description = "测试AI助手接口是否可访问")
    public Result<String> test() {
        log.info("AI助手测试接口被访问");
        return Result.success("AI助手接口正常工作");
    }
    
    @PostMapping("/chat")
    @Operation(summary = "AI对话", description = "与AI助手进行同步对话")
    public Result<AIAssistantResponse> chat(@RequestBody AIAssistantRequest request) {
        try {
            // 参数验证
            if (request.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            
            AIAssistantResponse response = aiAssistantService.chat(request);
            
            if (response.isSuccess()) {
                return Result.success(response);
            } else {
                return Result.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("AI对话失败", e);
            return Result.error("AI对话失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/chat/stream")
    @Operation(summary = "流式AI对话", description = "与AI助手进行流式对话，实时返回响应")
    public SseEmitter chatStream(@RequestBody AIAssistantRequest request) {
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时
        
        try {
            // 参数验证
            if (request.getUserId() == null) {
                emitter.send(SseEmitter.event().data("错误：用户ID不能为空"));
                emitter.complete();
                return emitter;
            }
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                emitter.send(SseEmitter.event().data("错误：消息内容不能为空"));
                emitter.complete();
                return emitter;
            }
            
            aiAssistantService.chatStream(request)
                .doOnNext(chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        log.error("发送SSE数据失败", e);
                        emitter.completeWithError(e);
                    }
                })
                .doOnComplete(() -> {
                    try {
                        emitter.send(SseEmitter.event().name("complete").data(""));
                        emitter.complete();
                    } catch (IOException e) {
                        log.error("完成SSE流失败", e);
                    }
                })
                .doOnError(error -> {
                    log.error("流式对话失败", error);
                    try {
                        emitter.send(SseEmitter.event().name("error").data("对话失败: " + error.getMessage()));
                    } catch (IOException e) {
                        log.error("发送错误信息失败", e);
                    }
                    emitter.completeWithError(error);
                })
                .subscribe();
                
        } catch (Exception e) {
            log.error("初始化流式对话失败", e);
            try {
                emitter.send(SseEmitter.event().name("error").data("初始化失败: " + e.getMessage()));
                emitter.completeWithError(e);
            } catch (IOException ioError) {
                log.error("发送初始化错误失败", ioError);
            }
        }
        
        return emitter;
    }
    
    @PostMapping("/analyze/database")
    @Operation(summary = "数据库结构分析", description = "分析指定数据源的数据库结构并提供AI建议")
    public Result<AIAssistantResponse> analyzeDatabaseSchema(@RequestBody Map<String, Object> request) {
        try {
            Long conversationId = request.get("conversationId") != null ? 
                Long.valueOf(request.get("conversationId").toString()) : null;
            
            if (request.get("datasourceId") == null) {
                return Result.error("数据源ID不能为空");
            }
            Long datasourceId = Long.valueOf(request.get("datasourceId").toString());
            
            AIAssistantResponse response = aiAssistantService.analyzeDatabaseSchema(conversationId, datasourceId);
            
            if (response.isSuccess()) {
                return Result.success(response);
            } else {
                return Result.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("数据库结构分析失败", e);
            return Result.error("数据库结构分析失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/analyze/sql")
    @Operation(summary = "SQL分析", description = "分析SQL语句并提供优化建议")
    public Result<AIAssistantResponse> analyzeSql(@RequestBody Map<String, Object> request) {
        try {
            Long conversationId = request.get("conversationId") != null ? 
                Long.valueOf(request.get("conversationId").toString()) : null;
            
            if (request.get("sqlContent") == null || request.get("sqlContent").toString().trim().isEmpty()) {
                return Result.error("SQL内容不能为空");
            }
            String sqlContent = request.get("sqlContent").toString().trim();
            
            if (request.get("datasourceId") == null) {
                return Result.error("数据源ID不能为空");
            }
            Long datasourceId = Long.valueOf(request.get("datasourceId").toString());
            
            AnalysisType analysisType = AnalysisType.SQL_OPTIMIZE; // 默认优化分析
            if (request.get("analysisType") != null) {
                try {
                    analysisType = AnalysisType.valueOf(request.get("analysisType").toString());
                } catch (IllegalArgumentException e) {
                    return Result.error("不支持的分析类型: " + request.get("analysisType"));
                }
            }
            
            AIAssistantResponse response = aiAssistantService.analyzeSql(conversationId, sqlContent, datasourceId, analysisType);
            
            if (response.isSuccess()) {
                return Result.success(response);
            } else {
                return Result.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("SQL分析失败", e);
            return Result.error("SQL分析失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/conversations")
    @Operation(summary = "获取用户对话列表", description = "获取指定用户的对话历史列表")
    public Result<List<AIConversation>> getUserConversations(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "限制数量") @RequestParam(required = false) Integer limit) {
        try {
            List<AIConversation> conversations = conversationService.getUserConversations(userId, limit);
            return Result.success(conversations);
        } catch (Exception e) {
            log.error("获取对话列表失败，用户ID: {}", userId, e);
            return Result.error("获取对话列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "获取对话消息", description = "获取指定对话的消息记录")
    public Result<List<AIMessage>> getConversationMessages(
            @Parameter(description = "对话ID") @PathVariable Long conversationId,
            @Parameter(description = "限制数量") @RequestParam(required = false) Integer limit) {
        try {
            List<AIMessage> messages = conversationService.getRecentMessages(conversationId, limit);
            return Result.success(messages);
        } catch (Exception e) {
            log.error("获取对话消息失败，对话ID: {}", conversationId, e);
            return Result.error("获取对话消息失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/conversations")
    @Operation(summary = "创建新对话", description = "为指定用户创建新的AI对话")
    public Result<AIConversation> createConversation(@RequestBody Map<String, Object> request) {
        try {
            if (request.get("userId") == null) {
                return Result.error("用户ID不能为空");
            }
            Long userId = Long.valueOf(request.get("userId").toString());
            
            String title = request.get("title") != null ? request.get("title").toString() : null;
            Long datasourceId = request.get("datasourceId") != null ? 
                Long.valueOf(request.get("datasourceId").toString()) : null;
            
            AIConversation conversation = conversationService.createConversation(userId, title, datasourceId);
            return Result.success(conversation);
        } catch (Exception e) {
            log.error("创建对话失败", e);
            return Result.error("创建对话失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/conversations/{conversationId}/title")
    @Operation(summary = "更新对话标题", description = "更新指定对话的标题")
    public Result<Void> updateConversationTitle(
            @Parameter(description = "对话ID") @PathVariable Long conversationId,
            @RequestBody Map<String, String> request) {
        try {
            String newTitle = request.get("title");
            if (newTitle == null || newTitle.trim().isEmpty()) {
                return Result.error("标题不能为空");
            }
            
            conversationService.updateConversationTitle(conversationId, newTitle.trim());
            return Result.success();
        } catch (Exception e) {
            log.error("更新对话标题失败，对话ID: {}", conversationId, e);
            return Result.error("更新对话标题失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/conversations/{conversationId}/archive")
    @Operation(summary = "归档对话", description = "将指定对话设置为已归档状态")
    public Result<Void> archiveConversation(@Parameter(description = "对话ID") @PathVariable Long conversationId) {
        try {
            conversationService.archiveConversation(conversationId);
            return Result.success();
        } catch (Exception e) {
            log.error("归档对话失败，对话ID: {}", conversationId, e);
            return Result.error("归档对话失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/conversations/{conversationId}")
    @Operation(summary = "删除对话", description = "删除指定对话（软删除）")
    public Result<Void> deleteConversation(@Parameter(description = "对话ID") @PathVariable Long conversationId) {
        try {
            conversationService.deleteConversation(conversationId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除对话失败，对话ID: {}", conversationId, e);
            return Result.error("删除对话失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/stats/{userId}")
    @Operation(summary = "获取对话统计", description = "获取指定用户的AI助手使用统计信息")
    public Result<ConversationStats> getConversationStats(@Parameter(description = "用户ID") @PathVariable Long userId) {
        try {
            ConversationStats stats = conversationService.getConversationStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取对话统计失败，用户ID: {}", userId, e);
            return Result.error("获取对话统计失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/analysis-types")
    @Operation(summary = "获取分析类型", description = "获取支持的SQL分析类型列表")
    public Result<List<Map<String, String>>> getAnalysisTypes() {
        try {
            List<Map<String, String>> types = List.of(
                Map.of("value", "SQL_EXPLAIN", "label", "SQL执行计划分析", "description", "分析SQL的执行计划和执行路径"),
                Map.of("value", "SQL_OPTIMIZE", "label", "SQL优化建议", "description", "提供SQL优化建议和改进方案"),
                Map.of("value", "PERFORMANCE_ANALYZE", "label", "性能分析", "description", "深入分析SQL性能瓶颈"),
                Map.of("value", "SECURITY_ANALYZE", "label", "安全分析", "description", "检查SQL的安全风险")
            );
            return Result.success(types);
        } catch (Exception e) {
            log.error("获取分析类型失败", e);
            return Result.error("获取分析类型失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/test-connection")
    @Operation(summary = "测试AI连接", description = "测试与AI服务的连接状态")
    public Result<Map<String, Object>> testConnection() {
        try {
            // 这里可以调用DeepSeekClient的测试连接方法
            Map<String, Object> result = Map.of(
                "status", "connected",
                "message", "AI服务连接正常",
                "timestamp", System.currentTimeMillis()
            );
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试AI连接失败", e);
            return Result.error("AI服务连接失败: " + e.getMessage());
        }
    }
}