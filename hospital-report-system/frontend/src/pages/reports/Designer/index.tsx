import React, { useState, useEffect, useCallback } from 'react';
import { DndProvider } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';
import {
  Layout,
  Sider,
  Content,
  Header,
  Button,
  Space,
  Typography,
  Breadcrumb,
  message,
  Modal,
  Input,
  Tooltip,
  Dropdown,
  Menu
} from 'antd';
import {
  SaveOutlined,
  EyeOutlined,
  PlayCircleOutlined,
  ShareAltOutlined,
  DownloadOutlined,
  UploadOutlined,
  CopyOutlined,
  SettingOutlined,
  HistoryOutlined,
  FullscreenOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
  UndoOutlined,
  RedoOutlined
} from '@ant-design/icons';
import { useParams, useNavigate } from 'react-router-dom';

import ComponentPanel from '../../../components/ComponentPanel';
import DesignCanvas from '../../../components/DesignCanvas';
import PropertyPanel from '../../../components/PropertyPanel';
import DataBindingPanel from '../../../components/DataBindingPanel';
import PreviewModal from '../../../components/PreviewModal';
import VersionHistoryModal from '../../../components/VersionHistoryModal';

import { reportApi } from '../../../services/report';
import type { ReportConfig, ReportComponent, ReportDataSource } from '../../../services/report';

const { Title } = Typography;

interface ReportDesignerProps {}

const ReportDesigner: React.FC<ReportDesignerProps> = () => {
  const { reportId } = useParams<{ reportId: string }>();
  const navigate = useNavigate();
  
  // State management
  const [report, setReport] = useState<ReportConfig | null>(null);
  const [components, setComponents] = useState<ReportComponent[]>([]);
  const [dataSources, setDataSources] = useState<ReportDataSource[]>([]);
  const [selectedComponent, setSelectedComponent] = useState<ReportComponent | null>(null);
  const [selectedDataSource, setSelectedDataSource] = useState<ReportDataSource | null>(null);
  
  // UI state
  const [loading, setLoading] = useState<boolean>(false);
  const [saving, setSaving] = useState<boolean>(false);
  const [canvasZoom, setCanvasZoom] = useState<number>(100);
  const [isFullscreen, setIsFullscreen] = useState<boolean>(false);
  const [siderCollapsed, setSiderCollapsed] = useState<boolean>(false);
  const [rightSiderCollapsed, setRightSiderCollapsed] = useState<boolean>(false);
  
  // Modal states
  const [previewVisible, setPreviewVisible] = useState<boolean>(false);
  const [versionHistoryVisible, setVersionHistoryVisible] = useState<boolean>(false);
  const [saveAsVisible, setSaveAsVisible] = useState<boolean>(false);
  const [exportVisible, setExportVisible] = useState<boolean>(false);
  
  // Undo/Redo state
  const [history, setHistory] = useState<ReportConfig[]>([]);
  const [historyIndex, setHistoryIndex] = useState<number>(-1);
  
  // Form states
  const [saveAsName, setSaveAsName] = useState<string>('');

  useEffect(() => {
    if (reportId && reportId !== 'new') {
      loadReport(parseInt(reportId));
    } else {
      initializeNewReport();
    }
  }, [reportId]);

  const loadReport = async (id: number) => {
    setLoading(true);
    try {
      const response = await reportApi.getReportWithComponents(id);
      const reportData = response.data;
      
      setReport(reportData);
      setComponents(reportData.components || []);
      setDataSources(reportData.dataSources || []);
      
      // Initialize history
      setHistory([reportData]);
      setHistoryIndex(0);
      
    } catch (error) {
      message.error('Failed to load report');
      console.error('Error loading report:', error);
    } finally {
      setLoading(false);
    }
  };

  const initializeNewReport = () => {
    const newReport: Partial<ReportConfig> = {
      reportName: 'Untitled Report',
      reportDescription: '',
      reportCategory: 'DASHBOARD',
      reportType: 'INTERACTIVE',
      canvasWidth: 1200,
      canvasHeight: 800,
      isPublished: false,
      isActive: true,
      accessLevel: 'PRIVATE',
      layoutConfig: '{}',
      componentsConfig: '{}',
      dataSourcesConfig: '{}',
      styleConfig: '{}'
    };
    
    setReport(newReport as ReportConfig);
    setComponents([]);
    setDataSources([]);
    setHistory([newReport as ReportConfig]);
    setHistoryIndex(0);
  };

  const saveReport = async () => {
    if (!report) return;
    
    setSaving(true);
    try {
      const reportData = {
        ...report,
        components,
        dataSources,
        componentsConfig: JSON.stringify(components),
        dataSourcesConfig: JSON.stringify(dataSources),
        updatedTime: new Date().toISOString()
      };
      
      let savedReport;
      if (report.reportId) {
        savedReport = await reportApi.updateReport(report.reportId, reportData);
      } else {
        savedReport = await reportApi.createReport({
          ...reportData,
          createdBy: 1, // TODO: Get from auth context
          updatedBy: 1
        });
      }
      
      setReport(savedReport.data);
      addToHistory(savedReport.data);
      message.success('Report saved successfully');
      
      // Update URL if this was a new report
      if (!report.reportId && savedReport.data.reportId) {
        navigate(`/reports/designer/${savedReport.data.reportId}`, { replace: true });
      }
      
    } catch (error) {
      message.error('Failed to save report');
      console.error('Error saving report:', error);
    } finally {
      setSaving(false);
    }
  };

  const addToHistory = useCallback((newState: ReportConfig) => {
    setHistory(prev => {
      const newHistory = prev.slice(0, historyIndex + 1);
      newHistory.push(newState);
      
      // Limit history size
      if (newHistory.length > 50) {
        newHistory.shift();
        return newHistory;
      }
      
      return newHistory;
    });
    setHistoryIndex(prev => prev + 1);
  }, [historyIndex]);

  const undo = () => {
    if (historyIndex > 0) {
      const prevState = history[historyIndex - 1];
      setReport(prevState);
      setComponents(prevState.components || []);
      setDataSources(prevState.dataSources || []);
      setHistoryIndex(historyIndex - 1);
    }
  };

  const redo = () => {
    if (historyIndex < history.length - 1) {
      const nextState = history[historyIndex + 1];
      setReport(nextState);
      setComponents(nextState.components || []);
      setDataSources(nextState.dataSources || []);
      setHistoryIndex(historyIndex + 1);
    }
  };

  const handleComponentAdd = (componentType: string, position: { x: number; y: number }) => {
    if (!report) return;
    
    const newComponent: Partial<ReportComponent> = {
      reportId: report.reportId,
      componentType,
      componentName: `${componentType}_${Date.now()}`,
      positionX: position.x,
      positionY: position.y,
      width: 200,
      height: 150,
      zIndex: components.length + 1,
      isVisible: true,
      isLocked: false,
      componentOrder: components.length + 1
    };
    
    setComponents(prev => [...prev, newComponent as ReportComponent]);
  };

  const handleComponentUpdate = (componentId: number, updates: Partial<ReportComponent>) => {
    setComponents(prev => 
      prev.map(comp => 
        comp.componentId === componentId 
          ? { ...comp, ...updates }
          : comp
      )
    );
  };

  const handleComponentDelete = (componentId: number) => {
    setComponents(prev => prev.filter(comp => comp.componentId !== componentId));
    if (selectedComponent?.componentId === componentId) {
      setSelectedComponent(null);
    }
  };

  const handleComponentSelect = (component: ReportComponent | null) => {
    setSelectedComponent(component);
  };

  const handleDataSourceAdd = (dataSource: Partial<ReportDataSource>) => {
    if (!report) return;
    
    const newDataSource: ReportDataSource = {
      ...dataSource,
      reportId: report.reportId,
      isActive: true,
      errorCount: 0
    } as ReportDataSource;
    
    setDataSources(prev => [...prev, newDataSource]);
  };

  const handleDataSourceUpdate = (dataSourceId: number, updates: Partial<ReportDataSource>) => {
    setDataSources(prev =>
      prev.map(ds =>
        ds.dataSourceId === dataSourceId
          ? { ...ds, ...updates }
          : ds
      )
    );
  };

  const handleDataSourceDelete = (dataSourceId: number) => {
    setDataSources(prev => prev.filter(ds => ds.dataSourceId !== dataSourceId));
    if (selectedDataSource?.dataSourceId === dataSourceId) {
      setSelectedDataSource(null);
    }
  };

  const handlePreview = () => {
    setPreviewVisible(true);
  };

  const handlePublish = async () => {
    if (!report?.reportId) return;
    
    try {
      await reportApi.publishReport(report.reportId);
      setReport(prev => prev ? { ...prev, isPublished: true } : null);
      message.success('Report published successfully');
    } catch (error) {
      message.error('Failed to publish report');
    }
  };

  const handleExport = async () => {
    if (!report?.reportId) return;
    
    try {
      const response = await reportApi.exportReport(report.reportId);
      const blob = new Blob([response.data], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${report.reportName}.json`;
      link.click();
      URL.revokeObjectURL(url);
      message.success('Report exported successfully');
    } catch (error) {
      message.error('Failed to export report');
    }
  };

  const handleDuplicate = async () => {
    if (!report?.reportId) return;
    
    try {
      const response = await reportApi.duplicateReport(
        report.reportId,
        `${report.reportName} (Copy)`,
        1 // TODO: Get from auth context
      );
      message.success('Report duplicated successfully');
      navigate(`/reports/designer/${response.data.reportId}`);
    } catch (error) {
      message.error('Failed to duplicate report');
    }
  };

  const handleSaveAs = async () => {
    if (!saveAsName.trim()) {
      message.error('Please enter a report name');
      return;
    }
    
    try {
      const reportData = {
        ...report,
        reportName: saveAsName,
        reportId: undefined, // Create new report
        isPublished: false,
        components,
        dataSources
      };
      
      const response = await reportApi.createReport({
        ...reportData,
        createdBy: 1, // TODO: Get from auth context
        updatedBy: 1
      });
      
      message.success('Report saved as new report');
      setSaveAsVisible(false);
      setSaveAsName('');
      navigate(`/reports/designer/${response.data.reportId}`);
    } catch (error) {
      message.error('Failed to save as new report');
    }
  };

  const toolbar = (
    <Space>
      <Button
        icon={<UndoOutlined />}
        disabled={historyIndex <= 0}
        onClick={undo}
        title="Undo"
      />
      <Button
        icon={<RedoOutlined />}
        disabled={historyIndex >= history.length - 1}
        onClick={redo}
        title="Redo"
      />
      
      <Button.Group>
        <Button
          icon={<ZoomOutOutlined />}
          disabled={canvasZoom <= 25}
          onClick={() => setCanvasZoom(prev => Math.max(25, prev - 25))}
        />
        <Button style={{ minWidth: '60px' }}>
          {canvasZoom}%
        </Button>
        <Button
          icon={<ZoomInOutlined />}
          disabled={canvasZoom >= 200}
          onClick={() => setCanvasZoom(prev => Math.min(200, prev + 25))}
        />
      </Button.Group>
      
      <Button
        icon={<FullscreenOutlined />}
        onClick={() => setIsFullscreen(!isFullscreen)}
        title="Toggle Fullscreen"
      />
      
      <Button
        icon={<SaveOutlined />}
        type="primary"
        loading={saving}
        onClick={saveReport}
      >
        Save
      </Button>
      
      <Button
        icon={<EyeOutlined />}
        onClick={handlePreview}
        disabled={!report?.reportId}
      >
        Preview
      </Button>
      
      <Dropdown
        overlay={
          <Menu>
            <Menu.Item
              key="saveAs"
              icon={<CopyOutlined />}
              onClick={() => setSaveAsVisible(true)}
            >
              Save As...
            </Menu.Item>
            <Menu.Item
              key="duplicate"
              icon={<CopyOutlined />}
              onClick={handleDuplicate}
              disabled={!report?.reportId}
            >
              Duplicate
            </Menu.Item>
            <Menu.Item
              key="export"
              icon={<DownloadOutlined />}
              onClick={handleExport}
              disabled={!report?.reportId}
            >
              Export
            </Menu.Item>
            <Menu.Divider />
            <Menu.Item
              key="versions"
              icon={<HistoryOutlined />}
              onClick={() => setVersionHistoryVisible(true)}
              disabled={!report?.reportId}
            >
              Version History
            </Menu.Item>
          </Menu>
        }
      >
        <Button icon={<SettingOutlined />}>
          More
        </Button>
      </Dropdown>
      
      <Button
        icon={<PlayCircleOutlined />}
        type="primary"
        onClick={handlePublish}
        disabled={!report?.reportId || report?.isPublished}
      >
        {report?.isPublished ? 'Published' : 'Publish'}
      </Button>
    </Space>
  );

  return (
    <DndProvider backend={HTML5Backend}>
      <Layout className={`report-designer ${isFullscreen ? 'fullscreen' : ''}`}>
        <Header className="designer-header">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <Breadcrumb>
                <Breadcrumb.Item>Reports</Breadcrumb.Item>
                <Breadcrumb.Item>Designer</Breadcrumb.Item>
                <Breadcrumb.Item>{report?.reportName || 'New Report'}</Breadcrumb.Item>
              </Breadcrumb>
              <Title level={4} style={{ margin: 0, marginTop: '4px' }}>
                {report?.reportName || 'Untitled Report'}
              </Title>
            </div>
            {toolbar}
          </div>
        </Header>
        
        <Layout>
          <Sider
            width={280}
            collapsed={siderCollapsed}
            onCollapse={setSiderCollapsed}
            collapsible
            className="designer-left-sider"
            theme="light"
          >
            <ComponentPanel onComponentAdd={handleComponentAdd} />
          </Sider>
          
          <Content className="designer-content">
            <DesignCanvas
              report={report}
              components={components}
              selectedComponent={selectedComponent}
              zoom={canvasZoom}
              onComponentUpdate={handleComponentUpdate}
              onComponentDelete={handleComponentDelete}
              onComponentSelect={handleComponentSelect}
              onComponentAdd={handleComponentAdd}
            />
          </Content>
          
          <Sider
            width={320}
            collapsed={rightSiderCollapsed}
            onCollapse={setRightSiderCollapsed}
            collapsible
            className="designer-right-sider"
            theme="light"
            reverseArrow
          >
            <div style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
              <div style={{ flex: 1, overflow: 'auto' }}>
                <PropertyPanel
                  selectedComponent={selectedComponent}
                  onComponentUpdate={handleComponentUpdate}
                />
              </div>
              <div style={{ flex: 1, overflow: 'auto' }}>
                <DataBindingPanel
                  selectedComponent={selectedComponent}
                  dataSources={dataSources}
                  onDataSourceAdd={handleDataSourceAdd}
                  onDataSourceUpdate={handleDataSourceUpdate}
                  onDataSourceDelete={handleDataSourceDelete}
                  onComponentUpdate={handleComponentUpdate}
                />
              </div>
            </div>
          </Sider>
        </Layout>
      </Layout>

      {/* Modals */}
      <PreviewModal
        visible={previewVisible}
        onCancel={() => setPreviewVisible(false)}
        reportId={report?.reportId}
      />

      <VersionHistoryModal
        visible={versionHistoryVisible}
        onCancel={() => setVersionHistoryVisible(false)}
        reportId={report?.reportId}
        onRestore={(versionId) => {
          // Handle version restore
          setVersionHistoryVisible(false);
        }}
      />

      <Modal
        title="Save As"
        open={saveAsVisible}
        onOk={handleSaveAs}
        onCancel={() => {
          setSaveAsVisible(false);
          setSaveAsName('');
        }}
        okText="Save"
      >
        <Input
          placeholder="Enter report name"
          value={saveAsName}
          onChange={(e) => setSaveAsName(e.target.value)}
          onPressEnter={handleSaveAs}
        />
      </Modal>
    </DndProvider>
  );
};

export default ReportDesigner;