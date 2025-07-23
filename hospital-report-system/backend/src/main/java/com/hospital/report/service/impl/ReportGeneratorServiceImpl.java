package com.hospital.report.service.impl;

import com.hospital.report.entity.ReportConfig;
import com.hospital.report.entity.ReportComponent;
import com.hospital.report.entity.ReportDataSource;
import com.hospital.report.service.ReportGeneratorService;
import com.hospital.report.service.ReportConfigService;
import com.hospital.report.service.ReportDataService;
import com.hospital.report.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGeneratorServiceImpl implements ReportGeneratorService {
    //暂时屏蔽
//
//    private final ReportConfigService reportConfigService;
//    private final ReportDataService reportDataService;
//    private final CacheService cacheService;
//    private final ObjectMapper objectMapper;
//
//    private static final String CACHE_KEY_PREFIX = "report:generated:";
//    private static final String PARAM_CACHE_KEY_PREFIX = "report:params:";
//    private static final int DEFAULT_CACHE_DURATION = 1800; // 30分钟
//
//    @Override
//    public Map<String, Object> generateReport(Long reportId, Map<String, Object> parameters) {
//        log.info("开始生成报表，reportId: {}, parameters: {}", reportId, parameters);
//
//        long startTime = System.currentTimeMillis();
//
//        try {
//            // 检查缓存
//            String cacheKey = buildCacheKey(reportId, parameters);
//            Map<String, Object> cachedResult = cacheService.get(cacheKey, Map.class);
//            if (cachedResult != null) {
//                log.info("从缓存获取报表数据，reportId: {}", reportId);
//                cachedResult.put("fromCache", true);
//                cachedResult.put("generationTime", System.currentTimeMillis() - startTime);
//                return cachedResult;
//            }
//
//            // 获取报表配置
//            ReportConfig reportConfig = reportConfigService.getReportWithComponents(reportId);
//            if (reportConfig == null) {
//                throw new RuntimeException("报表配置不存在: " + reportId);
//            }
//
//            // 验证参数
//            if (!validateParameters(reportId, parameters)) {
//                throw new RuntimeException("报表参数验证失败");
//            }
//
//            // 构建基础报表信息
//            Map<String, Object> result = new HashMap<>();
//            result.put("reportId", reportId);
//            result.put("reportName", reportConfig.getReportName());
//            result.put("reportDescription", reportConfig.getReportDescription());
//            result.put("canvasWidth", reportConfig.getCanvasWidth());
//            result.put("canvasHeight", reportConfig.getCanvasHeight());
//            result.put("generatedAt", System.currentTimeMillis());
//            result.put("parameters", parameters);
//
//            // 解析布局配置
//            Map<String, Object> layoutConfig = parseJsonConfig(reportConfig.getLayoutConfig());
//            result.put("layoutConfig", layoutConfig);
//
//            // 解析样式配置
//            Map<String, Object> styleConfig = parseJsonConfig(reportConfig.getStyleConfig());
//            result.put("styleConfig", styleConfig);
//
//            // 并行生成组件数据
//            List<ReportComponent> components = reportConfig.getComponents();
//            List<ReportDataSource> dataSources = reportConfig.getDataSources();
//
//            Map<Long, ReportDataSource> dataSourceMap = dataSources.stream()
//                .collect(Collectors.toMap(ReportDataSource::getDataSourceId, ds -> ds));
//
//            // 并行处理组件
//            List<CompletableFuture<Map<String, Object>>> componentFutures = components.stream()
//                .filter(component -> component.getIsVisible() != null && component.getIsVisible())
//                .map(component -> CompletableFuture.supplyAsync(() -> {
//                    try {
//                        ReportDataSource dataSource = dataSourceMap.get(component.getDataSourceId());
//                        return generateComponentData(component, dataSource, parameters);
//                    } catch (Exception e) {
//                        log.error("组件数据生成失败, componentId: {}", component.getComponentId(), e);
//                        return createErrorComponent(component, e.getMessage());
//                    }
//                }))
//                .collect(Collectors.toList());
//
//            // 等待所有组件数据生成完成
//            List<Map<String, Object>> componentResults = componentFutures.stream()
//                .map(CompletableFuture::join)
//                .collect(Collectors.toList());
//
//            result.put("components", componentResults);
//
//            // 计算生成统计信息
//            long generationTime = System.currentTimeMillis() - startTime;
//            result.put("generationTime", generationTime);
//            result.put("componentCount", componentResults.size());
//            result.put("fromCache", false);
//
//            // 缓存结果
//            int cacheDuration = determineCacheDuration(reportConfig, componentResults);
//            cacheService.set(cacheKey, result, cacheDuration);
//
//            log.info("报表生成完成，reportId: {}, 耗时: {}ms, 组件数: {}",
//                reportId, generationTime, componentResults.size());
//
//            return result;
//
//        } catch (Exception e) {
//            log.error("报表生成失败，reportId: {}", reportId, e);
//            throw new RuntimeException("报表生成失败: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public Map<String, Object> generateComponentData(ReportComponent component, ReportDataSource dataSource, Map<String, Object> parameters) {
//        Map<String, Object> componentData = new HashMap<>();
//
//        // 基础组件信息
//        componentData.put("componentId", component.getComponentId());
//        componentData.put("componentType", component.getComponentType());
//        componentData.put("componentName", component.getComponentName());
//        componentData.put("position", Map.of(
//            "x", component.getPositionX(),
//            "y", component.getPositionY(),
//            "width", component.getWidth(),
//            "height", component.getHeight()
//        ));
//        componentData.put("zIndex", component.getZIndex());
//        componentData.put("isVisible", component.getIsVisible());
//
//        // 解析配置
//        Map<String, Object> dataConfig = parseJsonConfig(component.getDataConfig());
//        Map<String, Object> styleConfig = parseJsonConfig(component.getStyleConfig());
//        Map<String, Object> chartConfig = parseJsonConfig(component.getChartConfig());
//        Map<String, Object> tableConfig = parseJsonConfig(component.getTableConfig());
//        Map<String, Object> textConfig = parseJsonConfig(component.getTextConfig());
//        Map<String, Object> imageConfig = parseJsonConfig(component.getImageConfig());
//
//        componentData.put("dataConfig", dataConfig);
//        componentData.put("styleConfig", styleConfig);
//        componentData.put("chartConfig", chartConfig);
//        componentData.put("tableConfig", tableConfig);
//        componentData.put("textConfig", textConfig);
//        componentData.put("imageConfig", imageConfig);
//
//        // 生成数据
//        if (dataSource != null && component.getDataSourceId() != null) {
//            try {
//                Map<String, Object> rawData = reportDataService.executeDataQuery(component.getDataSourceId(), parameters);
//                Map<String, Object> processedData = processComponentData(component, rawData, dataConfig);
//                componentData.put("data", processedData);
//                componentData.put("dataError", null);
//            } catch (Exception e) {
//                log.error("组件数据查询失败, componentId: {}, dataSourceId: {}",
//                    component.getComponentId(), component.getDataSourceId(), e);
//                componentData.put("data", null);
//                componentData.put("dataError", e.getMessage());
//            }
//        }
//
//        return componentData;
//    }
//
//    @Override
//    public boolean validateParameters(Long reportId, Map<String, Object> parameters) {
//        try {
//            List<Map<String, Object>> paramDefs = getParameterDefinitions(reportId);
//
//            for (Map<String, Object> paramDef : paramDefs) {
//                String paramName = (String) paramDef.get("name");
//                Boolean required = (Boolean) paramDef.getOrDefault("required", false);
//                String dataType = (String) paramDef.get("dataType");
//
//                Object value = parameters.get(paramName);
//
//                // 检查必填参数
//                if (required && (value == null || value.toString().trim().isEmpty())) {
//                    log.warn("必填参数缺失: {}", paramName);
//                    return false;
//                }
//
//                // 检查数据类型
//                if (value != null && !validateDataType(value, dataType)) {
//                    log.warn("参数类型不匹配: {} -> {}", paramName, dataType);
//                    return false;
//                }
//            }
//
//            return true;
//        } catch (Exception e) {
//            log.error("参数验证失败, reportId: {}", reportId, e);
//            return false;
//        }
//    }
//
//    @Override
//    public List<Map<String, Object>> getParameterDefinitions(Long reportId) {
//        String cacheKey = PARAM_CACHE_KEY_PREFIX + reportId;
//        List<Map<String, Object>> cachedParams = cacheService.get(cacheKey, List.class);
//
//        if (cachedParams != null) {
//            return cachedParams;
//        }
//
//        try {
//            ReportConfig reportConfig = reportConfigService.getReportWithComponents(reportId);
//            if (reportConfig == null) {
//                return new ArrayList<>();
//            }
//
//            // 从报表配置中提取参数定义
//            List<Map<String, Object>> parameters = new ArrayList<>();
//
//            // 解析组件中的参数引用
//            for (ReportComponent component : reportConfig.getComponents()) {
//                Map<String, Object> dataConfig = parseJsonConfig(component.getDataConfig());
//                extractParametersFromConfig(dataConfig, parameters);
//            }
//
//            // 解析数据源中的参数引用
//            for (ReportDataSource dataSource : reportConfig.getDataSources()) {
//                Map<String, Object> parametersConfig = parseJsonConfig(dataSource.getParametersConfig());
//                if (parametersConfig != null && !parametersConfig.isEmpty()) {
//                    List<Map<String, Object>> dsParams = (List<Map<String, Object>>) parametersConfig.get("parameters");
//                    if (dsParams != null) {
//                        parameters.addAll(dsParams);
//                    }
//                }
//            }
//
//            // 去重并排序
//            List<Map<String, Object>> uniqueParameters = parameters.stream()
//                .filter(Objects::nonNull)
//                .collect(Collectors.toMap(
//                    p -> (String) p.get("name"),
//                    p -> p,
//                    (existing, replacement) -> existing
//                ))
//                .values()
//                .stream()
//                .sorted((p1, p2) -> {
//                    Integer order1 = (Integer) p1.getOrDefault("order", 999);
//                    Integer order2 = (Integer) p2.getOrDefault("order", 999);
//                    return order1.compareTo(order2);
//                })
//                .collect(Collectors.toList());
//
//            // 缓存参数定义
//            cacheService.set(cacheKey, uniqueParameters, 3600); // 缓存1小时
//
//            return uniqueParameters;
//
//        } catch (Exception e) {
//            log.error("获取参数定义失败, reportId: {}", reportId, e);
//            return new ArrayList<>();
//        }
//    }
//
//    @Override
//    public byte[] generateThumbnail(Long reportId, Map<String, Object> parameters) {
//        // TODO: 实现缩略图生成
//        // 可以使用无头浏览器或图像处理库生成缩略图
//        log.info("生成报表缩略图: {}", reportId);
//        return new byte[0];
//    }
//
//    @Override
//    public long estimateGenerationTime(Long reportId, Map<String, Object> parameters) {
//        try {
//            ReportConfig reportConfig = reportConfigService.getReportWithComponents(reportId);
//            if (reportConfig == null) {
//                return 1000; // 默认估计1秒
//            }
//
//            int componentCount = reportConfig.getComponents().size();
//            int dataSourceCount = reportConfig.getDataSources().size();
//
//            // 基于组件数量和数据源数量估算时间
//            long baseTime = 500; // 基础时间500ms
//            long componentTime = componentCount * 200; // 每个组件200ms
//            long dataSourceTime = dataSourceCount * 300; // 每个数据源300ms
//
//            return baseTime + componentTime + dataSourceTime;
//
//        } catch (Exception e) {
//            log.error("估算生成时间失败, reportId: {}", reportId, e);
//            return 2000; // 默认估计2秒
//        }
//    }
//
//    // 私有辅助方法
//
//    private String buildCacheKey(Long reportId, Map<String, Object> parameters) {
//        String paramHash = parameters.entrySet().stream()
//            .sorted(Map.Entry.comparingByKey())
//            .map(entry -> entry.getKey() + "=" + entry.getValue())
//            .collect(Collectors.joining("&"));
//
//        return CACHE_KEY_PREFIX + reportId + ":" + paramHash.hashCode();
//    }
//
//    private Map<String, Object> parseJsonConfig(String jsonConfig) {
//        try {
//            if (jsonConfig == null || jsonConfig.trim().isEmpty()) {
//                return new HashMap<>();
//            }
//            return objectMapper.readValue(jsonConfig, new TypeReference<Map<String, Object>>() {});
//        } catch (Exception e) {
//            log.warn("JSON配置解析失败: {}", jsonConfig, e);
//            return new HashMap<>();
//        }
//    }
//
//    private Map<String, Object> createErrorComponent(ReportComponent component, String errorMessage) {
//        Map<String, Object> errorData = new HashMap<>();
//        errorData.put("componentId", component.getComponentId());
//        errorData.put("componentType", component.getComponentType());
//        errorData.put("componentName", component.getComponentName());
//        errorData.put("error", true);
//        errorData.put("errorMessage", errorMessage);
//        errorData.put("position", Map.of(
//            "x", component.getPositionX(),
//            "y", component.getPositionY(),
//            "width", component.getWidth(),
//            "height", component.getHeight()
//        ));
//        return errorData;
//    }
//
//    private int determineCacheDuration(ReportConfig reportConfig, List<Map<String, Object>> componentResults) {
//        // 根据报表刷新间隔和数据源刷新频率确定缓存时长
//        int minRefreshInterval = reportConfig.getDataSources().stream()
//            .mapToInt(ds -> ds.getRefreshInterval() != null ? ds.getRefreshInterval() : DEFAULT_CACHE_DURATION)
//            .min()
//            .orElse(DEFAULT_CACHE_DURATION);
//
//        return Math.min(minRefreshInterval, DEFAULT_CACHE_DURATION);
//    }
//
//    private Map<String, Object> processComponentData(ReportComponent component, Map<String, Object> rawData, Map<String, Object> dataConfig) {
//        Map<String, Object> processedData = new HashMap<>(rawData);
//
//        try {
//            // 应用过滤条件
//            String filterCondition = (String) dataConfig.get("filterCondition");
//            if (filterCondition != null && !filterCondition.trim().isEmpty()) {
//                // TODO: 实现数据过滤逻辑
//                log.debug("应用过滤条件: {}", filterCondition);
//            }
//
//            // 应用排序
//            String sortField = (String) dataConfig.get("sortField");
//            String sortOrder = (String) dataConfig.get("sortOrder");
//            if (sortField != null && !sortField.trim().isEmpty()) {
//                // TODO: 实现数据排序逻辑
//                log.debug("应用排序: {} {}", sortField, sortOrder);
//            }
//
//            // 应用数据限制
//            Integer limit = (Integer) dataConfig.get("limit");
//            if (limit != null && limit > 0) {
//                List<Map<String, Object>> records = (List<Map<String, Object>>) processedData.get("records");
//                if (records != null && records.size() > limit) {
//                    processedData.put("records", records.subList(0, limit));
//                    processedData.put("truncated", true);
//                    processedData.put("originalCount", records.size());
//                }
//            }
//
//        } catch (Exception e) {
//            log.error("数据处理失败, componentId: {}", component.getComponentId(), e);
//        }
//
//        return processedData;
//    }
//
//    private void extractParametersFromConfig(Map<String, Object> config, List<Map<String, Object>> parameters) {
//        // 从配置中提取参数引用（如 ${paramName}）
//        for (Map.Entry<String, Object> entry : config.entrySet()) {
//            String value = entry.getValue().toString();
//            if (value.matches(".*\\$\\{[^}]+\\}.*")) {
//                // 提取参数名
//                String paramName = value.replaceAll(".*\\$\\{([^}]+)\\}.*", "$1");
//
//                Map<String, Object> paramDef = new HashMap<>();
//                paramDef.put("name", paramName);
//                paramDef.put("label", paramName);
//                paramDef.put("dataType", "string");
//                paramDef.put("required", false);
//                paramDef.put("defaultValue", "");
//
//                parameters.add(paramDef);
//            }
//        }
//    }
//
//    private boolean validateDataType(Object value, String dataType) {
//        if (dataType == null) return true;
//
//        try {
//            switch (dataType.toLowerCase()) {
//                case "string":
//                    return true; // 任何值都可以转为字符串
//                case "number":
//                case "integer":
//                    Double.parseDouble(value.toString());
//                    return true;
//                case "boolean":
//                    return value instanceof Boolean ||
//                           "true".equalsIgnoreCase(value.toString()) ||
//                           "false".equalsIgnoreCase(value.toString());
//                case "date":
//                    // TODO: 实现日期验证
//                    return true;
//                default:
//                    return true;
//            }
//        } catch (Exception e) {
//            return false;
//        }
//    }
}