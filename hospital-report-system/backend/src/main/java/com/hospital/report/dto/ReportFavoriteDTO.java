package com.hospital.report.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 报表收藏数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportFavoriteDTO {

    /**
     * 收藏ID
     */
    private Long id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 报表ID
     */
    @NotNull(message = "报表ID不能为空")
    private Long reportId;

    /**
     * 报表名称（关联查询获得）
     */
    private String reportName;

    /**
     * 报表描述（关联查询获得）
     */
    private String reportDescription;

    /**
     * 报表创建者（关联查询获得）
     */
    private String reportCreator;

    /**
     * 报表类型（关联查询获得）
     */
    private String reportType;

    /**
     * 收藏时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 收藏备注
     */
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String note;

    /**
     * 收藏标签
     */
    @Size(max = 200, message = "标签长度不能超过200字符")
    private String tags;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;
}

/**
 * 创建收藏请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CreateFavoriteRequest {

    /**
     * 报表ID
     */
    @NotNull(message = "报表ID不能为空")
    private Long reportId;

    /**
     * 收藏备注
     */
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String note;

    /**
     * 收藏标签
     */
    @Size(max = 200, message = "标签长度不能超过200字符")
    private String tags;

    /**
     * 排序权重
     */
    private Integer sortOrder;
}

/**
 * 更新收藏请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UpdateFavoriteRequest {

    /**
     * 收藏备注
     */
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String note;

    /**
     * 收藏标签
     */
    @Size(max = 200, message = "标签长度不能超过200字符")
    private String tags;

    /**
     * 排序权重
     */
    private Integer sortOrder;
}

/**
 * 收藏统计DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class FavoriteStatsDTO {

    /**
     * 用户收藏总数
     */
    private Long userFavoriteCount;

    /**
     * 报表被收藏总数
     */
    private Long reportFavoriteCount;

    /**
     * 最近收藏时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastFavoriteTime;
}
