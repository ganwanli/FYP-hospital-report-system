package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("lineage_impact_analysis")
public class LineageImpactAnalysis extends Model<LineageImpactAnalysis> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("analysis_id")
    private String analysisId;

    @TableField("source_node_id")
    private String sourceNodeId;

    @TableField("target_node_id")
    private String targetNodeId;

    @TableField("impact_type")
    private String impactType;

    @TableField("impact_level")
    private String impactLevel;

    @TableField("impact_scope")
    private String impactScope;

    @TableField("affected_objects")
    private String affectedObjects;

    @TableField("impact_description")
    private String impactDescription;

    @TableField("impact_probability")
    private Double impactProbability;

    @TableField("risk_level")
    private String riskLevel;

    @TableField("mitigation_strategy")
    private String mitigationStrategy;

    @TableField("analysis_method")
    private String analysisMethod;

    @TableField("analysis_depth")
    private Integer analysisDepth;

    @TableField("execution_time")
    private Long executionTime;

    @TableField("analysis_result")
    private String analysisResult;

    @TableField("recommendations")
    private String recommendations;

    @TableField("status")
    private Integer status;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}