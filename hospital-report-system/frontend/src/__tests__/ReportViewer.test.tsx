import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import ReportViewer from '../pages/reports/Viewer';
import * as reportViewApi from '../services/reportView';

// Mock the reportView service
jest.mock('../services/reportView');
const mockReportViewApi = reportViewApi as jest.Mocked<typeof reportViewApi>;

// Mock antd message
jest.mock('antd', () => ({
  ...jest.requireActual('antd'),
  message: {
    success: jest.fn(),
    error: jest.fn(),
    info: jest.fn(),
  },
}));

// Mock react-router-dom params
const mockParams = { reportId: '1' };
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useParams: () => mockParams,
  useNavigate: () => jest.fn(),
}));

// Mock clipboard API
Object.assign(navigator, {
  clipboard: {
    writeText: jest.fn(),
  },
});

describe('ReportViewer Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const mockParameterDefinitions = [
    {
      name: 'startDate',
      type: 'DATE',
      label: '开始日期',
      required: true,
      defaultValue: '2023-01-01',
    },
    {
      name: 'endDate',
      type: 'DATE',
      label: '结束日期',
      required: true,
      defaultValue: '2023-12-31',
    },
    {
      name: 'department',
      type: 'SELECT',
      label: '科室',
      required: false,
      options: ['内科', '外科', '儿科'],
    },
  ];

  const mockReportData = {
    reportId: 1,
    reportName: '患者统计报表',
    reportDescription: '医院患者统计分析',
    canvasWidth: 800,
    canvasHeight: 600,
    components: [
      {
        componentId: 'table1',
        componentName: '患者列表',
        componentType: 'table',
        position: { x: 50, y: 50, width: 700, height: 300 },
        data: {
          columns: [
            { title: '姓名', dataIndex: 'name' },
            { title: '年龄', dataIndex: 'age' },
            { title: '性别', dataIndex: 'gender' },
          ],
          records: [
            { name: '张三', age: 30, gender: '男' },
            { name: '李四', age: 25, gender: '女' },
          ],
        },
        isVisible: true,
        zIndex: 1,
      },
    ],
    generatedAt: '2023-01-01T10:00:00',
    generationTime: 500,
    fromCache: false,
    componentCount: 1,
  };

  const renderReportViewer = () => {
    return render(
      <BrowserRouter>
        <ReportViewer />
      </BrowserRouter>
    );
  };

  test('renders report viewer interface', async () => {
    mockReportViewApi.getParameterDefinitions.mockResolvedValue({
      data: { success: true, data: mockParameterDefinitions },
    });

    mockReportViewApi.generateReport.mockResolvedValue({
      data: { success: true, data: mockReportData },
    });

    renderReportViewer();

    await waitFor(() => {
      expect(screen.getByText('报表')).toBeInTheDocument();
      expect(screen.getByText('查看')).toBeInTheDocument();
      expect(screen.getByText('患者统计报表')).toBeInTheDocument();
    });
  });

  test('loads and displays parameter definitions', async () => {
    mockReportViewApi.getParameterDefinitions.mockResolvedValue({
      data: { success: true, data: mockParameterDefinitions },
    });

    renderReportViewer();

    await waitFor(() => {
      expect(screen.getByLabelText('开始日期')).toBeInTheDocument();
      expect(screen.getByLabelText('结束日期')).toBeInTheDocument();
      expect(screen.getByLabelText('科室')).toBeInTheDocument();
    });

    expect(mockReportViewApi.getParameterDefinitions).toHaveBeenCalledWith(1);
  });

  test('generates report with parameters', async () => {
    mockReportViewApi.getParameterDefinitions.mockResolvedValue({
      data: { success: true, data: mockParameterDefinitions },
    });

    mockReportViewApi.estimateGenerationTime.mockResolvedValue({
      data: { success: true, data: 2000 },
    });

    mockReportViewApi.generateReport.mockResolvedValue({
      data: { success: true, data: mockReportData },
    });

    renderReportViewer();

    await waitFor(() => {
      expect(screen.getByLabelText('开始日期')).toBeInTheDocument();
    });

    // Fill parameters
    fireEvent.change(screen.getByLabelText('开始日期'), {
      target: { value: '2023-01-01' },
    });
    fireEvent.change(screen.getByLabelText('结束日期'), {
      target: { value: '2023-12-31' },
    });

    // Generate report
    const generateButton = screen.getByRole('button', { name: /生成/ });
    fireEvent.click(generateButton);

    await waitFor(() => {
      expect(mockReportViewApi.generateReport).toHaveBeenCalledWith(1, {
        startDate: '2023-01-01',
        endDate: '2023-12-31',
        department: undefined,
      });
      expect(screen.getByText('患者列表')).toBeInTheDocument();
      expect(screen.getByText('张三')).toBeInTheDocument();
      expect(screen.getByText('李四')).toBeInTheDocument();
    });
  });

  test('refreshes report', async () => {
    mockReportViewApi.getParameterDefinitions.mockResolvedValue({
      data: { success: true, data: [] },
    });

    mockReportViewApi.generateReport.mockResolvedValue({
      data: { success: true, data: mockReportData },
    });

    renderReportViewer();

    await waitFor(() => {
      expect(screen.getByText('患者统计报表')).toBeInTheDocument();
    });

    // Click refresh button
    const refreshButton = screen.getByRole('button', { name: /刷新/ });
    fireEvent.click(refreshButton);

    await waitFor(() => {
      expect(mockReportViewApi.generateReport).toHaveBeenCalledTimes(2);
    });
  });

  test('switches between desktop and mobile view', async () => {
    mockReportViewApi.getParameterDefinitions.mockResolvedValue({
      data: { success: true, data: [] },
    });

    mockReportViewApi.generateReport.mockResolvedValue({
      data: { success: true, data: mockReportData },
    });

    renderReportViewer();

    await waitFor(() => {
      expect(screen.getByText('患者统计报表')).toBeInTheDocument();
    });

    // Switch to mobile view
    const mobileButton = screen.getByTitle('切换到移动端视图');
    fireEvent.click(mobileButton);

    await waitFor(() => {
      expect(screen.getByText('患者列表')).toBeInTheDocument();
    });

    // Switch back to desktop view
    const desktopButton = screen.getByTitle('切换到桌面端视图');
    fireEvent.click(desktopButton);

    await waitFor(() => {
      expect(screen.getByText('患者列表')).toBeInTheDocument();
    });
  });

  test('exports report to PDF', async () => {
    mockReportViewApi.getParameterDefinitions.mockResolvedValue({
      data: { success: true, data: [] },
    });

    mockReportViewApi.generateReport.mockResolvedValue({
      data: { success: true, data: mockReportData },
    });

    const mockPdfBlob = new Blob(['PDF content'], { type: 'application/pdf' });
    mockReportViewApi.exportToPdf.mockResolvedValue({
      data: mockPdfBlob,
    });

    // Mock URL.createObjectURL
    global.URL.createObjectURL = jest.fn(() => 'blob:url');
    global.URL.revokeObjectURL = jest.fn();

    renderReportViewer();

    await waitFor(() => {
      expect(screen.getByText('患者统计报表')).toBeInTheDocument();
    });

    // Open export modal
    const exportButton = screen.getByRole('button', { name: /导出/ });
    fireEvent.click(exportButton);

    await waitFor(() => {
      expect(screen.getByText('导出选项')).toBeInTheDocument();
    });

    // Select PDF format and export
    const pdfCard = screen.getByText('PDF文档');
    fireEvent.click(pdfCard);

    const exportConfirmButton = screen.getByRole('button', { name: /导出 PDF/ });
    fireEvent.click(exportConfirmButton);

    await waitFor(() => {
      expect(mockReportViewApi.exportToPdf).toHaveBeenCalledWith(
        1,
        expect.any(Object),
        expect.any(Object)
      );
    });
  });

  test('shares report', async () => {
    mockReportViewApi.getParameterDefinitions.mockResolvedValue({
      data: { success: true, data: [] },
    });

    mockReportViewApi.generateReport.mockResolvedValue({
      data: { success: true, data: mockReportData },
    });

    const mockShareResponse = {
      data: { success: true, data: { shareCode: 'abc123' } },
    };

    const mockUrlResponse = {
      data: { success: true, data: 'https://example.com/share/abc123' },
    };

    mockReportViewApi.createShare.mockResolvedValue(mockShareResponse);
    mockReportViewApi.generateShareUrl.mockResolvedValue(mockUrlResponse);

    renderReportViewer();

    await waitFor(() => {
      expect(screen.getByText('患者统计报表')).toBeInTheDocument();
    });

    // Open share modal
    const shareButton = screen.getByRole('button', { name: /分享/ });
    fireEvent.click(shareButton);

    await waitFor(() => {
      expect(screen.getByText('分享报表')).toBeInTheDocument();
    });

    // Fill share form
    fireEvent.change(screen.getByLabelText('分享标题'), {
      target: { value: '患者统计报表分享' },
    });

    // Create share
    const createShareButton = screen.getByRole('button', { name: /创建分享/ });
    fireEvent.click(createShareButton);

    await waitFor(() => {
      expect(mockReportViewApi.createShare).toHaveBeenCalled();
      expect(mockReportViewApi.generateShareUrl).toHaveBeenCalledWith('abc123');
      expect(navigator.clipboard.writeText).toHaveBeenCalledWith('https://example.com/share/abc123');
    });
  });

  test('prints report', async () => {
    mockReportViewApi.getParameterDefinitions.mockResolvedValue({
      data: { success: true, data: [] },
    });

    mockReportViewApi.generateReport.mockResolvedValue({
      data: { success: true, data: mockReportData },
    });

    mockReportViewApi.renderToPrint.mockResolvedValue({
      data: { success: true, data: '<html>Print content</html>' },
    });

    // Mock window.open
    const mockPrintWindow = {
      document: {
        write: jest.fn(),
        close: jest.fn(),
      },
      focus: jest.fn(),
      print: jest.fn(),
    };
    global.window.open = jest.fn(() => mockPrintWindow as any);

    renderReportViewer();

    await waitFor(() => {
      expect(screen.getByText('患者统计报表')).toBeInTheDocument();
    });

    // Click print button
    const printButton = screen.getByRole('button', { name: /打印/ });
    fireEvent.click(printButton);

    await waitFor(() => {
      expect(mockReportViewApi.renderToPrint).toHaveBeenCalledWith(1, expect.any(Object));
      expect(window.open).toHaveBeenCalledWith('', '_blank');
      expect(mockPrintWindow.document.write).toHaveBeenCalledWith('<html>Print content</html>');
      expect(mockPrintWindow.print).toHaveBeenCalled();
    });
  });

  test('toggles fullscreen mode', async () => {
    mockReportViewApi.getParameterDefinitions.mockResolvedValue({
      data: { success: true, data: [] },
    });

    mockReportViewApi.generateReport.mockResolvedValue({
      data: { success: true, data: mockReportData },
    });

    renderReportViewer();

    await waitFor(() => {
      expect(screen.getByText('患者统计报表')).toBeInTheDocument();
    });

    // Enter fullscreen
    const fullscreenButton = screen.getByTitle('全屏显示');
    fireEvent.click(fullscreenButton);

    await waitFor(() => {
      const viewer = document.querySelector('.report-viewer');
      expect(viewer).toHaveClass('fullscreen');
    });

    // Exit fullscreen
    const exitFullscreenButton = screen.getByTitle('退出全屏');
    fireEvent.click(exitFullscreenButton);

    await waitFor(() => {
      const viewer = document.querySelector('.report-viewer');
      expect(viewer).not.toHaveClass('fullscreen');
    });
  });

  test('handles auto refresh', async () => {
    jest.useFakeTimers();

    mockReportViewApi.getParameterDefinitions.mockResolvedValue({
      data: { success: true, data: [] },
    });

    mockReportViewApi.generateReport.mockResolvedValue({
      data: { success: true, data: mockReportData },
    });

    renderReportViewer();

    await waitFor(() => {
      expect(screen.getByText('患者统计报表')).toBeInTheDocument();
    });

    // Enable auto refresh
    const settingsButton = screen.getByRole('button', { name: /settings/ });
    fireEvent.click(settingsButton);

    const autoRefreshItem = screen.getByText(/自动刷新/);
    fireEvent.click(autoRefreshItem);

    // Fast forward time
    jest.advanceTimersByTime(30000); // 30 seconds

    await waitFor(() => {
      expect(mockReportViewApi.generateReport).toHaveBeenCalledTimes(2);
    });

    jest.useRealTimers();
  });

  test('handles parameter validation errors', async () => {
    mockReportViewApi.getParameterDefinitions.mockResolvedValue({
      data: { success: true, data: mockParameterDefinitions },
    });

    renderReportViewer();

    await waitFor(() => {
      expect(screen.getByLabelText('开始日期')).toBeInTheDocument();
    });

    // Try to generate without required parameters
    const generateButton = screen.getByRole('button', { name: /生成/ });
    fireEvent.click(generateButton);

    await waitFor(() => {
      expect(screen.getByText('请输入开始日期')).toBeInTheDocument();
      expect(screen.getByText('请输入结束日期')).toBeInTheDocument();
    });
  });

  test('handles report generation errors', async () => {
    mockReportViewApi.getParameterDefinitions.mockResolvedValue({
      data: { success: true, data: [] },
    });

    mockReportViewApi.generateReport.mockRejectedValue({
      response: {
        data: {
          success: false,
          message: '数据源连接失败',
        },
      },
    });

    renderReportViewer();

    await waitFor(() => {
      expect(screen.getByText('报表生成失败')).toBeInTheDocument();
      expect(screen.getByText('数据源连接失败')).toBeInTheDocument();
    });
  });
});