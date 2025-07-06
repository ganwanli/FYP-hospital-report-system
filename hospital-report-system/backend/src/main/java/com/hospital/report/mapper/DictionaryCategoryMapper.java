package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.DictionaryCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface DictionaryCategoryMapper extends BaseMapper<DictionaryCategory> {

    @Select("SELECT * FROM dictionary_category " +
            "WHERE is_deleted = 0 " +
            "ORDER BY sort_order, created_time")
    List<DictionaryCategory> selectAllCategories();

    @Select("SELECT * FROM dictionary_category " +
            "WHERE is_deleted = 0 " +
            "AND parent_id = #{parentId} " +
            "ORDER BY sort_order, created_time")
    List<DictionaryCategory> selectByParentId(@Param("parentId") Long parentId);

    @Select("SELECT " +
            "  c.*, " +
            "  COUNT(d.id) as field_count " +
            "FROM dictionary_category c " +
            "LEFT JOIN data_dictionary d ON c.id = d.category_id AND d.is_deleted = 0 " +
            "WHERE c.is_deleted = 0 " +
            "GROUP BY c.id " +
            "ORDER BY c.sort_order, c.created_time")
    List<Map<String, Object>> selectCategoriesWithCount();

    @Update("UPDATE dictionary_category " +
            "SET field_count = (" +
            "  SELECT COUNT(*) FROM data_dictionary " +
            "  WHERE category_id = #{categoryId} AND is_deleted = 0" +
            ") " +
            "WHERE id = #{categoryId}")
    int updateFieldCount(@Param("categoryId") Long categoryId);

    @Select("SELECT COUNT(*) FROM dictionary_category " +
            "WHERE is_deleted = 0 " +
            "AND category_code = #{categoryCode} " +
            "AND id != #{excludeId}")
    int checkCategoryCodeExists(@Param("categoryCode") String categoryCode, @Param("excludeId") Long excludeId);

    @Select("SELECT * FROM dictionary_category " +
            "WHERE is_deleted = 0 " +
            "AND level = #{level} " +
            "ORDER BY sort_order")
    List<DictionaryCategory> selectByLevel(@Param("level") Integer level);
}