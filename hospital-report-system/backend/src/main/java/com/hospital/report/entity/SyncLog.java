package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sync_log")
public class SyncLog extends Model<SyncLog> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    private Long taskId;

    @TableField("task_code")
    private String taskCode;

    @TableField("execution_id")
    private String executionId;

    @TableField("status")
    private String status;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("duration")
    private Long duration;

    @TableField("source_count")
    private Long sourceCount;

    @TableField("target_count")
    private Long targetCount;

    @TableField("success_count")
    private Long successCount;

    @TableField("error_count")
    private Long errorCount;

    @TableField("skip_count")
    private Long skipCount;

    @TableField("progress_percent")
    private Double progressPercent;

    @TableField("current_value")
    private String currentValue;

    @TableField("error_message")
    private String errorMessage;

    @TableField("error_stack")
    private String errorStack;

    @TableField("sync_details")
    private String syncDetails;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("max_retry")
    private Integer maxRetry;

    @TableField("trigger_type")
    private String triggerType;

    @TableField("trigger_user")
    private Long triggerUser;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}