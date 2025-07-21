package com.hospital.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类字段混合树形结构VO
 * 支持分类作为分支节点，字段作为叶子节点
 * 
 * @author system
 * @since 2025-01-20
 */
@Data
@Schema(description = "分类字段混合树形结构VO")
public class CategoryFieldTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    @Schema(description = "节点ID", example = "1")
    private Long id;

    /**
     * 节点类型：category-分类，field-字段
     */
    @Schema(description = "节点类型", example = "category", allowableValues = {"category", "field"})
    private String nodeType;

    /**
     * 节点编码
     */
    @Schema(description = "节点编码", example = "REPORT")
    private String code;

    /**
     * 节点名称
     */
    @Schema(description = "节点名称", example = "上报类")
    private String name;

    /**
     * 英文名称（仅字段节点有效）
     */
    @Schema(description = "英文名称")
    private String nameEn;

    /**
     * 父级ID，0表示顶级
     */
    @Schema(description = "父级ID，0表示顶级", example = "0")
    private Long parentId;

    /**
     * 所属分类ID（仅字段节点有效）
     */
    @Schema(description = "所属分类ID")
    private Long categoryId;

    /**
     * 层级，1为顶级
     */
    @Schema(description = "层级，1为顶级", example = "1")
    private Integer level;

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
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 状态：1-正常，0-禁用
     */
    @Schema(description = "状态：1-正常，0-禁用", example = "1")
    private Integer status;

    // 字段特有属性
    /**
     * 数据类型（仅字段节点有效）
     */
    @Schema(description = "数据类型")
    private String dataType;

    /**
     * 数据长度（仅字段节点有效）
     */
    @Schema(description = "数据长度")
    private String dataLength;

    /**
     * 源数据库名（仅字段节点有效）
     */
    @Schema(description = "源数据库名")
    private String sourceDatabase;

    /**
     * 源数据表名（仅字段节点有效）
     */
    @Schema(description = "源数据表名")
    private String sourceTable;

    /**
     * 源字段名（仅字段节点有效）
     */
    @Schema(description = "源字段名")
    private String sourceField;

    /**
     * 筛选条件（仅字段节点有效）
     */
    @Schema(description = "筛选条件")
    private String filterCondition;

    /**
     * SQL查询语句（仅字段节点有效）
     */
    @Schema(description = "SQL查询语句")
    private String calculationSql;

    /**
     * 更新频率（仅字段节点有效）
     */
    @Schema(description = "更新频率")
    private String updateFrequency;

    /**
     * 数据负责人（仅字段节点有效）
     */
    @Schema(description = "数据负责人")
    private String dataOwner;

    /**
     * 备注说明
     */
    @Schema(description = "备注说明")
    private String remark;

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

    // 树形结构属性
    /**
     * 是否有子节点
     */
    @Schema(description = "是否有子节点", example = "true")
    private Boolean hasChildren;

    /**
     * 关联字段数量（仅分类节点有效）
     */
    @Schema(description = "关联字段数量", example = "5")
    private Integer fieldCount;

    /**
     * 子节点列表
     */
    @Schema(description = "子节点列表")
    private List<CategoryFieldTreeVO> children;

    // 前端树组件属性
    /**
     * 节点键值（用于前端树组件）
     */
    @Schema(description = "节点键值", example = "category_1")
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
    public CategoryFieldTreeVO() {
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
        this.key = this.nodeType + "_" + this.id;
        this.title = this.name;
        this.value = this.code;
        this.isLeaf = !this.hasChildren;
        this.disabled = this.status != null && this.status == 0;
    }

    /**
     * 判断是否为分类节点
     */
    public boolean isCategory() {
        return "category".equals(this.nodeType);
    }

    /**
     * 判断是否为字段节点
     */
    public boolean isField() {
        return "field".equals(this.nodeType);
    }

    /**
     * 判断是否为顶级节点
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
    public void addChild(CategoryFieldTreeVO child) {
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
        int total = 0;
        if (this.isField()) {
            total = 1;
        } else if (this.fieldCount != null) {
            total = this.fieldCount;
        }
        
        if (this.children != null) {
            for (CategoryFieldTreeVO child : this.children) {
                total += child.getTotalFieldCount();
            }
        }
        return total;
    }
}
