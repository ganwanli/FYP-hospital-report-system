package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.report.config.DynamicDataSourceManager;
import com.hospital.report.entity.DataSource;
import com.hospital.report.mapper.DataSourceMapper;
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
        refreshAllDataSources();
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

            // 逻辑删除
            dataSource.setIsDeleted(true);
            dataSource.setUpdatedTime(LocalDateTime.now());
            boolean result = updateById(dataSource);

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
                log.info("刷新数据源成功: {}", code);
            } catch (Exception e) {
                log.error("刷新数据源失败: {}", code, e);
            }
        }
    }

    @Override
    public void refreshAllDataSources() {
        List<DataSource> activeDataSources = findActiveDataSources();
        for (DataSource dataSource : activeDataSources) {
            refreshDataSource(dataSource.getDatasourceCode());
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
        BeanUtils.copyProperties(dataSource, config);
        return config;
    }
}