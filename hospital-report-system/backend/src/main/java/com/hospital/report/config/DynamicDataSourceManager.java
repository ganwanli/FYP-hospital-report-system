package com.hospital.report.config;

import com.hospital.report.utils.AESUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicDataSourceManager {

    private final AESUtil aesUtil;
    
    private final Map<String, HikariDataSource> dataSourceMap = new ConcurrentHashMap<>();
    
    private static final ThreadLocal<String> currentDataSource = new ThreadLocal<>();
    
    public void createDataSource(String key, DataSourceConfig config) {
        if (dataSourceMap.containsKey(key)) {
            log.warn("数据源 {} 已存在，将覆盖原有配置", key);
            removeDataSource(key);
        }
        
        try {
            HikariConfig hikariConfig = buildHikariConfig(config);
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            
            // 测试连接
            try (Connection connection = dataSource.getConnection()) {
                log.info("数据源 {} 创建成功", key);
            }
            
            dataSourceMap.put(key, dataSource);
        } catch (Exception e) {
            log.error("创建数据源 {} 失败", key, e);
            throw new RuntimeException("创建数据源失败: " + e.getMessage(), e);
        }
    }
    
    public void removeDataSource(String key) {
        HikariDataSource dataSource = dataSourceMap.remove(key);
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("数据源 {} 已移除", key);
        }
    }
    
    public HikariDataSource getDataSource(String key) {
        return dataSourceMap.get(key);
    }
    
    public boolean testConnection(DataSourceConfig config) {
        HikariConfig hikariConfig = buildHikariConfig(config);
        try (HikariDataSource testDataSource = new HikariDataSource(hikariConfig);
             Connection connection = testDataSource.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            log.error("测试数据源连接失败", e);
            return false;
        }
    }
    
    public void setCurrentDataSource(String key) {
        currentDataSource.set(key);
    }
    
    public String getCurrentDataSource() {
        return currentDataSource.get();
    }
    
    public void clearCurrentDataSource() {
        currentDataSource.remove();
    }
    
    private HikariConfig buildHikariConfig(DataSourceConfig config) {
        HikariConfig hikariConfig = new HikariConfig();

        // 添加空值检查和日志
        String driverClassName = config.getDriverClassName();
        String jdbcUrl = config.getJdbcUrl();
        String username = config.getUsername();

        log.debug("Building HikariConfig with:");
        log.debug("  driverClassName: {}", driverClassName);
        log.debug("  jdbcUrl: {}", jdbcUrl);
        log.debug("  username: {}", username);

        if (driverClassName == null || driverClassName.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver class name cannot be null or empty");
        }
        if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("JDBC URL cannot be null or empty");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        
        // 解密密码
        String password = config.getPassword();
        try {
            if (password != null && !password.isEmpty()) {
                password = aesUtil.decrypt(password);
                log.debug("密码解密成功");
            }
        } catch (Exception e) {
            log.warn("密码解密失败，使用原始密码: {}", e.getMessage());
        }
        hikariConfig.setPassword(password);
        
        // 连接池配置
        hikariConfig.setMinimumIdle(config.getMinIdle() != null ? config.getMinIdle() : 5);
        hikariConfig.setMaximumPoolSize(config.getMaxActive() != null ? config.getMaxActive() : 20);
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout() != null ? config.getConnectionTimeout() : 30000);
        hikariConfig.setIdleTimeout(config.getIdleTimeout() != null ? config.getIdleTimeout() : 600000);
        hikariConfig.setMaxLifetime(config.getMaxLifetime() != null ? config.getMaxLifetime() : 1800000);
        hikariConfig.setLeakDetectionThreshold(config.getLeakDetectionThreshold() != null ? config.getLeakDetectionThreshold() : 60000);
        
        // 连接测试
        if (config.getValidationQuery() != null && !config.getValidationQuery().isEmpty()) {
            hikariConfig.setConnectionTestQuery(config.getValidationQuery());
        }
        
        // 连接池名称
        hikariConfig.setPoolName(config.getDatasourceCode() + "-Pool");
        
        return hikariConfig;
    }
    
    public Map<String, Object> getDataSourceStats(String key) {
        HikariDataSource dataSource = dataSourceMap.get(key);
        if (dataSource == null) {
            return null;
        }
        
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("poolName", dataSource.getPoolName());
        stats.put("activeConnections", dataSource.getHikariPoolMXBean().getActiveConnections());
        stats.put("idleConnections", dataSource.getHikariPoolMXBean().getIdleConnections());
        stats.put("totalConnections", dataSource.getHikariPoolMXBean().getTotalConnections());
        stats.put("threadsAwaitingConnection", dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        stats.put("maximumPoolSize", dataSource.getMaximumPoolSize());
        stats.put("minimumIdle", dataSource.getMinimumIdle());
        stats.put("isClosed", dataSource.isClosed());
        
        return stats;
    }
    
    public static class DataSourceConfig {
        private String datasourceCode;
        private String databaseType;
        private String driverClassName;
        private String jdbcUrl;
        private String username;
        private String password;
        private Integer minIdle;
        private Integer maxActive;
        private Long connectionTimeout;
        private Long idleTimeout;
        private Long maxLifetime;
        private Long leakDetectionThreshold;
        private String validationQuery;
        
        // Getters and Setters
        public String getDatasourceCode() { return datasourceCode; }
        public void setDatasourceCode(String datasourceCode) { this.datasourceCode = datasourceCode; }
        
        public String getDatabaseType() { return databaseType; }
        public void setDatabaseType(String databaseType) { this.databaseType = databaseType; }
        
        public String getDriverClassName() { return driverClassName; }
        public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
        
        public String getJdbcUrl() { return jdbcUrl; }
        public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public Integer getMinIdle() { return minIdle; }
        public void setMinIdle(Integer minIdle) { this.minIdle = minIdle; }
        
        public Integer getMaxActive() { return maxActive; }
        public void setMaxActive(Integer maxActive) { this.maxActive = maxActive; }
        
        public Long getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(Long connectionTimeout) { this.connectionTimeout = connectionTimeout; }
        
        public Long getIdleTimeout() { return idleTimeout; }
        public void setIdleTimeout(Long idleTimeout) { this.idleTimeout = idleTimeout; }
        
        public Long getMaxLifetime() { return maxLifetime; }
        public void setMaxLifetime(Long maxLifetime) { this.maxLifetime = maxLifetime; }
        
        public Long getLeakDetectionThreshold() { return leakDetectionThreshold; }
        public void setLeakDetectionThreshold(Long leakDetectionThreshold) { this.leakDetectionThreshold = leakDetectionThreshold; }
        
        public String getValidationQuery() { return validationQuery; }
        public void setValidationQuery(String validationQuery) { this.validationQuery = validationQuery; }
    }
}