package com.hospital.report.dto;

import lombok.Data;

@Data
public class RoleQueryRequest {

    private Long current = 1L;
    
    private Long size = 10L;
    
    private String roleName;
    
    private String roleCode;
    
    private Integer status;
}