import React, { useState, useEffect } from 'react'
import {
  Card,
  Table,
  Button,
  Space,
  Popconfirm,
  message,
  Modal,
  Tag,
  Tooltip,
  Input,
  Select,
  Row,
  Col,
  Statistic,
  Badge,
  Typography
} from 'antd'
import {
  PlusOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  StopOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  EyeOutlined,
  ScheduleOutlined,
  SyncOutlined
} from '@ant-design/icons'
import { syncAPI, dataSourceAPI } from '@/services'
import type { SyncTask, SyncContext } from '@/services'
import SyncTaskForm from './components/SyncTaskForm'
import ExecutionModal from './components/ExecutionModal'

const { Search } = Input
const { Option } = Select
const { Title, Text } = Typography

interface DataSourceOption {
  id: number
  datasourceName: string
  datasourceCode: string
  databaseType: string
}

const SyncTaskList: React.FC = () => {
  const [tasks, setTasks] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  })
  const [filters, setFilters] = useState({
    taskName: '',
    taskType: '',
    isEnabled: undefined as boolean | undefined
  })
  const [formVisible, setFormVisible] = useState(false)
  const [editingTask, setEditingTask] = useState<SyncTask | null>(null)
  const [executionVisible, setExecutionVisible] = useState(false)
  const [selectedTask, setSelectedTask] = useState<any>(null)
  const [dataSources, setDataSources] = useState<DataSourceOption[]>([])
  const [runningTasks, setRunningTasks] = useState<SyncContext[]>([])
  const [statistics, setStatistics] = useState<any>({})

  useEffect(() => {
    fetchTasks()
    fetchDataSources()
    fetchStatistics()
    fetchRunningTasks()
    
    // 定时刷新运行状态
    const interval = setInterval(fetchRunningTasks, 5000)
    return () => clearInterval(interval)
  }, [pagination.current, pagination.pageSize, filters])

  const fetchTasks = async () => {
    setLoading(true)
    try {
      const response = await syncAPI.getTasks({
        current: pagination.current,
        size: pagination.pageSize,
        ...filters
      })
      
      if (response.code === 200) {
        setTasks(response.data.records)
        setPagination(prev => ({
          ...prev,
          total: response.data.total
        }))
      }
    } catch (error) {
      message.error('获取同步任务列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchDataSources = async () => {
    try {
      const response = await dataSourceAPI.getDataSources({
        current: 1,
        size: 1000
      })
      
      if (response.code === 200) {
        setDataSources(response.data.records)
      }
    } catch (error) {
      console.error('获取数据源列表失败:', error)
    }
  }

  const fetchStatistics = async () => {
    try {
      const response = await syncAPI.getSyncStatistics()
      if (response.code === 200) {
        setStatistics(response.data)
      }
    } catch (error) {
      console.error('获取统计信息失败:', error)
    }
  }

  const fetchRunningTasks = async () => {
    try {
      const response = await syncAPI.getRunningTasks()
      if (response.code === 200) {
        setRunningTasks(response.data)
      }
    } catch (error) {
      console.error('获取运行中任务失败:', error)
    }
  }

  const handleExecute = async (record: any) => {
    try {
      const response = await syncAPI.executeTask(record.id)
      if (response.code === 200) {
        message.success('任务执行成功')
        setSelectedTask(record)
        setExecutionVisible(true)
        fetchRunningTasks()
      } else {
        message.error(response.message || '任务执行失败')
      }
    } catch (error) {
      message.error('任务执行失败')
    }
  }

  const handleScheduleToggle = async (record: any) => {
    try {
      if (record.isScheduled) {
        await syncAPI.unscheduleTask(record.id)
        message.success('取消调度成功')
      } else {
        await syncAPI.scheduleTask(record.id)
        message.success('开启调度成功')
      }
      fetchTasks()
    } catch (error) {
      message.error('调度操作失败')
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await syncAPI.deleteTask(id)
      message.success('删除成功')
      fetchTasks()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleEdit = (record: any) => {
    setEditingTask(record)
    setFormVisible(true)
  }

  const handleFormSubmit = async (values: SyncTask) => {
    try {
      if (editingTask) {
        await syncAPI.updateTask(editingTask.id!, values)
        message.success('更新成功')
      } else {
        await syncAPI.createTask(values)
        message.success('创建成功')
      }
      
      setFormVisible(false)
      setEditingTask(null)
      fetchTasks()
      fetchStatistics()
    } catch (error) {
      message.error(editingTask ? '更新失败' : '创建失败')
    }
  }

  const getStatusTag = (status: number, isEnabled: boolean) => {
    if (!isEnabled) {
      return <Tag color="default">已禁用</Tag>
    }
    
    switch (status) {
      case 0:
        return <Tag color="blue">待运行</Tag>
      case 1:
        return <Tag color="green">运行中</Tag>
      case 2:
        return <Tag color="orange">已暂停</Tag>
      case 3:
        return <Tag color="red">已停止</Tag>
      default:
        return <Tag color="default">未知</Tag>
    }
  }

  const getSyncModeTag = (syncMode: string) => {
    const colors = {
      'FULL': 'blue',
      'INCREMENTAL': 'green'
    }
    
    const labels = {
      'FULL': '全量同步',
      'INCREMENTAL': '增量同步'
    }
    
    return <Tag color={colors[syncMode as keyof typeof colors]}>{labels[syncMode as keyof typeof labels]}</Tag>
  }

  const getRunningStatus = (taskId: number) => {
    const runningTask = runningTasks.find(task => task.taskId === taskId)
    if (!runningTask) return null
    
    const statusColors = {
      'PENDING': 'blue',
      'RUNNING': 'green',
      'SUCCESS': 'green',
      'FAILED': 'red',
      'CANCELLED': 'orange'
    }
    
    const statusLabels = {
      'PENDING': '等待中',
      'RUNNING': '运行中',
      'SUCCESS': '成功',
      'FAILED': '失败',
      'CANCELLED': '已取消'
    }
    
    return (
      <div>
        <Badge status="processing" />
        <Tag color={statusColors[runningTask.status as keyof typeof statusColors]}>
          {statusLabels[runningTask.status as keyof typeof statusLabels]}
        </Tag>
        {runningTask.progressPercent !== undefined && (
          <Text type="secondary">
            ({runningTask.progressPercent.toFixed(1)}%)
          </Text>
        )}
      </div>
    )
  }

  const columns = [
    {
      title: '任务名称',
      dataIndex: 'taskName',
      key: 'taskName',
      width: 150,
      render: (text: string, record: any) => (
        <div>
          <div>{text}</div>
          <Text type="secondary" style={{ fontSize: 12 }}>{record.taskCode}</Text>
        </div>
      )
    },
    {
      title: '同步类型',
      dataIndex: 'syncType',
      key: 'syncType',
      width: 100,
      render: (text: string) => (
        <Tag color={text === 'TABLE' ? 'blue' : 'purple'}>
          {text === 'TABLE' ? '表同步' : 'SQL同步'}
        </Tag>
      )
    },
    {
      title: '同步模式',
      dataIndex: 'syncMode',
      key: 'syncMode',
      width: 100,
      render: (text: string) => getSyncModeTag(text)
    },
    {
      title: 'Cron表达式',
      dataIndex: 'cronExpression',
      key: 'cronExpression',
      width: 120,
      render: (text: string, record: any) => (
        <div>
          {text ? (
            <div>
              <Text code style={{ fontSize: 11 }}>{text}</Text>
              {record.nextFireTime && (
                <div>
                  <Text type="secondary" style={{ fontSize: 11 }}>
                    下次: {new Date(record.nextFireTime).toLocaleString()}
                  </Text>
                </div>
              )}
            </div>
          ) : (
            <Text type="secondary">手动执行</Text>
          )}
        </div>
      )
    },
    {
      title: '状态',
      key: 'status',
      width: 120,
      render: (_: any, record: any) => (
        <div>
          {getStatusTag(record.status, record.isEnabled)}
          {getRunningStatus(record.id)}
        </div>
      )
    },
    {
      title: '调度状态',
      key: 'schedule',
      width: 100,
      render: (_: any, record: any) => (
        <div>
          {record.cronExpression ? (
            record.isScheduled ? (
              <Tag color="green" icon={<CheckCircleOutlined />}>已调度</Tag>
            ) : (
              <Tag color="red" icon={<ExclamationCircleOutlined />}>未调度</Tag>
            )
          ) : (
            <Tag color="default">手动</Tag>
          )}
        </div>
      )
    },
    {
      title: '创建时间',
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 120,
      render: (text: string) => new Date(text).toLocaleString()
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: any, record: any) => (
        <Space size="small">
          <Tooltip title="执行任务">
            <Button
              type="primary"
              size="small"
              icon={<PlayCircleOutlined />}
              onClick={() => handleExecute(record)}
              disabled={!record.isEnabled}
            />
          </Tooltip>
          
          {record.cronExpression && (
            <Tooltip title={record.isScheduled ? "取消调度" : "开启调度"}>
              <Button
                size="small"
                icon={<ScheduleOutlined />}
                onClick={() => handleScheduleToggle(record)}
                type={record.isScheduled ? "default" : "primary"}
              />
            </Tooltip>
          )}
          
          <Tooltip title="查看执行">
            <Button
              size="small"
              icon={<EyeOutlined />}
              onClick={() => {
                setSelectedTask(record)
                setExecutionVisible(true)
              }}
            />
          </Tooltip>
          
          <Tooltip title="编辑">
            <Button
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          
          <Popconfirm
            title="确定要删除这个同步任务吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Tooltip title="删除">
              <Button
                size="small"
                danger
                icon={<DeleteOutlined />}
              />
            </Tooltip>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div>
      {/* 统计概览 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="总任务数"
              value={statistics.taskStats?.total_tasks || 0}
              prefix={<SyncOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="已启用任务"
              value={statistics.taskStats?.enabled_tasks || 0}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="运行中任务"
              value={runningTasks.length}
              prefix={<PlayCircleOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="表同步任务"
              value={statistics.taskStats?.table_sync_tasks || 0}
              prefix={<SyncOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Card title={<Title level={4}>同步任务管理</Title>}>
        {/* 搜索和过滤 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Search
              placeholder="搜索任务名称"
              allowClear
              value={filters.taskName}
              onChange={(e) => setFilters(prev => ({ ...prev, taskName: e.target.value }))}
              onSearch={() => {
                setPagination(prev => ({ ...prev, current: 1 }))
                fetchTasks()
              }}
            />
          </Col>
          <Col span={4}>
            <Select
              placeholder="任务类型"
              allowClear
              style={{ width: '100%' }}
              value={filters.taskType}
              onChange={(value) => {
                setFilters(prev => ({ ...prev, taskType: value }))
                setPagination(prev => ({ ...prev, current: 1 }))
              }}
            >
              <Option value="TABLE">表同步</Option>
              <Option value="SQL">SQL同步</Option>
            </Select>
          </Col>
          <Col span={4}>
            <Select
              placeholder="启用状态"
              allowClear
              style={{ width: '100%' }}
              value={filters.isEnabled}
              onChange={(value) => {
                setFilters(prev => ({ ...prev, isEnabled: value }))
                setPagination(prev => ({ ...prev, current: 1 }))
              }}
            >
              <Option value={true}>已启用</Option>
              <Option value={false}>已禁用</Option>
            </Select>
          </Col>
          <Col span={10}>
            <Space>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => {
                  setEditingTask(null)
                  setFormVisible(true)
                }}
              >
                新建任务
              </Button>
              <Button
                icon={<ReloadOutlined />}
                onClick={() => {
                  fetchTasks()
                  fetchStatistics()
                  fetchRunningTasks()
                }}
              >
                刷新
              </Button>
            </Space>
          </Col>
        </Row>

        <Table
          columns={columns}
          dataSource={tasks}
          rowKey="id"
          loading={loading}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
            onChange: (current, pageSize) => {
              setPagination(prev => ({
                ...prev,
                current,
                pageSize: pageSize || prev.pageSize
              }))
            }
          }}
          scroll={{ x: 1200 }}
        />
      </Card>

      {/* 任务表单 */}
      <SyncTaskForm
        visible={formVisible}
        task={editingTask}
        dataSources={dataSources}
        onSubmit={handleFormSubmit}
        onCancel={() => {
          setFormVisible(false)
          setEditingTask(null)
        }}
      />

      {/* 执行监控 */}
      <ExecutionModal
        visible={executionVisible}
        task={selectedTask}
        onCancel={() => {
          setExecutionVisible(false)
          setSelectedTask(null)
        }}
        onRefresh={fetchRunningTasks}
      />
    </div>
  )
}

export default SyncTaskList