import React, { useState, useEffect } from 'react';
import {
  List,
  Card,
  Space,
  Button,
  Tag,
  Typography,
  Row,
  Col,
  Input,
  Select,
  DatePicker,
  Empty,
  Tooltip,
  Modal,
  message,
  Spin
} from 'antd';
import {
  PlayCircleOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  EyeOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  DatabaseOutlined
} from '@ant-design/icons';
import { sqlExecutionApi } from '../../services/sql';
import type { ExecutionHistory } from '../../services/sql';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

const { Text, Paragraph } = Typography;
const { Search } = Input;
const { Option } = Select;
const { RangePicker } = DatePicker;

interface ExecutionHistoryProps {
  userId: number;
  onSelectExecution?: (execution: ExecutionHistory) => void;
}

const ExecutionHistoryComponent: React.FC<ExecutionHistoryProps> = ({
  userId,
  onSelectExecution
}) => {
  const [history, setHistory] = useState<ExecutionHistory[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [searchText, setSearchText] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs] | null>(null);
  const [selectedExecution, setSelectedExecution] = useState<ExecutionHistory | null>(null);
  const [detailModalVisible, setDetailModalVisible] = useState<boolean>(false);

  useEffect(() => {
    loadHistory();
  }, [userId]);

  const loadHistory = async () => {
    setLoading(true);
    try {
      const response = await sqlExecutionApi.getExecutionHistory(userId, 100);
      setHistory(response.data);
    } catch (error) {
      message.error('Failed to load execution history');
    } finally {
      setLoading(false);
    }
  };

  const clearHistory = async () => {
    Modal.confirm({
      title: 'Clear Execution History',
      content: 'Are you sure you want to clear your execution history? This action cannot be undone.',
      onOk: async () => {
        try {
          await sqlExecutionApi.clearExecutionHistory(userId, 30);
          message.success('Execution history cleared');
          loadHistory();
        } catch (error) {
          message.error('Failed to clear execution history');
        }
      }
    });
  };

  const filteredHistory = history.filter(execution => {
    // Search filter
    if (searchText) {
      const searchLower = searchText.toLowerCase();
      const matchesSearch = 
        execution.sqlContent.toLowerCase().includes(searchLower) ||
        (execution.templateName && execution.templateName.toLowerCase().includes(searchLower)) ||
        (execution.databaseName && execution.databaseName.toLowerCase().includes(searchLower));
      
      if (!matchesSearch) return false;
    }

    // Status filter
    if (statusFilter && execution.executionStatus !== statusFilter) {
      return false;
    }

    // Date range filter
    if (dateRange) {
      const executionDate = dayjs(execution.startTime);
      if (!executionDate.isBetween(dateRange[0], dateRange[1], 'day', '[]')) {
        return false;
      }
    }

    return true;
  });

  const getStatusColor = (status: string): string => {
    switch (status.toLowerCase()) {
      case 'success':
        return 'green';
      case 'failed':
      case 'error':
        return 'red';
      case 'running':
        return 'blue';
      case 'cancelled':
        return 'orange';
      default:
        return 'default';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case 'success':
        return <CheckCircleOutlined />;
      case 'failed':
      case 'error':
        return <CloseCircleOutlined />;
      case 'running':
        return <ClockCircleOutlined />;
      default:
        return <PlayCircleOutlined />;
    }
  };

  const formatDuration = (duration: number): string => {
    if (duration < 1000) {
      return `${duration}ms`;
    } else if (duration < 60000) {
      return `${(duration / 1000).toFixed(1)}s`;
    } else {
      return `${(duration / 60000).toFixed(1)}m`;
    }
  };

  const truncateSQL = (sql: string, maxLength: number = 100): string => {
    if (sql.length <= maxLength) return sql;
    return sql.substring(0, maxLength) + '...';
  };

  const showExecutionDetail = (execution: ExecutionHistory) => {
    setSelectedExecution(execution);
    setDetailModalVisible(true);
  };

  const renderExecutionItem = (execution: ExecutionHistory) => {
    const isAsync = execution.isAsync;
    const hasError = execution.executionStatus === 'FAILED' || execution.executionStatus === 'ERROR';

    return (
      <List.Item
        key={execution.executionId}
        actions={[
          <Tooltip title="View Details">
            <Button
              icon={<EyeOutlined />}
              size="small"
              onClick={() => showExecutionDetail(execution)}
            />
          </Tooltip>,
          onSelectExecution && (
            <Tooltip title="Use this SQL">
              <Button
                icon={<PlayCircleOutlined />}
                size="small"
                type="primary"
                onClick={() => onSelectExecution(execution)}
              />
            </Tooltip>
          )
        ].filter(Boolean)}
      >
        <List.Item.Meta
          title={
            <Space>
              {execution.templateName ? (
                <Text strong>{execution.templateName}</Text>
              ) : (
                <Text type="secondary">Ad-hoc Query</Text>
              )}
              <Tag
                color={getStatusColor(execution.executionStatus)}
                icon={getStatusIcon(execution.executionStatus)}
              >
                {execution.executionStatus}
              </Tag>
              {isAsync && (
                <Tag color="purple">ASYNC</Tag>
              )}
              {execution.cacheHit && (
                <Tag color="orange">CACHED</Tag>
              )}
            </Space>
          }
          description={
            <div>
              <Paragraph 
                ellipsis={{ rows: 2, expandable: true }} 
                style={{ margin: 0, marginBottom: '8px' }}
                code
              >
                {execution.sqlContent}
              </Paragraph>
              <Row gutter={16}>
                <Col>
                  <Space size="large">
                    <Text type="secondary">
                      <ClockCircleOutlined style={{ marginRight: '4px' }} />
                      {dayjs(execution.startTime).fromNow()}
                    </Text>
                    <Text type="secondary">
                      <DatabaseOutlined style={{ marginRight: '4px' }} />
                      {execution.databaseName}
                    </Text>
                    {execution.executionDuration && (
                      <Text type="secondary">
                        Duration: {formatDuration(execution.executionDuration)}
                      </Text>
                    )}
                    {execution.resultRows !== undefined && (
                      <Text type="secondary">
                        Rows: {execution.resultRows.toLocaleString()}
                      </Text>
                    )}
                  </Space>
                </Col>
              </Row>
              {hasError && execution.errorMessage && (
                <Text type="danger" style={{ marginTop: '8px', display: 'block' }}>
                  Error: {execution.errorMessage}
                </Text>
              )}
            </div>
          }
        />
      </List.Item>
    );
  };

  return (
    <div>
      {/* Filters */}
      <Card size="small" style={{ marginBottom: '16px' }}>
        <Row gutter={[16, 16]}>
          <Col span={8}>
            <Search
              placeholder="Search SQL or template name..."
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              allowClear
            />
          </Col>
          <Col span={6}>
            <Select
              placeholder="Filter by status"
              style={{ width: '100%' }}
              allowClear
              value={statusFilter}
              onChange={setStatusFilter}
            >
              <Option value="SUCCESS">Success</Option>
              <Option value="FAILED">Failed</Option>
              <Option value="ERROR">Error</Option>
              <Option value="RUNNING">Running</Option>
              <Option value="CANCELLED">Cancelled</Option>
            </Select>
          </Col>
          <Col span={6}>
            <RangePicker
              style={{ width: '100%' }}
              value={dateRange}
              onChange={setDateRange}
              placeholder={['Start date', 'End date']}
            />
          </Col>
          <Col span={4}>
            <Space>
              <Button
                icon={<ReloadOutlined />}
                onClick={loadHistory}
                loading={loading}
              >
                Refresh
              </Button>
              <Button
                icon={<DeleteOutlined />}
                danger
                onClick={clearHistory}
              >
                Clear
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* History List */}
      <Card
        title={
          <Space>
            <Text strong>Execution History</Text>
            <Text type="secondary">({filteredHistory.length} executions)</Text>
          </Space>
        }
      >
        <Spin spinning={loading}>
          {filteredHistory.length === 0 ? (
            <Empty
              description="No execution history found"
              image={Empty.PRESENTED_IMAGE_SIMPLE}
            />
          ) : (
            <List
              dataSource={filteredHistory}
              renderItem={renderExecutionItem}
              pagination={{
                pageSize: 10,
                showTotal: (total, range) => 
                  `${range[0]}-${range[1]} of ${total} executions`,
                showSizeChanger: true,
                showQuickJumper: true
              }}
            />
          )}
        </Spin>
      </Card>

      {/* Detail Modal */}
      <Modal
        title="Execution Details"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setDetailModalVisible(false)}>
            Close
          </Button>,
          onSelectExecution && selectedExecution && (
            <Button
              key="use"
              type="primary"
              onClick={() => {
                onSelectExecution(selectedExecution);
                setDetailModalVisible(false);
              }}
            >
              Use This SQL
            </Button>
          )
        ].filter(Boolean)}
        width={800}
      >
        {selectedExecution && (
          <div>
            <Row gutter={[16, 16]}>
              <Col span={12}>
                <Text strong>Execution ID:</Text>
                <br />
                <Text code>{selectedExecution.executionId}</Text>
              </Col>
              <Col span={12}>
                <Text strong>Status:</Text>
                <br />
                <Tag
                  color={getStatusColor(selectedExecution.executionStatus)}
                  icon={getStatusIcon(selectedExecution.executionStatus)}
                >
                  {selectedExecution.executionStatus}
                </Tag>
              </Col>
              <Col span={12}>
                <Text strong>Start Time:</Text>
                <br />
                <Text>{dayjs(selectedExecution.startTime).format('YYYY-MM-DD HH:mm:ss')}</Text>
              </Col>
              <Col span={12}>
                <Text strong>Duration:</Text>
                <br />
                <Text>{selectedExecution.executionDuration ? formatDuration(selectedExecution.executionDuration) : 'N/A'}</Text>
              </Col>
              <Col span={12}>
                <Text strong>Database:</Text>
                <br />
                <Text>{selectedExecution.databaseName}</Text>
              </Col>
              <Col span={12}>
                <Text strong>Query Type:</Text>
                <br />
                <Text>{selectedExecution.queryType}</Text>
              </Col>
              {selectedExecution.resultRows !== undefined && (
                <Col span={12}>
                  <Text strong>Result Rows:</Text>
                  <br />
                  <Text>{selectedExecution.resultRows.toLocaleString()}</Text>
                </Col>
              )}
              {selectedExecution.affectedRows !== undefined && (
                <Col span={12}>
                  <Text strong>Affected Rows:</Text>
                  <br />
                  <Text>{selectedExecution.affectedRows.toLocaleString()}</Text>
                </Col>
              )}
            </Row>

            <div style={{ marginTop: '16px' }}>
              <Text strong>SQL Content:</Text>
              <pre style={{ 
                background: '#f5f5f5', 
                padding: '12px', 
                borderRadius: '4px',
                marginTop: '8px',
                maxHeight: '200px',
                overflow: 'auto'
              }}>
                {selectedExecution.sqlContent}
              </pre>
            </div>

            {selectedExecution.parameters && Object.keys(selectedExecution.parameters).length > 0 && (
              <div style={{ marginTop: '16px' }}>
                <Text strong>Parameters:</Text>
                <pre style={{ 
                  background: '#f5f5f5', 
                  padding: '12px', 
                  borderRadius: '4px',
                  marginTop: '8px',
                  maxHeight: '150px',
                  overflow: 'auto'
                }}>
                  {JSON.stringify(selectedExecution.parameters, null, 2)}
                </pre>
              </div>
            )}

            {selectedExecution.errorMessage && (
              <div style={{ marginTop: '16px' }}>
                <Text strong>Error Message:</Text>
                <div style={{ 
                  background: '#fff2f0', 
                  border: '1px solid #ffccc7',
                  padding: '12px', 
                  borderRadius: '4px',
                  marginTop: '8px'
                }}>
                  <Text type="danger">{selectedExecution.errorMessage}</Text>
                </div>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default ExecutionHistoryComponent;