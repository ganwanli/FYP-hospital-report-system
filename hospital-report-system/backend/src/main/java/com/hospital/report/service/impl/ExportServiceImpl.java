package com.hospital.report.service.impl;

import com.hospital.report.service.ExportService;
import com.hospital.report.service.ReportRendererService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {
    
    private final ReportRendererService reportRendererService;
    
    private static final String[] SUPPORTED_FORMATS = {"PDF", "EXCEL", "WORD", "CSV", "PNG", "JPEG"};
    
    @Override
    public byte[] exportToPdf(Map<String, Object> reportData, Map<String, Object> options) {
        try {
            log.info("开始导出PDF报表，reportId: {}", reportData.get("reportId"));
            
            // 渲染为打印版HTML
            String printHtml = reportRendererService.renderToPrintHtml(reportData, options);
            
            // 使用HTML转PDF库（这里模拟实现）
            byte[] pdfBytes = convertHtmlToPdf(printHtml, options);
            
            log.info("PDF导出完成，大小: {} bytes", pdfBytes.length);
            return pdfBytes;
            
        } catch (Exception e) {
            log.error("PDF导出失败", e);
            throw new RuntimeException("PDF导出失败: " + e.getMessage());
        }
    }
    
    @Override
    public byte[] exportToExcel(Map<String, Object> reportData, Map<String, Object> options) {
        try {
            log.info("开始导出Excel报表，reportId: {}", reportData.get("reportId"));
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // 创建Excel工作簿
            createExcelWorkbook(reportData, options, outputStream);
            
            byte[] excelBytes = outputStream.toByteArray();
            log.info("Excel导出完成，大小: {} bytes", excelBytes.length);
            
            return excelBytes;
            
        } catch (Exception e) {
            log.error("Excel导出失败", e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage());
        }
    }
    
    @Override
    public byte[] exportToWord(Map<String, Object> reportData, Map<String, Object> options) {
        try {
            log.info("开始导出Word报表，reportId: {}", reportData.get("reportId"));
            
            // 模拟Word导出
            String reportContent = generateWordContent(reportData, options);
            byte[] wordBytes = reportContent.getBytes("UTF-8");
            
            log.info("Word导出完成，大小: {} bytes", wordBytes.length);
            return wordBytes;
            
        } catch (Exception e) {
            log.error("Word导出失败", e);
            throw new RuntimeException("Word导出失败: " + e.getMessage());
        }
    }
    
    @Override
    public byte[] exportToCsv(Map<String, Object> reportData, Map<String, Object> options) {
        try {
            log.info("开始导出CSV报表，reportId: {}", reportData.get("reportId"));
            
            StringBuilder csvContent = new StringBuilder();
            
            // 添加BOM以支持中文
            csvContent.append("\uFEFF");
            
            // 生成CSV内容
            generateCsvContent(reportData, options, csvContent);
            
            byte[] csvBytes = csvContent.toString().getBytes("UTF-8");
            log.info("CSV导出完成，大小: {} bytes", csvBytes.length);
            
            return csvBytes;
            
        } catch (Exception e) {
            log.error("CSV导出失败", e);
            throw new RuntimeException("CSV导出失败: " + e.getMessage());
        }
    }
    
    @Override
    public byte[] exportToImage(Map<String, Object> reportData, String format, Map<String, Object> options) {
        try {
            log.info("开始导出{}图片，reportId: {}", format, reportData.get("reportId"));
            
            // 渲染HTML
            String html = reportRendererService.renderToHtml(reportData, options);
            
            // 转换为图片（这里模拟实现）
            byte[] imageBytes = convertHtmlToImage(html, format, options);
            
            log.info("{}导出完成，大小: {} bytes", format, imageBytes.length);
            return imageBytes;
            
        } catch (Exception e) {
            log.error("{}导出失败", format, e);
            throw new RuntimeException(format + "导出失败: " + e.getMessage());
        }
    }
    
    @Override
    public String[] getSupportedFormats() {
        return SUPPORTED_FORMATS.clone();
    }
    
    @Override
    public boolean validateExportOptions(String format, Map<String, Object> options) {
        if (format == null || format.trim().isEmpty()) {
            return false;
        }
        
        boolean formatSupported = Arrays.asList(SUPPORTED_FORMATS).contains(format.toUpperCase());
        if (!formatSupported) {
            log.warn("不支持的导出格式: {}", format);
            return false;
        }
        
        // 验证格式特定的选项
        switch (format.toUpperCase()) {
            case "PDF":
                return validatePdfOptions(options);
            case "EXCEL":
                return validateExcelOptions(options);
            case "WORD":
                return validateWordOptions(options);
            case "CSV":
                return validateCsvOptions(options);
            case "PNG":
            case "JPEG":
                return validateImageOptions(options);
            default:
                return true;
        }
    }
    
    // 私有方法实现
    
    private byte[] convertHtmlToPdf(String html, Map<String, Object> options) {
        // 这里应该使用实际的HTML转PDF库，如iText、wkhtmltopdf等
        // 模拟实现
        try {
            String pdfContent = "PDF Header\n" + html.replaceAll("<[^>]*>", "") + "\nPDF Footer";
            return pdfContent.getBytes("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("HTML转PDF失败", e);
        }
    }
    
    private void createExcelWorkbook(Map<String, Object> reportData, Map<String, Object> options, ByteArrayOutputStream outputStream) throws IOException {
        // 这里应该使用EasyExcel或Apache POI创建Excel文件
        // 模拟实现
        StringBuilder excelContent = new StringBuilder();
        
        // Excel头部信息
        excelContent.append("报表名称,").append(reportData.get("reportName")).append("\n");
        excelContent.append("生成时间,").append(new Date((Long) reportData.get("generatedAt"))).append("\n");
        excelContent.append("\n");
        
        // 导出组件数据
        List<Map<String, Object>> components = (List<Map<String, Object>>) reportData.get("components");
        if (components != null) {
            for (Map<String, Object> component : components) {
                String componentType = (String) component.get("componentType");
                
                if ("table".equals(componentType)) {
                    exportTableToExcel(component, excelContent);
                } else if (componentType.contains("chart")) {
                    exportChartDataToExcel(component, excelContent);
                }
                
                excelContent.append("\n");
            }
        }
        
        outputStream.write(excelContent.toString().getBytes("UTF-8"));
    }
    
    private void exportTableToExcel(Map<String, Object> component, StringBuilder content) {
        Map<String, Object> data = (Map<String, Object>) component.get("data");
        if (data == null) return;
        
        String componentName = (String) component.get("componentName");
        content.append("表格: ").append(componentName).append("\n");
        
        List<Map<String, Object>> columns = (List<Map<String, Object>>) data.get("columns");
        List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records");
        
        if (columns != null && records != null) {
            // 写入表头
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) content.append(",");
                content.append(escapeCSV((String) columns.get(i).get("title")));
            }
            content.append("\n");
            
            // 写入数据行
            for (Map<String, Object> record : records) {
                for (int i = 0; i < columns.size(); i++) {
                    if (i > 0) content.append(",");
                    String dataIndex = (String) columns.get(i).get("dataIndex");
                    Object value = record.get(dataIndex);
                    content.append(escapeCSV(value != null ? value.toString() : ""));
                }
                content.append("\n");
            }
        }
    }
    
    private void exportChartDataToExcel(Map<String, Object> component, StringBuilder content) {
        String componentName = (String) component.get("componentName");
        content.append("图表: ").append(componentName).append("\n");
        
        Map<String, Object> data = (Map<String, Object>) component.get("data");
        if (data != null) {
            List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records");
            if (records != null && !records.isEmpty()) {
                // 获取数据的键作为列头
                Set<String> keys = records.get(0).keySet();
                
                // 写入表头
                content.append(String.join(",", keys)).append("\n");
                
                // 写入数据
                for (Map<String, Object> record : records) {
                    List<String> values = new ArrayList<>();
                    for (String key : keys) {
                        Object value = record.get(key);
                        values.add(escapeCSV(value != null ? value.toString() : ""));
                    }
                    content.append(String.join(",", values)).append("\n");
                }
            }
        }
    }
    
    private String generateWordContent(Map<String, Object> reportData, Map<String, Object> options) {
        StringBuilder wordContent = new StringBuilder();
        
        wordContent.append("报表名称: ").append(reportData.get("reportName")).append("\n\n");
        wordContent.append("生成时间: ").append(new Date((Long) reportData.get("generatedAt"))).append("\n\n");
        
        if (reportData.get("reportDescription") != null) {
            wordContent.append("报表描述: ").append(reportData.get("reportDescription")).append("\n\n");
        }
        
        // 导出组件内容
        List<Map<String, Object>> components = (List<Map<String, Object>>) reportData.get("components");
        if (components != null) {
            wordContent.append("报表内容:\n\n");
            
            for (Map<String, Object> component : components) {
                String componentName = (String) component.get("componentName");
                String componentType = (String) component.get("componentType");
                
                wordContent.append("组件: ").append(componentName)
                          .append(" (").append(componentType).append(")\n");
                
                if ("table".equals(componentType)) {
                    wordContent.append(exportTableToText(component));
                } else if ("text".equals(componentType)) {
                    wordContent.append(exportTextToWord(component));
                }
                
                wordContent.append("\n");
            }
        }
        
        return wordContent.toString();
    }
    
    private void generateCsvContent(Map<String, Object> reportData, Map<String, Object> options, StringBuilder csvContent) {
        csvContent.append("报表名称,").append(escapeCSV((String) reportData.get("reportName"))).append("\n");
        csvContent.append("生成时间,").append(escapeCSV(new Date((Long) reportData.get("generatedAt")).toString())).append("\n");
        csvContent.append("\n");
        
        // 导出表格组件数据
        List<Map<String, Object>> components = (List<Map<String, Object>>) reportData.get("components");
        if (components != null) {
            for (Map<String, Object> component : components) {
                String componentType = (String) component.get("componentType");
                
                if ("table".equals(componentType)) {
                    csvContent.append("表格,").append(escapeCSV((String) component.get("componentName"))).append("\n");
                    exportTableToCsv(component, csvContent);
                    csvContent.append("\n");
                }
            }
        }
    }
    
    private void exportTableToCsv(Map<String, Object> component, StringBuilder csvContent) {
        Map<String, Object> data = (Map<String, Object>) component.get("data");
        if (data == null) return;
        
        List<Map<String, Object>> columns = (List<Map<String, Object>>) data.get("columns");
        List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records");
        
        if (columns != null && records != null) {
            // 表头
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) csvContent.append(",");
                csvContent.append(escapeCSV((String) columns.get(i).get("title")));
            }
            csvContent.append("\n");
            
            // 数据行
            for (Map<String, Object> record : records) {
                for (int i = 0; i < columns.size(); i++) {
                    if (i > 0) csvContent.append(",");
                    String dataIndex = (String) columns.get(i).get("dataIndex");
                    Object value = record.get(dataIndex);
                    csvContent.append(escapeCSV(value != null ? value.toString() : ""));
                }
                csvContent.append("\n");
            }
        }
    }
    
    private String exportTableToText(Map<String, Object> component) {
        StringBuilder text = new StringBuilder();
        Map<String, Object> data = (Map<String, Object>) component.get("data");
        
        if (data != null) {
            List<Map<String, Object>> columns = (List<Map<String, Object>>) data.get("columns");
            List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records");
            
            if (columns != null && records != null) {
                // 简单的文本表格格式
                for (Map<String, Object> column : columns) {
                    text.append(column.get("title")).append("\t");
                }
                text.append("\n");
                
                for (Map<String, Object> record : records) {
                    for (Map<String, Object> column : columns) {
                        String dataIndex = (String) column.get("dataIndex");
                        Object value = record.get(dataIndex);
                        text.append(value != null ? value.toString() : "").append("\t");
                    }
                    text.append("\n");
                }
            }
        }
        
        return text.toString();
    }
    
    private String exportTextToWord(Map<String, Object> component) {
        Map<String, Object> textConfig = (Map<String, Object>) component.get("textConfig");
        if (textConfig != null) {
            return (String) textConfig.getOrDefault("content", "");
        }
        return "";
    }
    
    private byte[] convertHtmlToImage(String html, String format, Map<String, Object> options) {
        // 这里应该使用无头浏览器或HTML转图片库
        // 模拟实现
        String imageContent = "Image data for " + format + ": " + html.substring(0, Math.min(100, html.length()));
        return imageContent.getBytes();
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        
        // 如果包含逗号、引号或换行符，需要用引号包围并转义内部引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    // 验证方法
    
    private boolean validatePdfOptions(Map<String, Object> options) {
        // 验证PDF导出选项
        if (options == null) return true;
        
        Object pageSize = options.get("pageSize");
        if (pageSize != null && !Arrays.asList("A4", "A3", "LETTER", "LEGAL").contains(pageSize)) {
            return false;
        }
        
        Object orientation = options.get("orientation");
        if (orientation != null && !Arrays.asList("PORTRAIT", "LANDSCAPE").contains(orientation)) {
            return false;
        }
        
        return true;
    }
    
    private boolean validateExcelOptions(Map<String, Object> options) {
        // 验证Excel导出选项
        if (options == null) return true;
        
        Object sheetName = options.get("sheetName");
        if (sheetName != null && sheetName.toString().length() > 31) {
            return false; // Excel工作表名长度限制
        }
        
        return true;
    }
    
    private boolean validateWordOptions(Map<String, Object> options) {
        // 验证Word导出选项
        return true; // Word选项较为灵活
    }
    
    private boolean validateCsvOptions(Map<String, Object> options) {
        // 验证CSV导出选项
        if (options == null) return true;
        
        Object delimiter = options.get("delimiter");
        if (delimiter != null && !Arrays.asList(",", ";", "\t", "|").contains(delimiter)) {
            return false;
        }
        
        return true;
    }
    
    private boolean validateImageOptions(Map<String, Object> options) {
        // 验证图片导出选项
        if (options == null) return true;
        
        Object width = options.get("width");
        if (width != null) {
            try {
                int w = Integer.parseInt(width.toString());
                if (w <= 0 || w > 10000) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        Object height = options.get("height");
        if (height != null) {
            try {
                int h = Integer.parseInt(height.toString());
                if (h <= 0 || h > 10000) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return true;
    }
}