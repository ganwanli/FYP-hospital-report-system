-- 医院报表管理系统 - 初始化数据脚本
-- Hospital Report Management System - Initial Data Script

-- 注意：执行此脚本前请确保已创建所有表结构

-- ===============================
-- 1. 部门初始化数据
-- ===============================

INSERT INTO sys_dept (dept_name, dept_code, dept_type, leader, phone, email, status, sort_order, created_by) VALUES
('医院总部', 'HOSPITAL_HQ', 'MANAGEMENT', '张院长', '020-12345678', 'dean@hospital.com', 1, 1, 1),
('医务部', 'MEDICAL_DEPT', 'MANAGEMENT', '李主任', '020-12345679', 'medical@hospital.com', 1, 2, 1),
('护理部', 'NURSING_DEPT', 'MANAGEMENT', '王护士长', '020-12345680', 'nursing@hospital.com', 1, 3, 1),
('内科', 'INTERNAL_MEDICINE', 'CLINICAL', '陈主任', '020-12345681', 'internal@hospital.com', 1, 4, 1),
('外科', 'SURGERY', 'CLINICAL', '刘主任', '020-12345682', 'surgery@hospital.com', 1, 5, 1),
('儿科', 'PEDIATRICS', 'CLINICAL', '赵主任', '020-12345683', 'pediatrics@hospital.com', 1, 6, 1),
('妇产科', 'GYNECOLOGY', 'CLINICAL', '孙主任', '020-12345684', 'gynecology@hospital.com', 1, 7, 1),
('急诊科', 'EMERGENCY', 'CLINICAL', '周主任', '020-12345685', 'emergency@hospital.com', 1, 8, 1),
('影像科', 'RADIOLOGY', 'AUXILIARY', '吴主任', '020-12345686', 'radiology@hospital.com', 1, 9, 1),
('检验科', 'LABORATORY', 'AUXILIARY', '郑主任', '020-12345687', 'laboratory@hospital.com', 1, 10, 1),
('药剂科', 'PHARMACY', 'AUXILIARY', '马主任', '020-12345688', 'pharmacy@hospital.com', 1, 11, 1),
('财务部', 'FINANCE_DEPT', 'MANAGEMENT', '许主任', '020-12345689', 'finance@hospital.com', 1, 12, 1),
('人事部', 'HR_DEPT', 'MANAGEMENT', '冯主任', '020-12345690', 'hr@hospital.com', 1, 13, 1),
('信息科', 'IT_DEPT', 'MANAGEMENT', '高主任', '020-12345691', 'it@hospital.com', 1, 14, 1);

-- ===============================
-- 2. 权限初始化数据
-- ===============================

-- 一级菜单权限
INSERT INTO sys_permission (permission_name, permission_code, permission_type, menu_url, menu_icon, sort_order, status, created_by) VALUES
('系统管理', 'SYSTEM_MANAGE', 1, '/system', 'system', 1, 1, 1),
('用户管理', 'USER_MANAGE', 1, '/system/user', 'user', 2, 1, 1),
('角色管理', 'ROLE_MANAGE', 1, '/system/role', 'role', 3, 1, 1),
('权限管理', 'PERMISSION_MANAGE', 1, '/system/permission', 'permission', 4, 1, 1),
('部门管理', 'DEPT_MANAGE', 1, '/system/dept', 'dept', 5, 1, 1),
('数据源管理', 'DATASOURCE_MANAGE', 1, '/datasource', 'datasource', 6, 1, 1),
('SQL模板管理', 'SQL_TEMPLATE_MANAGE', 1, '/template', 'template', 7, 1, 1),
('报表配置', 'REPORT_CONFIG', 1, '/report', 'report', 8, 1, 1),
('报表分析', 'REPORT_ANALYSIS', 1, '/analysis', 'analysis', 9, 1, 1),
('系统监控', 'SYSTEM_MONITOR', 1, '/monitor', 'monitor', 10, 1, 1);

-- 获取刚插入的权限ID
SET @system_manage_id = (SELECT id FROM sys_permission WHERE permission_code = 'SYSTEM_MANAGE');
SET @user_manage_id = (SELECT id FROM sys_permission WHERE permission_code = 'USER_MANAGE');
SET @role_manage_id = (SELECT id FROM sys_permission WHERE permission_code = 'ROLE_MANAGE');
SET @permission_manage_id = (SELECT id FROM sys_permission WHERE permission_code = 'PERMISSION_MANAGE');
SET @dept_manage_id = (SELECT id FROM sys_permission WHERE permission_code = 'DEPT_MANAGE');
SET @datasource_manage_id = (SELECT id FROM sys_permission WHERE permission_code = 'DATASOURCE_MANAGE');
SET @sql_template_manage_id = (SELECT id FROM sys_permission WHERE permission_code = 'SQL_TEMPLATE_MANAGE');
SET @report_config_id = (SELECT id FROM sys_permission WHERE permission_code = 'REPORT_CONFIG');
SET @report_analysis_id = (SELECT id FROM sys_permission WHERE permission_code = 'REPORT_ANALYSIS');
SET @system_monitor_id = (SELECT id FROM sys_permission WHERE permission_code = 'SYSTEM_MONITOR');

-- 用户管理子权限
INSERT INTO sys_permission (parent_id, permission_name, permission_code, permission_type, sort_order, status, created_by) VALUES
(@user_manage_id, '用户查询', 'USER_QUERY', 2, 1, 1, 1),
(@user_manage_id, '用户新增', 'USER_ADD', 2, 2, 1, 1),
(@user_manage_id, '用户编辑', 'USER_EDIT', 2, 3, 1, 1),
(@user_manage_id, '用户删除', 'USER_DELETE', 2, 4, 1, 1),
(@user_manage_id, '用户重置密码', 'USER_RESET_PASSWORD', 2, 5, 1, 1);

-- 角色管理子权限
INSERT INTO sys_permission (parent_id, permission_name, permission_code, permission_type, sort_order, status, created_by) VALUES
(@role_manage_id, '角色查询', 'ROLE_QUERY', 2, 1, 1, 1),
(@role_manage_id, '角色新增', 'ROLE_ADD', 2, 2, 1, 1),
(@role_manage_id, '角色编辑', 'ROLE_EDIT', 2, 3, 1, 1),
(@role_manage_id, '角色删除', 'ROLE_DELETE', 2, 4, 1, 1),
(@role_manage_id, '角色分配权限', 'ROLE_ASSIGN_PERMISSION', 2, 5, 1, 1);

-- 数据源管理子权限
INSERT INTO sys_permission (parent_id, permission_name, permission_code, permission_type, sort_order, status, created_by) VALUES
(@datasource_manage_id, '数据源查询', 'DATASOURCE_QUERY', 2, 1, 1, 1),
(@datasource_manage_id, '数据源新增', 'DATASOURCE_ADD', 2, 2, 1, 1),
(@datasource_manage_id, '数据源编辑', 'DATASOURCE_EDIT', 2, 3, 1, 1),
(@datasource_manage_id, '数据源删除', 'DATASOURCE_DELETE', 2, 4, 1, 1),
(@datasource_manage_id, '数据源测试连接', 'DATASOURCE_TEST', 2, 5, 1, 1);

-- SQL模板管理子权限
INSERT INTO sys_permission (parent_id, permission_name, permission_code, permission_type, sort_order, status, created_by) VALUES
(@sql_template_manage_id, 'SQL模板查询', 'SQL_TEMPLATE_QUERY', 2, 1, 1, 1),
(@sql_template_manage_id, 'SQL模板新增', 'SQL_TEMPLATE_ADD', 2, 2, 1, 1),
(@sql_template_manage_id, 'SQL模板编辑', 'SQL_TEMPLATE_EDIT', 2, 3, 1, 1),
(@sql_template_manage_id, 'SQL模板删除', 'SQL_TEMPLATE_DELETE', 2, 4, 1, 1),
(@sql_template_manage_id, 'SQL模板执行', 'SQL_TEMPLATE_EXECUTE', 2, 5, 1, 1);

-- 报表配置子权限
INSERT INTO sys_permission (parent_id, permission_name, permission_code, permission_type, sort_order, status, created_by) VALUES
(@report_config_id, '报表查询', 'REPORT_QUERY', 2, 1, 1, 1),
(@report_config_id, '报表新增', 'REPORT_ADD', 2, 2, 1, 1),
(@report_config_id, '报表编辑', 'REPORT_EDIT', 2, 3, 1, 1),
(@report_config_id, '报表删除', 'REPORT_DELETE', 2, 4, 1, 1),
(@report_config_id, '报表执行', 'REPORT_EXECUTE', 2, 5, 1, 1),
(@report_config_id, '报表导出', 'REPORT_EXPORT', 2, 6, 1, 1),
(@report_config_id, '报表发布', 'REPORT_PUBLISH', 2, 7, 1, 1);

-- ===============================
-- 3. 角色初始化数据
-- ===============================

INSERT INTO sys_role (role_name, role_code, description, status, sort_order, created_by) VALUES
('超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1, 1, 1),
('系统管理员', 'SYSTEM_ADMIN', '系统管理员，负责系统配置和用户管理', 1, 2, 1),
('数据管理员', 'DATA_ADMIN', '数据管理员，负责数据源和SQL模板管理', 1, 3, 1),
('报表管理员', 'REPORT_ADMIN', '报表管理员，负责报表配置和管理', 1, 4, 1),
('报表分析师', 'REPORT_ANALYST', '报表分析师，负责报表分析和查看', 1, 5, 1),
('部门管理员', 'DEPT_ADMIN', '部门管理员，管理本部门的报表', 1, 6, 1),
('普通用户', 'NORMAL_USER', '普通用户，只能查看分配的报表', 1, 7, 1);

-- ===============================
-- 4. 用户初始化数据
-- ===============================

INSERT INTO sys_user (username, password, salt, real_name, email, phone, status, dept_id, created_by) VALUES
('admin', '$2a$10$7JB720yubVSa0IeNRxnV9.GNyQSNrxhqmcVQHqXK6TnGdvJmgdU6e', 'admin_salt', '超级管理员', 'admin@hospital.com', '13800138000', 1, 1, 1),
('sysadmin', '$2a$10$7JB720yubVSa0IeNRxnV9.GNyQSNrxhqmcVQHqXK6TnGdvJmgdU6e', 'sys_salt', '系统管理员', 'sysadmin@hospital.com', '13800138001', 1, 14, 1),
('dataadmin', '$2a$10$7JB720yubVSa0IeNRxnV9.GNyQSNrxhqmcVQHqXK6TnGdvJmgdU6e', 'data_salt', '数据管理员', 'dataadmin@hospital.com', '13800138002', 1, 14, 1),
('reportadmin', '$2a$10$7JB720yubVSa0IeNRxnV9.GNyQSNrxhqmcVQHqXK6TnGdvJmgdU6e', 'report_salt', '报表管理员', 'reportadmin@hospital.com', '13800138003', 1, 14, 1),
('analyst', '$2a$10$7JB720yubVSa0IeNRxnV9.GNyQSNrxhqmcVQHqXK6TnGdvJmgdU6e', 'analyst_salt', '报表分析师', 'analyst@hospital.com', '13800138004', 1, 2, 1),
('deptadmin', '$2a$10$7JB720yubVSa0IeNRxnV9.GNyQSNrxhqmcVQHqXK6TnGdvJmgdU6e', 'dept_salt', '部门管理员', 'deptadmin@hospital.com', '13800138005', 1, 4, 1),
('user1', '$2a$10$7JB720yubVSa0IeNRxnV9.GNyQSNrxhqmcVQHqXK6TnGdvJmgdU6e', 'user1_salt', '普通用户1', 'user1@hospital.com', '13800138006', 1, 4, 1),
('user2', '$2a$10$7JB720yubVSa0IeNRxnV9.GNyQSNrxhqmcVQHqXK6TnGdvJmgdU6e', 'user2_salt', '普通用户2', 'user2@hospital.com', '13800138007', 1, 5, 1);

-- ===============================
-- 5. 用户角色关联数据
-- ===============================

-- 获取用户ID和角色ID
SET @admin_user_id = (SELECT id FROM sys_user WHERE username = 'admin');
SET @sysadmin_user_id = (SELECT id FROM sys_user WHERE username = 'sysadmin');
SET @dataadmin_user_id = (SELECT id FROM sys_user WHERE username = 'dataadmin');
SET @reportadmin_user_id = (SELECT id FROM sys_user WHERE username = 'reportadmin');
SET @analyst_user_id = (SELECT id FROM sys_user WHERE username = 'analyst');
SET @deptadmin_user_id = (SELECT id FROM sys_user WHERE username = 'deptadmin');
SET @user1_user_id = (SELECT id FROM sys_user WHERE username = 'user1');
SET @user2_user_id = (SELECT id FROM sys_user WHERE username = 'user2');

SET @super_admin_role_id = (SELECT id FROM sys_role WHERE role_code = 'SUPER_ADMIN');
SET @system_admin_role_id = (SELECT id FROM sys_role WHERE role_code = 'SYSTEM_ADMIN');
SET @data_admin_role_id = (SELECT id FROM sys_role WHERE role_code = 'DATA_ADMIN');
SET @report_admin_role_id = (SELECT id FROM sys_role WHERE role_code = 'REPORT_ADMIN');
SET @report_analyst_role_id = (SELECT id FROM sys_role WHERE role_code = 'REPORT_ANALYST');
SET @dept_admin_role_id = (SELECT id FROM sys_role WHERE role_code = 'DEPT_ADMIN');
SET @normal_user_role_id = (SELECT id FROM sys_role WHERE role_code = 'NORMAL_USER');

INSERT INTO sys_user_role (user_id, role_id, created_by) VALUES
(@admin_user_id, @super_admin_role_id, 1),
(@sysadmin_user_id, @system_admin_role_id, 1),
(@dataadmin_user_id, @data_admin_role_id, 1),
(@reportadmin_user_id, @report_admin_role_id, 1),
(@analyst_user_id, @report_analyst_role_id, 1),
(@deptadmin_user_id, @dept_admin_role_id, 1),
(@user1_user_id, @normal_user_role_id, 1),
(@user2_user_id, @normal_user_role_id, 1);

-- ===============================
-- 6. 角色权限关联数据
-- ===============================

-- 超级管理员拥有所有权限
INSERT INTO sys_role_permission (role_id, permission_id, created_by)
SELECT @super_admin_role_id, id, 1 FROM sys_permission;

-- 系统管理员权限
INSERT INTO sys_role_permission (role_id, permission_id, created_by)
SELECT @system_admin_role_id, id, 1 FROM sys_permission 
WHERE permission_code IN ('SYSTEM_MANAGE', 'USER_MANAGE', 'ROLE_MANAGE', 'PERMISSION_MANAGE', 'DEPT_MANAGE', 'SYSTEM_MONITOR',
                         'USER_QUERY', 'USER_ADD', 'USER_EDIT', 'USER_DELETE', 'USER_RESET_PASSWORD',
                         'ROLE_QUERY', 'ROLE_ADD', 'ROLE_EDIT', 'ROLE_DELETE', 'ROLE_ASSIGN_PERMISSION');

-- 数据管理员权限
INSERT INTO sys_role_permission (role_id, permission_id, created_by)
SELECT @data_admin_role_id, id, 1 FROM sys_permission 
WHERE permission_code IN ('DATASOURCE_MANAGE', 'SQL_TEMPLATE_MANAGE', 'SYSTEM_MONITOR',
                         'DATASOURCE_QUERY', 'DATASOURCE_ADD', 'DATASOURCE_EDIT', 'DATASOURCE_DELETE', 'DATASOURCE_TEST',
                         'SQL_TEMPLATE_QUERY', 'SQL_TEMPLATE_ADD', 'SQL_TEMPLATE_EDIT', 'SQL_TEMPLATE_DELETE', 'SQL_TEMPLATE_EXECUTE');

-- 报表管理员权限
INSERT INTO sys_role_permission (role_id, permission_id, created_by)
SELECT @report_admin_role_id, id, 1 FROM sys_permission 
WHERE permission_code IN ('REPORT_CONFIG', 'REPORT_ANALYSIS', 'SYSTEM_MONITOR',
                         'REPORT_QUERY', 'REPORT_ADD', 'REPORT_EDIT', 'REPORT_DELETE', 'REPORT_EXECUTE', 'REPORT_EXPORT', 'REPORT_PUBLISH');

-- 报表分析师权限
INSERT INTO sys_role_permission (role_id, permission_id, created_by)
SELECT @report_analyst_role_id, id, 1 FROM sys_permission 
WHERE permission_code IN ('REPORT_ANALYSIS', 'REPORT_QUERY', 'REPORT_EXECUTE', 'REPORT_EXPORT');

-- 部门管理员权限
INSERT INTO sys_role_permission (role_id, permission_id, created_by)
SELECT @dept_admin_role_id, id, 1 FROM sys_permission 
WHERE permission_code IN ('REPORT_ANALYSIS', 'REPORT_QUERY', 'REPORT_EXECUTE', 'REPORT_EXPORT');

-- 普通用户权限
INSERT INTO sys_role_permission (role_id, permission_id, created_by)
SELECT @normal_user_role_id, id, 1 FROM sys_permission 
WHERE permission_code IN ('REPORT_ANALYSIS', 'REPORT_QUERY', 'REPORT_EXECUTE');

-- ===============================
-- 7. 数据字典初始化数据
-- ===============================

-- 用户状态字典
INSERT INTO sys_dict (dict_type, dict_code, dict_label, dict_value, description, sort_order, status, created_by) VALUES
('USER_STATUS', 'ENABLE', '启用', '1', '用户状态-启用', 1, 1, 1),
('USER_STATUS', 'DISABLE', '禁用', '0', '用户状态-禁用', 2, 1, 1);

-- 数据源类型字典
INSERT INTO sys_dict (dict_type, dict_code, dict_label, dict_value, description, sort_order, status, created_by) VALUES
('DATASOURCE_TYPE', 'MYSQL', 'MySQL', 'MYSQL', 'MySQL数据库', 1, 1, 1),
('DATASOURCE_TYPE', 'ORACLE', 'Oracle', 'ORACLE', 'Oracle数据库', 2, 1, 1),
('DATASOURCE_TYPE', 'POSTGRESQL', 'PostgreSQL', 'POSTGRESQL', 'PostgreSQL数据库', 3, 1, 1),
('DATASOURCE_TYPE', 'SQLSERVER', 'SQL Server', 'SQLSERVER', 'SQL Server数据库', 4, 1, 1),
('DATASOURCE_TYPE', 'SQLITE', 'SQLite', 'SQLITE', 'SQLite数据库', 5, 1, 1);

-- SQL模板类型字典
INSERT INTO sys_dict (dict_type, dict_code, dict_label, dict_value, description, sort_order, status, created_by) VALUES
('SQL_TEMPLATE_TYPE', 'SELECT', '查询', 'SELECT', 'SQL查询模板', 1, 1, 1),
('SQL_TEMPLATE_TYPE', 'INSERT', '插入', 'INSERT', 'SQL插入模板', 2, 1, 1),
('SQL_TEMPLATE_TYPE', 'UPDATE', '更新', 'UPDATE', 'SQL更新模板', 3, 1, 1),
('SQL_TEMPLATE_TYPE', 'DELETE', '删除', 'DELETE', 'SQL删除模板', 4, 1, 1);

-- 报表类型字典
INSERT INTO sys_dict (dict_type, dict_code, dict_label, dict_value, description, sort_order, status, created_by) VALUES
('REPORT_TYPE', 'TABLE', '表格', 'TABLE', '表格报表', 1, 1, 1),
('REPORT_TYPE', 'CHART', '图表', 'CHART', '图表报表', 2, 1, 1),
('REPORT_TYPE', 'EXPORT', '导出', 'EXPORT', '导出报表', 3, 1, 1);

-- 图表类型字典
INSERT INTO sys_dict (dict_type, dict_code, dict_label, dict_value, description, sort_order, status, created_by) VALUES
('CHART_TYPE', 'LINE', '折线图', 'LINE', '折线图', 1, 1, 1),
('CHART_TYPE', 'BAR', '柱状图', 'BAR', '柱状图', 2, 1, 1),
('CHART_TYPE', 'PIE', '饼图', 'PIE', '饼图', 3, 1, 1),
('CHART_TYPE', 'AREA', '面积图', 'AREA', '面积图', 4, 1, 1),
('CHART_TYPE', 'SCATTER', '散点图', 'SCATTER', '散点图', 5, 1, 1);

-- 报表访问级别字典
INSERT INTO sys_dict (dict_type, dict_code, dict_label, dict_value, description, sort_order, status, created_by) VALUES
('REPORT_ACCESS_LEVEL', 'PUBLIC', '公开', 'PUBLIC', '所有用户可访问', 1, 1, 1),
('REPORT_ACCESS_LEVEL', 'PRIVATE', '私有', 'PRIVATE', '仅创建者可访问', 2, 1, 1),
('REPORT_ACCESS_LEVEL', 'DEPT', '部门', 'DEPT', '部门内用户可访问', 3, 1, 1);

-- 执行状态字典
INSERT INTO sys_dict (dict_type, dict_code, dict_label, dict_value, description, sort_order, status, created_by) VALUES
('EXECUTION_STATUS', 'RUNNING', '运行中', 'RUNNING', '正在执行', 1, 1, 1),
('EXECUTION_STATUS', 'SUCCESS', '成功', 'SUCCESS', '执行成功', 2, 1, 1),
('EXECUTION_STATUS', 'FAILED', '失败', 'FAILED', '执行失败', 3, 1, 1),
('EXECUTION_STATUS', 'TIMEOUT', '超时', 'TIMEOUT', '执行超时', 4, 1, 1);

-- ===============================
-- 8. 默认数据源配置
-- ===============================

INSERT INTO sys_datasource (datasource_name, datasource_code, datasource_type, host, port, database_name, username, password, connection_url, driver_class, description, status, created_by) VALUES
('本地MySQL', 'LOCAL_MYSQL', 'MYSQL', 'localhost', 3306, 'hospital_report_system', 'root', 'AES_ENCRYPT("password", "datasource_key")', 'jdbc:mysql://localhost:3306/hospital_report_system?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8', 'com.mysql.cj.jdbc.Driver', '本地MySQL数据源', 1, 1);

-- ===============================
-- 9. 报表分类初始化数据
-- ===============================

INSERT INTO sys_report_category (category_name, category_code, category_icon, description, sort_order, status, created_by) VALUES
('医疗统计', 'MEDICAL_STATISTICS', 'medical', '医疗相关统计报表', 1, 1, 1),
('财务分析', 'FINANCIAL_ANALYSIS', 'financial', '财务分析报表', 2, 1, 1),
('人员管理', 'STAFF_MANAGEMENT', 'staff', '人员管理报表', 3, 1, 1),
('设备管理', 'EQUIPMENT_MANAGEMENT', 'equipment', '设备管理报表', 4, 1, 1),
('质量控制', 'QUALITY_CONTROL', 'quality', '质量控制报表', 5, 1, 1);

-- 医疗统计子分类
SET @medical_statistics_id = (SELECT id FROM sys_report_category WHERE category_code = 'MEDICAL_STATISTICS');
INSERT INTO sys_report_category (parent_id, category_name, category_code, category_icon, description, sort_order, status, created_by) VALUES
(@medical_statistics_id, '门诊统计', 'OUTPATIENT_STATISTICS', 'outpatient', '门诊相关统计', 1, 1, 1),
(@medical_statistics_id, '住院统计', 'INPATIENT_STATISTICS', 'inpatient', '住院相关统计', 2, 1, 1),
(@medical_statistics_id, '手术统计', 'SURGERY_STATISTICS', 'surgery', '手术相关统计', 3, 1, 1),
(@medical_statistics_id, '药品统计', 'MEDICINE_STATISTICS', 'medicine', '药品相关统计', 4, 1, 1);

-- ===============================
-- 10. 系统配置初始化数据
-- ===============================

INSERT INTO sys_config (config_name, config_key, config_value, config_type, config_group, description, is_system, sort_order, status, created_by) VALUES
('系统名称', 'SYSTEM_NAME', '医院报表管理系统', 'STRING', 'SYSTEM', '系统名称配置', 1, 1, 1, 1),
('系统版本', 'SYSTEM_VERSION', '1.0.0', 'STRING', 'SYSTEM', '系统版本号', 1, 2, 1, 1),
('系统描述', 'SYSTEM_DESCRIPTION', '医院报表管理系统是一个专业的医疗报表管理平台', 'STRING', 'SYSTEM', '系统描述', 1, 3, 1, 1),
('默认密码', 'DEFAULT_PASSWORD', '123456', 'STRING', 'SECURITY', '用户默认密码', 1, 4, 1, 1),
('密码有效期', 'PASSWORD_EXPIRE_DAYS', '90', 'NUMBER', 'SECURITY', '密码有效期（天）', 1, 5, 1, 1),
('登录失败锁定次数', 'LOGIN_FAIL_LOCK_COUNT', '5', 'NUMBER', 'SECURITY', '登录失败锁定次数', 1, 6, 1, 1),
('会话超时时间', 'SESSION_TIMEOUT', '30', 'NUMBER', 'SECURITY', '会话超时时间（分钟）', 1, 7, 1, 1),
('文件上传大小限制', 'FILE_UPLOAD_SIZE_LIMIT', '10485760', 'NUMBER', 'SYSTEM', '文件上传大小限制（字节）', 1, 8, 1, 1),
('系统邮箱', 'SYSTEM_EMAIL', 'system@hospital.com', 'STRING', 'SYSTEM', '系统邮箱地址', 1, 9, 1, 1),
('报表缓存默认过期时间', 'REPORT_CACHE_EXPIRE_TIME', '300', 'NUMBER', 'REPORT', '报表缓存默认过期时间（秒）', 1, 10, 1, 1),
('报表执行超时时间', 'REPORT_EXECUTION_TIMEOUT', '60', 'NUMBER', 'REPORT', '报表执行超时时间（秒）', 1, 11, 1, 1),
('报表最大结果集大小', 'REPORT_MAX_RESULT_SIZE', '10000', 'NUMBER', 'REPORT', '报表最大结果集大小（行）', 1, 12, 1, 1);

-- ===============================
-- 11. 字段定义初始化数据
-- ===============================

INSERT INTO sys_field_define (field_name, field_code, field_type, field_length, business_meaning, data_source, category, dict_type, is_required, status, created_by) VALUES
('患者ID', 'PATIENT_ID', 'STRING', 20, '患者唯一标识', 'HIS系统', 'MEDICAL', NULL, 1, 1, 1),
('患者姓名', 'PATIENT_NAME', 'STRING', 50, '患者姓名', 'HIS系统', 'MEDICAL', NULL, 1, 1, 1),
('性别', 'GENDER', 'STRING', 10, '患者性别', 'HIS系统', 'MEDICAL', NULL, 1, 1, 1),
('年龄', 'AGE', 'NUMBER', 3, '患者年龄', 'HIS系统', 'MEDICAL', NULL, 0, 1, 1),
('出生日期', 'BIRTH_DATE', 'DATE', NULL, '患者出生日期', 'HIS系统', 'MEDICAL', NULL, 0, 1, 1),
('科室编码', 'DEPT_CODE', 'STRING', 20, '科室编码', 'HIS系统', 'MEDICAL', NULL, 1, 1, 1),
('科室名称', 'DEPT_NAME', 'STRING', 50, '科室名称', 'HIS系统', 'MEDICAL', NULL, 1, 1, 1),
('医生编码', 'DOCTOR_CODE', 'STRING', 20, '医生编码', 'HIS系统', 'MEDICAL', NULL, 1, 1, 1),
('医生姓名', 'DOCTOR_NAME', 'STRING', 50, '医生姓名', 'HIS系统', 'MEDICAL', NULL, 1, 1, 1),
('诊断代码', 'DIAGNOSIS_CODE', 'STRING', 20, 'ICD-10诊断代码', 'HIS系统', 'MEDICAL', NULL, 1, 1, 1),
('诊断名称', 'DIAGNOSIS_NAME', 'STRING', 100, '诊断名称', 'HIS系统', 'MEDICAL', NULL, 1, 1, 1),
('药品编码', 'MEDICINE_CODE', 'STRING', 20, '药品编码', 'HIS系统', 'MEDICAL', NULL, 1, 1, 1),
('药品名称', 'MEDICINE_NAME', 'STRING', 100, '药品名称', 'HIS系统', 'MEDICAL', NULL, 1, 1, 1),
('费用金额', 'AMOUNT', 'NUMBER', 12, '费用金额', 'HIS系统', 'FINANCIAL', NULL, 1, 1, 1),
('费用类型', 'AMOUNT_TYPE', 'STRING', 20, '费用类型', 'HIS系统', 'FINANCIAL', NULL, 1, 1, 1),
('就诊日期', 'VISIT_DATE', 'DATE', NULL, '就诊日期', 'HIS系统', 'MEDICAL', NULL, 1, 1, 1),
('住院号', 'INPATIENT_NO', 'STRING', 20, '住院号', 'HIS系统', 'MEDICAL', NULL, 0, 1, 1),
('床位号', 'BED_NO', 'STRING', 10, '床位号', 'HIS系统', 'MEDICAL', NULL, 0, 1, 1),
('入院日期', 'ADMISSION_DATE', 'DATE', NULL, '入院日期', 'HIS系统', 'MEDICAL', NULL, 0, 1, 1),
('出院日期', 'DISCHARGE_DATE', 'DATE', NULL, '出院日期', 'HIS系统', 'MEDICAL', NULL, 0, 1, 1);

-- ===============================
-- 12. 默认账户说明
-- ===============================

-- 所有用户的默认密码都是：123456
-- 密码使用BCrypt加密，盐值已包含在加密后的密码中

-- 用户账户说明：
-- admin/123456          - 超级管理员
-- sysadmin/123456       - 系统管理员
-- dataadmin/123456      - 数据管理员
-- reportadmin/123456    - 报表管理员
-- analyst/123456        - 报表分析师
-- deptadmin/123456      - 部门管理员
-- user1/123456          - 普通用户1
-- user2/123456          - 普通用户2

-- ===============================
-- 初始化数据脚本执行完成
-- ===============================