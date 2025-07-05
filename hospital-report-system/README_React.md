# 医院报表管理系统项目结构 (React版本)

## 项目技术栈

### 后端技术栈
- **Spring Boot 3.1.0** - 核心框架
- **MyBatis Plus 3.5.3** - ORM框架
- **Spring Security 6.x** - 安全框架
- **JWT** - 身份认证
- **Redis** - 缓存
- **MySQL 8.0+** - 数据库

### 前端技术栈
- **React 18.2.x** - 前端框架
- **TypeScript 5.x** - 类型系统
- **Ant Design 5.x** - UI组件库
- **Vite 4.x** - 构建工具
- **Axios** - HTTP客户端
- **Zustand** - 状态管理
- **React Query** - 数据获取
- **React Router** - 路由管理

## 完整项目目录结构

```
hospital-report-system/
├── backend/                                    # 后端项目
│   ├── pom.xml                                # Maven配置文件
│   └── src/
│       ├── main/
│       │   ├── java/com/hospital/report/
│       │   │   ├── HospitalReportApplication.java     # 启动类
│       │   │   ├── config/                    # 配置类
│       │   │   │   ├── SecurityConfig.java    # Spring Security配置
│       │   │   │   ├── RedisConfig.java       # Redis配置
│       │   │   │   ├── MyBatisPlusConfig.java # MyBatis Plus配置
│       │   │   │   ├── JwtConfig.java         # JWT配置
│       │   │   │   ├── CorsConfig.java        # 跨域配置
│       │   │   │   └── SwaggerConfig.java     # Swagger配置
│       │   │   ├── controller/                # 控制器层
│       │   │   │   ├── AuthController.java    # 认证控制器
│       │   │   │   ├── UserController.java    # 用户管理
│       │   │   │   ├── RoleController.java    # 角色管理
│       │   │   │   ├── DeptController.java    # 部门管理
│       │   │   │   ├── DatasourceController.java # 数据源管理
│       │   │   │   ├── SqlTemplateController.java # SQL模板管理
│       │   │   │   ├── ReportController.java  # 报表管理
│       │   │   │   └── SystemController.java  # 系统管理
│       │   │   ├── dto/                       # 数据传输对象
│       │   │   │   ├── request/               # 请求DTO
│       │   │   │   ├── response/              # 响应DTO
│       │   │   │   └── common/                # 通用DTO
│       │   │   ├── entity/                    # 实体类
│       │   │   │   ├── SysUser.java          # 用户实体
│       │   │   │   ├── SysRole.java          # 角色实体
│       │   │   │   ├── SysDept.java          # 部门实体
│       │   │   │   ├── SysDatasource.java    # 数据源实体
│       │   │   │   ├── SysSqlTemplate.java   # SQL模板实体
│       │   │   │   └── SysReportConfig.java  # 报表配置实体
│       │   │   ├── mapper/                    # MyBatis映射器
│       │   │   │   ├── SysUserMapper.java
│       │   │   │   ├── SysRoleMapper.java
│       │   │   │   └── ...
│       │   │   ├── service/                   # 服务层
│       │   │   │   ├── impl/                  # 服务实现
│       │   │   │   ├── AuthService.java      # 认证服务
│       │   │   │   ├── UserService.java      # 用户服务
│       │   │   │   ├── ReportService.java    # 报表服务
│       │   │   │   └── ...
│       │   │   ├── security/                  # 安全相关
│       │   │   │   ├── JwtAuthenticationFilter.java
│       │   │   │   ├── JwtTokenUtil.java
│       │   │   │   ├── UserDetailsImpl.java
│       │   │   │   └── ...
│       │   │   ├── utils/                     # 工具类
│       │   │   │   ├── ResponseUtil.java     # 响应工具
│       │   │   │   ├── PasswordUtil.java     # 密码工具
│       │   │   │   ├── DateUtil.java         # 日期工具
│       │   │   │   └── ...
│       │   │   └── exception/                 # 异常处理
│       │   │       ├── GlobalExceptionHandler.java
│       │   │       ├── BusinessException.java
│       │   │       └── ...
│       │   └── resources/
│       │       ├── application.yml            # 应用配置
│       │       ├── application-dev.yml        # 开发环境配置
│       │       ├── application-prod.yml       # 生产环境配置
│       │       ├── mapper/                    # MyBatis XML映射文件
│       │       ├── static/                    # 静态资源
│       │       └── templates/                 # 模板文件
│       └── test/                              # 测试代码
│           └── java/com/hospital/report/
├── frontend/                                  # 前端项目 (React + TypeScript)
│   ├── package.json                          # NPM配置文件
│   ├── tsconfig.json                         # TypeScript配置
│   ├── vite.config.ts                        # Vite配置
│   ├── tailwind.config.js                    # Tailwind CSS配置
│   ├── .eslintrc.js                          # ESLint配置
│   ├── .prettierrc                           # Prettier配置
│   ├── index.html                            # HTML模板
│   ├── public/                               # 公共资源
│   │   ├── favicon.ico
│   │   └── logo.png
│   ├── src/
│   │   ├── main.tsx                          # 应用入口
│   │   ├── App.tsx                           # 根组件
│   │   ├── components/                       # 组件目录
│   │   │   ├── common/                       # 通用组件
│   │   │   │   ├── Layout/                   # 布局组件
│   │   │   │   ├── Header/                   # 头部组件
│   │   │   │   ├── Sidebar/                  # 侧边栏组件
│   │   │   │   ├── Loading/                  # 加载组件
│   │   │   │   └── ErrorBoundary/            # 错误边界
│   │   │   ├── charts/                       # 图表组件
│   │   │   │   ├── LineChart/
│   │   │   │   ├── BarChart/
│   │   │   │   ├── PieChart/
│   │   │   │   └── index.ts
│   │   │   ├── forms/                        # 表单组件
│   │   │   │   ├── UserForm/
│   │   │   │   ├── ReportForm/
│   │   │   │   └── index.ts
│   │   │   └── tables/                       # 表格组件
│   │   │       ├── DataTable/
│   │   │       ├── ReportTable/
│   │   │       └── index.ts
│   │   ├── pages/                            # 页面组件
│   │   │   ├── auth/                         # 认证页面
│   │   │   │   ├── Login/
│   │   │   │   └── index.ts
│   │   │   ├── dashboard/                    # 仪表板
│   │   │   │   ├── Overview/
│   │   │   │   └── index.ts
│   │   │   ├── system/                       # 系统管理
│   │   │   │   ├── User/                     # 用户管理
│   │   │   │   ├── Role/                     # 角色管理
│   │   │   │   ├── Dept/                     # 部门管理
│   │   │   │   └── index.ts
│   │   │   ├── report/                       # 报表管理
│   │   │   │   ├── Config/                   # 报表配置
│   │   │   │   ├── View/                     # 报表查看
│   │   │   │   ├── Analysis/                 # 报表分析
│   │   │   │   └── index.ts
│   │   │   ├── datasource/                   # 数据源管理
│   │   │   │   ├── List/
│   │   │   │   ├── Config/
│   │   │   │   └── index.ts
│   │   │   └── template/                     # SQL模板管理
│   │   │       ├── List/
│   │   │       ├── Editor/
│   │   │       └── index.ts
│   │   ├── hooks/                            # 自定义Hook
│   │   │   ├── useAuth.ts                    # 认证Hook
│   │   │   ├── useApi.ts                     # API Hook
│   │   │   ├── useLocalStorage.ts            # 本地存储Hook
│   │   │   └── index.ts
│   │   ├── services/                         # API服务
│   │   │   ├── api.ts                        # API配置
│   │   │   ├── auth.ts                       # 认证服务
│   │   │   ├── user.ts                       # 用户服务
│   │   │   ├── report.ts                     # 报表服务
│   │   │   └── index.ts
│   │   ├── stores/                           # 状态管理
│   │   │   ├── authStore.ts                  # 认证状态
│   │   │   ├── userStore.ts                  # 用户状态
│   │   │   ├── reportStore.ts                # 报表状态
│   │   │   └── index.ts
│   │   ├── types/                            # 类型定义
│   │   │   ├── api.ts                        # API类型
│   │   │   ├── user.ts                       # 用户类型
│   │   │   ├── report.ts                     # 报表类型
│   │   │   └── index.ts
│   │   ├── utils/                            # 工具函数
│   │   │   ├── request.ts                    # 请求工具
│   │   │   ├── auth.ts                       # 认证工具
│   │   │   ├── format.ts                     # 格式化工具
│   │   │   ├── validate.ts                   # 验证工具
│   │   │   └── index.ts
│   │   ├── assets/                           # 静态资源
│   │   │   ├── images/
│   │   │   ├── icons/
│   │   │   └── styles/
│   │   ├── styles/                           # 样式文件
│   │   │   ├── globals.css                   # 全局样式
│   │   │   ├── antd.css                      # Ant Design样式覆盖
│   │   │   └── components.css                # 组件样式
│   │   ├── layouts/                          # 布局组件
│   │   │   ├── BasicLayout/
│   │   │   ├── AuthLayout/
│   │   │   └── index.ts
│   │   └── routes/                           # 路由配置
│   │       ├── index.tsx                     # 路由入口
│   │       ├── PrivateRoute.tsx              # 私有路由
│   │       └── routes.ts                     # 路由配置
│   ├── dist/                                 # 构建输出
│   └── config/                               # 配置文件
└── database/                                 # 数据库脚本
    ├── init.sql                              # 原始初始化脚本
    ├── create_tables.sql                     # 创建表脚本
    ├── init_data.sql                         # 初始化数据脚本
    ├── indexes.sql                           # 索引优化脚本
    ├── database_design.md                    # 数据库设计文档
    └── table_relationships.md                # 表关系文档
```

## 技术栈特色说明

### React 18 特性
- **并发特性** - Suspense、并发渲染
- **自动批处理** - 提升性能
- **新的 Hooks** - useId、useTransition、useDeferredValue

### TypeScript 5.x
- **严格类型检查** - 提高代码质量
- **智能提示** - 提升开发效率
- **类型安全** - 减少运行时错误

### Ant Design 5.x
- **现代化设计** - 符合最新设计趋势
- **丰富组件** - 200+ 高质量组件
- **主题定制** - 灵活的主题系统
- **国际化** - 完善的多语言支持

### 状态管理 - Zustand
- **轻量级** - 比Redux更简单
- **TypeScript友好** - 原生支持
- **性能优秀** - 最小重渲染

### 构建工具 - Vite
- **极速冷启动** - 比Webpack快10-100倍
- **热更新** - 毫秒级更新
- **开箱即用** - 零配置支持TypeScript

## 项目特点

1. **现代化架构** - 采用最新的前端技术栈
2. **类型安全** - 全面的TypeScript支持
3. **组件化开发** - 高度可复用的组件设计
4. **性能优化** - 代码分割、懒加载、缓存策略
5. **开发体验** - 热更新、自动化测试、代码规范
6. **响应式设计** - 适配各种屏幕尺寸
7. **国际化支持** - 多语言切换
8. **主题定制** - 灵活的主题系统