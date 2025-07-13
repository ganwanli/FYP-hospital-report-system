import React, { useState } from 'react'
import { Form, Input, Button, Checkbox, message, Typography, Divider } from 'antd'
import { UserOutlined, LockOutlined, LoginOutlined, EyeInvisibleOutlined, EyeTwoTone } from '@ant-design/icons'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import { authAPI } from '@/services/auth'
import { AuthLayout } from '@/layouts'
import type { LoginRequest } from '@/types/auth'
import './ModernLogin.css'

const { Title, Text } = Typography

const ModernLoginPage: React.FC = () => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useAuthStore()

  const from = location.state?.from?.pathname || '/'

  const onFinish = async (values: LoginRequest) => {
    setLoading(true)
    try {
      const response = await authAPI.login(values)
      if (response.code === 200) {
        login(response.data)
        message.success('登录成功')
        navigate(from, { replace: true })
      } else {
        message.error(response.message || '登录失败')
      }
    } catch (error: any) {
      message.error(error.message || '登录失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  const handleQuickLogin = (username: string, password: string) => {
    form.setFieldsValue({ username, password })
  }

  return (
    <div className="modern-login-container">
      {/* 背景装饰 */}
      <div className="background-decoration">
        <div className="floating-shape shape-1"></div>
        <div className="floating-shape shape-2"></div>
        <div className="floating-shape shape-3"></div>
        <div className="floating-shape shape-4"></div>
      </div>

      {/* 主要内容 */}
      <div className="login-content">
        {/* 左侧信息区域 */}
        <div className="login-info-section">
          <div className="info-content">
            <div className="logo-section">
              <div className="logo-icon">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14h-2v-2h2v2zm0-4h-2V7h2v6z"/>
                </svg>
              </div>
              <Title level={1} className="system-title">
                医院报告管理系统
              </Title>
              <Text className="system-subtitle">
                Hospital Report Management System
              </Text>
            </div>
            
            <div className="features-list">
              <div className="feature-item">
                <div className="feature-icon">📊</div>
                <div className="feature-text">
                  <Text strong>智能报告管理</Text>
                  <Text type="secondary">高效处理医疗报告数据</Text>
                </div>
              </div>
              <div className="feature-item">
                <div className="feature-icon">🔒</div>
                <div className="feature-text">
                  <Text strong>安全可靠</Text>
                  <Text type="secondary">保护患者隐私信息</Text>
                </div>
              </div>
              <div className="feature-item">
                <div className="feature-icon">⚡</div>
                <div className="feature-text">
                  <Text strong>快速响应</Text>
                  <Text type="secondary">实时数据同步更新</Text>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 右侧登录表单 */}
        <div className="login-form-section">
          <div className="form-container">
            <div className="form-header">
              <Title level={2} className="welcome-title">
                欢迎回来
              </Title>
              <Text type="secondary" className="welcome-subtitle">
                请登录您的账户以继续
              </Text>
            </div>

            <Form
              form={form}
              name="modern-login"
              onFinish={onFinish}
              autoComplete="off"
              size="large"
              className="modern-login-form"
            >
              <Form.Item
                name="username"
                rules={[
                  { required: true, message: '请输入用户名' },
                  { min: 3, message: '用户名至少3个字符' },
                  { max: 50, message: '用户名不能超过50个字符' }
                ]}
              >
                <Input
                  prefix={<UserOutlined className="input-icon" />}
                  placeholder="用户名"
                  autoComplete="username"
                  className="modern-input"
                />
              </Form.Item>

              <Form.Item
                name="password"
                rules={[
                  { required: true, message: '请输入密码' },
                  { min: 6, message: '密码至少6个字符' }
                ]}
              >
                <Input.Password
                  prefix={<LockOutlined className="input-icon" />}
                  placeholder="密码"
                  autoComplete="current-password"
                  className="modern-input"
                  iconRender={(visible) => (visible ? <EyeTwoTone /> : <EyeInvisibleOutlined />)}
                />
              </Form.Item>

              <Form.Item>
                <div className="form-options">
                  <Form.Item name="rememberMe" valuePropName="checked" noStyle>
                    <Checkbox className="remember-checkbox">记住我</Checkbox>
                  </Form.Item>
                  <a href="#" className="forgot-link">
                    忘记密码?
                  </a>
                </div>
              </Form.Item>

              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  loading={loading}
                  className="login-submit-btn"
                  icon={<LoginOutlined />}
                  block
                >
                  {loading ? '登录中...' : '登录'}
                </Button>
              </Form.Item>
            </Form>

            <Divider className="divider">
              <Text type="secondary">或</Text>
            </Divider>

            <div className="quick-login-section">
              <Text type="secondary" className="quick-login-title">
                快速登录
              </Text>
              <div className="quick-login-buttons">
                <Button
                  size="small"
                  className="quick-btn admin-btn"
                  onClick={() => handleQuickLogin('admin', 'admin123')}
                >
                  管理员
                </Button>
                <Button
                  size="small"
                  className="quick-btn doctor-btn"
                  onClick={() => handleQuickLogin('doctor', 'doctor123')}
                >
                  医生
                </Button>
                <Button
                  size="small"
                  className="quick-btn nurse-btn"
                  onClick={() => handleQuickLogin('nurse', 'nurse123')}
                >
                  护士
                </Button>
              </div>
            </div>

            <div className="form-footer">
              <Text type="secondary" className="footer-text">
                © 2024 Hospital Report Management System. All rights reserved.
              </Text>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default ModernLoginPage 