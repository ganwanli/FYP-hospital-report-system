package com.hospital.report.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hospital.report.entity.DataDictionary;
import com.hospital.report.service.DataDictionaryService;
import com.hospital.report.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
public class DataDictionaryController {

    private final DataDictionaryService dataDictionaryService;

    @PostMapping("/fields")
    public Result<DataDictionary> createField(@RequestBody DataDictionary field) {
        try {
            // 检查字段编码是否重复
            if (dataDictionaryService.checkFieldCodeExists(field.getFieldCode(), null)) {
                return Result.error("字段编码已存在");
            }
            
            dataDictionaryService.save(field);
            return Result.success(field);
            
        } catch (Exception e) {
            log.error("创建字段失败: {}", e.getMessage(), e);
            return Result.error("创建字段失败: " + e.getMessage());
        }
    }

    @PutMapping("/fields/{id}")
    public Result<DataDictionary> updateField(@PathVariable Long id, @RequestBody DataDictionary field) {
        try {
            // 检查字段编码是否重复
            if (dataDictionaryService.checkFieldCodeExists(field.getFieldCode(), id)) {
                return Result.error("字段编码已存在");
            }
            
            field.setId(id);
            dataDictionaryService.updateById(field);
            return Result.success(field);
            
        } catch (Exception e) {
            log.error("更新字段失败: {}", e.getMessage(), e);
            return Result.error("更新字段失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/fields/{id}")
    public Result<Void> deleteField(@PathVariable Long id) {
        try {
            dataDictionaryService.removeById(id);
            return Result.success();
            
        } catch (Exception e) {
            log.error("删除字段失败: {}", e.getMessage(), e);
            return Result.error("删除字段失败: " + e.getMessage());
        }
    }

    @GetMapping("/fields")
    public Result<IPage<Map<String, Object>>> getFields(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) String approvalStatus) {
        
        try {
            IPage<Map<String, Object>> result = dataDictionaryService.getDataDictionaryPage(
                current, size, keyword, categoryId, dataType, approvalStatus
            );
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("获取字段列表失败: {}", e.getMessage(), e);
            return Result.error("获取字段列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/fields/{id}")
    public Result<Map<String, Object>> getFieldDetail(@PathVariable Long id) {
        try {
            Map<String, Object> detail = dataDictionaryService.getFieldDetail(id);
            return Result.success(detail);
            
        } catch (Exception e) {
            log.error("获取字段详情失败: {}", e.getMessage(), e);
            return Result.error("获取字段详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public Result<List<DataDictionary>> searchFields(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        try {
            List<DataDictionary> fields = dataDictionaryService.searchFields(keyword, limit);
            return Result.success(fields);
            
        } catch (Exception e) {
            log.error("搜索字段失败: {}", e.getMessage(), e);
            return Result.error("搜索字段失败: " + e.getMessage());
        }
    }

    @GetMapping("/search/fulltext")
    public Result<List<Map<String, Object>>> fullTextSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "50") Integer limit) {
        
        try {
            List<Map<String, Object>> results = dataDictionaryService.fullTextSearch(keyword, limit);
            return Result.success(results);
            
        } catch (Exception e) {
            log.error("全文搜索失败: {}", e.getMessage(), e);
            return Result.error("全文搜索失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = dataDictionaryService.getStatistics();
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取统计信息失败: {}", e.getMessage(), e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/popular")
    public Result<List<Map<String, Object>>> getPopularFields(
            @RequestParam(defaultValue = "10") Integer limit) {
        
        try {
            List<Map<String, Object>> fields = dataDictionaryService.getPopularFields(limit);
            return Result.success(fields);
            
        } catch (Exception e) {
            log.error("获取热门字段失败: {}", e.getMessage(), e);
            return Result.error("获取热门字段失败: " + e.getMessage());
        }
    }

    @PostMapping("/usage/{fieldId}")
    public Result<Void> recordUsage(
            @PathVariable Long fieldId,
            @RequestParam String usageType,
            @RequestParam(required = false) String usageContext,
            @RequestParam(required = false) Long userId) {
        
        try {
            dataDictionaryService.incrementUsageCount(fieldId, usageType, usageContext, userId);
            return Result.success();
            
        } catch (Exception e) {
            log.error("记录使用统计失败: {}", e.getMessage(), e);
            return Result.error("记录使用统计失败: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public Result<Map<String, Object>> importFields(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long userId) {
        
        try {
            if (file.isEmpty()) {
                return Result.error("上传文件不能为空");
            }
            
            Map<String, Object> result = dataDictionaryService.importFields(file, userId);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("导入字段失败: {}", e.getMessage(), e);
            return Result.error("导入字段失败: " + e.getMessage());
        }
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportFields(@RequestBody(required = false) List<Long> fieldIds) {
        try {
            byte[] excelData = dataDictionaryService.exportFields(fieldIds);
            
            String filename = "数据字典_" + System.currentTimeMillis() + ".xlsx";
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelData);
                    
        } catch (Exception e) {
            log.error("导出字段失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/batch/approve")
    public Result<Void> batchApprove(
            @RequestBody Map<String, Object> request) {
        
        try {
            @SuppressWarnings("unchecked")
            List<Long> fieldIds = (List<Long>) request.get("fieldIds");
            String approvalStatus = (String) request.get("approvalStatus");
            String approvalUser = (String) request.get("approvalUser");
            
            dataDictionaryService.batchApprove(fieldIds, approvalStatus, approvalUser);
            return Result.success();
            
        } catch (Exception e) {
            log.error("批量审批失败: {}", e.getMessage(), e);
            return Result.error("批量审批失败: " + e.getMessage());
        }
    }

    @PostMapping("/copy/{fieldId}")
    public Result<DataDictionary> copyField(
            @PathVariable Long fieldId,
            @RequestParam String newFieldCode) {
        
        try {
            DataDictionary newField = dataDictionaryService.copyField(fieldId, newFieldCode);
            return Result.success(newField);
            
        } catch (Exception e) {
            log.error("复制字段失败: {}", e.getMessage(), e);
            return Result.error("复制字段失败: " + e.getMessage());
        }
    }

    @GetMapping("/fields/{fieldId}/related")
    public Result<List<DataDictionary>> getRelatedFields(@PathVariable Long fieldId) {
        try {
            List<DataDictionary> relatedFields = dataDictionaryService.getRelatedFields(fieldId);
            return Result.success(relatedFields);
            
        } catch (Exception e) {
            log.error("获取相关字段失败: {}", e.getMessage(), e);
            return Result.error("获取相关字段失败: " + e.getMessage());
        }
    }

    @GetMapping("/fields/{fieldId}/history")
    public Result<List<Map<String, Object>>> getFieldHistory(@PathVariable Long fieldId) {
        try {
            List<Map<String, Object>> history = dataDictionaryService.getFieldHistory(fieldId);
            return Result.success(history);
            
        } catch (Exception e) {
            log.error("获取字段历史失败: {}", e.getMessage(), e);
            return Result.error("获取字段历史失败: " + e.getMessage());
        }
    }

    @PostMapping("/standardize/{fieldId}")
    public Result<Void> standardizeField(
            @PathVariable Long fieldId,
            @RequestParam String standardReference) {
        
        try {
            dataDictionaryService.standardizeField(fieldId, standardReference);
            return Result.success();
            
        } catch (Exception e) {
            log.error("标准化字段失败: {}", e.getMessage(), e);
            return Result.error("标准化字段失败: " + e.getMessage());
        }
    }

    @GetMapping("/lineage/{fieldCode}")
    public Result<Map<String, Object>> getDataLineage(@PathVariable String fieldCode) {
        try {
            Map<String, Object> lineage = dataDictionaryService.getDataLineage(fieldCode);
            return Result.success(lineage);
            
        } catch (Exception e) {
            log.error("获取数据血缘失败: {}", e.getMessage(), e);
            return Result.error("获取数据血缘失败: " + e.getMessage());
        }
    }

    @PostMapping("/validate/fieldCode")
    public Result<Boolean> validateFieldCode(
            @RequestParam String fieldCode,
            @RequestParam(required = false) Long excludeId) {
        
        try {
            boolean exists = dataDictionaryService.checkFieldCodeExists(fieldCode, excludeId);
            return Result.success(!exists);
            
        } catch (Exception e) {
            log.error("验证字段编码失败: {}", e.getMessage(), e);
            return Result.error("验证字段编码失败: " + e.getMessage());
        }
    }
}