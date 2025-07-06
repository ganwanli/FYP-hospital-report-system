import React, { useState, useEffect } from 'react';
import {
  Card,
  Form,
  Input,
  InputNumber,
  Select,
  Switch,
  ColorPicker,
  Tabs,
  Space,
  Typography,
  Divider,
  Button,
  Collapse,
} from 'antd';
import {
  SettingOutlined,
  PaletteOutlined,
  LayoutOutlined,
  FormatPainterOutlined,
} from '@ant-design/icons';
import { ReportComponent } from '../../services/report';

const { Panel } = Collapse;
const { Text } = Typography;
const { Option } = Select;
const { TextArea } = Input;

interface PropertyPanelProps {
  selectedComponent: ReportComponent | null;
  onComponentUpdate: (componentId: number, updates: Partial<ReportComponent>) => void;
}

const PropertyPanel: React.FC<PropertyPanelProps> = ({
  selectedComponent,
  onComponentUpdate,
}) => {
  const [form] = Form.useForm();
  const [activeKey, setActiveKey] = useState<string[]>(['basic', 'style', 'data']);

  useEffect(() => {
    if (selectedComponent) {
      // Parse configuration strings
      const textConfig = selectedComponent.textConfig ? JSON.parse(selectedComponent.textConfig) : {};
      const styleConfig = selectedComponent.styleConfig ? JSON.parse(selectedComponent.styleConfig) : {};
      const chartConfig = selectedComponent.chartConfig ? JSON.parse(selectedComponent.chartConfig) : {};
      const tableConfig = selectedComponent.tableConfig ? JSON.parse(selectedComponent.tableConfig) : {};
      const imageConfig = selectedComponent.imageConfig ? JSON.parse(selectedComponent.imageConfig) : {};

      form.setFieldsValue({
        componentName: selectedComponent.componentName,
        positionX: selectedComponent.positionX,
        positionY: selectedComponent.positionY,
        width: selectedComponent.width,
        height: selectedComponent.height,
        zIndex: selectedComponent.zIndex,
        isVisible: selectedComponent.isVisible,
        isLocked: selectedComponent.isLocked,
        // Style properties
        backgroundColor: styleConfig.backgroundColor || 'transparent',
        color: styleConfig.color || '#000000',
        fontSize: styleConfig.fontSize || 14,
        fontWeight: styleConfig.fontWeight || 'normal',
        textAlign: styleConfig.textAlign || 'left',
        borderRadius: styleConfig.borderRadius || 0,
        border: styleConfig.border || 'none',
        boxShadow: styleConfig.boxShadow || 'none',
        opacity: styleConfig.opacity || 1,
        // Text properties
        textContent: textConfig.content || '',
        textType: textConfig.textType || 'text',
        titleLevel: textConfig.level || 4,
        lineHeight: textConfig.lineHeight || 1.5,
        letterSpacing: textConfig.letterSpacing || 'normal',
        // Chart properties
        chartTitle: chartConfig.title || '',
        showLegend: chartConfig.showLegend !== false,
        seriesName: chartConfig.seriesName || '',
        smooth: chartConfig.smooth !== false,
        radius: chartConfig.radius || 0.8,
        // Table properties
        tableSize: tableConfig.size || 'small',
        tablePagination: tableConfig.pagination !== false,
        tableBordered: tableConfig.bordered !== false,
        tableShowHeader: tableConfig.showHeader !== false,
        pageSize: tableConfig.pageSize || 10,
        // Image properties
        imageSrc: imageConfig.src || '',
        imageAlt: imageConfig.alt || '',
        objectFit: imageConfig.objectFit || 'cover',
      });
    }
  }, [selectedComponent, form]);

  const handleFormChange = (changedFields: any, allFields: any) => {
    if (!selectedComponent) return;

    const values = form.getFieldsValue();
    const updates: Partial<ReportComponent> = {};

    // Basic properties
    if (changedFields.componentName !== undefined) {
      updates.componentName = changedFields.componentName;
    }
    if (changedFields.positionX !== undefined) {
      updates.positionX = changedFields.positionX;
    }
    if (changedFields.positionY !== undefined) {
      updates.positionY = changedFields.positionY;
    }
    if (changedFields.width !== undefined) {
      updates.width = changedFields.width;
    }
    if (changedFields.height !== undefined) {
      updates.height = changedFields.height;
    }
    if (changedFields.zIndex !== undefined) {
      updates.zIndex = changedFields.zIndex;
    }
    if (changedFields.isVisible !== undefined) {
      updates.isVisible = changedFields.isVisible;
    }
    if (changedFields.isLocked !== undefined) {
      updates.isLocked = changedFields.isLocked;
    }

    // Style configuration
    const styleConfig = {
      backgroundColor: values.backgroundColor,
      color: values.color,
      fontSize: values.fontSize,
      fontWeight: values.fontWeight,
      textAlign: values.textAlign,
      borderRadius: values.borderRadius,
      border: values.border,
      boxShadow: values.boxShadow,
      opacity: values.opacity,
      lineHeight: values.lineHeight,
      letterSpacing: values.letterSpacing,
    };
    updates.styleConfig = JSON.stringify(styleConfig);

    // Component-specific configurations
    if (selectedComponent.componentType === 'text') {
      const textConfig = {
        content: values.textContent,
        textType: values.textType,
        level: values.titleLevel,
        lineHeight: values.lineHeight,
        letterSpacing: values.letterSpacing,
      };
      updates.textConfig = JSON.stringify(textConfig);
    }

    if (['bar-chart', 'line-chart', 'pie-chart'].includes(selectedComponent.componentType)) {
      const chartConfig = {
        title: values.chartTitle,
        showLegend: values.showLegend,
        seriesName: values.seriesName,
        smooth: values.smooth,
        radius: values.radius,
      };
      updates.chartConfig = JSON.stringify(chartConfig);
    }

    if (selectedComponent.componentType === 'table') {
      const tableConfig = {
        size: values.tableSize,
        pagination: values.tablePagination,
        bordered: values.tableBordered,
        showHeader: values.tableShowHeader,
        pageSize: values.pageSize,
      };
      updates.tableConfig = JSON.stringify(tableConfig);
    }

    if (selectedComponent.componentType === 'image') {
      const imageConfig = {
        src: values.imageSrc,
        alt: values.imageAlt,
        objectFit: values.objectFit,
      };
      updates.imageConfig = JSON.stringify(imageConfig);
    }

    onComponentUpdate(selectedComponent.componentId, updates);
  };

  const renderBasicProperties = () => (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Form.Item label="组件名称" name="componentName">
        <Input placeholder="请输入组件名称" />
      </Form.Item>
      
      <Form.Item label="可见性" name="isVisible" valuePropName="checked">
        <Switch checkedChildren="显示" unCheckedChildren="隐藏" />
      </Form.Item>
      
      <Form.Item label="锁定" name="isLocked" valuePropName="checked">
        <Switch checkedChildren="锁定" unCheckedChildren="解锁" />
      </Form.Item>
      
      <Divider />
      
      <Text strong>位置和尺寸</Text>
      
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
        <Form.Item label="X坐标" name="positionX">
          <InputNumber min={0} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item label="Y坐标" name="positionY">
          <InputNumber min={0} style={{ width: '100%' }} />
        </Form.Item>
      </div>
      
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
        <Form.Item label="宽度" name="width">
          <InputNumber min={50} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item label="高度" name="height">
          <InputNumber min={50} style={{ width: '100%' }} />
        </Form.Item>
      </div>
      
      <Form.Item label="层级" name="zIndex">
        <InputNumber min={1} style={{ width: '100%' }} />
      </Form.Item>
    </Space>
  );

  const renderStyleProperties = () => (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Form.Item label="背景色" name="backgroundColor">
        <ColorPicker showText />
      </Form.Item>
      
      <Form.Item label="文字颜色" name="color">
        <ColorPicker showText />
      </Form.Item>
      
      <Form.Item label="字体大小" name="fontSize">
        <InputNumber min={8} max={72} addonAfter="px" style={{ width: '100%' }} />
      </Form.Item>
      
      <Form.Item label="字体粗细" name="fontWeight">
        <Select style={{ width: '100%' }}>
          <Option value="normal">正常</Option>
          <Option value="bold">加粗</Option>
          <Option value="lighter">细体</Option>
        </Select>
      </Form.Item>
      
      <Form.Item label="文字对齐" name="textAlign">
        <Select style={{ width: '100%' }}>
          <Option value="left">左对齐</Option>
          <Option value="center">居中</Option>
          <Option value="right">右对齐</Option>
        </Select>
      </Form.Item>
      
      <Form.Item label="圆角" name="borderRadius">
        <InputNumber min={0} addonAfter="px" style={{ width: '100%' }} />
      </Form.Item>
      
      <Form.Item label="透明度" name="opacity">
        <InputNumber min={0} max={1} step={0.1} style={{ width: '100%' }} />
      </Form.Item>
    </Space>
  );

  const renderComponentSpecificProperties = () => {
    if (!selectedComponent) return null;

    switch (selectedComponent.componentType) {
      case 'text':
        return (
          <Space direction="vertical" style={{ width: '100%' }}>
            <Form.Item label="文本内容" name="textContent">
              <TextArea rows={3} placeholder="请输入文本内容" />
            </Form.Item>
            
            <Form.Item label="文本类型" name="textType">
              <Select style={{ width: '100%' }}>
                <Option value="text">普通文本</Option>
                <Option value="title">标题</Option>
                <Option value="paragraph">段落</Option>
              </Select>
            </Form.Item>
            
            <Form.Item label="标题级别" name="titleLevel">
              <Select style={{ width: '100%' }}>
                <Option value={1}>H1</Option>
                <Option value={2}>H2</Option>
                <Option value={3}>H3</Option>
                <Option value={4}>H4</Option>
                <Option value={5}>H5</Option>
              </Select>
            </Form.Item>
            
            <Form.Item label="行高" name="lineHeight">
              <InputNumber min={1} max={3} step={0.1} style={{ width: '100%' }} />
            </Form.Item>
          </Space>
        );

      case 'bar-chart':
      case 'line-chart':
      case 'pie-chart':
        return (
          <Space direction="vertical" style={{ width: '100%' }}>
            <Form.Item label="图表标题" name="chartTitle">
              <Input placeholder="请输入图表标题" />
            </Form.Item>
            
            <Form.Item label="显示图例" name="showLegend" valuePropName="checked">
              <Switch />
            </Form.Item>
            
            <Form.Item label="系列名称" name="seriesName">
              <Input placeholder="请输入系列名称" />
            </Form.Item>
            
            {selectedComponent.componentType === 'line-chart' && (
              <Form.Item label="平滑曲线" name="smooth" valuePropName="checked">
                <Switch />
              </Form.Item>
            )}
            
            {selectedComponent.componentType === 'pie-chart' && (
              <Form.Item label="半径" name="radius">
                <InputNumber min={0.1} max={1} step={0.1} style={{ width: '100%' }} />
              </Form.Item>
            )}
          </Space>
        );

      case 'table':
        return (
          <Space direction="vertical" style={{ width: '100%' }}>
            <Form.Item label="表格大小" name="tableSize">
              <Select style={{ width: '100%' }}>
                <Option value="small">小</Option>
                <Option value="middle">中</Option>
                <Option value="large">大</Option>
              </Select>
            </Form.Item>
            
            <Form.Item label="显示分页" name="tablePagination" valuePropName="checked">
              <Switch />
            </Form.Item>
            
            <Form.Item label="显示边框" name="tableBordered" valuePropName="checked">
              <Switch />
            </Form.Item>
            
            <Form.Item label="显示表头" name="tableShowHeader" valuePropName="checked">
              <Switch />
            </Form.Item>
            
            <Form.Item label="每页条数" name="pageSize">
              <InputNumber min={1} max={100} style={{ width: '100%' }} />
            </Form.Item>
          </Space>
        );

      case 'image':
        return (
          <Space direction="vertical" style={{ width: '100%' }}>
            <Form.Item label="图片地址" name="imageSrc">
              <Input placeholder="请输入图片URL" />
            </Form.Item>
            
            <Form.Item label="替代文本" name="imageAlt">
              <Input placeholder="请输入替代文本" />
            </Form.Item>
            
            <Form.Item label="填充方式" name="objectFit">
              <Select style={{ width: '100%' }}>
                <Option value="cover">覆盖</Option>
                <Option value="contain">包含</Option>
                <Option value="fill">填充</Option>
                <Option value="none">无</Option>
                <Option value="scale-down">缩小</Option>
              </Select>
            </Form.Item>
          </Space>
        );

      default:
        return null;
    }
  };

  if (!selectedComponent) {
    return (
      <Card
        size="small"
        title={
          <Space>
            <SettingOutlined />
            <Text strong>属性面板</Text>
          </Space>
        }
        style={{ margin: '8px', height: 'calc(100% - 16px)' }}
      >
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            height: '200px',
            color: '#999',
            textAlign: 'center',
          }}
        >
          <div>
            <SettingOutlined style={{ fontSize: '48px', marginBottom: '16px' }} />
            <div>请选择一个组件</div>
            <div style={{ fontSize: '12px', marginTop: '8px' }}>
              选择组件后可在此处配置其属性
            </div>
          </div>
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
          <Text strong>属性面板</Text>
        </Space>
      }
      style={{ margin: '8px', height: 'calc(100% - 16px)' }}
      bodyStyle={{ padding: '12px', height: 'calc(100% - 57px)', overflow: 'auto' }}
    >
      <Form
        form={form}
        layout="vertical"
        size="small"
        onValuesChange={handleFormChange}
      >
        <Collapse
          activeKey={activeKey}
          onChange={setActiveKey}
          ghost
          size="small"
        >
          <Panel
            header={
              <Space>
                <LayoutOutlined />
                <Text strong>基础属性</Text>
              </Space>
            }
            key="basic"
          >
            {renderBasicProperties()}
          </Panel>
          
          <Panel
            header={
              <Space>
                <PaletteOutlined />
                <Text strong>样式属性</Text>
              </Space>
            }
            key="style"
          >
            {renderStyleProperties()}
          </Panel>
          
          {renderComponentSpecificProperties() && (
            <Panel
              header={
                <Space>
                  <FormatPainterOutlined />
                  <Text strong>组件属性</Text>
                </Space>
              }
              key="component"
            >
              {renderComponentSpecificProperties()}
            </Panel>
          )}
        </Collapse>
      </Form>
    </Card>
  );
};

export default PropertyPanel;