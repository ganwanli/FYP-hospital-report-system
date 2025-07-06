import React from 'react'
import ReactDOM from 'react-dom/client'

const SimpleApp = () => {
  return (
    <div style={{ padding: '20px', fontSize: '18px', color: 'blue' }}>
      <h1>测试页面</h1>
      <p>如果你能看到这个页面，说明React正在工作！</p>
      <p>时间: {new Date().toLocaleString()}</p>
    </div>
  )
}

ReactDOM.createRoot(document.getElementById('root')!).render(<SimpleApp />)