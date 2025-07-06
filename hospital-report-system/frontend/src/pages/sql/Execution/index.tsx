import React, { useState, useRef, useEffect } from 'react';
import { 
  Card, 
  Button, 
  Row, 
  Col, 
  Select, 
  message, 
  Spin, 
  Typography, 
  Space,
  Drawer,
  Modal,
  Tooltip,
  Badge,
  Progress,
  Divider
} from 'antd';
import { 
  PlayCircleOutlined, 
  SaveOutlined, 
  HistoryOutlined,
  SettingOutlined,
  QuestionCircleOutlined,
  ThunderboltOutlined,
  DatabaseOutlined,
  FileTextOutlined,
  BarChartOutlined
} from '@ant-design/icons';
import MonacoEditor from '@monaco-editor/react';
import ParameterPanel from '../components/ParameterPanel';
import ResultDisplay from '../components/ResultDisplay';
import PerformanceMonitor from '../components/PerformanceMonitor';
import ExecutionHistory from '../components/ExecutionHistory';
import { sqlExecutionApi, sqlTemplateApi } from '../services/sql';
import type { ExecutionResult, SqlTemplate, ExecutionRequest } from '../services/sql';

const { Title, Text } = Typography;
const { Option } = Select;

interface SqlExecutionPageProps {}

const SqlExecutionPage: React.FC<SqlExecutionPageProps> = () => {
  const [sqlContent, setSqlContent] = useState<string>('');
  const [selectedTemplate, setSelectedTemplate] = useState<SqlTemplate | null>(null);
  const [parameters, setParameters] = useState<Record<string, any>>({});
  const [databaseType, setDatabaseType] = useState<string>('MySQL');
  const [executing, setExecuting] = useState<boolean>(false);
  const [asyncTaskId, setAsyncTaskId] = useState<string | null>(null);
  const [executionResult, setExecutionResult] = useState<ExecutionResult | null>(null);
  
  // Drawer states
  const [parameterDrawerVisible, setParameterDrawerVisible] = useState<boolean>(false);
  const [historyDrawerVisible, setHistoryDrawerVisible] = useState<boolean>(false);
  const [performanceDrawerVisible, setPerformanceDrawerVisible] = useState<boolean>(false);
  
  // Template selection
  const [templates, setTemplates] = useState<SqlTemplate[]>([]);
  const [loadingTemplates, setLoadingTemplates] = useState<boolean>(false);
  
  // Performance monitoring
  const [executionStats, setExecutionStats] = useState<any>(null);
  const [asyncProgress, setAsyncProgress] = useState<number>(0);
  
  const editorRef = useRef<any>(null);
  const pollingRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    loadTemplates();
    loadExecutionStats();
  }, []);

  useEffect(() => {
    // Auto-detect parameters when SQL content changes
    if (sqlContent) {
      detectParameters();
    }
  }, [sqlContent]);

  useEffect(() => {
    // Poll async execution status
    if (asyncTaskId && pollingRef.current === null) {
      startPollingAsyncStatus();
    }
    
    return () => {
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
        pollingRef.current = null;
      }
    };
  }, [asyncTaskId]);

  const loadTemplates = async () => {
    setLoadingTemplates(true);
    try {
      const response = await sqlTemplateApi.getTemplateList({
        page: 1,
        size: 100,
        isActive: true
      });
      setTemplates(response.data.records);
    } catch (error) {
      message.error('Failed to load templates');
    } finally {
      setLoadingTemplates(false);
    }
  };

  const loadExecutionStats = async () => {
    try {
      const userId = 1; // Get from auth context
      const stats = await sqlExecutionApi.getExecutionStatistics(userId);
      setExecutionStats(stats.data);
    } catch (error) {
      console.error('Failed to load execution stats:', error);
    }
  };

  const detectParameters = async () => {
    if (!sqlContent.trim()) return;
    
    try {
      const response = await sqlTemplateApi.extractParameters(sqlContent);
      const detectedParams: Record<string, any> = {};
      
      response.data.forEach(param => {
        if (!parameters[param.parameterName]) {
          detectedParams[param.parameterName] = param.defaultValue || '';
        }
      });
      
      if (Object.keys(detectedParams).length > 0) {
        setParameters(prev => ({ ...prev, ...detectedParams }));
      }
    } catch (error) {
      console.error('Failed to detect parameters:', error);
    }
  };

  const handleTemplateSelect = (templateId: number) => {
    const template = templates.find(t => t.templateId === templateId);
    if (template) {
      setSelectedTemplate(template);
      setSqlContent(template.templateContent);
      setDatabaseType(template.databaseType);
      
      // Set default parameter values
      const defaultParams: Record<string, any> = {};
      template.parameters?.forEach(param => {
        defaultParams[param.parameterName] = param.defaultValue || '';
      });
      setParameters(defaultParams);
    }
  };

  const validateSql = async (): Promise<boolean> => {
    if (!sqlContent.trim()) {
      message.error('Please enter SQL content');
      return false;
    }

    try {
      const response = await sqlExecutionApi.validateSqlBeforeExecution(sqlContent, databaseType);
      const validation = response.data;
      
      if (!validation.valid) {
        message.error(`SQL validation failed: ${validation.errorMessage}`);
        return false;
      }
      
      if (validation.riskLevel === 'HIGH' || validation.riskLevel === 'CRITICAL') {
        const confirmed = await new Promise<boolean>(resolve => {
          Modal.confirm({
            title: 'Security Warning',
            content: `This query has a ${validation.riskLevel} security risk. Do you want to continue?`,
            onOk: () => resolve(true),
            onCancel: () => resolve(false)
          });
        });
        
        if (!confirmed) return false;
      }
      
      return true;
    } catch (error) {
      message.error('SQL validation failed');
      return false;
    }
  };

  const executeQuery = async (async = false) => {
    if (executing) return;
    
    const isValid = await validateSql();
    if (!isValid) return;

    setExecuting(true);
    setExecutionResult(null);
    setAsyncTaskId(null);
    setAsyncProgress(0);

    try {
      const request: ExecutionRequest = {
        templateId: selectedTemplate?.templateId,
        sqlContent: selectedTemplate ? undefined : sqlContent,
        parameters,
        databaseType,
        userId: 1 // Get from auth context
      };

      if (async) {
        const response = await sqlExecutionApi.executeQueryAsync(request);
        setAsyncTaskId(response.data);
        message.info('Async execution started. You will be notified when it completes.');
      } else {
        const response = await sqlExecutionApi.executeQuery(request);
        setExecutionResult(response.data);
        
        if (response.data.success) {
          message.success(`Query executed successfully in ${response.data.executionTime}ms`);
        } else {
          message.error(`Query failed: ${response.data.errorMessage}`);
        }
      }
      
      // Update usage count if using template
      if (selectedTemplate) {
        await sqlTemplateApi.updateUsageCount(selectedTemplate.templateId);
      }
      
      // Refresh stats
      loadExecutionStats();
      
    } catch (error: any) {
      message.error(`Execution failed: ${error.message}`);
    } finally {
      if (!async) {
        setExecuting(false);
      }
    }
  };

  const startPollingAsyncStatus = () => {
    pollingRef.current = setInterval(async () => {
      if (!asyncTaskId) return;
      
      try {
        const statusResponse = await sqlExecutionApi.getAsyncExecutionStatus(asyncTaskId);
        const status = statusResponse.data;
        
        switch (status.status) {
          case 'COMPLETED':
            const resultResponse = await sqlExecutionApi.getAsyncExecutionResult(asyncTaskId);
            setExecutionResult(resultResponse.data);
            setExecuting(false);
            setAsyncTaskId(null);
            setAsyncProgress(100);
            
            if (pollingRef.current) {
              clearInterval(pollingRef.current);
              pollingRef.current = null;
            }
            
            message.success('Async execution completed');
            break;
            
          case 'FAILED':
          case 'CANCELLED':
            setExecuting(false);
            setAsyncTaskId(null);
            
            if (pollingRef.current) {
              clearInterval(pollingRef.current);
              pollingRef.current = null;
            }
            
            message.error(`Async execution ${status.status.toLowerCase()}`);
            break;
            
          case 'RUNNING':
            // Simulate progress (in real implementation, this would come from backend)
            setAsyncProgress(prev => Math.min(prev + 5, 90));
            break;
        }
      } catch (error) {
        console.error('Failed to poll async status:', error);
      }
    }, 1000);
  };

  const cancelAsyncExecution = async () => {
    if (!asyncTaskId) return;
    
    try {
      await sqlExecutionApi.cancelAsyncExecution(asyncTaskId);
      setExecuting(false);
      setAsyncTaskId(null);
      setAsyncProgress(0);
      
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
        pollingRef.current = null;
      }
      
      message.info('Async execution cancelled');
    } catch (error) {
      message.error('Failed to cancel async execution');
    }
  };

  const explainQuery = async () => {
    if (!sqlContent.trim()) {
      message.error('Please enter SQL content');
      return;
    }

    try {
      const response = await sqlExecutionApi.explainQuery(sqlContent, databaseType);
      Modal.info({
        title: 'Query Execution Plan',
        content: (
          <pre style={{ maxHeight: '400px', overflow: 'auto' }}>
            {JSON.stringify(response.data, null, 2)}
          </pre>
        ),
        width: 600
      });
    } catch (error) {
      message.error('Failed to explain query');
    }
  };

  const saveAsTemplate = () => {
    if (!sqlContent.trim()) {
      message.error('Please enter SQL content');
      return;
    }

    Modal.confirm({
      title: 'Save as Template',
      content: 'Do you want to save this SQL as a template?',
      onOk: () => {
        // Navigate to template creation page with pre-filled content
        // This would typically use React Router
        window.location.href = `/sql-templates/create?content=${encodeURIComponent(sqlContent)}`;
      }
    });
  };

  return (
    <div style={{ padding: '24px' }}>
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card>
            <Row justify="space-between" align="middle" style={{ marginBottom: '16px' }}>
              <Col>
                <Title level={3} style={{ margin: 0 }}>
                  <DatabaseOutlined style={{ marginRight: '8px' }} />
                  SQL Execution Console
                </Title>
              </Col>
              <Col>
                <Space>
                  {executionStats && (
                    <Badge count={executionStats.totalExecutions} showZero>
                      <Button 
                        icon={<BarChartOutlined />}
                        onClick={() => setPerformanceDrawerVisible(true)}
                      >
                        Performance
                      </Button>
                    </Badge>
                  )}
                  <Button 
                    icon={<HistoryOutlined />}
                    onClick={() => setHistoryDrawerVisible(true)}
                  >
                    History
                  </Button>
                  <Button 
                    icon={<SettingOutlined />}
                    onClick={() => setParameterDrawerVisible(true)}
                  >
                    Parameters
                  </Button>
                </Space>
              </Col>
            </Row>

            <Row gutter={[16, 16]} style={{ marginBottom: '16px' }}>
              <Col span={12}>
                <Space>
                  <Text strong>Template:</Text>
                  <Select
                    style={{ width: 300 }}
                    placeholder="Select a template (optional)"
                    allowClear
                    loading={loadingTemplates}
                    onChange={handleTemplateSelect}
                    value={selectedTemplate?.templateId}
                  >
                    {templates.map(template => (
                      <Option key={template.templateId} value={template.templateId}>
                        {template.templateName}
                      </Option>
                    ))}
                  </Select>
                </Space>
              </Col>
              <Col span={12}>
                <Space>
                  <Text strong>Database:</Text>
                  <Select
                    style={{ width: 150 }}
                    value={databaseType}
                    onChange={setDatabaseType}
                  >
                    <Option value="MySQL">MySQL</Option>
                    <Option value="PostgreSQL">PostgreSQL</Option>
                    <Option value="Oracle">Oracle</Option>
                    <Option value="SQL Server">SQL Server</Option>
                  </Select>
                </Space>
              </Col>
            </Row>

            <div style={{ border: '1px solid #d9d9d9', borderRadius: '6px', marginBottom: '16px' }}>
              <MonacoEditor
                height="300px"
                language="sql"
                value={sqlContent}
                onChange={(value) => setSqlContent(value || '')}
                onMount={(editor) => {
                  editorRef.current = editor;
                }}
                options={{
                  minimap: { enabled: false },
                  fontSize: 14,
                  lineNumbers: 'on',
                  roundedSelection: false,
                  scrollBeyondLastLine: false,
                  automaticLayout: true,
                  theme: 'vs-light'
                }}
              />
            </div>

            <Row justify="space-between" align="middle">
              <Col>
                <Space>
                  <Button
                    type="primary"
                    icon={<PlayCircleOutlined />}
                    loading={executing && !asyncTaskId}
                    onClick={() => executeQuery(false)}
                    disabled={!sqlContent.trim()}
                  >
                    Execute
                  </Button>
                  <Button
                    icon={<ThunderboltOutlined />}
                    loading={executing && !!asyncTaskId}
                    onClick={() => executeQuery(true)}
                    disabled={!sqlContent.trim()}
                  >
                    Execute Async
                  </Button>
                  {asyncTaskId && (
                    <Button
                      danger
                      onClick={cancelAsyncExecution}
                    >
                      Cancel
                    </Button>
                  )}
                  <Button
                    icon={<QuestionCircleOutlined />}
                    onClick={explainQuery}
                    disabled={!sqlContent.trim()}
                  >
                    Explain
                  </Button>
                  <Button
                    icon={<SaveOutlined />}
                    onClick={saveAsTemplate}
                    disabled={!sqlContent.trim()}
                  >
                    Save as Template
                  </Button>
                </Space>
              </Col>
              <Col>
                {asyncTaskId && (
                  <Space>
                    <Text>Async Progress:</Text>
                    <Progress 
                      percent={asyncProgress} 
                      size="small" 
                      style={{ width: '100px' }}
                    />
                  </Space>
                )}
              </Col>
            </Row>
          </Card>
        </Col>

        {executionResult && (
          <Col span={24}>
            <ResultDisplay 
              result={executionResult}
              onExport={(format) => {
                // Handle export
                message.info(`Exporting to ${format}...`);
              }}
            />
          </Col>
        )}
      </Row>

      {/* Parameter Drawer */}
      <Drawer
        title="Query Parameters"
        placement="right"
        onClose={() => setParameterDrawerVisible(false)}
        open={parameterDrawerVisible}
        width={400}
      >
        <ParameterPanel
          parameters={selectedTemplate?.parameters || []}
          values={parameters}
          onChange={setParameters}
        />
      </Drawer>

      {/* History Drawer */}
      <Drawer
        title="Execution History"
        placement="right"
        onClose={() => setHistoryDrawerVisible(false)}
        open={historyDrawerVisible}
        width={600}
      >
        <ExecutionHistory
          userId={1} // Get from auth context
          onSelectExecution={(execution) => {
            setSqlContent(execution.sqlContent);
            setParameters(execution.parameters || {});
            setHistoryDrawerVisible(false);
          }}
        />
      </Drawer>

      {/* Performance Drawer */}
      <Drawer
        title="Performance Monitoring"
        placement="right"
        onClose={() => setPerformanceDrawerVisible(false)}
        open={performanceDrawerVisible}
        width={800}
      >
        <PerformanceMonitor userId={1} />
      </Drawer>
    </div>
  );
};

export default SqlExecutionPage;