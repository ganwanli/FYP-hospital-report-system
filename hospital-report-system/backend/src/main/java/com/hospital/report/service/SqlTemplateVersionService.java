package com.hospital.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.report.entity.SqlTemplateVersion;

import java.util.List;

/**
 * SQL模板版本服务接口
 */
public interface SqlTemplateVersionService extends IService<SqlTemplateVersion> {

    /**
     * 根据模板ID获取所有版本（包含用户信息）
     */
    List<SqlTemplateVersion> getVersionsByTemplateId(Long templateId);

    /**
     * 获取模板的当前版本
     */
    SqlTemplateVersion getCurrentVersion(Long templateId);

    /**
     * 创建新版本
     */
    SqlTemplateVersion createVersion(SqlTemplateVersion version);

    /**
     * 设置当前版本
     */
    boolean setCurrentVersion(Long versionId);

    /**
     * 根据模板ID和版本号获取版本
     */
    SqlTemplateVersion getVersionByTemplateIdAndVersionNumber(Long templateId, String versionNumber);

    /**
     * 获取模板的版本数量
     */
    int getVersionCount(Long templateId);

    /**
     * 生成下一个版本号
     */
    String generateNextVersionNumber(Long templateId);

    /**
     * 删除模板的所有版本
     */
    boolean deleteVersionsByTemplateId(Long templateId);

    /**
     * 检查内容是否已存在
     */
    boolean isContentExists(Long templateId, String templateHash);

    /**
     * 获取最新版本
     */
    SqlTemplateVersion getLatestVersion(Long templateId);

    /**
     * 获取子版本列表
     */
    List<SqlTemplateVersion> getChildVersions(Long parentVersionId);

    /**
     * 更新审批状态
     */
    boolean updateApprovalStatus(Long versionId, String approvalStatus, Long approvedBy);

    /**
     * 获取待审批的版本列表
     */
    List<SqlTemplateVersion> getPendingApprovalVersions();

    /**
     * 获取用户最近的版本
     */
    List<SqlTemplateVersion> getUserRecentVersions(Long userId, Integer limit);

    /**
     * 验证版本内容
     */
    boolean validateVersion(Long versionId, String validationMessage);

    /**
     * 比较两个版本的差异
     */
    String compareVersions(Long fromVersionId, Long toVersionId);

    /**
     * 回滚到指定版本
     */
    boolean rollbackToVersion(Long templateId, Long versionId);

    /**
     * 获取版本历史统计
     */
    Object getVersionStatistics(Long templateId);
}
