import React, { useState, useEffect } from 'react';
import {
  Modal,
  Form,
  Input,
  Select,
  Switch,
  DatePicker,
  InputNumber,
  Button,
  Space,
  Typography,
  Divider,
  Card,
  Tag,
  Alert,
  Tooltip,
  message,
} from 'antd';
import {
  ShareAltOutlined,
  LinkOutlined,
  CopyOutlined,
  EyeOutlined,
  LockOutlined,
  ClockCircleOutlined,
  TeamOutlined,
  DownloadOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import type { ReportShare } from '../../services/reportView';

const { Option } = Select;
const { Text } = Typography;
const { TextArea } = Input;

interface ShareModalProps {
  visible: boolean;
  onCancel: () => void;
  onShare: (shareData: Partial<ReportShare>) => void;
  reportId?: number;
  reportName?: string;
}

const ShareModal: React.FC<ShareModalProps> = ({
  visible,
  onCancel,
  onShare,
  reportId,
  reportName,
}) => {
  const [form] = Form.useForm();
  const [shareType, setShareType] = useState<string>('PUBLIC');
  const [allowExport, setAllowExport] = useState(true);
  const [hasExpiration, setHasExpiration] = useState(false);
  const [hasAccessLimit, setHasAccessLimit] = useState(false);
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    if (visible) {
      form.resetFields();
      setShareType('PUBLIC');
      setAllowExport(true);
      setHasExpiration(false);
      setHasAccessLimit(false);
      
      // 设置默认值
      form.setFieldsValue({
        shareTitle: reportName ? `${reportName} - 分享` : '报表分享',
        shareType: 'PUBLIC',
        allowExport: true,
        allowedFormats: ['PDF', 'EXCEL', 'CSV'],
      });
    }
  }, [visible, form, reportName]);

  const handleShare = async () => {
    try {
      const values = await form.validateFields();
      setCreating(true);
      
      const shareData: Partial<ReportShare> = {
        shareTitle: values.shareTitle,
        shareDescription: values.shareDescription,
        shareType: values.shareType,
        accessPassword: values.shareType === 'PASSWORD' ? values.accessPassword : undefined,
        expireTime: hasExpiration && values.expireTime ? values.expireTime.format('YYYY-MM-DD HH:mm:ss') : undefined,
        maxAccessCount: hasAccessLimit ? values.maxAccessCount : undefined,
        allowExport: values.allowExport,
        allowedFormats: values.allowExport ? JSON.stringify(values.allowedFormats || []) : undefined,
        isActive: true,
        createdBy: 1, // TODO: 从用户上下文获取
      };
      
      await onShare(shareData);
      
    } catch (error) {
      console.error('Share validation failed:', error);
    } finally {
      setCreating(false);
    }
  };

  const shareTypeOptions = [
    {
      value: 'PUBLIC',
      label: '公开分享',
      icon: <TeamOutlined style={{ color: '#52c41a' }} />,
      description: '任何人都可以通过链接访问',
      color: 'green'
    },
    {
      value: 'PASSWORD',
      label: '密码访问',
      icon: <LockOutlined style={{ color: '#fa8c16' }} />,
      description: '需要输入密码才能访问',
      color: 'orange'
    },
    {
      value: 'PRIVATE',
      label: '私有分享',
      icon: <EyeOutlined style={{ color: '#1890ff' }} />,
      description: '仅限指定用户访问',
      color: 'blue'
    },
  ];

  const currentShareType = shareTypeOptions.find(opt => opt.value === shareType);

  const formatOptions = [
    { label: 'PDF文档', value: 'PDF' },
    { label: 'Excel表格', value: 'EXCEL' },
    { label: 'Word文档', value: 'WORD' },
    { label: 'CSV数据', value: 'CSV' },
    { label: 'PNG图片', value: 'PNG' },
    { label: 'JPEG图片', value: 'JPEG' },
  ];

  return (
    <Modal
      title={
        <Space>
          <ShareAltOutlined />
          <span>分享报表</span>
        </Space>
      }
      open={visible}
      onCancel={onCancel}
      width={700}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          取消
        </Button>,
        <Button
          key="share"
          type="primary"
          icon={<ShareAltOutlined />}
          loading={creating}
          onClick={handleShare}
          disabled={!reportId}
        >
          创建分享
        </Button>,
      ]}
    >
      {!reportId && (
        <Alert
          message="无法分享"
          description="报表ID不存在"
          type="warning"
          showIcon
          style={{ marginBottom: '16px' }}
        />
      )}

      <Form form={form} layout="vertical" disabled={!reportId}>
        {/* 基础信息 */}
        <Card size="small" title="基础信息" style={{ marginBottom: '16px' }}>
          <Form.Item 
            label="分享标题" 
            name="shareTitle"
            rules={[{ required: true, message: '请输入分享标题' }]}
          >
            <Input placeholder="请输入分享标题" />
          </Form.Item>
          
          <Form.Item label="分享描述" name="shareDescription">
            <TextArea 
              rows={2} 
              placeholder="请输入分享描述（可选）"
              maxLength={200}
              showCount
            />
          </Form.Item>
        </Card>

        {/* 访问控制 */}
        <Card size="small" title="访问控制" style={{ marginBottom: '16px' }}>
          <Form.Item label="分享类型" name="shareType" required>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px' }}>
              {shareTypeOptions.map(option => (
                <Card
                  key={option.value}
                  size="small"
                  hoverable
                  onClick={() => {
                    setShareType(option.value);
                    form.setFieldValue('shareType', option.value);
                  }}
                  style={{
                    cursor: 'pointer',
                    border: shareType === option.value ? `2px solid ${option.color === 'green' ? '#52c41a' : option.color === 'orange' ? '#fa8c16' : '#1890ff'}` : '1px solid #e8e8e8',
                  }}
                  bodyStyle={{ padding: '12px' }}
                >
                  <Space direction="vertical" size="small" style={{ width: '100%' }}>
                    <Space>
                      {option.icon}
                      <Text strong>{option.label}</Text>
                      {shareType === option.value && <Tag color={option.color}>已选择</Tag>}
                    </Space>
                    <Text type="secondary" style={{ fontSize: '12px' }}>
                      {option.description}
                    </Text>
                  </Space>
                </Card>
              ))}
            </div>
          </Form.Item>

          {shareType === 'PASSWORD' && (
            <Form.Item 
              label="访问密码" 
              name="accessPassword"
              rules={[{ required: true, message: '请输入访问密码' }]}
            >
              <Input.Password placeholder="请输入4-20位访问密码" maxLength={20} />
            </Form.Item>
          )}
        </Card>

        {/* 访问限制 */}
        <Card size="small" title="访问限制" style={{ marginBottom: '16px' }}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Space>
                <ClockCircleOutlined />
                <Text>设置过期时间</Text>
              </Space>
              <Switch 
                checked={hasExpiration} 
                onChange={setHasExpiration}
                size="small"
              />
            </div>
            
            {hasExpiration && (
              <Form.Item 
                name="expireTime"
                rules={[{ required: true, message: '请选择过期时间' }]}
              >
                <DatePicker
                  showTime
                  placeholder="选择过期时间"
                  style={{ width: '100%' }}
                  disabledDate={(current) => current && current.isBefore(dayjs(), 'day')}
                />
              </Form.Item>
            )}

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Space>
                <TeamOutlined />
                <Text>限制访问次数</Text>
              </Space>
              <Switch 
                checked={hasAccessLimit} 
                onChange={setHasAccessLimit}
                size="small"
              />
            </div>
            
            {hasAccessLimit && (
              <Form.Item 
                name="maxAccessCount"
                rules={[{ required: true, message: '请输入最大访问次数' }]}
              >
                <InputNumber
                  min={1}
                  max={10000}
                  placeholder="最大访问次数"
                  style={{ width: '100%' }}
                  addonAfter="次"
                />
              </Form.Item>
            )}
          </Space>
        </Card>

        {/* 导出权限 */}
        <Card size="small" title="导出权限" style={{ marginBottom: '16px' }}>
          <Form.Item label="允许导出" name="allowExport" valuePropName="checked">
            <Switch 
              checked={allowExport}
              onChange={setAllowExport}
              checkedChildren="允许"
              unCheckedChildren="禁止"
            />
          </Form.Item>
          
          {allowExport && (
            <Form.Item label="允许的导出格式" name="allowedFormats">
              <Select
                mode="multiple"
                placeholder="选择允许的导出格式"
                style={{ width: '100%' }}
              >
                {formatOptions.map(format => (
                  <Option key={format.value} value={format.value}>
                    {format.label}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          )}
        </Card>

        <Divider />

        <div style={{ background: '#f6ffed', padding: '12px', borderRadius: '4px' }}>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <div>
                <span style={{ color: '#52c41a', marginRight: '4px' }}>🔒</span>
                <strong>分享安全提示：</strong>
              </div>
              <ul style={{ margin: '4px 0 0 16px', paddingLeft: '0' }}>
                <li>公开分享的链接可以被任何人访问，请谨慎使用</li>
                <li>设置密码可以增加访问安全性</li>
                <li>建议设置合理的过期时间和访问次数限制</li>
                <li>可以随时在分享管理中禁用或删除分享</li>
              </ul>
            </Space>
          </Text>
        </div>
      </Form>
    </Modal>
  );
};

export default ShareModal;