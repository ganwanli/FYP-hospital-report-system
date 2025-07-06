package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.entity.DataDictionary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface DataDictionaryMapper extends BaseMapper<DataDictionary> {

    @Select("SELECT " +
            "  d.*, " +
            "  c.category_name " +
            "FROM data_dictionary d " +
            "LEFT JOIN dictionary_category c ON d.category_id = c.id " +
            "WHERE d.is_deleted = 0 " +
            "${ew.customSqlSegment}")
    IPage<Map<String, Object>> selectDictionaryPage(Page<?> page, @Param("ew") Object wrapper);

    @Select("SELECT * FROM data_dictionary " +
            "WHERE is_deleted = 0 " +
            "AND (field_name_cn LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR field_name_en LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR field_code LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR business_meaning LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY usage_count DESC, updated_time DESC " +
            "LIMIT #{limit}")
    List<DataDictionary> searchFields(@Param("keyword") String keyword, @Param("limit") Integer limit);

    @Select("SELECT " +
            "  data_type, " +
            "  COUNT(*) as count " +
            "FROM data_dictionary " +
            "WHERE is_deleted = 0 " +
            "GROUP BY data_type " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getDataTypeStatistics();

    @Select("SELECT " +
            "  c.category_name, " +
            "  COUNT(d.id) as field_count " +
            "FROM dictionary_category c " +
            "LEFT JOIN data_dictionary d ON c.id = d.category_id AND d.is_deleted = 0 " +
            "WHERE c.is_deleted = 0 " +
            "GROUP BY c.id, c.category_name " +
            "ORDER BY field_count DESC")
    List<Map<String, Object>> getCategoryStatistics();

    @Select("SELECT " +
            "  owner_department, " +
            "  COUNT(*) as field_count " +
            "FROM data_dictionary " +
            "WHERE is_deleted = 0 " +
            "AND owner_department IS NOT NULL " +
            "GROUP BY owner_department " +
            "ORDER BY field_count DESC")
    List<Map<String, Object>> getDepartmentStatistics();

    @Select("SELECT " +
            "  DATE_FORMAT(created_time, '%Y-%m') as month, " +
            "  COUNT(*) as count " +
            "FROM data_dictionary " +
            "WHERE is_deleted = 0 " +
            "AND created_time >= #{startDate} " +
            "GROUP BY DATE_FORMAT(created_time, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> getMonthlyCreationStats(@Param("startDate") LocalDateTime startDate);

    @Update("UPDATE data_dictionary " +
            "SET usage_count = IFNULL(usage_count, 0) + 1, " +
            "    last_used_time = NOW() " +
            "WHERE id = #{fieldId}")
    int incrementUsageCount(@Param("fieldId") Long fieldId);

    @Select("SELECT * FROM data_dictionary " +
            "WHERE is_deleted = 0 " +
            "AND category_id = #{categoryId} " +
            "ORDER BY field_code")
    List<DataDictionary> selectByCategory(@Param("categoryId") Long categoryId);

    @Select("SELECT " +
            "  field_code, " +
            "  field_name_cn, " +
            "  field_name_en, " +
            "  data_type, " +
            "  business_meaning, " +
            "  usage_count " +
            "FROM data_dictionary " +
            "WHERE is_deleted = 0 " +
            "ORDER BY usage_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getPopularFields(@Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM data_dictionary " +
            "WHERE is_deleted = 0 " +
            "AND field_code = #{fieldCode} " +
            "AND id != #{excludeId}")
    int checkFieldCodeExists(@Param("fieldCode") String fieldCode, @Param("excludeId") Long excludeId);

    @Select("SELECT * FROM data_dictionary " +
            "WHERE is_deleted = 0 " +
            "AND approval_status = #{status} " +
            "ORDER BY created_time DESC")
    List<DataDictionary> selectByApprovalStatus(@Param("status") String status);

    @Select("SELECT " +
            "  d.*, " +
            "  c.category_name, " +
            "  MATCH(d.field_name_cn, d.field_name_en, d.business_meaning) " +
            "  AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) as relevance " +
            "FROM data_dictionary d " +
            "LEFT JOIN dictionary_category c ON d.category_id = c.id " +
            "WHERE d.is_deleted = 0 " +
            "AND MATCH(d.field_name_cn, d.field_name_en, d.business_meaning) " +
            "AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) " +
            "ORDER BY relevance DESC, d.usage_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> fullTextSearch(@Param("keyword") String keyword, @Param("limit") Integer limit);
}