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
        try {
            // 首先尝试直接使用DriverManager测试连接，避免HikariCP的类加载器问题
            return testConnectionDirectly(config);
        } catch (Exception e) {
            log.error("直接连接测试失败，尝试使用HikariCP: {}", e.getMessage());
            // 如果直接连接失败，再尝试HikariCP方式
            return testConnectionWithHikari(config);
        }
    }
    
    /**
     * 直接使用DriverManager测试连接，避免HikariCP的类加载器问题
     */
    private boolean testConnectionDirectly(DataSourceConfig config) {
        String driverClassName = config.getDriverClassName();
        String jdbcUrl = config.getJdbcUrl();
        String username = config.getUsername();
        String password = config.getPassword();
        
        log.debug("Testing connection directly with DriverManager:");
        log.debug("  driverClassName: {}", driverClassName);
        log.debug("  jdbcUrl: {}", jdbcUrl);
        log.debug("  username: {}", username);
        
        try {
            // 使用多种方式尝试加载驱动程序
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader currentClassLoader = this.getClass().getClassLoader();
            
            Class<?> driverClass = null;
            Exception lastException = null;
            
            // 方法1: 使用当前类的类加载器
            try {
                driverClass = currentClassLoader.loadClass(driverClassName);
                log.debug("Driver loaded via current class loader: {}", driverClassName);
            } catch (ClassNotFoundException e) {
                lastException = e;
                log.debug("Failed to load driver via current class loader: {}", e.getMessage());
            }
            
            // 方法2: 使用上下文类加载器
            if (driverClass == null && contextClassLoader != null) {
                try {
                    driverClass = contextClassLoader.loadClass(driverClassName);
                    log.debug("Driver loaded via context class loader: {}", driverClassName);
                } catch (ClassNotFoundException e) {
                    lastException = e;
                    log.debug("Failed to load driver via context class loader: {}", e.getMessage());
                }
            }
            
            // 方法3: 使用Class.forName
            if (driverClass == null) {
                try {
                    driverClass = Class.forName(driverClassName);
                    log.debug("Driver loaded via Class.forName: {}", driverClassName);
                } catch (ClassNotFoundException e) {
                    lastException = e;
                    log.debug("Failed to load driver via Class.forName: {}", e.getMessage());
                }
            }
            
            // 方法4: 使用Class.forName with current class loader
            if (driverClass == null) {
                try {
                    driverClass = Class.forName(driverClassName, true, currentClassLoader);
                    log.debug("Driver loaded via Class.forName with current class loader: {}", driverClassName);
                } catch (ClassNotFoundException e) {
                    lastException = e;
                    log.debug("Failed to load driver via Class.forName with current class loader: {}", e.getMessage());
                }
            }
            
            if (driverClass == null) {
                log.error("Failed to load driver class {} using all methods", driverClassName);
                if (lastException != null) {
                    throw new RuntimeException("Driver class not found: " + driverClassName, lastException);
                } else {
                    throw new RuntimeException("Driver class not found: " + driverClassName);
                }
            }
            
            log.debug("Driver loaded successfully: {}", driverClassName);
            
            // 确保驱动程序注册
            if (driverClass != null) {
                Object driverInstance = driverClass.getDeclaredConstructor().newInstance();
                if (driverInstance instanceof java.sql.Driver) {
                    java.sql.DriverManager.registerDriver((java.sql.Driver) driverInstance);
                    log.debug("Driver registered successfully: {}", driverClassName);
                }
            }
            
            // 直接使用DriverManager创建连接
            try (java.sql.Connection connection = java.sql.DriverManager.getConnection(jdbcUrl, username, password)) {
                boolean isValid = connection.isValid(5);
                log.debug("Connection test result: {}", isValid);
                return isValid;
            }
        } catch (java.sql.SQLException e) {
            log.error("SQL connection failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during connection test: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 使用HikariCP测试连接（备用方案）
     */
    private boolean testConnectionWithHikari(DataSourceConfig config) {
        HikariConfig hikariConfig = buildHikariConfigForTesting(config);
        try (HikariDataSource testDataSource = new HikariDataSource(hikariConfig);
             Connection connection = testDataSource.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            log.error("测试数据源连接失败: {} - URL: {} - User: {}", e.getMessage(), config.getJdbcUrl(), config.getUsername(), e);
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
        
        // 处理密码 - 区分加密密码和明文密码
        String password = config.getPassword();
        if (password != null && !password.isEmpty()) {
            // 判断是否为加密密码（Base64编码的密码通常不包含特殊字符，且长度固定）
            boolean isEncrypted = isEncryptedPassword(password);

            if (isEncrypted) {
                try {
                    password = aesUtil.decrypt(password);
                    log.debug("密码解密成功");
                } catch (Exception e) {
                    log.error("密码解密失败: {}", e.getMessage());
                    throw new IllegalArgumentException("Invalid encrypted password", e);
                }
            } else {
                // 明文密码直接使用
                log.debug("使用明文密码");
            }
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

    /**
     * 构建用于测试连接的 HikariConfig
     * 测试连接时，密码总是作为明文处理（因为来自前端用户输入）
     */
    private HikariConfig buildHikariConfigForTesting(DataSourceConfig config) {
        HikariConfig hikariConfig = new HikariConfig();

        // 添加空值检查和日志
        String driverClassName = config.getDriverClassName();
        String jdbcUrl = config.getJdbcUrl();
        String username = config.getUsername();

        log.debug("Building HikariConfig for testing with:");
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

        // 预加载驱动程序类，避免HikariCP类加载器问题
        try {
            log.debug("Attempting to preload driver class: {}", driverClassName);
            Class.forName(driverClassName);
            log.debug("Driver class preloaded successfully: {}", driverClassName);
        } catch (ClassNotFoundException e) {
            log.error("Failed to preload driver class: {}", driverClassName, e);
            throw new IllegalArgumentException("Driver class not found: " + driverClassName, e);
        }

        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        
        // 测试连接时，密码总是作为明文处理
        String password = config.getPassword();
        if (password != null && !password.isEmpty()) {
            log.debug("使用明文密码进行测试连接");
            hikariConfig.setPassword(password);
        }
        
        // 连接池配置 - 测试时使用较小的配置
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout() != null ? config.getConnectionTimeout() : 30000);
        
        // 连接测试
        if (config.getValidationQuery() != null && !config.getValidationQuery().isEmpty()) {
            hikariConfig.setConnectionTestQuery(config.getValidationQuery());
        }
        
        // 连接池名称
        hikariConfig.setPoolName("TestConnection-Pool-" + System.currentTimeMillis());
        
        return hikariConfig;
    }

    /**
     * 判断密码是否为加密密码
     * 加密密码通常是Base64编码，有固定的特征
     */
    private boolean isEncryptedPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        // Base64编码的特征：
        // 1. 只包含A-Z, a-z, 0-9, +, /, = 字符
        // 2. 长度是4的倍数（padding后）
        // 3. 只有末尾可能有1-2个=号

        // 检查字符集
        if (!password.matches("^[A-Za-z0-9+/]*={0,2}$")) {
            return false;
        }

        // 检查长度
        if (password.length() % 4 != 0) {
            return false;
        }

        // 检查是否看起来像加密后的密码（通常比较长且随机）
        // 明文密码通常比较短且有意义，加密密码通常较长且随机
        if (password.length() < 16) {
            return false;
        }

        // 尝试Base64解码，如果失败说明不是有效的Base64
        try {
            java.util.Base64.getDecoder().decode(password);
            return true;
        } catch (Exception e) {
            return false;
        }
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