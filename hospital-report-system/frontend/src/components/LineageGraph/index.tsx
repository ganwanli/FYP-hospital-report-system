import React, { useCallback, useEffect, useState } from 'react';
import ReactFlow, {
  Node,
  Edge,
  Controls,
  Background,
  MiniMap,
  useNodesState,
  useEdgesState,
  addEdge,
  Connection,
  ConnectionMode,
  Panel,
} from 'reactflow';
import 'reactflow/dist/style.css';
import { Card, Input, Select, Button, Drawer, Descriptions, Tag, Space, message } from 'antd';
import { SearchOutlined, FullscreenOutlined, ReloadOutlined } from '@ant-design/icons';
import { getLineageGraph, getNodeDetail, updateNodePosition } from '@/services/lineage';

const { Search } = Input;
const { Option } = Select;

interface LineageGraphProps {
  nodeId?: string;
  maxDepth?: number;
  direction?: string;
  onNodeSelect?: (nodeId: string) => void;
}

interface NodeDetailData {
  node: any;
  upstreamCount: number;
  downstreamCount: number;
  recentLineages: any[];
  accessStats: any;
}

const nodeTypes = {
  table: { color: '#1890ff', shape: 'rectangle' },
  column: { color: '#52c41a', shape: 'circle' },
  view: { color: '#fa8c16', shape: 'diamond' },
  procedure: { color: '#722ed1', shape: 'triangle' },
  function: { color: '#eb2f96', shape: 'hexagon' },
  default: { color: '#8c8c8c', shape: 'circle' },
};

const LineageGraph: React.FC<LineageGraphProps> = ({
  nodeId,
  maxDepth = 3,
  direction = 'ALL',
  onNodeSelect,
}) => {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [loading, setLoading] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedDirection, setSelectedDirection] = useState(direction);
  const [selectedDepth, setSelectedDepth] = useState(maxDepth);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [selectedNodeDetail, setSelectedNodeDetail] = useState<NodeDetailData | null>(null);

  const buildGraphFromData = useCallback((graphData: any) => {
    const { nodes: nodeData, edges: edgeData } = graphData;

    const reactFlowNodes: Node[] = nodeData.map((node: any) => {
      const nodeType = node.type?.toLowerCase() || 'default';
      const typeConfig = nodeTypes[nodeType as keyof typeof nodeTypes] || nodeTypes.default;

      return {
        id: node.id,
        type: 'default',
        position: node.position || { x: Math.random() * 400, y: Math.random() * 400 },
        data: {
          label: (
            <div style={{ textAlign: 'center', padding: '8px' }}>
              <div style={{ fontWeight: 'bold', fontSize: '12px' }}>{node.label}</div>
              <div style={{ fontSize: '10px', color: '#666' }}>{node.type}</div>
            </div>
          ),
          ...node.data,
        },
        style: {
          background: typeConfig.color,
          color: 'white',
          border: '1px solid #ddd',
          borderRadius: '8px',
          width: 180,
          height: 60,
        },
      };
    });

    const reactFlowEdges: Edge[] = edgeData.map((edge: any) => ({
      id: edge.id,
      source: edge.source,
      target: edge.target,
      type: 'smoothstep',
      label: edge.label,
      style: {
        stroke: edge.data?.verified ? '#52c41a' : '#d9d9d9',
        strokeWidth: 2,
      },
      markerEnd: {
        type: 'arrowclosed',
        color: edge.data?.verified ? '#52c41a' : '#d9d9d9',
      },
      data: edge.data,
    }));

    setNodes(reactFlowNodes);
    setEdges(reactFlowEdges);
  }, [setNodes, setEdges]);

  const loadLineageGraph = useCallback(async (targetNodeId?: string) => {
    if (!targetNodeId && !nodeId) return;
    
    setLoading(true);
    try {
      const graphData = await getLineageGraph(targetNodeId || nodeId!, {
        maxDepth: selectedDepth,
        direction: selectedDirection,
      });
      buildGraphFromData(graphData);
    } catch (error) {
      message.error('加载血缘关系图失败');
      console.error('Load lineage graph error:', error);
    } finally {
      setLoading(false);
    }
  }, [nodeId, selectedDepth, selectedDirection, buildGraphFromData]);

  const handleNodeClick = useCallback(async (event: React.MouseEvent, node: Node) => {
    try {
      const detail = await getNodeDetail(node.id);
      setSelectedNodeDetail(detail);
      setDrawerVisible(true);
      onNodeSelect?.(node.id);
    } catch (error) {
      message.error('获取节点详情失败');
    }
  }, [onNodeSelect]);

  const handleNodeDragStop = useCallback(async (event: React.MouseEvent, node: Node) => {
    try {
      await updateNodePosition(node.id, node.position.x, node.position.y);
    } catch (error) {
      console.error('Update node position error:', error);
    }
  }, []);

  const onConnect = useCallback(
    (params: Connection) => setEdges((eds) => addEdge(params, eds)),
    [setEdges]
  );

  const handleSearch = useCallback((keyword: string) => {
    if (!keyword) {
      loadLineageGraph();
      return;
    }

    const filteredNodes = nodes.filter((node) =>
      node.data.label.toString().toLowerCase().includes(keyword.toLowerCase()) ||
      node.data.tableName?.toLowerCase().includes(keyword.toLowerCase()) ||
      node.data.columnName?.toLowerCase().includes(keyword.toLowerCase())
    );

    const filteredNodeIds = new Set(filteredNodes.map(node => node.id));
    const filteredEdges = edges.filter(edge => 
      filteredNodeIds.has(edge.source) && filteredNodeIds.has(edge.target)
    );

    setNodes(filteredNodes);
    setEdges(filteredEdges);
  }, [nodes, edges, setNodes, setEdges, loadLineageGraph]);

  const handleRefresh = useCallback(() => {
    loadLineageGraph();
  }, [loadLineageGraph]);

  const handleDirectionChange = useCallback((value: string) => {
    setSelectedDirection(value);
  }, []);

  const handleDepthChange = useCallback((value: number) => {
    setSelectedDepth(value);
  }, []);

  useEffect(() => {
    if (nodeId) {
      loadLineageGraph();
    }
  }, [nodeId, loadLineageGraph]);

  const nodeColor = (node: Node) => {
    const nodeType = node.data?.type?.toLowerCase() || 'default';
    return nodeTypes[nodeType as keyof typeof nodeTypes]?.color || nodeTypes.default.color;
  };

  return (
    <Card 
      loading={loading}
      bodyStyle={{ padding: 0, height: '600px', position: 'relative' }}
    >
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        onNodeClick={handleNodeClick}
        onNodeDragStop={handleNodeDragStop}
        connectionMode={ConnectionMode.Loose}
        fitView
        fitViewOptions={{ padding: 0.2 }}
      >
        <Panel position="top-left">
          <Space direction="vertical" style={{ background: 'white', padding: '12px', borderRadius: '6px', boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
            <Search
              placeholder="搜索节点..."
              allowClear
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onSearch={handleSearch}
              style={{ width: 200 }}
              prefix={<SearchOutlined />}
            />
            
            <Space>
              <Select 
                value={selectedDirection} 
                onChange={handleDirectionChange}
                style={{ width: 100 }}
                size="small"
              >
                <Option value="ALL">全部</Option>
                <Option value="UPSTREAM">上游</Option>
                <Option value="DOWNSTREAM">下游</Option>
              </Select>
              
              <Select 
                value={selectedDepth} 
                onChange={handleDepthChange}
                style={{ width: 80 }}
                size="small"
              >
                <Option value={1}>1层</Option>
                <Option value={2}>2层</Option>
                <Option value={3}>3层</Option>
                <Option value={5}>5层</Option>
              </Select>
              
              <Button
                type="primary"
                size="small"
                onClick={() => loadLineageGraph()}
                icon={<ReloadOutlined />}
              >
                刷新
              </Button>
            </Space>
          </Space>
        </Panel>

        <Panel position="top-right">
          <div style={{ background: 'white', padding: '8px', borderRadius: '6px', boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
            <Space>
              <Tag color="blue">表: {nodes.filter(n => n.data?.type === 'TABLE').length}</Tag>
              <Tag color="green">字段: {nodes.filter(n => n.data?.type === 'COLUMN').length}</Tag>
              <Tag color="orange">关系: {edges.length}</Tag>
            </Space>
          </div>
        </Panel>

        <Controls />
        <MiniMap nodeColor={nodeColor} />
        <Background variant="dots" gap={20} size={1} />
      </ReactFlow>

      <Drawer
        title="节点详情"
        placement="right"
        width={480}
        onClose={() => setDrawerVisible(false)}
        open={drawerVisible}
      >
        {selectedNodeDetail && (
          <div>
            <Descriptions title="基本信息" bordered column={1} size="small">
              <Descriptions.Item label="节点ID">{selectedNodeDetail.node.nodeId}</Descriptions.Item>
              <Descriptions.Item label="节点名称">{selectedNodeDetail.node.nodeName}</Descriptions.Item>
              <Descriptions.Item label="显示名称">{selectedNodeDetail.node.displayName}</Descriptions.Item>
              <Descriptions.Item label="节点类型">
                <Tag color="blue">{selectedNodeDetail.node.nodeType}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="节点分类">{selectedNodeDetail.node.nodeCategory}</Descriptions.Item>
              <Descriptions.Item label="表名">{selectedNodeDetail.node.tableName}</Descriptions.Item>
              <Descriptions.Item label="字段名">{selectedNodeDetail.node.columnName}</Descriptions.Item>
              <Descriptions.Item label="数据类型">{selectedNodeDetail.node.dataType}</Descriptions.Item>
              <Descriptions.Item label="业务含义">{selectedNodeDetail.node.businessMeaning}</Descriptions.Item>
              <Descriptions.Item label="系统来源">{selectedNodeDetail.node.systemSource}</Descriptions.Item>
              <Descriptions.Item label="责任人">{selectedNodeDetail.node.ownerUser}</Descriptions.Item>
              <Descriptions.Item label="部门">{selectedNodeDetail.node.ownerDepartment}</Descriptions.Item>
              <Descriptions.Item label="重要性级别">
                <Tag color={selectedNodeDetail.node.criticalityLevel === 'CRITICAL' ? 'red' : 
                            selectedNodeDetail.node.criticalityLevel === 'HIGH' ? 'orange' : 'blue'}>
                  {selectedNodeDetail.node.criticalityLevel}
                </Tag>
              </Descriptions.Item>
            </Descriptions>

            <Descriptions title="血缘统计" bordered column={2} size="small" style={{ marginTop: 16 }}>
              <Descriptions.Item label="上游依赖">{selectedNodeDetail.upstreamCount}</Descriptions.Item>
              <Descriptions.Item label="下游影响">{selectedNodeDetail.downstreamCount}</Descriptions.Item>
            </Descriptions>

            <Descriptions title="访问信息" bordered column={1} size="small" style={{ marginTop: 16 }}>
              <Descriptions.Item label="最后访问时间">
                {selectedNodeDetail.node.lastAccessTime ? 
                  new Date(selectedNodeDetail.node.lastAccessTime).toLocaleString() : '从未访问'}
              </Descriptions.Item>
              <Descriptions.Item label="访问频率">
                <Tag color={selectedNodeDetail.accessStats?.accessFrequency === 'DAILY' ? 'green' : 
                           selectedNodeDetail.accessStats?.accessFrequency === 'WEEKLY' ? 'blue' : 
                           selectedNodeDetail.accessStats?.accessFrequency === 'MONTHLY' ? 'orange' : 'red'}>
                  {selectedNodeDetail.accessStats?.accessFrequency || 'UNKNOWN'}
                </Tag>
              </Descriptions.Item>
            </Descriptions>
          </div>
        )}
      </Drawer>
    </Card>
  );
};

export default LineageGraph;