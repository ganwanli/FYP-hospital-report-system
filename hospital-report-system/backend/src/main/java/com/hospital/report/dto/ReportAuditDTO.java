package com.hospital.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报表审核相关DTO
 */
public class ReportAuditDTO {

    /**
     * 审核请求DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditRequest {
        @NotBlank(message = "审核决定不能为空")
        @Pattern(regexp = "APPROVED|REJECTED", message = "审核决定必须是APPROVED或REJECTED")
        private String decision;
        
        private String comment;
        
        @NotNull(message = "审核时间不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime auditTime;
        
        @NotNull(message = "新状态不能为空")
        private Integer newStatus;
    }

    /**
     * 批量审核请求DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchAuditRequest {
        @NotNull(message = "报表ID列表不能为空")
        private List<Long> reportIds;
        
        @NotBlank(message = "审核决定不能为空")
        @Pattern(regexp = "APPROVED|REJECTED", message = "审核决定必须是APPROVED或REJECTED")
        private String decision;
        
        private String comment;
        
        @NotNull(message = "审核时间不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime auditTime;
        
        @NotNull(message = "新状态不能为空")
        private Integer newStatus;
    }

    /**
     * 审核结果DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditResult {
        private Long reportId;
        private String reportName;
        private Integer oldStatus;
        private Integer newStatus;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime auditTime;
        private String auditorName;
        private String auditComment;
    }

    /**
     * 批量审核结果DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchAuditResult {
        private Integer totalCount;
        private Integer successCount;
        private Integer failureCount;
        private List<String> errors;
        private List<AuditResult> results;
    }

    /**
     * 审核统计DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditStatistics {
        private Integer totalReports;
        private Integer pendingCount;
        private Integer approvedCount;
        private Integer rejectedCount;
        private Integer publishedCount;
        private Integer approvedUnpublishedCount;
        private Double avgAuditHours;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastAuditTime;
    }

    /**
     * 发布结果DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PublishResult {
        private Long reportId;
        private String reportName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime publishTime;
        private String publisherName;
    }

    /**
     * 取消发布结果DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnpublishResult {
        private Long reportId;
        private String reportName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime unpublishTime;
    }

    /**
     * 发布统计DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PublishStatistics {
        private Integer totalReports;
        private Integer publishedCount;
        private Integer unpublishedCount;
        private Integer approvedUnpublishedCount;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastPublishTime;
    }

    /**
     * 审核日志DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditLog {
        private Long id;
        private Long reportId;
        private String auditorName;
        private String auditDecision;
        private String auditComment;
        private Integer oldStatus;
        private Integer newStatus;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime auditTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdTime;
    }
}
