package com.hospital.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.report.entity.DictionaryCategory;

import java.util.List;
import java.util.Map;

public interface DictionaryCategoryService extends IService<DictionaryCategory> {

    /**
     * 获取分类树
     */
    List<Map<String, Object>> getCategoryTree();

    /**
     * 获取分类列表（包含字段数量）
     */
    List<Map<String, Object>> getCategoriesWithCount();

    /**
     * 检查分类编码是否存在
     */
    boolean checkCategoryCodeExists(String categoryCode, Long excludeId);

    /**
     * 更新分类字段数量
     */
    void updateFieldCount(Long categoryId);

    /**
     * 获取子分类
     */
    List<DictionaryCategory> getChildCategories(Long parentId);

    /**
     * 删除分类（级联检查）
     */
    void deleteCategory(Long categoryId);

    /**
     * 移动分类
     */
    void moveCategory(Long categoryId, Long newParentId, Integer newSortOrder);

    /**
     * 获取分类路径
     */
    String getCategoryPath(Long categoryId);

    /**
     * 批量更新排序
     */
    void updateSortOrder(List<Map<String, Object>> categories);
}