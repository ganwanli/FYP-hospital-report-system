import React, { useState, useEffect } from 'react';
import { Modal, Spin, message, Button, Space, Tooltip } from 'antd';
import {
  FullscreenOutlined,
  FullscreenExitOutlined,
  ReloadOutlined,
  DownloadOutlined,
} from '@ant-design/icons';
import { reportApi } from '../../services/report';

interface PreviewModalProps {
  visible: boolean;
  reportId?: number;
  onCancel: () => void;
}

const PreviewModal: React.FC<PreviewModalProps> = ({
  visible,
  reportId,
  onCancel,
}) => {
  const [loading, setLoading] = useState(false);
  const [previewData, setPreviewData] = useState<any>(null);
  const [isFullscreen, setIsFullscreen] = useState(false);

  useEffect(() => {
    if (visible && reportId) {
      loadPreview();
    }
  }, [visible, reportId]);

  const loadPreview = async () => {
    if (!reportId) return;
    
    setLoading(true);
    try {
      const response = await reportApi.previewReport(reportId);
      setPreviewData(response.data);
    } catch (error) {
      message.error('预览加载失败');
      console.error('Preview error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    loadPreview();
  };

  const handleFullscreen = () => {
    setIsFullscreen(!isFullscreen);
  };

  const handleExport = async () => {
    if (!reportId) return;
    
    try {
      const response = await reportApi.renderReport(reportId);
      const dataStr = JSON.stringify(response.data, null, 2);
      const dataBlob = new Blob([dataStr], { type: 'application/json' });
      const url = URL.createObjectURL(dataBlob);
      
      const link = document.createElement('a');
      link.href = url;
      link.download = `report_${reportId}_preview.json`;
      link.click();
      
      URL.revokeObjectURL(url);
      message.success('预览数据导出成功');
    } catch (error) {
      message.error('导出失败');
    }
  };

  const renderPreviewContent = () => {
    if (!previewData) return null;

    const { canvasWidth, canvasHeight, components } = previewData;

    return (
      <div
        style={{
          width: '100%',
          height: isFullscreen ? '100vh' : '600px',
          overflow: 'auto',
          backgroundColor: '#f5f5f5',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'flex-start',
          padding: '20px',
        }}
      >
        <div
          style={{
            width: `${canvasWidth}px`,
            height: `${canvasHeight}px`,
            backgroundColor: '#ffffff',
            position: 'relative',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            borderRadius: '4px',
          }}
        >
          {components?.map((component: any) => (
            <div
              key={component.componentId}
              style={{
                position: 'absolute',
                left: `${component.position.x}px`,
                top: `${component.position.y}px`,
                width: `${component.position.width}px`,
                height: `${component.position.height}px`,
                zIndex: component.zIndex,
                visibility: component.isVisible === false ? 'hidden' : 'visible',
              }}
            >
              {renderPreviewComponent(component)}
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderPreviewComponent = (component: any) => {
    switch (component.componentType) {
      case 'table':
        return renderTablePreview(component);
      case 'bar-chart':
      case 'line-chart':
      case 'pie-chart':
        return renderChartPreview(component);
      case 'text':
        return renderTextPreview(component);
      case 'image':
        return renderImagePreview(component);
      case 'divider':
        return renderDividerPreview(component);
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
              color: '#999',
            }}
          >
            预览不支持的组件类型
          </div>
        );
    }
  };

  const renderTablePreview = (component: any) => {
    const data = component.data?.records || [];
    const columns = component.data?.columns || [];
    
    return (
      <div style={{ width: '100%', height: '100%', overflow: 'auto' }}>
        <table
          style={{
            width: '100%',
            fontSize: '12px',
            borderCollapse: 'collapse',
          }}
        >
          <thead>
            <tr>
              {columns.map((col: any, index: number) => (
                <th
                  key={index}
                  style={{
                    border: '1px solid #d9d9d9',
                    padding: '4px 8px',
                    backgroundColor: '#fafafa',
                  }}
                >
                  {col.title}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data.slice(0, 10).map((row: any, rowIndex: number) => (
              <tr key={rowIndex}>
                {columns.map((col: any, colIndex: number) => (
                  <td
                    key={colIndex}
                    style={{
                      border: '1px solid #d9d9d9',
                      padding: '4px 8px',
                    }}
                  >
                    {row[col.dataIndex]}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  const renderChartPreview = (component: any) => {
    return (
      <div
        style={{
          width: '100%',
          height: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          backgroundColor: '#f9f9f9',
          border: '1px solid #d9d9d9',
          color: '#666',
        }}
      >
        📊 {component.componentName}
        <br />
        <small>图表预览</small>
      </div>
    );
  };

  const renderTextPreview = (component: any) => {
    // 简化的文本渲染
    return (
      <div
        style={{
          width: '100%',
          height: '100%',
          padding: '8px',
          fontSize: '14px',
          overflow: 'hidden',
        }}
      >
        {component.componentName}
      </div>
    );
  };

  const renderImagePreview = (component: any) => {
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
          color: '#999',
        }}
      >
        🖼️ 图片组件
      </div>
    );
  };

  const renderDividerPreview = (component: any) => {
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
            height: '1px',
            backgroundColor: '#d9d9d9',
          }}
        />
      </div>
    );
  };

  return (
    <Modal
      title="报表预览"
      open={visible}
      onCancel={onCancel}
      width={isFullscreen ? '100vw' : '90vw'}
      style={isFullscreen ? { top: 0, maxWidth: 'none' } : { top: 20 }}
      bodyStyle={{
        padding: 0,
        height: isFullscreen ? 'calc(100vh - 55px)' : '600px',
      }}
      footer={[
        <Space key="actions">
          <Tooltip title="刷新">
            <Button
              icon={<ReloadOutlined />}
              onClick={handleRefresh}
              loading={loading}
            />
          </Tooltip>
          <Tooltip title="导出数据">
            <Button
              icon={<DownloadOutlined />}
              onClick={handleExport}
            />
          </Tooltip>
          <Tooltip title={isFullscreen ? '退出全屏' : '全屏'}>
            <Button
              icon={isFullscreen ? <FullscreenExitOutlined /> : <FullscreenOutlined />}
              onClick={handleFullscreen}
            />
          </Tooltip>
          <Button onClick={onCancel}>
            关闭
          </Button>
        </Space>,
      ]}
    >
      <Spin spinning={loading} tip="加载预览数据...">
        {renderPreviewContent()}
      </Spin>
    </Modal>
  );
};

export default PreviewModal;