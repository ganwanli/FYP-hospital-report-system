package com.hospital.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.entity.ReportConfig;
import com.hospital.report.entity.ReportComponent;
import com.hospital.report.entity.ReportDataSource;

import java.util.List;
import java.util.Map;

public interface ReportConfigService {

    ReportConfig createReport(ReportConfig reportConfig);

    ReportConfig updateReport(ReportConfig reportConfig);

    void deleteReport(Long reportId);

    ReportConfig getReportById(Long reportId);

    ReportConfig getReportWithComponents(Long reportId);

    IPage<ReportConfig> getReportList(Page<ReportConfig> page, String reportName, String reportCategory,
                                     String reportType, Boolean isPublished, Boolean isActive,
                                     Long createdBy, String accessLevel);

    List<ReportConfig> searchReports(String keyword);

    List<ReportConfig> getPublishedReports(Integer limit);

    List<ReportConfig> getUserRecentReports(Long userId, Integer limit);

    List<String> getAllCategories();

    List<String> getAllTypes();

    List<String> getAllAccessLevels();

    Map<String, Object> getReportStatistics();

    List<Map<String, Object>> getCategoryStatistics();

    List<Map<String, Object>> getTypeStatistics();

    List<Map<String, Object>> getMonthlyCreationStatistics();

    void publishReport(Long reportId);

    void unpublishReport(Long reportId);

    ReportConfig duplicateReport(Long reportId, String newName, Long userId);

    void updateThumbnail(Long reportId, String thumbnail);

    boolean isReportNameExists(String reportName, Long excludeId);

    // Component management
    ReportComponent addComponent(ReportComponent component);

    ReportComponent updateComponent(ReportComponent component);

    void deleteComponent(Long componentId);

    List<ReportComponent> getComponentsByReportId(Long reportId);

    void updateComponentPosition(Long componentId, Integer x, Integer y, Integer width, Integer height);

    void updateComponentZIndex(Long componentId, Integer zIndex);

    void updateComponentVisibility(Long componentId, Boolean isVisible);

    void updateComponentLock(Long componentId, Boolean isLocked);

    void reorderComponents(Long reportId, List<Long> componentIds);

    // Data source management
    ReportDataSource addDataSource(ReportDataSource dataSource);

    ReportDataSource updateDataSource(ReportDataSource dataSource);

    void deleteDataSource(Long dataSourceId);

    List<ReportDataSource> getDataSourcesByReportId(Long reportId);

    void refreshDataSource(Long dataSourceId);

    void testDataSourceConnection(Long dataSourceId);

    boolean isDataSourceNameExists(String sourceName, Long reportId, Long excludeId);

    // Version management
    void saveVersion(Long reportId, String versionDescription, Long userId);

    List<Map<String, Object>> getReportVersions(Long reportId);

    void restoreVersion(Long reportId, Long versionId);

    // Export and import
    String exportReport(Long reportId);

    ReportConfig importReport(String reportData, Long userId);

    // Template management
    void saveAsTemplate(Long reportId, String templateName, String templateDescription);

    List<ReportConfig> getReportTemplates();
}