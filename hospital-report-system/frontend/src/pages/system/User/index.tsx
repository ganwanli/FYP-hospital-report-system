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
  Radio,
  DatePicker,
  message,
  Popconfirm,
  Card,
  Row,
  Col,
  Avatar,
  Typography,
  Tooltip
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  UnlockOutlined,
  ReloadOutlined,
  SearchOutlined,
  UserOutlined,
  SettingOutlined,
  LockOutlined
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { userAPI, roleAPI } from '@/services'
import { usePermission } from '@/hooks'
import UserRoleModal from './components/UserRoleModal'
import type { User, Role } from '@/types'
import './index.css'

const { Option } = Select
const { Title } = Typography

interface UserFormData {
  username: string
  password?: string
  realName: string
  email?: string
  phone?: string
  gender?: number
  departmentId?: number
  position?: string
  employeeId?: string
  remarks?: string
}

const UserManagement: React.FC = () => {
  const [users, setUsers] = useState<User[]>([])
  const [roles, setRoles] = useState<Role[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [roleModalVisible, setRoleModalVisible] = useState(false)
  const [editingUser, setEditingUser] = useState<User | null>(null)
  const [selectedUser, setSelectedUser] = useState<User | null>(null)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  })
  const [searchForm] = Form.useForm()
  const [userForm] = Form.useForm()
  const { hasPermission } = usePermission()

  const fetchUsers = async (params = {}) => {
    setLoading(true)
    try {
      const response = await userAPI.getUserPage({
        current: pagination.current,
        size: pagination.pageSize,
        ...params
      })
      if (response.code === 200) {
        setUsers(response.data.records)
        setPagination(prev => ({
          ...prev,
          total: response.data.total
        }))
      }
    } catch (error) {
      message.error('获取用户列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchRoles = async () => {
    try {
      const response = await roleAPI.getRoleList()
      if (response.code === 200) {
        setRoles(response.data)
      }
    } catch (error) {
      message.error('获取角色列表失败')
    }
  }

  useEffect(() => {
    fetchUsers()
    fetchRoles()
  }, [pagination.current, pagination.pageSize])

  const handleSearch = () => {
    const values = searchForm.getFieldsValue()
    setPagination(prev => ({ ...prev, current: 1 }))
    fetchUsers(values)
  }

  const handleReset = () => {
    searchForm.resetFields()
    setPagination(prev => ({ ...prev, current: 1 }))
    fetchUsers()
  }

  const handleAdd = () => {
    setEditingUser(null)
    userForm.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (user: User) => {
    setEditingUser(user)
    userForm.setFieldsValue({
      ...user,
      password: undefined
    })
    setModalVisible(true)
  }

  const handleDelete = async (userId: number) => {
    try {
      const response = await userAPI.deleteUser(userId)
      if (response.code === 200) {
        message.success('删除成功')
        fetchUsers()
      } else {
        message.error(response.message)
      }
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleStatusChange = async (userId: number, status: number) => {
    try {
      const response = await userAPI.updateUserStatus(userId, status)
      if (response.code === 200) {
        message.success('状态更新成功')
        fetchUsers()
      } else {
        message.error(response.message)
      }
    } catch (error) {
      message.error('状态更新失败')
    }
  }

  const handleResetPassword = async (userId: number) => {
    try {
      const response = await userAPI.resetUserPassword(userId)
      if (response.code === 200) {
        message.success('密码重置成功，新密码为：HospitalReport@123')
      } else {
        message.error(response.message)
      }
    } catch (error) {
      message.error('密码重置失败')
    }
  }

  const handleUnlock = async (userId: number) => {
    try {
      const response = await userAPI.unlockUser(userId)
      if (response.code === 200) {
        message.success('用户解锁成功')
        fetchUsers()
      } else {
        message.error(response.message)
      }
    } catch (error) {
      message.error('用户解锁失败')
    }
  }

  const handleModalOk = async () => {
    try {
      const values = await userForm.validateFields()
      
      if (editingUser) {
        const response = await userAPI.updateUser(editingUser.id, values)
        if (response.code === 200) {
          message.success('更新成功')
          setModalVisible(false)
          fetchUsers()
        } else {
          message.error(response.message)
        }
      } else {
        const response = await userAPI.createUser(values)
        if (response.code === 200) {
          message.success('创建成功')
          setModalVisible(false)
          fetchUsers()
        } else {
          message.error(response.message)
        }
      }
    } catch (error) {
      console.error('表单验证失败:', error)
    }
  }

  const handleAssignRole = (user: User) => {
    setSelectedUser(user)
    setRoleModalVisible(true)
  }

  const handleTableChange = (paginationConfig: any) => {
    setPagination(prev => ({
      ...prev,
      current: paginationConfig.current,
      pageSize: paginationConfig.pageSize
    }))
  }

  const columns: ColumnsType<User> = [
    {
      title: '头像',
      dataIndex: 'avatar',
      width: 80,
      render: (avatar, record) => (
        <Avatar 
          src={avatar} 
          icon={<UserOutlined />}
          alt={record.realName}
        />
      )
    },
    {
      title: '用户名',
      dataIndex: 'username',
      width: 120,
      render: (text) => <span className="username">{text}</span>
    },
    {
      title: '真实姓名',
      dataIndex: 'realName',
      width: 120
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      width: 180,
      ellipsis: true
    },
    {
      title: '电话',
      dataIndex: 'phone',
      width: 120
    },
    {
      title: '性别',
      dataIndex: 'gender',
      width: 80,
      render: (gender) => (
        <Tag color={gender === 1 ? 'blue' : gender === 2 ? 'pink' : 'default'}>
          {gender === 1 ? '男' : gender === 2 ? '女' : '未知'}
        </Tag>
      )
    },
    {
      title: '职位',
      dataIndex: 'position',
      width: 120,
      ellipsis: true
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status, record) => (
        <Space direction="vertical" size={4}>
          <Tag color={status === 1 ? 'success' : 'error'}>
            {status === 1 ? '正常' : '禁用'}
          </Tag>
          {record.isLocked && (
            <Tag color="warning" icon={<LockOutlined />}>
              已锁定
            </Tag>
          )}
        </Space>
      )
    },
    {
      title: '最后登录',
      dataIndex: 'lastLoginTime',
      width: 160,
      render: (time, record) => (
        <div>
          <div>{time || '从未登录'}</div>
          {record.lastLoginIp && (
            <div className="login-ip">IP: {record.lastLoginIp}</div>
          )}
        </div>
      )
    },
    {
      title: '操作',
      key: 'action',
      width: 280,
      fixed: 'right',
      render: (_, record) => (
        <Space wrap>
          {hasPermission('USER_UPDATE') && (
            <Tooltip title="编辑">
              <Button
                type="link"
                size="small"
                icon={<EditOutlined />}
                onClick={() => handleEdit(record)}
              />
            </Tooltip>
          )}
          
          {hasPermission('ROLE_ASSIGN_USER') && (
            <Tooltip title="分配角色">
              <Button
                type="link"
                size="small"
                icon={<SettingOutlined />}
                onClick={() => handleAssignRole(record)}
              />
            </Tooltip>
          )}
          
          {hasPermission('USER_UPDATE') && (
            <Button
              type="link"
              size="small"
              onClick={() => handleStatusChange(record.id, record.status === 1 ? 0 : 1)}
            >
              {record.status === 1 ? '禁用' : '启用'}
            </Button>
          )}
          
          {hasPermission('USER_RESET_PASSWORD') && (
            <Popconfirm
              title="确认重置密码？"
              description="密码将重置为默认密码"
              onConfirm={() => handleResetPassword(record.id)}
            >
              <Button type="link" size="small">
                重置密码
              </Button>
            </Popconfirm>
          )}
          
          {hasPermission('USER_UNLOCK') && record.isLocked && (
            <Tooltip title="解锁用户">
              <Button
                type="link"
                size="small"
                icon={<UnlockOutlined />}
                onClick={() => handleUnlock(record.id)}
              />
            </Tooltip>
          )}
          
          {hasPermission('USER_DELETE') && (
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
    <div className="user-management">
      <Card>
        <div className="page-header">
          <Title level={4}>用户管理</Title>
        </div>
        
        {/* 搜索表单 */}
        <Card size="small" className="search-card">
          <Form form={searchForm} layout="inline">
            <Row gutter={16} style={{ width: '100%' }}>
              <Col>
                <Form.Item name="username" label="用户名">
                  <Input placeholder="请输入用户名" allowClear />
                </Form.Item>
              </Col>
              <Col>
                <Form.Item name="realName" label="真实姓名">
                  <Input placeholder="请输入真实姓名" allowClear />
                </Form.Item>
              </Col>
              <Col>
                <Form.Item name="email" label="邮箱">
                  <Input placeholder="请输入邮箱" allowClear />
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
          {hasPermission('USER_CREATE') && (
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增用户
            </Button>
          )}
        </div>

        {/* 用户表格 */}
        <Table
          columns={columns}
          dataSource={users}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条/共 ${total} 条`
          }}
          onChange={handleTableChange}
          scroll={{ x: 1200 }}
        />
      </Card>

      {/* 用户表单弹窗 */}
      <Modal
        title={editingUser ? '编辑用户' : '新增用户'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
        width={600}
        destroyOnClose
      >
        <Form form={userForm} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="username"
                label="用户名"
                rules={[
                  { required: true, message: '请输入用户名' },
                  { min: 3, max: 50, message: '用户名长度为3-50个字符' }
                ]}
              >
                <Input placeholder="请输入用户名" disabled={!!editingUser} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="realName"
                label="真实姓名"
                rules={[
                  { required: true, message: '请输入真实姓名' },
                  { max: 50, message: '姓名不能超过50个字符' }
                ]}
              >
                <Input placeholder="请输入真实姓名" />
              </Form.Item>
            </Col>
          </Row>

          {!editingUser && (
            <Form.Item
              name="password"
              label="密码"
              rules={[
                { required: true, message: '请输入密码' },
                { min: 6, message: '密码至少6个字符' }
              ]}
            >
              <Input.Password placeholder="请输入密码" />
            </Form.Item>
          )}

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="email"
                label="邮箱"
                rules={[
                  { type: 'email', message: '邮箱格式不正确' },
                  { max: 100, message: '邮箱不能超过100个字符' }
                ]}
              >
                <Input placeholder="请输入邮箱" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="phone"
                label="电话"
                rules={[
                  { max: 20, message: '电话不能超过20个字符' }
                ]}
              >
                <Input placeholder="请输入电话" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="gender" label="性别">
                <Radio.Group>
                  <Radio value={1}>男</Radio>
                  <Radio value={2}>女</Radio>
                </Radio.Group>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="position"
                label="职位"
                rules={[
                  { max: 50, message: '职位不能超过50个字符' }
                ]}
              >
                <Input placeholder="请输入职位" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="employeeId"
            label="员工编号"
            rules={[
              { max: 50, message: '员工编号不能超过50个字符' }
            ]}
          >
            <Input placeholder="请输入员工编号" />
          </Form.Item>

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

      {/* 角色分配弹窗 */}
      {selectedUser && (
        <UserRoleModal
          visible={roleModalVisible}
          user={selectedUser}
          roles={roles}
          onCancel={() => {
            setRoleModalVisible(false)
            setSelectedUser(null)
          }}
          onSuccess={() => {
            setRoleModalVisible(false)
            setSelectedUser(null)
            fetchUsers()
          }}
        />
      )}
    </div>
  )
}

export default UserManagement