package com.hospital.report.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hospital.report.ai.enums.AnalysisType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sql_analysis_log")
public class SqlAnalysisLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("conversation_id")
    private Long conversationId;
    
    @TableField("sql_content")
    private String sqlContent;
    
    @TableField("analysis_type")
    private AnalysisType analysisType;
    
    @TableField("analysis_result")
    private String analysisResult;
    
    @TableField("ai_suggestions")
    private String aiSuggestions;
    
    @TableField("execution_time")
    private Long executionTime;
    
    @TableField("datasource_id")
    private Long datasourceId;
    
    @TableField("status")
    private String status = "SUCCESS";
    
    @TableField("error_message")
    private String errorMessage;
    
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}