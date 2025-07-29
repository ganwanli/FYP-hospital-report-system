package com.hospital.report.mapper;

import com.hospital.report.entity.ReportAuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 报表审核记录Mapper接口
 */
@Mapper
public interface ReportAuditLogMapper {

    /**
     * 插入审核记录
     */
    @Insert("INSERT INTO report_audit_log (report_id, auditor_id, auditor_name, audit_decision, " +
            "audit_comment, old_status, new_status, audit_time, created_time, updated_time) " +
            "VALUES (#{reportId}, #{auditorId}, #{auditorName}, #{auditDecision}, " +
            "#{auditComment}, #{oldStatus}, #{newStatus}, #{auditTime}, #{createdTime}, #{updatedTime})")
    int insert(ReportAuditLog auditLog);

    /**
     * 根据报表ID查询审核历史
     */
    @Select("SELECT * FROM report_audit_log WHERE report_id = #{reportId} ORDER BY audit_time DESC")
    List<ReportAuditLog> findByReportId(@Param("reportId") Long reportId);

    /**
     * 分页查询审核历史
     */
    @Select("SELECT * FROM report_audit_log WHERE report_id = #{reportId} " +
            "ORDER BY audit_time DESC LIMIT #{offset}, #{size}")
    List<ReportAuditLog> findByReportIdWithPaging(@Param("reportId") Long reportId, 
                                                  @Param("offset") int offset, 
                                                  @Param("size") int size);

    /**
     * 统计审核历史总数
     */
    @Select("SELECT COUNT(*) FROM report_audit_log WHERE report_id = #{reportId}")
    long countByReportId(@Param("reportId") Long reportId);

    /**
     * 获取审核统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_audits, " +
            "COUNT(CASE WHEN audit_decision = 'APPROVED' THEN 1 END) as approved_count, " +
            "COUNT(CASE WHEN audit_decision = 'REJECTED' THEN 1 END) as rejected_count, " +
            "AVG(CASE WHEN audit_time IS NOT NULL THEN " +
            "TIMESTAMPDIFF(HOUR, created_time, audit_time) END) as avg_audit_hours " +
            "FROM report_audit_log " +
            "WHERE audit_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)")
    Map<String, Object> getAuditStatistics();

    /**
     * 获取审核员的审核统计
     */
    @Select("SELECT " +
            "auditor_name, " +
            "COUNT(*) as total_audits, " +
            "COUNT(CASE WHEN audit_decision = 'APPROVED' THEN 1 END) as approved_count, " +
            "COUNT(CASE WHEN audit_decision = 'REJECTED' THEN 1 END) as rejected_count " +
            "FROM report_audit_log " +
            "WHERE auditor_id = #{auditorId} " +
            "GROUP BY auditor_id, auditor_name")
    Map<String, Object> getAuditorStatistics(@Param("auditorId") Long auditorId);

    /**
     * 获取最近的审核记录
     */
    @Select("SELECT ral.*, rc.report_name " +
            "FROM report_audit_log ral " +
            "LEFT JOIN report_config rc ON ral.report_id = rc.id " +
            "ORDER BY ral.audit_time DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getRecentAuditLogs(@Param("limit") int limit);

    /**
     * 根据审核员ID查询审核记录
     */
    @Select("SELECT * FROM report_audit_log WHERE auditor_id = #{auditorId} ORDER BY audit_time DESC")
    List<ReportAuditLog> findByAuditorId(@Param("auditorId") Long auditorId);

    /**
     * 根据审核决定查询记录
     */
    @Select("SELECT * FROM report_audit_log WHERE audit_decision = #{decision} ORDER BY audit_time DESC")
    List<ReportAuditLog> findByAuditDecision(@Param("decision") String decision);

    /**
     * 查询指定时间范围内的审核记录
     */
    @Select("SELECT * FROM report_audit_log " +
            "WHERE audit_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY audit_time DESC")
    List<ReportAuditLog> findByAuditTimeBetween(@Param("startTime") String startTime, 
                                               @Param("endTime") String endTime);
}
