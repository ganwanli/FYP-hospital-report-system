package com.hospital.report.controller;

import com.hospital.report.common.Result;
import com.hospital.report.dto.LoginRequest;
import com.hospital.report.dto.LoginResponse;
import com.hospital.report.service.AuthService;
import com.hospital.report.utils.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证控制器", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest, 
                                      HttpServletRequest request) {
        try {
            String clientIp = IpUtil.getClientIp(request);
            LoginResponse response = authService.login(loginRequest, clientIp);
            
            log.info("用户 {} 登录成功，IP: {}", loginRequest.getUsername(), clientIp);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("用户 {} 登录失败: {}", loginRequest.getUsername(), e.getMessage());
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            authService.logout(token);
            return Result.success();
            
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage());
            return Result.error("登出失败: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用刷新令牌获取新的访问令牌")
    public Result<LoginResponse> refresh(@RequestParam String refreshToken) {
        try {
            LoginResponse response = authService.refreshToken(refreshToken);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("刷新Token失败: {}", e.getMessage());
            return Result.error("刷新Token失败: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<LoginResponse.UserInfo> getCurrentUser() {
        try {
            var user = authService.getCurrentUser();
            if (user == null) {
                return Result.error("用户未登录");
            }

            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setRealName(user.getRealName());
            userInfo.setEmail(user.getEmail());
            userInfo.setPhone(user.getPhone());
            userInfo.setAvatar(user.getAvatar());
            userInfo.setGender(user.getGender());
            userInfo.setPosition(user.getPosition());
            
            return Result.success(userInfo);
            
        } catch (Exception e) {
            log.error("获取当前用户信息失败: {}", e.getMessage());
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/permissions")
    @Operation(summary = "获取用户权限", description = "获取当前用户的所有权限")
    public Result<Object> getUserPermissions() {
        try {
            var user = authService.getCurrentUser();
            if (user == null) {
                return Result.error("用户未登录");
            }

            // 返回用户的权限信息
            return Result.success(user.getPermissions());
            
        } catch (Exception e) {
            log.error("获取用户权限失败: {}", e.getMessage());
            return Result.error("获取权限失败: " + e.getMessage());
        }
    }

    @GetMapping("/check-permission")
    @Operation(summary = "检查权限", description = "检查当前用户是否具有指定权限")
    public Result<Boolean> checkPermission(@RequestParam String permission) {
        try {
            boolean hasPermission = authService.hasPermission(permission);
            return Result.success(hasPermission);
            
        } catch (Exception e) {
            log.error("检查权限失败: {}", e.getMessage());
            return Result.error("检查权限失败: " + e.getMessage());
        }
    }
}