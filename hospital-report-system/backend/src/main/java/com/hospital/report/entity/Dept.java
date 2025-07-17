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
@TableName("sys_dept")
public class Dept extends Model<Dept> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("dept_name")
    private String deptName;

    @TableField("dept_code")
    private String deptCode;

    @TableField("parent_id")
    private Long parentId;

    @TableField("dept_type")
    private String deptType;

    @TableField("leader")
    private String leader;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("address")
    private String address;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private Integer status;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
