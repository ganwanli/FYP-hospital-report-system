import React, { useState, useEffect } from 'react';
import {
  Form,
  Input,
  InputNumber,
  Switch,
  Select,
  DatePicker,
  TimePicker,
  Card,
  Space,
  Button,
  Tooltip,
  Typography,
  Row,
  Col,
  Alert,
  Divider,
  Tag
} from 'antd';
import {
  InfoCircleOutlined,
  EyeInvisibleOutlined,
  EyeOutlined,
  ClearOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import type { SqlTemplateParameter } from '../../services/sql';
import dayjs from 'dayjs';

const { TextArea } = Input;
const { Option } = Select;
const { Text } = Typography;

interface ParameterPanelProps {
  parameters: SqlTemplateParameter[];
  values: Record<string, any>;
  onChange: (values: Record<string, any>) => void;
  disabled?: boolean;
}

const ParameterPanel: React.FC<ParameterPanelProps> = ({
  parameters,
  values,
  onChange,
  disabled = false
}) => {
  const [form] = Form.useForm();
  const [maskedFields, setMaskedFields] = useState<Set<string>>(new Set());
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    // Set form values when parameters or values change
    const formValues: Record<string, any> = {};
    parameters.forEach(param => {
      const value = values[param.parameterName];
      if (value !== undefined) {
        formValues[param.parameterName] = formatValueForForm(value, param);
      } else if (param.defaultValue) {
        formValues[param.parameterName] = formatValueForForm(param.defaultValue, param);
      }
    });
    form.setFieldsValue(formValues);
  }, [parameters, values]);

  useEffect(() => {
    // Auto-mask sensitive parameters
    const sensitiveMask = new Set<string>();
    parameters.forEach(param => {
      if (param.isSensitive) {
        sensitiveMask.add(param.parameterName);
      }
    });
    setMaskedFields(sensitiveMask);
  }, [parameters]);

  const formatValueForForm = (value: any, param: SqlTemplateParameter) => {
    if (value === null || value === undefined) return undefined;
    
    switch (param.parameterType?.toLowerCase()) {
      case 'date':
        return typeof value === 'string' ? dayjs(value) : value;
      case 'datetime':
      case 'timestamp':
        return typeof value === 'string' ? dayjs(value) : value;
      case 'time':
        return typeof value === 'string' ? dayjs(value, 'HH:mm:ss') : value;
      case 'boolean':
      case 'bool':
        return Boolean(value);
      case 'list':
      case 'array':
        return Array.isArray(value) ? value : [];
      default:
        return value;
    }
  };

  const formatValueForOutput = (value: any, param: SqlTemplateParameter) => {
    if (value === null || value === undefined) return undefined;
    
    switch (param.parameterType?.toLowerCase()) {
      case 'date':
        return dayjs.isDayjs(value) ? value.format('YYYY-MM-DD') : value;
      case 'datetime':
      case 'timestamp':
        return dayjs.isDayjs(value) ? value.format('YYYY-MM-DD HH:mm:ss') : value;
      case 'time':
        return dayjs.isDayjs(value) ? value.format('HH:mm:ss') : value;
      case 'integer':
      case 'int':
        return parseInt(value, 10);
      case 'long':
      case 'bigint':
        return parseInt(value, 10);
      case 'decimal':
      case 'numeric':
      case 'double':
        return parseFloat(value);
      case 'boolean':
      case 'bool':
        return Boolean(value);
      case 'list':
      case 'array':
        return Array.isArray(value) ? value : [value];
      default:
        return value;
    }
  };

  const validateParameter = (param: SqlTemplateParameter, value: any): string | null => {
    if (param.isRequired && (value === undefined || value === null || value === '')) {
      return `${param.parameterName} is required`;
    }

    if (value === undefined || value === null || value === '') {
      return null; // Skip validation for empty optional fields
    }

    const stringValue = String(value);

    // Length validation
    if (param.minLength && stringValue.length < param.minLength) {
      return `Minimum length is ${param.minLength}`;
    }
    if (param.maxLength && stringValue.length > param.maxLength) {
      return `Maximum length is ${param.maxLength}`;
    }

    // Value range validation
    if (param.minValue && parseFloat(stringValue) < parseFloat(param.minValue)) {
      return `Minimum value is ${param.minValue}`;
    }
    if (param.maxValue && parseFloat(stringValue) > parseFloat(param.maxValue)) {
      return `Maximum value is ${param.maxValue}`;
    }

    // Allowed values validation
    if (param.allowedValues) {
      const allowedList = param.allowedValues.split(',').map(v => v.trim());
      if (!allowedList.includes(stringValue)) {
        return `Value must be one of: ${param.allowedValues}`;
      }
    }

    // Pattern validation
    if (param.validationRule) {
      try {
        const regex = new RegExp(param.validationRule);
        if (!regex.test(stringValue)) {
          return param.validationMessage || 'Invalid format';
        }
      } catch (error) {
        console.warn('Invalid validation rule:', param.validationRule);
      }
    }

    return null;
  };

  const handleFieldChange = (parameterName: string, value: any) => {
    const param = parameters.find(p => p.parameterName === parameterName);
    if (!param) return;

    const formattedValue = formatValueForOutput(value, param);
    const error = validateParameter(param, formattedValue);
    
    setValidationErrors(prev => ({
      ...prev,
      [parameterName]: error || ''
    }));

    const newValues = {
      ...values,
      [parameterName]: formattedValue
    };
    onChange(newValues);
  };

  const toggleFieldMask = (parameterName: string) => {
    setMaskedFields(prev => {
      const newSet = new Set(prev);
      if (newSet.has(parameterName)) {
        newSet.delete(parameterName);
      } else {
        newSet.add(parameterName);
      }
      return newSet;
    });
  };

  const clearAllValues = () => {
    const clearedValues: Record<string, any> = {};
    parameters.forEach(param => {
      if (param.defaultValue) {
        clearedValues[param.parameterName] = formatValueForOutput(param.defaultValue, param);
      }
    });
    onChange(clearedValues);
    form.resetFields();
    setValidationErrors({});
  };

  const resetToDefaults = () => {
    const defaultValues: Record<string, any> = {};
    const formValues: Record<string, any> = {};
    
    parameters.forEach(param => {
      if (param.defaultValue) {
        const value = formatValueForOutput(param.defaultValue, param);
        defaultValues[param.parameterName] = value;
        formValues[param.parameterName] = formatValueForForm(param.defaultValue, param);
      }
    });
    
    onChange(defaultValues);
    form.setFieldsValue(formValues);
    setValidationErrors({});
  };

  const renderParameterInput = (param: SqlTemplateParameter) => {
    const isRequired = param.isRequired;
    const isMasked = maskedFields.has(param.parameterName);
    const error = validationErrors[param.parameterName];
    const value = values[param.parameterName];

    const commonProps = {
      disabled,
      placeholder: param.parameterDescription || `Enter ${param.parameterName}`,
      status: error ? 'error' as const : undefined
    };

    let inputComponent;

    switch (param.inputType?.toLowerCase()) {
      case 'select':
      case 'dropdown':
        const options = param.allowedValues ? param.allowedValues.split(',').map(v => v.trim()) : [];
        inputComponent = (
          <Select {...commonProps} allowClear>
            {options.map(option => (
              <Option key={option} value={option}>
                {option}
              </Option>
            ))}
          </Select>
        );
        break;

      case 'textarea':
        inputComponent = (
          <TextArea
            {...commonProps}
            rows={3}
            maxLength={param.maxLength}
            showCount={!!param.maxLength}
          />
        );
        break;

      case 'number':
        inputComponent = (
          <InputNumber
            {...commonProps}
            style={{ width: '100%' }}
            min={param.minValue ? parseFloat(param.minValue) : undefined}
            max={param.maxValue ? parseFloat(param.maxValue) : undefined}
            precision={param.parameterType?.toLowerCase().includes('decimal') ? 2 : 0}
          />
        );
        break;

      case 'switch':
        inputComponent = (
          <Switch
            disabled={disabled}
            checked={Boolean(value)}
          />
        );
        break;

      case 'date':
        inputComponent = (
          <DatePicker
            {...commonProps}
            style={{ width: '100%' }}
            format="YYYY-MM-DD"
          />
        );
        break;

      case 'datetime':
        inputComponent = (
          <DatePicker
            {...commonProps}
            style={{ width: '100%' }}
            showTime
            format="YYYY-MM-DD HH:mm:ss"
          />
        );
        break;

      case 'time':
        inputComponent = (
          <TimePicker
            {...commonProps}
            style={{ width: '100%' }}
            format="HH:mm:ss"
          />
        );
        break;

      default:
        inputComponent = (
          <Input
            {...commonProps}
            type={isMasked ? 'password' : 'text'}
            maxLength={param.maxLength}
            suffix={param.isSensitive ? (
              <Button
                type="text"
                size="small"
                icon={isMasked ? <EyeOutlined /> : <EyeInvisibleOutlined />}
                onClick={() => toggleFieldMask(param.parameterName)}
              />
            ) : undefined}
          />
        );
    }

    return (
      <Form.Item
        key={param.parameterName}
        name={param.parameterName}
        label={
          <Space>
            <Text strong={isRequired}>
              {param.parameterName}
              {isRequired && <span style={{ color: 'red' }}>*</span>}
            </Text>
            {param.parameterDescription && (
              <Tooltip title={param.parameterDescription}>
                <InfoCircleOutlined style={{ color: '#1890ff' }} />
              </Tooltip>
            )}
            {param.isSensitive && (
              <Tag color="orange" size="small">Sensitive</Tag>
            )}
          </Space>
        }
        validateStatus={error ? 'error' : ''}
        help={error || (param.validationMessage && `Format: ${param.validationMessage}`)}
        rules={[
          {
            required: isRequired,
            message: `${param.parameterName} is required`
          }
        ]}
      >
        {inputComponent}
      </Form.Item>
    );
  };

  const hasErrors = Object.values(validationErrors).some(error => error);
  const requiredParams = parameters.filter(p => p.isRequired);
  const optionalParams = parameters.filter(p => !p.isRequired);

  if (parameters.length === 0) {
    return (
      <Alert
        message="No Parameters"
        description="This SQL template does not require any parameters."
        type="info"
        showIcon
      />
    );
  }

  return (
    <div>
      <Row justify="space-between" align="middle" style={{ marginBottom: '16px' }}>
        <Col>
          <Text strong>
            Parameters ({parameters.length})
          </Text>
        </Col>
        <Col>
          <Space>
            <Button
              size="small"
              icon={<ReloadOutlined />}
              onClick={resetToDefaults}
              disabled={disabled}
            >
              Reset
            </Button>
            <Button
              size="small"
              icon={<ClearOutlined />}
              onClick={clearAllValues}
              disabled={disabled}
            >
              Clear
            </Button>
          </Space>
        </Col>
      </Row>

      {hasErrors && (
        <Alert
          message="Parameter Validation Errors"
          description="Please fix the errors below before executing the query."
          type="error"
          showIcon
          style={{ marginBottom: '16px' }}
        />
      )}

      <Form
        form={form}
        layout="vertical"
        onValuesChange={(changedValues) => {
          Object.entries(changedValues).forEach(([key, value]) => {
            handleFieldChange(key, value);
          });
        }}
      >
        {requiredParams.length > 0 && (
          <Card size="small" title="Required Parameters" style={{ marginBottom: '16px' }}>
            {requiredParams
              .sort((a, b) => (a.parameterOrder || 0) - (b.parameterOrder || 0))
              .map(renderParameterInput)}
          </Card>
        )}

        {optionalParams.length > 0 && (
          <Card size="small" title="Optional Parameters">
            {optionalParams
              .sort((a, b) => (a.parameterOrder || 0) - (b.parameterOrder || 0))
              .map(renderParameterInput)}
          </Card>
        )}
      </Form>

      <Divider />

      <Row>
        <Col span={24}>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            <InfoCircleOutlined style={{ marginRight: '4px' }} />
            Parameters are automatically detected from your SQL template using ${'{parameterName}'} syntax.
          </Text>
        </Col>
      </Row>
    </div>
  );
};

export default ParameterPanel;