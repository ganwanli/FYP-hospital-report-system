import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { message } from 'antd';
import DataSourceManager from '../pages/datasources/DataSourceManager';
import * as dataSourceApi from '../services/dataSource';

// Mock the dataSource service
jest.mock('../services/dataSource');
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

// Mock user context
const mockUser = {
  id: 1,
  username: 'admin',
  role: 'ADMIN',
};

jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: mockUser,
    isAuthenticated: true,
  }),
}));

describe('DataSourceManager Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const mockDataSources = [
    {
      id: 1,
      name: '主数据库',
      type: 'MYSQL',
      host: 'localhost',
      port: 3306,
      database: 'hospital_db',
      username: 'root',
      status: 'ACTIVE',
      createdAt: '2023-01-01T00:00:00',
    },
    {
      id: 2,
      name: '备份数据库',
      type: 'POSTGRESQL',
      host: 'backup.example.com',
      port: 5432,
      database: 'backup_db',
      username: 'backup_user',
      status: 'INACTIVE',
      createdAt: '2023-01-02T00:00:00',
    },
  ];

  const renderDataSourceManager = () => {
    return render(
      <BrowserRouter>
        <DataSourceManager />
      </BrowserRouter>
    );
  };

  test('renders data source list correctly', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    renderDataSourceManager();

    await waitFor(() => {
      expect(screen.getByText('数据源管理')).toBeInTheDocument();
      expect(screen.getByText('主数据库')).toBeInTheDocument();
      expect(screen.getByText('备份数据库')).toBeInTheDocument();
      expect(screen.getByText('MYSQL')).toBeInTheDocument();
      expect(screen.getByText('POSTGRESQL')).toBeInTheDocument();
    });

    expect(mockDataSourceApi.getDataSources).toHaveBeenCalledTimes(1);
  });

  test('opens create data source modal', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    renderDataSourceManager();

    await waitFor(() => {
      expect(screen.getByText('数据源管理')).toBeInTheDocument();
    });

    const addButton = screen.getByRole('button', { name: /新增数据源/ });
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(screen.getByText('新增数据源')).toBeInTheDocument();
      expect(screen.getByLabelText('数据源名称')).toBeInTheDocument();
      expect(screen.getByLabelText('数据库类型')).toBeInTheDocument();
    });
  });

  test('creates new data source successfully', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    const newDataSource = {
      id: 3,
      name: '新数据源',
      type: 'MYSQL',
      host: 'new.example.com',
      port: 3306,
      database: 'new_db',
      username: 'new_user',
      status: 'ACTIVE',
    };

    mockDataSourceApi.createDataSource.mockResolvedValue({
      data: { success: true, data: newDataSource },
    });

    renderDataSourceManager();

    await waitFor(() => {
      expect(screen.getByText('数据源管理')).toBeInTheDocument();
    });

    const addButton = screen.getByRole('button', { name: /新增数据源/ });
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(screen.getByText('新增数据源')).toBeInTheDocument();
    });

    // Fill form
    fireEvent.change(screen.getByLabelText('数据源名称'), {
      target: { value: '新数据源' },
    });
    fireEvent.change(screen.getByLabelText('主机地址'), {
      target: { value: 'new.example.com' },
    });
    fireEvent.change(screen.getByLabelText('端口'), {
      target: { value: '3306' },
    });
    fireEvent.change(screen.getByLabelText('数据库名'), {
      target: { value: 'new_db' },
    });
    fireEvent.change(screen.getByLabelText('用户名'), {
      target: { value: 'new_user' },
    });
    fireEvent.change(screen.getByLabelText('密码'), {
      target: { value: 'password123' },
    });

    // Submit form
    const submitButton = screen.getByRole('button', { name: '确定' });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockDataSourceApi.createDataSource).toHaveBeenCalledWith({
        name: '新数据源',
        type: 'MYSQL',
        host: 'new.example.com',
        port: 3306,
        database: 'new_db',
        username: 'new_user',
        password: 'password123',
      });
      expect(message.success).toHaveBeenCalledWith('数据源创建成功');
    });
  });

  test('tests data source connection', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    mockDataSourceApi.testConnection.mockResolvedValue({
      data: { success: true, data: true },
    });

    renderDataSourceManager();

    await waitFor(() => {
      expect(screen.getByText('主数据库')).toBeInTheDocument();
    });

    const testButtons = screen.getAllByText('测试连接');
    fireEvent.click(testButtons[0]);

    await waitFor(() => {
      expect(mockDataSourceApi.testConnection).toHaveBeenCalledWith(1);
      expect(message.success).toHaveBeenCalledWith('连接测试成功');
    });
  });

  test('handles connection test failure', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    mockDataSourceApi.testConnection.mockResolvedValue({
      data: { success: true, data: false },
    });

    renderDataSourceManager();

    await waitFor(() => {
      expect(screen.getByText('主数据库')).toBeInTheDocument();
    });

    const testButtons = screen.getAllByText('测试连接');
    fireEvent.click(testButtons[0]);

    await waitFor(() => {
      expect(message.error).toHaveBeenCalledWith('连接测试失败');
    });
  });

  test('deletes data source with confirmation', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    mockDataSourceApi.deleteDataSource.mockResolvedValue({
      data: { success: true },
    });

    // Mock window.confirm
    window.confirm = jest.fn(() => true);

    renderDataSourceManager();

    await waitFor(() => {
      expect(screen.getByText('主数据库')).toBeInTheDocument();
    });

    const deleteButtons = screen.getAllByText('删除');
    fireEvent.click(deleteButtons[0]);

    await waitFor(() => {
      expect(window.confirm).toHaveBeenCalledWith('确定要删除数据源"主数据库"吗？');
      expect(mockDataSourceApi.deleteDataSource).toHaveBeenCalledWith(1);
      expect(message.success).toHaveBeenCalledWith('数据源删除成功');
    });
  });

  test('cancels delete when user clicks cancel', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    // Mock window.confirm to return false
    window.confirm = jest.fn(() => false);

    renderDataSourceManager();

    await waitFor(() => {
      expect(screen.getByText('主数据库')).toBeInTheDocument();
    });

    const deleteButtons = screen.getAllByText('删除');
    fireEvent.click(deleteButtons[0]);

    expect(window.confirm).toHaveBeenCalled();
    expect(mockDataSourceApi.deleteDataSource).not.toHaveBeenCalled();
  });

  test('edits existing data source', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    const updatedDataSource = {
      ...mockDataSources[0],
      name: '更新的数据源',
    };

    mockDataSourceApi.updateDataSource.mockResolvedValue({
      data: { success: true, data: updatedDataSource },
    });

    renderDataSourceManager();

    await waitFor(() => {
      expect(screen.getByText('主数据库')).toBeInTheDocument();
    });

    const editButtons = screen.getAllByText('编辑');
    fireEvent.click(editButtons[0]);

    await waitFor(() => {
      expect(screen.getByText('编辑数据源')).toBeInTheDocument();
    });

    // Update name
    const nameInput = screen.getByDisplayValue('主数据库');
    fireEvent.change(nameInput, { target: { value: '更新的数据源' } });

    // Submit form
    const submitButton = screen.getByRole('button', { name: '确定' });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockDataSourceApi.updateDataSource).toHaveBeenCalledWith(1, expect.objectContaining({
        name: '更新的数据源',
      }));
      expect(message.success).toHaveBeenCalledWith('数据源更新成功');
    });
  });

  test('handles form validation errors', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    renderDataSourceManager();

    await waitFor(() => {
      expect(screen.getByText('数据源管理')).toBeInTheDocument();
    });

    const addButton = screen.getByRole('button', { name: /新增数据源/ });
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(screen.getByText('新增数据源')).toBeInTheDocument();
    });

    // Try to submit empty form
    const submitButton = screen.getByRole('button', { name: '确定' });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('请输入数据源名称')).toBeInTheDocument();
      expect(screen.getByText('请输入主机地址')).toBeInTheDocument();
    });
  });

  test('filters data sources by type', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    renderDataSourceManager();

    await waitFor(() => {
      expect(screen.getByText('主数据库')).toBeInTheDocument();
      expect(screen.getByText('备份数据库')).toBeInTheDocument();
    });

    // Filter by MYSQL
    const typeFilter = screen.getByRole('combobox');
    fireEvent.change(typeFilter, { target: { value: 'MYSQL' } });

    await waitFor(() => {
      expect(screen.getByText('主数据库')).toBeInTheDocument();
      expect(screen.queryByText('备份数据库')).not.toBeInTheDocument();
    });
  });

  test('searches data sources by name', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    renderDataSourceManager();

    await waitFor(() => {
      expect(screen.getByText('主数据库')).toBeInTheDocument();
      expect(screen.getByText('备份数据库')).toBeInTheDocument();
    });

    // Search for "主"
    const searchInput = screen.getByPlaceholderText('搜索数据源名称');
    fireEvent.change(searchInput, { target: { value: '主' } });

    await waitFor(() => {
      expect(screen.getByText('主数据库')).toBeInTheDocument();
      expect(screen.queryByText('备份数据库')).not.toBeInTheDocument();
    });
  });

  test('handles API error gracefully', async () => {
    mockDataSourceApi.getDataSources.mockRejectedValue(new Error('API Error'));

    renderDataSourceManager();

    await waitFor(() => {
      expect(message.error).toHaveBeenCalledWith('加载数据源列表失败');
    });
  });

  test('refreshes data source list', async () => {
    mockDataSourceApi.getDataSources.mockResolvedValue({
      data: { success: true, data: mockDataSources },
    });

    renderDataSourceManager();

    await waitFor(() => {
      expect(screen.getByText('主数据库')).toBeInTheDocument();
    });

    const refreshButton = screen.getByRole('button', { name: /刷新/ });
    fireEvent.click(refreshButton);

    expect(mockDataSourceApi.getDataSources).toHaveBeenCalledTimes(2);
  });
});