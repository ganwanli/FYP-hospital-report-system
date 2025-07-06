package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.report.entity.DataDictionary;
import com.hospital.report.entity.FieldUsageLog;
import com.hospital.report.mapper.DataDictionaryMapper;
import com.hospital.report.mapper.FieldUsageLogMapper;
import com.hospital.report.service.DataDictionaryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataDictionaryServiceImpl extends ServiceImpl<DataDictionaryMapper, DataDictionary> 
        implements DataDictionaryService {

    private final DataDictionaryMapper dataDictionaryMapper;
    private final FieldUsageLogMapper fieldUsageLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<Map<String, Object>> getDataDictionaryPage(Integer current, Integer size,
                                                           String keyword, Long categoryId,
                                                           String dataType, String approvalStatus) {
        Page<DataDictionary> page = new Page<>(current, size);
        QueryWrapper<DataDictionary> wrapper = new QueryWrapper<>();
        
        wrapper.eq("is_deleted", false);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("field_name_cn", keyword)
                    .or().like("field_name_en", keyword)
                    .or().like("field_code", keyword)
                    .or().like("business_meaning", keyword));
        }
        
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        
        if (dataType != null && !dataType.trim().isEmpty()) {
            wrapper.eq("data_type", dataType);
        }
        
        if (approvalStatus != null && !approvalStatus.trim().isEmpty()) {
            wrapper.eq("approval_status", approvalStatus);
        }
        
        wrapper.orderByDesc("updated_time");
        
        return dataDictionaryMapper.selectDictionaryPage(page, wrapper);
    }

    @Override
    public List<DataDictionary> searchFields(String keyword, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        return dataDictionaryMapper.searchFields(keyword, limit);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 基础统计
        long totalFields = this.count(new QueryWrapper<DataDictionary>().eq("is_deleted", false));
        long standardFields = this.count(new QueryWrapper<DataDictionary>()
                .eq("is_deleted", false)
                .eq("is_standard", true));
        long pendingApproval = this.count(new QueryWrapper<DataDictionary>()
                .eq("is_deleted", false)
                .eq("approval_status", "PENDING"));
        
        stats.put("totalFields", totalFields);
        stats.put("standardFields", standardFields);
        stats.put("pendingApproval", pendingApproval);
        stats.put("standardRate", totalFields > 0 ? (double) standardFields / totalFields * 100 : 0);
        
        // 数据类型分布
        stats.put("dataTypeStats", dataDictionaryMapper.getDataTypeStatistics());
        
        // 分类分布
        stats.put("categoryStats", dataDictionaryMapper.getCategoryStatistics());
        
        // 部门分布
        stats.put("departmentStats", dataDictionaryMapper.getDepartmentStatistics());
        
        // 月度创建统计
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        stats.put("monthlyStats", dataDictionaryMapper.getMonthlyCreationStats(sixMonthsAgo));
        
        return stats;
    }

    @Override
    @Transactional
    public void incrementUsageCount(Long fieldId, String usageType, String usageContext, Long userId) {
        // 更新字段使用次数
        dataDictionaryMapper.incrementUsageCount(fieldId);
        
        // 记录使用日志
        DataDictionary field = this.getById(fieldId);
        if (field != null) {
            FieldUsageLog usageLog = new FieldUsageLog();
            usageLog.setFieldId(fieldId);
            usageLog.setFieldCode(field.getFieldCode());
            usageLog.setUsageType(usageType);
            usageLog.setUsageContext(usageContext);
            usageLog.setUserId(userId);
            usageLog.setCreatedTime(LocalDateTime.now());
            
            fieldUsageLogMapper.insert(usageLog);
        }
    }

    @Override
    public boolean checkFieldCodeExists(String fieldCode, Long excludeId) {
        return dataDictionaryMapper.checkFieldCodeExists(fieldCode, excludeId != null ? excludeId : -1L) > 0;
    }

    @Override
    public List<Map<String, Object>> getPopularFields(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return dataDictionaryMapper.getPopularFields(limit);
    }

    @Override
    public List<Map<String, Object>> fullTextSearch(String keyword, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 50;
        }
        return dataDictionaryMapper.fullTextSearch(keyword, limit);
    }

    @Override
    @Transactional
    public Map<String, Object> importFields(MultipartFile file, Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 跳过标题行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    DataDictionary field = parseRowToField(row);
                    field.setCreatedBy(userId);
                    field.setUpdatedBy(userId);
                    field.setCreatedTime(LocalDateTime.now());
                    field.setUpdatedTime(LocalDateTime.now());
                    field.setStatus(1);
                    field.setIsDeleted(false);
                    
                    // 检查字段编码是否重复
                    if (checkFieldCodeExists(field.getFieldCode(), null)) {
                        errors.add("第" + (i + 1) + "行：字段编码已存在 - " + field.getFieldCode());
                        errorCount++;
                        continue;
                    }
                    
                    this.save(field);
                    successCount++;
                    
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行：" + e.getMessage());
                    errorCount++;
                }
            }
            
        } catch (IOException e) {
            log.error("导入文件解析失败", e);
            errors.add("文件解析失败：" + e.getMessage());
        }

        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("errors", errors);
        
        return result;
    }

    @Override
    public byte[] exportFields(List<Long> fieldIds) {
        List<DataDictionary> fields;
        
        if (fieldIds != null && !fieldIds.isEmpty()) {
            fields = this.listByIds(fieldIds);
        } else {
            fields = this.list(new QueryWrapper<DataDictionary>().eq("is_deleted", false));
        }

        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("数据字典");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "字段编码", "中文名称", "英文名称", "数据类型", "长度", "精度", "小数位",
                "是否可空", "默认值", "业务含义", "数据来源", "更新频率", "负责人", "部门",
                "表名", "列名", "值域范围", "示例值", "标签", "备注"
            };
            
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            
            // 填充数据
            for (int i = 0; i < fields.size(); i++) {
                Row row = sheet.createRow(i + 1);
                DataDictionary field = fields.get(i);
                
                row.createCell(0).setCellValue(field.getFieldCode());
                row.createCell(1).setCellValue(field.getFieldNameCn());
                row.createCell(2).setCellValue(field.getFieldNameEn());
                row.createCell(3).setCellValue(field.getDataType());
                row.createCell(4).setCellValue(field.getDataLength() != null ? field.getDataLength() : 0);
                row.createCell(5).setCellValue(field.getDataPrecision() != null ? field.getDataPrecision() : 0);
                row.createCell(6).setCellValue(field.getDataScale() != null ? field.getDataScale() : 0);
                row.createCell(7).setCellValue(field.getIsNullable() != null ? field.getIsNullable() : false);
                row.createCell(8).setCellValue(field.getDefaultValue());
                row.createCell(9).setCellValue(field.getBusinessMeaning());
                row.createCell(10).setCellValue(field.getDataSource());
                row.createCell(11).setCellValue(field.getUpdateFrequency());
                row.createCell(12).setCellValue(field.getOwnerUser());
                row.createCell(13).setCellValue(field.getOwnerDepartment());
                row.createCell(14).setCellValue(field.getTableName());
                row.createCell(15).setCellValue(field.getColumnName());
                row.createCell(16).setCellValue(field.getValueRange());
                row.createCell(17).setCellValue(field.getSampleValues());
                row.createCell(18).setCellValue(field.getTags());
                row.createCell(19).setCellValue(field.getRemark());
            }
            
            // 自适应列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
            
        } catch (IOException e) {
            log.error("导出Excel失败", e);
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void batchApprove(List<Long> fieldIds, String approvalStatus, String approvalUser) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return;
        }
        
        for (Long fieldId : fieldIds) {
            DataDictionary field = new DataDictionary();
            field.setId(fieldId);
            field.setApprovalStatus(approvalStatus);
            field.setApprovalUser(approvalUser);
            field.setApprovalTime(LocalDateTime.now());
            field.setUpdatedTime(LocalDateTime.now());
            
            this.updateById(field);
        }
    }

    @Override
    public Map<String, Object> getFieldDetail(Long fieldId) {
        Map<String, Object> detail = new HashMap<>();
        
        DataDictionary field = this.getById(fieldId);
        if (field == null) {
            return detail;
        }
        
        detail.put("field", field);
        
        // 获取使用统计
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<FieldUsageLog> usageHistory = fieldUsageLogMapper.getFieldUsageHistory(fieldId, 20);
        detail.put("usageHistory", usageHistory);
        
        // 获取相关字段
        List<DataDictionary> relatedFields = getRelatedFields(fieldId);
        detail.put("relatedFields", relatedFields);
        
        return detail;
    }

    @Override
    public List<DataDictionary> getRelatedFields(Long fieldId) {
        DataDictionary field = this.getById(fieldId);
        if (field == null || field.getRelatedFields() == null) {
            return new ArrayList<>();
        }
        
        try {
            String[] relatedFieldCodes = field.getRelatedFields().split(",");
            QueryWrapper<DataDictionary> wrapper = new QueryWrapper<>();
            wrapper.eq("is_deleted", false)
                    .in("field_code", Arrays.asList(relatedFieldCodes));
            
            return this.list(wrapper);
        } catch (Exception e) {
            log.warn("解析相关字段失败：{}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public DataDictionary copyField(Long fieldId, String newFieldCode) {
        DataDictionary originalField = this.getById(fieldId);
        if (originalField == null) {
            throw new RuntimeException("源字段不存在");
        }
        
        if (checkFieldCodeExists(newFieldCode, null)) {
            throw new RuntimeException("新字段编码已存在");
        }
        
        DataDictionary newField = new DataDictionary();
        // 复制所有属性
        newField.setFieldCode(newFieldCode);
        newField.setFieldNameCn(originalField.getFieldNameCn() + "_副本");
        newField.setFieldNameEn(originalField.getFieldNameEn() + "_copy");
        newField.setDataType(originalField.getDataType());
        newField.setDataLength(originalField.getDataLength());
        newField.setDataPrecision(originalField.getDataPrecision());
        newField.setDataScale(originalField.getDataScale());
        newField.setIsNullable(originalField.getIsNullable());
        newField.setDefaultValue(originalField.getDefaultValue());
        newField.setBusinessMeaning(originalField.getBusinessMeaning());
        newField.setDataSource(originalField.getDataSource());
        newField.setUpdateFrequency(originalField.getUpdateFrequency());
        newField.setOwnerUser(originalField.getOwnerUser());
        newField.setOwnerDepartment(originalField.getOwnerDepartment());
        newField.setCategoryId(originalField.getCategoryId());
        newField.setDataQualityRules(originalField.getDataQualityRules());
        newField.setValueRange(originalField.getValueRange());
        newField.setSampleValues(originalField.getSampleValues());
        newField.setTags(originalField.getTags());
        
        // 设置新字段属性
        newField.setUsageCount(0L);
        newField.setApprovalStatus("PENDING");
        newField.setStatus(1);
        newField.setIsStandard(false);
        newField.setVersion("1.0");
        newField.setCreatedTime(LocalDateTime.now());
        newField.setUpdatedTime(LocalDateTime.now());
        newField.setIsDeleted(false);
        
        this.save(newField);
        return newField;
    }

    @Override
    public List<Map<String, Object>> getFieldHistory(Long fieldId) {
        // 这里可以实现字段变更历史记录
        // 需要额外的历史表来记录字段的变更
        List<Map<String, Object>> history = new ArrayList<>();
        
        DataDictionary field = this.getById(fieldId);
        if (field != null && field.getChangeLog() != null) {
            try {
                // 解析变更日志JSON
                List<Map<String, Object>> changes = objectMapper.readValue(
                    field.getChangeLog(), 
                    List.class
                );
                history.addAll(changes);
            } catch (Exception e) {
                log.warn("解析变更日志失败：{}", e.getMessage());
            }
        }
        
        return history;
    }

    @Override
    @Transactional
    public void standardizeField(Long fieldId, String standardReference) {
        DataDictionary field = new DataDictionary();
        field.setId(fieldId);
        field.setIsStandard(true);
        field.setStandardReference(standardReference);
        field.setUpdatedTime(LocalDateTime.now());
        
        this.updateById(field);
    }

    @Override
    public Map<String, Object> getDataLineage(String fieldCode) {
        Map<String, Object> lineage = new HashMap<>();
        
        // 查找当前字段
        QueryWrapper<DataDictionary> wrapper = new QueryWrapper<>();
        wrapper.eq("field_code", fieldCode).eq("is_deleted", false);
        DataDictionary field = this.getOne(wrapper);
        
        if (field == null) {
            return lineage;
        }
        
        lineage.put("current", field);
        
        // 查找上游字段（数据来源）
        List<DataDictionary> upstream = new ArrayList<>();
        if (field.getDataSource() != null) {
            QueryWrapper<DataDictionary> upstreamWrapper = new QueryWrapper<>();
            upstreamWrapper.eq("table_name", field.getDataSource())
                    .eq("is_deleted", false);
            upstream = this.list(upstreamWrapper);
        }
        lineage.put("upstream", upstream);
        
        // 查找下游字段（引用当前字段的）
        QueryWrapper<DataDictionary> downstreamWrapper = new QueryWrapper<>();
        downstreamWrapper.like("related_fields", fieldCode)
                .eq("is_deleted", false);
        List<DataDictionary> downstream = this.list(downstreamWrapper);
        lineage.put("downstream", downstream);
        
        return lineage;
    }

    private DataDictionary parseRowToField(Row row) {
        DataDictionary field = new DataDictionary();
        
        field.setFieldCode(getCellStringValue(row.getCell(0)));
        field.setFieldNameCn(getCellStringValue(row.getCell(1)));
        field.setFieldNameEn(getCellStringValue(row.getCell(2)));
        field.setDataType(getCellStringValue(row.getCell(3)));
        field.setDataLength(getCellIntValue(row.getCell(4)));
        field.setDataPrecision(getCellIntValue(row.getCell(5)));
        field.setDataScale(getCellIntValue(row.getCell(6)));
        field.setIsNullable(getCellBooleanValue(row.getCell(7)));
        field.setDefaultValue(getCellStringValue(row.getCell(8)));
        field.setBusinessMeaning(getCellStringValue(row.getCell(9)));
        field.setDataSource(getCellStringValue(row.getCell(10)));
        field.setUpdateFrequency(getCellStringValue(row.getCell(11)));
        field.setOwnerUser(getCellStringValue(row.getCell(12)));
        field.setOwnerDepartment(getCellStringValue(row.getCell(13)));
        field.setTableName(getCellStringValue(row.getCell(14)));
        field.setColumnName(getCellStringValue(row.getCell(15)));
        field.setValueRange(getCellStringValue(row.getCell(16)));
        field.setSampleValues(getCellStringValue(row.getCell(17)));
        field.setTags(getCellStringValue(row.getCell(18)));
        field.setRemark(getCellStringValue(row.getCell(19)));
        
        // 设置默认值
        field.setUsageCount(0L);
        field.setApprovalStatus("PENDING");
        field.setIsStandard(false);
        field.setVersion("1.0");
        
        return field;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return cell.getStringCellValue();
    }

    private Integer getCellIntValue(Cell cell) {
        if (cell == null) return null;
        return (int) cell.getNumericCellValue();
    }

    private Boolean getCellBooleanValue(Cell cell) {
        if (cell == null) return false;
        return cell.getBooleanCellValue();
    }
}