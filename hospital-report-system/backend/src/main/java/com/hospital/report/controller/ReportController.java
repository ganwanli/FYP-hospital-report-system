package com.hospital.report.controller;

import com.hospital.report.common.Result;
import com.hospital.report.dto.ReportConfigDTO;
import com.hospital.report.entity.ReportConfig;
import com.hospital.report.entity.ReportComponent;
import com.hospital.report.entity.ReportDataSource;
import com.hospital.report.service.ReportConfigService;
import com.hospital.report.service.ReportDataService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController   //按口方法返回矿象 转换成json文本
@RequestMapping("/report")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

//    private final ReportConfigService reportConfigService;
    private final ReportDataService reportDataService;
    @Autowired
    private ReportConfigService reportConfigService;

    // Report CRUD operations
    @PostMapping
    public Result<ReportConfig> createReport(@RequestBody ReportConfigDTO reportConfig) {
        try {
            ReportConfig created = reportConfigService.createReport(reportConfig);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("Failed to create report: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<ReportConfig> updateReport(@PathVariable Long id, @RequestBody ReportConfig reportConfig) {
        try {
            reportConfig.setId(id);
            ReportConfig updated = reportConfigService.updateReport(reportConfig);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("Failed to update report: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReport(@PathVariable Long id) {
        try {
            reportConfigService.deleteReport(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to delete report: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<ReportConfig> getReport(@PathVariable Long id) {
        try {
            ReportConfig report = reportConfigService.getReportById(id);
            if (report != null) {
                return Result.success(report);
            } else {
                return Result.error("Report not found");
            }
        } catch (Exception e) {
            return Result.error("Failed to get report: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<IPage<ReportConfig>> getReportList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "100") Integer size,
            @RequestParam(required = false) String reportName,
            @RequestParam(required = false) String reportCategory,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long createdBy,
            @RequestParam(required = false) String accessLevel) {
        try {
            Page<ReportConfig> pageObj = new Page<>(current, size);
            IPage<ReportConfig> result = reportConfigService.getReportList(pageObj, reportName, reportCategory,
                    reportType, isPublished, isActive, createdBy, accessLevel);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("Failed to get report list: " + e.getMessage());
        }
    }

    /**
     * 增加报表观看次数
     */
    @PutMapping("/increment-view-count/{id}")
    public Result<Map<String, Object>> incrementViewCount(@PathVariable long id) {
        try {
            // 调用服务层方法增加观看次数
            int newViewCount = reportConfigService.incrementViewCount(id);

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("reportId", id);
            data.put("newViewCount", newViewCount);

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("Failed to increment view count: " + e.getMessage());
        }
    }

    /**
     * 获取报表观看次数
     */
    @GetMapping("/{id}/view-count")
    public Result<Map<String, Object>> getViewCount(@PathVariable int id) {
        try {
            int viewCount = reportConfigService.getViewCount(id);

            Map<String, Object> data = new HashMap<>();
            data.put("reportId", id);
            data.put("viewCount", viewCount);

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("Failed to get view count: " + e.getMessage());
        }
    }

    /**
     * 重置报表观看次数
     */
    @PutMapping("/{id}/reset-view-count")
    public Result<Map<String, Object>> resetViewCount(@PathVariable int id) {
        try {
            reportConfigService.resetViewCount(id);

            Map<String, Object> data = new HashMap<>();
            data.put("reportId", id);
            data.put("newViewCount", 0L);

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("Failed to reset view count: " + e.getMessage());
        }
    }

    /**
    @GetMapping("/{id}/full")
    public Result<ReportConfig> getReportWithComponents(@PathVariable Long id) {
        try {
            ReportConfig report = reportConfigService.getReportWithComponents(id);
            if (report != null) {
                return Result.success(report);
            } else {
                return Result.error("Report not found");
            }
        } catch (Exception e) {
            return Result.error("Failed to get report: " + e.getMessage());
        }
    }



    @GetMapping("/search")
    public Result<List<ReportConfig>> searchReports(@RequestParam String keyword) {
        try {
            List<ReportConfig> reports = reportConfigService.searchReports(keyword);
            return Result.success(reports);
        } catch (Exception e) {
            return Result.error("Failed to search reports: " + e.getMessage());
        }
    }

    // Component management
    @PostMapping("/{reportId}/components")
    public Result<ReportComponent> addComponent(@PathVariable Long reportId, @RequestBody ReportComponent component) {
        try {
            component.setReportId(reportId);
            ReportComponent created = reportConfigService.addComponent(component);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("Failed to add component: " + e.getMessage());
        }
    }

    @PutMapping("/components/{componentId}")
    public Result<ReportComponent> updateComponent(@PathVariable Long componentId, @RequestBody ReportComponent component) {
        try {
            component.setComponentId(componentId);
            ReportComponent updated = reportConfigService.updateComponent(component);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("Failed to update component: " + e.getMessage());
        }
    }

    @DeleteMapping("/components/{componentId}")
    public Result<Void> deleteComponent(@PathVariable Long componentId) {
        try {
            reportConfigService.deleteComponent(componentId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to delete component: " + e.getMessage());
        }
    }

    @GetMapping("/{reportId}/components")
    public Result<List<ReportComponent>> getComponents(@PathVariable Long reportId) {
        try {
            List<ReportComponent> components = reportConfigService.getComponentsByReportId(reportId);
            return Result.success(components);
        } catch (Exception e) {
            return Result.error("Failed to get components: " + e.getMessage());
        }
    }

    @PutMapping("/components/{componentId}/position")
    public Result<Void> updateComponentPosition(
            @PathVariable Long componentId,
            @RequestParam Integer x,
            @RequestParam Integer y,
            @RequestParam Integer width,
            @RequestParam Integer height) {
        try {
            reportConfigService.updateComponentPosition(componentId, x, y, width, height);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to update component position: " + e.getMessage());
        }
    }

    @PutMapping("/components/{componentId}/z-index")
    public Result<Void> updateComponentZIndex(@PathVariable Long componentId, @RequestParam Integer zIndex) {
        try {
            reportConfigService.updateComponentZIndex(componentId, zIndex);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to update component z-index: " + e.getMessage());
        }
    }

    @PutMapping("/components/{componentId}/visibility")
    public Result<Void> updateComponentVisibility(@PathVariable Long componentId, @RequestParam Boolean isVisible) {
        try {
            reportConfigService.updateComponentVisibility(componentId, isVisible);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to update component visibility: " + e.getMessage());
        }
    }

    @PutMapping("/components/{componentId}/lock")
    public Result<Void> updateComponentLock(@PathVariable Long componentId, @RequestParam Boolean isLocked) {
        try {
            reportConfigService.updateComponentLock(componentId, isLocked);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to update component lock: " + e.getMessage());
        }
    }

    // Data source management
    @PostMapping("/{reportId}/datasources")
    public Result<ReportDataSource> addDataSource(@PathVariable Long reportId, @RequestBody ReportDataSource dataSource) {
        try {
            dataSource.setReportId(reportId);
            ReportDataSource created = reportConfigService.addDataSource(dataSource);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("Failed to add data source: " + e.getMessage());
        }
    }

    @PutMapping("/datasources/{dataSourceId}")
    public Result<ReportDataSource> updateDataSource(@PathVariable Long dataSourceId, @RequestBody ReportDataSource dataSource) {
        try {
            dataSource.setDataSourceId(dataSourceId);
            ReportDataSource updated = reportConfigService.updateDataSource(dataSource);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("Failed to update data source: " + e.getMessage());
        }
    }

    @DeleteMapping("/datasources/{dataSourceId}")
    public Result<Void> deleteDataSource(@PathVariable Long dataSourceId) {
        try {
            reportConfigService.deleteDataSource(dataSourceId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to delete data source: " + e.getMessage());
        }
    }

    @GetMapping("/{reportId}/datasources")
    public Result<List<ReportDataSource>> getDataSources(@PathVariable Long reportId) {
        try {
            List<ReportDataSource> dataSources = reportConfigService.getDataSourcesByReportId(reportId);
            return Result.success(dataSources);
        } catch (Exception e) {
            return Result.error("Failed to get data sources: " + e.getMessage());
        }
    }

    @PostMapping("/datasources/{dataSourceId}/test")
    public Result<Map<String, Object>> testDataSource(@PathVariable Long dataSourceId) {
        try {
            Map<String, Object> result = reportDataService.validateDataSource(dataSourceId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("Failed to test data source: " + e.getMessage());
        }
    }

    @PostMapping("/datasources/{dataSourceId}/refresh")
    public Result<Void> refreshDataSource(@PathVariable Long dataSourceId) {
        try {
            reportConfigService.refreshDataSource(dataSourceId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to refresh data source: " + e.getMessage());
        }
    }

    @GetMapping("/datasources/{dataSourceId}/preview")
    public Result<Map<String, Object>> previewDataSource(
            @PathVariable Long dataSourceId,
            @RequestParam(defaultValue = "100") Integer limit) {
        try {
            Map<String, Object> preview = reportDataService.getDataSourcePreview(dataSourceId, limit);
            return Result.success(preview);
        } catch (Exception e) {
            return Result.error("Failed to preview data source: " + e.getMessage());
        }
    }

    @GetMapping("/datasources/{dataSourceId}/schema")
    public Result<Map<String, Object>> getDataSourceSchema(@PathVariable Long dataSourceId) {
        try {
            Map<String, Object> schema = reportDataService.getDataSourceSchema(dataSourceId);
            return Result.success(schema);
        } catch (Exception e) {
            return Result.error("Failed to get data source schema: " + e.getMessage());
        }
    }

    /***
     * 报表预览
     * 暂时屏蔽这个方法
    // Report preview and rendering
    @PostMapping("/{reportId}/preview")
    public Result<Map<String, Object>> previewReport(@PathVariable Long reportId, @RequestBody(required = false) Map<String, Object> parameters) {
        try {
            Map<String, Object> preview = generateReportPreview(reportId, parameters);
            return Result.success(preview);
        } catch (Exception e) {
            return Result.error("Failed to preview report: " + e.getMessage());
        }
    }
    **/

    /**
     * 报表渲染
     * 暂时屏蔽
    @PostMapping("/{reportId}/render")
    public Result<Map<String, Object>> renderReport(@PathVariable Long reportId, @RequestBody(required = false) Map<String, Object> parameters) {
        try {
            Map<String, Object> rendered = generateReportData(reportId, parameters);
            return Result.success(rendered);
        } catch (Exception e) {
            return Result.error("Failed to render report: " + e.getMessage());
        }
    }


    // Publishing and sharing
    @PostMapping("/{reportId}/publish")
    public Result<Void> publishReport(@PathVariable Long reportId) {
        try {
            reportConfigService.publishReport(reportId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to publish report: " + e.getMessage());
        }
    }

    @PostMapping("/{reportId}/unpublish")
    public Result<Void> unpublishReport(@PathVariable Long reportId) {
        try {
            reportConfigService.unpublishReport(reportId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to unpublish report: " + e.getMessage());
        }
    }

    // Report operations
    @PostMapping("/{reportId}/duplicate")
    public Result<ReportConfig> duplicateReport(@PathVariable Long reportId, @RequestParam String newName, @RequestParam Long userId) {
        try {
            ReportConfig duplicated = reportConfigService.duplicateReport(reportId, newName, userId);
            return Result.success(duplicated);
        } catch (Exception e) {
            return Result.error("Failed to duplicate report: " + e.getMessage());
        }
    }

    @GetMapping("/{reportId}/export")
    public Result<String> exportReport(@PathVariable Long reportId) {
        try {
            String exported = reportConfigService.exportReport(reportId);
            return Result.success(exported);
        } catch (Exception e) {
            return Result.error("Failed to export report: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public Result<ReportConfig> importReport(@RequestParam String reportData, @RequestParam Long userId) {
        try {
            ReportConfig imported = reportConfigService.importReport(reportData, userId);
            return Result.success(imported);
        } catch (Exception e) {
            return Result.error("Failed to import report: " + e.getMessage());
        }
    }

    @PostMapping("/{reportId}/thumbnail")
    public Result<Void> updateThumbnail(@PathVariable Long reportId, @RequestParam("file") MultipartFile file) {
        try {
            // TODO: Implement file upload and thumbnail generation
            String thumbnail = "thumbnail_" + reportId + "_" + System.currentTimeMillis();
            reportConfigService.updateThumbnail(reportId, thumbnail);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to update thumbnail: " + e.getMessage());
        }
    }

    // Statistics and metadata
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getReportStatistics() {
        try {
            Map<String, Object> statistics = reportConfigService.getReportStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error("Failed to get report statistics: " + e.getMessage());
        }
    }

    @GetMapping("/categories")
    public Result<List<String>> getAllCategories() {
        try {
            List<String> categories = reportConfigService.getAllCategories();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error("Failed to get categories: " + e.getMessage());
        }
    }

    @GetMapping("/types")
    public Result<List<String>> getAllTypes() {
        try {
            List<String> types = reportConfigService.getAllTypes();
            return Result.success(types);
        } catch (Exception e) {
            return Result.error("Failed to get types: " + e.getMessage());
        }
    }

    @GetMapping("/templates")
    public Result<List<ReportConfig>> getReportTemplates() {
        try {
            List<ReportConfig> templates = reportConfigService.getReportTemplates();
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("Failed to get report templates: " + e.getMessage());
        }
    }

    // Version management
    @PostMapping("/{reportId}/versions")
    public Result<Void> saveVersion(@PathVariable Long reportId, @RequestParam String description, @RequestParam Long userId) {
        try {
            reportConfigService.saveVersion(reportId, description, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to save version: " + e.getMessage());
        }
    }

    @GetMapping("/{reportId}/versions")
    public Result<List<Map<String, Object>>> getReportVersions(@PathVariable Long reportId) {
        try {
            List<Map<String, Object>> versions = reportConfigService.getReportVersions(reportId);
            return Result.success(versions);
        } catch (Exception e) {
            return Result.error("Failed to get report versions: " + e.getMessage());
        }
    }

    @PostMapping("/{reportId}/versions/{versionId}/restore")
    public Result<Void> restoreVersion(@PathVariable Long reportId, @PathVariable Long versionId) {
        try {
            reportConfigService.restoreVersion(reportId, versionId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to restore version: " + e.getMessage());
        }
    }


     * 生成报表预览数据
     暂时屏蔽这个方法

    // Helper methods for report preview and rendering
    private Map<String, Object> generateReportPreview(Long reportId, Map<String, Object> parameters) {
        // Get report configuration
        ReportConfig report = reportConfigService.getReportWithComponents(reportId);
        if (report == null) {
            throw new RuntimeException("Report not found");
        }

        // Generate preview data for each component
        Map<String, Object> preview = new HashMap<>();
        preview.put("reportId", reportId);
        preview.put("reportName", report.getReportName());
        preview.put("canvasWidth", report.getCanvasWidth());
        preview.put("canvasHeight", report.getCanvasHeight());

        // Load component data
        List<Map<String, Object>> componentData = new ArrayList<>();
        for (ReportComponent component : report.getComponents()) {
            Map<String, Object> compData = new HashMap<>();
            compData.put("componentId", component.getComponentId());
            compData.put("componentType", component.getComponentType());
            compData.put("componentName", component.getComponentName());
            compData.put("position", Map.of(
                "x", component.getPositionX(),
                "y", component.getPositionY(),
                "width", component.getWidth(),
                "height", component.getHeight()
            ));
            compData.put("isVisible", component.getIsVisible());
            compData.put("isLocked", component.getIsLocked());
            compData.put("zIndex", component.getZIndex());
            
            // Load component data if data source is configured
            if (component.getDataSourceId() != null) {
                try {
                    Map<String, Object> dataResult = reportDataService.getDataSourcePreview(component.getDataSourceId(), 50);
                    compData.put("data", dataResult);
                } catch (Exception e) {
                    compData.put("dataError", e.getMessage());
                }
            }
            
            componentData.add(compData);
        }
        
        preview.put("components", componentData);
        preview.put("dataSources", report.getDataSources());
        preview.put("layoutConfig", report.getLayoutConfig());
        preview.put("styleConfig", report.getStyleConfig());
        preview.put("generatedAt", System.currentTimeMillis());
        
        return preview;
    }
    * */

    /**
     * 生成报表数据
     * 暂时屏蔽这个方法
     *
    private Map<String, Object> generateReportData(Long reportId, Map<String, Object> parameters) {
        // Similar to preview but with full data
        ReportConfig report = reportConfigService.getReportWithComponents(reportId);
        if (report == null) {
            throw new RuntimeException("Report not found");
        }

        Map<String, Object> rendered = new HashMap<>();
        rendered.put("reportId", reportId);
        rendered.put("reportName", report.getReportName());
        rendered.put("canvasWidth", report.getCanvasWidth());
        rendered.put("canvasHeight", report.getCanvasHeight());
        
        // Load full component data
        List<Map<String, Object>> componentData = new ArrayList<>();
        for (ReportComponent component : report.getComponents()) {
            Map<String, Object> compData = new HashMap<>();
            compData.put("componentId", component.getComponentId());
            compData.put("componentType", component.getComponentType());
            compData.put("componentName", component.getComponentName());
            compData.put("position", Map.of(
                "x", component.getPositionX(),
                "y", component.getPositionY(),
                "width", component.getWidth(),
                "height", component.getHeight()
            ));
            compData.put("isVisible", component.getIsVisible());
            compData.put("zIndex", component.getZIndex());
            
            // Load full component data
            if (component.getDataSourceId() != null) {
                try {
                    Map<String, Object> dataResult = reportDataService.executeDataQuery(component.getDataSourceId(), parameters != null ? parameters : new HashMap<>());
                    compData.put("data", dataResult);
                } catch (Exception e) {
                    compData.put("dataError", e.getMessage());
                }
            }
            
            componentData.add(compData);
        }
        
        rendered.put("components", componentData);
        rendered.put("dataSources", report.getDataSources());
        rendered.put("layoutConfig", report.getLayoutConfig());
        rendered.put("styleConfig", report.getStyleConfig());
        rendered.put("parameters", parameters);
        rendered.put("generatedAt", System.currentTimeMillis());
        
        return rendered;
    }
    * */
}