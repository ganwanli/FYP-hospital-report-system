package com.hospital.report.controller;

import com.hospital.report.common.Result;
import com.hospital.report.entity.SqlTemplateVersion;
import com.hospital.report.service.SqlTemplateVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SQL模板版本控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/sql-template-versions")
@RequiredArgsConstructor
@Tag(name = "SQL模板版本管理", description = "SQL模板版本相关接口")
public class SqlTemplateVersionController {

    private final SqlTemplateVersionService sqlTemplateVersionService;

    @GetMapping("/template/{templateId}")
    @Operation(summary = "获取模板的所有版本")
    public Result<List<SqlTemplateVersion>> getVersionsByTemplateId(
            @Parameter(description = "模板ID") @PathVariable Long templateId) {
        try {
            List<SqlTemplateVersion> versions = sqlTemplateVersionService.getVersionsByTemplateId(templateId);
            return Result.success(versions);
        } catch (Exception e) {
            log.error("Failed to get versions for template {}: {}", templateId, e.getMessage());
            return Result.error("获取版本列表失败");
        }
    }

    @GetMapping("/template/{templateId}/current")
    @Operation(summary = "获取模板的当前版本")
    public Result<SqlTemplateVersion> getCurrentVersion(
            @Parameter(description = "模板ID") @PathVariable Long templateId) {
        try {
            SqlTemplateVersion currentVersion = sqlTemplateVersionService.getCurrentVersion(templateId);
            if (currentVersion == null) {
                return Result.error("未找到当前版本");
            }
            return Result.success(currentVersion);
        } catch (Exception e) {
            log.error("Failed to get current version for template {}: {}", templateId, e.getMessage());
            return Result.error("获取当前版本失败");
        }
    }

    @PostMapping
    @Operation(summary = "创建新版本")
    public Result<SqlTemplateVersion> createVersion(@RequestBody SqlTemplateVersion version) {
        try {
            SqlTemplateVersion createdVersion = sqlTemplateVersionService.createVersion(version);
            return Result.success(createdVersion);
        } catch (Exception e) {
            log.error("Failed to create version: {}", e.getMessage());
            return Result.error("创建版本失败: " + e.getMessage());
        }
    }

    @PutMapping("/{versionId}/set-current")
    @Operation(summary = "设置为当前版本")
    public Result<Void> setCurrentVersion(
            @Parameter(description = "版本ID") @PathVariable Long versionId) {
        try {
            boolean success = sqlTemplateVersionService.setCurrentVersion(versionId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("设置当前版本失败");
            }
        } catch (Exception e) {
            log.error("Failed to set current version {}: {}", versionId, e.getMessage());
            return Result.error("设置当前版本失败");
        }
    }

    @GetMapping("/template/{templateId}/version/{versionNumber}")
    @Operation(summary = "根据版本号获取版本")
    public Result<SqlTemplateVersion> getVersionByNumber(
            @Parameter(description = "模板ID") @PathVariable Long templateId,
            @Parameter(description = "版本号") @PathVariable String versionNumber) {
        try {
            SqlTemplateVersion version = sqlTemplateVersionService.getVersionByTemplateIdAndVersionNumber(templateId, versionNumber);
            if (version == null) {
                return Result.error("版本不存在");
            }
            return Result.success(version);
        } catch (Exception e) {
            log.error("Failed to get version {} for template {}: {}", versionNumber, templateId, e.getMessage());
            return Result.error("获取版本失败");
        }
    }

    @GetMapping("/template/{templateId}/count")
    @Operation(summary = "获取模板的版本数量")
    public Result<Integer> getVersionCount(
            @Parameter(description = "模板ID") @PathVariable Long templateId) {
        try {
            int count = sqlTemplateVersionService.getVersionCount(templateId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("Failed to get version count for template {}: {}", templateId, e.getMessage());
            return Result.error("获取版本数量失败");
        }
    }

    @GetMapping("/template/{templateId}/next-version-number")
    @Operation(summary = "生成下一个版本号")
    public Result<String> generateNextVersionNumber(
            @Parameter(description = "模板ID") @PathVariable Long templateId) {
        try {
            String nextVersion = sqlTemplateVersionService.generateNextVersionNumber(templateId);
            return Result.success(nextVersion);
        } catch (Exception e) {
            log.error("Failed to generate next version number for template {}: {}", templateId, e.getMessage());
            return Result.error("生成版本号失败");
        }
    }

    @DeleteMapping("/template/{templateId}")
    @Operation(summary = "删除模板的所有版本")
    public Result<Void> deleteVersionsByTemplateId(
            @Parameter(description = "模板ID") @PathVariable Long templateId) {
        try {
            boolean success = sqlTemplateVersionService.deleteVersionsByTemplateId(templateId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除版本失败");
            }
        } catch (Exception e) {
            log.error("Failed to delete versions for template {}: {}", templateId, e.getMessage());
            return Result.error("删除版本失败");
        }
    }

    @GetMapping("/template/{templateId}/latest")
    @Operation(summary = "获取最新版本")
    public Result<SqlTemplateVersion> getLatestVersion(
            @Parameter(description = "模板ID") @PathVariable Long templateId) {
        try {
            SqlTemplateVersion latestVersion = sqlTemplateVersionService.getLatestVersion(templateId);
            if (latestVersion == null) {
                return Result.error("未找到版本");
            }
            return Result.success(latestVersion);
        } catch (Exception e) {
            log.error("Failed to get latest version for template {}: {}", templateId, e.getMessage());
            return Result.error("获取最新版本失败");
        }
    }

    @PutMapping("/{versionId}/approval")
    @Operation(summary = "更新审批状态")
    public Result<Void> updateApprovalStatus(
            @Parameter(description = "版本ID") @PathVariable Long versionId,
            @Parameter(description = "审批状态") @RequestParam String approvalStatus,
            @Parameter(description = "审批人ID") @RequestParam Long approvedBy) {
        try {
            boolean success = sqlTemplateVersionService.updateApprovalStatus(versionId, approvalStatus, approvedBy);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新审批状态失败");
            }
        } catch (Exception e) {
            log.error("Failed to update approval status for version {}: {}", versionId, e.getMessage());
            return Result.error("更新审批状态失败");
        }
    }

    @GetMapping("/pending-approval")
    @Operation(summary = "获取待审批的版本列表")
    public Result<List<SqlTemplateVersion>> getPendingApprovalVersions() {
        try {
            List<SqlTemplateVersion> versions = sqlTemplateVersionService.getPendingApprovalVersions();
            return Result.success(versions);
        } catch (Exception e) {
            log.error("Failed to get pending approval versions: {}", e.getMessage());
            return Result.error("获取待审批版本失败");
        }
    }

    @GetMapping("/user/{userId}/recent")
    @Operation(summary = "获取用户最近的版本")
    public Result<List<SqlTemplateVersion>> getUserRecentVersions(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<SqlTemplateVersion> versions = sqlTemplateVersionService.getUserRecentVersions(userId, limit);
            return Result.success(versions);
        } catch (Exception e) {
            log.error("Failed to get recent versions for user {}: {}", userId, e.getMessage());
            return Result.error("获取用户最近版本失败");
        }
    }

    @PutMapping("/{versionId}/validate")
    @Operation(summary = "验证版本")
    public Result<Void> validateVersion(
            @Parameter(description = "版本ID") @PathVariable Long versionId,
            @Parameter(description = "验证消息") @RequestParam(required = false) String validationMessage) {
        try {
            boolean success = sqlTemplateVersionService.validateVersion(versionId, validationMessage);
            if (success) {
                return Result.success();
            } else {
                return Result.error("验证版本失败");
            }
        } catch (Exception e) {
            log.error("Failed to validate version {}: {}", versionId, e.getMessage());
            return Result.error("验证版本失败");
        }
    }

    @PostMapping("/template/{templateId}/rollback/{versionId}")
    @Operation(summary = "回滚到指定版本")
    public Result<Void> rollbackToVersion(
            @Parameter(description = "模板ID") @PathVariable Long templateId,
            @Parameter(description = "版本ID") @PathVariable Long versionId) {
        try {
            boolean success = sqlTemplateVersionService.rollbackToVersion(templateId, versionId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("回滚版本失败");
            }
        } catch (Exception e) {
            log.error("Failed to rollback template {} to version {}: {}", templateId, versionId, e.getMessage());
            return Result.error("回滚版本失败");
        }
    }

    @GetMapping("/template/{templateId}/statistics")
    @Operation(summary = "获取版本统计信息")
    public Result<Object> getVersionStatistics(
            @Parameter(description = "模板ID") @PathVariable Long templateId) {
        try {
            Object statistics = sqlTemplateVersionService.getVersionStatistics(templateId);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("Failed to get version statistics for template {}: {}", templateId, e.getMessage());
            return Result.error("获取版本统计失败");
        }
    }
}
