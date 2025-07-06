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
@TableName("lineage_node")
public class LineageNode extends Model<LineageNode> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("node_id")
    private String nodeId;

    @TableField("node_type")
    private String nodeType;

    @TableField("node_name")
    private String nodeName;

    @TableField("display_name")
    private String displayName;

    @TableField("node_category")
    private String nodeCategory;

    @TableField("database_name")
    private String databaseName;

    @TableField("schema_name")
    private String schemaName;

    @TableField("table_name")
    private String tableName;

    @TableField("column_name")
    private String columnName;

    @TableField("data_type")
    private String dataType;

    @TableField("business_meaning")
    private String businessMeaning;

    @TableField("technical_description")
    private String technicalDescription;

    @TableField("owner_user")
    private String ownerUser;

    @TableField("owner_department")
    private String ownerDepartment;

    @TableField("system_source")
    private String systemSource;

    @TableField("environment")
    private String environment;

    @TableField("node_properties")
    private String nodeProperties;

    @TableField("position_x")
    private Double positionX;

    @TableField("position_y")
    private Double positionY;

    @TableField("node_level")
    private Integer nodeLevel;

    @TableField("criticality_level")
    private String criticalityLevel;

    @TableField("sensitivity_level")
    private String sensitivityLevel;

    @TableField("data_classification")
    private String dataClassification;

    @TableField("usage_frequency")
    private String usageFrequency;

    @TableField("last_access_time")
    private LocalDateTime lastAccessTime;

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

    @TableField("data_quality_score")
    private Double dataQualityScore;

    @TableField(exist = false)
    private Integer connectionCount;

    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}