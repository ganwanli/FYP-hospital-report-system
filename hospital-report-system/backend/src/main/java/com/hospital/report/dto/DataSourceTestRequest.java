package com.hospital.report.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
public class DataSourceTestRequest {

    @NotBlank(message = "数据库类型不能为空")
    private String databaseType;

    @NotBlank(message = "主机地址不能为空")
    private String host;

    @NotNull(message = "端口不能为空")
    @Min(value = 1, message = "端口必须大于0")
    @Max(value = 65535, message = "端口不能超过65535")
    private Integer port;

    @NotBlank(message = "数据库名不能为空")
    private String database;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String authentication = "Username & Password";

    private Boolean ssl = false;

    private Integer timeout = 30;

    // 生成JDBC URL的方法
    public String generateJdbcUrl() {
        String baseUrl;
        switch (databaseType.toLowerCase()) {
            case "mysql":
                baseUrl = String.format("jdbc:mysql://%s:%d/%s", host, port, database);
                if (ssl) {
                    baseUrl += "?useSSL=true&serverTimezone=GMT%2B8";
                } else {
                    baseUrl += "?useSSL=false&serverTimezone=GMT%2B8";
                }
                break;
            case "postgresql":
                baseUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
                if (ssl) {
                    baseUrl += "?ssl=true";
                }
                break;
            case "mariadb":
                baseUrl = String.format("jdbc:mariadb://%s:%d/%s", host, port, database);
                if (ssl) {
                    baseUrl += "?useSSL=true";
                }
                break;
            case "sqlserver":
                baseUrl = String.format("jdbc:sqlserver://%s:%d;databaseName=%s", host, port, database);
                if (ssl) {
                    baseUrl += ";encrypt=true";
                }
                break;
            case "oracle":
                baseUrl = String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, database);
                break;
            case "mongodb":
                baseUrl = String.format("mongodb://%s:%d/%s", host, port, database);
                break;
            default:
                baseUrl = String.format("jdbc:%s://%s:%d/%s", databaseType.toLowerCase(), host, port, database);
        }
        return baseUrl;
    }

    // 获取驱动类名
    public String getDriverClassName() {
        switch (databaseType.toLowerCase()) {
            case "mysql":
                return "com.mysql.cj.jdbc.Driver";
            case "postgresql":
                return "org.postgresql.Driver";
            case "mariadb":
                return "org.mariadb.jdbc.Driver";
            case "sqlserver":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case "oracle":
                return "oracle.jdbc.OracleDriver";
            case "h2":
                return "org.h2.Driver";
            default:
                return "com.mysql.cj.jdbc.Driver";
        }
    }

    // 获取验证查询
    public String getValidationQuery() {
        switch (databaseType.toLowerCase()) {
            case "mysql":
            case "postgresql":
            case "mariadb":
            case "sqlserver":
            case "h2":
                return "SELECT 1";
            case "oracle":
                return "SELECT 1 FROM DUAL";
            default:
                return "SELECT 1";
        }
    }
}
