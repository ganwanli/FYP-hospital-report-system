import React, { useState } from 'react';
import { useDrag } from 'react-dnd';
import {
  Card,
  Collapse,
  Space,
  Typography,
  Tooltip,
  Input,
  Empty
} from 'antd';
import {
  TableOutlined,
  BarChartOutlined,
  LineChartOutlined,
  PieChartOutlined,
  FontSizeOutlined,
  PictureOutlined,
  MinusOutlined,
  SearchOutlined,
  AppstoreOutlined,
  DashboardOutlined,
  FileTextOutlined
} from '@ant-design/icons';
import { componentTypes, ComponentType } from '../../services/report';

const { Panel } = Collapse;
const { Text } = Typography;
const { Search } = Input;

interface ComponentPanelProps {
  onComponentAdd: (componentType: string, position: { x: number; y: number }) => void;
}

interface DraggableComponentProps {
  componentType: ComponentType;
}

const DraggableComponent: React.FC<DraggableComponentProps> = ({ componentType }) => {
  const [{ isDragging }, drag] = useDrag({
    type: 'component',
    item: { 
      type: 'component',
      componentType: componentType.type,
      name: componentType.name,
      defaultProps: componentType.defaultProps
    },
    collect: (monitor) => ({
      isDragging: monitor.isDragging(),
    }),
  });

  const getIcon = (type: string) => {
    switch (type) {
      case 'table':
        return <TableOutlined />;
      case 'bar-chart':
        return <BarChartOutlined />;
      case 'line-chart':
        return <LineChartOutlined />;
      case 'pie-chart':
        return <PieChartOutlined />;
      case 'text':
        return <FontSizeOutlined />;
      case 'image':
        return <PictureOutlined />;
      case 'divider':
        return <MinusOutlined />;
      default:
        return <AppstoreOutlined />;
    }
  };

  return (
    <div
      ref={drag}
      className={`draggable-component ${isDragging ? 'dragging' : ''}`}
      style={{
        opacity: isDragging ? 0.5 : 1,
        cursor: 'grab',
        padding: '8px',
        margin: '4px 0',
        border: '1px solid #d9d9d9',
        borderRadius: '6px',
        backgroundColor: '#fafafa',
        transition: 'all 0.2s',
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.backgroundColor = '#e6f7ff';
        e.currentTarget.style.borderColor = '#1890ff';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.backgroundColor = '#fafafa';
        e.currentTarget.style.borderColor = '#d9d9d9';
      }}
    >
      <Tooltip title={componentType.description} placement="right">
        <Space>
          {getIcon(componentType.type)}
          <Text strong>{componentType.name}</Text>
        </Space>
      </Tooltip>
    </div>
  );
};

const ComponentPanel: React.FC<ComponentPanelProps> = ({ onComponentAdd }) => {
  const [searchText, setSearchText] = useState<string>('');
  const [activeKey, setActiveKey] = useState<string[]>(['data', 'chart', 'basic']);

  // Filter components based on search text
  const filteredComponents = componentTypes.filter(component =>
    component.name.toLowerCase().includes(searchText.toLowerCase()) ||
    component.description.toLowerCase().includes(searchText.toLowerCase()) ||
    component.type.toLowerCase().includes(searchText.toLowerCase())
  );

  // Group components by category
  const groupedComponents = filteredComponents.reduce((groups, component) => {
    if (!groups[component.category]) {
      groups[component.category] = [];
    }
    groups[component.category].push(component);
    return groups;
  }, {} as Record<string, ComponentType[]>);

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case 'data':
        return <TableOutlined />;
      case 'chart':
        return <DashboardOutlined />;
      case 'basic':
        return <FileTextOutlined />;
      default:
        return <AppstoreOutlined />;
    }
  };

  const getCategoryTitle = (category: string) => {
    switch (category) {
      case 'data':
        return '数据组件';
      case 'chart':
        return '图表组件';
      case 'basic':
        return '基础组件';
      default:
        return '其他组件';
    }
  };

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Card 
        size="small" 
        title={
          <Space>
            <AppstoreOutlined />
            <Text strong>组件库</Text>
          </Space>
        }
        style={{ 
          flex: 1, 
          margin: '8px',
          display: 'flex',
          flexDirection: 'column'
        }}
        bodyStyle={{ 
          flex: 1, 
          padding: '12px',
          display: 'flex',
          flexDirection: 'column'
        }}
      >
        {/* Search */}
        <Search
          placeholder="搜索组件..."
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          style={{ marginBottom: '12px' }}
          allowClear
        />

        {/* Component Categories */}
        <div style={{ flex: 1, overflow: 'auto' }}>
          {Object.keys(groupedComponents).length === 0 ? (
            <Empty
              image={Empty.PRESENTED_IMAGE_SIMPLE}
              description="未找到匹配的组件"
            />
          ) : (
            <Collapse
              activeKey={activeKey}
              onChange={(keys) => setActiveKey(keys as string[])}
              ghost
              size="small"
            >
              {Object.entries(groupedComponents).map(([category, components]) => (
                <Panel
                  header={
                    <Space>
                      {getCategoryIcon(category)}
                      <Text strong>{getCategoryTitle(category)}</Text>
                      <Text type="secondary">({components.length})</Text>
                    </Space>
                  }
                  key={category}
                >
                  <div style={{ marginTop: '8px' }}>
                    {components.map((component) => (
                      <DraggableComponent
                        key={component.type}
                        componentType={component}
                      />
                    ))}
                  </div>
                </Panel>
              ))}
            </Collapse>
          )}
        </div>

        {/* Help Text */}
        <div style={{ 
          marginTop: '12px', 
          padding: '8px', 
          backgroundColor: '#f6ffed',
          border: '1px solid #b7eb8f',
          borderRadius: '4px'
        }}>
          <Text type="secondary" style={{ fontSize: '12px' }}>
            💡 拖拽组件到画布中来创建报表元素
          </Text>
        </div>
      </Card>
    </div>
  );
};

export default ComponentPanel;