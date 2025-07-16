package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.DictCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 数据字典分类Mapper接口
 * 
 * @author system
 * @since 2025-01-15
 */
@Mapper
public interface DictCategoryMapper extends BaseMapper<DictCategory> {

    /**
     * 查询所有分类（带层级排序）
     * 
     * @return 分类列表
     */
    @Select("SELECT dc.*, " +
            "       (SELECT category_name FROM dict_category WHERE id = dc.parent_id) as parent_name, " +
            "       (SELECT COUNT(*) FROM dict_category WHERE parent_id = dc.id AND status = 1) as has_children, " +
            "       (SELECT COUNT(*) FROM dict_field WHERE category_id = dc.id AND status = 1) as field_count " +
            "FROM dict_category dc " +
            "WHERE dc.status = 1 " +
            "ORDER BY dc.category_level ASC, dc.sort_order ASC, dc.create_time ASC")
    List<DictCategory> selectAllWithHierarchy();

    /**
     * 查询指定父级的子分类列表
     * 
     * @param parentId 父级分类ID
     * @return 子分类列表
     */
    @Select("SELECT dc.*, " +
            "       (SELECT category_name FROM dict_category WHERE id = dc.parent_id) as parent_name, " +
            "       (SELECT COUNT(*) FROM dict_category WHERE parent_id = dc.id AND status = 1) as has_children, " +
            "       (SELECT COUNT(*) FROM dict_field WHERE category_id = dc.id AND status = 1) as field_count " +
            "FROM dict_category dc " +
            "WHERE dc.parent_id = #{parentId} AND dc.status = 1 " +
            "ORDER BY dc.sort_order ASC, dc.create_time ASC")
    List<DictCategory> selectChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 查询分类路径（从根节点到指定节点）
     * 
     * @param categoryId 分类ID
     * @return 路径分类列表
     */
    @Select("WITH RECURSIVE category_path AS ( " +
            "    SELECT id, category_code, category_name, parent_id, category_level, 1 as path_level " +
            "    FROM dict_category " +
            "    WHERE id = #{categoryId} " +
            "    UNION ALL " +
            "    SELECT dc.id, dc.category_code, dc.category_name, dc.parent_id, dc.category_level, cp.path_level + 1 " +
            "    FROM dict_category dc " +
            "    INNER JOIN category_path cp ON dc.id = cp.parent_id " +
            ") " +
            "SELECT * FROM category_path ORDER BY category_level ASC")
    List<DictCategory> selectCategoryPath(@Param("categoryId") Long categoryId);

    /**
     * 检查分类编码是否存在
     * 
     * @param categoryCode 分类编码
     * @param excludeId 排除的分类ID（用于更新时检查）
     * @return 存在的数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM dict_category " +
            "WHERE category_code = #{categoryCode} " +
            "<if test='excludeId != null'>" +
            "  AND id != #{excludeId} " +
            "</if>" +
            "</script>")
    int checkCategoryCodeExists(@Param("categoryCode") String categoryCode, @Param("excludeId") Long excludeId);

    /**
     * 查询指定层级的分类
     * 
     * @param level 分类层级
     * @return 分类列表
     */
    @Select("SELECT dc.*, " +
            "       (SELECT category_name FROM dict_category WHERE id = dc.parent_id) as parent_name, " +
            "       (SELECT COUNT(*) FROM dict_category WHERE parent_id = dc.id AND status = 1) as has_children, " +
            "       (SELECT COUNT(*) FROM dict_field WHERE category_id = dc.id AND status = 1) as field_count " +
            "FROM dict_category dc " +
            "WHERE dc.category_level = #{level} AND dc.status = 1 " +
            "ORDER BY dc.sort_order ASC, dc.create_time ASC")
    List<DictCategory> selectByLevel(@Param("level") Integer level);

    /**
     * 查询所有子分类ID（递归）
     * 
     * @param parentId 父级分类ID
     * @return 子分类ID列表
     */
    @Select("WITH RECURSIVE children_ids AS ( " +
            "    SELECT id FROM dict_category WHERE parent_id = #{parentId} " +
            "    UNION ALL " +
            "    SELECT dc.id FROM dict_category dc " +
            "    INNER JOIN children_ids ci ON dc.parent_id = ci.id " +
            ") " +
            "SELECT id FROM children_ids")
    List<Long> selectAllChildrenIds(@Param("parentId") Long parentId);

    /**
     * 批量更新状态
     * 
     * @param ids 分类ID列表
     * @param status 状态
     * @param updateBy 更新人
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE dict_category SET status = #{status}, update_by = #{updateBy}, update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "  #{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status, @Param("updateBy") String updateBy);

    /**
     * 更新排序序号
     * 
     * @param id 分类ID
     * @param sortOrder 排序序号
     * @param updateBy 更新人
     * @return 更新数量
     */
    @Update("UPDATE dict_category SET sort_order = #{sortOrder}, update_by = #{updateBy}, update_time = NOW() " +
            "WHERE id = #{id}")
    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder, @Param("updateBy") String updateBy);

    /**
     * 查询同级分类的最大排序号
     * 
     * @param parentId 父级分类ID
     * @return 最大排序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM dict_category WHERE parent_id = #{parentId}")
    Integer selectMaxSortOrderByParentId(@Param("parentId") Long parentId);

    /**
     * 查询分类及其统计信息
     * 
     * @param categoryCode 分类编码
     * @return 分类信息
     */
    @Select("SELECT dc.*, " +
            "       (SELECT category_name FROM dict_category WHERE id = dc.parent_id) as parent_name, " +
            "       (SELECT COUNT(*) FROM dict_category WHERE parent_id = dc.id AND status = 1) as has_children, " +
            "       (SELECT COUNT(*) FROM dict_field WHERE category_id = dc.id AND status = 1) as field_count " +
            "FROM dict_category dc " +
            "WHERE dc.category_code = #{categoryCode}")
    DictCategory selectByCodeWithStats(@Param("categoryCode") String categoryCode);

    /**
     * 搜索分类（按名称或编码）
     * 
     * @param keyword 关键词
     * @return 分类列表
     */
    @Select("SELECT dc.*, " +
            "       (SELECT category_name FROM dict_category WHERE id = dc.parent_id) as parent_name, " +
            "       (SELECT COUNT(*) FROM dict_category WHERE parent_id = dc.id AND status = 1) as has_children, " +
            "       (SELECT COUNT(*) FROM dict_field WHERE category_id = dc.id AND status = 1) as field_count " +
            "FROM dict_category dc " +
            "WHERE dc.status = 1 " +
            "  AND (dc.category_name LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR dc.category_code LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY dc.category_level ASC, dc.sort_order ASC")
    List<DictCategory> searchCategories(@Param("keyword") String keyword);

    /**
     * 检查分类是否有关联的字段
     * 
     * @param categoryId 分类ID
     * @return 关联字段数量
     */
    @Select("SELECT COUNT(*) FROM dict_field WHERE category_id = #{categoryId} AND status = 1")
    int countRelatedFields(@Param("categoryId") Long categoryId);

    /**
     * 检查分类是否有子分类
     * 
     * @param categoryId 分类ID
     * @return 子分类数量
     */
    @Select("SELECT COUNT(*) FROM dict_category WHERE parent_id = #{categoryId} AND status = 1")
    int countChildren(@Param("categoryId") Long categoryId);
}
