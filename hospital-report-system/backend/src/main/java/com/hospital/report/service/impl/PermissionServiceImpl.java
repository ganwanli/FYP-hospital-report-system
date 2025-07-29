package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.report.dto.PermissionTreeNode;
import com.hospital.report.entity.Permission;
import com.hospital.report.mapper.PermissionMapper;
import com.hospital.report.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    public List<Permission> findAllPermissions() {
        return permissionMapper.findAllPermissions();
    }

    @Override
    public List<Permission> findPermissionsByParentId(Long parentId) {
        return permissionMapper.findPermissionsByParentId(parentId);
    }

    @Override
    public List<Permission> findPermissionsByUserId(Long userId) {
        return permissionMapper.findPermissionsByUserId(userId);
    }

    @Override
    public List<PermissionTreeNode> buildPermissionTree() {
        return buildPermissionTree(0L);
    }

    @Override
    public List<PermissionTreeNode> buildPermissionTree(Long parentId) {
        List<Permission> allPermissions = findAllPermissions();
        return buildTree(allPermissions, parentId);
    }

    @Override
    public List<PermissionTreeNode> buildUserPermissionTree(Long userId) {
        List<Permission> userPermissions = findPermissionsByUserId(userId);
        return buildTree(userPermissions, 0L);
    }

    private List<PermissionTreeNode> buildTree(List<Permission> permissions, Long parentId) {
        List<PermissionTreeNode> result = new ArrayList<>();
        
        for (Permission permission : permissions) {
            if (permission.getParentId().equals(parentId)) {
                PermissionTreeNode node = convertToTreeNode(permission);
                
                // 递归查找子节点
                List<PermissionTreeNode> children = buildTree(permissions, permission.getId());
                node.setChildren(children);
                node.setHasChildren(!children.isEmpty());
                
                result.add(node);
            }
        }
        
        return result.stream()
                .sorted((a, b) -> {
                    int sortA = a.getSortOrder() != null ? a.getSortOrder() : 0;
                    int sortB = b.getSortOrder() != null ? b.getSortOrder() : 0;
                    return Integer.compare(sortA, sortB);
                })
                .collect(Collectors.toList());
    }

    private PermissionTreeNode convertToTreeNode(Permission permission) {
        PermissionTreeNode node = new PermissionTreeNode();
        BeanUtils.copyProperties(permission, node);
        return node;
    }

    @Override
    @Transactional
    public boolean createPermission(Permission permission) {
        try {
            permission.setCreatedTime(LocalDateTime.now());
            permission.setUpdatedTime(LocalDateTime.now());
            permission.setStatus(1);
            permission.setDeleted(0);
            
            if (permission.getParentId() == null) {
                permission.setParentId(0L);
            }
            
            return save(permission);
        } catch (Exception e) {
            log.error("创建权限失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updatePermission(Permission permission) {
        try {
            permission.setUpdatedTime(LocalDateTime.now());
            return updateById(permission);
        } catch (Exception e) {
            log.error("更新权限失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deletePermission(Long permissionId) {
        try {
            // 检查是否有子权限
            if (hasChildren(permissionId)) {
                throw new RuntimeException("该权限下还有子权限，不能删除");
            }
            
            // 逻辑删除权限
            Permission permission = new Permission();
            permission.setId(permissionId);
            permission.setDeleted(1);
            permission.setUpdatedTime(LocalDateTime.now());
            
            return updateById(permission);
        } catch (Exception e) {
            log.error("删除权限失败", e);
            return false;
        }
    }

    @Override
    public boolean hasChildren(Long permissionId) {
        int count = permissionMapper.countChildrenByParentId(permissionId);
        return count > 0;
    }

    @Override
    public List<Permission> getMenuPermissions() {
        List<Permission> allPermissions = findAllPermissions();
        return allPermissions.stream()
                .filter(p -> "MENU".equals(p.getPermissionType()) || "DIRECTORY".equals(p.getPermissionType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> getButtonPermissions(Long parentId) {
        List<Permission> allPermissions = findAllPermissions();
        return allPermissions.stream()
                .filter(p -> "BUTTON".equals(p.getPermissionType()) && p.getParentId().equals(parentId))
                .collect(Collectors.toList());
    }
}