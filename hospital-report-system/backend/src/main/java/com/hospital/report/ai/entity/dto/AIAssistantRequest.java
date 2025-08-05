package com.hospital.report.ai.entity.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AIAssistantRequest {
    
    private Long conversationId;
    private Long userId;
    private Long datasourceId;
    private String message;
    private String analysisType; // 新增字段，用于标识请求类型
    private String originalQuery; // 新增字段，用于存储原始查询
    private Map<String, Object> context;
}