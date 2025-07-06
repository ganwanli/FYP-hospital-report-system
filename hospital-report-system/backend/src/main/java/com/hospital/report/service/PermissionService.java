package com.hospital.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.report.dto.PermissionTreeNode;
import com.hospital.report.entity.Permission;

import java.util.List;

public interface PermissionService extends IService<Permission> {

    List<Permission> findAllPermissions();

    List<Permission> findPermissionsByParentId(Long parentId);

    List<Permission> findPermissionsByUserId(Long userId);

    List<PermissionTreeNode> buildPermissionTree();

    List<PermissionTreeNode> buildPermissionTree(Long parentId);

    List<PermissionTreeNode> buildUserPermissionTree(Long userId);

    boolean createPermission(Permission permission);

    boolean updatePermission(Permission permission);

    boolean deletePermission(Long permissionId);

    boolean hasChildren(Long permissionId);

    List<Permission> getMenuPermissions();

    List<Permission> getButtonPermissions(Long parentId);
}