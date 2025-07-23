package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.entity.SqlTemplate;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SqlTemplateMapper extends BaseMapper<SqlTemplate> {

    @Insert("INSERT INTO sql_template (" +
            "template_name, template_description, template_content, template_category, template_version, " +
            "business_type, usage_type, department_code, tags, datasource_id, database_type, " +
            "template_hash, is_active, is_public, approval_status, " +
            "created_by, created_time, updated_by, updated_time, " +
            "usage_count, execution_timeout, max_rows" +
            ") VALUES (" +
            "#{templateName}, #{templateDescription}, #{templateContent}, #{templateCategory}, #{templateVersion}, " +
            "#{businessType}, #{usageType}, #{departmentCode}, #{tags}, #{datasourceId}, #{databaseType}, " +
            "#{templateHash}, #{isActive}, #{isPublic}, #{approvalStatus}, " +
            "#{createdBy}, #{createdTime}, #{updatedBy}, #{updatedTime}, " +
            "#{usageCount}, #{executionTimeout}, #{maxRows}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "templateId", keyColumn = "template_id")
    int insertTemplate(SqlTemplate template);

    @Select("SELECT t.*, u1.username as created_by_name, u2.username as updated_by_name, u3.username as approved_by_name " +
            "FROM sql_template t " +
            "LEFT JOIN sys_user u1 ON t.created_by = u1.id " +
            "LEFT JOIN sys_user u2 ON t.updated_by = u2.id " +
            "LEFT JOIN sys_user u3 ON t.approved_by = u3.id " +
            "WHERE t.template_id = #{templateId}")
    SqlTemplate selectByIdWithUserInfo(@Param("templateId") Long templateId);

    @Select("<script>" +
            "SELECT t.* " +
            "FROM sql_template t  " +
            "WHERE 1=1 " +
            "<if test='templateName != null and templateName.trim() != \"\"'>" +
            "  AND t.template_name LIKE CONCAT('%', #{templateName}, '%') " +
            "</if>" +
            "<if test='templateCategory != null and templateCategory.trim() != \"\"'>" +
            "  AND t.template_category = #{templateCategory} " +
            "</if>" +
            "<if test='isActive != null'>" +
            "  AND t.is_active = #{isActive} " +
            "</if>" +
            "<if test='isPublic != null'>" +
            "  AND t.is_public = #{isPublic} " +
            "</if>" +
            "<if test='createdBy != null'>" +
            "  AND t.created_by = #{createdBy} " +
            "</if>" +
            "<if test='tags != null and tags.trim() != \"\"'>" +
            "  AND t.tags LIKE CONCAT('%', #{tags}, '%') " +
            "</if>" +
            "<if test='databaseType != null and databaseType.trim() != \"\"'>" +
            "  AND t.database_type = #{databaseType} " +
            "</if>" +
            "<if test='approvalStatus != null and approvalStatus.trim() != \"\"'>" +
            "  AND t.approval_status = #{approvalStatus} " +
            "</if>" +
            "ORDER BY t.updated_time DESC" +
            "</script>")
    IPage<SqlTemplate> selectTemplateList(Page<SqlTemplate> page,
                                          @Param("templateName") String templateName,
                                          @Param("templateCategory") String templateCategory,
                                          @Param("isActive") Boolean isActive,
                                          @Param("isPublic") Boolean isPublic,
                                          @Param("createdBy") Long createdBy,
                                          @Param("tags") String tags,
                                          @Param("databaseType") String databaseType,
                                          @Param("approvalStatus") String approvalStatus);

    @Select("SELECT template_category, COUNT(*) as count FROM sql_template WHERE is_active = true GROUP BY template_category")
    List<Map<String, Object>> selectCategoryStatistics();

    @Select("SELECT database_type, COUNT(*) as count FROM sql_template WHERE is_active = true GROUP BY database_type")
    List<Map<String, Object>> selectDatabaseTypeStatistics();

    @Select("SELECT " +
            "DATE_FORMAT(created_time, '%Y-%m') as month, " +
            "COUNT(*) as count " +
            "FROM sql_template " +
            "WHERE created_time >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
            "GROUP BY DATE_FORMAT(created_time, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> selectMonthlyCreationStatistics();

    @Select("SELECT " +
            "COUNT(*) as total_templates, " +
            "COUNT(CASE WHEN is_active = true THEN 1 END) as active_templates, " +
            "COUNT(CASE WHEN is_public = true THEN 1 END) as public_templates, " +
            "COUNT(CASE WHEN approval_status = 'APPROVED' THEN 1 END) as approved_templates, " +
            "COUNT(CASE WHEN approval_status = 'PENDING' THEN 1 END) as pending_templates")
    Map<String, Object> selectOverallStatistics();

    @Update("UPDATE sql_template SET usage_count = usage_count + 1, last_used_time = NOW() WHERE template_id = #{templateId}")
    int updateUsageCount(@Param("templateId") Long templateId);

    @Select("SELECT * FROM sql_template WHERE template_name = #{templateName} AND template_id != #{templateId}")
    List<SqlTemplate> selectByTemplateNameExcludeId(@Param("templateName") String templateName, @Param("templateId") Long templateId);

    @Select("SELECT * FROM sql_template WHERE template_hash = #{templateHash} AND template_id != #{templateId}")
    List<SqlTemplate> selectByTemplateHashExcludeId(@Param("templateHash") String templateHash, @Param("templateId") Long templateId);

    @Select("SELECT * FROM sql_template WHERE is_active = true AND is_public = true ORDER BY usage_count DESC LIMIT #{limit}")
    List<SqlTemplate> selectPopularTemplates(@Param("limit") Integer limit);

    @Select("SELECT * FROM sql_template WHERE created_by = #{userId} ORDER BY updated_time DESC LIMIT #{limit}")
    List<SqlTemplate> selectUserRecentTemplates(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Select("SELECT DISTINCT template_category FROM sql_template WHERE template_category IS NOT NULL ORDER BY template_category")
    List<String> selectAllCategories();

    @Select("SELECT DISTINCT database_type FROM sql_template WHERE database_type IS NOT NULL ORDER BY database_type")
    List<String> selectAllDatabaseTypes();

    @Select("SELECT DISTINCT tags FROM sql_template WHERE tags IS NOT NULL")
    List<String> selectAllTags();

    @Update("UPDATE sql_template SET approval_status = #{approvalStatus}, approved_by = #{approvedBy}, approved_time = NOW() WHERE template_id = #{templateId}")
    int updateApprovalStatus(@Param("templateId") Long templateId, @Param("approvalStatus") String approvalStatus, @Param("approvedBy") Long approvedBy);

    @Update(" UPDATE sql_template " +
            "SET template_name = #{templateName}, template_description = #{templateDescription}, template_content = #{templateContent}, " +
            " template_category = #{templateCategory}, template_version = #{templateVersion}," +
            " business_type = #{businessType}, usage_type = #{usageType}, department_code = #{departmentCode}," +
            " tags = #{tags}, datasource_id = #{datasourceId}, template_hash = #{templateHash}," +
            " is_active = #{isActive}, is_public = #{isPublic}, approval_status = #{approvalStatus} " +
            "WHERE template_id = #{templateId}")
    int updateTemplateToNewVersion(SqlTemplate newTemplate);

    @Select("SELECT * FROM sql_template WHERE approval_status = 'PENDING' ORDER BY created_time ASC")
    List<SqlTemplate> selectPendingApprovalTemplates();

    @Select("<script>" +
            "SELECT * FROM sql_template WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (template_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR template_description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR template_content LIKE CONCAT('%', #{keyword}, '%') " +
            "OR tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY usage_count DESC, updated_time DESC " +
            "</script>")
    List<SqlTemplate> searchTemplates(@Param("keyword") String keyword);

    @Delete("DELETE FROM sql_template WHERE template_id = #{templateId}")
    int deleteSqlTemplateById(@Param("templateId") Long templateId);
}