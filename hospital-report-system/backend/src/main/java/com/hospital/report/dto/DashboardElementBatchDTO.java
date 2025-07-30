package com.hospital.report.dto;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Dashboard元素批量保存请求DTO
 */
@Data
public class DashboardElementBatchDTO {
    
    /**
     * 元素列表
     */
    @NotNull(message = "元素列表不能为空")
    @Valid
    private List<ElementData> elements;
    
    @Data
    public static class ElementData {
        /**
         * 元素ID（前端生成）
         */
        @NotBlank(message = "元素ID不能为空")
        private String elementId;
        
        /**
         * 元素类型
         */
        @NotBlank(message = "元素类型不能为空")
        private String elementType;
        
        /**
         * X坐标位置
         */
        @NotNull(message = "X坐标不能为空")
        private Integer positionX;
        
        /**
         * Y坐标位置
         */
        @NotNull(message = "Y坐标不能为空")
        private Integer positionY;
        
        /**
         * 宽度
         */
        @NotNull(message = "宽度不能为空")
        private Integer width;
        
        /**
         * 高度
         */
        @NotNull(message = "高度不能为空")
        private Integer height;
        
        /**
         * 内容配置JSON
         */
        private String contentConfig;
        
        /**
         * 数据配置JSON
         */
        private String dataConfig;
        
        /**
         * 样式配置JSON
         */
        private String styleConfig;
        
        /**
         * 排序顺序
         */
        private Integer sortOrder = 0;
    }
}