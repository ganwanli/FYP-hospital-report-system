package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Select("SELECT r.* " +
            "FROM sys_role r " +
            "WHERE r.deleted = 0 " +
            "ORDER BY r.sort_order ASC, r.created_time DESC")
    List<Role> findAllRoles();

    @Select("SELECT r.* " +
            "FROM sys_role r " +
            "LEFT JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    @Select("SELECT p.* " +
            "FROM sys_permission p " +
            "LEFT JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0")
    List<Long> findPermissionIdsByRoleId(@Param("roleId") Long roleId);
}