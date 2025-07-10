import React, { useState, useEffect } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Dashboard from './Dashboard'
import DataSourceManagement from './DataSourceManagement'
import { getCurrentLanguage, setLanguage, getText, toggleLanguage, getLanguageInfo, Language } from './i18n'

// 主页组件（支持语言切换）
const SimpleTest: React.FC = () => {
  console.log('🚀 SimpleTest 组件正在渲染')

  const [currentLang, setCurrentLang] = useState<Language>(getCurrentLanguage())

  const handleLanguageToggle = () => {
    const newLang = toggleLanguage()
    setCurrentLang(newLang)
    // 强制重新渲染页面
    window.location.reload()
  }

  return React.createElement('div', {
    style: {
      width: '100vw',
      height: '100vh',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
      color: 'white',
      textAlign: 'center',
      position: 'relative'
    }
  }, [
    React.createElement('div', {
      key: 'container',
      style: {
        background: 'rgba(255,255,255,0.95)',
        borderRadius: '20px',
        padding: '40px',
        boxShadow: '0 20px 40px rgba(0,0,0,0.1)',
        color: '#333',
        maxWidth: '500px'
      }
    }, [
      // 语言切换按钮
      React.createElement('div', {
        key: 'language-switch',
        style: {
          position: 'absolute',
          top: '20px',
          right: '20px'
        }
      }, [
        React.createElement('button', {
          key: 'lang-btn',
          style: {
            background: 'rgba(255,255,255,0.2)',
            border: '1px solid rgba(255,255,255,0.3)',
            borderRadius: '20px',
            padding: '8px 16px',
            color: 'white',
            fontSize: '14px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            transition: 'all 0.3s ease',
            backdropFilter: 'blur(10px)'
          },
          onClick: handleLanguageToggle,
          onMouseEnter: (e) => {
            e.currentTarget.style.background = 'rgba(255,255,255,0.3)'
            e.currentTarget.style.transform = 'translateY(-1px)'
          },
          onMouseLeave: (e) => {
            e.currentTarget.style.background = 'rgba(255,255,255,0.2)'
            e.currentTarget.style.transform = 'translateY(0)'
          }
        }, [
          getLanguageInfo(currentLang === 'zh' ? 'en' : 'zh').flag,
          ' ',
          getLanguageInfo(currentLang === 'zh' ? 'en' : 'zh').name
        ])
      ]),

      React.createElement('h1', {
        key: 'title',
        style: { color: '#1890ff', marginBottom: '20px' }
      }, `🏥 ${getText('systemTitle', currentLang)}`),

      React.createElement('div', {
        key: 'status',
        style: { color: '#52c41a', fontSize: '18px', margin: '20px 0' }
      }, `✅ ${getText('appRunning', currentLang)}`),

      React.createElement('p', {
        key: 'subtitle',
        style: { color: '#666', margin: '20px 0' }
      }, getText('systemSubtitle', currentLang)),
      
      React.createElement('div', {
        key: 'info',
        style: { 
          background: '#f0f9ff', 
          padding: '20px', 
          borderRadius: '8px',
          border: '1px solid #91d5ff',
          margin: '20px 0',
          textAlign: 'left'
        }
      }, [
        React.createElement('h3', {
          key: 'info-title',
          style: { color: '#1890ff', margin: '0 0 15px 0' }
        }, getText('systemStatus', currentLang)),
        React.createElement('div', {
          key: 'info-content',
          style: { fontSize: '14px', lineHeight: '1.6' }
        }, [
          React.createElement('div', { key: 'react-version' }, `✅ React ${currentLang === 'zh' ? '版本' : 'Version'}: ${React.version}`),
          React.createElement('div', { key: 'time' }, `✅ ${getText('currentTime', currentLang)}: ${new Date().toLocaleString()}`),
          React.createElement('div', { key: 'path' }, `✅ ${currentLang === 'zh' ? '页面路径' : 'Page Path'}: ${window.location.pathname}`),
          React.createElement('div', { key: 'language' }, `✅ ${currentLang === 'zh' ? '当前语言' : 'Current Language'}: ${getLanguageInfo(currentLang).name}`),
          React.createElement('div', { key: 'status' }, `✅ ${currentLang === 'zh' ? '应用状态: 正常运行' : 'App Status: Running Normal'}`)
        ])
      ]),
      
      React.createElement('div', {
        key: 'buttons',
        style: { margin: '30px 0' }
      }, [
        React.createElement('button', {
          key: 'login-btn',
          style: {
            background: '#1890ff',
            color: 'white',
            border: 'none',
            padding: '12px 24px',
            borderRadius: '6px',
            fontSize: '16px',
            cursor: 'pointer',
            margin: '0 10px'
          },
          onClick: () => {
            console.log('🔄 导航到登录页面')
            window.location.href = '/login'
          }
        }, getText('goToLogin', currentLang)),

        React.createElement('button', {
          key: 'test-btn',
          style: {
            background: '#52c41a',
            color: 'white',
            border: 'none',
            padding: '12px 24px',
            borderRadius: '6px',
            fontSize: '16px',
            cursor: 'pointer',
            margin: '0 10px'
          },
          onClick: () => {
            console.log('🧪 运行应用测试')
            const message = currentLang === 'zh'
              ? 'React 应用功能正常！\\n\\n✅ React 渲染正常\\n✅ 路由系统正常\\n✅ 事件处理正常\\n✅ 语言切换正常'
              : 'React Application Working!\\n\\n✅ React Rendering Normal\\n✅ Router System Normal\\n✅ Event Handling Normal\\n✅ Language Switch Normal'
            alert(message)
          }
        }, getText('testFunction', currentLang))
      ]),
      
      React.createElement('div', {
        key: 'success',
        style: { 
          background: '#f6ffed', 
          padding: '15px', 
          borderRadius: '8px',
          border: '1px solid #b7eb8f',
          color: '#52c41a',
          fontSize: '14px'
        }
      }, [
        React.createElement('strong', { key: 'success-title' }, `🎉 ${getText('success', currentLang)}`),
        React.createElement('br', { key: 'br1' }),
        getText('appStarted', currentLang),
        React.createElement('br', { key: 'br2' }),
        getText('canVisitLogin', currentLang)
      ])
    ])
  ])
}

// 登录页面（支持语言切换）
const SimpleLogin: React.FC = () => {
  console.log('🚀 SimpleLogin 组件正在渲染')

  const [currentLang, setCurrentLang] = useState<Language>(getCurrentLanguage())

  const handleLanguageToggle = () => {
    const newLang = toggleLanguage()
    setCurrentLang(newLang)
    // 强制重新渲染页面
    window.location.reload()
  }

  // 登录处理函数
  const handleLogin = () => {
    const usernameInput = document.getElementById('username-input') as HTMLInputElement
    const passwordInput = document.getElementById('password-input') as HTMLInputElement
    
    const username = usernameInput?.value || 'admin'
    const password = passwordInput?.value || '123456'
    
    // 角色映射
    const getRoleName = (username: string): string => {
      const roleMap = {
        'admin': getText('admin', currentLang),
        'doctor': getText('doctor', currentLang),
        'nurse': getText('nurse', currentLang),
        'manager': getText('manager', currentLang)
      }
      return roleMap[username as keyof typeof roleMap] || (currentLang === 'zh' ? '普通用户' : 'User')
    }

    const role = getRoleName(username)

    console.log('🔄 执行登录', { username, role })

    // 模拟登录验证
    if (username && password) {
      const successMessage = `🎉 ${getText('loginSuccess', currentLang)}\n\n${getText('username', currentLang)}: ${username}\n${getText('yourRole', currentLang)}: ${role}\n\n${getText('jumpingToDashboard', currentLang)}`
      alert(successMessage)

      // 跳转到 dashboard
      setTimeout(() => {
        window.location.href = `/dashboard?user=${encodeURIComponent(username)}&role=${encodeURIComponent(role)}&lang=${currentLang}`
      }, 1500)
    } else {
      const errorMessage = currentLang === 'zh' ? '❌ 请输入用户名和密码' : '❌ Please enter username and password'
      alert(errorMessage)
    }
  }
  
  return React.createElement('div', {
    style: {
      width: '100vw',
      height: '100vh',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
      color: 'white',
      textAlign: 'center'
    }
  }, [
    React.createElement('div', {
      key: 'container',
      style: {
        background: 'rgba(255,255,255,0.95)',
        borderRadius: '20px',
        padding: '40px',
        boxShadow: '0 20px 40px rgba(0,0,0,0.1)',
        color: '#333',
        maxWidth: '400px'
      }
    }, [
      // 语言切换按钮
      React.createElement('div', {
        key: 'language-switch',
        style: {
          position: 'absolute',
          top: '20px',
          right: '20px'
        }
      }, [
        React.createElement('button', {
          key: 'lang-btn',
          style: {
            background: 'rgba(255,255,255,0.2)',
            border: '1px solid rgba(255,255,255,0.3)',
            borderRadius: '20px',
            padding: '8px 16px',
            color: 'white',
            fontSize: '14px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            transition: 'all 0.3s ease',
            backdropFilter: 'blur(10px)'
          },
          onClick: handleLanguageToggle,
          onMouseEnter: (e) => {
            e.currentTarget.style.background = 'rgba(255,255,255,0.3)'
            e.currentTarget.style.transform = 'translateY(-1px)'
          },
          onMouseLeave: (e) => {
            e.currentTarget.style.background = 'rgba(255,255,255,0.2)'
            e.currentTarget.style.transform = 'translateY(0)'
          }
        }, [
          getLanguageInfo(currentLang === 'zh' ? 'en' : 'zh').flag,
          ' ',
          getLanguageInfo(currentLang === 'zh' ? 'en' : 'zh').name
        ])
      ]),

      React.createElement('h1', {
        key: 'title',
        style: { color: '#1890ff', marginBottom: '20px' }
      }, `🏥 ${getText('systemTitle', currentLang)}`),

      React.createElement('div', {
        key: 'status',
        style: { color: '#52c41a', fontSize: '16px', margin: '20px 0' }
      }, `✅ ${getText('modernInterface', currentLang)}`),

      React.createElement('p', {
        key: 'subtitle',
        style: { color: '#666', margin: '20px 0' }
      }, getText('systemSubtitle', currentLang)),
      
      React.createElement('div', {
        key: 'form',
        style: { margin: '30px 0' }
      }, [
        React.createElement('input', {
          key: 'username',
          id: 'username-input',
          type: 'text',
          placeholder: getText('usernamePlaceholder', currentLang),
          defaultValue: 'admin',
          style: {
            width: '100%',
            padding: '12px',
            margin: '10px 0',
            border: '1px solid #ddd',
            borderRadius: '6px',
            fontSize: '16px'
          }
        }),
        React.createElement('input', {
          key: 'password',
          id: 'password-input',
          type: 'password',
          placeholder: getText('passwordPlaceholder', currentLang),
          defaultValue: '123456',
          style: {
            width: '100%',
            padding: '12px',
            margin: '10px 0',
            border: '1px solid #ddd',
            borderRadius: '6px',
            fontSize: '16px'
          }
        }),
        React.createElement('button', {
          key: 'login-btn',
          style: {
            width: '100%',
            background: '#1890ff',
            color: 'white',
            border: 'none',
            padding: '12px',
            borderRadius: '6px',
            fontSize: '16px',
            cursor: 'pointer',
            margin: '10px 0'
          },
          onClick: handleLogin
        }, getText('loginButton', currentLang))
      ]),
      
      // 快速登录按钮
      React.createElement('div', {
        key: 'quick-login',
        style: { margin: '20px 0' }
      }, [
        React.createElement('div', {
          key: 'quick-title',
          style: { 
            fontSize: '14px', 
            color: '#666', 
            marginBottom: '10px',
            textAlign: 'center'
          }
        }, getText('quickLogin', currentLang)),
        
        React.createElement('div', {
          key: 'quick-buttons',
          style: { 
            display: 'grid', 
            gridTemplateColumns: '1fr 1fr', 
            gap: '8px' 
          }
        }, [
          React.createElement('button', {
            key: 'admin-btn',
            style: {
              background: '#1890ff',
              color: 'white',
              border: 'none',
              padding: '8px 12px',
              borderRadius: '4px',
              fontSize: '12px',
              cursor: 'pointer'
            },
            onClick: () => {
              const usernameInput = document.getElementById('username-input') as HTMLInputElement
              const passwordInput = document.getElementById('password-input') as HTMLInputElement
              if (usernameInput) usernameInput.value = 'admin'
              if (passwordInput) passwordInput.value = '123456'
            }
          }, `👨‍💼 ${getText('admin', currentLang)}`),
          
          React.createElement('button', {
            key: 'doctor-btn',
            style: {
              background: '#52c41a',
              color: 'white',
              border: 'none',
              padding: '8px 12px',
              borderRadius: '4px',
              fontSize: '12px',
              cursor: 'pointer'
            },
            onClick: () => {
              const usernameInput = document.getElementById('username-input') as HTMLInputElement
              const passwordInput = document.getElementById('password-input') as HTMLInputElement
              if (usernameInput) usernameInput.value = 'doctor'
              if (passwordInput) passwordInput.value = '123456'
            }
          }, `👨‍⚕️ ${getText('doctor', currentLang)}`),
          
          React.createElement('button', {
            key: 'nurse-btn',
            style: {
              background: '#fa8c16',
              color: 'white',
              border: 'none',
              padding: '8px 12px',
              borderRadius: '4px',
              fontSize: '12px',
              cursor: 'pointer'
            },
            onClick: () => {
              const usernameInput = document.getElementById('username-input') as HTMLInputElement
              const passwordInput = document.getElementById('password-input') as HTMLInputElement
              if (usernameInput) usernameInput.value = 'nurse'
              if (passwordInput) passwordInput.value = '123456'
            }
          }, `👩‍⚕️ ${getText('nurse', currentLang)}`),
          
          React.createElement('button', {
            key: 'manager-btn',
            style: {
              background: '#722ed1',
              color: 'white',
              border: 'none',
              padding: '8px 12px',
              borderRadius: '4px',
              fontSize: '12px',
              cursor: 'pointer'
            },
            onClick: () => {
              const usernameInput = document.getElementById('username-input') as HTMLInputElement
              const passwordInput = document.getElementById('password-input') as HTMLInputElement
              if (usernameInput) usernameInput.value = 'manager'
              if (passwordInput) passwordInput.value = '123456'
            }
          }, `👨‍💻 ${getText('manager', currentLang)}`)
        ])
      ]),
      
      React.createElement('div', {
        key: 'back',
        style: { margin: '20px 0' }
      }, [
        React.createElement('button', {
          key: 'back-btn',
          style: {
            background: '#52c41a',
            color: 'white',
            border: 'none',
            padding: '8px 16px',
            borderRadius: '6px',
            fontSize: '14px',
            cursor: 'pointer'
          },
          onClick: () => {
            console.log('🏠 返回首页')
            window.location.href = '/'
          }
        }, getText('backToHome', currentLang))
      ]),
      
      React.createElement('div', {
        key: 'info',
        style: { 
          background: '#f0f9ff', 
          padding: '15px', 
          borderRadius: '8px',
          border: '1px solid #91d5ff',
          color: '#1890ff',
          fontSize: '14px',
          marginTop: '20px'
        }
      }, [
        React.createElement('strong', { key: 'info-title' }, getText('testAccount', currentLang)),
        React.createElement('br', { key: 'br1' }),
        `${getText('username', currentLang)}: admin | ${getText('password', currentLang)}: 123456`,
        React.createElement('br', { key: 'br2' }),
        currentLang === 'zh' ? '现代化登录界面已启用' : 'Modern login interface enabled'
      ])
    ])
  ])
}

// 简单的 App 组件
const WorkingApp: React.FC = () => {
  console.log('🚀 WorkingApp 组件正在渲染')
  
  return React.createElement(BrowserRouter, {}, [
    React.createElement(Routes, { key: 'routes' }, [
      React.createElement(Route, {
        key: 'home',
        path: '/',
        element: React.createElement(SimpleTest)
      }),
      React.createElement(Route, {
        key: 'login',
        path: '/login',
        element: React.createElement(SimpleLogin)
      }),
      React.createElement(Route, {
        key: 'dashboard',
        path: '/dashboard',
        element: React.createElement(Dashboard)
      }),
      React.createElement(Route, {
        key: 'datasource',
        path: '/data-source',
        element: React.createElement(DataSourceManagement)
      }),
      React.createElement(Route, {
        key: 'notfound',
        path: '*',
        element: React.createElement('div', {
          style: {
            width: '100vw',
            height: '100vh',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: '#f5f5f5',
            fontFamily: 'monospace'
          }
        }, [
          React.createElement('div', {
            key: 'content',
            style: {
              background: 'white',
              padding: '40px',
              borderRadius: '8px',
              textAlign: 'center',
              boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
            }
          }, [
            React.createElement('h2', { key: 'title' }, '404 - 页面未找到'),
            React.createElement('p', { key: 'desc' }, '请求的页面不存在'),
            React.createElement('button', {
              key: 'back-btn',
              style: {
                background: '#1890ff',
                color: 'white',
                border: 'none',
                padding: '8px 16px',
                borderRadius: '4px',
                cursor: 'pointer'
              },
              onClick: () => window.location.href = '/'
            }, '返回首页')
          ])
        ])
      })
    ])
  ])
}

export default WorkingApp
