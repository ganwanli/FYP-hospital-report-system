package com.hospital.report.controller;

import com.hospital.report.common.Result;
import com.hospital.report.entity.SqlTemplate;
import com.hospital.report.service.SqlTemplateService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/sql-templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SqlTemplateController {

    private final SqlTemplateService sqlTemplateService;

    @PostMapping
    public Result<SqlTemplate> createTemplate(@RequestBody SqlTemplate template) {
        try {
            SqlTemplate created = sqlTemplateService.createTemplate(template);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("Failed to create template: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<SqlTemplate> updateTemplate(@PathVariable Long id, @RequestBody SqlTemplate template) {
        try {
            template.setTemplateId(id);
            SqlTemplate updated = sqlTemplateService.updateTemplate(template);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("Failed to update template: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        try {
            sqlTemplateService.deleteTemplate(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to delete template: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<SqlTemplate> getTemplate(@PathVariable Long id) {
        try {
            SqlTemplate template = sqlTemplateService.getTemplateById(id);
            if (template != null) {
                return Result.success(template);
            } else {
                return Result.error("Template not found");
            }
        } catch (Exception e) {
            return Result.error("Failed to get template: " + e.getMessage());
        }
    }

    @GetMapping
    public Result<IPage<SqlTemplate>> getTemplateList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String templateCategory,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) Long createdBy,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String databaseType,
            @RequestParam(required = false) String approvalStatus) {
        try {
            Page<SqlTemplate> pageObj = new Page<>(page, size);
            IPage<SqlTemplate> result = sqlTemplateService.getTemplateList(pageObj, templateName, templateCategory,
                    isActive, isPublic, createdBy, tags, databaseType, approvalStatus);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("Failed to get template list: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public Result<List<SqlTemplate>> searchTemplates(@RequestParam String keyword) {
        try {
            List<SqlTemplate> templates = sqlTemplateService.searchTemplates(keyword);
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("Failed to search templates: " + e.getMessage());
        }
    }

    @GetMapping("/popular")
    public Result<List<SqlTemplate>> getPopularTemplates(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<SqlTemplate> templates = sqlTemplateService.getPopularTemplates(limit);
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("Failed to get popular templates: " + e.getMessage());
        }
    }

    @GetMapping("/recent")
    public Result<List<SqlTemplate>> getUserRecentTemplates(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<SqlTemplate> templates = sqlTemplateService.getUserRecentTemplates(userId, limit);
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("Failed to get recent templates: " + e.getMessage());
        }
    }

    @GetMapping("/categories")
    public Result<List<String>> getAllCategories() {
        try {
            List<String> categories = sqlTemplateService.getAllCategories();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error("Failed to get categories: " + e.getMessage());
        }
    }

    @GetMapping("/database-types")
    public Result<List<String>> getAllDatabaseTypes() {
        try {
            List<String> types = sqlTemplateService.getAllDatabaseTypes();
            return Result.success(types);
        } catch (Exception e) {
            return Result.error("Failed to get database types: " + e.getMessage());
        }
    }

    @GetMapping("/tags")
    public Result<List<String>> getAllTags() {
        try {
            List<String> tags = sqlTemplateService.getAllTags();
            return Result.success(tags);
        } catch (Exception e) {
            return Result.error("Failed to get tags: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getTemplateStatistics() {
        try {
            Map<String, Object> stats = sqlTemplateService.getTemplateStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("Failed to get statistics: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/category")
    public Result<List<Map<String, Object>>> getCategoryStatistics() {
        try {
            List<Map<String, Object>> stats = sqlTemplateService.getCategoryStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("Failed to get category statistics: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/database-type")
    public Result<List<Map<String, Object>>> getDatabaseTypeStatistics() {
        try {
            List<Map<String, Object>> stats = sqlTemplateService.getDatabaseTypeStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("Failed to get database type statistics: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/monthly")
    public Result<List<Map<String, Object>>> getMonthlyCreationStatistics() {
        try {
            List<Map<String, Object>> stats = sqlTemplateService.getMonthlyCreationStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("Failed to get monthly statistics: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approveTemplate(@PathVariable Long id, @RequestParam Long approvedBy) {
        try {
            sqlTemplateService.approveTemplate(id, approvedBy);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to approve template: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public Result<Void> rejectTemplate(@PathVariable Long id, @RequestParam Long approvedBy) {
        try {
            sqlTemplateService.rejectTemplate(id, approvedBy);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to reject template: " + e.getMessage());
        }
    }

    @GetMapping("/pending-approval")
    public Result<List<SqlTemplate>> getPendingApprovalTemplates() {
        try {
            List<SqlTemplate> templates = sqlTemplateService.getPendingApprovalTemplates();
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("Failed to get pending approval templates: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    public Result<String> validateTemplate(@RequestBody Map<String, String> request) {
        try {
            String templateContent = request.get("templateContent");
            String databaseType = request.get("databaseType");
            String validationResult = sqlTemplateService.validateTemplate(templateContent, databaseType);
            
            if (validationResult == null) {
                return Result.success("Template is valid");
            } else {
                return Result.error(validationResult);
            }
        } catch (Exception e) {
            return Result.error("Validation failed: " + e.getMessage());
        }
    }

    @PostMapping("/extract-parameters")
    public Result<List<com.hospital.report.entity.SqlTemplateParameter>> extractParameters(@RequestBody Map<String, String> request) {
        try {
            String templateContent = request.get("templateContent");
            List<com.hospital.report.entity.SqlTemplateParameter> parameters = sqlTemplateService.extractParameters(templateContent);
            return Result.success(parameters);
        } catch (Exception e) {
            return Result.error("Failed to extract parameters: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/duplicate")
    public Result<SqlTemplate> duplicateTemplate(@PathVariable Long id, @RequestParam String newName) {
        try {
            SqlTemplate duplicated = sqlTemplateService.duplicateTemplate(id, newName);
            return Result.success(duplicated);
        } catch (Exception e) {
            return Result.error("Failed to duplicate template: " + e.getMessage());
        }
    }

    @GetMapping("/category/{category}")
    public Result<List<SqlTemplate>> getTemplatesByCategory(@PathVariable String category) {
        try {
            List<SqlTemplate> templates = sqlTemplateService.getTemplatesByCategory(category);
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("Failed to get templates by category: " + e.getMessage());
        }
    }

    @GetMapping("/database-type/{databaseType}")
    public Result<List<SqlTemplate>> getTemplatesByDatabaseType(@PathVariable String databaseType) {
        try {
            List<SqlTemplate> templates = sqlTemplateService.getTemplatesByDatabaseType(databaseType);
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("Failed to get templates by database type: " + e.getMessage());
        }
    }

    @PutMapping("/bulk/status")
    public Result<Void> bulkUpdateTemplateStatus(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> templateIds = (List<Long>) request.get("templateIds");
            Boolean isActive = (Boolean) request.get("isActive");
            
            sqlTemplateService.bulkUpdateTemplateStatus(templateIds, isActive);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to bulk update template status: " + e.getMessage());
        }
    }

    @DeleteMapping("/bulk")
    public Result<Void> bulkDeleteTemplates(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> templateIds = (List<Long>) request.get("templateIds");
            
            sqlTemplateService.bulkDeleteTemplates(templateIds);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to bulk delete templates: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/usage-statistics")
    public Result<Map<String, Object>> getTemplateUsageStatistics(@PathVariable Long id) {
        try {
            Map<String, Object> stats = sqlTemplateService.getTemplateUsageStatistics(id);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("Failed to get usage statistics: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/usage-history")
    public Result<List<Map<String, Object>>> getTemplateUsageHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "30") Integer days) {
        try {
            List<Map<String, Object>> history = sqlTemplateService.getTemplateUsageHistory(id, days);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error("Failed to get usage history: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/usage")
    public Result<Void> updateUsageCount(@PathVariable Long id) {
        try {
            sqlTemplateService.updateUsageCount(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to update usage count: " + e.getMessage());
        }
    }
}