package com.hospital.report.service;

import java.util.Map;

/**
 * 报表导出服务接口
 */
public interface ExportService {
    
    /**
     * 导出为PDF
     * @param reportData 报表数据
     * @param options 导出选项
     * @return PDF字节数组
     */
    byte[] exportToPdf(Map<String, Object> reportData, Map<String, Object> options);
    
    /**
     * 导出为Excel
     * @param reportData 报表数据
     * @param options 导出选项
     * @return Excel字节数组
     */
    byte[] exportToExcel(Map<String, Object> reportData, Map<String, Object> options);
    
    /**
     * 导出为Word
     * @param reportData 报表数据
     * @param options 导出选项
     * @return Word字节数组
     */
    byte[] exportToWord(Map<String, Object> reportData, Map<String, Object> options);
    
    /**
     * 导出为CSV
     * @param reportData 报表数据
     * @param options 导出选项
     * @return CSV字节数组
     */
    byte[] exportToCsv(Map<String, Object> reportData, Map<String, Object> options);
    
    /**
     * 导出为图片
     * @param reportData 报表数据
     * @param format 图片格式（PNG/JPEG）
     * @param options 导出选项
     * @return 图片字节数组
     */
    byte[] exportToImage(Map<String, Object> reportData, String format, Map<String, Object> options);
    
    /**
     * 获取支持的导出格式
     * @return 支持的格式列表
     */
    String[] getSupportedFormats();
    
    /**
     * 验证导出参数
     * @param format 导出格式
     * @param options 导出选项
     * @return 是否有效
     */
    boolean validateExportOptions(String format, Map<String, Object> options);
}