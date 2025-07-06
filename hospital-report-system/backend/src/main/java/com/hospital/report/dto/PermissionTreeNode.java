package com.hospital.report.dto;

import lombok.Data;

import java.util.List;

@Data
public class PermissionTreeNode {

    private Long id;

    private Long parentId;

    private String permissionName;

    private String permissionCode;

    private String permissionType;

    private String menuUrl;

    private String menuIcon;

    private String component;

    private String redirect;

    private Integer sortOrder;

    private Boolean isVisible;

    private Boolean isExternal;

    private Boolean isCache;

    private Integer status;

    private String remarks;

    private List<PermissionTreeNode> children;

    private Boolean hasChildren;

    public PermissionTreeNode() {}

    public PermissionTreeNode(Long id, String permissionName, String permissionCode) {
        this.id = id;
        this.permissionName = permissionName;
        this.permissionCode = permissionCode;
    }
}