package com.hospital.report.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hospital.report.ai.enums.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_message")
public class AIMessage {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("conversation_id")
    private Long conversationId;
    
    @TableField("message_type")
    private MessageType messageType;
    
    @TableField("content")
    private String content;
    
    @TableField("metadata")
    private String metadata;
    
    @TableField("token_count")
    private Integer tokenCount = 0;
    
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    public static AIMessage createUserMessage(Long conversationId, String content) {
        AIMessage message = new AIMessage();
        message.setConversationId(conversationId);
        message.setMessageType(MessageType.USER);
        message.setContent(content);
        return message;
    }
    
    public static AIMessage createAssistantMessage(Long conversationId, String content) {
        AIMessage message = new AIMessage();
        message.setConversationId(conversationId);
        message.setMessageType(MessageType.ASSISTANT);
        message.setContent(content);
        return message;
    }
    
    public static AIMessage createSystemMessage(Long conversationId, String content) {
        AIMessage message = new AIMessage();
        message.setConversationId(conversationId);
        message.setMessageType(MessageType.SYSTEM);
        message.setContent(content);
        return message;
    }
}