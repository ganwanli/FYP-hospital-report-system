import React, { useMemo } from 'react';
import { Image } from 'antd';
import { ReportComponent } from '../../services/report';

interface ImageComponentProps {
  component: ReportComponent;
}

const ImageComponent: React.FC<ImageComponentProps> = ({ component }) => {
  const imageConfig = useMemo(() => {
    try {
      return component.imageConfig ? JSON.parse(component.imageConfig) : {};
    } catch {
      return {};
    }
  }, [component.imageConfig]);

  const styleConfig = useMemo(() => {
    try {
      return component.styleConfig ? JSON.parse(component.styleConfig) : {};
    } catch {
      return {};
    }
  }, [component.styleConfig]);

  const src = imageConfig.src || '';
  const alt = imageConfig.alt || '图片';
  const objectFit = imageConfig.objectFit || 'cover';
  const borderRadius = styleConfig.borderRadius || '0px';
  const border = styleConfig.border || 'none';
  const boxShadow = styleConfig.boxShadow || 'none';
  const opacity = styleConfig.opacity || 1;
  const filter = styleConfig.filter || 'none';

  // Default placeholder image for design mode
  const placeholderSrc = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjE1MCIgdmlld0JveD0iMCAwIDIwMCAxNTAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyMDAiIGhlaWdodD0iMTUwIiBmaWxsPSIjRjVGNUY1Ii8+CjxwYXRoIGQ9Ik04NSA2MEw5NSA3MEwxMTUgNTBMMTM1IDcwTDE1NSA1MEwxNzUgNzBWMTEwSDE1NUgxMzVIMTE1SDk1SDg1VjEwMFY5MFY4MFY3MFY2MFoiIGZpbGw9IiNEOUQ5RDkiLz4KPGNpcmNsZSBjeD0iNjAiIGN5PSI0MCIgcj0iMTAiIGZpbGw9IiNEOUQ5RDkiLz4KPHN2ZyB4PSI3NSIgeT0iNjAiIHdpZHRoPSI1MCIgaGVpZ2h0PSI1MCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSIjOTk5OTk5Ij4KPHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGZpbGw9Im5vbmUiIHZpZXdCb3g9IjAgMCAyNCAyNCIgc3Ryb2tlPSJjdXJyZW50Q29sb3IiIHN0cm9rZS13aWR0aD0iMiI+CjxwYXRoIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIgZD0iTTQgMTZsNC00IDQgNCA2LTYgMiAyIi8+CjxwYXRoIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIgZD0iTTkgN2gxdjF2MWgxVjhoMVY3aDFWNkg5VjdaIi8+CjxwYXRoIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIgZD0iTTMgM2gxOGEyIDIgMCAwIDEgMiAydjE0YTIgMiAwIDAgMS0yIDJIM2EyIDIgMCAwIDEtMi0yVjVhMiAyIDAgMCAxIDItMloiLz4KPC9zdmc+Cjwvc3ZnPgo=';

  const imageStyle = {
    width: '100%',
    height: '100%',
    objectFit: objectFit as any,
    borderRadius,
    border,
    boxShadow,
    opacity,
    filter,
  };

  if (!src) {
    return (
      <div
        style={{
          width: '100%',
          height: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          backgroundColor: '#f5f5f5',
          borderRadius,
          border: border === 'none' ? '1px dashed #d9d9d9' : border,
          boxShadow,
          opacity,
          filter,
        }}
      >
        <div style={{ textAlign: 'center', color: '#999' }}>
          <img
            src={placeholderSrc}
            alt="placeholder"
            style={{
              width: '60px',
              height: '45px',
              marginBottom: '8px',
              opacity: 0.5,
            }}
          />
          <div style={{ fontSize: '12px' }}>
            点击配置图片
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ width: '100%', height: '100%', overflow: 'hidden' }}>
      <Image
        src={src}
        alt={alt}
        style={imageStyle}
        preview={false}
        fallback={placeholderSrc}
        onError={(e) => {
          console.error('Image load error:', e);
        }}
      />
    </div>
  );
};

export default ImageComponent;