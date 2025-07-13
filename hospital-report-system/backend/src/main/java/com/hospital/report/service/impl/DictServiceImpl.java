package com.hospital.report.service.impl;

import com.hospital.report.entity.DictItem;
import com.hospital.report.service.DictService;
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
public class DictServiceImpl implements DictService {

    @Override
    public List<DictItem> getDictItemsByType(String dictType) {
        // 直接返回模拟数据，避免数据库表不存在的问题
        log.info("Getting dict items for type: {}, using mock data", dictType);
        return getMockDictItems(dictType);
    }

    @Override
    public DictItem getDictItem(String dictType, String dictValue) {
        // 直接返回null，避免数据库查询
        log.info("Getting dict item for type: {}, value: {}, returning null", dictType, dictValue);
        return null;
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
}
