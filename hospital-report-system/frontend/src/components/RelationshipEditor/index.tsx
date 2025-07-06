import React, { useState, useCallback } from 'react';
import {
  Modal,
  Form,
  Input,
  Select,
  Button,
  Space,
  Row,
  Col,
  Card,
  Tag,
  Divider,
  message,
  Popconfirm,
} from 'antd';
import {
  PlusOutlined,
  DeleteOutlined,
  EditOutlined,
  SaveOutlined,
  CloseOutlined,
} from '@ant-design/icons';
import { buildLineageRelation, verifyLineageRelation } from '@/services/lineage';

const { Option } = Select;
const { TextArea } = Input;

interface LineageRelation {
  id?: number;
  sourceNodeId: string;
  targetNodeId: string;
  relationType: string;
  transformRule?: string;
  metadata?: any;
  verified?: boolean;
  confidence?: number;
}

interface RelationshipEditorProps {
  visible: boolean;
  onClose: () => void;
  onSave?: (relation: LineageRelation) => void;
  initialData?: LineageRelation;
  sourceNodeOptions?: any[];
  targetNodeOptions?: any[];
}

const RelationshipEditor: React.FC<RelationshipEditorProps> = ({
  visible,
  onClose,
  onSave,
  initialData,
  sourceNodeOptions = [],
  targetNodeOptions = [],
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [metadataFields, setMetadataFields] = useState<Array<{ key: string; value: string }>>([]);

  const relationTypeOptions = [
    { value: 'DERIVED', label: '派生关系', color: 'blue' },
    { value: 'TRANSFORM', label: '转换关系', color: 'green' },
    { value: 'COPY', label: '复制关系', color: 'orange' },
    { value: 'AGGREGATE', label: '聚合关系', color: 'purple' },
    { value: 'JOIN', label: '关联关系', color: 'cyan' },
    { value: 'FILTER', label: '过滤关系', color: 'magenta' },
    { value: 'LOOKUP', label: '查找关系', color: 'volcano' },
    { value: 'UNION', label: '联合关系', color: 'geekblue' },
  ];

  const addMetadataField = useCallback(() => {
    setMetadataFields(prev => [...prev, { key: '', value: '' }]);
  }, []);

  const removeMetadataField = useCallback((index: number) => {
    setMetadataFields(prev => prev.filter((_, i) => i !== index));
  }, []);

  const updateMetadataField = useCallback((index: number, field: 'key' | 'value', value: string) => {
    setMetadataFields(prev => 
      prev.map((item, i) => 
        i === index ? { ...item, [field]: value } : item
      )
    );
  }, []);

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      // 构建元数据对象
      const metadata: any = {};
      metadataFields.forEach(field => {
        if (field.key && field.value) {
          metadata[field.key] = field.value;
        }
      });

      const relationData: LineageRelation = {
        ...values,
        metadata: Object.keys(metadata).length > 0 ? metadata : undefined,
      };

      if (initialData?.id) {
        // 更新已有关系
        await verifyLineageRelation(initialData.id, 'VERIFIED', '通过关系编辑器验证');
        message.success('关系更新成功');
      } else {
        // 创建新关系
        await buildLineageRelation(relationData);
        message.success('关系创建成功');
      }

      onSave?.(relationData);
      onClose();
    } catch (error) {
      message.error('保存失败');
      console.error('Save relation error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    form.resetFields();
    setMetadataFields([]);
    onClose();
  };

  // 初始化表单数据
  React.useEffect(() => {
    if (visible && initialData) {
      form.setFieldsValue(initialData);
      if (initialData.metadata) {
        const fields = Object.entries(initialData.metadata).map(([key, value]) => ({
          key,
          value: String(value),
        }));
        setMetadataFields(fields);
      }
    } else if (visible) {
      form.resetFields();
      setMetadataFields([]);
    }
  }, [visible, initialData, form]);

  return (
    <Modal
      title={initialData?.id ? '编辑血缘关系' : '创建血缘关系'}
      open={visible}
      onCancel={handleCancel}
      width={800}
      footer={
        <Space>
          <Button icon={<CloseOutlined />} onClick={handleCancel}>
            取消
          </Button>
          <Button
            type="primary"
            icon={<SaveOutlined />}
            loading={loading}
            onClick={handleSave}
          >
            保存
          </Button>
        </Space>
      }
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          relationType: 'DERIVED',
        }}
      >
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              name="sourceNodeId"
              label="源节点"
              rules={[{ required: true, message: '请选择源节点' }]}
            >
              <Select
                showSearch
                placeholder="选择源节点"
                optionFilterProp="children"
                filterOption={(input, option) =>
                  option?.children?.toString().toLowerCase().includes(input.toLowerCase())
                }
              >
                {sourceNodeOptions.map(option => (
                  <Option key={option.value} value={option.value}>
                    {option.label}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              name="targetNodeId"
              label="目标节点"
              rules={[{ required: true, message: '请选择目标节点' }]}
            >
              <Select
                showSearch
                placeholder="选择目标节点"
                optionFilterProp="children"
                filterOption={(input, option) =>
                  option?.children?.toString().toLowerCase().includes(input.toLowerCase())
                }
              >
                {targetNodeOptions.map(option => (
                  <Option key={option.value} value={option.value}>
                    {option.label}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={16}>
          <Col span={24}>
            <Form.Item
              name="relationType"
              label="关系类型"
              rules={[{ required: true, message: '请选择关系类型' }]}
            >
              <Select placeholder="选择关系类型">
                {relationTypeOptions.map(option => (
                  <Option key={option.value} value={option.value}>
                    <Tag color={option.color}>{option.label}</Tag>
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={16}>
          <Col span={24}>
            <Form.Item
              name="transformRule"
              label="转换规则"
              tooltip="描述数据从源节点到目标节点的转换逻辑"
            >
              <TextArea
                rows={4}
                placeholder="例如: target_field = UPPER(source_field) 或描述具体的业务转换逻辑"
              />
            </Form.Item>
          </Col>
        </Row>

        <Divider>元数据信息</Divider>

        <Card 
          title="元数据键值对" 
          size="small"
          extra={
            <Button
              type="dashed"
              icon={<PlusOutlined />}
              onClick={addMetadataField}
              size="small"
            >
              添加元数据
            </Button>
          }
        >
          {metadataFields.map((field, index) => (
            <Row key={index} gutter={8} style={{ marginBottom: 8 }}>
              <Col span={8}>
                <Input
                  placeholder="键名"
                  value={field.key}
                  onChange={(e) => updateMetadataField(index, 'key', e.target.value)}
                />
              </Col>
              <Col span={12}>
                <Input
                  placeholder="值"
                  value={field.value}
                  onChange={(e) => updateMetadataField(index, 'value', e.target.value)}
                />
              </Col>
              <Col span={4}>
                <Popconfirm
                  title="确定删除这个元数据字段吗？"
                  onConfirm={() => removeMetadataField(index)}
                  okText="确定"
                  cancelText="取消"
                >
                  <Button
                    type="text"
                    danger
                    icon={<DeleteOutlined />}
                    size="small"
                  />
                </Popconfirm>
              </Col>
            </Row>
          ))}
          
          {metadataFields.length === 0 && (
            <div style={{ 
              textAlign: 'center', 
              color: '#999', 
              padding: '20px 0' 
            }}>
              暂无元数据，点击上方按钮添加
            </div>
          )}
        </Card>

        <div style={{ marginTop: 16, padding: 16, backgroundColor: '#f6f8fa', borderRadius: 6 }}>
          <h4 style={{ margin: '0 0 8px 0', color: '#666' }}>使用说明：</h4>
          <ul style={{ margin: 0, paddingLeft: 16, color: '#666', fontSize: '12px' }}>
            <li>选择源节点和目标节点建立血缘关系</li>
            <li>选择合适的关系类型来描述数据流向</li>
            <li>转换规则用于描述数据处理逻辑</li>
            <li>元数据可以存储额外的关系属性信息</li>
          </ul>
        </div>
      </Form>
    </Modal>
  );
};

export default RelationshipEditor;