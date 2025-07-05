import React, { useEffect } from 'react'
import { Breadcrumb } from 'antd'
import { useLocation, Link } from 'react-router-dom'
import { HomeOutlined } from '@ant-design/icons'
import { useAppStore } from '@/stores/appStore'
import './index.css'

interface BreadcrumbItem {
  path: string
  name: string
  icon?: React.ReactNode
}

const BreadcrumbComponent: React.FC = () => {
  const location = useLocation()
  const { breadcrumbs, setBreadcrumbs } = useAppStore()

  // 路由与面包屑映射
  const routeMap: Record<string, BreadcrumbItem> = {
    '/dashboard': { path: '/dashboard', name: '仪表板', icon: <HomeOutlined /> },
    '/system': { path: '/system', name: '系统管理' },
    '/system/user': { path: '/system/user', name: '用户管理' },
    '/system/role': { path: '/system/role', name: '角色管理' },
    '/system/dept': { path: '/system/dept', name: '部门管理' },
    '/datasource': { path: '/datasource', name: '数据源管理' },
    '/datasource/config': { path: '/datasource/config', name: '数据源配置' },
    '/template': { path: '/template', name: 'SQL模板管理' },
    '/template/editor': { path: '/template/editor', name: 'SQL编辑器' },
    '/report': { path: '/report', name: '报表管理' },
    '/report/config': { path: '/report/config', name: '报表配置' },
    '/report/view': { path: '/report/view', name: '报表查看' },
    '/report/analysis': { path: '/report/analysis', name: '报表分析' },
  }

  // 生成面包屑路径
  const generateBreadcrumbs = (pathname: string): BreadcrumbItem[] => {
    const paths = pathname.split('/').filter(Boolean)
    const breadcrumbItems: BreadcrumbItem[] = []
    
    // 始终添加首页
    if (pathname !== '/dashboard') {
      breadcrumbItems.push(routeMap['/dashboard'])
    }
    
    // 构建路径
    let currentPath = ''
    paths.forEach(path => {
      currentPath += `/${path}`
      const item = routeMap[currentPath]
      if (item) {
        breadcrumbItems.push(item)
      }
    })
    
    return breadcrumbItems
  }

  // 更新面包屑
  useEffect(() => {
    const newBreadcrumbs = generateBreadcrumbs(location.pathname)
    setBreadcrumbs(newBreadcrumbs)
  }, [location.pathname, setBreadcrumbs])

  // 生成面包屑项
  const items = breadcrumbs.map((item, index) => {
    const isLast = index === breadcrumbs.length - 1
    
    return {
      key: item.path,
      title: isLast ? (
        <span className="breadcrumb-current">
          {item.icon}
          {item.name}
        </span>
      ) : (
        <Link to={item.path} className="breadcrumb-link">
          {item.icon}
          {item.name}
        </Link>
      ),
    }
  })

  if (breadcrumbs.length === 0) {
    return null
  }

  return (
    <div className="breadcrumb-container">
      <Breadcrumb items={items} />
    </div>
  )
}

export default BreadcrumbComponent