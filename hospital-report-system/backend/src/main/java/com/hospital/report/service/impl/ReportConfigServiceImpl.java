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
import java.util.ArrayList;

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
        log.info("开始创建报表，报表名称：{}，原始代码：{}", reportConfig.getReportName(), reportConfig.getReportCode());
        
        // 如果reportCode为空，自动生成一个唯一的reportCode
        if (reportConfig.getReportCode() == null || reportConfig.getReportCode().trim().isEmpty()) {
            String generatedCode = generateUniqueReportCode(reportConfig.getReportName());
            reportConfig.setReportCode(generatedCode);
            log.info("自动生成报表代码：{}", generatedCode);
        }
        
        reportConfig.setCreatedTime(LocalDateTime.now());
        reportConfig.setUpdatedTime(LocalDateTime.now());
        reportConfig.setIsActive(1);
        reportConfig.setIsPublished(0);
        reportConfig.setVersion("v1.0");

        // 确保必填字段有默认值
        if (reportConfig.getReportConfig() == null || reportConfig.getReportConfig().trim().isEmpty()) {
            reportConfig.setReportConfig("{\"columns\":[],\"pagination\":{\"pageSize\":20},\"sorting\":{\"enabled\":true},\"filtering\":{\"enabled\":true}}");
        }
        
        if (reportConfig.getChartConfig() == null || reportConfig.getChartConfig().trim().isEmpty()) {
            String chartType = reportConfig.getReportType() != null ? reportConfig.getReportType().toLowerCase() : "table";
            String title = reportConfig.getReportName() != null ? reportConfig.getReportName() : "新报表";
            reportConfig.setChartConfig("{\"type\":\"" + chartType + "\",\"title\":\"" + title + "\",\"showLegend\":true,\"showDataLabels\":true}");
        }

        //创建一个ReportConfig对象，再把reportConfigDTO的数据复制到reportConfigPojo中
        ReportConfig reportConfigPojo = new ReportConfig();
        BeanUtils.copyProperties(reportConfig, reportConfigPojo);
        
        log.info("保存前的报表代码：{}", reportConfigPojo.getReportCode());

        //用reportConfigRepository自带的方法save保存数据，这个方法如果没有数据的id，会自动创建一个id新增一条数据进去
        ReportConfig savedReport = reportConfigRepository.save (reportConfigPojo);
        
        log.info("保存后的报表代码：{}", savedReport.getReportCode());
        
//        暂时不做版本控制
        // Create initial version
//        saveVersion(reportConfig.getReportId(), "Initial version", reportConfig.getCreatedBy());
        
        return savedReport;
    }
    
    /**
     * 生成唯一的报表代码
     * 格式：RPT_业务前缀_YYYYMMDD_序号
     */
    private String generateUniqueReportCode(String reportName) {
        // 获取当前日期字符串
        String dateStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 生成业务前缀（从报表名称中提取，如果是中文则使用拼音首字母或默认前缀）
        String businessPrefix = generateBusinessPrefix(reportName);
        
        // 基础代码格式
        String baseCode = "RPT_" + businessPrefix + "_" + dateStr;
        
        // 查找当天同前缀的最大序号
        int maxSequence = getMaxSequenceForToday(baseCode);
        
        // 生成新的序号
        int newSequence = maxSequence + 1;
        
        // 最终的报表代码
        String reportCode = baseCode + "_" + String.format("%03d", newSequence);
        
        return reportCode;
    }
    
    /**
     * 从报表名称生成业务前缀
     */
    private String generateBusinessPrefix(String reportName) {
        if (reportName == null || reportName.trim().isEmpty()) {
            return "GEN"; // 默认前缀 General
        }
        
        // 如果包含关键词，使用对应的业务前缀
        String name = reportName.toUpperCase();
        if (name.contains("患者") || name.contains("病人") || name.contains("PATIENT")) {
            return "PAT";
        } else if (name.contains("医生") || name.contains("医师") || name.contains("DOCTOR")) {
            return "DOC";
        } else if (name.contains("药品") || name.contains("药物") || name.contains("DRUG") || name.contains("MEDICINE")) {
            return "MED";
        } else if (name.contains("检查") || name.contains("化验") || name.contains("TEST") || name.contains("EXAM")) {
            return "TEST";
        } else if (name.contains("手术") || name.contains("SURGERY") || name.contains("OPERATION")) {
            return "SUR";
        } else if (name.contains("财务") || name.contains("费用") || name.contains("FINANCE") || name.contains("COST")) {
            return "FIN";
        } else if (name.contains("统计") || name.contains("分析") || name.contains("STAT") || name.contains("ANALYSIS")) {
            return "STAT";
        } else {
            return "GEN"; // 通用前缀
        }
    }
    
    /**
     * 获取今天同前缀的最大序号
     */
    private int getMaxSequenceForToday(String baseCode) {
        try {
            // 查询今天以baseCode开头的所有reportCode
            List<String> existingCodes = reportConfigRepository.findReportCodesByPrefix(baseCode + "_");
            
            int maxSequence = 0;
            for (String code : existingCodes) {
                try {
                    // 提取序号部分（最后的3位数字）
                    String[] parts = code.split("_");
                    if (parts.length >= 4) {
                        int sequence = Integer.parseInt(parts[parts.length - 1]);
                        maxSequence = Math.max(maxSequence, sequence);
                    }
                } catch (NumberFormatException e) {
                    // 忽略格式不正确的代码
                    log.warn("Invalid report code format: {}", code);
                }
            }
            
            return maxSequence;
        } catch (Exception e) {
            log.error("Error getting max sequence for baseCode: {}", baseCode, e);
            return 0; // 发生错误时返回0，从001开始
        }
    }

    @Override
    @Transactional
    public ReportConfig updateReport(ReportConfig reportConfig) {
        log.info("开始更新报表，ID: {}, linkedReportId: {}, triggerParamField: {}", 
                reportConfig.getId(), reportConfig.getLinkedReportId(), reportConfig.getTriggerParamField());
        
        System.out.println("Service层：开始处理更新报表，ID: " + reportConfig.getId());
        
        // 检查是否只是更新链接关系 - 使用更简单的检测逻辑
        boolean isLinkingOnlyUpdate = isLinkingOnlyUpdate(reportConfig);
        System.out.println("Service层：检测是否为链接更新: " + isLinkingOnlyUpdate);
        
        if (isLinkingOnlyUpdate) {
            log.info("检测到仅更新链接关系，使用专门的更新方法");
            System.out.println("Service层：执行链接更新逻辑");
            
            // 获取现有报表实体
            ReportConfig existingReport = reportConfigRepository.findById(reportConfig.getId())
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
            System.out.println("Service层：成功获取现有报表，ID: " + existingReport.getId());
            
            // 只更新链接相关字段
            existingReport.setLinkedReportId(reportConfig.getLinkedReportId());
            existingReport.setTriggerParamField(reportConfig.getTriggerParamField());
            existingReport.setUpdatedTime(LocalDateTime.now());
            
            System.out.println("Service层：准备保存链接更新");
            ReportConfig savedReport = reportConfigRepository.save(existingReport);
            System.out.println("Service层：链接更新保存完成，ID: " + savedReport.getId());
            
            log.info("链接关系更新完成 - linkedReportId: {}, triggerParamField: {}", 
                    savedReport.getLinkedReportId(), savedReport.getTriggerParamField());
            
            System.out.println("Service层：准备返回链接更新结果");
            return savedReport;
        }
        
        System.out.println("Service层：执行常规更新逻辑");
        // 常规更新 - 先获取现有实体，然后逐字段更新
        ReportConfig existingReport = reportConfigRepository.findById(reportConfig.getId())
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        
        // 逐字段更新，只更新非null字段
        updateNonNullFields(existingReport, reportConfig);
        existingReport.setUpdatedTime(LocalDateTime.now());
        
        ReportConfig saved = reportConfigRepository.save(existingReport);
        System.out.println("Service层：常规更新保存完成，ID: " + saved.getId());
        
        log.info("更新后的报表关联信息 - linkedReportId: {}, triggerParamField: {}", 
                saved.getLinkedReportId(), saved.getTriggerParamField());

        System.out.println("Service层：准备返回常规更新结果");
        return saved;
    }
    
    /**
     * 检查是否只是链接更新（只传递了linkedReportId和/或triggerParamField）
     */
    private boolean isLinkingOnlyUpdate(ReportConfig input) {
        try {
            System.out.println("Service层：检查是否为纯链接更新");
            
            // 检查关键业务字段是否都为null
            boolean hasOnlyLinkingFields = 
                input.getReportName() == null &&
                input.getReportCode() == null &&
                input.getReportType() == null &&
                input.getDatasourceId() == null &&
                input.getSqlTemplateId() == null &&
                input.getReportCategoryId() == null &&
                input.getAccessLevel() == null &&
                input.getDescription() == null &&
                input.getVersion() == null &&
                input.getReportConfig() == null &&
                input.getChartConfig() == null &&
                input.getExportConfig() == null &&
                // 检查是否设置了链接字段
                (input.getLinkedReportId() != null || input.getTriggerParamField() != null);
            
            System.out.println("Service层：链接更新检查结果: " + hasOnlyLinkingFields);
            return hasOnlyLinkingFields;
        } catch (Exception e) {
            System.out.println("Service层：检查链接更新时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 只更新非null字段
     */
    private void updateNonNullFields(ReportConfig existing, ReportConfig input) {
        if (input.getReportName() != null) existing.setReportName(input.getReportName());
        if (input.getReportCode() != null) existing.setReportCode(input.getReportCode());
        if (input.getReportType() != null) existing.setReportType(input.getReportType());
        if (input.getDatasourceId() != null) existing.setDatasourceId(input.getDatasourceId());
        if (input.getSqlTemplateId() != null) existing.setSqlTemplateId(input.getSqlTemplateId());
        if (input.getReportCategoryId() != null) existing.setReportCategoryId(input.getReportCategoryId());
        if (input.getAccessLevel() != null) existing.setAccessLevel(input.getAccessLevel());
        if (input.getDescription() != null) existing.setDescription(input.getDescription());
        if (input.getVersion() != null) existing.setVersion(input.getVersion());
        if (input.getReportConfig() != null) existing.setReportConfig(input.getReportConfig());
        if (input.getChartConfig() != null) existing.setChartConfig(input.getChartConfig());
        if (input.getExportConfig() != null) existing.setExportConfig(input.getExportConfig());
        if (input.getLinkedReportId() != null) existing.setLinkedReportId(input.getLinkedReportId());
        if (input.getTriggerParamField() != null) existing.setTriggerParamField(input.getTriggerParamField());
        if (input.getCacheEnabled() != null) existing.setCacheEnabled(input.getCacheEnabled());
        if (input.getCacheTimeout() != null) existing.setCacheTimeout(input.getCacheTimeout());
        if (input.getRefreshInterval() != null) existing.setRefreshInterval(input.getRefreshInterval());
        if (input.getIsPublished() != null) existing.setIsPublished(input.getIsPublished());
        if (input.getIsActive() != null) existing.setIsActive(input.getIsActive());
        if (input.getApprovalStatus() != null) existing.setApprovalStatus(input.getApprovalStatus());
        if (input.getBusinessType() != null) existing.setBusinessType(input.getBusinessType());
        if (input.getDepartmentCode() != null) existing.setDepartmentCode(input.getDepartmentCode());
        if (input.getUsageType() != null) existing.setUsageType(input.getUsageType());
        if (input.getUpdatedBy() != null) existing.setUpdatedBy(input.getUpdatedBy());
    }

    @Override
    @Transactional
    public void deleteReport(Long reportId) {
        log.info("开始删除报表ID: {}", reportId);
        
        // 获取要删除的报表信息
        ReportConfig reportToDelete = reportConfigRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        
        // 如果报表有关联的子报表，必须先删除所有子报表
        if (reportToDelete.getLinkedReportId() != null) {
            log.info("检测到父报表(ID: {})有关联的子报表(ID: {})，必须先删除子报表", reportId, reportToDelete.getLinkedReportId());
            try {
                // 递归删除子报表（这会处理嵌套的子报表）
                deleteReport(reportToDelete.getLinkedReportId());
                log.info("成功删除关联的子报表ID: {}", reportToDelete.getLinkedReportId());
            } catch (Exception e) {
                log.error("删除关联子报表时出现异常: {}", e.getMessage());
                throw new RuntimeException("无法删除关联的子报表，删除操作终止: " + e.getMessage());
            }
        }
        
        // 清除所有对当前报表的引用（处理当前报表作为其他报表子报表的情况）
        clearAllReferencesToReport(reportId);
        
        // 安全删除报表
        log.info("删除报表ID: {} 的主记录", reportId);
        reportConfigRepository.deleteById(reportId);
        log.info("成功删除报表ID: {}", reportId);
    }

    @Override
    @Transactional
    public int cascadeDeleteReportWithChildren(Long reportId) {
        log.info("开始级联删除报表ID: {} 及其所有子报表", reportId);
        
        // 获取要删除的报表信息
        ReportConfig reportToDelete = reportConfigRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        
        int deletedCount = 0;
        
        // 首先，收集所有需要删除的报表IDs
        List<Long> allReportIdsToDelete = new ArrayList<>();
        collectReportIdsForDeletion(reportToDelete, allReportIdsToDelete);
        log.info("收集到需要删除的报表IDs: {}", allReportIdsToDelete);
        
        // 重要：在删除任何报表之前，清除所有对这些报表的引用
        for (Long idToDelete : allReportIdsToDelete) {
            clearAllReferencesToReport(idToDelete);
        }
        
        // 现在可以安全地删除所有报表（顺序不再重要，因为引用已被清除）
        for (Long idToDelete : allReportIdsToDelete) {
            try {
                log.info("删除报表ID: {}", idToDelete);
                reportConfigRepository.deleteById(idToDelete);
                deletedCount++;
            } catch (Exception e) {
                log.error("删除报表ID: {} 时出现异常: {}", idToDelete, e.getMessage(), e);
            }
        }
        
        log.info("级联删除完成，共删除了 {} 个报表", deletedCount);
        return deletedCount;
    }
    
    /**
     * 收集所有需要删除的报表ID（深度优先遍历）
     * @param report 当前报表
     * @param reportIds 收集的报表ID列表
     */
    private void collectReportIdsForDeletion(ReportConfig report, List<Long> reportIds) {
        // 避免重复添加
        if (reportIds.contains(report.getId())) {
            return;
        }
        
        // 添加当前报表ID
        reportIds.add(report.getId());
        log.info("收集删除报表ID: {}", report.getId());
        
        // 递归处理子报表
        if (report.getLinkedReportId() != null) {
            try {
                ReportConfig childReport = reportConfigRepository.findById(report.getLinkedReportId())
                    .orElse(null);
                
                if (childReport != null) {
                    log.info("发现子报表ID: {} (父报表ID: {})", childReport.getId(), report.getId());
                    collectReportIdsForDeletion(childReport, reportIds);
                }
            } catch (Exception e) {
                log.warn("获取子报表ID: {} 时出现异常: {}", report.getLinkedReportId(), e.getMessage());
            }
        }
    }
    
    /**
     * 清除所有对指定报表的引用
     * @param reportIdToDelete 要删除的报表ID
     */
    private void clearAllReferencesToReport(Long reportIdToDelete) {
        try {
            log.info("清除所有对报表ID: {} 的引用", reportIdToDelete);
            
            // 查找所有引用了该报表作为子报表的父报表
            List<ReportConfig> parentReports = reportConfigRepository.findByLinkedReportId(reportIdToDelete);
            log.info("找到 {} 个父报表引用了报表ID: {}", parentReports.size(), reportIdToDelete);
            
            for (ReportConfig parentReport : parentReports) {
                log.info("清除父报表ID: {} 对子报表ID: {} 的引用", parentReport.getId(), reportIdToDelete);
                parentReport.setLinkedReportId(null);
                parentReport.setTriggerParamField(null);
                parentReport.setUpdatedTime(LocalDateTime.now());
                reportConfigRepository.save(parentReport);
                log.info("已清除父报表ID: {} 的引用", parentReport.getId());
            }
        } catch (Exception e) {
            log.error("清除对报表ID: {} 的引用时出现异常: {}", reportIdToDelete, e.getMessage(), e);
        }
    }
    
    /**
     * 清除所有父报表对即将删除的报表的引用，避免外键约束冲突
     * @param reportIdsToDelete 将要删除的报表ID列表
     * @deprecated 使用 clearAllReferencesToReport 替代
     */
    @Deprecated
    private void clearParentReferences(List<Long> reportIdsToDelete) {
        for (Long reportIdToDelete : reportIdsToDelete) {
            clearAllReferencesToReport(reportIdToDelete);
        }
    }
    
    /**
     * 递归删除子报表（保留此方法用于兼容，但推荐使用新的级联删除逻辑）
     * @param parentReport 父报表
     * @return 删除的子报表数量
     * @deprecated 使用 cascadeDeleteReportWithChildren 替代
     */
    @Deprecated
    private int cascadeDeleteChildren(ReportConfig parentReport) {
        int deletedCount = 0;
        
        if (parentReport.getLinkedReportId() != null) {
            try {
                ReportConfig childReport = reportConfigRepository.findById(parentReport.getLinkedReportId())
                    .orElse(null);
                
                if (childReport != null) {
                    log.info("删除子报表ID: {} (父报表ID: {})", childReport.getId(), parentReport.getId());
                    
                    // 递归删除子报表的子报表（如果存在）
                    deletedCount += cascadeDeleteChildren(childReport);
                    
                    // 删除当前子报表
                    reportConfigRepository.deleteById(childReport.getId());
                    deletedCount++;
                }
            } catch (Exception e) {
                log.warn("删除子报表ID: {} 时出现异常: {}", parentReport.getLinkedReportId(), e.getMessage());
            }
        }
        
        return deletedCount;
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
        
        // 验证子报表是否存在
        ReportConfig childReport = reportConfigRepository.findById(childReportId)
            .orElseThrow(() -> new IllegalArgumentException("Child report not found: " + childReportId));
        
        // 使用专门的部分更新方法更新父报表的关联关系
        updateParentReportLinking(parentReportId, childReportId, triggerParamField);
        
        // 返回更新后的父报表
        return reportConfigRepository.findById(parentReportId)
            .orElseThrow(() -> new IllegalArgumentException("Parent report not found after update: " + parentReportId));
    }

    @Override
    @Transactional
    public ReportConfig removeLinkedReport(Long parentReportId) {
        log.info("Removing linked report for parent: {}", parentReportId);
        
        // 使用专门的部分更新方法移除父报表的关联关系
        updateParentReportLinking(parentReportId, null, null);
        
        // 返回更新后的父报表
        return reportConfigRepository.findById(parentReportId)
            .orElseThrow(() -> new IllegalArgumentException("Parent report not found after update: " + parentReportId));
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
        childReport.setReportCode(generateUniqueReportCodeOld(parentReport.getReportCode() + "_LINKED"));
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
        childReport.setIsParentReport(0); // 标记为子报表，不在列表中显示
        
        // 保存子报表
        ReportConfig savedChildReport = reportConfigRepository.save(childReport);
        
        // 使用专门的部分更新方法更新父报表的关联关系
        updateParentReportLinking(parentReportId, savedChildReport.getId(), triggerParamField);
        
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
            
            // 确保父报表标记正确（如果前端没有传，默认为1）
            if (parentReport.getIsParentReport() == null) {
                parentReport.setIsParentReport(1);
            }
            
            // 验证必需字段
            if (parentReport.getDatasourceId() == null) {
                throw new IllegalArgumentException("Parent report datasourceId is required");
            }
            if (parentReport.getSqlTemplateId() == null) {
                throw new IllegalArgumentException("Parent report sqlTemplateId is required");
            }
            if (parentReport.getReportName() == null || parentReport.getReportName().trim().isEmpty()) {
                throw new IllegalArgumentException("Parent report name is required");
            }
            
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
                // 先保存当前的关键字段值
                Long currentDatasourceId = parentReport.getDatasourceId();
                Long currentSqlTemplateId = parentReport.getSqlTemplateId();
                String currentReportName = parentReport.getReportName();
                
                updateReportFromMap(parentReport, parentReportConfig);
                
                // 确保关键字段不为空
                if (parentReport.getDatasourceId() == null && currentDatasourceId != null) {
                    parentReport.setDatasourceId(currentDatasourceId);
                    log.warn("Restored datasourceId from current value: {}", currentDatasourceId);
                }
                if (parentReport.getSqlTemplateId() == null && currentSqlTemplateId != null) {
                    parentReport.setSqlTemplateId(currentSqlTemplateId);
                    log.warn("Restored sqlTemplateId from current value: {}", currentSqlTemplateId);
                }
                if ((parentReport.getReportName() == null || parentReport.getReportName().trim().isEmpty()) && currentReportName != null) {
                    parentReport.setReportName(currentReportName);
                    log.warn("Restored reportName from current value: {}", currentReportName);
                }
                
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
        log.info("Child report config before updateReportFromMap: {}", childReportConfig);
        updateReportFromMap(childReport, childReportConfig);
        log.info("Child report isParentReport after updateReportFromMap: {}", childReport.getIsParentReport());
        
        // 设置子报表特有的关联信息
        childReport.setLinkedReportId(null); // 子报表本身不指向其他报表
        childReport.setTriggerParamField(null); // 子报表本身不包含触发参数
        childReport.setIsParentReport(0); // 强制标记为子报表，不在列表中显示
        log.info("Child report isParentReport after forced setting: {}", childReport.getIsParentReport());
        
        // 设置创建和更新信息
        childReport.setCreatedBy(createdBy);
        childReport.setUpdatedBy(createdBy);
        childReport.setCreatedTime(LocalDateTime.now());
        childReport.setUpdatedTime(LocalDateTime.now());
        childReport.setIsDeleted(0);
        
        // 保存子报表
        ReportConfig savedChildReport = reportConfigRepository.save(childReport);
        log.info("Child report created with ID: {}", savedChildReport.getId());
        
        // 3. 更新父报表的关联关系 - 使用专门的部分更新方法避免null字段问题
        updateParentReportLinking(parentReport.getId(), savedChildReport.getId(), triggerParamField);
        
        log.info("Successfully created linked report with parent data - childId: {}, parentId: {}", 
                savedChildReport.getId(), parentReport.getId());
        return savedChildReport;
    }
    
    /**
     * 从Map配置数据更新ReportConfig实体
     */
    private void updateReportFromMap(ReportConfig report, Map<String, Object> configMap) {
        if (configMap.get("reportName") != null && !configMap.get("reportName").toString().trim().isEmpty()) {
            report.setReportName(configMap.get("reportName").toString());
        }
        if (configMap.get("reportCode") != null && !configMap.get("reportCode").toString().trim().isEmpty()) {
            String requestedCode = configMap.get("reportCode").toString();
            // 确保报表代码唯一性
            String uniqueCode = generateUniqueReportCodeOld(requestedCode);
            report.setReportCode(uniqueCode);
        }
        if (configMap.get("description") != null) {
            report.setDescription(configMap.get("description").toString());
        }
        if (configMap.get("reportType") != null && !configMap.get("reportType").toString().trim().isEmpty()) {
            report.setReportType(configMap.get("reportType").toString());
        }
        if (configMap.get("accessLevel") != null && !configMap.get("accessLevel").toString().trim().isEmpty()) {
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
        if (configMap.get("version") != null && !configMap.get("version").toString().trim().isEmpty()) {
            report.setVersion(configMap.get("version").toString());
        }
        // 关键字段：只在有效值时才更新
        if (configMap.get("sqlTemplateId") != null && !configMap.get("sqlTemplateId").toString().trim().isEmpty()) {
            try {
                Long sqlTemplateId = Long.valueOf(configMap.get("sqlTemplateId").toString());
                if (sqlTemplateId > 0) {
                    report.setSqlTemplateId(sqlTemplateId);
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid sqlTemplateId value: {}", configMap.get("sqlTemplateId"));
            }
        }
        // 关键字段：只在有效值时才更新
        if (configMap.get("datasourceId") != null && !configMap.get("datasourceId").toString().trim().isEmpty()) {
            try {
                Long datasourceId = Long.valueOf(configMap.get("datasourceId").toString());
                if (datasourceId > 0) {
                    report.setDatasourceId(datasourceId);
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid datasourceId value: {}", configMap.get("datasourceId"));
            }
        }
        if (configMap.get("categoryId") != null && !configMap.get("categoryId").toString().trim().isEmpty()) {
            try {
                Long categoryId = Long.valueOf(configMap.get("categoryId").toString());
                if (categoryId > 0) {
                    report.setReportCategoryId(categoryId);
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid categoryId value: {}", configMap.get("categoryId"));
            }
        }
        if (configMap.get("isPublished") != null) {
            try {
                report.setIsPublished(Integer.valueOf(configMap.get("isPublished").toString()));
            } catch (NumberFormatException e) {
                log.warn("Invalid isPublished value: {}", configMap.get("isPublished"));
            }
        }
        if (configMap.get("isActive") != null) {
            try {
                report.setIsActive(Integer.valueOf(configMap.get("isActive").toString()));
            } catch (NumberFormatException e) {
                log.warn("Invalid isActive value: {}", configMap.get("isActive"));
            }
        }
        if (configMap.get("approvalStatus") != null) {
            try {
                report.setApprovalStatus(Integer.valueOf(configMap.get("approvalStatus").toString()));
            } catch (NumberFormatException e) {
                log.warn("Invalid approvalStatus value: {}", configMap.get("approvalStatus"));
            }
        }
        if (configMap.get("chartConfig") != null) {
            report.setChartConfig(configMap.get("chartConfig").toString());
        }
        if (configMap.get("isParentReport") != null) {
            try {
                report.setIsParentReport(Integer.valueOf(configMap.get("isParentReport").toString()));
            } catch (NumberFormatException e) {
                log.warn("Invalid isParentReport value: {}", configMap.get("isParentReport"));
            }
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
    private String generateUniqueReportCodeOld(String baseCode) {
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
    
    /**
     * 专门用于更新父报表关联关系的方法，避免部分更新导致其他字段变为null的问题
     * @param parentReportId 父报表ID
     * @param childReportId 子报表ID（可以为null表示移除关联）
     * @param triggerParamField 触发参数字段（可以为null表示移除关联）
     */
    @Transactional
    private void updateParentReportLinking(Long parentReportId, Long childReportId, String triggerParamField) {
        log.info("Updating parent report linking - parentId: {}, childId: {}, triggerField: {}", 
                 parentReportId, childReportId, triggerParamField);
        
        // 先获取完整的父报表实体
        ReportConfig parentReport = reportConfigRepository.findById(parentReportId)
            .orElseThrow(() -> new IllegalArgumentException("Parent report not found: " + parentReportId));
        
        log.info("Parent report before linking update - datasourceId: {}, sqlTemplateId: {}, linkedReportId: {}", 
                 parentReport.getDatasourceId(), parentReport.getSqlTemplateId(), parentReport.getLinkedReportId());
        
        // 只更新关联字段，保持其他字段不变
        parentReport.setLinkedReportId(childReportId);
        parentReport.setTriggerParamField(triggerParamField);
        parentReport.setUpdatedTime(LocalDateTime.now());
        
        // 验证关键字段是否存在（仅在需要的时候验证）
        if (parentReport.getDatasourceId() == null) {
            log.error("Critical error: datasourceId is null before save for parent report {}", parentReportId);
            throw new IllegalStateException("Parent report datasourceId cannot be null");
        }
        
        if (parentReport.getSqlTemplateId() == null) {
            log.error("Critical error: sqlTemplateId is null before save for parent report {}", parentReportId);
            throw new IllegalStateException("Parent report sqlTemplateId cannot be null");
        }
        
        log.info("Parent report before save - datasourceId: {}, sqlTemplateId: {}, linkedReportId: {}", 
                 parentReport.getDatasourceId(), parentReport.getSqlTemplateId(), parentReport.getLinkedReportId());
        
        // 保存更新
        ReportConfig savedParent = reportConfigRepository.save(parentReport);
        
        String operationType = (childReportId == null) ? "removed" : "set";
        log.info("Parent report linking {} - ID: {}, linkedReportId: {}, triggerParamField: {}, datasourceId: {}", 
                 operationType, savedParent.getId(), savedParent.getLinkedReportId(), 
                 savedParent.getTriggerParamField(), savedParent.getDatasourceId());
    }
}