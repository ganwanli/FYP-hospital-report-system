-- 医院报表管理系统 - 索引优化脚本
-- Hospital Report Management System - Index Optimization Script

-- 注意：创建表时已经包含了基本索引，这里是额外的性能优化索引

-- ===============================
-- 1. 用户权限模块索引优化
-- ===============================

-- 用户表额外索引
CREATE INDEX idx_user_status_dept ON sys_user(status, dept_id) COMMENT '用户状态部门复合索引';
CREATE INDEX idx_user_login_time ON sys_user(last_login_time) COMMENT '最后登录时间索引';
CREATE INDEX idx_user_real_name ON sys_user(real_name) COMMENT '真实姓名索引';

-- 角色表额外索引
CREATE INDEX idx_role_name ON sys_role(role_name) COMMENT '角色名称索引';

-- 权限表额外索引
CREATE INDEX idx_permission_parent_type ON sys_permission(parent_id, permission_type) COMMENT '父权限类型复合索引';
CREATE INDEX idx_permission_menu_url ON sys_permission(menu_url) COMMENT '菜单URL索引';

-- 部门表额外索引
CREATE INDEX idx_dept_name ON sys_dept(dept_name) COMMENT '部门名称索引';
CREATE INDEX idx_dept_leader ON sys_dept(leader) COMMENT '部门负责人索引';

-- 用户角色关联表额外索引
CREATE INDEX idx_user_role_created_time ON sys_user_role(created_time) COMMENT '用户角色创建时间索引';

-- 角色权限关联表额外索引
CREATE INDEX idx_role_permission_created_time ON sys_role_permission(created_time) COMMENT '角色权限创建时间索引';

-- ===============================
-- 2. 数据源管理模块索引优化
-- ===============================

-- 数据源表额外索引
CREATE INDEX idx_datasource_host_port ON sys_datasource(host, port) COMMENT '数据源主机端口复合索引';
CREATE INDEX idx_datasource_database_name ON sys_datasource(database_name) COMMENT '数据库名称索引';
CREATE INDEX idx_datasource_type_status ON sys_datasource(datasource_type, status) COMMENT '数据源类型状态复合索引';

-- ===============================
-- 3. 数据字典管理模块索引优化
-- ===============================

-- 数据字典表额外索引
CREATE INDEX idx_dict_type_status ON sys_dict(dict_type, status) COMMENT '字典类型状态复合索引';
CREATE INDEX idx_dict_parent_code ON sys_dict(parent_code) COMMENT '父字典编码索引';
CREATE INDEX idx_dict_label ON sys_dict(dict_label) COMMENT '字典标签索引';

-- 字段定义表额外索引
CREATE INDEX idx_field_define_type_category ON sys_field_define(field_type, category) COMMENT '字段类型分类复合索引';
CREATE INDEX idx_field_define_name ON sys_field_define(field_name) COMMENT '字段名称索引';
CREATE INDEX idx_field_define_dict_type ON sys_field_define(dict_type) COMMENT '关联字典类型索引';

-- ===============================
-- 4. SQL模板管理模块索引优化
-- ===============================

-- SQL模板表额外索引
CREATE INDEX idx_sql_template_type_status ON sys_sql_template(template_type, status) COMMENT 'SQL模板类型状态复合索引';
CREATE INDEX idx_sql_template_name ON sys_sql_template(template_name) COMMENT 'SQL模板名称索引';
CREATE INDEX idx_sql_template_category ON sys_sql_template(category) COMMENT 'SQL模板分类索引';
CREATE INDEX idx_sql_template_public ON sys_sql_template(is_public) COMMENT 'SQL模板公开状态索引';
CREATE INDEX idx_sql_template_execution_count ON sys_sql_template(execution_count) COMMENT 'SQL模板执行次数索引';
CREATE INDEX idx_sql_template_last_exec_time ON sys_sql_template(last_execution_time) COMMENT 'SQL模板最后执行时间索引';

-- SQL模板参数表额外索引
CREATE INDEX idx_sql_template_param_code ON sys_sql_template_param(param_code) COMMENT 'SQL模板参数编码索引';
CREATE INDEX idx_sql_template_param_type ON sys_sql_template_param(param_type) COMMENT 'SQL模板参数类型索引';
CREATE INDEX idx_sql_template_param_required ON sys_sql_template_param(is_required) COMMENT 'SQL模板参数必填索引';

-- ===============================
-- 5. 报表配置管理模块索引优化
-- ===============================

-- 报表配置表额外索引
CREATE INDEX idx_report_config_name ON sys_report_config(report_name) COMMENT '报表名称索引';
CREATE INDEX idx_report_config_type_status ON sys_report_config(report_type, status) COMMENT '报表类型状态复合索引';
CREATE INDEX idx_report_config_access_level ON sys_report_config(access_level) COMMENT '报表访问级别索引';
CREATE INDEX idx_report_config_published ON sys_report_config(is_published) COMMENT '报表发布状态索引';
CREATE INDEX idx_report_config_cache_enabled ON sys_report_config(cache_enabled) COMMENT '报表缓存启用状态索引';
CREATE INDEX idx_report_config_view_count ON sys_report_config(view_count) COMMENT '报表查看次数索引';

-- 报表分类表额外索引
CREATE INDEX idx_report_category_name ON sys_report_category(category_name) COMMENT '报表分类名称索引';

-- ===============================
-- 6. 报表生成记录模块索引优化
-- ===============================

-- 报表执行记录表额外索引
CREATE INDEX idx_report_execution_type ON sys_report_execution(execution_type) COMMENT '报表执行类型索引';
CREATE INDEX idx_report_execution_status_time ON sys_report_execution(execution_status, start_time) COMMENT '报表执行状态时间复合索引';
CREATE INDEX idx_report_execution_end_time ON sys_report_execution(end_time) COMMENT '报表执行结束时间索引';
CREATE INDEX idx_report_execution_duration ON sys_report_execution(execution_time) COMMENT '报表执行耗时索引';
CREATE INDEX idx_report_execution_result_count ON sys_report_execution(result_count) COMMENT '报表执行结果数量索引';
CREATE INDEX idx_report_execution_client_ip ON sys_report_execution(client_ip) COMMENT '报表执行客户端IP索引';
CREATE INDEX idx_report_execution_id ON sys_report_execution(execution_id) COMMENT '报表执行ID索引';

-- 报表缓存表额外索引
CREATE INDEX idx_report_cache_hit_count ON sys_report_cache(hit_count) COMMENT '报表缓存命中次数索引';
CREATE INDEX idx_report_cache_last_hit_time ON sys_report_cache(last_hit_time) COMMENT '报表缓存最后命中时间索引';
CREATE INDEX idx_report_cache_size ON sys_report_cache(cache_size) COMMENT '报表缓存大小索引';

-- ===============================
-- 7. 系统管理模块索引优化
-- ===============================

-- 系统日志表额外索引
CREATE INDEX idx_log_type_time ON sys_log(log_type, created_time) COMMENT '系统日志类型时间复合索引';
CREATE INDEX idx_log_operation ON sys_log(operation) COMMENT '系统日志操作索引';
CREATE INDEX idx_log_module ON sys_log(module) COMMENT '系统日志模块索引';
CREATE INDEX idx_log_request_uri ON sys_log(request_uri) COMMENT '系统日志请求URI索引';
CREATE INDEX idx_log_client_ip ON sys_log(client_ip) COMMENT '系统日志客户端IP索引';
CREATE INDEX idx_log_execution_time ON sys_log(execution_time) COMMENT '系统日志执行时间索引';
CREATE INDEX idx_log_status_time ON sys_log(status, created_time) COMMENT '系统日志状态时间复合索引';

-- 系统配置表额外索引
CREATE INDEX idx_config_name ON sys_config(config_name) COMMENT '系统配置名称索引';
CREATE INDEX idx_config_type ON sys_config(config_type) COMMENT '系统配置类型索引';
CREATE INDEX idx_config_is_system ON sys_config(is_system) COMMENT '系统配置是否系统索引';
CREATE INDEX idx_config_is_encrypted ON sys_config(is_encrypted) COMMENT '系统配置是否加密索引';

-- ===============================
-- 8. 分区表设计建议（适用于大数据量表）
-- ===============================

-- 对于日志表，建议按月分区
-- ALTER TABLE sys_log PARTITION BY RANGE (YEAR(created_time) * 100 + MONTH(created_time)) (
--     PARTITION p202401 VALUES LESS THAN (202402),
--     PARTITION p202402 VALUES LESS THAN (202403),
--     PARTITION p202403 VALUES LESS THAN (202404),
--     -- ... 继续添加分区
--     PARTITION p_max VALUES LESS THAN MAXVALUE
-- );

-- 对于报表执行记录表，建议按月分区
-- ALTER TABLE sys_report_execution PARTITION BY RANGE (YEAR(created_time) * 100 + MONTH(created_time)) (
--     PARTITION p202401 VALUES LESS THAN (202402),
--     PARTITION p202402 VALUES LESS THAN (202403),
--     PARTITION p202403 VALUES LESS THAN (202404),
--     -- ... 继续添加分区
--     PARTITION p_max VALUES LESS THAN MAXVALUE
-- );

-- ===============================
-- 9. 性能优化建议
-- ===============================

-- 1. 定期分析表统计信息
-- ANALYZE TABLE table_name;

-- 2. 定期优化表
-- OPTIMIZE TABLE table_name;

-- 3. 监控慢查询
-- SET GLOBAL slow_query_log = 'ON';
-- SET GLOBAL long_query_time = 1;

-- 4. 适当调整MySQL配置参数
-- innodb_buffer_pool_size = 系统内存的70-80%
-- innodb_log_file_size = 256M
-- innodb_flush_log_at_trx_commit = 1
-- query_cache_size = 64M (MySQL 5.7及以下)

-- ===============================
-- 10. 索引使用监控
-- ===============================

-- 查看索引使用情况
-- SELECT 
--     table_schema,
--     table_name,
--     index_name,
--     seq_in_index,
--     column_name,
--     cardinality
-- FROM information_schema.statistics 
-- WHERE table_schema = 'hospital_report_system'
-- ORDER BY table_name, index_name, seq_in_index;

-- 查看未使用的索引
-- SELECT 
--     object_schema,
--     object_name,
--     index_name
-- FROM performance_schema.table_io_waits_summary_by_index_usage
-- WHERE object_schema = 'hospital_report_system'
--   AND index_name IS NOT NULL
--   AND index_name != 'PRIMARY'
--   AND count_star = 0;

-- ===============================
-- 11. 定期维护脚本
-- ===============================

-- 清理过期缓存数据
-- DELETE FROM sys_report_cache WHERE expire_time < NOW();

-- 清理过期日志数据（保留3个月）
-- DELETE FROM sys_log WHERE created_time < DATE_SUB(NOW(), INTERVAL 3 MONTH);

-- 清理过期执行记录（保留6个月）
-- DELETE FROM sys_report_execution WHERE created_time < DATE_SUB(NOW(), INTERVAL 6 MONTH);