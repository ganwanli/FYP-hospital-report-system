package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据字典分类实体类
 * 
 * @author system
 * @since 2025-01-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("dict_category")
@Schema(description = "数据字典分类")
public class DictCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "分类ID")
    private Long id;

    /**
     * 分类编码
     */
    @TableField("category_code")
    @NotBlank(message = "分类编码不能为空")
    @Size(max = 50, message = "分类编码长度不能超过50个字符")
    @Schema(description = "分类编码", required = true, example = "REPORT")
    private String categoryCode;

    /**
     * 分类名称
     */
    @TableField("category_name")
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称长度不能超过100个字符")
    @Schema(description = "分类名称", required = true, example = "上报类")
    private String categoryName;

    /**
     * 父级分类ID，0表示顶级
     */
    @TableField("parent_id")
    @Schema(description = "父级分类ID，0表示顶级", example = "0")
    private Long parentId;

    /**
     * 分类层级，1为顶级
     */
    @TableField("category_level")
    @Schema(description = "分类层级，1为顶级", example = "1")
    private Integer categoryLevel;

    /**
     * 排序序号
     */
    @TableField("sort_order")
    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    /**
     * 图标
     */
    @TableField("icon")
    @Size(max = 50, message = "图标长度不能超过50个字符")
    @Schema(description = "图标", example = "folder")
    private String icon;

    /**
     * 分类描述
     */
    @TableField("description")
    @Schema(description = "分类描述")
    private String description;

    /**
     * 状态：1-正常，0-禁用
     */
    @TableField("status")
    @Schema(description = "状态：1-正常，0-禁用", example = "1")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 更新人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人")
    private String updateBy;

    // 扩展字段（不存储在数据库中）
    /**
     * 子分类列表（用于树形结构）
     */
    @TableField(exist = false)
    @Schema(description = "子分类列表")
    private List<DictCategory> children;

    /**
     * 是否有子节点
     */
    @TableField(exist = false)
    @Schema(description = "是否有子节点")
    private Boolean hasChildren;

    /**
     * 分类路径（用于显示完整路径）
     */
    @TableField(exist = false)
    @Schema(description = "分类路径")
    private String categoryPath;

    /**
     * 父级分类名称
     */
    @TableField(exist = false)
    @Schema(description = "父级分类名称")
    private String parentName;

    /**
     * 关联字段数量
     */
    @TableField(exist = false)
    @Schema(description = "关联字段数量")
    private Integer fieldCount;

    /**
     * 构造函数
     */
    public DictCategory() {
        this.parentId = 0L;
        this.categoryLevel = 1;
        this.sortOrder = 0;
        this.status = 1;
        this.hasChildren = false;
        this.fieldCount = 0;
    }

    /**
     * 判断是否为顶级分类
     *
     * @return true-顶级分类，false-非顶级分类
     */
    public boolean isTopLevel() {
        return this.parentId == null || this.parentId == 0L;
    }

    /**
     * 判断是否启用
     *
     * @return true-启用，false-禁用
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 设置为禁用状态
     */
    public void disable() {
        this.status = 0;
    }

    /**
     * 设置为启用状态
     */
    public void enable() {
        this.status = 1;
    }

    /**
     * 计算分类层级
     *
     * @param parentLevel 父级层级
     */
    public void calculateLevel(Integer parentLevel) {
        this.categoryLevel = (parentLevel == null ? 0 : parentLevel) + 1;
    }

    /**
     * 重写toString方法
     */
    @Override
    public String toString() {
        return "DictCategory{" +
                "id=" + id +
                ", categoryCode='" + categoryCode + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", parentId=" + parentId +
                ", categoryLevel=" + categoryLevel +
                ", sortOrder=" + sortOrder +
                ", status=" + status +
                '}';
    }
}
