export interface DataSource {
  id?: number
  datasourceName: string
  datasourceCode: string
  databaseType: string
  jdbcUrl: string
  username: string
  password: string
  driverClassName: string
  isEncrypted?: boolean
  encryptionKey?: string
  
  // 连接池配置
  initialSize?: number
  minIdle?: number
  maxActive?: number
  maxWait?: number
  timeBetweenEvictionRunsMillis?: number
  minEvictableIdleTimeMillis?: number
  validationQuery?: string
  testWhileIdle?: boolean
  testOnBorrow?: boolean
  testOnReturn?: boolean
  poolPreparedStatements?: boolean
  maxPoolPreparedStatementPerConnectionSize?: number
  connectionTimeout?: number
  idleTimeout?: number
  maxLifetime?: number
  
  // 监控配置
  enableMonitoring?: boolean
  monitoringInterval?: number
  slowSqlThreshold?: number
  
  status?: number
  isDeleted?: boolean
  description?: string
  createdBy?: number
  createdTime?: string
  updatedBy?: number
  updatedTime?: string
}

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
  createdBy?: number
  createdTime?: string
  updatedBy?: number
  updatedTime?: string
  isDeleted?: boolean
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
  syncDetails?: string
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
  
  sourceDatasourceId: number
  targetDatasourceId: number
  sourceTable?: string
  targetTable?: string
  sourceSql?: string
  targetSql?: string
  
  incrementalColumn?: string
  incrementalType?: string
  lastSyncValue?: string
  currentSyncValue?: string
  
  filterCondition?: string
  mappingConfig?: string
  fieldMapping?: Record<string, string>
  
  batchSize?: number
  timeoutSeconds?: number
  retryTimes?: number
  retryInterval?: number
  enableTransaction?: boolean
  parallelThreads?: number
  
  startTime?: string
  endTime?: string
  
  sourceCount?: number
  targetCount?: number
  successCount?: number
  errorCount?: number
  skipCount?: number
  
  progressPercent?: number
  status: string
  errorMessage?: string
  errorStack?: string
  
  currentRetry?: number
  triggerType: string
  triggerUser?: number
  
  cancelled: boolean
  paused: boolean
}