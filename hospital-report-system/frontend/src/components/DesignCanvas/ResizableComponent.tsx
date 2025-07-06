import React, { useState, useRef, useCallback } from 'react';
import { ReportComponent } from '../../services/report';

interface ResizableComponentProps {
  component: ReportComponent;
  isSelected: boolean;
  isLocked: boolean;
  children: React.ReactNode;
  onUpdate: (updates: Partial<ReportComponent>) => void;
  onClick: (e: React.MouseEvent) => void;
  onDoubleClick: () => void;
}

const ResizableComponent: React.FC<ResizableComponentProps> = ({
  component,
  isSelected,
  isLocked,
  children,
  onUpdate,
  onClick,
  onDoubleClick,
}) => {
  const componentRef = useRef<HTMLDivElement>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isResizing, setIsResizing] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const [resizeStart, setResizeStart] = useState({ x: 0, y: 0, width: 0, height: 0 });
  const [resizeDirection, setResizeDirection] = useState<string>('');

  const handleMouseDown = useCallback((e: React.MouseEvent) => {
    if (isLocked) return;
    
    e.preventDefault();
    e.stopPropagation();
    
    const rect = componentRef.current?.getBoundingClientRect();
    if (!rect) return;

    setIsDragging(true);
    setDragStart({
      x: e.clientX - component.positionX,
      y: e.clientY - component.positionY,
    });

    const handleMouseMove = (e: MouseEvent) => {
      if (!isDragging) return;
      
      const newX = Math.max(0, e.clientX - dragStart.x);
      const newY = Math.max(0, e.clientY - dragStart.y);
      
      onUpdate({
        positionX: newX,
        positionY: newY,
      });
    };

    const handleMouseUp = () => {
      setIsDragging(false);
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };

    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
  }, [component.positionX, component.positionY, dragStart, isDragging, isLocked, onUpdate]);

  const handleResizeMouseDown = useCallback((e: React.MouseEvent, direction: string) => {
    if (isLocked) return;
    
    e.preventDefault();
    e.stopPropagation();
    
    setIsResizing(true);
    setResizeDirection(direction);
    setResizeStart({
      x: e.clientX,
      y: e.clientY,
      width: component.width,
      height: component.height,
    });

    const handleMouseMove = (e: MouseEvent) => {
      if (!isResizing) return;
      
      const deltaX = e.clientX - resizeStart.x;
      const deltaY = e.clientY - resizeStart.y;
      
      let newWidth = component.width;
      let newHeight = component.height;
      let newX = component.positionX;
      let newY = component.positionY;
      
      switch (direction) {
        case 'n':
          newHeight = Math.max(50, resizeStart.height - deltaY);
          newY = component.positionY + (resizeStart.height - newHeight);
          break;
        case 's':
          newHeight = Math.max(50, resizeStart.height + deltaY);
          break;
        case 'e':
          newWidth = Math.max(50, resizeStart.width + deltaX);
          break;
        case 'w':
          newWidth = Math.max(50, resizeStart.width - deltaX);
          newX = component.positionX + (resizeStart.width - newWidth);
          break;
        case 'ne':
          newWidth = Math.max(50, resizeStart.width + deltaX);
          newHeight = Math.max(50, resizeStart.height - deltaY);
          newY = component.positionY + (resizeStart.height - newHeight);
          break;
        case 'nw':
          newWidth = Math.max(50, resizeStart.width - deltaX);
          newHeight = Math.max(50, resizeStart.height - deltaY);
          newX = component.positionX + (resizeStart.width - newWidth);
          newY = component.positionY + (resizeStart.height - newHeight);
          break;
        case 'se':
          newWidth = Math.max(50, resizeStart.width + deltaX);
          newHeight = Math.max(50, resizeStart.height + deltaY);
          break;
        case 'sw':
          newWidth = Math.max(50, resizeStart.width - deltaX);
          newHeight = Math.max(50, resizeStart.height + deltaY);
          newX = component.positionX + (resizeStart.width - newWidth);
          break;
      }
      
      onUpdate({
        positionX: newX,
        positionY: newY,
        width: newWidth,
        height: newHeight,
      });
    };

    const handleMouseUp = () => {
      setIsResizing(false);
      setResizeDirection('');
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };

    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
  }, [component, resizeStart, isResizing, isLocked, onUpdate]);

  const getComponentStyle = () => ({
    position: 'absolute' as const,
    left: `${component.positionX}px`,
    top: `${component.positionY}px`,
    width: `${component.width}px`,
    height: `${component.height}px`,
    zIndex: component.zIndex || 1,
    cursor: isLocked ? 'not-allowed' : isDragging ? 'grabbing' : 'grab',
    border: isSelected ? '2px solid #1890ff' : '1px solid transparent',
    borderRadius: '4px',
    boxShadow: isSelected ? '0 0 0 2px rgba(24, 144, 255, 0.2)' : undefined,
    opacity: component.isVisible === false ? 0.5 : 1,
    userSelect: 'none',
  });

  const getResizeHandleStyle = (direction: string) => ({
    position: 'absolute' as const,
    backgroundColor: '#1890ff',
    border: '1px solid #fff',
    borderRadius: '2px',
    width: '8px',
    height: '8px',
    zIndex: 10,
    cursor: getCursorForDirection(direction),
    display: isSelected && !isLocked ? 'block' : 'none',
    ...getHandlePosition(direction),
  });

  const getCursorForDirection = (direction: string) => {
    switch (direction) {
      case 'n':
      case 's':
        return 'ns-resize';
      case 'e':
      case 'w':
        return 'ew-resize';
      case 'ne':
      case 'sw':
        return 'nesw-resize';
      case 'nw':
      case 'se':
        return 'nwse-resize';
      default:
        return 'default';
    }
  };

  const getHandlePosition = (direction: string) => {
    switch (direction) {
      case 'n':
        return { top: '-4px', left: '50%', transform: 'translateX(-50%)' };
      case 's':
        return { bottom: '-4px', left: '50%', transform: 'translateX(-50%)' };
      case 'e':
        return { right: '-4px', top: '50%', transform: 'translateY(-50%)' };
      case 'w':
        return { left: '-4px', top: '50%', transform: 'translateY(-50%)' };
      case 'ne':
        return { top: '-4px', right: '-4px' };
      case 'nw':
        return { top: '-4px', left: '-4px' };
      case 'se':
        return { bottom: '-4px', right: '-4px' };
      case 'sw':
        return { bottom: '-4px', left: '-4px' };
      default:
        return {};
    }
  };

  return (
    <div
      ref={componentRef}
      style={getComponentStyle()}
      onMouseDown={handleMouseDown}
      onClick={onClick}
      onDoubleClick={onDoubleClick}
      className={`resizable-component ${isSelected ? 'selected' : ''} ${isLocked ? 'locked' : ''}`}
    >
      {children}
      
      {/* Resize handles */}
      {['n', 's', 'e', 'w', 'ne', 'nw', 'se', 'sw'].map(direction => (
        <div
          key={direction}
          style={getResizeHandleStyle(direction)}
          onMouseDown={(e) => handleResizeMouseDown(e, direction)}
        />
      ))}
    </div>
  );
};

export default ResizableComponent;