package com.hospital.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类树形结构VO
 *
 * @author system
 * @since 2025-01-15
 */
@Data
@Schema(description = "分类树形结构VO")
public class CategoryTreeVO implements Serializable {

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
     * 是否有子节点
     */
    @Schema(description = "是否有子节点", example = "true")
    private Boolean hasChildren;

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
     * 子分类列表
     */
    @Schema(description = "子分类列表")
    private List<CategoryTreeVO> children;

    /**
     * 节点键值（用于前端树组件）
     */
    @Schema(description = "节点键值", example = "1")
    private String key;

    /**
     * 节点标题（用于前端树组件）
     */
    @Schema(description = "节点标题", example = "上报类")
    private String title;

    /**
     * 节点值（用于前端树组件）
     */
    @Schema(description = "节点值", example = "REPORT")
    private String value;

    /**
     * 是否为叶子节点
     */
    @Schema(description = "是否为叶子节点", example = "false")
    private Boolean isLeaf;

    /**
     * 是否禁用
     */
    @Schema(description = "是否禁用", example = "false")
    private Boolean disabled;

    /**
     * 是否可选择
     */
    @Schema(description = "是否可选择", example = "true")
    private Boolean selectable;

    /**
     * 构造函数
     */
    public CategoryTreeVO() {
        this.hasChildren = false;
        this.fieldCount = 0;
        this.isLeaf = true;
        this.disabled = false;
        this.selectable = true;
    }

    /**
     * 设置前端树组件需要的属性
     */
    public void setTreeProperties() {
        this.key = String.valueOf(this.id);
        this.title = this.categoryName;
        this.value = this.categoryCode;
        this.isLeaf = !this.hasChildren;
        this.disabled = this.status != null && this.status == 0;
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

    /**
     * 添加子节点
     */
    public void addChild(CategoryTreeVO child) {
        if (this.children == null) {
            this.children = new java.util.ArrayList<>();
        }
        this.children.add(child);
        this.hasChildren = true;
        this.isLeaf = false;
    }

    /**
     * 获取子节点数量
     */
    public int getChildrenCount() {
        return this.children == null ? 0 : this.children.size();
    }

    /**
     * 递归计算总字段数量
     */
    public int getTotalFieldCount() {
        int total = this.fieldCount == null ? 0 : this.fieldCount;
        if (this.children != null) {
            for (CategoryTreeVO child : this.children) {
                total += child.getTotalFieldCount();
            }
        }
        return total;
    }
}
