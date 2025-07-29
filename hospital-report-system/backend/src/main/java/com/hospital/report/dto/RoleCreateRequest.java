package com.hospital.report.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class RoleCreateRequest {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称不能超过50个字符")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码不能超过50个字符")
    private String roleCode;

    @Size(max = 255, message = "角色描述不能超过255个字符")
    private String description;

    @Size(max = 50, message = "数据范围不能超过50个字符")
    private String dataScope = "ALL";

    @NotNull(message = "排序顺序不能为空")
    private Integer sortOrder = 0;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remarks;

    private int status = 1;
}