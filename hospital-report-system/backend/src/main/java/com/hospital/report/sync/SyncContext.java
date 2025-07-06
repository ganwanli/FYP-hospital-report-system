package com.hospital.report.sync;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SyncContext {
    
    private String executionId;
    private Long taskId;
    private String taskCode;
    private String taskName;
    private String syncType;
    private String syncMode;
    
    private Long sourceDatasourceId;
    private Long targetDatasourceId;
    private String sourceTable;
    private String targetTable;
    private String sourceSql;
    private String targetSql;
    
    private String incrementalColumn;
    private String incrementalType;
    private String lastSyncValue;
    private String currentSyncValue;
    
    private String filterCondition;
    private String mappingConfig;
    private Map<String, String> fieldMapping;
    
    private Integer batchSize;
    private Integer timeoutSeconds;
    private Integer retryTimes;
    private Integer retryInterval;
    private Boolean enableTransaction;
    private Integer parallelThreads;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    private Long sourceCount;
    private Long targetCount;
    private Long successCount;
    private Long errorCount;
    private Long skipCount;
    
    private Double progressPercent;
    private String status;
    private String errorMessage;
    private String errorStack;
    
    private Integer currentRetry;
    private String triggerType;
    private Long triggerUser;
    
    private volatile boolean cancelled = false;
    private volatile boolean paused = false;
    
    public void updateProgress(long processed, long total) {
        if (total > 0) {
            this.progressPercent = (double) processed / total * 100;
        }
    }
    
    public void incrementSuccess() {
        this.successCount = (this.successCount == null ? 0 : this.successCount) + 1;
    }
    
    public void incrementError() {
        this.errorCount = (this.errorCount == null ? 0 : this.errorCount) + 1;
    }
    
    public void incrementSkip() {
        this.skipCount = (this.skipCount == null ? 0 : this.skipCount) + 1;
    }
    
    public boolean shouldRetry() {
        return currentRetry == null || currentRetry < retryTimes;
    }
    
    public void incrementRetry() {
        this.currentRetry = (this.currentRetry == null ? 0 : this.currentRetry) + 1;
    }
}