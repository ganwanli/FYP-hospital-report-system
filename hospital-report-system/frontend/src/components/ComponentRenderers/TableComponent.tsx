import React, { useMemo } from 'react';
import { Table } from 'antd';
import { ReportComponent } from '../../services/report';

interface TableComponentProps {
  component: ReportComponent;
}

const TableComponent: React.FC<TableComponentProps> = ({ component }) => {
  const tableConfig = useMemo(() => {
    try {
      return component.tableConfig ? JSON.parse(component.tableConfig) : {};
    } catch {
      return {};
    }
  }, [component.tableConfig]);

  const dataConfig = useMemo(() => {
    try {
      return component.dataConfig ? JSON.parse(component.dataConfig) : {};
    } catch {
      return {};
    }
  }, [component.dataConfig]);

  const styleConfig = useMemo(() => {
    try {
      return component.styleConfig ? JSON.parse(component.styleConfig) : {};
    } catch {
      return {};
    }
  }, [component.styleConfig]);

  // Sample data for design mode
  const sampleData = [
    { key: 1, name: '患者A', age: 25, gender: '男', department: '内科', status: '正常' },
    { key: 2, name: '患者B', age: 30, gender: '女', department: '外科', status: '异常' },
    { key: 3, name: '患者C', age: 35, gender: '男', department: '儿科', status: '正常' },
    { key: 4, name: '患者D', age: 28, gender: '女', department: '妇科', status: '正常' },
    { key: 5, name: '患者E', age: 42, gender: '男', department: '骨科', status: '异常' },
  ];

  const defaultColumns = [
    {
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
      width: 80,
    },
    {
      title: '年龄',
      dataIndex: 'age',
      key: 'age',
      width: 60,
    },
    {
      title: '性别',
      dataIndex: 'gender',
      key: 'gender',
      width: 60,
    },
    {
      title: '科室',
      dataIndex: 'department',
      key: 'department',
      width: 80,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: string) => (
        <span style={{ color: status === '正常' ? '#52c41a' : '#ff4d4f' }}>
          {status}
        </span>
      ),
    },
  ];

  const columns = tableConfig.columns || defaultColumns;
  const dataSource = dataConfig.data || sampleData;

  return (
    <div style={{ width: '100%', height: '100%', overflow: 'hidden' }}>
      <Table
        columns={columns}
        dataSource={dataSource}
        size={tableConfig.size || 'small'}
        pagination={tableConfig.pagination !== false ? {
          pageSize: tableConfig.pageSize || 5,
          showSizeChanger: false,
          showQuickJumper: false,
          showTotal: (total, range) => `${range[0]}-${range[1]} 共 ${total} 条`,
        } : false}
        bordered={tableConfig.bordered !== false}
        showHeader={tableConfig.showHeader !== false}
        scroll={{ y: component.height - (tableConfig.pagination !== false ? 60 : 20) }}
        style={{
          fontSize: styleConfig.fontSize || '12px',
          ...styleConfig,
        }}
      />
    </div>
  );
};

export default TableComponent;