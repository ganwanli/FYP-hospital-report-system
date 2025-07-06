package com.hospital.report.aspect;

import com.hospital.report.annotation.RequiresPermission;
import com.hospital.report.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final AuthService authService;

    @Around("@annotation(com.hospital.report.annotation.RequiresPermission) || @within(com.hospital.report.annotation.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        
        RequiresPermission requiresPermission = method.getAnnotation(RequiresPermission.class);
        if (requiresPermission == null) {
            requiresPermission = method.getDeclaringClass().getAnnotation(RequiresPermission.class);
        }
        
        if (requiresPermission != null && requiresPermission.value().length > 0) {
            String[] permissions = requiresPermission.value();
            RequiresPermission.Logical logical = requiresPermission.logical();
            
            boolean hasPermission = false;
            
            if (logical == RequiresPermission.Logical.AND) {
                hasPermission = true;
                for (String permission : permissions) {
                    if (!authService.hasPermission(permission)) {
                        hasPermission = false;
                        break;
                    }
                }
            } else {
                for (String permission : permissions) {
                    if (authService.hasPermission(permission)) {
                        hasPermission = true;
                        break;
                    }
                }
            }
            
            if (!hasPermission) {
                throw new RuntimeException("没有权限访问此资源");
            }
        }
        
        return point.proceed();
    }
}