import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { message } from 'antd';
import Login from '../pages/auth/Login';
import * as authApi from '../services/auth';

// Mock the auth service
jest.mock('../services/auth');
const mockAuthApi = authApi as jest.Mocked<typeof authApi>;

// Mock antd message
jest.mock('antd', () => ({
  ...jest.requireActual('antd'),
  message: {
    success: jest.fn(),
    error: jest.fn(),
  },
}));

// Mock react-router-dom
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

// Mock localStorage
const mockLocalStorage = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  clear: jest.fn(),
};
Object.defineProperty(window, 'localStorage', {
  value: mockLocalStorage,
});

describe('Login Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderLogin = () => {
    return render(
      <BrowserRouter>
        <Login />
      </BrowserRouter>
    );
  };

  test('renders login form correctly', () => {
    renderLogin();
    
    expect(screen.getByText('用户登录')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('请输入用户名')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('请输入密码')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '登录' })).toBeInTheDocument();
    expect(screen.getByText('还没有账户？')).toBeInTheDocument();
  });

  test('shows validation errors for empty fields', async () => {
    renderLogin();
    
    const loginButton = screen.getByRole('button', { name: '登录' });
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(screen.getByText('请输入用户名')).toBeInTheDocument();
      expect(screen.getByText('请输入密码')).toBeInTheDocument();
    });
  });

  test('successful login redirects to dashboard', async () => {
    const mockResponse = {
      data: {
        success: true,
        data: {
          token: 'mock-jwt-token',
          user: {
            id: 1,
            username: 'testuser',
            email: 'test@example.com',
            realName: '测试用户',
            role: 'USER',
          },
        },
      },
    };

    mockAuthApi.login.mockResolvedValue(mockResponse);

    renderLogin();

    const usernameInput = screen.getByPlaceholderText('请输入用户名');
    const passwordInput = screen.getByPlaceholderText('请输入密码');
    const loginButton = screen.getByRole('button', { name: '登录' });

    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(mockAuthApi.login).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'password123',
      });
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith('token', 'mock-jwt-token');
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith('user', JSON.stringify(mockResponse.data.data.user));
      expect(message.success).toHaveBeenCalledWith('登录成功');
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  test('failed login shows error message', async () => {
    const mockError = {
      response: {
        data: {
          success: false,
          message: '用户名或密码错误',
        },
      },
    };

    mockAuthApi.login.mockRejectedValue(mockError);

    renderLogin();

    const usernameInput = screen.getByPlaceholderText('请输入用户名');
    const passwordInput = screen.getByPlaceholderText('请输入密码');
    const loginButton = screen.getByRole('button', { name: '登录' });

    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(passwordInput, { target: { value: 'wrongpassword' } });
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(message.error).toHaveBeenCalledWith('用户名或密码错误');
      expect(mockNavigate).not.toHaveBeenCalled();
    });
  });

  test('shows loading state during login', async () => {
    let resolveLogin: (value: any) => void;
    const loginPromise = new Promise((resolve) => {
      resolveLogin = resolve;
    });

    mockAuthApi.login.mockReturnValue(loginPromise);

    renderLogin();

    const usernameInput = screen.getByPlaceholderText('请输入用户名');
    const passwordInput = screen.getByPlaceholderText('请输入密码');
    const loginButton = screen.getByRole('button', { name: '登录' });

    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(loginButton);

    // Check loading state
    expect(screen.getByRole('button', { name: '登录' })).toBeDisabled();
    expect(screen.getByText('登录中...')).toBeInTheDocument();

    // Resolve the promise
    resolveLogin!({
      data: {
        success: true,
        data: {
          token: 'mock-jwt-token',
          user: { id: 1, username: 'testuser' },
        },
      },
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: '登录' })).not.toBeDisabled();
    });
  });

  test('remember me checkbox works', () => {
    renderLogin();

    const rememberCheckbox = screen.getByLabelText('记住我');
    expect(rememberCheckbox).not.toBeChecked();

    fireEvent.click(rememberCheckbox);
    expect(rememberCheckbox).toBeChecked();
  });

  test('navigate to register page', () => {
    renderLogin();

    const registerLink = screen.getByText('立即注册');
    fireEvent.click(registerLink);

    expect(mockNavigate).toHaveBeenCalledWith('/register');
  });

  test('forgot password link works', () => {
    renderLogin();

    const forgotPasswordLink = screen.getByText('忘记密码？');
    fireEvent.click(forgotPasswordLink);

    expect(mockNavigate).toHaveBeenCalledWith('/forgot-password');
  });

  test('handles network error gracefully', async () => {
    const networkError = new Error('Network Error');
    mockAuthApi.login.mockRejectedValue(networkError);

    renderLogin();

    const usernameInput = screen.getByPlaceholderText('请输入用户名');
    const passwordInput = screen.getByPlaceholderText('请输入密码');
    const loginButton = screen.getByRole('button', { name: '登录' });

    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(message.error).toHaveBeenCalledWith('网络错误，请稍后重试');
    });
  });

  test('validates username format', async () => {
    renderLogin();

    const usernameInput = screen.getByPlaceholderText('请输入用户名');
    const passwordInput = screen.getByPlaceholderText('请输入密码');
    const loginButton = screen.getByRole('button', { name: '登录' });

    // Test with invalid username (too short)
    fireEvent.change(usernameInput, { target: { value: 'ab' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(screen.getByText('用户名至少需要3个字符')).toBeInTheDocument();
    });
  });

  test('validates password format', async () => {
    renderLogin();

    const usernameInput = screen.getByPlaceholderText('请输入用户名');
    const passwordInput = screen.getByPlaceholderText('请输入密码');
    const loginButton = screen.getByRole('button', { name: '登录' });

    // Test with invalid password (too short)
    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(passwordInput, { target: { value: '123' } });
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(screen.getByText('密码至少需要6个字符')).toBeInTheDocument();
    });
  });
});