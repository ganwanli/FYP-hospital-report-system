package com.hospital.report.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("ai_usage_stats")
public class AIUsageStats {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("stat_date")
    private LocalDate statDate;
    
    @TableField("total_conversations")
    private Integer totalConversations = 0;
    
    @TableField("total_messages")
    private Integer totalMessages = 0;
    
    @TableField("total_tokens_used")
    private Integer totalTokensUsed = 0;
    
    @TableField("sql_analysis_count")
    private Integer sqlAnalysisCount = 0;
    
    @TableField("database_analysis_count")
    private Integer databaseAnalysisCount = 0;
    
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}