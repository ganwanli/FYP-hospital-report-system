package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.report.dto.*;
import com.hospital.report.entity.DictCategory;
import com.hospital.report.exception.BusinessException;
import com.hospital.report.mapper.DictCategoryMapper;
import com.hospital.report.service.DictCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据字典分类服务实现类
 * 
 * @author system
 * @since 2025-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictCategoryServiceImpl extends ServiceImpl<DictCategoryMapper, DictCategory> implements DictCategoryService {

    private final DictCategoryMapper dictCategoryMapper;

    @Override
    @Cacheable(value = "dict:category:tree", key = "'all'")
    public List<CategoryTreeVO> getCategoryTree() {
        log.info("获取完整分类树");
        List<DictCategory> categories = dictCategoryMapper.selectAllWithHierarchy();
        return buildCategoryTree(categories);
    }

    @Override
    @Cacheable(value = "dict:category:tree", key = "'enabled'")
    public List<CategoryTreeVO> getEnabledCategoryTree() {
        log.info("获取启用的分类树");
        LambdaQueryWrapper<DictCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictCategory::getStatus, 1)
                .orderByAsc(DictCategory::getCategoryLevel)
                .orderByAsc(DictCategory::getSortOrder)
                .orderByAsc(DictCategory::getCreateTime);
        List<DictCategory> categories = list(wrapper);
        return buildCategoryTree(categories);
    }

    @Override
    public Page<DictCategoryVO> getCategoryList(Long current, Long size, String keyword) {
        log.info("获取分类列表，页码：{}，页大小：{}，关键词：{}", current, size, keyword);
        
        Page<DictCategory> page = new Page<>(current, size);
        List<DictCategory> categories;
        
        if (StringUtils.hasText(keyword)) {
            categories = dictCategoryMapper.searchCategories(keyword);
        } else {
            categories = dictCategoryMapper.selectAllWithHierarchy();
        }
        
        // 手动分页
        int start = (int) ((current - 1) * size);
        int end = Math.min(start + size.intValue(), categories.size());
        List<DictCategory> pageData = categories.subList(start, end);
        
        page.setRecords(pageData);
        page.setTotal(categories.size());
        
        // 转换为VO
        Page<DictCategoryVO> result = new Page<>();
        BeanUtils.copyProperties(page, result);
        result.setRecords(pageData.stream().map(this::convertToVO).collect(Collectors.toList()));
        
        return result;
    }

    @Override
    public DictCategoryVO getCategoryById(Long id) {
        log.info("根据ID获取分类详情，ID：{}", id);
        DictCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dict:category:tree", allEntries = true)
    public DictCategoryVO createCategory(DictCategoryDTO dto) {
        log.info("新增分类：{}", dto);
        
        // 验证分类编码唯一性
        if (checkCategoryCodeExists(dto.getCategoryCode(), null)) {
            throw new BusinessException("分类编码已存在");
        }
        
        // 验证父级分类存在性
        if (dto.getParentId() != 0) {
            DictCategory parent = getById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父级分类不存在");
            }
        }
        
        DictCategory category = new DictCategory();
        BeanUtils.copyProperties(dto, category);
        
        // 计算层级
        category.setCategoryLevel(calculateCategoryLevel(dto.getParentId()));
        
        // 设置排序号
        if (category.getSortOrder() == null || category.getSortOrder() == 0) {
            category.setSortOrder(getNextSortOrder(dto.getParentId()));
        }
        
        // 设置默认状态
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        
        save(category);
        
        log.info("分类新增成功，ID：{}", category.getId());
        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dict:category:tree", allEntries = true)
    public DictCategoryVO updateCategory(Long id, DictCategoryDTO dto) {
        log.info("修改分类，ID：{}，数据：{}", id, dto);
        
        DictCategory existingCategory = getById(id);
        if (existingCategory == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 验证分类编码唯一性
        if (checkCategoryCodeExists(dto.getCategoryCode(), id)) {
            throw new BusinessException("分类编码已存在");
        }
        
        // 验证父级分类
        if (dto.getParentId() != 0) {
            if (dto.getParentId().equals(id)) {
                throw new BusinessException("不能将自己设置为父级分类");
            }
            
            DictCategory parent = getById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父级分类不存在");
            }
            
            // 检查是否会形成循环引用
            if (isCircularReference(id, dto.getParentId())) {
                throw new BusinessException("不能将子分类设置为父级分类");
            }
        }
        
        DictCategory category = new DictCategory();
        BeanUtils.copyProperties(dto, category);
        category.setId(id);
        
        // 如果父级分类发生变化，重新计算层级
        if (!existingCategory.getParentId().equals(dto.getParentId())) {
            category.setCategoryLevel(calculateCategoryLevel(dto.getParentId()));
            updateChildrenLevel(id);
        }
        
        updateById(category);
        
        log.info("分类修改成功，ID：{}", id);
        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dict:category:tree", allEntries = true)
    public boolean deleteCategory(Long id) {
        log.info("删除分类，ID：{}", id);
        
        validateCategoryCanDelete(id);
        
        boolean result = removeById(id);
        
        if (result) {
            log.info("分类删除成功，ID：{}", id);
        } else {
            log.error("分类删除失败，ID：{}", id);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dict:category:tree", allEntries = true)
    public boolean deleteCategoriesBatch(List<Long> ids) {
        log.info("批量删除分类，IDs：{}", ids);
        
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException("删除的分类ID列表不能为空");
        }
        
        // 验证每个分类是否可以删除
        for (Long id : ids) {
            validateCategoryCanDelete(id);
        }
        
        boolean result = removeByIds(ids);
        
        if (result) {
            log.info("分类批量删除成功，数量：{}", ids.size());
        } else {
            log.error("分类批量删除失败，IDs：{}", ids);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dict:category:tree", allEntries = true)
    public boolean updateSort(CategorySortDTO dto) {
        log.info("调整分类排序：{}", dto);
        
        if (CollectionUtils.isEmpty(dto.getSortItems())) {
            throw new BusinessException("排序项列表不能为空");
        }
        
        // TODO: 获取当前用户
        String currentUser = "system";
        
        for (CategorySortDTO.SortItem item : dto.getSortItems()) {
            dictCategoryMapper.updateSortOrder(item.getId(), item.getSortOrder(), currentUser);
        }
        
        log.info("分类排序调整成功");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dict:category:tree", allEntries = true)
    public boolean updateStatus(CategoryStatusDTO dto) {
        log.info("批量修改分类状态：{}", dto);
        
        if (CollectionUtils.isEmpty(dto.getIds())) {
            throw new BusinessException("分类ID列表不能为空");
        }
        
        // 如果是禁用操作，需要验证每个分类是否可以禁用
        if (dto.getStatus() == 0) {
            for (Long id : dto.getIds()) {
                validateCategoryCanDisable(id);
            }
        }
        
        // TODO: 获取当前用户
        String currentUser = "system";
        
        int updateCount = dictCategoryMapper.batchUpdateStatus(dto.getIds(), dto.getStatus(), currentUser);
        
        log.info("分类状态批量修改成功，影响行数：{}", updateCount);
        return updateCount > 0;
    }

    @Override
    public List<DictCategoryVO> getChildrenByParentId(Long parentId) {
        log.info("获取子分类列表，父级ID：{}", parentId);
        List<DictCategory> children = dictCategoryMapper.selectChildrenByParentId(parentId);
        return children.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<DictCategoryVO> getCategoryPath(Long id) {
        log.info("获取分类路径，ID：{}", id);
        List<DictCategory> path = dictCategoryMapper.selectCategoryPath(id);
        return path.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public boolean checkCategoryCodeExists(String categoryCode, Long excludeId) {
        int count = dictCategoryMapper.checkCategoryCodeExists(categoryCode, excludeId);
        return count > 0;
    }

    @Override
    public List<DictCategoryVO> getCategoriesByLevel(Integer level) {
        log.info("根据层级获取分类列表，层级：{}", level);
        List<DictCategory> categories = dictCategoryMapper.selectByLevel(level);
        return categories.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<DictCategoryVO> getTopLevelCategories() {
        log.info("获取所有顶级分类");
        return getCategoriesByLevel(1);
    }

    @Override
    public List<CategoryTreeVO> buildCategoryTree(List<DictCategory> categories) {
        return buildCategoryTree(categories, 0L);
    }

    @Override
    public List<CategoryTreeVO> buildCategoryTree(List<DictCategory> categories, Long parentId) {
        if (CollectionUtils.isEmpty(categories)) {
            return new ArrayList<>();
        }

        // 按父级ID分组
        Map<Long, List<DictCategory>> categoryMap = categories.stream()
                .collect(Collectors.groupingBy(DictCategory::getParentId));

        // 构建树形结构
        return buildTreeRecursive(categoryMap, parentId);
    }

    @Override
    public void validateCategoryCanDelete(Long id) {
        DictCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查是否有子分类
        int childrenCount = dictCategoryMapper.countChildren(id);
        if (childrenCount > 0) {
            throw new BusinessException("该分类下存在子分类，不能删除");
        }

        // 检查是否有关联字段
        int fieldCount = dictCategoryMapper.countRelatedFields(id);
        if (fieldCount > 0) {
            throw new BusinessException("该分类下存在关联字段，不能删除");
        }
    }

    @Override
    public void validateCategoryCanDisable(Long id) {
        DictCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查子分类是否都已禁用
        List<DictCategory> children = dictCategoryMapper.selectChildrenByParentId(id);
        boolean hasEnabledChildren = children.stream().anyMatch(child -> child.getStatus() == 1);
        if (hasEnabledChildren) {
            throw new BusinessException("该分类下存在启用的子分类，请先禁用子分类");
        }
    }

    @Override
    public Integer calculateCategoryLevel(Long parentId) {
        if (parentId == null || parentId == 0) {
            return 1;
        }

        DictCategory parent = getById(parentId);
        if (parent == null) {
            throw new BusinessException("父级分类不存在");
        }

        return parent.getCategoryLevel() + 1;
    }

    @Override
    public Integer getNextSortOrder(Long parentId) {
        Integer maxSortOrder = dictCategoryMapper.selectMaxSortOrderByParentId(parentId);
        return maxSortOrder + 1;
    }

    @Override
    @CacheEvict(value = "dict:category:tree", allEntries = true)
    public void refreshCategoryCache() {
        log.info("刷新分类缓存");
    }

    @Override
    public DictCategoryVO getCategoryByCode(String categoryCode) {
        log.info("根据编码获取分类，编码：{}", categoryCode);
        DictCategory category = dictCategoryMapper.selectByCodeWithStats(categoryCode);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return convertToVO(category);
    }

    @Override
    public List<DictCategoryVO> searchCategories(String keyword) {
        log.info("搜索分类，关键词：{}", keyword);
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        List<DictCategory> categories = dictCategoryMapper.searchCategories(keyword);
        return categories.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 递归构建树形结构
     */
    private List<CategoryTreeVO> buildTreeRecursive(Map<Long, List<DictCategory>> categoryMap, Long parentId) {
        List<DictCategory> children = categoryMap.get(parentId);
        if (CollectionUtils.isEmpty(children)) {
            return new ArrayList<>();
        }

        return children.stream()
                .sorted(Comparator.comparing(DictCategory::getSortOrder)
                        .thenComparing(DictCategory::getCreateTime))
                .map(category -> {
                    CategoryTreeVO treeVO = convertToTreeVO(category);
                    List<CategoryTreeVO> childrenTree = buildTreeRecursive(categoryMap, category.getId());
                    treeVO.setChildren(childrenTree);
                    treeVO.setHasChildren(!CollectionUtils.isEmpty(childrenTree));
                    treeVO.setTreeProperties();
                    return treeVO;
                })
                .collect(Collectors.toList());
    }

    /**
     * 检查是否会形成循环引用
     */
    private boolean isCircularReference(Long categoryId, Long parentId) {
        if (parentId == null || parentId == 0) {
            return false;
        }

        List<Long> allChildrenIds = dictCategoryMapper.selectAllChildrenIds(categoryId);
        return allChildrenIds.contains(parentId);
    }

    /**
     * 更新子分类的层级
     */
    private void updateChildrenLevel(Long parentId) {
        List<DictCategory> children = dictCategoryMapper.selectChildrenByParentId(parentId);
        for (DictCategory child : children) {
            child.setCategoryLevel(calculateCategoryLevel(parentId));
            updateById(child);
            // 递归更新子分类的子分类
            updateChildrenLevel(child.getId());
        }
    }

    /**
     * 转换为VO对象
     */
    private DictCategoryVO convertToVO(DictCategory category) {
        if (category == null) {
            return null;
        }

        DictCategoryVO vo = new DictCategoryVO();
        BeanUtils.copyProperties(category, vo);

        // 设置额外属性
        vo.setHasChildren(category.getHasChildren());
        vo.setFieldCount(category.getFieldCount());
        vo.setParentName(category.getParentName());

        // 构建分类路径
        if (category.getParentId() != null && category.getParentId() != 0) {
            try {
                List<DictCategoryVO> path = getCategoryPath(category.getId());
                String categoryPath = path.stream()
                        .map(DictCategoryVO::getCategoryName)
                        .collect(Collectors.joining(" / "));
                vo.setCategoryPath(categoryPath);
            } catch (Exception e) {
                vo.setCategoryPath(category.getCategoryName());
            }
        } else {
            vo.setCategoryPath(category.getCategoryName());
        }

        return vo;
    }

    /**
     * 转换为树形VO对象
     */
    private CategoryTreeVO convertToTreeVO(DictCategory category) {
        if (category == null) {
            return null;
        }

        CategoryTreeVO treeVO = new CategoryTreeVO();
        BeanUtils.copyProperties(category, treeVO);

        // 设置额外属性
        treeVO.setHasChildren(category.getHasChildren());
        treeVO.setFieldCount(category.getFieldCount());

        return treeVO;
    }
}
