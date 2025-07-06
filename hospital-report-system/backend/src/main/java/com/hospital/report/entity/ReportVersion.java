package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("report_version")
public class ReportVersion {

    @TableId(value = "version_id", type = IdType.AUTO)
    private Long versionId;

    @TableField("report_id")
    private Long reportId;

    @TableField("version_number")
    private String versionNumber;

    @TableField("version_description")
    private String versionDescription;

    @TableField("layout_config")
    private String layoutConfig;

    @TableField("components_config")
    private String componentsConfig;

    @TableField("data_sources_config")
    private String dataSourcesConfig;

    @TableField("style_config")
    private String styleConfig;

    @TableField("is_current")
    private Boolean isCurrent;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("parent_version_id")
    private Long parentVersionId;

    @TableField("change_log")
    private String changeLog;

    @TableField("file_size")
    private Long fileSize;

    @TableField("thumbnail")
    private String thumbnail;

    @TableField(exist = false)
    private String createdByName;
}