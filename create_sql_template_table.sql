-- 创建SQL模板表和测试数据
USE hospital_report_system;

-- 创建SQL模板表
CREATE TABLE IF NOT EXISTS sql_template (
    template_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    template_description TEXT COMMENT '模板描述',
    template_category VARCHAR(100) COMMENT '模板分类',
    template_content LONGTEXT NOT NULL COMMENT 'SQL内容',
    template_version VARCHAR(20) DEFAULT '1.0' COMMENT '版本号',
    database_type VARCHAR(50) DEFAULT 'MySQL' COMMENT '数据库类型',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    tags VARCHAR(500) COMMENT '标签',
    approval_status VARCHAR(20) DEFAULT 'APPROVED' COMMENT '审批状态',
    created_by BIGINT COMMENT '创建人ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人ID',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    approved_by BIGINT COMMENT '审批人ID',
    approved_time DATETIME COMMENT '审批时间',
    INDEX idx_template_category (template_category),
    INDEX idx_is_active (is_active),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL模板表';

-- 创建SQL模板参数表
CREATE TABLE IF NOT EXISTS sql_template_parameter (
    parameter_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参数ID',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    parameter_name VARCHAR(100) NOT NULL COMMENT '参数名称',
    parameter_type VARCHAR(50) NOT NULL COMMENT '参数类型',
    default_value VARCHAR(500) COMMENT '默认值',
    is_required BOOLEAN DEFAULT FALSE COMMENT '是否必填',
    description TEXT COMMENT '参数描述',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL模板参数表';

-- 创建SQL模板版本表
CREATE TABLE IF NOT EXISTS sql_template_version (
    version_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '版本ID',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    version_number VARCHAR(20) NOT NULL COMMENT '版本号',
    template_content LONGTEXT NOT NULL COMMENT 'SQL内容',
    change_description TEXT COMMENT '变更说明',
    created_by BIGINT COMMENT '创建人ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_template_id (template_id),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL模板版本表';

-- 插入测试数据
INSERT INTO sql_template (template_name, template_description, template_category, template_content, database_type, is_active, is_public, created_by) VALUES
('门诊患者统计', '统计每日门诊患者数量和科室分布情况', '门诊', 'SELECT department, COUNT(*) as patient_count FROM outpatient_records WHERE visit_date = CURDATE() GROUP BY department;', 'MySQL', TRUE, TRUE, 1),
('住院收入分析', '分析各科室住院患者收入情况', '住院', 'SELECT d.name as department, SUM(i.amount) as total_revenue FROM inpatient_bills i JOIN departments d ON i.department_id = d.id WHERE i.bill_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) GROUP BY d.name;', 'MySQL', TRUE, TRUE, 1),
('医生绩效考核', '医生月度绩效考核指标统计', '绩效', 'SELECT doctor_name, patient_count, surgery_count, satisfaction_score FROM doctor_performance WHERE month = MONTH(CURDATE()) AND year = YEAR(CURDATE());', 'MySQL', TRUE, TRUE, 1),
('急诊科日报表', '急诊科每日患者统计', '急诊', 'SELECT COUNT(*) as total_patients, AVG(waiting_time) as avg_waiting_time FROM emergency_records WHERE visit_date = CURDATE();', 'MySQL', TRUE, TRUE, 1),
('手术室使用率', '手术室使用率统计', '手术', 'SELECT room_name, COUNT(*) as surgery_count, SUM(duration) as total_duration FROM surgery_records WHERE surgery_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) GROUP BY room_name;', 'MySQL', TRUE, TRUE, 1),
('检验科报告统计', '检验科报告数量统计', '检验', 'SELECT test_type, COUNT(*) as report_count FROM lab_reports WHERE report_date = CURDATE() GROUP BY test_type;', 'MySQL', TRUE, TRUE, 1),
('影像科检查统计', '影像科检查数量统计', '影像', 'SELECT exam_type, COUNT(*) as exam_count FROM radiology_exams WHERE exam_date = CURDATE() GROUP BY exam_type;', 'MySQL', TRUE, TRUE, 1),
('药房销售统计', '药房药品销售统计', '药房', 'SELECT drug_name, SUM(quantity) as total_quantity, SUM(amount) as total_amount FROM pharmacy_sales WHERE sale_date = CURDATE() GROUP BY drug_name;', 'MySQL', TRUE, TRUE, 1),
('财务科收入统计', '财务科日收入统计', '财务', 'SELECT payment_type, SUM(amount) as total_amount FROM payments WHERE payment_date = CURDATE() GROUP BY payment_type;', 'MySQL', TRUE, TRUE, 1),
('人事科员工统计', '人事科员工信息统计', '人事', 'SELECT department, COUNT(*) as employee_count FROM employees WHERE status = "ACTIVE" GROUP BY department;', 'MySQL', TRUE, TRUE, 1);

-- 插入一些不同分类的模板用于测试过滤功能
INSERT INTO sql_template (template_name, template_description, template_category, template_content, database_type, is_active, is_public, created_by) VALUES
('内科门诊统计', '内科门诊患者统计', '内科', 'SELECT COUNT(*) as patient_count FROM outpatient_records WHERE department = "内科" AND visit_date = CURDATE();', 'MySQL', TRUE, TRUE, 1),
('外科手术统计', '外科手术数量统计', '外科', 'SELECT COUNT(*) as surgery_count FROM surgery_records WHERE department = "外科" AND surgery_date = CURDATE();', 'MySQL', TRUE, TRUE, 1),
('儿科疫苗统计', '儿科疫苗接种统计', '儿科', 'SELECT vaccine_type, COUNT(*) as vaccination_count FROM vaccinations WHERE department = "儿科" AND vaccination_date = CURDATE() GROUP BY vaccine_type;', 'MySQL', TRUE, TRUE, 1),
('上报类数据统计', '用于上报的数据统计', '上报类', 'SELECT * FROM report_data WHERE report_type = "GOVERNMENT" AND report_date = CURDATE();', 'MySQL', TRUE, TRUE, 1),
('内部运营分析', '内部运营数据分析', '内部运营', 'SELECT department, revenue, cost, profit FROM operational_data WHERE data_date = CURDATE();', 'MySQL', TRUE, TRUE, 1),
('质量监控报告', '医疗质量监控报告', '质量监控', 'SELECT indicator_name, target_value, actual_value FROM quality_indicators WHERE report_date = CURDATE();', 'MySQL', TRUE, TRUE, 1);

COMMIT;
