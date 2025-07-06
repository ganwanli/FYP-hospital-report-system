package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("report_data_source")
public class ReportDataSource {

    @TableId(value = "data_source_id", type = IdType.AUTO)
    private Long dataSourceId;

    @TableField("report_id")
    private Long reportId;

    @TableField("source_name")
    private String sourceName;

    @TableField("source_type")
    private String sourceType;

    @TableField("connection_config")
    private String connectionConfig;

    @TableField("query_config")
    private String queryConfig;

    @TableField("sql_template_id")
    private Long sqlTemplateId;

    @TableField("api_config")
    private String apiConfig;

    @TableField("static_data")
    private String staticData;

    @TableField("refresh_interval")
    private Integer refreshInterval;

    @TableField("cache_enabled")
    private Boolean cacheEnabled;

    @TableField("cache_duration")
    private Integer cacheDuration;

    @TableField("parameters_config")
    private String parametersConfig;

    @TableField("transform_config")
    private String transformConfig;

    @TableField("is_active")
    private Boolean isActive;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @TableField("last_refresh_time")
    private LocalDateTime lastRefreshTime;

    @TableField("error_message")
    private String errorMessage;

    @TableField("error_count")
    private Integer errorCount;
}