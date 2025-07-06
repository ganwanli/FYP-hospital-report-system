import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'
import { immer } from 'zustand/middleware/immer'
import type { UserInfo, LoginResponse, AuthState } from '@/types'
import { authAPI } from '@/services/auth'

export const useAuthStore = create<AuthState>()(
  devtools(
    persist(
      immer((set, get) => ({
        // 初始状态
        isAuthenticated: false,
        userInfo: null,
        token: null,
        refreshToken: null,
        permissions: [],
        roles: [],

        // 登录
        login: (loginData: LoginResponse) => {
          set((state) => {
            state.isAuthenticated = true
            state.userInfo = loginData.userInfo
            state.token = loginData.token
            state.refreshToken = loginData.refreshToken
            state.permissions = loginData.userInfo.permissions || []
            state.roles = loginData.userInfo.roles || []
            
            // 存储token到localStorage
            localStorage.setItem('token', loginData.token)
            localStorage.setItem('refreshToken', loginData.refreshToken)
          })
        },

        // 登出
        logout: () => {
          set((state) => {
            state.isAuthenticated = false
            state.userInfo = null
            state.token = null
            state.refreshToken = null
            state.permissions = []
            state.roles = []
            
            // 清除localStorage中的token
            localStorage.removeItem('token')
            localStorage.removeItem('refreshToken')
          })
        },

        // 设置token
        setToken: (token: string) => {
          set((state) => {
            state.token = token
            localStorage.setItem('token', token)
          })
        },

        // 设置用户信息
        setUserInfo: (userInfo: UserInfo) => {
          set((state) => {
            state.userInfo = userInfo
            state.permissions = userInfo.permissions || []
            state.roles = userInfo.roles || []
          })
        },

        // 检查权限
        hasPermission: (permission: string | string[]) => {
          const { permissions, roles } = get()
          
          // 超级管理员拥有所有权限
          if (roles.includes('SUPER_ADMIN') || roles.includes('admin')) {
            return true
          }
          
          if (Array.isArray(permission)) {
            return permission.some(p => permissions.includes(p))
          }
          
          return permissions.includes(permission)
        },

        // 检查角色
        hasRole: (role: string | string[]) => {
          const { roles } = get()
          
          if (Array.isArray(role)) {
            return role.some(r => roles.includes(r))
          }
          
          return roles.includes(role)
        },

        // 清除认证信息
        clearAuth: () => {
          set((state) => {
            state.isAuthenticated = false
            state.userInfo = null
            state.token = null
            state.refreshToken = null
            state.permissions = []
            state.roles = []
            
            // 清除localStorage中的token
            localStorage.removeItem('token')
            localStorage.removeItem('refreshToken')
          })
        },

        // 更新token
        updateToken: (token: string, refreshToken?: string) => {
          set((state) => {
            state.token = token
            if (refreshToken) {
              state.refreshToken = refreshToken
            }
            localStorage.setItem('token', token)
            if (refreshToken) {
              localStorage.setItem('refreshToken', refreshToken)
            }
          })
        },

        // 刷新token
        refreshUserToken: async () => {
          const { refreshToken } = get()
          if (!refreshToken) {
            throw new Error('No refresh token available')
          }
          
          try {
            const response = await authAPI.refreshToken(refreshToken)
            if (response.code === 200) {
              set((state) => {
                state.token = response.data.token
                state.refreshToken = response.data.refreshToken
                localStorage.setItem('token', response.data.token)
                localStorage.setItem('refreshToken', response.data.refreshToken)
              })
            } else {
              throw new Error(response.message || '刷新token失败')
            }
          } catch (error) {
            // 刷新失败，清除认证信息
            get().logout()
            throw error
          }
        },
      })),
      {
        name: 'auth-store',
        partialize: (state) => ({
          isAuthenticated: state.isAuthenticated,
          userInfo: state.userInfo,
          token: state.token,
          refreshToken: state.refreshToken,
          permissions: state.permissions,
          roles: state.roles,
        }),
      }
    ),
    {
      name: 'auth-store',
    }
  )
)