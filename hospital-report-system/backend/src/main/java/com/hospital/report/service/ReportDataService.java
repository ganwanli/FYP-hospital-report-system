package com.hospital.report.service;

import java.util.List;
import java.util.Map;

public interface ReportDataService {

    Map<String, Object> executeDataQuery(Long dataSourceId, Map<String, Object> parameters);

    Map<String, Object> executeDataQuery(String sourceType, String queryConfig, Map<String, Object> parameters);

    Map<String, Object> getStaticData(String staticDataConfig);

    Map<String, Object> callApiEndpoint(String apiConfig, Map<String, Object> parameters);

    Map<String, Object> executeSqlTemplate(Long templateId, Map<String, Object> parameters);

    Map<String, Object> transformData(Map<String, Object> rawData, String transformConfig);

    List<Map<String, Object>> aggregateData(List<Map<String, Object>> data, String aggregateConfig);

    Map<String, Object> filterData(Map<String, Object> data, String filterConfig);

    Map<String, Object> getDataSourcePreview(Long dataSourceId, Integer limit);

    Map<String, Object> validateDataSource(Long dataSourceId);

    Map<String, Object> getDataSourceSchema(Long dataSourceId);

    void refreshDataSourceCache(Long dataSourceId);

    Map<String, Object> getDataSourceStatistics(Long dataSourceId);

    List<Map<String, Object>> getDataSourceHistory(Long dataSourceId, Integer days);

    Map<String, Object> combineDataSources(List<Long> dataSourceIds, String combineConfig);

    Map<String, Object> getRealtimeData(Long dataSourceId, Map<String, Object> parameters);

    void scheduleDataRefresh(Long dataSourceId, String cronExpression);

    Map<String, Object> exportData(Long dataSourceId, String format, Map<String, Object> parameters);
}