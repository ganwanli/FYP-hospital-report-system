package com.hospital.report.demo;

import org.springframework.web.bind.annotation.*;
import lombok.Data;
import java.util.*;

@RestController
@RequestMapping("/api/lineage/demo")
@CrossOrigin(origins = "*")
public class LineageDemoController {

    // 模拟数据存储
    private static final List<NodeDemo> NODES = new ArrayList<>();
    private static final List<LineageDemo> LINEAGES = new ArrayList<>();

    static {
        // 初始化一些演示数据
        NODES.add(new NodeDemo("node1", "患者基础信息表", "TABLE", "patient_info", null, "HIS系统"));
        NODES.add(new NodeDemo("node2", "患者ID", "COLUMN", "patient_info", "patient_id", "HIS系统"));
        NODES.add(new NodeDemo("node3", "患者姓名", "COLUMN", "patient_info", "patient_name", "HIS系统"));
        NODES.add(new NodeDemo("node4", "住院记录表", "TABLE", "admission_record", null, "HIS系统"));
        NODES.add(new NodeDemo("node5", "住院ID", "COLUMN", "admission_record", "admission_id", "HIS系统"));
        NODES.add(new NodeDemo("node6", "患者统计报表", "VIEW", "patient_stats", null, "报表系统"));

        LINEAGES.add(new LineageDemo("lineage1", "node2", "node5", "DERIVED", "外键关联"));
        LINEAGES.add(new LineageDemo("lineage2", "node1", "node6", "AGGREGATE", "聚合统计"));
        LINEAGES.add(new LineageDemo("lineage3", "node4", "node6", "TRANSFORM", "数据转换"));
    }

    @GetMapping("/nodes")
    public List<NodeDemo> getNodes(@RequestParam(required = false) String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return NODES;
        }
        return NODES.stream()
                .filter(node -> node.getNodeName().contains(keyword) || 
                               (node.getTableName() != null && node.getTableName().contains(keyword)))
                .toList();
    }

    @PostMapping("/nodes")
    public NodeDemo createNode(@RequestBody NodeDemo node) {
        if (node.getNodeId() == null) {
            node.setNodeId("node" + (NODES.size() + 1));
        }
        NODES.add(node);
        return node;
    }

    @GetMapping("/nodes/statistics")
    public Map<String, Object> getNodeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNodes", NODES.size());
        stats.put("activeNodes", NODES.size());
        stats.put("recentlyAccessedNodes", 3);
        stats.put("ownershipRate", 85.5);
        return stats;
    }

    @GetMapping("/{nodeId}/graph")
    public Map<String, Object> getLineageGraph(@PathVariable String nodeId) {
        Map<String, Object> graph = new HashMap<>();
        
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        // 添加中心节点
        NodeDemo centerNode = NODES.stream()
                .filter(n -> n.getNodeId().equals(nodeId))
                .findFirst().orElse(null);
        
        if (centerNode != null) {
            nodes.add(buildNodeData(centerNode, 200, 200));
            
            // 添加相关的血缘关系
            int x = 100, y = 100;
            for (LineageDemo lineage : LINEAGES) {
                if (lineage.getSourceId().equals(nodeId) || lineage.getTargetId().equals(nodeId)) {
                    // 添加源节点
                    NodeDemo sourceNode = findNodeById(lineage.getSourceId());
                    if (sourceNode != null && nodes.stream().noneMatch(n -> n.get("id").equals(sourceNode.getNodeId()))) {
                        nodes.add(buildNodeData(sourceNode, x, y));
                        x += 150;
                    }
                    
                    // 添加目标节点
                    NodeDemo targetNode = findNodeById(lineage.getTargetId());
                    if (targetNode != null && nodes.stream().noneMatch(n -> n.get("id").equals(targetNode.getNodeId()))) {
                        nodes.add(buildNodeData(targetNode, x, y + 100));
                        x += 150;
                    }
                    
                    // 添加边
                    edges.add(buildEdgeData(lineage));
                }
            }
        }

        graph.put("nodes", nodes);
        graph.put("edges", edges);
        graph.put("metadata", Map.of(
                "nodeCount", nodes.size(),
                "edgeCount", edges.size(),
                "maxDepth", 3,
                "direction", "ALL"
        ));

        return graph;
    }

    @PostMapping("/{nodeId}/impact-analysis")
    public Map<String, Object> performImpactAnalysis(@PathVariable String nodeId,
                                                     @RequestParam String changeType) {
        Map<String, Object> analysis = new HashMap<>();
        
        // 模拟影响分析结果
        List<Map<String, Object>> downstreamImpacts = new ArrayList<>();
        List<Map<String, Object>> upstreamDependencies = new ArrayList<>();
        
        // 查找下游影响
        for (LineageDemo lineage : LINEAGES) {
            if (lineage.getSourceId().equals(nodeId)) {
                NodeDemo targetNode = findNodeById(lineage.getTargetId());
                if (targetNode != null) {
                    Map<String, Object> impact = new HashMap<>();
                    impact.put("nodeId", targetNode.getNodeId());
                    impact.put("level", 1);
                    impact.put("relationType", lineage.getRelationType());
                    impact.put("impactType", "DOWNSTREAM");
                    downstreamImpacts.add(impact);
                }
            }
        }
        
        // 查找上游依赖
        for (LineageDemo lineage : LINEAGES) {
            if (lineage.getTargetId().equals(nodeId)) {
                NodeDemo sourceNode = findNodeById(lineage.getSourceId());
                if (sourceNode != null) {
                    Map<String, Object> dependency = new HashMap<>();
                    dependency.put("nodeId", sourceNode.getNodeId());
                    dependency.put("level", 1);
                    dependency.put("relationType", lineage.getRelationType());
                    dependency.put("impactType", "UPSTREAM");
                    upstreamDependencies.add(dependency);
                }
            }
        }

        String riskLevel = calculateRiskLevel(downstreamImpacts.size() + upstreamDependencies.size(), changeType);
        List<String> recommendations = generateRecommendations(changeType, downstreamImpacts.size() + upstreamDependencies.size());

        analysis.put("analysisId", UUID.randomUUID().toString());
        analysis.put("sourceNodeId", nodeId);
        analysis.put("changeType", changeType);
        analysis.put("analysisDepth", 3);
        analysis.put("downstreamImpacts", downstreamImpacts);
        analysis.put("upstreamDependencies", upstreamDependencies);
        analysis.put("totalAffectedNodes", downstreamImpacts.size() + upstreamDependencies.size());
        analysis.put("riskLevel", riskLevel);
        analysis.put("recommendations", recommendations);
        analysis.put("executionTime", 125L);
        analysis.put("analysisTime", new Date());

        return analysis;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchLineage(@RequestParam String keyword) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (LineageDemo lineage : LINEAGES) {
            NodeDemo sourceNode = findNodeById(lineage.getSourceId());
            NodeDemo targetNode = findNodeById(lineage.getTargetId());
            
            if (sourceNode != null && targetNode != null) {
                if (sourceNode.getNodeName().contains(keyword) || 
                    targetNode.getNodeName().contains(keyword) ||
                    lineage.getRelationType().contains(keyword)) {
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", lineage.getLineageId());
                    result.put("sourceId", sourceNode.getNodeId());
                    result.put("targetId", targetNode.getNodeId());
                    result.put("sourceName", sourceNode.getNodeName());
                    result.put("targetName", targetNode.getNodeName());
                    result.put("relationType", lineage.getRelationType());
                    result.put("confidence", 0.85);
                    result.put("verified", true);
                    results.add(result);
                }
            }
        }
        
        return results;
    }

    private NodeDemo findNodeById(String nodeId) {
        return NODES.stream()
                .filter(node -> node.getNodeId().equals(nodeId))
                .findFirst().orElse(null);
    }

    private Map<String, Object> buildNodeData(NodeDemo node, int x, int y) {
        Map<String, Object> nodeData = new HashMap<>();
        nodeData.put("id", node.getNodeId());
        nodeData.put("label", node.getNodeName());
        nodeData.put("type", node.getNodeType());
        nodeData.put("position", Map.of("x", x, "y", y));
        nodeData.put("data", Map.of(
                "tableName", node.getTableName(),
                "columnName", node.getColumnName(),
                "system", node.getSystemSource()
        ));
        return nodeData;
    }

    private Map<String, Object> buildEdgeData(LineageDemo lineage) {
        Map<String, Object> edgeData = new HashMap<>();
        edgeData.put("id", lineage.getLineageId());
        edgeData.put("source", lineage.getSourceId());
        edgeData.put("target", lineage.getTargetId());
        edgeData.put("label", lineage.getRelationType());
        edgeData.put("type", lineage.getRelationType());
        edgeData.put("data", Map.of(
                "transformRule", lineage.getTransformRule(),
                "confidence", 0.85,
                "verified", true
        ));
        return edgeData;
    }

    private String calculateRiskLevel(int affectedNodeCount, String changeType) {
        if (affectedNodeCount >= 5 || "DELETE".equals(changeType)) {
            return "CRITICAL";
        } else if (affectedNodeCount >= 3 || "SCHEMA_CHANGE".equals(changeType)) {
            return "HIGH";
        } else if (affectedNodeCount >= 1) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    private List<String> generateRecommendations(String changeType, int affectedNodeCount) {
        List<String> recommendations = new ArrayList<>();
        
        if (affectedNodeCount > 2) {
            recommendations.add("建议在非业务高峰期进行变更");
            recommendations.add("提前通知相关数据使用方");
        }
        
        if ("DELETE".equals(changeType)) {
            recommendations.add("确认所有下游依赖已迁移或删除");
            recommendations.add("备份相关数据");
        }
        
        recommendations.add("执行完整的回归测试");
        recommendations.add("准备回滚方案");
        
        return recommendations;
    }

    @Data
    public static class NodeDemo {
        private String nodeId;
        private String nodeName;
        private String nodeType;
        private String tableName;
        private String columnName;
        private String systemSource;

        public NodeDemo() {}

        public NodeDemo(String nodeId, String nodeName, String nodeType, String tableName, String columnName, String systemSource) {
            this.nodeId = nodeId;
            this.nodeName = nodeName;
            this.nodeType = nodeType;
            this.tableName = tableName;
            this.columnName = columnName;
            this.systemSource = systemSource;
        }
    }

    @Data
    public static class LineageDemo {
        private String lineageId;
        private String sourceId;
        private String targetId;
        private String relationType;
        private String transformRule;

        public LineageDemo() {}

        public LineageDemo(String lineageId, String sourceId, String targetId, String relationType, String transformRule) {
            this.lineageId = lineageId;
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.relationType = relationType;
            this.transformRule = transformRule;
        }
    }
}