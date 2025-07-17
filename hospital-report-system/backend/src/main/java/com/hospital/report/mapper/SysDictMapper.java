package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统字典Mapper接口
 * System Dictionary Mapper Interface
 * 
 * @author Hospital Report System
 * @version 1.0
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {

    /**
     * 根据字典类型获取字典项列表
     * @param dictType 字典类型
     * @return 字典项列表
     */
    @Select("SELECT * FROM sys_dict WHERE dict_type = #{dictType} AND status = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<SysDict> selectByDictType(@Param("dictType") String dictType);

    /**
     * 根据字典类型和值获取字典项
     * @param dictType 字典类型
     * @param dictValue 字典值
     * @return 字典项
     */
    @Select("SELECT * FROM sys_dict WHERE dict_type = #{dictType} AND dict_value = #{dictValue} AND status = 1 AND deleted = 0")
    SysDict selectByTypeAndValue(@Param("dictType") String dictType, @Param("dictValue") String dictValue);

    /**
     * 根据字典类型和编码获取字典项
     * @param dictType 字典类型
     * @param dictCode 字典编码
     * @return 字典项
     */
    @Select("SELECT * FROM sys_dict WHERE dict_type = #{dictType} AND dict_code = #{dictCode} AND status = 1 AND deleted = 0")
    SysDict selectByTypeAndCode(@Param("dictType") String dictType, @Param("dictCode") String dictCode);

    /**
     * 获取所有字典类型
     * @return 字典类型列表
     */
    @Select("SELECT DISTINCT dict_type FROM sys_dict WHERE status = 1 AND deleted = 0 ORDER BY dict_type")
    List<String> selectAllDictTypes();
}
