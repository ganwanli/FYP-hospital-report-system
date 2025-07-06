import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Progress,
  Table,
  Typography,
  Space,
  Button,
  Select,
  DatePicker,
  Alert,
  Tooltip,
  Tag,
  Empty,
  Spin
} from 'antd';
import {
  DashboardOutlined,
  ClockCircleOutlined,
  DatabaseOutlined,
  BarChartOutlined,
  LineChartOutlined,
  PieChartOutlined,
  WarningOutlined,
  CheckCircleOutlined,
  ReloadOutlined,
  DownloadOutlined
} from '@ant-design/icons';
import { Line, Column, Pie } from '@ant-design/plots';
import { sqlExecutionApi } from '../../services/sql';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { Option } = Select;
const { RangePicker } = DatePicker;

interface PerformanceMonitorProps {
  userId: number;
}

interface SlowQuery {
  sql: string;
  executionTime: number;
  parameters: Record<string, any>;
  timestamp: string;
}

interface CacheStats {
  cacheSize: number;
  maxCacheSize: number;
  totalHits: number;
  totalMisses: number;
  totalRequests: number;
  hitRate: number;
  estimatedMemoryUsage: number;
}

interface SystemMetrics {
  totalMemory: number;
  freeMemory: number;
  usedMemory: number;
  maxMemory: number;
  memoryUsagePercentage: number;
  cpuUsage: number;
  activeExecutions: number;
  totalExecutions: number;
}

const PerformanceMonitor: React.FC<PerformanceMonitorProps> = ({ userId }) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [executionStats, setExecutionStats] = useState<any>(null);
  const [slowQueries, setSlowQueries] = useState<SlowQuery[]>([]);
  const [cacheStats, setCacheStats] = useState<CacheStats | null>(null);
  const [systemMetrics, setSystemMetrics] = useState<SystemMetrics | null>(null);
  const [timeRange, setTimeRange] = useState<string>('24h');
  const [refreshInterval, setRefreshInterval] = useState<number>(30); // seconds

  useEffect(() => {
    loadAllData();
    
    // Set up auto-refresh
    const interval = setInterval(() => {
      loadAllData();
    }, refreshInterval * 1000);

    return () => clearInterval(interval);
  }, [userId, timeRange, refreshInterval]);

  const loadAllData = async () => {
    setLoading(true);
    try {
      await Promise.all([
        loadExecutionStats(),
        loadSlowQueries(),
        loadCacheStats(),
        loadSystemMetrics()
      ]);
    } catch (error) {
      console.error('Failed to load performance data:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadExecutionStats = async () => {
    try {
      const response = await sqlExecutionApi.getExecutionStatistics(userId);
      setExecutionStats(response.data);
    } catch (error) {
      console.error('Failed to load execution stats:', error);
    }
  };

  const loadSlowQueries = async () => {
    try {
      const response = await sqlExecutionApi.getSlowQueries(20);
      setSlowQueries(response.data);
    } catch (error) {
      console.error('Failed to load slow queries:', error);
    }
  };

  const loadCacheStats = async () => {
    try {
      const response = await sqlExecutionApi.getCacheStatistics();
      setCacheStats(response.data);
    } catch (error) {
      console.error('Failed to load cache stats:', error);
    }
  };

  const loadSystemMetrics = async () => {
    try {
      // This would typically come from a system metrics endpoint
      // For now, we'll simulate some data
      setSystemMetrics({
        totalMemory: 8589934592, // 8GB
        freeMemory: 2147483648,  // 2GB
        usedMemory: 6442450944,  // 6GB
        maxMemory: 8589934592,   // 8GB
        memoryUsagePercentage: 75,
        cpuUsage: 45.5,
        activeExecutions: 3,
        totalExecutions: 1250
      });
    } catch (error) {
      console.error('Failed to load system metrics:', error);
    }
  };

  const clearCache = async () => {
    try {
      await sqlExecutionApi.clearQueryCache();
      await loadCacheStats();
    } catch (error) {
      console.error('Failed to clear cache:', error);
    }
  };

  // Generate mock performance history data
  const generatePerformanceHistory = () => {
    const data = [];
    const now = dayjs();
    const hours = timeRange === '24h' ? 24 : timeRange === '7d' ? 168 : 720;
    
    for (let i = hours; i >= 0; i--) {
      const time = now.subtract(i, 'hour');
      data.push({
        time: time.format('MM-DD HH:mm'),
        avgExecutionTime: Math.random() * 1000 + 500,
        queryCount: Math.floor(Math.random() * 50) + 10,
        errorRate: Math.random() * 10,
        cacheHitRate: Math.random() * 40 + 60
      });
    }
    return data;
  };

  const performanceHistory = generatePerformanceHistory();

  // Generate mock query type distribution
  const queryTypeData = [
    { type: 'SELECT', value: 75, count: 1875 },
    { type: 'INSERT', value: 15, count: 375 },
    { type: 'UPDATE', value: 8, count: 200 },
    { type: 'DELETE', value: 2, count: 50 }
  ];

  const slowQueryColumns: ColumnsType<SlowQuery> = [
    {
      title: 'SQL Query',
      dataIndex: 'sql',
      key: 'sql',
      ellipsis: true,
      render: (sql: string) => (
        <Tooltip title={sql}>
          <Text code style={{ maxWidth: '300px', display: 'block' }}>
            {sql.length > 100 ? sql.substring(0, 100) + '...' : sql}
          </Text>
        </Tooltip>
      )
    },
    {
      title: 'Execution Time',
      dataIndex: 'executionTime',
      key: 'executionTime',
      width: 120,
      sorter: (a, b) => a.executionTime - b.executionTime,
      render: (time: number) => (
        <Text strong style={{ color: time > 5000 ? '#ff4d4f' : time > 1000 ? '#fa8c16' : '#52c41a' }}>
          {time > 1000 ? `${(time / 1000).toFixed(1)}s` : `${time}ms`}
        </Text>
      )
    },
    {
      title: 'Timestamp',
      dataIndex: 'timestamp',
      key: 'timestamp',
      width: 180,
      render: (timestamp: string) => dayjs(timestamp).format('MM-DD HH:mm:ss')
    }
  ];

  const lineChartConfig = {
    data: performanceHistory,
    xField: 'time',
    yField: 'avgExecutionTime',
    seriesField: 'type',
    smooth: true,
    animation: {
      appear: {
        animation: 'path-in',
        duration: 1000,
      },
    },
    point: {
      size: 3,
      shape: 'circle',
    },
    tooltip: {
      showMarkers: false,
    },
  };

  const columnChartConfig = {
    data: performanceHistory,
    xField: 'time',
    yField: 'queryCount',
    color: '#1890ff',
    columnWidthRatio: 0.8,
    meta: {
      queryCount: {
        alias: 'Query Count',
      },
    },
  };

  const pieChartConfig = {
    data: queryTypeData,
    angleField: 'value',
    colorField: 'type',
    radius: 0.8,
    label: {
      type: 'outer',
      content: '{name} {percentage}',
    },
    interactions: [
      {
        type: 'element-selected',
      },
      {
        type: 'element-active',
      },
    ],
  };

  return (
    <div>
      {/* Control Panel */}
      <Card size="small" style={{ marginBottom: '16px' }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Space>
              <Text strong>Performance Monitor</Text>
              <Select
                value={timeRange}
                onChange={setTimeRange}
                style={{ width: 120 }}
              >
                <Option value="24h">Last 24h</Option>
                <Option value="7d">Last 7d</Option>
                <Option value="30d">Last 30d</Option>
              </Select>
              <Select
                value={refreshInterval}
                onChange={setRefreshInterval}
                style={{ width: 150 }}
              >
                <Option value={10}>Refresh 10s</Option>
                <Option value={30}>Refresh 30s</Option>
                <Option value={60}>Refresh 1m</Option>
                <Option value={300}>Refresh 5m</Option>
              </Select>
            </Space>
          </Col>
          <Col>
            <Space>
              <Button
                icon={<ReloadOutlined />}
                onClick={loadAllData}
                loading={loading}
              >
                Refresh
              </Button>
              <Button
                icon={<DownloadOutlined />}
                onClick={() => {
                  // Export performance data
                  console.log('Export performance data');
                }}
              >
                Export
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      <Spin spinning={loading}>
        {/* Key Metrics */}
        <Row gutter={[16, 16]} style={{ marginBottom: '16px' }}>
          <Col span={6}>
            <Card>
              <Statistic
                title="Total Executions"
                value={executionStats?.totalExecutions || 0}
                prefix={<DatabaseOutlined />}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="Avg Execution Time"
                value={executionStats?.avgExecutionTime || 0}
                suffix="ms"
                prefix={<ClockCircleOutlined />}
                valueStyle={{ color: '#52c41a' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="Success Rate"
                value={executionStats ? 
                  ((executionStats.successfulExecutions / executionStats.totalExecutions) * 100).toFixed(1) : 0}
                suffix="%"
                prefix={<CheckCircleOutlined />}
                valueStyle={{ color: '#52c41a' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="Cache Hit Rate"
                value={cacheStats?.hitRate || 0}
                suffix="%"
                prefix={<BarChartOutlined />}
                valueStyle={{ color: '#fa8c16' }}
              />
            </Card>
          </Col>
        </Row>

        {/* System Health */}
        {systemMetrics && (
          <Row gutter={[16, 16]} style={{ marginBottom: '16px' }}>
            <Col span={12}>
              <Card title="System Resources" size="small">
                <Row gutter={[16, 16]}>
                  <Col span={12}>
                    <Text strong>Memory Usage</Text>
                    <Progress
                      percent={systemMetrics.memoryUsagePercentage}
                      status={systemMetrics.memoryUsagePercentage > 80 ? 'exception' : 'normal'}
                      format={() => `${(systemMetrics.usedMemory / 1024 / 1024 / 1024).toFixed(1)}GB`}
                    />
                  </Col>
                  <Col span={12}>
                    <Text strong>CPU Usage</Text>
                    <Progress
                      percent={systemMetrics.cpuUsage}
                      status={systemMetrics.cpuUsage > 80 ? 'exception' : 'normal'}
                      format={() => `${systemMetrics.cpuUsage.toFixed(1)}%`}
                    />
                  </Col>
                </Row>
              </Card>
            </Col>
            <Col span={12}>
              <Card title="Cache Statistics" size="small">
                <Row gutter={[16, 16]}>
                  <Col span={12}>
                    <Statistic
                      title="Cache Size"
                      value={cacheStats?.cacheSize || 0}
                      suffix={`/ ${cacheStats?.maxCacheSize || 0}`}
                    />
                  </Col>
                  <Col span={12}>
                    <Statistic
                      title="Memory Usage"
                      value={cacheStats ? (cacheStats.estimatedMemoryUsage / 1024 / 1024).toFixed(1) : 0}
                      suffix="MB"
                    />
                  </Col>
                </Row>
                <Button
                  type="primary"
                  danger
                  size="small"
                  onClick={clearCache}
                  style={{ marginTop: '8px' }}
                >
                  Clear Cache
                </Button>
              </Card>
            </Col>
          </Row>
        )}

        {/* Performance Charts */}
        <Row gutter={[16, 16]} style={{ marginBottom: '16px' }}>
          <Col span={16}>
            <Card title="Execution Time Trend" size="small">
              <Line {...lineChartConfig} height={300} />
            </Card>
          </Col>
          <Col span={8}>
            <Card title="Query Type Distribution" size="small">
              <Pie {...pieChartConfig} height={300} />
            </Card>
          </Col>
        </Row>

        <Row gutter={[16, 16]} style={{ marginBottom: '16px' }}>
          <Col span={24}>
            <Card title="Query Volume" size="small">
              <Column {...columnChartConfig} height={200} />
            </Card>
          </Col>
        </Row>

        {/* Slow Queries */}
        <Row gutter={[16, 16]}>
          <Col span={24}>
            <Card
              title={
                <Space>
                  <WarningOutlined style={{ color: '#fa8c16' }} />
                  <Text strong>Slow Queries (>{'>'}1s)</Text>
                  <Tag color="orange">{slowQueries.length}</Tag>
                </Space>
              }
              size="small"
            >
              {slowQueries.length === 0 ? (
                <Empty
                  description="No slow queries detected"
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
              ) : (
                <Table
                  columns={slowQueryColumns}
                  dataSource={slowQueries}
                  size="small"
                  pagination={{
                    pageSize: 10,
                    showTotal: (total, range) => 
                      `${range[0]}-${range[1]} of ${total} slow queries`
                  }}
                  rowKey={(record, index) => index || 0}
                />
              )}
            </Card>
          </Col>
        </Row>

        {/* Alerts */}
        {systemMetrics && systemMetrics.memoryUsagePercentage > 80 && (
          <Alert
            message="High Memory Usage"
            description="System memory usage is above 80%. Consider optimizing queries or clearing cache."
            type="warning"
            showIcon
            style={{ marginTop: '16px' }}
          />
        )}

        {cacheStats && cacheStats.hitRate < 50 && (
          <Alert
            message="Low Cache Hit Rate"
            description="Cache hit rate is below 50%. Consider reviewing cache configuration."
            type="info"
            showIcon
            style={{ marginTop: '16px' }}
          />
        )}
      </Spin>
    </div>
  );
};

export default PerformanceMonitor;