package com.hospital.report.aspect;

import com.hospital.report.annotation.DataSource;
import com.hospital.report.config.DynamicDataSourceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
// @Component
@Order(1)
@RequiredArgsConstructor
public class DataSourceAspect {

    private final DynamicDataSourceManager dataSourceManager;

    @Around("@annotation(com.hospital.report.annotation.DataSource) || @within(com.hospital.report.annotation.DataSource)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        
        DataSource dataSource = method.getAnnotation(DataSource.class);
        if (dataSource == null) {
            dataSource = method.getDeclaringClass().getAnnotation(DataSource.class);
        }
        
        String dataSourceKey = null;
        if (dataSource != null) {
            dataSourceKey = dataSource.value();
            log.debug("切换到数据源: {}", dataSourceKey);
            dataSourceManager.setCurrentDataSource(dataSourceKey);
        }
        
        try {
            return point.proceed();
        } finally {
            if (dataSourceKey != null) {
                dataSourceManager.clearCurrentDataSource();
                log.debug("清除数据源上下文: {}", dataSourceKey);
            }
        }
    }
}