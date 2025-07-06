import React, { useState, useEffect } from 'react'
import { Modal, Tree, message, Spin } from 'antd'
import { roleAPI, permissionAPI } from '@/services'
import type { Role } from '@/types'

interface RolePermissionModalProps {
  visible: boolean
  role: Role
  onCancel: () => void
  onSuccess: () => void
}

interface PermissionTreeNode {
  key: string
  title: string
  children?: PermissionTreeNode[]
}

const RolePermissionModal: React.FC<RolePermissionModalProps> = ({
  visible,
  role,
  onCancel,
  onSuccess
}) => {
  const [checkedKeys, setCheckedKeys] = useState<string[]>([])
  const [permissionTree, setPermissionTree] = useState<PermissionTreeNode[]>([])
  const [loading, setLoading] = useState(false)
  const [treeLoading, setTreeLoading] = useState(false)

  useEffect(() => {
    if (visible && role) {
      fetchPermissionTree()
      fetchRolePermissions()
    }
  }, [visible, role])

  const fetchPermissionTree = async () => {
    setTreeLoading(true)
    try {
      const response = await permissionAPI.getPermissionTree()
      if (response.code === 200) {
        const treeData = convertToTreeData(response.data)
        setPermissionTree(treeData)
      }
    } catch (error) {
      message.error('获取权限树失败')
    } finally {
      setTreeLoading(false)
    }
  }

  const fetchRolePermissions = async () => {
    try {
      const response = await roleAPI.getRolePermissions(role.id)
      if (response.code === 200) {
        const permissionIds = response.data.map((id: number) => id.toString())
        setCheckedKeys(permissionIds)
      }
    } catch (error) {
      message.error('获取角色权限失败')
    }
  }

  const convertToTreeData = (data: any[]): PermissionTreeNode[] => {
    return data.map(item => ({
      key: item.id.toString(),
      title: item.permissionName,
      children: item.children ? convertToTreeData(item.children) : undefined
    }))
  }

  const handleOk = async () => {
    setLoading(true)
    try {
      const permissionIds = checkedKeys.map(key => parseInt(key))
      const response = await roleAPI.assignPermissions(role.id, { permissionIds })
      
      if (response.code === 200) {
        message.success('权限分配成功')
        onSuccess()
      } else {
        message.error(response.message || '权限分配失败')
      }
    } catch (error) {
      message.error('权限分配失败')
    } finally {
      setLoading(false)
    }
  }

  const handleCheck = (checkedKeysValue: any) => {
    setCheckedKeys(checkedKeysValue)
  }

  return (
    <Modal
      title={`分配权限 - ${role.roleName}`}
      open={visible}
      onOk={handleOk}
      onCancel={onCancel}
      width={600}
      confirmLoading={loading}
      destroyOnClose
    >
      <div style={{ maxHeight: 400, overflow: 'auto' }}>
        {treeLoading ? (
          <div style={{ textAlign: 'center', padding: 50 }}>
            <Spin size="large" />
          </div>
        ) : (
          <Tree
            checkable
            checkedKeys={checkedKeys}
            onCheck={handleCheck}
            treeData={permissionTree}
            defaultExpandAll
          />
        )}
      </div>
    </Modal>
  )
}

export default RolePermissionModal