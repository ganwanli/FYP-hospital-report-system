package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_permission")
public class Permission extends Model<Permission> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("parent_id")
    private Long parentId;

    @TableField("permission_name")
    private String permissionName;

    @TableField("permission_code")
    private String permissionCode;

    @TableField("permission_type")
    private String permissionType;

    @TableField("menu_url")
    private String menuUrl;

    @TableField("menu_icon")
    private String menuIcon;

    @TableField("component")
    private String component;

    @TableField("redirect")
    private String redirect;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("is_visible")
    private Boolean isVisible;

    @TableField("is_external")
    private Boolean isExternal;

    @TableField("is_cache")
    private Boolean isCache;

    @TableField("status")
    private Integer status;

    @TableField("remarks")
    private String remarks;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}