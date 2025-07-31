package com.hospital.report.ai.client.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    
    @Data
    public static class Choice {
        private Integer index;
        private ChatRequest.ChatMessage message;
        private String finishReason;
    }
    
    @Data
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}