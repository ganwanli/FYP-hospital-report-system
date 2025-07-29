package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.UserPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户个人权限Mapper接口
 * 
 * @author System
 * @since 2024-07-28
 */
@Mapper
public interface UserPermissionMapper extends BaseMapper<UserPermission> {

    /**
     * 获取用户通过角色获得的权限ID列表（仅角色权限）
     *
     * @param userId 用户ID
     * @return 权限ID列表
     */
    @Select("SELECT DISTINCT rp.permission_id " +
            "FROM sys_user_role ur " +
            "JOIN sys_role_permission rp ON ur.role_id = rp.role_id " +
            "WHERE ur.user_id = #{userId} ")
    List<Long> findRolePermissionIdsByUserId(@Param("userId") Long userId);

    /**
     * 获取用户的有效权限（包含角色权限和个人权限，排除拒绝权限）
     *
     * @param userId 用户ID
     * @return 权限ID列表
     */
    @Select("SELECT DISTINCT p.id " +
            "FROM sys_permission p " +
            "WHERE p.id IN (" +
            "    SELECT DISTINCT rp.permission_id " +
            "    FROM sys_user_role ur " +
            "    JOIN sys_role_permission rp ON ur.role_id = rp.role_id " +
            "    WHERE ur.user_id = #{userId} AND ur.deleted = 0 " +
            "    UNION " +
            "    SELECT DISTINCT up.permission_id " +
            "    FROM sys_user_permission up " +
            "    WHERE up.user_id = #{userId} AND up.deleted = 0 AND up.is_active = 1 " +
            "    AND up.permission_type = 'GRANT' " +
            "    AND (up.expire_time IS NULL OR up.expire_time > NOW()) " +
            ") " +
            "AND p.id NOT IN (" +
            "    SELECT up.permission_id " +
            "    FROM sys_user_permission up " +
            "    WHERE up.user_id = #{userId} AND up.deleted = 0 AND up.is_active = 1 " +
            "    AND up.permission_type = 'DENY' " +
            "    AND (up.expire_time IS NULL OR up.expire_time > NOW()) " +
            ") " +
            "AND p.deleted = 0")
    List<Long> findEffectivePermissionIdsByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否有指定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否有权限
     */
    @Select("SELECT COUNT(*) > 0 " +
            "FROM (" +
            "    SELECT 1 " +
            "    FROM sys_user_role ur " +
            "    JOIN sys_role_permission rp ON ur.role_id = rp.role_id " +
            "    JOIN sys_permission p ON rp.permission_id = p.id " +
            "    WHERE ur.user_id = #{userId} AND p.permission_code = #{permissionCode} " +
            "    AND ur.deleted = 0 AND p.deleted = 0 " +
            "    UNION " +
            "    SELECT 1 " +
            "    FROM sys_user_permission up " +
            "    JOIN sys_permission p ON up.permission_id = p.id " +
            "    WHERE up.user_id = #{userId} AND p.permission_code = #{permissionCode} " +
            "    AND up.deleted = 0 AND up.is_active = 1 AND up.permission_type = 'GRANT' " +
            "    AND (up.expire_time IS NULL OR up.expire_time > NOW()) " +
            ") AS granted_perms " +
            "WHERE NOT EXISTS (" +
            "    SELECT 1 " +
            "    FROM sys_user_permission up " +
            "    JOIN sys_permission p ON up.permission_id = p.id " +
            "    WHERE up.user_id = #{userId} AND p.permission_code = #{permissionCode} " +
            "    AND up.deleted = 0 AND up.is_active = 1 AND up.permission_type = 'DENY' " +
            "    AND (up.expire_time IS NULL OR up.expire_time > NOW()) " +
            ")")
    boolean hasPermission(@Param("userId") Long userId, @Param("permissionCode") String permissionCode);
}
