package com.hospital.report.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRequest {
    
    private String model;
    private List<ChatMessage> messages;
    private Double temperature;
    private Integer maxTokens;
    private Boolean stream;
    
    @Data
    public static class ChatMessage {
        private String role; // "user", "assistant", "system"
        private String content;
        
        public static ChatMessage user(String content) {
            ChatMessage msg = new ChatMessage();
            msg.setRole("user");
            msg.setContent(content);
            return msg;
        }
        
        public static ChatMessage assistant(String content) {
            ChatMessage msg = new ChatMessage();
            msg.setRole("assistant");
            msg.setContent(content);
            return msg;
        }
        
        public static ChatMessage system(String content) {
            ChatMessage msg = new ChatMessage();
            msg.setRole("system");
            msg.setContent(content);
            return msg;
        }
    }
}