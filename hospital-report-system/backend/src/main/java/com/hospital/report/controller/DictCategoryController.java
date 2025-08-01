package com.hospital.report.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.common.Result;
import com.hospital.report.dto.*;
import com.hospital.report.service.DictCategoryService;
import com.hospital.report.service.DictFieldService;
import com.hospital.report.entity.DictField;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 数据字典分类控制器
 * 
 * @author system
 * @since 2025-01-15
 */
@Slf4j
@RestController
@RequestMapping("/dict/category")
@RequiredArgsConstructor
@Validated
@Tag(name = "数据字典分类管理", description = "数据字典分类的增删改查和树形结构管理")
public class DictCategoryController {

    private final DictCategoryService dictCategoryService;
    private final DictFieldService dictFieldService;

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树", description = "获取完整的分类树形结构")
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        log.info("获取分类树");
        List<CategoryTreeVO> tree = dictCategoryService.getCategoryTree();
        return Result.success(tree);
    }

    /**
     * 获取启用的分类树
     */
    @GetMapping("/tree/enabled")
    @Operation(summary = "获取启用的分类树", description = "获取只包含启用状态的分类树形结构")
    public Result<List<CategoryTreeVO>> getEnabledCategoryTree() {
        log.info("获取启用的分类树");
        List<CategoryTreeVO> tree = dictCategoryService.getEnabledCategoryTree();
        return Result.success(tree);
    }

    /**
     * 获取分类字段混合树
     */
    @GetMapping("/field-tree")
    @Operation(summary = "获取分类字段混合树", description = "获取分类作为分支节点，字段作为叶子节点的完整树形结构")
    public Result<List<CategoryFieldTreeVO>> getCategoryFieldTree() {
        log.info("获取分类字段混合树");
        List<CategoryFieldTreeVO> tree = dictCategoryService.getCategoryFieldTree();
        return Result.success(tree);
    }

    /**
     * 获取启用的分类字段混合树
     */
    @GetMapping("/field-tree/enabled")
    @Operation(summary = "获取启用的分类字段混合树", description = "获取只包含启用状态的分类字段混合树形结构")
    public Result<List<CategoryFieldTreeVO>> getEnabledCategoryFieldTree() {
        log.info("获取启用的分类字段混合树");
        List<CategoryFieldTreeVO> tree = dictCategoryService.getEnabledCategoryFieldTree();
        return Result.success(tree);
    }

    /**
     * 获取分类列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取分类列表", description = "分页获取分类列表，支持关键词搜索")
    public Result<Page<DictCategoryVO>> getCategoryList(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        log.info("获取分类列表，页码：{}，页大小：{}，关键词：{}", current, size, keyword);
        Page<DictCategoryVO> page = dictCategoryService.getCategoryList(current, size, keyword);
        return Result.success(page);
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情", description = "根据ID获取分类详细信息")
    public Result<DictCategoryVO> getCategoryById(
            @Parameter(description = "分类ID", required = true) @PathVariable @NotNull Long id) {
        log.info("获取分类详情，ID：{}", id);
        DictCategoryVO category = dictCategoryService.getCategoryById(id);
        return Result.success(category);
    }

    /**
     * 新增分类
     */
    @PostMapping
    @Operation(summary = "新增分类", description = "创建新的分类")
    public Result<DictCategoryVO> createCategory(@Valid @RequestBody DictCategoryDTO dto) {
        log.info("新增分类：{}", dto);
        DictCategoryVO category = dictCategoryService.createCategory(dto);
        return Result.success(category);
    }

    /**
     * 修改分类
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改分类", description = "根据ID修改分类信息")
    public Result<DictCategoryVO> updateCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable @NotNull Long id,
            @Valid @RequestBody DictCategoryDTO dto) {
        log.info("修改分类，ID：{}，数据：{}", id, dto);
        DictCategoryVO category = dictCategoryService.updateCategory(id, dto);
        return Result.success(category);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "根据ID删除分类")
    public Result<Boolean> deleteCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable @NotNull Long id) {
        log.info("删除分类，ID：{}", id);
        boolean result = dictCategoryService.deleteCategory(id);
        return Result.success(result);
    }

    /**
     * 批量删除分类
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除分类", description = "根据ID列表批量删除分类")
    public Result<Boolean> deleteCategoriesBatch(@RequestBody @NotEmpty List<Long> ids) {
        log.info("批量删除分类，IDs：{}", ids);
        boolean result = dictCategoryService.deleteCategoriesBatch(ids);
        return Result.success(result);
    }

    /**
     * 调整排序
     */
    @PostMapping("/sort")
    @Operation(summary = "调整排序", description = "批量调整分类的排序顺序")
    public Result<Boolean> updateSort(@Valid @RequestBody CategorySortDTO dto) {
        log.info("调整分类排序：{}", dto);
        boolean result = dictCategoryService.updateSort(dto);
        return Result.success(result);
    }

    /**
     * 批量修改状态
     */
    @PutMapping("/status")
    @Operation(summary = "批量修改状态", description = "批量启用或禁用分类")
    public Result<Boolean> updateStatus(@Valid @RequestBody CategoryStatusDTO dto) {
        log.info("批量修改分类状态：{}", dto);
        boolean result = dictCategoryService.updateStatus(dto);
        return Result.success(result);
    }

    /**
     * 获取子分类
     */
    @GetMapping("/children/{id}")
    @Operation(summary = "获取子分类", description = "获取指定分类的直接子分类列表")
    public Result<List<DictCategoryVO>> getChildrenByParentId(
            @Parameter(description = "父级分类ID", required = true) @PathVariable @NotNull Long id) {
        log.info("获取子分类，父级ID：{}", id);
        List<DictCategoryVO> children = dictCategoryService.getChildrenByParentId(id);
        return Result.success(children);
    }

    /**
     * 获取分类路径
     */
    @GetMapping("/path/{id}")
    @Operation(summary = "获取分类路径", description = "获取从根节点到指定分类的完整路径")
    public Result<List<DictCategoryVO>> getCategoryPath(
            @Parameter(description = "分类ID", required = true) @PathVariable @NotNull Long id) {
        log.info("获取分类路径，ID：{}", id);
        List<DictCategoryVO> path = dictCategoryService.getCategoryPath(id);
        return Result.success(path);
    }

    /**
     * 检查编码是否存在
     */
    @GetMapping("/check-code")
    @Operation(summary = "检查编码是否存在", description = "检查分类编码是否已存在")
    public Result<Boolean> checkCategoryCodeExists(
            @Parameter(description = "分类编码", required = true) @RequestParam @NotBlank String categoryCode,
            @Parameter(description = "排除的分类ID") @RequestParam(required = false) Long excludeId) {
        log.info("检查分类编码是否存在，编码：{}，排除ID：{}", categoryCode, excludeId);
        boolean exists = dictCategoryService.checkCategoryCodeExists(categoryCode, excludeId);
        return Result.success(exists);
    }

    /**
     * 根据层级获取分类
     */
    @GetMapping("/level/{level}")
    @Operation(summary = "根据层级获取分类", description = "获取指定层级的所有分类")
    public Result<List<DictCategoryVO>> getCategoriesByLevel(
            @Parameter(description = "分类层级", required = true) @PathVariable @NotNull Integer level) {
        log.info("根据层级获取分类，层级：{}", level);
        List<DictCategoryVO> categories = dictCategoryService.getCategoriesByLevel(level);
        return Result.success(categories);
    }

    /**
     * 获取顶级分类
     */
    @GetMapping("/top-level")
    @Operation(summary = "获取顶级分类", description = "获取所有顶级分类（层级为1的分类）")
    public Result<List<DictCategoryVO>> getTopLevelCategories() {
        log.info("获取顶级分类");
        List<DictCategoryVO> categories = dictCategoryService.getTopLevelCategories();
        return Result.success(categories);
    }

    /**
     * 根据编码获取分类
     */
    @GetMapping("/code/{categoryCode}")
    @Operation(summary = "根据编码获取分类", description = "根据分类编码获取分类信息")
    public Result<DictCategoryVO> getCategoryByCode(
            @Parameter(description = "分类编码", required = true) @PathVariable @NotBlank String categoryCode) {
        log.info("根据编码获取分类，编码：{}", categoryCode);
        DictCategoryVO category = dictCategoryService.getCategoryByCode(categoryCode);
        return Result.success(category);
    }

    /**
     * 搜索分类
     */
    @GetMapping("/search")
    @Operation(summary = "搜索分类", description = "根据关键词搜索分类")
    public Result<List<DictCategoryVO>> searchCategories(
            @Parameter(description = "搜索关键词", required = true) @RequestParam @NotBlank String keyword) {
        log.info("搜索分类，关键词：{}", keyword);
        List<DictCategoryVO> categories = dictCategoryService.searchCategories(keyword);
        return Result.success(categories);
    }

    /**
     * 刷新缓存
     */
    @PostMapping("/refresh-cache")
    @Operation(summary = "刷新缓存", description = "刷新分类树缓存")
    public Result<Boolean> refreshCache() {
        log.info("刷新分类缓存");
        dictCategoryService.refreshCategoryCache();
        return Result.success(true);
    }

    /**
     * 新增字段
     */
    @PostMapping("/field")
    @Operation(summary = "新增字段", description = "在指定分类下创建新的字段")
    public Result<DictField> createField(@RequestBody Map<String, Object> requestData) {
        try {
            log.info("新增字段请求数据：{}", requestData);

            // 创建DictField对象并设置字段值
            DictField dictField = new DictField();

            // 基本字段映射
            if (requestData.get("categoryId") != null) {
                dictField.setCategoryId(Long.valueOf(requestData.get("categoryId").toString()));
            }
            if (requestData.get("code") != null) {
                dictField.setFieldCode(requestData.get("code").toString());
            }
            if (requestData.get("name") != null) {
                dictField.setFieldName(requestData.get("name").toString());
            }
            if (requestData.get("nameEn") != null) {
                dictField.setFieldNameEn(requestData.get("nameEn").toString());
            }
            if (requestData.get("description") != null) {
                dictField.setDescription(requestData.get("description").toString());
            }
            if (requestData.get("dataType") != null) {
                dictField.setDataType(requestData.get("dataType").toString());
            }
            if (requestData.get("dataLength") != null) {
                dictField.setDataLength(requestData.get("dataLength").toString());
            }
            if (requestData.get("sourceDatabase") != null) {
                dictField.setSourceDatabase(requestData.get("sourceDatabase").toString());
            }
            if (requestData.get("sourceTable") != null) {
                dictField.setSourceTable(requestData.get("sourceTable").toString());
            }
            if (requestData.get("sourceField") != null) {
                dictField.setSourceField(requestData.get("sourceField").toString());
            }
            if (requestData.get("filterCondition") != null) {
                dictField.setFilterCondition(requestData.get("filterCondition").toString());
            }
            if (requestData.get("calculationSql") != null) {
                dictField.setCalculationSql(requestData.get("calculationSql").toString());
            }
            if (requestData.get("updateFrequency") != null) {
                dictField.setUpdateFrequency(requestData.get("updateFrequency").toString());
            }
            if (requestData.get("dataOwner") != null) {
                dictField.setDataOwner(requestData.get("dataOwner").toString());
            }
            if (requestData.get("remark") != null) {
                dictField.setRemark(requestData.get("remark").toString());
            }
            if (requestData.get("nodeType") != null) {
                dictField.setNodeType(requestData.get("nodeType").toString());
            }
            if (requestData.get("status") != null) {
                dictField.setStatus(Integer.valueOf(requestData.get("status").toString()));
            }

            // 验证必要字段
            if (dictField.getCategoryId() == null) {
                return Result.error("分类ID不能为空");
            }
            if (dictField.getFieldCode() == null || dictField.getFieldCode().trim().isEmpty()) {
                return Result.error("字段编码不能为空");
            }
            if (dictField.getFieldName() == null || dictField.getFieldName().trim().isEmpty()) {
                return Result.error("字段名称不能为空");
            }

            // 设置默认值
            if (dictField.getNodeType() == null) {
                dictField.setNodeType("field");
            }
            if (dictField.getStatus() == null) {
                dictField.setStatus(1);
            }

            log.info("映射后的字段对象：{}", dictField);

            // 调用服务创建字段
            boolean success = dictFieldService.createDictField(dictField);

            if (success) {
                // 刷新缓存
                dictCategoryService.refreshCategoryCache();
                return Result.success(dictField);
            } else {
                return Result.error("创建字段失败");
            }

        } catch (Exception e) {
            log.error("创建字段失败", e);
            return Result.error("系统异常：" + e.getMessage());
        }
    }
}
