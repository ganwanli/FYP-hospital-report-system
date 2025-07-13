// SQL Template API Service
import { apiRequest } from '@/lib/api'

export interface SqlTemplate {
  templateId: number
  templateName: string
  templateDescription: string
  templateContent: string
  templateCategory: string
  databaseType: string
  isActive: boolean
  isPublic: boolean
  createdBy: number
  createdByName?: string
  updatedBy: number
  updatedByName?: string
  createdTime: string
  updatedTime: string
  usageCount: number
  tags?: string
  approvalStatus?: string
  approvedBy?: number
  approvedByName?: string
}

export interface SqlTemplateParameter {
  parameterId: number
  templateId: number
  parameterName: string
  parameterType: string
  defaultValue?: string
  isRequired: boolean
  description?: string
}

// SQL Template API
export const sqlTemplateApi = {
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
  }) => {
    const queryParams = new URLSearchParams()
    
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        queryParams.append(key, value.toString())
      }
    })
    
    return apiRequest(`/api/sql-templates?${queryParams.toString()}`, {
      method: 'GET'
    })
  },

  // 创建模板
  createTemplate: (template: Partial<SqlTemplate>) =>
    apiRequest('/api/sql-templates', {
      method: 'POST',
      body: JSON.stringify(template)
    }),

  // 更新模板
  updateTemplate: (id: number, template: Partial<SqlTemplate>) =>
    apiRequest(`/api/sql-templates/${id}`, {
      method: 'PUT',
      body: JSON.stringify(template)
    }),

  // 删除模板
  deleteTemplate: (id: number) =>
    apiRequest(`/api/sql-templates/${id}`, {
      method: 'DELETE'
    }),

  // 获取模板详情
  getTemplate: (id: number) =>
    apiRequest(`/api/sql-templates/${id}`, {
      method: 'GET'
    }),

  // 搜索模板
  searchTemplates: (keyword: string) =>
    apiRequest(`/api/sql-templates/search?keyword=${encodeURIComponent(keyword)}`, {
      method: 'GET'
    }),

  // 获取热门模板
  getPopularTemplates: (limit = 10) =>
    apiRequest(`/api/sql-templates/popular?limit=${limit}`, {
      method: 'GET'
    }),

  // 获取用户最近模板
  getUserRecentTemplates: (userId: number, limit = 10) =>
    apiRequest(`/api/sql-templates/recent?userId=${userId}&limit=${limit}`, {
      method: 'GET'
    }),

  // 获取所有分类
  getAllCategories: () =>
    apiRequest('/api/sql-templates/categories', {
      method: 'GET'
    }),

  // 获取所有数据库类型
  getAllDatabaseTypes: () =>
    apiRequest('/api/sql-templates/database-types', {
      method: 'GET'
    }),

  // 获取所有标签
  getAllTags: () =>
    apiRequest('/api/sql-templates/tags', {
      method: 'GET'
    }),

  // 根据分类获取模板
  getTemplatesByCategory: (category: string) =>
    apiRequest(`/api/sql-templates/category/${encodeURIComponent(category)}`, {
      method: 'GET'
    }),

  // 验证模板
  validateTemplate: (templateContent: string, databaseType: string) =>
    apiRequest('/api/sql-templates/validate', {
      method: 'POST',
      body: JSON.stringify({ templateContent, databaseType })
    }),

  // 提取参数
  extractParameters: (templateContent: string) =>
    apiRequest('/api/sql-templates/extract-parameters', {
      method: 'POST',
      body: JSON.stringify({ templateContent })
    }),

  // 复制模板
  duplicateTemplate: (id: number, newName: string) =>
    apiRequest(`/api/sql-templates/${id}/duplicate?newName=${encodeURIComponent(newName)}`, {
      method: 'POST'
    }),

  // 批量更新状态
  bulkUpdateTemplateStatus: (templateIds: number[], isActive: boolean) =>
    apiRequest('/api/sql-templates/bulk/status', {
      method: 'PUT',
      body: JSON.stringify({ templateIds, isActive })
    }),

  // 批量删除
  bulkDeleteTemplates: (templateIds: number[]) =>
    apiRequest('/api/sql-templates/bulk', {
      method: 'DELETE',
      body: JSON.stringify({ templateIds })
    }),
}
