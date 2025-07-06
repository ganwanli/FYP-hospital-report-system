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
@TableName("data_dictionary")
public class DataDictionary extends Model<DataDictionary> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("field_code")
    private String fieldCode;

    @TableField("field_name_cn")
    private String fieldNameCn;

    @TableField("field_name_en")
    private String fieldNameEn;

    @TableField("data_type")
    private String dataType;

    @TableField("data_length")
    private Integer dataLength;

    @TableField("data_precision")
    private Integer dataPrecision;

    @TableField("data_scale")
    private Integer dataScale;

    @TableField("is_nullable")
    private Boolean isNullable;

    @TableField("default_value")
    private String defaultValue;

    @TableField("business_meaning")
    private String businessMeaning;

    @TableField("data_source")
    private String dataSource;

    @TableField("update_frequency")
    private String updateFrequency;

    @TableField("owner_user")
    private String ownerUser;

    @TableField("owner_department")
    private String ownerDepartment;

    @TableField("category_id")
    private Long categoryId;

    @TableField("category_path")
    private String categoryPath;

    @TableField("table_name")
    private String tableName;

    @TableField("column_name")
    private String columnName;

    @TableField("data_quality_rules")
    private String dataQualityRules;

    @TableField("value_range")
    private String valueRange;

    @TableField("sample_values")
    private String sampleValues;

    @TableField("related_fields")
    private String relatedFields;

    @TableField("usage_count")
    private Long usageCount;

    @TableField("last_used_time")
    private LocalDateTime lastUsedTime;

    @TableField("approval_status")
    private String approvalStatus;

    @TableField("approval_user")
    private String approvalUser;

    @TableField("approval_time")
    private LocalDateTime approvalTime;

    @TableField("version")
    private String version;

    @TableField("change_log")
    private String changeLog;

    @TableField("status")
    private Integer status;

    @TableField("is_standard")
    private Boolean isStandard;

    @TableField("standard_reference")
    private String standardReference;

    @TableField("tags")
    private String tags;

    @TableField("remark")
    private String remark;

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