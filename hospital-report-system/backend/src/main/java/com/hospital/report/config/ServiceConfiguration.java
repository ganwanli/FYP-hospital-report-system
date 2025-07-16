package com.hospital.report.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 服务配置类
 * 用于解决循环依赖问题
 * 
 * @author system
 * @since 2025-01-16
 */
@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
public class ServiceConfiguration {
    
    // 这个配置类主要用于启用AOP代理，帮助解决循环依赖问题
    // exposeProxy = true 允许在代理对象中访问当前代理
    
}
