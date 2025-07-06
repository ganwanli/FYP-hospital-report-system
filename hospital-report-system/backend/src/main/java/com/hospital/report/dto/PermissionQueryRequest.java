package com.hospital.report.dto;

import lombok.Data;

@Data
public class PermissionQueryRequest {

    private Long current = 1L;
    
    private Long size = 10L;
    
    private String permissionName;
    
    private String permissionCode;
    
    private String permissionType;
    
    private Long parentId;
    
    private Integer status;
}