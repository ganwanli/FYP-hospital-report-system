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
@TableName("field_usage_log")
public class FieldUsageLog extends Model<FieldUsageLog> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("field_id")
    private Long fieldId;

    @TableField("field_code")
    private String fieldCode;

    @TableField("usage_type")
    private String usageType;

    @TableField("usage_context")
    private String usageContext;

    @TableField("user_id")
    private Long userId;

    @TableField("user_name")
    private String userName;

    @TableField("system_source")
    private String systemSource;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("usage_details")
    private String usageDetails;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}