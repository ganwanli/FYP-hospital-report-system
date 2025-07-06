import React, { useState, useEffect } from 'react';
import {
  Card,
  Form,
  Input,
  InputNumber,
  Select,
  DatePicker,
  Switch,
  Button,
  Space,
  Typography,
  Divider,
  Tooltip,
  Alert,
  Collapse,
} from 'antd';
import {
  SearchOutlined,
  ReloadOutlined,
  InfoCircleOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import type { ReportParameterDefinition } from '../../services/reportView';

const { Panel } = Collapse;
const { Text } = Typography;
const { Option } = Select;
const { TextArea } = Input;

interface ReportParameterPanelProps {
  parameters: ReportParameterDefinition[];
  values: Record<string, any>;
  onChange: (values: Record<string, any>) => void;
  onSubmit: () => void;
  loading?: boolean;
}

const ReportParameterPanel: React.FC<ReportParameterPanelProps> = ({
  parameters,
  values,
  onChange,
  onSubmit,
  loading = false,
}) => {
  const [form] = Form.useForm();
  const [expandedPanels, setExpandedPanels] = useState<string[]>(['basic']);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    form.setFieldsValue(values);
  }, [values, form]);

  const handleValuesChange = (changedValues: any, allValues: any) => {
    onChange(allValues);
    
    // 清除已更正的错误
    const newErrors = { ...errors };
    Object.keys(changedValues).forEach(key => {
      if (newErrors[key]) {
        delete newErrors[key];
      }
    });
    setErrors(newErrors);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const validationErrors: Record<string, string> = {};
      
      // 自定义验证
      parameters.forEach(param => {
        const value = values[param.name];
        
        if (param.required && (value === undefined || value === null || value === '')) {
          validationErrors[param.name] = `${param.label} 是必填项`;
          return;
        }
        
        if (value !== undefined && value !== null && param.validation) {
          const { min, max, pattern, message } = param.validation;
          
          if (typeof value === 'number') {
            if (min !== undefined && value < min) {
              validationErrors[param.name] = message || `${param.label} 不能小于 ${min}`;
              return;
            }
            if (max !== undefined && value > max) {
              validationErrors[param.name] = message || `${param.label} 不能大于 ${max}`;
              return;
            }
          }
          
          if (typeof value === 'string' && pattern) {
            const regex = new RegExp(pattern);
            if (!regex.test(value)) {
              validationErrors[param.name] = message || `${param.label} 格式不正确`;
              return;
            }
          }
        }
      });
      
      if (Object.keys(validationErrors).length > 0) {
        setErrors(validationErrors);
        return;
      }
      
      setErrors({});
      onSubmit();
      
    } catch (errorInfo) {
      console.error('Validation failed:', errorInfo);
    }
  };

  const handleReset = () => {
    const defaultValues: Record<string, any> = {};
    parameters.forEach(param => {
      if (param.defaultValue !== undefined) {
        defaultValues[param.name] = param.defaultValue;
      }
    });
    
    form.setFieldsValue(defaultValues);
    onChange(defaultValues);
    setErrors({});
  };

  const renderParameterInput = (param: ReportParameterDefinition) => {
    const error = errors[param.name];
    const status = error ? 'error' : undefined;
    
    const commonProps = {
      placeholder: param.placeholder || `请输入${param.label}`,
      status,
    };

    switch (param.dataType) {
      case 'number':
        return (
          <InputNumber
            {...commonProps}
            style={{ width: '100%' }}
            min={param.validation?.min}
            max={param.validation?.max}
            precision={0}
          />
        );
        
      case 'boolean':
        return (
          <Switch
            checkedChildren="是"
            unCheckedChildren="否"
          />
        );
        
      case 'date':
        return (
          <DatePicker
            {...commonProps}
            style={{ width: '100%' }}
            format="YYYY-MM-DD"
          />
        );
        
      case 'select':
        return (
          <Select
            {...commonProps}
            style={{ width: '100%' }}
            allowClear
            showSearch
            filterOption={(input, option) =>
              (option?.children as unknown as string)?.toLowerCase().includes(input.toLowerCase())
            }
          >
            {param.options?.map(option => (
              <Option key={option.value} value={option.value}>
                {option.label}
              </Option>
            ))}
          </Select>
        );
        
      default:
        if (param.placeholder?.includes('多行') || param.description?.includes('多行')) {
          return (
            <TextArea
              {...commonProps}
              rows={3}
              maxLength={500}
              showCount
            />
          );
        }
        
        return (
          <Input
            {...commonProps}
            maxLength={100}
          />
        );
    }
  };

  const groupedParameters = parameters.reduce((groups, param) => {
    // 根据参数名称前缀或类型进行分组
    let group = 'basic';
    
    if (param.name.startsWith('date') || param.dataType === 'date') {
      group = 'date';
    } else if (param.name.includes('filter') || param.name.includes('condition')) {
      group = 'filter';
    } else if (param.name.includes('option') || param.name.includes('setting')) {
      group = 'options';
    }
    
    if (!groups[group]) {
      groups[group] = [];
    }
    groups[group].push(param);
    
    return groups;
  }, {} as Record<string, ReportParameterDefinition[]>);

  const getGroupTitle = (groupKey: string) => {
    switch (groupKey) {
      case 'basic': return '基础参数';
      case 'date': return '日期参数';
      case 'filter': return '过滤条件';
      case 'options': return '选项设置';
      default: return '其他参数';
    }
  };

  const getGroupIcon = (groupKey: string) => {
    switch (groupKey) {
      case 'basic': return <SettingOutlined />;
      case 'date': return <InfoCircleOutlined />;
      case 'filter': return <SearchOutlined />;
      case 'options': return <SettingOutlined />;
      default: return <InfoCircleOutlined />;
    }
  };

  if (parameters.length === 0) {
    return (
      <Card size="small" title="参数设置" style={{ margin: '8px' }}>
        <div style={{ textAlign: 'center', padding: '20px', color: '#999' }}>
          此报表无需参数
        </div>
      </Card>
    );
  }

  return (
    <Card 
      size="small" 
      title={
        <Space>
          <SettingOutlined />
          <Text strong>参数设置</Text>
        </Space>
      }
      style={{ margin: '8px', height: 'calc(100% - 16px)' }}
      bodyStyle={{ 
        padding: '12px', 
        height: 'calc(100% - 57px)', 
        overflow: 'auto' 
      }}
    >
      <Form
        form={form}
        layout="vertical"
        size="small"
        onValuesChange={handleValuesChange}
        initialValues={values}
      >
        <Collapse
          activeKey={expandedPanels}
          onChange={setExpandedPanels}
          ghost
          size="small"
        >
          {Object.entries(groupedParameters).map(([groupKey, groupParams]) => (
            <Panel
              key={groupKey}
              header={
                <Space>
                  {getGroupIcon(groupKey)}
                  <Text strong>{getGroupTitle(groupKey)}</Text>
                  <Text type="secondary">({groupParams.length})</Text>
                </Space>
              }
            >
              {groupParams.map(param => (
                <div key={param.name}>
                  <Form.Item
                    label={
                      <Space>
                        <Text strong>{param.label}</Text>
                        {param.required && <Text type="danger">*</Text>}
                        {param.description && (
                          <Tooltip title={param.description}>
                            <InfoCircleOutlined style={{ color: '#999' }} />
                          </Tooltip>
                        )}
                      </Space>
                    }
                    name={param.name}
                    validateStatus={errors[param.name] ? 'error' : undefined}
                    help={errors[param.name]}
                    rules={[
                      {
                        required: param.required,
                        message: `请输入${param.label}`,
                      },
                    ]}
                  >
                    {renderParameterInput(param)}
                  </Form.Item>
                </div>
              ))}
            </Panel>
          ))}
        </Collapse>
        
        <Divider />
        
        <Space direction="vertical" style={{ width: '100%' }}>
          <Button
            type="primary"
            icon={<SearchOutlined />}
            onClick={handleSubmit}
            loading={loading}
            block
            size="middle"
          >
            生成报表
          </Button>
          
          <Button
            icon={<ReloadOutlined />}
            onClick={handleReset}
            block
            size="small"
          >
            重置参数
          </Button>
        </Space>
        
        {Object.keys(errors).length > 0 && (
          <>
            <Divider />
            <Alert
              message="参数验证失败"
              description={
                <ul style={{ margin: 0, paddingLeft: '20px' }}>
                  {Object.entries(errors).map(([key, error]) => (
                    <li key={key}>{error}</li>
                  ))}
                </ul>
              }
              type="error"
              showIcon
              size="small"
            />
          </>
        )}
        
        <div style={{ 
          marginTop: '16px', 
          padding: '8px', 
          backgroundColor: '#f6ffed',
          border: '1px solid #b7eb8f',
          borderRadius: '4px'
        }}>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            💡 修改参数后点击"生成报表"按钮刷新数据
          </Text>
        </div>
      </Form>
    </Card>
  );
};

export default ReportParameterPanel;