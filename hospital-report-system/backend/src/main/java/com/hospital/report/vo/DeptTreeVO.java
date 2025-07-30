package com.hospital.report.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeptTreeVO {
    private Long id;
    private String deptName;
    private String deptCode;
    private Long parentId;
    private String deptType;
    private String leader;
    private String phone;
    private String email;
    private String address;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Long createdBy;
    private Long updatedBy;
    private List<DeptTreeVO> children;
}
