package com.hospital.report.ai.enums;

public enum MessageType {
    USER("用户消息"),
    ASSISTANT("AI助手消息"),
    SYSTEM("系统消息");
    
    private final String description;
    
    MessageType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}