package com.hospital.report.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "report_config")
public class ReportConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint NOT NULL AUTO_INCREMENT COMMENT '报表配置ID'")
    private Long id;

    @Column(name = "report_name", nullable = false, length = 200, columnDefinition = "varchar(200) NOT NULL COMMENT '报表名称'")
    private String reportName;

    @Column(name = "report_code", nullable = false, length = 100, unique = true, columnDefinition = "varchar(100) NOT NULL COMMENT '报表编码'")
    private String reportCode;

    @Column(name = "report_type", nullable = false, length = 20, columnDefinition = "varchar(20) NOT NULL COMMENT '报表类型(TABLE,CHART,EXPORT)'")
    private String reportType;

    @Column(name = "report_category_id", columnDefinition = "bigint DEFAULT NULL COMMENT '分类ID'")
    private Long reportCategoryId;

    @Column(name = "sql_template_id", nullable = false, columnDefinition = "bigint NOT NULL COMMENT 'SQL模板ID'")
    private Long sqlTemplateId;

    @Column(name = "datasource_id", columnDefinition = "bigint DEFAULT NULL COMMENT '数据源ID'")
    private Long datasourceId;

    @Lob
    @Column(name = "report_config", nullable = false, columnDefinition = "longtext NOT NULL COMMENT '报表配置(JSON格式)'")
    private String reportConfig;

    @Lob
    @Column(name = "chart_config", columnDefinition = "longtext COMMENT '图表配置(JSON格式)'")
    private String chartConfig;

    @Lob
    @Column(name = "export_config", columnDefinition = "longtext DEFAULT NULL COMMENT '导出配置(JSON格式)'")
    private String exportConfig;

    @Column(name = "cache_enabled", columnDefinition = "tinyint DEFAULT '0' COMMENT '是否启用缓存(1:是,0:否)'")
    private Integer cacheEnabled = 0;

    @Column(name = "cache_timeout", columnDefinition = "int DEFAULT '300' COMMENT '缓存超时时间(秒)'")
    private Integer cacheTimeout = 300;

    @Column(name = "refresh_interval", columnDefinition = "int DEFAULT '0' COMMENT '刷新间隔(秒,0表示不自动刷新)'")
    private Integer refreshInterval = 0;

    @Column(name = "access_level", length = 20, columnDefinition = "varchar(20) DEFAULT 'PRIVATE' COMMENT '访问级别(PUBLIC,PRIVATE,DEPT)'")
    private String accessLevel = "PRIVATE";

    @Column(name = "description", columnDefinition = "text DEFAULT NULL COMMENT '描述'")
    private String description;

    @Column(name = "version", length = 20, columnDefinition = "varchar(20) DEFAULT '1.0' COMMENT '版本号'")
    private String version = "1.0";

    @Column(name = "view_count", columnDefinition = "int DEFAULT '0' COMMENT '查看次数'")
    private Integer viewCount = 0;

    @Column(name = "last_view_time", columnDefinition = "datetime DEFAULT NULL COMMENT '最后查看时间'")
    private LocalDateTime lastViewTime;

    @Column(name = "is_published", columnDefinition = "tinyint DEFAULT '0' COMMENT '是否发布(1:是,0:否)'")
    private Integer isPublished = 0;

    @Column(name = "is_active", columnDefinition = "tinyint DEFAULT '1' COMMENT '状态(1:启用,0:禁用)'")
    private Integer isActive = 1;

    @Column(name = "is_deleted", columnDefinition = "tinyint DEFAULT '0' COMMENT '是否删除(0:否,1:是)'")
    private Integer isDeleted = 0;

    @Column(name = "created_time", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'")
    private LocalDateTime createdTime;

    @Column(name = "updated_time", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'")
    private LocalDateTime updatedTime;

    @Column(name = "created_by", columnDefinition = "bigint DEFAULT NULL COMMENT '创建人ID'")
    private Long createdBy;

    @Column(name = "updated_by", columnDefinition = "bigint DEFAULT NULL COMMENT '更新人ID'")
    private Long updatedBy;

    @Column(name = "approval_status", columnDefinition = "tinyint DEFAULT 1 COMMENT '审核状态: 1-草稿, 2-待审核, 3-审核通过, 4-审核拒绝'")
    private Integer approvalStatus = 1;

    @Column(name = "submit_time", columnDefinition = "datetime DEFAULT NULL COMMENT '提交审核时间'")
    private LocalDateTime submitTime;

    @Column(name = "audit_time", columnDefinition = "datetime DEFAULT NULL COMMENT '最后审核时间'")
    private LocalDateTime auditTime;

    @Column(name = "auditor_id", columnDefinition = "bigint DEFAULT NULL COMMENT '最后审核员ID'")
    private Long auditorId;

    @Column(name = "audit_comment", columnDefinition = "text DEFAULT NULL COMMENT '最后审核意见'")
    private String auditComment;

    @Column(name = "publish_time", columnDefinition = "datetime DEFAULT NULL COMMENT '发布时间'")
    private LocalDateTime publishTime;

    @Column(name = "unpublish_time", columnDefinition = "datetime DEFAULT NULL COMMENT '取消发布时间'")
    private LocalDateTime unpublishTime;

    @Column(name = "publisher_id", columnDefinition = "bigint DEFAULT NULL COMMENT '发布人ID'")
    private Long publisherId;

    // 保留原有字段以兼容现有代码
    @Column(name = "approved_by", length = 20, columnDefinition = "varchar(20) DEFAULT NULL COMMENT '审批人(兼容字段)'")
    private String approvedBy;

    @Column(name = "approved_time", columnDefinition = "datetime DEFAULT NULL COMMENT '审批时间(兼容字段)'")
    private LocalDateTime approvedTime;

    @Column(name = "department_code", length = 20, columnDefinition = "varchar(20) DEFAULT NULL COMMENT '科室代码'")
    private String departmentCode;

    @Column(name = "business_type", length = 20, columnDefinition = "varchar(20) DEFAULT NULL COMMENT '业务分类类别'")
    private String businessType;

    @Column(name = "usage_type", length = 20, columnDefinition = "varchar(20) DEFAULT NULL COMMENT '用途分类类别'")
    private String usageType;

    // 外键关联 - 如果需要的话可以取消注释
    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id",
               foreignKey = @ForeignKey(name = "fk_report_config_category"))
    private ReportCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datasource_id", referencedColumnName = "id",
               foreignKey = @ForeignKey(name = "fk_report_config_datasource"))
    private SysDatasource datasource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", referencedColumnName = "id",
               foreignKey = @ForeignKey(name = "fk_report_config_template"))
    private SysSqlTemplate template;
    */

    // 非持久化字段
    @Transient
    private String createdByName;

    @Transient
    private String updatedByName;

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