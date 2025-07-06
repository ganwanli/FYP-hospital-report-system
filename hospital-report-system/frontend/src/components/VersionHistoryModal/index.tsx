import React, { useState, useEffect } from 'react';
import {
  Modal,
  Table,
  Button,
  message,
  Space,
  Typography,
  Tag,
  Tooltip,
  Popconfirm,
} from 'antd';
import {
  HistoryOutlined,
  RestoreOutlined,
  DeleteOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { reportApi } from '../../services/report';

const { Text } = Typography;

interface VersionHistoryModalProps {
  visible: boolean;
  reportId?: number;
  onCancel: () => void;
  onRestore: (versionId: number) => void;
}

interface VersionRecord {
  versionId: number;
  version: string;
  description: string;
  createdBy: string;
  createdTime: string;
  isCurrent: boolean;
  changeCount: number;
}

const VersionHistoryModal: React.FC<VersionHistoryModalProps> = ({
  visible,
  reportId,
  onCancel,
  onRestore,
}) => {
  const [loading, setLoading] = useState(false);
  const [versions, setVersions] = useState<VersionRecord[]>([]);

  useEffect(() => {
    if (visible && reportId) {
      loadVersions();
    }
  }, [visible, reportId]);

  const loadVersions = async () => {
    if (!reportId) return;
    
    setLoading(true);
    try {
      const response = await reportApi.getReportVersions(reportId);
      setVersions(response.data || []);
    } catch (error) {
      message.error('版本历史加载失败');
      console.error('Load versions error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleRestore = async (versionId: number, version: string) => {
    if (!reportId) return;
    
    try {
      await reportApi.restoreVersion(reportId, versionId);
      message.success(`已恢复到版本 ${version}`);
      onRestore(versionId);
      onCancel();
    } catch (error) {
      message.error('版本恢复失败');
      console.error('Restore version error:', error);
    }
  };

  const handleSaveCurrentVersion = async () => {
    if (!reportId) return;
    
    Modal.confirm({
      title: '保存当前版本',
      content: '请输入版本描述',
      okText: '保存',
      cancelText: '取消',
      onOk: async (close) => {
        try {
          const description = `版本保存于 ${new Date().toLocaleString()}`;
          await reportApi.saveVersion(reportId, description, 1); // TODO: 获取当前用户ID
          message.success('版本保存成功');
          loadVersions();
          close();
        } catch (error) {
          message.error('版本保存失败');
        }
      },
    });
  };

  const formatDateTime = (dateTime: string) => {
    return new Date(dateTime).toLocaleString('zh-CN');
  };

  const columns = [
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      width: 100,
      render: (version: string, record: VersionRecord) => (
        <Space>
          <Text strong>{version}</Text>
          {record.isCurrent && <Tag color="blue">当前</Tag>}
        </Space>
      ),
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '修改者',
      dataIndex: 'createdBy',
      key: 'createdBy',
      width: 100,
    },
    {
      title: '创建时间',
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 160,
      render: formatDateTime,
    },
    {
      title: '变更数',
      dataIndex: 'changeCount',
      key: 'changeCount',
      width: 80,
      render: (count: number) => (
        <Tag color={count > 10 ? 'red' : count > 5 ? 'orange' : 'green'}>
          {count}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'actions',
      width: 120,
      render: (_, record: VersionRecord) => (
        <Space size="small">
          <Tooltip title="预览此版本">
            <Button
              type="text"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => {
                message.info('预览功能开发中...');
              }}
            />
          </Tooltip>
          
          {!record.isCurrent && (
            <Tooltip title="恢复到此版本">
              <Popconfirm
                title={`确定要恢复到版本 ${record.version} 吗？`}
                description="这将覆盖当前的报表配置"
                onConfirm={() => handleRestore(record.versionId, record.version)}
                okText="确定"
                cancelText="取消"
              >
                <Button
                  type="text"
                  size="small"
                  icon={<RestoreOutlined />}
                />
              </Popconfirm>
            </Tooltip>
          )}
          
          <Tooltip title="删除版本">
            <Popconfirm
              title="确定要删除此版本吗？"
              description="删除后无法恢复"
              onConfirm={() => {
                message.info('删除功能开发中...');
              }}
              okText="确定"
              cancelText="取消"
              disabled={record.isCurrent}
            >
              <Button
                type="text"
                size="small"
                icon={<DeleteOutlined />}
                danger
                disabled={record.isCurrent}
              />
            </Popconfirm>
          </Tooltip>
        </Space>
      ),
    },
  ];

  return (
    <Modal
      title={
        <Space>
          <HistoryOutlined />
          <Text strong>版本历史</Text>
        </Space>
      }
      open={visible}
      onCancel={onCancel}
      width={800}
      footer={[
        <Button key="save" type="primary" onClick={handleSaveCurrentVersion}>
          保存当前版本
        </Button>,
        <Button key="close" onClick={onCancel}>
          关闭
        </Button>,
      ]}
      bodyStyle={{ padding: '16px' }}
    >
      <div style={{ marginBottom: '16px' }}>
        <Text type="secondary">
          版本历史记录了报表的所有变更，您可以随时恢复到之前的版本。
        </Text>
      </div>
      
      <Table
        columns={columns}
        dataSource={versions}
        rowKey="versionId"
        loading={loading}
        size="small"
        pagination={{
          pageSize: 10,
          showSizeChanger: false,
          showQuickJumper: true,
          showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
        }}
        scroll={{ y: 400 }}
        locale={{
          emptyText: '暂无版本历史',
        }}
      />
      
      <div style={{ marginTop: '16px', fontSize: '12px', color: '#999' }}>
        <Text type="secondary">
          💡 提示：建议在重要修改前保存版本，以便后续回滚
        </Text>
      </div>
    </Modal>
  );
};

export default VersionHistoryModal;