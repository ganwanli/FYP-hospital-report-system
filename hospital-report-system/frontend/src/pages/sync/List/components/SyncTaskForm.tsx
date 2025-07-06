import React, { useState, useEffect } from 'react'
import {
  Modal,
  Form,
  Input,
  Select,
  Switch,
  InputNumber,
  Row,
  Col,
  Tabs,
  Card,
  message,
  Button,
  Space,
  Typography,
  Divider
} from 'antd'
import { InfoCircleOutlined, TestOutlined } from '@ant-design/icons'
import { syncAPI, dataSourceAPI } from '@/services'
import type { SyncTask } from '@/services'

const { Option } = Select
const { TextArea } = Input
const { TabPane } = Tabs
const { Text } = Typography

interface SyncTaskFormProps {
  visible: boolean
  task: SyncTask | null
  dataSources: any[]
  onSubmit: (values: SyncTask) => void
  onCancel: () => void
}

const SyncTaskForm: React.FC<SyncTaskFormProps> = ({
  visible,
  task,
  dataSources,
  onSubmit,
  onCancel
}) => {
  const [form] = Form.useForm()
  const [testingConnection, setTestingConnection] = useState(false)
  const [sourceTables, setSourceTables] = useState<string[]>([])
  const [targetTables, setTargetTables] = useState<string[]>([])

  useEffect(() => {
    if (visible && task) {
      form.setFieldsValue({
        ...task,
        mappingConfig: task.mappingConfig ? JSON.parse(task.mappingConfig) : {}
      })
    } else if (visible) {
      form.resetFields()
      form.setFieldsValue({
        taskType: 'SCHEDULED',
        syncType: 'TABLE',
        syncMode: 'INCREMENTAL',
        batchSize: 1000,
        timeoutSeconds: 300,
        retryTimes: 3,
        retryInterval: 30,
        enableTransaction: true,
        parallelThreads: 1,
        isEnabled: true
      })
    }
  }, [visible, task, form])

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      
      // 处理字段映射配置
      if (values.mappingConfig && typeof values.mappingConfig === 'object') {
        values.mappingConfig = JSON.stringify(values.mappingConfig)
      }
      
      onSubmit(values)
    } catch (error) {
      console.error('表单验证失败:', error)
    }
  }

  const handleTestConnection = async () => {
    const sourceDatasourceId = form.getFieldValue('sourceDatasourceId')
    const targetDatasourceId = form.getFieldValue('targetDatasourceId')
    
    if (!sourceDatasourceId || !targetDatasourceId) {
      message.warning('请先选择源数据源和目标数据源')
      return
    }

    setTestingConnection(true)
    try {
      const [sourceTest, targetTest] = await Promise.all([
        dataSourceAPI.testDataSourceConnection(sourceDatasourceId),
        dataSourceAPI.testDataSourceConnection(targetDatasourceId)
      ])
      
      if (sourceTest.code === 200 && targetTest.code === 200) {
        message.success('数据源连接测试成功')
      } else {
        message.error('数据源连接测试失败')
      }
    } catch (error) {
      message.error('连接测试失败')
    } finally {
      setTestingConnection(false)
    }
  }

  const fetchTables = async (datasourceId: number, type: 'source' | 'target') => {
    try {
      const response = await dataSourceAPI.getDataSourceTables(datasourceId)
      if (response.code === 200) {
        if (type === 'source') {
          setSourceTables(response.data)
        } else {
          setTargetTables(response.data)
        }
      }
    } catch (error) {
      console.error(`获取${type === 'source' ? '源' : '目标'}表列表失败:`, error)
    }
  }

  return (
    <Modal
      title={task ? '编辑同步任务' : '新建同步任务'}
      open={visible}
      onCancel={onCancel}
      width={800}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          取消
        </Button>,
        <Button
          key="test"
          icon={<TestOutlined />}
          loading={testingConnection}
          onClick={handleTestConnection}
        >
          测试连接
        </Button>,
        <Button key="submit" type="primary" onClick={handleSubmit}>
          {task ? '更新' : '创建'}
        </Button>
      ]}
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          taskType: 'SCHEDULED',
          syncType: 'TABLE',
          syncMode: 'INCREMENTAL',
          batchSize: 1000,
          timeoutSeconds: 300,
          retryTimes: 3,
          retryInterval: 30,
          enableTransaction: true,
          parallelThreads: 1,
          isEnabled: true
        }}
      >
        <Tabs defaultActiveKey="basic">
          <TabPane tab="基本信息" key="basic">
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  label="任务名称"
                  name="taskName"
                  rules={[{ required: true, message: '请输入任务名称' }]}
                >
                  <Input placeholder="请输入任务名称" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label="任务编码"
                  name="taskCode"
                  rules={[{ required: true, message: '请输入任务编码' }]}
                >
                  <Input placeholder="请输入任务编码" />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="任务类型"
                  name="taskType"
                  rules={[{ required: true, message: '请选择任务类型' }]}
                >
                  <Select placeholder="请选择任务类型">
                    <Option value="SCHEDULED">定时任务</Option>
                    <Option value="MANUAL">手动任务</Option>
                  </Select>
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="同步类型"
                  name="syncType"
                  rules={[{ required: true, message: '请选择同步类型' }]}
                >
                  <Select placeholder="请选择同步类型">
                    <Option value="TABLE">表同步</Option>
                    <Option value="SQL">SQL同步</Option>
                  </Select>
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="同步模式"
                  name="syncMode"
                  rules={[{ required: true, message: '请选择同步模式' }]}
                >
                  <Select placeholder="请选择同步模式">
                    <Option value="FULL">全量同步</Option>
                    <Option value="INCREMENTAL">增量同步</Option>
                  </Select>
                </Form.Item>
              </Col>
            </Row>

            <Form.Item
              label="任务描述"
              name="description"
            >
              <TextArea rows={3} placeholder="请输入任务描述" />
            </Form.Item>

            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  label="启用状态"
                  name="isEnabled"
                  valuePropName="checked"
                >
                  <Switch checkedChildren="启用" unCheckedChildren="禁用" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label="Cron表达式"
                  name="cronExpression"
                  tooltip="定时任务的执行时间表达式"
                >
                  <Input placeholder="0 0 2 * * ?" />
                </Form.Item>
              </Col>
            </Row>
          </TabPane>

          <TabPane tab="数据源配置" key="datasource">
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  label="源数据源"
                  name="sourceDatasourceId"
                  rules={[{ required: true, message: '请选择源数据源' }]}
                >
                  <Select
                    placeholder="请选择源数据源"
                    onChange={(value) => fetchTables(value, 'source')}
                    showSearch
                    filterOption={(input, option) =>
                      option?.children?.toLowerCase().indexOf(input.toLowerCase()) >= 0
                    }
                  >
                    {dataSources.map(ds => (
                      <Option key={ds.id} value={ds.id}>
                        {ds.datasourceName} ({ds.databaseType})
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  label="目标数据源"
                  name="targetDatasourceId"
                  rules={[{ required: true, message: '请选择目标数据源' }]}
                >
                  <Select
                    placeholder="请选择目标数据源"
                    onChange={(value) => fetchTables(value, 'target')}
                    showSearch
                    filterOption={(input, option) =>
                      option?.children?.toLowerCase().indexOf(input.toLowerCase()) >= 0
                    }
                  >
                    {dataSources.map(ds => (
                      <Option key={ds.id} value={ds.id}>
                        {ds.datasourceName} ({ds.databaseType})
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
            </Row>

            <Form.Item dependencies={['syncType']}>
              {({ getFieldValue }) => {
                const syncType = getFieldValue('syncType')
                
                if (syncType === 'TABLE') {
                  return (
                    <Row gutter={16}>
                      <Col span={12}>
                        <Form.Item
                          label="源表名"
                          name="sourceTable"
                          rules={[{ required: true, message: '请输入源表名' }]}
                        >
                          <Select
                            placeholder="请选择或输入源表名"
                            showSearch
                            allowClear
                            mode="combobox"
                          >
                            {sourceTables.map(table => (
                              <Option key={table} value={table}>{table}</Option>
                            ))}
                          </Select>
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item
                          label="目标表名"
                          name="targetTable"
                          rules={[{ required: true, message: '请输入目标表名' }]}
                        >
                          <Select
                            placeholder="请选择或输入目标表名"
                            showSearch
                            allowClear
                            mode="combobox"
                          >
                            {targetTables.map(table => (
                              <Option key={table} value={table}>{table}</Option>
                            ))}
                          </Select>
                        </Form.Item>
                      </Col>
                    </Row>
                  )
                } else {
                  return (
                    <div>
                      <Form.Item
                        label="源查询SQL"
                        name="sourceSql"
                        rules={[{ required: true, message: '请输入源查询SQL' }]}
                      >
                        <TextArea rows={4} placeholder="SELECT * FROM table_name WHERE ..." />
                      </Form.Item>
                      <Form.Item
                        label="目标插入SQL"
                        name="targetSql"
                        tooltip="可选，默认使用INSERT语句"
                      >
                        <TextArea rows={4} placeholder="INSERT INTO table_name VALUES (...)" />
                      </Form.Item>
                    </div>
                  )
                }
              }}
            </Form.Item>
          </TabPane>

          <TabPane tab="增量配置" key="incremental">
            <Form.Item dependencies={['syncMode']}>
              {({ getFieldValue }) => {
                const syncMode = getFieldValue('syncMode')
                
                if (syncMode === 'INCREMENTAL') {
                  return (
                    <div>
                      <Row gutter={16}>
                        <Col span={12}>
                          <Form.Item
                            label="增量字段"
                            name="incrementalColumn"
                            rules={[{ required: true, message: '请输入增量字段' }]}
                          >
                            <Input placeholder="如: update_time, id" />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item
                            label="增量类型"
                            name="incrementalType"
                            rules={[{ required: true, message: '请选择增量类型' }]}
                          >
                            <Select placeholder="请选择增量类型">
                              <Option value="TIMESTAMP">时间戳</Option>
                              <Option value="NUMBER">数字</Option>
                            </Select>
                          </Form.Item>
                        </Col>
                      </Row>

                      <Form.Item
                        label="初始值"
                        name="lastSyncValue"
                        tooltip="增量同步的起始值，为空时从最小值开始"
                      >
                        <Input placeholder="如: 2023-01-01 00:00:00 或 0" />
                      </Form.Item>
                    </div>
                  )
                } else {
                  return (
                    <Card>
                      <Text type="secondary">
                        <InfoCircleOutlined /> 全量同步模式下无需配置增量参数
                      </Text>
                    </Card>
                  )
                }
              }}
            </Form.Item>

            <Divider />

            <Form.Item
              label="过滤条件"
              name="filterCondition"
              tooltip="WHERE子句中的额外过滤条件"
            >
              <TextArea rows={3} placeholder="如: status = 'active' AND category = 'A'" />
            </Form.Item>
          </TabPane>

          <TabPane tab="执行参数" key="execution">
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="批次大小"
                  name="batchSize"
                  rules={[{ required: true, message: '请输入批次大小' }]}
                >
                  <InputNumber min={1} max={10000} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="超时时间(秒)"
                  name="timeoutSeconds"
                  rules={[{ required: true, message: '请输入超时时间' }]}
                >
                  <InputNumber min={30} max={3600} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="并行线程数"
                  name="parallelThreads"
                  rules={[{ required: true, message: '请输入并行线程数' }]}
                >
                  <InputNumber min={1} max={10} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="重试次数"
                  name="retryTimes"
                  rules={[{ required: true, message: '请输入重试次数' }]}
                >
                  <InputNumber min={0} max={10} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="重试间隔(秒)"
                  name="retryInterval"
                  rules={[{ required: true, message: '请输入重试间隔' }]}
                >
                  <InputNumber min={1} max={300} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="启用事务"
                  name="enableTransaction"
                  valuePropName="checked"
                >
                  <Switch checkedChildren="是" unCheckedChildren="否" />
                </Form.Item>
              </Col>
            </Row>
          </TabPane>
        </Tabs>
      </Form>
    </Modal>
  )
}

export default SyncTaskForm