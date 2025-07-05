# 医院报表管理系统

一个基于Spring Boot + React的现代化医院报表管理系统。

## 技术栈

### 后端
- **Spring Boot 3.1.0** - 核心框架
- **MyBatis Plus 3.5.3** - ORM框架
- **Spring Security 6.x** - 安全框架
- **JWT** - 身份认证
- **Redis** - 缓存
- **MySQL 8.0+** - 数据库
- **HikariCP** - 连接池

### 前端
- **React 18.2.x** - 前端框架
- **TypeScript 5.x** - 类型系统
- **Ant Design 5.x** - UI组件库
- **Vite 4.x** - 构建工具
- **Zustand** - 状态管理
- **React Router 6.x** - 路由管理
- **Axios** - HTTP客户端

## 项目结构

```
hospital-report-system/
├── backend/                    # Spring Boot后端
│   ├── src/main/java/com/hospital/report/
│   │   ├── config/            # 配置类
│   │   ├── controller/        # 控制器
│   │   ├── service/           # 服务层
│   │   ├── mapper/            # MyBatis映射器
│   │   ├── entity/            # 实体类
│   │   ├── dto/               # 数据传输对象
│   │   ├── utils/             # 工具类
│   │   ├── security/          # 安全配置
│   │   └── exception/         # 异常处理
│   └── src/main/resources/
│       ├── application.yml    # 主配置文件
│       ├── application-dev.yml # 开发环境配置
│       └── application-prod.yml # 生产环境配置
├── frontend/                   # React前端
│   ├── src/
│   │   ├── components/        # 公共组件
│   │   ├── pages/             # 页面组件
│   │   ├── layouts/           # 布局组件
│   │   ├── routes/            # 路由配置
│   │   ├── services/          # API服务
│   │   ├── stores/            # 状态管理
│   │   ├── types/             # TypeScript类型
│   │   ├── utils/             # 工具函数
│   │   └── styles/            # 样式文件
│   ├── package.json
│   └── vite.config.ts
└── database/                   # 数据库脚本
    ├── create_tables.sql      # 建表脚本
    ├── init_data.sql          # 初始化数据
    ├── indexes.sql            # 索引优化
    └── database_design.md     # 数据库设计文档
```

## 核心功能

### 1. 用户权限管理
- 用户管理：用户增删改查、角色分配
- 角色管理：角色权限配置
- 部门管理：层级部门结构
- 权限控制：基于RBAC的细粒度权限控制

### 2. 数据源管理
- 多数据源配置：支持MySQL、Oracle、PostgreSQL等
- 连接池管理：HikariCP高性能连接池
- 动态数据源切换：支持读写分离

### 3. SQL模板管理
- SQL模板设计：可视化SQL编辑器
- 参数化查询：支持动态参数
- 模板分类：按业务分类管理
- 版本控制：模板版本管理

### 4. 报表配置管理
- 报表设计：拖拽式报表设计器
- 图表配置：支持多种图表类型
- 导出功能：Excel、PDF等格式导出
- 缓存机制：Redis缓存提升性能

### 5. 报表分析
- 实时查询：实时数据展示
- 数据可视化：丰富的图表展示
- 交互式分析：支持钻取、筛选
- 定时任务：自动生成报表

## 快速开始

### 环境要求
- Java 17+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+

### 数据库初始化

1. 创建数据库：
```sql
CREATE DATABASE hospital_report_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行建表脚本：
```bash
mysql -u root -p hospital_report_system < database/create_tables.sql
```

3. 插入初始化数据：
```bash
mysql -u root -p hospital_report_system < database/init_data.sql
```

### 后端启动

1. 进入后端目录：
```bash
cd backend
```

2. 配置数据库连接（application-dev.yml）：
```yaml
spring:
  datasource:
    primary:
      url: jdbc:mysql://localhost:3306/hospital_report_system
      username: root
      password: your_password
```

3. 启动应用：
```bash
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

### 前端启动

1. 进入前端目录：
```bash
cd frontend
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

前端将在 http://localhost:3000 启动

## 默认账户

系统预置了以下测试账户（密码均为：123456）：

- **admin** - 超级管理员
- **sysadmin** - 系统管理员
- **dataadmin** - 数据管理员
- **reportadmin** - 报表管理员
- **analyst** - 报表分析师

## API文档

启动后端后，可访问 Swagger API文档：
http://localhost:8080/api/swagger-ui.html

## 主要特性

### 安全特性
- JWT身份认证
- RBAC权限控制
- 密码加密存储
- 请求防篡改
- SQL注入防护

### 性能特性
- 连接池优化
- Redis缓存
- 查询优化
- 代码分割
- 懒加载

### 用户体验
- 响应式设计
- 国际化支持
- 主题切换
- 操作友好
- 错误处理

## 开发计划

- [ ] 完善报表设计器
- [ ] 添加更多图表类型
- [ ] 支持更多数据源
- [ ] 移动端适配
- [ ] 大数据处理优化

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目基于 MIT 许可证开源。

## 联系方式

如有问题或建议，请提交 Issue 或联系开发团队。