import React from 'react'
import ReactDOM from 'react-dom/client'
import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

// 简单的App组件
const SimpleApp = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>医院报表管理系统</h1>
      <p>系统正在加载中...</p>
      <p>时间: {new Date().toLocaleString()}</p>
    </div>
  )
}

// 设置dayjs中文
dayjs.locale('zh-cn')

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ConfigProvider locale={zhCN}>
      <SimpleApp />
    </ConfigProvider>
  </React.StrictMode>
)