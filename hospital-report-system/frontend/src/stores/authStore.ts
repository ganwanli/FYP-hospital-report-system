import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'
import { immer } from 'zustand/middleware/immer'
import type { User, LoginResponse } from '@/types'

interface AuthState {
  // 状态
  isAuthenticated: boolean
  user: User | null
  token: string | null
  refreshToken: string | null
  permissions: string[]
  
  // 操作
  login: (loginData: LoginResponse) => void
  logout: () => void
  updateUser: (user: Partial<User>) => void
  updateToken: (token: string, refreshToken?: string) => void
  hasPermission: (permission: string | string[]) => boolean
  hasRole: (roleCode: string | string[]) => boolean
  clearAuth: () => void
}

export const useAuthStore = create<AuthState>()(
  devtools(
    persist(
      immer((set, get) => ({
        // 初始状态
        isAuthenticated: false,
        user: null,
        token: null,
        refreshToken: null,
        permissions: [],

        // 登录
        login: (loginData: LoginResponse) => {
          set((state) => {
            state.isAuthenticated = true
            state.user = loginData.user
            state.token = loginData.token
            state.refreshToken = loginData.refreshToken
            state.permissions = loginData.user.permissions || []
            
            // 存储token到localStorage
            localStorage.setItem('token', loginData.token)
            localStorage.setItem('refreshToken', loginData.refreshToken)
          })
        },

        // 登出
        logout: () => {
          set((state) => {
            state.isAuthenticated = false
            state.user = null
            state.token = null
            state.refreshToken = null
            state.permissions = []
            
            // 清除localStorage中的token
            localStorage.removeItem('token')
            localStorage.removeItem('refreshToken')
          })
        },

        // 更新用户信息
        updateUser: (userData: Partial<User>) => {
          set((state) => {
            if (state.user) {
              Object.assign(state.user, userData)
              if (userData.permissions) {
                state.permissions = userData.permissions
              }
            }
          })
        },

        // 更新token
        updateToken: (token: string, refreshToken?: string) => {
          set((state) => {
            state.token = token
            if (refreshToken) {
              state.refreshToken = refreshToken
            }
            
            // 更新localStorage中的token
            localStorage.setItem('token', token)
            if (refreshToken) {
              localStorage.setItem('refreshToken', refreshToken)
            }
          })
        },

        // 检查权限
        hasPermission: (permission: string | string[]) => {
          const { permissions, user } = get()
          
          // 超级管理员拥有所有权限
          if (user?.roles?.some(role => role.roleCode === 'SUPER_ADMIN')) {
            return true
          }
          
          if (Array.isArray(permission)) {
            return permission.some(p => permissions.includes(p))
          }
          
          return permissions.includes(permission)
        },

        // 检查角色
        hasRole: (roleCode: string | string[]) => {
          const { user } = get()
          
          if (!user?.roles) return false
          
          const userRoles = user.roles.map(role => role.roleCode)
          
          if (Array.isArray(roleCode)) {
            return roleCode.some(code => userRoles.includes(code))
          }
          
          return userRoles.includes(roleCode)
        },

        // 清除认证信息
        clearAuth: () => {
          set((state) => {
            state.isAuthenticated = false
            state.user = null
            state.token = null
            state.refreshToken = null
            state.permissions = []
            
            // 清除localStorage
            localStorage.removeItem('token')
            localStorage.removeItem('refreshToken')
          })
        },
      })),
      {
        name: 'auth-store',
        partialize: (state) => ({
          isAuthenticated: state.isAuthenticated,
          user: state.user,
          token: state.token,
          refreshToken: state.refreshToken,
          permissions: state.permissions,
        }),
      }
    ),
    {
      name: 'auth-store',
    }
  )
)