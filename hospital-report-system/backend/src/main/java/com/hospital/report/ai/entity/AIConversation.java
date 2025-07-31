package com.hospital.report.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hospital.report.ai.enums.ConversationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("ai_conversation")
public class AIConversation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("title")
    private String title;
    
    @TableField("datasource_id")
    private Long datasourceId;
    
    @TableField("status")
    private ConversationStatus status = ConversationStatus.ACTIVE;
    
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    @TableField(exist = false)
    private List<AIMessage> messages;
    
    @TableField(exist = false)
    private Integer messageCount;
    
    @TableField(exist = false)
    private String lastMessage;
}