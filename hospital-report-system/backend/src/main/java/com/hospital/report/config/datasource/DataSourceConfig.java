package com.hospital.report.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 多数据源配置
 * 
 * @author Hospital Report System
 * @since 2024-01-01
 */
// @Configuration
public class DataSourceConfig {

    /**
     * 主数据源配置
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.primary")
    public DataSource primaryDataSource() {
        return new HikariDataSource();
    }

    /**
     * 从数据源配置
     */
    @Bean
    @ConfigurationProperties("app.datasource.dynamic.datasource.slave")
    public DataSource slaveDataSource() {
        return new HikariDataSource();
    }

    /**
     * 动态数据源配置
     */
    @Bean
    public DynamicDataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        
        dataSourceMap.put(DataSourceType.MASTER.name(), primaryDataSource());
        dataSourceMap.put(DataSourceType.SLAVE.name(), slaveDataSource());
        
        // 设置数据源映射
        dynamicDataSource.setTargetDataSources(dataSourceMap);
        // 设置默认数据源
        dynamicDataSource.setDefaultTargetDataSource(primaryDataSource());
        
        return dynamicDataSource;
    }

    /**
     * 主数据源JdbcTemplate
     */
    @Bean
    @Primary
    public JdbcTemplate primaryJdbcTemplate() {
        return new JdbcTemplate(primaryDataSource());
    }

    /**
     * 从数据源JdbcTemplate
     */
    @Bean
    public JdbcTemplate slaveJdbcTemplate() {
        return new JdbcTemplate(slaveDataSource());
    }
}