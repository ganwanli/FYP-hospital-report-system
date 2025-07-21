package com.hospital.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.report.dto.*;
import com.hospital.report.entity.DictCategory;

import java.util.List;

/**
 * 数据字典分类服务接口
 * 
 * @author system
 * @since 2025-01-15
 */
public interface DictCategoryService extends IService<DictCategory> {

    /**
     * 获取分类树
     * 
     * @return 分类树列表
     */
    List<CategoryTreeVO> getCategoryTree();

    /**
     * 获取分类树（只包含启用的分类）
     * 
     * @return 分类树列表
     */
    List<CategoryTreeVO> getEnabledCategoryTree();

    /**
     * 获取分类列表（分页）
     * 
     * @param current 当前页
     * @param size 页大小
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    Page<DictCategoryVO> getCategoryList(Long current, Long size, String keyword);

    /**
     * 根据ID获取分类详情
     * 
     * @param id 分类ID
     * @return 分类详情
     */
    DictCategoryVO getCategoryById(Long id);

    /**
     * 新增分类
     * 
     * @param dto 分类信息
     * @return 新增的分类
     */
    DictCategoryVO createCategory(DictCategoryDTO dto);

    /**
     * 修改分类
     * 
     * @param id 分类ID
     * @param dto 分类信息
     * @return 修改后的分类
     */
    DictCategoryVO updateCategory(Long id, DictCategoryDTO dto);

    /**
     * 删除分类
     * 
     * @param id 分类ID
     * @return 是否删除成功
     */
    boolean deleteCategory(Long id);

    /**
     * 批量删除分类
     * 
     * @param ids 分类ID列表
     * @return 是否删除成功
     */
    boolean deleteCategoriesBatch(List<Long> ids);

    /**
     * 调整排序
     * 
     * @param dto 排序信息
     * @return 是否调整成功
     */
    boolean updateSort(CategorySortDTO dto);

    /**
     * 批量修改状态
     * 
     * @param dto 状态信息
     * @return 是否修改成功
     */
    boolean updateStatus(CategoryStatusDTO dto);

    /**
     * 获取子分类列表
     * 
     * @param parentId 父级分类ID
     * @return 子分类列表
     */
    List<DictCategoryVO> getChildrenByParentId(Long parentId);

    /**
     * 获取分类路径
     * 
     * @param id 分类ID
     * @return 分类路径列表
     */
    List<DictCategoryVO> getCategoryPath(Long id);

    /**
     * 检查分类编码是否存在
     * 
     * @param categoryCode 分类编码
     * @param excludeId 排除的分类ID
     * @return 是否存在
     */
    boolean checkCategoryCodeExists(String categoryCode, Long excludeId);

    /**
     * 根据层级获取分类列表
     * 
     * @param level 分类层级
     * @return 分类列表
     */
    List<DictCategoryVO> getCategoriesByLevel(Integer level);

    /**
     * 获取所有顶级分类
     * 
     * @return 顶级分类列表
     */
    List<DictCategoryVO> getTopLevelCategories();

    /**
     * 构建分类树
     * 
     * @param categories 分类列表
     * @return 分类树
     */
    List<CategoryTreeVO> buildCategoryTree(List<DictCategory> categories);

    /**
     * 构建分类树（指定根节点）
     *
     * @param categories 分类列表
     * @param parentId 父级分类ID
     * @return 分类树
     */
    List<CategoryTreeVO> buildCategoryTree(List<DictCategory> categories, Long parentId);

    /**
     * 获取分类字段混合树
     * 分类作为分支节点，字段作为叶子节点
     *
     * @return 分类字段混合树
     */
    List<CategoryFieldTreeVO> getCategoryFieldTree();

    /**
     * 获取启用的分类字段混合树
     * 分类作为分支节点，字段作为叶子节点
     *
     * @return 启用的分类字段混合树
     */
    List<CategoryFieldTreeVO> getEnabledCategoryFieldTree();

    /**
     * 验证分类是否可以删除
     * 
     * @param id 分类ID
     * @throws RuntimeException 如果不能删除则抛出异常
     */
    void validateCategoryCanDelete(Long id);

    /**
     * 验证分类是否可以禁用
     * 
     * @param id 分类ID
     * @throws RuntimeException 如果不能禁用则抛出异常
     */
    void validateCategoryCanDisable(Long id);

    /**
     * 计算分类层级
     * 
     * @param parentId 父级分类ID
     * @return 分类层级
     */
    Integer calculateCategoryLevel(Long parentId);

    /**
     * 获取下一个排序号
     * 
     * @param parentId 父级分类ID
     * @return 下一个排序号
     */
    Integer getNextSortOrder(Long parentId);

    /**
     * 刷新分类缓存
     */
    void refreshCategoryCache();

    /**
     * 根据编码获取分类
     * 
     * @param categoryCode 分类编码
     * @return 分类信息
     */
    DictCategoryVO getCategoryByCode(String categoryCode);

    /**
     * 搜索分类
     * 
     * @param keyword 关键词
     * @return 分类列表
     */
    List<DictCategoryVO> searchCategories(String keyword);
}
