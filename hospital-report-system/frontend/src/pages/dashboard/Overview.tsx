import React from 'react'
import { Card, Row, Col, Statistic, Typography, Space } from 'antd'
import { DatabaseOutlined, FileTextOutlined, UserOutlined, BarChartOutlined } from '@ant-design/icons'

const { Title } = Typography

const Overview: React.FC = () => {
  return (
    <div style={{ padding: '24px' }}>
      <Title level={2}>仪表板</Title>
      
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="数据源"
              value={8}
              prefix={<DatabaseOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="报表数量"
              value={32}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="用户数量"
              value={156}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="今日访问"
              value={1234}
              prefix={<BarChartOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: '24px' }}>
        <Col xs={24} md={12}>
          <Card title="快速操作" size="small">
            <Space direction="vertical" style={{ width: '100%' }}>
              <a href="/datasource">数据源管理</a>
              <a href="/template">SQL模板管理</a>
              <a href="/report/config">报表配置</a>
              <a href="/system/user">用户管理</a>
            </Space>
          </Card>
        </Col>
        <Col xs={24} md={12}>
          <Card title="系统信息" size="small">
            <Space direction="vertical" style={{ width: '100%' }}>
              <div>版本: v1.0.0</div>
              <div>最后更新: 2024-07-06</div>
              <div>运行状态: 正常</div>
              <div>在线用户: 28</div>
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Overview