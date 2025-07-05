import React from 'react'
import { Spin } from 'antd'
import './index.css'

interface GlobalLoadingProps {
  tip?: string
}

const GlobalLoading: React.FC<GlobalLoadingProps> = ({ 
  tip = '加载中...' 
}) => {
  return (
    <div className="global-loading">
      <div className="global-loading-backdrop" />
      <div className="global-loading-content">
        <Spin size="large" tip={tip} />
      </div>
    </div>
  )
}

export default GlobalLoading