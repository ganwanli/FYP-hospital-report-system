package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.report.entity.Role;
import com.hospital.report.mapper.RoleMapper;
import com.hospital.report.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Role> findAllRoles() {
        return roleMapper.findAllRoles();
    }

    @Override
    public List<Role> findRolesByUserId(Long userId) {
        return roleMapper.findRolesByUserId(userId);
    }

    @Override
    public List<Long> findPermissionIdsByRoleId(Long roleId) {
        return roleMapper.findPermissionIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        try {
            // 先删除角色的所有权限关联
            jdbcTemplate.update("DELETE FROM sys_role_permission WHERE role_id = ?", roleId);
            
            // 批量插入新的权限关联
            if (permissionIds != null && !permissionIds.isEmpty()) {
                String sql = "INSERT INTO sys_role_permission (role_id, permission_id, created_time) VALUES (?, ?, ?)";
                
                for (Long permissionId : permissionIds) {
                    jdbcTemplate.update(sql, roleId, permissionId, LocalDateTime.now());
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("分配权限给角色失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean assignRolesToUser(Long userId, List<Long> roleIds) {
        try {
            // 先删除用户的所有角色关联
            jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", userId);
            
            // 批量插入新的角色关联
            if (roleIds != null && !roleIds.isEmpty()) {
                String sql = "INSERT INTO sys_user_role (user_id, role_id, created_time) VALUES (?, ?, ?)";
                
                for (Long roleId : roleIds) {
                    jdbcTemplate.update(sql, userId, roleId, LocalDateTime.now());
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("分配角色给用户失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean removeRoleFromUser(Long userId, Long roleId) {
        try {
            int result = jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ? AND role_id = ?", userId, roleId);
            return result > 0;
        } catch (Exception e) {
            log.error("移除用户角色失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean createRole(Role role) {
        try {
            role.setCreatedTime(LocalDateTime.now());
            role.setUpdatedTime(LocalDateTime.now());
            role.setStatus(1);
            role.setIsDeleted(false);
            return save(role);
        } catch (Exception e) {
            log.error("创建角色失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateRole(Role role) {
        try {
            role.setUpdatedTime(LocalDateTime.now());
            return updateById(role);
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteRole(Long roleId) {
        try {
            // 检查是否有用户关联此角色
            List<Long> userIds = getUserIdsByRoleId(roleId);
            if (!userIds.isEmpty()) {
                throw new RuntimeException("该角色下还有用户，不能删除");
            }
            
            // 逻辑删除角色
            Role role = new Role();
            role.setId(roleId);
            role.setIsDeleted(true);
            role.setUpdatedTime(LocalDateTime.now());
            
            // 删除角色权限关联
            jdbcTemplate.update("DELETE FROM sys_role_permission WHERE role_id = ?", roleId);
            
            return updateById(role);
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return false;
        }
    }

    @Override
    public List<Long> getUserIdsByRoleId(Long roleId) {
        String sql = "SELECT user_id FROM sys_user_role WHERE role_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, roleId);
    }
}