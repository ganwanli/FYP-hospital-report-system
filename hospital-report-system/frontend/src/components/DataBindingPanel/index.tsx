import React, { useState, useEffect } from 'react';
import {
  Card,
  Form,
  Input,
  Select,
  Button,
  Space,
  Typography,
  Divider,
  Modal,
  Table,
  message,
  Tabs,
  Switch,
  InputNumber,
  Collapse,
  Tree,
  Tag,
} from 'antd';
import {
  DatabaseOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  ReloadOutlined,
  LinkOutlined,
  ApiOutlined,
  FileTextOutlined,
} from '@ant-design/icons';
import { ReportComponent, ReportDataSource, reportApi } from '../../services/report';

const { Panel } = Collapse;
const { Text } = Typography;
const { Option } = Select;
const { TextArea } = Input;
const { TabPane } = Tabs;

interface DataBindingPanelProps {
  selectedComponent: ReportComponent | null;
  dataSources: ReportDataSource[];
  onDataSourceAdd: (dataSource: Partial<ReportDataSource>) => void;
  onDataSourceUpdate: (dataSourceId: number, updates: Partial<ReportDataSource>) => void;
  onDataSourceDelete: (dataSourceId: number) => void;
  onComponentUpdate: (componentId: number, updates: Partial<ReportComponent>) => void;
}

const DataBindingPanel: React.FC<DataBindingPanelProps> = ({
  selectedComponent,
  dataSources,
  onDataSourceAdd,
  onDataSourceUpdate,
  onDataSourceDelete,
  onComponentUpdate,
}) => {
  const [form] = Form.useForm();
  const [dataSourceForm] = Form.useForm();
  
  const [activeKey, setActiveKey] = useState<string[]>(['binding', 'datasources']);
  const [modalVisible, setModalVisible] = useState(false);
  const [previewVisible, setPreviewVisible] = useState(false);
  const [editingDataSource, setEditingDataSource] = useState<ReportDataSource | null>(null);
  const [previewData, setPreviewData] = useState<any>(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [testLoading, setTestLoading] = useState(false);

  useEffect(() => {
    if (selectedComponent) {
      const dataConfig = selectedComponent.dataConfig ? JSON.parse(selectedComponent.dataConfig) : {};
      form.setFieldsValue({
        dataSourceId: selectedComponent.dataSourceId,
        xField: dataConfig.xField || '',
        yField: dataConfig.yField || '',
        angleField: dataConfig.angleField || '',
        colorField: dataConfig.colorField || '',
        valueField: dataConfig.valueField || '',
        labelField: dataConfig.labelField || '',
        groupField: dataConfig.groupField || '',
        filterCondition: dataConfig.filterCondition || '',
        sortField: dataConfig.sortField || '',
        sortOrder: dataConfig.sortOrder || 'asc',
        limit: dataConfig.limit || 100,
      });
    }
  }, [selectedComponent, form]);

  const handleDataBindingChange = (changedFields: any, allFields: any) => {
    if (!selectedComponent) return;

    const values = form.getFieldsValue();
    const dataConfig = {
      xField: values.xField,
      yField: values.yField,
      angleField: values.angleField,
      colorField: values.colorField,
      valueField: values.valueField,
      labelField: values.labelField,
      groupField: values.groupField,
      filterCondition: values.filterCondition,
      sortField: values.sortField,
      sortOrder: values.sortOrder,
      limit: values.limit,
    };

    onComponentUpdate(selectedComponent.componentId, {
      dataSourceId: values.dataSourceId,
      dataConfig: JSON.stringify(dataConfig),
    });
  };

  const handleAddDataSource = () => {
    setEditingDataSource(null);
    dataSourceForm.resetFields();
    setModalVisible(true);
  };

  const handleEditDataSource = (dataSource: ReportDataSource) => {
    setEditingDataSource(dataSource);
    
    const connectionConfig = dataSource.connectionConfig ? JSON.parse(dataSource.connectionConfig) : {};
    const queryConfig = dataSource.queryConfig ? JSON.parse(dataSource.queryConfig) : {};
    const apiConfig = dataSource.apiConfig ? JSON.parse(dataSource.apiConfig) : {};
    
    dataSourceForm.setFieldsValue({
      sourceName: dataSource.sourceName,
      sourceType: dataSource.sourceType,
      // SQL config
      host: connectionConfig.host || '',
      port: connectionConfig.port || 3306,
      database: connectionConfig.database || '',
      username: connectionConfig.username || '',
      password: connectionConfig.password || '',
      sqlQuery: queryConfig.sql || '',
      // API config
      apiUrl: apiConfig.url || '',
      apiMethod: apiConfig.method || 'GET',
      apiHeaders: apiConfig.headers ? JSON.stringify(apiConfig.headers, null, 2) : '',
      apiParams: apiConfig.params ? JSON.stringify(apiConfig.params, null, 2) : '',
      // Static data
      staticData: dataSource.staticData || '',
      // Cache config
      cacheEnabled: dataSource.cacheEnabled,
      cacheDuration: dataSource.cacheDuration || 300,
      refreshInterval: dataSource.refreshInterval || 0,
    });
    setModalVisible(true);
  };

  const handleSaveDataSource = async () => {
    try {
      const values = await dataSourceForm.validateFields();
      
      const connectionConfig = {
        host: values.host,
        port: values.port,
        database: values.database,
        username: values.username,
        password: values.password,
      };
      
      const queryConfig = {
        sql: values.sqlQuery,
      };
      
      const apiConfig = {
        url: values.apiUrl,
        method: values.apiMethod,
        headers: values.apiHeaders ? JSON.parse(values.apiHeaders) : {},
        params: values.apiParams ? JSON.parse(values.apiParams) : {},
      };

      const dataSourceData: Partial<ReportDataSource> = {
        sourceName: values.sourceName,
        sourceType: values.sourceType,
        connectionConfig: JSON.stringify(connectionConfig),
        queryConfig: JSON.stringify(queryConfig),
        apiConfig: JSON.stringify(apiConfig),
        staticData: values.staticData,
        cacheEnabled: values.cacheEnabled,
        cacheDuration: values.cacheDuration,
        refreshInterval: values.refreshInterval,
      };

      if (editingDataSource) {
        onDataSourceUpdate(editingDataSource.dataSourceId, dataSourceData);
      } else {
        onDataSourceAdd(dataSourceData);
      }

      setModalVisible(false);
      message.success(editingDataSource ? '数据源更新成功' : '数据源创建成功');
    } catch (error) {
      console.error('Save data source error:', error);
    }
  };

  const handleTestDataSource = async () => {
    if (!editingDataSource?.dataSourceId) return;
    
    setTestLoading(true);
    try {
      const response = await reportApi.testDataSource(editingDataSource.dataSourceId);
      if (response.data.success) {
        message.success('数据源连接测试成功');
      } else {
        message.error(`连接测试失败: ${response.data.message}`);
      }
    } catch (error) {
      message.error('连接测试失败');
    } finally {
      setTestLoading(false);
    }
  };

  const handlePreviewDataSource = async (dataSource: ReportDataSource) => {
    setPreviewLoading(true);
    try {
      const response = await reportApi.previewDataSource(dataSource.dataSourceId, 50);
      setPreviewData(response.data);
      setPreviewVisible(true);
    } catch (error) {
      message.error('数据预览失败');
    } finally {
      setPreviewLoading(false);
    }
  };

  const handleDeleteDataSource = (dataSource: ReportDataSource) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除数据源 "${dataSource.sourceName}" 吗？`,
      onOk: () => {
        onDataSourceDelete(dataSource.dataSourceId);
        message.success('数据源删除成功');
      },
    });
  };

  const getDataSourceIcon = (type: string) => {
    switch (type) {
      case 'SQL':
        return <DatabaseOutlined />;
      case 'API':
        return <ApiOutlined />;
      case 'STATIC':
        return <FileTextOutlined />;
      default:
        return <DatabaseOutlined />;
    }
  };

  const renderDataBindingForm = () => {
    if (!selectedComponent) {
      return (
        <div style={{ textAlign: 'center', color: '#999', padding: '20px' }}>
          请选择一个组件
        </div>
      );
    }

    const componentType = selectedComponent.componentType;
    const selectedDataSource = dataSources.find(ds => ds.dataSourceId === selectedComponent.dataSourceId);

    return (
      <Space direction="vertical" style={{ width: '100%' }}>
        <Form.Item label="数据源" name="dataSourceId">
          <Select
            placeholder="请选择数据源"
            style={{ width: '100%' }}
            allowClear
          >
            {dataSources.map(ds => (
              <Option key={ds.dataSourceId} value={ds.dataSourceId}>
                <Space>
                  {getDataSourceIcon(ds.sourceType)}
                  {ds.sourceName}
                </Space>
              </Option>
            ))}
          </Select>
        </Form.Item>

        {selectedDataSource && (
          <>
            <Divider />
            <Text strong>字段映射</Text>
            
            {(componentType === 'bar-chart' || componentType === 'line-chart') && (
              <>
                <Form.Item label="X轴字段" name="xField">
                  <Input placeholder="请输入X轴字段名" />
                </Form.Item>
                <Form.Item label="Y轴字段" name="yField">
                  <Input placeholder="请输入Y轴字段名" />
                </Form.Item>
              </>
            )}
            
            {componentType === 'pie-chart' && (
              <>
                <Form.Item label="角度字段" name="angleField">
                  <Input placeholder="请输入角度字段名" />
                </Form.Item>
                <Form.Item label="分类字段" name="colorField">
                  <Input placeholder="请输入分类字段名" />
                </Form.Item>
              </>
            )}
            
            {componentType === 'table' && (
              <Form.Item label="显示字段" name="displayFields">
                <Select mode="tags" placeholder="请输入要显示的字段名">
                  {/* TODO: 从数据源schema获取字段列表 */}
                </Select>
              </Form.Item>
            )}

            <Divider />
            <Text strong>数据过滤</Text>
            
            <Form.Item label="过滤条件" name="filterCondition">
              <TextArea rows={2} placeholder="请输入SQL WHERE条件" />
            </Form.Item>
            
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
              <Form.Item label="排序字段" name="sortField">
                <Input placeholder="字段名" />
              </Form.Item>
              <Form.Item label="排序方式" name="sortOrder">
                <Select>
                  <Option value="asc">升序</Option>
                  <Option value="desc">降序</Option>
                </Select>
              </Form.Item>
            </div>
            
            <Form.Item label="数据限制" name="limit">
              <InputNumber min={1} max={10000} style={{ width: '100%' }} />
            </Form.Item>
          </>
        )}
      </Space>
    );
  };

  const renderDataSourceList = () => (
    <div style={{ marginTop: '12px' }}>
      <div style={{ marginBottom: '12px' }}>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={handleAddDataSource}
          size="small"
          block
        >
          添加数据源
        </Button>
      </div>
      
      <Space direction="vertical" style={{ width: '100%' }}>
        {dataSources.map(dataSource => (
          <Card
            key={dataSource.dataSourceId}
            size="small"
            style={{ width: '100%' }}
            bodyStyle={{ padding: '12px' }}
            title={
              <Space>
                {getDataSourceIcon(dataSource.sourceType)}
                <Text strong>{dataSource.sourceName}</Text>
                <Tag color={dataSource.isActive ? 'green' : 'red'}>
                  {dataSource.isActive ? '活跃' : '禁用'}
                </Tag>
              </Space>
            }
            extra={
              <Space>
                <Button
                  type="text"
                  icon={<EyeOutlined />}
                  size="small"
                  onClick={() => handlePreviewDataSource(dataSource)}
                  loading={previewLoading}
                />
                <Button
                  type="text"
                  icon={<EditOutlined />}
                  size="small"
                  onClick={() => handleEditDataSource(dataSource)}
                />
                <Button
                  type="text"
                  icon={<DeleteOutlined />}
                  size="small"
                  danger
                  onClick={() => handleDeleteDataSource(dataSource)}
                />
              </Space>
            }
          >
            <div style={{ fontSize: '12px', color: '#666' }}>
              <div>类型: {dataSource.sourceType}</div>
              {dataSource.errorMessage && (
                <div style={{ color: '#ff4d4f' }}>
                  错误: {dataSource.errorMessage}
                </div>
              )}
            </div>
          </Card>
        ))}
        
        {dataSources.length === 0 && (
          <div style={{ textAlign: 'center', color: '#999', padding: '20px' }}>
            暂无数据源
          </div>
        )}
      </Space>
    </div>
  );

  const renderDataSourceForm = () => (
    <Form form={dataSourceForm} layout="vertical" size="small">
      <Form.Item
        label="数据源名称"
        name="sourceName"
        rules={[{ required: true, message: '请输入数据源名称' }]}
      >
        <Input placeholder="请输入数据源名称" />
      </Form.Item>
      
      <Form.Item
        label="数据源类型"
        name="sourceType"
        rules={[{ required: true, message: '请选择数据源类型' }]}
      >
        <Select placeholder="请选择数据源类型">
          <Option value="SQL">SQL数据库</Option>
          <Option value="API">API接口</Option>
          <Option value="STATIC">静态数据</Option>
        </Select>
      </Form.Item>

      <Tabs defaultActiveKey="config">
        <TabPane tab="配置" key="config">
          <Form.Item noStyle shouldUpdate={(prev, curr) => prev.sourceType !== curr.sourceType}>
            {({ getFieldValue }) => {
              const sourceType = getFieldValue('sourceType');
              
              if (sourceType === 'SQL') {
                return (
                  <>
                    <Form.Item label="主机地址" name="host" rules={[{ required: true }]}>
                      <Input placeholder="localhost" />
                    </Form.Item>
                    <Form.Item label="端口" name="port" rules={[{ required: true }]}>
                      <InputNumber min={1} max={65535} style={{ width: '100%' }} />
                    </Form.Item>
                    <Form.Item label="数据库名" name="database" rules={[{ required: true }]}>
                      <Input placeholder="database_name" />
                    </Form.Item>
                    <Form.Item label="用户名" name="username" rules={[{ required: true }]}>
                      <Input placeholder="username" />
                    </Form.Item>
                    <Form.Item label="密码" name="password" rules={[{ required: true }]}>
                      <Input.Password placeholder="password" />
                    </Form.Item>
                    <Form.Item label="SQL查询" name="sqlQuery" rules={[{ required: true }]}>
                      <TextArea rows={4} placeholder="SELECT * FROM table_name" />
                    </Form.Item>
                  </>
                );
              }
              
              if (sourceType === 'API') {
                return (
                  <>
                    <Form.Item label="API地址" name="apiUrl" rules={[{ required: true }]}>
                      <Input placeholder="https://api.example.com/data" />
                    </Form.Item>
                    <Form.Item label="请求方法" name="apiMethod">
                      <Select>
                        <Option value="GET">GET</Option>
                        <Option value="POST">POST</Option>
                        <Option value="PUT">PUT</Option>
                        <Option value="DELETE">DELETE</Option>
                      </Select>
                    </Form.Item>
                    <Form.Item label="请求头" name="apiHeaders">
                      <TextArea rows={3} placeholder='{"Content-Type": "application/json"}' />
                    </Form.Item>
                    <Form.Item label="请求参数" name="apiParams">
                      <TextArea rows={3} placeholder='{"param1": "value1"}' />
                    </Form.Item>
                  </>
                );
              }
              
              if (sourceType === 'STATIC') {
                return (
                  <Form.Item label="静态数据" name="staticData" rules={[{ required: true }]}>
                    <TextArea
                      rows={8}
                      placeholder='[{"name": "张三", "age": 25}, {"name": "李四", "age": 30}]'
                    />
                  </Form.Item>
                );
              }
              
              return null;
            }}
          </Form.Item>
        </TabPane>
        
        <TabPane tab="缓存" key="cache">
          <Form.Item label="启用缓存" name="cacheEnabled" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="缓存时长(秒)" name="cacheDuration">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="自动刷新间隔(秒)" name="refreshInterval">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
        </TabPane>
      </Tabs>
    </Form>
  );

  return (
    <Card
      size="small"
      title={
        <Space>
          <DatabaseOutlined />
          <Text strong>数据绑定</Text>
        </Space>
      }
      style={{ margin: '8px', height: 'calc(100% - 16px)' }}
      bodyStyle={{ padding: '12px', height: 'calc(100% - 57px)', overflow: 'auto' }}
    >
      <Collapse
        activeKey={activeKey}
        onChange={setActiveKey}
        ghost
        size="small"
      >
        <Panel
          header={
            <Space>
              <LinkOutlined />
              <Text strong>数据绑定</Text>
            </Space>
          }
          key="binding"
        >
          <Form
            form={form}
            layout="vertical"
            size="small"
            onValuesChange={handleDataBindingChange}
          >
            {renderDataBindingForm()}
          </Form>
        </Panel>
        
        <Panel
          header={
            <Space>
              <DatabaseOutlined />
              <Text strong>数据源管理</Text>
            </Space>
          }
          key="datasources"
        >
          {renderDataSourceList()}
        </Panel>
      </Collapse>

      {/* 数据源配置模态框 */}
      <Modal
        title={editingDataSource ? '编辑数据源' : '新建数据源'}
        open={modalVisible}
        onOk={handleSaveDataSource}
        onCancel={() => setModalVisible(false)}
        width={600}
        footer={[
          <Button key="cancel" onClick={() => setModalVisible(false)}>
            取消
          </Button>,
          editingDataSource && (
            <Button
              key="test"
              icon={<ReloadOutlined />}
              loading={testLoading}
              onClick={handleTestDataSource}
            >
              测试连接
            </Button>
          ),
          <Button key="save" type="primary" onClick={handleSaveDataSource}>
            保存
          </Button>,
        ]}
      >
        {renderDataSourceForm()}
      </Modal>

      {/* 数据预览模态框 */}
      <Modal
        title="数据预览"
        open={previewVisible}
        onCancel={() => setPreviewVisible(false)}
        width={800}
        footer={[
          <Button key="close" onClick={() => setPreviewVisible(false)}>
            关闭
          </Button>,
        ]}
      >
        {previewData && (
          <Table
            dataSource={previewData.records || []}
            columns={previewData.columns || []}
            size="small"
            scroll={{ x: true, y: 400 }}
            pagination={{ pageSize: 20 }}
          />
        )}
      </Modal>
    </Card>
  );
};

export default DataBindingPanel;