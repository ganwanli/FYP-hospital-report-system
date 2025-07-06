import React, { useState, useEffect, useCallback } from 'react';
import {
  Layout,
  Card,
  Button,
  Space,
  Typography,
  Breadcrumb,
  message,
  Spin,
  Alert,
  Drawer,
  Modal,
  Tooltip,
  Dropdown,
  Menu,
  Tag,
  Progress,
} from 'antd';
import {
  ReloadOutlined,
  DownloadOutlined,
  ShareAltOutlined,
  PrinterOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined,
  SettingOutlined,
  MobileOutlined,
  DesktopOutlined,
  EyeOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import { useParams, useNavigate } from 'react-router-dom';
import { reportViewApi, reportShareApi } from '../../../services/reportView';
import type { 
  ReportParameterDefinition, 
  ReportGenerationResult, 
  ExportOptions,
  ReportShare 
} from '../../../services/reportView';
import ParameterPanel from '../../../components/ReportParameterPanel';
import ReportRenderer from '../../../components/ReportRenderer';
import ExportModal from '../../../components/ExportModal';
import ShareModal from '../../../components/ShareModal';

const { Header, Content, Sider } = Layout;
const { Title, Text } = Typography;

interface ReportViewerProps {}

const ReportViewer: React.FC<ReportViewerProps> = () => {
  const { reportId } = useParams<{ reportId: string }>();
  const navigate = useNavigate();

  // 状态管理
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [reportData, setReportData] = useState<ReportGenerationResult | null>(null);
  const [parameters, setParameters] = useState<Record<string, any>>({});
  const [parameterDefinitions, setParameterDefinitions] = useState<ReportParameterDefinition[]>([]);
  
  // UI状态
  const [parameterPanelVisible, setParameterPanelVisible] = useState(true);
  const [exportModalVisible, setExportModalVisible] = useState(false);
  const [shareModalVisible, setShareModalVisible] = useState(false);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [viewMode, setViewMode] = useState<'desktop' | 'mobile'>('desktop');
  const [autoRefresh, setAutoRefresh] = useState(false);
  const [refreshInterval, setRefreshInterval] = useState(30); // 秒
  
  // 错误状态
  const [error, setError] = useState<string | null>(null);
  const [estimatedTime, setEstimatedTime] = useState<number>(0);

  useEffect(() => {
    if (reportId) {
      loadParameterDefinitions();
    }
  }, [reportId]);

  useEffect(() => {
    let intervalId: NodeJS.Timeout;
    
    if (autoRefresh && refreshInterval > 0) {
      intervalId = setInterval(() => {
        if (reportData) {
          generateReport(false);
        }
      }, refreshInterval * 1000);
    }
    
    return () => {
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, [autoRefresh, refreshInterval, reportData]);

  const loadParameterDefinitions = async () => {
    if (!reportId) return;
    
    setLoading(true);
    try {
      const response = await reportViewApi.getParameterDefinitions(parseInt(reportId));
      setParameterDefinitions(response.data || []);
      
      // 设置默认参数值
      const defaultParams: Record<string, any> = {};
      response.data?.forEach((param: ReportParameterDefinition) => {
        if (param.defaultValue !== undefined) {
          defaultParams[param.name] = param.defaultValue;
        }
      });
      setParameters(defaultParams);
      
      // 如果有默认参数或无参数，自动生成报表
      if (response.data?.length === 0 || Object.keys(defaultParams).length > 0) {
        generateReport(false);
      }
      
    } catch (error) {
      message.error('加载参数定义失败');
      setError('加载参数定义失败');
    } finally {
      setLoading(false);
    }
  };

  const generateReport = async (showLoading = true) => {
    if (!reportId) return;
    
    if (showLoading) {
      setGenerating(true);
    }
    setError(null);
    
    try {
      // 估算生成时间
      const estimateResponse = await reportViewApi.estimateGenerationTime(parseInt(reportId), parameters);
      setEstimatedTime(estimateResponse.data || 0);
      
      // 生成报表
      const response = await reportViewApi.generateReport(parseInt(reportId), parameters);
      setReportData(response.data);
      
      if (response.data.fromCache) {
        message.success('报表加载完成（来自缓存）');
      } else {
        message.success(`报表生成完成，耗时 ${response.data.generationTime}ms`);
      }
      
    } catch (error: any) {
      const errorMsg = error.response?.data?.message || '报表生成失败';
      message.error(errorMsg);
      setError(errorMsg);
    } finally {
      if (showLoading) {
        setGenerating(false);
      }
    }
  };

  const handleParametersChange = (newParameters: Record<string, any>) => {
    setParameters(newParameters);
  };

  const handleParametersSubmit = () => {
    generateReport();
  };

  const handleExport = async (format: string, options: ExportOptions) => {
    if (!reportId || !reportData) return;
    
    try {
      let response;
      const fileName = `${reportData.reportName}_${Date.now()}`;
      
      switch (format.toLowerCase()) {
        case 'pdf':
          response = await reportViewApi.exportToPdf(parseInt(reportId), parameters, options);
          downloadBlob(response.data, `${fileName}.pdf`, 'application/pdf');
          break;
        case 'excel':
          response = await reportViewApi.exportToExcel(parseInt(reportId), parameters, options);
          downloadBlob(response.data, `${fileName}.xlsx`, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
          break;
        case 'word':
          response = await reportViewApi.exportToWord(parseInt(reportId), parameters, options);
          downloadBlob(response.data, `${fileName}.docx`, 'application/vnd.openxmlformats-officedocument.wordprocessingml.document');
          break;
        case 'csv':
          response = await reportViewApi.exportToCsv(parseInt(reportId), parameters, options);
          downloadBlob(response.data, `${fileName}.csv`, 'text/csv');
          break;
        case 'png':
        case 'jpeg':
          response = await reportViewApi.exportToImage(parseInt(reportId), format.toUpperCase(), parameters, options);
          downloadBlob(response.data, `${fileName}.${format.toLowerCase()}`, `image/${format.toLowerCase()}`);
          break;
        default:
          message.error('不支持的导出格式');
          return;
      }
      
      message.success(`${format.toUpperCase()} 导出成功`);
      setExportModalVisible(false);
      
    } catch (error) {
      message.error(`${format.toUpperCase()} 导出失败`);
    }
  };

  const downloadBlob = (blob: Blob, fileName: string, mimeType: string) => {
    const url = URL.createObjectURL(new Blob([blob], { type: mimeType }));
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
    URL.revokeObjectURL(url);
  };

  const handleShare = async (shareData: Partial<ReportShare>) => {
    try {
      const response = await reportShareApi.createShare({
        ...shareData,
        reportId: parseInt(reportId!)
      });
      
      message.success('分享创建成功');
      setShareModalVisible(false);
      
      // 获取分享URL
      const urlResponse = await reportShareApi.generateShareUrl(response.data.shareCode);
      
      // 复制到剪贴板
      navigator.clipboard.writeText(urlResponse.data);
      message.info('分享链接已复制到剪贴板');
      
    } catch (error) {
      message.error('创建分享失败');
    }
  };

  const handlePrint = () => {
    if (!reportData) return;
    
    const printWindow = window.open('', '_blank');
    if (printWindow) {
      // 获取打印版HTML
      reportViewApi.renderToPrint(parseInt(reportId!), parameters)
        .then(response => {
          printWindow.document.write(response.data);
          printWindow.document.close();
          printWindow.focus();
          printWindow.print();
        })
        .catch(() => {
          message.error('打印预览生成失败');
          printWindow.close();
        });
    }
  };

  const toggleFullscreen = () => {
    setIsFullscreen(!isFullscreen);
  };

  const toggleViewMode = () => {
    setViewMode(viewMode === 'desktop' ? 'mobile' : 'desktop');
  };

  const exportMenu = (
    <Menu onClick={({ key }) => {
      if (key === 'export') {
        setExportModalVisible(true);
      }
    }}>
      <Menu.Item key="export" icon={<DownloadOutlined />}>
        导出选项
      </Menu.Item>
    </Menu>
  );

  const settingsMenu = (
    <Menu onClick={({ key }) => {
      switch (key) {
        case 'autoRefresh':
          setAutoRefresh(!autoRefresh);
          break;
        case 'refreshInterval':
          Modal.confirm({
            title: '设置刷新间隔',
            content: (
              <div>
                <p>当前刷新间隔: {refreshInterval} 秒</p>
                <input
                  type="number"
                  min="10"
                  max="300"
                  defaultValue={refreshInterval}
                  onChange={(e) => setRefreshInterval(parseInt(e.target.value) || 30)}
                  style={{ width: '100%', padding: '4px 8px', border: '1px solid #d9d9d9', borderRadius: '4px' }}
                />
              </div>
            ),
            onOk: () => {
              message.success('刷新间隔已更新');
            }
          });
          break;
      }
    }}>
      <Menu.Item 
        key="autoRefresh" 
        icon={<ClockCircleOutlined />}
      >
        自动刷新 {autoRefresh && <Tag color="green">已开启</Tag>}
      </Menu.Item>
      <Menu.Item key="refreshInterval">
        刷新间隔 ({refreshInterval}s)
      </Menu.Item>
    </Menu>
  );

  const toolbar = (
    <Space>
      <Button
        icon={<ReloadOutlined />}
        onClick={() => generateReport()}
        loading={generating}
        disabled={!reportData && !error}
      >
        刷新
      </Button>
      
      <Button
        icon={viewMode === 'desktop' ? <MobileOutlined /> : <DesktopOutlined />}
        onClick={toggleViewMode}
        title={viewMode === 'desktop' ? '切换到移动端视图' : '切换到桌面端视图'}
      />
      
      <Dropdown overlay={exportMenu} disabled={!reportData}>
        <Button icon={<DownloadOutlined />}>
          导出
        </Button>
      </Dropdown>
      
      <Button
        icon={<ShareAltOutlined />}
        onClick={() => setShareModalVisible(true)}
        disabled={!reportData}
      >
        分享
      </Button>
      
      <Button
        icon={<PrinterOutlined />}
        onClick={handlePrint}
        disabled={!reportData}
      >
        打印
      </Button>
      
      <Button
        icon={isFullscreen ? <FullscreenExitOutlined /> : <FullscreenOutlined />}
        onClick={toggleFullscreen}
        title={isFullscreen ? '退出全屏' : '全屏显示'}
      />
      
      <Dropdown overlay={settingsMenu}>
        <Button icon={<SettingOutlined />} />
      </Dropdown>
    </Space>
  );

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" tip="加载中..." />
      </div>
    );
  }

  return (
    <Layout className={`report-viewer ${isFullscreen ? 'fullscreen' : ''}`}>
      <Header className="viewer-header">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <Breadcrumb>
              <Breadcrumb.Item>报表</Breadcrumb.Item>
              <Breadcrumb.Item>查看</Breadcrumb.Item>
              <Breadcrumb.Item>{reportData?.reportName || `报表 ${reportId}`}</Breadcrumb.Item>
            </Breadcrumb>
            <Title level={4} style={{ margin: 0, marginTop: '4px', color: 'white' }}>
              {reportData?.reportName || `报表 ${reportId}`}
            </Title>
          </div>
          {toolbar}
        </div>
      </Header>
      
      <Layout>
        {parameterDefinitions.length > 0 && (
          <Sider
            width={300}
            collapsed={!parameterPanelVisible}
            onCollapse={setParameterPanelVisible}
            collapsible
            theme="light"
            className="viewer-parameter-sider"
          >
            <ParameterPanel
              parameters={parameterDefinitions}
              values={parameters}
              onChange={handleParametersChange}
              onSubmit={handleParametersSubmit}
              loading={generating}
            />
          </Sider>
        )}
        
        <Content className="viewer-content">
          {error && (
            <Alert
              message="报表生成失败"
              description={error}
              type="error"
              showIcon
              style={{ margin: '16px' }}
              action={
                <Button size="small" onClick={() => generateReport()}>
                  重试
                </Button>
              }
            />
          )}
          
          {generating && (
            <Card style={{ margin: '16px' }}>
              <div style={{ textAlign: 'center', padding: '40px' }}>
                <Spin size="large" />
                <div style={{ marginTop: '16px' }}>
                  <Text>正在生成报表...</Text>
                  {estimatedTime > 0 && (
                    <div style={{ marginTop: '8px' }}>
                      <Text type="secondary">预计耗时: {Math.ceil(estimatedTime / 1000)} 秒</Text>
                    </div>
                  )}
                </div>
              </div>
            </Card>
          )}
          
          {reportData && !generating && (
            <ReportRenderer
              reportData={reportData}
              viewMode={viewMode}
              isFullscreen={isFullscreen}
            />
          )}
          
          {!reportData && !generating && !error && parameterDefinitions.length > 0 && (
            <Card style={{ margin: '16px' }}>
              <div style={{ textAlign: 'center', padding: '40px' }}>
                <EyeOutlined style={{ fontSize: '48px', color: '#d9d9d9', marginBottom: '16px' }} />
                <div>
                  <Text type="secondary">请设置参数并点击生成按钮查看报表</Text>
                </div>
              </div>
            </Card>
          )}
        </Content>
      </Layout>
      
      {/* 导出模态框 */}
      <ExportModal
        visible={exportModalVisible}
        onCancel={() => setExportModalVisible(false)}
        onExport={handleExport}
        reportData={reportData}
      />
      
      {/* 分享模态框 */}
      <ShareModal
        visible={shareModalVisible}
        onCancel={() => setShareModalVisible(false)}
        onShare={handleShare}
        reportId={reportId ? parseInt(reportId) : undefined}
        reportName={reportData?.reportName}
      />
      
      {/* 报表信息 */}
      {reportData && (
        <div
          style={{
            position: 'fixed',
            bottom: '16px',
            right: '16px',
            backgroundColor: 'rgba(0, 0, 0, 0.8)',
            color: 'white',
            padding: '8px 12px',
            borderRadius: '4px',
            fontSize: '12px',
            zIndex: 1000,
          }}
        >
          <div>组件: {reportData.componentCount}</div>
          <div>生成时间: {reportData.generationTime}ms</div>
          {reportData.fromCache && <div>来源: 缓存</div>}
          {autoRefresh && <div>自动刷新: {refreshInterval}s</div>}
        </div>
      )}
    </Layout>
  );
};

export default ReportViewer;