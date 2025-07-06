import React, { useState, useEffect } from 'react'
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Select,
  Row,
  Col,
  Tag,
  Tooltip,
  message,
  Modal,
  Popconfirm,
  Upload,
  Statistic,
  Typography,
  Divider,
  Badge
} from 'antd'
import {
  PlusOutlined,
  SearchOutlined,
  ExportOutlined,
  ImportOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  CopyOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  CloseCircleOutlined,
  ReloadOutlined,
  DownloadOutlined,
  BookOutlined,
  StarOutlined,
  TagsOutlined
} from '@ant-design/icons'
import { dictionaryAPI } from '@/services'
import type { DataDictionary } from '@/services'
import FieldForm from './components/FieldForm'
import FieldDetail from './components/FieldDetail'
import CategoryTree from './components/CategoryTree'
import StatisticsPanel from './components/StatisticsPanel'

const { Search } = Input
const { Option } = Select
const { Title, Text } = Typography

const DataDictionaryList: React.FC = () => {
  const [fields, setFields] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0
  })
  const [filters, setFilters] = useState({
    keyword: '',
    categoryId: undefined as number | undefined,
    dataType: '',
    approvalStatus: ''
  })
  const [selectedRowKeys, setSelectedRowKeys] = useState<number[]>([])
  const [formVisible, setFormVisible] = useState(false)
  const [detailVisible, setDetailVisible] = useState(false)
  const [editingField, setEditingField] = useState<DataDictionary | null>(null)
  const [selectedField, setSelectedField] = useState<any>(null)
  const [categoryTreeVisible, setCategoryTreeVisible] = useState(false)
  const [statisticsVisible, setStatisticsVisible] = useState(false)
  const [categories, setCategories] = useState<any[]>([])
  const [statistics, setStatistics] = useState<any>({})

  useEffect(() => {
    fetchFields()
    fetchCategories()
    fetchStatistics()
  }, [pagination.current, pagination.pageSize, filters])

  const fetchFields = async () => {
    setLoading(true)
    try {
      const response = await dictionaryAPI.getFields({
        current: pagination.current,
        size: pagination.pageSize,
        ...filters
      })
      
      if (response.code === 200) {
        setFields(response.data.records)
        setPagination(prev => ({
          ...prev,
          total: response.data.total
        }))
      }
    } catch (error) {
      message.error('获取字段列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchCategories = async () => {
    try {
      const response = await dictionaryAPI.getCategoryTree()
      if (response.code === 200) {
        setCategories(response.data)
      }
    } catch (error) {
      console.error('获取分类树失败:', error)
    }
  }

  const fetchStatistics = async () => {
    try {
      const response = await dictionaryAPI.getStatistics()
      if (response.code === 200) {
        setStatistics(response.data)
      }
    } catch (error) {
      console.error('获取统计信息失败:', error)
    }
  }

  const handleCreate = () => {
    setEditingField(null)
    setFormVisible(true)
  }

  const handleEdit = (record: any) => {
    setEditingField(record)
    setFormVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await dictionaryAPI.deleteField(id)
      message.success('删除成功')
      fetchFields()
      fetchStatistics()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleFormSubmit = async (values: DataDictionary) => {
    try {
      if (editingField) {
        await dictionaryAPI.updateField(editingField.id!, values)
        message.success('更新成功')
      } else {
        await dictionaryAPI.createField(values)
        message.success('创建成功')
      }
      
      setFormVisible(false)
      setEditingField(null)
      fetchFields()
      fetchStatistics()
    } catch (error) {
      message.error(editingField ? '更新失败' : '创建失败')
    }
  }

  const handleViewDetail = (record: any) => {
    setSelectedField(record)
    setDetailVisible(true)
    
    // 记录查看使用统计
    dictionaryAPI.recordUsage(record.id, 'VIEW', 'DETAIL_PAGE')
  }

  const handleCopy = async (record: any) => {
    Modal.confirm({
      title: '复制字段',
      content: (
        <div>
          <p>确定要复制字段 "{record.fieldNameCn}" 吗？</p>
          <Input placeholder="请输入新的字段编码" id="newFieldCode" />
        </div>
      ),
      onOk: async () => {
        const newFieldCode = (document.getElementById('newFieldCode') as HTMLInputElement)?.value
        if (!newFieldCode) {
          message.error('请输入新的字段编码')
          return
        }
        
        try {
          await dictionaryAPI.copyField(record.id, newFieldCode)
          message.success('复制成功')
          fetchFields()
        } catch (error) {
          message.error('复制失败')
        }
      }
    })
  }

  const handleBatchApprove = async (status: string) => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要审批的字段')
      return
    }

    try {
      await dictionaryAPI.batchApprove(selectedRowKeys, status, 'admin')
      message.success('批量审批成功')
      setSelectedRowKeys([])
      fetchFields()
    } catch (error) {
      message.error('批量审批失败')
    }
  }

  const handleImport = async (file: File) => {
    try {
      const response = await dictionaryAPI.importFields(file)
      if (response.code === 200) {
        const { successCount, errorCount, errors } = response.data
        
        if (errorCount === 0) {
          message.success(`导入成功，共导入 ${successCount} 条记录`)
        } else {
          Modal.info({
            title: '导入结果',
            content: (
              <div>
                <p>成功导入：{successCount} 条</p>
                <p>失败：{errorCount} 条</p>
                {errors.length > 0 && (
                  <div>
                    <p>错误详情：</p>
                    <ul style={{ maxHeight: 200, overflow: 'auto' }}>
                      {errors.map((error: string, index: number) => (
                        <li key={index}>{error}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            ),
            width: 600
          })
        }
        
        fetchFields()
        fetchStatistics()
      }
    } catch (error) {
      message.error('导入失败')
    }
  }

  const handleExport = async () => {
    try {
      const response = await dictionaryAPI.exportFields(
        selectedRowKeys.length > 0 ? selectedRowKeys : undefined
      )
      
      // 创建下载链接
      const blob = new Blob([response], { 
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
      })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `数据字典_${new Date().toISOString().split('T')[0]}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      message.success('导出成功')
    } catch (error) {
      message.error('导出失败')
    }
  }

  const getApprovalStatusTag = (status: string) => {
    const statusConfig = {
      'PENDING': { color: 'orange', text: '待审批', icon: <ExclamationCircleOutlined /> },
      'APPROVED': { color: 'green', text: '已审批', icon: <CheckCircleOutlined /> },
      'REJECTED': { color: 'red', text: '已拒绝', icon: <CloseCircleOutlined /> }
    }
    
    const config = statusConfig[status as keyof typeof statusConfig]
    return config ? (
      <Tag color={config.color} icon={config.icon}>
        {config.text}
      </Tag>
    ) : <Tag>{status}</Tag>
  }

  const getDataTypeTag = (dataType: string) => {
    const typeColors = {
      'VARCHAR': 'blue',
      'INT': 'green',
      'DECIMAL': 'orange',
      'DATE': 'purple',
      'DATETIME': 'purple',
      'TEXT': 'cyan',
      'BOOLEAN': 'magenta'
    }
    
    return (
      <Tag color={typeColors[dataType as keyof typeof typeColors] || 'default'}>
        {dataType}
      </Tag>
    )
  }

  const columns = [
    {
      title: '字段编码',
      dataIndex: 'fieldCode',
      key: 'fieldCode',
      width: 150,
      render: (text: string, record: any) => (
        <div>
          <Text code>{text}</Text>
          {record.isStandard && (
            <Tooltip title="标准字段">
              <StarOutlined style={{ color: '#faad14', marginLeft: 4 }} />
            </Tooltip>
          )}
        </div>
      )
    },
    {
      title: '字段名称',
      key: 'fieldName',
      width: 200,
      render: (_: any, record: any) => (
        <div>
          <div style={{ fontWeight: 500 }}>{record.fieldNameCn}</div>
          {record.fieldNameEn && (
            <Text type="secondary" style={{ fontSize: 12 }}>
              {record.fieldNameEn}
            </Text>
          )}
        </div>
      )
    },
    {
      title: '数据类型',
      dataIndex: 'dataType',
      key: 'dataType',
      width: 100,
      render: (dataType: string, record: any) => (
        <div>
          {getDataTypeTag(dataType)}
          {record.dataLength && (
            <Text type="secondary" style={{ fontSize: 11 }}>
              ({record.dataLength}
              {record.dataPrecision && `,${record.dataPrecision}`}
              {record.dataScale && `,${record.dataScale}`})
            </Text>
          )}
        </div>
      )
    },
    {
      title: '业务含义',
      dataIndex: 'businessMeaning',
      key: 'businessMeaning',
      width: 250,
      ellipsis: { showTitle: false },
      render: (text: string) => (
        <Tooltip title={text} placement="topLeft">
          <span>{text}</span>
        </Tooltip>
      )
    },
    {
      title: '分类',
      dataIndex: 'categoryName',
      key: 'categoryName',
      width: 120,
      render: (text: string) => text && <Tag>{text}</Tag>
    },
    {
      title: '负责人',
      key: 'owner',
      width: 120,
      render: (_: any, record: any) => (
        <div>
          {record.ownerUser && <div>{record.ownerUser}</div>}
          {record.ownerDepartment && (
            <Text type="secondary" style={{ fontSize: 11 }}>
              {record.ownerDepartment}
            </Text>
          )}
        </div>
      )
    },
    {
      title: '使用次数',
      dataIndex: 'usageCount',
      key: 'usageCount',
      width: 80,
      render: (count: number) => (
        <Badge count={count} style={{ backgroundColor: '#52c41a' }} />
      )
    },
    {
      title: '审批状态',
      dataIndex: 'approvalStatus',
      key: 'approvalStatus',
      width: 100,
      render: (status: string) => getApprovalStatusTag(status)
    },
    {
      title: '更新时间',
      dataIndex: 'updatedTime',
      key: 'updatedTime',
      width: 120,
      render: (time: string) => time ? new Date(time).toLocaleDateString() : '-'
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_: any, record: any) => (
        <Space size="small">
          <Tooltip title="查看详情">
            <Button
              type="primary"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleViewDetail(record)}
            />
          </Tooltip>
          
          <Tooltip title="编辑">
            <Button
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          
          <Tooltip title="复制">
            <Button
              size="small"
              icon={<CopyOutlined />}
              onClick={() => handleCopy(record)}
            />
          </Tooltip>
          
          <Popconfirm
            title="确定要删除这个字段吗？"
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

  const rowSelection = {
    selectedRowKeys,
    onChange: (newSelectedRowKeys: React.Key[]) => {
      setSelectedRowKeys(newSelectedRowKeys as number[])
    }
  }

  return (
    <div>
      {/* 统计概览 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="总字段数"
              value={statistics.totalFields || 0}
              prefix={<BookOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="标准字段"
              value={statistics.standardFields || 0}
              prefix={<StarOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="待审批"
              value={statistics.pendingApproval || 0}
              prefix={<ExclamationCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="标准化率"
              value={statistics.standardRate || 0}
              suffix="%"
              precision={1}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
      </Row>

      <Card title={<Title level={4}>数据字典管理</Title>}>
        {/* 搜索和过滤 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Search
              placeholder="搜索字段名称、编码或含义"
              allowClear
              value={filters.keyword}
              onChange={(e) => setFilters(prev => ({ ...prev, keyword: e.target.value }))}
              onSearch={() => {
                setPagination(prev => ({ ...prev, current: 1 }))
                fetchFields()
              }}
            />
          </Col>
          <Col span={4}>
            <Select
              placeholder="选择分类"
              allowClear
              style={{ width: '100%' }}
              value={filters.categoryId}
              onChange={(value) => {
                setFilters(prev => ({ ...prev, categoryId: value }))
                setPagination(prev => ({ ...prev, current: 1 }))
              }}
            >
              {categories.map((category: any) => (
                <Option key={category.key} value={category.value}>
                  {category.title}
                </Option>
              ))}
            </Select>
          </Col>
          <Col span={4}>
            <Select
              placeholder="数据类型"
              allowClear
              style={{ width: '100%' }}
              value={filters.dataType}
              onChange={(value) => {
                setFilters(prev => ({ ...prev, dataType: value }))
                setPagination(prev => ({ ...prev, current: 1 }))
              }}
            >
              <Option value="VARCHAR">VARCHAR</Option>
              <Option value="INT">INT</Option>
              <Option value="DECIMAL">DECIMAL</Option>
              <Option value="DATE">DATE</Option>
              <Option value="DATETIME">DATETIME</Option>
              <Option value="TEXT">TEXT</Option>
              <Option value="BOOLEAN">BOOLEAN</Option>
            </Select>
          </Col>
          <Col span={4}>
            <Select
              placeholder="审批状态"
              allowClear
              style={{ width: '100%' }}
              value={filters.approvalStatus}
              onChange={(value) => {
                setFilters(prev => ({ ...prev, approvalStatus: value }))
                setPagination(prev => ({ ...prev, current: 1 }))
              }}
            >
              <Option value="PENDING">待审批</Option>
              <Option value="APPROVED">已审批</Option>
              <Option value="REJECTED">已拒绝</Option>
            </Select>
          </Col>
          <Col span={6}>
            <Space>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={handleCreate}
              >
                新建字段
              </Button>
              
              <Button
                icon={<TagsOutlined />}
                onClick={() => setCategoryTreeVisible(true)}
              >
                分类管理
              </Button>
              
              <Button
                icon={<SearchOutlined />}
                onClick={() => setStatisticsVisible(true)}
              >
                统计分析
              </Button>
            </Space>
          </Col>
        </Row>

        {/* 批量操作 */}
        {selectedRowKeys.length > 0 && (
          <Row style={{ marginBottom: 16 }}>
            <Col span={24}>
              <Space>
                <Text>已选择 {selectedRowKeys.length} 项</Text>
                <Divider type="vertical" />
                <Button
                  size="small"
                  onClick={() => handleBatchApprove('APPROVED')}
                >
                  批量通过
                </Button>
                <Button
                  size="small"
                  onClick={() => handleBatchApprove('REJECTED')}
                >
                  批量拒绝
                </Button>
                <Button
                  size="small"
                  icon={<ExportOutlined />}
                  onClick={handleExport}
                >
                  导出选中
                </Button>
              </Space>
            </Col>
          </Row>
        )}

        {/* 工具栏 */}
        <Row justify="space-between" style={{ marginBottom: 16 }}>
          <Col>
            <Space>
              <Upload
                accept=".xlsx,.xls"
                showUploadList={false}
                beforeUpload={(file) => {
                  handleImport(file)
                  return false
                }}
              >
                <Button icon={<ImportOutlined />}>
                  导入字段
                </Button>
              </Upload>
              
              <Button
                icon={<ExportOutlined />}
                onClick={handleExport}
              >
                导出全部
              </Button>
            </Space>
          </Col>
          
          <Col>
            <Button
              icon={<ReloadOutlined />}
              onClick={() => {
                fetchFields()
                fetchStatistics()
              }}
            >
              刷新
            </Button>
          </Col>
        </Row>

        <Table
          columns={columns}
          dataSource={fields}
          rowKey="id"
          rowSelection={rowSelection}
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
          scroll={{ x: 1400 }}
        />
      </Card>

      {/* 字段表单 */}
      <FieldForm
        visible={formVisible}
        field={editingField}
        categories={categories}
        onSubmit={handleFormSubmit}
        onCancel={() => {
          setFormVisible(false)
          setEditingField(null)
        }}
      />

      {/* 字段详情 */}
      <FieldDetail
        visible={detailVisible}
        field={selectedField}
        onCancel={() => {
          setDetailVisible(false)
          setSelectedField(null)
        }}
        onEdit={(field) => {
          setDetailVisible(false)
          setEditingField(field)
          setFormVisible(true)
        }}
      />

      {/* 分类管理 */}
      <CategoryTree
        visible={categoryTreeVisible}
        onCancel={() => setCategoryTreeVisible(false)}
        onRefresh={fetchCategories}
      />

      {/* 统计分析 */}
      <StatisticsPanel
        visible={statisticsVisible}
        statistics={statistics}
        onCancel={() => setStatisticsVisible(false)}
      />
    </div>
  )
}

export default DataDictionaryList