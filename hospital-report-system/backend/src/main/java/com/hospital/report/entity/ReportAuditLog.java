package com.hospital.report.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 报表审核记录表
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "report_audit_log")
public class ReportAuditLog {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID'")
    private Long id;

    /**
     * 报表ID
     */
    @Column(name = "report_id", nullable = false, columnDefinition = "bigint NOT NULL COMMENT '报表ID'")
    private Long reportId;

    /**
     * 审核员ID
     */
    @Column(name = "auditor_id", nullable = false, columnDefinition = "bigint NOT NULL COMMENT '审核员ID'")
    private Long auditorId;

    /**
     * 审核员姓名
     */
    @Column(name = "auditor_name", nullable = false, length = 100, columnDefinition = "varchar(100) NOT NULL COMMENT '审核员姓名'")
    private String auditorName;

    /**
     * 审核决定: APPROVED, REJECTED
     */
    @Column(name = "audit_decision", nullable = false, length = 20, columnDefinition = "varchar(20) NOT NULL COMMENT '审核决定: APPROVED, REJECTED'")
    private String auditDecision;

    /**
     * 审核意见
     */
    @Lob
    @Column(name = "audit_comment", columnDefinition = "text COMMENT '审核意见'")
    private String auditComment;

    /**
     * 审核前状态
     */
    @Column(name = "old_status", nullable = false, columnDefinition = "int NOT NULL COMMENT '审核前状态'")
    private Integer oldStatus;

    /**
     * 审核后状态
     */
    @Column(name = "new_status", nullable = false, columnDefinition = "int NOT NULL COMMENT '审核后状态'")
    private Integer newStatus;

    /**
     * 审核时间
     */
    @Column(name = "audit_time", nullable = false, columnDefinition = "datetime NOT NULL COMMENT '审核时间'")
    private LocalDateTime auditTime;

    /**
     * 创建时间
     */
    @Column(name = "created_time", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
        if (updatedTime == null) {
            updatedTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
