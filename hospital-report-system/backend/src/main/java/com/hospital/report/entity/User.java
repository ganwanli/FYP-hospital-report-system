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

    @TableField(value = "gender", exist = false)
    private Integer gender;

    @TableField(value = "birth_date", exist = false)
    private LocalDateTime birthDate;

    @TableField("department_id")
    private Long departmentId;

    @TableField(value = "position", exist = false)
    private String position;

    @TableField(value = "employee_id", exist = false)
    private String employeeId;

    @TableField("status")
    private Integer status;

    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("login_count")
    private Integer loginCount;

    @TableField(value = "password_update_time", exist = false)
    private LocalDateTime passwordUpdateTime;

    @TableField(value = "account_expire_time", exist = false)
    private LocalDateTime accountExpireTime;

    @TableField(value = "password_expire_time", exist = false)
    private LocalDateTime passwordExpireTime;

    @TableField(value = "is_locked", exist = false)
    private Boolean isLocked;

    @TableField(value = "lock_time", exist = false)
    private LocalDateTime lockTime;

    @TableField(value = "failed_login_attempts", exist = false)
    private Integer failedLoginAttempts;

    @TableField(value = "remarks", exist = false)
    private String remarks;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private List<Role> roles;

    @TableField(exist = false)
    private List<String> permissions;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}