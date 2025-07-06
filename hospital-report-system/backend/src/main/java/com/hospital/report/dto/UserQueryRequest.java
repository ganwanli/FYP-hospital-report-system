package com.hospital.report.dto;

import lombok.Data;

@Data
public class UserQueryRequest {

    private Long current = 1L;
    
    private Long size = 10L;
    
    private String username;
    
    private String realName;
    
    private String email;
    
    private Integer status;
    
    private Long departmentId;
}