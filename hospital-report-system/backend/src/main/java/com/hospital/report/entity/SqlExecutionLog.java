package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sql_execution_log")
public class SqlExecutionLog {

    @TableId(value = "execution_id", type = IdType.AUTO)
    private Long executionId;

    @TableField("template_id")
    private Long templateId;

    @TableField("user_id")
    private Long userId;

    @TableField("session_id")
    private String sessionId;

    @TableField("sql_content")
    private String sqlContent;

    @TableField("parameter_values")
    private String parameterValues;

    @TableField("execution_status")
    private String executionStatus;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("execution_duration")
    private Long executionDuration;

    @TableField("affected_rows")
    private Long affectedRows;

    @TableField("result_rows")
    private Long resultRows;

    @TableField("error_message")
    private String errorMessage;

    @TableField("error_code")
    private String errorCode;

    @TableField("database_name")
    private String databaseName;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("execution_plan")
    private String executionPlan;

    @TableField("memory_usage")
    private Long memoryUsage;

    @TableField("cpu_usage")
    private Double cpuUsage;

    @TableField("cache_hit")
    private Boolean cacheHit;

    @TableField("cache_key")
    private String cacheKey;

    @TableField("query_type")
    private String queryType;

    @TableField("is_async")
    private Boolean isAsync;

    @TableField("task_id")
    private String taskId;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String templateName;

    @TableField(exist = false)
    private Map<String, Object> parameters;

    @TableField(exist = false)
    private Object resultData;
}