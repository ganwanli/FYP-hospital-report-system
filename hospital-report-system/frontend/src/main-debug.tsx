import React from 'react'
import ReactDOM from 'react-dom/client'

console.log('Starting React app...')

const App = () => {
  console.log('App component rendering...')
  
  return (
    <div style={{ 
      padding: '50px',
      fontSize: '24px',
      color: '#1890ff',
      textAlign: 'center',
      backgroundColor: '#f0f0f0'
    }}>
      <h1>🎉 医院报表管理系统</h1>
      <p>React 应用正在运行！</p>
      <p>时间: {new Date().toLocaleString()}</p>
      <div style={{ marginTop: '30px' }}>
        <button onClick={() => alert('点击成功！')}>
          测试按钮
        </button>
      </div>
    </div>
  )
}

console.log('Creating React root...')
const root = ReactDOM.createRoot(document.getElementById('root')!)
console.log('Rendering app...')
root.render(<App />)
console.log('App rendered!')