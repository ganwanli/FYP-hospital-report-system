import React, { useState } from 'react'
import { Form, Input, Button, Checkbox, message, Card, Space, Typography } from 'antd'
import { UserOutlined, LockOutlined, LoginOutlined } from '@ant-design/icons'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import { authAPI } from '@/services/auth'
import { AuthLayout } from '@/layouts'
import type { LoginRequest } from '@/types/auth'
import './index.css'

const { Title, Text } = Typography

const LoginPage: React.FC = () => {
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
    <AuthLayout>
      <div className="login-container">
        <div className="login-header">
          <Title level={2} className="login-title">
            医院报告管理系统
          </Title>
          <Text type="secondary">
            Hospital Report Management System
          </Text>
        </div>

        <Card className="login-card">
          <Form
            form={form}
            name="login"
            onFinish={onFinish}
            autoComplete="off"
            size="large"
            className="login-form"
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
                prefix={<UserOutlined />}
                placeholder="用户名"
                autoComplete="username"
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
                prefix={<LockOutlined />}
                placeholder="密码"
                autoComplete="current-password"
              />
            </Form.Item>

            <Form.Item>
              <div className="login-options">
                <Form.Item name="rememberMe" valuePropName="checked" noStyle>
                  <Checkbox>记住我</Checkbox>
                </Form.Item>
                <a href="#" className="forgot-password">
                  忘记密码?
                </a>
              </div>
            </Form.Item>

            <Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                className="login-button"
                icon={<LoginOutlined />}
                block
              >
                登录
              </Button>
            </Form.Item>
          </Form>

          <div className="quick-login">
            <Text type="secondary" className="quick-login-title">
              快速登录
            </Text>
            <Space wrap>
              <Button
                size="small"
                type="link"
                onClick={() => handleQuickLogin('admin', 'admin123')}
              >
                管理员
              </Button>
              <Button
                size="small"
                type="link"
                onClick={() => handleQuickLogin('doctor', 'doctor123')}
              >
                医生
              </Button>
              <Button
                size="small"
                type="link"
                onClick={() => handleQuickLogin('nurse', 'nurse123')}
              >
                护士
              </Button>
            </Space>
          </div>
        </Card>

        <div className="login-footer">
          <Text type="secondary">
            © 2024 Hospital Report Management System. All rights reserved.
          </Text>
        </div>
      </div>
    </AuthLayout>
  )
}

export default LoginPage