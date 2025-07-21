package com.hospital.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.report.entity.DictCategory;
import com.hospital.report.entity.DictField;

import java.util.List;
import java.util.Map;

/**
 * 数据字段字典服务接口
 */
public interface DictFieldService extends IService<DictField> {

    /**
     * 分页查询数据字段
     *
     * @param page 分页参数
     * @param fieldName 字段名称
     * @param fieldCode 字段编码
     * @param categoryId 分类ID
     * @param fieldType 字段类型
     * @param status 状态
     * @return 分页结果
     */
    IPage<DictField> getDictFieldPage(Page<DictField> page, String fieldName, String fieldCode, 
                                      Long categoryId, String fieldType, Integer status);

    /**
     * 根据ID查询数据字段详情
     *
     * @param fieldId 字段ID
     * @return 字段详情
     */
    DictField getDictFieldById(Long fieldId);

    /**
     * 根据字段编码查询字段
     *
     * @param fieldCode 字段编码
     * @return 字段信息
     */
    DictField getDictFieldByCode(String fieldCode);

    /**
     * 创建数据字段
     *
     * @param dictField 字段信息
     * @return 创建结果
     */
    boolean createDictField(DictField dictField);

    /**
     * 更新数据字段
     *
     * @param dictField 字段信息
     * @return 更新结果
     */
    boolean updateDictField(DictField dictField);

    /**
     * 删除数据字段
     *
     * @param fieldId 字段ID
     * @param updatedBy 操作人ID
     * @return 删除结果
     */
    boolean deleteDictField(Long fieldId, Long updatedBy);

    /**
     * 批量删除数据字段
     *
     * @param fieldIds 字段ID列表
     * @param updatedBy 操作人ID
     * @return 删除结果
     */
    boolean batchDeleteDictFields(List<Long> fieldIds, Long updatedBy);

    /**
     * 获取所有分类列表
     *
     * @return 分类列表
     */
    List<DictCategory> getAllCategories();

    /**
     * 根据分类ID获取字段列表
     *
     * @param categoryId 分类ID
     * @return 字段列表
     */
    List<DictField> getFieldsByCategoryId(Long categoryId);

    /**
     * 获取所有字段列表
     *
     * @return 字段列表
     */
    List<DictField> getAllFields();

    /**
     * 获取树形结构数据
     *
     * @return 树形数据
     */
    List<DictField> getTreeData();

    /**
     * 搜索字段
     *
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    List<DictField> searchFields(String keyword);

    /**
     * 检查字段编码是否存在
     *
     * @param fieldCode 字段编码
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean checkFieldCodeExists(String fieldCode, Long excludeId);

    /**
     * 批量更新状态
     *
     * @param fieldIds 字段ID列表
     * @param status 状态
     * @param updatedBy 操作人ID
     * @return 更新结果
     */
    boolean batchUpdateStatus(List<Long> fieldIds, Integer status, Long updatedBy);

    /**
     * 获取字段统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getFieldStatistics();

    /**
     * 导出字段数据
     *
     * @param categoryId 分类ID（可选）
     * @param fieldType 字段类型（可选）
     * @return 导出数据
     */
    List<DictField> exportFields(Long categoryId, String fieldType);

    /**
     * 导入字段数据
     *
     * @param fields 字段列表
     * @param createdBy 创建人ID
     * @return 导入结果
     */
    Map<String, Object> importFields(List<DictField> fields, Long createdBy);

    /**
     * 复制字段
     *
     * @param fieldId 源字段ID
     * @param newFieldCode 新字段编码
     * @param newFieldName 新字段名称
     * @param createdBy 创建人ID
     * @return 复制结果
     */
    boolean copyField(Long fieldId, String newFieldCode, String newFieldName, Long createdBy);

    /**
     * 移动字段到指定分类
     *
     * @param fieldId 字段ID
     * @param targetCategoryId 目标分类ID
     * @param updatedBy 操作人ID
     * @return 移动结果
     */
    boolean moveFieldToCategory(Long fieldId, Long targetCategoryId, Long updatedBy);

    /**
     * 获取字段的使用统计
     *
     * @param fieldId 字段ID
     * @return 使用统计
     */
    Map<String, Object> getFieldUsageStatistics(Long fieldId);
}
