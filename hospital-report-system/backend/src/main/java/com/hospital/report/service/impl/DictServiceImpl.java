package com.hospital.report.service.impl;

import com.hospital.report.entity.DictItem;
import com.hospital.report.entity.SysDict;
import com.hospital.report.mapper.SysDictMapper;
import com.hospital.report.service.DictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典服务实现类
 * Dictionary Service Implementation
 *
 * @author Hospital Report System
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final SysDictMapper sysDictMapper;

    @Override
    public List<DictItem> getDictItemsByType(String dictType) {
        try {
            log.info("Getting dict items for type: {} from database", dictType);
            List<SysDict> sysDictItems = sysDictMapper.selectByDictType(dictType);

            // 如果数据库中没有数据，返回模拟数据作为备选
            if (sysDictItems == null || sysDictItems.isEmpty()) {
                log.warn("No dict items found in database for type: {}, using mock data", dictType);
                return getMockDictItems(dictType);
            }

            // 将SysDict转换为DictItem
            List<DictItem> items = convertSysDictToDictItem(sysDictItems);

            log.info("Found {} dict items for type: {}", items.size(), dictType);
            return items;
        } catch (Exception e) {
            log.error("Error getting dict items for type: {}, using mock data. Error: {}", dictType, e.getMessage());
            return getMockDictItems(dictType);
        }
    }

    @Override
    public DictItem getDictItem(String dictType, String dictValue) {
        try {
            log.info("Getting dict item for type: {}, value: {} from database", dictType, dictValue);
            SysDict sysDict = sysDictMapper.selectByTypeAndValue(dictType, dictValue);

            if (sysDict == null) {
                log.warn("No dict item found for type: {}, value: {}", dictType, dictValue);
                return null;
            }

            // 将SysDict转换为DictItem
            return convertSysDictToDictItem(sysDict);
        } catch (Exception e) {
            log.error("Error getting dict item for type: {}, value: {}. Error: {}", dictType, dictValue, e.getMessage());
            return null;
        }
    }

    @Override
    public String getDictLabel(String dictType, String dictValue) {
        DictItem item = getDictItem(dictType, dictValue);
        return item != null ? item.getDictLabel() : dictValue;
    }

    /**
     * 获取模拟字典数据
     */
    private List<DictItem> getMockDictItems(String dictType) {
        List<DictItem> mockItems = new ArrayList<>();
        
        if ("SQL_ASSET_BUSINESS".equals(dictType)) {
            mockItems.add(createMockItem(dictType, "outpatient", "门诊", "OUTPATIENT", 1));
            mockItems.add(createMockItem(dictType, "inpatient", "住院", "INPATIENT", 2));
            mockItems.add(createMockItem(dictType, "emergency", "急诊", "EMERGENCY", 3));
            mockItems.add(createMockItem(dictType, "pharmacy", "药房", "PHARMACY", 4));
            mockItems.add(createMockItem(dictType, "laboratory", "检验", "LABORATORY", 5));
        } else if ("SQL_ASSET_USAGE".equals(dictType)) {
            mockItems.add(createMockItem(dictType, "report", "上报类", "REPORT", 1));
            mockItems.add(createMockItem(dictType, "analysis", "分析类", "ANALYSIS", 2));
            mockItems.add(createMockItem(dictType, "statistics", "统计类", "STATISTICS", 3));
            mockItems.add(createMockItem(dictType, "query", "查询类", "QUERY", 4));
        }
        
        return mockItems;
    }

    /**
     * 创建模拟字典项
     */
    private DictItem createMockItem(String dictType, String dictValue, String dictLabel, String dictCode, int sortOrder) {
        DictItem item = new DictItem();
        item.setDictType(dictType);
        item.setDictValue(dictValue);
        item.setDictLabel(dictLabel);
        item.setDictCode(dictCode);
        item.setSortOrder(sortOrder);
        item.setStatus(1);
        return item;
    }

    /**
     * 将SysDict转换为DictItem
     */
    private DictItem convertSysDictToDictItem(SysDict sysDict) {
        if (sysDict == null) {
            return null;
        }

        DictItem item = new DictItem();
        item.setId(sysDict.getId());
        item.setDictType(sysDict.getDictType());
        item.setDictValue(sysDict.getDictValue());
        item.setDictLabel(sysDict.getDictLabel());
        item.setDictCode(sysDict.getDictCode());
        item.setSortOrder(sysDict.getSortOrder());
        item.setStatus(sysDict.getStatus());
        item.setRemark(sysDict.getDescription()); // 将description映射到remark
        item.setCreatedBy(sysDict.getCreatedBy());
        item.setCreatedTime(sysDict.getCreatedTime());
        item.setUpdatedBy(sysDict.getUpdatedBy());
        item.setUpdatedTime(sysDict.getUpdatedTime());
        item.setIsDeleted(sysDict.getDeleted() != null && sysDict.getDeleted() == 1);

        return item;
    }

    /**
     * 将SysDict列表转换为DictItem列表
     */
    private List<DictItem> convertSysDictToDictItem(List<SysDict> sysDictList) {
        if (sysDictList == null || sysDictList.isEmpty()) {
            return new ArrayList<>();
        }

        List<DictItem> items = new ArrayList<>();
        for (SysDict sysDict : sysDictList) {
            DictItem item = convertSysDictToDictItem(sysDict);
            if (item != null) {
                items.add(item);
            }
        }

        return items;
    }
}
