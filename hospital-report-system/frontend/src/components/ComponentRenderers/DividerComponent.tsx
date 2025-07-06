import React, { useMemo } from 'react';
import { Divider } from 'antd';
import { ReportComponent } from '../../services/report';

interface DividerComponentProps {
  component: ReportComponent;
}

const DividerComponent: React.FC<DividerComponentProps> = ({ component }) => {
  const styleConfig = useMemo(() => {
    try {
      return component.styleConfig ? JSON.parse(component.styleConfig) : {};
    } catch {
      return {};
    }
  }, [component.styleConfig]);

  const color = styleConfig.color || '#d9d9d9';
  const lineStyle = styleConfig.lineStyle || 'solid';
  const thickness = styleConfig.thickness || 1;
  const orientation = styleConfig.orientation || 'horizontal';
  const margin = styleConfig.margin || '8px 0';

  const dividerStyle = {
    borderColor: color,
    borderStyle: lineStyle,
    borderWidth: orientation === 'horizontal' ? `${thickness}px 0 0 0` : `0 0 0 ${thickness}px`,
    margin: orientation === 'horizontal' ? margin : '0 8px',
    width: orientation === 'horizontal' ? '100%' : 'auto',
    height: orientation === 'horizontal' ? 'auto' : '100%',
  };

  if (orientation === 'vertical') {
    return (
      <div
        style={{
          width: '100%',
          height: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <div
          style={{
            width: `${thickness}px`,
            height: '100%',
            backgroundColor: color,
            borderRadius: lineStyle === 'solid' ? '0' : '50%',
          }}
        />
      </div>
    );
  }

  return (
    <div
      style={{
        width: '100%',
        height: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <div
        style={{
          width: '100%',
          height: `${thickness}px`,
          backgroundColor: color,
          borderRadius: lineStyle === 'solid' ? '0' : '50%',
        }}
      />
    </div>
  );
};

export default DividerComponent;