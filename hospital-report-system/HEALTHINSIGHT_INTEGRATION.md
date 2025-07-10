# 🏥 HealthInsight 前端集成指南

## 📋 概述

本文档介绍了如何将 HealthInsight 现代化前端集成到医院报表管理系统中，替换原有的前端界面。

## 🎯 集成目标

- ✅ 保留现有后端 Spring Boot 系统
- ✅ 使用 HealthInsight 作为新的前端界面
- ✅ 实现前后端 API 集成
- ✅ 支持现有的认证和权限系统
- ✅ 保持数据库结构不变

## 🏗️ 技术架构

### 前端技术栈 (HealthInsight)
- **框架**: Next.js 15.2.4
- **UI 库**: React 19 + TypeScript
- **样式**: Tailwind CSS 4.0
- **组件库**: shadcn/ui + Radix UI
- **图标**: Lucide React
- **端口**: 3000

### 后端技术栈 (保持不变)
- **框架**: Spring Boot 3.1.0
- **数据库**: MySQL
- **认证**: JWT
- **API 文档**: Swagger/OpenAPI
- **端口**: 8080

## 📁 项目结构

```
hospital-report-system/
├── backend/                    # Spring Boot 后端 (保持不变)
├── frontend/                   # 原前端 (React + Vite + Ant Design)
├── healthinsight/              # 新前端 (Next.js + shadcn/ui)
│   ├── app/                    # Next.js 应用目录
│   ├── components/             # React 组件
│   ├── lib/                    # 工具库和服务
│   │   ├── api.ts             # API 配置和请求工具
│   │   ├── auth.ts            # 认证服务
│   │   ├── services.ts        # 数据服务
│   │   └── utils.ts           # 工具函数
│   ├── public/                # 静态资源
│   ├── package.json           # 依赖配置
│   └── next.config.ts         # Next.js 配置
├── database/                   # 数据库脚本
├── start-healthinsight.sh      # HealthInsight 启动脚本
├── start-with-healthinsight.sh # 完整系统启动脚本
└── README.md
```

## 🚀 快速启动

### 方法 1: 一键启动完整系统
```bash
# 启动后端 + HealthInsight 前端
./start-with-healthinsight.sh
```

### 方法 2: 分别启动服务

#### 1. 启动后端
```bash
cd backend
mvn spring-boot:run
```

#### 2. 启动 HealthInsight 前端
```bash
./start-healthinsight.sh
```

## 🔗 访问地址

- **HealthInsight 前端**: http://localhost:3000
- **后端 API**: http://localhost:8080/api
- **API 文档**: http://localhost:8080/api/swagger-ui.html
- **健康检查**: http://localhost:8080/api/actuator/health

## 👥 测试账户

| 角色 | 用户名 | 密码 | 权限 |
|------|------|------|------|
| 系统管理员 | admin | 123456 | 全部权限 |
| 医生 | doctor | 123456 | 查看/创建报表 |
| 护士 | nurse | 123456 | 查看报表 |
| 部门主管 | manager | 123456 | 部门管理 |

## 🔧 配置说明

### API 代理配置
HealthInsight 通过 Next.js 的 `rewrites` 功能将 API 请求代理到后端：

```typescript
// next.config.ts
async rewrites() {
  return [
    {
      source: '/api/:path*',
      destination: 'http://localhost:8080/api/:path*',
    },
  ];
}
```

### 环境变量
```bash
# .env.local (可选)
BACKEND_URL=http://localhost:8080
```

## 🔐 认证集成

### JWT Token 处理
- 登录成功后，JWT token 存储在 localStorage
- 所有 API 请求自动添加 Authorization header
- Token 过期时自动跳转到登录页面

### 权限管理
- 基于角色的访问控制 (RBAC)
- 前端组件根据用户权限显示/隐藏功能
- 后端 API 进行权限验证

## 📊 功能模块

### 1. 仪表板 (Dashboard)
- 实时数据展示
- 关键指标监控
- 图表和统计信息
- 系统状态监控

### 2. 数据源管理
- 数据库连接配置
- 连接测试
- 数据源状态监控

### 3. 报表管理
- 报表创建和编辑
- 报表执行和查看
- 报表导出功能

### 4. 用户管理
- 用户账户管理
- 角色权限分配
- 密码重置

### 5. 系统设置
- 主题切换
- 语言设置
- 系统配置

## 🌍 国际化支持

### 语言切换
- 支持中文 (zh-CN) 和英文 (en-US)
- 实时语言切换
- 设置持久化存储

### 主题支持
- 浅色主题
- 深色主题
- 跟随系统主题
- 自定义主题色

## 🔄 数据流

```
HealthInsight Frontend (Next.js)
    ↓ HTTP Requests
Next.js API Proxy
    ↓ Forward to
Spring Boot Backend
    ↓ Database Queries
MySQL Database
```

## 🛠️ 开发指南

### 添加新的 API 端点
1. 在 `lib/api.ts` 中添加端点配置
2. 在 `lib/services.ts` 中添加服务函数
3. 在组件中调用服务函数

### 添加新的页面
1. 在 `app/` 目录下创建新的页面文件
2. 在主导航中添加菜单项
3. 配置路由和权限

### 自定义组件
1. 在 `components/` 目录下创建组件
2. 使用 shadcn/ui 组件库
3. 遵循 TypeScript 类型定义

## 🐛 故障排除

### 常见问题

#### 1. 前端无法连接后端
- 检查后端是否在 8080 端口运行
- 检查 CORS 配置
- 查看浏览器控制台错误信息

#### 2. 登录失败
- 检查后端认证服务
- 验证用户账户和密码
- 查看网络请求状态

#### 3. 页面显示异常
- 检查 Node.js 版本 (需要 >= 18)
- 清除浏览器缓存
- 重新安装依赖: `npm install`

### 日志查看
```bash
# 后端日志
tail -f logs/hospital-report-system.log

# 前端开发日志
# 查看终端输出
```

## 📈 性能优化

### 前端优化
- Next.js 自动代码分割
- 图片优化
- 静态资源缓存
- 服务端渲染 (SSR)

### 后端优化
- 数据库连接池
- Redis 缓存
- API 响应压缩
- 查询优化

## 🔮 未来计划

- [ ] 移动端适配
- [ ] PWA 支持
- [ ] 实时数据推送
- [ ] 更多图表类型
- [ ] 高级报表设计器
- [ ] 数据可视化增强

## 📞 技术支持

如有问题，请查看：
1. 项目 README.md
2. API 文档: http://localhost:8080/api/swagger-ui.html
3. 开发者工具控制台
4. 后端日志文件

---

**注意**: HealthInsight 前端完全兼容现有的后端系统，可以无缝替换原有前端，同时保持所有现有功能和数据。
