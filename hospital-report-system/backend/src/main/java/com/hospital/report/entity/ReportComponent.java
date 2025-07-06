package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("report_component")
public class ReportComponent {

    @TableId(value = "component_id", type = IdType.AUTO)
    private Long componentId;

    @TableField("report_id")
    private Long reportId;

    @TableField("component_type")
    private String componentType;

    @TableField("component_name")
    private String componentName;

    @TableField("position_x")
    private Integer positionX;

    @TableField("position_y")
    private Integer positionY;

    @TableField("width")
    private Integer width;

    @TableField("height")
    private Integer height;

    @TableField("z_index")
    private Integer zIndex;

    @TableField("data_source_id")
    private Long dataSourceId;

    @TableField("data_config")
    private String dataConfig;

    @TableField("style_config")
    private String styleConfig;

    @TableField("chart_config")
    private String chartConfig;

    @TableField("table_config")
    private String tableConfig;

    @TableField("text_config")
    private String textConfig;

    @TableField("image_config")
    private String imageConfig;

    @TableField("is_visible")
    private Boolean isVisible;

    @TableField("is_locked")
    private Boolean isLocked;

    @TableField("component_order")
    private Integer componentOrder;

    @TableField("parent_component_id")
    private Long parentComponentId;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @TableField("conditions_config")
    private String conditionsConfig;

    @TableField("interaction_config")
    private String interactionConfig;
}