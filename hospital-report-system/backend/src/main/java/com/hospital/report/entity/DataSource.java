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

    @TableField("datasource_type")
    private String databaseType;

    @TableField("driver_class")
    private String driverClassName;

    @TableField("connection_url")
    private String jdbcUrl;

    @TableField("host")
    private String host;

    @TableField("port")
    private Integer port;

    @TableField("database_name")
    private String databaseName;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("min_pool_size")
    private Integer initialSize;

    @TableField(exist = false)
    private Integer minIdle;

    @TableField("max_pool_size")
    private Integer maxActive;

    @TableField(exist = false)
    private Long maxWait;

    @TableField(exist = false)
    private Long timeBetweenEvictionRunsMillis;

    @TableField(exist = false)
    private Long minEvictableIdleTimeMillis;

    @TableField("test_query")
    private String validationQuery;

    @TableField(exist = false)
    private Boolean testWhileIdle;

    @TableField(exist = false)
    private Boolean testOnBorrow;

    @TableField(exist = false)
    private Boolean testOnReturn;

    @TableField("connection_timeout")
    private Long connectionTimeout;

    @TableField(exist = false)
    private Long idleTimeout;

    @TableField(exist = false)
    private Long maxLifetime;

    @TableField(exist = false)
    private Long leakDetectionThreshold;

    @TableField("status")
    private Integer status;

    @TableField(exist = false)
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

    @TableField("deleted")
    @TableLogic
    private Boolean isDeleted;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}