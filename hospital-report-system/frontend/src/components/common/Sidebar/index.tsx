import React from 'react'
import { Layout, Menu } from 'antd'
import { useNavigate, useLocation } from 'react-router-dom'
import {
  DashboardOutlined,
  UserOutlined,
  TeamOutlined,
  ApartmentOutlined,
  DatabaseOutlined,
  CodeOutlined,
  BarChartOutlined,
  FileTextOutlined,
  LineChartOutlined,
  SettingOutlined,
} from '@ant-design/icons'
import { useAppStore } from '@/stores/appStore'
import { useAuthStore } from '@/stores/authStore'
import type { MenuProps } from 'antd'
import './index.css'

const { Sider } = Layout

type MenuItem = Required<MenuProps>['items'][number]

interface SidebarProps {
  className?: string
}

const Sidebar: React.FC<SidebarProps> = ({ className }) => {
  const navigate = useNavigate()
  const location = useLocation()
  const { sidebarCollapsed } = useAppStore()
  const { hasPermission } = useAuthStore()

  // 菜单项配置
  const menuItems: MenuItem[] = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: '仪表板',
    },
    hasPermission(['USER_MANAGE', 'ROLE_MANAGE', 'DEPT_MANAGE']) && {
      key: '/system',
      icon: <SettingOutlined />,
      label: '系统管理',
      children: [
        hasPermission(['USER_MANAGE']) && {
          key: '/system/user',
          icon: <UserOutlined />,
          label: '用户管理',
        },
        hasPermission(['ROLE_MANAGE']) && {
          key: '/system/role',
          icon: <TeamOutlined />,
          label: '角色管理',
        },
        hasPermission(['DEPT_MANAGE']) && {
          key: '/system/dept',
          icon: <ApartmentOutlined />,
          label: '部门管理',
        },
      ].filter(Boolean),
    },
    hasPermission(['DATASOURCE_MANAGE']) && {
      key: '/datasource',
      icon: <DatabaseOutlined />,
      label: '数据源管理',
      children: [
        {
          key: '/datasource',
          icon: <DatabaseOutlined />,
          label: '数据源列表',
        },
      ],
    },
    hasPermission(['SQL_TEMPLATE_MANAGE']) && {
      key: '/template',
      icon: <CodeOutlined />,
      label: 'SQL模板',
      children: [
        {
          key: '/template',
          icon: <CodeOutlined />,
          label: '模板管理',
        },
      ],
    },
    hasPermission(['REPORT_CONFIG', 'REPORT_QUERY', 'REPORT_ANALYSIS']) && {
      key: '/report',
      icon: <BarChartOutlined />,
      label: '报表管理',
      children: [
        hasPermission(['REPORT_CONFIG']) && {
          key: '/report/config',
          icon: <FileTextOutlined />,
          label: '报表配置',
        },
        hasPermission(['REPORT_QUERY']) && {
          key: '/report/view',
          icon: <BarChartOutlined />,
          label: '报表查看',
        },
        hasPermission(['REPORT_ANALYSIS']) && {
          key: '/report/analysis',
          icon: <LineChartOutlined />,
          label: '报表分析',
        },
      ].filter(Boolean),
    },
  ].filter(Boolean) as MenuItem[]

  // 获取当前选中的菜单
  const getSelectedKeys = () => {
    const { pathname } = location
    return [pathname]
  }

  // 获取当前展开的菜单
  const getOpenKeys = () => {
    const { pathname } = location
    const openKeys: string[] = []
    
    if (pathname.startsWith('/system')) {
      openKeys.push('/system')
    } else if (pathname.startsWith('/datasource')) {
      openKeys.push('/datasource')
    } else if (pathname.startsWith('/template')) {
      openKeys.push('/template')
    } else if (pathname.startsWith('/report')) {
      openKeys.push('/report')
    }
    
    return openKeys
  }

  // 处理菜单点击
  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    navigate(key)
  }

  return (
    <Sider
      trigger={null}
      collapsible
      collapsed={sidebarCollapsed}
      width={256}
      className={`layout-sidebar ${className || ''}`}
    >
      <div className="sidebar-logo">
        <div className="logo-icon">
          <BarChartOutlined />
        </div>
        {!sidebarCollapsed && (
          <div className="logo-text">
            报表系统
          </div>
        )}
      </div>
      
      <Menu
        theme="dark"
        mode="inline"
        selectedKeys={getSelectedKeys()}
        defaultOpenKeys={getOpenKeys()}
        items={menuItems}
        onClick={handleMenuClick}
        className="sidebar-menu"
      />
    </Sider>
  )
}

export default Sidebar