-- ===============================
-- 简化的子报表关联功能数据库修改脚本
-- ===============================

USE hospital_report_system;

-- 为报表配置表添加子报表关联字段
-- 注意：实际使用时请检查表名是否为 sys_report_config 还是 report_config
ALTER TABLE sys_report_config 
ADD COLUMN linked_report_id BIGINT NULL COMMENT '关联的子报表ID' AFTER chart_config,
ADD COLUMN trigger_param_field VARCHAR(100) NULL COMMENT '触发参数字段名(如CASE_NO)' AFTER linked_report_id,
ADD INDEX idx_linked_report_id (linked_report_id);

-- 添加外键约束（可选，建议在生产环境中使用）
ALTER TABLE sys_report_config 
ADD CONSTRAINT fk_report_linked_report 
FOREIGN KEY (linked_report_id) REFERENCES sys_report_config(id);

COMMIT;

-- 数据示例（仅供参考）：
-- 父报表记录：
-- id=1, report_name="患者统计报表", linked_report_id=2, trigger_param_field="CASE_NO"
-- 
-- 子报表记录：  
-- id=2, report_name="患者详情报表", report_config="{完整配置}", linked_report_id=NULL
--
-- 当用户点击父报表(id=1)的表格行时：
-- 1. 系统检查 linked_report_id=2
-- 2. 提取行数据中 CASE_NO 字段的值 
-- 3. 跳转到报表id=2，并传递参数
-- 4. 子报表使用自己存储的完整配置进行渲染