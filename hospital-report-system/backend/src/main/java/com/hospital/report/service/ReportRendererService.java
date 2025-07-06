package com.hospital.report.service;

import java.util.Map;

/**
 * 报表渲染器接口
 * 负责将报表数据渲染成不同格式
 */
public interface ReportRendererService {
    
    /**
     * 渲染HTML格式报表
     * @param reportData 报表数据
     * @param options 渲染选项
     * @return HTML内容
     */
    String renderToHtml(Map<String, Object> reportData, Map<String, Object> options);
    
    /**
     * 渲染JSON格式报表
     * @param reportData 报表数据
     * @param options 渲染选项
     * @return JSON内容
     */
    String renderToJson(Map<String, Object> reportData, Map<String, Object> options);
    
    /**
     * 渲染移动端HTML
     * @param reportData 报表数据
     * @param options 渲染选项
     * @return 移动端HTML内容
     */
    String renderToMobileHtml(Map<String, Object> reportData, Map<String, Object> options);
    
    /**
     * 渲染打印版HTML
     * @param reportData 报表数据
     * @param options 渲染选项
     * @return 打印版HTML内容
     */
    String renderToPrintHtml(Map<String, Object> reportData, Map<String, Object> options);
    
    /**
     * 渲染单个组件
     * @param componentData 组件数据
     * @param renderType 渲染类型（html/mobile/print）
     * @param options 渲染选项
     * @return 渲染结果
     */
    String renderComponent(Map<String, Object> componentData, String renderType, Map<String, Object> options);
    
    /**
     * 生成报表CSS样式
     * @param reportData 报表数据
     * @param renderType 渲染类型
     * @return CSS内容
     */
    String generateStyles(Map<String, Object> reportData, String renderType);
    
    /**
     * 生成报表JavaScript
     * @param reportData 报表数据
     * @param renderType 渲染类型
     * @return JavaScript内容
     */
    String generateScripts(Map<String, Object> reportData, String renderType);
}