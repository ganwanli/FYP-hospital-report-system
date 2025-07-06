import { request } from '@/utils'

export interface DataDictionary {
  id?: number
  fieldCode: string
  fieldNameCn: string
  fieldNameEn?: string
  dataType: string
  dataLength?: number
  dataPrecision?: number
  dataScale?: number
  isNullable?: boolean
  defaultValue?: string
  businessMeaning?: string
  dataSource?: string
  updateFrequency?: string
  ownerUser?: string
  ownerDepartment?: string
  categoryId?: number
  categoryPath?: string
  tableName?: string
  columnName?: string
  dataQualityRules?: string
  valueRange?: string
  sampleValues?: string
  relatedFields?: string
  usageCount?: number
  lastUsedTime?: string
  approvalStatus?: string
  approvalUser?: string
  approvalTime?: string
  version?: string
  changeLog?: string
  status?: number
  isStandard?: boolean
  standardReference?: string
  tags?: string
  remark?: string
  createdBy?: number
  createdTime?: string
  updatedBy?: number
  updatedTime?: string
}

export interface DictionaryCategory {
  id?: number
  categoryCode: string
  categoryName: string
  categoryDesc?: string
  parentId?: number
  level?: number
  sortOrder?: number
  icon?: string
  color?: string
  fieldCount?: number
  status?: number
  createdTime?: string
  updatedTime?: string
}

export interface FieldUsageLog {
  id: number
  fieldId: number
  fieldCode: string
  usageType: string
  usageContext?: string
  userId?: number
  userName?: string
  systemSource?: string
  ipAddress?: string
  usageDetails?: string
  createdTime: string
}

export const dictionaryAPI = {
  // 字段管理
  createField: (data: DataDictionary) => 
    request.post('/api/dictionary/fields', data),
  
  updateField: (id: number, data: DataDictionary) => 
    request.put(`/api/dictionary/fields/${id}`, data),
  
  deleteField: (id: number) => 
    request.delete(`/api/dictionary/fields/${id}`),
  
  getFields: (params?: any) => 
    request.get('/api/dictionary/fields', { params }),
  
  getFieldDetail: (id: number) => 
    request.get(`/api/dictionary/fields/${id}`),

  // 搜索功能
  searchFields: (keyword: string, limit?: number) => 
    request.get('/api/dictionary/search', { params: { keyword, limit } }),
  
  fullTextSearch: (keyword: string, limit?: number) => 
    request.get('/api/dictionary/search/fulltext', { params: { keyword, limit } }),

  // 统计信息
  getStatistics: () => 
    request.get('/api/dictionary/statistics'),
  
  getPopularFields: (limit?: number) => 
    request.get('/api/dictionary/popular', { params: { limit } }),

  // 使用统计
  recordUsage: (fieldId: number, usageType: string, usageContext?: string, userId?: number) => 
    request.post(`/api/dictionary/usage/${fieldId}`, null, { 
      params: { usageType, usageContext, userId } 
    }),

  // 导入导出
  importFields: (file: File, userId?: number) => {
    const formData = new FormData()
    formData.append('file', file)
    if (userId) formData.append('userId', userId.toString())
    return request.post('/api/dictionary/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  exportFields: (fieldIds?: number[]) => 
    request.post('/api/dictionary/export', fieldIds, { responseType: 'blob' }),

  // 批量操作
  batchApprove: (fieldIds: number[], approvalStatus: string, approvalUser: string) => 
    request.post('/api/dictionary/batch/approve', { fieldIds, approvalStatus, approvalUser }),

  // 字段操作
  copyField: (fieldId: number, newFieldCode: string) => 
    request.post(`/api/dictionary/copy/${fieldId}`, null, { params: { newFieldCode } }),
  
  getRelatedFields: (fieldId: number) => 
    request.get(`/api/dictionary/fields/${fieldId}/related`),
  
  getFieldHistory: (fieldId: number) => 
    request.get(`/api/dictionary/fields/${fieldId}/history`),
  
  standardizeField: (fieldId: number, standardReference: string) => 
    request.post(`/api/dictionary/standardize/${fieldId}`, null, { params: { standardReference } }),
  
  getDataLineage: (fieldCode: string) => 
    request.get(`/api/dictionary/lineage/${fieldCode}`),

  // 验证
  validateFieldCode: (fieldCode: string, excludeId?: number) => 
    request.post('/api/dictionary/validate/fieldCode', null, { params: { fieldCode, excludeId } }),

  // 分类管理
  createCategory: (data: DictionaryCategory) => 
    request.post('/api/dictionary/categories', data),
  
  updateCategory: (id: number, data: DictionaryCategory) => 
    request.put(`/api/dictionary/categories/${id}`, data),
  
  deleteCategory: (id: number) => 
    request.delete(`/api/dictionary/categories/${id}`),
  
  getCategoryTree: () => 
    request.get('/api/dictionary/categories/tree'),
  
  getCategoriesWithCount: () => 
    request.get('/api/dictionary/categories/list'),
  
  getChildCategories: (id: number) => 
    request.get(`/api/dictionary/categories/${id}/children`),
  
  moveCategory: (id: number, newParentId: number, newSortOrder: number) => 
    request.post(`/api/dictionary/categories/${id}/move`, null, { 
      params: { newParentId, newSortOrder } 
    }),
  
  getCategoryPath: (id: number) => 
    request.get(`/api/dictionary/categories/${id}/path`),
  
  updateSortOrder: (categories: Array<{ id: number; sortOrder: number }>) => 
    request.post('/api/dictionary/categories/batch/sort', categories),
  
  validateCategoryCode: (categoryCode: string, excludeId?: number) => 
    request.post('/api/dictionary/categories/validate/categoryCode', null, { 
      params: { categoryCode, excludeId } 
    }),
  
  updateFieldCount: (id: number) => 
    request.post(`/api/dictionary/categories/${id}/updateFieldCount`)
}