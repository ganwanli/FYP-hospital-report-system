import axios, { AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'
import { message } from 'antd'
import { useAuthStore } from '@/stores/authStore'
import { useAppStore } from '@/stores/appStore'
import type { ApiResponse, RequestOptions } from '@/types'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 显示loading
    const options = config.metadata as RequestOptions
    if (options?.showLoading !== false) {
      useAppStore.getState().setLoading(true)
    }
    
    return config
  },
  (error: AxiosError) => {
    useAppStore.getState().setLoading(false)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // 隐藏loading
    useAppStore.getState().setLoading(false)
    
    const { code, message: msg, data } = response.data
    
    // 处理业务错误
    if (code !== 200) {
      const options = response.config.metadata as RequestOptions
      
      if (options?.showError !== false) {
        message.error(options?.errorMessage || msg || '请求失败')
      }
      
      // 处理认证失败
      if (code === 401) {
        useAuthStore.getState().clearAuth()
        window.location.href = '/login'
        return Promise.reject(new Error('认证失败'))
      }
      
      // 处理权限不足
      if (code === 403) {
        message.error('权限不足，访问被拒绝')
        return Promise.reject(new Error('权限不足'))
      }
      
      return Promise.reject(new Error(msg || '请求失败'))
    }
    
    return data
  },
  async (error: AxiosError) => {
    useAppStore.getState().setLoading(false)
    
    const { response, code } = error
    const options = error.config?.metadata as RequestOptions
    
    // 网络错误
    if (code === 'ECONNABORTED' || code === 'NETWORK_ERROR') {
      if (options?.showError !== false) {
        message.error('网络错误，请检查网络连接')
      }
      return Promise.reject(error)
    }
    
    // HTTP错误
    if (response) {
      const { status, data } = response
      
      switch (status) {
        case 401:
          // token过期，尝试刷新token
          if (await refreshToken()) {
            // 重新发送原请求
            return request(error.config!)
          } else {
            useAuthStore.getState().clearAuth()
            window.location.href = '/login'
          }
          break
          
        case 403:
          message.error('权限不足，访问被拒绝')
          break
          
        case 404:
          message.error('请求的资源不存在')
          break
          
        case 500:
          message.error('服务器内部错误')
          break
          
        default:
          if (options?.showError !== false) {
            message.error(
              options?.errorMessage || 
              (data as any)?.message || 
              `请求失败 (${status})`
            )
          }
      }
    } else {
      if (options?.showError !== false) {
        message.error(options?.errorMessage || '请求失败')
      }
    }
    
    return Promise.reject(error)
  }
)

// 刷新token
const refreshToken = async (): Promise<boolean> => {
  try {
    const refreshToken = localStorage.getItem('refreshToken')
    if (!refreshToken) {
      return false
    }
    
    const response = await axios.post(
      `${import.meta.env.VITE_API_BASE_URL}/auth/refresh`,
      { refreshToken }
    )
    
    const { token, refreshToken: newRefreshToken } = response.data.data
    useAuthStore.getState().updateToken(token, newRefreshToken)
    
    return true
  } catch {
    return false
  }
}

// 扩展axios配置类型
declare module 'axios' {
  interface AxiosRequestConfig {
    metadata?: RequestOptions
  }
}

// 通用请求方法
export const http = {
  get: <T = any>(
    url: string, 
    config?: AxiosRequestConfig & { metadata?: RequestOptions }
  ): Promise<T> => {
    return request.get(url, config)
  },
  
  post: <T = any>(
    url: string, 
    data?: any, 
    config?: AxiosRequestConfig & { metadata?: RequestOptions }
  ): Promise<T> => {
    return request.post(url, data, config)
  },
  
  put: <T = any>(
    url: string, 
    data?: any, 
    config?: AxiosRequestConfig & { metadata?: RequestOptions }
  ): Promise<T> => {
    return request.put(url, data, config)
  },
  
  delete: <T = any>(
    url: string, 
    config?: AxiosRequestConfig & { metadata?: RequestOptions }
  ): Promise<T> => {
    return request.delete(url, config)
  },
  
  upload: <T = any>(
    url: string, 
    file: File, 
    config?: AxiosRequestConfig & { 
      metadata?: RequestOptions
      onUploadProgress?: (progressEvent: any) => void 
    }
  ): Promise<T> => {
    const formData = new FormData()
    formData.append('file', file)
    
    return request.post(url, formData, {
      ...config,
      headers: {
        'Content-Type': 'multipart/form-data',
        ...config?.headers,
      },
    })
  },
}

export default request