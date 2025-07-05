export interface Datasource {
  id: number
  datasourceName: string
  datasourceCode: string
  datasourceType: string
  host: string
  port: number
  databaseName: string
  username: string
  password?: string
  connectionUrl: string
  driverClass: string
  status: number
  description?: string
  createdTime: string
  updatedTime: string
}

export interface SqlTemplate {
  id: number
  templateName: string
  templateCode: string
  templateType: string
  datasourceId: number
  sqlContent: string
  parameters?: TemplateParameter[]
  resultFields?: ResultField[]
  category?: string
  description?: string
  version: string
  isPublic: number
  executionCount: number
  lastExecutionTime?: string
  avgExecutionTime: number
  status: number
  createdTime: string
  updatedTime: string
}

export interface TemplateParameter {
  id: number
  templateId: number
  paramName: string
  paramCode: string
  paramType: string
  paramLength?: number
  defaultValue?: string
  isRequired: number
  validationRule?: string
  description?: string
  sortOrder: number
}

export interface ResultField {
  fieldName: string
  fieldType: string
  fieldComment?: string
}

export interface ReportConfig {
  id: number
  reportName: string
  reportCode: string
  reportType: string
  categoryId?: number
  templateId: number
  datasourceId: number
  reportConfig: any
  chartConfig?: any
  exportConfig?: any
  cacheEnabled: number
  cacheTimeout: number
  refreshInterval: number
  accessLevel: string
  description?: string
  version: string
  isPublished: number
  viewCount: number
  lastViewTime?: string
  status: number
  createdTime: string
  updatedTime: string
}

export interface ReportCategory {
  id: number
  parentId: number
  categoryName: string
  categoryCode: string
  categoryIcon?: string
  description?: string
  sortOrder: number
  status: number
  children?: ReportCategory[]
}

export interface ReportExecution {
  id: number
  reportId: number
  executionId: string
  executionType: string
  executionParams?: any
  executionSql: string
  executionStatus: string
  startTime: string
  endTime?: string
  executionTime: number
  resultCount: number
  resultSize: number
  errorMessage?: string
  clientIp?: string
  executionUser: number
  datasourceId: number
  createdTime: string
}