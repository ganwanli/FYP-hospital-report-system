package com.hospital.report.config;

import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 权限检查配置类
 * 用于配置哪些类和方法需要跳过权限检查，防止递归调用
 * 
 * @author system
 * @since 2025-01-16
 */
@Configuration
public class PermissionCheckConfig {
    
    /**
     * 需要跳过权限检查的类名集合
     */
    private static final Set<String> SKIP_PERMISSION_CHECK_CLASSES = new HashSet<>(Arrays.asList(
        "AuthServiceImpl",
        "UserServiceImpl", 
        "PermissionCacheServiceImpl",
        "JwtAuthenticationFilter",
        "SecurityConfig",
        "PermissionAspect"
    ));
    
    /**
     * 需要跳过权限检查的方法名集合
     */
    private static final Set<String> SKIP_PERMISSION_CHECK_METHODS = new HashSet<>(Arrays.asList(
        "loadUserByUsername",
        "findByUsername",
        "findPermissionsByUserId",
        "findRolesByUserId",
        "getCurrentUser",
        "hasPermission",
        "hasRole",
        "authenticate",
        "checkPermission",
        "login",
        "logout",
        "refresh",
        "refreshUserPermissions",
        "cacheUserPermissions",
        "cacheUserRoles",
        "clearUserCache",
        "getUserPermissions",
        "getUserRoles"
    ));
    
    /**
     * 检查指定的类是否需要跳过权限检查
     * 
     * @param className 类名
     * @return true表示需要跳过，false表示需要检查
     */
    public static boolean shouldSkipPermissionCheck(String className) {
        return SKIP_PERMISSION_CHECK_CLASSES.contains(className);
    }
    
    /**
     * 检查指定的方法是否需要跳过权限检查
     * 
     * @param methodName 方法名
     * @return true表示需要跳过，false表示需要检查
     */
    public static boolean shouldSkipPermissionCheck(String className, String methodName) {
        return shouldSkipPermissionCheck(className) || SKIP_PERMISSION_CHECK_METHODS.contains(methodName);
    }
    
    /**
     * 检查是否是认证相关的端点，这些端点不需要权限检查
     * 
     * @param requestUri 请求URI
     * @return true表示是认证相关端点，false表示不是
     */
    public static boolean isAuthEndpoint(String requestUri) {
        if (requestUri == null) {
            return false;
        }
        
        return requestUri.contains("/auth/") || 
               requestUri.contains("/login") || 
               requestUri.contains("/logout") || 
               requestUri.contains("/refresh");
    }
}
