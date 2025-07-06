package com.hospital.report.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.entity.SyncLog;
import com.hospital.report.entity.SyncTask;
import com.hospital.report.mapper.SyncLogMapper;
import com.hospital.report.mapper.SyncTaskMapper;
import com.hospital.report.service.QuartzScheduleService;
import com.hospital.report.service.SyncService;
import com.hospital.report.sync.SyncContext;
import com.hospital.report.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;
    private final QuartzScheduleService quartzScheduleService;
    private final SyncTaskMapper syncTaskMapper;
    private final SyncLogMapper syncLogMapper;

    @PostMapping("/tasks")
    public Result<SyncTask> createTask(@RequestBody SyncTask syncTask) {
        try {
            syncTask.setCreatedTime(LocalDateTime.now());
            syncTask.setUpdatedTime(LocalDateTime.now());
            syncTask.setIsDeleted(false);
            syncTask.setStatus(0);
            
            syncTaskMapper.insert(syncTask);
            
            if (syncTask.getIsEnabled() && syncTask.getCronExpression() != null) {
                quartzScheduleService.scheduleTask(syncTask);
            }
            
            return Result.success(syncTask);
            
        } catch (Exception e) {
            log.error("创建同步任务失败: {}", e.getMessage(), e);
            return Result.error("创建同步任务失败: " + e.getMessage());
        }
    }

    @PutMapping("/tasks/{id}")
    public Result<SyncTask> updateTask(@PathVariable Long id, @RequestBody SyncTask syncTask) {
        try {
            syncTask.setId(id);
            syncTask.setUpdatedTime(LocalDateTime.now());
            
            syncTaskMapper.updateById(syncTask);
            
            quartzScheduleService.rescheduleTask(syncTask);
            
            return Result.success(syncTask);
            
        } catch (Exception e) {
            log.error("更新同步任务失败: {}", e.getMessage(), e);
            return Result.error("更新同步任务失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/tasks/{id}")
    public Result<Void> deleteTask(@PathVariable Long id) {
        try {
            SyncTask task = new SyncTask();
            task.setId(id);
            task.setIsDeleted(true);
            task.setUpdatedTime(LocalDateTime.now());
            
            syncTaskMapper.updateById(task);
            quartzScheduleService.unscheduleTask(id);
            
            return Result.success();
            
        } catch (Exception e) {
            log.error("删除同步任务失败: {}", e.getMessage(), e);
            return Result.error("删除同步任务失败: " + e.getMessage());
        }
    }

    @GetMapping("/tasks")
    public Result<IPage<Map<String, Object>>> getTasks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Boolean isEnabled) {
        
        try {
            Page<SyncTask> page = new Page<>(current, size);
            QueryWrapper<SyncTask> queryWrapper = new QueryWrapper<>();
            
            queryWrapper.eq("is_deleted", false);
            
            if (taskName != null && !taskName.trim().isEmpty()) {
                queryWrapper.like("task_name", taskName);
            }
            
            if (taskType != null && !taskType.trim().isEmpty()) {
                queryWrapper.eq("task_type", taskType);
            }
            
            if (isEnabled != null) {
                queryWrapper.eq("is_enabled", isEnabled);
            }
            
            queryWrapper.orderByDesc("created_time");
            
            IPage<SyncTask> result = syncTaskMapper.selectPage(page, queryWrapper);
            
            IPage<Map<String, Object>> resultPage = new Page<>(current, size, result.getTotal());
            
            List<Map<String, Object>> records = result.getRecords().stream()
                    .map(task -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", task.getId());
                        map.put("taskName", task.getTaskName());
                        map.put("taskCode", task.getTaskCode());
                        map.put("taskType", task.getTaskType());
                        map.put("syncType", task.getSyncType());
                        map.put("syncMode", task.getSyncMode());
                        map.put("cronExpression", task.getCronExpression());
                        map.put("isEnabled", task.getIsEnabled());
                        map.put("status", task.getStatus());
                        map.put("description", task.getDescription());
                        map.put("createdTime", task.getCreatedTime());
                        map.put("updatedTime", task.getUpdatedTime());
                        map.put("isScheduled", quartzScheduleService.isTaskScheduled(task.getId()));
                        map.put("nextFireTime", quartzScheduleService.getTaskNextFireTime(task.getId()));
                        return map;
                    })
                    .toList();
            
            resultPage.setRecords(records);
            
            return Result.success(resultPage);
            
        } catch (Exception e) {
            log.error("获取同步任务列表失败: {}", e.getMessage(), e);
            return Result.error("获取同步任务列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/tasks/{id}")
    public Result<SyncTask> getTask(@PathVariable Long id) {
        try {
            SyncTask task = syncTaskMapper.selectById(id);
            if (task == null) {
                return Result.error("同步任务不存在");
            }
            return Result.success(task);
            
        } catch (Exception e) {
            log.error("获取同步任务详情失败: {}", e.getMessage(), e);
            return Result.error("获取同步任务详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/execute/{taskId}")
    public Result<String> executeTask(@PathVariable Long taskId) {
        try {
            String executionId = syncService.executeSync(taskId, "MANUAL", null);
            return Result.success(executionId);
            
        } catch (Exception e) {
            log.error("执行同步任务失败: {}", e.getMessage(), e);
            return Result.error("执行同步任务失败: " + e.getMessage());
        }
    }

    @PostMapping("/cancel/{executionId}")
    public Result<Void> cancelExecution(@PathVariable String executionId) {
        try {
            boolean success = syncService.cancelSync(executionId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("同步任务不存在或已结束");
            }
            
        } catch (Exception e) {
            log.error("取消同步任务失败: {}", e.getMessage(), e);
            return Result.error("取消同步任务失败: " + e.getMessage());
        }
    }

    @PostMapping("/pause/{executionId}")
    public Result<Void> pauseExecution(@PathVariable String executionId) {
        try {
            boolean success = syncService.pauseSync(executionId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("同步任务不存在或已结束");
            }
            
        } catch (Exception e) {
            log.error("暂停同步任务失败: {}", e.getMessage(), e);
            return Result.error("暂停同步任务失败: " + e.getMessage());
        }
    }

    @PostMapping("/resume/{executionId}")
    public Result<Void> resumeExecution(@PathVariable String executionId) {
        try {
            boolean success = syncService.resumeSync(executionId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("同步任务不存在或已结束");
            }
            
        } catch (Exception e) {
            log.error("恢复同步任务失败: {}", e.getMessage(), e);
            return Result.error("恢复同步任务失败: " + e.getMessage());
        }
    }

    @GetMapping("/status/{executionId}")
    public Result<SyncContext> getSyncStatus(@PathVariable String executionId) {
        try {
            SyncContext context = syncService.getSyncStatus(executionId);
            if (context != null) {
                return Result.success(context);
            } else {
                return Result.error("同步任务不存在或已结束");
            }
            
        } catch (Exception e) {
            log.error("获取同步状态失败: {}", e.getMessage(), e);
            return Result.error("获取同步状态失败: " + e.getMessage());
        }
    }

    @GetMapping("/running")
    public Result<List<SyncContext>> getRunningTasks() {
        try {
            List<SyncContext> runningTasks = syncService.getAllRunningSyncs();
            return Result.success(runningTasks);
            
        } catch (Exception e) {
            log.error("获取运行中任务失败: {}", e.getMessage(), e);
            return Result.error("获取运行中任务失败: " + e.getMessage());
        }
    }

    @PostMapping("/schedule/{taskId}")
    public Result<Void> scheduleTask(@PathVariable Long taskId) {
        try {
            SyncTask task = syncTaskMapper.selectById(taskId);
            if (task == null) {
                return Result.error("同步任务不存在");
            }
            
            quartzScheduleService.scheduleTask(task);
            return Result.success();
            
        } catch (Exception e) {
            log.error("调度同步任务失败: {}", e.getMessage(), e);
            return Result.error("调度同步任务失败: " + e.getMessage());
        }
    }

    @PostMapping("/unschedule/{taskId}")
    public Result<Void> unscheduleTask(@PathVariable Long taskId) {
        try {
            quartzScheduleService.unscheduleTask(taskId);
            return Result.success();
            
        } catch (Exception e) {
            log.error("取消调度同步任务失败: {}", e.getMessage(), e);
            return Result.error("取消调度同步任务失败: " + e.getMessage());
        }
    }

    @PostMapping("/pause-schedule/{taskId}")
    public Result<Void> pauseSchedule(@PathVariable Long taskId) {
        try {
            quartzScheduleService.pauseTask(taskId);
            return Result.success();
            
        } catch (Exception e) {
            log.error("暂停调度失败: {}", e.getMessage(), e);
            return Result.error("暂停调度失败: " + e.getMessage());
        }
    }

    @PostMapping("/resume-schedule/{taskId}")
    public Result<Void> resumeSchedule(@PathVariable Long taskId) {
        try {
            quartzScheduleService.resumeTask(taskId);
            return Result.success();
            
        } catch (Exception e) {
            log.error("恢复调度失败: {}", e.getMessage(), e);
            return Result.error("恢复调度失败: " + e.getMessage());
        }
    }

    @GetMapping("/logs")
    public Result<IPage<SyncLog>> getSyncLogs(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String taskCode) {
        
        try {
            Page<SyncLog> page = new Page<>(current, size);
            QueryWrapper<SyncLog> queryWrapper = new QueryWrapper<>();
            
            if (taskId != null) {
                queryWrapper.eq("task_id", taskId);
            }
            
            if (status != null && !status.trim().isEmpty()) {
                queryWrapper.eq("status", status);
            }
            
            if (taskCode != null && !taskCode.trim().isEmpty()) {
                queryWrapper.like("task_code", taskCode);
            }
            
            queryWrapper.orderByDesc("start_time");
            
            IPage<SyncLog> result = syncLogMapper.selectPage(page, queryWrapper);
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("获取同步日志失败: {}", e.getMessage(), e);
            return Result.error("获取同步日志失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getSyncStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            Map<String, Object> taskStats = syncTaskMapper.getTaskStatistics();
            statistics.put("taskStats", taskStats);
            
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            List<Map<String, Object>> dateStats = syncLogMapper.getSyncStatsByDateRange(weekAgo, LocalDateTime.now());
            statistics.put("dateStats", dateStats);
            
            List<Map<String, Object>> statusDistribution = syncLogMapper.getStatusDistribution(weekAgo);
            statistics.put("statusDistribution", statusDistribution);
            
            List<Map<String, Object>> taskExecutionStats = syncLogMapper.getTaskExecutionStats(weekAgo);
            statistics.put("taskExecutionStats", taskExecutionStats);
            
            List<SyncContext> runningTasks = syncService.getAllRunningSyncs();
            statistics.put("runningTaskCount", runningTasks.size());
            statistics.put("runningTasks", runningTasks);
            
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取同步统计失败: {}", e.getMessage(), e);
            return Result.error("获取同步统计失败: " + e.getMessage());
        }
    }
}