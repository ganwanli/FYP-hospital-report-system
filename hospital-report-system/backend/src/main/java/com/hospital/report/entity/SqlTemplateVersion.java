package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sql_template_version")
public class SqlTemplateVersion {

    @TableId(value = "version_id", type = IdType.AUTO)
    private Long versionId;

    @TableField("template_id")
    private Long templateId;

    @TableField("version_number")
    private String versionNumber;

    @TableField("version_description")
    private String versionDescription;

    @TableField("template_content")
    private String templateContent;

    @TableField("change_log")
    private String changeLog;

    @TableField("is_current")
    private Boolean isCurrent;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("template_hash")
    private String templateHash;

    @TableField("parent_version_id")
    private Long parentVersionId;

    @TableField("validation_status")
    private String validationStatus;

    @TableField("validation_message")
    private String validationMessage;

    @TableField("approval_status")
    private String approvalStatus;

    @TableField("approved_by")
    private Long approvedBy;

    @TableField("approved_time")
    private LocalDateTime approvedTime;

    @TableField(exist = false)
    private String createdByName;

    @TableField(exist = false)
    private String approvedByName;
}