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
@TableName("sync_task")
public class SyncTask extends Model<SyncTask> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("task_name")
    private String taskName;

    @TableField("task_code")
    private String taskCode;

    @TableField("task_type")
    private String taskType;

    @TableField("source_datasource_id")
    private Long sourceDatasourceId;

    @TableField("target_datasource_id")
    private Long targetDatasourceId;

    @TableField("source_table")
    private String sourceTable;

    @TableField("target_table")
    private String targetTable;

    @TableField("source_sql")
    private String sourceSql;

    @TableField("target_sql")
    private String targetSql;

    @TableField("sync_type")
    private String syncType;

    @TableField("sync_mode")
    private String syncMode;

    @TableField("cron_expression")
    private String cronExpression;

    @TableField("batch_size")
    private Integer batchSize;

    @TableField("timeout_seconds")
    private Integer timeoutSeconds;

    @TableField("retry_times")
    private Integer retryTimes;

    @TableField("retry_interval")
    private Integer retryInterval;

    @TableField("enable_transaction")
    private Boolean enableTransaction;

    @TableField("parallel_threads")
    private Integer parallelThreads;

    @TableField("incremental_column")
    private String incrementalColumn;

    @TableField("incremental_type")
    private String incrementalType;

    @TableField("last_sync_value")
    private String lastSyncValue;

    @TableField("filter_condition")
    private String filterCondition;

    @TableField("mapping_config")
    private String mappingConfig;

    @TableField("status")
    private Integer status;

    @TableField("is_enabled")
    private Boolean isEnabled;

    @TableField("description")
    private String description;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}