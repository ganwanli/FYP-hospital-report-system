import { create } from 'zustand'
import { devtools } from 'zustand/middleware'
import { immer } from 'zustand/middleware/immer'

interface AppState {
  // 全局loading状态
  loading: boolean
  
  // 侧边栏状态
  sidebarCollapsed: boolean
  
  // 主题配置
  theme: {
    primaryColor: string
    borderRadius: number
    colorBgBase: string
    colorTextBase: string
    algorithm: 'light' | 'dark'
  }
  
  // 面包屑导航
  breadcrumbs: Array<{
    path: string
    name: string
  }>
  
  // 操作
  setLoading: (loading: boolean) => void
  toggleSidebar: () => void
  setSidebarCollapsed: (collapsed: boolean) => void
  updateTheme: (theme: Partial<AppState['theme']>) => void
  setBreadcrumbs: (breadcrumbs: AppState['breadcrumbs']) => void
  addBreadcrumb: (breadcrumb: { path: string; name: string }) => void
}

export const useAppStore = create<AppState>()(
  devtools(
    immer((set) => ({
      // 初始状态
      loading: false,
      sidebarCollapsed: false,
      theme: {
        primaryColor: '#1890ff',
        borderRadius: 6,
        colorBgBase: '#ffffff',
        colorTextBase: '#000000',
        algorithm: 'light',
      },
      breadcrumbs: [],

      // 设置全局loading
      setLoading: (loading: boolean) => {
        set((state) => {
          state.loading = loading
        })
      },

      // 切换侧边栏状态
      toggleSidebar: () => {
        set((state) => {
          state.sidebarCollapsed = !state.sidebarCollapsed
        })
      },

      // 设置侧边栏状态
      setSidebarCollapsed: (collapsed: boolean) => {
        set((state) => {
          state.sidebarCollapsed = collapsed
        })
      },

      // 更新主题
      updateTheme: (themeUpdate) => {
        set((state) => {
          Object.assign(state.theme, themeUpdate)
        })
      },

      // 设置面包屑
      setBreadcrumbs: (breadcrumbs) => {
        set((state) => {
          state.breadcrumbs = breadcrumbs
        })
      },

      // 添加面包屑
      addBreadcrumb: (breadcrumb) => {
        set((state) => {
          const existingIndex = state.breadcrumbs.findIndex(
            item => item.path === breadcrumb.path
          )
          
          if (existingIndex >= 0) {
            // 如果已存在，移除后面的面包屑
            state.breadcrumbs = state.breadcrumbs.slice(0, existingIndex + 1)
          } else {
            // 如果不存在，添加到末尾
            state.breadcrumbs.push(breadcrumb)
          }
        })
      },
    })),
    {
      name: 'app-store',
    }
  )
)