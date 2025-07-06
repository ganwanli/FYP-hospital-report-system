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
@TableName("data_lineage")
public class DataLineage extends Model<DataLineage> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("source_type")
    private String sourceType;

    @TableField("source_id")
    private String sourceId;

    @TableField("source_name")
    private String sourceName;

    @TableField("source_table")
    private String sourceTable;

    @TableField("source_column")
    private String sourceColumn;

    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private String targetId;

    @TableField("target_name")
    private String targetName;

    @TableField("target_table")
    private String targetTable;

    @TableField("target_column")
    private String targetColumn;

    @TableField("relation_type")
    private String relationType;

    @TableField("transform_rule")
    private String transformRule;

    @TableField("transform_sql")
    private String transformSql;

    @TableField("data_flow_direction")
    private String dataFlowDirection;

    @TableField("process_type")
    private String processType;

    @TableField("process_name")
    private String processName;

    @TableField("process_description")
    private String processDescription;

    @TableField("schedule_info")
    private String scheduleInfo;

    @TableField("dependency_level")
    private Integer dependencyLevel;

    @TableField("confidence_score")
    private Double confidenceScore;

    @TableField("discovery_method")
    private String discoveryMethod;

    @TableField("last_verified_time")
    private LocalDateTime lastVerifiedTime;

    @TableField("verification_status")
    private String verificationStatus;

    @TableField("impact_scope")
    private String impactScope;

    @TableField("business_context")
    private String businessContext;

    @TableField("technical_context")
    private String technicalContext;

    @TableField("data_quality_impact")
    private String dataQualityImpact;

    @TableField("performance_impact")
    private String performanceImpact;

    @TableField("tags")
    private String tags;

    @TableField("metadata")
    private String metadata;

    @TableField("status")
    private Integer status;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}