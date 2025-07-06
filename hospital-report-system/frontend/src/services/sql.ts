import request from '../utils/request';

export interface SqlTemplate {
  templateId: number;
  templateName: string;
  templateDescription: string;
  templateCategory: string;
  templateContent: string;
  templateVersion: string;
  isActive: boolean;
  isPublic: boolean;
  createdBy: number;
  createdTime: string;
  updatedBy: number;
  updatedTime: string;
  usageCount: number;
  lastUsedTime: string;
  tags: string;
  databaseType: string;
  executionTimeout: number;
  maxRows: number;
  validationStatus: string;
  validationMessage: string;
  templateHash: string;
  approvalStatus: string;
  approvedBy: number;
  approvedTime: string;
  parameters: SqlTemplateParameter[];
  versions: SqlTemplateVersion[];
  createdByName: string;
  updatedByName: string;
  approvedByName: string;
}

export interface SqlTemplateParameter {
  parameterId: number;
  templateId: number;
  parameterName: string;
  parameterType: string;
  parameterDescription: string;
  defaultValue: string;
  isRequired: boolean;
  validationRule: string;
  validationMessage: string;
  parameterOrder: number;
  minLength: number;
  maxLength: number;
  minValue: string;
  maxValue: string;
  allowedValues: string;
  inputType: string;
  createdTime: string;
  updatedTime: string;
  isSensitive: boolean;
  maskPattern: string;
}

export interface SqlTemplateVersion {
  versionId: number;
  templateId: number;
  versionNumber: string;
  versionDescription: string;
  templateContent: string;
  changeLog: string;
  isCurrent: boolean;
  createdBy: number;
  createdTime: string;
  templateHash: string;
  parentVersionId: number;
  validationStatus: string;
  validationMessage: string;
  approvalStatus: string;
  approvedBy: number;
  approvedTime: string;
  createdByName: string;
  approvedByName: string;
}

export interface ExecutionResult {
  success: boolean;
  sql: string;
  parameters: Record<string, any>;
  data: any[];
  columns: ColumnMetadata[];
  rowCount: number;
  affectedRows: number;
  queryType: string;
  startTime: string;
  endTime: string;
  executionTime: number;
  memoryUsage: number;
  cpuUsage: number;
  truncated: boolean;
  totalRows: number;
  errorMessage: string;
  errorCode: string;
  cacheKey: string;
  fromCache: boolean;
}

export interface ColumnMetadata {
  name: string;
  type: string;
  typeName: string;
  size: number;
  precision: number;
  scale: number;
  nullable: boolean;
  autoIncrement: boolean;
  primaryKey: boolean;
  tableName: string;
  schemaName: string;
}

export interface ExecutionRequest {
  templateId?: number;
  sqlContent?: string;
  parameters?: Record<string, any>;
  databaseType?: string;
  userId: number;
}

export interface ExecutionHistory {
  executionId: number;
  templateId: number;
  userId: number;
  sessionId: string;
  sqlContent: string;
  parameterValues: string;
  executionStatus: string;
  startTime: string;
  endTime: string;
  executionDuration: number;
  affectedRows: number;
  resultRows: number;
  errorMessage: string;
  errorCode: string;
  databaseName: string;
  ipAddress: string;
  userAgent: string;
  executionPlan: string;
  memoryUsage: number;
  cpuUsage: number;
  cacheHit: boolean;
  cacheKey: string;
  queryType: string;
  isAsync: boolean;
  taskId: string;
  userName: string;
  templateName: string;
  parameters: Record<string, any>;
  resultData: any;
}

// SQL Template API
export const sqlTemplateApi = {
  // 创建模板
  createTemplate: (template: Partial<SqlTemplate>) =>
    request.post<SqlTemplate>('/api/sql-templates', template),

  // 更新模板
  updateTemplate: (id: number, template: Partial<SqlTemplate>) =>
    request.put<SqlTemplate>(`/api/sql-templates/${id}`, template),

  // 删除模板
  deleteTemplate: (id: number) =>
    request.delete(`/api/sql-templates/${id}`),

  // 获取模板详情
  getTemplate: (id: number) =>
    request.get<SqlTemplate>(`/api/sql-templates/${id}`),

  // 获取模板列表
  getTemplateList: (params: {
    page?: number;
    size?: number;
    templateName?: string;
    templateCategory?: string;
    isActive?: boolean;
    isPublic?: boolean;
    createdBy?: number;
    tags?: string;
    databaseType?: string;
    approvalStatus?: string;
  }) =>
    request.get<{
      records: SqlTemplate[];
      total: number;
      current: number;
      size: number;
    }>('/api/sql-templates', { params }),

  // 搜索模板
  searchTemplates: (keyword: string) =>
    request.get<SqlTemplate[]>('/api/sql-templates/search', { params: { keyword } }),

  // 获取热门模板
  getPopularTemplates: (limit = 10) =>
    request.get<SqlTemplate[]>('/api/sql-templates/popular', { params: { limit } }),

  // 获取用户最近模板
  getUserRecentTemplates: (userId: number, limit = 10) =>
    request.get<SqlTemplate[]>('/api/sql-templates/recent', { params: { userId, limit } }),

  // 获取所有分类
  getAllCategories: () =>
    request.get<string[]>('/api/sql-templates/categories'),

  // 获取所有数据库类型
  getAllDatabaseTypes: () =>
    request.get<string[]>('/api/sql-templates/database-types'),

  // 获取所有标签
  getAllTags: () =>
    request.get<string[]>('/api/sql-templates/tags'),

  // 获取统计信息
  getTemplateStatistics: () =>
    request.get<Record<string, any>>('/api/sql-templates/statistics'),

  // 获取分类统计
  getCategoryStatistics: () =>
    request.get<Array<{ category: string; count: number }>>('/api/sql-templates/statistics/category'),

  // 获取数据库类型统计
  getDatabaseTypeStatistics: () =>
    request.get<Array<{ databaseType: string; count: number }>>('/api/sql-templates/statistics/database-type'),

  // 获取月度创建统计
  getMonthlyCreationStatistics: () =>
    request.get<Array<{ month: string; count: number }>>('/api/sql-templates/statistics/monthly'),

  // 审批模板
  approveTemplate: (id: number, approvedBy: number) =>
    request.post(`/api/sql-templates/${id}/approve`, null, { params: { approvedBy } }),

  // 拒绝模板
  rejectTemplate: (id: number, approvedBy: number) =>
    request.post(`/api/sql-templates/${id}/reject`, null, { params: { approvedBy } }),

  // 获取待审批模板
  getPendingApprovalTemplates: () =>
    request.get<SqlTemplate[]>('/api/sql-templates/pending-approval'),

  // 验证模板
  validateTemplate: (templateContent: string, databaseType: string) =>
    request.post<string>('/api/sql-templates/validate', { templateContent, databaseType }),

  // 提取参数
  extractParameters: (templateContent: string) =>
    request.post<SqlTemplateParameter[]>('/api/sql-templates/extract-parameters', { templateContent }),

  // 复制模板
  duplicateTemplate: (id: number, newName: string) =>
    request.post<SqlTemplate>(`/api/sql-templates/${id}/duplicate`, null, { params: { newName } }),

  // 批量更新状态
  bulkUpdateTemplateStatus: (templateIds: number[], isActive: boolean) =>
    request.put('/api/sql-templates/bulk/status', { templateIds, isActive }),

  // 批量删除
  bulkDeleteTemplates: (templateIds: number[]) =>
    request.delete('/api/sql-templates/bulk', { data: { templateIds } }),

  // 获取使用统计
  getTemplateUsageStatistics: (id: number) =>
    request.get<Record<string, any>>(`/api/sql-templates/${id}/usage-statistics`),

  // 获取使用历史
  getTemplateUsageHistory: (id: number, days = 30) =>
    request.get<Array<Record<string, any>>>(`/api/sql-templates/${id}/usage-history`, { params: { days } }),

  // 更新使用计数
  updateUsageCount: (id: number) =>
    request.post(`/api/sql-templates/${id}/usage`)
};

// SQL Execution API
export const sqlExecutionApi = {
  // 执行SQL
  executeQuery: (request: ExecutionRequest) =>
    request.post<ExecutionResult>('/api/sql-execution/execute', request),

  // 异步执行SQL
  executeQueryAsync: (request: ExecutionRequest) =>
    request.post<string>('/api/sql-execution/execute-async', request),

  // 获取异步执行结果
  getAsyncExecutionResult: (taskId: string) =>
    request.get<ExecutionResult>(`/api/sql-execution/async/${taskId}/result`),

  // 获取异步执行状态
  getAsyncExecutionStatus: (taskId: string) =>
    request.get<Record<string, any>>(`/api/sql-execution/async/${taskId}/status`),

  // 取消异步执行
  cancelAsyncExecution: (taskId: string) =>
    request.post(`/api/sql-execution/async/${taskId}/cancel`),

  // 获取执行历史
  getExecutionHistory: (userId: number, limit = 50) =>
    request.get<ExecutionHistory[]>('/api/sql-execution/history', { params: { userId, limit } }),

  // 获取执行统计
  getExecutionStatistics: (userId: number) =>
    request.get<Record<string, any>>('/api/sql-execution/statistics', { params: { userId } }),

  // 清除执行历史
  clearExecutionHistory: (userId: number, daysOld = 30) =>
    request.delete('/api/sql-execution/history', { params: { userId, daysOld } }),

  // 验证SQL
  validateSqlBeforeExecution: (sqlContent: string, databaseType: string) =>
    request.post<Record<string, any>>('/api/sql-execution/validate', { sqlContent, databaseType }),

  // 解释查询
  explainQuery: (sqlContent: string, databaseType: string) =>
    request.post<Record<string, any>>('/api/sql-execution/explain', { sqlContent, databaseType }),

  // 获取慢查询
  getSlowQueries: (limit = 20) =>
    request.get<Array<Record<string, any>>>('/api/sql-execution/slow-queries', { params: { limit } }),

  // 优化查询
  optimizeQuery: (sqlContent: string, databaseType: string) =>
    request.post('/api/sql-execution/optimize', { sqlContent, databaseType }),

  // 获取缓存统计
  getCacheStatistics: () =>
    request.get<Record<string, any>>('/api/sql-execution/cache/statistics'),

  // 清除查询缓存
  clearQueryCache: (pattern?: string) =>
    request.post('/api/sql-execution/cache/clear', null, { params: { pattern } }),

  // 获取活跃执行
  getActiveExecutions: () =>
    request.get<Array<Record<string, any>>>('/api/sql-execution/active'),

  // 获取执行详情
  getExecutionDetails: (executionId: number) =>
    request.get<Record<string, any>>(`/api/sql-execution/execution/${executionId}`),

  // 导出执行结果
  exportExecutionResults: (executionId: number, format: string) =>
    request.post(`/api/sql-execution/export/${executionId}`, null, { params: { format } })
};