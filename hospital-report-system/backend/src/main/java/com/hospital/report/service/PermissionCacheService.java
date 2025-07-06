package com.hospital.report.service;

import java.util.List;
import java.util.Set;

public interface PermissionCacheService {

    void cacheUserPermissions(Long userId, List<String> permissions);

    List<String> getCachedUserPermissions(Long userId);

    void cacheUserRoles(Long userId, List<String> roles);

    List<String> getCachedUserRoles(Long userId);

    void cacheRolePermissions(Long roleId, Set<String> permissions);

    Set<String> getCachedRolePermissions(Long roleId);

    void clearUserCache(Long userId);

    void clearRoleCache(Long roleId);

    void clearAllCache();

    void refreshUserPermissions(Long userId);

    void refreshRolePermissions(Long roleId);

    boolean hasPermissionInCache(Long userId, String permission);

    boolean hasRoleInCache(Long userId, String role);
}