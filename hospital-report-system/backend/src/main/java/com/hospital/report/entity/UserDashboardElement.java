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
@Table(name = "user_dashboard_elements")
public class UserDashboardElement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint NOT NULL AUTO_INCREMENT COMMENT '元素ID'")
    private Long id;

    @Column(name = "dashboard_id", nullable = false, columnDefinition = "bigint NOT NULL COMMENT 'Dashboard ID'")
    private Long dashboardId;

    @Column(name = "element_id", nullable = false, length = 100, columnDefinition = "varchar(100) NOT NULL COMMENT '元素ID(前端生成)'")
    private String elementId;

    @Column(name = "element_type", nullable = false, length = 50, columnDefinition = "varchar(50) NOT NULL COMMENT '元素类型(text,report,chart)'")
    private String elementType;

    @Column(name = "position_x", nullable = false, columnDefinition = "int NOT NULL COMMENT 'X坐标位置'")
    private Integer positionX;

    @Column(name = "position_y", nullable = false, columnDefinition = "int NOT NULL COMMENT 'Y坐标位置'")
    private Integer positionY;

    @Column(name = "width", nullable = false, columnDefinition = "int NOT NULL COMMENT '宽度'")
    private Integer width;

    @Column(name = "height", nullable = false, columnDefinition = "int NOT NULL COMMENT '高度'")
    private Integer height;

    @Lob
    @Column(name = "content_config", columnDefinition = "longtext COMMENT '内容配置JSON'")
    private String contentConfig;

    @Lob
    @Column(name = "data_config", columnDefinition = "longtext COMMENT '数据配置JSON'")
    private String dataConfig;

    @Lob
    @Column(name = "style_config", columnDefinition = "longtext COMMENT '样式配置JSON'")
    private String styleConfig;

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