import request from '../utils/request';

// 报表查看API
export const reportViewApi = {
  // 获取参数定义
  getParameterDefinitions: (reportId: number) =>
    request.get(`/api/reports/view/${reportId}/parameters`),

  // 生成报表
  generateReport: (reportId: number, parameters?: Record<string, any>) =>
    request.post(`/api/reports/view/${reportId}/generate`, parameters || {}),

  // 渲染HTML报表
  renderToHtml: (reportId: number, parameters?: Record<string, any>, mobile = false) =>
    request.post(`/api/reports/view/${reportId}/render/html?mobile=${mobile}`, parameters || {}),

  // 渲染打印版HTML
  renderToPrint: (reportId: number, parameters?: Record<string, any>) =>
    request.post(`/api/reports/view/${reportId}/render/print`, parameters || {}),

  // 渲染JSON报表
  renderToJson: (reportId: number, parameters?: Record<string, any>) =>
    request.post(`/api/reports/view/${reportId}/render/json`, parameters || {}),

  // 验证参数
  validateParameters: (reportId: number, parameters: Record<string, any>) =>
    request.post(`/api/reports/view/${reportId}/validate`, parameters),

  // 估算生成时间
  estimateGenerationTime: (reportId: number, parameters?: Record<string, any>) =>
    request.post(`/api/reports/view/${reportId}/estimate`, parameters || {}),

  // 生成缩略图
  generateThumbnail: (reportId: number, parameters?: Record<string, any>) =>
    request.post(`/api/reports/view/${reportId}/thumbnail`, parameters || {}),

  // 导出功能
  exportToPdf: (reportId: number, parameters?: Record<string, any>, options?: Record<string, any>) =>
    request.post(`/api/reports/view/${reportId}/export/pdf`, {
      parameters: parameters || {},
      options: options || {}
    }, { responseType: 'blob' }),

  exportToExcel: (reportId: number, parameters?: Record<string, any>, options?: Record<string, any>) =>
    request.post(`/api/reports/view/${reportId}/export/excel`, {
      parameters: parameters || {},
      options: options || {}
    }, { responseType: 'blob' }),

  exportToWord: (reportId: number, parameters?: Record<string, any>, options?: Record<string, any>) =>
    request.post(`/api/reports/view/${reportId}/export/word`, {
      parameters: parameters || {},
      options: options || {}
    }, { responseType: 'blob' }),

  exportToCsv: (reportId: number, parameters?: Record<string, any>, options?: Record<string, any>) =>
    request.post(`/api/reports/view/${reportId}/export/csv`, {
      parameters: parameters || {},
      options: options || {}
    }, { responseType: 'blob' }),

  exportToImage: (reportId: number, format = 'PNG', parameters?: Record<string, any>, options?: Record<string, any>) =>
    request.post(`/api/reports/view/${reportId}/export/image?format=${format}`, {
      parameters: parameters || {},
      options: options || {}
    }, { responseType: 'blob' }),

  // 获取支持的导出格式
  getSupportedFormats: () =>
    request.get('/api/reports/view/export/formats'),
};

// 报表分享API
export const reportShareApi = {
  // 创建分享
  createShare: (shareData: Partial<ReportShare>) =>
    request.post('/api/reports/share', shareData),

  // 更新分享
  updateShare: (shareId: number, shareData: Partial<ReportShare>) =>
    request.put(`/api/reports/share/${shareId}`, shareData),

  // 删除分享
  deleteShare: (shareId: number) =>
    request.delete(`/api/reports/share/${shareId}`),

  // 获取分享列表
  getShareList: (params: {
    page?: number;
    size?: number;
    reportId?: number;
    shareType?: string;
    isActive?: boolean;
    createdBy?: number;
  }) =>
    request.get('/api/reports/share', { params }),

  // 根据分享码获取分享信息
  getShareByCode: (shareCode: string) =>
    request.get(`/api/reports/share/code/${shareCode}`),

  // 验证分享访问权限
  validateShareAccess: (shareCode: string, password?: string) =>
    request.post(`/api/reports/share/validate/${shareCode}`, { password }),

  // 获取分享统计信息
  getShareStatistics: (shareId: number) =>
    request.get(`/api/reports/share/${shareId}/statistics`),

  // 刷新分享码
  refreshShareCode: (shareId: number) =>
    request.post(`/api/reports/share/${shareId}/refresh`),

  // 生成分享URL
  generateShareUrl: (shareCode: string) =>
    request.get(`/api/reports/share/${shareCode}/url`),

  // 清理过期分享
  cleanupExpiredShares: () =>
    request.post('/api/reports/share/cleanup'),
};

// 类型定义
export interface ReportParameterDefinition {
  name: string;
  label: string;
  dataType: 'string' | 'number' | 'boolean' | 'date' | 'select';
  required: boolean;
  defaultValue?: any;
  options?: Array<{ label: string; value: any }>;
  placeholder?: string;
  description?: string;
  validation?: {
    min?: number;
    max?: number;
    pattern?: string;
    message?: string;
  };
}

export interface ReportGenerationResult {
  reportId: number;
  reportName: string;
  reportDescription: string;
  canvasWidth: number;
  canvasHeight: number;
  generatedAt: number;
  parameters: Record<string, any>;
  components: ReportComponentData[];
  layoutConfig: Record<string, any>;
  styleConfig: Record<string, any>;
  generationTime: number;
  componentCount: number;
  fromCache: boolean;
}

export interface ReportComponentData {
  componentId: number;
  componentType: string;
  componentName: string;
  position: {
    x: number;
    y: number;
    width: number;
    height: number;
  };
  zIndex: number;
  isVisible: boolean;
  data?: any;
  dataError?: string;
  dataConfig: Record<string, any>;
  styleConfig: Record<string, any>;
  chartConfig?: Record<string, any>;
  tableConfig?: Record<string, any>;
  textConfig?: Record<string, any>;
  imageConfig?: Record<string, any>;
}

export interface ReportShare {
  shareId: number;
  reportId: number;
  shareCode: string;
  shareTitle: string;
  shareDescription: string;
  accessPassword?: string;
  shareType: 'PUBLIC' | 'PRIVATE' | 'PASSWORD';
  expireTime?: string;
  maxAccessCount?: number;
  currentAccessCount: number;
  allowExport: boolean;
  allowedFormats?: string;
  parametersConfig?: string;
  permissionsConfig?: string;
  isActive: boolean;
  createdBy: number;
  createdTime: string;
  updatedBy: number;
  updatedTime: string;
  createdByName?: string;
  reportName?: string;
}

export interface ShareValidationResult {
  valid: boolean;
  message?: string;
  requirePassword?: boolean;
  share?: ReportShare;
}

export interface ExportOptions {
  pageSize?: 'A4' | 'A3' | 'LETTER' | 'LEGAL';
  orientation?: 'PORTRAIT' | 'LANDSCAPE';
  width?: number;
  height?: number;
  sheetName?: string;
  delimiter?: ',' | ';' | '\t' | '|';
  includeHeader?: boolean;
  quality?: number;
}