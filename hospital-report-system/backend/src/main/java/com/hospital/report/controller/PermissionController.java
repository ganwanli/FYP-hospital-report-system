package com.hospital.report.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.annotation.RequiresPermission;
import com.hospital.report.common.Result;
import com.hospital.report.dto.PermissionCreateRequest;
import com.hospital.report.dto.PermissionQueryRequest;
import com.hospital.report.dto.PermissionTreeNode;
import com.hospital.report.entity.Permission;
import com.hospital.report.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/system/permission")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限管理相关接口")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/tree")
    @Operation(summary = "获取权限树", description = "获取完整的权限树结构")
    @RequiresPermission("PERMISSION_QUERY")
    public Result<List<PermissionTreeNode>> getPermissionTree() {
        try {
            List<PermissionTreeNode> tree = permissionService.buildPermissionTree();
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取权限树失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/tree/{parentId}")
    @Operation(summary = "获取子权限树", description = "获取指定父节点下的权限树")
    @RequiresPermission("PERMISSION_QUERY")
    public Result<List<PermissionTreeNode>> getPermissionTreeByParent(@PathVariable Long parentId) {
        try {
            List<PermissionTreeNode> tree = permissionService.buildPermissionTree(parentId);
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取子权限树失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/tree")
    @Operation(summary = "获取用户权限树", description = "获取用户拥有的权限树结构")
    @RequiresPermission("PERMISSION_QUERY")
    public Result<List<PermissionTreeNode>> getUserPermissionTree(@PathVariable Long userId) {
        try {
            List<PermissionTreeNode> tree = permissionService.buildUserPermissionTree(userId);
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取用户权限树失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询权限", description = "分页查询权限列表")
    @RequiresPermission("PERMISSION_QUERY")
    public Result<Page<Permission>> getPermissionPage(@ModelAttribute PermissionQueryRequest request) {
        try {
            Page<Permission> page = new Page<>(request.getCurrent(), request.getSize());
            QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
            
            if (request.getPermissionName() != null && !request.getPermissionName().isEmpty()) {
                queryWrapper.like("permission_name", request.getPermissionName());
            }
            if (request.getPermissionCode() != null && !request.getPermissionCode().isEmpty()) {
                queryWrapper.like("permission_code", request.getPermissionCode());
            }
            if (request.getPermissionType() != null && !request.getPermissionType().isEmpty()) {
                queryWrapper.eq("permission_type", request.getPermissionType());
            }
            if (request.getParentId() != null) {
                queryWrapper.eq("parent_id", request.getParentId());
            }
            if (request.getStatus() != null) {
                queryWrapper.eq("status", request.getStatus());
            }
            
            queryWrapper.orderByAsc("sort_order").orderByDesc("created_time");
            Page<Permission> result = permissionService.page(page, queryWrapper);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询权限失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "查询权限列表", description = "查询所有权限列表")
    @RequiresPermission("PERMISSION_QUERY")
    public Result<List<Permission>> getPermissionList() {
        try {
            List<Permission> permissions = permissionService.findAllPermissions();
            return Result.success(permissions);
        } catch (Exception e) {
            log.error("查询权限列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/menu")
    @Operation(summary = "获取菜单权限", description = "获取所有菜单类型的权限")
    @RequiresPermission("PERMISSION_QUERY")
    public Result<List<Permission>> getMenuPermissions() {
        try {
            List<Permission> permissions = permissionService.getMenuPermissions();
            return Result.success(permissions);
        } catch (Exception e) {
            log.error("获取菜单权限失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/button/{parentId}")
    @Operation(summary = "获取按钮权限", description = "获取指定菜单下的按钮权限")
    @RequiresPermission("PERMISSION_QUERY")
    public Result<List<Permission>> getButtonPermissions(@PathVariable Long parentId) {
        try {
            List<Permission> permissions = permissionService.getButtonPermissions(parentId);
            return Result.success(permissions);
        } catch (Exception e) {
            log.error("获取按钮权限失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询权限", description = "根据权限ID查询权限详情")
    @RequiresPermission("PERMISSION_QUERY")
    public Result<Permission> getPermissionById(@PathVariable Long id) {
        try {
            Permission permission = permissionService.getById(id);
            if (permission == null) {
                return Result.error("权限不存在");
            }
            return Result.success(permission);
        } catch (Exception e) {
            log.error("根据ID查询权限失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "新增权限", description = "新增权限")
    @RequiresPermission("PERMISSION_CREATE")
    public Result<Void> createPermission(@Valid @RequestBody PermissionCreateRequest request) {
        try {
            Permission permission = new Permission();
            permission.setParentId(request.getParentId());
            permission.setPermissionName(request.getPermissionName());
            permission.setPermissionCode(request.getPermissionCode());
            permission.setPermissionType(request.getPermissionType());
            permission.setMenuUrl(request.getMenuUrl());
            permission.setMenuIcon(request.getMenuIcon());
            permission.setComponent(request.getComponent());
            permission.setRedirect(request.getRedirect());
            permission.setSortOrder(request.getSortOrder());
            permission.setIsVisible(request.getIsVisible());
            permission.setIsExternal(request.getIsExternal());
            permission.setIsCache(request.getIsCache());
            permission.setRemarks(request.getRemarks());
            
            boolean success = permissionService.createPermission(permission);
            if (success) {
                return Result.success();
            } else {
                return Result.error("新增权限失败");
            }
        } catch (Exception e) {
            log.error("新增权限失败", e);
            return Result.error("新增失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新权限", description = "更新权限信息")
    @RequiresPermission("PERMISSION_UPDATE")
    public Result<Void> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionCreateRequest request) {
        try {
            Permission permission = permissionService.getById(id);
            if (permission == null) {
                return Result.error("权限不存在");
            }
            
            permission.setId(id);
            permission.setParentId(request.getParentId());
            permission.setPermissionName(request.getPermissionName());
            permission.setPermissionCode(request.getPermissionCode());
            permission.setPermissionType(request.getPermissionType());
            permission.setMenuUrl(request.getMenuUrl());
            permission.setMenuIcon(request.getMenuIcon());
            permission.setComponent(request.getComponent());
            permission.setRedirect(request.getRedirect());
            permission.setSortOrder(request.getSortOrder());
            permission.setIsVisible(request.getIsVisible());
            permission.setIsExternal(request.getIsExternal());
            permission.setIsCache(request.getIsCache());
            permission.setRemarks(request.getRemarks());
            
            boolean success = permissionService.updatePermission(permission);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新权限失败");
            }
        } catch (Exception e) {
            log.error("更新权限失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限", description = "删除权限")
    @RequiresPermission("PERMISSION_DELETE")
    public Result<Void> deletePermission(@PathVariable Long id) {
        try {
            Permission permission = permissionService.getById(id);
            if (permission == null) {
                return Result.error("权限不存在");
            }
            
            boolean success = permissionService.deletePermission(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除权限失败");
            }
        } catch (Exception e) {
            log.error("删除权限失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新权限状态", description = "启用或禁用权限")
    @RequiresPermission("PERMISSION_UPDATE")
    public Result<Void> updatePermissionStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            Permission permission = new Permission();
            permission.setId(id);
            permission.setStatus(status);
            
            boolean success = permissionService.updatePermission(permission);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新权限状态失败");
            }
        } catch (Exception e) {
            log.error("更新权限状态失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }
}