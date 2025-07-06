import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { DndProvider } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';
import ReportDesigner from '../pages/reports/Designer';
import * as reportApi from '../services/report';
import * as dataSourceApi from '../services/dataSource';

// Mock the services
jest.mock('../services/report');
jest.mock('../services/dataSource');
const mockReportApi = reportApi as jest.Mocked<typeof reportApi>;
const mockDataSourceApi = dataSourceApi as jest.Mocked<typeof dataSourceApi>;

// Mock antd message
jest.mock('antd', () => ({
  ...jest.requireActual('antd'),
  message: {
    success: jest.fn(),
    error: jest.fn(),
    warning: jest.fn(),
  },
}));

// Mock ECharts
jest.mock('echarts', () => ({
  init: jest.fn(() => ({
    setOption: jest.fn(),
    resize: jest.fn(),
    dispose: jest.fn(),
  })),
}));

// Mock user context
jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: { id: 1, username: 'admin', role: 'ADMIN' },
    isAuthenticated: true,
  }),
}));

const renderReportDesigner = () => {
  return render(
    <DndProvider backend={HTML5Backend}>
      <ReportDesigner />
    </DndProvider>
  );
};

describe('ReportDesigner Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const mockDataSources = [
    {
      id: 1,
      name: '主数据库',
      type: 'MYSQL',
      status: 'ACTIVE',
    },
  ];

  const mockReport = {
    id: 1,
    name: '测试报表',
    description: '测试报表描述',
    dataSourceId: 1,
    canvasWidth: 800,
    canvasHeight: 600,
    componentsJson: '[]',
    status: 'ACTIVE',
  };

  test('renders report designer interface', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    renderReportDesigner();

    await waitFor(() => {
      expect(screen.getByText('报表设计器')).toBeInTheDocument();
      expect(screen.getByText('组件面板')).toBeInTheDocument();
      expect(screen.getByText('设计画布')).toBeInTheDocument();
      expect(screen.getByText('属性面板')).toBeInTheDocument();
    });
  });

  test('creates new report', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    mockReportApi.createReport.mockResolvedValue({
      data: { success: true, data: mockReport },
    });

    renderReportDesigner();

    await waitFor(() => {
      expect(screen.getByText('报表设计器')).toBeInTheDocument();
    });

    // Click new report button
    const newReportButton = screen.getByRole('button', { name: /新建报表/ });
    fireEvent.click(newReportButton);

    await waitFor(() => {
      expect(screen.getByText('新建报表')).toBeInTheDocument();
    });

    // Fill report form
    fireEvent.change(screen.getByLabelText('报表名称'), {
      target: { value: '测试报表' },
    });
    fireEvent.change(screen.getByLabelText('报表描述'), {
      target: { value: '测试报表描述' },
    });

    // Select data source
    const dataSourceSelect = screen.getByLabelText('数据源');
    fireEvent.change(dataSourceSelect, { target: { value: '1' } });

    // Submit form
    const submitButton = screen.getByRole('button', { name: '确定' });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockReportApi.createReport).toHaveBeenCalledWith({
        name: '测试报表',
        description: '测试报表描述',
        dataSourceId: 1,
        canvasWidth: 800,
        canvasHeight: 600,
        componentsJson: '[]',
      });
    });
  });

  test('adds table component to canvas', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    renderReportDesigner();

    await waitFor(() => {
      expect(screen.getByText('组件面板')).toBeInTheDocument();
    });

    // Find table component in panel
    const tableComponent = screen.getByText('表格');
    expect(tableComponent).toBeInTheDocument();

    // Get canvas area
    const canvas = screen.getByTestId('design-canvas');
    expect(canvas).toBeInTheDocument();

    // Simulate drag and drop (simplified test)
    fireEvent.click(tableComponent);

    // Verify component was added to canvas
    await waitFor(() => {
      expect(screen.getByText('表格组件')).toBeInTheDocument();
    });
  });

  test('configures component properties', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    renderReportDesigner();

    await waitFor(() => {
      expect(screen.getByText('报表设计器')).toBeInTheDocument();
    });

    // Add a component first
    const tableComponent = screen.getByText('表格');
    fireEvent.click(tableComponent);

    await waitFor(() => {
      expect(screen.getByText('表格组件')).toBeInTheDocument();
    });

    // Select the component
    const addedComponent = screen.getByText('表格组件');
    fireEvent.click(addedComponent);

    // Check properties panel
    await waitFor(() => {
      expect(screen.getByText('基础属性')).toBeInTheDocument();
      expect(screen.getByLabelText('组件名称')).toBeInTheDocument();
      expect(screen.getByLabelText('宽度')).toBeInTheDocument();
      expect(screen.getByLabelText('高度')).toBeInTheDocument();
    });

    // Update component name
    const nameInput = screen.getByLabelText('组件名称');
    fireEvent.change(nameInput, { target: { value: '患者列表' } });

    await waitFor(() => {
      expect(screen.getByDisplayValue('患者列表')).toBeInTheDocument();
    });
  });

  test('saves report configuration', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    mockReportApi.updateReport.mockResolvedValue({
      data: { success: true, data: mockReport },
    });

    renderReportDesigner();

    await waitFor(() => {
      expect(screen.getByText('报表设计器')).toBeInTheDocument();
    });

    // Add some components and configure them...
    // (simplified for this test)

    // Save report
    const saveButton = screen.getByRole('button', { name: /保存/ });
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(mockReportApi.updateReport).toHaveBeenCalled();
    });
  });

  test('previews report', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    mockReportApi.previewReport.mockResolvedValue({
      data: {
        success: true,
        data: {
          components: [],
          title: '测试报表',
        },
      },
    });

    renderReportDesigner();

    await waitFor(() => {
      expect(screen.getByText('报表设计器')).toBeInTheDocument();
    });

    // Click preview button
    const previewButton = screen.getByRole('button', { name: /预览/ });
    fireEvent.click(previewButton);

    await waitFor(() => {
      expect(mockReportApi.previewReport).toHaveBeenCalled();
      expect(screen.getByText('报表预览')).toBeInTheDocument();
    });
  });

  test('handles component drag and drop', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    renderReportDesigner();

    await waitFor(() => {
      expect(screen.getByText('组件面板')).toBeInTheDocument();
    });

    // Test chart component
    const chartComponent = screen.getByText('柱状图');
    fireEvent.click(chartComponent);

    await waitFor(() => {
      expect(screen.getByText('图表组件')).toBeInTheDocument();
    });

    // Test text component
    const textComponent = screen.getByText('文本');
    fireEvent.click(textComponent);

    await waitFor(() => {
      expect(screen.getByText('文本组件')).toBeInTheDocument();
    });
  });

  test('validates report configuration', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    renderReportDesigner();

    await waitFor(() => {
      expect(screen.getByText('报表设计器')).toBeInTheDocument();
    });

    // Try to save without required fields
    const saveButton = screen.getByRole('button', { name: /保存/ });
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(screen.getByText('请先设置报表基本信息')).toBeInTheDocument();
    });
  });

  test('handles undo and redo operations', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    renderReportDesigner();

    await waitFor(() => {
      expect(screen.getByText('报表设计器')).toBeInTheDocument();
    });

    // Add a component
    const tableComponent = screen.getByText('表格');
    fireEvent.click(tableComponent);

    await waitFor(() => {
      expect(screen.getByText('表格组件')).toBeInTheDocument();
    });

    // Undo
    const undoButton = screen.getByRole('button', { name: /撤销/ });
    fireEvent.click(undoButton);

    await waitFor(() => {
      expect(screen.queryByText('表格组件')).not.toBeInTheDocument();
    });

    // Redo
    const redoButton = screen.getByRole('button', { name: /重做/ });
    fireEvent.click(redoButton);

    await waitFor(() => {
      expect(screen.getByText('表格组件')).toBeInTheDocument();
    });
  });

  test('handles component deletion', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    renderReportDesigner();

    await waitFor(() => {
      expect(screen.getByText('组件面板')).toBeInTheDocument();
    });

    // Add a component
    const tableComponent = screen.getByText('表格');
    fireEvent.click(tableComponent);

    await waitFor(() => {
      expect(screen.getByText('表格组件')).toBeInTheDocument();
    });

    // Select and delete component
    const addedComponent = screen.getByText('表格组件');
    fireEvent.click(addedComponent);

    // Press delete key
    fireEvent.keyDown(document, { key: 'Delete' });

    await waitFor(() => {
      expect(screen.queryByText('表格组件')).not.toBeInTheDocument();
    });
  });

  test('handles component copy and paste', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    renderReportDesigner();

    await waitFor(() => {
      expect(screen.getByText('组件面板')).toBeInTheDocument();
    });

    // Add a component
    const tableComponent = screen.getByText('表格');
    fireEvent.click(tableComponent);

    await waitFor(() => {
      expect(screen.getByText('表格组件')).toBeInTheDocument();
    });

    // Select component
    const addedComponent = screen.getByText('表格组件');
    fireEvent.click(addedComponent);

    // Copy (Ctrl+C)
    fireEvent.keyDown(document, { key: 'c', ctrlKey: true });

    // Paste (Ctrl+V)
    fireEvent.keyDown(document, { key: 'v', ctrlKey: true });

    await waitFor(() => {
      const tableComponents = screen.getAllByText('表格组件');
      expect(tableComponents).toHaveLength(2);
    });
  });
});