package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sql_template_usage_log")
public class SqlTemplateUsageLog {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    @TableField("template_id")
    private Long templateId;

    @TableField("user_id")
    private Long userId;

    @TableField("execution_time")
    private LocalDateTime executionTime;

    @TableField("execution_duration")
    private Long executionDuration;

    @TableField("execution_status")
    private String executionStatus;

    @TableField("error_message")
    private String errorMessage;

    @TableField("affected_rows")
    private Long affectedRows;

    @TableField("parameter_values")
    private String parameterValues;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("database_name")
    private String databaseName;

    @TableField("execution_plan")
    private String executionPlan;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String templateName;
}