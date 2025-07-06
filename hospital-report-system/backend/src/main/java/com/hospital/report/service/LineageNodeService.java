package com.hospital.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.report.entity.LineageNode;

import java.util.List;
import java.util.Map;

public interface LineageNodeService extends IService<LineageNode> {

    /**
     * 创建或更新节点
     */
    LineageNode createOrUpdateNode(LineageNode node);

    /**
     * 搜索节点
     */
    List<LineageNode> searchNodes(String keyword, String nodeType, String systemSource);

    /**
     * 获取节点统计信息
     */
    Map<String, Object> getNodeStatistics();

    /**
     * 更新节点位置
     */
    void updateNodePosition(String nodeId, Double positionX, Double positionY);

    /**
     * 获取节点详情（包含关联信息）
     */
    Map<String, Object> getNodeDetail(String nodeId);

    /**
     * 批量导入节点
     */
    Map<String, Object> importNodes(List<Map<String, Object>> nodes);

    /**
     * 自动发现并注册节点
     */
    List<LineageNode> discoverAndRegisterNodes(String systemSource, Map<String, Object> config);

    /**
     * 获取节点层级结构
     */
    Map<String, Object> getNodeHierarchy(String rootNodeId);

    /**
     * 节点分类统计
     */
    List<Map<String, Object>> getNodeCategoryStats();

    /**
     * 获取关键节点（高影响度）
     */
    List<LineageNode> getCriticalNodes(Integer limit);

    /**
     * 节点健康状态检查
     */
    Map<String, Object> checkNodeHealth(String nodeId);

    /**
     * 同步节点元数据
     */
    void syncNodeMetadata(String nodeId, Map<String, Object> metadata);
}