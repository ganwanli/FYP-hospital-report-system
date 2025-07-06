package com.hospital.report.dto;

import lombok.Data;

@Data
public class DataSourceQueryRequest {

    private Long current = 1L;
    
    private Long size = 10L;
    
    private String datasourceName;
    
    private String datasourceCode;
    
    private String databaseType;
    
    private Integer status;
}