import { http } from '@/utils/request'
import type { LoginRequest, LoginResponse, RefreshTokenResponse, User } from '@/types'

export const authApi = {
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
  getUserInfo: (): Promise<User> => {
    return http.get('/auth/user')
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