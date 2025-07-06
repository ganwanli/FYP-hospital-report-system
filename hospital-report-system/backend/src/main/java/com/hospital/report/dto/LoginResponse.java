package com.hospital.report.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {

    private String token;

    private String refreshToken;

    private Long expiresIn;

    private String tokenType = "Bearer";

    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String email;
        private String phone;
        private String avatar;
        private Integer gender;
        private String department;
        private String position;
        private List<String> roles;
        private List<String> permissions;
    }
}