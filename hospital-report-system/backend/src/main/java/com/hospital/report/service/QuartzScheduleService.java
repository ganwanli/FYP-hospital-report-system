package com.hospital.report.service;

import com.hospital.report.entity.SyncTask;
import com.hospital.report.job.SyncJob;
import com.hospital.report.mapper.SyncTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuartzScheduleService implements ApplicationRunner {

    private final Scheduler scheduler;
    private final SyncTaskMapper syncTaskMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("初始化定时任务调度器...");
        initScheduledTasks();
        log.info("定时任务调度器初始化完成");
    }

    private void initScheduledTasks() {
        try {
            List<SyncTask> enabledTasks = syncTaskMapper.selectEnabledTasks();
            
            for (SyncTask task : enabledTasks) {
                if (task.getCronExpression() != null && !task.getCronExpression().trim().isEmpty()) {
                    scheduleTask(task);
                }
            }
            
            log.info("已加载 {} 个定时同步任务", enabledTasks.size());
            
        } catch (Exception e) {
            log.error("初始化定时任务失败: {}", e.getMessage(), e);
        }
    }

    public void scheduleTask(SyncTask task) {
        try {
            String jobName = "sync_job_" + task.getId();
            String jobGroup = "sync_job_group";
            String triggerName = "sync_trigger_" + task.getId();
            String triggerGroup = "sync_trigger_group";

            JobDetail jobDetail = JobBuilder.newJob(SyncJob.class)
                    .withIdentity(jobName, jobGroup)
                    .withDescription(task.getDescription())
                    .usingJobData("taskId", task.getId())
                    .usingJobData("taskCode", task.getTaskCode())
                    .usingJobData("taskName", task.getTaskName())
                    .build();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerName, triggerGroup)
                    .withDescription(task.getDescription())
                    .withSchedule(CronScheduleBuilder.cronSchedule(task.getCronExpression()))
                    .build();

            if (scheduler.checkExists(jobDetail.getKey())) {
                scheduler.deleteJob(jobDetail.getKey());
            }

            scheduler.scheduleJob(jobDetail, trigger);
            
            log.info("任务调度成功: {} - {}", task.getTaskCode(), task.getCronExpression());
            
        } catch (Exception e) {
            log.error("任务调度失败: {} - {}", task.getTaskCode(), e.getMessage(), e);
        }
    }

    public void unscheduleTask(Long taskId) {
        try {
            String jobName = "sync_job_" + taskId;
            String jobGroup = "sync_job_group";
            
            JobKey jobKey = new JobKey(jobName, jobGroup);
            
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                log.info("任务取消调度成功: {}", taskId);
            }
            
        } catch (Exception e) {
            log.error("任务取消调度失败: {} - {}", taskId, e.getMessage(), e);
        }
    }

    public void rescheduleTask(SyncTask task) {
        unscheduleTask(task.getId());
        if (task.getIsEnabled() && task.getCronExpression() != null) {
            scheduleTask(task);
        }
    }

    public void pauseTask(Long taskId) {
        try {
            String jobName = "sync_job_" + taskId;
            String jobGroup = "sync_job_group";
            
            JobKey jobKey = new JobKey(jobName, jobGroup);
            scheduler.pauseJob(jobKey);
            
            log.info("任务暂停成功: {}", taskId);
            
        } catch (Exception e) {
            log.error("任务暂停失败: {} - {}", taskId, e.getMessage(), e);
        }
    }

    public void resumeTask(Long taskId) {
        try {
            String jobName = "sync_job_" + taskId;
            String jobGroup = "sync_job_group";
            
            JobKey jobKey = new JobKey(jobName, jobGroup);
            scheduler.resumeJob(jobKey);
            
            log.info("任务恢复成功: {}", taskId);
            
        } catch (Exception e) {
            log.error("任务恢复失败: {} - {}", taskId, e.getMessage(), e);
        }
    }

    public void triggerTask(Long taskId) {
        try {
            String jobName = "sync_job_" + taskId;
            String jobGroup = "sync_job_group";
            
            JobKey jobKey = new JobKey(jobName, jobGroup);
            scheduler.triggerJob(jobKey);
            
            log.info("任务手动触发成功: {}", taskId);
            
        } catch (Exception e) {
            log.error("任务手动触发失败: {} - {}", taskId, e.getMessage(), e);
        }
    }

    public boolean isTaskScheduled(Long taskId) {
        try {
            String jobName = "sync_job_" + taskId;
            String jobGroup = "sync_job_group";
            
            JobKey jobKey = new JobKey(jobName, jobGroup);
            return scheduler.checkExists(jobKey);
            
        } catch (Exception e) {
            log.error("检查任务调度状态失败: {} - {}", taskId, e.getMessage(), e);
            return false;
        }
    }

    public String getTaskNextFireTime(Long taskId) {
        try {
            String triggerName = "sync_trigger_" + taskId;
            String triggerGroup = "sync_trigger_group";
            
            TriggerKey triggerKey = new TriggerKey(triggerName, triggerGroup);
            Trigger trigger = scheduler.getTrigger(triggerKey);
            
            if (trigger != null && trigger.getNextFireTime() != null) {
                return trigger.getNextFireTime().toString();
            }
            
        } catch (Exception e) {
            log.error("获取任务下次执行时间失败: {} - {}", taskId, e.getMessage(), e);
        }
        
        return null;
    }
}