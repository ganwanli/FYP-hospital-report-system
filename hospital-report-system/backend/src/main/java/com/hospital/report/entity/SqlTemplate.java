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

    @TableField("is_active")
    private Boolean isActive;

    @TableField("is_public")
    private Boolean isPublic;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @TableField("usage_count")
    private Integer usageCount;

    @TableField("last_used_time")
    private LocalDateTime lastUsedTime;

    @TableField("tags")
    private String tags;

    @TableField("database_type")
    private String databaseType;

    @TableField("execution_timeout")
    private Integer executionTimeout;

    @TableField("max_rows")
    private Integer maxRows;

    @TableField("validation_status")
    private String validationStatus;

    @TableField("validation_message")
    private String validationMessage;

    @TableField("template_hash")
    private String templateHash;

    @TableField("approval_status")
    private String approvalStatus;

    @TableField("approved_by")
    private Long approvedBy;

    @TableField("approved_time")
    private LocalDateTime approvedTime;

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