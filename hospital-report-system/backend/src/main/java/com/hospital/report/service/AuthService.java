package com.hospital.report.service;

import com.hospital.report.dto.LoginRequest;
import com.hospital.report.dto.LoginResponse;
import com.hospital.report.entity.User;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest, String clientIp);

    void logout(String token);

    LoginResponse refreshToken(String refreshToken);

    User getCurrentUser();

    boolean hasPermission(String permission);

    boolean hasRole(String role);

    boolean isTokenValid(String token);

    void validateLoginAttempts(String username);

    void recordLoginSuccess(String username, String clientIp);

    void recordLoginFailure(String username);
}