package com.hospital.report.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.common.Result;
import com.hospital.report.entity.DictCategory;
import com.hospital.report.entity.DictField;
import com.hospital.report.service.DictFieldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据字段字典控制器
 */
@Slf4j
@RestController
@RequestMapping("/dict-fields")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "数据字段字典管理", description = "数据字段字典相关接口")
public class DictFieldController {

    @Autowired
    private DictFieldService dictFieldService;

    @GetMapping("/page")
    @Operation(summary = "分页查询数据字段", description = "分页查询数据字段列表")
    public Result<IPage<DictField>> getDictFieldPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "字段名称") @RequestParam(required = false) String fieldName,
            @Parameter(description = "字段编码") @RequestParam(required = false) String fieldCode,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "字段类型") @RequestParam(required = false) String fieldType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {

        try {
            Page<DictField> page = new Page<>(current, size);
            IPage<DictField> result = dictFieldService.getDictFieldPage(page, fieldName, fieldCode, categoryId, fieldType, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询数据字段失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{fieldId}")
    @Operation(summary = "根据ID查询数据字段", description = "根据字段ID查询数据字段详情")
    public Result<DictField> getDictFieldById(@Parameter(description = "字段ID") @PathVariable Long fieldId) {
        try {
            DictField dictField = dictFieldService.getDictFieldById(fieldId);
            if (dictField == null) {
                return Result.error("字段不存在");
            }
            return Result.success(dictField);
        } catch (Exception e) {
            log.error("查询数据字段失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/code/{fieldCode}")
    @Operation(summary = "根据编码查询数据字段", description = "根据字段编码查询数据字段")
    public Result<DictField> getDictFieldByCode(@Parameter(description = "字段编码") @PathVariable String fieldCode) {
        try {
            DictField dictField = dictFieldService.getDictFieldByCode(fieldCode);
            if (dictField == null) {
                return Result.error("字段不存在");
            }
            return Result.success(dictField);
        } catch (Exception e) {
            log.error("查询数据字段失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    @Operation(summary = "创建数据字段", description = "创建新的数据字段")
    public Result<String> createDictField(@RequestBody DictField dictField) {
        try {
            boolean success = dictFieldService.createDictField(dictField);
            if (success) {
                return Result.success("创建成功");
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建数据字段失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{fieldId}")
    @Operation(summary = "更新数据字段", description = "更新指定的数据字段")
    public Result<String> updateDictField(@Parameter(description = "字段ID") @PathVariable Long fieldId,
                                          @RequestBody DictField dictField) {
        try {
            dictField.setId(fieldId);
            boolean success = dictFieldService.updateDictField(dictField);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新数据字段失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{fieldId}")
    @Operation(summary = "删除数据字段", description = "删除指定的数据字段")
    public Result<String> deleteDictField(@Parameter(description = "字段ID") @PathVariable Long fieldId,
                                        @Parameter(description = "操作人ID") @RequestParam Long updatedBy) {
        try {
            boolean success = dictFieldService.deleteDictField(fieldId, updatedBy);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除数据字段失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除数据字段", description = "批量删除指定的数据字段")
    public Result<String> batchDeleteDictFields(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> fieldIds = (List<Long>) request.get("fieldIds");
            Long updatedBy = Long.valueOf(request.get("updatedBy").toString());

            boolean success = dictFieldService.batchDeleteDictFields(fieldIds, updatedBy);
            if (success) {
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除数据字段失败", e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/categories")
    @Operation(summary = "获取所有分类", description = "获取所有数据字段分类")
    public Result<List<DictCategory>> getAllCategories() {
        try {
            List<DictCategory> categories = dictFieldService.getAllCategories();
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取分类失败", e);
            return Result.error("获取分类失败: " + e.getMessage());
        }
    }

    @GetMapping("/category/{categoryId}/fields")
    @Operation(summary = "根据分类获取字段", description = "根据分类ID获取字段列表")
    public Result<List<DictField>> getFieldsByCategoryId(@Parameter(description = "分类ID") @PathVariable Long categoryId) {
        try {
            List<DictField> fields = dictFieldService.getFieldsByCategoryId(categoryId);
            return Result.success(fields);
        } catch (Exception e) {
            log.error("获取字段失败", e);
            return Result.error("获取字段失败: " + e.getMessage());
        }
    }

    @GetMapping("/fields")
    @Operation(summary = "获取所有字段", description = "获取所有数据字段")
    public Result<List<DictField>> getAllFields() {
        try {
            List<DictField> fields = dictFieldService.getAllFields();
            return Result.success(fields);
        } catch (Exception e) {
            log.error("获取字段失败", e);
            return Result.error("获取字段失败: " + e.getMessage());
        }
    }

    @GetMapping("/dictionary")
    @Operation(summary = "获取数据字典树形结构", description = "获取包含分类和字段的树形结构数据")
    public Result<List<DictField>> getDictionary() {
        try {
            List<DictField> treeData = dictFieldService.getTreeData();
            return Result.success(treeData);
        } catch (Exception e) {
            log.error("获取数据字典失败", e);
            return Result.error("获取数据字典失败: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "搜索数据字段", description = "根据关键词搜索数据字段")
    public Result<List<DictField>> searchFields(@Parameter(description = "搜索关键词") @RequestParam String keyword) {
        try {
            List<DictField> fields = dictFieldService.searchFields(keyword);
            return Result.success(fields);
        } catch (Exception e) {
            log.error("搜索字段失败", e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    @GetMapping("/check-code")
    @Operation(summary = "检查字段编码", description = "检查字段编码是否已存在")
    public Result<Boolean> checkFieldCodeExists(@Parameter(description = "字段编码") @RequestParam String fieldCode,
                                                @Parameter(description = "排除的ID") @RequestParam(required = false) Long excludeId) {
        try {
            boolean exists = dictFieldService.checkFieldCodeExists(fieldCode, excludeId);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查字段编码失败", e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }

    @PutMapping("/batch/status")
    @Operation(summary = "批量更新状态", description = "批量更新数据字段状态")
    public Result<String> batchUpdateStatus(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> fieldIds = (List<Long>) request.get("fieldIds");
            Integer status = Integer.valueOf(request.get("status").toString());
            Long updatedBy = Long.valueOf(request.get("updatedBy").toString());
            
            boolean success = dictFieldService.batchUpdateStatus(fieldIds, status, updatedBy);
            if (success) {
                return Result.success("批量更新成功");
            } else {
                return Result.error("批量更新失败");
            }
        } catch (Exception e) {
            log.error("批量更新状态失败", e);
            return Result.error("批量更新失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取字段统计", description = "获取数据字段统计信息")
    public Result<Map<String, Object>> getFieldStatistics() {
        try {
            Map<String, Object> statistics = dictFieldService.getFieldStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @PostMapping("/{fieldId}/copy")
    @Operation(summary = "复制字段", description = "复制指定的数据字段")
    public Result<String> copyField(@Parameter(description = "字段ID") @PathVariable Long fieldId,
                                  @Parameter(description = "新字段编码") @RequestParam String newFieldCode,
                                  @Parameter(description = "新字段名称") @RequestParam String newFieldName,
                                  @Parameter(description = "创建人ID") @RequestParam Long createdBy) {
        try {
            boolean success = dictFieldService.copyField(fieldId, newFieldCode, newFieldName, createdBy);
            if (success) {
                return Result.success("复制成功");
            } else {
                return Result.error("复制失败");
            }
        } catch (Exception e) {
            log.error("复制字段失败", e);
            return Result.error("复制失败: " + e.getMessage());
        }
    }

    @PutMapping("/{fieldId}/move")
    @Operation(summary = "移动字段", description = "移动字段到指定分类")
    public Result<String> moveFieldToCategory(@Parameter(description = "字段ID") @PathVariable Long fieldId,
                                            @Parameter(description = "目标分类ID") @RequestParam Long targetCategoryId,
                                            @Parameter(description = "操作人ID") @RequestParam Long updatedBy) {
        try {
            boolean success = dictFieldService.moveFieldToCategory(fieldId, targetCategoryId, updatedBy);
            if (success) {
                return Result.success("移动成功");
            } else {
                return Result.error("移动失败");
            }
        } catch (Exception e) {
            log.error("移动字段失败", e);
            return Result.error("移动失败: " + e.getMessage());
        }
    }
}
