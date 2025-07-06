import React, { useRef, useState, useCallback } from 'react';
import { useDrop } from 'react-dnd';
import {
  Card,
  Button,
  Space,
  Typography,
  Tooltip,
  message
} from 'antd';
import {
  LockOutlined,
  UnlockOutlined,
  EyeOutlined,
  EyeInvisibleOutlined,
  CopyOutlined,
  DeleteOutlined,
  BringForwardOutlined,
  SendBackwardOutlined
} from '@ant-design/icons';
import ResizableComponent from './ResizableComponent';
import ComponentRenderer from './ComponentRenderer';
import { ReportConfig, ReportComponent, componentTypes } from '../../services/report';

const { Text } = Typography;

interface DesignCanvasProps {
  report: ReportConfig | null;
  components: ReportComponent[];
  selectedComponent: ReportComponent | null;
  zoom: number;
  onComponentUpdate: (componentId: number, updates: Partial<ReportComponent>) => void;
  onComponentDelete: (componentId: number) => void;
  onComponentSelect: (component: ReportComponent | null) => void;
  onComponentAdd: (componentType: string, position: { x: number; y: number }) => void;
}

const DesignCanvas: React.FC<DesignCanvasProps> = ({
  report,
  components,
  selectedComponent,
  zoom,
  onComponentUpdate,
  onComponentDelete,
  onComponentSelect,
  onComponentAdd,
}) => {
  const canvasRef = useRef<HTMLDivElement>(null);
  const [dragOffset, setDragOffset] = useState<{ x: number; y: number }>({ x: 0, y: 0 });
  const [isSelecting, setIsSelecting] = useState<boolean>(false);
  const [selectionRect, setSelectionRect] = useState<{
    startX: number;
    startY: number;
    endX: number;
    endY: number;
  } | null>(null);

  const [{ isOver, canDrop }, drop] = useDrop({
    accept: 'component',
    drop: (item: any, monitor) => {
      const canvasRect = canvasRef.current?.getBoundingClientRect();
      if (!canvasRect) return;

      const clientOffset = monitor.getClientOffset();
      if (!clientOffset) return;

      const x = Math.round((clientOffset.x - canvasRect.left - dragOffset.x) / (zoom / 100));
      const y = Math.round((clientOffset.y - canvasRect.top - dragOffset.y) / (zoom / 100));

      // Ensure component is within canvas bounds
      const constrainedX = Math.max(0, Math.min(x, (report?.canvasWidth || 1200) - 200));
      const constrainedY = Math.max(0, Math.min(y, (report?.canvasHeight || 800) - 150));

      onComponentAdd(item.componentType, { x: constrainedX, y: constrainedY });
    },
    hover: (item: any, monitor) => {
      const canvasRect = canvasRef.current?.getBoundingClientRect();
      if (!canvasRect) return;

      const clientOffset = monitor.getClientOffset();
      if (!clientOffset) {
        setDragOffset({ x: 0, y: 0 });
        return;
      }

      // Calculate drag offset for visual feedback
      const x = clientOffset.x - canvasRect.left;
      const y = clientOffset.y - canvasRect.top;
      setDragOffset({ x, y });
    },
    collect: (monitor) => ({
      isOver: monitor.isOver(),
      canDrop: monitor.canDrop(),
    }),
  });

  const handleCanvasClick = useCallback((e: React.MouseEvent) => {
    // Only deselect if clicking on empty canvas
    if (e.target === e.currentTarget) {
      onComponentSelect(null);
    }
  }, [onComponentSelect]);

  const handleComponentClick = useCallback((component: ReportComponent, e: React.MouseEvent) => {
    e.stopPropagation();
    onComponentSelect(component);
  }, [onComponentSelect]);

  const handleComponentDoubleClick = useCallback((component: ReportComponent) => {
    // TODO: Open component editor modal
    message.info(`编辑组件: ${component.componentName}`);
  }, []);

  const handleComponentCopy = useCallback((component: ReportComponent) => {
    const newComponent = {
      ...component,
      componentId: Date.now(), // Temporary ID
      componentName: `${component.componentName}_copy`,
      positionX: component.positionX + 20,
      positionY: component.positionY + 20,
    };
    
    onComponentAdd(component.componentType, { 
      x: newComponent.positionX, 
      y: newComponent.positionY 
    });
    message.success('组件已复制');
  }, [onComponentAdd]);

  const handleComponentLock = useCallback((component: ReportComponent) => {
    onComponentUpdate(component.componentId, { isLocked: !component.isLocked });
  }, [onComponentUpdate]);

  const handleComponentVisibility = useCallback((component: ReportComponent) => {
    onComponentUpdate(component.componentId, { isVisible: !component.isVisible });
  }, [onComponentUpdate]);

  const handleComponentLayerUp = useCallback((component: ReportComponent) => {
    const maxZIndex = Math.max(...components.map(c => c.zIndex || 0));
    onComponentUpdate(component.componentId, { zIndex: maxZIndex + 1 });
  }, [components, onComponentUpdate]);

  const handleComponentLayerDown = useCallback((component: ReportComponent) => {
    const minZIndex = Math.min(...components.map(c => c.zIndex || 0));
    onComponentUpdate(component.componentId, { zIndex: Math.max(1, minZIndex - 1) });
  }, [components, onComponentUpdate]);

  const getCanvasStyle = () => ({
    width: `${(report?.canvasWidth || 1200) * (zoom / 100)}px`,
    height: `${(report?.canvasHeight || 800) * (zoom / 100)}px`,
    transform: `scale(${zoom / 100})`,
    transformOrigin: 'top left',
    backgroundColor: '#ffffff',
    backgroundImage: `
      linear-gradient(rgba(0,0,0,0.1) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0,0,0,0.1) 1px, transparent 1px)
    `,
    backgroundSize: '20px 20px',
    position: 'relative' as const,
    border: '1px solid #d9d9d9',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  });

  const renderComponentToolbar = (component: ReportComponent) => {
    if (selectedComponent?.componentId !== component.componentId) return null;

    return (
      <div
        style={{
          position: 'absolute',
          top: -35,
          left: 0,
          backgroundColor: '#1890ff',
          borderRadius: '4px',
          padding: '4px',
          zIndex: 10000,
          boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <Space size="small">
          <Tooltip title={component.isLocked ? '解锁' : '锁定'}>
            <Button
              type="text"
              size="small"
              icon={component.isLocked ? <UnlockOutlined /> : <LockOutlined />}
              onClick={() => handleComponentLock(component)}
              style={{ color: 'white' }}
            />
          </Tooltip>
          
          <Tooltip title={component.isVisible ? '隐藏' : '显示'}>
            <Button
              type="text"
              size="small"
              icon={component.isVisible ? <EyeInvisibleOutlined /> : <EyeOutlined />}
              onClick={() => handleComponentVisibility(component)}
              style={{ color: 'white' }}
            />
          </Tooltip>
          
          <Tooltip title="复制">
            <Button
              type="text"
              size="small"
              icon={<CopyOutlined />}
              onClick={() => handleComponentCopy(component)}
              style={{ color: 'white' }}
            />
          </Tooltip>
          
          <Tooltip title="上移一层">
            <Button
              type="text"
              size="small"
              icon={<BringForwardOutlined />}
              onClick={() => handleComponentLayerUp(component)}
              style={{ color: 'white' }}
            />
          </Tooltip>
          
          <Tooltip title="下移一层">
            <Button
              type="text"
              size="small"
              icon={<SendBackwardOutlined />}
              onClick={() => handleComponentLayerDown(component)}
              style={{ color: 'white' }}
            />
          </Tooltip>
          
          <Tooltip title="删除">
            <Button
              type="text"
              size="small"
              icon={<DeleteOutlined />}
              onClick={() => onComponentDelete(component.componentId)}
              style={{ color: 'white' }}
              danger
            />
          </Tooltip>
        </Space>
      </div>
    );
  };

  const renderDragPreview = () => {
    if (!isOver || !canDrop) return null;

    return (
      <div
        style={{
          position: 'absolute',
          left: dragOffset.x - 100,
          top: dragOffset.y - 75,
          width: '200px',
          height: '150px',
          backgroundColor: 'rgba(24, 144, 255, 0.1)',
          border: '2px dashed #1890ff',
          borderRadius: '4px',
          pointerEvents: 'none',
          zIndex: 1000,
        }}
      />
    );
  };

  return (
    <div
      style={{
        height: '100%',
        overflow: 'auto',
        backgroundColor: '#f5f5f5',
        position: 'relative',
      }}
    >
      <div
        style={{
          padding: '20px',
          minHeight: '100%',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'flex-start',
        }}
      >
        <div
          ref={(node) => {
            canvasRef.current = node;
            drop(node);
          }}
          style={getCanvasStyle()}
          onClick={handleCanvasClick}
          className={`design-canvas ${isOver ? 'drag-over' : ''}`}
        >
          {/* Render components */}
          {components
            .filter(component => component.isVisible !== false)
            .sort((a, b) => (a.zIndex || 0) - (b.zIndex || 0))
            .map((component) => (
              <ResizableComponent
                key={component.componentId}
                component={component}
                isSelected={selectedComponent?.componentId === component.componentId}
                isLocked={component.isLocked || false}
                onUpdate={(updates) => onComponentUpdate(component.componentId, updates)}
                onClick={(e) => handleComponentClick(component, e)}
                onDoubleClick={() => handleComponentDoubleClick(component)}
              >
                <div style={{ position: 'relative', width: '100%', height: '100%' }}>
                  {renderComponentToolbar(component)}
                  <ComponentRenderer component={component} />
                </div>
              </ResizableComponent>
            ))}

          {/* Drag preview */}
          {renderDragPreview()}

          {/* Empty state */}
          {components.length === 0 && (
            <div
              style={{
                position: 'absolute',
                top: '50%',
                left: '50%',
                transform: 'translate(-50%, -50%)',
                textAlign: 'center',
                color: '#999',
              }}
            >
              <Text type="secondary" style={{ fontSize: '16px' }}>
                拖拽组件到此处开始设计报表
              </Text>
            </div>
          )}
        </div>
      </div>

      {/* Canvas info */}
      <div
        style={{
          position: 'absolute',
          bottom: '16px',
          right: '16px',
          backgroundColor: 'rgba(0, 0, 0, 0.6)',
          color: 'white',
          padding: '4px 8px',
          borderRadius: '4px',
          fontSize: '12px',
        }}
      >
        {report?.canvasWidth || 1200} × {report?.canvasHeight || 800} | {zoom}%
      </div>
    </div>
  );
};

export default DesignCanvas;