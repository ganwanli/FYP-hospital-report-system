import request from '../utils/request';

export interface ReportConfig {
  reportId: number;
  reportName: string;
  reportDescription: string;
  reportCategory: string;
  reportType: string;
  layoutConfig: string;
  componentsConfig: string;
  dataSourcesConfig: string;
  styleConfig: string;
  canvasWidth: number;
  canvasHeight: number;
  isPublished: boolean;
  isActive: boolean;
  createdBy: number;
  createdTime: string;
  updatedBy: number;
  updatedTime: string;
  publishedTime: string;
  version: string;
  tags: string;
  accessLevel: string;
  refreshInterval: number;
  thumbnail: string;
  createdByName: string;
  updatedByName: string;
  components: ReportComponent[];
  dataSources: ReportDataSource[];
}

export interface ReportComponent {
  componentId: number;
  reportId: number;
  componentType: string;
  componentName: string;
  positionX: number;
  positionY: number;
  width: number;
  height: number;
  zIndex: number;
  dataSourceId: number;
  dataConfig: string;
  styleConfig: string;
  chartConfig: string;
  tableConfig: string;
  textConfig: string;
  imageConfig: string;
  isVisible: boolean;
  isLocked: boolean;
  componentOrder: number;
  parentComponentId: number;
  createdTime: string;
  updatedTime: string;
  conditionsConfig: string;
  interactionConfig: string;
}

export interface ReportDataSource {
  dataSourceId: number;
  reportId: number;
  sourceName: string;
  sourceType: string;
  connectionConfig: string;
  queryConfig: string;
  sqlTemplateId: number;
  apiConfig: string;
  staticData: string;
  refreshInterval: number;
  cacheEnabled: boolean;
  cacheDuration: number;
  parametersConfig: string;
  transformConfig: string;
  isActive: boolean;
  createdTime: string;
  updatedTime: string;
  lastRefreshTime: string;
  errorMessage: string;
  errorCount: number;
}

export interface ComponentType {
  type: string;
  name: string;
  icon: string;
  category: string;
  description: string;
  defaultProps: Record<string, any>;
}

// Report API
export const reportApi = {
  // Report CRUD
  createReport: (report: Partial<ReportConfig>) =>
    request.post<ReportConfig>('/api/reports', report),

  updateReport: (id: number, report: Partial<ReportConfig>) =>
    request.put<ReportConfig>(`/api/reports/${id}`, report),

  deleteReport: (id: number) =>
    request.delete(`/api/reports/${id}`),

  getReport: (id: number) =>
    request.get<ReportConfig>(`/api/reports/${id}`),

  getReportWithComponents: (id: number) =>
    request.get<ReportConfig>(`/api/reports/${id}/full`),

  getReportList: (params: {
    page?: number;
    size?: number;
    reportName?: string;
    reportCategory?: string;
    reportType?: string;
    isPublished?: boolean;
    isActive?: boolean;
    createdBy?: number;
    accessLevel?: string;
  }) =>
    request.get<{
      records: ReportConfig[];
      total: number;
      current: number;
      size: number;
    }>('/api/reports', { params }),

  searchReports: (keyword: string) =>
    request.get<ReportConfig[]>('/api/reports/search', { params: { keyword } }),

  // Component management
  addComponent: (reportId: number, component: Partial<ReportComponent>) =>
    request.post<ReportComponent>(`/api/reports/${reportId}/components`, component),

  updateComponent: (componentId: number, component: Partial<ReportComponent>) =>
    request.put<ReportComponent>(`/api/reports/components/${componentId}`, component),

  deleteComponent: (componentId: number) =>
    request.delete(`/api/reports/components/${componentId}`),

  getComponents: (reportId: number) =>
    request.get<ReportComponent[]>(`/api/reports/${reportId}/components`),

  updateComponentPosition: (componentId: number, x: number, y: number, width: number, height: number) =>
    request.put(`/api/reports/components/${componentId}/position`, null, {
      params: { x, y, width, height }
    }),

  updateComponentZIndex: (componentId: number, zIndex: number) =>
    request.put(`/api/reports/components/${componentId}/z-index`, null, {
      params: { zIndex }
    }),

  updateComponentVisibility: (componentId: number, isVisible: boolean) =>
    request.put(`/api/reports/components/${componentId}/visibility`, null, {
      params: { isVisible }
    }),

  updateComponentLock: (componentId: number, isLocked: boolean) =>
    request.put(`/api/reports/components/${componentId}/lock`, null, {
      params: { isLocked }
    }),

  // Data source management
  addDataSource: (reportId: number, dataSource: Partial<ReportDataSource>) =>
    request.post<ReportDataSource>(`/api/reports/${reportId}/datasources`, dataSource),

  updateDataSource: (dataSourceId: number, dataSource: Partial<ReportDataSource>) =>
    request.put<ReportDataSource>(`/api/reports/datasources/${dataSourceId}`, dataSource),

  deleteDataSource: (dataSourceId: number) =>
    request.delete(`/api/reports/datasources/${dataSourceId}`),

  getDataSources: (reportId: number) =>
    request.get<ReportDataSource[]>(`/api/reports/${reportId}/datasources`),

  testDataSource: (dataSourceId: number) =>
    request.post<Record<string, any>>(`/api/reports/datasources/${dataSourceId}/test`),

  refreshDataSource: (dataSourceId: number) =>
    request.post(`/api/reports/datasources/${dataSourceId}/refresh`),

  previewDataSource: (dataSourceId: number, limit = 100) =>
    request.get<Record<string, any>>(`/api/reports/datasources/${dataSourceId}/preview`, {
      params: { limit }
    }),

  getDataSourceSchema: (dataSourceId: number) =>
    request.get<Record<string, any>>(`/api/reports/datasources/${dataSourceId}/schema`),

  // Report preview and rendering
  previewReport: (reportId: number, parameters?: Record<string, any>) =>
    request.post<Record<string, any>>(`/api/reports/${reportId}/preview`, parameters),

  renderReport: (reportId: number, parameters?: Record<string, any>) =>
    request.post<Record<string, any>>(`/api/reports/${reportId}/render`, parameters),

  // Publishing and sharing
  publishReport: (reportId: number) =>
    request.post(`/api/reports/${reportId}/publish`),

  unpublishReport: (reportId: number) =>
    request.post(`/api/reports/${reportId}/unpublish`),

  // Report operations
  duplicateReport: (reportId: number, newName: string, userId: number) =>
    request.post<ReportConfig>(`/api/reports/${reportId}/duplicate`, null, {
      params: { newName, userId }
    }),

  exportReport: (reportId: number) =>
    request.get<string>(`/api/reports/${reportId}/export`),

  importReport: (reportData: string, userId: number) =>
    request.post<ReportConfig>('/api/reports/import', null, {
      params: { reportData, userId }
    }),

  updateThumbnail: (reportId: number, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return request.post(`/api/reports/${reportId}/thumbnail`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  // Statistics and metadata
  getReportStatistics: () =>
    request.get<Record<string, any>>('/api/reports/statistics'),

  getAllCategories: () =>
    request.get<string[]>('/api/reports/categories'),

  getAllTypes: () =>
    request.get<string[]>('/api/reports/types'),

  getReportTemplates: () =>
    request.get<ReportConfig[]>('/api/reports/templates'),

  // Version management
  saveVersion: (reportId: number, description: string, userId: number) =>
    request.post(`/api/reports/${reportId}/versions`, null, {
      params: { description, userId }
    }),

  getReportVersions: (reportId: number) =>
    request.get<Array<Record<string, any>>>(`/api/reports/${reportId}/versions`),

  restoreVersion: (reportId: number, versionId: number) =>
    request.post(`/api/reports/${reportId}/versions/${versionId}/restore`)
};

// Component types definition
export const componentTypes: ComponentType[] = [
  {
    type: 'table',
    name: '表格',
    icon: 'table',
    category: 'data',
    description: '数据表格组件',
    defaultProps: {
      width: 400,
      height: 300,
      pagination: true,
      showHeader: true,
      bordered: true,
      size: 'middle'
    }
  },
  {
    type: 'bar-chart',
    name: '柱状图',
    icon: 'bar-chart',
    category: 'chart',
    description: '柱状图表组件',
    defaultProps: {
      width: 400,
      height: 300,
      xField: '',
      yField: '',
      title: '',
      showLegend: true
    }
  },
  {
    type: 'line-chart',
    name: '折线图',
    icon: 'line-chart',
    category: 'chart',
    description: '折线图表组件',
    defaultProps: {
      width: 400,
      height: 300,
      xField: '',
      yField: '',
      title: '',
      showLegend: true,
      smooth: true
    }
  },
  {
    type: 'pie-chart',
    name: '饼图',
    icon: 'pie-chart',
    category: 'chart',
    description: '饼图表组件',
    defaultProps: {
      width: 400,
      height: 300,
      angleField: '',
      colorField: '',
      title: '',
      showLegend: true,
      radius: 0.8
    }
  },
  {
    type: 'text',
    name: '文本',
    icon: 'font-size',
    category: 'basic',
    description: '文本组件',
    defaultProps: {
      width: 200,
      height: 50,
      content: '文本内容',
      fontSize: 14,
      fontWeight: 'normal',
      color: '#000000',
      textAlign: 'left'
    }
  },
  {
    type: 'image',
    name: '图片',
    icon: 'picture',
    category: 'basic',
    description: '图片组件',
    defaultProps: {
      width: 200,
      height: 150,
      src: '',
      alt: '图片',
      objectFit: 'cover'
    }
  },
  {
    type: 'divider',
    name: '分割线',
    icon: 'minus',
    category: 'basic',
    description: '分割线组件',
    defaultProps: {
      width: 300,
      height: 1,
      color: '#d9d9d9',
      style: 'solid'
    }
  }
];