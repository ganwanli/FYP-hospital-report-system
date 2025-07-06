import React, { useState, useEffect } from 'react'
import { Modal, Transfer, message } from 'antd'
import { roleAPI } from '@/services'
import type { User, Role } from '@/types'

interface UserRoleModalProps {
  visible: boolean
  user: User
  roles: Role[]
  onCancel: () => void
  onSuccess: () => void
}

interface TransferRole {
  key: string
  title: string
  description?: string
}

const UserRoleModal: React.FC<UserRoleModalProps> = ({
  visible,
  user,
  roles,
  onCancel,
  onSuccess
}) => {
  const [targetKeys, setTargetKeys] = useState<string[]>([])
  const [loading, setLoading] = useState(false)

  const dataSource: TransferRole[] = roles.map(role => ({
    key: role.id.toString(),
    title: role.roleName,
    description: role.description
  }))

  useEffect(() => {
    if (visible && user) {
      fetchUserRoles()
    }
  }, [visible, user])

  const fetchUserRoles = async () => {
    try {
      const response = await roleAPI.getUserRoles(user.id)
      if (response.code === 200) {
        const roleIds = response.data.map((role: Role) => role.id.toString())
        setTargetKeys(roleIds)
      }
    } catch (error) {
      message.error('获取用户角色失败')
    }
  }

  const handleOk = async () => {
    setLoading(true)
    try {
      const roleIds = targetKeys.map(key => parseInt(key))
      const response = await roleAPI.assignRoles(user.id, { roleIds })
      
      if (response.code === 200) {
        message.success('角色分配成功')
        onSuccess()
      } else {
        message.error(response.message || '角色分配失败')
      }
    } catch (error) {
      message.error('角色分配失败')
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (nextTargetKeys: string[]) => {
    setTargetKeys(nextTargetKeys)
  }

  return (
    <Modal
      title={`分配角色 - ${user.realName}`}
      open={visible}
      onOk={handleOk}
      onCancel={onCancel}
      width={600}
      confirmLoading={loading}
      destroyOnClose
    >
      <Transfer
        dataSource={dataSource}
        targetKeys={targetKeys}
        onChange={handleChange}
        render={item => item.title}
        titles={['可分配角色', '已分配角色']}
        oneWay={false}
        pagination
        showSearch
        filterOption={(inputValue, option) =>
          option.title.toLowerCase().includes(inputValue.toLowerCase()) ||
          (option.description && option.description.toLowerCase().includes(inputValue.toLowerCase()))
        }
        listStyle={{
          width: 250,
          height: 300
        }}
      />
    </Modal>
  )
}

export default UserRoleModal