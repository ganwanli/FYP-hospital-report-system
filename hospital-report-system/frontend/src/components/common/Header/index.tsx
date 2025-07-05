import React from 'react'
import { Layout, Menu, Avatar, Dropdown, Space, Button, Tooltip } from 'antd'
import { 
  UserOutlined, 
  SettingOutlined, 
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  BellOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined
} from '@ant-design/icons'
import { useAuthStore } from '@/stores/authStore'
import { useAppStore } from '@/stores/appStore'
import './index.css'

const { Header } = Layout

interface HeaderComponentProps {
  className?: string
}

const HeaderComponent: React.FC<HeaderComponentProps> = ({ className }) => {
  const { user, logout } = useAuthStore()
  const { sidebarCollapsed, toggleSidebar } = useAppStore()
  const [isFullscreen, setIsFullscreen] = React.useState(false)

  // 处理全屏
  const handleFullscreen = () => {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen()
      setIsFullscreen(true)
    } else {
      document.exitFullscreen()
      setIsFullscreen(false)
    }
  }

  // 监听全屏状态变化
  React.useEffect(() => {
    const handleFullscreenChange = () => {
      setIsFullscreen(!!document.fullscreenElement)
    }

    document.addEventListener('fullscreenchange', handleFullscreenChange)
    return () => {
      document.removeEventListener('fullscreenchange', handleFullscreenChange)
    }
  }, [])

  // 用户下拉菜单
  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人中心',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '个人设置',
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: logout,
    },
  ]

  return (
    <Header className={`layout-header ${className || ''}`}>
      <div className="header-left">
        <Button
          type="text"
          icon={sidebarCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          onClick={toggleSidebar}
          className="collapse-button"
        />
        
        <div className="header-title">
          {import.meta.env.VITE_APP_TITLE}
        </div>
      </div>

      <div className="header-right">
        <Space size="middle">
          {/* 全屏按钮 */}
          <Tooltip title={isFullscreen ? '退出全屏' : '全屏'}>
            <Button
              type="text"
              icon={isFullscreen ? <FullscreenExitOutlined /> : <FullscreenOutlined />}
              onClick={handleFullscreen}
            />
          </Tooltip>

          {/* 通知按钮 */}
          <Tooltip title="通知">
            <Button
              type="text"
              icon={<BellOutlined />}
            />
          </Tooltip>

          {/* 用户信息 */}
          <Dropdown
            menu={{ items: userMenuItems }}
            placement="bottomRight"
            arrow
          >
            <div className="user-info">
              <Avatar
                size="small"
                src={user?.avatar}
                icon={<UserOutlined />}
              />
              <span className="username">{user?.realName || user?.username}</span>
            </div>
          </Dropdown>
        </Space>
      </div>
    </Header>
  )
}

export default HeaderComponent