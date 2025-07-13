package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.DictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 字典项Mapper接口
 * Dictionary Item Mapper Interface
 * 
 * @author Hospital Report System
 * @version 1.0
 */
@Mapper
public interface DictItemMapper extends BaseMapper<DictItem> {

    /**
     * 根据字典类型获取字典项列表
     * @param dictType 字典类型
     * @return 字典项列表
     */
    @Select("SELECT * FROM dict_item WHERE dict_type = #{dictType} AND status = 1 AND is_deleted = 0 ORDER BY sort_order ASC")
    List<DictItem> selectByDictType(@Param("dictType") String dictType);

    /**
     * 根据字典类型和值获取字典项
     * @param dictType 字典类型
     * @param dictValue 字典值
     * @return 字典项
     */
    @Select("SELECT * FROM dict_item WHERE dict_type = #{dictType} AND dict_value = #{dictValue} AND status = 1 AND is_deleted = 0")
    DictItem selectByTypeAndValue(@Param("dictType") String dictType, @Param("dictValue") String dictValue);
}
