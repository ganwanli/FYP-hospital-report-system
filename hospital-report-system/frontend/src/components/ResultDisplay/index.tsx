import React, { useState, useMemo } from 'react';
import {
  Card,
  Table,
  Space,
  Button,
  Dropdown,
  Typography,
  Tag,
  Alert,
  Row,
  Col,
  Statistic,
  Input,
  Select,
  Pagination,
  Tooltip,
  Modal,
  message,
  Empty
} from 'antd';
import type { ColumnsType, TableProps } from 'antd/es/table';
import {
  DownloadOutlined,
  SearchOutlined,
  FilterOutlined,
  EyeOutlined,
  CopyOutlined,
  WarningOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  DatabaseOutlined,
  FileTextOutlined
} from '@ant-design/icons';
import type { ExecutionResult, ColumnMetadata } from '../../services/sql';

const { Text, Title } = Typography;
const { Search } = Input;
const { Option } = Select;

interface ResultDisplayProps {
  result: ExecutionResult;
  onExport?: (format: string) => void;
}

const ResultDisplay: React.FC<ResultDisplayProps> = ({ result, onExport }) => {
  const [searchText, setSearchText] = useState<string>('');
  const [filterColumn, setFilterColumn] = useState<string>('');
  const [filterValue, setFilterValue] = useState<string>('');
  const [pageSize, setPageSize] = useState<number>(50);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [sortedInfo, setSortedInfo] = useState<any>({});
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

  // Filter and search data
  const filteredData = useMemo(() => {
    if (!result.data || !Array.isArray(result.data)) return [];

    let filtered = [...result.data];

    // Apply search filter
    if (searchText) {
      filtered = filtered.filter(row => 
        Object.values(row).some(value => 
          String(value).toLowerCase().includes(searchText.toLowerCase())
        )
      );
    }

    // Apply column filter
    if (filterColumn && filterValue) {
      filtered = filtered.filter(row => {
        const cellValue = String(row[filterColumn] || '').toLowerCase();
        return cellValue.includes(filterValue.toLowerCase());
      });
    }

    return filtered;
  }, [result.data, searchText, filterColumn, filterValue]);

  // Generate table columns from metadata
  const columns: ColumnsType<any> = useMemo(() => {
    if (!result.columns || result.columns.length === 0) {
      // Fallback: generate columns from data
      if (result.data && result.data.length > 0) {
        return Object.keys(result.data[0]).map(key => ({
          title: key,
          dataIndex: key,
          key,
          sorter: true,
          width: 150,
          ellipsis: true,
          render: (value: any) => renderCellValue(value, 'STRING')
        }));
      }
      return [];
    }

    return result.columns.map((col: ColumnMetadata) => ({
      title: (
        <Space>
          <Text strong>{col.name}</Text>
          <Tooltip title={`Type: ${col.typeName}, Size: ${col.size}${col.nullable ? ', Nullable' : ''}`}>
            <Tag color={getColumnTypeColor(col.type)} size="small">
              {col.type}
            </Tag>
          </Tooltip>
        </Space>
      ),
      dataIndex: col.name,
      key: col.name,
      width: getColumnWidth(col),
      ellipsis: true,
      sorter: isColumnSortable(col.type),
      sortOrder: sortedInfo.columnKey === col.name && sortedInfo.order,
      render: (value: any) => renderCellValue(value, col.type),
      filterDropdown: ({ setSelectedKeys, selectedKeys, confirm, clearFilters }) => (
        <div style={{ padding: 8 }}>
          <Input
            placeholder={`Search ${col.name}`}
            value={selectedKeys[0]}
            onChange={e => setSelectedKeys(e.target.value ? [e.target.value] : [])}
            onPressEnter={() => confirm()}
            style={{ marginBottom: 8, display: 'block' }}
          />
          <Space>
            <Button
              type="primary"
              onClick={() => confirm()}
              icon={<SearchOutlined />}
              size="small"
            >
              Search
            </Button>
            <Button onClick={() => clearFilters?.()} size="small">
              Reset
            </Button>
          </Space>
        </div>
      ),
      filterIcon: (filtered: boolean) => (
        <SearchOutlined style={{ color: filtered ? '#1890ff' : undefined }} />
      ),
      onFilter: (value, record) =>
        record[col.name]
          ? record[col.name].toString().toLowerCase().includes((value as string).toLowerCase())
          : false
    }));
  }, [result.columns, result.data, sortedInfo]);

  const getColumnTypeColor = (type: string): string => {
    switch (type.toLowerCase()) {
      case 'string':
      case 'varchar':
      case 'text':
        return 'blue';
      case 'integer':
      case 'bigint':
      case 'int':
        return 'green';
      case 'decimal':
      case 'double':
      case 'float':
        return 'orange';
      case 'boolean':
        return 'purple';
      case 'date':
      case 'datetime':
      case 'timestamp':
        return 'cyan';
      default:
        return 'default';
    }
  };

  const getColumnWidth = (col: ColumnMetadata): number => {
    switch (col.type.toLowerCase()) {
      case 'boolean':
        return 80;
      case 'date':
        return 120;
      case 'datetime':
      case 'timestamp':
        return 180;
      case 'integer':
      case 'bigint':
        return 100;
      default:
        return Math.min(Math.max(col.size * 8, 120), 300);
    }
  };

  const isColumnSortable = (type: string): boolean => {
    return ['string', 'integer', 'bigint', 'decimal', 'double', 'date', 'datetime', 'timestamp'].includes(type.toLowerCase());
  };

  const renderCellValue = (value: any, type: string): React.ReactNode => {
    if (value === null || value === undefined) {
      return <Text type="secondary" italic>NULL</Text>;
    }

    switch (type.toLowerCase()) {
      case 'boolean':
        return value ? (
          <Tag color="green" icon={<CheckCircleOutlined />}>TRUE</Tag>
        ) : (
          <Tag color="red" icon={<CloseCircleOutlined />}>FALSE</Tag>
        );
      
      case 'date':
      case 'datetime':
      case 'timestamp':
        return (
          <Tooltip title={value}>
            <Text code>{value}</Text>
          </Tooltip>
        );
      
      case 'decimal':
      case 'double':
      case 'float':
        return <Text code>{Number(value).toLocaleString()}</Text>;
      
      case 'integer':
      case 'bigint':
        return <Text code>{Number(value).toLocaleString()}</Text>;
      
      default:
        const stringValue = String(value);
        if (stringValue.length > 100) {
          return (
            <Tooltip title={stringValue}>
              <Text ellipsis>{stringValue.substring(0, 100)}...</Text>
            </Tooltip>
          );
        }
        return <Text>{stringValue}</Text>;
    }
  };

  const handleTableChange: TableProps<any>['onChange'] = (pagination, filters, sorter) => {
    setSortedInfo(sorter);
  };

  const handleExport = (format: string) => {
    if (onExport) {
      onExport(format);
    } else {
      message.info(`Exporting ${filteredData.length} rows to ${format.toUpperCase()}...`);
    }
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text).then(() => {
      message.success('Copied to clipboard');
    });
  };

  const showRawData = () => {
    Modal.info({
      title: 'Raw Query Result',
      content: (
        <pre style={{ maxHeight: '400px', overflow: 'auto' }}>
          {JSON.stringify(result, null, 2)}
        </pre>
      ),
      width: 800
    });
  };

  const exportMenu = {
    items: [
      {
        key: 'csv',
        label: 'Export as CSV',
        icon: <FileTextOutlined />,
        onClick: () => handleExport('csv')
      },
      {
        key: 'json',
        label: 'Export as JSON',
        icon: <FileTextOutlined />,
        onClick: () => handleExport('json')
      },
      {
        key: 'excel',
        label: 'Export as Excel',
        icon: <FileTextOutlined />,
        onClick: () => handleExport('excel')
      }
    ]
  };

  const rowSelection = {
    selectedRowKeys,
    onChange: (newSelectedRowKeys: React.Key[]) => {
      setSelectedRowKeys(newSelectedRowKeys);
    },
    getCheckboxProps: (record: any) => ({
      disabled: false,
      name: record.id,
    }),
  };

  // Render error state
  if (!result.success) {
    return (
      <Card title="Execution Error">
        <Alert
          message="Query Execution Failed"
          description={result.errorMessage}
          type="error"
          showIcon
          icon={<CloseCircleOutlined />}
        />
        {result.errorCode && (
          <div style={{ marginTop: '16px' }}>
            <Text strong>Error Code: </Text>
            <Text code>{result.errorCode}</Text>
          </div>
        )}
      </Card>
    );
  }

  // Render empty state
  if (!result.data || result.data.length === 0) {
    return (
      <Card title="Query Results">
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description="No data returned"
        />
        <div style={{ marginTop: '16px', textAlign: 'center' }}>
          <Text type="secondary">
            The query executed successfully but returned no results.
          </Text>
        </div>
      </Card>
    );
  }

  return (
    <Card
      title={
        <Row justify="space-between" align="middle">
          <Col>
            <Space>
              <DatabaseOutlined />
              <Text strong>Query Results</Text>
              {result.fromCache && (
                <Tag color="orange" icon={<ClockCircleOutlined />}>
                  From Cache
                </Tag>
              )}
              {result.truncated && (
                <Tag color="red" icon={<WarningOutlined />}>
                  Truncated
                </Tag>
              )}
            </Space>
          </Col>
          <Col>
            <Space>
              <Button
                icon={<EyeOutlined />}
                onClick={showRawData}
                size="small"
              >
                Raw Data
              </Button>
              <Button
                icon={<CopyOutlined />}
                onClick={() => copyToClipboard(JSON.stringify(result.data, null, 2))}
                size="small"
              >
                Copy
              </Button>
              <Dropdown menu={exportMenu} placement="bottomRight">
                <Button icon={<DownloadOutlined />} size="small">
                  Export
                </Button>
              </Dropdown>
            </Space>
          </Col>
        </Row>
      }
    >
      {/* Statistics Row */}
      <Row gutter={16} style={{ marginBottom: '16px' }}>
        <Col span={6}>
          <Statistic
            title="Total Rows"
            value={result.totalRows || result.rowCount}
            prefix={<DatabaseOutlined />}
          />
        </Col>
        <Col span={6}>
          <Statistic
            title="Execution Time"
            value={result.executionTime}
            suffix="ms"
            prefix={<ClockCircleOutlined />}
          />
        </Col>
        <Col span={6}>
          <Statistic
            title="Memory Usage"
            value={result.memoryUsage ? (result.memoryUsage / 1024 / 1024).toFixed(2) : 0}
            suffix="MB"
          />
        </Col>
        <Col span={6}>
          <Statistic
            title="CPU Usage"
            value={result.cpuUsage?.toFixed(2) || 0}
            suffix="%"
          />
        </Col>
      </Row>

      {/* Filters Row */}
      <Row gutter={16} style={{ marginBottom: '16px' }}>
        <Col span={8}>
          <Search
            placeholder="Search in all columns..."
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            onSearch={setSearchText}
            allowClear
          />
        </Col>
        <Col span={6}>
          <Select
            placeholder="Filter column"
            style={{ width: '100%' }}
            allowClear
            value={filterColumn}
            onChange={setFilterColumn}
          >
            {columns.map(col => (
              <Option key={col.key as string} value={col.key as string}>
                {col.title as string}
              </Option>
            ))}
          </Select>
        </Col>
        <Col span={6}>
          <Input
            placeholder="Filter value"
            value={filterValue}
            onChange={(e) => setFilterValue(e.target.value)}
            disabled={!filterColumn}
            allowClear
          />
        </Col>
        <Col span={4}>
          <Select
            placeholder="Page size"
            value={pageSize}
            onChange={setPageSize}
            style={{ width: '100%' }}
          >
            <Option value={25}>25 rows</Option>
            <Option value={50}>50 rows</Option>
            <Option value={100}>100 rows</Option>
            <Option value={200}>200 rows</Option>
          </Select>
        </Col>
      </Row>

      {/* Truncation Warning */}
      {result.truncated && (
        <Alert
          message="Results Truncated"
          description={`Showing first ${result.rowCount} of ${result.totalRows} rows. Use pagination or filters to view more data.`}
          type="warning"
          showIcon
          style={{ marginBottom: '16px' }}
        />
      )}

      {/* Selected Rows Info */}
      {selectedRowKeys.length > 0 && (
        <Alert
          message={`${selectedRowKeys.length} rows selected`}
          type="info"
          style={{ marginBottom: '16px' }}
          action={
            <Button size="small" onClick={() => setSelectedRowKeys([])}>
              Clear Selection
            </Button>
          }
        />
      )}

      {/* Data Table */}
      <Table
        columns={columns}
        dataSource={filteredData}
        rowKey={(record, index) => index || 0}
        pagination={{
          current: currentPage,
          pageSize: pageSize,
          total: filteredData.length,
          showTotal: (total, range) => 
            `${range[0]}-${range[1]} of ${total} rows`,
          showSizeChanger: false,
          onChange: setCurrentPage
        }}
        rowSelection={rowSelection}
        onChange={handleTableChange}
        scroll={{ x: 'max-content', y: 400 }}
        size="small"
        bordered
      />

      {/* Footer Info */}
      <Row justify="space-between" style={{ marginTop: '16px', paddingTop: '16px', borderTop: '1px solid #f0f0f0' }}>
        <Col>
          <Text type="secondary">
            Query Type: <Text code>{result.queryType}</Text>
            {result.affectedRows !== undefined && (
              <>
                {' '} | Affected Rows: <Text code>{result.affectedRows}</Text>
              </>
            )}
          </Text>
        </Col>
        <Col>
          <Text type="secondary">
            Executed at: {result.startTime ? new Date(result.startTime).toLocaleString() : 'Unknown'}
          </Text>
        </Col>
      </Row>
    </Card>
  );
};

export default ResultDisplay;