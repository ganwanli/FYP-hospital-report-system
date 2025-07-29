package com.hospital.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.report.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends IService<User>, UserDetailsService {

    User findByUsername(String username);

    List<String> findPermissionsByUserId(Long userId);

    List<String> findRolesByUserId(Long userId);

    boolean checkPassword(String rawPassword, String encodedPassword);

    void updateLoginInfo(Long userId, String loginIp);

    void incrementFailedLoginAttempts(String username);

    void resetFailedLoginAttempts(String username);

    void lockUser(String username);

    void unlockUser(String username);

    boolean isAccountLocked(String username);

    boolean isAccountExpired(String username);

    boolean isPasswordExpired(String username);

    boolean updatePassword(Long userId, String newPassword);

    boolean createUser(User user);

    boolean createUserWithRole(User user, Long roleId);

    boolean updateUser(User user);

    boolean deleteUser(Long userId);

    boolean deleteUser(Long userId, Long updatedBy);
}