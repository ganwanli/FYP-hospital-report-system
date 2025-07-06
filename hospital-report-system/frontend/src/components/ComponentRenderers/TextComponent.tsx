import React, { useMemo } from 'react';
import { Typography } from 'antd';
import { ReportComponent } from '../../services/report';

const { Text, Title, Paragraph } = Typography;

interface TextComponentProps {
  component: ReportComponent;
}

const TextComponent: React.FC<TextComponentProps> = ({ component }) => {
  const textConfig = useMemo(() => {
    try {
      return component.textConfig ? JSON.parse(component.textConfig) : {};
    } catch {
      return {};
    }
  }, [component.textConfig]);

  const styleConfig = useMemo(() => {
    try {
      return component.styleConfig ? JSON.parse(component.styleConfig) : {};
    } catch {
      return {};
    }
  }, [component.styleConfig]);

  const content = textConfig.content || '文本内容';
  const textType = textConfig.textType || 'text'; // text, title, paragraph
  const level = textConfig.level || 4; // for title
  const fontSize = styleConfig.fontSize || textConfig.fontSize || 14;
  const fontWeight = styleConfig.fontWeight || textConfig.fontWeight || 'normal';
  const color = styleConfig.color || textConfig.color || '#000000';
  const textAlign = styleConfig.textAlign || textConfig.textAlign || 'left';
  const lineHeight = styleConfig.lineHeight || textConfig.lineHeight || 1.5;
  const letterSpacing = styleConfig.letterSpacing || textConfig.letterSpacing || 'normal';
  const backgroundColor = styleConfig.backgroundColor || 'transparent';
  const padding = styleConfig.padding || '0px';
  const borderRadius = styleConfig.borderRadius || '0px';
  const border = styleConfig.border || 'none';
  const boxShadow = styleConfig.boxShadow || 'none';

  const baseStyle = {
    fontSize: `${fontSize}px`,
    fontWeight,
    color,
    textAlign: textAlign as any,
    lineHeight,
    letterSpacing,
    backgroundColor,
    padding,
    borderRadius,
    border,
    boxShadow,
    width: '100%',
    height: '100%',
    overflow: 'hidden',
    display: 'flex',
    alignItems: textConfig.verticalAlign === 'middle' ? 'center' : 
                textConfig.verticalAlign === 'bottom' ? 'flex-end' : 'flex-start',
    margin: 0,
  };

  const renderContent = () => {
    switch (textType) {
      case 'title':
        return (
          <Title
            level={level}
            style={{
              ...baseStyle,
              fontSize: `${fontSize}px`,
              fontWeight,
              color,
              margin: 0,
              lineHeight,
            }}
          >
            {content}
          </Title>
        );
      case 'paragraph':
        return (
          <Paragraph
            style={{
              ...baseStyle,
              fontSize: `${fontSize}px`,
              color,
              margin: 0,
              lineHeight,
            }}
          >
            {content}
          </Paragraph>
        );
      default:
        return (
          <Text
            style={{
              ...baseStyle,
              fontSize: `${fontSize}px`,
              color,
              lineHeight,
            }}
          >
            {content}
          </Text>
        );
    }
  };

  return (
    <div style={{ width: '100%', height: '100%', overflow: 'hidden' }}>
      {renderContent()}
    </div>
  );
};

export default TextComponent;