import React, { useState, useEffect } from 'react'
import {
  Modal,
  Card,
  Row,
  Col,
  Statistic,
  Progress,
  Timeline,
  Tag,
  Button,
  Space,
  Typography,
  Tabs,
  Table,
  message,
  Spin,
  Empty
} from 'antd'
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  StopOutlined,
  ReloadOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons'
import { syncAPI } from '@/services'
import type { SyncContext, SyncLog } from '@/services'

const { Title, Text } = Typography
const { TabPane } = Tabs

interface ExecutionModalProps {
  visible: boolean
  task: any
  onCancel: () => void
  onRefresh: () => void
}

const ExecutionModal: React.FC<ExecutionModalProps> = ({
  visible,
  task,
  onCancel,
  onRefresh
}) => {
  const [currentExecution, setCurrentExecution] = useState<SyncContext | null>(null)
  const [executionLogs, setExecutionLogs] = useState<SyncLog[]>([])
  const [loading, setLoading] = useState(false)
  const [logsLoading, setLogsLoading] = useState(false)
  const [activeTab, setActiveTab] = useState('current')

  useEffect(() => {
    if (visible && task) {
      fetchExecutionLogs()
      // 如果有运行中的任务，定时刷新状态
      const interval = setInterval(checkCurrentExecution, 2000)
      return () => clearInterval(interval)
    }
  }, [visible, task])

  const checkCurrentExecution = async () => {
    if (!task) return
    
    try {
      const response = await syncAPI.getRunningTasks()
      if (response.code === 200) {
        const runningTask = response.data.find((t: SyncContext) => t.taskId === task.id)
        setCurrentExecution(runningTask || null)
      }
    } catch (error) {
      console.error('获取运行状态失败:', error)
    }
  }

  const fetchExecutionLogs = async () => {
    if (!task) return
    
    setLogsLoading(true)
    try {
      const response = await syncAPI.getSyncLogs({
        taskId: task.id,
        current: 1,
        size: 20
      })
      
      if (response.code === 200) {
        setExecutionLogs(response.data.records)
      }
    } catch (error) {
      console.error('获取执行日志失败:', error)
    } finally {
      setLogsLoading(false)
    }
  }

  const handleExecute = async () => {
    setLoading(true)
    try {
      const response = await syncAPI.executeTask(task.id)
      if (response.code === 200) {
        message.success('任务执行成功')
        onRefresh()
        setTimeout(checkCurrentExecution, 1000)
      } else {
        message.error(response.message || '任务执行失败')
      }
    } catch (error) {
      message.error('任务执行失败')
    } finally {
      setLoading(false)
    }
  }

  const handleCancel = async () => {
    if (!currentExecution) return
    
    try {
      await syncAPI.cancelExecution(currentExecution.executionId)
      message.success('任务取消成功')
      setTimeout(checkCurrentExecution, 1000)
    } catch (error) {
      message.error('任务取消失败')
    }
  }

  const handlePause = async () => {
    if (!currentExecution) return
    
    try {
      await syncAPI.pauseExecution(currentExecution.executionId)
      message.success('任务暂停成功')
      setTimeout(checkCurrentExecution, 1000)
    } catch (error) {
      message.error('任务暂停失败')
    }
  }

  const handleResume = async () => {
    if (!currentExecution) return
    
    try {
      await syncAPI.resumeExecution(currentExecution.executionId)
      message.success('任务恢复成功')
      setTimeout(checkCurrentExecution, 1000)
    } catch (error) {
      message.error('任务恢复失败')
    }
  }

  const getStatusColor = (status: string) => {
    const colors = {
      'PENDING': 'blue',
      'RUNNING': 'green',
      'SUCCESS': 'green',
      'FAILED': 'red',
      'CANCELLED': 'orange'
    }
    return colors[status as keyof typeof colors] || 'default'
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

  const formatDuration = (ms: number) => {
    const seconds = Math.floor(ms / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    
    if (hours > 0) {
      return `${hours}时${minutes % 60}分${seconds % 60}秒`
    } else if (minutes > 0) {
      return `${minutes}分${seconds % 60}秒`
    } else {
      return `${seconds}秒`
    }
  }

  const logColumns = [
    {
      title: '执行ID',
      dataIndex: 'executionId',
      key: 'executionId',
      width: 120,
      render: (text: string) => <Text code>{text.slice(0, 8)}</Text>
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: string) => (
        <Tag color={getStatusColor(status)} icon={getStatusIcon(status)}>
          {getStatusText(status)}
        </Tag>
      )
    },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      key: 'startTime',
      width: 120,
      render: (text: string) => new Date(text).toLocaleString()
    },
    {
      title: '结束时间',
      dataIndex: 'endTime',
      key: 'endTime',
      width: 120,
      render: (text: string) => text ? new Date(text).toLocaleString() : '-'
    },
    {
      title: '耗时',
      dataIndex: 'duration',
      key: 'duration',
      width: 80,
      render: (duration: number) => duration ? formatDuration(duration) : '-'
    },
    {
      title: '处理记录',
      key: 'records',
      width: 100,
      render: (_: any, record: SyncLog) => (
        <div>
          <div>成功: {record.successCount || 0}</div>
          <div>失败: {record.errorCount || 0}</div>
        </div>
      )
    },
    {
      title: '进度',
      dataIndex: 'progressPercent',
      key: 'progressPercent',
      width: 80,
      render: (percent: number) => (
        <Progress
          type="circle"
          size="small"
          percent={Math.round(percent || 0)}
          format={() => `${Math.round(percent || 0)}%`}
        />
      )
    },
    {
      title: '触发方式',
      dataIndex: 'triggerType',
      key: 'triggerType',
      width: 80,
      render: (type: string) => (
        <Tag color={type === 'MANUAL' ? 'blue' : 'green'}>
          {type === 'MANUAL' ? '手动' : '定时'}
        </Tag>
      )
    }
  ]

  return (
    <Modal
      title={
        <Space>
          <PlayCircleOutlined />
          {`任务执行监控 - ${task?.taskName || ''}`}
        </Space>
      }
      open={visible}
      onCancel={onCancel}
      width={1200}
      footer={[
        <Button key="refresh" icon={<ReloadOutlined />} onClick={checkCurrentExecution}>
          刷新状态
        </Button>,
        <Button key="close" onClick={onCancel}>
          关闭
        </Button>
      ]}
    >
      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        <TabPane tab="当前执行" key="current">
          {currentExecution ? (
            <div>
              {/* 执行状态概览 */}
              <Row gutter={16} style={{ marginBottom: 16 }}>
                <Col span={6}>
                  <Card size="small">
                    <Statistic
                      title="执行状态"
                      value={getStatusText(currentExecution.status)}
                      prefix={getStatusIcon(currentExecution.status)}
                      valueStyle={{ color: getStatusColor(currentExecution.status) }}
                    />
                  </Card>
                </Col>
                <Col span={6}>
                  <Card size="small">
                    <Statistic
                      title="执行进度"
                      value={currentExecution.progressPercent || 0}
                      suffix="%"
                      precision={1}
                    />
                  </Card>
                </Col>
                <Col span={6}>
                  <Card size="small">
                    <Statistic
                      title="成功记录"
                      value={currentExecution.successCount || 0}
                      valueStyle={{ color: '#3f8600' }}
                    />
                  </Card>
                </Col>
                <Col span={6}>
                  <Card size="small">
                    <Statistic
                      title="失败记录"
                      value={currentExecution.errorCount || 0}
                      valueStyle={{ color: '#cf1322' }}
                    />
                  </Card>
                </Col>
              </Row>

              {/* 进度条 */}
              <Card title="执行进度" size="small" style={{ marginBottom: 16 }}>
                <Progress
                  percent={Math.round(currentExecution.progressPercent || 0)}
                  status={currentExecution.status === 'FAILED' ? 'exception' : 'active'}
                  strokeColor={{
                    '0%': '#108ee9',
                    '100%': '#87d068'
                  }}
                />
                <div style={{ marginTop: 8 }}>
                  <Text>
                    已处理: {currentExecution.successCount || 0} / 总计: {currentExecution.sourceCount || 0}
                  </Text>
                </div>
              </Card>

              {/* 执行详情 */}
              <Row gutter={16}>
                <Col span={12}>
                  <Card title="执行信息" size="small">
                    <div style={{ lineHeight: 2 }}>
                      <div><Text strong>执行ID:</Text> <Text code>{currentExecution.executionId}</Text></div>
                      <div><Text strong>同步模式:</Text> <Tag>{currentExecution.syncMode}</Tag></div>
                      <div><Text strong>开始时间:</Text> {currentExecution.startTime ? new Date(currentExecution.startTime).toLocaleString() : '-'}</div>
                      <div><Text strong>触发方式:</Text> <Tag color={currentExecution.triggerType === 'MANUAL' ? 'blue' : 'green'}>{currentExecution.triggerType === 'MANUAL' ? '手动' : '定时'}</Tag></div>
                    </div>
                  </Card>
                </Col>
                <Col span={12}>
                  <Card title="数据统计" size="small">
                    <div style={{ lineHeight: 2 }}>
                      <div><Text strong>源数据量:</Text> {currentExecution.sourceCount || 0}</div>
                      <div><Text strong>目标数据量:</Text> {currentExecution.targetCount || 0}</div>
                      <div><Text strong>跳过记录:</Text> {currentExecution.skipCount || 0}</div>
                      <div><Text strong>重试次数:</Text> {currentExecution.currentRetry || 0}</div>
                    </div>
                  </Card>
                </Col>
              </Row>

              {/* 控制按钮 */}
              <Card title="执行控制" size="small" style={{ marginTop: 16 }}>
                <Space>
                  {currentExecution.status === 'RUNNING' && (
                    <>
                      <Button
                        type="primary"
                        icon={<PauseCircleOutlined />}
                        onClick={handlePause}
                        disabled={currentExecution.paused}
                      >
                        {currentExecution.paused ? '已暂停' : '暂停'}
                      </Button>
                      {currentExecution.paused && (
                        <Button
                          type="primary"
                          icon={<PlayCircleOutlined />}
                          onClick={handleResume}
                        >
                          恢复
                        </Button>
                      )}
                      <Button
                        danger
                        icon={<StopOutlined />}
                        onClick={handleCancel}
                      >
                        取消
                      </Button>
                    </>
                  )}
                  {!currentExecution || ['SUCCESS', 'FAILED', 'CANCELLED'].includes(currentExecution.status) && (
                    <Button
                      type="primary"
                      icon={<PlayCircleOutlined />}
                      loading={loading}
                      onClick={handleExecute}
                    >
                      重新执行
                    </Button>
                  )}
                </Space>
              </Card>

              {/* 错误信息 */}
              {currentExecution.errorMessage && (
                <Card title="错误信息" size="small" style={{ marginTop: 16 }}>
                  <Text type="danger">{currentExecution.errorMessage}</Text>
                </Card>
              )}
            </div>
          ) : (
            <Empty
              description="暂无执行任务"
              image={Empty.PRESENTED_IMAGE_SIMPLE}
            >
              <Button
                type="primary"
                icon={<PlayCircleOutlined />}
                loading={loading}
                onClick={handleExecute}
              >
                立即执行
              </Button>
            </Empty>
          )}
        </TabPane>

        <TabPane tab="执行历史" key="history">
          <Card>
            <Table
              columns={logColumns}
              dataSource={executionLogs}
              rowKey="id"
              loading={logsLoading}
              pagination={{
                pageSize: 10,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total) => `共 ${total} 条记录`
              }}
              scroll={{ x: 800 }}
            />
          </Card>
        </TabPane>

        <TabPane tab="执行时间线" key="timeline">
          <Card>
            <Spin spinning={logsLoading}>
              {executionLogs.length > 0 ? (
                <Timeline mode="left">
                  {executionLogs.map((log) => (
                    <Timeline.Item
                      key={log.id}
                      color={getStatusColor(log.status)}
                      dot={getStatusIcon(log.status)}
                    >
                      <div style={{ marginBottom: 8 }}>
                        <Space>
                          <Tag color={getStatusColor(log.status)}>
                            {getStatusText(log.status)}
                          </Tag>
                          <Text code>{log.executionId.slice(0, 8)}</Text>
                          <Text type="secondary">{new Date(log.startTime).toLocaleString()}</Text>
                        </Space>
                      </div>
                      <div>
                        <Text>
                          处理记录: 成功 {log.successCount || 0} / 失败 {log.errorCount || 0}
                        </Text>
                        {log.duration && (
                          <Text type="secondary"> · 耗时: {formatDuration(log.duration)}</Text>
                        )}
                      </div>
                      {log.errorMessage && (
                        <div style={{ marginTop: 4 }}>
                          <Text type="danger">{log.errorMessage}</Text>
                        </div>
                      )}
                    </Timeline.Item>
                  ))}
                </Timeline>
              ) : (
                <Empty description="暂无执行记录" />
              )}
            </Spin>
          </Card>
        </TabPane>
      </Tabs>
    </Modal>
  )
}

export default ExecutionModal