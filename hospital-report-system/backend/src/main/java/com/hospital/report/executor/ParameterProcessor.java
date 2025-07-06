package com.hospital.report.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParameterProcessor {

    private final ObjectMapper objectMapper;
    
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    private static final Map<String, String> DATE_FORMAT_PATTERNS = new HashMap<>();
    
    static {
        DATE_FORMAT_PATTERNS.put("yyyy-MM-dd", "\\d{4}-\\d{2}-\\d{2}");
        DATE_FORMAT_PATTERNS.put("yyyy-MM-dd HH:mm:ss", "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
        DATE_FORMAT_PATTERNS.put("yyyy/MM/dd", "\\d{4}/\\d{2}/\\d{2}");
        DATE_FORMAT_PATTERNS.put("MM/dd/yyyy", "\\d{2}/\\d{2}/\\d{4}");
    }

    public String processParameters(String sqlTemplate, Map<String, Object> parameters) {
        if (!StringUtils.hasText(sqlTemplate) || parameters == null || parameters.isEmpty()) {
            return sqlTemplate;
        }

        String processedSql = sqlTemplate;
        Matcher matcher = PARAMETER_PATTERN.matcher(sqlTemplate);
        
        while (matcher.find()) {
            String paramName = matcher.group(1);
            String placeholder = "${" + paramName + "}";
            
            if (parameters.containsKey(paramName)) {
                Object value = parameters.get(paramName);
                String replacementValue = formatParameterValue(value);
                processedSql = processedSql.replace(placeholder, replacementValue);
            } else {
                log.warn("Parameter {} not found in provided parameters", paramName);
            }
        }
        
        return processedSql;
    }

    public Map<String, Object> validateParameters(Map<String, Object> parameters, Map<String, ParameterDefinition> definitions) {
        Map<String, Object> validatedParams = new HashMap<>();
        List<String> errors = new ArrayList<>();

        for (Map.Entry<String, ParameterDefinition> entry : definitions.entrySet()) {
            String paramName = entry.getKey();
            ParameterDefinition definition = entry.getValue();
            Object value = parameters.get(paramName);

            if (definition.isRequired() && (value == null || value.toString().trim().isEmpty())) {
                errors.add("Parameter '" + paramName + "' is required");
                continue;
            }

            if (value != null) {
                try {
                    Object validatedValue = validateAndConvertValue(value, definition);
                    validatedParams.put(paramName, validatedValue);
                } catch (Exception e) {
                    errors.add("Parameter '" + paramName + "': " + e.getMessage());
                }
            } else if (definition.getDefaultValue() != null) {
                validatedParams.put(paramName, definition.getDefaultValue());
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Parameter validation failed: " + String.join(", ", errors));
        }

        return validatedParams;
    }

    public String serializeParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "{}";
        }

        try {
            return objectMapper.writeValueAsString(parameters);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize parameters", e);
            return "{}";
        }
    }

    public Map<String, Object> deserializeParameters(String parametersJson) {
        if (!StringUtils.hasText(parametersJson)) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(parametersJson, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize parameters", e);
            return new HashMap<>();
        }
    }

    public List<String> extractParameterNames(String sqlTemplate) {
        List<String> paramNames = new ArrayList<>();
        if (!StringUtils.hasText(sqlTemplate)) {
            return paramNames;
        }

        Matcher matcher = PARAMETER_PATTERN.matcher(sqlTemplate);
        Set<String> uniqueParams = new HashSet<>();
        
        while (matcher.find()) {
            String paramName = matcher.group(1);
            if (uniqueParams.add(paramName)) {
                paramNames.add(paramName);
            }
        }
        
        return paramNames;
    }

    public Map<String, ParameterDefinition> inferParameterTypes(String sqlTemplate) {
        Map<String, ParameterDefinition> definitions = new HashMap<>();
        List<String> paramNames = extractParameterNames(sqlTemplate);
        
        for (String paramName : paramNames) {
            ParameterDefinition definition = new ParameterDefinition();
            definition.setName(paramName);
            definition.setType(inferParameterType(paramName));
            definition.setRequired(true);
            definitions.put(paramName, definition);
        }
        
        return definitions;
    }

    private String formatParameterValue(Object value) {
        if (value == null) {
            return "NULL";
        }

        if (value instanceof String) {
            return "'" + escapeSqlString((String) value) + "'";
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? "1" : "0";
        } else if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return "'" + sdf.format((Date) value) + "'";
        } else if (value instanceof LocalDate) {
            return "'" + ((LocalDate) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "'";
        } else if (value instanceof LocalDateTime) {
            return "'" + ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "'";
        } else if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (collection.isEmpty()) {
                return "('')";
            }
            StringBuilder sb = new StringBuilder("(");
            boolean first = true;
            for (Object item : collection) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(formatParameterValue(item));
                first = false;
            }
            sb.append(")");
            return sb.toString();
        } else {
            return "'" + escapeSqlString(value.toString()) + "'";
        }
    }

    private String escapeSqlString(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''")
                   .replace("\\", "\\\\")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private Object validateAndConvertValue(Object value, ParameterDefinition definition) {
        String type = definition.getType();
        String stringValue = value.toString().trim();

        switch (type.toUpperCase()) {
            case "STRING":
            case "VARCHAR":
            case "TEXT":
                return validateStringValue(stringValue, definition);
            case "INTEGER":
            case "INT":
                return validateIntegerValue(stringValue, definition);
            case "LONG":
            case "BIGINT":
                return validateLongValue(stringValue, definition);
            case "DECIMAL":
            case "NUMERIC":
            case "DOUBLE":
                return validateDecimalValue(stringValue, definition);
            case "BOOLEAN":
            case "BOOL":
                return validateBooleanValue(stringValue, definition);
            case "DATE":
                return validateDateValue(stringValue, definition);
            case "DATETIME":
            case "TIMESTAMP":
                return validateDateTimeValue(stringValue, definition);
            case "LIST":
            case "ARRAY":
                return validateListValue(value, definition);
            default:
                return stringValue;
        }
    }

    private String validateStringValue(String value, ParameterDefinition definition) {
        if (definition.getMinLength() != null && value.length() < definition.getMinLength()) {
            throw new IllegalArgumentException("Value too short, minimum length: " + definition.getMinLength());
        }
        if (definition.getMaxLength() != null && value.length() > definition.getMaxLength()) {
            throw new IllegalArgumentException("Value too long, maximum length: " + definition.getMaxLength());
        }
        if (definition.getPattern() != null && !value.matches(definition.getPattern())) {
            throw new IllegalArgumentException("Value does not match required pattern");
        }
        return value;
    }

    private Integer validateIntegerValue(String value, ParameterDefinition definition) {
        try {
            Integer intValue = Integer.parseInt(value);
            if (definition.getMinValue() != null && intValue < Integer.parseInt(definition.getMinValue())) {
                throw new IllegalArgumentException("Value too small, minimum: " + definition.getMinValue());
            }
            if (definition.getMaxValue() != null && intValue > Integer.parseInt(definition.getMaxValue())) {
                throw new IllegalArgumentException("Value too large, maximum: " + definition.getMaxValue());
            }
            return intValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer value: " + value);
        }
    }

    private Long validateLongValue(String value, ParameterDefinition definition) {
        try {
            Long longValue = Long.parseLong(value);
            if (definition.getMinValue() != null && longValue < Long.parseLong(definition.getMinValue())) {
                throw new IllegalArgumentException("Value too small, minimum: " + definition.getMinValue());
            }
            if (definition.getMaxValue() != null && longValue > Long.parseLong(definition.getMaxValue())) {
                throw new IllegalArgumentException("Value too large, maximum: " + definition.getMaxValue());
            }
            return longValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid long value: " + value);
        }
    }

    private Double validateDecimalValue(String value, ParameterDefinition definition) {
        try {
            Double doubleValue = Double.parseDouble(value);
            if (definition.getMinValue() != null && doubleValue < Double.parseDouble(definition.getMinValue())) {
                throw new IllegalArgumentException("Value too small, minimum: " + definition.getMinValue());
            }
            if (definition.getMaxValue() != null && doubleValue > Double.parseDouble(definition.getMaxValue())) {
                throw new IllegalArgumentException("Value too large, maximum: " + definition.getMaxValue());
            }
            return doubleValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid decimal value: " + value);
        }
    }

    private Boolean validateBooleanValue(String value, ParameterDefinition definition) {
        String lowerValue = value.toLowerCase();
        if ("true".equals(lowerValue) || "1".equals(lowerValue) || "yes".equals(lowerValue)) {
            return true;
        } else if ("false".equals(lowerValue) || "0".equals(lowerValue) || "no".equals(lowerValue)) {
            return false;
        } else {
            throw new IllegalArgumentException("Invalid boolean value: " + value);
        }
    }

    private Date validateDateValue(String value, ParameterDefinition definition) {
        for (Map.Entry<String, String> entry : DATE_FORMAT_PATTERNS.entrySet()) {
            if (value.matches(entry.getValue())) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(entry.getKey());
                    return sdf.parse(value);
                } catch (ParseException e) {
                    // Continue to next pattern
                }
            }
        }
        throw new IllegalArgumentException("Invalid date format: " + value);
    }

    private Date validateDateTimeValue(String value, ParameterDefinition definition) {
        return validateDateValue(value, definition);
    }

    private List<Object> validateListValue(Object value, ParameterDefinition definition) {
        if (value instanceof Collection) {
            return new ArrayList<>((Collection<?>) value);
        } else if (value instanceof String) {
            String stringValue = (String) value;
            if (stringValue.startsWith("[") && stringValue.endsWith("]")) {
                try {
                    return objectMapper.readValue(stringValue, List.class);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("Invalid JSON array format: " + stringValue);
                }
            } else {
                return Arrays.asList(stringValue.split(","));
            }
        } else {
            throw new IllegalArgumentException("Invalid list value type");
        }
    }

    private String inferParameterType(String paramName) {
        String lowerName = paramName.toLowerCase();
        
        if (lowerName.contains("date") || lowerName.contains("time")) {
            return "DATE";
        } else if (lowerName.contains("id") || lowerName.contains("count") || lowerName.contains("num")) {
            return "INTEGER";
        } else if (lowerName.contains("amount") || lowerName.contains("price") || lowerName.contains("rate")) {
            return "DECIMAL";
        } else if (lowerName.contains("flag") || lowerName.contains("is") || lowerName.contains("has")) {
            return "BOOLEAN";
        } else if (lowerName.contains("list") || lowerName.contains("ids") || lowerName.contains("array")) {
            return "LIST";
        } else {
            return "STRING";
        }
    }

    public static class ParameterDefinition {
        private String name;
        private String type;
        private boolean required;
        private String defaultValue;
        private Integer minLength;
        private Integer maxLength;
        private String minValue;
        private String maxValue;
        private String pattern;
        private List<String> allowedValues;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
        
        public Integer getMinLength() { return minLength; }
        public void setMinLength(Integer minLength) { this.minLength = minLength; }
        
        public Integer getMaxLength() { return maxLength; }
        public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }
        
        public String getMinValue() { return minValue; }
        public void setMinValue(String minValue) { this.minValue = minValue; }
        
        public String getMaxValue() { return maxValue; }
        public void setMaxValue(String maxValue) { this.maxValue = maxValue; }
        
        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }
        
        public List<String> getAllowedValues() { return allowedValues; }
        public void setAllowedValues(List<String> allowedValues) { this.allowedValues = allowedValues; }
    }
}