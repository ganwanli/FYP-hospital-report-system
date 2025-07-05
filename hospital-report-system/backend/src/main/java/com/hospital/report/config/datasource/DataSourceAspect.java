package com.hospital.report.config.datasource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 多数据源处理切面
 * 
 * @author Hospital Report System
 * @since 2024-01-01
 */
@Aspect
@Order(1)
@Component
public class DataSourceAspect {

    private static final Logger log = LoggerFactory.getLogger(DataSourceAspect.class);

    /**
     * 切点：所有标注了@DataSource注解的方法
     */
    @Pointcut("@annotation(com.hospital.report.config.datasource.DataSource) || @within(com.hospital.report.config.datasource.DataSource)")
    public void dsPointCut() {
    }

    /**
     * 环绕通知
     */
    @Around("dsPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        DataSource dataSource = getDataSource(point);
        
        if (dataSource != null) {
            DataSourceContextHolder.setDataSourceType(dataSource.value());
        }
        
        try {
            return point.proceed();
        } finally {
            // 销毁数据源，在执行方法之后
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    /**
     * 获取需要切换的数据源
     */
    public DataSource getDataSource(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        
        DataSource dataSource = AnnotationUtils.findAnnotation(method, DataSource.class);
        if (dataSource != null) {
            return dataSource;
        }

        return AnnotationUtils.findAnnotation(signature.getDeclaringType(), DataSource.class);
    }
}