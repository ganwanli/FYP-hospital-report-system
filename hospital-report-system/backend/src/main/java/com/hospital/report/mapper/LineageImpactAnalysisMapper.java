package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.LineageImpactAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface LineageImpactAnalysisMapper extends BaseMapper<LineageImpactAnalysis> {

    @Select("SELECT * FROM lineage_impact_analysis " +
            "WHERE source_node_id = #{nodeId} " +
            "ORDER BY impact_level, created_time DESC")
    List<LineageImpactAnalysis> getImpactAnalysisBySource(@Param("nodeId") String nodeId);

    @Select("SELECT " +
            "  impact_type, " +
            "  COUNT(*) as count, " +
            "  AVG(impact_probability) as avg_probability " +
            "FROM lineage_impact_analysis " +
            "GROUP BY impact_type " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getImpactTypeStatistics();

    @Select("SELECT " +
            "  risk_level, " +
            "  COUNT(*) as count " +
            "FROM lineage_impact_analysis " +
            "GROUP BY risk_level " +
            "ORDER BY " +
            "  CASE risk_level " +
            "    WHEN 'CRITICAL' THEN 1 " +
            "    WHEN 'HIGH' THEN 2 " +
            "    WHEN 'MEDIUM' THEN 3 " +
            "    WHEN 'LOW' THEN 4 " +
            "    ELSE 5 " +
            "  END")
    List<Map<String, Object>> getRiskLevelStatistics();

    @Select("SELECT " +
            "  source_node_id, " +
            "  COUNT(*) as analysis_count, " +
            "  AVG(impact_probability) as avg_probability " +
            "FROM lineage_impact_analysis " +
            "GROUP BY source_node_id " +
            "HAVING COUNT(*) >= #{minCount} " +
            "ORDER BY analysis_count DESC, avg_probability DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopImpactNodes(@Param("minCount") Integer minCount, @Param("limit") Integer limit);
}