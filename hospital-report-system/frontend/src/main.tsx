import React from 'react'
import ReactDOM from 'react-dom/client'
import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import App from './App'

console.log('🚀 开始启动 React 应用')

// Ant Design 全局配置
const antdConfig = {
  locale: zhCN,
  theme: {
    token: {
      colorPrimary: '#1890ff',
      borderRadius: 6,
    },
  },
}

console.log('📦 配置完成，开始渲染')

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ConfigProvider {...antdConfig}>
      <App />
    </ConfigProvider>
  </React.StrictMode>
)

console.log('✅ React 应用渲染完成')