package com.hospital.report.service.impl;

import com.hospital.report.dto.LoginRequest;
import com.hospital.report.dto.LoginResponse;
import com.hospital.report.entity.User;
import com.hospital.report.service.AuthService;
import com.hospital.report.service.UserService;
import com.hospital.report.utils.JwtTokenUtil;
import com.hospital.report.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final RedisUtil redisUtil;

    private static final String TOKEN_PREFIX = "token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_TOKEN_PREFIX = "user_token:";
    private static final long REFRESH_TOKEN_EXPIRE = 7 * 24 * 60 * 60; // 7天

    @Override
    public LoginResponse login(LoginRequest loginRequest, String clientIp) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 验证登录尝试次数
        validateLoginAttempts(username);

        try {
            log.info("开始认证用户: {}", username);
            
            // 执行身份验证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            log.info("Spring Security认证成功: {}", username);

            // 获取用户信息
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            log.info("获取UserDetails成功: {}", userDetails.getUsername());
            
            User user = userService.findByUsername(username);
            log.info("从数据库查询用户成功: {}", user != null ? user.getUsername() : "null");

            if (user == null) {
                throw new BadCredentialsException("用户不存在");
            }

            log.info("开始生成JWT Token");
            // 生成JWT Token
            String token = jwtTokenUtil.generateToken(userDetails);
            String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
            log.info("JWT Token生成成功");

            log.info("开始记录登录成功信息");
            // 记录登录成功
            recordLoginSuccess(username, clientIp);
            log.info("记录登录成功信息完成");

            log.info("开始缓存Token");
            // 缓存Token
            cacheToken(token, refreshToken, user, loginRequest.getRememberMe());
            log.info("缓存Token完成");

            log.info("开始构建响应");
            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setRefreshToken(refreshToken);
            response.setExpiresIn(jwtTokenUtil.getExpirationTime());

            // 构建用户信息
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            BeanUtils.copyProperties(user, userInfo);
            userInfo.setRoles(userService.findRolesByUserId(user.getId()));
            userInfo.setPermissions(userService.findPermissionsByUserId(user.getId()));
            response.setUserInfo(userInfo);
            log.info("构建响应完成");

            return response;

        } catch (Exception e) {
            // 记录登录失败
            log.error("Login failed for user {}: {}", username, e.getMessage(), e);
            recordLoginFailure(username);
            throw new BadCredentialsException("用户名或密码错误");
        }
    }

    @Override
    public void logout(String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            
            // 从Redis中移除Token
            redisUtil.delete(TOKEN_PREFIX + token);
            redisUtil.delete(USER_TOKEN_PREFIX + username);
            
            // 清除Security Context
            SecurityContextHolder.clearContext();
            
            log.info("用户 {} 已登出", username);
        } catch (Exception e) {
            log.error("登出失败", e);
        }
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        try {
            if (!jwtTokenUtil.validateRefreshToken(refreshToken)) {
                throw new BadCredentialsException("刷新令牌无效");
            }

            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
            UserDetails userDetails = userService.loadUserByUsername(username);
            
            String newToken = jwtTokenUtil.generateToken(userDetails);
            String newRefreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

            // 更新缓存
            User user = userService.findByUsername(username);
            cacheToken(newToken, newRefreshToken, user, false);

            LoginResponse response = new LoginResponse();
            response.setToken(newToken);
            response.setRefreshToken(newRefreshToken);
            response.setExpiresIn(jwtTokenUtil.getExpirationTime());

            return response;

        } catch (Exception e) {
            throw new BadCredentialsException("刷新令牌失败");
        }
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userService.findByUsername(userDetails.getUsername());
        }
        return null;
    }

    @Override
    public boolean hasPermission(String permission) {
        User user = getCurrentUser();
        if (user == null) return false;
        
        List<String> permissions = userService.findPermissionsByUserId(user.getId());
        return permissions.contains(permission);
    }

    @Override
    public boolean hasRole(String role) {
        User user = getCurrentUser();
        if (user == null) return false;
        
        List<String> roles = userService.findRolesByUserId(user.getId());
        return roles.contains(role);
    }

    @Override
    public boolean isTokenValid(String token) {
        return jwtTokenUtil.validateToken(token) && redisUtil.hasKey(TOKEN_PREFIX + token);
    }

    @Override
    public void validateLoginAttempts(String username) {
        if (userService.isAccountLocked(username)) {
            throw new BadCredentialsException("账户已被锁定，请稍后再试");
        }
    }

    @Override
    public void recordLoginSuccess(String username, String clientIp) {
        User user = userService.findByUsername(username);
        if (user != null) {
            userService.updateLoginInfo(user.getId(), clientIp);
            userService.resetFailedLoginAttempts(username);
        }
    }

    @Override
    public void recordLoginFailure(String username) {
        userService.incrementFailedLoginAttempts(username);
    }

    private void cacheToken(String token, String refreshToken, User user, Boolean rememberMe) {
        long expiration = rememberMe ? REFRESH_TOKEN_EXPIRE : jwtTokenUtil.getExpirationTime() / 1000;
        
        // 缓存Token
        redisUtil.set(TOKEN_PREFIX + token, user.getUsername(), expiration);
        redisUtil.set(REFRESH_TOKEN_PREFIX + refreshToken, user.getUsername(), REFRESH_TOKEN_EXPIRE);
        redisUtil.set(USER_TOKEN_PREFIX + user.getUsername(), token, expiration);
    }
}