package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.entity.ReportConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReportConfigMapper extends BaseMapper<ReportConfig> {

    @Select("SELECT r.*, u1.username as created_by_name, u2.username as updated_by_name " +
            "FROM report_config r " +
            "LEFT JOIN sys_user u1 ON r.created_by = u1.id " +
            "LEFT JOIN sys_user u2 ON r.updated_by = u2.id " +
            "WHERE r.id = #{reportId}")
    ReportConfig selectByIdWithUserInfo(@Param("reportId") Long reportId);

    @Select("<script>" +
            "SELECT r.*, u1.username as created_by_name, u2.username as updated_by_name " +
            "FROM report_config r " +
            "LEFT JOIN sys_user u1 ON r.created_by = u1.id " +
            "LEFT JOIN sys_user u2 ON r.updated_by = u2.id " +
            "WHERE 1=1 " +
            "<if test='reportName != null and reportName != \"\"'>" +
            "AND r.report_name LIKE CONCAT('%', #{reportName}, '%') " +
            "</if>" +
            "<if test='reportCategory != null and reportCategory != \"\"'>" +
            "AND r.report_category = #{reportCategory} " +
            "</if>" +
            "<if test='reportType != null and reportType != \"\"'>" +
            "AND r.report_type = #{reportType} " +
            "</if>" +
            "<if test='isPublished != null'>" +
            "AND r.is_published = #{isPublished} " +
            "</if>" +
            "<if test='isActive != null'>" +
            "AND r.is_active = #{isActive} " +
            "</if>" +
            "<if test='createdBy != null'>" +
            "AND r.created_by = #{createdBy} " +
            "</if>" +
            "<if test='accessLevel != null and accessLevel != \"\"'>" +
            "AND r.access_level = #{accessLevel} " +
            "</if>" +
            "ORDER BY r.updated_time DESC" +
            "</script>")
    IPage<ReportConfig> selectReportList(Page<ReportConfig> page,
                                        @Param("reportName") String reportName,
                                        @Param("reportCategory") String reportCategory,
                                        @Param("reportType") String reportType,
                                        @Param("isPublished") Boolean isPublished,
                                        @Param("isActive") Boolean isActive,
                                        @Param("createdBy") Long createdBy,
                                        @Param("accessLevel") String accessLevel);

    @Select("SELECT report_category, COUNT(*) as count " +
            "FROM report_config " +
            "WHERE is_active = true " +
            "GROUP BY report_category")
    List<Map<String, Object>> selectCategoryStatistics();

    @Select("SELECT report_type, COUNT(*) as count " +
            "FROM report_config " +
            "WHERE is_active = true " +
            "GROUP BY report_type")
    List<Map<String, Object>> selectTypeStatistics();

    @Select("SELECT " +
            "DATE_FORMAT(created_time, '%Y-%m') as month, " +
            "COUNT(*) as count " +
            "FROM report_config " +
            "WHERE created_time >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
            "GROUP BY DATE_FORMAT(created_time, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> selectMonthlyCreationStatistics();

    @Select("SELECT " +
            "COUNT(*) as total_reports, " +
            "COUNT(CASE WHEN is_active = true THEN 1 END) as active_reports, " +
            "COUNT(CASE WHEN is_published = true THEN 1 END) as published_reports, " +
            "COUNT(CASE WHEN access_level = 'PUBLIC' THEN 1 END) as public_reports, " +
            "COUNT(CASE WHEN access_level = 'PRIVATE' THEN 1 END) as private_reports")
    Map<String, Object> selectOverallStatistics();

    @Update("UPDATE report_config SET is_published = #{isPublished}, published_time = NOW() WHERE report_id = #{reportId}")
    int updatePublishStatus(@Param("reportId") Long reportId, @Param("isPublished") Boolean isPublished);

    @Select("SELECT * FROM report_config WHERE report_name = #{reportName} AND report_id != #{reportId}")
    List<ReportConfig> selectByReportNameExcludeId(@Param("reportName") String reportName, @Param("reportId") Long reportId);

    @Select("SELECT * FROM report_config WHERE is_published = true AND is_active = true ORDER BY updated_time DESC LIMIT #{limit}")
    List<ReportConfig> selectPublishedReports(@Param("limit") Integer limit);

    @Select("SELECT * FROM report_config WHERE created_by = #{userId} ORDER BY updated_time DESC LIMIT #{limit}")
    List<ReportConfig> selectUserRecentReports(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Select("SELECT DISTINCT report_category FROM report_config WHERE report_category IS NOT NULL ORDER BY report_category")
    List<String> selectAllCategories();

    @Select("SELECT DISTINCT report_type FROM report_config WHERE report_type IS NOT NULL ORDER BY report_type")
    List<String> selectAllTypes();

    @Select("SELECT DISTINCT access_level FROM report_config WHERE access_level IS NOT NULL ORDER BY access_level")
    List<String> selectAllAccessLevels();

    @Select("<script>" +
            "SELECT * FROM report_config WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (report_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR report_description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY updated_time DESC " +
            "</script>")
    List<ReportConfig> searchReports(@Param("keyword") String keyword);

    @Insert("INSERT INTO report_config " +
            "(report_name, report_description, report_category, report_type, layout_config, " +
            "components_config, data_sources_config, style_config, canvas_width, canvas_height, " +
            "is_published, is_active, created_by, created_time, updated_by, updated_time, " +
            "version, tags, access_level, refresh_interval, thumbnail) " +
            "SELECT " +
            "CONCAT(report_name, '_copy'), report_description, report_category, report_type, layout_config, " +
            "components_config, data_sources_config, style_config, canvas_width, canvas_height, " +
            "false, true, #{userId}, NOW(), #{userId}, NOW(), " +
            "'v1.0', tags, access_level, refresh_interval, thumbnail " +
            "FROM report_config WHERE report_id = #{reportId}")
    @Options(useGeneratedKeys = true, keyProperty = "reportId")
    int duplicateReport(@Param("reportId") Long reportId, @Param("userId") Long userId);

    @Update("UPDATE report_config SET " +
            "thumbnail = #{thumbnail}, " +
            "updated_time = NOW() " +
            "WHERE report_id = #{reportId}")
    int updateThumbnail(@Param("reportId") Long reportId, @Param("thumbnail") String thumbnail);

    @Delete("DELETE FROM report_config WHERE report_id = #{reportId}")
    int deleteReport(@Param("reportId") Long reportId);
}