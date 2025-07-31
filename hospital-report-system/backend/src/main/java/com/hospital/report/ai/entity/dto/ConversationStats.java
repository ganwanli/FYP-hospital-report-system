package com.hospital.report.ai.entity.dto;

import lombok.Data;

@Data
public class ConversationStats {
    
    private Long totalConversations;
    private Long activeConversations;
    private Long todayConversations;
    private Long totalMessages;
    private Long totalTokensUsed;
    private Double averageTokensPerMessage;
    private Long sqlAnalysisCount;
    private Long databaseAnalysisCount;
}