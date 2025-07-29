package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.report.entity.User;
import com.hospital.report.mapper.UserMapper;
import com.hospital.report.service.UserService;
import com.hospital.report.service.UserPermissionService;
import com.hospital.report.service.RoleService;
import com.hospital.report.utils.PasswordEncoderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoderUtil passwordEncoderUtil;
    private final UserPermissionService userPermissionService;
    private final RoleService roleService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        List<String> permissions = findPermissionsByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(isAccountExpired(user))
                .accountLocked(isAccountLocked(user))
                .credentialsExpired(isPasswordExpired(user))
                .disabled(user.getStatus() == 0)
                .build();
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public List<String> findPermissionsByUserId(Long userId) {
        return userMapper.findPermissionsByUserId(userId);
    }

    @Override
    public List<String> findRolesByUserId(Long userId) {
        return userMapper.findRolesByUserId(userId);
    }

    @Override
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoderUtil.matches(rawPassword, encodedPassword);
    }



    @Override
    @Transactional
    public void updateLoginInfo(Long userId, String loginIp) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        user.setLoginCount(getById(userId).getLoginCount() + 1);
        updateById(user);
    }

    @Override
    @Transactional
    public void incrementFailedLoginAttempts(String username) {
        // 暂时禁用失败登录尝试计数功能，因为数据库缺少相关字段
        log.warn("incrementFailedLoginAttempts 功能已暂时禁用");
        /*
        User user = findByUsername(username);
        if (user != null) {
            int attempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
            attempts++;
            
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setFailedLoginAttempts(attempts);
            
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                updateUser.setIsLocked(true);
                updateUser.setLockTime(LocalDateTime.now());
            }
            
            updateById(updateUser);
        }
        */
    }

    @Override
    @Transactional
    public void resetFailedLoginAttempts(String username) {
        // 暂时禁用失败登录尝试重置功能，因为数据库缺少相关字段
        log.warn("resetFailedLoginAttempts 功能已暂时禁用");
        /*
        User user = findByUsername(username);
        if (user != null && user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setFailedLoginAttempts(0);
            updateUser.setIsLocked(false);
            updateUser.setLockTime(null);
            updateById(updateUser);
        }
        */
    }

    @Override
    @Transactional
    public void lockUser(String username) {
        User user = findByUsername(username);
        if (user != null) {
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setIsLocked(true);
            updateUser.setLockTime(LocalDateTime.now());
            updateById(updateUser);
        }
    }

    @Override
    @Transactional
    public void unlockUser(String username) {
        User user = findByUsername(username);
        if (user != null) {
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setIsLocked(false);
            updateUser.setLockTime(null);
            updateUser.setFailedLoginAttempts(0);
            updateById(updateUser);
        }
    }

    @Override
    public boolean isAccountLocked(String username) {
        // 暂时禁用账户锁定检查功能，因为数据库缺少相关字段
        return false;
        /*
        User user = findByUsername(username);
        if (user == null || user.getIsLocked() == null || !user.getIsLocked()) {
            return false;
        }
        
        if (user.getLockTime() != null) {
            LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_DURATION_MINUTES);
            if (LocalDateTime.now().isAfter(unlockTime)) {
                unlockUser(username);
                return false;
            }
        }
        
        return true;
        */
    }

    @Override
    public boolean isAccountExpired(String username) {
        User user = findByUsername(username);
        return isAccountExpired(user);
    }
    
    private boolean isAccountExpired(User user) {
        if (user == null || user.getAccountExpireTime() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(user.getAccountExpireTime());
    }

    @Override
    public boolean isPasswordExpired(String username) {
        User user = findByUsername(username);
        return isPasswordExpired(user);
    }
    
    private boolean isPasswordExpired(User user) {
        if (user == null || user.getPasswordExpireTime() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(user.getPasswordExpireTime());
    }
    
    private boolean isAccountLocked(User user) {
        // 暂时禁用账户锁定检查功能，因为数据库缺少相关字段
        return false;
    }

    @Override
    @Transactional
    public boolean updatePassword(Long userId, String newPassword) {
        User user = new User();
        user.setId(userId);
        user.setPassword(passwordEncoderUtil.encode(newPassword));
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setPasswordExpireTime(LocalDateTime.now().plusDays(90));
        return updateById(user);
    }

    @Override
    @Transactional
    public boolean createUser(User user) {
        // 使用BCrypt加密密码（与登录验证保持一致）
        user.setPassword(passwordEncoderUtil.encode(user.getPassword()));

        // 设置其他字段
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setPasswordExpireTime(LocalDateTime.now().plusDays(90));
        user.setStatus(1);
        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLoginCount(0);
        user.setDeleted(0);

        return save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUserWithRole(User user, Long roleId) {
        try {
            log.info("开始创建用户，用户名: {}, 角色ID: {}", user.getUsername(), roleId);

            // 第一步：创建用户
            boolean userCreated = createUser(user);
            if (!userCreated) {
                log.error("创建用户失败，用户名: {}", user.getUsername());
                throw new RuntimeException("创建用户失败");
            }

            log.info("用户创建成功，用户ID: {}", user.getId());

            // 第二步：如果指定了角色，分配角色给用户
            if (roleId != null) {
                log.info("开始分配角色，用户ID: {}, 角色ID: {}", user.getId(), roleId);

                boolean roleAssigned = roleService.assignRoleToUser(user.getId(), roleId, user.getCreatedBy());
                if (!roleAssigned) {
                    log.error("分配角色失败，用户ID: {}, 角色ID: {}", user.getId(), roleId);
                    throw new RuntimeException("分配角色失败");
                }

                log.info("角色分配成功，用户ID: {}, 角色ID: {}", user.getId(), roleId);
            }

            // 第三步：将角色权限复制到个人权限表
            log.info("开始复制角色权限到个人权限表，用户ID: {}", user.getId());

            int copiedPermissions = userPermissionService.resetUserPermissionsFromRoles(
                user.getId(),
                user.getCreatedBy() != null ? user.getCreatedBy() : 1L
            );

            log.info("权限复制成功，用户ID: {}, 复制权限数量: {}", user.getId(), copiedPermissions);

            log.info("用户创建完成，用户ID: {}, 用户名: {}, 角色ID: {}, 权限数量: {}",
                    user.getId(), user.getUsername(), roleId, copiedPermissions);

            return true;

        } catch (Exception e) {
            log.error("创建用户失败，用户名: {}, 角色ID: {}, 错误: {}", user.getUsername(), roleId, e.getMessage(), e);
            // 事务会自动回滚
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean updateUser(User user) {
        user.setUpdatedTime(LocalDateTime.now());
        return updateById(user);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        return deleteUser(userId, null);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId, Long updatedBy) {
        log.info("开始删除用户，ID: {}, 操作人ID: {}", userId, updatedBy);

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId)
                     .eq("deleted", 0)  // 只更新未删除的用户
                     .set("deleted", 1)
                     .set("updated_time", LocalDateTime.now());

        // 如果提供了操作人ID，则设置updated_by字段
        if (updatedBy != null) {
            updateWrapper.set("updated_by", updatedBy);
        }

        int rows = baseMapper.update(null, updateWrapper);
        log.info("删除用户完成，ID: {}, 操作人ID: {}, 影响行数: {}", userId, updatedBy, rows);

        return rows > 0;
    }
}