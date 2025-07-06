import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Button,
  Select,
  Input,
  Table,
  Tag,
  Alert,
  Space,
  Descriptions,
  Progress,
  message,
  Modal,
  List,
  Divider,
} from 'antd';
import {
  ExclamationCircleOutlined,
  ReloadOutlined,
  SearchOutlined,
  BugOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons';
import { performImpactAnalysis, searchNodes } from '@/services/lineage';

const { Option } = Select;
const { Search } = Input;

interface ImpactAnalysisResult {
  analysisId: string;
  sourceNodeId: string;
  changeType: string;
  analysisDepth: number;
  downstreamImpacts: any[];
  upstreamDependencies: any[];
  totalAffectedNodes: number;
  riskLevel: string;
  recommendations: string[];
  executionTime: number;
  analysisTime: string;
}

const ImpactAnalysis: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [selectedNodeId, setSelectedNodeId] = useState<string>('');
  const [changeType, setChangeType] = useState<string>('UPDATE');
  const [analysisDepth, setAnalysisDepth] = useState<number>(3);
  const [analysisResult, setAnalysisResult] = useState<ImpactAnalysisResult | null>(null);
  const [nodeOptions, setNodeOptions] = useState<any[]>([]);
  const [searchKeyword, setSearchKeyword] = useState<string>('');
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedImpact, setSelectedImpact] = useState<any>(null);

  const riskLevelConfig = {
    LOW: { color: 'green', text: '低风险' },
    MEDIUM: { color: 'orange', text: '中风险' },
    HIGH: { color: 'red', text: '高风险' },
    CRITICAL: { color: 'red', text: '关键风险' },
  };

  const changeTypeOptions = [
    { value: 'UPDATE', label: '数据更新' },
    { value: 'DELETE', label: '数据删除' },
    { value: 'SCHEMA_CHANGE', label: '结构变更' },
    { value: 'RENAME', label: '重命名' },
    { value: 'MOVE', label: '迁移' },
    { value: 'DEPRECATE', label: '废弃' },
  ];

  const impactColumns = [
    {
      title: '节点ID',
      dataIndex: 'nodeId',
      key: 'nodeId',
      width: 200,
      ellipsis: true,
    },
    {
      title: '影响层级',
      dataIndex: 'level',
      key: 'level',
      width: 100,
      render: (level: number) => (
        <Tag color={level === 1 ? 'green' : level === 2 ? 'orange' : 'red'}>
          第{level}层
        </Tag>
      ),
    },
    {
      title: '关系类型',
      dataIndex: 'relationType',
      key: 'relationType',
      width: 120,
      render: (type: string) => <Tag>{type}</Tag>,
    },
    {
      title: '影响方向',
      dataIndex: 'impactType',
      key: 'impactType',
      width: 100,
      render: (type: string) => (
        <Tag color={type === 'DOWNSTREAM' ? 'blue' : 'purple'}>
          {type === 'DOWNSTREAM' ? '下游' : '上游'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (record: any) => (
        <Button
          type="link"
          size="small"
          onClick={() => handleViewDetail(record)}
        >
          查看详情
        </Button>
      ),
    },
  ];

  const loadNodeOptions = async (keyword: string) => {
    if (!keyword) return;
    
    try {
      const nodes = await searchNodes({ keyword, limit: 20 });
      setNodeOptions(nodes.map((node: any) => ({
        value: node.nodeId,
        label: `${node.nodeName} (${node.tableName || ''}.${node.columnName || ''})`,
        ...node,
      })));
    } catch (error) {
      console.error('Load node options error:', error);
    }
  };

  const handleAnalysis = async () => {
    if (!selectedNodeId) {
      message.error('请先选择要分析的节点');
      return;
    }

    setLoading(true);
    try {
      const result = await performImpactAnalysis(selectedNodeId, {
        changeType,
        analysisDepth,
      });
      setAnalysisResult(result);
      message.success('影响分析完成');
    } catch (error) {
      message.error('影响分析失败');
      console.error('Impact analysis error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleViewDetail = (impact: any) => {
    setSelectedImpact(impact);
    setDetailModalVisible(true);
  };

  const getRiskLevelProgress = (riskLevel: string) => {
    const config = riskLevelConfig[riskLevel as keyof typeof riskLevelConfig];
    if (!config) return { percent: 0, status: 'normal' as const };

    const percentMap = {
      LOW: 25,
      MEDIUM: 50,
      HIGH: 75,
      CRITICAL: 100,
    };

    return {
      percent: percentMap[riskLevel as keyof typeof percentMap],
      status: (riskLevel === 'CRITICAL' || riskLevel === 'HIGH' ? 'exception' : 'normal') as const,
    };
  };

  useEffect(() => {
    if (searchKeyword) {
      const timer = setTimeout(() => {
        loadNodeOptions(searchKeyword);
      }, 300);
      return () => clearTimeout(timer);
    }
  }, [searchKeyword]);

  return (
    <div style={{ padding: '24px' }}>
      <Row gutter={[16, 16]}>
        {/* 分析配置 */}
        <Col span={24}>
          <Card title="影响分析配置">
            <Row gutter={16} align="middle">
              <Col span={8}>
                <label style={{ display: 'block', marginBottom: 8 }}>目标节点：</label>
                <Select
                  showSearch
                  placeholder="搜索并选择节点"
                  style={{ width: '100%' }}
                  value={selectedNodeId}
                  onChange={setSelectedNodeId}
                  onSearch={setSearchKeyword}
                  filterOption={false}
                  notFoundContent={loading ? '搜索中...' : '暂无数据'}
                >
                  {nodeOptions.map((option) => (
                    <Option key={option.value} value={option.value}>
                      {option.label}
                    </Option>
                  ))}
                </Select>
              </Col>
              
              <Col span={4}>
                <label style={{ display: 'block', marginBottom: 8 }}>变更类型：</label>
                <Select
                  style={{ width: '100%' }}
                  value={changeType}
                  onChange={setChangeType}
                >
                  {changeTypeOptions.map((option) => (
                    <Option key={option.value} value={option.value}>
                      {option.label}
                    </Option>
                  ))}
                </Select>
              </Col>
              
              <Col span={4}>
                <label style={{ display: 'block', marginBottom: 8 }}>分析深度：</label>
                <Select
                  style={{ width: '100%' }}
                  value={analysisDepth}
                  onChange={setAnalysisDepth}
                >
                  <Option value={1}>1层</Option>
                  <Option value={2}>2层</Option>
                  <Option value={3}>3层</Option>
                  <Option value={5}>5层</Option>
                </Select>
              </Col>
              
              <Col span={4}>
                <label style={{ display: 'block', marginBottom: 8 }}>&nbsp;</label>
                <Button
                  type="primary"
                  loading={loading}
                  onClick={handleAnalysis}
                  icon={<BugOutlined />}
                  style={{ width: '100%' }}
                >
                  开始分析
                </Button>
              </Col>
            </Row>
          </Card>
        </Col>

        {/* 分析结果总览 */}
        {analysisResult && (
          <>
            <Col span={24}>
              <Card title="分析结果总览">
                <Row gutter={16}>
                  <Col span={6}>
                    <div style={{ textAlign: 'center' }}>
                      <div style={{ fontSize: '32px', fontWeight: 'bold', color: '#1890ff' }}>
                        {analysisResult.totalAffectedNodes}
                      </div>
                      <div style={{ color: '#666' }}>受影响节点数</div>
                    </div>
                  </Col>
                  <Col span={6}>
                    <div style={{ textAlign: 'center' }}>
                      <div style={{ fontSize: '32px', fontWeight: 'bold', color: '#52c41a' }}>
                        {analysisResult.downstreamImpacts.length}
                      </div>
                      <div style={{ color: '#666' }}>下游影响</div>
                    </div>
                  </Col>
                  <Col span={6}>
                    <div style={{ textAlign: 'center' }}>
                      <div style={{ fontSize: '32px', fontWeight: 'bold', color: '#fa8c16' }}>
                        {analysisResult.upstreamDependencies.length}
                      </div>
                      <div style={{ color: '#666' }}>上游依赖</div>
                    </div>
                  </Col>
                  <Col span={6}>
                    <div style={{ textAlign: 'center' }}>
                      <div style={{ fontSize: '32px', fontWeight: 'bold', color: '#722ed1' }}>
                        {analysisResult.executionTime}ms
                      </div>
                      <div style={{ color: '#666' }}>分析耗时</div>
                    </div>
                  </Col>
                </Row>

                <Divider />

                <Row gutter={16}>
                  <Col span={12}>
                    <div style={{ marginBottom: 16 }}>
                      <div style={{ marginBottom: 8 }}>
                        <strong>风险等级：</strong>
                        <Tag 
                          color={riskLevelConfig[analysisResult.riskLevel as keyof typeof riskLevelConfig]?.color}
                          style={{ marginLeft: 8 }}
                        >
                          {riskLevelConfig[analysisResult.riskLevel as keyof typeof riskLevelConfig]?.text}
                        </Tag>
                      </div>
                      <Progress 
                        {...getRiskLevelProgress(analysisResult.riskLevel)}
                        showInfo={false}
                        strokeWidth={8}
                      />
                    </div>
                  </Col>
                  <Col span={12}>
                    <div>
                      <strong>分析时间：</strong>
                      <span style={{ marginLeft: 8 }}>
                        {new Date(analysisResult.analysisTime).toLocaleString()}
                      </span>
                    </div>
                  </Col>
                </Row>

                {analysisResult.riskLevel === 'HIGH' || analysisResult.riskLevel === 'CRITICAL' ? (
                  <Alert
                    message="高风险警告"
                    description="此次变更可能对系统产生重大影响，请谨慎操作并做好充分的测试和回滚准备。"
                    type="error"
                    showIcon
                    style={{ marginTop: 16 }}
                  />
                ) : null}
              </Card>
            </Col>

            {/* 建议措施 */}
            <Col span={24}>
              <Card title="建议措施">
                <List
                  dataSource={analysisResult.recommendations}
                  renderItem={(item, index) => (
                    <List.Item>
                      <Space>
                        <CheckCircleOutlined style={{ color: '#52c41a' }} />
                        <span>{item}</span>
                      </Space>
                    </List.Item>
                  )}
                />
              </Card>
            </Col>

            {/* 影响详情 */}
            <Col span={12}>
              <Card title="下游影响节点">
                <Table
                  columns={impactColumns}
                  dataSource={analysisResult.downstreamImpacts}
                  rowKey="nodeId"
                  size="small"
                  pagination={false}
                  scroll={{ y: 300 }}
                />
              </Card>
            </Col>

            <Col span={12}>
              <Card title="上游依赖节点">
                <Table
                  columns={impactColumns}
                  dataSource={analysisResult.upstreamDependencies}
                  rowKey="nodeId"
                  size="small"
                  pagination={false}
                  scroll={{ y: 300 }}
                />
              </Card>
            </Col>
          </>
        )}
      </Row>

      {/* 节点详情模态框 */}
      <Modal
        title="节点详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={600}
      >
        {selectedImpact && (
          <Descriptions column={1} bordered>
            <Descriptions.Item label="节点ID">{selectedImpact.nodeId}</Descriptions.Item>
            <Descriptions.Item label="影响层级">
              <Tag color={selectedImpact.level === 1 ? 'green' : selectedImpact.level === 2 ? 'orange' : 'red'}>
                第{selectedImpact.level}层
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="关系类型">
              <Tag>{selectedImpact.relationType}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="影响方向">
              <Tag color={selectedImpact.impactType === 'DOWNSTREAM' ? 'blue' : 'purple'}>
                {selectedImpact.impactType === 'DOWNSTREAM' ? '下游影响' : '上游依赖'}
              </Tag>
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default ImpactAnalysis;