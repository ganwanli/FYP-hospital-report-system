package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户个人权限实体类
 * 
 * @author System
 * @since 2024-07-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user_permission")
public class UserPermission {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 权限ID
     */
    @TableField("permission_id")
    private Long permissionId;

    /**
     * 权限类型：GRANT-授予，DENY-拒绝
     */
    @TableField("permission_type")
    private String permissionType;



    /**
     * 授权人ID
     */
    @TableField("granted_by")
    private Long grantedBy;

    /**
     * 授权时间
     */
    @TableField("granted_time")
    private LocalDateTime grantedTime;

    /**
     * 权限过期时间，NULL表示永不过期
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 是否激活：1-激活，0-禁用
     */
    @TableField("is_active")
    private Integer isActive;

    /**
     * 备注说明
     */
    @TableField("remarks")
    private String remarks;

    /**
     * 创建人
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    // 权限类型常量
    public static final String PERMISSION_TYPE_GRANT = "GRANT";
    public static final String PERMISSION_TYPE_DENY = "DENY";

    // 激活状态常量
    public static final Integer ACTIVE_YES = 1;
    public static final Integer ACTIVE_NO = 0;

    /**
     * 检查权限是否有效
     */
    public boolean isValid() {
        return isActive != null && isActive.equals(ACTIVE_YES) &&
               (expireTime == null || expireTime.isAfter(LocalDateTime.now()));
    }

    /**
     * 检查是否为授权权限
     */
    public boolean isGrantPermission() {
        return PERMISSION_TYPE_GRANT.equals(permissionType);
    }

    /**
     * 检查是否为拒绝权限
     */
    public boolean isDenyPermission() {
        return PERMISSION_TYPE_DENY.equals(permissionType);
    }


}
