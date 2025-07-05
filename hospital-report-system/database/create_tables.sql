-- 医院报表管理系统 - 完整数据库创建脚本
-- Hospital Report Management System - Complete Database Creation Script
-- 执行前请确保已创建数据库: CREATE DATABASE hospital_report_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hospital_report_system;

-- ===============================
-- 1. 用户权限管理模块
-- ===============================

-- 1.1 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    salt VARCHAR(32) NOT NULL COMMENT '密码盐值',
    real_name VARCHAR(100) NOT NULL COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    dept_id BIGINT COMMENT '部门ID',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    login_count INT DEFAULT 0 COMMENT '登录次数',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除(0:否,1:是)',
    INDEX idx_username (username),
    INDEX idx_status (status),
    INDEX idx_dept_id (dept_id),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_created_time (created_time),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 1.2 角色表
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description TEXT COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除(0:否,1:是)',
    INDEX idx_status (status),
    INDEX idx_created_time (created_time),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 1.3 权限表
CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    permission_type TINYINT DEFAULT 1 COMMENT '权限类型(1:菜单,2:按钮,3:接口)',
    menu_url VARCHAR(200) COMMENT '菜单URL',
    menu_icon VARCHAR(100) COMMENT '菜单图标',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除(0:否,1:是)',
    INDEX idx_parent_id (parent_id),
    INDEX idx_permission_type (permission_type),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time),
    UNIQUE KEY uk_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 1.4 用户角色关联表
CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '创建人ID',
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 1.5 角色权限关联表
CREATE TABLE sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '创建人ID',
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id),
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 1.6 部门表
CREATE TABLE sys_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(50) NOT NULL COMMENT '部门编码',
    dept_type VARCHAR(20) NOT NULL COMMENT '部门类型',
    leader VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    address VARCHAR(200) COMMENT '地址',
    status TINYINT DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除(0:否,1:是)',
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time),
    UNIQUE KEY uk_dept_code (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- ===============================
-- 2. 数据源管理模块
-- ===============================

-- 2.1 数据源配置表
CREATE TABLE sys_datasource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据源ID',
    datasource_name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    datasource_code VARCHAR(50) NOT NULL COMMENT '数据源编码',
    datasource_type VARCHAR(20) NOT NULL COMMENT '数据源类型(MYSQL,ORACLE,POSTGRESQL等)',
    host VARCHAR(100) NOT NULL COMMENT '主机地址',
    port INT NOT NULL COMMENT '端口号',
    database_name VARCHAR(100) NOT NULL COMMENT '数据库名',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(500) NOT NULL COMMENT '密码(加密)',
    connection_url VARCHAR(500) NOT NULL COMMENT '连接URL',
    driver_class VARCHAR(200) NOT NULL COMMENT '驱动类名',
    connection_params TEXT COMMENT '连接参数(JSON格式)',
    max_pool_size INT DEFAULT 10 COMMENT '最大连接池大小',
    min_pool_size INT DEFAULT 5 COMMENT '最小连接池大小',
    connection_timeout INT DEFAULT 30000 COMMENT '连接超时时间(毫秒)',
    test_query VARCHAR(200) COMMENT '测试查询SQL',
    status TINYINT DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    description TEXT COMMENT '描述',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除(0:否,1:是)',
    INDEX idx_datasource_type (datasource_type),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time),
    UNIQUE KEY uk_datasource_code (datasource_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据源配置表';

-- ===============================
-- 3. 数据字典管理模块
-- ===============================

-- 3.1 数据字典表
CREATE TABLE sys_dict (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典ID',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
    dict_code VARCHAR(100) NOT NULL COMMENT '字典编码',
    dict_label VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(500) NOT NULL COMMENT '字典值',
    parent_code VARCHAR(100) COMMENT '父字典编码',
    dict_level TINYINT DEFAULT 1 COMMENT '字典层级',
    description TEXT COMMENT '描述',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除(0:否,1:是)',
    INDEX idx_dict_type (dict_type),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time),
    UNIQUE KEY uk_dict_type_code (dict_type, dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典表';

-- 3.2 字段定义表
CREATE TABLE sys_field_define (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字段定义ID',
    field_name VARCHAR(100) NOT NULL COMMENT '字段名称',
    field_code VARCHAR(100) NOT NULL COMMENT '字段编码',
    field_type VARCHAR(20) NOT NULL COMMENT '字段类型(STRING,NUMBER,DATE,BOOLEAN等)',
    field_length INT COMMENT '字段长度',
    field_precision INT COMMENT '字段精度',
    field_scale INT COMMENT '字段小数位',
    default_value VARCHAR(500) COMMENT '默认值',
    business_meaning TEXT COMMENT '业务含义',
    data_source VARCHAR(100) COMMENT '数据来源',
    validation_rule TEXT COMMENT '验证规则(JSON格式)',
    format_rule VARCHAR(200) COMMENT '格式化规则',
    dict_type VARCHAR(100) COMMENT '关联字典类型',
    category VARCHAR(50) COMMENT '字段分类',
    is_required TINYINT DEFAULT 0 COMMENT '是否必填(1:是,0:否)',
    is_unique TINYINT DEFAULT 0 COMMENT '是否唯一(1:是,0:否)',
    status TINYINT DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除(0:否,1:是)',
    INDEX idx_field_type (field_type),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time),
    UNIQUE KEY uk_field_code (field_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字段定义表';

-- ===============================
-- 4. SQL模板管理模块
-- ===============================

-- 4.1 SQL模板表
CREATE TABLE sys_sql_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'SQL模板ID',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    template_code VARCHAR(100) NOT NULL COMMENT '模板编码',
    template_type VARCHAR(20) NOT NULL COMMENT '模板类型(SELECT,INSERT,UPDATE,DELETE)',
    datasource_id BIGINT NOT NULL COMMENT '数据源ID',
    sql_content LONGTEXT NOT NULL COMMENT 'SQL内容',
    parameters TEXT COMMENT '参数定义(JSON格式)',
    result_fields TEXT COMMENT '结果字段定义(JSON格式)',
    category VARCHAR(50) COMMENT '模板分类',
    description TEXT COMMENT '描述',
    version VARCHAR(20) DEFAULT '1.0' COMMENT '版本号',
    is_public TINYINT DEFAULT 0 COMMENT '是否公共(1:是,0:否)',
    execution_count INT DEFAULT 0 COMMENT '执行次数',
    last_execution_time DATETIME COMMENT '最后执行时间',
    avg_execution_time BIGINT DEFAULT 0 COMMENT '平均执行时间(毫秒)',
    status TINYINT DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除(0:否,1:是)',
    INDEX idx_template_type (template_type),
    INDEX idx_datasource_id (datasource_id),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by),
    INDEX idx_created_time (created_time),
    UNIQUE KEY uk_template_code (template_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL模板表';

-- 4.2 SQL模板参数表
CREATE TABLE sys_sql_template_param (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参数ID',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    param_name VARCHAR(100) NOT NULL COMMENT '参数名称',
    param_code VARCHAR(100) NOT NULL COMMENT '参数编码',
    param_type VARCHAR(20) NOT NULL COMMENT '参数类型',
    param_length INT COMMENT '参数长度',
    default_value VARCHAR(500) COMMENT '默认值',
    is_required TINYINT DEFAULT 0 COMMENT '是否必填(1:是,0:否)',
    validation_rule TEXT COMMENT '验证规则',
    description TEXT COMMENT '描述',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除(0:否,1:是)',
    INDEX idx_template_id (template_id),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL模板参数表';

-- ===============================
-- 5. 报表配置管理模块
-- ===============================

-- 5.1 报表配置表
CREATE TABLE sys_report_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报表配置ID',
    report_name VARCHAR(200) NOT NULL COMMENT '报表名称',
    report_code VARCHAR(100) NOT NULL COMMENT '报表编码',
    report_type VARCHAR(20) NOT NULL COMMENT '报表类型(TABLE,CHART,EXPORT)',
    category_id BIGINT COMMENT '分类ID',
    template_id BIGINT NOT NULL COMMENT 'SQL模板ID',
    datasource_id BIGINT NOT NULL COMMENT '数据源ID',
    report_config LONGTEXT NOT NULL COMMENT '报表配置(JSON格式)',
    chart_config LONGTEXT COMMENT '图表配置(JSON格式)',
    export_config TEXT COMMENT '导出配置(JSON格式)',
    cache_enabled TINYINT DEFAULT 0 COMMENT '是否启用缓存(1:是,0:否)',
    cache_timeout INT DEFAULT 300 COMMENT '缓存超时时间(秒)',
    refresh_interval INT DEFAULT 0 COMMENT '刷新间隔(秒,0表示不自动刷新)',
    access_level VARCHAR(20) DEFAULT 'PRIVATE' COMMENT '访问级别(PUBLIC,PRIVATE,DEPT)',
    description TEXT COMMENT '描述',
    version VARCHAR(20) DEFAULT '1.0' COMMENT '版本号',
    is_published TINYINT DEFAULT 0 COMMENT '是否发布(1:是,0:否)',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    last_view_time DATETIME COMMENT '最后查看时间',
    status TINYINT DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除(0:否,1:是)',
    INDEX idx_report_type (report_type),
    INDEX idx_category_id (category_id),
    INDEX idx_template_id (template_id),
    INDEX idx_datasource_id (datasource_id),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by),
    INDEX idx_created_time (created_time),
    UNIQUE KEY uk_report_code (report_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表配置表';

-- 5.2 报表分类表
CREATE TABLE sys_report_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    category_code VARCHAR(50) NOT NULL COMMENT '分类编码',
    category_icon VARCHAR(100) COMMENT '分类图标',
    description TEXT COMMENT '描述',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除(0:否,1:是)',
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time),
    UNIQUE KEY uk_category_code (category_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表分类表';

-- ===============================
-- 6. 报表生成记录模块
-- ===============================

-- 6.1 报表执行记录表
CREATE TABLE sys_report_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '执行记录ID',
    report_id BIGINT NOT NULL COMMENT '报表配置ID',
    execution_id VARCHAR(100) NOT NULL COMMENT '执行ID(唯一标识)',
    execution_type VARCHAR(20) NOT NULL COMMENT '执行类型(MANUAL,SCHEDULE,API)',
    execution_params TEXT COMMENT '执行参数(JSON格式)',
    execution_sql LONGTEXT NOT NULL COMMENT '执行SQL',
    execution_status VARCHAR(20) NOT NULL COMMENT '执行状态(RUNNING,SUCCESS,FAILED,TIMEOUT)',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    execution_time BIGINT DEFAULT 0 COMMENT '执行时间(毫秒)',
    result_count INT DEFAULT 0 COMMENT '结果记录数',
    result_size BIGINT DEFAULT 0 COMMENT '结果大小(字节)',
    error_message TEXT COMMENT '错误信息',
    error_stack LONGTEXT COMMENT '错误堆栈',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    user_agent TEXT COMMENT '用户代理',
    execution_user BIGINT NOT NULL COMMENT '执行用户ID',
    datasource_id BIGINT NOT NULL COMMENT '数据源ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_report_id (report_id),
    INDEX idx_execution_status (execution_status),
    INDEX idx_start_time (start_time),
    INDEX idx_execution_user (execution_user),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表执行记录表';

-- 6.2 报表缓存表
CREATE TABLE sys_report_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '缓存ID',
    report_id BIGINT NOT NULL COMMENT '报表配置ID',
    cache_key VARCHAR(500) NOT NULL COMMENT '缓存键',
    cache_params TEXT COMMENT '缓存参数(JSON格式)',
    cache_data LONGTEXT NOT NULL COMMENT '缓存数据(JSON格式)',
    cache_size BIGINT DEFAULT 0 COMMENT '缓存大小(字节)',
    hit_count INT DEFAULT 0 COMMENT '命中次数',
    last_hit_time DATETIME COMMENT '最后命中时间',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    INDEX idx_report_id (report_id),
    INDEX idx_cache_key (cache_key),
    INDEX idx_expire_time (expire_time),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表缓存表';

-- ===============================
-- 7. 系统管理模块
-- ===============================

-- 7.1 系统日志表
CREATE TABLE sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    log_type VARCHAR(20) NOT NULL COMMENT '日志类型(LOGIN,LOGOUT,OPERATION,ERROR,SYSTEM)',
    operation VARCHAR(100) COMMENT '操作类型',
    module VARCHAR(50) COMMENT '模块名称',
    method VARCHAR(200) COMMENT '方法名',
    request_uri VARCHAR(500) COMMENT '请求URI',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_params LONGTEXT COMMENT '请求参数',
    response_result LONGTEXT COMMENT '响应结果',
    execution_time BIGINT DEFAULT 0 COMMENT '执行时间(毫秒)',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    user_agent TEXT COMMENT '用户代理',
    browser VARCHAR(50) COMMENT '浏览器',
    os VARCHAR(50) COMMENT '操作系统',
    error_message TEXT COMMENT '错误信息',
    error_stack LONGTEXT COMMENT '错误堆栈',
    status TINYINT DEFAULT 1 COMMENT '状态(1:成功,0:失败)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '操作用户ID',
    INDEX idx_log_type (log_type),
    INDEX idx_created_time (created_time),
    INDEX idx_created_by (created_by),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- 7.2 系统配置表
CREATE TABLE sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value LONGTEXT NOT NULL COMMENT '配置值',
    config_type VARCHAR(20) NOT NULL COMMENT '配置类型(STRING,NUMBER,BOOLEAN,JSON)',
    config_group VARCHAR(50) COMMENT '配置分组',
    description TEXT COMMENT '描述',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统配置(1:是,0:否)',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密(1:是,0:否)',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态(1:启用,0:禁用)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除(0:否,1:是)',
    INDEX idx_config_group (config_group),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ===============================
-- 8. 外键约束
-- ===============================

-- 用户表外键
ALTER TABLE sys_user ADD CONSTRAINT fk_user_dept FOREIGN KEY (dept_id) REFERENCES sys_dept(id);

-- 用户角色关联表外键
ALTER TABLE sys_user_role ADD CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id);
ALTER TABLE sys_user_role ADD CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id);

-- 角色权限关联表外键
ALTER TABLE sys_role_permission ADD CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role(id);
ALTER TABLE sys_role_permission ADD CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission(id);

-- SQL模板表外键
ALTER TABLE sys_sql_template ADD CONSTRAINT fk_sql_template_datasource FOREIGN KEY (datasource_id) REFERENCES sys_datasource(id);

-- SQL模板参数表外键
ALTER TABLE sys_sql_template_param ADD CONSTRAINT fk_sql_template_param_template FOREIGN KEY (template_id) REFERENCES sys_sql_template(id);

-- 报表配置表外键
ALTER TABLE sys_report_config ADD CONSTRAINT fk_report_config_category FOREIGN KEY (category_id) REFERENCES sys_report_category(id);
ALTER TABLE sys_report_config ADD CONSTRAINT fk_report_config_template FOREIGN KEY (template_id) REFERENCES sys_sql_template(id);
ALTER TABLE sys_report_config ADD CONSTRAINT fk_report_config_datasource FOREIGN KEY (datasource_id) REFERENCES sys_datasource(id);

-- 报表执行记录表外键
ALTER TABLE sys_report_execution ADD CONSTRAINT fk_report_execution_report FOREIGN KEY (report_id) REFERENCES sys_report_config(id);
ALTER TABLE sys_report_execution ADD CONSTRAINT fk_report_execution_datasource FOREIGN KEY (datasource_id) REFERENCES sys_datasource(id);

-- 报表缓存表外键
ALTER TABLE sys_report_cache ADD CONSTRAINT fk_report_cache_report FOREIGN KEY (report_id) REFERENCES sys_report_config(id);