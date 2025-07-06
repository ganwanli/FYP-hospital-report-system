import React, { useRef, useEffect, useMemo } from 'react';
import * as echarts from 'echarts';
import { ReportComponent } from '../../services/report';

interface ChartComponentProps {
  component: ReportComponent;
}

const ChartComponent: React.FC<ChartComponentProps> = ({ component }) => {
  const chartRef = useRef<HTMLDivElement>(null);
  const chartInstance = useRef<echarts.ECharts | null>(null);

  const chartConfig = useMemo(() => {
    try {
      return component.chartConfig ? JSON.parse(component.chartConfig) : {};
    } catch {
      return {};
    }
  }, [component.chartConfig]);

  const dataConfig = useMemo(() => {
    try {
      return component.dataConfig ? JSON.parse(component.dataConfig) : {};
    } catch {
      return {};
    }
  }, [component.dataConfig]);

  const styleConfig = useMemo(() => {
    try {
      return component.styleConfig ? JSON.parse(component.styleConfig) : {};
    } catch {
      return {};
    }
  }, [component.styleConfig]);

  // Sample data for different chart types
  const getSampleData = () => {
    switch (component.componentType) {
      case 'bar-chart':
        return {
          categories: ['内科', '外科', '儿科', '妇科', '骨科'],
          values: [120, 200, 150, 80, 70],
        };
      case 'line-chart':
        return {
          categories: ['1月', '2月', '3月', '4月', '5月', '6月'],
          values: [820, 932, 901, 934, 1290, 1330],
        };
      case 'pie-chart':
        return [
          { value: 335, name: '内科' },
          { value: 310, name: '外科' },
          { value: 234, name: '儿科' },
          { value: 135, name: '妇科' },
          { value: 148, name: '骨科' },
        ];
      default:
        return {};
    }
  };

  const getChartOption = () => {
    const sampleData = getSampleData();
    const title = chartConfig.title || component.componentName;
    const showLegend = chartConfig.showLegend !== false;

    const baseOption = {
      title: {
        text: title,
        textStyle: {
          fontSize: styleConfig.titleFontSize || 14,
          color: styleConfig.titleColor || '#333',
        },
      },
      tooltip: {
        trigger: 'axis',
      },
      legend: {
        show: showLegend,
        top: 'bottom',
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: showLegend ? '15%' : '3%',
        containLabel: true,
      },
    };

    switch (component.componentType) {
      case 'bar-chart':
        return {
          ...baseOption,
          xAxis: {
            type: 'category',
            data: sampleData.categories,
            axisLabel: {
              fontSize: styleConfig.axisLabelFontSize || 12,
            },
          },
          yAxis: {
            type: 'value',
            axisLabel: {
              fontSize: styleConfig.axisLabelFontSize || 12,
            },
          },
          series: [
            {
              name: chartConfig.seriesName || '数量',
              type: 'bar',
              data: sampleData.values,
              itemStyle: {
                color: styleConfig.primaryColor || '#1890ff',
              },
            },
          ],
        };

      case 'line-chart':
        return {
          ...baseOption,
          xAxis: {
            type: 'category',
            data: sampleData.categories,
            axisLabel: {
              fontSize: styleConfig.axisLabelFontSize || 12,
            },
          },
          yAxis: {
            type: 'value',
            axisLabel: {
              fontSize: styleConfig.axisLabelFontSize || 12,
            },
          },
          series: [
            {
              name: chartConfig.seriesName || '数量',
              type: 'line',
              data: sampleData.values,
              smooth: chartConfig.smooth !== false,
              itemStyle: {
                color: styleConfig.primaryColor || '#1890ff',
              },
              lineStyle: {
                color: styleConfig.primaryColor || '#1890ff',
              },
            },
          ],
        };

      case 'pie-chart':
        return {
          ...baseOption,
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)',
          },
          series: [
            {
              name: chartConfig.seriesName || '分布',
              type: 'pie',
              radius: `${(chartConfig.radius || 0.8) * 100}%`,
              data: sampleData,
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.5)',
                },
              },
            },
          ],
        };

      default:
        return {
          ...baseOption,
          series: [],
        };
    }
  };

  useEffect(() => {
    if (chartRef.current) {
      chartInstance.current = echarts.init(chartRef.current);
      const option = getChartOption();
      chartInstance.current.setOption(option);
    }

    return () => {
      if (chartInstance.current) {
        chartInstance.current.dispose();
        chartInstance.current = null;
      }
    };
  }, [component.componentType, chartConfig, dataConfig, styleConfig]);

  useEffect(() => {
    if (chartInstance.current) {
      chartInstance.current.resize();
    }
  }, [component.width, component.height]);

  return (
    <div
      ref={chartRef}
      style={{
        width: '100%',
        height: '100%',
        backgroundColor: styleConfig.backgroundColor || 'transparent',
      }}
    />
  );
};

export default ChartComponent;