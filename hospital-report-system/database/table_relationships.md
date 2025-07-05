# 医院报表管理系统 - 数据库表关系图描述

## 表关系图 (Entity Relationship Diagram)

### 核心实体关系

```
用户权限管理模块:
sys_user ──┐
           ├─→ sys_user_role ←─┐
           │                   │
           │                   ├─→ sys_role ──┐
           │                   │              │
           │                   │              ├─→ sys_role_permission ←─┐
           │                   │              │                         │
           └─→ sys_dept        │              │                         │
                               │              │                         │
                               │              │                         ├─→ sys_permission
                               │              │                         │
                               │              │                         │
数据源管理模块:                │              │                         │
sys_datasource ←──────────────┼──────────────┼─────────────────────────┤
                               │              │                         │
                               │              │                         │
SQL模板管理模块:               │              │                         │
sys_sql_template ←─────────────┤              │                         │
    │                          │              │                         │
    └─→ sys_sql_template_param │              │                         │
                               │              │                         │
                               │              │                         │
报表配置管理模块:              │              │                         │
sys_report_category ←──────────┼──────────────┤                         │
    │                          │              │                         │
    └─→ sys_report_config ←────┼──────────────┤                         │
            │                  │              │                         │
            │                  │              │                         │
            ├─→ sys_report_execution          │                         │
            │                  │              │                         │
            └─→ sys_report_cache              │                         │
                               │              │                         │
                               │              │                         │
数据字典管理模块:              │              │                         │
sys_dict ←─────────────────────┤              │                         │
sys_field_define ←─────────────┤              │                         │
                               │              │                         │
                               │              │                         │
系统管理模块:                  │              │                         │
sys_log ←──────────────────────┤              │                         │
sys_config ←───────────────────┘              │                         │
                                              │                         │
                                              └─────────────────────────┘
```

### 详细关系说明

#### 1. 用户权限管理关系
- **sys_user** (用户表) - 核心用户信息
  - 1:N → sys_dept (部门表) - 用户所属部门
  - M:N → sys_role (角色表) 通过 sys_user_role 关联表

- **sys_role** (角色表) - 角色定义
  - M:N → sys_permission (权限表) 通过 sys_role_permission 关联表

- **sys_permission** (权限表) - 权限定义
  - 自关联：parent_id → id (树形结构)

#### 2. 数据源管理关系
- **sys_datasource** (数据源表) - 数据源配置
  - 1:N → sys_sql_template (SQL模板表)
  - 1:N → sys_report_config (报表配置表)
  - 1:N → sys_report_execution (报表执行记录表)

#### 3. SQL模板管理关系
- **sys_sql_template** (SQL模板表) - SQL模板定义
  - N:1 → sys_datasource (数据源表)
  - 1:N → sys_sql_template_param (SQL模板参数表)
  - 1:N → sys_report_config (报表配置表)

#### 4. 报表配置管理关系
- **sys_report_category** (报表分类表) - 报表分类
  - 自关联：parent_id → id (树形结构)
  - 1:N → sys_report_config (报表配置表)

- **sys_report_config** (报表配置表) - 报表配置
  - N:1 → sys_report_category (报表分类表)
  - N:1 → sys_sql_template (SQL模板表)
  - N:1 → sys_datasource (数据源表)
  - 1:N → sys_report_execution (报表执行记录表)
  - 1:N → sys_report_cache (报表缓存表)

#### 5. 系统管理关系
- **sys_log** (系统日志表) - 系统操作日志
  - N:1 → sys_user (用户表) 通过 created_by 字段

- **sys_config** (系统配置表) - 系统配置
  - 独立表，无外键关联

#### 6. 数据字典管理关系
- **sys_dict** (数据字典表) - 数据字典
  - 自关联：parent_code → dict_code (树形结构)

- **sys_field_define** (字段定义表) - 字段定义
  - N:1 → sys_dict (数据字典表) 通过 dict_type 字段

### 关系类型说明

#### 一对一关系 (1:1)
- 目前设计中没有严格的一对一关系

#### 一对多关系 (1:N)
- sys_dept → sys_user (部门对用户)
- sys_datasource → sys_sql_template (数据源对SQL模板)
- sys_datasource → sys_report_config (数据源对报表配置)
- sys_sql_template → sys_sql_template_param (SQL模板对参数)
- sys_sql_template → sys_report_config (SQL模板对报表配置)
- sys_report_category → sys_report_config (报表分类对报表配置)
- sys_report_config → sys_report_execution (报表配置对执行记录)
- sys_report_config → sys_report_cache (报表配置对缓存)

#### 多对多关系 (M:N)
- sys_user ↔ sys_role (用户对角色)
- sys_role ↔ sys_permission (角色对权限)

#### 树形结构关系
- sys_permission (权限树)
- sys_dept (部门树)
- sys_report_category (报表分类树)
- sys_dict (数据字典树)

### 数据流向

1. **用户权限数据流**
   ```
   sys_user → sys_user_role → sys_role → sys_role_permission → sys_permission
   ```

2. **报表生成数据流**
   ```
   sys_datasource → sys_sql_template → sys_report_config → sys_report_execution
   ```

3. **报表缓存数据流**
   ```
   sys_report_config → sys_report_cache
   ```

4. **日志记录数据流**
   ```
   sys_user → sys_log
   ```

### 关键约束

1. **外键约束**
   - 确保数据引用完整性
   - 级联删除/更新策略

2. **唯一约束**
   - 用户名唯一性
   - 角色编码唯一性
   - 权限编码唯一性
   - 数据源编码唯一性
   - SQL模板编码唯一性
   - 报表配置编码唯一性

3. **检查约束**
   - 状态字段值范围检查
   - 删除标志字段值检查

### 索引策略

1. **主键索引** - 所有表的id字段
2. **唯一索引** - 编码字段、用户名等
3. **外键索引** - 所有外键字段
4. **业务索引** - 状态、时间、类型等常用查询字段
5. **复合索引** - 多字段组合查询优化

这个关系图描述了整个系统的数据模型架构，为后续的应用开发提供了清晰的数据结构基础。