package com.hospital.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 分类排序DTO
 * 
 * @author system
 * @since 2025-01-15
 */
@Data
@Schema(description = "分类排序DTO")
public class CategorySortDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 排序项列表
     */
    @NotEmpty(message = "排序项列表不能为空")
    @Schema(description = "排序项列表", required = true)
    private List<SortItem> sortItems;

    /**
     * 排序项
     */
    @Data
    @Schema(description = "排序项")
    public static class SortItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 分类ID
         */
        @NotNull(message = "分类ID不能为空")
        @Schema(description = "分类ID", required = true, example = "1")
        private Long id;

        /**
         * 排序序号
         */
        @NotNull(message = "排序序号不能为空")
        @Schema(description = "排序序号", required = true, example = "1")
        private Integer sortOrder;
    }
}
