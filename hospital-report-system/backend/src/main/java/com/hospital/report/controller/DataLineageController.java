package com.hospital.report.controller;

import com.hospital.report.entity.LineageNode;
import com.hospital.report.service.DataLineageService;
import com.hospital.report.service.LineageNodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/lineage")
@RequiredArgsConstructor
public class DataLineageController {

    private final DataLineageService dataLineageService;
    private final LineageNodeService lineageNodeService;

    @GetMapping("/nodes")
    public ResponseEntity<List<LineageNode>> searchNodes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String nodeType,
            @RequestParam(required = false) String systemSource) {
        List<LineageNode> nodes = lineageNodeService.searchNodes(keyword, nodeType, systemSource);
        return ResponseEntity.ok(nodes);
    }

    @PostMapping("/nodes")
    public ResponseEntity<LineageNode> createOrUpdateNode(@RequestBody LineageNode node) {
        LineageNode result = lineageNodeService.createOrUpdateNode(node);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/nodes/{nodeId}")
    public ResponseEntity<Map<String, Object>> getNodeDetail(@PathVariable String nodeId) {
        Map<String, Object> detail = lineageNodeService.getNodeDetail(nodeId);
        return ResponseEntity.ok(detail);
    }

    @PutMapping("/nodes/{nodeId}/position")
    public ResponseEntity<Void> updateNodePosition(
            @PathVariable String nodeId,
            @RequestParam Double positionX,
            @RequestParam Double positionY) {
        lineageNodeService.updateNodePosition(nodeId, positionX, positionY);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/nodes/statistics")
    public ResponseEntity<Map<String, Object>> getNodeStatistics() {
        Map<String, Object> stats = lineageNodeService.getNodeStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/nodes/categories")
    public ResponseEntity<List<Map<String, Object>>> getNodeCategoryStats() {
        List<Map<String, Object>> stats = lineageNodeService.getNodeCategoryStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/nodes/critical")
    public ResponseEntity<List<LineageNode>> getCriticalNodes(
            @RequestParam(defaultValue = "20") Integer limit) {
        List<LineageNode> nodes = lineageNodeService.getCriticalNodes(limit);
        return ResponseEntity.ok(nodes);
    }

    @GetMapping("/nodes/{nodeId}/health")
    public ResponseEntity<Map<String, Object>> checkNodeHealth(@PathVariable String nodeId) {
        Map<String, Object> health = lineageNodeService.checkNodeHealth(nodeId);
        return ResponseEntity.ok(health);
    }

    @PostMapping("/nodes/import")
    public ResponseEntity<Map<String, Object>> importNodes(@RequestBody List<Map<String, Object>> nodes) {
        Map<String, Object> result = lineageNodeService.importNodes(nodes);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/nodes/discover")
    public ResponseEntity<List<LineageNode>> discoverNodes(
            @RequestParam String systemSource,
            @RequestBody Map<String, Object> config) {
        List<LineageNode> nodes = lineageNodeService.discoverAndRegisterNodes(systemSource, config);
        return ResponseEntity.ok(nodes);
    }

    @GetMapping("/nodes/{nodeId}/hierarchy")
    public ResponseEntity<Map<String, Object>> getNodeHierarchy(@PathVariable String nodeId) {
        Map<String, Object> hierarchy = lineageNodeService.getNodeHierarchy(nodeId);
        return ResponseEntity.ok(hierarchy);
    }

    @PutMapping("/nodes/{nodeId}/metadata")
    public ResponseEntity<Void> syncNodeMetadata(
            @PathVariable String nodeId,
            @RequestBody Map<String, Object> metadata) {
        lineageNodeService.syncNodeMetadata(nodeId, metadata);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{nodeId}")
    public ResponseEntity<Map<String, Object>> getNodeLineage(
            @PathVariable String nodeId,
            @RequestParam(defaultValue = "5") Integer maxDepth,
            @RequestParam(defaultValue = "ALL") String direction) {
        Map<String, Object> lineage = dataLineageService.getNodeLineage(nodeId, maxDepth, direction);
        return ResponseEntity.ok(lineage);
    }

    @GetMapping("/{nodeId}/graph")
    public ResponseEntity<Map<String, Object>> getLineageGraph(
            @PathVariable String nodeId,
            @RequestParam(defaultValue = "5") Integer maxDepth,
            @RequestParam(defaultValue = "ALL") String direction) {
        Map<String, Object> graph = dataLineageService.getLineageGraph(nodeId, maxDepth, direction);
        return ResponseEntity.ok(graph);
    }

    @PostMapping("/relations")
    public ResponseEntity<Void> buildLineageRelation(
            @RequestParam String sourceNodeId,
            @RequestParam String targetNodeId,
            @RequestParam String relationType,
            @RequestParam(required = false) String transformRule,
            @RequestBody(required = false) Map<String, Object> metadata) {
        dataLineageService.buildLineageRelation(sourceNodeId, targetNodeId, relationType, transformRule, metadata);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{nodeId}/impact-analysis")
    public ResponseEntity<Map<String, Object>> performImpactAnalysis(
            @PathVariable String nodeId,
            @RequestParam String changeType,
            @RequestParam(defaultValue = "3") Integer analysisDepth) {
        Map<String, Object> analysis = dataLineageService.performImpactAnalysis(nodeId, changeType, analysisDepth);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchLineage(
            @RequestParam String keyword,
            @RequestParam(required = false) String nodeType,
            @RequestParam(required = false) String relationType) {
        List<Map<String, Object>> results = dataLineageService.searchLineage(keyword, nodeType, relationType);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getLineageStatistics() {
        Map<String, Object> stats = dataLineageService.getLineageStatistics();
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/relations/{lineageId}/verify")
    public ResponseEntity<Void> verifyLineageRelation(
            @PathVariable Long lineageId,
            @RequestParam String verificationStatus,
            @RequestParam(required = false) String verificationComment) {
        dataLineageService.verifyLineageRelation(lineageId, verificationStatus, verificationComment);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/discover")
    public ResponseEntity<List<Map<String, Object>>> discoverLineageRelations(
            @RequestParam String systemSource,
            @RequestParam String discoveryMethod) {
        List<Map<String, Object>> relations = dataLineageService.discoverLineageRelations(systemSource, discoveryMethod);
        return ResponseEntity.ok(relations);
    }

    @GetMapping("/{nodeId}/data-flow")
    public ResponseEntity<Map<String, Object>> getDataFlowAnalysis(@PathVariable String nodeId) {
        Map<String, Object> analysis = dataLineageService.getDataFlowAnalysis(nodeId);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/path")
    public ResponseEntity<List<Map<String, Object>>> getLineagePath(
            @RequestParam String sourceNodeId,
            @RequestParam String targetNodeId) {
        List<Map<String, Object>> path = dataLineageService.getLineagePath(sourceNodeId, targetNodeId);
        return ResponseEntity.ok(path);
    }

    @PostMapping("/relations/import")
    public ResponseEntity<Map<String, Object>> importLineageRelations(
            @RequestBody List<Map<String, Object>> relations) {
        Map<String, Object> result = dataLineageService.importLineageRelations(relations);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{nodeId}/export")
    public ResponseEntity<byte[]> exportLineageRelations(
            @PathVariable String nodeId,
            @RequestParam(defaultValue = "JSON") String format) {
        byte[] data = dataLineageService.exportLineageRelations(nodeId, format);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=lineage_" + nodeId + ".json")
                .body(data);
    }

    @GetMapping("/circular-dependencies")
    public ResponseEntity<List<Map<String, Object>>> detectCircularDependencies() {
        List<Map<String, Object>> circularDeps = dataLineageService.detectCircularDependencies();
        return ResponseEntity.ok(circularDeps);
    }

    @GetMapping("/orphan-nodes")
    public ResponseEntity<List<LineageNode>> getOrphanNodes() {
        List<LineageNode> orphanNodes = dataLineageService.getOrphanNodes();
        return ResponseEntity.ok(orphanNodes);
    }

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, Object>> performLineageHealthCheck() {
        Map<String, Object> healthCheck = dataLineageService.performLineageHealthCheck();
        return ResponseEntity.ok(healthCheck);
    }
}