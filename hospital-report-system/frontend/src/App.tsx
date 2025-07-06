import React, { useEffect } from 'react'
import { ConfigProvider, theme } from 'antd'
import { useAppStore } from './stores/appStore'
import AppRouter from './routes'
import GlobalLoading from './components/common/Loading/GlobalLoading'

const App: React.FC = () => {
  const { loading, theme: appTheme } = useAppStore()

  // 监听主题变化，动态更新ConfigProvider
  useEffect(() => {
    // 可以在这里添加初始化逻辑
  }, [])

  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: appTheme.primaryColor,
          borderRadius: appTheme.borderRadius,
          colorBgBase: appTheme.colorBgBase,
          colorTextBase: appTheme.colorTextBase,
        },
        algorithm: appTheme.algorithm === 'dark' ? theme.darkAlgorithm : theme.defaultAlgorithm,
      }}
    >
      <AppRouter />
      {loading && <GlobalLoading />}
    </ConfigProvider>
  )
}

export default App