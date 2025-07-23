package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sql_template")
public class SqlTemplate {

    @TableId(value = "template_id", type = IdType.AUTO)
    private Long templateId;

    @TableField("template_name")
    private String templateName;

    @TableField("template_description")
    private String templateDescription;

    @TableField("template_category")
    private String templateCategory;

    @TableField("template_content")
    private String templateContent;

    @TableField("template_version")
    private String templateVersion;

    @TableField("datasource_id")
    private Long datasourceId;

    @TableField("database_type")
    private String databaseType;

    @TableField("is_active")
    private Boolean isActive;

    @TableField("is_public")
    private Boolean isPublic;

    @TableField("usage_count")
    private Integer usageCount;

    @TableField("tags")
    private String tags;

    @TableField("approval_status")
    private String approvalStatus;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @TableField("approved_by")
    private Long approvedBy;

    @TableField("approved_time")
    private LocalDateTime approvedTime;

    @TableField("department_code")
    private String departmentCode;

    @TableField("business_type")
    private String businessType;

    @TableField("usage_type")
    private String usageType;

    @TableField("execution_timeout")
    private Integer executionTimeout;

    @TableField("template_hash")
    private String templateHash;

    @TableField("max_rows")
    private Integer maxRows;

    @TableField("last_used_time")
    private LocalDateTime lastUsedTime;

    @TableField("modification_note")
    private String modificationNote;

    @TableField("change_log")
    private String changeLog;

    @TableField(exist = false)
    private List<SqlTemplateParameter> parameters;

    @TableField(exist = false)
    private List<SqlTemplateVersion> versions;

    @TableField(exist = false)
    private String createdByName;

    @TableField(exist = false)
    private String updatedByName;

    @TableField(exist = false)
    private String approvedByName;
}