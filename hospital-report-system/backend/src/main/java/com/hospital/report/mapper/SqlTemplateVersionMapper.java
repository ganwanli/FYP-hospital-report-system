package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.SqlTemplateVersion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SqlTemplateVersionMapper extends BaseMapper<SqlTemplateVersion> {

    @Select("SELECT v.*, u1.username as created_by_name, u2.username as approved_by_name " +
            "FROM sql_template_version v " +
            "LEFT JOIN user u1 ON v.created_by = u1.user_id " +
            "LEFT JOIN user u2 ON v.approved_by = u2.user_id " +
            "WHERE v.template_id = #{templateId} " +
            "ORDER BY v.created_time DESC")
    List<SqlTemplateVersion> selectByTemplateIdWithUserInfo(@Param("templateId") Long templateId);

    @Select("SELECT * FROM sql_template_version WHERE template_id = #{templateId} AND is_current = true")
    SqlTemplateVersion selectCurrentVersion(@Param("templateId") Long templateId);

    @Update("UPDATE sql_template_version SET is_current = false WHERE template_id = #{templateId}")
    int clearCurrentVersion(@Param("templateId") Long templateId);

    @Update("UPDATE sql_template_version SET is_current = true WHERE version_id = #{versionId}")
    int setCurrentVersion(@Param("versionId") Long versionId);

    @Select("SELECT * FROM sql_template_version WHERE template_id = #{templateId} AND version_number = #{versionNumber}")
    SqlTemplateVersion selectByTemplateIdAndVersionNumber(@Param("templateId") Long templateId, @Param("versionNumber") String versionNumber);

    @Select("SELECT COUNT(*) FROM sql_template_version WHERE template_id = #{templateId}")
    int countByTemplateId(@Param("templateId") Long templateId);

    @Select("SELECT MAX(CAST(SUBSTRING(version_number, 3) AS UNSIGNED)) FROM sql_template_version WHERE template_id = #{templateId} AND version_number LIKE 'v%'")
    Integer selectMaxVersionNumber(@Param("templateId") Long templateId);

    @Delete("DELETE FROM sql_template_version WHERE template_id = #{templateId}")
    int deleteByTemplateId(@Param("templateId") Long templateId);

    @Select("SELECT * FROM sql_template_version WHERE template_hash = #{templateHash} AND template_id = #{templateId}")
    SqlTemplateVersion selectByTemplateHashAndTemplateId(@Param("templateHash") String templateHash, @Param("templateId") Long templateId);

    @Select("SELECT * FROM sql_template_version WHERE template_id = #{templateId} ORDER BY created_time DESC LIMIT 1")
    SqlTemplateVersion selectLatestVersion(@Param("templateId") Long templateId);

    @Select("SELECT * FROM sql_template_version WHERE parent_version_id = #{parentVersionId} ORDER BY created_time ASC")
    List<SqlTemplateVersion> selectChildVersions(@Param("parentVersionId") Long parentVersionId);

    @Update("UPDATE sql_template_version SET approval_status = #{approvalStatus}, approved_by = #{approvedBy}, approved_time = NOW() WHERE version_id = #{versionId}")
    int updateApprovalStatus(@Param("versionId") Long versionId, @Param("approvalStatus") String approvalStatus, @Param("approvedBy") Long approvedBy);

    @Select("SELECT * FROM sql_template_version WHERE approval_status = 'PENDING' ORDER BY created_time ASC")
    List<SqlTemplateVersion> selectPendingApprovalVersions();

    @Select("SELECT v.* FROM sql_template_version v " +
            "INNER JOIN sql_template t ON v.template_id = t.template_id " +
            "WHERE t.created_by = #{userId} " +
            "ORDER BY v.created_time DESC " +
            "LIMIT #{limit}")
    List<SqlTemplateVersion> selectUserRecentVersions(@Param("userId") Long userId, @Param("limit") Integer limit);
}