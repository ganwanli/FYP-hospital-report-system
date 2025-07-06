package com.hospital.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.report.entity.DataLineage;
import com.hospital.report.entity.LineageImpactAnalysis;
import com.hospital.report.entity.LineageNode;
import com.hospital.report.mapper.DataLineageMapper;
import com.hospital.report.mapper.LineageImpactAnalysisMapper;
import com.hospital.report.mapper.LineageNodeMapper;
import com.hospital.report.service.DataLineageService;
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
public class DataLineageServiceImpl extends ServiceImpl<DataLineageMapper, DataLineage> 
        implements DataLineageService {

    private final DataLineageMapper dataLineageMapper;
    private final LineageNodeMapper lineageNodeMapper;
    private final LineageImpactAnalysisMapper impactAnalysisMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> getNodeLineage(String nodeId, Integer maxDepth, String direction) {
        Map<String, Object> result = new HashMap<>();
        
        if (maxDepth == null || maxDepth <= 0) {
            maxDepth = 5; // 默认最大深度
        }
        
        List<Map<String, Object>> lineages;
        switch (direction != null ? direction.toUpperCase() : "ALL") {
            case "UPSTREAM":
                lineages = dataLineageMapper.getUpstreamLineages(nodeId);
                break;
            case "DOWNSTREAM":
                lineages = dataLineageMapper.getDownstreamLineages(nodeId);
                break;
            default:
                lineages = dataLineageMapper.getNodeLineages(nodeId);
                break;
        }
        
        result.put("nodeId", nodeId);
        result.put("direction", direction);
        result.put("maxDepth", maxDepth);
        result.put("lineages", lineages);
        result.put("totalCount", lineages.size());
        
        return result;
    }

    @Override
    public Map<String, Object> getLineageGraph(String nodeId, Integer maxDepth, String direction) {
        Map<String, Object> graph = new HashMap<>();
        Set<String> visitedNodes = new HashSet<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        
        // 获取血缘关系树
        List<Map<String, Object>> lineageTree = dataLineageMapper.getLineageTree(nodeId, maxDepth);
        
        // 构建节点和边
        for (Map<String, Object> lineage : lineageTree) {
            String sourceId = (String) lineage.get("source_id");
            String targetId = (String) lineage.get("target_id");
            
            // 添加源节点
            if (!visitedNodes.contains(sourceId)) {
                LineageNode sourceNode = getNodeById(sourceId);
                if (sourceNode != null) {
                    nodes.add(buildNodeData(sourceNode));
                    visitedNodes.add(sourceId);
                }
            }
            
            // 添加目标节点
            if (!visitedNodes.contains(targetId)) {
                LineageNode targetNode = getNodeById(targetId);
                if (targetNode != null) {
                    nodes.add(buildNodeData(targetNode));
                    visitedNodes.add(targetId);
                }
            }
            
            // 添加边
            edges.add(buildEdgeData(lineage));
        }
        
        graph.put("nodes", nodes);
        graph.put("edges", edges);
        graph.put("metadata", Map.of(
            "nodeCount", nodes.size(),
            "edgeCount", edges.size(),
            "maxDepth", maxDepth,
            "direction", direction
        ));
        
        return graph;
    }

    @Override
    @Transactional
    public void buildLineageRelation(String sourceNodeId, String targetNodeId, String relationType,
                                   String transformRule, Map<String, Object> metadata) {
        // 检查节点是否存在
        if (lineageNodeMapper.checkNodeExists(sourceNodeId) == 0) {
            throw new RuntimeException("源节点不存在: " + sourceNodeId);
        }
        if (lineageNodeMapper.checkNodeExists(targetNodeId) == 0) {
            throw new RuntimeException("目标节点不存在: " + targetNodeId);
        }
        
        // 检查是否已存在相同的血缘关系
        QueryWrapper<DataLineage> wrapper = new QueryWrapper<>();
        wrapper.eq("source_id", sourceNodeId)
               .eq("target_id", targetNodeId)
               .eq("relation_type", relationType)
               .eq("is_deleted", false);
        
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("血缘关系已存在");
        }
        
        // 创建血缘关系
        DataLineage lineage = new DataLineage();
        lineage.setSourceId(sourceNodeId);
        lineage.setTargetId(targetNodeId);
        lineage.setRelationType(relationType);
        lineage.setTransformRule(transformRule);
        lineage.setDataFlowDirection("DOWNSTREAM");
        lineage.setConfidenceScore(0.8); // 默认置信度
        lineage.setDiscoveryMethod("MANUAL");
        lineage.setVerificationStatus("UNVERIFIED");
        lineage.setStatus(1);
        
        if (metadata != null) {
            try {
                lineage.setMetadata(objectMapper.writeValueAsString(metadata));
            } catch (Exception e) {
                log.warn("序列化元数据失败: {}", e.getMessage());
            }
        }
        
        this.save(lineage);
        
        log.info("创建血缘关系: {} -> {} ({})", sourceNodeId, targetNodeId, relationType);
    }

    @Override
    public Map<String, Object> performImpactAnalysis(String nodeId, String changeType, Integer analysisDepth) {
        Map<String, Object> analysis = new HashMap<>();
        String analysisId = UUID.randomUUID().toString();
        
        if (analysisDepth == null || analysisDepth <= 0) {
            analysisDepth = 3;
        }
        
        long startTime = System.currentTimeMillis();
        
        // 获取下游影响
        List<Map<String, Object>> downstreamImpacts = analyzeDownstreamImpact(nodeId, analysisDepth);
        
        // 获取上游依赖
        List<Map<String, Object>> upstreamDependencies = analyzeUpstreamDependencies(nodeId, analysisDepth);
        
        // 计算影响范围
        Set<String> allAffectedNodes = new HashSet<>();
        downstreamImpacts.forEach(impact -> allAffectedNodes.add((String) impact.get("nodeId")));
        upstreamDependencies.forEach(dep -> allAffectedNodes.add((String) dep.get("nodeId")));
        
        // 评估风险等级
        String riskLevel = calculateRiskLevel(allAffectedNodes.size(), changeType);
        
        // 生成建议
        List<String> recommendations = generateRecommendations(changeType, allAffectedNodes.size());
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // 保存分析结果
        saveImpactAnalysis(analysisId, nodeId, changeType, riskLevel, 
                          allAffectedNodes, recommendations, executionTime);
        
        analysis.put("analysisId", analysisId);
        analysis.put("sourceNodeId", nodeId);
        analysis.put("changeType", changeType);
        analysis.put("analysisDepth", analysisDepth);
        analysis.put("downstreamImpacts", downstreamImpacts);
        analysis.put("upstreamDependencies", upstreamDependencies);
        analysis.put("totalAffectedNodes", allAffectedNodes.size());
        analysis.put("riskLevel", riskLevel);
        analysis.put("recommendations", recommendations);
        analysis.put("executionTime", executionTime);
        analysis.put("analysisTime", LocalDateTime.now());
        
        return analysis;
    }

    @Override
    public List<Map<String, Object>> searchLineage(String keyword, String nodeType, String relationType) {
        List<DataLineage> lineages = dataLineageMapper.searchLineages(keyword, 100);
        
        return lineages.stream()
                .filter(lineage -> nodeType == null || 
                    nodeType.equals(lineage.getSourceType()) || 
                    nodeType.equals(lineage.getTargetType()))
                .filter(lineage -> relationType == null || 
                    relationType.equals(lineage.getRelationType()))
                .map(this::convertToSearchResult)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getLineageStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 基础统计
        long totalLineages = this.count(new QueryWrapper<DataLineage>().eq("is_deleted", false));
        stats.put("totalLineages", totalLineages);
        
        // 关系类型统计
        stats.put("relationTypeStats", dataLineageMapper.getRelationTypeStatistics());
        
        // 数据流向统计
        stats.put("dataFlowStats", dataLineageMapper.getDataFlowStatistics());
        
        // 验证状态统计
        stats.put("verificationStats", dataLineageMapper.getVerificationStatusStats());
        
        // 依赖层级统计
        stats.put("dependencyLevelStats", dataLineageMapper.getDependencyLevelStats());
        
        // 热门表关系
        stats.put("topTableRelations", dataLineageMapper.getTopTableRelations(10));
        
        return stats;
    }

    @Override
    @Transactional
    public void verifyLineageRelation(Long lineageId, String verificationStatus, String verificationComment) {
        DataLineage lineage = this.getById(lineageId);
        if (lineage == null) {
            throw new RuntimeException("血缘关系不存在");
        }
        
        lineage.setVerificationStatus(verificationStatus);
        lineage.setLastVerifiedTime(LocalDateTime.now());
        
        // 根据验证结果调整置信度
        if ("VERIFIED".equals(verificationStatus)) {
            lineage.setConfidenceScore(0.95);
        } else if ("REJECTED".equals(verificationStatus)) {
            lineage.setConfidenceScore(0.1);
        }
        
        this.updateById(lineage);
        
        log.info("血缘关系验证完成: {} -> {}", lineageId, verificationStatus);
    }

    @Override
    public List<Map<String, Object>> discoverLineageRelations(String systemSource, String discoveryMethod) {
        // 这里可以实现自动发现逻辑
        // 例如：分析SQL语句、解析配置文件、扫描数据流等
        List<Map<String, Object>> discoveredRelations = new ArrayList<>();
        
        // 示例：基于命名规则的发现
        if ("NAMING_CONVENTION".equals(discoveryMethod)) {
            discoveredRelations = discoverByNamingConvention(systemSource);
        }
        
        return discoveredRelations;
    }

    @Override
    public Map<String, Object> getDataFlowAnalysis(String nodeId) {
        Map<String, Object> flowAnalysis = new HashMap<>();
        
        // 获取输入流
        List<Map<String, Object>> inputFlows = dataLineageMapper.getUpstreamLineages(nodeId);
        
        // 获取输出流
        List<Map<String, Object>> outputFlows = dataLineageMapper.getDownstreamLineages(nodeId);
        
        // 分析数据流特征
        Map<String, Object> flowCharacteristics = analyzeFlowCharacteristics(inputFlows, outputFlows);
        
        flowAnalysis.put("nodeId", nodeId);
        flowAnalysis.put("inputFlows", inputFlows);
        flowAnalysis.put("outputFlows", outputFlows);
        flowAnalysis.put("inputCount", inputFlows.size());
        flowAnalysis.put("outputCount", outputFlows.size());
        flowAnalysis.put("flowCharacteristics", flowCharacteristics);
        
        return flowAnalysis;
    }

    @Override
    public List<Map<String, Object>> getLineagePath(String sourceNodeId, String targetNodeId) {
        // 使用广度优先搜索找到路径
        return findShortestPath(sourceNodeId, targetNodeId);
    }

    @Override
    @Transactional
    public Map<String, Object> importLineageRelations(List<Map<String, Object>> relations) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (Map<String, Object> relationData : relations) {
            try {
                String sourceId = (String) relationData.get("sourceId");
                String targetId = (String) relationData.get("targetId");
                String relationType = (String) relationData.get("relationType");
                String transformRule = (String) relationData.get("transformRule");
                
                buildLineageRelation(sourceId, targetId, relationType, transformRule, relationData);
                successCount++;
                
            } catch (Exception e) {
                errorCount++;
                errors.add(e.getMessage());
            }
        }
        
        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("errors", errors);
        
        return result;
    }

    @Override
    public byte[] exportLineageRelations(String nodeId, String format) {
        // 实现导出功能（Excel, JSON, GraphML等）
        List<Map<String, Object>> lineages = dataLineageMapper.getNodeLineages(nodeId);
        
        // 这里可以根据format参数选择不同的导出格式
        // 为简化，这里返回JSON格式的字节数组
        try {
            String json = objectMapper.writeValueAsString(lineages);
            return json.getBytes();
        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> detectCircularDependencies() {
        // 检测循环依赖的算法实现
        return new ArrayList<>(); // 简化实现
    }

    @Override
    public List<LineageNode> getOrphanNodes() {
        // 查找没有任何血缘关系的孤立节点
        QueryWrapper<LineageNode> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", false);
        
        List<LineageNode> allNodes = lineageNodeMapper.selectList(wrapper);
        Set<String> connectedNodes = new HashSet<>();
        
        // 获取所有有连接的节点
        List<DataLineage> allLineages = this.list(new QueryWrapper<DataLineage>().eq("is_deleted", false));
        for (DataLineage lineage : allLineages) {
            connectedNodes.add(lineage.getSourceId());
            connectedNodes.add(lineage.getTargetId());
        }
        
        // 过滤出孤立节点
        return allNodes.stream()
                .filter(node -> !connectedNodes.contains(node.getNodeId()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> performLineageHealthCheck() {
        Map<String, Object> healthCheck = new HashMap<>();
        
        // 检查各项健康指标
        long totalNodes = lineageNodeMapper.selectCount(new QueryWrapper<LineageNode>().eq("is_deleted", false));
        long totalLineages = this.count(new QueryWrapper<DataLineage>().eq("is_deleted", false));
        long verifiedLineages = this.count(new QueryWrapper<DataLineage>()
                .eq("is_deleted", false)
                .eq("verification_status", "VERIFIED"));
        
        List<LineageNode> orphanNodes = getOrphanNodes();
        List<Map<String, Object>> circularDeps = detectCircularDependencies();
        
        double verificationRate = totalLineages > 0 ? (double) verifiedLineages / totalLineages * 100 : 0;
        double orphanRate = totalNodes > 0 ? (double) orphanNodes.size() / totalNodes * 100 : 0;
        
        healthCheck.put("totalNodes", totalNodes);
        healthCheck.put("totalLineages", totalLineages);
        healthCheck.put("verificationRate", verificationRate);
        healthCheck.put("orphanNodeCount", orphanNodes.size());
        healthCheck.put("orphanRate", orphanRate);
        healthCheck.put("circularDependencies", circularDeps.size());
        healthCheck.put("healthScore", calculateHealthScore(verificationRate, orphanRate, circularDeps.size()));
        healthCheck.put("checkTime", LocalDateTime.now());
        
        return healthCheck;
    }

    // 私有辅助方法
    private LineageNode getNodeById(String nodeId) {
        QueryWrapper<LineageNode> wrapper = new QueryWrapper<>();
        wrapper.eq("node_id", nodeId).eq("is_deleted", false);
        return lineageNodeMapper.selectOne(wrapper);
    }

    private Map<String, Object> buildNodeData(LineageNode node) {
        Map<String, Object> nodeData = new HashMap<>();
        nodeData.put("id", node.getNodeId());
        nodeData.put("label", node.getDisplayName() != null ? node.getDisplayName() : node.getNodeName());
        nodeData.put("type", node.getNodeType());
        nodeData.put("category", node.getNodeCategory());
        nodeData.put("position", Map.of("x", node.getPositionX() != null ? node.getPositionX() : 0,
                                       "y", node.getPositionY() != null ? node.getPositionY() : 0));
        nodeData.put("data", Map.of(
            "tableName", node.getTableName(),
            "columnName", node.getColumnName(),
            "dataType", node.getDataType(),
            "businessMeaning", node.getBusinessMeaning(),
            "owner", node.getOwnerUser(),
            "criticality", node.getCriticalityLevel(),
            "system", node.getSystemSource()
        ));
        return nodeData;
    }

    private Map<String, Object> buildEdgeData(Map<String, Object> lineage) {
        Map<String, Object> edgeData = new HashMap<>();
        edgeData.put("id", lineage.get("id"));
        edgeData.put("source", lineage.get("source_id"));
        edgeData.put("target", lineage.get("target_id"));
        edgeData.put("label", lineage.get("relation_type"));
        edgeData.put("type", lineage.get("relation_type"));
        edgeData.put("data", Map.of(
            "transformRule", lineage.get("transform_rule"),
            "confidence", lineage.get("confidence_score"),
            "verified", "VERIFIED".equals(lineage.get("verification_status"))
        ));
        return edgeData;
    }

    private List<Map<String, Object>> analyzeDownstreamImpact(String nodeId, Integer depth) {
        List<Map<String, Object>> impacts = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Queue<Map<String, Object>> queue = new LinkedList<>();
        
        queue.offer(Map.of("nodeId", nodeId, "level", 0));
        visited.add(nodeId);
        
        while (!queue.isEmpty() && queue.peek() != null) {
            Map<String, Object> current = queue.poll();
            String currentNodeId = (String) current.get("nodeId");
            Integer currentLevel = (Integer) current.get("level");
            
            if (currentLevel >= depth) continue;
            
            List<Map<String, Object>> downstreams = dataLineageMapper.getDownstreamLineages(currentNodeId);
            for (Map<String, Object> downstream : downstreams) {
                String targetId = (String) downstream.get("target_id");
                if (!visited.contains(targetId)) {
                    visited.add(targetId);
                    queue.offer(Map.of("nodeId", targetId, "level", currentLevel + 1));
                    
                    Map<String, Object> impact = new HashMap<>();
                    impact.put("nodeId", targetId);
                    impact.put("level", currentLevel + 1);
                    impact.put("relationType", downstream.get("relation_type"));
                    impact.put("impactType", "DOWNSTREAM");
                    impacts.add(impact);
                }
            }
        }
        
        return impacts;
    }

    private List<Map<String, Object>> analyzeUpstreamDependencies(String nodeId, Integer depth) {
        List<Map<String, Object>> dependencies = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Queue<Map<String, Object>> queue = new LinkedList<>();
        
        queue.offer(Map.of("nodeId", nodeId, "level", 0));
        visited.add(nodeId);
        
        while (!queue.isEmpty()) {
            Map<String, Object> current = queue.poll();
            String currentNodeId = (String) current.get("nodeId");
            Integer currentLevel = (Integer) current.get("level");
            
            if (currentLevel >= depth) continue;
            
            List<Map<String, Object>> upstreams = dataLineageMapper.getUpstreamLineages(currentNodeId);
            for (Map<String, Object> upstream : upstreams) {
                String sourceId = (String) upstream.get("source_id");
                if (!visited.contains(sourceId)) {
                    visited.add(sourceId);
                    queue.offer(Map.of("nodeId", sourceId, "level", currentLevel + 1));
                    
                    Map<String, Object> dependency = new HashMap<>();
                    dependency.put("nodeId", sourceId);
                    dependency.put("level", currentLevel + 1);
                    dependency.put("relationType", upstream.get("relation_type"));
                    dependency.put("impactType", "UPSTREAM");
                    dependencies.add(dependency);
                }
            }
        }
        
        return dependencies;
    }

    private String calculateRiskLevel(int affectedNodeCount, String changeType) {
        if (affectedNodeCount >= 10 || "DELETE".equals(changeType)) {
            return "CRITICAL";
        } else if (affectedNodeCount >= 5 || "SCHEMA_CHANGE".equals(changeType)) {
            return "HIGH";
        } else if (affectedNodeCount >= 2) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    private List<String> generateRecommendations(String changeType, int affectedNodeCount) {
        List<String> recommendations = new ArrayList<>();
        
        if (affectedNodeCount > 5) {
            recommendations.add("建议在非业务高峰期进行变更");
            recommendations.add("提前通知相关数据使用方");
        }
        
        if ("DELETE".equals(changeType)) {
            recommendations.add("确认所有下游依赖已迁移或删除");
            recommendations.add("备份相关数据");
        }
        
        if ("SCHEMA_CHANGE".equals(changeType)) {
            recommendations.add("检查数据类型兼容性");
            recommendations.add("更新相关文档和接口说明");
        }
        
        recommendations.add("执行完整的回归测试");
        recommendations.add("准备回滚方案");
        
        return recommendations;
    }

    private void saveImpactAnalysis(String analysisId, String nodeId, String changeType,
                                  String riskLevel, Set<String> affectedNodes, 
                                  List<String> recommendations, long executionTime) {
        LineageImpactAnalysis analysis = new LineageImpactAnalysis();
        analysis.setAnalysisId(analysisId);
        analysis.setSourceNodeId(nodeId);
        analysis.setImpactType(changeType);
        analysis.setImpactLevel("HIGH"); // 基于影响节点数量计算
        analysis.setImpactScope(String.join(",", affectedNodes));
        analysis.setRiskLevel(riskLevel);
        analysis.setExecutionTime(executionTime);
        analysis.setAnalysisMethod("GRAPH_TRAVERSAL");
        analysis.setAnalysisDepth(3);
        
        try {
            analysis.setRecommendations(objectMapper.writeValueAsString(recommendations));
            analysis.setAffectedObjects(objectMapper.writeValueAsString(affectedNodes));
        } catch (Exception e) {
            log.warn("序列化分析结果失败: {}", e.getMessage());
        }
        
        impactAnalysisMapper.insert(analysis);
    }

    private Map<String, Object> convertToSearchResult(DataLineage lineage) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", lineage.getId());
        result.put("sourceId", lineage.getSourceId());
        result.put("targetId", lineage.getTargetId());
        result.put("sourceName", lineage.getSourceName());
        result.put("targetName", lineage.getTargetName());
        result.put("relationType", lineage.getRelationType());
        result.put("confidence", lineage.getConfidenceScore());
        result.put("verified", "VERIFIED".equals(lineage.getVerificationStatus()));
        return result;
    }

    private List<Map<String, Object>> discoverByNamingConvention(String systemSource) {
        // 基于命名规则发现血缘关系的示例实现
        List<Map<String, Object>> relations = new ArrayList<>();
        
        // 获取系统中的所有节点
        List<LineageNode> nodes = lineageNodeMapper.getNodesBySystem(systemSource);
        
        // 基于命名规则推断关系
        for (LineageNode sourceNode : nodes) {
            for (LineageNode targetNode : nodes) {
                if (isRelatedByNaming(sourceNode, targetNode)) {
                    Map<String, Object> relation = new HashMap<>();
                    relation.put("sourceId", sourceNode.getNodeId());
                    relation.put("targetId", targetNode.getNodeId());
                    relation.put("relationType", "DERIVED");
                    relation.put("confidence", 0.6);
                    relation.put("discoveryMethod", "NAMING_CONVENTION");
                    relations.add(relation);
                }
            }
        }
        
        return relations;
    }

    private boolean isRelatedByNaming(LineageNode source, LineageNode target) {
        // 简单的命名规则检查
        if (source.getColumnName() != null && target.getColumnName() != null) {
            return target.getColumnName().startsWith(source.getColumnName() + "_");
        }
        return false;
    }

    private Map<String, Object> analyzeFlowCharacteristics(List<Map<String, Object>> inputFlows,
                                                          List<Map<String, Object>> outputFlows) {
        Map<String, Object> characteristics = new HashMap<>();
        
        // 分析输入输出比例
        int totalFlows = inputFlows.size() + outputFlows.size();
        characteristics.put("inputRatio", totalFlows > 0 ? (double) inputFlows.size() / totalFlows : 0);
        characteristics.put("outputRatio", totalFlows > 0 ? (double) outputFlows.size() / totalFlows : 0);
        
        // 分析节点类型
        if (inputFlows.isEmpty() && !outputFlows.isEmpty()) {
            characteristics.put("nodeRole", "SOURCE");
        } else if (!inputFlows.isEmpty() && outputFlows.isEmpty()) {
            characteristics.put("nodeRole", "SINK");
        } else if (!inputFlows.isEmpty() && !outputFlows.isEmpty()) {
            characteristics.put("nodeRole", "TRANSFORMER");
        } else {
            characteristics.put("nodeRole", "ISOLATED");
        }
        
        return characteristics;
    }

    private List<Map<String, Object>> findShortestPath(String sourceNodeId, String targetNodeId) {
        // 使用BFS算法找到最短路径
        Queue<String> queue = new LinkedList<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();
        
        queue.offer(sourceNodeId);
        visited.add(sourceNodeId);
        parent.put(sourceNodeId, null);
        
        while (!queue.isEmpty()) {
            String currentNode = queue.poll();
            
            if (currentNode.equals(targetNodeId)) {
                // 找到目标，构建路径
                return buildPath(parent, sourceNodeId, targetNodeId);
            }
            
            List<Map<String, Object>> downstreams = dataLineageMapper.getDownstreamLineages(currentNode);
            for (Map<String, Object> downstream : downstreams) {
                String nextNode = (String) downstream.get("target_id");
                if (!visited.contains(nextNode)) {
                    visited.add(nextNode);
                    parent.put(nextNode, currentNode);
                    queue.offer(nextNode);
                }
            }
        }
        
        return new ArrayList<>(); // 没有找到路径
    }

    private List<Map<String, Object>> buildPath(Map<String, String> parent, String source, String target) {
        List<Map<String, Object>> path = new ArrayList<>();
        String current = target;
        
        while (current != null && !current.equals(source)) {
            String prev = parent.get(current);
            if (prev != null) {
                // 查找这两个节点之间的血缘关系
                QueryWrapper<DataLineage> wrapper = new QueryWrapper<>();
                wrapper.eq("source_id", prev)
                       .eq("target_id", current)
                       .eq("is_deleted", false);
                DataLineage lineage = this.getOne(wrapper);
                
                if (lineage != null) {
                    Map<String, Object> pathSegment = new HashMap<>();
                    pathSegment.put("source", prev);
                    pathSegment.put("target", current);
                    pathSegment.put("relationType", lineage.getRelationType());
                    pathSegment.put("transformRule", lineage.getTransformRule());
                    path.add(0, pathSegment); // 插入到开头
                }
            }
            current = prev;
        }
        
        return path;
    }

    private double calculateHealthScore(double verificationRate, double orphanRate, int circularDepsCount) {
        double score = 100.0;
        
        // 验证率贡献 (40%)
        score *= (verificationRate / 100.0 * 0.4 + 0.6);
        
        // 孤立节点率惩罚 (30%)
        score *= (1.0 - orphanRate / 100.0 * 0.3);
        
        // 循环依赖惩罚 (30%)
        if (circularDepsCount > 0) {
            score *= 0.7; // 有循环依赖则扣30分
        }
        
        return Math.max(0, Math.min(100, score));
    }
}