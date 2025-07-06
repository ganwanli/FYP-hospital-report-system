package com.hospital.report.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.annotation.RequiresPermission;
import com.hospital.report.common.Result;
import com.hospital.report.dto.UserCreateRequest;
import com.hospital.report.dto.UserQueryRequest;
import com.hospital.report.dto.UserUpdateRequest;
import com.hospital.report.entity.User;
import com.hospital.report.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {

    private final UserService userService;

    @GetMapping("/page")
    @Operation(summary = "分页查询用户", description = "分页查询用户列表")
    @RequiresPermission("USER_QUERY")
    public Result<Page<User>> getUserPage(@ModelAttribute UserQueryRequest request) {
        try {
            Page<User> page = new Page<>(request.getCurrent(), request.getSize());
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            
            if (request.getUsername() != null && !request.getUsername().isEmpty()) {
                queryWrapper.like("username", request.getUsername());
            }
            if (request.getRealName() != null && !request.getRealName().isEmpty()) {
                queryWrapper.like("real_name", request.getRealName());
            }
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                queryWrapper.like("email", request.getEmail());
            }
            if (request.getStatus() != null) {
                queryWrapper.eq("status", request.getStatus());
            }
            if (request.getDepartmentId() != null) {
                queryWrapper.eq("department_id", request.getDepartmentId());
            }
            
            queryWrapper.orderByDesc("created_time");
            Page<User> result = userService.page(page, queryWrapper);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询用户失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "查询用户列表", description = "查询所有用户列表")
    @RequiresPermission("USER_QUERY")
    public Result<List<User>> getUserList() {
        try {
            List<User> users = userService.list();
            return Result.success(users);
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户", description = "根据用户ID查询用户详情")
    @RequiresPermission("USER_QUERY")
    public Result<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            log.error("根据ID查询用户失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "新增用户", description = "新增用户")
    @RequiresPermission("USER_CREATE")
    public Result<Void> createUser(@Valid @RequestBody UserCreateRequest request) {
        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setGender(request.getGender());
            user.setDepartmentId(request.getDepartmentId());
            user.setPosition(request.getPosition());
            user.setEmployeeId(request.getEmployeeId());
            user.setRemarks(request.getRemarks());
            
            boolean success = userService.createUser(user);
            if (success) {
                return Result.success();
            } else {
                return Result.error("新增用户失败");
            }
        } catch (Exception e) {
            log.error("新增用户失败", e);
            return Result.error("新增失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    @RequiresPermission("USER_UPDATE")
    public Result<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            user.setId(id);
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setGender(request.getGender());
            user.setDepartmentId(request.getDepartmentId());
            user.setPosition(request.getPosition());
            user.setEmployeeId(request.getEmployeeId());
            user.setRemarks(request.getRemarks());
            
            boolean success = userService.updateUser(user);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新用户失败");
            }
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "删除用户")
    @RequiresPermission("USER_DELETE")
    public Result<Void> deleteUser(@PathVariable Long id) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            boolean success = userService.deleteUser(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除用户失败");
            }
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @RequiresPermission("USER_UPDATE")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            User user = new User();
            user.setId(id);
            user.setStatus(status);
            
            boolean success = userService.updateUser(user);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新用户状态失败");
            }
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "重置用户密码", description = "重置用户密码")
    @RequiresPermission("USER_RESET_PASSWORD")
    public Result<Void> resetUserPassword(@PathVariable Long id) {
        try {
            String defaultPassword = "HospitalReport@123";
            boolean success = userService.updatePassword(id, defaultPassword);
            if (success) {
                return Result.success();
            } else {
                return Result.error("重置密码失败");
            }
        } catch (Exception e) {
            log.error("重置用户密码失败", e);
            return Result.error("重置失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "解锁用户", description = "解锁被锁定的用户")
    @RequiresPermission("USER_UNLOCK")
    public Result<Void> unlockUser(@PathVariable Long id) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            userService.unlockUser(user.getUsername());
            return Result.success();
        } catch (Exception e) {
            log.error("解锁用户失败", e);
            return Result.error("解锁失败: " + e.getMessage());
        }
    }
}