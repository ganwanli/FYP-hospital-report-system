import React from 'react'
import { Layout } from 'antd'
import './index.css'

const { Content } = Layout

interface AuthLayoutProps {
  children: React.ReactNode
}

const AuthLayout: React.FC<AuthLayoutProps> = ({ children }) => {
  return (
    <Layout className="auth-layout">
      <Content className="auth-content">
        <div className="auth-container">
          <div className="auth-background">
            <div className="auth-background-overlay" />
          </div>
          <div className="auth-form-wrapper">
            {children}
          </div>
        </div>
      </Content>
    </Layout>
  )
}

export default AuthLayout