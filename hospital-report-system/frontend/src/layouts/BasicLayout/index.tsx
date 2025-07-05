import React from 'react'
import { Layout } from 'antd'
import { Outlet } from 'react-router-dom'
import Header from '@/components/common/Header'
import Sidebar from '@/components/common/Sidebar'
import Breadcrumb from '@/components/common/Breadcrumb'
import { useAppStore } from '@/stores/appStore'
import './index.css'

const { Content } = Layout

const BasicLayout: React.FC = () => {
  const { sidebarCollapsed } = useAppStore()

  return (
    <Layout className="basic-layout">
      <Sidebar />
      <Layout className={`main-layout ${sidebarCollapsed ? 'collapsed' : ''}`}>
        <Header />
        <Breadcrumb />
        <Content className="main-content">
          <div className="content-wrapper">
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  )
}

export default BasicLayout