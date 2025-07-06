package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.report.entity.DataLineage;
import com.hospital.report.entity.LineageNode;
import com.hospital.report.mapper.DataLineageMapper;
import com.hospital.report.mapper.LineageNodeMapper;
import com.hospital.report.service.LineageNodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LineageNodeServiceImpl extends ServiceImpl<LineageNodeMapper, LineageNode> 
        implements LineageNodeService {

    private final LineageNodeMapper lineageNodeMapper;
    private final DataLineageMapper dataLineageMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public LineageNode createOrUpdateNode(LineageNode node) {
        if (node.getNodeId() == null || node.getNodeId().isEmpty()) {
            node.setNodeId(UUID.randomUUID().toString());
        }

        QueryWrapper<LineageNode> wrapper = new QueryWrapper<>();
        wrapper.eq("node_id", node.getNodeId()).eq("is_deleted", false);
        LineageNode existingNode = this.getOne(wrapper);

        if (existingNode != null) {
            existingNode.setNodeName(node.getNodeName());
            existingNode.setDisplayName(node.getDisplayName());
            existingNode.setNodeType(node.getNodeType());
            existingNode.setNodeCategory(node.getNodeCategory());
            existingNode.setTableName(node.getTableName());
            existingNode.setColumnName(node.getColumnName());
            existingNode.setDataType(node.getDataType());
            existingNode.setBusinessMeaning(node.getBusinessMeaning());
            existingNode.setSystemSource(node.getSystemSource());
            existingNode.setOwnerUser(node.getOwnerUser());
            existingNode.setOwnerDepartment(node.getOwnerDepartment());
            existingNode.setCriticalityLevel(node.getCriticalityLevel());
            existingNode.setDataQualityScore(node.getDataQualityScore());
            existingNode.setStatus(node.getStatus());
            existingNode.setUpdatedTime(LocalDateTime.now());
            
            this.updateById(existingNode);
            return existingNode;
        } else {
            node.setCreatedTime(LocalDateTime.now());
            node.setUpdatedTime(LocalDateTime.now());
            node.setLastAccessTime(LocalDateTime.now());
            if (node.getStatus() == null) {
                node.setStatus(1);
            }
            this.save(node);
            return node;
        }
    }

    @Override
    public List<LineageNode> searchNodes(String keyword, String nodeType, String systemSource) {
        QueryWrapper<LineageNode> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", false);

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("node_name", keyword)
                           .or().like("display_name", keyword)
                           .or().like("business_meaning", keyword)
                           .or().like("table_name", keyword)
                           .or().like("column_name", keyword));
        }

        if (nodeType != null && !nodeType.trim().isEmpty()) {
            wrapper.eq("node_type", nodeType);
        }

        if (systemSource != null && !systemSource.trim().isEmpty()) {
            wrapper.eq("system_source", systemSource);
        }

        wrapper.orderByDesc("last_access_time");
        wrapper.last("LIMIT 100");

        return this.list(wrapper);
    }

    @Override
    public Map<String, Object> getNodeStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalNodes = this.count(new QueryWrapper<LineageNode>().eq("is_deleted", false));
        stats.put("totalNodes", totalNodes);

        stats.put("nodeTypeStats", lineageNodeMapper.getNodeTypeStatistics());
        stats.put("systemSourceStats", lineageNodeMapper.getSystemSourceStatistics());
        stats.put("criticalityStats", lineageNodeMapper.getCriticalityLevelStats());
        stats.put("topTables", lineageNodeMapper.getTopTables(10));

        QueryWrapper<LineageNode> activeWrapper = new QueryWrapper<>();
        activeWrapper.eq("is_deleted", false)
                    .eq("status", 1);
        long activeNodes = this.count(activeWrapper);
        stats.put("activeNodes", activeNodes);

        QueryWrapper<LineageNode> recentWrapper = new QueryWrapper<>();
        recentWrapper.eq("is_deleted", false)
                    .ge("last_access_time", LocalDateTime.now().minusDays(7));
        long recentlyAccessedNodes = this.count(recentWrapper);
        stats.put("recentlyAccessedNodes", recentlyAccessedNodes);

        QueryWrapper<LineageNode> ownerWrapper = new QueryWrapper<>();
        ownerWrapper.eq("is_deleted", false)
                   .isNotNull("owner_user");
        long nodesWithOwner = this.count(ownerWrapper);
        double ownershipRate = totalNodes > 0 ? (double) nodesWithOwner / totalNodes * 100 : 0;
        stats.put("ownershipRate", ownershipRate);

        return stats;
    }

    @Override
    @Transactional
    public void updateNodePosition(String nodeId, Double positionX, Double positionY) {
        int updated = lineageNodeMapper.updateNodePosition(nodeId, positionX, positionY);
        if (updated == 0) {
            throw new RuntimeException("节点不存在或更新失败: " + nodeId);
        }
        log.info("更新节点位置: {} -> ({}, {})", nodeId, positionX, positionY);
    }

    @Override
    public Map<String, Object> getNodeDetail(String nodeId) {
        QueryWrapper<LineageNode> wrapper = new QueryWrapper<>();
        wrapper.eq("node_id", nodeId).eq("is_deleted", false);
        LineageNode node = this.getOne(wrapper);

        if (node == null) {
            throw new RuntimeException("节点不存在: " + nodeId);
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("node", node);

        QueryWrapper<DataLineage> upstreamWrapper = new QueryWrapper<>();
        upstreamWrapper.eq("target_id", nodeId).eq("is_deleted", false);
        long upstreamCount = dataLineageMapper.selectCount(upstreamWrapper);
        detail.put("upstreamCount", upstreamCount);

        QueryWrapper<DataLineage> downstreamWrapper = new QueryWrapper<>();
        downstreamWrapper.eq("source_id", nodeId).eq("is_deleted", false);
        long downstreamCount = dataLineageMapper.selectCount(downstreamWrapper);
        detail.put("downstreamCount", downstreamCount);

        List<Map<String, Object>> recentLineages = dataLineageMapper.getRecentLineagesByNode(nodeId, 10);
        detail.put("recentLineages", recentLineages);

        Map<String, Object> accessStats = calculateAccessStats(node);
        detail.put("accessStats", accessStats);

        return detail;
    }

    @Override
    @Transactional
    public Map<String, Object> importNodes(List<Map<String, Object>> nodes) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> nodeData : nodes) {
            try {
                LineageNode node = convertMapToNode(nodeData);
                createOrUpdateNode(node);
                successCount++;
            } catch (Exception e) {
                errorCount++;
                errors.add("导入节点失败: " + e.getMessage());
                log.error("导入节点失败", e);
            }
        }

        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("errors", errors);

        return result;
    }

    @Override
    public List<LineageNode> discoverAndRegisterNodes(String systemSource, Map<String, Object> config) {
        List<LineageNode> discoveredNodes = new ArrayList<>();

        String discoveryMethod = (String) config.getOrDefault("method", "METADATA_SCAN");

        switch (discoveryMethod) {
            case "METADATA_SCAN":
                discoveredNodes = discoverNodesByMetadata(systemSource, config);
                break;
            case "CONNECTION_ANALYSIS":
                discoveredNodes = discoverNodesByConnections(systemSource, config);
                break;
            case "NAMING_PATTERN":
                discoveredNodes = discoverNodesByNaming(systemSource, config);
                break;
            default:
                log.warn("未知的发现方法: {}", discoveryMethod);
        }

        for (LineageNode node : discoveredNodes) {
            createOrUpdateNode(node);
        }

        log.info("发现并注册了 {} 个节点", discoveredNodes.size());
        return discoveredNodes;
    }

    @Override
    public Map<String, Object> getNodeHierarchy(String rootNodeId) {
        Map<String, Object> hierarchy = new HashMap<>();
        Set<String> visited = new HashSet<>();
        
        LineageNode rootNode = getNodeById(rootNodeId);
        if (rootNode == null) {
            throw new RuntimeException("根节点不存在: " + rootNodeId);
        }

        Map<String, Object> tree = buildNodeTree(rootNodeId, visited, 0, 5);
        hierarchy.put("root", tree);
        hierarchy.put("totalNodes", visited.size());

        return hierarchy;
    }

    @Override
    public List<Map<String, Object>> getNodeCategoryStats() {
        QueryWrapper<LineageNode> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", false)
               .groupBy("node_category")
               .select("node_category as category, COUNT(*) as count");

        List<Map<String, Object>> categoryStats = this.listMaps(wrapper);

        for (Map<String, Object> stat : categoryStats) {
            String category = (String) stat.get("category");
            if (category != null) {
                QueryWrapper<LineageNode> criticalWrapper = new QueryWrapper<>();
                criticalWrapper.eq("is_deleted", false)
                              .eq("node_category", category)
                              .eq("criticality_level", "CRITICAL");
                long criticalCount = this.count(criticalWrapper);
                stat.put("criticalCount", criticalCount);
            }
        }

        return categoryStats;
    }

    @Override
    public List<LineageNode> getCriticalNodes(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20;
        }

        QueryWrapper<LineageNode> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", false)
               .in("criticality_level", Arrays.asList("CRITICAL", "HIGH"))
               .orderByDesc("data_quality_score")
               .orderByDesc("last_access_time")
               .last("LIMIT " + limit);

        List<LineageNode> criticalNodes = this.list(wrapper);

        for (LineageNode node : criticalNodes) {
            int connectionCount = calculateConnectionCount(node.getNodeId());
            node.setConnectionCount(connectionCount);
        }

        return criticalNodes.stream()
                .sorted((a, b) -> Integer.compare(b.getConnectionCount(), a.getConnectionCount()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> checkNodeHealth(String nodeId) {
        LineageNode node = getNodeById(nodeId);
        if (node == null) {
            throw new RuntimeException("节点不存在: " + nodeId);
        }

        Map<String, Object> health = new HashMap<>();
        health.put("nodeId", nodeId);

        double healthScore = 100.0;
        List<String> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (node.getBusinessMeaning() == null || node.getBusinessMeaning().trim().isEmpty()) {
            healthScore -= 15;
            issues.add("缺少业务含义描述");
            recommendations.add("添加业务含义描述");
        }

        if (node.getOwnerUser() == null || node.getOwnerUser().trim().isEmpty()) {
            healthScore -= 10;
            issues.add("缺少责任人");
            recommendations.add("指定数据责任人");
        }

        if (node.getDataQualityScore() != null && node.getDataQualityScore() < 0.8) {
            healthScore -= 20;
            issues.add("数据质量分数低于80%");
            recommendations.add("提升数据质量");
        }

        int connectionCount = calculateConnectionCount(nodeId);
        if (connectionCount == 0) {
            healthScore -= 25;
            issues.add("孤立节点，无血缘关系");
            recommendations.add("建立血缘关系");
        }

        if (node.getLastAccessTime() != null && 
            node.getLastAccessTime().isBefore(LocalDateTime.now().minusDays(30))) {
            healthScore -= 10;
            issues.add("超过30天未访问");
            recommendations.add("检查节点是否仍在使用");
        }

        health.put("healthScore", Math.max(0, healthScore));
        health.put("issues", issues);
        health.put("recommendations", recommendations);
        health.put("connectionCount", connectionCount);
        health.put("checkTime", LocalDateTime.now());

        return health;
    }

    @Override
    @Transactional
    public void syncNodeMetadata(String nodeId, Map<String, Object> metadata) {
        LineageNode node = getNodeById(nodeId);
        if (node == null) {
            throw new RuntimeException("节点不存在: " + nodeId);
        }

        if (metadata.containsKey("dataType")) {
            node.setDataType((String) metadata.get("dataType"));
        }
        if (metadata.containsKey("businessMeaning")) {
            node.setBusinessMeaning((String) metadata.get("businessMeaning"));
        }
        if (metadata.containsKey("ownerUser")) {
            node.setOwnerUser((String) metadata.get("ownerUser"));
        }
        if (metadata.containsKey("criticalityLevel")) {
            node.setCriticalityLevel((String) metadata.get("criticalityLevel"));
        }
        if (metadata.containsKey("dataQualityScore")) {
            Object score = metadata.get("dataQualityScore");
            if (score instanceof Number) {
                node.setDataQualityScore(((Number) score).doubleValue());
            }
        }

        node.setUpdatedTime(LocalDateTime.now());
        this.updateById(node);

        log.info("同步节点元数据: {}", nodeId);
    }

    private LineageNode getNodeById(String nodeId) {
        QueryWrapper<LineageNode> wrapper = new QueryWrapper<>();
        wrapper.eq("node_id", nodeId).eq("is_deleted", false);
        return this.getOne(wrapper);
    }

    private LineageNode convertMapToNode(Map<String, Object> nodeData) {
        LineageNode node = new LineageNode();
        
        node.setNodeId((String) nodeData.get("nodeId"));
        node.setNodeName((String) nodeData.get("nodeName"));
        node.setDisplayName((String) nodeData.get("displayName"));
        node.setNodeType((String) nodeData.get("nodeType"));
        node.setNodeCategory((String) nodeData.get("nodeCategory"));
        node.setTableName((String) nodeData.get("tableName"));
        node.setColumnName((String) nodeData.get("columnName"));
        node.setDataType((String) nodeData.get("dataType"));
        node.setBusinessMeaning((String) nodeData.get("businessMeaning"));
        node.setSystemSource((String) nodeData.get("systemSource"));
        node.setOwnerUser((String) nodeData.get("ownerUser"));
        node.setOwnerDepartment((String) nodeData.get("ownerDepartment"));
        node.setCriticalityLevel((String) nodeData.get("criticalityLevel"));
        
        Object qualityScore = nodeData.get("dataQualityScore");
        if (qualityScore instanceof Number) {
            node.setDataQualityScore(((Number) qualityScore).doubleValue());
        }
        
        Object positionX = nodeData.get("positionX");
        if (positionX instanceof Number) {
            node.setPositionX(((Number) positionX).doubleValue());
        }
        
        Object positionY = nodeData.get("positionY");
        if (positionY instanceof Number) {
            node.setPositionY(((Number) positionY).doubleValue());
        }

        return node;
    }

    private Map<String, Object> calculateAccessStats(LineageNode node) {
        Map<String, Object> stats = new HashMap<>();
        
        if (node.getLastAccessTime() != null) {
            long daysSinceAccess = java.time.temporal.ChronoUnit.DAYS.between(
                node.getLastAccessTime(), LocalDateTime.now());
            stats.put("daysSinceLastAccess", daysSinceAccess);
            
            if (daysSinceAccess <= 1) {
                stats.put("accessFrequency", "DAILY");
            } else if (daysSinceAccess <= 7) {
                stats.put("accessFrequency", "WEEKLY");
            } else if (daysSinceAccess <= 30) {
                stats.put("accessFrequency", "MONTHLY");
            } else {
                stats.put("accessFrequency", "RARELY");
            }
        } else {
            stats.put("accessFrequency", "NEVER");
        }

        return stats;
    }

    private List<LineageNode> discoverNodesByMetadata(String systemSource, Map<String, Object> config) {
        List<LineageNode> nodes = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tableMetadata = (List<Map<String, Object>>) config.get("tables");
        
        if (tableMetadata != null) {
            for (Map<String, Object> table : tableMetadata) {
                String tableName = (String) table.get("tableName");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> columns = (List<Map<String, Object>>) table.get("columns");
                
                if (columns != null) {
                    for (Map<String, Object> column : columns) {
                        LineageNode node = new LineageNode();
                        node.setNodeId(systemSource + "." + tableName + "." + column.get("columnName"));
                        node.setNodeName((String) column.get("columnName"));
                        node.setDisplayName((String) column.get("displayName"));
                        node.setNodeType("COLUMN");
                        node.setNodeCategory("DATA_ELEMENT");
                        node.setTableName(tableName);
                        node.setColumnName((String) column.get("columnName"));
                        node.setDataType((String) column.get("dataType"));
                        node.setBusinessMeaning((String) column.get("businessMeaning"));
                        node.setSystemSource(systemSource);
                        node.setCriticalityLevel("MEDIUM");
                        
                        nodes.add(node);
                    }
                }
            }
        }
        
        return nodes;
    }

    private List<LineageNode> discoverNodesByConnections(String systemSource, Map<String, Object> config) {
        return new ArrayList<>();
    }

    private List<LineageNode> discoverNodesByNaming(String systemSource, Map<String, Object> config) {
        return new ArrayList<>();
    }

    private Map<String, Object> buildNodeTree(String nodeId, Set<String> visited, int currentDepth, int maxDepth) {
        if (visited.contains(nodeId) || currentDepth >= maxDepth) {
            return null;
        }

        visited.add(nodeId);
        LineageNode node = getNodeById(nodeId);
        if (node == null) {
            return null;
        }

        Map<String, Object> treeNode = new HashMap<>();
        treeNode.put("nodeId", nodeId);
        treeNode.put("nodeName", node.getNodeName());
        treeNode.put("nodeType", node.getNodeType());
        treeNode.put("depth", currentDepth);

        List<Map<String, Object>> children = new ArrayList<>();
        List<Map<String, Object>> downstreams = dataLineageMapper.getDownstreamLineages(nodeId);
        
        for (Map<String, Object> downstream : downstreams) {
            String childId = (String) downstream.get("target_id");
            Map<String, Object> childTree = buildNodeTree(childId, visited, currentDepth + 1, maxDepth);
            if (childTree != null) {
                children.add(childTree);
            }
        }

        treeNode.put("children", children);
        return treeNode;
    }

    private int calculateConnectionCount(String nodeId) {
        QueryWrapper<DataLineage> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", false)
               .and(w -> w.eq("source_id", nodeId).or().eq("target_id", nodeId));
        return Math.toIntExact(dataLineageMapper.selectCount(wrapper));
    }
}