package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.DataLineage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DataLineageMapper extends BaseMapper<DataLineage> {

    @Select("SELECT " +
            "  dl.*, " +
            "  sn.node_name as source_node_name, " +
            "  tn.node_name as target_node_name " +
            "FROM data_lineage dl " +
            "LEFT JOIN lineage_node sn ON dl.source_id = sn.node_id " +
            "LEFT JOIN lineage_node tn ON dl.target_id = tn.node_id " +
            "WHERE dl.is_deleted = 0 " +
            "AND (dl.source_id = #{nodeId} OR dl.target_id = #{nodeId}) " +
            "ORDER BY dl.dependency_level, dl.created_time")
    List<Map<String, Object>> getNodeLineages(@Param("nodeId") String nodeId);

    @Select("SELECT " +
            "  dl.*, " +
            "  sn.node_name as source_node_name, " +
            "  tn.node_name as target_node_name " +
            "FROM data_lineage dl " +
            "LEFT JOIN lineage_node sn ON dl.source_id = sn.node_id " +
            "LEFT JOIN lineage_node tn ON dl.target_id = tn.node_id " +
            "WHERE dl.is_deleted = 0 " +
            "AND dl.source_id = #{nodeId} " +
            "ORDER BY dl.dependency_level")
    List<Map<String, Object>> getDownstreamLineages(@Param("nodeId") String nodeId);

    @Select("SELECT " +
            "  dl.*, " +
            "  sn.node_name as source_node_name, " +
            "  tn.node_name as target_node_name " +
            "FROM data_lineage dl " +
            "LEFT JOIN lineage_node sn ON dl.source_id = sn.node_id " +
            "LEFT JOIN lineage_node tn ON dl.target_id = tn.node_id " +
            "WHERE dl.is_deleted = 0 " +
            "AND dl.target_id = #{nodeId} " +
            "ORDER BY dl.dependency_level")
    List<Map<String, Object>> getUpstreamLineages(@Param("nodeId") String nodeId);

    @Select("WITH RECURSIVE lineage_tree AS ( " +
            "  SELECT source_id, target_id, 1 as level " +
            "  FROM data_lineage " +
            "  WHERE source_id = #{startNodeId} AND is_deleted = 0 " +
            "  UNION ALL " +
            "  SELECT dl.source_id, dl.target_id, lt.level + 1 " +
            "  FROM data_lineage dl " +
            "  INNER JOIN lineage_tree lt ON dl.source_id = lt.target_id " +
            "  WHERE dl.is_deleted = 0 AND lt.level < #{maxDepth} " +
            ") " +
            "SELECT DISTINCT " +
            "  lt.source_id, " +
            "  lt.target_id, " +
            "  lt.level, " +
            "  dl.*, " +
            "  sn.node_name as source_node_name, " +
            "  tn.node_name as target_node_name " +
            "FROM lineage_tree lt " +
            "JOIN data_lineage dl ON lt.source_id = dl.source_id AND lt.target_id = dl.target_id " +
            "LEFT JOIN lineage_node sn ON dl.source_id = sn.node_id " +
            "LEFT JOIN lineage_node tn ON dl.target_id = tn.node_id " +
            "ORDER BY lt.level, dl.created_time")
    List<Map<String, Object>> getLineageTree(@Param("startNodeId") String startNodeId, @Param("maxDepth") Integer maxDepth);

    @Select("SELECT " +
            "  dl.target_id as node_id, " +
            "  COUNT(*) as impact_count " +
            "FROM data_lineage dl " +
            "WHERE dl.is_deleted = 0 " +
            "AND dl.source_id IN " +
            "  (SELECT target_id FROM data_lineage " +
            "   WHERE source_id = #{nodeId} AND is_deleted = 0) " +
            "GROUP BY dl.target_id " +
            "ORDER BY impact_count DESC")
    List<Map<String, Object>> getImpactAnalysis(@Param("nodeId") String nodeId);

    @Select("SELECT " +
            "  relation_type, " +
            "  COUNT(*) as count " +
            "FROM data_lineage " +
            "WHERE is_deleted = 0 " +
            "GROUP BY relation_type " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getRelationTypeStatistics();

    @Select("SELECT " +
            "  data_flow_direction, " +
            "  COUNT(*) as count " +
            "FROM data_lineage " +
            "WHERE is_deleted = 0 " +
            "GROUP BY data_flow_direction " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getDataFlowStatistics();

    @Select("SELECT " +
            "  source_table, " +
            "  target_table, " +
            "  COUNT(*) as relation_count " +
            "FROM data_lineage " +
            "WHERE is_deleted = 0 " +
            "AND source_table IS NOT NULL " +
            "AND target_table IS NOT NULL " +
            "GROUP BY source_table, target_table " +
            "ORDER BY relation_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopTableRelations(@Param("limit") Integer limit);

    @Select("SELECT " +
            "  verification_status, " +
            "  COUNT(*) as count " +
            "FROM data_lineage " +
            "WHERE is_deleted = 0 " +
            "GROUP BY verification_status")
    List<Map<String, Object>> getVerificationStatusStats();

    @Select("SELECT * FROM data_lineage " +
            "WHERE is_deleted = 0 " +
            "AND (source_name LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR target_name LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR transform_rule LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR business_context LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY confidence_score DESC " +
            "LIMIT #{limit}")
    List<DataLineage> searchLineages(@Param("keyword") String keyword, @Param("limit") Integer limit);

    @Select("SELECT " +
            "  dependency_level, " +
            "  AVG(confidence_score) as avg_confidence, " +
            "  COUNT(*) as count " +
            "FROM data_lineage " +
            "WHERE is_deleted = 0 " +
            "GROUP BY dependency_level " +
            "ORDER BY dependency_level")
    List<Map<String, Object>> getDependencyLevelStats();

    @Select("SELECT " +
            "  dl.*, " +
            "  sn.node_name as source_node_name, " +
            "  tn.node_name as target_node_name " +
            "FROM data_lineage dl " +
            "LEFT JOIN lineage_node sn ON dl.source_id = sn.node_id " +
            "LEFT JOIN lineage_node tn ON dl.target_id = tn.node_id " +
            "WHERE dl.is_deleted = 0 " +
            "AND (dl.source_id = #{nodeId} OR dl.target_id = #{nodeId}) " +
            "ORDER BY dl.created_time DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getRecentLineagesByNode(@Param("nodeId") String nodeId, @Param("limit") Integer limit);
}