package com.hospital.report.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 报表收藏实体类
 * 用于存储用户收藏的报表信息
 */
@Entity
@Table(name = "report_favorites", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "report_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportFavorite {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 报表ID
     */
    @Column(name = "report_id", nullable = false)
    private Long reportId;

    /**
     * 收藏时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 收藏备注（可选）
     */
    @Column(name = "note", length = 500)
    private String note;

    /**
     * 收藏标签（可选，用于分类）
     */
    @Column(name = "tags", length = 200)
    private String tags;

    /**
     * 是否启用（软删除标记）
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 排序权重（用户可以调整收藏的显示顺序）
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}
