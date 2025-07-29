package com.hospital.report.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.report.common.Result;
import com.hospital.report.entity.Permission;
import com.hospital.report.entity.UserPermission;
import com.hospital.report.mapper.UserPermissionMapper;
import com.hospital.report.service.PermissionService;
import com.hospital.report.service.UserPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户个人权限管理控制器
 * 
 * @author System
 * @since 2024-07-29
 */
@Slf4j
@RestController
@RequestMapping("/system/user-permission")
@RequiredArgsConstructor
@Tag(name = "用户个人权限管理", description = "用户个人权限管理相关接口")
public class UserPermissionController {

    private final UserPermissionService userPermissionService;
    private final PermissionService permissionService;

    @GetMapping("/personal/{userId}")
    @Operation(summary = "获取用户个人权限", description = "获取用户的个人权限列表")
    public Result<List<UserPermission>> getUserPersonalPermissions(@PathVariable Long userId) {
        try {
            List<UserPermission> permissions = userPermissionService.list(
                userPermissionService.lambdaQuery()
                    .eq(UserPermission::getUserId, userId)
                    .eq(UserPermission::getDeleted, 0)
                    .getWrapper()
            );
            return Result.success(permissions);
        } catch (Exception e) {
            log.error("获取用户个人权限失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/effective/{userId}")
    @Operation(summary = "获取用户有效权限", description = "获取用户的有效权限列表（角色权限+个人权限-拒绝权限）")
    public Result<List<Permission>> getUserEffectivePermissions(@PathVariable Long userId) {
        try {
            // 获取有效权限ID列表
            UserPermissionMapper mapper = (UserPermissionMapper) userPermissionService.getBaseMapper();
            List<Long> permissionIds = mapper.findEffectivePermissionIdsByUserId(userId);

            if (permissionIds.isEmpty()) {
                return Result.success(List.of());
            }

            // 获取权限详情
            List<Permission> permissions = permissionService.listByIds(permissionIds);
            return Result.success(permissions);
        } catch (Exception e) {
            log.error("获取用户有效权限失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @PostMapping("/save/{userId}")
    @Operation(summary = "保存用户个人权限", description = "保存用户的个人权限设置")
    public Result<String> saveUserPersonalPermissions(@PathVariable Long userId, @RequestBody SaveUserPermissionsRequest request) {
        try {
            // 先清除用户的所有个人权限
            userPermissionService.remove(
                new QueryWrapper<UserPermission>().eq("user_id", userId)
            );

            // 保存新的个人权限
            if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
                List<UserPermission> userPermissions = request.getPermissionIds().stream()
                    .map(permissionId -> {
                        UserPermission up = new UserPermission();
                        up.setUserId(userId);
                        up.setPermissionId(permissionId);
                        up.setPermissionType("GRANT");
                        up.setCreatedBy(1L); // 可以从请求中获取
                        up.setCreatedTime(LocalDateTime.now());
                        return up;
                    })
                    .collect(Collectors.toList());

                userPermissionService.saveBatch(userPermissions);
            }

            return Result.success("保存成功");
        } catch (Exception e) {
            log.error("保存用户个人权限失败", e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    @PostMapping("/reset/{userId}")
    @Operation(summary = "重置用户权限", description = "重置用户权限，将角色权限复制到个人权限表")
    public Result<Integer> resetUserPermissions(@PathVariable Long userId, @RequestParam(required = false) Long operatedBy) {
        try {
            if (operatedBy == null) {
                operatedBy = 1L; // 默认操作人
            }
            
            int copiedCount = userPermissionService.resetUserPermissionsFromRoles(userId, operatedBy);
            return Result.success(copiedCount);
        } catch (Exception e) {
            log.error("重置用户权限失败", e);
            return Result.error("重置失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats/{userId}")
    @Operation(summary = "获取用户权限统计", description = "获取用户权限统计信息")
    public Result<UserPermissionService.UserPermissionStats> getUserPermissionStats(@PathVariable Long userId) {
        try {
            UserPermissionService.UserPermissionStats stats = userPermissionService.getUserPermissionStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户权限统计失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
}

/**
 * 保存用户权限请求类
 */
@Data
class SaveUserPermissionsRequest {
    private List<Long> permissionIds;
}
