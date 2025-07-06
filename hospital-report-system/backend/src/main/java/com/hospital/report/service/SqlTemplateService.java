package com.hospital.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.entity.SqlTemplate;
import com.hospital.report.entity.SqlTemplateParameter;

import java.util.List;
import java.util.Map;

public interface SqlTemplateService {

    SqlTemplate createTemplate(SqlTemplate template);

    SqlTemplate updateTemplate(SqlTemplate template);

    void deleteTemplate(Long templateId);

    SqlTemplate getTemplateById(Long templateId);

    IPage<SqlTemplate> getTemplateList(Page<SqlTemplate> page, String templateName, String templateCategory, 
                                       Boolean isActive, Boolean isPublic, Long createdBy, String tags, 
                                       String databaseType, String approvalStatus);

    List<SqlTemplate> searchTemplates(String keyword);

    List<SqlTemplate> getPopularTemplates(Integer limit);

    List<SqlTemplate> getUserRecentTemplates(Long userId, Integer limit);

    List<String> getAllCategories();

    List<String> getAllDatabaseTypes();

    List<String> getAllTags();

    Map<String, Object> getTemplateStatistics();

    List<Map<String, Object>> getCategoryStatistics();

    List<Map<String, Object>> getDatabaseTypeStatistics();

    List<Map<String, Object>> getMonthlyCreationStatistics();

    void updateUsageCount(Long templateId);

    void approveTemplate(Long templateId, Long approvedBy);

    void rejectTemplate(Long templateId, Long approvedBy);

    List<SqlTemplate> getPendingApprovalTemplates();

    String validateTemplate(String templateContent, String databaseType);

    List<SqlTemplateParameter> extractParameters(String templateContent);

    String generateTemplateHash(String templateContent);

    SqlTemplate duplicateTemplate(Long templateId, String newName);

    void exportTemplate(Long templateId, String format);

    SqlTemplate importTemplate(String templateData, String format);

    boolean isTemplateNameExists(String templateName, Long excludeId);

    boolean isTemplateContentExists(String templateContent, Long excludeId);

    List<SqlTemplate> getTemplatesByCategory(String category);

    List<SqlTemplate> getTemplatesByDatabaseType(String databaseType);

    void bulkUpdateTemplateStatus(List<Long> templateIds, Boolean isActive);

    void bulkDeleteTemplates(List<Long> templateIds);

    Map<String, Object> getTemplateUsageStatistics(Long templateId);

    List<Map<String, Object>> getTemplateUsageHistory(Long templateId, Integer days);
}