package com.hospital.report.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class DataSourceCreateRequest {

    @NotBlank(message = "数据源名称不能为空")
    @Size(max = 100, message = "数据源名称不能超过100个字符")
    private String datasourceName;

    @NotBlank(message = "数据源编码不能为空")
    @Size(max = 50, message = "数据源编码不能超过50个字符")
    private String datasourceCode;

    @NotBlank(message = "数据库类型不能为空")
    private String databaseType;

    private String driverClassName;

    @NotBlank(message = "JDBC URL不能为空")
    @Size(max = 500, message = "JDBC URL不能超过500个字符")
    private String jdbcUrl;

    @NotBlank(message = "用户名不能为空")
    @Size(max = 100, message = "用户名不能超过100个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private Integer initialSize = 5;

    private Integer minIdle = 5;

    private Integer maxActive = 20;

    private Long maxWait = 60000L;

    private Long connectionTimeout = 30000L;

    private Long idleTimeout = 600000L;

    private Long maxLifetime = 1800000L;

    private Long leakDetectionThreshold = 60000L;

    private String validationQuery;

    private Boolean testWhileIdle = true;

    private Boolean testOnBorrow = false;

    private Boolean testOnReturn = false;

    private Boolean isDefault = false;

    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
}