import React, { Suspense, useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { Spin } from 'antd'
import { useAuthStore } from '@/stores/authStore'
import { useAuth } from '@/hooks/useAuth'
import BasicLayout from '@/layouts/BasicLayout'
import AuthLayout from '@/layouts/AuthLayout'

// 懒加载页面组件
const Login = React.lazy(() => import('@/pages/Login'))
const Dashboard = React.lazy(() => import('@/pages/dashboard/Overview'))
const NotFound = React.lazy(() => import('@/pages/common/NotFound'))

// 系统管理页面
const UserManagement = React.lazy(() => import('@/pages/system/User'))
const RoleManagement = React.lazy(() => import('@/pages/system/Role'))
const DeptManagement = React.lazy(() => import('@/pages/system/Dept'))

// 数据源管理页面
const DatasourceList = React.lazy(() => import('@/pages/datasource/List'))
const DatasourceConfig = React.lazy(() => import('@/pages/datasource/Config'))

// SQL模板管理页面
const TemplateList = React.lazy(() => import('@/pages/template/List'))
const TemplateEditor = React.lazy(() => import('@/pages/template/Editor'))

// 报表管理页面
const ReportConfig = React.lazy(() => import('@/pages/report/Config'))
const ReportView = React.lazy(() => import('@/pages/report/View'))
const ReportAnalysis = React.lazy(() => import('@/pages/report/Analysis'))

// 数据字典管理页面
const DictionaryList = React.lazy(() => import('@/pages/dictionary/List'))

// 数据血缘追踪页面
const LineageManagement = React.lazy(() => import('@/pages/lineage/Management'))
const ImpactAnalysis = React.lazy(() => import('@/pages/lineage/Impact'))
const LineageSearch = React.lazy(() => import('@/pages/lineage/Search'))

// 加载组件
const PageLoading: React.FC = () => (
  <div style={{ 
    display: 'flex', 
    justifyContent: 'center', 
    alignItems: 'center', 
    height: '100vh' 
  }}>
    <Spin size="large" tip="页面加载中..." />
  </div>
)

// 路由守卫组件
const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuthStore()
  const location = useLocation()
  
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }
  
  return <>{children}</>
}

// 权限路由组件
const AuthorizedRoute: React.FC<{ 
  children: React.ReactNode
  authority?: string[]
}> = ({ children, authority }) => {
  const { hasPermission } = useAuthStore()
  
  if (authority && !hasPermission(authority)) {
    return <Navigate to="/403" replace />
  }
  
  return <>{children}</>
}

// 自动登录组件
const AutoLogin: React.FC = () => {
  const { isAuthenticated, token } = useAuthStore()
  const { getUserInfo, checkTokenExpiry } = useAuth()

  useEffect(() => {
    const initAuth = async () => {
      if (token && !isAuthenticated) {
        try {
          if (checkTokenExpiry()) {
            await getUserInfo()
          }
        } catch (error) {
          console.error('自动登录失败:', error)
        }
      }
    }

    initAuth()
  }, [])

  return null
}

const AppRouter: React.FC = () => {
  return (
    <BrowserRouter>
      <AutoLogin />
      <Suspense fallback={<PageLoading />}>
        <Routes>
          {/* 认证相关路由 */}
          <Route path="/login" element={
            <AuthLayout>
              <Login />
            </AuthLayout>
          } />
          
          {/* 主应用路由 */}
          <Route path="/" element={
            <PrivateRoute>
              <BasicLayout />
            </PrivateRoute>
          }>
            {/* 仪表板 */}
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="dashboard" element={<Dashboard />} />
            
            {/* 系统管理 */}
            <Route path="system">
              <Route path="user" element={
                <AuthorizedRoute authority={['USER_MANAGE']}>
                  <UserManagement />
                </AuthorizedRoute>
              } />
              <Route path="role" element={
                <AuthorizedRoute authority={['ROLE_MANAGE']}>
                  <RoleManagement />
                </AuthorizedRoute>
              } />
              <Route path="dept" element={
                <AuthorizedRoute authority={['DEPT_MANAGE']}>
                  <DeptManagement />
                </AuthorizedRoute>
              } />
            </Route>
            
            {/* 数据源管理 */}
            <Route path="datasource">
              <Route index element={
                <AuthorizedRoute authority={['DATASOURCE_MANAGE']}>
                  <DatasourceList />
                </AuthorizedRoute>
              } />
              <Route path="config/:id?" element={
                <AuthorizedRoute authority={['DATASOURCE_MANAGE']}>
                  <DatasourceConfig />
                </AuthorizedRoute>
              } />
            </Route>
            
            {/* SQL模板管理 */}
            <Route path="template">
              <Route index element={
                <AuthorizedRoute authority={['SQL_TEMPLATE_MANAGE']}>
                  <TemplateList />
                </AuthorizedRoute>
              } />
              <Route path="editor/:id?" element={
                <AuthorizedRoute authority={['SQL_TEMPLATE_MANAGE']}>
                  <TemplateEditor />
                </AuthorizedRoute>
              } />
            </Route>
            
            {/* 报表管理 */}
            <Route path="report">
              <Route path="config" element={
                <AuthorizedRoute authority={['REPORT_CONFIG']}>
                  <ReportConfig />
                </AuthorizedRoute>
              } />
              <Route path="view/:id?" element={
                <AuthorizedRoute authority={['REPORT_QUERY']}>
                  <ReportView />
                </AuthorizedRoute>
              } />
              <Route path="analysis" element={
                <AuthorizedRoute authority={['REPORT_ANALYSIS']}>
                  <ReportAnalysis />
                </AuthorizedRoute>
              } />
            </Route>

            {/* 数据字典管理 */}
            <Route path="dictionary">
              <Route index element={
                <AuthorizedRoute authority={['DICTIONARY_MANAGE']}>
                  <DictionaryList />
                </AuthorizedRoute>
              } />
            </Route>

            {/* 数据血缘追踪 */}
            <Route path="lineage">
              <Route index element={
                <AuthorizedRoute authority={['LINEAGE_MANAGE']}>
                  <LineageManagement />
                </AuthorizedRoute>
              } />
              <Route path="impact" element={
                <AuthorizedRoute authority={['LINEAGE_ANALYSIS']}>
                  <ImpactAnalysis />
                </AuthorizedRoute>
              } />
              <Route path="search" element={
                <AuthorizedRoute authority={['LINEAGE_QUERY']}>
                  <LineageSearch />
                </AuthorizedRoute>
              } />
            </Route>
          </Route>
          
          {/* 404页面 */}
          <Route path="*" element={<NotFound />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  )
}

export default AppRouter