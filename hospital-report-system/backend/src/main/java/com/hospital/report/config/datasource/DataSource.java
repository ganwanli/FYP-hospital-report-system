package com.hospital.report.config.datasource;

import java.lang.annotation.*;

/**
 * 数据源切换注解
 * 
 * @author Hospital Report System
 * @since 2024-01-01
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DataSource {
    
    /**
     * 切换数据源名称
     */
    DataSourceType value() default DataSourceType.MASTER;
}