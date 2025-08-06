package com.hospital.report.controller;

import com.hospital.report.entity.DictionaryCategory;
import com.hospital.report.service.DictionaryCategoryService;
import com.hospital.report.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/dictionary/categories")
@RequiredArgsConstructor
public class DictionaryCategoryController {

    private final DictionaryCategoryService dictionaryCategoryService;

    @PostMapping
    public Result<DictionaryCategory> createCategory(@RequestBody DictionaryCategory category) {
        try {
            // 检查分类编码是否重复
            if (dictionaryCategoryService.checkCategoryCodeExists(category.getCategoryCode(), null)) {
                return Result.error("分类编码已存在");
            }
            
            // 设置层级
            Integer level = 1;
            if (category.getParentId() != null) {
                DictionaryCategory parent = dictionaryCategoryService.getById(category.getParentId());
                if (parent != null) {
                    level = parent.getLevel() + 1;
                }
            }
            category.setLevel(level);
            
            dictionaryCategoryService.save(category);
            return Result.success(category);
            
        } catch (Exception e) {
            log.error("创建分类失败: {}", e.getMessage(), e);
            return Result.error("创建分类失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<DictionaryCategory> updateCategory(@PathVariable Long id, @RequestBody DictionaryCategory category) {
        try {
            // 检查分类编码是否重复
            if (dictionaryCategoryService.checkCategoryCodeExists(category.getCategoryCode(), id)) {
                return Result.error("分类编码已存在");
            }
            
            category.setId(id);
            dictionaryCategoryService.updateById(category);
            return Result.success(category);
            
        } catch (Exception e) {
            log.error("更新分类失败: {}", e.getMessage(), e);
            return Result.error("更新分类失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        try {
            dictionaryCategoryService.deleteCategory(id);
            return Result.success();
            
        } catch (Exception e) {
            log.error("删除分类失败: {}", e.getMessage(), e);
            return Result.error("删除分类失败: " + e.getMessage());
        }
    }

    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getCategoryTree() {
        try {
            List<Map<String, Object>> tree = dictionaryCategoryService.getCategoryTree();
            return Result.success(tree);
            
        } catch (Exception e) {
            log.error("获取分类树失败: {}", e.getMessage(), e);
            return Result.error("获取分类树失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getCategoriesWithCount() {
        try {
            List<Map<String, Object>> categories = dictionaryCategoryService.getCategoriesWithCount();
            return Result.success(categories);
            
        } catch (Exception e) {
            log.error("获取分类列表失败: {}", e.getMessage(), e);
            return Result.error("获取分类列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/children")
    public Result<List<DictionaryCategory>> getChildCategories(@PathVariable Long id) {
        try {
            List<DictionaryCategory> children = dictionaryCategoryService.getChildCategories(id);
            return Result.success(children);
            
        } catch (Exception e) {
            log.error("获取子分类失败: {}", e.getMessage(), e);
            return Result.error("获取子分类失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/move")
    public Result<Void> moveCategory(
            @PathVariable Long id,
            @RequestParam Long newParentId,
            @RequestParam Integer newSortOrder) {
        
        try {
            dictionaryCategoryService.moveCategory(id, newParentId, newSortOrder);
            return Result.success();
            
        } catch (Exception e) {
            log.error("移动分类失败: {}", e.getMessage(), e);
            return Result.error("移动分类失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/path")
    public Result<String> getCategoryPath(@PathVariable Long id) {
        try {
            String path = dictionaryCategoryService.getCategoryPath(id);
            return Result.success(path);
            
        } catch (Exception e) {
            log.error("获取分类路径失败: {}", e.getMessage(), e);
            return Result.error("获取分类路径失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch/sort")
    public Result<Void> updateSortOrder(@RequestBody List<Map<String, Object>> categories) {
        try {
            dictionaryCategoryService.updateSortOrder(categories);
            return Result.success();
            
        } catch (Exception e) {
            log.error("更新排序失败: {}", e.getMessage(), e);
            return Result.error("更新排序失败: " + e.getMessage());
        }
    }

    @PostMapping("/validate/categoryCode")
    public Result<Boolean> validateCategoryCode(
            @RequestParam String categoryCode,
            @RequestParam(required = false) Long excludeId) {
        
        try {
            boolean exists = dictionaryCategoryService.checkCategoryCodeExists(categoryCode, excludeId);
            return Result.success(!exists);
            
        } catch (Exception e) {
            log.error("验证分类编码失败: {}", e.getMessage(), e);
            return Result.error("验证分类编码失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/updateFieldCount")
    public Result<Void> updateFieldCount(@PathVariable Long id) {
        try {
            dictionaryCategoryService.updateFieldCount(id);
            return Result.success();
            
        } catch (Exception e) {
            log.error("更新字段数量失败: {}", e.getMessage(), e);
            return Result.error("更新字段数量失败: " + e.getMessage());
        }
    }
}