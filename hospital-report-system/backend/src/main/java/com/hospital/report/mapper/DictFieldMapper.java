package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.entity.DictField;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 数据字段字典 Mapper 接口
 */
@Mapper
public interface DictFieldMapper extends BaseMapper<DictField> {

    /**
     * 分页查询数据字段
     */
    @Select("<script>" +
            "SELECT df.*, " +
            "u1.username as created_by_name, " +
            "u2.username as updated_by_name " +
            "FROM dict_field df " +
            "LEFT JOIN sys_user u1 ON df.created_by = u1.user_id " +
            "LEFT JOIN sys_user u2 ON df.updated_by = u2.user_id " +
            "WHERE df.is_deleted = 0 " +
            "<if test='fieldName != null and fieldName != \"\"'>" +
            "AND df.field_name LIKE CONCAT('%', #{fieldName}, '%') " +
            "</if>" +
            "<if test='fieldCode != null and fieldCode != \"\"'>" +
            "AND df.field_code LIKE CONCAT('%', #{fieldCode}, '%') " +
            "</if>" +
            "<if test='categoryId != null'>" +
            "AND df.category_id = #{categoryId} " +
            "</if>" +
            "<if test='fieldType != null and fieldType != \"\"'>" +
            "AND df.field_type = #{fieldType} " +
            "</if>" +
            "<if test='status != null'>" +
            "AND df.status = #{status} " +
            "</if>" +
            "ORDER BY df.sort_order ASC, df.created_time DESC " +
            "</script>")
    IPage<DictField> selectDictFieldPage(Page<DictField> page,
                                         @Param("fieldName") String fieldName,
                                         @Param("fieldCode") String fieldCode,
                                         @Param("categoryId") Long categoryId,
                                         @Param("fieldType") String fieldType,
                                         @Param("status") Integer status);

    /**
     * 根据分类ID查询字段列表
     */
    @Select("SELECT df.*, " +
            "u1.username as created_by_name, " +
            "u2.username as updated_by_name " +
            "FROM dict_field df " +
            "LEFT JOIN sys_user u1 ON df.created_by = u1.user_id " +
            "LEFT JOIN sys_user u2 ON df.updated_by = u2.user_id " +
            "WHERE df.is_deleted = 0 AND df.category_id = #{categoryId} " +
            "AND df.status = 1 " +
            "ORDER BY df.sort_order ASC, df.created_time DESC")
    List<DictField> selectFieldsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 查询所有分类（field_type = 'category'）
     */
    @Select("SELECT df.*, " +
            "u1.username as created_by_name, " +
            "u2.username as updated_by_name " +
            "FROM dict_field df " +
            "LEFT JOIN sys_user u1 ON df.created_by = u1.user_id " +
            "LEFT JOIN sys_user u2 ON df.updated_by = u2.user_id " +
            "WHERE df.is_deleted = 0 AND df.field_type = 'category' " +
            "AND df.status = 1 " +
            "ORDER BY df.sort_order ASC, df.created_time DESC")
    List<DictField> selectAllCategories();

    /**
     * 查询所有字段（field_type = 'field'）
     */
    @Select("SELECT df.*, " +
            "u1.username as created_by_name, " +
            "u2.username as updated_by_name " +
            "FROM dict_field df " +
            "LEFT JOIN sys_user u1 ON df.created_by = u1.id " +
            "LEFT JOIN sys_user u2 ON df.updated_by = u2.id " +
            "WHERE df.is_deleted = 0 AND df.field_type = 'field' " +
            "AND df.status = 1 " +
            "ORDER BY df.category_id ASC, df.sort_order ASC, df.created_time DESC")
    List<DictField> selectAllFields();

    /**
     * 查询树形结构数据（包含分类和字段）
     */
    @Select("SELECT df.*, " +
            "u1.username as created_by_name, " +
            "u2.username as updated_by_name " +
            "FROM dict_field df " +
            "LEFT JOIN sys_user u1 ON df.created_by = u1.user_id " +
            "LEFT JOIN sys_user u2 ON df.updated_by = u2.user_id " +
            "WHERE df.is_deleted = 0 AND df.status = 1 " +
            "ORDER BY df.field_type DESC, df.parent_category_id ASC, df.sort_order ASC, df.created_time DESC")
    List<DictField> selectTreeData();

    /**
     * 根据字段编码查询字段
     */
    @Select("SELECT df.*, " +
            "u1.username as created_by_name, " +
            "u2.username as updated_by_name " +
            "FROM dict_field df " +
            "LEFT JOIN sys_user u1 ON df.created_by = u1.user_id " +
            "LEFT JOIN sys_user u2 ON df.updated_by = u2.user_id " +
            "WHERE df.is_deleted = 0 AND df.field_code = #{fieldCode}")
    DictField selectByFieldCode(@Param("fieldCode") String fieldCode);

    /**
     * 根据关键词搜索字段
     */
    @Select("<script>" +
            "SELECT df.*, " +
            "u1.username as created_by_name, " +
            "u2.username as updated_by_name " +
            "FROM dict_field df " +
            "LEFT JOIN sys_user u1 ON df.created_by = u1.user_id " +
            "LEFT JOIN sys_user u2 ON df.updated_by = u2.user_id " +
            "WHERE df.is_deleted = 0 AND df.status = 1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (df.field_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR df.field_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR df.field_description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR df.category_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY df.field_type DESC, df.sort_order ASC, df.created_time DESC " +
            "</script>")
    List<DictField> searchFields(@Param("keyword") String keyword);

    /**
     * 检查字段编码是否存在
     */
    @Select("SELECT COUNT(*) FROM dict_field WHERE field_code = #{fieldCode} AND is_deleted = 0 " +
            "<if test='excludeId != null'>AND field_id != #{excludeId}</if>")
    int checkFieldCodeExists(@Param("fieldCode") String fieldCode, @Param("excludeId") Long excludeId);

    /**
     * 获取分类下的最大排序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM dict_field " +
            "WHERE category_id = #{categoryId} AND is_deleted = 0")
    Integer getMaxSortOrderByCategory(@Param("categoryId") Long categoryId);

    /**
     * 批量更新状态
     */
    @Update("<script>" +
            "UPDATE dict_field SET status = #{status}, updated_by = #{updatedBy}, updated_time = NOW() " +
            "WHERE field_id IN " +
            "<foreach collection='fieldIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("fieldIds") List<Long> fieldIds, 
                         @Param("status") Integer status, 
                         @Param("updatedBy") Long updatedBy);

    /**
     * 批量删除（逻辑删除）
     */
    @Update("<script>" +
            "UPDATE dict_field SET is_deleted = 1, updated_by = #{updatedBy}, updated_time = NOW() " +
            "WHERE field_id IN " +
            "<foreach collection='fieldIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDelete(@Param("fieldIds") List<Long> fieldIds, @Param("updatedBy") Long updatedBy);

    /**
     * 插入新的字段记录
     */
    @Insert("INSERT INTO dict_field (" +
            "field_code, field_name, field_name_en, category_id,  " +
            "field_type, data_type, data_length, source_database, source_table, " +
            "source_field, filter_condition, calculation_sql, update_frequency, " +
            "data_owner, description, remark, " +
            "sort_order, status, created_by, created_time, updated_by, updated_time, is_deleted" +
            ") VALUES (" +
            "#{fieldCode}, #{fieldName}, #{fieldNameEn}, #{categoryId},  " +
            "#{fieldType}, #{dataType}, #{dataLength}, #{sourceDatabase}, #{sourceTable}, " +
            "#{sourceField}, #{filterCondition}, #{calculationSql}, #{updateFrequency}, " +
            "#{dataOwner}, #{description}, #{remark}, " +
            "#{sortOrder}, #{status}, #{createdBy}, NOW(), #{updatedBy}, NOW(), 0" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDictField(DictField dictField);

    /**
     * 动态更新字段记录 - 只更新非空字段
     */
    @Update("<script>" +
            "UPDATE dict_field " +
            "<set>" +
            "<if test='fieldCode != null and fieldCode != \"\"'>field_code = #{fieldCode},</if>" +
            "<if test='fieldName != null and fieldName != \"\"'>field_name = #{fieldName},</if>" +
            "<if test='fieldNameEn != null and fieldNameEn != \"\"'>field_name_en = #{fieldNameEn},</if>" +
            "<if test='categoryId != null'>category_id = #{categoryId},</if>" +
            "<if test='fieldType != null and fieldType != \"\"'>field_type = #{fieldType},</if>" +
            "<if test='dataType != null and dataType != \"\"'>data_type = #{dataType},</if>" +
            "<if test='dataLength != null and dataLength != \"\"'>data_length = #{dataLength},</if>" +
            "<if test='sourceDatabase != null and sourceDatabase != \"\"'>source_database = #{sourceDatabase},</if>" +
            "<if test='sourceTable != null and sourceTable != \"\"'>source_table = #{sourceTable},</if>" +
            "<if test='sourceField != null and sourceField != \"\"'>source_field = #{sourceField},</if>" +
            "<if test='filterCondition != null'>filter_condition = #{filterCondition},</if>" +
            "<if test='calculationSql != null'>calculation_sql = #{calculationSql},</if>" +
            "<if test='updateFrequency != null and updateFrequency != \"\"'>update_frequency = #{updateFrequency},</if>" +
            "<if test='dataOwner != null and dataOwner != \"\"'>data_owner = #{dataOwner},</if>" +
            "<if test='description != null'>description = #{description},</if>" +
            "<if test='remark != null'>remark = #{remark},</if>" +
            "<if test='sortOrder != null'>sort_order = #{sortOrder},</if>" +
            "<if test='status != null'>status = #{status},</if>" +
            "<if test='updatedBy != null and updatedBy != \"\"'>updated_by = #{updatedBy},</if>" +
            "<if test='isPublic != null'>is_public = #{isPublic},</if>" +
            "<if test='isDeleted != null'>is_deleted = #{isDeleted},</if>" +
            "updated_time = #{updatedTime}, " +
            "<if test='version != null'>version = #{version} + 1</if>" +
            "<if test='version == null'>version = version + 1</if>" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    boolean updateDictField(DictField dictField);
}
