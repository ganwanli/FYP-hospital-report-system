package com.hospital.report.executor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class PerformanceMonitor {

    private final Map<String, PerformanceMetrics> activeMetrics = new ConcurrentHashMap<>();
    private final Map<String, List<PerformanceSnapshot>> historicalMetrics = new ConcurrentHashMap<>();
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private final AtomicLong executionCounter = new AtomicLong(0);

    public PerformanceMetrics startMonitoring() {
        String metricsId = "metrics_" + System.currentTimeMillis() + "_" + executionCounter.incrementAndGet();
        
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setMetricsId(metricsId);
        metrics.setStartTime(LocalDateTime.now());
        metrics.setStartMemoryUsage(getMemoryUsage());
        metrics.setStartCpuUsage(getCpuUsage());
        
        activeMetrics.put(metricsId, metrics);
        
        return metrics;
    }

    public void stopMonitoring(PerformanceMetrics metrics) {
        if (metrics == null || metrics.getMetricsId() == null) {
            return;
        }

        LocalDateTime endTime = LocalDateTime.now();
        long executionTime = ChronoUnit.MILLIS.between(metrics.getStartTime(), endTime);
        long endMemoryUsage = getMemoryUsage();
        double endCpuUsage = getCpuUsage();

        metrics.setEndTime(endTime);
        metrics.setExecutionTime(executionTime);
        metrics.setEndMemoryUsage(endMemoryUsage);
        metrics.setEndCpuUsage(endCpuUsage);
        metrics.setMemoryUsage(Math.max(0, endMemoryUsage - metrics.getStartMemoryUsage()));
        metrics.setCpuUsage(Math.max(0, endCpuUsage - metrics.getStartCpuUsage()));

        activeMetrics.remove(metrics.getMetricsId());
        
        // Store historical data
        PerformanceSnapshot snapshot = new PerformanceSnapshot();
        snapshot.setExecutionTime(executionTime);
        snapshot.setMemoryUsage(metrics.getMemoryUsage());
        snapshot.setCpuUsage(metrics.getCpuUsage());
        snapshot.setTimestamp(endTime);
        
        historicalMetrics.computeIfAbsent("global", k -> new ArrayList<>()).add(snapshot);
        
        // Keep only last 1000 snapshots
        List<PerformanceSnapshot> snapshots = historicalMetrics.get("global");
        if (snapshots.size() > 1000) {
            snapshots.remove(0);
        }

        log.debug("Performance monitoring completed for {}: {}ms, {}MB, {}% CPU", 
                 metrics.getMetricsId(), executionTime, metrics.getMemoryUsage() / 1024 / 1024, 
                 String.format("%.2f", metrics.getCpuUsage()));
    }

    public void recordSlowQuery(String sql, long executionTime, Map<String, Object> parameters) {
        if (executionTime > 5000) { // 5 seconds threshold
            SlowQueryInfo slowQuery = new SlowQueryInfo();
            slowQuery.setSql(sql);
            slowQuery.setExecutionTime(executionTime);
            slowQuery.setParameters(parameters);
            slowQuery.setTimestamp(LocalDateTime.now());
            
            List<SlowQueryInfo> slowQueries = (List<SlowQueryInfo>) historicalMetrics.computeIfAbsent("slow_queries", k -> new ArrayList<>());
            slowQueries.add(slowQuery);
            
            if (slowQueries.size() > 100) {
                slowQueries.remove(0);
            }
            
            log.warn("Slow query detected: {}ms - {}", executionTime, sql.substring(0, Math.min(100, sql.length())));
        }
    }

    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Memory metrics
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = Runtime.getRuntime().maxMemory();
        
        metrics.put("totalMemory", totalMemory);
        metrics.put("freeMemory", freeMemory);
        metrics.put("usedMemory", usedMemory);
        metrics.put("maxMemory", maxMemory);
        metrics.put("memoryUsagePercentage", (double) usedMemory / maxMemory * 100);
        
        // CPU metrics
        double cpuUsage = getCpuUsage();
        metrics.put("cpuUsage", cpuUsage);
        
        // Active executions
        metrics.put("activeExecutions", activeMetrics.size());
        metrics.put("totalExecutions", executionCounter.get());
        
        return metrics;
    }

    public Map<String, Object> getPerformanceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<PerformanceSnapshot> snapshots = historicalMetrics.get("global");
        if (snapshots == null || snapshots.isEmpty()) {
            return stats;
        }

        // Calculate statistics
        long totalExecutions = snapshots.size();
        long totalExecutionTime = snapshots.stream().mapToLong(PerformanceSnapshot::getExecutionTime).sum();
        long avgExecutionTime = totalExecutionTime / totalExecutions;
        long maxExecutionTime = snapshots.stream().mapToLong(PerformanceSnapshot::getExecutionTime).max().orElse(0);
        long minExecutionTime = snapshots.stream().mapToLong(PerformanceSnapshot::getExecutionTime).min().orElse(0);
        
        long totalMemoryUsage = snapshots.stream().mapToLong(PerformanceSnapshot::getMemoryUsage).sum();
        long avgMemoryUsage = totalMemoryUsage / totalExecutions;
        long maxMemoryUsage = snapshots.stream().mapToLong(PerformanceSnapshot::getMemoryUsage).max().orElse(0);
        
        double totalCpuUsage = snapshots.stream().mapToDouble(PerformanceSnapshot::getCpuUsage).sum();
        double avgCpuUsage = totalCpuUsage / totalExecutions;
        double maxCpuUsage = snapshots.stream().mapToDouble(PerformanceSnapshot::getCpuUsage).max().orElse(0);
        
        stats.put("totalExecutions", totalExecutions);
        stats.put("avgExecutionTime", avgExecutionTime);
        stats.put("maxExecutionTime", maxExecutionTime);
        stats.put("minExecutionTime", minExecutionTime);
        stats.put("avgMemoryUsage", avgMemoryUsage);
        stats.put("maxMemoryUsage", maxMemoryUsage);
        stats.put("avgCpuUsage", avgCpuUsage);
        stats.put("maxCpuUsage", maxCpuUsage);
        
        return stats;
    }

    public List<Map<String, Object>> getSlowQueries(int limit) {
        List<SlowQueryInfo> slowQueries = (List<SlowQueryInfo>) historicalMetrics.get("slow_queries");
        if (slowQueries == null || slowQueries.isEmpty()) {
            return new ArrayList<>();
        }

        return slowQueries.stream()
                .sorted((a, b) -> Long.compare(b.getExecutionTime(), a.getExecutionTime()))
                .limit(limit)
                .map(query -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("sql", query.getSql());
                    map.put("executionTime", query.getExecutionTime());
                    map.put("parameters", query.getParameters());
                    map.put("timestamp", query.getTimestamp());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Map<String, Object>> getPerformanceHistory(int hours) {
        List<PerformanceSnapshot> snapshots = historicalMetrics.get("global");
        if (snapshots == null || snapshots.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        
        return snapshots.stream()
                .filter(snapshot -> snapshot.getTimestamp().isAfter(cutoff))
                .map(snapshot -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("executionTime", snapshot.getExecutionTime());
                    map.put("memoryUsage", snapshot.getMemoryUsage());
                    map.put("cpuUsage", snapshot.getCpuUsage());
                    map.put("timestamp", snapshot.getTimestamp());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public void clearMetrics() {
        historicalMetrics.clear();
        executionCounter.set(0);
        log.info("Performance metrics cleared");
    }

    public Map<String, Object> getActiveExecutions() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> executions = new ArrayList<>();
        
        for (PerformanceMetrics metrics : activeMetrics.values()) {
            Map<String, Object> execution = new HashMap<>();
            execution.put("metricsId", metrics.getMetricsId());
            execution.put("startTime", metrics.getStartTime());
            execution.put("duration", ChronoUnit.MILLIS.between(metrics.getStartTime(), LocalDateTime.now()));
            executions.add(execution);
        }
        
        result.put("count", executions.size());
        result.put("executions", executions);
        
        return result;
    }

    public boolean isExecutionSlow(long executionTime) {
        return executionTime > 5000; // 5 seconds threshold
    }

    public String getPerformanceLevel(long executionTime, long memoryUsage, double cpuUsage) {
        if (executionTime > 10000 || memoryUsage > 100 * 1024 * 1024 || cpuUsage > 80) {
            return "POOR";
        } else if (executionTime > 5000 || memoryUsage > 50 * 1024 * 1024 || cpuUsage > 60) {
            return "MODERATE";
        } else if (executionTime > 1000 || memoryUsage > 20 * 1024 * 1024 || cpuUsage > 40) {
            return "GOOD";
        } else {
            return "EXCELLENT";
        }
    }

    private long getMemoryUsage() {
        return memoryBean.getHeapMemoryUsage().getUsed();
    }

    private double getCpuUsage() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100;
        }
        return 0.0;
    }

    @Data
    public static class PerformanceMetrics {
        private String metricsId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long executionTime;
        private Long startMemoryUsage;
        private Long endMemoryUsage;
        private Long memoryUsage;
        private Double startCpuUsage;
        private Double endCpuUsage;
        private Double cpuUsage;
        private String executionPlan;
        private Map<String, Object> additionalMetrics = new HashMap<>();
    }

    @Data
    public static class PerformanceSnapshot {
        private Long executionTime;
        private Long memoryUsage;
        private Double cpuUsage;
        private LocalDateTime timestamp;
    }

    @Data
    public static class SlowQueryInfo {
        private String sql;
        private Long executionTime;
        private Map<String, Object> parameters;
        private LocalDateTime timestamp;
    }
}