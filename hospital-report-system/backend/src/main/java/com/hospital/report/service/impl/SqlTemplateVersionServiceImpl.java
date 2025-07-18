package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.report.entity.SqlTemplateVersion;
import com.hospital.report.mapper.SqlTemplateVersionMapper;
import com.hospital.report.service.SqlTemplateVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL模板版本服务实现类
 */
@Slf4j
@Service
public class SqlTemplateVersionServiceImpl extends ServiceImpl<SqlTemplateVersionMapper, SqlTemplateVersion> 
        implements SqlTemplateVersionService {

    @Override
    public List<SqlTemplateVersion> getVersionsByTemplateId(Long templateId) {
        return baseMapper.selectByTemplateIdWithUserInfo(templateId);
    }

    @Override
    public SqlTemplateVersion getCurrentVersion(Long templateId) {
        return baseMapper.selectCurrentVersion(templateId);
    }

    @Override
    @Transactional
    public SqlTemplateVersion createVersion(SqlTemplateVersion version) {
        try {
            // 生成内容哈希
            if (version.getTemplateContent() != null) {
                String hash = DigestUtils.md5DigestAsHex(version.getTemplateContent().getBytes());
                version.setTemplateHash(hash);
            }

            // 设置创建时间
            version.setCreatedTime(LocalDateTime.now());

            // 如果没有指定版本号，自动生成
            if (version.getVersionNumber() == null || version.getVersionNumber().isEmpty()) {
                version.setVersionNumber(generateNextVersionNumber(version.getTemplateId()));
            }

            // 如果设置为当前版本，先清除其他当前版本
            if (Boolean.TRUE.equals(version.getIsCurrent())) {
                baseMapper.clearCurrentVersion(version.getTemplateId());
            }

            // 保存版本（使用自定义insert方法确保ID回填）
            int insertResult = baseMapper.insertVersion(version);
            if (insertResult <= 0) {
                throw new RuntimeException("Failed to insert version");
            }
            
            log.info("Created new version {} for template {}", version.getVersionNumber(), version.getTemplateId());
            return version;
        } catch (Exception e) {
            log.error("Failed to create version for template {}: {}", version.getTemplateId(), e.getMessage());
            throw new RuntimeException("创建版本失败", e);
        }
    }

    @Override
    @Transactional
    public boolean setCurrentVersion(Long versionId) {
        try {
            SqlTemplateVersion version = getById(versionId);
            if (version == null) {
                return false;
            }

            // 清除当前版本标记
            baseMapper.clearCurrentVersion(version.getTemplateId());
            
            // 设置新的当前版本
            int result = baseMapper.setCurrentVersion(versionId);
            
            log.info("Set version {} as current for template {}", versionId, version.getTemplateId());
            return result > 0;
        } catch (Exception e) {
            log.error("Failed to set current version {}: {}", versionId, e.getMessage());
            return false;
        }
    }

    @Override
    public SqlTemplateVersion getVersionByTemplateIdAndVersionNumber(Long templateId, String versionNumber) {
        return baseMapper.selectByTemplateIdAndVersionNumber(templateId, versionNumber);
    }

    @Override
    public int getVersionCount(Long templateId) {
        return baseMapper.countByTemplateId(templateId);
    }

    @Override
    public String generateNextVersionNumber(Long templateId) {
        Integer maxVersion = baseMapper.selectMaxVersionNumber(templateId);
        if (maxVersion == null) {
            return "v1.0";
        }
        return "v" + (maxVersion + 1) + ".0";
    }

    @Override
    @Transactional
    public boolean deleteVersionsByTemplateId(Long templateId) {
        try {
            int result = baseMapper.deleteByTemplateId(templateId);
            log.info("Deleted {} versions for template {}", result, templateId);
            return result > 0;
        } catch (Exception e) {
            log.error("Failed to delete versions for template {}: {}", templateId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isContentExists(Long templateId, String templateHash) {
        SqlTemplateVersion existing = baseMapper.selectByTemplateHashAndTemplateId(templateHash, templateId);
        return existing != null;
    }

    @Override
    public SqlTemplateVersion getLatestVersion(Long templateId) {
        return baseMapper.selectLatestVersion(templateId);
    }

    @Override
    public List<SqlTemplateVersion> getChildVersions(Long parentVersionId) {
        return baseMapper.selectChildVersions(parentVersionId);
    }

    @Override
    @Transactional
    public boolean updateApprovalStatus(Long versionId, String approvalStatus, Long approvedBy) {
        try {
            int result = baseMapper.updateApprovalStatus(versionId, approvalStatus, approvedBy);
            log.info("Updated approval status for version {} to {}", versionId, approvalStatus);
            return result > 0;
        } catch (Exception e) {
            log.error("Failed to update approval status for version {}: {}", versionId, e.getMessage());
            return false;
        }
    }

    @Override
    public List<SqlTemplateVersion> getPendingApprovalVersions() {
        return baseMapper.selectPendingApprovalVersions();
    }

    @Override
    public List<SqlTemplateVersion> getUserRecentVersions(Long userId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return baseMapper.selectUserRecentVersions(userId, limit);
    }

    @Override
    @Transactional
    public boolean validateVersion(Long versionId, String validationMessage) {
        try {
            SqlTemplateVersion version = getById(versionId);
            if (version == null) {
                return false;
            }

            version.setValidationStatus("VALID");
            version.setValidationMessage(validationMessage);
            
            boolean result = updateById(version);
            log.info("Validated version {}", versionId);
            return result;
        } catch (Exception e) {
            log.error("Failed to validate version {}: {}", versionId, e.getMessage());
            return false;
        }
    }

    @Override
    public String compareVersions(Long fromVersionId, Long toVersionId) {
        // 这里可以实现版本比较逻辑
        // 简单实现，返回基本信息
        SqlTemplateVersion fromVersion = getById(fromVersionId);
        SqlTemplateVersion toVersion = getById(toVersionId);
        
        if (fromVersion == null || toVersion == null) {
            return "版本不存在";
        }
        
        return String.format("从版本 %s 到版本 %s 的变更", 
                fromVersion.getVersionNumber(), toVersion.getVersionNumber());
    }

    @Override
    @Transactional
    public boolean rollbackToVersion(Long templateId, Long versionId) {
        try {
            // 清除当前版本标记
            baseMapper.clearCurrentVersion(templateId);
            
            // 设置指定版本为当前版本
            int result = baseMapper.setCurrentVersion(versionId);
            
            log.info("Rolled back template {} to version {}", templateId, versionId);
            return result > 0;
        } catch (Exception e) {
            log.error("Failed to rollback template {} to version {}: {}", templateId, versionId, e.getMessage());
            return false;
        }
    }

    @Override
    public Object getVersionStatistics(Long templateId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总版本数
        int totalVersions = getVersionCount(templateId);
        stats.put("totalVersions", totalVersions);
        
        // 当前版本
        SqlTemplateVersion currentVersion = getCurrentVersion(templateId);
        stats.put("currentVersion", currentVersion != null ? currentVersion.getVersionNumber() : null);
        
        // 最新版本
        SqlTemplateVersion latestVersion = getLatestVersion(templateId);
        stats.put("latestVersion", latestVersion != null ? latestVersion.getVersionNumber() : null);
        
        return stats;
    }
}
