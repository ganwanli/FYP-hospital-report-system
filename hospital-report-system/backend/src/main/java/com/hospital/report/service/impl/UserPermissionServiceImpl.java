package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.report.entity.UserPermission;
import com.hospital.report.mapper.UserPermissionMapper;
import com.hospital.report.service.UserPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户个人权限服务实现类
 * 
 * @author System
 * @since 2024-07-28
 */
@Slf4j
@Service
public class UserPermissionServiceImpl extends ServiceImpl<UserPermissionMapper, UserPermission> implements UserPermissionService {

    /**
     * 重置用户个人权限，将角色权限复制到个人权限表
     * 
     * @param userId 用户ID
     * @param operatedBy 操作人ID
     * @return 复制的权限数量
     */
    @Override
    @Transactional
    public int resetUserPermissionsFromRoles(Long userId, Long operatedBy) {
        try {
            log.info("开始重置用户个人权限，用户ID: {}, 操作人ID: {}", userId, operatedBy);
            
            // 1. 清空用户现有的个人权限
            UpdateWrapper<UserPermission> deleteWrapper = new UpdateWrapper<>();
            deleteWrapper.eq("user_id", userId)
                         .eq("deleted", 0)
                         .set("deleted", 1)
                         .set("updated_by", operatedBy)
                         .set("updated_time", LocalDateTime.now());
            
            update(deleteWrapper);
            log.info("清空用户现有个人权限完成，用户ID: {}", userId);
            
            // 2. 获取用户的所有角色权限
            List<Long> rolePermissionIds = baseMapper.findRolePermissionIdsByUserId(userId);
            log.info("获取到用户角色权限数量: {}, 用户ID: {}", rolePermissionIds.size(), userId);
            
            if (rolePermissionIds.isEmpty()) {
                log.info("用户没有角色权限，无需复制，用户ID: {}", userId);
                return 0;
            }
            
            // 3. 将角色权限复制到个人权限表
            int successCount = 0;
            for (Long permissionId : rolePermissionIds) {
                UserPermission userPermission = new UserPermission();
                userPermission.setUserId(userId);
                userPermission.setPermissionId(permissionId);
                userPermission.setPermissionType(UserPermission.PERMISSION_TYPE_GRANT);
                userPermission.setGrantedBy(operatedBy);
                userPermission.setGrantedTime(LocalDateTime.now());
                userPermission.setIsActive(UserPermission.ACTIVE_YES);
                userPermission.setRemarks("从角色权限自动复制");
                
                if (save(userPermission)) {
                    successCount++;
                }
            }
            
            log.info("用户权限重置完成，用户ID: {}, 复制权限数量: {}", userId, successCount);
            return successCount;

        } catch (Exception e) {
            log.error("重置用户权限失败，用户ID: {}", userId, e);
            return 0;
        }
    }

    @Override
    public UserPermissionStats getUserPermissionStats(Long userId) {
        try {
            UserPermissionStats stats = new UserPermissionStats();
            stats.setUserId(userId);

            // 获取用户的个人权限
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserPermission> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("user_id", userId).eq("deleted", 0);

            java.util.List<UserPermission> userPermissions = list(wrapper);

            // 统计个人授权权限数量
            stats.setPersonalGranted((int) userPermissions.stream()
                    .filter(up -> UserPermission.PERMISSION_TYPE_GRANT.equals(up.getPermissionType()))
                    .count());

            // 统计个人拒绝权限数量
            stats.setPersonalDenied((int) userPermissions.stream()
                    .filter(up -> UserPermission.PERMISSION_TYPE_DENY.equals(up.getPermissionType()))
                    .count());

            // 统计过期权限数量
            stats.setExpiredPermissions((int) userPermissions.stream()
                    .filter(up -> up.getExpireTime() != null && up.getExpireTime().isBefore(LocalDateTime.now()))
                    .count());

            // 获取角色权限数量
            java.util.List<Long> rolePermissionIds = baseMapper.findRolePermissionIdsByUserId(userId);
            stats.setRolePermissions(rolePermissionIds.size());

            // 总权限数 = 角色权限数 + 个人授权权限数 - 个人拒绝权限数
            stats.setTotalPermissions(stats.getRolePermissions() + stats.getPersonalGranted() - stats.getPersonalDenied());

            // 设置最后修改时间
            stats.setLastModified(userPermissions.stream()
                    .map(UserPermission::getUpdatedTime)
                    .max(LocalDateTime::compareTo)
                    .orElse(null));

            return stats;
        } catch (Exception e) {
            log.error("获取用户权限统计失败，用户ID: {}", userId, e);
            UserPermissionStats stats = new UserPermissionStats();
            stats.setUserId(userId);
            stats.setTotalPermissions(0);
            stats.setRolePermissions(0);
            stats.setPersonalGranted(0);
            stats.setPersonalDenied(0);
            stats.setExpiredPermissions(0);
            return stats;
        }
    }
}
