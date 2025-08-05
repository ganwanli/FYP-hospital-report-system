package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.report.dto.ReportConfigDTO;
import com.hospital.report.entity.ReportConfig;
import com.hospital.report.entity.ReportComponent;
import com.hospital.report.entity.ReportDataSource;
import com.hospital.report.entity.ReportVersion;
import com.hospital.report.mapper.ReportConfigMapper;
import com.hospital.report.mapper.ReportComponentMapper;
import com.hospital.report.mapper.ReportDataSourceMapper;
import com.hospital.report.mapper.ReportVersionMapper;
import com.hospital.report.repository.ReportConfigRepository;
import com.hospital.report.service.ReportConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportConfigServiceImpl implements ReportConfigService {

    private final ReportConfigMapper reportConfigMapper;
    private final ReportComponentMapper reportComponentMapper;
    private final ReportDataSourceMapper reportDataSourceMapper;
    private final ReportVersionMapper reportVersionMapper;
    private final ObjectMapper objectMapper;

    @Autowired
    ReportConfigRepository reportConfigRepository;

    @Override
    @Transactional
    public ReportConfig createReport(ReportConfigDTO reportConfig) {
        reportConfig.setCreatedTime(LocalDateTime.now());
        reportConfig.setUpdatedTime(LocalDateTime.now());
        reportConfig.setIsActive(1);
        reportConfig.setIsPublished(0);
        reportConfig.setVersion("v1.0");

        //创建一个ReportConfig对象，再把reportConfigDTO的数据复制到reportConfigPojo中
        ReportConfig reportConfigPojo = new ReportConfig();
        BeanUtils.copyProperties(reportConfig, reportConfigPojo);

        //用reportConfigRepository自带的方法save保存数据，这个方法如果没有数据的id，会自动创建一个id新增一条数据进去
        ReportConfig savedReport = reportConfigRepository.save (reportConfigPojo);
        
//        暂时不做版本控制
        // Create initial version
//        saveVersion(reportConfig.getReportId(), "Initial version", reportConfig.getCreatedBy());
        
        return savedReport;
    }

    @Override
    @Transactional
    public ReportConfig updateReport(ReportConfig reportConfig) {
        ReportConfig existingReport = reportConfigRepository.findById(reportConfig.getId()).orElseThrow(() -> new IllegalArgumentException("Report not found"));
//                reportConfigMapper.selectById(reportConfig.getId());
//        if (existingReport == null) {
//            throw new RuntimeException("Report not found");
//        }
        
        reportConfig.setUpdatedTime(LocalDateTime.now());

        reportConfigRepository.save(reportConfig);
        


        return getReportById(reportConfig.getId());
    }

    @Override
    @Transactional
    public void deleteReport(Long reportId) {
        // Delete in order: components, data sources, versions, then report
        reportConfigRepository.deleteById(reportId);
//        reportComponentMapper.deleteByReportId(reportId);
//        reportDataSourceMapper.deleteByReportId(reportId);
//        reportVersionMapper.deleteByReportId(reportId);
//        reportConfigMapper.deleteById(reportId);
    }

    @Override
    public ReportConfig getReportById(Long reportId) {
        return reportConfigRepository.findById(reportId).orElseThrow(() -> new IllegalArgumentException("Report not found"));
//        return reportConfigMapper.selectByIdWithUserInfo(reportId);
    }

    @Override
    public IPage<ReportConfig> getReportList(Page<ReportConfig> page, String reportName, String reportCategory,
                                             String reportType, Boolean isPublished, Boolean isActive,
                                             Long createdBy, String accessLevel) {

        return reportConfigMapper.selectReportList(page, reportName, reportCategory, reportType,
                isPublished, isActive, createdBy, accessLevel);
    }

    /**
     *
//
//    @Override
//    public ReportConfig getReportWithComponents(Long reportId) {
//        ReportConfig report = reportConfigMapper.selectByIdWithUserInfo(reportId);
//        if (report != null) {
//            report.setComponents(reportComponentMapper.selectByReportId(reportId));
//            report.setDataSources(reportDataSourceMapper.selectByReportId(reportId));
//        }
//        return report;
//    }



    @Override
    public List<ReportConfig> searchReports(String keyword) {
        return reportConfigMapper.searchReports(keyword);
    }

    @Override
    public List<ReportConfig> getPublishedReports(Integer limit) {
        return reportConfigMapper.selectPublishedReports(limit);
    }

    @Override
    public List<ReportConfig> getUserRecentReports(Long userId, Integer limit) {
        return reportConfigMapper.selectUserRecentReports(userId, limit);
    }

    @Override
    public List<String> getAllCategories() {
        return reportConfigMapper.selectAllCategories();
    }

    @Override
    public List<String> getAllTypes() {
        return reportConfigMapper.selectAllTypes();
    }

    @Override
    public List<String> getAllAccessLevels() {
        return reportConfigMapper.selectAllAccessLevels();
    }

    @Override
    public Map<String, Object> getReportStatistics() {
        return reportConfigMapper.selectOverallStatistics();
    }

    @Override
    public List<Map<String, Object>> getCategoryStatistics() {
        return reportConfigMapper.selectCategoryStatistics();
    }

    @Override
    public List<Map<String, Object>> getTypeStatistics() {
        return reportConfigMapper.selectTypeStatistics();
    }

    @Override
    public List<Map<String, Object>> getMonthlyCreationStatistics() {
        return reportConfigMapper.selectMonthlyCreationStatistics();
    }

    @Override
    public void publishReport(Long reportId) {
        reportConfigMapper.updatePublishStatus(reportId, true);
    }

    @Override
    public void unpublishReport(Long reportId) {
        reportConfigMapper.updatePublishStatus(reportId, false);
    }

    @Override
    @Transactional
    public ReportConfig duplicateReport(Long reportId, String newName, Long userId) {
        ReportConfig originalReport = getReportWithComponents(reportId);
        if (originalReport == null) {
            throw new RuntimeException("Report not found");
        }
        
        // Create new report
        ReportConfig newReport = new ReportConfig();
        newReport.setReportName(newName);
        newReport.setReportDescription(originalReport.getReportDescription() + " (Copy)");
        newReport.setReportCategory(originalReport.getReportCategory());
        newReport.setReportType(originalReport.getReportType());
        newReport.setLayoutConfig(originalReport.getLayoutConfig());
        newReport.setComponentsConfig(originalReport.getComponentsConfig());
        newReport.setDataSourcesConfig(originalReport.getDataSourcesConfig());
        newReport.setStyleConfig(originalReport.getStyleConfig());
        newReport.setCanvasWidth(originalReport.getCanvasWidth());
        newReport.setCanvasHeight(originalReport.getCanvasHeight());
        newReport.setIsPublished(false);
        newReport.setIsActive(true);
        newReport.setCreatedBy(userId);
        newReport.setUpdatedBy(userId);
        newReport.setTags(originalReport.getTags());
        newReport.setAccessLevel(originalReport.getAccessLevel());
        newReport.setRefreshInterval(originalReport.getRefreshInterval());
        
        // Copy components and data sources
        newReport.setComponents(originalReport.getComponents());
        newReport.setDataSources(originalReport.getDataSources());
        
        return createReport(newReport);
    }

    @Override
    public void updateThumbnail(Long reportId, String thumbnail) {
        reportConfigMapper.updateThumbnail(reportId, thumbnail);
    }

    @Override
    public boolean isReportNameExists(String reportName, Long excludeId) {
        List<ReportConfig> reports = reportConfigMapper.selectByReportNameExcludeId(reportName, excludeId);
        return !reports.isEmpty();
    }

    // Component management methods
    @Override
    public ReportComponent addComponent(ReportComponent component) {
        component.setCreatedTime(LocalDateTime.now());
        component.setUpdatedTime(LocalDateTime.now());
        component.setIsVisible(true);
        component.setIsLocked(false);
        
        if (component.getZIndex() == null) {
            Integer maxZIndex = reportComponentMapper.selectMaxZIndex(component.getReportId());
            component.setZIndex(maxZIndex != null ? maxZIndex + 1 : 1);
        }
        
        if (component.getComponentOrder() == null) {
            Integer maxOrder = reportComponentMapper.selectMaxComponentOrder(component.getReportId());
            component.setComponentOrder(maxOrder != null ? maxOrder + 1 : 1);
        }
        
        reportComponentMapper.insert(component);
        return component;
    }

    @Override
    public ReportComponent updateComponent(ReportComponent component) {
        component.setUpdatedTime(LocalDateTime.now());
        reportComponentMapper.updateById(component);
        return reportComponentMapper.selectByComponentId(component.getComponentId());
    }

    @Override
    public void deleteComponent(Long componentId) {
        reportComponentMapper.deleteById(componentId);
    }

    @Override
    public List<ReportComponent> getComponentsByReportId(Long reportId) {
        return reportComponentMapper.selectByReportId(reportId);
    }

    @Override
    public void updateComponentPosition(Long componentId, Integer x, Integer y, Integer width, Integer height) {
        reportComponentMapper.updateComponentPosition(componentId, x, y, width, height);
    }

    @Override
    public void updateComponentZIndex(Long componentId, Integer zIndex) {
        reportComponentMapper.updateComponentZIndex(componentId, zIndex);
    }

    @Override
    public void updateComponentVisibility(Long componentId, Boolean isVisible) {
        reportComponentMapper.updateComponentVisibility(componentId, isVisible);
    }

    @Override
    public void updateComponentLock(Long componentId, Boolean isLocked) {
        reportComponentMapper.updateComponentLock(componentId, isLocked);
    }

    @Override
    @Transactional
    public void reorderComponents(Long reportId, List<Long> componentIds) {
        for (int i = 0; i < componentIds.size(); i++) {
            reportComponentMapper.updateComponentOrder(componentIds.get(i), i + 1);
        }
    }

    // Data source management methods
    @Override
    public ReportDataSource addDataSource(ReportDataSource dataSource) {
        dataSource.setCreatedTime(LocalDateTime.now());
        dataSource.setUpdatedTime(LocalDateTime.now());
        dataSource.setIsActive(true);
        dataSource.setErrorCount(0);
        
        reportDataSourceMapper.insert(dataSource);
        return dataSource;
    }

    @Override
    public ReportDataSource updateDataSource(ReportDataSource dataSource) {
        dataSource.setUpdatedTime(LocalDateTime.now());
        reportDataSourceMapper.updateById(dataSource);
        return reportDataSourceMapper.selectByDataSourceId(dataSource.getDataSourceId());
    }

    @Override
    public void deleteDataSource(Long dataSourceId) {
        reportDataSourceMapper.deleteById(dataSourceId);
    }

    @Override
    public List<ReportDataSource> getDataSourcesByReportId(Long reportId) {
        return reportDataSourceMapper.selectByReportId(reportId);
    }

    @Override
    public void refreshDataSource(Long dataSourceId) {
        // TODO: Implement data source refresh logic
        reportDataSourceMapper.updateRefreshStatus(dataSourceId, null, 0);
    }

    @Override
    public void testDataSourceConnection(Long dataSourceId) {
        // TODO: Implement data source connection test
        log.info("Testing data source connection for ID: {}", dataSourceId);
    }

    @Override
    public boolean isDataSourceNameExists(String sourceName, Long reportId, Long excludeId) {
        List<ReportDataSource> dataSources = reportDataSourceMapper.selectBySourceNameExcludeId(sourceName, reportId, excludeId);
        return !dataSources.isEmpty();
    }

    // Version management methods
    @Override
    @Transactional
    public void saveVersion(Long reportId, String versionDescription, Long userId) {
        ReportConfig report = getReportWithComponents(reportId);
        if (report == null) {
            throw new RuntimeException("Report not found");
        }
        
        // Clear current version flag
        reportVersionMapper.clearCurrentVersion(reportId);
        
        // Generate new version number
        Integer maxVersion = reportVersionMapper.selectMaxVersionNumber(reportId);
        String newVersionNumber = "v" + (maxVersion != null ? maxVersion + 1 : 1) + ".0";
        
        // Create new version
        ReportVersion version = new ReportVersion();
        version.setReportId(reportId);
        version.setVersionNumber(newVersionNumber);
        version.setVersionDescription(versionDescription);
        version.setLayoutConfig(report.getLayoutConfig());
        version.setComponentsConfig(report.getComponentsConfig());
        version.setDataSourcesConfig(report.getDataSourcesConfig());
        version.setStyleConfig(report.getStyleConfig());
        version.setIsCurrent(true);
        version.setCreatedBy(userId);
        version.setCreatedTime(LocalDateTime.now());
        version.setThumbnail(report.getThumbnail());
        
        reportVersionMapper.insert(version);
        
        // Update report version
        report.setVersion(newVersionNumber);
        reportConfigMapper.updateById(report);
    }

    @Override
    public List<Map<String, Object>> getReportVersions(Long reportId) {
        return reportVersionMapper.selectByReportIdWithUserInfo(reportId);
    }

    @Override
    @Transactional
    public void restoreVersion(Long reportId, Long versionId) {
        ReportVersion version = reportVersionMapper.selectById(versionId);
        if (version == null || !version.getReportId().equals(reportId)) {
            throw new RuntimeException("Version not found");
        }
        
        // Update report with version data
        ReportConfig report = new ReportConfig();
        report.setReportId(reportId);
        report.setLayoutConfig(version.getLayoutConfig());
        report.setComponentsConfig(version.getComponentsConfig());
        report.setDataSourcesConfig(version.getDataSourcesConfig());
        report.setStyleConfig(version.getStyleConfig());
        report.setVersion(version.getVersionNumber());
        report.setUpdatedTime(LocalDateTime.now());
        
        reportConfigMapper.updateById(report);
        
        // Set version as current
        reportVersionMapper.clearCurrentVersion(reportId);
        reportVersionMapper.setCurrentVersion(versionId);
    }

    @Override
    public String exportReport(Long reportId) {
        ReportConfig report = getReportWithComponents(reportId);
        if (report == null) {
            throw new RuntimeException("Report not found");
        }
        
        try {
            return objectMapper.writeValueAsString(report);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to export report", e);
        }
    }

    @Override
    @Transactional
    public ReportConfig importReport(String reportData, Long userId) {
        try {
            ReportConfig report = objectMapper.readValue(reportData, ReportConfig.class);
            report.setReportId(null);
            report.setCreatedBy(userId);
            report.setUpdatedBy(userId);
            report.setIsPublished(false);
            
            return createReport(report);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to import report", e);
        }
    }

    @Override
    @Transactional
    public void saveAsTemplate(Long reportId, String templateName, String templateDescription) {
        ReportConfig originalReport = getReportWithComponents(reportId);
        if (originalReport == null) {
            throw new RuntimeException("Report not found");
        }
        
        ReportConfig template = new ReportConfig();
        template.setReportName(templateName);
        template.setReportDescription(templateDescription);
        template.setReportCategory("TEMPLATE");
        template.setReportType(originalReport.getReportType());
        template.setLayoutConfig(originalReport.getLayoutConfig());
        template.setComponentsConfig(originalReport.getComponentsConfig());
        template.setDataSourcesConfig(originalReport.getDataSourcesConfig());
        template.setStyleConfig(originalReport.getStyleConfig());
        template.setCanvasWidth(originalReport.getCanvasWidth());
        template.setCanvasHeight(originalReport.getCanvasHeight());
        template.setIsPublished(true);
        template.setIsActive(true);
        template.setCreatedBy(originalReport.getCreatedBy());
        template.setUpdatedBy(originalReport.getUpdatedBy());
        template.setAccessLevel("PUBLIC");
        template.setComponents(originalReport.getComponents());
        template.setDataSources(originalReport.getDataSources());
        
        createReport(template);
    }

    @Override
    public List<ReportConfig> getReportTemplates() {
        QueryWrapper<ReportConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("report_category", "TEMPLATE")
                   .eq("is_published", true)
                   .eq("is_active", true)
                   .orderByDesc("updated_time");
        return reportConfigMapper.selectList(queryWrapper);
    }

    private void saveComponents(Long reportId, List<ReportComponent> components) {
        for (ReportComponent component : components) {
            component.setReportId(reportId);
            component.setCreatedTime(LocalDateTime.now());
            component.setUpdatedTime(LocalDateTime.now());
            if (component.getIsVisible() == null) {
                component.setIsVisible(true);
            }
            if (component.getIsLocked() == null) {
                component.setIsLocked(false);
            }
        }
        
        if (!components.isEmpty()) {
            reportComponentMapper.batchInsert(components);
        }
    }

    private void saveDataSources(Long reportId, List<ReportDataSource> dataSources) {
        for (ReportDataSource dataSource : dataSources) {
            dataSource.setReportId(reportId);
            dataSource.setCreatedTime(LocalDateTime.now());
            dataSource.setUpdatedTime(LocalDateTime.now());
            if (dataSource.getIsActive() == null) {
                dataSource.setIsActive(true);
            }
            if (dataSource.getErrorCount() == null) {
                dataSource.setErrorCount(0);
            }
        }
        
        if (!dataSources.isEmpty()) {
            reportDataSourceMapper.batchInsert(dataSources);
        }
    }

    **/

    // 观看次数管理实现
    @Override
    @Transactional
    public int incrementViewCount(long reportId) {
        log.info("Incrementing view count for report {}", reportId);

        // 检查报表是否存在
        ReportConfig report = reportConfigMapper.selectByIdWithUserInfo(reportId);
        if (report == null) {
            throw new RuntimeException("Report not found with id: " + reportId);
        }

        // 增加观看次数
        int currentViewCount = report.getViewCount() != null ? report.getViewCount() : 0;
        int newViewCount = currentViewCount + 1;

        // 更新数据库
        report.setViewCount(newViewCount);
        report.setUpdatedTime(LocalDateTime.now());
        reportConfigRepository.save(report);

        log.info("View count for report {} updated from {} to {}", reportId, currentViewCount, newViewCount);
        return newViewCount;
    }

    @Override
    public int getViewCount(int reportId) {
        ReportConfig report = reportConfigMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("Report not found with id: " + reportId);
        }
        return report.getViewCount() != null ? report.getViewCount() : 0;
    }

    @Override
    @Transactional
    public void resetViewCount(int reportId) {
        log.info("Resetting view count for report {}", reportId);

        ReportConfig report = reportConfigMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("Report not found with id: " + reportId);
        }

        report.setViewCount(0);
        report.setUpdatedTime(LocalDateTime.now());
        reportConfigMapper.updateById(report);

        log.info("View count for report {} reset to 0", reportId);
    }

    // 子报表关联管理实现
    @Override
    @Transactional
    public ReportConfig setLinkedReport(Long parentReportId, Long childReportId, String triggerParamField) {
        log.info("Setting linked report: parent={}, child={}, triggerField={}", parentReportId, childReportId, triggerParamField);
        
        ReportConfig parentReport = reportConfigRepository.findById(parentReportId)
            .orElseThrow(() -> new IllegalArgumentException("Parent report not found: " + parentReportId));
        
        ReportConfig childReport = reportConfigRepository.findById(childReportId)
            .orElseThrow(() -> new IllegalArgumentException("Child report not found: " + childReportId));
        
        // 设置关联关系
        parentReport.setLinkedReportId(childReportId);
        parentReport.setTriggerParamField(triggerParamField);
        parentReport.setUpdatedTime(LocalDateTime.now());
        
        ReportConfig saved = reportConfigRepository.save(parentReport);
        log.info("Successfully set linked report for parent report: {}", parentReportId);
        return saved;
    }

    @Override
    @Transactional
    public ReportConfig removeLinkedReport(Long parentReportId) {
        log.info("Removing linked report for parent: {}", parentReportId);
        
        ReportConfig parentReport = reportConfigRepository.findById(parentReportId)
            .orElseThrow(() -> new IllegalArgumentException("Parent report not found: " + parentReportId));
        
        parentReport.setLinkedReportId(null);
        parentReport.setTriggerParamField(null);
        parentReport.setUpdatedTime(LocalDateTime.now());
        
        ReportConfig saved = reportConfigRepository.save(parentReport);
        log.info("Successfully removed linked report for parent report: {}", parentReportId);
        return saved;
    }

    @Override
    public ReportConfig getLinkedReport(Long parentReportId) {
        log.info("Getting linked report for parent: {}", parentReportId);
        
        ReportConfig parentReport = reportConfigRepository.findById(parentReportId)
            .orElseThrow(() -> new IllegalArgumentException("Parent report not found: " + parentReportId));
        
        if (parentReport.getLinkedReportId() == null) {
            return null;
        }
        
        return reportConfigRepository.findById(parentReport.getLinkedReportId())
            .orElse(null);
    }

    @Override
    @Transactional
    public ReportConfig createLinkedReport(Long parentReportId, String childReportName, String triggerParamField, Long createdBy) {
        log.info("Creating linked report: parent={}, name={}, triggerField={}, createdBy={}", 
                 parentReportId, childReportName, triggerParamField, createdBy);
        
        ReportConfig parentReport = reportConfigRepository.findById(parentReportId)
            .orElseThrow(() -> new IllegalArgumentException("Parent report not found: " + parentReportId));
        
        // 创建子报表配置（基于父报表配置）
        ReportConfig childReport = new ReportConfig();
        
        // 重要：确保子报表是新记录，不设置ID
        childReport.setId(null);
        
        childReport.setReportName(childReportName);
        childReport.setReportCode(generateUniqueReportCode(parentReport.getReportCode() + "_LINKED"));
        childReport.setReportType(parentReport.getReportType());
        childReport.setReportCategoryId(parentReport.getReportCategoryId());
        childReport.setSqlTemplateId(parentReport.getSqlTemplateId());
        childReport.setDatasourceId(parentReport.getDatasourceId());
        
        // 复制报表配置
        childReport.setReportConfig(parentReport.getReportConfig());
        childReport.setChartConfig(parentReport.getChartConfig());
        childReport.setExportConfig(parentReport.getExportConfig());
        
        // 设置其他属性
        childReport.setCacheEnabled(parentReport.getCacheEnabled());
        childReport.setCacheTimeout(parentReport.getCacheTimeout());
        childReport.setRefreshInterval(parentReport.getRefreshInterval());
        childReport.setAccessLevel("PRIVATE"); // 子报表默认私有
        childReport.setDescription("Linked report for: " + parentReport.getReportName());
        childReport.setVersion("1.0");
        childReport.setIsPublished(0);
        childReport.setIsActive(1);
        childReport.setIsDeleted(0);
        childReport.setCreatedBy(createdBy);
        childReport.setUpdatedBy(createdBy);
        
        // 重要：确保子报表不会继承父报表的关联设置
        childReport.setLinkedReportId(null);
        childReport.setTriggerParamField(null);
        
        // 保存子报表
        ReportConfig savedChildReport = reportConfigRepository.save(childReport);
        
        // 直接在当前事务中更新父报表的关联关系，避免重新获取实体
        parentReport.setLinkedReportId(savedChildReport.getId());
        parentReport.setTriggerParamField(triggerParamField);
        parentReport.setUpdatedTime(LocalDateTime.now());
        reportConfigRepository.save(parentReport);
        
        log.info("Successfully created linked report with ID: {} and linked to parent: {}", savedChildReport.getId(), parentReportId);
        return savedChildReport;
    }

    @Override
    @Transactional
    public ReportConfig createLinkedReportWithParent(Long parentReportId, String triggerParamField, 
                                                   Map<String, Object> childReportConfig, 
                                                   Map<String, Object> parentReportConfig, 
                                                   Long createdBy) {
        log.info("Creating linked report with parent data - parentReportId: {}, triggerParamField: {}, createdBy: {}", 
                 parentReportId, triggerParamField, createdBy);
        
        ReportConfig parentReport;
        
        // 1. 处理父报表 - 检查是否为新创建的报表
        if (parentReportId == null || parentReportId == 0) {
            // 创建新的父报表
            if (parentReportConfig == null) {
                throw new IllegalArgumentException("Parent report configuration is required when creating new parent report");
            }
            
            log.info("Creating new parent report");
            parentReport = new ReportConfig();
            parentReport.setId(null); // 确保是新记录
            updateReportFromMap(parentReport, parentReportConfig);
            
            // 设置创建和更新信息
            parentReport.setCreatedBy(createdBy);
            parentReport.setUpdatedBy(createdBy);
            parentReport.setCreatedTime(LocalDateTime.now());
            parentReport.setUpdatedTime(LocalDateTime.now());
            parentReport.setIsDeleted(0);
            
            // 先保存父报表以获取ID
            parentReport = reportConfigRepository.save(parentReport);
            log.info("New parent report created with ID: {}", parentReport.getId());
            
        } else {
            // 更新现有的父报表
            parentReport = reportConfigRepository.findById(parentReportId)
                .orElseThrow(() -> new IllegalArgumentException("Parent report not found: " + parentReportId));
            
            if (parentReportConfig != null) {
                log.info("Updating existing parent report with new configuration");
                updateReportFromMap(parentReport, parentReportConfig);
                parentReport.setUpdatedBy(createdBy);
                parentReport.setUpdatedTime(LocalDateTime.now());
                parentReport = reportConfigRepository.save(parentReport);
                log.info("Parent report updated successfully");
            }
        }
        
        // 2. 创建子报表
        ReportConfig childReport = new ReportConfig();
        
        // 重要：确保子报表是新记录，不设置ID
        childReport.setId(null);
        
        // 从配置数据中设置子报表属性
        updateReportFromMap(childReport, childReportConfig);
        
        // 设置子报表特有的关联信息
        childReport.setLinkedReportId(null); // 子报表本身不指向其他报表
        childReport.setTriggerParamField(null); // 子报表本身不包含触发参数
        
        // 设置创建和更新信息
        childReport.setCreatedBy(createdBy);
        childReport.setUpdatedBy(createdBy);
        childReport.setCreatedTime(LocalDateTime.now());
        childReport.setUpdatedTime(LocalDateTime.now());
        childReport.setIsDeleted(0);
        
        // 保存子报表
        ReportConfig savedChildReport = reportConfigRepository.save(childReport);
        log.info("Child report created with ID: {}", savedChildReport.getId());
        
        // 3. 更新父报表的关联关系
        parentReport.setLinkedReportId(savedChildReport.getId());
        parentReport.setTriggerParamField(triggerParamField);
        parentReport.setUpdatedTime(LocalDateTime.now());
        parentReport = reportConfigRepository.save(parentReport);
        
        log.info("Successfully created linked report with parent data - childId: {}, parentId: {}", 
                savedChildReport.getId(), parentReport.getId());
        return savedChildReport;
    }
    
    /**
     * 从Map配置数据更新ReportConfig实体
     */
    private void updateReportFromMap(ReportConfig report, Map<String, Object> configMap) {
        if (configMap.get("reportName") != null) {
            report.setReportName(configMap.get("reportName").toString());
        }
        if (configMap.get("reportCode") != null) {
            String requestedCode = configMap.get("reportCode").toString();
            // 确保报表代码唯一性
            String uniqueCode = generateUniqueReportCode(requestedCode);
            report.setReportCode(uniqueCode);
        }
        if (configMap.get("description") != null) {
            report.setDescription(configMap.get("description").toString());
        }
        if (configMap.get("reportType") != null) {
            report.setReportType(configMap.get("reportType").toString());
        }
        if (configMap.get("accessLevel") != null) {
            report.setAccessLevel(configMap.get("accessLevel").toString());
        }
        if (configMap.get("businessType") != null) {
            report.setBusinessType(configMap.get("businessType").toString());
        }
        if (configMap.get("departmentCode") != null) {
            report.setDepartmentCode(configMap.get("departmentCode").toString());
        }
        if (configMap.get("usageType") != null) {
            report.setUsageType(configMap.get("usageType").toString());
        }
        if (configMap.get("version") != null) {
            report.setVersion(configMap.get("version").toString());
        }
        if (configMap.get("sqlTemplateId") != null) {
            report.setSqlTemplateId(Long.valueOf(configMap.get("sqlTemplateId").toString()));
        }
        if (configMap.get("datasourceId") != null) {
            report.setDatasourceId(Long.valueOf(configMap.get("datasourceId").toString()));
        }
        if (configMap.get("categoryId") != null && configMap.get("categoryId") != "") {
            report.setReportCategoryId(Long.valueOf(configMap.get("categoryId").toString()));
        }
//        if (configMap.get("isPublished") != null) {
//            report.setIsPublished(Boolean.valueOf(configMap.get("isPublic").toString()) ? 1 : 0);
//        }
        if (configMap.get("isPublished") != null) {
            report.setIsPublished(Integer.valueOf(configMap.get("isPublished").toString()));
        }
        if (configMap.get("isActive") != null) {
            report.setIsActive(Integer.valueOf(configMap.get("isActive").toString()));
        }
        if (configMap.get("approvalStatus") != null) {
            report.setApprovalStatus(Integer.valueOf(configMap.get("approvalStatus").toString()));
        }
        if (configMap.get("chartConfig") != null) {
            report.setChartConfig(configMap.get("chartConfig").toString());
        }
        
//        // 处理tags - 可能是List或字符串
//        if (configMap.get("tags") != null) {
//            Object tagsObj = configMap.get("tags");
//            if (tagsObj instanceof List) {
//                @SuppressWarnings("unchecked")
//                List<String> tagsList = (List<String>) tagsObj;
//                report.setTags(String.join(",", tagsList));
//            } else {
//                report.setTags(tagsObj.toString());
//            }
//        }
    }
    
    /**
     * 生成唯一的报表代码
     * @param baseCode 基础代码
     * @return 唯一的报表代码
     */
    private String generateUniqueReportCode(String baseCode) {
        String uniqueCode = baseCode;
        int counter = 1;
        
        // 检查代码是否已存在，如果存在则添加序号
        while (reportConfigRepository.existsByReportCode(uniqueCode)) {
            uniqueCode = baseCode + "_" + counter;
            counter++;
            
            // 防止无限循环，最多尝试1000次
            if (counter > 1000) {
                // 使用UUID确保唯一性
                uniqueCode = baseCode + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                break;
            }
        }
        
        return uniqueCode;
    }
}