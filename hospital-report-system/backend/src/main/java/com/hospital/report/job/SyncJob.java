package com.hospital.report.job;

import com.hospital.report.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class SyncJob implements Job {

    private final SyncService syncService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        
        Long taskId = dataMap.getLong("taskId");
        String taskCode = dataMap.getString("taskCode");
        String taskName = dataMap.getString("taskName");
        
        log.info("开始执行定时同步任务: {} - {}", taskCode, taskName);
        
        try {
            String executionId = syncService.executeSync(taskId, "SCHEDULED", null);
            log.info("定时同步任务启动成功: {} - 执行ID: {}", taskCode, executionId);
            
        } catch (Exception e) {
            log.error("定时同步任务执行失败: {} - {}", taskCode, e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}