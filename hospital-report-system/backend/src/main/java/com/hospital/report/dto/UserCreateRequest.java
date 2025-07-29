package com.hospital.report.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名不能超过50个字符")
    private String realName;

    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String email;

    @Size(max = 20, message = "电话号码不能超过20个字符")
    private String phone;

    private Integer gender;

    private Long departmentId;

    @Size(max = 50, message = "职位不能超过50个字符")
    private String position;

    @Size(max = 50, message = "员工编号不能超过50个字符")
    private String employeeId;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remarks;

    private Long createdBy;

    // 角色ID，可选
    private Long roleId;
}