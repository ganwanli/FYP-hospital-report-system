import React, { useState, useEffect } from 'react';
import {
  Modal,
  Form,
  Select,
  Input,
  InputNumber,
  Switch,
  Button,
  Space,
  Typography,
  Divider,
  Radio,
  Card,
  message,
  Alert,
} from 'antd';
import {
  DownloadOutlined,
  FileOutlined,
  FilePdfOutlined,
  FileExcelOutlined,
  FileWordOutlined,
  FileImageOutlined,
} from '@ant-design/icons';
import type { ExportOptions, ReportGenerationResult } from '../../services/reportView';

const { Option } = Select;
const { Text } = Typography;

interface ExportModalProps {
  visible: boolean;
  onCancel: () => void;
  onExport: (format: string, options: ExportOptions) => void;
  reportData?: ReportGenerationResult | null;
}

const ExportModal: React.FC<ExportModalProps> = ({
  visible,
  onCancel,
  onExport,
  reportData,
}) => {
  const [form] = Form.useForm();
  const [selectedFormat, setSelectedFormat] = useState<string>('PDF');
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    if (visible) {
      form.resetFields();
      setSelectedFormat('PDF');
    }
  }, [visible, form]);

  const handleExport = async () => {
    try {
      const values = await form.validateFields();
      setExporting(true);
      
      const options: ExportOptions = {
        ...values,
      };
      
      await onExport(selectedFormat, options);
      
    } catch (error) {
      console.error('Export validation failed:', error);
    } finally {
      setExporting(false);
    }
  };

  const formatOptions = [
    {
      value: 'PDF',
      label: 'PDF文档',
      icon: <FilePdfOutlined style={{ color: '#ff4d4f' }} />,
      description: '适合打印和正式文档分发',
      supports: ['pageSize', 'orientation'],
    },
    {
      value: 'EXCEL',
      label: 'Excel表格',
      icon: <FileExcelOutlined style={{ color: '#52c41a' }} />,
      description: '适合数据分析和进一步编辑',
      supports: ['sheetName', 'includeHeader'],
    },
    {
      value: 'WORD',
      label: 'Word文档',
      icon: <FileWordOutlined style={{ color: '#1890ff' }} />,
      description: '适合文档编辑和格式调整',
      supports: [],
    },
    {
      value: 'CSV',
      label: 'CSV数据',
      icon: <FileOutlined />,
      description: '适合数据导入和系统集成',
      supports: ['delimiter', 'includeHeader'],
    },
    {
      value: 'PNG',
      label: 'PNG图片',
      icon: <FileImageOutlined style={{ color: '#722ed1' }} />,
      description: '适合图片展示和网页使用',
      supports: ['width', 'height', 'quality'],
    },
    {
      value: 'JPEG',
      label: 'JPEG图片',
      icon: <FileImageOutlined style={{ color: '#fa8c16' }} />,
      description: '适合压缩和快速分享',
      supports: ['width', 'height', 'quality'],
    },
  ];

  const currentFormat = formatOptions.find(f => f.value === selectedFormat);

  const renderFormatSpecificOptions = () => {
    if (!currentFormat?.supports.length) return null;

    return (
      <Card size="small" title="格式设置" style={{ marginTop: '16px' }}>
        {currentFormat.supports.includes('pageSize') && (
          <Form.Item label="页面大小" name="pageSize" initialValue="A4">
            <Select>
              <Option value="A4">A4</Option>
              <Option value="A3">A3</Option>
              <Option value="LETTER">Letter</Option>
              <Option value="LEGAL">Legal</Option>
            </Select>
          </Form.Item>
        )}

        {currentFormat.supports.includes('orientation') && (
          <Form.Item label="页面方向" name="orientation" initialValue="PORTRAIT">
            <Radio.Group>
              <Radio value="PORTRAIT">纵向</Radio>
              <Radio value="LANDSCAPE">横向</Radio>
            </Radio.Group>
          </Form.Item>
        )}

        {currentFormat.supports.includes('sheetName') && (
          <Form.Item 
            label="工作表名称" 
            name="sheetName" 
            initialValue={reportData?.reportName || '报表数据'}
            rules={[{ max: 31, message: '工作表名称不能超过31个字符' }]}
          >
            <Input placeholder="请输入工作表名称" />
          </Form.Item>
        )}

        {currentFormat.supports.includes('delimiter') && (
          <Form.Item label="分隔符" name="delimiter" initialValue=",">
            <Select>
              <Option value=",">逗号 (,)</Option>
              <Option value=";">分号 (;)</Option>
              <Option value="\t">制表符 (Tab)</Option>
              <Option value="|">竖线 (|)</Option>
            </Select>
          </Form.Item>
        )}

        {currentFormat.supports.includes('includeHeader') && (
          <Form.Item label="包含表头" name="includeHeader" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
        )}

        {currentFormat.supports.includes('width') && (
          <Form.Item label="图片宽度" name="width" initialValue={1200}>
            <InputNumber min={100} max={5000} addonAfter="px" style={{ width: '100%' }} />
          </Form.Item>
        )}

        {currentFormat.supports.includes('height') && (
          <Form.Item label="图片高度" name="height" initialValue={800}>
            <InputNumber min={100} max={5000} addonAfter="px" style={{ width: '100%' }} />
          </Form.Item>
        )}

        {currentFormat.supports.includes('quality') && (
          <Form.Item label="图片质量" name="quality" initialValue={90}>
            <InputNumber min={10} max={100} addonAfter="%" style={{ width: '100%' }} />
          </Form.Item>
        )}
      </Card>
    );
  };

  return (
    <Modal
      title={
        <Space>
          <DownloadOutlined />
          <span>导出报表</span>
        </Space>
      }
      open={visible}
      onCancel={onCancel}
      width={600}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          取消
        </Button>,
        <Button
          key="export"
          type="primary"
          icon={<DownloadOutlined />}
          loading={exporting}
          onClick={handleExport}
          disabled={!reportData}
        >
          导出 {selectedFormat}
        </Button>,
      ]}
    >
      {!reportData && (
        <Alert
          message="无法导出"
          description="请先生成报表数据"
          type="warning"
          showIcon
          style={{ marginBottom: '16px' }}
        />
      )}

      <Form form={form} layout="vertical" disabled={!reportData}>
        <Form.Item label="选择导出格式">
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '12px' }}>
            {formatOptions.map(format => (
              <Card
                key={format.value}
                size="small"
                hoverable
                onClick={() => setSelectedFormat(format.value)}
                style={{
                  cursor: 'pointer',
                  border: selectedFormat === format.value ? '2px solid #1890ff' : '1px solid #e8e8e8',
                  backgroundColor: selectedFormat === format.value ? '#f6ffed' : 'white',
                }}
                bodyStyle={{ padding: '12px' }}
              >
                <Space direction="vertical" size="small" style={{ width: '100%' }}>
                  <Space>
                    {format.icon}
                    <Text strong>{format.label}</Text>
                  </Space>
                  <Text type="secondary" style={{ fontSize: '12px' }}>
                    {format.description}
                  </Text>
                </Space>
              </Card>
            ))}
          </div>
        </Form.Item>

        {renderFormatSpecificOptions()}

        <Divider />

        <div style={{ background: '#f6ffed', padding: '12px', borderRadius: '4px' }}>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            <span style={{ color: '#52c41a', marginRight: '4px' }}>💡</span>
            导出格式说明：
            <ul style={{ margin: '8px 0 0 16px', paddingLeft: '0' }}>
              <li>PDF - 保持原始布局，适合打印和存档</li>
              <li>Excel - 仅导出表格数据，适合数据分析</li>
              <li>CSV - 纯数据格式，适合系统导入</li>
              <li>图片 - 可视化展示，适合分享和演示</li>
            </ul>
          </Text>
        </div>
      </Form>
    </Modal>
  );
};

export default ExportModal;