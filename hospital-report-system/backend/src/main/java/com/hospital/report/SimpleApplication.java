package com.hospital.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 医院报表管理系统启动类（简化版本）
 */
@SpringBootApplication
public class SimpleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleApplication.class, args);
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