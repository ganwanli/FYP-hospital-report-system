package com.hospital.report.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class PermissionCreateRequest {

    @NotNull(message = "父级权限ID不能为空")
    private Long parentId = 0L;

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称不能超过50个字符")
    private String permissionName;

    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码不能超过100个字符")
    private String permissionCode;

    @NotBlank(message = "权限类型不能为空")
    @Size(max = 20, message = "权限类型不能超过20个字符")
    private String permissionType;

    @Size(max = 255, message = "菜单URL不能超过255个字符")
    private String menuUrl;

    @Size(max = 50, message = "菜单图标不能超过50个字符")
    private String menuIcon;

    @Size(max = 255, message = "组件路径不能超过255个字符")
    private String component;

    @Size(max = 255, message = "重定向地址不能超过255个字符")
    private String redirect;

    @NotNull(message = "排序顺序不能为空")
    private Integer sortOrder = 0;

    private Boolean isVisible = true;

    private Boolean isExternal = false;

    private Boolean isCache = true;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remarks;
}