package com.hospital.report.ai.entity.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AIAssistantRequest {
    
    private Long conversationId;
    private Long userId;
    private Long datasourceId;
    private String message;
    private Map<String, Object> context;
}