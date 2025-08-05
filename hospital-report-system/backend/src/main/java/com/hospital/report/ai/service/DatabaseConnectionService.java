package com.hospital.report.ai.service;

import com.hospital.report.entity.DataSource;
import com.hospital.report.service.DataSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接服务
 */
@Service
@Slf4j
public class DatabaseConnectionService {

    @Autowired
    private DataSourceService dataSourceService;

    /**
     * 获取数据库连接
     */
    public Connection getConnection(DataSource dataSource) throws SQLException {
        try {
            String url = buildJdbcUrl(dataSource);
            log.debug("连接数据库: {}", url);
            
            // 解密密码
            String decryptedPassword = dataSourceService.decryptPassword(dataSource.getPassword());
            log.debug("密码解密完成");
            
            return DriverManager.getConnection(url, dataSource.getUsername(), decryptedPassword);
            
        } catch (SQLException e) {
            log.error("连接数据库失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 构建JDBC URL
     */
    private String buildJdbcUrl(DataSource dataSource) {
        String host = dataSource.getHost();
        Integer port = dataSource.getPort();
        String database = dataSource.getDatabaseName();
        String databaseType = dataSource.getDatabaseType().toLowerCase();
        
        switch (databaseType) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true", 
                    host, port, database);
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            case "oracle":
                return String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, database);
            case "sqlserver":
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s", host, port, database);
            default:
                throw new IllegalArgumentException("不支持的数据库类型: " + databaseType);
        }
    }
}