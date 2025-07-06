import React from 'react';
import { ReportComponent } from '../../services/report';
import TableComponent from '../ComponentRenderers/TableComponent';
import ChartComponent from '../ComponentRenderers/ChartComponent';
import TextComponent from '../ComponentRenderers/TextComponent';
import ImageComponent from '../ComponentRenderers/ImageComponent';
import DividerComponent from '../ComponentRenderers/DividerComponent';

interface ComponentRendererProps {
  component: ReportComponent;
}

const ComponentRenderer: React.FC<ComponentRendererProps> = ({ component }) => {
  const renderComponent = () => {
    switch (component.componentType) {
      case 'table':
        return <TableComponent component={component} />;
      case 'bar-chart':
      case 'line-chart':
      case 'pie-chart':
        return <ChartComponent component={component} />;
      case 'text':
        return <TextComponent component={component} />;
      case 'image':
        return <ImageComponent component={component} />;
      case 'divider':
        return <DividerComponent component={component} />;
      default:
        return (
          <div
            style={{
              width: '100%',
              height: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              backgroundColor: '#f5f5f5',
              border: '1px dashed #d9d9d9',
              borderRadius: '4px',
              color: '#999',
              fontSize: '14px',
            }}
          >
            未知组件类型: {component.componentType}
          </div>
        );
    }
  };

  return (
    <div style={{ width: '100%', height: '100%', overflow: 'hidden' }}>
      {renderComponent()}
    </div>
  );
};

export default ComponentRenderer;