package com.hospital.report.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "user_dashboard")
public class UserDashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint NOT NULL AUTO_INCREMENT COMMENT 'Dashboard ID'")
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "bigint NOT NULL COMMENT '用户ID'")
    private Long userId;

    @Column(name = "dashboard_name", nullable = false, length = 200, columnDefinition = "varchar(200) NOT NULL COMMENT 'Dashboard名称'")
    private String dashboardName;

    @Column(name = "dashboard_type", nullable = false, columnDefinition = "int NOT NULL DEFAULT 1 COMMENT 'Dashboard类型(1:个人,2:部门,3:公共)'")
    private Integer dashboardType;

    @Column(name = "is_default", columnDefinition = "tinyint(1) DEFAULT 0 COMMENT '是否为默认Dashboard'")
    private Boolean isDefault;

    @Lob
    @Column(name = "layout_config", columnDefinition = "longtext COMMENT '布局配置JSON'")
    private String layoutConfig;

    @Column(name = "description", length = 500, columnDefinition = "varchar(500) COMMENT 'Dashboard描述'")
    private String description;

    @Column(name = "sort_order", columnDefinition = "int DEFAULT 0 COMMENT '排序顺序'")
    private Integer sortOrder;

    @Column(name = "status", columnDefinition = "tinyint DEFAULT 1 COMMENT '状态(1:启用,0:禁用)'")
    private Integer status;

    @Column(name = "created_time", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'")
    private LocalDateTime createdTime;

    @Column(name = "updated_time", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'")
    private LocalDateTime updatedTime;

    @Column(name = "created_by", columnDefinition = "bigint COMMENT '创建人'")
    private Long createdBy;

    @Column(name = "updated_by", columnDefinition = "bigint COMMENT '更新人'")
    private Long updatedBy;

    @Version
    @Column(name = "version", columnDefinition = "int DEFAULT 1 COMMENT '版本号(乐观锁)'")
    private Integer version;

    @Column(name = "is_deleted", columnDefinition = "tinyint(1) DEFAULT 0 COMMENT '是否删除'")
    private Boolean isDeleted;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (isDefault == null) {
            isDefault = false;
        }
        if (status == null) {
            status = 1;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (version == null) {
            version = 1;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}