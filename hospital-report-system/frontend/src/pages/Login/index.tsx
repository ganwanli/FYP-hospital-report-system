import React, { useState, useEffect } from 'react'
import {
  Form,
  Input,
  Button,
  Checkbox,
  message,
  Card,
  Space,
  Typography,
  Divider,
  Row,
  Col,
  Avatar,
  theme,
  Tooltip,
  Badge,
  Spin
} from 'antd'
import {
  UserOutlined,
  LockOutlined,
  LoginOutlined,
  MailOutlined,
  PhoneOutlined,
  SafetyOutlined,
  MedicineBoxOutlined,
  HeartOutlined,
  UserSwitchOutlined,
  EyeInvisibleOutlined,
  EyeTwoTone,
  ShieldCheckOutlined,
  TeamOutlined,
  SettingOutlined,
  StarOutlined
} from '@ant-design/icons'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import { authAPI } from '@/services/auth'
import { AuthLayout } from '@/layouts'
import type { LoginRequest } from '@/types/auth'
import './index.css'

const { Title, Text, Paragraph } = Typography

const LoginPage: React.FC = () => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [currentTime, setCurrentTime] = useState(new Date())
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useAuthStore()
  const { token } = theme.useToken()

  const from = location.state?.from?.pathname || '/'

  // 更新时间
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date())
    }, 1000)
    return () => clearInterval(timer)
  }, [])

  const onFinish = async (values: LoginRequest) => {
    setLoading(true)
    try {
      const response = await authAPI.login(values)
      if (response.code === 200) {
        login(response.data)
        message.success('登录成功，欢迎回来！')
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
    message.info(`已填入${username}的登录信息`)
  }

  const quickLoginUsers = [
    {
      username: 'admin',
      password: '123456',
      role: '系统管理员',
      description: '拥有系统全部权限',
      icon: <ShieldCheckOutlined />,
      color: '#1890ff',
      gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    },
    {
      username: 'doctor',
      password: '123456',
      role: '医生',
      description: '医疗数据管理',
      icon: <MedicineBoxOutlined />,
      color: '#52c41a',
      gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)'
    },
    {
      username: 'nurse',
      password: '123456',
      role: '护士',
      description: '患者护理记录',
      icon: <HeartOutlined />,
      color: '#f5222d',
      gradient: 'linear-gradient(135deg, #ff6b6b 0%, #ffa726 100%)'
    },
    {
      username: 'manager',
      password: '123456',
      role: '部门主管',
      description: '部门数据统计',
      icon: <TeamOutlined />,
      color: '#722ed1',
      gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }
  ]

  return (
    <div className="modern-login-page">
      {/* 动态背景 */}
      <div className="login-background">
        <div className="bg-animation"></div>
        <div className="bg-particles"></div>
        <div className="bg-overlay"></div>
      </div>

      {/* 顶部状态栏 */}
      <div className="login-status-bar">
        <div className="status-left">
          <Badge status="processing" text="系统运行正常" />
        </div>
        <div className="status-right">
          <Text className="current-time">
            {currentTime.toLocaleString('zh-CN', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            })}
          </Text>
        </div>
      </div>

      <div className="login-container">
        <Row justify="center" align="middle" style={{ minHeight: '100vh' }}>
          <Col xs={22} sm={20} md={16} lg={12} xl={10} xxl={8}>
            <div className="login-wrapper">
              {/* 系统标题 */}
              <div className="login-header">
                <div className="logo-container">
                  <div className="logo-bg">
                    <Avatar
                      size={80}
                      className="system-logo"
                      icon={<MedicineBoxOutlined />}
                    />
                  </div>
                  <div className="logo-glow"></div>
                </div>
                <Title level={1} className="system-title">
                  医院报告管理系统
                </Title>
                <Text className="system-subtitle">
                  Hospital Report Management System
                </Text>
                <div className="title-decoration">
                  <div className="decoration-line"></div>
                  <StarOutlined className="decoration-icon" />
                  <div className="decoration-line"></div>
                </div>
              </div>

              {/* 登录卡片 */}
              <Card
                className="modern-login-card"
                bordered={false}
              >
                <div className="card-header">
                  <Title level={3} className="form-title">
                    <LoginOutlined /> 用户登录
                  </Title>
                  <Text type="secondary" className="form-subtitle">
                    请输入您的账户信息
                  </Text>
                </div>

                <Spin spinning={loading} tip="登录中...">
                  <Form
                    form={form}
                    name="login"
                    onFinish={onFinish}
                    autoComplete="off"
                    size="large"
                    className="modern-login-form"
                    initialValues={{ rememberMe: true }}
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
                        placeholder="请输入用户名"
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
                        placeholder="请输入密码"
                        autoComplete="current-password"
                        className="modern-input"
                        iconRender={(visible) => (visible ? <EyeTwoTone /> : <EyeInvisibleOutlined />)}
                      />
                    </Form.Item>

                    <Form.Item>
                      <div className="login-options">
                        <Form.Item name="rememberMe" valuePropName="checked" noStyle>
                          <Checkbox className="remember-checkbox">
                            <span>记住登录状态</span>
                          </Checkbox>
                        </Form.Item>
                        <Tooltip title="请联系系统管理员重置密码">
                          <a href="#" className="forgot-password">
                            忘记密码?
                          </a>
                        </Tooltip>
                      </div>
                    </Form.Item>

                    <Form.Item>
                      <Button
                        type="primary"
                        htmlType="submit"
                        loading={loading}
                        className="modern-login-button"
                        icon={<LoginOutlined />}
                        block
                      >
                        {loading ? '登录中...' : '立即登录'}
                      </Button>
                    </Form.Item>
                  </Form>
                </Spin>

                <Divider className="quick-login-divider">
                  <Text type="secondary" className="divider-text">
                    <SettingOutlined /> 快速登录
                  </Text>
                </Divider>

                <div className="quick-login-section">
                  <Row gutter={[12, 12]}>
                    {quickLoginUsers.map((user, index) => (
                      <Col span={12} key={index}>
                        <Tooltip
                          title={`${user.role} - ${user.description}`}
                          placement="top"
                        >
                          <Card
                            size="small"
                            hoverable
                            className="modern-quick-login-card"
                            onClick={() => handleQuickLogin(user.username, user.password)}
                            style={{
                              background: user.gradient,
                              border: 'none'
                            }}
                          >
                            <div className="quick-card-content">
                              <div className="quick-avatar-container">
                                <Avatar
                                  size={40}
                                  className="quick-avatar"
                                  style={{
                                    backgroundColor: 'rgba(255, 255, 255, 0.2)',
                                    color: '#fff',
                                    border: '2px solid rgba(255, 255, 255, 0.3)'
                                  }}
                                  icon={user.icon}
                                />
                              </div>
                              <div className="quick-info">
                                <Text strong className="quick-role">
                                  {user.role}
                                </Text>
                                <Text className="quick-username">
                                  @{user.username}
                                </Text>
                              </div>
                            </div>
                          </Card>
                        </Tooltip>
                      </Col>
                    ))}
                  </Row>
                </div>

                <div className="login-help">
                  <div className="help-content">
                    <Text className="help-text">
                      💡 默认密码：123456 | 首次登录建议修改密码
                    </Text>
                  </div>
                </div>
              </Card>

              {/* 系统特性 */}
              <div className="system-features">
                <Row gutter={24}>
                  <Col span={8}>
                    <div className="feature-item">
                      <div className="feature-icon">
                        <ShieldCheckOutlined />
                      </div>
                      <Text className="feature-text">安全可靠</Text>
                      <Text className="feature-desc">多重安全防护</Text>
                    </div>
                  </Col>
                  <Col span={8}>
                    <div className="feature-item">
                      <div className="feature-icon">
                        <TeamOutlined />
                      </div>
                      <Text className="feature-text">多角色</Text>
                      <Text className="feature-desc">权限精细管理</Text>
                    </div>
                  </Col>
                  <Col span={8}>
                    <div className="feature-item">
                      <div className="feature-icon">
                        <MedicineBoxOutlined />
                      </div>
                      <Text className="feature-text">专业医疗</Text>
                      <Text className="feature-desc">医疗数据专用</Text>
                    </div>
                  </Col>
                </Row>
              </div>

              {/* 页脚 */}
              <div className="modern-login-footer">
                <div className="footer-content">
                  <Text className="copyright">
                    © 2024 Hospital Report Management System
                  </Text>
                  <Text className="version">
                    Version 2.0.0
                  </Text>
                </div>
              </div>
            </div>
          </Col>
        </Row>
      </div>
    </div>
  )
}

export default LoginPage