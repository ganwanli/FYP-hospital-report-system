package com.hospital.report.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ReportConfigDTO implements Serializable {

    private Long id;

    @NotBlank(message = "报表名称不能为空")
    @Size(max = 100, message = "报表名称长度不能超过100个字符")
    private String reportName;

    @NotBlank(message = "报表编码不能为空")
    @Size(max = 50, message = "报表编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "报表编码只能包含大写字母、数字和下划线")
    private String reportCode;

    @Size(max = 20, message = "报表类型长度不能超过20个字符")
    private String reportType;

    @Positive(message = "报表分类ID必须为正数")
    private Long reportCategoryId;

    @NotNull(message = "SQL模板不能为空")
    @Positive(message = "SQL模板ID必须为正数")
    private Long sqlTemplateId;

    @NotNull(message = "数据源不能为空")
    @Positive(message = "数据源ID必须为正数")
    private Long datasourceId;

    @Size(max = 5000, message = "报表配置长度不能超过5000个字符")
    private String reportConfig;

    @Size(max = 5000, message = "图表配置长度不能超过5000个字符")
    private String chartConfig;

    @Size(max = 2000, message = "导出配置长度不能超过2000个字符")
    private String exportConfig;

    @Min(value = 0, message = "缓存启用状态只能为0或1")
    @Max(value = 1, message = "缓存启用状态只能为0或1")
    private Integer cacheEnabled;

    @Min(value = 0, message = "缓存超时时间不能为负数")
    @Max(value = 86400, message = "缓存超时时间不能超过86400秒(24小时)")
    private Integer cacheTimeout;

    @Min(value = 0, message = "刷新间隔不能为负数")
    @Max(value = 86400, message = "刷新间隔不能超过86400秒(24小时)")
    private Integer refreshInterval;

    @Size(max = 20, message = "访问级别长度不能超过20个字符")
    private String accessLevel;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    @Size(max = 20, message = "版本号长度不能超过20个字符")
    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "版本号格式应为x.y.z")
    private String version;

    @Min(value = 0, message = "查看次数不能为负数")
    private Integer viewCount;

    private LocalDateTime lastViewTime;

    @Min(value = 0, message = "发布状态只能为0或1")
    @Max(value = 1, message = "发布状态只能为0或1")
    private Integer isPublished;

    @Min(value = 0, message = "激活状态只能为0或1")
    @Max(value = 1, message = "激活状态只能为0或1")
    private Integer isActive;

    @Min(value = 0, message = "删除状态只能为0或1")
    @Max(value = 1, message = "删除状态只能为0或1")
    private Integer isDeleted;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    @Positive(message = "创建人ID必须为正数")
    private Long createdBy;

    @Positive(message = "更新人ID必须为正数")
    private Long updatedBy;

    @Min(value = 0, message = "审批状态值不能为负数")
    @Max(value = 3, message = "审批状态值不能超过3")
    private Integer approvalStatus;

    @Size(max = 50, message = "审批人长度不能超过50个字符")
    private String approvedBy;

    private LocalDateTime approvedTime;

    @Size(max = 20, message = "部门编码长度不能超过20个字符")
    @Pattern(regexp = "^[A-Z0-9_]*$", message = "部门编码只能包含大写字母、数字和下划线")
    private String departmentCode;

    @Size(max = 30, message = "业务类型长度不能超过30个字符")
    private String businessType;

    @Size(max = 30, message = "使用类型长度不能超过30个字符")
    private String usageType;

    // 非持久化字段 - 用于显示
    private String createdByName;

    private String updatedByName;

    private String reportCategoryName;

    private String sqlTemplateName;

    private String datasourceName;

    private String approvalStatusName;
}
