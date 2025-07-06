import React, { useState, useEffect } from 'react'
import {
  Table,
  Tree,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  InputNumber,
  Select,
  Radio,
  Switch,
  message,
  Popconfirm,
  Card,
  Row,
  Col,
  Typography,
  Tooltip,
  Divider
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  FolderOutlined,
  FileOutlined,
  SearchOutlined,
  ReloadOutlined,
  AppstoreOutlined
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { permissionAPI } from '@/services'
import { usePermission } from '@/hooks'
import type { Permission } from '@/types'
import './index.css'

const { Option } = Select
const { Title } = Typography
const { DirectoryTree } = Tree

interface PermissionFormData {
  parentId: number
  permissionName: string
  permissionCode: string
  permissionType: string
  menuUrl?: string
  menuIcon?: string
  component?: string
  redirect?: string
  sortOrder: number
  isVisible: boolean
  isExternal: boolean
  isCache: boolean
  remarks?: string
}

interface PermissionTreeNode {
  key: string
  title: React.ReactNode
  icon?: React.ReactNode
  children?: PermissionTreeNode[]
  data: Permission
}

const PermissionManagement: React.FC = () => {
  const [permissions, setPermissions] = useState<Permission[]>([])
  const [permissionTree, setPermissionTree] = useState<PermissionTreeNode[]>([])
  const [selectedPermissions, setSelectedPermissions] = useState<Permission[]>([])
  const [loading, setLoading] = useState(false)
  const [treeLoading, setTreeLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingPermission, setEditingPermission] = useState<Permission | null>(null)
  const [selectedTreeNode, setSelectedTreeNode] = useState<string | null>(null)
  const [expandedKeys, setExpandedKeys] = useState<string[]>([])
  const [permissionForm] = Form.useForm()
  const { hasPermission } = usePermission()

  const fetchPermissions = async () => {
    setLoading(true)
    try {
      const response = await permissionAPI.getPermissionList()
      if (response.code === 200) {
        setPermissions(response.data)
      }
    } catch (error) {
      message.error('获取权限列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchPermissionTree = async () => {
    setTreeLoading(true)
    try {
      const response = await permissionAPI.getPermissionTree()
      if (response.code === 200) {
        const treeData = convertToTreeData(response.data)
        setPermissionTree(treeData)
        
        // 默认展开第一层
        const firstLevelKeys = treeData.map(item => item.key)
        setExpandedKeys(firstLevelKeys)
      }
    } catch (error) {
      message.error('获取权限树失败')
    } finally {
      setTreeLoading(false)
    }
  }

  useEffect(() => {
    fetchPermissions()
    fetchPermissionTree()
  }, [])

  const convertToTreeData = (data: any[]): PermissionTreeNode[] => {
    return data.map(item => ({
      key: item.id.toString(),
      title: (
        <div className="tree-node-title">
          <span>{item.permissionName}</span>
          <Tag size="small" color={getPermissionTypeColor(item.permissionType)}>
            {getPermissionTypeText(item.permissionType)}
          </Tag>
        </div>
      ),
      icon: getPermissionIcon(item.permissionType),
      children: item.children ? convertToTreeData(item.children) : undefined,
      data: item
    }))
  }

  const getPermissionTypeColor = (type: string) => {
    const colorMap: Record<string, string> = {
      'DIRECTORY': 'blue',
      'MENU': 'green',
      'BUTTON': 'orange'
    }
    return colorMap[type] || 'default'
  }

  const getPermissionTypeText = (type: string) => {
    const textMap: Record<string, string> = {
      'DIRECTORY': '目录',
      'MENU': '菜单',
      'BUTTON': '按钮'
    }
    return textMap[type] || type
  }

  const getPermissionIcon = (type: string) => {
    const iconMap: Record<string, React.ReactNode> = {
      'DIRECTORY': <FolderOutlined />,
      'MENU': <AppstoreOutlined />,
      'BUTTON': <FileOutlined />
    }
    return iconMap[type] || <FileOutlined />
  }

  const handleTreeSelect = (selectedKeys: string[], info: any) => {
    if (selectedKeys.length > 0) {
      const nodeKey = selectedKeys[0]
      setSelectedTreeNode(nodeKey)
      
      // 获取选中节点及其子节点的所有权限
      const getNodeAndChildren = (nodes: PermissionTreeNode[], key: string): Permission[] => {
        for (const node of nodes) {
          if (node.key === key) {
            const result = [node.data]
            if (node.children) {
              node.children.forEach(child => {
                result.push(...getNodeAndChildren([child], child.key))
              })
            }
            return result
          }
          if (node.children) {
            const found = getNodeAndChildren(node.children, key)
            if (found.length > 0) return found
          }
        }
        return []
      }
      
      const nodePermissions = getNodeAndChildren(permissionTree, nodeKey)
      setSelectedPermissions(nodePermissions)
    } else {
      setSelectedTreeNode(null)
      setSelectedPermissions([])
    }
  }

  const handleAdd = (parentId?: number) => {
    setEditingPermission(null)
    permissionForm.resetFields()
    permissionForm.setFieldsValue({
      parentId: parentId || 0,
      permissionType: 'MENU',
      sortOrder: 0,
      isVisible: true,
      isExternal: false,
      isCache: true
    })
    setModalVisible(true)
  }

  const handleEdit = (permission: Permission) => {
    setEditingPermission(permission)
    permissionForm.setFieldsValue(permission)
    setModalVisible(true)
  }

  const handleDelete = async (permissionId: number) => {
    try {
      const response = await permissionAPI.deletePermission(permissionId)
      if (response.code === 200) {
        message.success('删除成功')
        fetchPermissions()
        fetchPermissionTree()
      } else {
        message.error(response.message)
      }
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleStatusChange = async (permissionId: number, status: number) => {
    try {
      const response = await permissionAPI.updatePermissionStatus(permissionId, status)
      if (response.code === 200) {
        message.success('状态更新成功')
        fetchPermissions()
        fetchPermissionTree()
      } else {
        message.error(response.message)
      }
    } catch (error) {
      message.error('状态更新失败')
    }
  }

  const handleModalOk = async () => {
    try {
      const values = await permissionForm.validateFields()
      
      if (editingPermission) {
        const response = await permissionAPI.updatePermission(editingPermission.id, values)
        if (response.code === 200) {
          message.success('更新成功')
          setModalVisible(false)
          fetchPermissions()
          fetchPermissionTree()
        } else {
          message.error(response.message)
        }
      } else {
        const response = await permissionAPI.createPermission(values)
        if (response.code === 200) {
          message.success('创建成功')
          setModalVisible(false)
          fetchPermissions()
          fetchPermissionTree()
        } else {
          message.error(response.message)
        }
      }
    } catch (error) {
      console.error('表单验证失败:', error)
    }
  }

  const columns: ColumnsType<Permission> = [
    {
      title: '权限名称',
      dataIndex: 'permissionName',
      width: 200,
      render: (text, record) => (
        <div className="permission-name">
          {getPermissionIcon(record.permissionType)}
          <span style={{ marginLeft: 8 }}>{text}</span>
        </div>
      )
    },
    {
      title: '权限编码',
      dataIndex: 'permissionCode',
      width: 200,
      render: (text) => <code className="permission-code">{text}</code>
    },
    {
      title: '类型',
      dataIndex: 'permissionType',
      width: 100,
      render: (type) => (
        <Tag color={getPermissionTypeColor(type)}>
          {getPermissionTypeText(type)}
        </Tag>
      )
    },
    {
      title: '路由路径',
      dataIndex: 'menuUrl',
      width: 180,
      ellipsis: true,
      render: (url) => url || '-'
    },
    {
      title: '组件',
      dataIndex: 'component',
      width: 150,
      ellipsis: true,
      render: (component) => component || '-'
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      width: 80,
      align: 'center'
    },
    {
      title: '可见',
      dataIndex: 'isVisible',
      width: 80,
      render: (visible) => (
        <Tag color={visible ? 'success' : 'default'}>
          {visible ? '是' : '否'}
        </Tag>
      )
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
      title: '操作',
      key: 'action',
      width: 180,
      fixed: 'right',
      render: (_, record) => (
        <Space wrap>
          {hasPermission('PERMISSION_CREATE') && (
            <Tooltip title="添加子权限">
              <Button
                type="link"
                size="small"
                icon={<PlusOutlined />}
                onClick={() => handleAdd(record.id)}
              />
            </Tooltip>
          )}
          
          {hasPermission('PERMISSION_UPDATE') && (
            <Tooltip title="编辑">
              <Button
                type="link"
                size="small"
                icon={<EditOutlined />}
                onClick={() => handleEdit(record)}
              />
            </Tooltip>
          )}
          
          {hasPermission('PERMISSION_UPDATE') && (
            <Button
              type="link"
              size="small"
              onClick={() => handleStatusChange(record.id, record.status === 1 ? 0 : 1)}
            >
              {record.status === 1 ? '禁用' : '启用'}
            </Button>
          )}
          
          {hasPermission('PERMISSION_DELETE') && (
            <Popconfirm
              title="确认删除？"
              description="删除后无法恢复，且会影响相关角色权限"
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
    <div className="permission-management">
      <Card>
        <div className="page-header">
          <Title level={4}>权限管理</Title>
        </div>
        
        <Row gutter={16}>
          {/* 权限树 */}
          <Col span={8}>
            <Card title="权限树" size="small" className="tree-card">
              <div className="tree-toolbar">
                {hasPermission('PERMISSION_CREATE') && (
                  <Button
                    type="primary"
                    size="small"
                    icon={<PlusOutlined />}
                    onClick={() => handleAdd()}
                  >
                    添加根权限
                  </Button>
                )}
              </div>
              
              <DirectoryTree
                loading={treeLoading}
                treeData={permissionTree}
                onSelect={handleTreeSelect}
                expandedKeys={expandedKeys}
                onExpand={setExpandedKeys}
                showIcon
                className="permission-tree"
              />
            </Card>
          </Col>
          
          {/* 权限列表 */}
          <Col span={16}>
            <Card 
              title={
                selectedTreeNode 
                  ? `权限详情 (${selectedPermissions.length}项)` 
                  : '所有权限'
              }
              size="small"
              extra={
                <Space>
                  {hasPermission('PERMISSION_CREATE') && (
                    <Button
                      type="primary"
                      size="small"
                      icon={<PlusOutlined />}
                      onClick={() => handleAdd()}
                    >
                      新增权限
                    </Button>
                  )}
                  <Button
                    size="small"
                    icon={<ReloadOutlined />}
                    onClick={() => {
                      fetchPermissions()
                      fetchPermissionTree()
                    }}
                  >
                    刷新
                  </Button>
                </Space>
              }
            >
              <Table
                columns={columns}
                dataSource={selectedTreeNode ? selectedPermissions : permissions}
                rowKey="id"
                loading={loading}
                pagination={{
                  showSizeChanger: true,
                  showQuickJumper: true,
                  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条/共 ${total} 条`
                }}
                scroll={{ x: 1000 }}
                size="small"
              />
            </Card>
          </Col>
        </Row>
      </Card>

      {/* 权限表单弹窗 */}
      <Modal
        title={editingPermission ? '编辑权限' : '新增权限'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
        width={700}
        destroyOnClose
      >
        <Form form={permissionForm} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="permissionName"
                label="权限名称"
                rules={[
                  { required: true, message: '请输入权限名称' },
                  { max: 50, message: '权限名称不能超过50个字符' }
                ]}
              >
                <Input placeholder="请输入权限名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="permissionCode"
                label="权限编码"
                rules={[
                  { required: true, message: '请输入权限编码' },
                  { max: 100, message: '权限编码不能超过100个字符' },
                  { pattern: /^[A-Z_:]+$/, message: '权限编码只能包含大写字母、下划线和冒号' }
                ]}
              >
                <Input placeholder="如：USER_MANAGE" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="permissionType"
                label="权限类型"
                rules={[{ required: true, message: '请选择权限类型' }]}
              >
                <Select placeholder="请选择权限类型">
                  <Option value="DIRECTORY">目录</Option>
                  <Option value="MENU">菜单</Option>
                  <Option value="BUTTON">按钮</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="parentId"
                label="父级权限"
                rules={[{ required: true, message: '请选择父级权限' }]}
              >
                <InputNumber
                  placeholder="0为根权限"
                  min={0}
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item noStyle dependencies={['permissionType']}>
            {({ getFieldValue }) => {
              const permissionType = getFieldValue('permissionType')
              
              if (permissionType === 'BUTTON') {
                return null
              }
              
              return (
                <>
                  <Row gutter={16}>
                    <Col span={12}>
                      <Form.Item
                        name="menuUrl"
                        label="路由路径"
                        rules={[
                          { max: 255, message: '路由路径不能超过255个字符' }
                        ]}
                      >
                        <Input placeholder="如：/system/user" />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item
                        name="component"
                        label="组件路径"
                        rules={[
                          { max: 255, message: '组件路径不能超过255个字符' }
                        ]}
                      >
                        <Input placeholder="如：@/pages/system/User" />
                      </Form.Item>
                    </Col>
                  </Row>
                  
                  <Row gutter={16}>
                    <Col span={12}>
                      <Form.Item
                        name="menuIcon"
                        label="菜单图标"
                        rules={[
                          { max: 50, message: '图标不能超过50个字符' }
                        ]}
                      >
                        <Input placeholder="如：UserOutlined" />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item
                        name="redirect"
                        label="重定向"
                        rules={[
                          { max: 255, message: '重定向不能超过255个字符' }
                        ]}
                      >
                        <Input placeholder="重定向路径" />
                      </Form.Item>
                    </Col>
                  </Row>
                </>
              )
            }}
          </Form.Item>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="sortOrder"
                label="排序"
                rules={[{ required: true, message: '请输入排序号' }]}
              >
                <InputNumber
                  placeholder="排序号"
                  min={0}
                  max={9999}
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="isVisible" label="是否可见" valuePropName="checked">
                <Switch />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="isExternal" label="外部链接" valuePropName="checked">
                <Switch />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="isCache" label="是否缓存" valuePropName="checked">
            <Switch />
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
    </div>
  )
}

export default PermissionManagement