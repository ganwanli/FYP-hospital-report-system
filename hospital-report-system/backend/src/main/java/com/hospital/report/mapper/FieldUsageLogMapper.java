package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.FieldUsageLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface FieldUsageLogMapper extends BaseMapper<FieldUsageLog> {

    @Select("SELECT " +
            "  field_code, " +
            "  COUNT(*) as usage_count, " +
            "  MAX(created_time) as last_used " +
            "FROM field_usage_log " +
            "WHERE created_time >= #{startDate} " +
            "GROUP BY field_code " +
            "ORDER BY usage_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopUsedFields(@Param("startDate") LocalDateTime startDate, @Param("limit") Integer limit);

    @Select("SELECT " +
            "  usage_type, " +
            "  COUNT(*) as count " +
            "FROM field_usage_log " +
            "WHERE created_time >= #{startDate} " +
            "GROUP BY usage_type " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getUsageTypeStatistics(@Param("startDate") LocalDateTime startDate);

    @Select("SELECT " +
            "  DATE_FORMAT(created_time, '%Y-%m-%d %H:00:00') as hour, " +
            "  COUNT(*) as count " +
            "FROM field_usage_log " +
            "WHERE created_time >= #{startDate} " +
            "GROUP BY DATE_FORMAT(created_time, '%Y-%m-%d %H:00:00') " +
            "ORDER BY hour")
    List<Map<String, Object>> getHourlyUsageStats(@Param("startDate") LocalDateTime startDate);

    @Select("SELECT " +
            "  user_name, " +
            "  COUNT(*) as usage_count " +
            "FROM field_usage_log " +
            "WHERE created_time >= #{startDate} " +
            "AND user_name IS NOT NULL " +
            "GROUP BY user_name " +
            "ORDER BY usage_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopUsers(@Param("startDate") LocalDateTime startDate, @Param("limit") Integer limit);

    @Select("SELECT * FROM field_usage_log " +
            "WHERE field_id = #{fieldId} " +
            "ORDER BY created_time DESC " +
            "LIMIT #{limit}")
    List<FieldUsageLog> getFieldUsageHistory(@Param("fieldId") Long fieldId, @Param("limit") Integer limit);
}