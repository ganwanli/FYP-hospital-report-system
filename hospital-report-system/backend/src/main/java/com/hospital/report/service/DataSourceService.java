package com.hospital.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.report.entity.DataSource;

import java.util.List;
import java.util.Map;

public interface DataSourceService extends IService<DataSource> {

    List<DataSource> findActiveDataSources();

    DataSource findByCode(String code);

    DataSource findDefaultDataSource();

    boolean testConnection(DataSource dataSource);

    boolean testConnection(Long dataSourceId);

    boolean createDataSource(DataSource dataSource);

    boolean updateDataSource(DataSource dataSource);

    boolean deleteDataSource(Long dataSourceId);

    void refreshDataSource(String code);

    void refreshAllDataSources();

    Map<String, Object> getDataSourceStats(String code);

    List<Map<String, Object>> getAllDataSourceStats();

    String encryptPassword(String password);

    String decryptPassword(String encryptedPassword);

    List<String> getSupportedDatabaseTypes();

    String getDriverClassName(String databaseType);

    String getValidationQuery(String databaseType);
}