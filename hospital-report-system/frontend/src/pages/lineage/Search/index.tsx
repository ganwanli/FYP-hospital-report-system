import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Input,
  Select,
  Table,
  Tag,
  Button,
  Space,
  Descriptions,
  Modal,
  Drawer,
  message,
} from 'antd';
import {
  SearchOutlined,
  EyeOutlined,
  NodeIndexOutlined,
  BranchesOutlined,
} from '@ant-design/icons';
import LineageGraph from '@/components/LineageGraph';
import { searchLineage, getLineagePath, getNodeDetail } from '@/services/lineage';

const { Search } = Input;
const { Option } = Select;

interface LineageSearchResult {
  id: number;
  sourceId: string;
  targetId: string;
  sourceName: string;
  targetName: string;
  relationType: string;
  confidence: number;
  verified: boolean;
}

const LineageSearch: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [searchResults, setSearchResults] = useState<LineageSearchResult[]>([]);
  const [searchParams, setSearchParams] = useState({
    keyword: '',
    nodeType: '',
    relationType: '',
  });
  const [selectedLineage, setSelectedLineage] = useState<LineageSearchResult | null>(null);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [graphDrawerVisible, setGraphDrawerVisible] = useState(false);
  const [selectedNodeId, setSelectedNodeId] = useState<string>('');
  const [pathModalVisible, setPathModalVisible] = useState(false);
  const [pathData, setPathData] = useState<any[]>([]);
  const [pathLoading, setPathLoading] = useState(false);
  const [sourceNode, setSourceNode] = useState<string>('');
  const [targetNode, setTargetNode] = useState<string>('');

  const relationTypeOptions = [
    { value: 'DERIVED', label: '派生关系' },
    { value: 'TRANSFORM', label: '转换关系' },
    { value: 'COPY', label: '复制关系' },
    { value: 'AGGREGATE', label: '聚合关系' },
    { value: 'JOIN', label: '关联关系' },
    { value: 'FILTER', label: '过滤关系' },
  ];

  const nodeTypeOptions = [
    { value: 'TABLE', label: '表' },
    { value: 'COLUMN', label: '字段' },
    { value: 'VIEW', label: '视图' },
    { value: 'PROCEDURE', label: '存储过程' },
    { value: 'FUNCTION', label: '函数' },
  ];

  const columns = [
    {
      title: '源节点',
      dataIndex: 'sourceName',
      key: 'sourceName',
      width: 150,
      render: (text: string, record: LineageSearchResult) => (
        <div>
          <div style={{ fontWeight: 'bold' }}>{text}</div>
          <div style={{ fontSize: '12px', color: '#666' }}>{record.sourceId}</div>
        </div>
      ),
    },
    {
      title: '目标节点',
      dataIndex: 'targetName',
      key: 'targetName',
      width: 150,
      render: (text: string, record: LineageSearchResult) => (
        <div>
          <div style={{ fontWeight: 'bold' }}>{text}</div>
          <div style={{ fontSize: '12px', color: '#666' }}>{record.targetId}</div>
        </div>
      ),
    },
    {
      title: '关系类型',
      dataIndex: 'relationType',
      key: 'relationType',
      width: 120,
      render: (type: string) => <Tag color="blue">{type}</Tag>,
    },
    {
      title: '置信度',
      dataIndex: 'confidence',
      key: 'confidence',
      width: 100,
      render: (confidence: number) => (
        <div>
          <div>{(confidence * 100).toFixed(1)}%</div>
          <div style={{ 
            width: '50px', 
            height: '4px', 
            backgroundColor: '#f0f0f0', 
            borderRadius: '2px',
            overflow: 'hidden'
          }}>
            <div
              style={{
                width: `${confidence * 100}%`,
                height: '100%',
                backgroundColor: confidence > 0.8 ? '#52c41a' : confidence > 0.6 ? '#fa8c16' : '#ff4d4f',
                transition: 'width 0.3s ease',
              }}
            />
          </div>
        </div>
      ),
    },
    {
      title: '验证状态',
      dataIndex: 'verified',
      key: 'verified',
      width: 100,
      render: (verified: boolean) => (
        <Tag color={verified ? 'green' : 'orange'}>
          {verified ? '已验证' : '未验证'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (record: LineageSearchResult) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record)}
          >
            详情
          </Button>
          <Button
            type="link"
            size="small"
            icon={<NodeIndexOutlined />}
            onClick={() => handleViewGraph(record.sourceId)}
          >
            血缘图
          </Button>
          <Button
            type="link"
            size="small"
            icon={<BranchesOutlined />}
            onClick={() => handleFindPath(record.sourceId, record.targetId)}
          >
            路径
          </Button>
        </Space>
      ),
    },
  ];

  const pathColumns = [
    {
      title: '源节点',
      dataIndex: 'source',
      key: 'source',
      width: 200,
    },
    {
      title: '目标节点',
      dataIndex: 'target',
      key: 'target',
      width: 200,
    },
    {
      title: '关系类型',
      dataIndex: 'relationType',
      key: 'relationType',
      width: 120,
      render: (type: string) => <Tag color="blue">{type}</Tag>,
    },
    {
      title: '转换规则',
      dataIndex: 'transformRule',
      key: 'transformRule',
      ellipsis: true,
    },
  ];

  const handleSearch = async () => {
    if (!searchParams.keyword.trim()) {
      message.warning('请输入搜索关键词');
      return;
    }

    setLoading(true);
    try {
      const results = await searchLineage(searchParams);
      setSearchResults(results);
    } catch (error) {
      message.error('搜索失败');
      console.error('Search error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleViewDetail = (lineage: LineageSearchResult) => {
    setSelectedLineage(lineage);
    setDetailModalVisible(true);
  };

  const handleViewGraph = (nodeId: string) => {
    setSelectedNodeId(nodeId);
    setGraphDrawerVisible(true);
  };

  const handleFindPath = async (sourceId: string, targetId: string) => {
    setPathLoading(true);
    try {
      const path = await getLineagePath(sourceId, targetId);
      setPathData(path);
      setSourceNode(sourceId);
      setTargetNode(targetId);
      setPathModalVisible(true);
    } catch (error) {
      message.error('查找路径失败');
      console.error('Find path error:', error);
    } finally {
      setPathLoading(false);
    }
  };

  const handleKeywordChange = (value: string) => {
    setSearchParams(prev => ({ ...prev, keyword: value }));
  };

  const handleFilterChange = (field: string, value: string) => {
    setSearchParams(prev => ({ ...prev, [field]: value || '' }));
  };

  return (
    <div style={{ padding: '24px' }}>
      <Row gutter={[16, 16]}>
        {/* 搜索区域 */}
        <Col span={24}>
          <Card title="血缘关系搜索">
            <Row gutter={[16, 16]}>
              <Col span={12}>
                <Search
                  placeholder="输入节点名称、表名、字段名或转换规则"
                  allowClear
                  enterButton="搜索"
                  size="large"
                  value={searchParams.keyword}
                  onChange={(e) => handleKeywordChange(e.target.value)}
                  onSearch={handleSearch}
                  loading={loading}
                />
              </Col>
              <Col span={6}>
                <Select
                  placeholder="节点类型"
                  allowClear
                  style={{ width: '100%' }}
                  size="large"
                  onChange={(value) => handleFilterChange('nodeType', value)}
                >
                  {nodeTypeOptions.map(option => (
                    <Option key={option.value} value={option.value}>
                      {option.label}
                    </Option>
                  ))}
                </Select>
              </Col>
              <Col span={6}>
                <Select
                  placeholder="关系类型"
                  allowClear
                  style={{ width: '100%' }}
                  size="large"
                  onChange={(value) => handleFilterChange('relationType', value)}
                >
                  {relationTypeOptions.map(option => (
                    <Option key={option.value} value={option.value}>
                      {option.label}
                    </Option>
                  ))}
                </Select>
              </Col>
            </Row>
          </Card>
        </Col>

        {/* 搜索结果 */}
        <Col span={24}>
          <Card 
            title={`搜索结果 (${searchResults.length})`}
            extra={
              <Button onClick={handleSearch} loading={loading} icon={<SearchOutlined />}>
                刷新
              </Button>
            }
          >
            <Table
              columns={columns}
              dataSource={searchResults}
              rowKey="id"
              loading={loading}
              scroll={{ x: 'max-content' }}
              pagination={{
                total: searchResults.length,
                pageSize: 20,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total) => `共 ${total} 条记录`,
              }}
            />
          </Card>
        </Col>
      </Row>

      {/* 详情模态框 */}
      <Modal
        title="血缘关系详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={700}
      >
        {selectedLineage && (
          <Descriptions column={1} bordered>
            <Descriptions.Item label="关系ID">{selectedLineage.id}</Descriptions.Item>
            <Descriptions.Item label="源节点ID">{selectedLineage.sourceId}</Descriptions.Item>
            <Descriptions.Item label="源节点名称">{selectedLineage.sourceName}</Descriptions.Item>
            <Descriptions.Item label="目标节点ID">{selectedLineage.targetId}</Descriptions.Item>
            <Descriptions.Item label="目标节点名称">{selectedLineage.targetName}</Descriptions.Item>
            <Descriptions.Item label="关系类型">
              <Tag color="blue">{selectedLineage.relationType}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="置信度">
              <Space>
                <span>{(selectedLineage.confidence * 100).toFixed(1)}%</span>
                <div style={{ 
                  width: '100px', 
                  height: '8px', 
                  backgroundColor: '#f0f0f0', 
                  borderRadius: '4px',
                  overflow: 'hidden',
                  display: 'inline-block'
                }}>
                  <div
                    style={{
                      width: `${selectedLineage.confidence * 100}%`,
                      height: '100%',
                      backgroundColor: selectedLineage.confidence > 0.8 ? '#52c41a' : 
                                     selectedLineage.confidence > 0.6 ? '#fa8c16' : '#ff4d4f',
                    }}
                  />
                </div>
              </Space>
            </Descriptions.Item>
            <Descriptions.Item label="验证状态">
              <Tag color={selectedLineage.verified ? 'green' : 'orange'}>
                {selectedLineage.verified ? '已验证' : '未验证'}
              </Tag>
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>

      {/* 血缘图抽屉 */}
      <Drawer
        title={`血缘关系图 - ${selectedNodeId}`}
        placement="right"
        width="80%"
        onClose={() => setGraphDrawerVisible(false)}
        open={graphDrawerVisible}
      >
        {selectedNodeId && (
          <LineageGraph 
            nodeId={selectedNodeId}
            maxDepth={3}
            direction="ALL"
            onNodeSelect={(nodeId) => console.log('Selected node:', nodeId)}
          />
        )}
      </Drawer>

      {/* 路径模态框 */}
      <Modal
        title={`血缘路径: ${sourceNode} → ${targetNode}`}
        open={pathModalVisible}
        onCancel={() => setPathModalVisible(false)}
        footer={null}
        width={800}
      >
        <Table
          columns={pathColumns}
          dataSource={pathData}
          rowKey={(record) => `${record.source}-${record.target}`}
          loading={pathLoading}
          pagination={false}
          size="small"
        />
        {pathData.length === 0 && !pathLoading && (
          <div style={{ textAlign: 'center', padding: '40px', color: '#999' }}>
            未找到血缘路径
          </div>
        )}
      </Modal>
    </div>
  );
};

export default LineageSearch;