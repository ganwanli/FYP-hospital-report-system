import { http } from '@/utils/request'
import type { LoginRequest, LoginResponse, RefreshTokenResponse, UserInfo } from '@/types'

export const authAPI = {
  // 登录
  login: (data: LoginRequest): Promise<LoginResponse> => {
    return http.post('/auth/login', data)
  },
  
  // 登出
  logout: (): Promise<void> => {
    return http.post('/auth/logout')
  },
  
  // 刷新token
  refreshToken: (refreshToken: string): Promise<RefreshTokenResponse> => {
    return http.post('/auth/refresh', { refreshToken })
  },
  
  // 获取用户信息
  getUserInfo: (): Promise<UserInfo> => {
    return http.get('/auth/me')
  },
  
  // 获取用户权限
  getUserPermissions: (): Promise<string[]> => {
    return http.get('/auth/permissions')
  },
  
  // 检查权限
  checkPermission: (permission: string): Promise<boolean> => {
    return http.get(`/auth/check-permission?permission=${permission}`)
  },
  
  // 修改密码
  changePassword: (data: {
    oldPassword: string
    newPassword: string
    confirmPassword: string
  }): Promise<void> => {
    return http.post('/auth/change-password', data)
  },
}