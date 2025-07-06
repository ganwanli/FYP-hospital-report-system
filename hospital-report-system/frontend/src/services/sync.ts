import { request } from '@/utils'

export interface SyncTask {
  id?: number
  taskName: string
  taskCode: string
  taskType: string
  sourceDatasourceId: number
  targetDatasourceId: number
  sourceTable?: string
  targetTable?: string
  sourceSql?: string
  targetSql?: string
  syncType: string
  syncMode: string
  cronExpression?: string
  batchSize?: number
  timeoutSeconds?: number
  retryTimes?: number
  retryInterval?: number
  enableTransaction?: boolean
  parallelThreads?: number
  incrementalColumn?: string
  incrementalType?: string
  lastSyncValue?: string
  filterCondition?: string
  mappingConfig?: string
  status?: number
  isEnabled: boolean
  description?: string
  createdTime?: string
  updatedTime?: string
}

export interface SyncLog {
  id: number
  taskId: number
  taskCode: string
  executionId: string
  status: string
  startTime: string
  endTime?: string
  duration?: number
  sourceCount?: number
  targetCount?: number
  successCount?: number
  errorCount?: number
  skipCount?: number
  progressPercent?: number
  currentValue?: string
  errorMessage?: string
  errorStack?: string
  retryCount?: number
  maxRetry?: number
  triggerType: string
  triggerUser?: number
  createdTime: string
}

export interface SyncContext {
  executionId: string
  taskId: number
  taskCode: string
  taskName: string
  syncType: string
  syncMode: string
  status: string
  startTime?: string
  endTime?: string
  sourceCount?: number
  targetCount?: number
  successCount?: number
  errorCount?: number
  skipCount?: number
  progressPercent?: number
  errorMessage?: string
  currentRetry?: number
  triggerType: string
  triggerUser?: number
  cancelled: boolean
  paused: boolean
}

export const syncAPI = {
  // 任务管理
  createTask: (data: SyncTask) => 
    request.post('/api/sync/tasks', data),
  
  updateTask: (id: number, data: SyncTask) => 
    request.put(`/api/sync/tasks/${id}`, data),
  
  deleteTask: (id: number) => 
    request.delete(`/api/sync/tasks/${id}`),
  
  getTasks: (params?: any) => 
    request.get('/api/sync/tasks', { params }),
  
  getTask: (id: number) => 
    request.get(`/api/sync/tasks/${id}`),

  // 任务执行
  executeTask: (taskId: number) => 
    request.post(`/api/sync/execute/${taskId}`),
  
  cancelExecution: (executionId: string) => 
    request.post(`/api/sync/cancel/${executionId}`),
  
  pauseExecution: (executionId: string) => 
    request.post(`/api/sync/pause/${executionId}`),
  
  resumeExecution: (executionId: string) => 
    request.post(`/api/sync/resume/${executionId}`),
  
  getSyncStatus: (executionId: string) => 
    request.get(`/api/sync/status/${executionId}`),
  
  getRunningTasks: () => 
    request.get('/api/sync/running'),

  // 任务调度
  scheduleTask: (taskId: number) => 
    request.post(`/api/sync/schedule/${taskId}`),
  
  unscheduleTask: (taskId: number) => 
    request.post(`/api/sync/unschedule/${taskId}`),
  
  pauseSchedule: (taskId: number) => 
    request.post(`/api/sync/pause-schedule/${taskId}`),
  
  resumeSchedule: (taskId: number) => 
    request.post(`/api/sync/resume-schedule/${taskId}`),

  // 日志查询
  getSyncLogs: (params?: any) => 
    request.get('/api/sync/logs', { params }),

  // 统计信息
  getSyncStatistics: () => 
    request.get('/api/sync/statistics')
}