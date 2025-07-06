# 医院报表系统 - 测试指南

## 概述

本文档提供了医院报表系统的完整测试指南，包括测试环境搭建、测试用例执行、测试报告生成等内容。

## 快速开始

### 环境要求

#### 基础环境
- **Java**: OpenJDK 17+
- **Node.js**: 18.0+
- **Maven**: 3.8+
- **npm**: 9.0+
- **MySQL**: 8.0+

#### 测试工具
- **JUnit**: 5.9+
- **Jest**: 29.0+
- **Postman/Newman**: 最新版
- **JMeter**: 5.5+

### 快速启动测试

```bash
# 1. 克隆项目
git clone <repository-url>
cd hospital-report-system

# 2. 运行自动化测试环境搭建
chmod +x tests/scripts/setup-test-environment.sh
./tests/scripts/setup-test-environment.sh setup

# 3. 运行完整测试套件
./tests/scripts/setup-test-environment.sh test

# 4. 查看测试报告
open tests/reports/test-summary.html
```

## 详细测试指南

### 1. 后端测试

#### 1.1 单元测试

```bash
cd backend
mvn test
```

**测试覆盖的模块:**
- 用户认证服务 (`UserServiceTest`)
- 数据源管理 (`DataSourceServiceTest`)
- 报表配置服务 (`ReportConfigServiceTest`)
- SQL执行引擎 (`SqlExecutorServiceTest`)

**示例测试用例:**
```java
@Test
@DisplayName("用户登录成功")
void testLoginSuccess() {
    // Given
    LoginRequest request = new LoginRequest("admin", "password123");
    
    // When
    User user = authService.login(request);
    
    // Then
    assertThat(user).isNotNull();
    assertThat(user.getUsername()).isEqualTo("admin");
}
```

#### 1.2 集成测试

```bash
cd backend
mvn verify -Pintegration-test
```

**集成测试包括:**
- API端点测试
- 数据库交互测试
- 外部服务集成测试
- 权限验证测试

#### 1.3 API测试

使用MockMvc进行API测试:
```java
@Test
void testCreateReport() throws Exception {
    mockMvc.perform(post("/api/reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reportRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
}
```

### 2. 前端测试

#### 2.1 单元测试

```bash
cd frontend
npm test
```

**测试覆盖的组件:**
- 登录组件 (`Login.test.tsx`)
- 数据源管理 (`DataSourceManager.test.tsx`)
- 报表设计器 (`ReportDesigner.test.tsx`)
- 报表查看器 (`ReportViewer.test.tsx`)

#### 2.2 组件测试示例

```typescript
test('用户登录成功后跳转到仪表板', async () => {
  const mockResponse = {
    data: {
      success: true,
      data: { token: 'mock-token', user: { username: 'admin' } }
    }
  };
  
  mockAuthApi.login.mockResolvedValue(mockResponse);
  
  render(<Login />);
  
  fireEvent.change(screen.getByPlaceholderText('用户名'), 
    { target: { value: 'admin' } });
  fireEvent.change(screen.getByPlaceholderText('密码'), 
    { target: { value: 'password123' } });
  fireEvent.click(screen.getByRole('button', { name: '登录' }));
  
  await waitFor(() => {
    expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
  });
});
```

#### 2.3 测试覆盖率

```bash
npm run test:coverage
```

目标覆盖率:
- 行覆盖率: ≥85%
- 分支覆盖率: ≥80%
- 函数覆盖率: ≥90%

### 3. API自动化测试

#### 3.1 Postman测试

```bash
# 使用Newman运行Postman测试集合
newman run tests/postman/hospital-report-system.postman_collection.json \
       -e tests/postman/test-environment.postman_environment.json \
       --reporters html,cli \
       --reporter-html-export tests/reports/postman-report.html
```

#### 3.2 测试集合结构

```
Hospital Report System API Tests/
├── Authentication Tests/
│   ├── User Registration
│   ├── User Login
│   └── Invalid Login
├── DataSource Management Tests/
│   ├── Create DataSource
│   ├── Get DataSources
│   ├── Test Connection
│   └── Execute SQL
├── Report Management Tests/
│   ├── Create Report
│   ├── Get Reports
│   ├── Generate Report
│   └── Export Report
└── Security Tests/
    ├── Unauthorized Access
    ├── SQL Injection Test
    └── XSS Prevention Test
```

#### 3.3 环境变量配置

```json
{
  "baseUrl": "http://localhost:8080/api",
  "testUsername": "admin",
  "testPassword": "admin123",
  "authToken": "{{dynamic_token}}"
}
```

### 4. 性能测试

#### 4.1 JMeter测试执行

```bash
# 运行性能测试
jmeter -n -t tests/jmeter/hospital-report-performance-test.jmx \
       -l tests/reports/performance-results.jtl \
       -e -o tests/reports/performance-report/

# 查看性能报告
open tests/reports/performance-report/index.html
```

#### 4.2 性能测试场景

1. **认证性能测试**
   - 并发用户数: 50
   - 持续时间: 5分钟
   - 目标: 平均响应时间 < 500ms

2. **报表生成性能测试**
   - 并发用户数: 20
   - 持续时间: 10分钟
   - 目标: 95%响应时间 < 5秒

3. **数据查询性能测试**
   - 并发用户数: 30
   - 持续时间: 5分钟
   - 目标: 平均响应时间 < 2秒

#### 4.3 性能指标监控

监控指标:
- 响应时间 (平均值、中位数、95%线)
- 吞吐量 (TPS)
- 错误率
- 资源使用率 (CPU、内存、数据库连接)

### 5. 安全测试

#### 5.1 自动化安全测试

```bash
# 运行完整安全测试套件
./tests/security/run-security-tests.sh

# 查看安全测试报告
open tests/security/reports/security_report_*.html
```

#### 5.2 安全测试检查清单

使用安全测试检查清单确保测试覆盖完整:

```bash
# 查看安全测试清单
cat tests/security/security-testing-checklist.md
```

主要安全测试项目:
- [ ] SQL注入防护
- [ ] XSS防护
- [ ] CSRF防护
- [ ] 认证绕过测试
- [ ] 权限提升测试
- [ ] 会话管理测试
- [ ] 输入验证测试

#### 5.3 漏洞扫描

```bash
# OWASP依赖检查
cd backend
mvn org.owasp:dependency-check-maven:check

# npm安全审计
cd frontend
npm audit

# 代码扫描 (如果配置了SonarQube)
mvn sonar:sonar
```

### 6. 端到端测试

#### 6.1 业务流程测试

**完整报表创建流程:**
1. 用户登录
2. 创建数据源
3. 设计报表
4. 生成报表
5. 导出报表

**测试脚本示例:**
```bash
#!/bin/bash
# 端到端测试脚本

echo "开始端到端测试..."

# 1. 启动应用
./start-application.sh

# 2. 等待应用启动
wait_for_service localhost 8080

# 3. 运行测试
newman run tests/e2e/complete-workflow.postman_collection.json

# 4. 验证结果
verify_test_results

echo "端到端测试完成"
```

### 7. 测试数据管理

#### 7.1 测试数据准备

```sql
-- 加载测试数据
mysql -u test_user -p test_password hospital_test < tests/data/test-data.sql
```

#### 7.2 测试数据清理

```bash
# 清理测试数据
./tests/scripts/cleanup-test-data.sh
```

#### 7.3 数据生成器

使用TestDataGenerator生成大量测试数据:
```java
@Service
@Profile("test")
public class TestDataGenerator {
    public void generatePatients(int count) {
        // 生成指定数量的患者测试数据
    }
    
    public void generateReports(int count) {
        // 生成指定数量的报表测试数据
    }
}
```

### 8. 持续集成测试

#### 8.1 CI/CD配置

**GitHub Actions示例:**
```yaml
name: 测试流水线
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: 运行测试
        run: |
          ./tests/scripts/setup-test-environment.sh
          ./tests/scripts/run-all-tests.sh
      - name: 上传测试报告
        uses: actions/upload-artifact@v3
        with:
          name: test-reports
          path: tests/reports/
```

#### 8.2 质量门禁

设置质量门禁标准:
- 单元测试通过率: 100%
- 代码覆盖率: ≥85%
- 安全扫描: 无高危漏洞
- 性能测试: 响应时间达标

### 9. 测试报告

#### 9.1 自动化报告生成

```bash
# 生成综合测试报告
./tests/scripts/generate-test-report.sh

# 报告包含:
# - 测试执行摘要
# - 覆盖率统计
# - 性能测试结果
# - 安全测试结果
# - 缺陷统计
```

#### 9.2 报告访问

测试报告位置:
- **单元测试报告**: `tests/reports/unit-test-report.html`
- **集成测试报告**: `tests/reports/integration-test-report.html`
- **API测试报告**: `tests/reports/postman-report.html`
- **性能测试报告**: `tests/reports/performance-report/index.html`
- **安全测试报告**: `tests/security/reports/security_report_*.html`
- **综合测试报告**: `tests/reports/test-summary.html`

### 10. 故障排除

#### 10.1 常见问题

**问题1: 数据库连接失败**
```bash
# 检查MySQL服务状态
systemctl status mysql

# 检查连接配置
mysql -u test_user -p test_password -h localhost hospital_test
```

**问题2: 前端测试失败**
```bash
# 清理node_modules
rm -rf node_modules package-lock.json
npm install

# 检查Node.js版本
node --version  # 应该是18.0+
```

**问题3: 性能测试超时**
```bash
# 检查JMeter配置
grep -r "timeout" tests/jmeter/

# 调整超时设置
vim tests/jmeter/hospital-report-performance-test.jmx
```

#### 10.2 日志分析

**查看应用日志:**
```bash
tail -f tests/logs/app.log
```

**查看测试日志:**
```bash
tail -f tests/logs/test.log
```

### 11. 测试最佳实践

#### 11.1 测试用例设计

1. **AAA模式**: Arrange, Act, Assert
2. **独立性**: 每个测试用例应该独立
3. **可重复性**: 测试结果应该一致
4. **清晰性**: 测试意图应该明确

#### 11.2 测试数据管理

1. **数据隔离**: 不同测试使用不同数据
2. **数据清理**: 测试后及时清理数据
3. **数据版本**: 维护测试数据版本
4. **数据安全**: 避免使用敏感数据

#### 11.3 性能测试实践

1. **渐进式负载**: 逐步增加负载
2. **监控资源**: 监控系统资源使用
3. **基线对比**: 与历史数据对比
4. **瓶颈分析**: 识别性能瓶颈

#### 11.4 安全测试实践

1. **左移测试**: 开发阶段就开始安全测试
2. **自动化扫描**: 集成自动化安全扫描
3. **定期评估**: 定期进行安全评估
4. **培训意识**: 提高团队安全意识

## 总结

本测试指南提供了医院报表系统的完整测试方法和流程。通过遵循本指南，可以确保系统的质量和稳定性。

### 关键要点
1. **完整覆盖**: 覆盖功能、性能、安全等各个方面
2. **自动化**: 尽可能自动化测试流程
3. **持续改进**: 基于测试结果持续改进
4. **文档维护**: 及时更新测试文档

### 下一步行动
1. 设置测试环境
2. 执行测试套件
3. 分析测试结果
4. 修复发现的问题
5. 持续监控和改进

---

如需更多帮助，请参考项目文档或联系开发团队。