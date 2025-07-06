package com.hospital.report.service;

import com.hospital.report.entity.ReportConfig;
import com.hospital.report.entity.ReportComponent;
import com.hospital.report.entity.ReportDataSource;

import java.util.Map;
import java.util.List;

/**
 * 报表生成器接口
 * 负责根据配置生成完整的报表数据
 */
public interface ReportGeneratorService {
    
    /**
     * 生成完整报表
     * @param reportId 报表ID
     * @param parameters 参数
     * @return 生成的报表数据
     */
    Map<String, Object> generateReport(Long reportId, Map<String, Object> parameters);
    
    /**
     * 生成报表组件数据
     * @param component 组件配置
     * @param dataSource 数据源配置
     * @param parameters 参数
     * @return 组件数据
     */
    Map<String, Object> generateComponentData(ReportComponent component, ReportDataSource dataSource, Map<String, Object> parameters);
    
    /**
     * 验证报表参数
     * @param reportId 报表ID
     * @param parameters 参数
     * @return 验证结果
     */
    boolean validateParameters(Long reportId, Map<String, Object> parameters);
    
    /**
     * 获取报表参数定义
     * @param reportId 报表ID
     * @return 参数定义
     */
    List<Map<String, Object>> getParameterDefinitions(Long reportId);
    
    /**
     * 生成报表缩略图
     * @param reportId 报表ID
     * @param parameters 参数
     * @return 缩略图数据
     */
    byte[] generateThumbnail(Long reportId, Map<String, Object> parameters);
    
    /**
     * 计算报表生成耗时
     * @param reportId 报表ID
     * @param parameters 参数
     * @return 估计耗时（毫秒）
     */
    long estimateGenerationTime(Long reportId, Map<String, Object> parameters);
}