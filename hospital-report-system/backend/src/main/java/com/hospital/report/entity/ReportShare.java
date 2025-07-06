package com.hospital.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 报表分享实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("report_share")
public class ReportShare {
    
    @TableId(value = "share_id", type = IdType.AUTO)
    private Long shareId;
    
    /**
     * 报表ID
     */
    @TableField("report_id")
    private Long reportId;
    
    /**
     * 分享码（唯一标识）
     */
    @TableField("share_code")
    private String shareCode;
    
    /**
     * 分享标题
     */
    @TableField("share_title")
    private String shareTitle;
    
    /**
     * 分享描述
     */
    @TableField("share_description")
    private String shareDescription;
    
    /**
     * 访问密码
     */
    @TableField("access_password")
    private String accessPassword;
    
    /**
     * 分享类型：PUBLIC-公开，PRIVATE-私有，PASSWORD-密码访问
     */
    @TableField("share_type")
    private String shareType;
    
    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;
    
    /**
     * 最大访问次数
     */
    @TableField("max_access_count")
    private Integer maxAccessCount;
    
    /**
     * 当前访问次数
     */
    @TableField("current_access_count")
    private Integer currentAccessCount;
    
    /**
     * 允许导出
     */
    @TableField("allow_export")
    private Boolean allowExport;
    
    /**
     * 允许的导出格式（JSON数组字符串）
     */
    @TableField("allowed_formats")
    private String allowedFormats;
    
    /**
     * 参数配置（JSON字符串）
     */
    @TableField("parameters_config")
    private String parametersConfig;
    
    /**
     * 权限配置（JSON字符串）
     */
    @TableField("permissions_config")
    private String permissionsConfig;
    
    /**
     * 是否激活
     */
    @TableField("is_active")
    private Boolean isActive;
    
    /**
     * 创建者ID
     */
    @TableField("created_by")
    private Long createdBy;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    /**
     * 更新者ID
     */
    @TableField("updated_by")
    private Long updatedBy;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    /**
     * 创建者姓名（非数据库字段）
     */
    @TableField(exist = false)
    private String createdByName;
    
    /**
     * 报表名称（非数据库字段）
     */
    @TableField(exist = false)
    private String reportName;
}