package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据字段字典实体类
 * 对应数据库表：dict_field
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("dict_field")
public class DictField implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字段ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 字段编码（唯一）
     */
    @TableField("field_code")
    private String fieldCode;

    /**
     * 字段名称
     */
    @TableField("field_name")
    private String fieldName;

    /**
     * 英文名称
     */
    @TableField("field_name_en")
    private String fieldNameEn;

    /**
     * 所属分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 字段描述/业务含义
     */
    @TableField("description")
    private String description;

    /**
     * 数据类型（VARCHAR、INT、DECIMAL等）
     */
    @TableField("data_type")
    private String dataType;

    /**
     * 数据长度（如：100 或 10,2）
     */
    @TableField("data_length")
    private String dataLength;

    /**
     * 源数据库名
     */
    @TableField("source_database")
    private String sourceDatabase;

    /**
     * 源数据表名
     */
    @TableField("source_table")
    private String sourceTable;

    /**
     * 源字段名或表达式
     */
    @TableField("source_field")
    private String sourceField;

    /**
     * 筛选条件
     */
    @TableField("filter_condition")
    private String filterCondition;

    /**
     * 完整的SQL查询语句
     */
    @TableField("calculation_sql")
    private String calculationSql;

    /**
     * 更新频率（实时、每日、每月等）
     */
    @TableField("update_frequency")
    private String updateFrequency;

    /**
     * 数据负责人
     */
    @TableField("data_owner")
    private String dataOwner;

    /**
     * 备注说明
     */
    @TableField("remark")
    private String remark;

    /**
     * 状态：1-正常，0-停用
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 更新人
     */
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 是否公开：0-私有，1-公开
     */
    @TableField("is_public")
    private Boolean isPublic;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableField("is_deleted")
    @TableLogic
    private int isDeleted;

    /**
     * 版本号，用于乐观锁
     */
    @TableField("version")
    @Version
    private Integer version;

    /**
     * 字段类型（例如 field/category）
     */
    @TableField("field_type")
    private String fieldType;

    /**
     * 节点类型（field/category）
     */
    @TableField(value = "node_type", exist = false)
    private String nodeType;

    // 扩展字段（不存储在数据库中）
    /**
     * 创建人姓名
     */
    @TableField(exist = false)
    private String createdByName;

    /**
     * 更新人姓名
     */
    @TableField(exist = false)
    private String updatedByName;

    /**
     * 子字段列表（用于树形结构）
     */
    @TableField(exist = false)
    private java.util.List<DictField> children;
}
