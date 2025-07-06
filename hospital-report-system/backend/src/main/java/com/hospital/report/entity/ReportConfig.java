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
@TableName("report_config")
public class ReportConfig {

    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    @TableField("report_name")
    private String reportName;

    @TableField("report_description")
    private String reportDescription;

    @TableField("report_category")
    private String reportCategory;

    @TableField("report_type")
    private String reportType;

    @TableField("layout_config")
    private String layoutConfig;

    @TableField("components_config")
    private String componentsConfig;

    @TableField("data_sources_config")
    private String dataSourcesConfig;

    @TableField("style_config")
    private String styleConfig;

    @TableField("canvas_width")
    private Integer canvasWidth;

    @TableField("canvas_height")
    private Integer canvasHeight;

    @TableField("is_published")
    private Boolean isPublished;

    @TableField("is_active")
    private Boolean isActive;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @TableField("published_time")
    private LocalDateTime publishedTime;

    @TableField("version")
    private String version;

    @TableField("tags")
    private String tags;

    @TableField("access_level")
    private String accessLevel;

    @TableField("refresh_interval")
    private Integer refreshInterval;

    @TableField("thumbnail")
    private String thumbnail;

    @TableField(exist = false)
    private String createdByName;

    @TableField(exist = false)
    private String updatedByName;

    @TableField(exist = false)
    private List<ReportComponent> components;

    @TableField(exist = false)
    private List<ReportDataSource> dataSources;
}