package com.hospital.report.config.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源
 * 
 * @author Hospital Report System
 * @since 2024-01-01
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * 确定当前的查找键
     * 这个方法是AbstractRoutingDataSource的关键方法，用于决定使用哪个数据源
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceType();
    }
}