package com.hospital.report.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.report.entity.ReportDataSource;
import com.hospital.report.mapper.ReportDataSourceMapper;
import com.hospital.report.service.ReportDataService;
import com.hospital.report.service.SqlExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportDataServiceImpl implements ReportDataService {

    private final ReportDataSourceMapper dataSourceMapper;
    private final SqlExecutionService sqlExecutionService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public Map<String, Object> executeDataQuery(Long dataSourceId, Map<String, Object> parameters) {
        ReportDataSource dataSource = dataSourceMapper.selectByDataSourceId(dataSourceId);
        if (dataSource == null) {
            throw new RuntimeException("Data source not found");
        }

        try {
            Map<String, Object> result;
            
            switch (dataSource.getSourceType().toUpperCase()) {
                case "SQL":
                case "DATABASE":
                    result = executeDatabaseQuery(dataSource, parameters);
                    break;
                case "API":
                    result = executeApiQuery(dataSource, parameters);
                    break;
                case "STATIC":
                    result = getStaticData(dataSource.getStaticData());
                    break;
                case "TEMPLATE":
                    result = executeSqlTemplate(dataSource.getSqlTemplateId(), parameters);
                    break;
                default:
                    throw new RuntimeException("Unsupported data source type: " + dataSource.getSourceType());
            }

            // Apply data transformation if configured
            if (dataSource.getTransformConfig() != null) {
                result = transformData(result, dataSource.getTransformConfig());
            }

            // Update last refresh time
            dataSourceMapper.updateRefreshStatus(dataSourceId, null, 0);
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to execute data query for data source {}: {}", dataSourceId, e.getMessage(), e);
            
            // Update error status
            String errorMessage = e.getMessage();
            Integer errorCount = dataSource.getErrorCount() != null ? dataSource.getErrorCount() + 1 : 1;
            dataSourceMapper.updateRefreshStatus(dataSourceId, errorMessage, errorCount);
            
            throw new RuntimeException("Data query execution failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> executeDataQuery(String sourceType, String queryConfig, Map<String, Object> parameters) {
        try {
            switch (sourceType.toUpperCase()) {
                case "SQL":
                case "DATABASE":
                    return executeSqlQuery(queryConfig, parameters);
                case "API":
                    return callApiEndpoint(queryConfig, parameters);
                case "STATIC":
                    return getStaticData(queryConfig);
                default:
                    throw new RuntimeException("Unsupported data source type: " + sourceType);
            }
        } catch (Exception e) {
            log.error("Failed to execute data query: {}", e.getMessage(), e);
            throw new RuntimeException("Data query execution failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getStaticData(String staticDataConfig) {
        try {
            if (staticDataConfig == null || staticDataConfig.trim().isEmpty()) {
                return createEmptyResult();
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> staticData = objectMapper.readValue(staticDataConfig, Map.class);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", staticData.get("data"));
            result.put("columns", staticData.get("columns"));
            result.put("total", getDataSize(staticData.get("data")));
            result.put("fromCache", false);
            
            return result;
            
        } catch (JsonProcessingException e) {
            log.error("Failed to parse static data config: {}", e.getMessage());
            throw new RuntimeException("Invalid static data configuration", e);
        }
    }

    @Override
    public Map<String, Object> callApiEndpoint(String apiConfig, Map<String, Object> parameters) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> config = objectMapper.readValue(apiConfig, Map.class);
            
            String url = (String) config.get("url");
            String method = (String) config.getOrDefault("method", "GET");
            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) config.getOrDefault("headers", new HashMap<>());
            
            // Replace parameters in URL
            if (parameters != null) {
                for (Map.Entry<String, Object> param : parameters.entrySet()) {
                    url = url.replace("${" + param.getKey() + "}", String.valueOf(param.getValue()));
                }
            }
            
            // Set up headers
            HttpHeaders httpHeaders = new HttpHeaders();
            headers.forEach(httpHeaders::set);
            
            // Create request entity
            HttpEntity<?> requestEntity;
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                requestEntity = new HttpEntity<>(parameters, httpHeaders);
            } else {
                requestEntity = new HttpEntity<>(httpHeaders);
            }
            
            // Execute request
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.valueOf(method.toUpperCase()), 
                requestEntity, 
                Map.class
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response.getBody());
            result.put("statusCode", response.getStatusCode().value());
            result.put("fromCache", false);
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to call API endpoint: {}", e.getMessage(), e);
            throw new RuntimeException("API call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> executeSqlTemplate(Long templateId, Map<String, Object> parameters) {
        try {
            return sqlExecutionService.executeQuery(templateId, parameters, 1L); // System user
        } catch (Exception e) {
            log.error("Failed to execute SQL template {}: {}", templateId, e.getMessage(), e);
            throw new RuntimeException("SQL template execution failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> transformData(Map<String, Object> rawData, String transformConfig) {
        try {
            if (transformConfig == null || transformConfig.trim().isEmpty()) {
                return rawData;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> config = objectMapper.readValue(transformConfig, Map.class);
            
            Object data = rawData.get("data");
            if (data instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) data;
                dataList = applyTransformations(dataList, config);
                
                Map<String, Object> result = new HashMap<>(rawData);
                result.put("data", dataList);
                result.put("total", dataList.size());
                
                return result;
            }
            
            return rawData;
            
        } catch (Exception e) {
            log.error("Failed to transform data: {}", e.getMessage(), e);
            return rawData; // Return original data if transformation fails
        }
    }

    @Override
    public List<Map<String, Object>> aggregateData(List<Map<String, Object>> data, String aggregateConfig) {
        try {
            if (aggregateConfig == null || aggregateConfig.trim().isEmpty()) {
                return data;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> config = objectMapper.readValue(aggregateConfig, Map.class);
            
            String groupBy = (String) config.get("groupBy");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> aggregations = (List<Map<String, Object>>) config.get("aggregations");
            
            if (groupBy == null || aggregations == null) {
                return data;
            }
            
            // Group data by specified field
            Map<Object, List<Map<String, Object>>> groupedData = data.stream()
                .collect(Collectors.groupingBy(row -> row.get(groupBy)));
            
            // Apply aggregations
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<Object, List<Map<String, Object>>> group : groupedData.entrySet()) {
                Map<String, Object> aggregatedRow = new HashMap<>();
                aggregatedRow.put(groupBy, group.getKey());
                
                for (Map<String, Object> agg : aggregations) {
                    String field = (String) agg.get("field");
                    String function = (String) agg.get("function");
                    String alias = (String) agg.getOrDefault("alias", field + "_" + function);
                    
                    Object value = applyAggregateFunction(group.getValue(), field, function);
                    aggregatedRow.put(alias, value);
                }
                
                result.add(aggregatedRow);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to aggregate data: {}", e.getMessage(), e);
            return data;
        }
    }

    @Override
    public Map<String, Object> filterData(Map<String, Object> data, String filterConfig) {
        try {
            if (filterConfig == null || filterConfig.trim().isEmpty()) {
                return data;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> config = objectMapper.readValue(filterConfig, Map.class);
            
            Object dataObj = data.get("data");
            if (dataObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataObj;
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> filters = (List<Map<String, Object>>) config.get("filters");
                
                if (filters != null) {
                    for (Map<String, Object> filter : filters) {
                        dataList = applyFilter(dataList, filter);
                    }
                }
                
                Map<String, Object> result = new HashMap<>(data);
                result.put("data", dataList);
                result.put("total", dataList.size());
                
                return result;
            }
            
            return data;
            
        } catch (Exception e) {
            log.error("Failed to filter data: {}", e.getMessage(), e);
            return data;
        }
    }

    @Override
    public Map<String, Object> getDataSourcePreview(Long dataSourceId, Integer limit) {
        Map<String, Object> result = executeDataQuery(dataSourceId, new HashMap<>());
        
        Object data = result.get("data");
        if (data instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) data;
            
            if (limit != null && dataList.size() > limit) {
                dataList = dataList.subList(0, limit);
                result.put("data", dataList);
                result.put("truncated", true);
                result.put("displayRows", limit);
            }
        }
        
        return result;
    }

    @Override
    public Map<String, Object> validateDataSource(Long dataSourceId) {
        try {
            ReportDataSource dataSource = dataSourceMapper.selectByDataSourceId(dataSourceId);
            if (dataSource == null) {
                return createValidationResult(false, "Data source not found");
            }
            
            // Test data source connection/query
            Map<String, Object> testResult = executeDataQuery(dataSourceId, new HashMap<>());
            
            if ((Boolean) testResult.get("success")) {
                return createValidationResult(true, "Data source is valid and accessible");
            } else {
                return createValidationResult(false, "Data source validation failed: " + testResult.get("errorMessage"));
            }
            
        } catch (Exception e) {
            return createValidationResult(false, "Validation error: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDataSourceSchema(Long dataSourceId) {
        try {
            Map<String, Object> preview = getDataSourcePreview(dataSourceId, 1);
            
            Object data = preview.get("data");
            if (data instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) data;
                
                if (!dataList.isEmpty()) {
                    Map<String, Object> firstRow = dataList.get(0);
                    List<Map<String, Object>> schema = new ArrayList<>();
                    
                    for (Map.Entry<String, Object> entry : firstRow.entrySet()) {
                        Map<String, Object> field = new HashMap<>();
                        field.put("name", entry.getKey());
                        field.put("type", inferDataType(entry.getValue()));
                        field.put("nullable", entry.getValue() == null);
                        schema.add(field);
                    }
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("schema", schema);
                    result.put("fieldCount", schema.size());
                    
                    return result;
                }
            }
            
            return createErrorResult("No data available to infer schema");
            
        } catch (Exception e) {
            return createErrorResult("Failed to get schema: " + e.getMessage());
        }
    }

    @Override
    public void refreshDataSourceCache(Long dataSourceId) {
        try {
            executeDataQuery(dataSourceId, new HashMap<>());
            log.info("Data source cache refreshed for ID: {}", dataSourceId);
        } catch (Exception e) {
            log.error("Failed to refresh data source cache for ID {}: {}", dataSourceId, e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDataSourceStatistics(Long dataSourceId) {
        // TODO: Implement data source statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalQueries", 0);
        stats.put("successfulQueries", 0);
        stats.put("failedQueries", 0);
        stats.put("avgExecutionTime", 0);
        stats.put("lastRefreshTime", null);
        return stats;
    }

    @Override
    public List<Map<String, Object>> getDataSourceHistory(Long dataSourceId, Integer days) {
        // TODO: Implement data source history
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> combineDataSources(List<Long> dataSourceIds, String combineConfig) {
        // TODO: Implement data source combination
        return createEmptyResult();
    }

    @Override
    public Map<String, Object> getRealtimeData(Long dataSourceId, Map<String, Object> parameters) {
        // For now, just execute the regular query
        return executeDataQuery(dataSourceId, parameters);
    }

    @Override
    public void scheduleDataRefresh(Long dataSourceId, String cronExpression) {
        // TODO: Implement scheduled data refresh
        log.info("Scheduled data refresh for data source {} with cron: {}", dataSourceId, cronExpression);
    }

    @Override
    public Map<String, Object> exportData(Long dataSourceId, String format, Map<String, Object> parameters) {
        Map<String, Object> data = executeDataQuery(dataSourceId, parameters);
        
        // TODO: Implement data export in different formats
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("format", format);
        result.put("data", data);
        result.put("filename", "export_" + dataSourceId + "_" + System.currentTimeMillis() + "." + format.toLowerCase());
        
        return result;
    }

    // Helper methods
    
    private Map<String, Object> executeDatabaseQuery(ReportDataSource dataSource, Map<String, Object> parameters) {
        try {
            String queryConfig = dataSource.getQueryConfig();
            return executeSqlQuery(queryConfig, parameters);
        } catch (Exception e) {
            throw new RuntimeException("Database query execution failed", e);
        }
    }

    private Map<String, Object> executeApiQuery(ReportDataSource dataSource, Map<String, Object> parameters) {
        try {
            String apiConfig = dataSource.getApiConfig();
            return callApiEndpoint(apiConfig, parameters);
        } catch (Exception e) {
            throw new RuntimeException("API query execution failed", e);
        }
    }

    private Map<String, Object> executeSqlQuery(String queryConfig, Map<String, Object> parameters) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> config = objectMapper.readValue(queryConfig, Map.class);
            
            String sql = (String) config.get("sql");
            String databaseType = (String) config.getOrDefault("databaseType", "MySQL");
            
            return sqlExecutionService.executeQuery(sql, parameters, databaseType, 1L);
            
        } catch (Exception e) {
            throw new RuntimeException("SQL query execution failed", e);
        }
    }

    private List<Map<String, Object>> applyTransformations(List<Map<String, Object>> data, Map<String, Object> config) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> transformations = (List<Map<String, Object>>) config.get("transformations");
        
        if (transformations == null) {
            return data;
        }
        
        for (Map<String, Object> transformation : transformations) {
            String type = (String) transformation.get("type");
            
            switch (type.toUpperCase()) {
                case "FILTER":
                    data = applyFilter(data, transformation);
                    break;
                case "SORT":
                    data = applySort(data, transformation);
                    break;
                case "MAP":
                    data = applyMapping(data, transformation);
                    break;
                case "LIMIT":
                    data = applyLimit(data, transformation);
                    break;
            }
        }
        
        return data;
    }

    private List<Map<String, Object>> applyFilter(List<Map<String, Object>> data, Map<String, Object> filter) {
        String field = (String) filter.get("field");
        String operator = (String) filter.get("operator");
        Object value = filter.get("value");
        
        return data.stream()
            .filter(row -> {
                Object fieldValue = row.get(field);
                return evaluateCondition(fieldValue, operator, value);
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> applySort(List<Map<String, Object>> data, Map<String, Object> sort) {
        String field = (String) sort.get("field");
        String direction = (String) sort.getOrDefault("direction", "ASC");
        
        return data.stream()
            .sorted((a, b) -> {
                Object valueA = a.get(field);
                Object valueB = b.get(field);
                
                int comparison = compareValues(valueA, valueB);
                return "DESC".equalsIgnoreCase(direction) ? -comparison : comparison;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> applyMapping(List<Map<String, Object>> data, Map<String, Object> mapping) {
        @SuppressWarnings("unchecked")
        Map<String, String> fieldMappings = (Map<String, String>) mapping.get("mappings");
        
        return data.stream()
            .map(row -> {
                Map<String, Object> newRow = new HashMap<>();
                for (Map.Entry<String, String> fieldMapping : fieldMappings.entrySet()) {
                    String oldField = fieldMapping.getKey();
                    String newField = fieldMapping.getValue();
                    newRow.put(newField, row.get(oldField));
                }
                return newRow;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> applyLimit(List<Map<String, Object>> data, Map<String, Object> limit) {
        Integer limitValue = (Integer) limit.get("limit");
        Integer offset = (Integer) limit.getOrDefault("offset", 0);
        
        if (limitValue == null) {
            return data;
        }
        
        int startIndex = Math.max(0, offset);
        int endIndex = Math.min(data.size(), startIndex + limitValue);
        
        return data.subList(startIndex, endIndex);
    }

    private Object applyAggregateFunction(List<Map<String, Object>> data, String field, String function) {
        switch (function.toUpperCase()) {
            case "COUNT":
                return data.size();
            case "SUM":
                return data.stream()
                    .mapToDouble(row -> {
                        Object value = row.get(field);
                        return value instanceof Number ? ((Number) value).doubleValue() : 0;
                    })
                    .sum();
            case "AVG":
                return data.stream()
                    .mapToDouble(row -> {
                        Object value = row.get(field);
                        return value instanceof Number ? ((Number) value).doubleValue() : 0;
                    })
                    .average()
                    .orElse(0);
            case "MIN":
                return data.stream()
                    .map(row -> row.get(field))
                    .filter(Objects::nonNull)
                    .min(this::compareValues)
                    .orElse(null);
            case "MAX":
                return data.stream()
                    .map(row -> row.get(field))
                    .filter(Objects::nonNull)
                    .max(this::compareValues)
                    .orElse(null);
            default:
                return null;
        }
    }

    private boolean evaluateCondition(Object fieldValue, String operator, Object value) {
        if (fieldValue == null && value == null) {
            return "EQUALS".equals(operator);
        }
        if (fieldValue == null || value == null) {
            return "NOT_EQUALS".equals(operator);
        }
        
        switch (operator.toUpperCase()) {
            case "EQUALS":
                return Objects.equals(fieldValue, value);
            case "NOT_EQUALS":
                return !Objects.equals(fieldValue, value);
            case "GREATER_THAN":
                return compareValues(fieldValue, value) > 0;
            case "LESS_THAN":
                return compareValues(fieldValue, value) < 0;
            case "GREATER_EQUALS":
                return compareValues(fieldValue, value) >= 0;
            case "LESS_EQUALS":
                return compareValues(fieldValue, value) <= 0;
            case "CONTAINS":
                return fieldValue.toString().contains(value.toString());
            case "STARTS_WITH":
                return fieldValue.toString().startsWith(value.toString());
            case "ENDS_WITH":
                return fieldValue.toString().endsWith(value.toString());
            default:
                return false;
        }
    }

    private int compareValues(Object a, Object b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        }
        
        return a.toString().compareTo(b.toString());
    }

    private String inferDataType(Object value) {
        if (value == null) return "NULL";
        if (value instanceof String) return "STRING";
        if (value instanceof Integer || value instanceof Long) return "INTEGER";
        if (value instanceof Double || value instanceof Float) return "DECIMAL";
        if (value instanceof Boolean) return "BOOLEAN";
        if (value instanceof Date || value instanceof LocalDateTime) return "DATETIME";
        return "OBJECT";
    }

    private int getDataSize(Object data) {
        if (data instanceof List) {
            return ((List<?>) data).size();
        }
        return 0;
    }

    private Map<String, Object> createEmptyResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", new ArrayList<>());
        result.put("total", 0);
        result.put("fromCache", false);
        return result;
    }

    private Map<String, Object> createErrorResult(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("errorMessage", message);
        result.put("data", new ArrayList<>());
        result.put("total", 0);
        return result;
    }

    private Map<String, Object> createValidationResult(boolean isValid, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", isValid);
        result.put("message", message);
        result.put("timestamp", LocalDateTime.now());
        return result;
    }
}