package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sql_template_parameter")
public class SqlTemplateParameter {

    @TableId(value = "parameter_id", type = IdType.AUTO)
    private Long parameterId;

    @TableField("template_id")
    private Long templateId;

    @TableField("parameter_name")
    private String parameterName;

    @TableField("parameter_type")
    private String parameterType;

    @TableField("parameter_description")
    private String parameterDescription;

    @TableField("default_value")
    private String defaultValue;

    @TableField("is_required")
    private Boolean isRequired;

    @TableField("validation_rule")
    private String validationRule;

    @TableField("validation_message")
    private String validationMessage;

    @TableField("parameter_order")
    private Integer parameterOrder;

    @TableField("min_length")
    private Integer minLength;

    @TableField("max_length")
    private Integer maxLength;

    @TableField("min_value")
    private String minValue;

    @TableField("max_value")
    private String maxValue;

    @TableField("allowed_values")
    private String allowedValues;

    @TableField("input_type")
    private String inputType;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

    @TableField("is_sensitive")
    private Boolean isSensitive;

    @TableField("mask_pattern")
    private String maskPattern;
}