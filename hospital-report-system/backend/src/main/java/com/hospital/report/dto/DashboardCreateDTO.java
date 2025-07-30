package com.hospital.report.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Dashboard创建请求DTO
 */
@Data
public class DashboardCreateDTO {
    
    /**
     * Dashboard名称
     */
    @NotBlank(message = "Dashboard名称不能为空")
    private String dashboardName;
    
    /**
     * Dashboard类型 (1:个人, 2:部门, 3:公共)
     */
    @NotNull(message = "Dashboard类型不能为空")
    private Integer dashboardType;
    
    /**
     * 是否为默认Dashboard
     */
    private Boolean isDefault = false;
    
    /**
     * Dashboard描述
     */
    private String description;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder = 0;
    
    /**
     * 布局配置JSON
     */
    private String layoutConfig;
}