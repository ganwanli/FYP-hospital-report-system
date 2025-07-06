package com.hospital.report.service.impl;

import com.hospital.report.service.PermissionCacheService;
import com.hospital.report.service.UserService;
import com.hospital.report.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCacheServiceImpl implements PermissionCacheService {

    private final RedisUtil redisUtil;
    private final UserService userService;

    private static final String USER_PERMISSIONS_KEY = "user:permissions:";
    private static final String USER_ROLES_KEY = "user:roles:";
    private static final String ROLE_PERMISSIONS_KEY = "role:permissions:";
    private static final long CACHE_EXPIRE_TIME = 2 * 60 * 60; // 2小时

    @Override
    public void cacheUserPermissions(Long userId, List<String> permissions) {
        try {
            String key = USER_PERMISSIONS_KEY + userId;
            redisUtil.set(key, permissions, CACHE_EXPIRE_TIME);
            log.debug("缓存用户权限: userId={}, permissions={}", userId, permissions);
        } catch (Exception e) {
            log.error("缓存用户权限失败", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getCachedUserPermissions(Long userId) {
        try {
            String key = USER_PERMISSIONS_KEY + userId;
            Object cached = redisUtil.get(key);
            return cached != null ? (List<String>) cached : null;
        } catch (Exception e) {
            log.error("获取缓存用户权限失败", e);
            return null;
        }
    }

    @Override
    public void cacheUserRoles(Long userId, List<String> roles) {
        try {
            String key = USER_ROLES_KEY + userId;
            redisUtil.set(key, roles, CACHE_EXPIRE_TIME);
            log.debug("缓存用户角色: userId={}, roles={}", userId, roles);
        } catch (Exception e) {
            log.error("缓存用户角色失败", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getCachedUserRoles(Long userId) {
        try {
            String key = USER_ROLES_KEY + userId;
            Object cached = redisUtil.get(key);
            return cached != null ? (List<String>) cached : null;
        } catch (Exception e) {
            log.error("获取缓存用户角色失败", e);
            return null;
        }
    }

    @Override
    public void cacheRolePermissions(Long roleId, Set<String> permissions) {
        try {
            String key = ROLE_PERMISSIONS_KEY + roleId;
            redisUtil.set(key, permissions, CACHE_EXPIRE_TIME);
            log.debug("缓存角色权限: roleId={}, permissions={}", roleId, permissions);
        } catch (Exception e) {
            log.error("缓存角色权限失败", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getCachedRolePermissions(Long roleId) {
        try {
            String key = ROLE_PERMISSIONS_KEY + roleId;
            Object cached = redisUtil.get(key);
            return cached != null ? (Set<String>) cached : null;
        } catch (Exception e) {
            log.error("获取缓存角色权限失败", e);
            return null;
        }
    }

    @Override
    public void clearUserCache(Long userId) {
        try {
            redisUtil.delete(USER_PERMISSIONS_KEY + userId);
            redisUtil.delete(USER_ROLES_KEY + userId);
            log.debug("清除用户缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("清除用户缓存失败", e);
        }
    }

    @Override
    public void clearRoleCache(Long roleId) {
        try {
            redisUtil.delete(ROLE_PERMISSIONS_KEY + roleId);
            log.debug("清除角色缓存: roleId={}", roleId);
        } catch (Exception e) {
            log.error("清除角色缓存失败", e);
        }
    }

    @Override
    public void clearAllCache() {
        try {
            redisUtil.deleteByPattern(USER_PERMISSIONS_KEY + "*");
            redisUtil.deleteByPattern(USER_ROLES_KEY + "*");
            redisUtil.deleteByPattern(ROLE_PERMISSIONS_KEY + "*");
            log.info("清除所有权限缓存");
        } catch (Exception e) {
            log.error("清除所有缓存失败", e);
        }
    }

    @Override
    public void refreshUserPermissions(Long userId) {
        try {
            // 清除旧缓存
            clearUserCache(userId);
            
            // 重新加载权限
            List<String> permissions = userService.findPermissionsByUserId(userId);
            List<String> roles = userService.findRolesByUserId(userId);
            
            // 重新缓存
            if (permissions != null && !permissions.isEmpty()) {
                cacheUserPermissions(userId, permissions);
            }
            if (roles != null && !roles.isEmpty()) {
                cacheUserRoles(userId, roles);
            }
            
            log.debug("刷新用户权限缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("刷新用户权限缓存失败", e);
        }
    }

    @Override
    public void refreshRolePermissions(Long roleId) {
        try {
            // 清除旧缓存
            clearRoleCache(roleId);
            
            // TODO: 根据需要实现角色权限的重新加载
            log.debug("刷新角色权限缓存: roleId={}", roleId);
        } catch (Exception e) {
            log.error("刷新角色权限缓存失败", e);
        }
    }

    @Override
    public boolean hasPermissionInCache(Long userId, String permission) {
        List<String> cachedPermissions = getCachedUserPermissions(userId);
        return cachedPermissions != null && cachedPermissions.contains(permission);
    }

    @Override
    public boolean hasRoleInCache(Long userId, String role) {
        List<String> cachedRoles = getCachedUserRoles(userId);
        return cachedRoles != null && cachedRoles.contains(role);
    }
}