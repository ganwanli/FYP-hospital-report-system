package com.hospital.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.report.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {

    List<Role> findAllRoles();

    List<Role> findRolesByUserId(Long userId);

    List<Long> findPermissionIdsByRoleId(Long roleId);

    boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    boolean assignRolesToUser(Long userId, List<Long> roleIds);

    boolean assignRoleToUser(Long userId, Long roleId, Long operatedBy);

    boolean removeRoleFromUser(Long userId, Long roleId);

    boolean createRole(Role role);

    boolean updateRole(Role role);

    boolean deleteRole(Long roleId);

    List<Long> getUserIdsByRoleId(Long roleId);

    List<com.hospital.report.entity.User> getUsersByRoleId(Long roleId);
}