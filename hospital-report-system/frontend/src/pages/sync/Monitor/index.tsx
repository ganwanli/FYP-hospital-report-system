import React, { useState, useEffect } from 'react'
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  Tag,
  Progress,
  Timeline,
  Typography,
  Space,
  Button,
  Empty,
  Spin,
  Alert,
  Divider
} from 'antd'
import {
  SyncOutlined,
  PlayCircleOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  BarChartOutlined,
  LineChartOutlined,
  ReloadOutlined,
  TrophyOutlined
} from '@ant-design/icons'
import { Line, Column, Pie } from '@ant-design/charts'
import { syncAPI } from '@/services'
import type { SyncContext } from '@/services'

const { Title, Text } = Typography

const SyncMonitor: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [statistics, setStatistics] = useState<any>({})
  const [runningTasks, setRunningTasks] = useState<SyncContext[]>([])
  const [refreshTime, setRefreshTime] = useState<Date>(new Date())

  useEffect(() => {
    fetchData()
    
    // 每5秒刷新一次数据
    const interval = setInterval(fetchData, 5000)
    return () => clearInterval(interval)
  }, [])

  const fetchData = async () => {
    setLoading(true)
    try {
      const [statsResponse, runningResponse] = await Promise.all([
        syncAPI.getSyncStatistics(),
        syncAPI.getRunningTasks()
      ])
      
      if (statsResponse.code === 200) {
        setStatistics(statsResponse.data)
      }
      
      if (runningResponse.code === 200) {
        setRunningTasks(runningResponse.data)
      }
      
      setRefreshTime(new Date())
    } catch (error) {
      console.error('获取监控数据失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status: string) => {
    const colors = {
      'PENDING': '#1890ff',
      'RUNNING': '#52c41a',
      'SUCCESS': '#52c41a',
      'FAILED': '#ff4d4f',
      'CANCELLED': '#faad14'
    }
    return colors[status as keyof typeof colors] || '#d9d9d9'
  }

  const getStatusIcon = (status: string) => {
    const icons = {
      'PENDING': <ClockCircleOutlined />,
      'RUNNING': <PlayCircleOutlined />,
      'SUCCESS': <CheckCircleOutlined />,
      'FAILED': <CloseCircleOutlined />,
      'CANCELLED': <ExclamationCircleOutlined />
    }
    return icons[status as keyof typeof icons] || <ClockCircleOutlined />
  }

  const getStatusText = (status: string) => {
    const texts = {
      'PENDING': '等待中',
      'RUNNING': '运行中',
      'SUCCESS': '成功',
      'FAILED': '失败',
      'CANCELLED': '已取消'
    }
    return texts[status as keyof typeof texts] || '未知'
  }

  // 状态分布饼图数据
  const statusPieData = (statistics.statusDistribution || []).map((item: any) => ({
    type: getStatusText(item.status),
    value: item.count,
    status: item.status
  }))

  // 每日执行趋势图数据
  const dateLineData = (statistics.dateStats || []).map((item: any) => ({
    date: item.date,
    total: item.total_count,
    success: item.success_count,
    failed: item.failed_count
  }))

  // 任务执行统计柱状图数据
  const taskColumnData = (statistics.taskExecutionStats || []).slice(0, 10).map((item: any) => ({
    taskCode: item.task_code,
    count: item.execution_count,
    successRate: item.success_count / item.execution_count * 100
  }))

  // 运行中任务表格列定义
  const runningColumns = [
    {
      title: '任务名称',
      key: 'task',
      render: (_: any, record: SyncContext) => (
        <div>
          <div>{record.taskName}</div>
          <Text type="secondary" style={{ fontSize: 12 }}>{record.taskCode}</Text>
        </div>
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={getStatusColor(status)} icon={getStatusIcon(status)}>
          {getStatusText(status)}
        </Tag>
      )
    },
    {
      title: '进度',
      key: 'progress',
      render: (_: any, record: SyncContext) => (
        <div>
          <Progress
            percent={Math.round(record.progressPercent || 0)}
            size="small"
            status={record.status === 'FAILED' ? 'exception' : 'active'}
          />
          <Text type="secondary" style={{ fontSize: 11 }}>
            {record.successCount || 0} / {record.sourceCount || 0}
          </Text>
        </div>
      )
    },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      key: 'startTime',
      render: (text: string) => text ? new Date(text).toLocaleString() : '-'
    },
    {
      title: '执行ID',
      dataIndex: 'executionId',
      key: 'executionId',
      render: (text: string) => <Text code>{text.slice(0, 8)}</Text>
    }
  ]

  return (
    <div>
      {/* 页面标题和刷新按钮 */}
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col>
          <Title level={3}>
            <SyncOutlined /> 同步监控仪表板
          </Title>
        </Col>
        <Col>
          <Space>
            <Text type="secondary">最后更新: {refreshTime.toLocaleTimeString()}</Text>
            <Button 
              icon={<ReloadOutlined />} 
              loading={loading}
              onClick={fetchData}
            >
              刷新
            </Button>
          </Space>
        </Col>
      </Row>

      {/* 核心指标概览 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总任务数"
              value={statistics.taskStats?.total_tasks || 0}
              prefix={<SyncOutlined style={{ color: '#1890ff' }} />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已启用任务"
              value={statistics.taskStats?.enabled_tasks || 0}
              prefix={<CheckCircleOutlined style={{ color: '#52c41a' }} />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="运行中任务"
              value={runningTasks.length}
              prefix={<PlayCircleOutlined style={{ color: '#faad14' }} />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="今日成功率"
              value={(() => {
                const dist = statistics.statusDistribution || []
                const success = dist.find((item: any) => item.status === 'SUCCESS')?.count || 0
                const total = dist.reduce((sum: number, item: any) => sum + item.count, 0)
                return total > 0 ? ((success / total) * 100).toFixed(1) : 0
              })()}
              suffix="%"
              prefix={<TrophyOutlined style={{ color: '#52c41a' }} />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 运行状态警告 */}
      {runningTasks.some(task => task.status === 'FAILED') && (
        <Alert
          message="检测到失败的同步任务"
          description={`有 ${runningTasks.filter(task => task.status === 'FAILED').length} 个任务执行失败，请及时处理`}
          type="error"
          showIcon
          closable
          style={{ marginBottom: 16 }}
        />
      )}

      <Row gutter={16}>
        {/* 左侧图表区域 */}
        <Col span={16}>
          <Row gutter={[16, 16]}>
            {/* 每日执行趋势 */}
            <Col span={24}>
              <Card 
                title={
                  <Space>
                    <LineChartOutlined />
                    最近7天执行趋势
                  </Space>
                }
                extra={
                  <Text type="secondary">
                    总执行: {dateLineData.reduce((sum, item) => sum + item.total, 0)} 次
                  </Text>
                }
              >
                {dateLineData.length > 0 ? (
                  <Line
                    data={dateLineData.map(item => [
                      { date: item.date, type: '总执行', value: item.total },
                      { date: item.date, type: '成功', value: item.success },
                      { date: item.date, type: '失败', value: item.failed }
                    ]).flat()}
                    xField="date"
                    yField="value"
                    seriesField="type"
                    height={200}
                    color={['#1890ff', '#52c41a', '#ff4d4f']}
                    point={{ size: 3 }}
                    smooth={true}
                  />
                ) : (
                  <Empty description="暂无数据" />
                )}
              </Card>
            </Col>

            {/* 任务执行统计 */}
            <Col span={24}>
              <Card 
                title={
                  <Space>
                    <BarChartOutlined />
                    任务执行排行 (Top 10)
                  </Space>
                }
              >
                {taskColumnData.length > 0 ? (
                  <Column
                    data={taskColumnData}
                    xField="taskCode"
                    yField="count"
                    height={250}
                    color="#1890ff"
                    label={{
                      position: 'top'
                    }}
                    meta={{
                      taskCode: { alias: '任务编码' },
                      count: { alias: '执行次数' }
                    }}
                  />
                ) : (
                  <Empty description="暂无数据" />
                )}
              </Card>
            </Col>
          </Row>
        </Col>

        {/* 右侧状态区域 */}
        <Col span={8}>
          <Row gutter={[16, 16]}>
            {/* 执行状态分布 */}
            <Col span={24}>
              <Card title="执行状态分布">
                {statusPieData.length > 0 ? (
                  <Pie
                    data={statusPieData}
                    angleField="value"
                    colorField="type"
                    radius={0.8}
                    height={200}
                    color={statusPieData.map(item => getStatusColor(item.status))}
                    label={{
                      type: 'inner',
                      offset: '-30%',
                      content: '{percentage}',
                      style: {
                        fontSize: 12,
                        textAlign: 'center',
                      },
                    }}
                    legend={{
                      position: 'bottom'
                    }}
                  />
                ) : (
                  <Empty description="暂无数据" />
                )}
              </Card>
            </Col>

            {/* 任务类型分布 */}
            <Col span={24}>
              <Card title="任务类型分布">
                <Row gutter={16}>
                  <Col span={12}>
                    <Statistic
                      title="表同步"
                      value={statistics.taskStats?.table_sync_tasks || 0}
                      valueStyle={{ color: '#1890ff' }}
                    />
                  </Col>
                  <Col span={12}>
                    <Statistic
                      title="SQL同步"
                      value={statistics.taskStats?.sql_sync_tasks || 0}
                      valueStyle={{ color: '#722ed1' }}
                    />
                  </Col>
                </Row>
              </Card>
            </Col>
          </Row>
        </Col>
      </Row>

      {/* 运行中任务 */}
      <Card 
        title={
          <Space>
            <PlayCircleOutlined />
            运行中任务 ({runningTasks.length})
          </Space>
        }
        style={{ marginTop: 16 }}
      >
        {runningTasks.length > 0 ? (
          <Table
            columns={runningColumns}
            dataSource={runningTasks}
            rowKey="executionId"
            pagination={false}
            size="small"
          />
        ) : (
          <Empty description="当前没有运行中的任务" />
        )}
      </Card>

      {/* 最近执行时间线 */}
      {statistics.taskExecutionStats && statistics.taskExecutionStats.length > 0 && (
        <Card 
          title={
            <Space>
              <ClockCircleOutlined />
              最近执行时间线
            </Space>
          }
          style={{ marginTop: 16 }}
        >
          <Timeline mode="left">
            {statistics.taskExecutionStats.slice(0, 5).map((item: any, index: number) => (
              <Timeline.Item
                key={index}
                color={item.success_count === item.execution_count ? 'green' : 'orange'}
              >
                <div>
                  <Space>
                    <Text strong>{item.task_code}</Text>
                    <Tag color="blue">执行 {item.execution_count} 次</Tag>
                    <Tag color="green">成功率 {((item.success_count / item.execution_count) * 100).toFixed(1)}%</Tag>
                  </Space>
                </div>
                <Text type="secondary">
                  最后执行: {item.last_execution ? new Date(item.last_execution).toLocaleString() : '无'}
                </Text>
              </Timeline.Item>
            ))}
          </Timeline>
        </Card>
      )}
    </div>
  )
}

export default SyncMonitor