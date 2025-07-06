import React, { useState, useEffect } from 'react'
import { Modal, Card, Row, Col, Statistic, Progress, Table, Typography, Space, Button } from 'antd'
import { ReloadOutlined, DatabaseOutlined } from '@ant-design/icons'
import { dataSourceAPI } from '@/services'
import type { DataSource } from '@/types'

const { Title, Text } = Typography

interface MonitorModalProps {
  visible: boolean
  dataSource: DataSource
  onCancel: () => void
}

const MonitorModal: React.FC<MonitorModalProps> = ({
  visible,
  dataSource,
  onCancel
}) => {
  const [stats, setStats] = useState<any>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (visible) {
      fetchStats()
      
      // 定时刷新
      const interval = setInterval(fetchStats, 5000)
      return () => clearInterval(interval)
    }
  }, [visible])

  const fetchStats = async () => {
    setLoading(true)
    try {
      const response = await dataSourceAPI.getDataSourceStats(dataSource.datasourceCode)
      if (response.code === 200) {
        setStats(response.data)
      }
    } catch (error) {
      console.error('获取统计信息失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const getUsagePercent = () => {
    if (!stats || stats.maximumPoolSize === 0) return 0
    return Math.round((stats.activeConnections / stats.maximumPoolSize) * 100)
  }

  const getUsageStatus = () => {
    const percent = getUsagePercent()
    if (percent >= 90) return 'exception'
    if (percent >= 70) return 'active'
    return 'normal'
  }

  const poolConfigColumns = [
    {
      title: '配置项',
      dataIndex: 'key',
      key: 'key',
      width: 150
    },
    {
      title: '当前值',
      dataIndex: 'value',
      key: 'value'
    }
  ]

  const poolConfigData = stats ? [
    { key: '连接池名称', value: stats.poolName },
    { key: '最大连接数', value: stats.maximumPoolSize },
    { key: '最小空闲连接', value: stats.minimumIdle },
    { key: '连接池状态', value: stats.isClosed ? '已关闭' : '运行中' }
  ] : []

  const connectionColumns = [
    {
      title: '连接类型',
      dataIndex: 'type',
      key: 'type'
    },
    {
      title: '连接数',
      dataIndex: 'count',
      key: 'count'
    },
    {
      title: '百分比',
      dataIndex: 'percent',
      key: 'percent',
      render: (percent: number) => `${percent}%`
    }
  ]

  const connectionData = stats ? [
    {
      type: '活跃连接',
      count: stats.activeConnections,
      percent: stats.maximumPoolSize > 0 ? Math.round((stats.activeConnections / stats.maximumPoolSize) * 100) : 0
    },
    {
      type: '空闲连接',
      count: stats.idleConnections,
      percent: stats.maximumPoolSize > 0 ? Math.round((stats.idleConnections / stats.maximumPoolSize) * 100) : 0
    },
    {
      type: '总连接数',
      count: stats.totalConnections,
      percent: stats.maximumPoolSize > 0 ? Math.round((stats.totalConnections / stats.maximumPoolSize) * 100) : 0
    }
  ] : []

  return (
    <Modal
      title={
        <Space>
          <DatabaseOutlined />
          {`连接池监控 - ${dataSource.datasourceName}`}
        </Space>
      }
      open={visible}
      onCancel={onCancel}
      width={900}
      footer={[
        <Button key="refresh" icon={<ReloadOutlined />} loading={loading} onClick={fetchStats}>
          刷新
        </Button>,
        <Button key="close" onClick={onCancel}>
          关闭
        </Button>
      ]}
    >
      {stats ? (
        <div>
          {/* 概览统计 */}
          <Row gutter={16} style={{ marginBottom: 16 }}>
            <Col span={6}>
              <Card size="small">
                <Statistic
                  title="活跃连接"
                  value={stats.activeConnections}
                  valueStyle={{ color: '#1890ff' }}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card size="small">
                <Statistic
                  title="空闲连接"
                  value={stats.idleConnections}
                  valueStyle={{ color: '#52c41a' }}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card size="small">
                <Statistic
                  title="总连接数"
                  value={stats.totalConnections}
                  valueStyle={{ color: '#722ed1' }}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card size="small">
                <Statistic
                  title="等待连接线程"
                  value={stats.threadsAwaitingConnection}
                  valueStyle={{ color: stats.threadsAwaitingConnection > 0 ? '#ff4d4f' : '#52c41a' }}
                />
              </Card>
            </Col>
          </Row>

          {/* 连接池使用率 */}
          <Card title="连接池使用率" size="small" style={{ marginBottom: 16 }}>
            <div style={{ textAlign: 'center' }}>
              <Progress
                type="circle"
                percent={getUsagePercent()}
                status={getUsageStatus()}
                format={percent => (
                  <div>
                    <div style={{ fontSize: 24, fontWeight: 'bold' }}>{percent}%</div>
                    <div style={{ fontSize: 12, color: '#666' }}>
                      {stats.activeConnections}/{stats.maximumPoolSize}
                    </div>
                  </div>
                )}
                width={120}
              />
              <div style={{ marginTop: 16 }}>
                <Text type="secondary">
                  当前活跃连接数：{stats.activeConnections} / 最大连接数：{stats.maximumPoolSize}
                </Text>
              </div>
            </div>
          </Card>

          <Row gutter={16}>
            {/* 连接池配置 */}
            <Col span={12}>
              <Card title="连接池配置" size="small">
                <Table
                  columns={poolConfigColumns}
                  dataSource={poolConfigData}
                  pagination={false}
                  size="small"
                  rowKey="key"
                />
              </Card>
            </Col>

            {/* 连接详情 */}
            <Col span={12}>
              <Card title="连接详情" size="small">
                <Table
                  columns={connectionColumns}
                  dataSource={connectionData}
                  pagination={false}
                  size="small"
                  rowKey="type"
                />
              </Card>
            </Col>
          </Row>

          {/* 数据源信息 */}
          <Card title="数据源信息" size="small" style={{ marginTop: 16 }}>
            <Row gutter={16}>
              <Col span={12}>
                <Space direction="vertical" style={{ width: '100%' }}>
                  <div>
                    <Text strong>数据源编码: </Text>
                    <Text code>{dataSource.datasourceCode}</Text>
                  </div>
                  <div>
                    <Text strong>数据库类型: </Text>
                    <Text>{dataSource.databaseType}</Text>
                  </div>
                  <div>
                    <Text strong>JDBC URL: </Text>
                    <Text code style={{ fontSize: 11 }}>{dataSource.jdbcUrl}</Text>
                  </div>
                </Space>
              </Col>
              <Col span={12}>
                <Space direction="vertical" style={{ width: '100%' }}>
                  <div>
                    <Text strong>用户名: </Text>
                    <Text>{dataSource.username}</Text>
                  </div>
                  <div>
                    <Text strong>状态: </Text>
                    <Text type={stats.isClosed ? 'danger' : 'success'}>
                      {stats.isClosed ? '已关闭' : '运行中'}
                    </Text>
                  </div>
                  <div>
                    <Text strong>连接池名称: </Text>
                    <Text code>{stats.poolName}</Text>
                  </div>
                </Space>
              </Col>
            </Row>
          </Card>
        </div>
      ) : (
        <div style={{ textAlign: 'center', padding: 40 }}>
          <Text type="secondary">暂无监控数据</Text>
        </div>
      )}
    </Modal>
  )
}

export default MonitorModal