package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.entity.SqlTemplateUsageLog;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface SqlTemplateUsageLogMapper extends BaseMapper<SqlTemplateUsageLog> {

    @Select("SELECT l.*, u.username as user_name, t.template_name " +
            "FROM sql_template_usage_log l " +
            "LEFT JOIN sys_user u ON l.user_id = u.id " +
            "LEFT JOIN sql_template t ON l.template_id = t.template_id " +
            "WHERE l.template_id = #{templateId} " +
            "ORDER BY l.execution_time DESC")
    IPage<SqlTemplateUsageLog> selectByTemplateId(Page<SqlTemplateUsageLog> page, @Param("templateId") Long templateId);

    @Select("SELECT l.*, u.username as user_name, t.template_name " +
            "FROM sql_template_usage_log l " +
            "LEFT JOIN sys_user u ON l.user_id = u.id " +
            "LEFT JOIN sql_template t ON l.template_id = t.template_id " +
            "WHERE l.user_id = #{userId} " +
            "ORDER BY l.execution_time DESC")
    IPage<SqlTemplateUsageLog> selectByUserId(Page<SqlTemplateUsageLog> page, @Param("userId") Long userId);

    @Select("SELECT " +
            "COUNT(*) as total_executions, " +
            "COUNT(CASE WHEN execution_status = 'SUCCESS' THEN 1 END) as successful_executions, " +
            "COUNT(CASE WHEN execution_status = 'FAILED' THEN 1 END) as failed_executions, " +
            "AVG(execution_duration) as avg_execution_duration, " +
            "MAX(execution_duration) as max_execution_duration, " +
            "MIN(execution_duration) as min_execution_duration " +
            "FROM sql_template_usage_log " +
            "WHERE template_id = #{templateId}")
    Map<String, Object> selectTemplateUsageStatistics(@Param("templateId") Long templateId);

    @Select("SELECT " +
            "DATE_FORMAT(execution_time, '%Y-%m-%d') as date, " +
            "COUNT(*) as execution_count, " +
            "COUNT(CASE WHEN execution_status = 'SUCCESS' THEN 1 END) as success_count, " +
            "COUNT(CASE WHEN execution_status = 'FAILED' THEN 1 END) as fail_count, " +
            "AVG(execution_duration) as avg_duration " +
            "FROM sql_template_usage_log " +
            "WHERE template_id = #{templateId} " +
            "AND execution_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "GROUP BY DATE_FORMAT(execution_time, '%Y-%m-%d') " +
            "ORDER BY date")
    List<Map<String, Object>> selectDailyUsageStatistics(@Param("templateId") Long templateId);

    @Select("SELECT " +
            "u.username, " +
            "COUNT(*) as execution_count, " +
            "COUNT(CASE WHEN l.execution_status = 'SUCCESS' THEN 1 END) as success_count, " +
            "AVG(l.execution_duration) as avg_duration " +
            "FROM sql_template_usage_log l " +
            "LEFT JOIN sys_user u ON l.user_id = u.id " +
            "WHERE l.template_id = #{templateId} " +
            "GROUP BY l.user_id, u.username " +
            "ORDER BY execution_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopUsers(@Param("templateId") Long templateId, @Param("limit") Integer limit);

    @Select("SELECT " +
            "error_message, " +
            "COUNT(*) as error_count, " +
            "MAX(execution_time) as last_occurrence " +
            "FROM sql_template_usage_log " +
            "WHERE template_id = #{templateId} " +
            "AND execution_status = 'FAILED' " +
            "AND error_message IS NOT NULL " +
            "GROUP BY error_message " +
            "ORDER BY error_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopErrors(@Param("templateId") Long templateId, @Param("limit") Integer limit);

    @Select("SELECT " +
            "DATE_FORMAT(execution_time, '%H') as hour, " +
            "COUNT(*) as execution_count " +
            "FROM sql_template_usage_log " +
            "WHERE template_id = #{templateId} " +
            "AND execution_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "GROUP BY DATE_FORMAT(execution_time, '%H') " +
            "ORDER BY hour")
    List<Map<String, Object>> selectHourlyUsagePattern(@Param("templateId") Long templateId);

    @Select("SELECT " +
            "COUNT(*) as total_executions, " +
            "COUNT(CASE WHEN execution_status = 'SUCCESS' THEN 1 END) as successful_executions, " +
            "COUNT(CASE WHEN execution_status = 'FAILED' THEN 1 END) as failed_executions, " +
            "AVG(execution_duration) as avg_execution_duration " +
            "FROM sql_template_usage_log " +
            "WHERE user_id = #{userId}")
    Map<String, Object> selectUserUsageStatistics(@Param("userId") Long userId);

    @Delete("DELETE FROM sql_template_usage_log WHERE execution_time < #{beforeTime}")
    int deleteOldLogs(@Param("beforeTime") LocalDateTime beforeTime);

    @Delete("DELETE FROM sql_template_usage_log WHERE template_id = #{templateId}")
    int deleteByTemplateId(@Param("templateId") Long templateId);

    @Select("SELECT t.template_name, COUNT(*) as usage_count " +
            "FROM sql_template_usage_log l " +
            "INNER JOIN sql_template t ON l.template_id = t.template_id " +
            "WHERE l.execution_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "GROUP BY l.template_id, t.template_name " +
            "ORDER BY usage_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectPopularTemplates(@Param("limit") Integer limit);

    @Select("SELECT " +
            "DATE_FORMAT(execution_time, '%Y-%m-%d %H') as hour, " +
            "COUNT(*) as execution_count " +
            "FROM sql_template_usage_log " +
            "WHERE execution_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR) " +
            "GROUP BY DATE_FORMAT(execution_time, '%Y-%m-%d %H') " +
            "ORDER BY hour")
    List<Map<String, Object>> selectRecentHourlyUsage();

    @Select("SELECT " +
            "execution_status, " +
            "COUNT(*) as count " +
            "FROM sql_template_usage_log " +
            "WHERE execution_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "GROUP BY execution_status")
    List<Map<String, Object>> selectWeeklyStatusDistribution();
}