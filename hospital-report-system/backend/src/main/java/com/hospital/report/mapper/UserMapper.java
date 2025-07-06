package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT u.*, r.role_name, r.role_code " +
            "FROM sys_user u " +
            "LEFT JOIN sys_user_role ur ON u.id = ur.user_id " +
            "LEFT JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE u.username = #{username} AND u.is_deleted = 0")
    User findByUsername(@Param("username") String username);

    @Select("SELECT p.permission_code " +
            "FROM sys_user u " +
            "LEFT JOIN sys_user_role ur ON u.id = ur.user_id " +
            "LEFT JOIN sys_role r ON ur.role_id = r.id " +
            "LEFT JOIN sys_role_permission rp ON r.id = rp.role_id " +
            "LEFT JOIN sys_permission p ON rp.permission_id = p.id " +
            "WHERE u.id = #{userId} AND u.is_deleted = 0 AND r.is_deleted = 0 AND p.is_deleted = 0")
    List<String> findPermissionsByUserId(@Param("userId") Long userId);

    @Select("SELECT r.* " +
            "FROM sys_user u " +
            "LEFT JOIN sys_user_role ur ON u.id = ur.user_id " +
            "LEFT JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE u.id = #{userId} AND u.is_deleted = 0 AND r.is_deleted = 0")
    List<String> findRolesByUserId(@Param("userId") Long userId);
}