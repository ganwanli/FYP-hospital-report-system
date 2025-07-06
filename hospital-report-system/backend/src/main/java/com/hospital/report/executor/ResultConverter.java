package com.hospital.report.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

@Component
@Slf4j
public class ResultConverter {

    public List<Map<String, Object>> convertResultSet(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                Object value = getColumnValue(resultSet, i, metaData.getColumnType(i));
                row.put(columnName, value);
            }
            rows.add(row);
        }

        return rows;
    }

    public List<Map<String, Object>> getColumnMetadata(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> columns = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            Map<String, Object> column = new LinkedHashMap<>();
            column.put("name", metaData.getColumnLabel(i));
            column.put("type", getColumnTypeName(metaData.getColumnType(i)));
            column.put("typeName", metaData.getColumnTypeName(i));
            column.put("size", metaData.getColumnDisplaySize(i));
            column.put("precision", metaData.getPrecision(i));
            column.put("scale", metaData.getScale(i));
            column.put("nullable", metaData.isNullable(i) == ResultSetMetaData.columnNullable);
            column.put("autoIncrement", metaData.isAutoIncrement(i));
            column.put("primaryKey", false); // Would need additional query to determine
            column.put("tableName", metaData.getTableName(i));
            column.put("schemaName", metaData.getSchemaName(i));
            columns.add(column);
        }

        return columns;
    }

    public Map<String, Object> convertToExportFormat(List<Map<String, Object>> data, String format) {
        Map<String, Object> result = new HashMap<>();
        
        switch (format.toLowerCase()) {
            case "csv":
                result.put("content", convertToCsv(data));
                result.put("contentType", "text/csv");
                result.put("filename", "export.csv");
                break;
            case "json":
                result.put("content", convertToJson(data));
                result.put("contentType", "application/json");
                result.put("filename", "export.json");
                break;
            case "excel":
                result.put("content", convertToExcel(data));
                result.put("contentType", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                result.put("filename", "export.xlsx");
                break;
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }
        
        return result;
    }

    public List<Map<String, Object>> paginate(List<Map<String, Object>> data, int page, int size) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        int start = (page - 1) * size;
        int end = Math.min(start + size, data.size());
        
        if (start >= data.size()) {
            return new ArrayList<>();
        }

        return data.subList(start, end);
    }

    public Map<String, Object> getResultSummary(List<Map<String, Object>> data, List<Map<String, Object>> columns) {
        Map<String, Object> summary = new HashMap<>();
        
        if (data == null || data.isEmpty()) {
            summary.put("totalRows", 0);
            summary.put("totalColumns", columns != null ? columns.size() : 0);
            summary.put("dataTypes", new HashMap<>());
            summary.put("nullCounts", new HashMap<>());
            return summary;
        }

        summary.put("totalRows", data.size());
        summary.put("totalColumns", columns != null ? columns.size() : 0);

        Map<String, String> dataTypes = new HashMap<>();
        Map<String, Integer> nullCounts = new HashMap<>();
        Map<String, Set<Object>> uniqueValues = new HashMap<>();

        if (columns != null) {
            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("name");
                dataTypes.put(columnName, (String) column.get("type"));
                nullCounts.put(columnName, 0);
                uniqueValues.put(columnName, new HashSet<>());
            }
        }

        for (Map<String, Object> row : data) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String columnName = entry.getKey();
                Object value = entry.getValue();
                
                if (value == null) {
                    nullCounts.merge(columnName, 1, Integer::sum);
                } else {
                    Set<Object> values = uniqueValues.get(columnName);
                    if (values != null && values.size() < 100) { // Limit unique values tracking
                        values.add(value);
                    }
                }
            }
        }

        summary.put("dataTypes", dataTypes);
        summary.put("nullCounts", nullCounts);
        
        Map<String, Integer> uniqueCounts = new HashMap<>();
        uniqueValues.forEach((key, value) -> uniqueCounts.put(key, value.size()));
        summary.put("uniqueCounts", uniqueCounts);

        return summary;
    }

    public List<Map<String, Object>> filterData(List<Map<String, Object>> data, String columnName, String filterValue, String operator) {
        if (data == null || data.isEmpty() || columnName == null || filterValue == null) {
            return data;
        }

        return data.stream()
                .filter(row -> {
                    Object value = row.get(columnName);
                    if (value == null) {
                        return "IS NULL".equalsIgnoreCase(operator);
                    }
                    
                    String stringValue = value.toString();
                    switch (operator.toUpperCase()) {
                        case "EQUALS":
                            return stringValue.equals(filterValue);
                        case "CONTAINS":
                            return stringValue.contains(filterValue);
                        case "STARTS_WITH":
                            return stringValue.startsWith(filterValue);
                        case "ENDS_WITH":
                            return stringValue.endsWith(filterValue);
                        case "GREATER_THAN":
                            return compareValues(value, filterValue) > 0;
                        case "LESS_THAN":
                            return compareValues(value, filterValue) < 0;
                        case "GREATER_EQUAL":
                            return compareValues(value, filterValue) >= 0;
                        case "LESS_EQUAL":
                            return compareValues(value, filterValue) <= 0;
                        case "NOT_EQUALS":
                            return !stringValue.equals(filterValue);
                        default:
                            return true;
                    }
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Map<String, Object>> sortData(List<Map<String, Object>> data, String columnName, boolean ascending) {
        if (data == null || data.isEmpty() || columnName == null) {
            return data;
        }

        return data.stream()
                .sorted((row1, row2) -> {
                    Object value1 = row1.get(columnName);
                    Object value2 = row2.get(columnName);
                    
                    if (value1 == null && value2 == null) return 0;
                    if (value1 == null) return ascending ? -1 : 1;
                    if (value2 == null) return ascending ? 1 : -1;
                    
                    int comparison = compareValues(value1, value2);
                    return ascending ? comparison : -comparison;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private Object getColumnValue(ResultSet resultSet, int columnIndex, int columnType) throws SQLException {
        switch (columnType) {
            case Types.BIT:
            case Types.BOOLEAN:
                return resultSet.getBoolean(columnIndex);
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return resultSet.getInt(columnIndex);
            case Types.BIGINT:
                return resultSet.getLong(columnIndex);
            case Types.REAL:
            case Types.FLOAT:
                return resultSet.getFloat(columnIndex);
            case Types.DOUBLE:
                return resultSet.getDouble(columnIndex);
            case Types.DECIMAL:
            case Types.NUMERIC:
                return resultSet.getBigDecimal(columnIndex);
            case Types.DATE:
                return resultSet.getDate(columnIndex);
            case Types.TIME:
                return resultSet.getTime(columnIndex);
            case Types.TIMESTAMP:
                return resultSet.getTimestamp(columnIndex);
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                return resultSet.getString(columnIndex);
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return resultSet.getBytes(columnIndex);
            case Types.BLOB:
                return resultSet.getBlob(columnIndex);
            case Types.CLOB:
                return resultSet.getClob(columnIndex);
            default:
                return resultSet.getObject(columnIndex);
        }
    }

    private String getColumnTypeName(int columnType) {
        switch (columnType) {
            case Types.BIT:
            case Types.BOOLEAN:
                return "BOOLEAN";
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return "INTEGER";
            case Types.BIGINT:
                return "BIGINT";
            case Types.REAL:
            case Types.FLOAT:
                return "FLOAT";
            case Types.DOUBLE:
                return "DOUBLE";
            case Types.DECIMAL:
            case Types.NUMERIC:
                return "DECIMAL";
            case Types.DATE:
                return "DATE";
            case Types.TIME:
                return "TIME";
            case Types.TIMESTAMP:
                return "TIMESTAMP";
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                return "STRING";
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return "BINARY";
            case Types.BLOB:
                return "BLOB";
            case Types.CLOB:
                return "CLOB";
            default:
                return "OBJECT";
        }
    }

    private String convertToCsv(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "";
        }

        StringBuilder csv = new StringBuilder();
        
        // Header
        Set<String> columns = data.get(0).keySet();
        csv.append(String.join(",", columns)).append("\n");
        
        // Data rows
        for (Map<String, Object> row : data) {
            List<String> values = new ArrayList<>();
            for (String column : columns) {
                Object value = row.get(column);
                String csvValue = value != null ? escapeCsvValue(value.toString()) : "";
                values.add(csvValue);
            }
            csv.append(String.join(",", values)).append("\n");
        }
        
        return csv.toString();
    }

    private String convertToJson(List<Map<String, Object>> data) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data);
        } catch (Exception e) {
            log.error("Failed to convert data to JSON", e);
            return "[]";
        }
    }

    private byte[] convertToExcel(List<Map<String, Object>> data) {
        // TODO: Implement Excel export using Apache POI
        throw new UnsupportedOperationException("Excel export not yet implemented");
    }

    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }

    private int compareValues(Object value1, Object value2) {
        if (value1 instanceof Number && value2 instanceof Number) {
            return Double.compare(((Number) value1).doubleValue(), ((Number) value2).doubleValue());
        } else if (value1 instanceof Comparable && value2 instanceof Comparable) {
            return ((Comparable) value1).compareTo(value2);
        } else {
            return value1.toString().compareTo(value2.toString());
        }
    }
}