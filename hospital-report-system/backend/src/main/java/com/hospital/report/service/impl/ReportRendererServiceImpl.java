package com.hospital.report.service.impl;

import com.hospital.report.service.ReportRendererService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportRendererServiceImpl implements ReportRendererService {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public String renderToHtml(Map<String, Object> reportData, Map<String, Object> options) {
        try {
            StringBuilder html = new StringBuilder();
            
            // HTML文档头部
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang=\"zh-CN\">\n");
            html.append("<head>\n");
            html.append("    <meta charset=\"UTF-8\">\n");
            html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            html.append("    <title>").append(reportData.get("reportName")).append("</title>\n");
            
            // 引入样式
            html.append("    <style>\n");
            html.append(generateStyles(reportData, "html"));
            html.append("    </style>\n");
            
            // 引入ECharts
            html.append("    <script src=\"https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js\"></script>\n");
            html.append("</head>\n");
            
            // HTML主体
            html.append("<body>\n");
            html.append("    <div class=\"report-container\">\n");
            
            // 报表头部信息
            html.append("        <div class=\"report-header\">\n");
            html.append("            <h1>").append(reportData.get("reportName")).append("</h1>\n");
            if (reportData.get("reportDescription") != null) {
                html.append("            <p class=\"report-description\">").append(reportData.get("reportDescription")).append("</p>\n");
            }
            html.append("            <div class=\"report-meta\">\n");
            html.append("                <span>生成时间: ").append(new Date((Long) reportData.get("generatedAt"))).append("</span>\n");
            if (reportData.get("parameters") != null) {
                Map<String, Object> params = (Map<String, Object>) reportData.get("parameters");
                if (!params.isEmpty()) {
                    html.append("                <span>参数: ").append(formatParameters(params)).append("</span>\n");
                }
            }
            html.append("            </div>\n");
            html.append("        </div>\n");
            
            // 报表画布
            Integer canvasWidth = (Integer) reportData.get("canvasWidth");
            Integer canvasHeight = (Integer) reportData.get("canvasHeight");
            
            html.append("        <div class=\"report-canvas\" style=\"width: ").append(canvasWidth).append("px; height: ").append(canvasHeight).append("px; position: relative; margin: 0 auto; background: white; border: 1px solid #e8e8e8;\">\n");
            
            // 渲染组件
            List<Map<String, Object>> components = (List<Map<String, Object>>) reportData.get("components");
            if (components != null) {
                for (Map<String, Object> component : components) {
                    html.append(renderComponent(component, "html", options));
                }
            }
            
            html.append("        </div>\n");
            html.append("    </div>\n");
            
            // JavaScript脚本
            html.append("    <script>\n");
            html.append(generateScripts(reportData, "html"));
            html.append("    </script>\n");
            
            html.append("</body>\n");
            html.append("</html>");
            
            return html.toString();
            
        } catch (Exception e) {
            log.error("HTML渲染失败", e);
            return "<html><body><h1>报表渲染失败</h1><p>" + e.getMessage() + "</p></body></html>";
        }
    }
    
    @Override
    public String renderToJson(Map<String, Object> reportData, Map<String, Object> options) {
        try {
            return objectMapper.writeValueAsString(reportData);
        } catch (Exception e) {
            log.error("JSON渲染失败", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
    
    @Override
    public String renderToMobileHtml(Map<String, Object> reportData, Map<String, Object> options) {
        try {
            StringBuilder html = new StringBuilder();
            
            // 移动端HTML头部
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang=\"zh-CN\">\n");
            html.append("<head>\n");
            html.append("    <meta charset=\"UTF-8\">\n");
            html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">\n");
            html.append("    <meta name=\"format-detection\" content=\"telephone=no\">\n");
            html.append("    <title>").append(reportData.get("reportName")).append("</title>\n");
            
            html.append("    <style>\n");
            html.append(generateStyles(reportData, "mobile"));
            html.append("    </style>\n");
            
            html.append("    <script src=\"https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js\"></script>\n");
            html.append("</head>\n");
            
            html.append("<body>\n");
            html.append("    <div class=\"mobile-report-container\">\n");
            
            // 移动端报表头部
            html.append("        <div class=\"mobile-report-header\">\n");
            html.append("            <h2>").append(reportData.get("reportName")).append("</h2>\n");
            html.append("            <div class=\"mobile-report-meta\">").append(new Date((Long) reportData.get("generatedAt"))).append("</div>\n");
            html.append("        </div>\n");
            
            // 移动端组件布局（垂直排列）
            html.append("        <div class=\"mobile-components\">\n");
            
            List<Map<String, Object>> components = (List<Map<String, Object>>) reportData.get("components");
            if (components != null) {
                for (Map<String, Object> component : components) {
                    html.append("            <div class=\"mobile-component-wrapper\">\n");
                    html.append(renderComponent(component, "mobile", options));
                    html.append("            </div>\n");
                }
            }
            
            html.append("        </div>\n");
            html.append("    </div>\n");
            
            html.append("    <script>\n");
            html.append(generateScripts(reportData, "mobile"));
            html.append("    </script>\n");
            
            html.append("</body>\n");
            html.append("</html>");
            
            return html.toString();
            
        } catch (Exception e) {
            log.error("移动端HTML渲染失败", e);
            return "<html><body><h1>移动端报表渲染失败</h1></body></html>";
        }
    }
    
    @Override
    public String renderToPrintHtml(Map<String, Object> reportData, Map<String, Object> options) {
        try {
            StringBuilder html = new StringBuilder();
            
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang=\"zh-CN\">\n");
            html.append("<head>\n");
            html.append("    <meta charset=\"UTF-8\">\n");
            html.append("    <title>").append(reportData.get("reportName")).append(" - 打印版</title>\n");
            
            html.append("    <style>\n");
            html.append(generateStyles(reportData, "print"));
            html.append("    </style>\n");
            html.append("</head>\n");
            
            html.append("<body>\n");
            html.append("    <div class=\"print-report-container\">\n");
            
            // 打印头部
            html.append("        <div class=\"print-header\">\n");
            html.append("            <h1>").append(reportData.get("reportName")).append("</h1>\n");
            html.append("            <div class=\"print-meta\">\n");
            html.append("                <span>打印时间: ").append(new Date()).append("</span>\n");
            html.append("            </div>\n");
            html.append("        </div>\n");
            
            // 打印内容
            html.append("        <div class=\"print-content\">\n");
            
            List<Map<String, Object>> components = (List<Map<String, Object>>) reportData.get("components");
            if (components != null) {
                for (Map<String, Object> component : components) {
                    html.append(renderComponent(component, "print", options));
                }
            }
            
            html.append("        </div>\n");
            html.append("    </div>\n");
            html.append("</body>\n");
            html.append("</html>");
            
            return html.toString();
            
        } catch (Exception e) {
            log.error("打印版HTML渲染失败", e);
            return "<html><body><h1>打印版报表渲染失败</h1></body></html>";
        }
    }
    
    @Override
    public String renderComponent(Map<String, Object> componentData, String renderType, Map<String, Object> options) {
        try {
            String componentType = (String) componentData.get("componentType");
            String componentId = componentData.get("componentId").toString();
            Map<String, Object> position = (Map<String, Object>) componentData.get("position");
            
            StringBuilder component = new StringBuilder();
            
            if ("mobile".equals(renderType)) {
                // 移动端组件渲染
                component.append("<div class=\"mobile-component mobile-").append(componentType).append("\" data-component-id=\"").append(componentId).append("\">\n");
                
                switch (componentType) {
                    case "table":
                        component.append(renderMobileTable(componentData));
                        break;
                    case "bar-chart":
                    case "line-chart":
                    case "pie-chart":
                        component.append(renderMobileChart(componentData));
                        break;
                    case "text":
                        component.append(renderMobileText(componentData));
                        break;
                    case "image":
                        component.append(renderMobileImage(componentData));
                        break;
                    default:
                        component.append("<div class=\"unsupported-component\">不支持的组件类型: ").append(componentType).append("</div>");
                }
                
                component.append("</div>\n");
                
            } else {
                // 桌面端组件渲染
                int x = (Integer) position.get("x");
                int y = (Integer) position.get("y");
                int width = (Integer) position.get("width");
                int height = (Integer) position.get("height");
                Integer zIndex = (Integer) componentData.get("zIndex");
                
                component.append("<div class=\"report-component component-").append(componentType).append("\" ");
                component.append("data-component-id=\"").append(componentId).append("\" ");
                component.append("style=\"position: absolute; left: ").append(x).append("px; top: ").append(y).append("px; ");
                component.append("width: ").append(width).append("px; height: ").append(height).append("px; ");
                if (zIndex != null) {
                    component.append("z-index: ").append(zIndex).append("; ");
                }
                component.append("\">\n");
                
                switch (componentType) {
                    case "table":
                        component.append(renderTable(componentData, renderType));
                        break;
                    case "bar-chart":
                    case "line-chart":
                    case "pie-chart":
                        component.append(renderChart(componentData, renderType));
                        break;
                    case "text":
                        component.append(renderText(componentData, renderType));
                        break;
                    case "image":
                        component.append(renderImage(componentData, renderType));
                        break;
                    case "divider":
                        component.append(renderDivider(componentData, renderType));
                        break;
                    default:
                        component.append("<div class=\"unsupported-component\">不支持的组件类型: ").append(componentType).append("</div>");
                }
                
                component.append("</div>\n");
            }
            
            return component.toString();
            
        } catch (Exception e) {
            log.error("组件渲染失败", e);
            return "<div class=\"component-error\">组件渲染失败: " + e.getMessage() + "</div>";
        }
    }
    
    @Override
    public String generateStyles(Map<String, Object> reportData, String renderType) {
        StringBuilder css = new StringBuilder();
        
        if ("mobile".equals(renderType)) {
            css.append(generateMobileStyles());
        } else if ("print".equals(renderType)) {
            css.append(generatePrintStyles());
        } else {
            css.append(generateDesktopStyles());
        }
        
        // 添加自定义样式
        Map<String, Object> styleConfig = (Map<String, Object>) reportData.get("styleConfig");
        if (styleConfig != null) {
            css.append(generateCustomStyles(styleConfig));
        }
        
        return css.toString();
    }
    
    @Override
    public String generateScripts(Map<String, Object> reportData, String renderType) {
        StringBuilder js = new StringBuilder();
        
        // 初始化图表
        List<Map<String, Object>> components = (List<Map<String, Object>>) reportData.get("components");
        if (components != null) {
            for (Map<String, Object> component : components) {
                String componentType = (String) component.get("componentType");
                if (componentType.contains("chart")) {
                    js.append(generateChartScript(component, renderType));
                }
            }
        }
        
        // 添加交互功能
        if (!"print".equals(renderType)) {
            js.append(generateInteractionScripts(renderType));
        }
        
        return js.toString();
    }
    
    // 私有辅助方法
    
    private String formatParameters(Map<String, Object> parameters) {
        return parameters.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(", "));
    }
    
    private String renderTable(Map<String, Object> componentData, String renderType) {
        Map<String, Object> data = (Map<String, Object>) componentData.get("data");
        Map<String, Object> tableConfig = (Map<String, Object>) componentData.get("tableConfig");
        
        if (data == null) {
            return "<div class=\"no-data\">暂无数据</div>";
        }
        
        StringBuilder table = new StringBuilder();
        table.append("<div class=\"table-container\">\n");
        table.append("<table class=\"report-table\">\n");
        
        // 表头
        List<Map<String, Object>> columns = (List<Map<String, Object>>) data.get("columns");
        if (columns != null && !columns.isEmpty()) {
            table.append("<thead><tr>\n");
            for (Map<String, Object> column : columns) {
                table.append("<th>").append(column.get("title")).append("</th>\n");
            }
            table.append("</tr></thead>\n");
        }
        
        // 表体
        List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records");
        if (records != null && !records.isEmpty()) {
            table.append("<tbody>\n");
            for (Map<String, Object> record : records) {
                table.append("<tr>\n");
                if (columns != null) {
                    for (Map<String, Object> column : columns) {
                        String dataIndex = (String) column.get("dataIndex");
                        Object value = record.get(dataIndex);
                        table.append("<td>").append(value != null ? value.toString() : "").append("</td>\n");
                    }
                }
                table.append("</tr>\n");
            }
            table.append("</tbody>\n");
        }
        
        table.append("</table>\n");
        table.append("</div>\n");
        
        return table.toString();
    }
    
    private String renderChart(Map<String, Object> componentData, String renderType) {
        String componentId = componentData.get("componentId").toString();
        String componentType = (String) componentData.get("componentType");
        
        return "<div id=\"chart_" + componentId + "\" class=\"chart-container " + componentType + "\" style=\"width: 100%; height: 100%;\"></div>";
    }
    
    private String renderText(Map<String, Object> componentData, String renderType) {
        Map<String, Object> textConfig = (Map<String, Object>) componentData.get("textConfig");
        Map<String, Object> styleConfig = (Map<String, Object>) componentData.get("styleConfig");
        
        String content = textConfig != null ? (String) textConfig.get("content") : "文本内容";
        String textType = textConfig != null ? (String) textConfig.get("textType") : "text";
        
        StringBuilder styles = new StringBuilder();
        if (styleConfig != null) {
            if (styleConfig.get("fontSize") != null) {
                styles.append("font-size: ").append(styleConfig.get("fontSize")).append("px; ");
            }
            if (styleConfig.get("color") != null) {
                styles.append("color: ").append(styleConfig.get("color")).append("; ");
            }
            if (styleConfig.get("textAlign") != null) {
                styles.append("text-align: ").append(styleConfig.get("textAlign")).append("; ");
            }
        }
        
        String tag = "text".equals(textType) ? "div" : ("title".equals(textType) ? "h3" : "p");
        
        return "<" + tag + " class=\"text-component\" style=\"" + styles.toString() + "\">" + content + "</" + tag + ">";
    }
    
    private String renderImage(Map<String, Object> componentData, String renderType) {
        Map<String, Object> imageConfig = (Map<String, Object>) componentData.get("imageConfig");
        
        if (imageConfig == null) {
            return "<div class=\"no-image\">暂无图片</div>";
        }
        
        String src = (String) imageConfig.get("src");
        String alt = (String) imageConfig.get("alt");
        
        if (src == null || src.trim().isEmpty()) {
            return "<div class=\"no-image\">暂无图片</div>";
        }
        
        return "<img src=\"" + src + "\" alt=\"" + (alt != null ? alt : "图片") + "\" style=\"width: 100%; height: 100%; object-fit: cover;\" />";
    }
    
    private String renderDivider(Map<String, Object> componentData, String renderType) {
        Map<String, Object> styleConfig = (Map<String, Object>) componentData.get("styleConfig");
        
        String color = styleConfig != null ? (String) styleConfig.get("color") : "#d9d9d9";
        
        return "<hr style=\"border: none; border-top: 1px solid " + color + "; margin: 0; width: 100%;\" />";
    }
    
    private String renderMobileTable(Map<String, Object> componentData) {
        // 移动端表格采用卡片式布局
        Map<String, Object> data = (Map<String, Object>) componentData.get("data");
        
        if (data == null) {
            return "<div class=\"mobile-no-data\">暂无数据</div>";
        }
        
        StringBuilder cards = new StringBuilder();
        cards.append("<div class=\"mobile-table-cards\">\n");
        
        List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records");
        List<Map<String, Object>> columns = (List<Map<String, Object>>) data.get("columns");
        
        if (records != null && columns != null) {
            for (Map<String, Object> record : records) {
                cards.append("<div class=\"mobile-table-card\">\n");
                for (Map<String, Object> column : columns) {
                    String dataIndex = (String) column.get("dataIndex");
                    String title = (String) column.get("title");
                    Object value = record.get(dataIndex);
                    
                    cards.append("<div class=\"mobile-table-row\">\n");
                    cards.append("<span class=\"mobile-table-label\">").append(title).append(":</span>\n");
                    cards.append("<span class=\"mobile-table-value\">").append(value != null ? value.toString() : "").append("</span>\n");
                    cards.append("</div>\n");
                }
                cards.append("</div>\n");
            }
        }
        
        cards.append("</div>\n");
        return cards.toString();
    }
    
    private String renderMobileChart(Map<String, Object> componentData) {
        String componentId = componentData.get("componentId").toString();
        String componentType = (String) componentData.get("componentType");
        
        return "<div id=\"mobile_chart_" + componentId + "\" class=\"mobile-chart-container " + componentType + "\" style=\"width: 100%; height: 300px;\"></div>";
    }
    
    private String renderMobileText(Map<String, Object> componentData) {
        Map<String, Object> textConfig = (Map<String, Object>) componentData.get("textConfig");
        String content = textConfig != null ? (String) textConfig.get("content") : "文本内容";
        
        return "<div class=\"mobile-text-component\">" + content + "</div>";
    }
    
    private String renderMobileImage(Map<String, Object> componentData) {
        Map<String, Object> imageConfig = (Map<String, Object>) componentData.get("imageConfig");
        
        if (imageConfig == null) {
            return "<div class=\"mobile-no-image\">暂无图片</div>";
        }
        
        String src = (String) imageConfig.get("src");
        String alt = (String) imageConfig.get("alt");
        
        if (src == null || src.trim().isEmpty()) {
            return "<div class=\"mobile-no-image\">暂无图片</div>";
        }
        
        return "<img src=\"" + src + "\" alt=\"" + (alt != null ? alt : "图片") + "\" class=\"mobile-image\" />";
    }
    
    private String generateDesktopStyles() {
        return """
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }
            .report-container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
            .report-header { text-align: center; margin-bottom: 30px; border-bottom: 1px solid #e8e8e8; padding-bottom: 20px; }
            .report-header h1 { margin: 0; color: #333; font-size: 28px; }
            .report-description { color: #666; margin: 10px 0; font-size: 14px; }
            .report-meta { color: #999; font-size: 12px; margin-top: 10px; }
            .report-meta span { margin-right: 20px; }
            .report-canvas { border-radius: 4px; }
            .report-component { border-radius: 4px; overflow: hidden; }
            .table-container { width: 100%; height: 100%; overflow: auto; }
            .report-table { width: 100%; border-collapse: collapse; font-size: 12px; }
            .report-table th, .report-table td { border: 1px solid #e8e8e8; padding: 8px; text-align: left; }
            .report-table th { background: #fafafa; font-weight: 600; }
            .chart-container { background: white; border-radius: 4px; }
            .text-component { width: 100%; height: 100%; display: flex; align-items: center; justify-content: flex-start; padding: 8px; box-sizing: border-box; overflow: hidden; }
            .no-data, .no-image { display: flex; align-items: center; justify-content: center; color: #999; background: #f9f9f9; border: 1px dashed #d9d9d9; border-radius: 4px; width: 100%; height: 100%; }
            .component-error { background: #fff2f0; border: 1px solid #ffccc7; color: #ff4d4f; padding: 8px; border-radius: 4px; }
            """;
    }
    
    private String generateMobileStyles() {
        return """
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 0; background: #f5f5f5; }
            .mobile-report-container { padding: 15px; }
            .mobile-report-header { background: white; padding: 15px; border-radius: 8px; margin-bottom: 15px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
            .mobile-report-header h2 { margin: 0; color: #333; font-size: 20px; }
            .mobile-report-meta { color: #999; font-size: 12px; margin-top: 8px; }
            .mobile-components { display: flex; flex-direction: column; gap: 15px; }
            .mobile-component-wrapper { background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
            .mobile-component { padding: 15px; }
            .mobile-table-cards { display: flex; flex-direction: column; gap: 10px; }
            .mobile-table-card { background: #f9f9f9; padding: 12px; border-radius: 6px; border: 1px solid #e8e8e8; }
            .mobile-table-row { display: flex; justify-content: space-between; align-items: center; padding: 4px 0; }
            .mobile-table-label { font-weight: 600; color: #666; font-size: 13px; }
            .mobile-table-value { color: #333; font-size: 13px; }
            .mobile-chart-container { background: white; border-radius: 4px; }
            .mobile-text-component { color: #333; line-height: 1.6; }
            .mobile-image { width: 100%; height: auto; border-radius: 4px; }
            .mobile-no-data, .mobile-no-image { text-align: center; color: #999; padding: 20px; background: #f9f9f9; border-radius: 4px; }
            """;
    }
    
    private String generatePrintStyles() {
        return """
            @page { margin: 20mm; }
            body { font-family: 'Times New Roman', serif; margin: 0; padding: 0; background: white; color: black; font-size: 12pt; }
            .print-report-container { width: 100%; }
            .print-header { text-align: center; margin-bottom: 20pt; border-bottom: 1pt solid black; padding-bottom: 10pt; }
            .print-header h1 { margin: 0; font-size: 18pt; font-weight: bold; }
            .print-meta { font-size: 10pt; margin-top: 5pt; }
            .print-content { }
            .report-component { page-break-inside: avoid; margin-bottom: 10pt; }
            .report-table { width: 100%; border-collapse: collapse; font-size: 10pt; }
            .report-table th, .report-table td { border: 0.5pt solid black; padding: 4pt; }
            .report-table th { background: #f0f0f0; font-weight: bold; }
            .text-component { margin-bottom: 5pt; }
            .chart-container { border: 0.5pt solid black; margin-bottom: 10pt; }
            .no-data, .no-image { border: 0.5pt dashed gray; padding: 10pt; text-align: center; color: gray; }
            """;
    }
    
    private String generateCustomStyles(Map<String, Object> styleConfig) {
        StringBuilder css = new StringBuilder();
        
        // 根据样式配置生成自定义CSS
        if (styleConfig.get("primaryColor") != null) {
            css.append(".report-header h1 { color: ").append(styleConfig.get("primaryColor")).append("; }\n");
        }
        
        if (styleConfig.get("backgroundColor") != null) {
            css.append(".report-canvas { background-color: ").append(styleConfig.get("backgroundColor")).append("; }\n");
        }
        
        return css.toString();
    }
    
    private String generateChartScript(Map<String, Object> componentData, String renderType) {
        String componentId = componentData.get("componentId").toString();
        String componentType = (String) componentData.get("componentType");
        Map<String, Object> chartConfig = (Map<String, Object>) componentData.get("chartConfig");
        Map<String, Object> data = (Map<String, Object>) componentData.get("data");
        
        String chartElementId = "mobile".equals(renderType) ? "mobile_chart_" + componentId : "chart_" + componentId;
        
        StringBuilder js = new StringBuilder();
        js.append("(function() {\n");
        js.append("    var chartDom = document.getElementById('").append(chartElementId).append("');\n");
        js.append("    if (!chartDom) return;\n");
        js.append("    var myChart = echarts.init(chartDom);\n");
        
        // 生成ECharts配置
        js.append("    var option = ").append(generateChartOption(componentType, chartConfig, data)).append(";\n");
        js.append("    myChart.setOption(option);\n");
        
        // 响应式调整
        if (!"print".equals(renderType)) {
            js.append("    window.addEventListener('resize', function() { myChart.resize(); });\n");
        }
        
        js.append("})();\n");
        
        return js.toString();
    }
    
    private String generateChartOption(String componentType, Map<String, Object> chartConfig, Map<String, Object> data) {
        // 生成ECharts配置选项
        try {
            Map<String, Object> option = new HashMap<>();
            
            // 基础配置
            if (chartConfig != null && chartConfig.get("title") != null) {
                option.put("title", Map.of("text", chartConfig.get("title")));
            }
            
            option.put("tooltip", Map.of("trigger", "axis"));
            option.put("legend", Map.of("show", chartConfig != null ? chartConfig.getOrDefault("showLegend", true) : true));
            
            // 根据图表类型生成配置
            // 这里简化处理，实际应该根据数据和配置生成完整的ECharts配置
            option.put("series", List.of(Map.of(
                "name", chartConfig != null ? chartConfig.getOrDefault("seriesName", "数据") : "数据",
                "type", componentType.replace("-chart", ""),
                "data", data != null ? data.getOrDefault("records", new ArrayList<>()) : new ArrayList<>()
            )));
            
            return objectMapper.writeValueAsString(option);
        } catch (Exception e) {
            log.error("生成图表配置失败", e);
            return "{}";
        }
    }
    
    private String generateInteractionScripts(String renderType) {
        if ("mobile".equals(renderType)) {
            return """
                // 移动端交互脚本
                document.addEventListener('DOMContentLoaded', function() {
                    // 添加触摸滚动优化
                    document.body.style.webkitOverflowScrolling = 'touch';
                    
                    // 图表容器点击事件
                    document.querySelectorAll('.mobile-chart-container').forEach(function(chart) {
                        chart.addEventListener('touchstart', function(e) {
                            e.stopPropagation();
                        });
                    });
                });
                """;
        } else {
            return """
                // 桌面端交互脚本
                document.addEventListener('DOMContentLoaded', function() {
                    // 添加组件悬停效果
                    document.querySelectorAll('.report-component').forEach(function(component) {
                        component.addEventListener('mouseenter', function() {
                            this.style.boxShadow = '0 2px 8px rgba(0,0,0,0.15)';
                        });
                        component.addEventListener('mouseleave', function() {
                            this.style.boxShadow = '';
                        });
                    });
                });
                """;
        }
    }
}