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
        ReportConfig existingReport = reportConfigMapper.selectById(reportConfig.getId());
        if (existingReport == null) {
            throw new RuntimeException("Report not found");
        }
        
        reportConfig.setUpdatedTime(LocalDateTime.now());

        reportConfigRepository.save(reportConfig);
        


        return getReportById(reportConfig.getId());
    }

    @Override
    @Transactional
    public void deleteReport(Long reportId) {
        // Delete in order: components, data sources, versions, then report
        reportComponentMapper.deleteByReportId(reportId);
        reportDataSourceMapper.deleteByReportId(reportId);
        reportVersionMapper.deleteByReportId(reportId);
        reportConfigMapper.deleteById(reportId);
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
}