import React, { useState, useEffect } from 'react'
import {
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  Select,
  InputNumber,
  Switch,
  message,
  Popconfirm,
  Card,
  Row,
  Col,
  Typography,
  Tooltip,
  Progress,
  Statistic
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  ReloadOutlined,
  SearchOutlined,
  DatabaseOutlined,
  LineChartOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { dataSourceAPI } from '@/services'
import { usePermission } from '@/hooks'
import DataSourceForm from './components/DataSourceForm'
import ConnectionTest from './components/ConnectionTest'
import MonitorModal from './components/MonitorModal'
import type { DataSource } from '@/types'
import './index.css'

const { Option } = Select
const { Title } = Typography

const DataSourceManagement: React.FC = () => {
  const [dataSources, setDataSources] = useState<DataSource[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [testModalVisible, setTestModalVisible] = useState(false)
  const [monitorModalVisible, setMonitorModalVisible] = useState(false)
  const [editingDataSource, setEditingDataSource] = useState<DataSource | null>(null)
  const [selectedDataSource, setSelectedDataSource] = useState<DataSource | null>(null)
  const [stats, setStats] = useState<any[]>([])
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  })
  const [searchForm] = Form.useForm()
  const { hasPermission } = usePermission()

  const fetchDataSources = async (params = {}) => {
    setLoading(true)
    try {
      const response = await dataSourceAPI.getDataSourcePage({
        current: pagination.current,
        size: pagination.pageSize,
        ...params
      })
      if (response.code === 200) {
        setDataSources(response.data.records)
        setPagination(prev => ({
          ...prev,
          total: response.data.total
        }))
      }
    } catch (error) {
      message.error('获取数据源列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchStats = async () => {
    try {
      const response = await dataSourceAPI.getDataSourceStats()
      if (response.code === 200) {
        setStats(response.data)
      }
    } catch (error) {
      console.error('获取统计信息失败:', error)
    }
  }

  useEffect(() => {
    fetchDataSources()
    fetchStats()
    
    // 定时刷新统计信息
    const interval = setInterval(fetchStats, 30000)
    return () => clearInterval(interval)
  }, [pagination.current, pagination.pageSize])

  const handleSearch = () => {
    const values = searchForm.getFieldsValue()
    setPagination(prev => ({ ...prev, current: 1 }))
    fetchDataSources(values)
  }

  const handleReset = () => {
    searchForm.resetFields()
    setPagination(prev => ({ ...prev, current: 1 }))
    fetchDataSources()
  }

  const handleAdd = () => {
    setEditingDataSource(null)
    setModalVisible(true)
  }

  const handleEdit = (dataSource: DataSource) => {
    setEditingDataSource(dataSource)
    setModalVisible(true)
  }

  const handleDelete = async (dataSourceId: number) => {
    try {
      const response = await dataSourceAPI.deleteDataSource(dataSourceId)
      if (response.code === 200) {
        message.success('删除成功')
        fetchDataSources()
        fetchStats()
      } else {
        message.error(response.message)
      }
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleTest = (dataSource: DataSource) => {
    setSelectedDataSource(dataSource)
    setTestModalVisible(true)
  }

  const handleMonitor = (dataSource: DataSource) => {
    setSelectedDataSource(dataSource)
    setMonitorModalVisible(true)
  }

  const handleRefresh = async (dataSourceId: number) => {
    try {
      const response = await dataSourceAPI.refreshDataSource(dataSourceId)
      if (response.code === 200) {
        message.success('刷新成功')
        fetchStats()
      } else {
        message.error(response.message)
      }
    } catch (error) {
      message.error('刷新失败')
    }
  }

  const handleTableChange = (paginationConfig: any) => {
    setPagination(prev => ({
      ...prev,
      current: paginationConfig.current,
      pageSize: paginationConfig.pageSize
    }))
  }

  const getStatusColor = (status: number) => {
    return status === 1 ? 'success' : 'error'
  }

  const getStatusText = (status: number) => {
    return status === 1 ? '活跃' : '禁用'
  }

  const getDatabaseTypeColor = (type: string) => {
    const colorMap: Record<string, string> = {
      'MySQL': 'blue',
      'PostgreSQL': 'green',
      'Oracle': 'orange',
      'SQL Server': 'purple',
      'H2': 'cyan'
    }
    return colorMap[type] || 'default'
  }

  const columns: ColumnsType<DataSource> = [
    {
      title: '数据源名称',
      dataIndex: 'datasourceName',
      width: 150,
      render: (text, record) => (
        <div className="datasource-name">
          <DatabaseOutlined style={{ marginRight: 8, color: '#1890ff' }} />
          <span>{text}</span>
          {record.isDefault && (
            <Tag color="gold" size="small" style={{ marginLeft: 8 }}>
              默认
            </Tag>
          )}
        </div>
      )
    },
    {
      title: '数据源编码',
      dataIndex: 'datasourceCode',
      width: 120,
      render: (text) => <code className="datasource-code">{text}</code>
    },
    {
      title: '数据库类型',
      dataIndex: 'databaseType',
      width: 120,
      render: (type) => (
        <Tag color={getDatabaseTypeColor(type)}>{type}</Tag>
      )
    },
    {
      title: 'JDBC URL',
      dataIndex: 'jdbcUrl',
      width: 250,
      ellipsis: true,
      render: (url) => (
        <Tooltip title={url}>
          <span className="jdbc-url">{url}</span>
        </Tooltip>
      )
    },
    {
      title: '用户名',
      dataIndex: 'username',
      width: 100,
      ellipsis: true
    },
    {
      title: '连接池配置',
      key: 'poolConfig',
      width: 150,
      render: (_, record) => (
        <div className="pool-config">
          <div>最小: {record.minIdle}</div>
          <div>最大: {record.maxActive}</div>
        </div>
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status, record) => {
        const stat = stats.find(s => s.datasourceCode === record.datasourceCode)
        return (
          <Space direction="vertical" size={4}>
            <Tag color={getStatusColor(status)}>
              {getStatusText(status)}
            </Tag>
            {stat && (
              <Tag color={stat.isClosed ? 'error' : 'success'} size="small">
                {stat.isClosed ? '断开' : '连接'}
              </Tag>
            )}
          </Space>
        )
      }
    },
    {
      title: '连接数',
      key: 'connections',
      width: 120,
      render: (_, record) => {
        const stat = stats.find(s => s.datasourceCode === record.datasourceCode)
        if (!stat) return '-'
        
        const usage = (stat.activeConnections / stat.maximumPoolSize) * 100
        return (
          <div className="connection-stats">
            <div>{stat.activeConnections}/{stat.maximumPoolSize}</div>
            <Progress 
              percent={usage} 
              size="small" 
              status={usage > 80 ? 'exception' : 'normal'}
              showInfo={false}
            />
          </div>
        )
      }
    },
    {
      title: '创建时间',
      dataIndex: 'createdTime',
      width: 160,
      render: (time) => time || '-'
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      fixed: 'right',
      render: (_, record) => (
        <Space wrap>
          <Tooltip title="测试连接">
            <Button
              type="link"
              size="small"
              icon={<PlayCircleOutlined />}
              onClick={() => handleTest(record)}
            />
          </Tooltip>
          
          {hasPermission('DATASOURCE_UPDATE') && (
            <Tooltip title="编辑">
              <Button
                type="link"
                size="small"
                icon={<EditOutlined />}
                onClick={() => handleEdit(record)}
              />
            </Tooltip>
          )}
          
          {hasPermission('DATASOURCE_MONITOR') && (
            <Tooltip title="监控">
              <Button
                type="link"
                size="small"
                icon={<LineChartOutlined />}
                onClick={() => handleMonitor(record)}
              />
            </Tooltip>
          )}
          
          {hasPermission('DATASOURCE_MANAGE') && (
            <Tooltip title="刷新">
              <Button
                type="link"
                size="small"
                icon={<ReloadOutlined />}
                onClick={() => handleRefresh(record.id)}
              />
            </Tooltip>
          )}
          
          {hasPermission('DATASOURCE_DELETE') && !record.isDefault && (
            <Popconfirm
              title="确认删除？"
              description="删除后无法恢复"
              onConfirm={() => handleDelete(record.id)}
            >
              <Tooltip title="删除">
                <Button
                  type="link"
                  size="small"
                  danger
                  icon={<DeleteOutlined />}
                />
              </Tooltip>
            </Popconfirm>
          )}
        </Space>
      )
    }
  ]

  return (
    <div className="datasource-management">
      <Card>
        <div className="page-header">
          <Title level={4}>数据源管理</Title>
        </div>

        {/* 统计卡片 */}
        <Row gutter={16} className="stats-cards">
          <Col span={6}>
            <Card size="small">
              <Statistic
                title="总数据源数"
                value={dataSources.length}
                prefix={<DatabaseOutlined />}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic
                title="活跃数据源"
                value={dataSources.filter(ds => ds.status === 1).length}
                prefix={<CheckCircleOutlined />}
                valueStyle={{ color: '#52c41a' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic
                title="禁用数据源"
                value={dataSources.filter(ds => ds.status === 0).length}
                prefix={<CloseCircleOutlined />}
                valueStyle={{ color: '#ff4d4f' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic
                title="总连接数"
                value={stats.reduce((sum, stat) => sum + (stat.totalConnections || 0), 0)}
                prefix={<LineChartOutlined />}
                valueStyle={{ color: '#722ed1' }}
              />
            </Card>
          </Col>
        </Row>
        
        {/* 搜索表单 */}
        <Card size="small" className="search-card">
          <Form form={searchForm} layout="inline">
            <Row gutter={16} style={{ width: '100%' }}>
              <Col>
                <Form.Item name="datasourceName" label="数据源名称">
                  <Input placeholder="请输入数据源名称" allowClear />
                </Form.Item>
              </Col>
              <Col>
                <Form.Item name="datasourceCode" label="数据源编码">
                  <Input placeholder="请输入数据源编码" allowClear />
                </Form.Item>
              </Col>
              <Col>
                <Form.Item name="databaseType" label="数据库类型">
                  <Select placeholder="请选择数据库类型" allowClear style={{ width: 150 }}>
                    <Option value="MySQL">MySQL</Option>
                    <Option value="PostgreSQL">PostgreSQL</Option>
                    <Option value="Oracle">Oracle</Option>
                    <Option value="SQL Server">SQL Server</Option>
                    <Option value="H2">H2</Option>
                  </Select>
                </Form.Item>
              </Col>
              <Col>
                <Form.Item name="status" label="状态">
                  <Select placeholder="请选择状态" allowClear style={{ width: 120 }}>
                    <Option value={1}>活跃</Option>
                    <Option value={0}>禁用</Option>
                  </Select>
                </Form.Item>
              </Col>
              <Col>
                <Space>
                  <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                    搜索
                  </Button>
                  <Button icon={<ReloadOutlined />} onClick={handleReset}>
                    重置
                  </Button>
                </Space>
              </Col>
            </Row>
          </Form>
        </Card>

        {/* 操作按钮 */}
        <div className="table-toolbar">
          {hasPermission('DATASOURCE_CREATE') && (
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增数据源
            </Button>
          )}
        </div>

        {/* 数据源表格 */}
        <Table
          columns={columns}
          dataSource={dataSources}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条/共 ${total} 条`
          }}
          onChange={handleTableChange}
          scroll={{ x: 1400 }}
        />
      </Card>

      {/* 数据源表单弹窗 */}
      <DataSourceForm
        visible={modalVisible}
        dataSource={editingDataSource}
        onCancel={() => {
          setModalVisible(false)
          setEditingDataSource(null)
        }}
        onSuccess={() => {
          setModalVisible(false)
          setEditingDataSource(null)
          fetchDataSources()
          fetchStats()
        }}
      />

      {/* 连接测试弹窗 */}
      {selectedDataSource && (
        <ConnectionTest
          visible={testModalVisible}
          dataSource={selectedDataSource}
          onCancel={() => {
            setTestModalVisible(false)
            setSelectedDataSource(null)
          }}
        />
      )}

      {/* 监控弹窗 */}
      {selectedDataSource && (
        <MonitorModal
          visible={monitorModalVisible}
          dataSource={selectedDataSource}
          onCancel={() => {
            setMonitorModalVisible(false)
            setSelectedDataSource(null)
          }}
        />
      )}
    </div>
  )
}

export default DataSourceManagement