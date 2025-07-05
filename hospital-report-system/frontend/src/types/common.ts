export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageParams {
  current: number
  pageSize: number
  total?: number
}

export interface PageResult<T> {
  records: T[]
  current: number
  pages: number
  size: number
  total: number
}

export interface RequestOptions {
  showLoading?: boolean
  showError?: boolean
  errorMessage?: string
}

export interface UploadFile {
  uid: string
  name: string
  status: 'uploading' | 'done' | 'error' | 'removed'
  url?: string
  response?: any
}

export interface MenuData {
  key: string
  path: string
  name: string
  icon?: string
  component?: string
  hideInMenu?: boolean
  authority?: string[]
  children?: MenuData[]
}

export interface RouteConfig {
  path: string
  component: React.ComponentType
  exact?: boolean
  authority?: string[]
  hideInMenu?: boolean
  name?: string
  icon?: string
}

export interface ThemeConfig {
  primaryColor: string
  borderRadius: number
  colorBgBase: string
  colorTextBase: string
}