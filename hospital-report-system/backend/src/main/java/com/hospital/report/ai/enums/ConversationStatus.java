package com.hospital.report.ai.enums;

public enum ConversationStatus {
    ACTIVE("活跃"),
    ARCHIVED("已归档"),
    DELETED("已删除");
    
    private final String description;
    
    ConversationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}