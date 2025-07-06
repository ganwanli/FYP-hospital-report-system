package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_user")
public class User extends Model<User> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("email")
    private String email;

    @TableField("phone")
    private String phone;

    @TableField("real_name")
    private String realName;

    @TableField("avatar")
    private String avatar;

    @TableField("gender")
    private Integer gender;

    @TableField("birth_date")
    private LocalDateTime birthDate;

    @TableField("department_id")
    private Long departmentId;

    @TableField("position")
    private String position;

    @TableField("employee_id")
    private String employeeId;

    @TableField("status")
    private Integer status;

    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("login_count")
    private Integer loginCount;

    @TableField("password_update_time")
    private LocalDateTime passwordUpdateTime;

    @TableField("account_expire_time")
    private LocalDateTime accountExpireTime;

    @TableField("password_expire_time")
    private LocalDateTime passwordExpireTime;

    @TableField("is_locked")
    private Boolean isLocked;

    @TableField("lock_time")
    private LocalDateTime lockTime;

    @TableField("failed_login_attempts")
    private Integer failedLoginAttempts;

    @TableField("remarks")
    private String remarks;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;

    @TableField(exist = false)
    private List<Role> roles;

    @TableField(exist = false)
    private List<String> permissions;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}