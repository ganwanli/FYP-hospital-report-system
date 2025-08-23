package com.hospital.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.dto.ReportConfigDTO;
import com.hospital.report.entity.ReportConfig;
import com.hospital.report.entity.ReportComponent;
import com.hospital.report.entity.ReportDataSource;

import java.util.List;
import java.util.Map;

public interface ReportConfigService {

    ReportConfig createReport(ReportConfigDTO reportConfig);

    ReportConfig updateReport(ReportConfig reportConfig);

    /**
     * 删除报表（支持级联删除）
     * - 如果是父报表，会先递归删除关联的子报表
     * - 如果是子报表，会清除父报表中的关联关系
     * @param reportId 报表ID
     */
    void deleteReport(Long reportId);

    /**
     * 级联删除报表及其所有关联的子报表
     * @param reportId 父报表ID
     * @return 删除的报表数量（包括父报表和子报表）
     */
    int cascadeDeleteReportWithChildren(Long reportId);

    ReportConfig getReportById(Long reportId);

    IPage<ReportConfig> getReportList(Page<ReportConfig> page, String reportName, String reportCategory,
                                      String reportType, Boolean isPublished, Boolean isActive,
                                      Long createdBy, String accessLevel);

    /**
    ReportConfig getReportWithComponents(Long reportId);



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

    **/

    // 观看次数管理
    /**
     * 增加报表观看次数
     * @param reportId 报表ID
     * @return 新的观看次数
     */
    int incrementViewCount(long reportId);

    /**
     * 获取报表观看次数
     * @param reportId 报表ID
     * @return 观看次数
     */
    int getViewCount(int reportId);

    /**
     * 重置报表观看次数
     * @param reportId 报表ID
     */
    void resetViewCount(int reportId);

    // 子报表关联管理
    /**
     * 设置报表的子报表关联
     * @param parentReportId 父报表ID
     * @param childReportId 子报表ID
     * @param triggerParamField 触发参数字段名
     * @return 更新后的父报表配置
     */
    ReportConfig setLinkedReport(Long parentReportId, Long childReportId, String triggerParamField);

    /**
     * 移除报表的子报表关联
     * @param parentReportId 父报表ID
     * @return 更新后的父报表配置
     */
    ReportConfig removeLinkedReport(Long parentReportId);

    /**
     * 获取报表的子报表配置
     * @param parentReportId 父报表ID
     * @return 子报表配置，如果没有关联则返回null
     */
    ReportConfig getLinkedReport(Long parentReportId);

    /**
     * 创建报表的子报表副本
     * @param parentReportId 父报表ID
     * @param childReportName 子报表名称
     * @param triggerParamField 触发参数字段名
     * @param createdBy 创建人ID
     * @return 创建的子报表配置
     */
    ReportConfig createLinkedReport(Long parentReportId, String childReportName, String triggerParamField, Long createdBy);

    /**
     * 创建关联报表并同时保存父报表和子报表数据
     * @param parentReportId 父报表ID
     * @param triggerParamField 触发参数字段名
     * @param childReportConfig 子报表配置数据
     * @param parentReportConfig 父报表配置数据
     * @param createdBy 创建人ID
     * @return 创建的子报表配置
     */
    ReportConfig createLinkedReportWithParent(Long parentReportId, String triggerParamField, 
                                            Map<String, Object> childReportConfig, 
                                            Map<String, Object> parentReportConfig, 
                                            Long createdBy);
}