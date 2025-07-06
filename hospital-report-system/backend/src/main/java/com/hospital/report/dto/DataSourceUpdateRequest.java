package com.hospital.report.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class DataSourceUpdateRequest {

    @NotBlank(message = "数据源名称不能为空")
    @Size(max = 100, message = "数据源名称不能超过100个字符")
    private String datasourceName;

    @NotBlank(message = "数据库类型不能为空")
    private String databaseType;

    private String driverClassName;

    @NotBlank(message = "JDBC URL不能为空")
    @Size(max = 500, message = "JDBC URL不能超过500个字符")
    private String jdbcUrl;

    @NotBlank(message = "用户名不能为空")
    @Size(max = 100, message = "用户名不能超过100个字符")
    private String username;

    private String password;

    private Integer initialSize;

    private Integer minIdle;

    private Integer maxActive;

    private Long maxWait;

    private Long connectionTimeout;

    private Long idleTimeout;

    private Long maxLifetime;

    private Long leakDetectionThreshold;

    private String validationQuery;

    private Boolean testWhileIdle;

    private Boolean testOnBorrow;

    private Boolean testOnReturn;

    private Boolean isDefault;

    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
}