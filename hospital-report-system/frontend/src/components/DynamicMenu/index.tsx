import React, { useState, useEffect } from 'react'
import { Menu } from 'antd'
import { useLocation, useNavigate } from 'react-router-dom'
import * as Icons from '@ant-design/icons'
import { permissionAPI } from '@/services'
import { usePermission } from '@/hooks'

interface MenuItem {
  key: string
  icon?: React.ReactNode
  label: string
  children?: MenuItem[]
  path?: string
}

interface MenuPermission {
  id: number
  parentId: number
  permissionName: string
  permissionCode: string
  permissionType: string
  menuUrl?: string
  menuIcon?: string
  sortOrder: number
  isVisible: boolean
  children?: MenuPermission[]
}

const DynamicMenu: React.FC = () => {
  const [menuItems, setMenuItems] = useState<MenuItem[]>([])
  const [openKeys, setOpenKeys] = useState<string[]>([])
  const [selectedKeys, setSelectedKeys] = useState<string[]>([])
  const location = useLocation()
  const navigate = useNavigate()
  const { hasPermission } = usePermission()

  useEffect(() => {
    fetchUserMenus()
  }, [])

  useEffect(() => {
    // 根据当前路径设置选中和展开的菜单项
    const path = location.pathname
    const selectedKey = findMenuKeyByPath(menuItems, path)
    if (selectedKey) {
      setSelectedKeys([selectedKey])
      // 展开父级菜单
      const parentKeys = findParentKeys(menuItems, selectedKey)
      setOpenKeys(parentKeys)
    }
  }, [location.pathname, menuItems])

  const fetchUserMenus = async () => {
    try {
      const response = await permissionAPI.getMenuPermissions()
      if (response.code === 200) {
        const menus = buildMenuTree(response.data)
        const filteredMenus = filterMenusByPermission(menus)
        setMenuItems(filteredMenus)
      }
    } catch (error) {
      console.error('获取菜单失败:', error)
    }
  }

  const buildMenuTree = (permissions: MenuPermission[]): MenuItem[] => {
    const menuMap = new Map<number, MenuPermission>()
    const rootMenus: MenuPermission[] = []

    // 创建映射
    permissions.forEach(permission => {
      if (permission.isVisible && permission.permissionType !== 'BUTTON') {
        menuMap.set(permission.id, { ...permission, children: [] })
      }
    })

    // 构建树结构
    permissions.forEach(permission => {
      if (permission.isVisible && permission.permissionType !== 'BUTTON') {
        const menu = menuMap.get(permission.id)
        if (menu) {
          if (permission.parentId === 0) {
            rootMenus.push(menu)
          } else {
            const parent = menuMap.get(permission.parentId)
            if (parent) {
              parent.children = parent.children || []
              parent.children.push(menu)
            }
          }
        }
      }
    })

    return convertToMenuItems(rootMenus)
  }

  const convertToMenuItems = (menus: MenuPermission[]): MenuItem[] => {
    return menus
      .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
      .map(menu => ({
        key: menu.id.toString(),
        icon: getMenuIcon(menu.menuIcon),
        label: menu.permissionName,
        path: menu.menuUrl,
        children: menu.children && menu.children.length > 0
          ? convertToMenuItems(menu.children)
          : undefined
      }))
  }

  const filterMenusByPermission = (menus: MenuItem[]): MenuItem[] => {
    return menus.filter(menu => {
      // 如果有子菜单，递归过滤
      if (menu.children && menu.children.length > 0) {
        menu.children = filterMenusByPermission(menu.children)
        // 如果过滤后还有子菜单，则保留该菜单项
        return menu.children.length > 0
      }
      
      // 叶子菜单项，检查权限
      return menu.path ? hasPermission(`MENU:${menu.path}`) : true
    })
  }

  const getMenuIcon = (iconName?: string): React.ReactNode => {
    if (!iconName) return null
    
    // 获取 Ant Design 图标组件
    const IconComponent = (Icons as any)[iconName]
    return IconComponent ? React.createElement(IconComponent) : null
  }

  const findMenuKeyByPath = (menus: MenuItem[], path: string): string | null => {
    for (const menu of menus) {
      if (menu.path === path) {
        return menu.key
      }
      if (menu.children) {
        const found = findMenuKeyByPath(menu.children, path)
        if (found) return found
      }
    }
    return null
  }

  const findParentKeys = (menus: MenuItem[], targetKey: string, parentKeys: string[] = []): string[] => {
    for (const menu of menus) {
      if (menu.key === targetKey) {
        return parentKeys
      }
      if (menu.children) {
        const found = findParentKeys(menu.children, targetKey, [...parentKeys, menu.key])
        if (found.length > parentKeys.length) {
          return found
        }
      }
    }
    return []
  }

  const handleMenuClick = ({ key, keyPath }: { key: string; keyPath: string[] }) => {
    const menu = findMenuByKey(menuItems, key)
    if (menu?.path) {
      navigate(menu.path)
    }
  }

  const findMenuByKey = (menus: MenuItem[], key: string): MenuItem | null => {
    for (const menu of menus) {
      if (menu.key === key) {
        return menu
      }
      if (menu.children) {
        const found = findMenuByKey(menu.children, key)
        if (found) return found
      }
    }
    return null
  }

  const handleOpenChange = (keys: string[]) => {
    setOpenKeys(keys)
  }

  return (
    <Menu
      mode="inline"
      theme="light"
      items={menuItems}
      selectedKeys={selectedKeys}
      openKeys={openKeys}
      onClick={handleMenuClick}
      onOpenChange={handleOpenChange}
      style={{ height: '100%', borderRight: 0 }}
    />
  )
}

export default DynamicMenu