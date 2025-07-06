package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.report.entity.DictionaryCategory;
import com.hospital.report.mapper.DictionaryCategoryMapper;
import com.hospital.report.mapper.DataDictionaryMapper;
import com.hospital.report.service.DictionaryCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryCategoryServiceImpl extends ServiceImpl<DictionaryCategoryMapper, DictionaryCategory> 
        implements DictionaryCategoryService {

    private final DictionaryCategoryMapper dictionaryCategoryMapper;
    private final DataDictionaryMapper dataDictionaryMapper;

    @Override
    public List<Map<String, Object>> getCategoryTree() {
        List<DictionaryCategory> allCategories = dictionaryCategoryMapper.selectAllCategories();
        return buildTree(allCategories, null);
    }

    @Override
    public List<Map<String, Object>> getCategoriesWithCount() {
        return dictionaryCategoryMapper.selectCategoriesWithCount();
    }

    @Override
    public boolean checkCategoryCodeExists(String categoryCode, Long excludeId) {
        return dictionaryCategoryMapper.checkCategoryCodeExists(
            categoryCode, 
            excludeId != null ? excludeId : -1L
        ) > 0;
    }

    @Override
    @Transactional
    public void updateFieldCount(Long categoryId) {
        dictionaryCategoryMapper.updateFieldCount(categoryId);
    }

    @Override
    public List<DictionaryCategory> getChildCategories(Long parentId) {
        return dictionaryCategoryMapper.selectByParentId(parentId);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        // 检查是否有子分类
        List<DictionaryCategory> children = getChildCategories(categoryId);
        if (!children.isEmpty()) {
            throw new RuntimeException("该分类下还有子分类，无法删除");
        }
        
        // 检查是否有字段使用该分类
        long fieldCount = dataDictionaryMapper.selectCount(
            new QueryWrapper<>().eq("category_id", categoryId).eq("is_deleted", false)
        );
        if (fieldCount > 0) {
            throw new RuntimeException("该分类下还有字段，无法删除");
        }
        
        // 软删除分类
        DictionaryCategory category = new DictionaryCategory();
        category.setId(categoryId);
        category.setIsDeleted(true);
        category.setUpdatedTime(LocalDateTime.now());
        this.updateById(category);
    }

    @Override
    @Transactional
    public void moveCategory(Long categoryId, Long newParentId, Integer newSortOrder) {
        DictionaryCategory category = this.getById(categoryId);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        
        // 检查是否会形成循环引用
        if (newParentId != null && isDescendant(categoryId, newParentId)) {
            throw new RuntimeException("不能移动到自己的子分类下");
        }
        
        // 更新分类
        category.setParentId(newParentId);
        category.setSortOrder(newSortOrder);
        category.setLevel(calculateLevel(newParentId));
        category.setUpdatedTime(LocalDateTime.now());
        
        this.updateById(category);
        
        // 更新子分类的层级
        updateChildrenLevel(categoryId);
    }

    @Override
    public String getCategoryPath(Long categoryId) {
        if (categoryId == null) {
            return "";
        }
        
        List<String> pathParts = new ArrayList<>();
        DictionaryCategory current = this.getById(categoryId);
        
        while (current != null) {
            pathParts.add(0, current.getCategoryName());
            if (current.getParentId() == null) {
                break;
            }
            current = this.getById(current.getParentId());
        }
        
        return String.join(" > ", pathParts);
    }

    @Override
    @Transactional
    public void updateSortOrder(List<Map<String, Object>> categories) {
        for (Map<String, Object> categoryData : categories) {
            Long id = Long.valueOf(categoryData.get("id").toString());
            Integer sortOrder = Integer.valueOf(categoryData.get("sortOrder").toString());
            
            DictionaryCategory category = new DictionaryCategory();
            category.setId(id);
            category.setSortOrder(sortOrder);
            category.setUpdatedTime(LocalDateTime.now());
            
            this.updateById(category);
        }
    }

    /**
     * 构建分类树
     */
    private List<Map<String, Object>> buildTree(List<DictionaryCategory> categories, Long parentId) {
        return categories.stream()
                .filter(category -> Objects.equals(category.getParentId(), parentId))
                .map(category -> {
                    Map<String, Object> node = new HashMap<>();
                    node.put("key", category.getId());
                    node.put("title", category.getCategoryName());
                    node.put("value", category.getId());
                    node.put("categoryCode", category.getCategoryCode());
                    node.put("categoryDesc", category.getCategoryDesc());
                    node.put("level", category.getLevel());
                    node.put("sortOrder", category.getSortOrder());
                    node.put("fieldCount", category.getFieldCount());
                    node.put("icon", category.getIcon());
                    node.put("color", category.getColor());
                    
                    List<Map<String, Object>> children = buildTree(categories, category.getId());
                    if (!children.isEmpty()) {
                        node.put("children", children);
                    }
                    
                    return node;
                })
                .sorted((a, b) -> {
                    Integer sortA = (Integer) a.get("sortOrder");
                    Integer sortB = (Integer) b.get("sortOrder");
                    return Integer.compare(
                        sortA != null ? sortA : 0, 
                        sortB != null ? sortB : 0
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 检查是否是子孙节点
     */
    private boolean isDescendant(Long ancestorId, Long nodeId) {
        if (ancestorId.equals(nodeId)) {
            return true;
        }
        
        DictionaryCategory node = this.getById(nodeId);
        while (node != null && node.getParentId() != null) {
            if (node.getParentId().equals(ancestorId)) {
                return true;
            }
            node = this.getById(node.getParentId());
        }
        
        return false;
    }

    /**
     * 计算分类层级
     */
    private Integer calculateLevel(Long parentId) {
        if (parentId == null) {
            return 1;
        }
        
        DictionaryCategory parent = this.getById(parentId);
        return parent != null ? parent.getLevel() + 1 : 1;
    }

    /**
     * 更新子分类的层级
     */
    private void updateChildrenLevel(Long parentId) {
        List<DictionaryCategory> children = getChildCategories(parentId);
        for (DictionaryCategory child : children) {
            child.setLevel(calculateLevel(parentId));
            child.setUpdatedTime(LocalDateTime.now());
            this.updateById(child);
            
            // 递归更新子分类的子分类
            updateChildrenLevel(child.getId());
        }
    }
}