import React, { useState, useEffect } from 'react'
import {
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  InputNumber,
  Select,
  message,
  Popconfirm,
  Card,
  Row,
  Col,
  Typography,
  Tooltip
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SettingOutlined,
  SearchOutlined,
  ReloadOutlined,
  KeyOutlined
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { roleAPI, permissionAPI } from '@/services'
import { usePermission } from '@/hooks'
import RolePermissionModal from './components/RolePermissionModal'
import type { Role } from '@/types'
import './index.css'

const { Option } = Select
const { Title } = Typography

interface RoleFormData {
  roleName: string
  roleCode: string
  description?: string
  dataScope?: string
  sortOrder: number
  remarks?: string
}

const RoleManagement: React.FC = () => {
  const [roles, setRoles] = useState<Role[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [permissionModalVisible, setPermissionModalVisible] = useState(false)
  const [editingRole, setEditingRole] = useState<Role | null>(null)
  const [selectedRole, setSelectedRole] = useState<Role | null>(null)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  })
  const [searchForm] = Form.useForm()
  const [roleForm] = Form.useForm()
  const { hasPermission } = usePermission()

  const fetchRoles = async (params = {}) => {
    setLoading(true)
    try {
      const response = await roleAPI.getRolePage({
        current: pagination.current,
        size: pagination.pageSize,
        ...params
      })
      if (response.code === 200) {
        setRoles(response.data.records)
        setPagination(prev => ({
          ...prev,
          total: response.data.total
        }))
      }
    } catch (error) {
      message.error('获取角色列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchRoles()
  }, [pagination.current, pagination.pageSize])

  const handleSearch = () => {
    const values = searchForm.getFieldsValue()
    setPagination(prev => ({ ...prev, current: 1 }))
    fetchRoles(values)
  }

  const handleReset = () => {
    searchForm.resetFields()
    setPagination(prev => ({ ...prev, current: 1 }))
    fetchRoles()
  }

  const handleAdd = () => {
    setEditingRole(null)
    roleForm.resetFields()
    roleForm.setFieldsValue({
      dataScope: 'ALL',
      sortOrder: 0
    })
    setModalVisible(true)
  }

  const handleEdit = (role: Role) => {
    setEditingRole(role)
    roleForm.setFieldsValue(role)
    setModalVisible(true)
  }

  const handleDelete = async (roleId: number) => {
    try {
      const response = await roleAPI.deleteRole(roleId)
      if (response.code === 200) {
        message.success('删除成功')
        fetchRoles()
      } else {
        message.error(response.message)
      }
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleStatusChange = async (roleId: number, status: number) => {
    try {
      const response = await roleAPI.updateRoleStatus(roleId, status)
      if (response.code === 200) {
        message.success('状态更新成功')
        fetchRoles()
      } else {
        message.error(response.message)
      }
    } catch (error) {
      message.error('状态更新失败')
    }
  }

  const handleModalOk = async () => {
    try {
      const values = await roleForm.validateFields()
      
      if (editingRole) {
        const response = await roleAPI.updateRole(editingRole.id, values)
        if (response.code === 200) {
          message.success('更新成功')
          setModalVisible(false)
          fetchRoles()
        } else {
          message.error(response.message)
        }
      } else {
        const response = await roleAPI.createRole(values)
        if (response.code === 200) {
          message.success('创建成功')
          setModalVisible(false)
          fetchRoles()
        } else {
          message.error(response.message)
        }
      }
    } catch (error) {
      console.error('表单验证失败:', error)
    }
  }

  const handleAssignPermission = (role: Role) => {
    setSelectedRole(role)
    setPermissionModalVisible(true)
  }

  const handleTableChange = (paginationConfig: any) => {
    setPagination(prev => ({
      ...prev,
      current: paginationConfig.current,
      pageSize: paginationConfig.pageSize
    }))
  }

  const columns: ColumnsType<Role> = [
    {
      title: '角色名称',
      dataIndex: 'roleName',
      width: 150,
      render: (text) => <span className="role-name">{text}</span>
    },
    {
      title: '角色编码',
      dataIndex: 'roleCode',
      width: 150,
      render: (text) => <code className="role-code">{text}</code>
    },
    {
      title: '描述',
      dataIndex: 'description',
      ellipsis: true,
      render: (text) => text || '-'
    },
    {
      title: '数据范围',
      dataIndex: 'dataScope',
      width: 120,
      render: (scope) => {
        const scopeMap: Record<string, { text: string; color: string }> = {
          'ALL': { text: '全部数据', color: 'blue' },
          'CUSTOM': { text: '自定义', color: 'green' },
          'DEPT': { text: '部门数据', color: 'orange' },
          'SELF': { text: '个人数据', color: 'purple' }
        }
        const config = scopeMap[scope] || { text: scope, color: 'default' }
        return <Tag color={config.color}>{config.text}</Tag>
      }
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      width: 80,
      align: 'center'
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status) => (
        <Tag color={status === 1 ? 'success' : 'error'}>
          {status === 1 ? '正常' : '禁用'}
        </Tag>
      )
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
      width: 200,
      fixed: 'right',
      render: (_, record) => (
        <Space wrap>
          {hasPermission('ROLE_UPDATE') && (
            <Tooltip title="编辑">
              <Button
                type="link"
                size="small"
                icon={<EditOutlined />}
                onClick={() => handleEdit(record)}
              />
            </Tooltip>
          )}
          
          {hasPermission('ROLE_ASSIGN_PERMISSION') && (
            <Tooltip title="分配权限">
              <Button
                type="link"
                size="small"
                icon={<KeyOutlined />}
                onClick={() => handleAssignPermission(record)}
              />
            </Tooltip>
          )}
          
          {hasPermission('ROLE_UPDATE') && (
            <Button
              type="link"
              size="small"
              onClick={() => handleStatusChange(record.id, record.status === 1 ? 0 : 1)}
            >
              {record.status === 1 ? '禁用' : '启用'}
            </Button>
          )}
          
          {hasPermission('ROLE_DELETE') && record.roleCode !== 'SUPER_ADMIN' && (
            <Popconfirm
              title="确认删除？"
              description="删除后无法恢复，且会影响相关用户权限"
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
    <div className="role-management">
      <Card>
        <div className="page-header">
          <Title level={4}>角色管理</Title>
        </div>
        
        {/* 搜索表单 */}
        <Card size="small" className="search-card">
          <Form form={searchForm} layout="inline">
            <Row gutter={16} style={{ width: '100%' }}>
              <Col>
                <Form.Item name="roleName" label="角色名称">
                  <Input placeholder="请输入角色名称" allowClear />
                </Form.Item>
              </Col>
              <Col>
                <Form.Item name="roleCode" label="角色编码">
                  <Input placeholder="请输入角色编码" allowClear />
                </Form.Item>
              </Col>
              <Col>
                <Form.Item name="status" label="状态">
                  <Select placeholder="请选择状态" allowClear style={{ width: 120 }}>
                    <Option value={1}>正常</Option>
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
          {hasPermission('ROLE_CREATE') && (
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增角色
            </Button>
          )}
        </div>

        {/* 角色表格 */}
        <Table
          columns={columns}
          dataSource={roles}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条/共 ${total} 条`
          }}
          onChange={handleTableChange}
          scroll={{ x: 1000 }}
        />
      </Card>

      {/* 角色表单弹窗 */}
      <Modal
        title={editingRole ? '编辑角色' : '新增角色'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
        width={600}
        destroyOnClose
      >
        <Form form={roleForm} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="roleName"
                label="角色名称"
                rules={[
                  { required: true, message: '请输入角色名称' },
                  { max: 50, message: '角色名称不能超过50个字符' }
                ]}
              >
                <Input placeholder="请输入角色名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="roleCode"
                label="角色编码"
                rules={[
                  { required: true, message: '请输入角色编码' },
                  { max: 50, message: '角色编码不能超过50个字符' },
                  { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线' }
                ]}
              >
                <Input placeholder="请输入角色编码" disabled={!!editingRole} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="description"
            label="角色描述"
            rules={[
              { max: 255, message: '描述不能超过255个字符' }
            ]}
          >
            <Input.TextArea placeholder="请输入角色描述" rows={3} />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="dataScope"
                label="数据范围"
                rules={[{ required: true, message: '请选择数据范围' }]}
              >
                <Select placeholder="请选择数据范围">
                  <Option value="ALL">全部数据</Option>
                  <Option value="CUSTOM">自定义</Option>
                  <Option value="DEPT">部门数据</Option>
                  <Option value="SELF">个人数据</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="sortOrder"
                label="排序"
                rules={[{ required: true, message: '请输入排序号' }]}
              >
                <InputNumber
                  placeholder="请输入排序号"
                  min={0}
                  max={9999}
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="remarks"
            label="备注"
            rules={[
              { max: 255, message: '备注不能超过255个字符' }
            ]}
          >
            <Input.TextArea placeholder="请输入备注" rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 权限分配弹窗 */}
      {selectedRole && (
        <RolePermissionModal
          visible={permissionModalVisible}
          role={selectedRole}
          onCancel={() => {
            setPermissionModalVisible(false)
            setSelectedRole(null)
          }}
          onSuccess={() => {
            setPermissionModalVisible(false)
            setSelectedRole(null)
            fetchRoles()
          }}
        />
      )}
    </div>
  )
}

export default RoleManagement