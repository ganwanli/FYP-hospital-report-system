import React, { useState } from 'react'
import { Modal, Button, Result, Spin, Typography, Space } from 'antd'
import { PlayCircleOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons'
import { dataSourceAPI } from '@/services'
import type { DataSource } from '@/types'

const { Text } = Typography

interface ConnectionTestProps {
  visible: boolean
  dataSource: DataSource
  onCancel: () => void
}

const ConnectionTest: React.FC<ConnectionTestProps> = ({
  visible,
  dataSource,
  onCancel
}) => {
  const [testing, setTesting] = useState(false)
  const [testResult, setTestResult] = useState<boolean | null>(null)
  const [startTime, setStartTime] = useState<number | null>(null)
  const [endTime, setEndTime] = useState<number | null>(null)

  const handleTest = async () => {
    setTesting(true)
    setTestResult(null)
    setStartTime(Date.now())
    setEndTime(null)
    
    try {
      const response = await dataSourceAPI.testConnection(dataSource.id)
      const result = response.code === 200 && response.data
      setTestResult(result)
      setEndTime(Date.now())
    } catch (error) {
      setTestResult(false)
      setEndTime(Date.now())
    } finally {
      setTesting(false)
    }
  }

  const getTestDuration = () => {
    if (startTime && endTime) {
      return endTime - startTime
    }
    return 0
  }

  const renderTestResult = () => {
    if (testing) {
      return (
        <div style={{ textAlign: 'center', padding: 40 }}>
          <Spin size="large" />
          <div style={{ marginTop: 16 }}>
            <Text>正在测试连接...</Text>
          </div>
        </div>
      )
    }

    if (testResult === null) {
      return (
        <div style={{ textAlign: 'center', padding: 40 }}>
          <Text type="secondary">点击下方按钮开始测试连接</Text>
        </div>
      )
    }

    return (
      <Result
        status={testResult ? 'success' : 'error'}
        title={testResult ? '连接测试成功' : '连接测试失败'}
        subTitle={
          <Space direction="vertical">
            <Text>数据源: {dataSource.datasourceName}</Text>
            <Text>JDBC URL: {dataSource.jdbcUrl}</Text>
            <Text>用户名: {dataSource.username}</Text>
            {endTime && (
              <Text type="secondary">耗时: {getTestDuration()}ms</Text>
            )}
          </Space>
        }
        icon={testResult ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
      />
    )
  }

  return (
    <Modal
      title={`连接测试 - ${dataSource.datasourceName}`}
      open={visible}
      onCancel={onCancel}
      width={600}
      footer={[
        <Button key="test" type="primary" icon={<PlayCircleOutlined />} loading={testing} onClick={handleTest}>
          {testing ? '测试中...' : '开始测试'}
        </Button>,
        <Button key="close" onClick={onCancel}>
          关闭
        </Button>
      ]}
    >
      <div style={{ minHeight: 200 }}>
        {renderTestResult()}
      </div>
      
      <div style={{ marginTop: 16, padding: 16, background: '#fafafa', borderRadius: 6 }}>
        <h4>连接信息</h4>
        <Space direction="vertical" style={{ width: '100%' }}>
          <div>
            <Text strong>数据库类型: </Text>
            <Text>{dataSource.databaseType}</Text>
          </div>
          <div>
            <Text strong>驱动类: </Text>
            <Text code>{dataSource.driverClassName}</Text>
          </div>
          <div>
            <Text strong>JDBC URL: </Text>
            <Text code>{dataSource.jdbcUrl}</Text>
          </div>
          <div>
            <Text strong>用户名: </Text>
            <Text>{dataSource.username}</Text>
          </div>
          <div>
            <Text strong>验证查询: </Text>
            <Text code>{dataSource.validationQuery || 'SELECT 1'}</Text>
          </div>
        </Space>
      </div>
    </Modal>
  )
}

export default ConnectionTest