package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.report.config.DynamicDataSourceManager;
import com.hospital.report.entity.DataSource;
import com.hospital.report.entity.User;
import com.hospital.report.mapper.DataSourceMapper;
import com.hospital.report.service.AuthService;
import com.hospital.report.service.DataSourceService;
import com.hospital.report.utils.AESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSourceServiceImpl extends ServiceImpl<DataSourceMapper, DataSource> implements DataSourceService {

    private final DataSourceMapper dataSourceMapper;
    private final DynamicDataSourceManager dataSourceManager;
    private final AuthService authService;
    private final AESUtil aesUtil;

    private static final Map<String, String> DATABASE_DRIVERS = new HashMap<>();
    private static final Map<String, String> VALIDATION_QUERIES = new HashMap<>();

    static {
        DATABASE_DRIVERS.put("MySQL", "com.mysql.cj.jdbc.Driver");
        DATABASE_DRIVERS.put("PostgreSQL", "org.postgresql.Driver");
        DATABASE_DRIVERS.put("Oracle", "oracle.jdbc.OracleDriver");
        DATABASE_DRIVERS.put("SQL Server", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        DATABASE_DRIVERS.put("H2", "org.h2.Driver");

        VALIDATION_QUERIES.put("MySQL", "SELECT 1");
        VALIDATION_QUERIES.put("PostgreSQL", "SELECT 1");
        VALIDATION_QUERIES.put("Oracle", "SELECT 1 FROM DUAL");
        VALIDATION_QUERIES.put("SQL Server", "SELECT 1");
        VALIDATION_QUERIES.put("H2", "SELECT 1");
    }

    @PostConstruct
    public void initDataSources() {
        log.info("初始化动态数据源...");
        try {
            refreshAllDataSources();
            log.info("动态数据源初始化完成");
        } catch (Exception e) {
            log.warn("动态数据源初始化失败，但应用将继续启动: {}", e.getMessage());
        }
    }

    @Override
    public List<DataSource> findActiveDataSources() {
        return dataSourceMapper.findActiveDataSources();
    }

    @Override
    public DataSource findByCode(String code) {
        return dataSourceMapper.findByCode(code);
    }

    @Override
    public DataSource findDefaultDataSource() {
        return dataSourceMapper.findDefaultDataSource();
    }

    @Override
    public boolean testConnection(DataSource dataSource) {
        try {
            DynamicDataSourceManager.DataSourceConfig config = convertToConfig(dataSource);
            return dataSourceManager.testConnection(config);
        } catch (Exception e) {
            log.error("测试数据源连接失败: {}", dataSource.getDatasourceCode(), e);
            return false;
        }
    }

    @Override
    public boolean testConnection(Long dataSourceId) {
        DataSource dataSource = getById(dataSourceId);
        if (dataSource == null) {
            return false;
        }
        return testConnection(dataSource);
    }

    @Override
    @Transactional
    public boolean createDataSource(DataSource dataSource) {
        try {
            // 设置默认值
            if (dataSource.getInitialSize() == null) dataSource.setInitialSize(5);
            if (dataSource.getMinIdle() == null) dataSource.setMinIdle(5);
            if (dataSource.getMaxActive() == null) dataSource.setMaxActive(20);
            if (dataSource.getMaxWait() == null) dataSource.setMaxWait(60000L);
            if (dataSource.getConnectionTimeout() == null) dataSource.setConnectionTimeout(30000L);
            if (dataSource.getIdleTimeout() == null) dataSource.setIdleTimeout(600000L);
            if (dataSource.getMaxLifetime() == null) dataSource.setMaxLifetime(1800000L);
            if (dataSource.getLeakDetectionThreshold() == null) dataSource.setLeakDetectionThreshold(60000L);
            if (dataSource.getTestWhileIdle() == null) dataSource.setTestWhileIdle(true);
            if (dataSource.getTestOnBorrow() == null) dataSource.setTestOnBorrow(false);
            if (dataSource.getTestOnReturn() == null) dataSource.setTestOnReturn(false);

            // 加密密码
            dataSource.setPassword(encryptPassword(dataSource.getPassword()));

            // 设置驱动和验证查询
            if (dataSource.getDriverClassName() == null || dataSource.getDriverClassName().isEmpty()) {
                dataSource.setDriverClassName(getDriverClassName(dataSource.getDatabaseType()));
            }
            if (dataSource.getValidationQuery() == null || dataSource.getValidationQuery().isEmpty()) {
                dataSource.setValidationQuery(getValidationQuery(dataSource.getDatabaseType()));
            }

            // 从 jdbcUrl 中提取 host 和 port
            extractHostAndPortFromJdbcUrl(dataSource);

            // 设置创建者和更新者信息
            try {
                User currentUser = authService.getCurrentUser();
                if (currentUser != null) {
                    dataSource.setCreatedBy(currentUser.getId());
                    dataSource.setUpdatedBy(currentUser.getId());
                } else {
                    // 如果没有当前用户（比如系统初始化），设置为系统用户ID
                    dataSource.setCreatedBy(1L);
                    dataSource.setUpdatedBy(1L);
                }
            } catch (Exception e) {
                log.warn("无法获取当前用户信息，使用默认用户ID", e);
                // 设置为系统用户ID
                dataSource.setCreatedBy(1L);
                dataSource.setUpdatedBy(1L);
            }

            dataSource.setCreatedTime(LocalDateTime.now());
            dataSource.setUpdatedTime(LocalDateTime.now());
            dataSource.setStatus(1);
            dataSource.setIsDeleted(false);

            boolean result = save(dataSource);
            if (result) {
                // 创建动态数据源
                refreshDataSource(dataSource.getDatasourceCode());
            }
            return result;
        } catch (Exception e) {
            log.error("创建数据源失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateDataSource(DataSource dataSource) {
        try {
            DataSource existing = getById(dataSource.getId());
            if (existing == null) {
                return false;
            }

            // 如果密码未变更，保持原有密码
            if (dataSource.getPassword() == null || dataSource.getPassword().isEmpty()) {
                dataSource.setPassword(existing.getPassword());
            } else {
                dataSource.setPassword(encryptPassword(dataSource.getPassword()));
            }

            // 设置更新者信息
            try {
                User currentUser = authService.getCurrentUser();
                if (currentUser != null) {
                    dataSource.setUpdatedBy(currentUser.getId());
                } else {
                    dataSource.setUpdatedBy(1L);
                }
            } catch (Exception e) {
                log.warn("无法获取当前用户信息，使用默认用户ID", e);
                dataSource.setUpdatedBy(1L);
            }

            dataSource.setUpdatedTime(LocalDateTime.now());
            boolean result = updateById(dataSource);
            if (result) {
                // 刷新动态数据源
                refreshDataSource(dataSource.getDatasourceCode());
            }
            return result;
        } catch (Exception e) {
            log.error("更新数据源失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteDataSource(Long dataSourceId) {
        try {
            DataSource dataSource = getById(dataSourceId);
            if (dataSource == null) {
                return false;
            }

            // 先设置更新者信息，然后进行逻辑删除
            try {
                User currentUser = authService.getCurrentUser();
                if (currentUser != null) {
                    dataSource.setUpdatedBy(currentUser.getId());
                } else {
                    dataSource.setUpdatedBy(1L);
                }
            } catch (Exception e) {
                log.warn("无法获取当前用户信息，使用默认用户ID", e);
                dataSource.setUpdatedBy(1L);
            }

            dataSource.setUpdatedTime(LocalDateTime.now());
            // 先更新 updated_by 和 updated_time
            updateById(dataSource);

            // 使用 MyBatis-Plus 的逻辑删除
            boolean result = removeById(dataSourceId);

            if (result) {
                // 移除动态数据源
                dataSourceManager.removeDataSource(dataSource.getDatasourceCode());
            }
            return result;
        } catch (Exception e) {
            log.error("删除数据源失败", e);
            return false;
        }
    }

    @Override
    public void refreshDataSource(String code) {
        DataSource dataSource = findByCode(code);
        if (dataSource != null && dataSource.getStatus() == 1) {
            try {
                DynamicDataSourceManager.DataSourceConfig config = convertToConfig(dataSource);
                dataSourceManager.createDataSource(code, config);
                log.info("动态数据源刷新成功: {}", code);
            } catch (Exception e) {
                // 动态数据源创建失败不应该影响数据库更新操作
                // 只记录警告，不抛出异常
                log.warn("动态数据源刷新失败，但数据库更新已成功: {} - {}", code, e.getMessage());
            }
        }
    }

    @Override
    public void refreshAllDataSources() {
        List<DataSource> activeDataSources = findActiveDataSources();
        for (DataSource dataSource : activeDataSources) {
            try {
                refreshDataSource(dataSource.getDatasourceCode());
            } catch (Exception e) {
                log.warn("刷新数据源 {} 失败，跳过: {}", dataSource.getDatasourceCode(), e.getMessage());
            }
        }
    }

    @Override
    public Map<String, Object> getDataSourceStats(String code) {
        return dataSourceManager.getDataSourceStats(code);
    }

    @Override
    public List<Map<String, Object>> getAllDataSourceStats() {
        List<DataSource> activeDataSources = findActiveDataSources();
        return activeDataSources.stream()
                .map(ds -> {
                    Map<String, Object> stats = dataSourceManager.getDataSourceStats(ds.getDatasourceCode());
                    if (stats != null) {
                        stats.put("datasourceCode", ds.getDatasourceCode());
                        stats.put("datasourceName", ds.getDatasourceName());
                        stats.put("databaseType", ds.getDatabaseType());
                    }
                    return stats;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public String encryptPassword(String password) {
        return aesUtil.encrypt(password);
    }

    @Override
    public String decryptPassword(String encryptedPassword) {
        return aesUtil.decrypt(encryptedPassword);
    }

    @Override
    public List<String> getSupportedDatabaseTypes() {
        return new ArrayList<>(DATABASE_DRIVERS.keySet());
    }

    @Override
    public String getDriverClassName(String databaseType) {
        return DATABASE_DRIVERS.get(databaseType);
    }

    @Override
    public String getValidationQuery(String databaseType) {
        return VALIDATION_QUERIES.get(databaseType);
    }

    private DynamicDataSourceManager.DataSourceConfig convertToConfig(DataSource dataSource) {
        DynamicDataSourceManager.DataSourceConfig config = new DynamicDataSourceManager.DataSourceConfig();

        // 添加调试日志
        log.debug("Converting DataSource to Config:");
        log.debug("  datasourceCode: {}", dataSource.getDatasourceCode());
        log.debug("  databaseType: {}", dataSource.getDatabaseType());
        log.debug("  driverClassName: {}", dataSource.getDriverClassName());
        log.debug("  jdbcUrl: {}", dataSource.getJdbcUrl());
        log.debug("  username: {}", dataSource.getUsername());

        // 手动设置字段值以确保正确复制
        config.setDatasourceCode(dataSource.getDatasourceCode());
        config.setDatabaseType(dataSource.getDatabaseType());
        config.setDriverClassName(dataSource.getDriverClassName());
        config.setJdbcUrl(dataSource.getJdbcUrl());
        config.setUsername(dataSource.getUsername());
        config.setPassword(dataSource.getPassword());
        config.setMinIdle(dataSource.getInitialSize());
        config.setMaxActive(dataSource.getMaxActive());
        config.setConnectionTimeout(dataSource.getConnectionTimeout());

        // 验证关键字段不为空
        if (config.getDriverClassName() == null) {
            log.error("DriverClassName is null after conversion!");
        }

        return config;
    }

    /**
     * 从 JDBC URL 中提取 host 和 port
     */
    private void extractHostAndPortFromJdbcUrl(DataSource dataSource) {
        String jdbcUrl = dataSource.getJdbcUrl();
        if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
            try {
                // 解析不同类型的 JDBC URL
                if (jdbcUrl.startsWith("jdbc:mysql://")) {
                    // MySQL: jdbc:mysql://localhost:3306/database
                    String remaining = jdbcUrl.substring("jdbc:mysql://".length());
                    int slashIndex = remaining.indexOf('/');

                    String hostPort;
                    String databaseName = "";

                    if (slashIndex > 0) {
                        hostPort = remaining.substring(0, slashIndex);
                        // 提取数据库名称
                        String dbPart = remaining.substring(slashIndex + 1);
                        int questionIndex = dbPart.indexOf('?');
                        if (questionIndex > 0) {
                            databaseName = dbPart.substring(0, questionIndex);
                        } else {
                            databaseName = dbPart;
                        }
                    } else {
                        hostPort = remaining;
                    }

                    String[] parts = hostPort.split(":");
                    dataSource.setHost(parts[0]);
                    if (parts.length > 1) {
                        dataSource.setPort(Integer.parseInt(parts[1]));
                    } else {
                        dataSource.setPort(3306); // MySQL 默认端口
                    }
                    dataSource.setDatabaseName(databaseName);
                } else if (jdbcUrl.startsWith("jdbc:postgresql://")) {
                    // PostgreSQL: jdbc:postgresql://localhost:5432/database
                    String remaining = jdbcUrl.substring("jdbc:postgresql://".length());
                    int slashIndex = remaining.indexOf('/');

                    String hostPort;
                    String databaseName = "";

                    if (slashIndex > 0) {
                        hostPort = remaining.substring(0, slashIndex);
                        // 提取数据库名称
                        String dbPart = remaining.substring(slashIndex + 1);
                        int questionIndex = dbPart.indexOf('?');
                        if (questionIndex > 0) {
                            databaseName = dbPart.substring(0, questionIndex);
                        } else {
                            databaseName = dbPart;
                        }
                    } else {
                        hostPort = remaining;
                    }

                    String[] parts = hostPort.split(":");
                    dataSource.setHost(parts[0]);
                    if (parts.length > 1) {
                        dataSource.setPort(Integer.parseInt(parts[1]));
                    } else {
                        dataSource.setPort(5432); // PostgreSQL 默认端口
                    }
                    dataSource.setDatabaseName(databaseName);
                } else {
                    // 默认情况，尝试提取 localhost
                    dataSource.setHost("localhost");
                    dataSource.setPort(3306);
                    dataSource.setDatabaseName("test");
                }
            } catch (Exception e) {
                log.warn("无法从 JDBC URL 中提取 host 和 port: {}", jdbcUrl, e);
                // 设置默认值
                dataSource.setHost("localhost");
                dataSource.setPort(3306);
                dataSource.setDatabaseName("test");
            }
        } else {
            // 设置默认值
            dataSource.setHost("localhost");
            dataSource.setPort(3306);
            dataSource.setDatabaseName("test");
        }
    }
}