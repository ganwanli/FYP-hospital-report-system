package com.hospital.report.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA配置类
 * 
 * @author Hospital Report System
 * @since 2024-01-01
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.hospital.report.repository")
@EntityScan(basePackages = "com.hospital.report.entity")
@EnableTransactionManagement
public class JpaConfig {
    
    // JPA配置已通过application.yml文件配置
    // 这个类主要用于确保Repository和Entity的正确扫描
}
