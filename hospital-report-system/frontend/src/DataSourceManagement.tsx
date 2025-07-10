import React, { useState, useEffect } from 'react'
import { getCurrentLanguage, setLanguage, getText, toggleLanguage, getLanguageInfo, Language } from './i18n'

// 数据源类型
type DataSourceType = 'oracle' | 'mysql' | 'postgresql' | 'mongodb' | 'sqlserver' | 'redis'
type DataSourceStatus = 'connected' | 'disconnected' | 'testing' | 'error'

// 数据源接口
interface DataSource {
  id: string
  name: string
  type: DataSourceType
  status: DataSourceStatus
  createTime: string
  host?: string
  port?: number
  database?: string
}

// 模拟数据源数据
const mockDataSources: DataSource[] = [
  {
    id: '1',
    name: '医院主数据库',
    type: 'oracle',
    status: 'connected',
    createTime: '2024-01-15 10:30:00',
    host: '192.168.1.100',
    port: 1521,
    database: 'HOSPITAL_DB'
  },
  {
    id: '2',
    name: '患者信息库',
    type: 'mysql',
    status: 'connected',
    createTime: '2024-01-20 14:20:00',
    host: '192.168.1.101',
    port: 3306,
    database: 'patient_info'
  },
  {
    id: '3',
    name: '报表数据仓库',
    type: 'postgresql',
    status: 'disconnected',
    createTime: '2024-02-01 09:15:00',
    host: '192.168.1.102',
    port: 5432,
    database: 'report_warehouse'
  },
  {
    id: '4',
    name: '日志数据库',
    type: 'mongodb',
    status: 'connected',
    createTime: '2024-02-10 16:45:00',
    host: '192.168.1.103',
    port: 27017,
    database: 'logs'
  },
  {
    id: '5',
    name: '缓存数据库',
    type: 'redis',
    status: 'error',
    createTime: '2024-02-15 11:30:00',
    host: '192.168.1.104',
    port: 6379
  }
]

// 数据源管理页面
const DataSourceManagement: React.FC = () => {
  const [currentLang, setCurrentLang] = useState<Language>(getCurrentLanguage())
  const [dataSources, setDataSources] = useState<DataSource[]>(mockDataSources)
  const [searchTerm, setSearchTerm] = useState('')
  const [filterType, setFilterType] = useState<DataSourceType | 'all'>('all')
  const [filterStatus, setFilterStatus] = useState<DataSourceStatus | 'all'>('all')

  // 语言切换处理
  const handleLanguageToggle = () => {
    const newLang = toggleLanguage()
    setCurrentLang(newLang)
    window.location.reload()
  }

  // 获取翻译文本的辅助函数
  const t = (key: string, subKey?: string) => {
    if (subKey) {
      return (getText as any)(key, currentLang)?.[subKey] || key
    }
    return getText(key as any, currentLang)
  }

  // 获取数据源类型图标
  const getTypeIcon = (type: DataSourceType) => {
    const icons = {
      oracle: '🔶',
      mysql: '🐬',
      postgresql: '🐘',
      mongodb: '🍃',
      sqlserver: '🏢',
      redis: '🔴'
    }
    return icons[type] || '💾'
  }

  // 获取状态颜色和图标
  const getStatusStyle = (status: DataSourceStatus) => {
    const styles = {
      connected: { color: '#52c41a', bg: '#f6ffed', icon: '🟢' },
      disconnected: { color: '#faad14', bg: '#fffbe6', icon: '🟡' },
      testing: { color: '#1890ff', bg: '#f0f9ff', icon: '🔵' },
      error: { color: '#ff4d4f', bg: '#fff2f0', icon: '🔴' }
    }
    return styles[status]
  }

  // 过滤数据源
  const filteredDataSources = dataSources.filter(ds => {
    const matchesSearch = ds.name.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesType = filterType === 'all' || ds.type === filterType
    const matchesStatus = filterStatus === 'all' || ds.status === filterStatus
    return matchesSearch && matchesType && matchesStatus
  })

  // 测试连接
  const handleTestConnection = (id: string) => {
    setDataSources(prev => prev.map(ds => 
      ds.id === id ? { ...ds, status: 'testing' as DataSourceStatus } : ds
    ))
    
    // 模拟测试过程
    setTimeout(() => {
      const success = Math.random() > 0.3 // 70% 成功率
      setDataSources(prev => prev.map(ds => 
        ds.id === id ? { 
          ...ds, 
          status: success ? 'connected' as DataSourceStatus : 'error' as DataSourceStatus 
        } : ds
      ))
      
      const message = success ? t('dataSource', 'testSuccess') : t('dataSource', 'testFailed')
      alert(message)
    }, 2000)
  }

  // 删除数据源
  const handleDelete = (id: string) => {
    if (confirm(t('dataSource', 'deleteConfirm'))) {
      setDataSources(prev => prev.filter(ds => ds.id !== id))
    }
  }

  return React.createElement('div', {
    style: {
      display: 'flex',
      height: '100vh',
      background: '#f5f7fa',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif'
    }
  }, [
    // 左侧导航栏
    React.createElement('div', {
      key: 'sidebar',
      style: {
        width: '240px',
        background: 'linear-gradient(180deg, #4f46e5 0%, #3b82f6 100%)',
        color: 'white',
        display: 'flex',
        flexDirection: 'column',
        boxShadow: '2px 0 8px rgba(0,0,0,0.1)'
      }
    }, [
      // Logo区域
      React.createElement('div', {
        key: 'logo',
        style: {
          padding: '24px 20px',
          borderBottom: '1px solid rgba(255,255,255,0.1)'
        }
      }, [
        React.createElement('div', {
          key: 'logo-content',
          style: {
            display: 'flex',
            alignItems: 'center',
            gap: '12px'
          }
        }, [
          React.createElement('div', {
            key: 'logo-icon',
            style: { fontSize: '28px' }
          }, '🏥'),
          React.createElement('div', { key: 'logo-text' }, [
            React.createElement('div', {
              key: 'title',
              style: { fontSize: '16px', fontWeight: 'bold' }
            }, t('systemTitle')),
            React.createElement('div', {
              key: 'subtitle',
              style: { fontSize: '12px', opacity: 0.8 }
            }, currentLang === 'zh' ? '数据管理平台' : 'Data Management Platform')
          ])
        ])
      ]),
      
      // 导航菜单
      React.createElement('nav', {
        key: 'nav',
        style: { flex: 1, padding: '20px 0' }
      }, [
        // 仪表盘
        React.createElement('div', {
          key: 'nav-dashboard',
          style: {
            padding: '12px 20px',
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            cursor: 'pointer',
            transition: 'background 0.2s',
            opacity: 0.7
          },
          onClick: () => window.location.href = '/dashboard',
          onMouseEnter: (e) => e.currentTarget.style.background = 'rgba(255,255,255,0.1)',
          onMouseLeave: (e) => e.currentTarget.style.background = 'transparent'
        }, [
          React.createElement('span', { key: 'icon', style: { fontSize: '20px' } }, '📊'),
          React.createElement('span', { key: 'text' }, t('nav', 'dashboard'))
        ]),
        
        // 数据源管理（当前页面）
        React.createElement('div', {
          key: 'nav-datasource',
          style: {
            padding: '12px 20px',
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            background: 'rgba(255,255,255,0.15)',
            borderRight: '3px solid white',
            fontWeight: 'bold'
          }
        }, [
          React.createElement('span', { key: 'icon', style: { fontSize: '20px' } }, '🗄️'),
          React.createElement('span', { key: 'text' }, t('nav', 'dataSource'))
        ]),
        
        // 其他导航项
        ...[
          { icon: '📋', key: 'reportConfig', href: '/report-config' },
          { icon: '📈', key: 'reportView', href: '/report-view' },
          { icon: '👥', key: 'userManagement', href: '/user-management' },
          { icon: '⚙️', key: 'systemSettings', href: '/system-settings' }
        ].map(item => 
          React.createElement('div', {
            key: `nav-${item.key}`,
            style: {
              padding: '12px 20px',
              display: 'flex',
              alignItems: 'center',
              gap: '12px',
              cursor: 'pointer',
              transition: 'background 0.2s',
              opacity: 0.7
            },
            onClick: () => alert(`${t('nav', item.key)} - ${t('functionDevelopment')}`),
            onMouseEnter: (e) => e.currentTarget.style.background = 'rgba(255,255,255,0.1)',
            onMouseLeave: (e) => e.currentTarget.style.background = 'transparent'
          }, [
            React.createElement('span', { key: 'icon', style: { fontSize: '20px' } }, item.icon),
            React.createElement('span', { key: 'text' }, t('nav', item.key))
          ])
        )
      ])
    ]),
    
    // 右侧主内容区域
    React.createElement('div', {
      key: 'main',
      style: {
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden'
      }
    }, [
      // 顶部导航栏
      React.createElement('div', {
        key: 'header',
        style: {
          background: 'white',
          padding: '16px 24px',
          borderBottom: '1px solid #e8eaed',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
        }
      }, [
        React.createElement('div', { key: 'breadcrumb' }, [
          React.createElement('h1', {
            key: 'title',
            style: {
              margin: 0,
              fontSize: '24px',
              fontWeight: 'bold',
              color: '#1f2937'
            }
          }, t('dataSource', 'title')),
          React.createElement('p', {
            key: 'subtitle',
            style: {
              margin: '4px 0 0 0',
              color: '#6b7280',
              fontSize: '14px'
            }
          }, t('dataSource', 'subtitle'))
        ]),
        
        // 语言切换按钮
        React.createElement('button', {
          key: 'lang-switch',
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

        // 主工作区域
        React.createElement('div', {
          key: 'content',
          style: {
            flex: 1,
            padding: '24px',
            overflow: 'auto'
          }
        }, [
          // 操作栏
          React.createElement('div', {
            key: 'toolbar',
            style: {
              background: 'white',
              padding: '20px',
              borderRadius: '12px',
              marginBottom: '20px',
              boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              flexWrap: 'wrap',
              gap: '16px'
            }
          }, [
            // 左侧操作按钮
            React.createElement('div', {
              key: 'left-actions',
              style: { display: 'flex', gap: '12px', alignItems: 'center' }
            }, [
              React.createElement('button', {
                key: 'add-btn',
                style: {
                  background: 'linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%)',
                  color: 'white',
                  border: 'none',
                  borderRadius: '8px',
                  padding: '10px 20px',
                  fontSize: '14px',
                  fontWeight: '500',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  transition: 'transform 0.2s',
                  boxShadow: '0 2px 4px rgba(79, 70, 229, 0.3)'
                },
                onClick: () => alert(`${t('dataSource', 'addDataSource')} - ${t('functionDevelopment')}`),
                onMouseEnter: (e) => e.currentTarget.style.transform = 'translateY(-1px)',
                onMouseLeave: (e) => e.currentTarget.style.transform = 'translateY(0)'
              }, [
                React.createElement('span', { key: 'icon' }, '➕'),
                React.createElement('span', { key: 'text' }, t('dataSource', 'addDataSource'))
              ])
            ]),

            // 右侧搜索和筛选
            React.createElement('div', {
              key: 'right-actions',
              style: { display: 'flex', gap: '12px', alignItems: 'center' }
            }, [
              // 搜索框
              React.createElement('input', {
                key: 'search',
                type: 'text',
                placeholder: t('dataSource', 'search'),
                value: searchTerm,
                onChange: (e) => setSearchTerm(e.target.value),
                style: {
                  padding: '8px 12px',
                  border: '1px solid #d1d5db',
                  borderRadius: '6px',
                  fontSize: '14px',
                  width: '200px',
                  outline: 'none',
                  transition: 'border-color 0.2s'
                },
                onFocus: (e) => e.currentTarget.style.borderColor = '#3b82f6',
                onBlur: (e) => e.currentTarget.style.borderColor = '#d1d5db'
              }),

              // 类型筛选
              React.createElement('select', {
                key: 'type-filter',
                value: filterType,
                onChange: (e) => setFilterType(e.target.value as DataSourceType | 'all'),
                style: {
                  padding: '8px 12px',
                  border: '1px solid #d1d5db',
                  borderRadius: '6px',
                  fontSize: '14px',
                  outline: 'none',
                  cursor: 'pointer'
                }
              }, [
                React.createElement('option', { key: 'all-types', value: 'all' }, t('dataSource', 'allTypes')),
                ...(['oracle', 'mysql', 'postgresql', 'mongodb', 'sqlserver', 'redis'] as DataSourceType[]).map(type =>
                  React.createElement('option', { key: type, value: type }, t('dataSource', `types.${type}`))
                )
              ]),

              // 状态筛选
              React.createElement('select', {
                key: 'status-filter',
                value: filterStatus,
                onChange: (e) => setFilterStatus(e.target.value as DataSourceStatus | 'all'),
                style: {
                  padding: '8px 12px',
                  border: '1px solid #d1d5db',
                  borderRadius: '6px',
                  fontSize: '14px',
                  outline: 'none',
                  cursor: 'pointer'
                }
              }, [
                React.createElement('option', { key: 'all-status', value: 'all' }, t('dataSource', 'allStatus')),
                ...(['connected', 'disconnected', 'testing', 'error'] as DataSourceStatus[]).map(status =>
                  React.createElement('option', { key: status, value: status }, t('dataSource', `statusTypes.${status}`))
                )
              ])
            ])
          ]),

          // 数据源列表
          React.createElement('div', {
            key: 'datasource-list',
            style: {
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(400px, 1fr))',
              gap: '20px'
            }
          }, filteredDataSources.map(ds => {
            const statusStyle = getStatusStyle(ds.status)

            return React.createElement('div', {
              key: ds.id,
              style: {
                background: 'white',
                borderRadius: '12px',
                padding: '20px',
                boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
                border: '1px solid #f0f0f0',
                transition: 'all 0.3s ease',
                cursor: 'pointer'
              },
              onMouseEnter: (e) => {
                e.currentTarget.style.boxShadow = '0 4px 16px rgba(0,0,0,0.12)'
                e.currentTarget.style.transform = 'translateY(-2px)'
              },
              onMouseLeave: (e) => {
                e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.06)'
                e.currentTarget.style.transform = 'translateY(0)'
              }
            }, [
              // 卡片头部
              React.createElement('div', {
                key: 'header',
                style: {
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'flex-start',
                  marginBottom: '16px'
                }
              }, [
                React.createElement('div', {
                  key: 'info',
                  style: { flex: 1 }
                }, [
                  React.createElement('div', {
                    key: 'name-row',
                    style: {
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px',
                      marginBottom: '8px'
                    }
                  }, [
                    React.createElement('span', {
                      key: 'type-icon',
                      style: { fontSize: '20px' }
                    }, getTypeIcon(ds.type)),
                    React.createElement('h3', {
                      key: 'name',
                      style: {
                        margin: 0,
                        fontSize: '16px',
                        fontWeight: 'bold',
                        color: '#1f2937'
                      }
                    }, ds.name)
                  ]),

                  React.createElement('div', {
                    key: 'type-badge',
                    style: {
                      display: 'inline-block',
                      background: '#f3f4f6',
                      color: '#374151',
                      padding: '4px 8px',
                      borderRadius: '4px',
                      fontSize: '12px',
                      fontWeight: '500'
                    }
                  }, t('dataSource', `types.${ds.type}`))
                ]),

                // 状态指示器
                React.createElement('div', {
                  key: 'status',
                  style: {
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                    background: statusStyle.bg,
                    color: statusStyle.color,
                    padding: '6px 12px',
                    borderRadius: '20px',
                    fontSize: '12px',
                    fontWeight: '500'
                  }
                }, [
                  React.createElement('span', { key: 'icon' }, statusStyle.icon),
                  React.createElement('span', { key: 'text' }, t('dataSource', `statusTypes.${ds.status}`))
                ])
              ]),

              // 连接信息
              React.createElement('div', {
                key: 'connection-info',
                style: {
                  background: '#f9fafb',
                  padding: '12px',
                  borderRadius: '8px',
                  marginBottom: '16px'
                }
              }, [
                ds.host && React.createElement('div', {
                  key: 'host',
                  style: {
                    fontSize: '13px',
                    color: '#6b7280',
                    marginBottom: '4px'
                  }
                }, `${currentLang === 'zh' ? '主机' : 'Host'}: ${ds.host}${ds.port ? `:${ds.port}` : ''}`),

                ds.database && React.createElement('div', {
                  key: 'database',
                  style: {
                    fontSize: '13px',
                    color: '#6b7280',
                    marginBottom: '4px'
                  }
                }, `${currentLang === 'zh' ? '数据库' : 'Database'}: ${ds.database}`),

                React.createElement('div', {
                  key: 'create-time',
                  style: {
                    fontSize: '13px',
                    color: '#6b7280'
                  }
                }, `${t('dataSource', 'createTime')}: ${ds.createTime}`)
              ]),

              // 操作按钮
              React.createElement('div', {
                key: 'actions',
                style: {
                  display: 'flex',
                  gap: '8px',
                  justifyContent: 'flex-end'
                }
              }, [
                React.createElement('button', {
                  key: 'test-btn',
                  style: {
                    background: ds.status === 'testing' ? '#f0f0f0' : '#e6f7ff',
                    color: ds.status === 'testing' ? '#999' : '#1890ff',
                    border: 'none',
                    borderRadius: '6px',
                    padding: '6px 12px',
                    fontSize: '12px',
                    cursor: ds.status === 'testing' ? 'not-allowed' : 'pointer',
                    transition: 'all 0.2s'
                  },
                  disabled: ds.status === 'testing',
                  onClick: (e) => {
                    e.stopPropagation()
                    handleTestConnection(ds.id)
                  }
                }, ds.status === 'testing' ?
                  (currentLang === 'zh' ? '测试中...' : 'Testing...') :
                  t('dataSource', 'testConnection')
                ),

                React.createElement('button', {
                  key: 'edit-btn',
                  style: {
                    background: '#f0f9ff',
                    color: '#3b82f6',
                    border: 'none',
                    borderRadius: '6px',
                    padding: '6px 12px',
                    fontSize: '12px',
                    cursor: 'pointer',
                    transition: 'all 0.2s'
                  },
                  onClick: (e) => {
                    e.stopPropagation()
                    alert(`${t('dataSource', 'edit')} ${ds.name} - ${t('functionDevelopment')}`)
                  }
                }, t('dataSource', 'edit')),

                React.createElement('button', {
                  key: 'delete-btn',
                  style: {
                    background: '#fff2f0',
                    color: '#ff4d4f',
                    border: 'none',
                    borderRadius: '6px',
                    padding: '6px 12px',
                    fontSize: '12px',
                    cursor: 'pointer',
                    transition: 'all 0.2s'
                  },
                  onClick: (e) => {
                    e.stopPropagation()
                    handleDelete(ds.id)
                  }
                }, t('dataSource', 'delete'))
              ])
            ])
          })),

          // 空状态
          filteredDataSources.length === 0 && React.createElement('div', {
            key: 'empty-state',
            style: {
              background: 'white',
              borderRadius: '12px',
              padding: '60px 20px',
              textAlign: 'center',
              boxShadow: '0 2px 8px rgba(0,0,0,0.06)'
            }
          }, [
            React.createElement('div', {
              key: 'empty-icon',
              style: { fontSize: '48px', marginBottom: '16px' }
            }, '📊'),
            React.createElement('h3', {
              key: 'empty-title',
              style: {
                margin: '0 0 8px 0',
                color: '#6b7280',
                fontSize: '18px'
              }
            }, currentLang === 'zh' ? '暂无数据源' : 'No Data Sources'),
            React.createElement('p', {
              key: 'empty-desc',
              style: {
                margin: 0,
                color: '#9ca3af',
                fontSize: '14px'
              }
            }, currentLang === 'zh' ? '点击"新增数据源"按钮开始添加' : 'Click "Add Data Source" to get started')
          ])
        ])
      ])
    ])
  ])
}

export default DataSourceManagement
