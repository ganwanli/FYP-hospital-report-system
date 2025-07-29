package com.hospital.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.report.entity.UserPermission;

import java.time.LocalDateTime;

/**
 * 用户个人权限服务接口
 *
 * @author System
 * @since 2024-07-28
 */
public interface UserPermissionService extends IService<UserPermission> {

    /**
     * 重置用户个人权限，将角色权限复制到个人权限表
     *
     * @param userId 用户ID
     * @param operatedBy 操作人ID
     * @return 复制的权限数量
     */
    int resetUserPermissionsFromRoles(Long userId, Long operatedBy);

    /**
     * 获取用户权限统计信息
     *
     * @param userId 用户ID
     * @return 权限统计
     */
    UserPermissionStats getUserPermissionStats(Long userId);

    /**
     * 用户权限统计信息
     */
    class UserPermissionStats {
        private Long userId;
        private Integer totalPermissions;      // 总权限数
        private Integer rolePermissions;       // 角色权限数
        private Integer personalGranted;       // 个人授权数
        private Integer personalDenied;        // 个人拒绝数
        private Integer expiredPermissions;    // 过期权限数
        private LocalDateTime lastModified;    // 最后修改时间

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Integer getTotalPermissions() { return totalPermissions; }
        public void setTotalPermissions(Integer totalPermissions) { this.totalPermissions = totalPermissions; }

        public Integer getRolePermissions() { return rolePermissions; }
        public void setRolePermissions(Integer rolePermissions) { this.rolePermissions = rolePermissions; }

        public Integer getPersonalGranted() { return personalGranted; }
        public void setPersonalGranted(Integer personalGranted) { this.personalGranted = personalGranted; }

        public Integer getPersonalDenied() { return personalDenied; }
        public void setPersonalDenied(Integer personalDenied) { this.personalDenied = personalDenied; }

        public Integer getExpiredPermissions() { return expiredPermissions; }
        public void setExpiredPermissions(Integer expiredPermissions) { this.expiredPermissions = expiredPermissions; }

        public LocalDateTime getLastModified() { return lastModified; }
        public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    }
}
