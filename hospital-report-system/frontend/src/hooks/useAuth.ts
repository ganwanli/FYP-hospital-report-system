import { useEffect } from 'react'
import { useAuthStore } from '@/stores/authStore'
import { authAPI } from '@/services/auth'
import { message } from 'antd'

export const useAuth = () => {
  const authStore = useAuthStore()

  const login = async (loginData: any) => {
    try {
      const response = await authAPI.login(loginData)
      if (response.code === 200) {
        authStore.login(response.data)
        return { success: true, data: response.data }
      } else {
        throw new Error(response.message || '登录失败')
      }
    } catch (error: any) {
      return { success: false, error: error.message }
    }
  }

  const logout = async () => {
    try {
      await authAPI.logout()
      authStore.logout()
      return { success: true }
    } catch (error: any) {
      authStore.logout()
      return { success: false, error: error.message }
    }
  }

  const refreshToken = async () => {
    try {
      if (!authStore.refreshToken) {
        throw new Error('No refresh token available')
      }
      
      const response = await authAPI.refreshToken(authStore.refreshToken)
      if (response.code === 200) {
        authStore.setToken(response.data.token)
        return { success: true, data: response.data }
      } else {
        throw new Error(response.message || '刷新token失败')
      }
    } catch (error: any) {
      authStore.logout()
      return { success: false, error: error.message }
    }
  }

  const getUserInfo = async () => {
    try {
      const response = await authAPI.getUserInfo()
      if (response.code === 200) {
        authStore.setUserInfo(response.data)
        return { success: true, data: response.data }
      } else {
        throw new Error(response.message || '获取用户信息失败')
      }
    } catch (error: any) {
      return { success: false, error: error.message }
    }
  }

  const checkTokenExpiry = () => {
    if (!authStore.token) return false
    
    try {
      const payload = JSON.parse(atob(authStore.token.split('.')[1]))
      const currentTime = Date.now() / 1000
      
      return payload.exp > currentTime
    } catch (error) {
      return false
    }
  }

  const autoRefreshToken = async () => {
    if (!authStore.isAuthenticated) return
    
    if (!checkTokenExpiry()) {
      const result = await refreshToken()
      if (!result.success) {
        message.error('登录已过期，请重新登录')
        authStore.logout()
      }
    }
  }

  useEffect(() => {
    const interval = setInterval(autoRefreshToken, 5 * 60 * 1000) // 每5分钟检查一次
    return () => clearInterval(interval)
  }, [authStore.isAuthenticated])

  return {
    ...authStore,
    login,
    logout,
    refreshToken,
    getUserInfo,
    checkTokenExpiry,
    autoRefreshToken
  }
}

export default useAuth