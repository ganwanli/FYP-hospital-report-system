package com.hospital.report.ai.entity.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AIAssistantResponse {
    
    private Long conversationId;
    private String message;
    private boolean success;
    private String errorMessage;
    private Map<String, Object> metadata;
    private Integer tokenUsed;
    
    public static AIAssistantResponse success(Long conversationId, String message) {
        AIAssistantResponse response = new AIAssistantResponse();
        response.setConversationId(conversationId);
        response.setMessage(message);
        response.setSuccess(true);
        return response;
    }
    
    public static AIAssistantResponse error(String errorMessage) {
        AIAssistantResponse response = new AIAssistantResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}