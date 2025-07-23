package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.entity.SqlTemplate;
import com.hospital.report.entity.SqlTemplateParameter;
import com.hospital.report.entity.SqlTemplateVersion;
import com.hospital.report.mapper.SqlTemplateMapper;
import com.hospital.report.mapper.SqlTemplateParameterMapper;
import com.hospital.report.mapper.SqlTemplateVersionMapper;
import com.hospital.report.mapper.SqlTemplateUsageLogMapper;
import com.hospital.report.service.SqlTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqlTemplateServiceImpl implements SqlTemplateService {

    private final SqlTemplateMapper sqlTemplateMapper;
    private final SqlTemplateParameterMapper parameterMapper;
    private final SqlTemplateVersionMapper versionMapper;
    private final SqlTemplateUsageLogMapper usageLogMapper;

    private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile("--.*$|/\\*.*?\\*/", Pattern.MULTILINE | Pattern.DOTALL);

    @Override
    @Transactional
    public SqlTemplate createTemplate(SqlTemplate template) {
        try {
            // 设置基本属性
            template.setCreatedTime(LocalDateTime.now());
            template.setUpdatedTime(LocalDateTime.now());
            template.setTemplateVersion("v1.0");
            template.setUsageCount(0);
            template.setIsActive(true);
            template.setApprovalStatus("PENDING");

            // 生成内容哈希
            if (template.getTemplateContent() != null) {
                template.setTemplateHash(generateTemplateHash(template.getTemplateContent()));
            }

            // 插入模板到数据库（使用自定义insert方法确保ID回填）
            int insertResult = sqlTemplateMapper.insertTemplate(template);
            if (insertResult <= 0) {
                throw new RuntimeException("Failed to insert template");
            }

            // 验证ID是否正确回填
            if (template.getTemplateId() == null) {
                throw new RuntimeException("Template ID was not generated properly");
            }

            log.info("Template created with ID: {}", template.getTemplateId());

            // 保存参数（如果有）
            if (template.getParameters() != null && !template.getParameters().isEmpty()) {
                saveParameters(template.getTemplateId(), template.getParameters());
                log.info("Saved {} parameters for template {}", template.getParameters().size(), template.getTemplateId());
            }

            // 创建初始版本
            createInitialVersion(template);
            log.info("Created initial version for template {}", template.getTemplateId());

            return template;

        } catch (Exception e) {
            log.error("Failed to create template: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create template: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public SqlTemplate updateTemplate(SqlTemplate template) {
        SqlTemplate existingTemplate = sqlTemplateMapper.selectByIdWithUserInfo(template.getTemplateId());
        if (existingTemplate == null) {
            throw new RuntimeException("Template not found");
        }
        
        template.setUpdatedTime(LocalDateTime.now());
        String newHash = generateTemplateHash(template.getTemplateContent());

        Integer maxVersion = versionMapper.selectMaxVersionNumber(template.getTemplateId());
        String newVersionNumber = "v" + (maxVersion != null ? maxVersion + 1 : 1) + ".0";
        template.setTemplateVersion(newVersionNumber);

        if (template.getTemplateContent() != null) {
            if (!newHash.equals(existingTemplate.getTemplateHash())) {
                template.setTemplateHash(newHash);
                template.setApprovalStatus("PENDING");
                createNewVersion(template, existingTemplate);
            }
        }
        
        sqlTemplateMapper.updateTemplateToNewVersion(template);
        
        if (template.getParameters() != null) {
            parameterMapper.deleteByTemplateId(template.getTemplateId());
            if (!template.getParameters().isEmpty()) {
                saveParameters(template.getTemplateId(), template.getParameters());
            }
        }
        
        return getTemplateById(template.getTemplateId());
    }

    @Override
    @Transactional
    public void deleteTemplate(Long templateId) {
        parameterMapper.deleteByTemplateId(templateId);
        versionMapper.deleteByTemplateId(templateId);
        //暂时删除此功能，此表不存在
//        usageLogMapper.deleteByTemplateId(templateId);
        sqlTemplateMapper.deleteSqlTemplateById(templateId);
    }

    @Override
    public SqlTemplate getTemplateById(Long templateId) {
        SqlTemplate template = sqlTemplateMapper.selectByIdWithUserInfo(templateId);
        if (template != null) {
            template.setParameters(parameterMapper.selectByTemplateId(templateId));
            template.setVersions(versionMapper.selectByTemplateIdWithUserInfo(templateId));
        }
        return template;
    }

    @Override
    public IPage<SqlTemplate> getTemplateList(Page<SqlTemplate> page, String templateName, String templateCategory,
                                              Boolean isActive, Boolean isPublic, Long createdBy, String tags,
                                              String databaseType, String approvalStatus) {
        try {
            log.info("Getting template list with page: " + page);
            log.info("Getting template list with parameters: " + templateName + ", " + templateCategory + ", " + isActive + ", " + isPublic + ", " + createdBy + ", " + tags + ", " + databaseType + ", " + approvalStatus);
            IPage<SqlTemplate> result = sqlTemplateMapper.selectTemplateList(page, templateName, templateCategory,
                    isActive, isPublic, createdBy, tags,
                    databaseType, approvalStatus);
            log.info("Template list retrieved successfully: " + result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get template list: " + e.getMessage(), e);
            return new Page<>();
        }
    }

    @Override
    public List<SqlTemplate> searchTemplates(String keyword) {
        return sqlTemplateMapper.searchTemplates(keyword);
    }

    @Override
    public List<SqlTemplate> getPopularTemplates(Integer limit) {
        return sqlTemplateMapper.selectPopularTemplates(limit);
    }

    @Override
    public List<SqlTemplate> getUserRecentTemplates(Long userId, Integer limit) {
        return sqlTemplateMapper.selectUserRecentTemplates(userId, limit);
    }

    @Override
    public List<String> getAllCategories() {
        return sqlTemplateMapper.selectAllCategories();
    }

    @Override
    public List<String> getAllDatabaseTypes() {
        return sqlTemplateMapper.selectAllDatabaseTypes();
    }

    @Override
    public List<String> getAllTags() {
        List<String> allTags = sqlTemplateMapper.selectAllTags();
        Set<String> uniqueTags = new HashSet<>();
        for (String tagStr : allTags) {
            if (StringUtils.hasText(tagStr)) {
                String[] tags = tagStr.split(",");
                for (String tag : tags) {
                    uniqueTags.add(tag.trim());
                }
            }
        }
        return new ArrayList<>(uniqueTags);
    }

    @Override
    public Map<String, Object> getTemplateStatistics() {
        return sqlTemplateMapper.selectOverallStatistics();
    }

    @Override
    public List<Map<String, Object>> getCategoryStatistics() {
        return sqlTemplateMapper.selectCategoryStatistics();
    }

    @Override
    public List<Map<String, Object>> getDatabaseTypeStatistics() {
        return sqlTemplateMapper.selectDatabaseTypeStatistics();
    }

    @Override
    public List<Map<String, Object>> getMonthlyCreationStatistics() {
        return sqlTemplateMapper.selectMonthlyCreationStatistics();
    }

    @Override
    public void updateUsageCount(Long templateId) {
        sqlTemplateMapper.updateUsageCount(templateId);
    }

    @Override
    public void approveTemplate(Long templateId, Long approvedBy) {
        sqlTemplateMapper.updateApprovalStatus(templateId, "APPROVED", approvedBy);
    }

    @Override
    public void rejectTemplate(Long templateId, Long approvedBy) {
        sqlTemplateMapper.updateApprovalStatus(templateId, "REJECTED", approvedBy);
    }

    @Override
    public List<SqlTemplate> getPendingApprovalTemplates() {
        return sqlTemplateMapper.selectPendingApprovalTemplates();
    }

    @Override
    public String validateTemplate(String templateContent, String databaseType) {
        if (!StringUtils.hasText(templateContent)) {
            return "SQL template content cannot be empty";
        }
        
        String cleanSql = SQL_COMMENT_PATTERN.matcher(templateContent).replaceAll("");
        
        if (!cleanSql.trim().toLowerCase().matches("^(select|insert|update|delete|with|call|exec).*")) {
            return "SQL template must start with a valid SQL statement";
        }
        
        if (cleanSql.toLowerCase().contains("drop ") || 
            cleanSql.toLowerCase().contains("truncate ") ||
            cleanSql.toLowerCase().contains("alter ")) {
            return "DDL statements are not allowed in templates";
        }
        
        return null;
    }

    @Override
    public List<SqlTemplateParameter> extractParameters(String templateContent) {
        List<SqlTemplateParameter> parameters = new ArrayList<>();
        if (!StringUtils.hasText(templateContent)) {
            return parameters;
        }
        
        Matcher matcher = PARAMETER_PATTERN.matcher(templateContent);
        Set<String> paramNames = new HashSet<>();
        int order = 1;
        
        while (matcher.find()) {
            String paramName = matcher.group(1);
            if (!paramNames.contains(paramName)) {
                paramNames.add(paramName);
                
                SqlTemplateParameter param = new SqlTemplateParameter();
                param.setParameterName(paramName);
                param.setParameterType(guessParameterType(paramName));
                param.setIsRequired(true);
                param.setParameterOrder(order++);
                param.setInputType("text");
                param.setCreatedTime(LocalDateTime.now());
                param.setUpdatedTime(LocalDateTime.now());
                param.setIsSensitive(false);
                
                parameters.add(param);
            }
        }
        
        return parameters;
    }

    @Override
    public String generateTemplateHash(String templateContent) {
        if (!StringUtils.hasText(templateContent)) {
            return "";
        }
        
        String normalized = templateContent.trim().toLowerCase()
                .replaceAll("\\s+", " ")
                .replaceAll("--.*$", "")
                .replaceAll("/\\*.*?\\*/", "");
        
        return DigestUtils.md5DigestAsHex(normalized.getBytes());
    }

    @Override
    @Transactional
    public SqlTemplate duplicateTemplate(Long templateId, String newName) {
        SqlTemplate original = getTemplateById(templateId);
        if (original == null) {
            throw new RuntimeException("Template not found");
        }
        
        SqlTemplate duplicate = new SqlTemplate();
        duplicate.setTemplateName(newName);
        duplicate.setTemplateDescription(original.getTemplateDescription() + " (Copy)");
        duplicate.setTemplateCategory(original.getTemplateCategory());
        duplicate.setTemplateContent(original.getTemplateContent());
        duplicate.setTemplateVersion("v1.0");
        duplicate.setIsActive(true);
        duplicate.setIsPublic(false);
        duplicate.setTags(original.getTags());
        duplicate.setDatasourceId(original.getDatasourceId()); // 复制数据源ID
        duplicate.setDatabaseType(original.getDatabaseType());
        duplicate.setExecutionTimeout(original.getExecutionTimeout());
        duplicate.setMaxRows(original.getMaxRows());
        duplicate.setParameters(original.getParameters());
        
        return createTemplate(duplicate);
    }

    @Override
    public void exportTemplate(Long templateId, String format) {
        // TODO: Implement export functionality
        throw new UnsupportedOperationException("Export functionality not yet implemented");
    }

    @Override
    public SqlTemplate importTemplate(String templateData, String format) {
        // TODO: Implement import functionality
        throw new UnsupportedOperationException("Import functionality not yet implemented");
    }

    @Override
    public boolean isTemplateNameExists(String templateName, Long excludeId) {
        List<SqlTemplate> templates = sqlTemplateMapper.selectByTemplateNameExcludeId(templateName, excludeId);
        return !templates.isEmpty();
    }

    @Override
    public boolean isTemplateContentExists(String templateContent, Long excludeId) {
        String hash = generateTemplateHash(templateContent);
        List<SqlTemplate> templates = sqlTemplateMapper.selectByTemplateHashExcludeId(hash, excludeId);
        return !templates.isEmpty();
    }

    @Override
    public List<SqlTemplate> getTemplatesByCategory(String category) {
        QueryWrapper<SqlTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("template_category", category)
                   .eq("is_active", true)
                   .orderByDesc("updated_time");
        return sqlTemplateMapper.selectList(queryWrapper);
    }

    @Override
    public List<SqlTemplate> getTemplatesByDatabaseType(String databaseType) {
        QueryWrapper<SqlTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("database_type", databaseType)
                   .eq("is_active", true)
                   .orderByDesc("updated_time");
        return sqlTemplateMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void bulkUpdateTemplateStatus(List<Long> templateIds, Boolean isActive) {
        if (templateIds == null || templateIds.isEmpty()) {
            return;
        }
        
        for (Long templateId : templateIds) {
            SqlTemplate template = new SqlTemplate();
            template.setTemplateId(templateId);
            template.setIsActive(isActive);
            template.setUpdatedTime(LocalDateTime.now());
            sqlTemplateMapper.updateById(template);
        }
    }

    @Override
    @Transactional
    public void bulkDeleteTemplates(List<Long> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) {
            return;
        }
        
        for (Long templateId : templateIds) {
            deleteTemplate(templateId);
        }
    }

    @Override
    public Map<String, Object> getTemplateUsageStatistics(Long templateId) {
        return usageLogMapper.selectTemplateUsageStatistics(templateId);
    }

    @Override
    public List<Map<String, Object>> getTemplateUsageHistory(Long templateId, Integer days) {
        return usageLogMapper.selectDailyUsageStatistics(templateId);
    }

    private void saveParameters(Long templateId, List<SqlTemplateParameter> parameters) {
        for (SqlTemplateParameter param : parameters) {
            param.setTemplateId(templateId);
            param.setCreatedTime(LocalDateTime.now());
            param.setUpdatedTime(LocalDateTime.now());
            parameterMapper.insert(param);
        }
    }

    private void createInitialVersion(SqlTemplate template) {
        SqlTemplateVersion version = new SqlTemplateVersion();
        version.setTemplateId(template.getTemplateId());
        version.setVersionNumber("v1.0");
        version.setVersionDescription("Initial version");
        version.setTemplateContent(template.getTemplateContent());
        version.setDataSourceId(template.getDatasourceId()); // 设置数据源ID
        version.setChangeLog("Initial template creation");
        version.setIsCurrent(true);
        version.setCreatedBy(template.getCreatedBy());
        version.setCreatedTime(LocalDateTime.now());
        version.setTemplateHash(template.getTemplateHash());
        version.setValidationStatus("PENDING");
        version.setApprovalStatus("PENDING");
        
        int insertResult = versionMapper.insertVersion(version);
        if (insertResult <= 0) {
            throw new RuntimeException("Failed to create initial version");
        }

        log.info("Created initial version {} for template {}", version.getVersionNumber(), template.getTemplateId());
    }

    private void createNewVersion(SqlTemplate template, SqlTemplate existingTemplate) {

        versionMapper.clearCurrentVersion(template.getTemplateId());
        
        SqlTemplateVersion version = new SqlTemplateVersion();
        version.setTemplateId(template.getTemplateId());
        version.setVersionNumber(template.getTemplateVersion());
        version.setVersionDescription("Template updated");
        version.setTemplateContent(template.getTemplateContent());
        version.setChangeLog(template.getChangeLog());
        version.setIsCurrent(true);
        version.setCreatedBy(template.getUpdatedBy());
        version.setCreatedTime(LocalDateTime.now());
        version.setTemplateHash(template.getTemplateHash());
        version.setValidationStatus("PENDING");
        version.setApprovalStatus("PENDING");
        version.setModificationNote(template.getModificationNote());
        version.setDataSourceId(template.getDatasourceId()); // 设置数据源ID
        
        SqlTemplateVersion currentVersion = versionMapper.selectCurrentVersion(template.getTemplateId());
        if (currentVersion != null) {
            version.setParentVersionId(currentVersion.getVersionId());
        }

        int insertResult = versionMapper.insertVersion(version);
        if (insertResult <= 0) {
            throw new RuntimeException("Failed to create new version");
        }

        log.info("Created new version {} for template {}", version.getVersionNumber(), template.getTemplateId());
    }

    private String guessParameterType(String paramName) {
        String lowerName = paramName.toLowerCase();
        
        if (lowerName.contains("date") || lowerName.contains("time")) {
            return "DATE";
        } else if (lowerName.contains("id") || lowerName.contains("count") || lowerName.contains("num")) {
            return "INTEGER";
        } else if (lowerName.contains("amount") || lowerName.contains("price") || lowerName.contains("rate")) {
            return "DECIMAL";
        } else if (lowerName.contains("flag") || lowerName.contains("is") || lowerName.contains("has")) {
            return "BOOLEAN";
        } else {
            return "STRING";
        }
    }
}