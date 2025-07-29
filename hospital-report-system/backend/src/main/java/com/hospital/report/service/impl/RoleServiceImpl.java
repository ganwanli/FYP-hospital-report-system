package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.report.entity.Role;
import com.hospital.report.mapper.RoleMapper;
import com.hospital.report.service.RoleService;
import com.hospital.report.service.UserPermissionService;
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
    private final UserPermissionService userPermissionService;

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

            // 角色分配成功后，重置用户权限（将新的角色权限复制到个人权限表）
            try {
                int copiedPermissions = userPermissionService.resetUserPermissionsFromRoles(userId, 1L);
                log.info("角色分配成功，已重置用户权限，复制{}个权限到个人权限表，用户ID: {}", copiedPermissions, userId);
            } catch (Exception e) {
                log.error("重置用户权限失败，用户ID: {}", userId, e);
                // 不影响角色分配的成功，只记录日志
            }

            return true;
        } catch (Exception e) {
            log.error("分配角色给用户失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean assignRoleToUser(Long userId, Long roleId, Long operatedBy) {
        try {
            log.info("开始分配角色给用户，用户ID: {}, 角色ID: {}, 操作人: {}", userId, roleId, operatedBy);

            String sql = "INSERT INTO sys_user_role (user_id, role_id, created_by, created_time) VALUES (?, ?, ?, ?)";
            int result = jdbcTemplate.update(sql, userId, roleId, operatedBy, LocalDateTime.now());

            if (result > 0) {
                log.info("角色分配成功，用户ID: {}, 角色ID: {}", userId, roleId);
                return true;
            } else {
                log.error("角色分配失败，用户ID: {}, 角色ID: {}", userId, roleId);
                return false;
            }
        } catch (Exception e) {
            log.error("分配角色给用户失败，用户ID: {}, 角色ID: {}", userId, roleId, e);
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
            role.setDeleted(false);
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
            role.setDeleted(true);
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

    @Override
    public List<com.hospital.report.entity.User> getUsersByRoleId(Long roleId) {
        String sql = "SELECT u.* FROM sys_user u " +
                    "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
                    "WHERE ur.role_id = ? AND u.deleted = 0 " +
                    "ORDER BY u.created_time DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            com.hospital.report.entity.User user = new com.hospital.report.entity.User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setRealName(rs.getString("real_name"));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            user.setGender(rs.getInt("gender"));
            user.setDepartmentId(rs.getLong("department_id"));
            user.setPosition(rs.getString("position"));
            user.setEmployeeId(rs.getString("employee_id"));
            user.setStatus(rs.getInt("status"));
            user.setCreatedTime(rs.getTimestamp("created_time") != null ?
                rs.getTimestamp("created_time").toLocalDateTime() : null);
            user.setUpdatedTime(rs.getTimestamp("updated_time") != null ?
                rs.getTimestamp("updated_time").toLocalDateTime() : null);
            return user;
        }, roleId);
    }
}