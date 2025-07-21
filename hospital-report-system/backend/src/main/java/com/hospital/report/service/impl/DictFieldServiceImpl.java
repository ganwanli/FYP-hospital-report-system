package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.report.entity.DictCategory;
import com.hospital.report.entity.DictField;
import com.hospital.report.mapper.DictCategoryMapper;
import com.hospital.report.mapper.DictFieldMapper;
import com.hospital.report.service.DictFieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据字段字典服务实现类
 */
@Slf4j
@Service
public class DictFieldServiceImpl extends ServiceImpl<DictFieldMapper, DictField> implements DictFieldService {

    @Autowired
    private DictCategoryMapper dictCategoryMapper;

    @Override
    public IPage<DictField> getDictFieldPage(Page<DictField> page, String fieldName, String fieldCode,
                                             Long categoryId, String fieldType, Integer status) {
        return baseMapper.selectDictFieldPage(page, fieldName, fieldCode, categoryId, fieldType, status);
    }

    @Override
    public DictField getDictFieldById(Long fieldId) {
        if (fieldId == null) {
            return null;
        }
        return baseMapper.selectById(fieldId);
    }

    @Override
    public DictField getDictFieldByCode(String fieldCode) {
        if (!StringUtils.hasText(fieldCode)) {
            return null;
        }
        return baseMapper.selectByFieldCode(fieldCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createDictField(DictField dictField) {
        try {
            // 检查字段编码是否已存在
            if (checkFieldCodeExists(dictField.getFieldCode(), null)) {
                throw new RuntimeException("字段编码已存在: " + dictField.getFieldCode());
            }

            // 设置默认值
            if (dictField.getStatus() == null) {
                dictField.setStatus(1);
            }
            if (dictField.getIsPublic() == null) {
                dictField.setIsPublic(true);
            }
            if (dictField.getSortOrder() == null) {
                // 获取分类下的最大排序号
                Integer maxSortOrder = baseMapper.getMaxSortOrderByCategory(dictField.getCategoryId());
                dictField.setSortOrder(maxSortOrder + 1);
            }

            // 设置创建和更新信息
            // 这里可以从SecurityContext获取当前用户ID，暂时设置为"1"
            String currentUserId = "1"; // TODO: 从SecurityContext获取当前用户ID
            dictField.setCreatedBy(currentUserId);
            dictField.setUpdatedBy(currentUserId);
            dictField.setCreatedTime(LocalDateTime.now());
            dictField.setUpdatedTime(LocalDateTime.now());
            dictField.setIsDeleted(0); // int类型，0表示未删除
            dictField.setVersion(1);

            // 调用Mapper的插入方法
            int result = baseMapper.insertDictField(dictField);

            if (result > 0) {
                log.info("数据字段创建成功，ID：{}", dictField.getId());
                return true;
            } else {
                log.error("数据字段创建失败，插入返回结果：{}", result);
                return false;
            }
        } catch (Exception e) {
            log.error("创建数据字段失败", e);
            throw new RuntimeException("创建数据字段失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictField(DictField dictField) {
        try {
            log.info("开始更新数据字段：{}", dictField);

            if (dictField.getId() == null) {
                throw new RuntimeException("字段ID不能为空");
            }

            // 检查字段编码是否已存在（排除当前记录）
            if (checkFieldCodeExists(dictField.getFieldCode(), dictField.getId())) {
                throw new RuntimeException("字段编码已存在: " + dictField.getFieldCode());
            }

            // 设置更新信息
            // 这里可以从SecurityContext获取当前用户ID，暂时设置为"1"
            String currentUserId = "1"; // TODO: 从SecurityContext获取当前用户ID
            dictField.setUpdatedBy(currentUserId);
            dictField.setUpdatedTime(LocalDateTime.now());

            // 调用Mapper的更新方法
            boolean result = baseMapper.updateDictField(dictField);

            if (result) {
                log.info("数据字段更新成功，ID：{}", dictField.getId());
                return true;
            } else {
                log.error("数据字段更新失败，更新返回结果：{}", result);
                return false;
            }

        } catch (Exception e) {
            log.error("更新数据字段失败", e);
            throw new RuntimeException("更新数据字段失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictField(Long fieldId, Long updatedBy) {
        try {
            if (fieldId == null) {
                throw new RuntimeException("字段ID不能为空");
            }

            DictField dictField = new DictField();
            dictField.setId(fieldId);
            dictField.setIsDeleted(1);
            dictField.setUpdatedBy(String.valueOf(updatedBy));
            dictField.setUpdatedTime(LocalDateTime.now());

            return baseMapper.updateDictField(dictField);
        } catch (Exception e) {
            log.error("删除数据字段失败", e);
            throw new RuntimeException("删除数据字段失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteDictFields(List<Long> fieldIds, Long updatedBy) {
        try {
            if (fieldIds == null || fieldIds.isEmpty()) {
                throw new RuntimeException("字段ID列表不能为空");
            }

            return baseMapper.batchDelete(fieldIds, updatedBy) > 0;
        } catch (Exception e) {
            log.error("批量删除数据字段失败", e);
            throw new RuntimeException("批量删除数据字段失败: " + e.getMessage());
        }
    }

    @Override
    public List<DictCategory> getAllCategories() {
        return dictCategoryMapper.selectAllWithHierarchy();
    }

    @Override
    public List<DictField> getFieldsByCategoryId(Long categoryId) {
        if (categoryId == null) {
            return new ArrayList<>();
        }
        return baseMapper.selectFieldsByCategoryId(categoryId);
    }

    @Override
    public List<DictField> getAllFields() {
        return baseMapper.selectAllFields();
    }

    @Override
    public List<DictField> getTreeData() {
        // 注意：这个方法现在已经被新的CategoryFieldTreeVO替代
        // 建议使用DictCategoryService.getCategoryFieldTree()方法
        List<DictField> allData = baseMapper.selectTreeData();
        return buildTree(allData);
    }

    @Override
    public List<DictField> searchFields(String keyword) {
        return baseMapper.searchFields(keyword);
    }

    @Override
    public boolean checkFieldCodeExists(String fieldCode, Long excludeId) {
        if (!StringUtils.hasText(fieldCode)) {
            return false;
        }
        return baseMapper.checkFieldCodeExists(fieldCode, excludeId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateStatus(List<Long> fieldIds, Integer status, Long updatedBy) {
        try {
            if (fieldIds == null || fieldIds.isEmpty()) {
                throw new RuntimeException("字段ID列表不能为空");
            }

            return baseMapper.batchUpdateStatus(fieldIds, status, updatedBy) > 0;
        } catch (Exception e) {
            log.error("批量更新状态失败", e);
            throw new RuntimeException("批量更新状态失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getFieldStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总字段数
        LambdaQueryWrapper<DictField> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(DictField::getIsDeleted, false);
        long totalCount = count(totalWrapper);
        statistics.put("totalCount", totalCount);

        // 分类数
        LambdaQueryWrapper<DictField> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.eq(DictField::getIsDeleted, false)
                      .eq(DictField::getDataType, "category");
        long categoryCount = count(categoryWrapper);
        statistics.put("categoryCount", categoryCount);

        // 字段数
        LambdaQueryWrapper<DictField> fieldWrapper = new LambdaQueryWrapper<>();
        fieldWrapper.eq(DictField::getIsDeleted, false)
                   .eq(DictField::getDataType, "field");
        long fieldCount = count(fieldWrapper);
        statistics.put("fieldCount", fieldCount);

        // 启用状态统计
        LambdaQueryWrapper<DictField> enabledWrapper = new LambdaQueryWrapper<>();
        enabledWrapper.eq(DictField::getIsDeleted, false)
                     .eq(DictField::getStatus, 1);
        long enabledCount = count(enabledWrapper);
        statistics.put("enabledCount", enabledCount);

        // 禁用状态统计
        LambdaQueryWrapper<DictField> disabledWrapper = new LambdaQueryWrapper<>();
        disabledWrapper.eq(DictField::getIsDeleted, false)
                      .eq(DictField::getStatus, 0);
        long disabledCount = count(disabledWrapper);
        statistics.put("disabledCount", disabledCount);

        return statistics;
    }

    @Override
    public List<DictField> exportFields(Long categoryId, String fieldType) {
        LambdaQueryWrapper<DictField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictField::getIsDeleted, false);
        
        if (categoryId != null) {
            wrapper.eq(DictField::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(fieldType)) {
            wrapper.eq(DictField::getDataType, fieldType);
        }
        
        wrapper.orderByAsc(DictField::getSortOrder, DictField::getCreatedTime);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importFields(List<DictField> fields, Long createdBy) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errorMessages = new ArrayList<>();

        for (DictField field : fields) {
            try {
                field.setCreatedBy(String.valueOf(createdBy));
                field.setUpdatedBy(String.valueOf(createdBy));
                createDictField(field);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errorMessages.add("字段 " + field.getFieldCode() + " 导入失败: " + e.getMessage());
            }
        }

        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errorMessages", errorMessages);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copyField(Long fieldId, String newFieldCode, String newFieldName, Long createdBy) {
        try {
            DictField sourceField = getDictFieldById(fieldId);
            if (sourceField == null) {
                throw new RuntimeException("源字段不存在");
            }

            DictField newField = new DictField();
            BeanUtils.copyProperties(sourceField, newField);
            newField.setId(null);
            newField.setFieldCode(newFieldCode);
            newField.setFieldName(newFieldName);
            newField.setCreatedBy(String.valueOf(createdBy));
            newField.setUpdatedBy(String.valueOf(createdBy));

            return createDictField(newField);
        } catch (Exception e) {
            log.error("复制字段失败", e);
            throw new RuntimeException("复制字段失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean moveFieldToCategory(Long fieldId, Long targetCategoryId, Long updatedBy) {
        try {
            DictField field = getDictFieldById(fieldId);
            if (field == null) {
                throw new RuntimeException("字段不存在");
            }

            field.setCategoryId(targetCategoryId);
            field.setUpdatedBy(String.valueOf(updatedBy));
            return updateDictField(field);
        } catch (Exception e) {
            log.error("移动字段失败", e);
            throw new RuntimeException("移动字段失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getFieldUsageStatistics(Long fieldId) {
        Map<String, Object> statistics = new HashMap<>();
        // 这里可以根据实际需求统计字段的使用情况
        // 比如在SQL模板中的使用次数等
        statistics.put("fieldId", fieldId);
        statistics.put("usageCount", 0); // 实际实现时需要查询相关表
        return statistics;
    }

    /**
     * 构建树形结构
     */
    private List<DictField> buildTree(List<DictField> allData) {
        Map<Long, List<DictField>> categoryMap = allData.stream()
                .filter(item -> "category".equals(item.getDataType()))
                .collect(Collectors.groupingBy(item -> item.getCategoryId() == null ? 0L : item.getCategoryId()));

        Map<Long, List<DictField>> fieldMap = allData.stream()
                .filter(item -> "field".equals(item.getDataType()))
                .collect(Collectors.groupingBy(DictField::getCategoryId));

        // 构建根节点
        List<DictField> rootCategories = categoryMap.getOrDefault(0L, new ArrayList<>());
        
        for (DictField category : rootCategories) {
            buildTreeRecursive(category, categoryMap, fieldMap);
        }

        return rootCategories;
    }

    /**
     * 递归构建树形结构
     */
    private void buildTreeRecursive(DictField parent, Map<Long, List<DictField>> categoryMap, Map<Long, List<DictField>> fieldMap) {
        List<DictField> children = new ArrayList<>();
        
        // 添加子分类
        List<DictField> subCategories = categoryMap.getOrDefault(parent.getId(), new ArrayList<>());
        for (DictField subCategory : subCategories) {
            buildTreeRecursive(subCategory, categoryMap, fieldMap);
            children.add(subCategory);
        }
        
        // 添加字段
        List<DictField> fields = fieldMap.getOrDefault(parent.getId(), new ArrayList<>());
        children.addAll(fields);
        
        parent.setChildren(children);
    }
}
