package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.LineageNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface LineageNodeMapper extends BaseMapper<LineageNode> {

    @Select("SELECT * FROM lineage_node " +
            "WHERE is_deleted = 0 " +
            "AND (node_name LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR display_name LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR business_meaning LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY last_access_time DESC " +
            "LIMIT #{limit}")
    List<LineageNode> searchNodes(@Param("keyword") String keyword, @Param("limit") Integer limit);

    @Select("SELECT " +
            "  node_type, " +
            "  COUNT(*) as count " +
            "FROM lineage_node " +
            "WHERE is_deleted = 0 " +
            "GROUP BY node_type " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getNodeTypeStatistics();

    @Select("SELECT " +
            "  system_source, " +
            "  COUNT(*) as count " +
            "FROM lineage_node " +
            "WHERE is_deleted = 0 " +
            "AND system_source IS NOT NULL " +
            "GROUP BY system_source " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getSystemSourceStatistics();

    @Select("SELECT " +
            "  criticality_level, " +
            "  COUNT(*) as count " +
            "FROM lineage_node " +
            "WHERE is_deleted = 0 " +
            "AND criticality_level IS NOT NULL " +
            "GROUP BY criticality_level " +
            "ORDER BY " +
            "  CASE criticality_level " +
            "    WHEN 'CRITICAL' THEN 1 " +
            "    WHEN 'HIGH' THEN 2 " +
            "    WHEN 'MEDIUM' THEN 3 " +
            "    WHEN 'LOW' THEN 4 " +
            "    ELSE 5 " +
            "  END")
    List<Map<String, Object>> getCriticalityLevelStats();

    @Update("UPDATE lineage_node " +
            "SET position_x = #{positionX}, position_y = #{positionY}, updated_time = NOW() " +
            "WHERE node_id = #{nodeId}")
    int updateNodePosition(@Param("nodeId") String nodeId, 
                          @Param("positionX") Double positionX, 
                          @Param("positionY") Double positionY);

    @Select("SELECT * FROM lineage_node " +
            "WHERE is_deleted = 0 " +
            "AND node_type = #{nodeType} " +
            "ORDER BY node_name")
    List<LineageNode> getNodesByType(@Param("nodeType") String nodeType);

    @Select("SELECT * FROM lineage_node " +
            "WHERE is_deleted = 0 " +
            "AND system_source = #{systemSource} " +
            "ORDER BY table_name, column_name")
    List<LineageNode> getNodesBySystem(@Param("systemSource") String systemSource);

    @Select("SELECT " +
            "  table_name, " +
            "  COUNT(*) as column_count " +
            "FROM lineage_node " +
            "WHERE is_deleted = 0 " +
            "AND table_name IS NOT NULL " +
            "GROUP BY table_name " +
            "ORDER BY column_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopTables(@Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM lineage_node " +
            "WHERE is_deleted = 0 " +
            "AND node_id = #{nodeId}")
    int checkNodeExists(@Param("nodeId") String nodeId);

    @Select("SELECT * FROM lineage_node " +
            "WHERE is_deleted = 0 " +
            "AND owner_department = #{department} " +
            "ORDER BY criticality_level, node_name")
    List<LineageNode> getNodesByDepartment(@Param("department") String department);
}