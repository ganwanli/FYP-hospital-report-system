// 简化的国际化系统
export type Language = 'zh' | 'en'

// 中文翻译
const zhTexts = {
  // 系统
  systemTitle: '医院报告管理系统',
  systemSubtitle: 'Hospital Report Management System',
  systemStatus: '系统状态',
  currentTime: '当前时间',
  appRunning: 'React 应用运行正常',
  
  // 登录
  loginTitle: '系统登录',
  username: '用户名',
  password: '密码',
  loginButton: '登录系统',
  quickLogin: '快速登录',
  modernInterface: '现代化登录界面',
  backToHome: '返回首页',
  loginSuccess: '登录成功！',
  jumpingToDashboard: '即将跳转到系统仪表板...',
  testAccount: '快速登录测试',
  usernamePlaceholder: '请输入用户名',
  passwordPlaceholder: '请输入密码',
  
  // 角色
  admin: '系统管理员',
  doctor: '医生',
  nurse: '护士',
  manager: '部门主管',
  
  // Dashboard
  welcome: '欢迎回来',
  yourRole: '您的角色',
  today: '今天是',
  logout: '退出登录',
  confirmLogout: '确定要退出登录吗？',
  systemFunctions: '系统功能',
  
  // 统计
  todayReports: '今日报表',
  onlineUsers: '在线用户',
  dataSources: '数据源',
  systemUptime: '系统运行',
  
  // 功能模块
  dataSourceManagement: '数据源管理',
  reportConfiguration: '报表配置',
  userManagement: '用户管理',
  systemMonitoring: '系统监控',
  reportViewing: '报表查看',
  systemSettings: '系统设置',
  
  dataSourceDesc: '管理数据库连接和数据源配置',
  reportConfigDesc: '创建和配置各类医院报表',
  userMgmtDesc: '管理系统用户和权限分配',
  systemMonitorDesc: '监控系统运行状态和性能',
  reportViewDesc: '查看和导出各类报表数据',
  systemSettingsDesc: '系统参数和配置管理',
  
  enterFunction: '即将进入',
  functionDevelopment: '功能开发中...',
  
  // 通用
  testFunction: '测试功能',
  goToLogin: '访问登录页面',
  success: '成功！',
  appStarted: 'React 应用已成功启动并正常运行！',
  canVisitLogin: '现在可以访问登录页面体验现代化界面。',
  
  // 语言
  switchLanguage: '切换语言',
  chinese: '中文',
  english: 'English',

  // 导航菜单
  nav: {
    dashboard: '仪表盘',
    dataSource: '数据源管理',
    reportConfig: '报表配置',
    reportView: '报表查看',
    userManagement: '用户管理',
    systemSettings: '系统设置'
  },

  // 数据源管理
  dataSource: {
    title: '数据源管理',
    subtitle: '管理和配置系统数据源连接',
    addDataSource: '新增数据源',
    search: '搜索数据源',
    filter: '筛选',
    allTypes: '所有类型',
    allStatus: '所有状态',

    // 表格列头
    name: '数据源名称',
    type: '类型',
    status: '状态',
    createTime: '创建时间',
    actions: '操作',

    // 数据源类型
    types: {
      oracle: 'Oracle',
      mysql: 'MySQL',
      postgresql: 'PostgreSQL',
      mongodb: 'MongoDB',
      sqlserver: 'SQL Server',
      redis: 'Redis'
    },

    // 状态
    statusTypes: {
      connected: '已连接',
      disconnected: '断开连接',
      testing: '测试中',
      error: '连接错误'
    },

    // 操作按钮
    edit: '编辑',
    delete: '删除',
    testConnection: '测试连接',
    connect: '连接',
    disconnect: '断开',

    // 提示信息
    deleteConfirm: '确定要删除这个数据源吗？',
    testSuccess: '连接测试成功',
    testFailed: '连接测试失败',
    connectSuccess: '连接成功',
    connectFailed: '连接失败'
  }
}

// 英文翻译
const enTexts = {
  // System
  systemTitle: 'Hospital Report Management System',
  systemSubtitle: '医院报告管理系统',
  systemStatus: 'System Status',
  currentTime: 'Current Time',
  appRunning: 'React Application Running',
  
  // Login
  loginTitle: 'System Login',
  username: 'Username',
  password: 'Password',
  loginButton: 'Login',
  quickLogin: 'Quick Login',
  modernInterface: 'Modern Login Interface',
  backToHome: 'Back to Home',
  loginSuccess: 'Login Successful!',
  jumpingToDashboard: 'Redirecting to dashboard...',
  testAccount: 'Quick Login Test',
  usernamePlaceholder: 'Please enter username',
  passwordPlaceholder: 'Please enter password',
  
  // Roles
  admin: 'Administrator',
  doctor: 'Doctor',
  nurse: 'Nurse',
  manager: 'Manager',
  
  // Dashboard
  welcome: 'Welcome back',
  yourRole: 'Your Role',
  today: 'Today is',
  logout: 'Logout',
  confirmLogout: 'Are you sure you want to logout?',
  systemFunctions: 'System Functions',
  
  // Statistics
  todayReports: "Today's Reports",
  onlineUsers: 'Online Users',
  dataSources: 'Data Sources',
  systemUptime: 'System Uptime',
  
  // Function modules
  dataSourceManagement: 'Data Source Management',
  reportConfiguration: 'Report Configuration',
  userManagement: 'User Management',
  systemMonitoring: 'System Monitoring',
  reportViewing: 'Report Viewing',
  systemSettings: 'System Settings',
  
  dataSourceDesc: 'Manage database connections and data source configurations',
  reportConfigDesc: 'Create and configure various hospital reports',
  userMgmtDesc: 'Manage system users and permission assignments',
  systemMonitorDesc: 'Monitor system running status and performance',
  reportViewDesc: 'View and export various report data',
  systemSettingsDesc: 'System parameters and configuration management',
  
  enterFunction: 'Entering',
  functionDevelopment: 'Function under development...',
  
  // Common
  testFunction: 'Test Functions',
  goToLogin: 'Go to Login',
  success: 'Success!',
  appStarted: 'React application has started successfully and is running normally!',
  canVisitLogin: 'You can now visit the login page to experience the modern interface.',
  
  // Language
  switchLanguage: 'Switch Language',
  chinese: '中文',
  english: 'English',

  // Navigation menu
  nav: {
    dashboard: 'Dashboard',
    dataSource: 'Data Sources',
    reportConfig: 'Report Config',
    reportView: 'Report View',
    userManagement: 'User Management',
    systemSettings: 'System Settings'
  },

  // Data Source Management
  dataSource: {
    title: 'Data Source Management',
    subtitle: 'Manage and configure system data source connections',
    addDataSource: 'Add Data Source',
    search: 'Search data sources',
    filter: 'Filter',
    allTypes: 'All Types',
    allStatus: 'All Status',

    // Table headers
    name: 'Data Source Name',
    type: 'Type',
    status: 'Status',
    createTime: 'Created Time',
    actions: 'Actions',

    // Data source types
    types: {
      oracle: 'Oracle',
      mysql: 'MySQL',
      postgresql: 'PostgreSQL',
      mongodb: 'MongoDB',
      sqlserver: 'SQL Server',
      redis: 'Redis'
    },

    // Status types
    statusTypes: {
      connected: 'Connected',
      disconnected: 'Disconnected',
      testing: 'Testing',
      error: 'Connection Error'
    },

    // Action buttons
    edit: 'Edit',
    delete: 'Delete',
    testConnection: 'Test Connection',
    connect: 'Connect',
    disconnect: 'Disconnect',

    // Messages
    deleteConfirm: 'Are you sure you want to delete this data source?',
    testSuccess: 'Connection test successful',
    testFailed: 'Connection test failed',
    connectSuccess: 'Connected successfully',
    connectFailed: 'Connection failed'
  }
}

// 翻译对象
const translations = {
  zh: zhTexts,
  en: enTexts
}

// 获取当前语言
export const getCurrentLanguage = (): Language => {
  try {
    const stored = localStorage.getItem('hospital-language') as Language
    if (stored === 'zh' || stored === 'en') return stored
  } catch {}
  
  // 检测浏览器语言
  const browserLang = navigator.language.toLowerCase()
  return browserLang.startsWith('zh') ? 'zh' : 'en'
}

// 设置语言
export const setLanguage = (lang: Language): void => {
  try {
    localStorage.setItem('hospital-language', lang)
  } catch {}
}

// 获取翻译文本
export const getText = (key: keyof typeof zhTexts, lang?: Language): string => {
  const currentLang = lang || getCurrentLanguage()
  return translations[currentLang][key] || translations.zh[key]
}

// 切换语言
export const toggleLanguage = (): Language => {
  const current = getCurrentLanguage()
  const newLang: Language = current === 'zh' ? 'en' : 'zh'
  setLanguage(newLang)
  return newLang
}

// 语言信息
export const getLanguageInfo = (lang: Language) => {
  return {
    zh: { name: '中文', flag: '🇨🇳' },
    en: { name: 'English', flag: '🇺🇸' }
  }[lang]
}
