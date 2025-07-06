package com.hospital.report.service;

import com.hospital.report.entity.SyncLog;
import com.hospital.report.entity.SyncTask;
import com.hospital.report.sync.SyncContext;
import com.hospital.report.sync.DataExtractor;
import com.hospital.report.mapper.SyncTaskMapper;
import com.hospital.report.mapper.SyncLogMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final SyncTaskMapper syncTaskMapper;
    private final SyncLogMapper syncLogMapper;
    private final DataExtractor dataExtractor;
    private final ObjectMapper objectMapper;
    
    private final Map<String, SyncContext> runningTasks = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public String executeSync(Long taskId, String triggerType, Long triggerUser) {
        SyncTask task = syncTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("同步任务不存在: " + taskId);
        }

        if (!task.getIsEnabled()) {
            throw new RuntimeException("同步任务已禁用: " + task.getTaskName());
        }

        String executionId = UUID.randomUUID().toString().replace("-", "");
        
        SyncContext context = buildSyncContext(task, executionId, triggerType, triggerUser);
        runningTasks.put(executionId, context);

        executorService.submit(() -> {
            try {
                performSync(context);
            } catch (Exception e) {
                log.error("同步任务执行异常: {}", e.getMessage(), e);
                context.setStatus("FAILED");
                context.setErrorMessage(e.getMessage());
                context.setErrorStack(getStackTrace(e));
            } finally {
                finalizeSyncExecution(context);
                runningTasks.remove(executionId);
            }
        });

        return executionId;
    }

    private void performSync(SyncContext context) {
        context.setStatus("RUNNING");
        context.setStartTime(LocalDateTime.now());
        
        saveSyncLog(context);

        try {
            long sourceCount = dataExtractor.countSourceData(context);
            context.setSourceCount(sourceCount);
            
            if (sourceCount == 0) {
                log.info("源数据为空，同步任务完成: {}", context.getTaskCode());
                context.setStatus("SUCCESS");
                return;
            }

            long processedCount = 0;
            Integer batchSize = context.getBatchSize() != null ? context.getBatchSize() : 1000;

            while (processedCount < sourceCount && !context.isCancelled()) {
                
                SyncContext batchContext = cloneSyncContext(context);
                batchContext.setBatchSize(batchSize);
                
                List<Map<String, Object>> batchData = dataExtractor.extractData(batchContext);
                
                if (batchData.isEmpty()) {
                    break;
                }

                try {
                    dataExtractor.writeData(batchContext, batchData);
                    processedCount += batchData.size();
                    
                    context.updateProgress(processedCount, sourceCount);
                    context.setSuccessCount(processedCount);
                    
                    updateLastSyncValue(context, batchData);
                    
                    log.info("批次处理完成: {}/{}, 任务: {}", 
                        processedCount, sourceCount, context.getTaskCode());
                    
                } catch (Exception e) {
                    log.error("批次处理失败: {}", e.getMessage(), e);
                    
                    if (context.shouldRetry()) {
                        context.incrementRetry();
                        log.info("重试第 {} 次: {}", context.getCurrentRetry(), context.getTaskCode());
                        
                        Thread.sleep(context.getRetryInterval() * 1000L);
                        continue;
                    } else {
                        throw e;
                    }
                }
                
                context.setCurrentRetry(0);
                
                if (context.isPaused()) {
                    log.info("同步任务已暂停: {}", context.getTaskCode());
                    while (context.isPaused() && !context.isCancelled()) {
                        Thread.sleep(1000);
                    }
                }
            }

            if (context.isCancelled()) {
                context.setStatus("CANCELLED");
                log.info("同步任务已取消: {}", context.getTaskCode());
            } else {
                context.setStatus("SUCCESS");
                log.info("同步任务完成: {}", context.getTaskCode());
            }

        } catch (Exception e) {
            log.error("同步任务异常: {}", e.getMessage(), e);
            context.setStatus("FAILED");
            context.setErrorMessage(e.getMessage());
            context.setErrorStack(getStackTrace(e));
            
            context.incrementError();
        }
    }

    private SyncContext buildSyncContext(SyncTask task, String executionId, String triggerType, Long triggerUser) {
        SyncContext context = new SyncContext();
        
        context.setExecutionId(executionId);
        context.setTaskId(task.getId());
        context.setTaskCode(task.getTaskCode());
        context.setTaskName(task.getTaskName());
        context.setSyncType(task.getSyncType());
        context.setSyncMode(task.getSyncMode());
        
        context.setSourceDatasourceId(task.getSourceDatasourceId());
        context.setTargetDatasourceId(task.getTargetDatasourceId());
        context.setSourceTable(task.getSourceTable());
        context.setTargetTable(task.getTargetTable());
        context.setSourceSql(task.getSourceSql());
        context.setTargetSql(task.getTargetSql());
        
        context.setIncrementalColumn(task.getIncrementalColumn());
        context.setIncrementalType(task.getIncrementalType());
        context.setLastSyncValue(task.getLastSyncValue());
        
        context.setFilterCondition(task.getFilterCondition());
        context.setMappingConfig(task.getMappingConfig());
        
        if (task.getMappingConfig() != null && !task.getMappingConfig().trim().isEmpty()) {
            try {
                Map<String, String> fieldMapping = objectMapper.readValue(
                    task.getMappingConfig(), 
                    new TypeReference<Map<String, String>>() {}
                );
                context.setFieldMapping(fieldMapping);
            } catch (Exception e) {
                log.warn("解析字段映射配置失败: {}", e.getMessage());
            }
        }
        
        context.setBatchSize(task.getBatchSize());
        context.setTimeoutSeconds(task.getTimeoutSeconds());
        context.setRetryTimes(task.getRetryTimes());
        context.setRetryInterval(task.getRetryInterval());
        context.setEnableTransaction(task.getEnableTransaction());
        context.setParallelThreads(task.getParallelThreads());
        
        context.setSuccessCount(0L);
        context.setErrorCount(0L);
        context.setSkipCount(0L);
        context.setProgressPercent(0.0);
        context.setStatus("PENDING");
        context.setCurrentRetry(0);
        context.setTriggerType(triggerType);
        context.setTriggerUser(triggerUser);
        
        return context;
    }

    private SyncContext cloneSyncContext(SyncContext original) {
        SyncContext clone = new SyncContext();
        
        clone.setExecutionId(original.getExecutionId());
        clone.setTaskId(original.getTaskId());
        clone.setTaskCode(original.getTaskCode());
        clone.setTaskName(original.getTaskName());
        clone.setSyncType(original.getSyncType());
        clone.setSyncMode(original.getSyncMode());
        
        clone.setSourceDatasourceId(original.getSourceDatasourceId());
        clone.setTargetDatasourceId(original.getTargetDatasourceId());
        clone.setSourceTable(original.getSourceTable());
        clone.setTargetTable(original.getTargetTable());
        clone.setSourceSql(original.getSourceSql());
        clone.setTargetSql(original.getTargetSql());
        
        clone.setIncrementalColumn(original.getIncrementalColumn());
        clone.setIncrementalType(original.getIncrementalType());
        clone.setLastSyncValue(original.getLastSyncValue());
        clone.setCurrentSyncValue(original.getCurrentSyncValue());
        
        clone.setFilterCondition(original.getFilterCondition());
        clone.setMappingConfig(original.getMappingConfig());
        clone.setFieldMapping(original.getFieldMapping());
        
        clone.setBatchSize(original.getBatchSize());
        clone.setTimeoutSeconds(original.getTimeoutSeconds());
        clone.setRetryTimes(original.getRetryTimes());
        clone.setRetryInterval(original.getRetryInterval());
        clone.setEnableTransaction(original.getEnableTransaction());
        clone.setParallelThreads(original.getParallelThreads());
        
        return clone;
    }

    private void updateLastSyncValue(SyncContext context, List<Map<String, Object>> batchData) {
        if (!"INCREMENTAL".equals(context.getSyncMode()) || 
            context.getIncrementalColumn() == null || 
            batchData.isEmpty()) {
            return;
        }

        Map<String, Object> lastRow = batchData.get(batchData.size() - 1);
        Object lastValue = lastRow.get(context.getIncrementalColumn());
        
        if (lastValue != null) {
            context.setCurrentSyncValue(lastValue.toString());
        }
    }

    private void finalizeSyncExecution(SyncContext context) {
        context.setEndTime(LocalDateTime.now());
        
        if (context.getStartTime() != null && context.getEndTime() != null) {
            long duration = java.time.Duration.between(context.getStartTime(), context.getEndTime()).toMillis();
            context.setEndTime(context.getEndTime());
        }

        saveSyncLog(context);

        if ("SUCCESS".equals(context.getStatus()) && context.getCurrentSyncValue() != null) {
            updateTaskLastSyncValue(context.getTaskId(), context.getCurrentSyncValue());
        }
    }

    private void saveSyncLog(SyncContext context) {
        SyncLog syncLog = new SyncLog();
        syncLog.setTaskId(context.getTaskId());
        syncLog.setTaskCode(context.getTaskCode());
        syncLog.setExecutionId(context.getExecutionId());
        syncLog.setStatus(context.getStatus());
        syncLog.setStartTime(context.getStartTime());
        syncLog.setEndTime(context.getEndTime());
        
        if (context.getStartTime() != null && context.getEndTime() != null) {
            long duration = java.time.Duration.between(context.getStartTime(), context.getEndTime()).toMillis();
            syncLog.setDuration(duration);
        }
        
        syncLog.setSourceCount(context.getSourceCount());
        syncLog.setTargetCount(context.getTargetCount());
        syncLog.setSuccessCount(context.getSuccessCount());
        syncLog.setErrorCount(context.getErrorCount());
        syncLog.setSkipCount(context.getSkipCount());
        syncLog.setProgressPercent(context.getProgressPercent());
        syncLog.setCurrentValue(context.getCurrentSyncValue());
        syncLog.setErrorMessage(context.getErrorMessage());
        syncLog.setErrorStack(context.getErrorStack());
        syncLog.setRetryCount(context.getCurrentRetry());
        syncLog.setMaxRetry(context.getRetryTimes());
        syncLog.setTriggerType(context.getTriggerType());
        syncLog.setTriggerUser(context.getTriggerUser());

        try {
            if (syncLog.getId() == null) {
                syncLogMapper.insert(syncLog);
            } else {
                syncLogMapper.updateById(syncLog);
            }
        } catch (Exception e) {
            log.error("保存同步日志失败: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void updateTaskLastSyncValue(Long taskId, String lastSyncValue) {
        SyncTask task = new SyncTask();
        task.setId(taskId);
        task.setLastSyncValue(lastSyncValue);
        task.setUpdatedTime(LocalDateTime.now());
        syncTaskMapper.updateById(task);
    }

    public boolean cancelSync(String executionId) {
        SyncContext context = runningTasks.get(executionId);
        if (context != null) {
            context.setCancelled(true);
            log.info("取消同步任务: {}", executionId);
            return true;
        }
        return false;
    }

    public boolean pauseSync(String executionId) {
        SyncContext context = runningTasks.get(executionId);
        if (context != null) {
            context.setPaused(true);
            log.info("暂停同步任务: {}", executionId);
            return true;
        }
        return false;
    }

    public boolean resumeSync(String executionId) {
        SyncContext context = runningTasks.get(executionId);
        if (context != null) {
            context.setPaused(false);
            log.info("恢复同步任务: {}", executionId);
            return true;
        }
        return false;
    }

    public SyncContext getSyncStatus(String executionId) {
        return runningTasks.get(executionId);
    }

    public List<SyncContext> getAllRunningSyncs() {
        return runningTasks.values().stream().toList();
    }

    private String getStackTrace(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}