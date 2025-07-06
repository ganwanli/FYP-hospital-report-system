import React, { useRef, useEffect } from 'react';
import { Card, Empty, Alert, Spin } from 'antd';
import * as echarts from 'echarts';
import type { ReportGenerationResult, ReportComponentData } from '../../services/reportView';

interface ReportRendererProps {
  reportData: ReportGenerationResult;
  viewMode: 'desktop' | 'mobile';
  isFullscreen?: boolean;
}

const ReportRenderer: React.FC<ReportRendererProps> = ({
  reportData,
  viewMode,
  isFullscreen = false,
}) => {
  const canvasRef = useRef<HTMLDivElement>(null);

  const renderComponent = (component: ReportComponentData, index: number) => {
    const { componentType, position, data, dataError } = component;

    if (dataError) {
      return (
        <div
          key={component.componentId}
          style={getComponentStyle(component, viewMode)}
          className="report-component-error"
        >
          <Alert
            message="组件错误"
            description={dataError}
            type="error"
            size="small"
            showIcon
          />
        </div>
      );
    }

    const content = (() => {
      switch (componentType) {
        case 'table':
          return <TableRenderer component={component} viewMode={viewMode} />;
        case 'bar-chart':
        case 'line-chart':
        case 'pie-chart':
          return <ChartRenderer component={component} viewMode={viewMode} />;
        case 'text':
          return <TextRenderer component={component} viewMode={viewMode} />;
        case 'image':
          return <ImageRenderer component={component} viewMode={viewMode} />;
        case 'divider':
          return <DividerRenderer component={component} viewMode={viewMode} />;
        default:
          return (
            <div style={{ 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center',
              background: '#f5f5f5',
              color: '#999',
              fontSize: '12px'
            }}>
              未知组件类型: {componentType}
            </div>
          );
      }
    })();

    if (viewMode === 'mobile') {
      return (
        <Card
          key={component.componentId}
          size="small"
          title={component.componentName}
          style={{ marginBottom: '12px' }}
          bodyStyle={{ padding: '12px' }}
          className="mobile-report-component"
        >
          {content}
        </Card>
      );
    }

    return (
      <div
        key={component.componentId}
        style={getComponentStyle(component, viewMode)}
        className="desktop-report-component"
      >
        {content}
      </div>
    );
  };

  const getComponentStyle = (component: ReportComponentData, mode: string) => {
    if (mode === 'mobile') {
      return {};
    }

    const { position, zIndex } = component;
    return {
      position: 'absolute' as const,
      left: `${position.x}px`,
      top: `${position.y}px`,
      width: `${position.width}px`,
      height: `${position.height}px`,
      zIndex: zIndex || 1,
      backgroundColor: 'white',
      borderRadius: '4px',
      overflow: 'hidden',
      boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
    };
  };

  if (!reportData) {
    return (
      <Card style={{ margin: '16px' }}>
        <Empty description="暂无报表数据" />
      </Card>
    );
  }

  if (viewMode === 'mobile') {
    return (
      <div className="mobile-report-container" style={{ padding: '16px' }}>
        {/* 报表标题 */}
        <Card 
          size="small" 
          style={{ marginBottom: '16px' }}
          bodyStyle={{ textAlign: 'center' }}
        >
          <h2 style={{ margin: 0, color: '#333' }}>{reportData.reportName}</h2>
          {reportData.reportDescription && (
            <p style={{ margin: '8px 0 0 0', color: '#666', fontSize: '14px' }}>
              {reportData.reportDescription}
            </p>
          )}
          <p style={{ margin: '8px 0 0 0', color: '#999', fontSize: '12px' }}>
            生成时间: {new Date(reportData.generatedAt).toLocaleString()}
          </p>
        </Card>

        {/* 移动端组件列表 */}
        <div className="mobile-components">
          {reportData.components
            .filter(component => component.isVisible !== false)
            .sort((a, b) => (a.zIndex || 0) - (b.zIndex || 0))
            .map((component, index) => renderComponent(component, index))}
        </div>
      </div>
    );
  }

  return (
    <div 
      className="desktop-report-container" 
      style={{ 
        padding: '20px',
        background: '#f5f5f5',
        minHeight: '100%',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'flex-start',
      }}
    >
      <div
        ref={canvasRef}
        className="report-canvas"
        style={{
          width: `${reportData.canvasWidth}px`,
          height: `${reportData.canvasHeight}px`,
          backgroundColor: 'white',
          position: 'relative',
          boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
          borderRadius: '8px',
          border: '1px solid #e8e8e8',
        }}
      >
        {reportData.components
          .filter(component => component.isVisible !== false)
          .sort((a, b) => (a.zIndex || 0) - (b.zIndex || 0))
          .map((component, index) => renderComponent(component, index))}
      </div>
    </div>
  );
};

// 表格渲染器
const TableRenderer: React.FC<{ component: ReportComponentData; viewMode: string }> = ({ 
  component, 
  viewMode 
}) => {
  const { data, tableConfig } = component;
  
  if (!data || !data.records) {
    return <Empty description="暂无数据" size="small" />;
  }

  const columns = data.columns || [];
  const records = data.records || [];

  if (viewMode === 'mobile') {
    return (
      <div className="mobile-table">
        {records.slice(0, 10).map((record: any, index: number) => (
          <div key={index} className="mobile-table-row" style={{
            background: '#f9f9f9',
            padding: '8px',
            marginBottom: '8px',
            borderRadius: '4px',
            border: '1px solid #e8e8e8'
          }}>
            {columns.map((col: any) => (
              <div key={col.dataIndex} style={{
                display: 'flex',
                justifyContent: 'space-between',
                padding: '2px 0',
                fontSize: '12px'
              }}>
                <span style={{ fontWeight: 'bold', color: '#666' }}>{col.title}:</span>
                <span>{record[col.dataIndex]}</span>
              </div>
            ))}
          </div>
        ))}
      </div>
    );
  }

  return (
    <div style={{ width: '100%', height: '100%', overflow: 'auto', fontSize: '12px' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            {columns.map((col: any) => (
              <th key={col.dataIndex} style={{
                border: '1px solid #e8e8e8',
                padding: '6px',
                background: '#fafafa',
                fontSize: '11px',
                fontWeight: 'bold'
              }}>
                {col.title}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {records.slice(0, 20).map((record: any, index: number) => (
            <tr key={index}>
              {columns.map((col: any) => (
                <td key={col.dataIndex} style={{
                  border: '1px solid #e8e8e8',
                  padding: '4px 6px',
                  fontSize: '11px'
                }}>
                  {record[col.dataIndex]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

// 图表渲染器
const ChartRenderer: React.FC<{ component: ReportComponentData; viewMode: string }> = ({ 
  component, 
  viewMode 
}) => {
  const chartRef = useRef<HTMLDivElement>(null);
  const chartInstance = useRef<echarts.ECharts | null>(null);

  useEffect(() => {
    if (chartRef.current && component.data) {
      chartInstance.current = echarts.init(chartRef.current);
      
      const option = generateChartOption(component);
      chartInstance.current.setOption(option);
    }

    return () => {
      if (chartInstance.current) {
        chartInstance.current.dispose();
        chartInstance.current = null;
      }
    };
  }, [component]);

  useEffect(() => {
    if (chartInstance.current) {
      chartInstance.current.resize();
    }
  }, [viewMode]);

  const generateChartOption = (component: ReportComponentData) => {
    const { componentType, chartConfig, data } = component;
    
    // 简化的图表配置生成
    const baseOption = {
      title: {
        text: chartConfig?.title || component.componentName,
        textStyle: { fontSize: viewMode === 'mobile' ? 14 : 12 }
      },
      tooltip: { trigger: 'axis' },
      legend: { 
        show: chartConfig?.showLegend !== false,
        textStyle: { fontSize: viewMode === 'mobile' ? 12 : 10 }
      },
      grid: {
        left: '10%',
        right: '10%',
        bottom: '15%',
        top: '20%',
        containLabel: true
      }
    };

    const records = data?.records || [];
    
    // 模拟数据处理
    switch (componentType) {
      case 'bar-chart':
        return {
          ...baseOption,
          xAxis: { type: 'category', data: ['A', 'B', 'C', 'D', 'E'] },
          yAxis: { type: 'value' },
          series: [{
            name: '数据',
            type: 'bar',
            data: [120, 200, 150, 80, 70]
          }]
        };
      case 'line-chart':
        return {
          ...baseOption,
          xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月'] },
          yAxis: { type: 'value' },
          series: [{
            name: '趋势',
            type: 'line',
            data: [820, 932, 901, 934, 1290],
            smooth: chartConfig?.smooth !== false
          }]
        };
      case 'pie-chart':
        return {
          ...baseOption,
          series: [{
            name: '分布',
            type: 'pie',
            radius: '60%',
            data: [
              { value: 335, name: '类型A' },
              { value: 310, name: '类型B' },
              { value: 234, name: '类型C' },
              { value: 135, name: '类型D' }
            ]
          }]
        };
      default:
        return baseOption;
    }
  };

  return (
    <div 
      ref={chartRef} 
      style={{ 
        width: '100%', 
        height: viewMode === 'mobile' ? '300px' : '100%',
        minHeight: viewMode === 'mobile' ? '300px' : undefined
      }} 
    />
  );
};

// 文本渲染器
const TextRenderer: React.FC<{ component: ReportComponentData; viewMode: string }> = ({ 
  component, 
  viewMode 
}) => {
  const { textConfig, styleConfig } = component;
  const content = textConfig?.content || component.componentName;
  
  const style = {
    width: '100%',
    height: '100%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-start',
    padding: '8px',
    fontSize: viewMode === 'mobile' ? '14px' : (styleConfig?.fontSize || '12px'),
    color: styleConfig?.color || '#333',
    textAlign: (styleConfig?.textAlign || 'left') as any,
    fontWeight: styleConfig?.fontWeight || 'normal',
    lineHeight: styleConfig?.lineHeight || 1.5,
    overflow: 'hidden',
    wordBreak: 'break-word' as any,
  };

  return <div style={style}>{content}</div>;
};

// 图片渲染器
const ImageRenderer: React.FC<{ component: ReportComponentData; viewMode: string }> = ({ 
  component, 
  viewMode 
}) => {
  const { imageConfig } = component;
  const src = imageConfig?.src;
  const alt = imageConfig?.alt || '图片';

  if (!src) {
    return (
      <div style={{
        width: '100%',
        height: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: '#f5f5f5',
        color: '#999',
        fontSize: '12px'
      }}>
        暂无图片
      </div>
    );
  }

  return (
    <img 
      src={src} 
      alt={alt}
      style={{
        width: '100%',
        height: '100%',
        objectFit: imageConfig?.objectFit || 'cover'
      }}
      onError={(e) => {
        (e.target as HTMLImageElement).style.display = 'none';
        (e.target as HTMLImageElement).parentElement!.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;width:100%;height:100%;background:#f5f5f5;color:#999;font-size:12px;">图片加载失败</div>';
      }}
    />
  );
};

// 分割线渲染器
const DividerRenderer: React.FC<{ component: ReportComponentData; viewMode: string }> = ({ 
  component, 
  viewMode 
}) => {
  const { styleConfig } = component;
  
  return (
    <div style={{
      width: '100%',
      height: '100%',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }}>
      <div style={{
        width: '100%',
        height: '1px',
        backgroundColor: styleConfig?.color || '#d9d9d9'
      }} />
    </div>
  );
};

export default ReportRenderer;