package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.SyncLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface SyncLogMapper extends BaseMapper<SyncLog> {

    @Select("SELECT " +
            "  DATE_FORMAT(start_time, '%Y-%m-%d') as date, " +
            "  COUNT(*) as total_count, " +
            "  SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
            "  SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed_count, " +
            "  SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled_count " +
            "FROM sync_log " +
            "WHERE start_time >= #{startDate} AND start_time <= #{endDate} " +
            "GROUP BY DATE_FORMAT(start_time, '%Y-%m-%d') " +
            "ORDER BY date")
    List<Map<String, Object>> getSyncStatsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    @Select("SELECT " +
            "  task_code, " +
            "  COUNT(*) as execution_count, " +
            "  SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
            "  SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed_count, " +
            "  AVG(duration) as avg_duration, " +
            "  MAX(end_time) as last_execution " +
            "FROM sync_log " +
            "WHERE start_time >= #{startDate} " +
            "GROUP BY task_code " +
            "ORDER BY execution_count DESC")
    List<Map<String, Object>> getTaskExecutionStats(@Param("startDate") LocalDateTime startDate);

    @Select("SELECT * FROM sync_log " +
            "WHERE task_id = #{taskId} " +
            "ORDER BY start_time DESC " +
            "LIMIT #{limit}")
    List<SyncLog> getRecentLogsByTaskId(@Param("taskId") Long taskId, @Param("limit") Integer limit);

    @Select("SELECT " +
            "  status, " +
            "  COUNT(*) as count " +
            "FROM sync_log " +
            "WHERE start_time >= #{startDate} " +
            "GROUP BY status")
    List<Map<String, Object>> getStatusDistribution(@Param("startDate") LocalDateTime startDate);
}