package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典项实体类
 * Dictionary Item Entity
 * 
 * @author Hospital Report System
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("dict_item")
public class DictItem extends Model<DictItem> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 字典类型
     */
    @TableField("dict_type")
    private String dictType;

    /**
     * 字典值
     */
    @TableField("dict_value")
    private String dictValue;

    /**
     * 字典标签
     */
    @TableField("dict_label")
    private String dictLabel;

    /**
     * 字典编码
     */
    @TableField("dict_code")
    private String dictCode;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态（1启用 0禁用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 是否删除
     */
    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
