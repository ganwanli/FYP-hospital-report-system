# 🚀 医院报表系统 - 快速启动指南

## 一键启动（推荐）

### 方法1：使用启动脚本
```bash
# 进入项目目录
cd hospital-report-system

# 给脚本执行权限
chmod +x start.sh

# 启动系统
./start.sh start
```

### 方法2：手动启动

#### 1️⃣ 准备数据库
```bash
# 登录MySQL
mysql -u root -p

# 创建数据库并初始化
CREATE DATABASE IF NOT EXISTS hospital_report_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital_report_system;
source database/init.sql;
```

#### 2️⃣ 启动后端
```bash
# 进入后端目录
cd backend

# 安装依赖并启动
mvn clean install -DskipTests
mvn spring-boot:run
```

#### 3️⃣ 启动前端
```bash
# 新开终端，进入前端目录
cd frontend

# 安装依赖并启动
npm install
npm run dev
```

## 访问系统

### 🌐 访问地址
- **前端应用**: http://localhost:5173
- **API文档**: http://localhost:8080/api/swagger-ui.html
- **健康检查**: http://localhost:8080/api/actuator/health

### 🔐 登录信息
```
用户名: admin
密码: admin123
```

## 🛠️ 快速检查

### 检查服务状态
```bash
# 检查后端服务
curl http://localhost:8080/api/actuator/health

# 检查前端服务
curl http://localhost:5173
```

### 环境要求检查
```bash
java -version    # 需要 Java 17+
node --version   # 需要 Node.js 16+
mvn --version    # 需要 Maven 3.6+
mysql --version  # 需要 MySQL 8.0+
```

## 🐛 问题排查

### 常见问题

#### 数据库连接失败
```bash
# 检查MySQL服务
brew services list | grep mysql  # macOS
systemctl status mysql           # Linux

# 检查数据库密码
# 编辑 backend/src/main/resources/application.yml
# 修改 spring.datasource.primary.password
```

#### 端口冲突
```bash
# 查看端口占用
lsof -i :8080  # 后端端口
lsof -i :5173  # 前端端口

# 停止占用进程
kill -9 <PID>
```

#### 依赖安装失败
```bash
# 清理并重新安装 - 后端
cd backend
mvn clean
mvn install -DskipTests

# 清理并重新安装 - 前端
cd frontend
rm -rf node_modules package-lock.json
npm install
```

## 🎯 开始使用

### 基本操作流程
1. **登录系统** → 使用 admin/admin123
2. **数据源管理** → 创建测试数据源
3. **报表设计** → 使用拖拽设计器
4. **报表生成** → 生成并查看报表
5. **报表导出** → 导出PDF/Excel

### 快速测试
```bash
# 测试API
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 测试前端
open http://localhost:5173
```

## 📚 更多信息

- 📖 **详细运行指南**: [RUN-PROJECT-GUIDE.md](./RUN-PROJECT-GUIDE.md)
- 🧪 **测试指南**: [tests/docs/testing-guide.md](./tests/docs/testing-guide.md)
- 🔒 **安全测试**: [tests/security/security-testing-checklist.md](./tests/security/security-testing-checklist.md)
- 📊 **测试报告**: [tests/reports/integration-test-report.md](./tests/reports/integration-test-report.md)

---

**🎉 系统已就绪！开始您的医院报表管理之旅吧！**