import React, { useState } from 'react'
import { Card, Form, Input, Button, Checkbox, Typography, message } from 'antd'
import { UserOutlined, LockOutlined, LoginOutlined } from '@ant-design/icons'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import { authAPI } from '@/services/auth'
import type { LoginRequest } from '@/types/auth'
import './AntdLogin.css'

const { Title, Text } = Typography

const AntdLogin: React.FC = () => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useAuthStore()
  const from = location.state?.from?.pathname || '/'

  const onFinish = async (values: LoginRequest) => {
    setLoading(true)
    try {
      const res = await authAPI.login(values)
      // 这里请根据实际API返回结构调整
      if (res.success || res.code === 200) {
        login(res.data)
        message.success('登录成功')
        navigate(from, { replace: true })
      } else {
        message.error(res.message || '登录失败')
      }
    } catch (e: any) {
      message.error(e.message || '登录失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="antd-login-bg">
      <div className="antd-login-center">
        <Card className="antd-login-card" bordered={false}>
          <div className="antd-login-header">
            <div className="antd-login-logo">🏥</div>
            <Title level={2} className="antd-login-title">医院报告管理系统</Title>
            <Text type="secondary">Hospital Report Management System</Text>
          </div>
          <Form
            form={form}
            name="antd-login"
            onFinish={onFinish}
            size="large"
            autoComplete="off"
            className="antd-login-form"
          >
            <Form.Item
              name="username"
              rules={[
                { required: true, message: '请输入用户名' },
                { min: 3, message: '用户名至少3个字符' }
              ]}
            >
              <Input prefix={<UserOutlined />} placeholder="用户名" autoComplete="username" />
            </Form.Item>
            <Form.Item
              name="password"
              rules={[
                { required: true, message: '请输入密码' },
                { min: 6, message: '密码至少6个字符' }
              ]}
            >
              <Input.Password prefix={<LockOutlined />} placeholder="密码" autoComplete="current-password" />
            </Form.Item>
            <Form.Item>
              <div className="antd-login-options">
                <Form.Item name="rememberMe" valuePropName="checked" noStyle>
                  <Checkbox>记住我</Checkbox>
                </Form.Item>
                <a className="antd-login-forgot" href="#">忘记密码?</a>
              </div>
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} icon={<LoginOutlined />} block>
                登录
              </Button>
            </Form.Item>
          </Form>
          <div className="antd-login-footer">
            <Text type="secondary" style={{ fontSize: 12 }}>© 2024 Hospital Report Management System</Text>
          </div>
        </Card>
      </div>
    </div>
  )
}

export default AntdLogin 