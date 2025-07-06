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
@TableName("sys_datasource")
public class DataSource extends Model<DataSource> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("datasource_name")
    private String datasourceName;

    @TableField("datasource_code")
    private String datasourceCode;

    @TableField("database_type")
    private String databaseType;

    @TableField("driver_class_name")
    private String driverClassName;

    @TableField("jdbc_url")
    private String jdbcUrl;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("initial_size")
    private Integer initialSize;

    @TableField("min_idle")
    private Integer minIdle;

    @TableField("max_active")
    private Integer maxActive;

    @TableField("max_wait")
    private Long maxWait;

    @TableField("time_between_eviction_runs_millis")
    private Long timeBetweenEvictionRunsMillis;

    @TableField("min_evictable_idle_time_millis")
    private Long minEvictableIdleTimeMillis;

    @TableField("validation_query")
    private String validationQuery;

    @TableField("test_while_idle")
    private Boolean testWhileIdle;

    @TableField("test_on_borrow")
    private Boolean testOnBorrow;

    @TableField("test_on_return")
    private Boolean testOnReturn;

    @TableField("connection_timeout")
    private Long connectionTimeout;

    @TableField("idle_timeout")
    private Long idleTimeout;

    @TableField("max_lifetime")
    private Long maxLifetime;

    @TableField("leak_detection_threshold")
    private Long leakDetectionThreshold;

    @TableField("status")
    private Integer status;

    @TableField("is_default")
    private Boolean isDefault;

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