package com.hospital.report.dto;

import lombok.Data;

import java.util.List;

@Data
public class RolePermissionRequest {

    private List<Long> permissionIds;
}