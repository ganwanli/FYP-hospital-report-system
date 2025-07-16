package com.hospital.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 分类状态批量修改DTO
 * 
 * @author system
 * @since 2025-01-15
 */
@Data
@Schema(description = "分类状态批量修改DTO")
public class CategoryStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID列表
     */
    @NotEmpty(message = "分类ID列表不能为空")
    @Schema(description = "分类ID列表", required = true)
    private List<Long> ids;

    /**
     * 状态：1-正常，0-禁用
     */
    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：1-正常，0-禁用", required = true, example = "1")
    private Integer status;
}
