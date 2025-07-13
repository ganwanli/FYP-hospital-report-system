package com.hospital.report.service;

import com.hospital.report.entity.DictItem;

import java.util.List;

/**
 * 字典服务接口
 * Dictionary Service Interface
 *
 * @author Hospital Report System
 * @version 1.0
 */
public interface DictService {

    /**
     * 根据字典类型获取字典项列表
     * @param dictType 字典类型
     * @return 字典项列表
     */
    List<DictItem> getDictItemsByType(String dictType);

    /**
     * 根据字典类型和值获取字典项
     * @param dictType 字典类型
     * @param dictValue 字典值
     * @return 字典项
     */
    DictItem getDictItem(String dictType, String dictValue);

    /**
     * 根据字典类型和值获取标签
     * @param dictType 字典类型
     * @param dictValue 字典值
     * @return 字典标签
     */
    String getDictLabel(String dictType, String dictValue);
}
