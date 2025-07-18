# SQL模板版本表创建说明

## 概述

本目录包含创建 `sql_template_version` 表的SQL脚本，用于管理SQL模板的版本控制。

## 文件说明

### 1. `create_sql_template_version_table.sql`
完整版本的创建脚本，包含：
- 表结构定义
- 外键约束
- 索引创建
- 示例数据插入

### 2. `create_sql_template_version_table_simple.sql`
简化版本的创建脚本，仅包含：
- 基本表结构
- 基本索引
- 无外键约束（避免依赖问题）

## 执行步骤

### 方式一：使用完整版本（推荐）

1. 确保 `sql_template` 表已存在
2. 连接到MySQL数据库
3. 执行以下命令：

```sql
source /path/to/create_sql_template_version_table.sql
```

或者复制文件内容到MySQL客户端执行。

### 方式二：使用简化版本

如果完整版本执行失败（通常是外键约束问题），使用简化版本：

```sql
source /path/to/create_sql_template_version_table_simple.sql
```

## 表结构说明

### 主要字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `version_id` | BIGINT | 版本ID（主键，自增） |
| `template_id` | BIGINT | 模板ID（外键关联sql_template表） |
| `version_number` | VARCHAR(50) | 版本号（如v1.0, v1.1） |
| `version_description` | VARCHAR(500) | 版本描述 |
| `template_content` | LONGTEXT | SQL模板内容 |
| `change_log` | TEXT | 变更日志 |
| `is_current` | BOOLEAN | 是否为当前版本 |
| `created_by` | BIGINT | 创建人ID |
| `created_time` | DATETIME | 创建时间 |
| `template_hash` | VARCHAR(64) | SQL内容的MD5哈希值 |
| `parent_version_id` | BIGINT | 父版本ID |
| `validation_status` | VARCHAR(20) | 验证状态 |
| `validation_message` | TEXT | 验证消息 |
| `approval_status` | VARCHAR(20) | 审批状态 |
| `approved_by` | BIGINT | 审批人ID |
| `approved_time` | DATETIME | 审批时间 |

### 状态值说明

#### validation_status（验证状态）
- `PENDING`: 待验证
- `VALID`: 有效
- `INVALID`: 无效

#### approval_status（审批状态）
- `PENDING`: 待审批
- `APPROVED`: 已审批
- `REJECTED`: 已拒绝

## 功能特性

### 1. 版本控制
- 支持多版本管理
- 自动生成版本号
- 版本间关系追溯

### 2. 内容管理
- SQL内容哈希检测
- 变更日志记录
- 当前版本标记

### 3. 审批流程
- 版本验证机制
- 审批状态管理
- 审批人记录

### 4. 性能优化
- 多字段索引
- 查询优化设计

## API接口

创建表后，可以使用以下API接口：

- `GET /api/sql-template-versions/template/{templateId}` - 获取模板的所有版本
- `GET /api/sql-template-versions/template/{templateId}/current` - 获取当前版本
- `POST /api/sql-template-versions` - 创建新版本
- `PUT /api/sql-template-versions/{versionId}/set-current` - 设置当前版本
- 更多接口请参考 `SqlTemplateVersionController.java`

## 注意事项

1. **外键约束**: 如果相关表不存在，请注释掉外键约束部分
2. **用户表**: 脚本中假设用户表名为 `sys_user`，请根据实际情况调整
3. **权限**: 确保数据库用户有创建表和索引的权限
4. **备份**: 执行前建议备份现有数据

## 故障排除

### 常见问题

1. **外键约束失败**
   - 检查 `sql_template` 表是否存在
   - 检查用户表名是否正确
   - 使用简化版本脚本

2. **唯一索引创建失败**
   - MySQL版本可能不支持条件索引
   - 可以在应用层保证逻辑约束

3. **字符集问题**
   - 确保数据库支持 `utf8mb4` 字符集

### 验证安装

执行以下SQL验证表是否创建成功：

```sql
-- 检查表是否存在
SHOW TABLES LIKE 'sql_template_version';

-- 查看表结构
DESCRIBE sql_template_version;

-- 查看索引
SHOW INDEX FROM sql_template_version;

-- 查看示例数据
SELECT * FROM sql_template_version LIMIT 5;
```

## 联系支持

如果遇到问题，请检查：
1. MySQL版本兼容性
2. 数据库权限设置
3. 相关表的存在性
4. 字符集配置
