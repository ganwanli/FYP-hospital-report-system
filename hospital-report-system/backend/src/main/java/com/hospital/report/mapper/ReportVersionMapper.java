package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.ReportVersion;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReportVersionMapper extends BaseMapper<ReportVersion> {

    @Select("SELECT v.*, u1.username as created_by_name " +
            "FROM report_version v " +
            "LEFT JOIN user u1 ON v.created_by = u1.user_id " +
            "WHERE v.report_id = #{reportId} " +
            "ORDER BY v.created_time DESC")
    List<Map<String, Object>> selectByReportIdWithUserInfo(@Param("reportId") Long reportId);

    @Select("SELECT * FROM report_version WHERE report_id = #{reportId} AND is_current = true")
    ReportVersion selectCurrentVersion(@Param("reportId") Long reportId);

    @Update("UPDATE report_version SET is_current = false WHERE report_id = #{reportId}")
    int clearCurrentVersion(@Param("reportId") Long reportId);

    @Update("UPDATE report_version SET is_current = true WHERE version_id = #{versionId}")
    int setCurrentVersion(@Param("versionId") Long versionId);

    @Select("SELECT * FROM report_version WHERE report_id = #{reportId} AND version_number = #{versionNumber}")
    ReportVersion selectByReportIdAndVersionNumber(@Param("reportId") Long reportId, @Param("versionNumber") String versionNumber);

    @Select("SELECT COUNT(*) FROM report_version WHERE report_id = #{reportId}")
    int countByReportId(@Param("reportId") Long reportId);

    @Select("SELECT MAX(CAST(SUBSTRING(version_number, 2) AS UNSIGNED)) FROM report_version WHERE report_id = #{reportId} AND version_number LIKE 'v%'")
    Integer selectMaxVersionNumber(@Param("reportId") Long reportId);

    @Delete("DELETE FROM report_version WHERE report_id = #{reportId}")
    int deleteByReportId(@Param("reportId") Long reportId);

    @Select("SELECT * FROM report_version WHERE report_id = #{reportId} ORDER BY created_time DESC LIMIT 1")
    ReportVersion selectLatestVersion(@Param("reportId") Long reportId);

    @Select("SELECT * FROM report_version WHERE parent_version_id = #{parentVersionId} ORDER BY created_time ASC")
    List<ReportVersion> selectChildVersions(@Param("parentVersionId") Long parentVersionId);

    @Select("SELECT v.* FROM report_version v " +
            "INNER JOIN report_config r ON v.report_id = r.report_id " +
            "WHERE r.created_by = #{userId} " +
            "ORDER BY v.created_time DESC " +
            "LIMIT #{limit}")
    List<ReportVersion> selectUserRecentVersions(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Update("UPDATE report_version SET " +
            "file_size = #{fileSize}, " +
            "thumbnail = #{thumbnail} " +
            "WHERE version_id = #{versionId}")
    int updateVersionMetadata(@Param("versionId") Long versionId, @Param("fileSize") Long fileSize, @Param("thumbnail") String thumbnail);
}