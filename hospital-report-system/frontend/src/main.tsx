import React from 'react'
import ReactDOM from 'react-dom/client'
import { ConfigProvider, App as AntdApp } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import App from './App'
import '@/styles/index.css'

// 设置dayjs中文
dayjs.locale('zh-cn')

// Ant Design 全局配置
const antdConfig = {
  locale: zhCN,
  theme: {
    token: {
      colorPrimary: '#1890ff',
      borderRadius: 6,
      colorBgBase: '#ffffff',
    },
    algorithm: undefined, // 可以设置为 theme.darkAlgorithm
  },
  componentSize: 'middle' as const,
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ConfigProvider {...antdConfig}>
      <AntdApp>
        <App />
      </AntdApp>
    </ConfigProvider>
  </React.StrictMode>
)