package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据字典实体类
 * Data Dictionary Entity
 * 
 * 用于管理和维护系统中的数据标准、元数据定义和业务术语
 * Used to manage and maintain data standards, metadata definitions and business terminology in the system
 *
 * @author Hospital Report System
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("data_dictionary")
public class DataDictionary extends Model<DataDictionary> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * Primary Key ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 字段编码
     * Field Code
     */
    @TableField("field_code")
    private String fieldCode;

    /**
     * 字段中文名称
     * Field Name in Chinese
     */
    @TableField("field_name_cn")
    private String fieldNameCn;

    /**
     * 字段英文名称
     * Field Name in English
     */
    @TableField("field_name_en")
    private String fieldNameEn;

    /**
     * 数据类型
     * Data Type
     */
    @TableField("data_type")
    private String dataType;

    /**
     * 数据长度
     * Data Length
     */
    @TableField("data_length")
    private Integer dataLength;

    /**
     * 数据精度
     * Data Precision
     */
    @TableField("data_precision")
    private Integer dataPrecision;

    /**
     * 数据小数位
     * Data Scale
     */
    @TableField("data_scale")
    private Integer dataScale;

    /**
     * 是否可为空
     * Is Nullable
     */
    @TableField("is_nullable")
    private Boolean isNullable;

    /**
     * 默认值
     * Default Value
     */
    @TableField("default_value")
    private String defaultValue;

    /**
     * 业务含义
     * Business Meaning
     */
    @TableField("business_meaning")
    private String businessMeaning;

    /**
     * 数据来源
     * Data Source
     */
    @TableField("data_source")
    private String dataSource;

    /**
     * 更新频率
     * Update Frequency
     */
    @TableField("update_frequency")
    private String updateFrequency;

    /**
     * 责任人
     * Owner User
     */
    @TableField("owner_user")
    private String ownerUser;

    /**
     * 责任部门
     * Owner Department
     */
    @TableField("owner_department")
    private String ownerDepartment;

    /**
     * 分类ID
     * Category ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 分类路径
     * Category Path
     */
    @TableField("category_path")
    private String categoryPath;

    /**
     * 表名
     * Table Name
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 列名
     * Column Name
     */
    @TableField("column_name")
    private String columnName;

    /**
     * 数据质量规则
     * Data Quality Rules
     */
    @TableField("data_quality_rules")
    private String dataQualityRules;

    /**
     * 取值范围
     * Value Range
     */
    @TableField("value_range")
    private String valueRange;

    /**
     * 样例值
     * Sample Values
     */
    @TableField("sample_values")
    private String sampleValues;

    /**
     * 关联字段
     * Related Fields
     */
    @TableField("related_fields")
    private String relatedFields;

    /**
     * 使用次数
     * Usage Count
     */
    @TableField("usage_count")
    private Long usageCount;

    /**
     * 最后使用时间
     * Last Used Time
     */
    @TableField("last_used_time")
    private LocalDateTime lastUsedTime;

    /**
     * 审批状态
     * Approval Status
     */
    @TableField("approval_status")
    private String approvalStatus;

    /**
     * 审批人
     * Approval User
     */
    @TableField("approval_user")
    private String approvalUser;

    /**
     * 审批时间
     * Approval Time
     */
    @TableField("approval_time")
    private LocalDateTime approvalTime;

    /**
     * 版本号
     * Version
     */
    @TableField("version")
    private String version;

    /**
     * 变更日志
     * Change Log
     */
    @TableField("change_log")
    private String changeLog;

    /**
     * 状态（0-禁用，1-启用）
     * Status (0-Disabled, 1-Enabled)
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否标准字段
     * Is Standard Field
     */
    @TableField("is_standard")
    private Boolean isStandard;

    /**
     * 标准参考
     * Standard Reference
     */
    @TableField("standard_reference")
    private String standardReference;

    /**
     * 标签（多个标签用逗号分隔）
     * Tags (Multiple tags separated by commas)
     */
    @TableField("tags")
    private String tags;

    /**
     * 备注
     * Remark
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建人ID
     * Created By ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间（自动填充）
     * Created Time (Auto Fill)
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新人ID
     * Updated By ID
     */
    @TableField("updated_by")
    private Long updatedBy;

    /**
     * 更新时间（自动填充）
     * Updated Time (Auto Fill)
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 是否删除（逻辑删除）
     * Is Deleted (Logical Delete)
     */
    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;

    /**
     * 获取主键值
     * Get Primary Key Value
     *
     * @return 主键ID Primary Key ID
     */
    @Override
    public Serializable pkVal() {
        return this.id;
    }
}