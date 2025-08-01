package com.hospital.report.aspect;

import com.hospital.report.annotation.RequiresPermission;
import com.hospital.report.config.PermissionCheckConfig;
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

    // 使用ThreadLocal来检测递归调用
    private static final ThreadLocal<Boolean> PERMISSION_CHECKING = new ThreadLocal<>();

    @Around("@annotation(com.hospital.report.annotation.RequiresPermission) || @within(com.hospital.report.annotation.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint point) throws Throwable {
        // 检测递归调用，防止StackOverflowError
        if (Boolean.TRUE.equals(PERMISSION_CHECKING.get())) {
            log.debug("检测到权限检查递归调用，跳过权限检查");
            return point.proceed();
        }

        // 获取方法信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        String className = method.getDeclaringClass().getSimpleName();
        String fullClassName = method.getDeclaringClass().getName();

        // 强制跳过认证相关的所有方法，防止无限递归
        if (fullClassName.contains("AuthenticationManager") ||
            fullClassName.contains("AuthenticationProvider") ||
            fullClassName.contains("UserDetailsService") ||
            fullClassName.contains("AuthService") ||
            fullClassName.contains("UserService") ||
            fullClassName.contains("PermissionCache") ||
            methodName.equals("authenticate") ||
            methodName.equals("loadUserByUsername") ||
            methodName.equals("findByUsername") ||
            methodName.equals("findPermissionsByUserId") ||
            methodName.equals("getCurrentUser") ||
            methodName.equals("createUser") ||
            methodName.equals("hasPermission")) {
            log.debug("强制跳过认证相关方法权限检查: {}.{}", className, methodName);
            return point.proceed();
        }

        // 使用配置类检查是否需要跳过权限检查
        if (PermissionCheckConfig.shouldSkipPermissionCheck(className, methodName)) {
            log.debug("跳过权限检查: {}.{}", className, methodName);
            return point.proceed();
        }

        // 跳过数据源相关的方法（临时解决方案）
        if ("DataSourceController".equals(className)) {
            log.debug("跳过数据源控制器权限检查: {}.{}", className, methodName);
            return point.proceed();
        }

        try {
            // 设置权限检查标志
            PERMISSION_CHECKING.set(true);

            // 获取方法级别的权限注解
            RequiresPermission requiresPermission = method.getAnnotation(RequiresPermission.class);

            // 如果方法没有权限注解，检查类级别的权限注解
            if (requiresPermission == null) {
                requiresPermission = method.getDeclaringClass().getAnnotation(RequiresPermission.class);
            }

            // 如果有权限注解且权限值不为空，则进行权限检查
            if (requiresPermission != null && requiresPermission.value().length > 0) {
                String[] permissions = requiresPermission.value();
                RequiresPermission.Logical logical = requiresPermission.logical();

                log.debug("检查权限: 方法={}, 权限={}, 逻辑={}",
                    method.getName(), String.join(",", permissions), logical);

                boolean hasPermission = false;

                if (logical == RequiresPermission.Logical.AND) {
                    // AND逻辑：必须拥有所有权限
                    hasPermission = true;
                    for (String permission : permissions) {
                        try {
                            if (!authService.hasPermission(permission)) {
                                hasPermission = false;
                                log.warn("用户缺少权限: {}", permission);
                                break;
                            }
                        } catch (Exception e) {
                            log.error("检查权限时发生异常: {}", permission, e);
                            hasPermission = false;
                            break;
                        }
                    }
                } else {
                    // OR逻辑：拥有任一权限即可
                    for (String permission : permissions) {
                        try {
                            if (authService.hasPermission(permission)) {
                                hasPermission = true;
                                break;
                            }
                        } catch (Exception e) {
                            log.error("检查权限时发生异常: {}", permission, e);
                        }
                    }
                }

                if (!hasPermission) {
                    String errorMsg = String.format("没有权限访问此资源，需要权限: %s", String.join(",", permissions));
                    log.warn("权限检查失败: 方法={}, 需要权限={}", method.getName(), String.join(",", permissions));
                    throw new RuntimeException(errorMsg);
                }

                log.debug("权限检查通过: 方法={}", method.getName());
            }

            return point.proceed();

        } catch (RuntimeException e) {
            // 重新抛出权限相关的运行时异常
            throw e;
        } catch (Exception e) {
            // 记录其他异常并重新抛出
            log.error("权限检查过程中发生未预期的异常", e);
            throw new RuntimeException("权限检查失败: " + e.getMessage(), e);
        } finally {
            // 清除权限检查标志
            PERMISSION_CHECKING.remove();
        }
    }
}