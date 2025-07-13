import React, { useState, useEffect } from 'react'
import { getCurrentLanguage, setLanguage, getText, toggleLanguage, getLanguageInfo, Language } from './i18n'

// 现代化 Dashboard 页面（支持语言切换）
const Dashboard: React.FC = () => {
  const [currentTime, setCurrentTime] = useState(new Date())
  const [userInfo, setUserInfo] = useState<any>(null)
  const [currentLang, setCurrentLang] = useState<Language>(getCurrentLanguage())
  
  // 更新时间
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date())
    }, 1000)
    return () => clearInterval(timer)
  }, [])
  
  // 获取用户信息
  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search)
    const user = urlParams.get('user') || 'admin'
    const role = urlParams.get('role') || getText('admin', currentLang)
    const lang = urlParams.get('lang') as Language

    // 如果URL中有语言参数，切换到对应语言
    if (lang && (lang === 'zh' || lang === 'en') && lang !== currentLang) {
      setLanguage(lang)
      setCurrentLang(lang)
    }

    setUserInfo({ username: user, role })
  }, [currentLang])
  
  const handleLanguageToggle = () => {
    const newLang = toggleLanguage()
    setCurrentLang(newLang)
    // 强制重新渲染页面
    window.location.reload()
  }

  const logout = () => {
    if (confirm(getText('confirmLogout', currentLang))) {
      window.location.href = '/login'
    }
  }
  
  const menuItems = [
    {
      title: getText('dataSourceManagement', currentLang),
      icon: '🗄️',
      description: getText('dataSourceDesc', currentLang),
      color: '#1890ff'
    },
    {
      title: getText('reportConfiguration', currentLang),
      icon: '📊',
      description: getText('reportConfigDesc', currentLang),
      color: '#52c41a'
    },
    {
      title: getText('userManagement', currentLang),
      icon: '👥',
      description: getText('userMgmtDesc', currentLang),
      color: '#fa8c16'
    },
    {
      title: getText('systemMonitoring', currentLang),
      icon: '📈',
      description: getText('systemMonitorDesc', currentLang),
      color: '#722ed1'
    },
    {
      title: getText('reportViewing', currentLang),
      icon: '📋',
      description: getText('reportViewDesc', currentLang),
      color: '#13c2c2'
    },
    {
      title: getText('systemSettings', currentLang),
      icon: '⚙️',
      description: getText('systemSettingsDesc', currentLang),
      color: '#eb2f96'
    }
  ]

  const statsData = [
    { title: getText('todayReports', currentLang), value: '156', unit: currentLang === 'zh' ? '份' : '', color: '#1890ff' },
    { title: getText('onlineUsers', currentLang), value: '23', unit: currentLang === 'zh' ? '人' : '', color: '#52c41a' },
    { title: getText('dataSources', currentLang), value: '8', unit: currentLang === 'zh' ? '个' : '', color: '#fa8c16' },
    { title: getText('systemUptime', currentLang), value: '99.9', unit: '%', color: '#722ed1' }
  ]
  
  return React.createElement('div', {
    style: {
      width: '100vw',
      height: '100vh',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      position: 'relative',
      overflow: 'hidden',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif'
    }
  }, [
    // 动态背景效果
    React.createElement('div', {
      key: 'bg-effects',
      style: {
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        background: `
          radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
          radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%),
          radial-gradient(circle at 40% 40%, rgba(120, 219, 255, 0.3) 0%, transparent 50%)
        `,
        animation: 'float 6s ease-in-out infinite'
      }
    }),
    
    // 顶部导航栏
    React.createElement('div', {
      key: 'header',
      style: {
        position: 'relative',
        zIndex: 1,
        background: 'rgba(255,255,255,0.95)',
        backdropFilter: 'blur(10px)',
        padding: '16px 24px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
      }
    }, [
      React.createElement('div', {
        key: 'logo',
        style: { display: 'flex', alignItems: 'center' }
      }, [
        React.createElement('div', {
          key: 'icon',
          style: { fontSize: '32px', marginRight: '12px' }
        }, '🏥'),
        React.createElement('div', { key: 'text' }, [
          React.createElement('h1', {
            key: 'title',
            style: { 
              margin: 0, 
              color: '#1890ff', 
              fontSize: '24px',
              fontWeight: 'bold'
            }
          }, getText('systemTitle', currentLang)),
          React.createElement('div', {
            key: 'subtitle',
            style: {
              color: '#666',
              fontSize: '14px',
              marginTop: '2px'
            }
          }, getText('systemSubtitle', currentLang))
        ])
      ]),
      
      React.createElement('div', {
        key: 'user-info',
        style: { display: 'flex', alignItems: 'center', gap: '16px' }
      }, [
        React.createElement('div', {
          key: 'time',
          style: { 
            color: '#666', 
            fontSize: '14px',
            textAlign: 'right'
          }
        }, [
          React.createElement('div', { key: 'date' }, currentTime.toLocaleDateString()),
          React.createElement('div', { key: 'time' }, currentTime.toLocaleTimeString())
        ]),

        // 语言切换按钮
        React.createElement('button', {
          key: 'language-switch',
          style: {
            background: '#f0f9ff',
            border: '1px solid #91d5ff',
            borderRadius: '20px',
            padding: '6px 12px',
            color: '#1890ff',
            fontSize: '14px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            transition: 'all 0.3s ease'
          },
          onClick: handleLanguageToggle,
          onMouseEnter: (e) => {
            e.currentTarget.style.background = '#e6f7ff'
            e.currentTarget.style.transform = 'translateY(-1px)'
          },
          onMouseLeave: (e) => {
            e.currentTarget.style.background = '#f0f9ff'
            e.currentTarget.style.transform = 'translateY(0)'
          }
        }, [
          getLanguageInfo(currentLang === 'zh' ? 'en' : 'zh').flag,
          ' ',
          getLanguageInfo(currentLang === 'zh' ? 'en' : 'zh').name
        ]),

        React.createElement('div', {
          key: 'user',
          style: {
            background: '#f0f9ff',
            padding: '8px 16px',
            borderRadius: '20px',
            border: '1px solid #91d5ff',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }
        }, [
          React.createElement('div', {
            key: 'avatar',
            style: {
              width: '32px',
              height: '32px',
              background: '#1890ff',
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'white',
              fontSize: '14px',
              fontWeight: 'bold'
            }
          }, userInfo?.username?.charAt(0)?.toUpperCase() || 'A'),
          React.createElement('div', { key: 'info' }, [
            React.createElement('div', {
              key: 'name',
              style: { fontSize: '14px', fontWeight: 'bold', color: '#1890ff' }
            }, userInfo?.username || 'admin'),
            React.createElement('div', {
              key: 'role',
              style: { fontSize: '12px', color: '#666' }
            }, userInfo?.role || getText('admin', currentLang))
          ])
        ]),
        
        React.createElement('button', {
          key: 'logout',
          style: {
            background: '#ff4d4f',
            color: 'white',
            border: 'none',
            padding: '8px 16px',
            borderRadius: '6px',
            cursor: 'pointer',
            fontSize: '14px'
          },
          onClick: logout
        }, getText('logout', currentLang))
      ])
    ]),
    
    // 主要内容区域
    React.createElement('div', {
      key: 'main',
      style: {
        position: 'relative',
        zIndex: 1,
        padding: '24px',
        maxWidth: '1200px',
        margin: '0 auto',
        height: 'calc(100vh - 80px)',
        overflow: 'auto'
      }
    }, [
      // 欢迎信息
      React.createElement('div', {
        key: 'welcome',
        style: {
          background: 'rgba(255,255,255,0.95)',
          borderRadius: '16px',
          padding: '24px',
          marginBottom: '24px',
          boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
          textAlign: 'center'
        }
      }, [
        React.createElement('h2', {
          key: 'welcome-title',
          style: { 
            color: '#1890ff', 
            marginBottom: '8px',
            fontSize: '28px'
          }
        }, `${getText('welcome', currentLang)}，${userInfo?.username || 'admin'}！`),
        React.createElement('p', {
          key: 'welcome-desc',
          style: {
            color: '#666',
            fontSize: '16px',
            margin: 0
          }
        }, `${getText('yourRole', currentLang)}：${userInfo?.role || getText('admin', currentLang)} | ${getText('today', currentLang)} ${currentTime.toLocaleDateString()}`)
      ]),
      
      // 统计数据
      React.createElement('div', {
        key: 'stats',
        style: {
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
          gap: '16px',
          marginBottom: '24px'
        }
      }, statsData.map((stat, index) => 
        React.createElement('div', {
          key: `stat-${index}`,
          style: {
            background: 'rgba(255,255,255,0.95)',
            borderRadius: '12px',
            padding: '20px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
            textAlign: 'center',
            border: `2px solid ${stat.color}20`
          }
        }, [
          React.createElement('div', {
            key: 'value',
            style: {
              fontSize: '32px',
              fontWeight: 'bold',
              color: stat.color,
              marginBottom: '8px'
            }
          }, `${stat.value}${stat.unit}`),
          React.createElement('div', {
            key: 'title',
            style: {
              fontSize: '16px',
              color: '#666',
              fontWeight: '500'
            }
          }, stat.title)
        ])
      )),
      
      // 功能菜单
      React.createElement('div', {
        key: 'menu-title',
        style: {
          color: 'white',
          fontSize: '20px',
          fontWeight: 'bold',
          marginBottom: '16px',
          textAlign: 'center'
        }
      }, getText('systemFunctions', currentLang)),
      
      React.createElement('div', {
        key: 'menu',
        style: {
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
          gap: '16px'
        }
      }, menuItems.map((item, index) => 
        React.createElement('div', {
          key: `menu-${index}`,
          style: {
            background: 'rgba(255,255,255,0.95)',
            borderRadius: '12px',
            padding: '20px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
            cursor: 'pointer',
            transition: 'transform 0.2s ease',
            border: `2px solid ${item.color}20`
          },
          onClick: () => {
            // 如果是数据源管理，跳转到专门的页面
            if (item.title === getText('dataSourceManagement', currentLang)) {
              window.location.href = '/data-source'
            } else {
              alert(`${getText('enterFunction', currentLang)}：${item.title}\n\n${item.description}\n\n${getText('functionDevelopment', currentLang)}`)
            }
          },
          onMouseEnter: (e) => {
            e.currentTarget.style.transform = 'translateY(-4px)'
          },
          onMouseLeave: (e) => {
            e.currentTarget.style.transform = 'translateY(0)'
          }
        }, [
          React.createElement('div', {
            key: 'icon',
            style: {
              fontSize: '32px',
              marginBottom: '12px'
            }
          }, item.icon),
          React.createElement('h3', {
            key: 'title',
            style: {
              color: item.color,
              marginBottom: '8px',
              fontSize: '18px'
            }
          }, item.title),
          React.createElement('p', {
            key: 'desc',
            style: {
              color: '#666',
              fontSize: '14px',
              margin: 0,
              lineHeight: '1.5'
            }
          }, item.description)
        ])
      ))
    ]),
    
    // CSS 动画
    React.createElement('style', { key: 'styles' }, `
      @keyframes float {
        0%, 100% { transform: translateY(0px) rotate(0deg); }
        33% { transform: translateY(-20px) rotate(1deg); }
        66% { transform: translateY(-10px) rotate(-1deg); }
      }
    `)
  ])
}

export default Dashboard
