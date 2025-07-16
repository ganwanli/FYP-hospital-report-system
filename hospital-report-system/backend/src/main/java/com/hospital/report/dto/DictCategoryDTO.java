package com.hospital.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 数据字典分类请求DTO
 * 
 * @author system
 * @since 2025-01-15
 */
@Data
@Schema(description = "数据字典分类请求DTO")
public class DictCategoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID（更新时需要）
     */
    @Schema(description = "分类ID", example = "1")
    private Long id;

    /**
     * 分类编码
     */
    @NotBlank(message = "分类编码不能为空")
    @Size(max = 50, message = "分类编码长度不能超过50个字符")
    @Schema(description = "分类编码", required = true, example = "REPORT")
    private String categoryCode;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称长度不能超过100个字符")
    @Schema(description = "分类名称", required = true, example = "上报类")
    private String categoryName;

    /**
     * 父级分类ID，0表示顶级
     */
    @NotNull(message = "父级分类ID不能为空")
    @Schema(description = "父级分类ID，0表示顶级", example = "0")
    private Long parentId;

    /**
     * 排序序号
     */
    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    /**
     * 图标
     */
    @Size(max = 50, message = "图标长度不能超过50个字符")
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
     * 构造函数
     */
    public DictCategoryDTO() {
        this.parentId = 0L;
        this.sortOrder = 0;
        this.status = 1;
    }
}
