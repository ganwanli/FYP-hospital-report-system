import React, { useState, useEffect } from 'react'
import { Modal, Form, Input, Select, InputNumber, Switch, Row, Col, message, Button, Space } from 'antd'
import { PlayCircleOutlined } from '@ant-design/icons'
import { dataSourceAPI } from '@/services'
import type { DataSource } from '@/types'

const { Option } = Select
const { TextArea } = Input

interface DataSourceFormProps {
  visible: boolean
  dataSource: DataSource | null
  onCancel: () => void
  onSuccess: () => void
}

const DataSourceForm: React.FC<DataSourceFormProps> = ({
  visible,
  dataSource,
  onCancel,
  onSuccess
}) => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [testLoading, setTestLoading] = useState(false)
  const [databaseTypes, setDatabaseTypes] = useState<string[]>([])

  useEffect(() => {
    if (visible) {
      fetchDatabaseTypes()
      if (dataSource) {
        form.setFieldsValue({
          ...dataSource,
          password: '******' // 隐藏密码
        })
      } else {
        form.resetFields()
        form.setFieldsValue({
          initialSize: 5,
          minIdle: 5,
          maxActive: 20,
          maxWait: 60000,
          connectionTimeout: 30000,
          idleTimeout: 600000,
          maxLifetime: 1800000,
          leakDetectionThreshold: 60000,
          testWhileIdle: true,
          testOnBorrow: false,
          testOnReturn: false,
          isDefault: false
        })
      }
    }
  }, [visible, dataSource])

  const fetchDatabaseTypes = async () => {
    try {
      const response = await dataSourceAPI.getSupportedDatabaseTypes()
      if (response.code === 200) {
        setDatabaseTypes(response.data)
      }
    } catch (error) {
      console.error('获取数据库类型失败:', error)
    }
  }

  const handleDatabaseTypeChange = async (type: string) => {
    try {
      const response = await dataSourceAPI.getDriverClassName(type)
      if (response.code === 200) {
        form.setFieldValue('driverClassName', response.data)
        
        // 设置默认验证查询
        const validationQueries: Record<string, string> = {
          'MySQL': 'SELECT 1',
          'PostgreSQL': 'SELECT 1',
          'Oracle': 'SELECT 1 FROM DUAL',
          'SQL Server': 'SELECT 1',
          'H2': 'SELECT 1'
        }
        form.setFieldValue('validationQuery', validationQueries[type] || 'SELECT 1')
      }
    } catch (error) {
      console.error('获取驱动类名失败:', error)
    }
  }

  const handleTest = async () => {
    try {
      const values = await form.validateFields([
        'databaseType', 'driverClassName', 'jdbcUrl', 'username', 'password'
      ])
      
      if (values.password === '******') {
        message.warning('请重新输入密码进行测试')
        return
      }
      
      setTestLoading(true)
      const response = await dataSourceAPI.testConnection(values)
      if (response.code === 200) {
        if (response.data) {
          message.success('连接测试成功')
        } else {
          message.error('连接测试失败')
        }
      } else {
        message.error(response.message || '连接测试失败')
      }
    } catch (error) {
      console.error('连接测试失败:', error)
      message.error('连接测试失败')
    } finally {
      setTestLoading(false)
    }
  }

  const handleOk = async () => {
    try {
      const values = await form.validateFields()
      
      // 如果是编辑且密码未变更，则删除密码字段
      if (dataSource && values.password === '******') {
        delete values.password
      }
      
      setLoading(true)
      
      if (dataSource) {
        const response = await dataSourceAPI.updateDataSource(dataSource.id, values)
        if (response.code === 200) {
          message.success('更新成功')
          onSuccess()
        } else {
          message.error(response.message)
        }
      } else {
        const response = await dataSourceAPI.createDataSource(values)
        if (response.code === 200) {
          message.success('创建成功')
          onSuccess()
        } else {
          message.error(response.message)
        }
      }
    } catch (error) {
      console.error('表单验证失败:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal
      title={dataSource ? '编辑数据源' : '新增数据源'}
      open={visible}
      onOk={handleOk}
      onCancel={onCancel}
      width={800}
      confirmLoading={loading}
      destroyOnClose
    >
      <Form form={form} layout="vertical">
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              name="datasourceName"
              label="数据源名称"
              rules={[
                { required: true, message: '请输入数据源名称' },
                { max: 100, message: '数据源名称不能超过100个字符' }
              ]}
            >
              <Input placeholder="请输入数据源名称" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              name="datasourceCode"
              label="数据源编码"
              rules={[
                { required: true, message: '请输入数据源编码' },
                { max: 50, message: '数据源编码不能超过50个字符' },
                { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '编码只能包含字母、数字和下划线，且以字母开头' }
              ]}
            >
              <Input placeholder="请输入数据源编码" disabled={!!dataSource} />
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              name="databaseType"
              label="数据库类型"
              rules={[{ required: true, message: '请选择数据库类型' }]}
            >
              <Select 
                placeholder="请选择数据库类型"
                onChange={handleDatabaseTypeChange}
              >
                {databaseTypes.map(type => (
                  <Option key={type} value={type}>{type}</Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              name="driverClassName"
              label="驱动类名"
              rules={[{ required: true, message: '请输入驱动类名' }]}
            >
              <Input placeholder="请输入驱动类名" />
            </Form.Item>
          </Col>
        </Row>

        <Form.Item
          name="jdbcUrl"
          label="JDBC URL"
          rules={[
            { required: true, message: '请输入JDBC URL' },
            { max: 500, message: 'JDBC URL不能超过500个字符' }
          ]}
        >
          <Input placeholder="请输入JDBC URL，如：jdbc:mysql://localhost:3306/database" />
        </Form.Item>

        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              name="username"
              label="用户名"
              rules={[
                { required: true, message: '请输入用户名' },
                { max: 100, message: '用户名不能超过100个字符' }
              ]}
            >
              <Input placeholder="请输入用户名" />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              name="password"
              label="密码"
              rules={[{ required: !dataSource, message: '请输入密码' }]}
            >
              <Input.Password placeholder="请输入密码" />
            </Form.Item>
          </Col>
        </Row>

        <Space style={{ marginBottom: 16 }}>
          <Button
            type="dashed"
            icon={<PlayCircleOutlined />}
            loading={testLoading}
            onClick={handleTest}
          >
            测试连接
          </Button>
        </Space>

        {/* 连接池配置 */}
        <div style={{ background: '#fafafa', padding: 16, marginBottom: 16, borderRadius: 6 }}>
          <h4>连接池配置</h4>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="initialSize" label="初始连接数">
                <InputNumber min={1} max={100} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="minIdle" label="最小空闲连接">
                <InputNumber min={1} max={100} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="maxActive" label="最大活跃连接">
                <InputNumber min={1} max={1000} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="maxWait" label="最大等待时间(ms)">
                <InputNumber min={1000} max={300000} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="connectionTimeout" label="连接超时(ms)">
                <InputNumber min={1000} max={60000} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="idleTimeout" label="空闲超时(ms)">
                <InputNumber min={60000} max={1800000} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="maxLifetime" label="连接最大生存时间(ms)">
                <InputNumber min={300000} max={3600000} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="leakDetectionThreshold" label="连接泄漏检测阈值(ms)">
                <InputNumber min={0} max={300000} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="validationQuery" label="验证查询">
                <Input placeholder="如：SELECT 1" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="testWhileIdle" label="空闲时测试" valuePropName="checked">
                <Switch />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="testOnBorrow" label="借用时测试" valuePropName="checked">
                <Switch />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="testOnReturn" label="归还时测试" valuePropName="checked">
                <Switch />
              </Form.Item>
            </Col>
          </Row>
        </div>

        <Row gutter={16}>
          <Col span={12}>
            <Form.Item name="isDefault" label="设为默认数据源" valuePropName="checked">
              <Switch />
            </Form.Item>
          </Col>
        </Row>

        <Form.Item
          name="description"
          label="描述"
          rules={[{ max: 500, message: '描述不能超过500个字符' }]}
        >
          <TextArea placeholder="请输入描述信息" rows={3} />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default DataSourceForm