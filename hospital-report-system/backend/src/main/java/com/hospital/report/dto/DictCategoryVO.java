package com.hospital.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据字典分类响应VO
 * 
 * @author system
 * @since 2025-01-15
 */
@Data
@Schema(description = "数据字典分类响应VO")
public class DictCategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    @Schema(description = "分类ID", example = "1")
    private Long id;

    /**
     * 分类编码
     */
    @Schema(description = "分类编码", example = "REPORT")
    private String categoryCode;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", example = "上报类")
    private String categoryName;

    /**
     * 父级分类ID，0表示顶级
     */
    @Schema(description = "父级分类ID，0表示顶级", example = "0")
    private Long parentId;

    /**
     * 父级分类名称
     */
    @Schema(description = "父级分类名称", example = "根分类")
    private String parentName;

    /**
     * 分类层级，1为顶级
     */
    @Schema(description = "分类层级，1为顶级", example = "1")
    private Integer categoryLevel;

    /**
     * 排序序号
     */
    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    /**
     * 图标
     */
    @Schema(description = "图标", example = "folder")
    private String icon;

    /**
     * 分类描述
     */
    @Schema(description = "分类描述")
    private String description;

    /**
     * 状态：1-正常，0-禁用
     */
    @Schema(description = "状态：1-正常，0-禁用", example = "1")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述", example = "正常")
    private String statusText;

    /**
     * 是否有子节点
     */
    @Schema(description = "是否有子节点", example = "true")
    private Boolean hasChildren;

    /**
     * 分类路径
     */
    @Schema(description = "分类路径", example = "根分类/上报类")
    private String categoryPath;

    /**
     * 关联字段数量
     */
    @Schema(description = "关联字段数量", example = "5")
    private Integer fieldCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateBy;

    /**
     * 获取状态描述
     */
    public String getStatusText() {
        if (this.status == null) {
            return "未知";
        }
        return this.status == 1 ? "正常" : "禁用";
    }

    /**
     * 判断是否为顶级分类
     */
    public boolean isTopLevel() {
        return this.parentId == null || this.parentId == 0L;
    }

    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }
}
