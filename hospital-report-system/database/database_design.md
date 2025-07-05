# 医院报表管理系统数据库设计文档

## 1. 数据库概述

### 1.1 设计原则
- 统一表名前缀：sys_
- 字段命名规范：下划线分隔
- 必须包含创建时间、更新时间字段
- 软删除设计（deleted字段）
- 考虑数据安全（敏感信息加密）

### 1.2 数据库规范
- 数据库名称：hospital_report_system
- 字符集：utf8mb4
- 排序规则：utf8mb4_unicode_ci
- 引擎：InnoDB

## 2. 表结构设计

### 2.1 用户权限管理模块

#### 2.1.1 用户表 (sys_user)
用于存储系统用户基本信息

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 用户ID |
| username | VARCHAR | 50 | - | Y | - | 用户名 |
| password | VARCHAR | 255 | - | Y | - | 密码(加密) |
| salt | VARCHAR | 32 | - | Y | - | 密码盐值 |
| real_name | VARCHAR | 100 | - | Y | - | 真实姓名 |
| email | VARCHAR | 100 | - | N | - | 邮箱 |
| phone | VARCHAR | 20 | - | N | - | 手机号 |
| avatar | VARCHAR | 500 | - | N | - | 头像URL |
| status | TINYINT | - | - | Y | 1 | 状态(1:启用,0:禁用) |
| dept_id | BIGINT | - | - | N | - | 部门ID |
| last_login_time | DATETIME | - | - | N | - | 最后登录时间 |
| last_login_ip | VARCHAR | 50 | - | N | - | 最后登录IP |
| login_count | INT | - | - | Y | 0 | 登录次数 |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |
| updated_by | BIGINT | - | - | N | - | 更新人ID |
| deleted | TINYINT | - | - | Y | 0 | 是否删除(0:否,1:是) |

#### 2.1.2 角色表 (sys_role)
用于存储系统角色信息

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 角色ID |
| role_name | VARCHAR | 100 | - | Y | - | 角色名称 |
| role_code | VARCHAR | 50 | - | Y | - | 角色编码 |
| description | TEXT | - | - | N | - | 角色描述 |
| status | TINYINT | - | - | Y | 1 | 状态(1:启用,0:禁用) |
| sort_order | INT | - | - | Y | 0 | 排序号 |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |
| updated_by | BIGINT | - | - | N | - | 更新人ID |
| deleted | TINYINT | - | - | Y | 0 | 是否删除(0:否,1:是) |

#### 2.1.3 权限表 (sys_permission)
用于存储系统权限信息

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 权限ID |
| parent_id | BIGINT | - | - | Y | 0 | 父权限ID |
| permission_name | VARCHAR | 100 | - | Y | - | 权限名称 |
| permission_code | VARCHAR | 100 | - | Y | - | 权限编码 |
| permission_type | TINYINT | - | - | Y | 1 | 权限类型(1:菜单,2:按钮,3:接口) |
| menu_url | VARCHAR | 200 | - | N | - | 菜单URL |
| menu_icon | VARCHAR | 100 | - | N | - | 菜单图标 |
| sort_order | INT | - | - | Y | 0 | 排序号 |
| status | TINYINT | - | - | Y | 1 | 状态(1:启用,0:禁用) |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |
| updated_by | BIGINT | - | - | N | - | 更新人ID |
| deleted | TINYINT | - | - | Y | 0 | 是否删除(0:否,1:是) |

#### 2.1.4 用户角色关联表 (sys_user_role)
用于存储用户与角色的关联关系

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 主键ID |
| user_id | BIGINT | - | - | Y | - | 用户ID |
| role_id | BIGINT | - | - | Y | - | 角色ID |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |

#### 2.1.5 角色权限关联表 (sys_role_permission)
用于存储角色与权限的关联关系

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 主键ID |
| role_id | BIGINT | - | - | Y | - | 角色ID |
| permission_id | BIGINT | - | - | Y | - | 权限ID |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |

#### 2.1.6 部门表 (sys_dept)
用于存储部门信息

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 部门ID |
| parent_id | BIGINT | - | - | Y | 0 | 父部门ID |
| dept_name | VARCHAR | 100 | - | Y | - | 部门名称 |
| dept_code | VARCHAR | 50 | - | Y | - | 部门编码 |
| dept_type | VARCHAR | 20 | - | Y | - | 部门类型 |
| leader | VARCHAR | 50 | - | N | - | 负责人 |
| phone | VARCHAR | 20 | - | N | - | 联系电话 |
| email | VARCHAR | 100 | - | N | - | 邮箱 |
| address | VARCHAR | 200 | - | N | - | 地址 |
| status | TINYINT | - | - | Y | 1 | 状态(1:启用,0:禁用) |
| sort_order | INT | - | - | Y | 0 | 排序号 |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |
| updated_by | BIGINT | - | - | N | - | 更新人ID |
| deleted | TINYINT | - | - | Y | 0 | 是否删除(0:否,1:是) |

### 2.2 数据源管理模块

#### 2.2.1 数据源配置表 (sys_datasource)
用于存储多数据源配置信息

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 数据源ID |
| datasource_name | VARCHAR | 100 | - | Y | - | 数据源名称 |
| datasource_code | VARCHAR | 50 | - | Y | - | 数据源编码 |
| datasource_type | VARCHAR | 20 | - | Y | - | 数据源类型(MYSQL,ORACLE,POSTGRESQL等) |
| host | VARCHAR | 100 | - | Y | - | 主机地址 |
| port | INT | - | - | Y | - | 端口号 |
| database_name | VARCHAR | 100 | - | Y | - | 数据库名 |
| username | VARCHAR | 100 | - | Y | - | 用户名 |
| password | VARCHAR | 500 | - | Y | - | 密码(加密) |
| connection_url | VARCHAR | 500 | - | Y | - | 连接URL |
| driver_class | VARCHAR | 200 | - | Y | - | 驱动类名 |
| connection_params | TEXT | - | - | N | - | 连接参数(JSON格式) |
| max_pool_size | INT | - | - | Y | 10 | 最大连接池大小 |
| min_pool_size | INT | - | - | Y | 5 | 最小连接池大小 |
| connection_timeout | INT | - | - | Y | 30000 | 连接超时时间(毫秒) |
| test_query | VARCHAR | 200 | - | N | - | 测试查询SQL |
| status | TINYINT | - | - | Y | 1 | 状态(1:启用,0:禁用) |
| description | TEXT | - | - | N | - | 描述 |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |
| updated_by | BIGINT | - | - | N | - | 更新人ID |
| deleted | TINYINT | - | - | Y | 0 | 是否删除(0:否,1:是) |

### 2.3 数据字典管理模块

#### 2.3.1 数据字典表 (sys_dict)
用于存储系统数据字典信息

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 字典ID |
| dict_type | VARCHAR | 100 | - | Y | - | 字典类型 |
| dict_code | VARCHAR | 100 | - | Y | - | 字典编码 |
| dict_label | VARCHAR | 100 | - | Y | - | 字典标签 |
| dict_value | VARCHAR | 500 | - | Y | - | 字典值 |
| parent_code | VARCHAR | 100 | - | N | - | 父字典编码 |
| dict_level | TINYINT | - | - | Y | 1 | 字典层级 |
| description | TEXT | - | - | N | - | 描述 |
| sort_order | INT | - | - | Y | 0 | 排序号 |
| status | TINYINT | - | - | Y | 1 | 状态(1:启用,0:禁用) |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |
| updated_by | BIGINT | - | - | N | - | 更新人ID |
| deleted | TINYINT | - | - | Y | 0 | 是否删除(0:否,1:是) |

#### 2.3.2 字段定义表 (sys_field_define)
用于存储字段定义和业务含义

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 字段定义ID |
| field_name | VARCHAR | 100 | - | Y | - | 字段名称 |
| field_code | VARCHAR | 100 | - | Y | - | 字段编码 |
| field_type | VARCHAR | 20 | - | Y | - | 字段类型(STRING,NUMBER,DATE,BOOLEAN等) |
| field_length | INT | - | - | N | - | 字段长度 |
| field_precision | INT | - | - | N | - | 字段精度 |
| field_scale | INT | - | - | N | - | 字段小数位 |
| default_value | VARCHAR | 500 | - | N | - | 默认值 |
| business_meaning | TEXT | - | - | N | - | 业务含义 |
| data_source | VARCHAR | 100 | - | N | - | 数据来源 |
| validation_rule | TEXT | - | - | N | - | 验证规则(JSON格式) |
| format_rule | VARCHAR | 200 | - | N | - | 格式化规则 |
| dict_type | VARCHAR | 100 | - | N | - | 关联字典类型 |
| category | VARCHAR | 50 | - | N | - | 字段分类 |
| is_required | TINYINT | - | - | Y | 0 | 是否必填(1:是,0:否) |
| is_unique | TINYINT | - | - | Y | 0 | 是否唯一(1:是,0:否) |
| status | TINYINT | - | - | Y | 1 | 状态(1:启用,0:禁用) |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |
| updated_by | BIGINT | - | - | N | - | 更新人ID |
| deleted | TINYINT | - | - | Y | 0 | 是否删除(0:否,1:是) |

### 2.4 SQL模板管理模块

#### 2.4.1 SQL模板表 (sys_sql_template)
用于存储SQL模板信息

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | SQL模板ID |
| template_name | VARCHAR | 200 | - | Y | - | 模板名称 |
| template_code | VARCHAR | 100 | - | Y | - | 模板编码 |
| template_type | VARCHAR | 20 | - | Y | - | 模板类型(SELECT,INSERT,UPDATE,DELETE) |
| datasource_id | BIGINT | - | - | Y | - | 数据源ID |
| sql_content | LONGTEXT | - | - | Y | - | SQL内容 |
| parameters | TEXT | - | - | N | - | 参数定义(JSON格式) |
| result_fields | TEXT | - | - | N | - | 结果字段定义(JSON格式) |
| category | VARCHAR | 50 | - | N | - | 模板分类 |
| description | TEXT | - | - | N | - | 描述 |
| version | VARCHAR | 20 | - | Y | '1.0' | 版本号 |
| is_public | TINYINT | - | - | Y | 0 | 是否公共(1:是,0:否) |
| execution_count | INT | - | - | Y | 0 | 执行次数 |
| last_execution_time | DATETIME | - | - | N | - | 最后执行时间 |
| avg_execution_time | BIGINT | - | - | Y | 0 | 平均执行时间(毫秒) |
| status | TINYINT | - | - | Y | 1 | 状态(1:启用,0:禁用) |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |
| updated_by | BIGINT | - | - | N | - | 更新人ID |
| deleted | TINYINT | - | - | Y | 0 | 是否删除(0:否,1:是) |

#### 2.4.2 SQL模板参数表 (sys_sql_template_param)
用于存储SQL模板参数详细信息

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 参数ID |
| template_id | BIGINT | - | - | Y | - | 模板ID |
| param_name | VARCHAR | 100 | - | Y | - | 参数名称 |
| param_code | VARCHAR | 100 | - | Y | - | 参数编码 |
| param_type | VARCHAR | 20 | - | Y | - | 参数类型 |
| param_length | INT | - | - | N | - | 参数长度 |
| default_value | VARCHAR | 500 | - | N | - | 默认值 |
| is_required | TINYINT | - | - | Y | 0 | 是否必填(1:是,0:否) |
| validation_rule | TEXT | - | - | N | - | 验证规则 |
| description | TEXT | - | - | N | - | 描述 |
| sort_order | INT | - | - | Y | 0 | 排序号 |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |
| updated_by | BIGINT | - | - | N | - | 更新人ID |
| deleted | TINYINT | - | - | Y | 0 | 是否删除(0:否,1:是) |

### 2.5 报表配置管理模块

#### 2.5.1 报表配置表 (sys_report_config)
用于存储报表配置信息

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 报表配置ID |
| report_name | VARCHAR | 200 | - | Y | - | 报表名称 |
| report_code | VARCHAR | 100 | - | Y | - | 报表编码 |
| report_type | VARCHAR | 20 | - | Y | - | 报表类型(TABLE,CHART,EXPORT) |
| category_id | BIGINT | - | - | N | - | 分类ID |
| template_id | BIGINT | - | - | Y | - | SQL模板ID |
| datasource_id | BIGINT | - | - | Y | - | 数据源ID |
| report_config | LONGTEXT | - | - | Y | - | 报表配置(JSON格式) |
| chart_config | LONGTEXT | - | - | N | - | 图表配置(JSON格式) |
| export_config | TEXT | - | - | N | - | 导出配置(JSON格式) |
| cache_enabled | TINYINT | - | - | Y | 0 | 是否启用缓存(1:是,0:否) |
| cache_timeout | INT | - | - | Y | 300 | 缓存超时时间(秒) |
| refresh_interval | INT | - | - | Y | 0 | 刷新间隔(秒,0表示不自动刷新) |
| access_level | VARCHAR | 20 | - | Y | 'PRIVATE' | 访问级别(PUBLIC,PRIVATE,DEPT) |
| description | TEXT | - | - | N | - | 描述 |
| version | VARCHAR | 20 | - | Y | '1.0' | 版本号 |
| is_published | TINYINT | - | - | Y | 0 | 是否发布(1:是,0:否) |
| view_count | INT | - | - | Y | 0 | 查看次数 |
| last_view_time | DATETIME | - | - | N | - | 最后查看时间 |
| status | TINYINT | - | - | Y | 1 | 状态(1:启用,0:禁用) |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |
| updated_by | BIGINT | - | - | N | - | 更新人ID |
| deleted | TINYINT | - | - | Y | 0 | 是否删除(0:否,1:是) |

#### 2.5.2 报表分类表 (sys_report_category)
用于存储报表分类信息

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 分类ID |
| parent_id | BIGINT | - | - | Y | 0 | 父分类ID |
| category_name | VARCHAR | 100 | - | Y | - | 分类名称 |
| category_code | VARCHAR | 50 | - | Y | - | 分类编码 |
| category_icon | VARCHAR | 100 | - | N | - | 分类图标 |
| description | TEXT | - | - | N | - | 描述 |
| sort_order | INT | - | - | Y | 0 | 排序号 |
| status | TINYINT | - | - | Y | 1 | 状态(1:启用,0:禁用) |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |
| updated_by | BIGINT | - | - | N | - | 更新人ID |
| deleted | TINYINT | - | - | Y | 0 | 是否删除(0:否,1:是) |

### 2.6 报表生成记录模块

#### 2.6.1 报表执行记录表 (sys_report_execution)
用于存储报表执行历史记录

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 执行记录ID |
| report_id | BIGINT | - | - | Y | - | 报表配置ID |
| execution_id | VARCHAR | 100 | - | Y | - | 执行ID(唯一标识) |
| execution_type | VARCHAR | 20 | - | Y | - | 执行类型(MANUAL,SCHEDULE,API) |
| execution_params | TEXT | - | - | N | - | 执行参数(JSON格式) |
| execution_sql | LONGTEXT | - | - | Y | - | 执行SQL |
| execution_status | VARCHAR | 20 | - | Y | - | 执行状态(RUNNING,SUCCESS,FAILED,TIMEOUT) |
| start_time | DATETIME | - | - | Y | - | 开始时间 |
| end_time | DATETIME | - | - | N | - | 结束时间 |
| execution_time | BIGINT | - | - | Y | 0 | 执行时间(毫秒) |
| result_count | INT | - | - | Y | 0 | 结果记录数 |
| result_size | BIGINT | - | - | Y | 0 | 结果大小(字节) |
| error_message | TEXT | - | - | N | - | 错误信息 |
| error_stack | LONGTEXT | - | - | N | - | 错误堆栈 |
| client_ip | VARCHAR | 50 | - | N | - | 客户端IP |
| user_agent | TEXT | - | - | N | - | 用户代理 |
| execution_user | BIGINT | - | - | Y | - | 执行用户ID |
| datasource_id | BIGINT | - | - | Y | - | 数据源ID |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |

#### 2.6.2 报表缓存表 (sys_report_cache)
用于存储报表缓存数据

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 缓存ID |
| report_id | BIGINT | - | - | Y | - | 报表配置ID |
| cache_key | VARCHAR | 500 | - | Y | - | 缓存键 |
| cache_params | TEXT | - | - | N | - | 缓存参数(JSON格式) |
| cache_data | LONGTEXT | - | - | Y | - | 缓存数据(JSON格式) |
| cache_size | BIGINT | - | - | Y | 0 | 缓存大小(字节) |
| hit_count | INT | - | - | Y | 0 | 命中次数 |
| last_hit_time | DATETIME | - | - | N | - | 最后命中时间 |
| expire_time | DATETIME | - | - | Y | - | 过期时间 |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |

### 2.7 系统管理模块

#### 2.7.1 系统日志表 (sys_log)
用于存储系统操作日志

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 日志ID |
| log_type | VARCHAR | 20 | - | Y | - | 日志类型(LOGIN,LOGOUT,OPERATION,ERROR,SYSTEM) |
| operation | VARCHAR | 100 | - | N | - | 操作类型 |
| module | VARCHAR | 50 | - | N | - | 模块名称 |
| method | VARCHAR | 200 | - | N | - | 方法名 |
| request_uri | VARCHAR | 500 | - | N | - | 请求URI |
| request_method | VARCHAR | 10 | - | N | - | 请求方法 |
| request_params | LONGTEXT | - | - | N | - | 请求参数 |
| response_result | LONGTEXT | - | - | N | - | 响应结果 |
| execution_time | BIGINT | - | - | Y | 0 | 执行时间(毫秒) |
| client_ip | VARCHAR | 50 | - | N | - | 客户端IP |
| user_agent | TEXT | - | - | N | - | 用户代理 |
| browser | VARCHAR | 50 | - | N | - | 浏览器 |
| os | VARCHAR | 50 | - | N | - | 操作系统 |
| error_message | TEXT | - | - | N | - | 错误信息 |
| error_stack | LONGTEXT | - | - | N | - | 错误堆栈 |
| status | TINYINT | - | - | Y | 1 | 状态(1:成功,0:失败) |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| created_by | BIGINT | - | - | N | - | 操作用户ID |

#### 2.7.2 系统配置表 (sys_config)
用于存储系统配置信息

| 字段名 | 数据类型 | 长度 | 主键 | 非空 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | BIGINT | - | Y | Y | AUTO_INCREMENT | 配置ID |
| config_name | VARCHAR | 100 | - | Y | - | 配置名称 |
| config_key | VARCHAR | 100 | - | Y | - | 配置键 |
| config_value | LONGTEXT | - | - | Y | - | 配置值 |
| config_type | VARCHAR | 20 | - | Y | - | 配置类型(STRING,NUMBER,BOOLEAN,JSON) |
| config_group | VARCHAR | 50 | - | N | - | 配置分组 |
| description | TEXT | - | - | N | - | 描述 |
| is_system | TINYINT | - | - | Y | 0 | 是否系统配置(1:是,0:否) |
| is_encrypted | TINYINT | - | - | Y | 0 | 是否加密(1:是,0:否) |
| sort_order | INT | - | - | Y | 0 | 排序号 |
| status | TINYINT | - | - | Y | 1 | 状态(1:启用,0:禁用) |
| created_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 创建时间 |
| updated_time | DATETIME | - | - | Y | CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | - | N | - | 创建人ID |
| updated_by | BIGINT | - | - | N | - | 更新人ID |
| deleted | TINYINT | - | - | Y | 0 | 是否删除(0:否,1:是) |

## 3. 表关系说明

### 3.1 用户权限关系
- sys_user ← sys_user_role → sys_role
- sys_role ← sys_role_permission → sys_permission
- sys_user → sys_dept (多对一)

### 3.2 数据源关系
- sys_sql_template → sys_datasource (多对一)
- sys_report_config → sys_datasource (多对一)

### 3.3 报表配置关系
- sys_report_config → sys_report_category (多对一)
- sys_report_config → sys_sql_template (多对一)
- sys_report_execution → sys_report_config (多对一)
- sys_report_cache → sys_report_config (多对一)

### 3.4 SQL模板关系
- sys_sql_template_param → sys_sql_template (多对一)

## 4. 索引设计

### 4.1 主键索引
所有表都有主键索引 (id)

### 4.2 唯一索引
- sys_user: uk_username (username)
- sys_role: uk_role_code (role_code)
- sys_permission: uk_permission_code (permission_code)
- sys_dept: uk_dept_code (dept_code)
- sys_datasource: uk_datasource_code (datasource_code)
- sys_dict: uk_dict_type_code (dict_type, dict_code)
- sys_field_define: uk_field_code (field_code)
- sys_sql_template: uk_template_code (template_code)
- sys_report_config: uk_report_code (report_code)
- sys_report_category: uk_category_code (category_code)
- sys_config: uk_config_key (config_key)

### 4.3 普通索引
- sys_user: idx_status, idx_dept_id, idx_email, idx_phone
- sys_role: idx_status
- sys_permission: idx_parent_id, idx_permission_type, idx_status
- sys_dept: idx_parent_id, idx_status
- sys_user_role: idx_user_id, idx_role_id
- sys_role_permission: idx_role_id, idx_permission_id
- sys_datasource: idx_datasource_type, idx_status
- sys_dict: idx_dict_type, idx_status
- sys_field_define: idx_field_type, idx_category, idx_status
- sys_sql_template: idx_template_type, idx_datasource_id, idx_status, idx_created_by
- sys_sql_template_param: idx_template_id
- sys_report_config: idx_report_type, idx_category_id, idx_template_id, idx_datasource_id, idx_status, idx_created_by
- sys_report_category: idx_parent_id, idx_status
- sys_report_execution: idx_report_id, idx_execution_status, idx_start_time, idx_execution_user
- sys_report_cache: idx_report_id, idx_cache_key, idx_expire_time
- sys_log: idx_log_type, idx_created_time, idx_created_by, idx_status
- sys_config: idx_config_group, idx_status

## 5. 数据安全设计

### 5.1 敏感信息加密
- 用户密码：使用BCrypt加密
- 数据源密码：使用AES加密
- 系统配置中的敏感信息：标记is_encrypted字段并加密存储

### 5.2 软删除
所有业务表都包含deleted字段，实现软删除功能

### 5.3 审计字段
所有表都包含created_time, updated_time, created_by, updated_by字段，用于审计追踪

### 5.4 数据权限
通过用户角色权限体系控制数据访问权限，报表配置支持访问级别控制