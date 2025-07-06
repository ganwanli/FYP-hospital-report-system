package com.hospital.report.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.annotation.RequiresPermission;
import com.hospital.report.common.Result;
import com.hospital.report.dto.RoleCreateRequest;
import com.hospital.report.dto.RolePermissionRequest;
import com.hospital.report.dto.RoleQueryRequest;
import com.hospital.report.dto.UserRoleRequest;
import com.hospital.report.entity.Role;
import com.hospital.report.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色管理相关接口")
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/page")
    @Operation(summary = "分页查询角色", description = "分页查询角色列表")
    @RequiresPermission("ROLE_QUERY")
    public Result<Page<Role>> getRolePage(@ModelAttribute RoleQueryRequest request) {
        try {
            Page<Role> page = new Page<>(request.getCurrent(), request.getSize());
            QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
            
            if (request.getRoleName() != null && !request.getRoleName().isEmpty()) {
                queryWrapper.like("role_name", request.getRoleName());
            }
            if (request.getRoleCode() != null && !request.getRoleCode().isEmpty()) {
                queryWrapper.like("role_code", request.getRoleCode());
            }
            if (request.getStatus() != null) {
                queryWrapper.eq("status", request.getStatus());
            }
            
            queryWrapper.orderByAsc("sort_order").orderByDesc("created_time");
            Page<Role> result = roleService.page(page, queryWrapper);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询角色失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "查询角色列表", description = "查询所有角色列表")
    @RequiresPermission("ROLE_QUERY")
    public Result<List<Role>> getRoleList() {
        try {
            List<Role> roles = roleService.findAllRoles();
            return Result.success(roles);
        } catch (Exception e) {
            log.error("查询角色列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询角色", description = "根据角色ID查询角色详情")
    @RequiresPermission("ROLE_QUERY")
    public Result<Role> getRoleById(@PathVariable Long id) {
        try {
            Role role = roleService.getById(id);
            if (role == null) {
                return Result.error("角色不存在");
            }
            return Result.success(role);
        } catch (Exception e) {
            log.error("根据ID查询角色失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "查询角色权限", description = "查询角色拥有的权限ID列表")
    @RequiresPermission("ROLE_QUERY")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        try {
            List<Long> permissionIds = roleService.findPermissionIdsByRoleId(id);
            return Result.success(permissionIds);
        } catch (Exception e) {
            log.error("查询角色权限失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户角色", description = "查询用户拥有的角色列表")
    @RequiresPermission("ROLE_QUERY")
    public Result<List<Role>> getUserRoles(@PathVariable Long userId) {
        try {
            List<Role> roles = roleService.findRolesByUserId(userId);
            return Result.success(roles);
        } catch (Exception e) {
            log.error("查询用户角色失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "新增角色", description = "新增角色")
    @RequiresPermission("ROLE_CREATE")
    public Result<Void> createRole(@Valid @RequestBody RoleCreateRequest request) {
        try {
            Role role = new Role();
            role.setRoleName(request.getRoleName());
            role.setRoleCode(request.getRoleCode());
            role.setDescription(request.getDescription());
            role.setDataScope(request.getDataScope());
            role.setSortOrder(request.getSortOrder());
            role.setRemarks(request.getRemarks());
            
            boolean success = roleService.createRole(role);
            if (success) {
                return Result.success();
            } else {
                return Result.error("新增角色失败");
            }
        } catch (Exception e) {
            log.error("新增角色失败", e);
            return Result.error("新增失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色", description = "更新角色信息")
    @RequiresPermission("ROLE_UPDATE")
    public Result<Void> updateRole(@PathVariable Long id, @Valid @RequestBody RoleCreateRequest request) {
        try {
            Role role = roleService.getById(id);
            if (role == null) {
                return Result.error("角色不存在");
            }
            
            role.setId(id);
            role.setRoleName(request.getRoleName());
            role.setRoleCode(request.getRoleCode());
            role.setDescription(request.getDescription());
            role.setDataScope(request.getDataScope());
            role.setSortOrder(request.getSortOrder());
            role.setRemarks(request.getRemarks());
            
            boolean success = roleService.updateRole(role);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新角色失败");
            }
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "删除角色")
    @RequiresPermission("ROLE_DELETE")
    public Result<Void> deleteRole(@PathVariable Long id) {
        try {
            Role role = roleService.getById(id);
            if (role == null) {
                return Result.error("角色不存在");
            }
            
            boolean success = roleService.deleteRole(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除角色失败");
            }
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新角色状态", description = "启用或禁用角色")
    @RequiresPermission("ROLE_UPDATE")
    public Result<Void> updateRoleStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            Role role = new Role();
            role.setId(id);
            role.setStatus(status);
            
            boolean success = roleService.updateRole(role);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新角色状态失败");
            }
        } catch (Exception e) {
            log.error("更新角色状态失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/permissions")
    @Operation(summary = "分配权限", description = "为角色分配权限")
    @RequiresPermission("ROLE_ASSIGN_PERMISSION")
    public Result<Void> assignPermissions(@PathVariable Long id, @Valid @RequestBody RolePermissionRequest request) {
        try {
            boolean success = roleService.assignPermissionsToRole(id, request.getPermissionIds());
            if (success) {
                return Result.success();
            } else {
                return Result.error("分配权限失败");
            }
        } catch (Exception e) {
            log.error("分配权限失败", e);
            return Result.error("分配失败: " + e.getMessage());
        }
    }

    @PostMapping("/user/{userId}/roles")
    @Operation(summary = "分配角色", description = "为用户分配角色")
    @RequiresPermission("ROLE_ASSIGN_USER")
    public Result<Void> assignRoles(@PathVariable Long userId, @Valid @RequestBody UserRoleRequest request) {
        try {
            boolean success = roleService.assignRolesToUser(userId, request.getRoleIds());
            if (success) {
                return Result.success();
            } else {
                return Result.error("分配角色失败");
            }
        } catch (Exception e) {
            log.error("分配角色失败", e);
            return Result.error("分配失败: " + e.getMessage());
        }
    }
}