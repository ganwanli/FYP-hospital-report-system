package com.hospital.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.report.entity.DataLineage;
import com.hospital.report.entity.LineageNode;

import java.util.List;
import java.util.Map;

public interface DataLineageService extends IService<DataLineage> {

    /**
     * 获取节点的血缘关系
     */
    Map<String, Object> getNodeLineage(String nodeId, Integer maxDepth, String direction);

    /**
     * 获取血缘关系图谱数据
     */
    Map<String, Object> getLineageGraph(String nodeId, Integer maxDepth, String direction);

    /**
     * 构建血缘关系
     */
    void buildLineageRelation(String sourceNodeId, String targetNodeId, String relationType, 
                             String transformRule, Map<String, Object> metadata);

    /**
     * 影响分析
     */
    Map<String, Object> performImpactAnalysis(String nodeId, String changeType, Integer analysisDepth);

    /**
     * 血缘关系搜索
     */
    List<Map<String, Object>> searchLineage(String keyword, String nodeType, String relationType);

    /**
     * 获取血缘统计信息
     */
    Map<String, Object> getLineageStatistics();

    /**
     * 验证血缘关系
     */
    void verifyLineageRelation(Long lineageId, String verificationStatus, String verificationComment);

    /**
     * 自动发现血缘关系
     */
    List<Map<String, Object>> discoverLineageRelations(String systemSource, String discoveryMethod);

    /**
     * 获取数据流向分析
     */
    Map<String, Object> getDataFlowAnalysis(String nodeId);

    /**
     * 获取血缘路径
     */
    List<Map<String, Object>> getLineagePath(String sourceNodeId, String targetNodeId);

    /**
     * 批量导入血缘关系
     */
    Map<String, Object> importLineageRelations(List<Map<String, Object>> relations);

    /**
     * 导出血缘关系
     */
    byte[] exportLineageRelations(String nodeId, String format);

    /**
     * 获取循环依赖检测
     */
    List<Map<String, Object>> detectCircularDependencies();

    /**
     * 获取孤立节点
     */
    List<LineageNode> getOrphanNodes();

    /**
     * 血缘关系健康检查
     */
    Map<String, Object> performLineageHealthCheck();
}