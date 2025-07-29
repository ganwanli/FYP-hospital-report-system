package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("SELECT * FROM sys_permission WHERE deleted = 0 ORDER BY sort_order ASC, created_time ASC")
    List<Permission> findAllPermissions();

    @Select("SELECT * FROM sys_permission WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY sort_order ASC")
    List<Permission> findPermissionsByParentId(Long parentId);

    @Select("SELECT p.* " +
            "FROM sys_permission p " +
            "LEFT JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "LEFT JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.is_deleted = 0")
    List<Permission> findPermissionsByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM sys_permission WHERE parent_id = #{parentId} AND is_deleted = 0")
    int countChildrenByParentId(Long parentId);
}