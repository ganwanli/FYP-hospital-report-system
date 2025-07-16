package com.hospital.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 医院报表管理系统启动类
 * 
 * @author Hospital Report System
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
@ComponentScan(basePackages = {"com.hospital.report"})
public class HospitalReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalReportApplication.class, args);
        System.out.println("""
                
                ===================================
                医院报表管理系统启动成功！
                Hospital Report System Started!
                ===================================
                API文档地址: http://localhost:8080/api/swagger-ui.html
                ===================================
                """);
    }
}