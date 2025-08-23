package com.hospital.report.ai.entity.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AIAssistantRequest {
    
    private Long conversationId;
    private Long userId;
    private Long datasourceId;
    private String message;
    private String analysisType; // 用于标识请求类型
    private String originalQuery; // 用于存储原始查询（自然语言转SQL）
    private String originalSql; // 用于存储原始SQL（SQL定制）
    private String userRequirements; // 用于存储用户需求（SQL定制）
    private Map<String, Object> context;
}