import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Input, Button, Table, Space, Tag, Modal, Form, Select, message } from 'antd';
import { SearchOutlined, PlusOutlined, EyeOutlined, EditOutlined, LinkOutlined } from '@ant-design/icons';
import LineageGraph from '@/components/LineageGraph';
import RelationshipEditor from '@/components/RelationshipEditor';
import { searchNodes, createOrUpdateNode, getNodeStatistics } from '@/services/lineage';

const { Search } = Input;
const { Option } = Select;

interface LineageNode {
  id: number;
  nodeId: string;
  nodeName: string;
  displayName: string;
  nodeType: string;
  nodeCategory: string;
  tableName: string;
  columnName: string;
  dataType: string;
  businessMeaning: string;
  systemSource: string;
  ownerUser: string;
  ownerDepartment: string;
  criticalityLevel: string;
  lastAccessTime: string;
  status: number;
}

const LineageManagement: React.FC = () => {
  const [nodes, setNodes] = useState<LineageNode[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState({
    keyword: '',
    nodeType: '',
    systemSource: '',
  });
  const [selectedNodeId, setSelectedNodeId] = useState<string>('');
  const [modalVisible, setModalVisible] = useState(false);
  const [editingNode, setEditingNode] = useState<LineageNode | null>(null);
  const [statistics, setStatistics] = useState<any>({});
  const [form] = Form.useForm();
  const [relationEditorVisible, setRelationEditorVisible] = useState(false);
  const [nodeOptionsForRelation, setNodeOptionsForRelation] = useState<any[]>([]);

  const columns = [
    {
      title: '节点ID',
      dataIndex: 'nodeId',
      key: 'nodeId',
      width: 200,
      ellipsis: true,
    },
    {
      title: '节点名称',
      dataIndex: 'nodeName',
      key: 'nodeName',
      width: 150,
    },
    {
      title: '显示名称',
      dataIndex: 'displayName',
      key: 'displayName',
      width: 150,
    },
    {
      title: '类型',
      dataIndex: 'nodeType',
      key: 'nodeType',
      width: 100,
      render: (type: string) => (
        <Tag color={type === 'TABLE' ? 'blue' : type === 'COLUMN' ? 'green' : 'orange'}>
          {type}
        </Tag>
      ),
    },
    {
      title: '表名',
      dataIndex: 'tableName',
      key: 'tableName',
      width: 120,
    },
    {
      title: '字段名',
      dataIndex: 'columnName',
      key: 'columnName',
      width: 120,
    },
    {
      title: '数据类型',
      dataIndex: 'dataType',
      key: 'dataType',
      width: 100,
    },
    {
      title: '系统来源',
      dataIndex: 'systemSource',
      key: 'systemSource',
      width: 120,
    },
    {
      title: '重要性',
      dataIndex: 'criticalityLevel',
      key: 'criticalityLevel',
      width: 100,
      render: (level: string) => (
        <Tag color={level === 'CRITICAL' ? 'red' : level === 'HIGH' ? 'orange' : 'blue'}>
          {level}
        </Tag>
      ),
    },
    {
      title: '最后访问',
      dataIndex: 'lastAccessTime',
      key: 'lastAccessTime',
      width: 150,
      render: (time: string) => time ? new Date(time).toLocaleDateString() : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (record: LineageNode) => (
        <Space size="small">
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => handleViewLineage(record.nodeId)}
          >
            血缘
          </Button>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEditNode(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            icon={<LinkOutlined />}
            onClick={() => handleCreateRelation(record)}
          >
            建立关系
          </Button>
        </Space>
      ),
    },
  ];

  const loadNodes = async () => {
    setLoading(true);
    try {
      const data = await searchNodes(searchParams);
      setNodes(data);
    } catch (error) {
      message.error('加载节点列表失败');
    } finally {
      setLoading(false);
    }
  };

  const loadStatistics = async () => {
    try {
      const stats = await getNodeStatistics();
      setStatistics(stats);
    } catch (error) {
      console.error('Load statistics error:', error);
    }
  };

  const handleSearch = (keyword: string) => {
    setSearchParams(prev => ({ ...prev, keyword }));
  };

  const handleFilterChange = (field: string, value: string) => {
    setSearchParams(prev => ({ ...prev, [field]: value }));
  };

  const handleViewLineage = (nodeId: string) => {
    setSelectedNodeId(nodeId);
  };

  const handleEditNode = (node: LineageNode) => {
    setEditingNode(node);
    form.setFieldsValue(node);
    setModalVisible(true);
  };

  const handleAddNode = () => {
    setEditingNode(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields();
      const nodeData = editingNode ? { ...editingNode, ...values } : values;
      
      await createOrUpdateNode(nodeData);
      message.success(editingNode ? '更新节点成功' : '创建节点成功');
      setModalVisible(false);
      loadNodes();
    } catch (error) {
      message.error('保存节点失败');
    }
  };

  const handleModalCancel = () => {
    setModalVisible(false);
    setEditingNode(null);
    form.resetFields();
  };

  const handleCreateRelation = async (node: LineageNode) => {
    try {
      // 加载所有节点作为关系编辑器的选项
      const allNodes = await searchNodes({});
      const options = allNodes.map((n: LineageNode) => ({
        value: n.nodeId,
        label: `${n.nodeName} (${n.tableName || ''}.${n.columnName || ''})`,
      }));
      setNodeOptionsForRelation(options);
      setRelationEditorVisible(true);
    } catch (error) {
      message.error('加载节点选项失败');
    }
  };

  const handleRelationSave = () => {
    setRelationEditorVisible(false);
    message.success('血缘关系保存成功');
    // 可以在这里刷新相关数据
  };

  useEffect(() => {
    loadNodes();
  }, [searchParams]);

  useEffect(() => {
    loadStatistics();
  }, []);

  return (
    <div style={{ padding: '24px' }}>
      <Row gutter={[16, 16]}>
        {/* 统计卡片 */}
        <Col span={6}>
          <Card>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#1890ff' }}>
                {statistics.totalNodes || 0}
              </div>
              <div style={{ color: '#666' }}>总节点数</div>
            </div>
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#52c41a' }}>
                {statistics.activeNodes || 0}
              </div>
              <div style={{ color: '#666' }}>活跃节点</div>
            </div>
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#fa8c16' }}>
                {statistics.recentlyAccessedNodes || 0}
              </div>
              <div style={{ color: '#666' }}>近期访问</div>
            </div>
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#722ed1' }}>
                {statistics.ownershipRate ? `${statistics.ownershipRate.toFixed(1)}%` : '0%'}
              </div>
              <div style={{ color: '#666' }}>责任人覆盖率</div>
            </div>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: '16px' }}>
        {/* 左侧节点列表 */}
        <Col span={selectedNodeId ? 12 : 24}>
          <Card 
            title="数据血缘节点管理"
            extra={
              <Button type="primary" icon={<PlusOutlined />} onClick={handleAddNode}>
                新增节点
              </Button>
            }
          >
            <Space style={{ marginBottom: 16, width: '100%' }} direction="vertical">
              <Row gutter={[8, 8]}>
                <Col span={8}>
                  <Search
                    placeholder="搜索节点..."
                    allowClear
                    onSearch={handleSearch}
                    style={{ width: '100%' }}
                  />
                </Col>
                <Col span={4}>
                  <Select
                    placeholder="节点类型"
                    allowClear
                    style={{ width: '100%' }}
                    onChange={(value) => handleFilterChange('nodeType', value || '')}
                  >
                    <Option value="TABLE">表</Option>
                    <Option value="COLUMN">字段</Option>
                    <Option value="VIEW">视图</Option>
                    <Option value="PROCEDURE">存储过程</Option>
                  </Select>
                </Col>
                <Col span={4}>
                  <Select
                    placeholder="系统来源"
                    allowClear
                    style={{ width: '100%' }}
                    onChange={(value) => handleFilterChange('systemSource', value || '')}
                  >
                    <Option value="HIS">HIS系统</Option>
                    <Option value="LIS">LIS系统</Option>
                    <Option value="RIS">RIS系统</Option>
                    <Option value="PACS">PACS系统</Option>
                  </Select>
                </Col>
                <Col span={8}>
                  <Button onClick={loadNodes}>刷新</Button>
                </Col>
              </Row>
            </Space>

            <Table
              columns={columns}
              dataSource={nodes}
              rowKey="id"
              loading={loading}
              size="small"
              scroll={{ x: 'max-content', y: 400 }}
              pagination={{
                total: nodes.length,
                pageSize: 20,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total) => `共 ${total} 条记录`,
              }}
            />
          </Card>
        </Col>

        {/* 右侧血缘关系图 */}
        {selectedNodeId && (
          <Col span={12}>
            <Card 
              title={`血缘关系图 - ${selectedNodeId}`}
              extra={
                <Button onClick={() => setSelectedNodeId('')}>
                  关闭
                </Button>
              }
            >
              <LineageGraph 
                nodeId={selectedNodeId}
                maxDepth={3}
                direction="ALL"
                onNodeSelect={(nodeId) => console.log('Selected node:', nodeId)}
              />
            </Card>
          </Col>
        )}
      </Row>

      {/* 节点编辑模态框 */}
      <Modal
        title={editingNode ? '编辑节点' : '新增节点'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={handleModalCancel}
        width={800}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            nodeType: 'COLUMN',
            nodeCategory: 'DATA_ELEMENT',
            criticalityLevel: 'MEDIUM',
            status: 1,
          }}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="nodeId"
                label="节点ID"
                rules={[{ required: true, message: '请输入节点ID' }]}
              >
                <Input placeholder="唯一标识符" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="nodeName"
                label="节点名称"
                rules={[{ required: true, message: '请输入节点名称' }]}
              >
                <Input placeholder="节点名称" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="displayName" label="显示名称">
                <Input placeholder="显示名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="nodeType"
                label="节点类型"
                rules={[{ required: true, message: '请选择节点类型' }]}
              >
                <Select>
                  <Option value="TABLE">表</Option>
                  <Option value="COLUMN">字段</Option>
                  <Option value="VIEW">视图</Option>
                  <Option value="PROCEDURE">存储过程</Option>
                  <Option value="FUNCTION">函数</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="tableName" label="表名">
                <Input placeholder="数据表名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="columnName" label="字段名">
                <Input placeholder="字段名称" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="dataType" label="数据类型">
                <Input placeholder="VARCHAR、INT等" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="systemSource" label="系统来源">
                <Select>
                  <Option value="HIS">HIS系统</Option>
                  <Option value="LIS">LIS系统</Option>
                  <Option value="RIS">RIS系统</Option>
                  <Option value="PACS">PACS系统</Option>
                  <Option value="EMR">电子病历</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="ownerUser" label="责任人">
                <Input placeholder="数据责任人" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="ownerDepartment" label="责任部门">
                <Input placeholder="责任部门" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="criticalityLevel" label="重要性级别">
                <Select>
                  <Option value="LOW">低</Option>
                  <Option value="MEDIUM">中</Option>
                  <Option value="HIGH">高</Option>
                  <Option value="CRITICAL">关键</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="status" label="状态">
                <Select>
                  <Option value={1}>启用</Option>
                  <Option value={0}>禁用</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="businessMeaning" label="业务含义">
            <Input.TextArea rows={3} placeholder="描述该节点的业务含义和用途" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 关系编辑器 */}
      <RelationshipEditor
        visible={relationEditorVisible}
        onClose={() => setRelationEditorVisible(false)}
        onSave={handleRelationSave}
        sourceNodeOptions={nodeOptionsForRelation}
        targetNodeOptions={nodeOptionsForRelation}
      />
    </div>
  );
};

export default LineageManagement;