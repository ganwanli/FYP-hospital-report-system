-- 医院报表管理系统数据库初始化脚本
-- Hospital Report Management System Database Initialization Script

-- 创建数据库
CREATE DATABASE IF NOT EXISTS hospital_report_system 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hospital_report_system;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(100) NOT NULL COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用，0禁用',
    dept_id BIGINT COMMENT '部门ID',
    role_id BIGINT COMMENT '角色ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0否，1是'
) COMMENT '用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description TEXT COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用，0禁用',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0否，1是'
) COMMENT '角色表';

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(50) NOT NULL UNIQUE COMMENT '部门编码',
    leader VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用，0禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0否，1是'
) COMMENT '部门表';

-- 报表模板表
CREATE TABLE IF NOT EXISTS report_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    template_code VARCHAR(100) NOT NULL UNIQUE COMMENT '模板编码',
    description TEXT COMMENT '模板描述',
    template_type VARCHAR(50) NOT NULL COMMENT '模板类型：DAILY,WEEKLY,MONTHLY,YEARLY',
    template_config JSON COMMENT '模板配置(JSON格式)',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用，0禁用',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0否，1是'
) COMMENT '报表模板表';

-- 报表实例表
CREATE TABLE IF NOT EXISTS report_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报表实例ID',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    report_name VARCHAR(200) NOT NULL COMMENT '报表名称',
    report_period VARCHAR(20) NOT NULL COMMENT '报表周期',
    report_data JSON COMMENT '报表数据(JSON格式)',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT草稿，SUBMITTED已提交，APPROVED已审核，REJECTED已拒绝',
    dept_id BIGINT COMMENT '部门ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    submitted_time DATETIME COMMENT '提交时间',
    submitted_by BIGINT COMMENT '提交人',
    approved_time DATETIME COMMENT '审核时间',
    approved_by BIGINT COMMENT '审核人',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0否，1是'
) COMMENT '报表实例表';

-- 报表审核记录表
CREATE TABLE IF NOT EXISTS report_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核记录ID',
    report_id BIGINT NOT NULL COMMENT '报表实例ID',
    audit_type VARCHAR(20) NOT NULL COMMENT '审核类型：SUBMIT,APPROVE,REJECT,RETURN',
    audit_status VARCHAR(20) NOT NULL COMMENT '审核状态',
    audit_comment TEXT COMMENT '审核意见',
    audit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    audit_user BIGINT COMMENT '审核人ID',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0否，1是'
) COMMENT '报表审核记录表';

-- 数据字典表
CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典ID',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
    dict_code VARCHAR(100) NOT NULL COMMENT '字典编码',
    dict_label VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典值',
    description TEXT COMMENT '描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用，0禁用',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0否，1是',
    UNIQUE KEY uk_dict_type_code (dict_type, dict_code)
) COMMENT '数据字典表';

-- 系统日志表
CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    operation VARCHAR(100) COMMENT '操作',
    method VARCHAR(200) COMMENT '方法名',
    params TEXT COMMENT '参数',
    time BIGINT COMMENT '执行时长(毫秒)',
    ip VARCHAR(64) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '操作人ID'
) COMMENT '系统日志表';

-- 添加外键约束
ALTER TABLE sys_user ADD CONSTRAINT fk_user_dept FOREIGN KEY (dept_id) REFERENCES sys_dept(id);
ALTER TABLE sys_user ADD CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES sys_role(id);
ALTER TABLE report_instance ADD CONSTRAINT fk_instance_template FOREIGN KEY (template_id) REFERENCES report_template(id);
ALTER TABLE report_instance ADD CONSTRAINT fk_instance_dept FOREIGN KEY (dept_id) REFERENCES sys_dept(id);
ALTER TABLE report_audit_log ADD CONSTRAINT fk_audit_report FOREIGN KEY (report_id) REFERENCES report_instance(id);

-- 创建索引
CREATE INDEX idx_user_username ON sys_user(username);
CREATE INDEX idx_user_status ON sys_user(status);
CREATE INDEX idx_user_dept ON sys_user(dept_id);
CREATE INDEX idx_role_code ON sys_role(role_code);
CREATE INDEX idx_dept_parent ON sys_dept(parent_id);
CREATE INDEX idx_template_code ON report_template(template_code);
CREATE INDEX idx_template_type ON report_template(template_type);
CREATE INDEX idx_instance_template ON report_instance(template_id);
CREATE INDEX idx_instance_status ON report_instance(status);
CREATE INDEX idx_instance_period ON report_instance(report_period);
CREATE INDEX idx_audit_report ON report_audit_log(report_id);
CREATE INDEX idx_dict_type ON sys_dict(dict_type);
CREATE INDEX idx_log_created_time ON sys_log(created_time);

-- 插入初始化数据
-- 插入默认角色
INSERT INTO sys_role (role_name, role_code, description) VALUES
('超级管理员', 'ADMIN', '系统超级管理员'),
('部门管理员', 'DEPT_ADMIN', '部门管理员'),
('普通用户', 'USER', '普通用户'),
('审核员', 'AUDITOR', '报表审核员');

-- 插入默认部门
INSERT INTO sys_dept (dept_name, dept_code, leader) VALUES
('总院', 'ROOT', '院长'),
('内科', 'INTERNAL', '内科主任'),
('外科', 'SURGERY', '外科主任'),
('儿科', 'PEDIATRICS', '儿科主任'),
('妇产科', 'GYNECOLOGY', '妇产科主任'),
('急诊科', 'EMERGENCY', '急诊科主任'),
('影像科', 'IMAGING', '影像科主任'),
('检验科', 'LABORATORY', '检验科主任'),
('药剂科', 'PHARMACY', '药剂科主任'),
('护理部', 'NURSING', '护理部主任');

-- 插入默认管理员用户 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, email, status, dept_id, role_id) VALUES
('admin', '$2a$10$7JB720yubVSa0IeNRxnV9.GNyQSNrxhqmcVQHqXK6TnGdvJmgdU6e', '系统管理员', 'admin@hospital.com', 1, 1, 1);

-- 插入数据字典
INSERT INTO sys_dict (dict_type, dict_code, dict_label, dict_value, sort_order) VALUES
('report_type', 'DAILY', '日报', 'DAILY', 1),
('report_type', 'WEEKLY', '周报', 'WEEKLY', 2),
('report_type', 'MONTHLY', '月报', 'MONTHLY', 3),
('report_type', 'YEARLY', '年报', 'YEARLY', 4),
('report_status', 'DRAFT', '草稿', 'DRAFT', 1),
('report_status', 'SUBMITTED', '已提交', 'SUBMITTED', 2),
('report_status', 'APPROVED', '已审核', 'APPROVED', 3),
('report_status', 'REJECTED', '已拒绝', 'REJECTED', 4),
('user_status', 'ENABLE', '启用', '1', 1),
('user_status', 'DISABLE', '禁用', '0', 2);

-- 插入报表模板示例
INSERT INTO report_template (template_name, template_code, description, template_type, template_config) VALUES
('门诊日报表', 'OUTPATIENT_DAILY', '门诊科室日报表模板', 'DAILY', 
'{"fields": [{"name": "门诊人数", "type": "number", "required": true}, {"name": "收入金额", "type": "number", "required": true}, {"name": "备注", "type": "text", "required": false}]}'),
('住院周报表', 'INPATIENT_WEEKLY', '住院科室周报表模板', 'WEEKLY', 
'{"fields": [{"name": "入院人数", "type": "number", "required": true}, {"name": "出院人数", "type": "number", "required": true}, {"name": "床位使用率", "type": "percent", "required": true}, {"name": "平均住院天数", "type": "number", "required": true}]}'),
('财务月报表', 'FINANCE_MONTHLY', '财务部门月报表模板', 'MONTHLY', 
'{"fields": [{"name": "总收入", "type": "number", "required": true}, {"name": "总支出", "type": "number", "required": true}, {"name": "利润", "type": "number", "required": true}, {"name": "分析说明", "type": "text", "required": false}]}');