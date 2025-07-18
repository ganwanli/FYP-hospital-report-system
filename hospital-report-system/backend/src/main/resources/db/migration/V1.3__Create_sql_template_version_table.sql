-- 创建SQL模板版本表
CREATE TABLE IF NOT EXISTS sql_template_version (
    version_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '版本ID',
    template_id BIGINT NOT NULL COMMENT '模板ID，关联sql_template表',
    version_number VARCHAR(50) NOT NULL COMMENT '版本号，如v1.0, v1.1等',
    version_description VARCHAR(500) COMMENT '版本描述',
    template_content LONGTEXT NOT NULL COMMENT 'SQL模板内容',
    change_log TEXT COMMENT '变更日志',
    is_current BOOLEAN DEFAULT FALSE COMMENT '是否为当前版本',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    template_hash VARCHAR(64) COMMENT 'SQL内容的MD5哈希值，用于检测内容变化',
    parent_version_id BIGINT COMMENT '父版本ID，用于版本追溯',
    validation_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '验证状态：PENDING-待验证, VALID-有效, INVALID-无效',
    validation_message TEXT COMMENT '验证消息',
    approval_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审批状态：PENDING-待审批, APPROVED-已审批, REJECTED-已拒绝',
    approved_by BIGINT COMMENT '审批人ID',
    approved_time DATETIME COMMENT '审批时间',
    
    INDEX idx_template_id (template_id),
    INDEX idx_version_number (version_number),
    INDEX idx_is_current (is_current),
    INDEX idx_created_by (created_by),
    INDEX idx_created_time (created_time),
    INDEX idx_template_hash (template_hash),
    INDEX idx_validation_status (validation_status),
    INDEX idx_approval_status (approval_status),
    
    CONSTRAINT fk_sql_template_version_template_id 
        FOREIGN KEY (template_id) REFERENCES sql_template(template_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    CONSTRAINT fk_sql_template_version_created_by 
        FOREIGN KEY (created_by) REFERENCES sys_user(user_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
        
    CONSTRAINT fk_sql_template_version_approved_by 
        FOREIGN KEY (approved_by) REFERENCES sys_user(user_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
        
    CONSTRAINT fk_sql_template_version_parent_version 
        FOREIGN KEY (parent_version_id) REFERENCES sql_template_version(version_id) 
        ON DELETE SET NULL ON UPDATE CASCADE
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL模板版本表';

-- 创建唯一索引，确保每个模板只有一个当前版本
CREATE UNIQUE INDEX uk_template_current_version 
ON sql_template_version (template_id, is_current) 
WHERE is_current = TRUE;

-- 插入示例数据（可选）
INSERT INTO sql_template_version (
    template_id, version_number, version_description, template_content, 
    change_log, is_current, created_by, template_hash, validation_status, approval_status
) VALUES 
(1, 'v1.0', '初始版本', 
 'SELECT department, COUNT(*) as patient_count FROM outpatient_records WHERE visit_date = CURDATE() GROUP BY department;',
 '创建门诊患者统计查询', TRUE, 1, 
 MD5('SELECT department, COUNT(*) as patient_count FROM outpatient_records WHERE visit_date = CURDATE() GROUP BY department;'),
 'VALID', 'APPROVED'),

(2, 'v1.0', '初始版本',
 'SELECT d.name as department, SUM(i.amount) as total_revenue FROM inpatient_bills i JOIN departments d ON i.department_id = d.id WHERE i.bill_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) GROUP BY d.name;',
 '创建住院收入分析查询', TRUE, 1,
 MD5('SELECT d.name as department, SUM(i.amount) as total_revenue FROM inpatient_bills i JOIN departments d ON i.department_id = d.id WHERE i.bill_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) GROUP BY d.name;'),
 'VALID', 'APPROVED'),

(3, 'v1.0', '初始版本',
 'SELECT doctor_name, patient_count, surgery_count, satisfaction_score FROM doctor_performance WHERE month = MONTH(CURDATE()) AND year = YEAR(CURDATE());',
 '创建医生绩效考核查询', TRUE, 1,
 MD5('SELECT doctor_name, patient_count, surgery_count, satisfaction_score FROM doctor_performance WHERE month = MONTH(CURDATE()) AND year = YEAR(CURDATE());'),
 'VALID', 'APPROVED');
