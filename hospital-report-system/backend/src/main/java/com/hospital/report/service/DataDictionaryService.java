package com.hospital.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.report.entity.DataDictionary;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DataDictionaryService extends IService<DataDictionary> {

    /**
     * 分页查询数据字典
     */
    IPage<Map<String, Object>> getDataDictionaryPage(Integer current, Integer size, 
                                                     String keyword, Long categoryId, 
                                                     String dataType, String approvalStatus);

    /**
     * 搜索字段
     */
    List<DataDictionary> searchFields(String keyword, Integer limit);

    /**
     * 获取统计信息
     */
    Map<String, Object> getStatistics();

    /**
     * 增加使用次数
     */
    void incrementUsageCount(Long fieldId, String usageType, String usageContext, Long userId);

    /**
     * 检查字段编码是否存在
     */
    boolean checkFieldCodeExists(String fieldCode, Long excludeId);

    /**
     * 获取热门字段
     */
    List<Map<String, Object>> getPopularFields(Integer limit);

    /**
     * 全文搜索
     */
    List<Map<String, Object>> fullTextSearch(String keyword, Integer limit);

    /**
     * 批量导入字段
     */
    Map<String, Object> importFields(MultipartFile file, Long userId);

    /**
     * 导出字段
     */
    byte[] exportFields(List<Long> fieldIds);

    /**
     * 批量审批
     */
    void batchApprove(List<Long> fieldIds, String approvalStatus, String approvalUser);

    /**
     * 获取字段详情（包含使用统计）
     */
    Map<String, Object> getFieldDetail(Long fieldId);

    /**
     * 获取相关字段
     */
    List<DataDictionary> getRelatedFields(Long fieldId);

    /**
     * 复制字段
     */
    DataDictionary copyField(Long fieldId, String newFieldCode);

    /**
     * 获取字段变更历史
     */
    List<Map<String, Object>> getFieldHistory(Long fieldId);

    /**
     * 标准化字段
     */
    void standardizeField(Long fieldId, String standardReference);

    /**
     * 获取数据血缘关系
     */
    Map<String, Object> getDataLineage(String fieldCode);
}