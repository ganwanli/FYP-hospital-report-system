-- Hospital Report System Test Data
-- This file contains sample data for testing purposes

-- Clear existing data
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE report_shares;
TRUNCATE TABLE report_configs;
TRUNCATE TABLE data_sources;
TRUNCATE TABLE users;
TRUNCATE TABLE patients;
TRUNCATE TABLE doctors;
TRUNCATE TABLE departments;
TRUNCATE TABLE revenue;
SET FOREIGN_KEY_CHECKS = 1;

-- Insert test users (password is 'password123' for all users)
INSERT INTO users (id, username, password, email, real_name, role, status, created_at, updated_at) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGDQR.jSckAta', 'admin@hospital.com', '系统管理员', 'ADMIN', 'ACTIVE', NOW(), NOW()),
(2, 'doctor1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGDQR.jSckAta', 'doctor1@hospital.com', '张医生', 'USER', 'ACTIVE', NOW(), NOW()),
(3, 'nurse1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGDQR.jSckAta', 'nurse1@hospital.com', '李护士', 'USER', 'ACTIVE', NOW(), NOW()),
(4, 'analyst1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGDQR.jSckAta', 'analyst1@hospital.com', '王分析师', 'ANALYST', 'ACTIVE', NOW(), NOW()),
(5, 'manager1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGDQR.jSckAta', 'manager1@hospital.com', '刘经理', 'MANAGER', 'ACTIVE', NOW(), NOW());

-- Insert test data sources
INSERT INTO data_sources (id, name, type, host, port, database_name, username, password, status, created_by, created_at, updated_at) VALUES
(1, '主数据库', 'MYSQL', 'localhost', 3306, 'hospital_test', 'test_user', 'test_password', 'ACTIVE', 1, NOW(), NOW()),
(2, '备份数据库', 'POSTGRESQL', 'localhost', 5432, 'hospital_backup', 'backup_user', 'backup_password', 'ACTIVE', 1, NOW(), NOW()),
(3, '历史数据库', 'MYSQL', 'history.hospital.com', 3306, 'hospital_history', 'history_user', 'history_password', 'INACTIVE', 1, NOW(), NOW());

-- Insert test departments
INSERT INTO departments (id, code, name, description, status, created_at) VALUES
(1, 'NEI', '内科', '内科诊疗科室', 'ACTIVE', NOW()),
(2, 'WAI', '外科', '外科手术科室', 'ACTIVE', NOW()),
(3, 'FU', '妇科', '妇科诊疗科室', 'ACTIVE', NOW()),
(4, 'ER', '儿科', '儿科诊疗科室', 'ACTIVE', NOW()),
(5, 'XIN', '心血管科', '心血管疾病专科', 'ACTIVE', NOW()),
(6, 'SHEN', '肾内科', '肾脏疾病专科', 'ACTIVE', NOW()),
(7, 'JINGSHEN', '精神科', '精神疾病专科', 'ACTIVE', NOW());

-- Insert test doctors
INSERT INTO doctors (id, doctor_id, name, department_id, title, phone, email, status, hire_date, created_at) VALUES
(1, 'D001', '张主任', 1, '主任医师', '13900139001', 'zhang@hospital.com', 'ACTIVE', '2020-01-01', NOW()),
(2, 'D002', '李医生', 2, '副主任医师', '13900139002', 'li@hospital.com', 'ACTIVE', '2021-03-15', NOW()),
(3, 'D003', '王医生', 3, '主治医师', '13900139003', 'wang@hospital.com', 'ACTIVE', '2022-06-01', NOW()),
(4, 'D004', '刘医生', 4, '住院医师', '13900139004', 'liu@hospital.com', 'ACTIVE', '2023-01-01', NOW()),
(5, 'D005', '陈医生', 5, '主任医师', '13900139005', 'chen@hospital.com', 'ACTIVE', '2019-05-01', NOW()),
(6, 'D006', '赵医生', 1, '主治医师', '13900139006', 'zhao@hospital.com', 'ACTIVE', '2021-09-01', NOW()),
(7, 'D007', '钱医生', 2, '住院医师', '13900139007', 'qian@hospital.com', 'ACTIVE', '2023-03-01', NOW());

-- Insert test patients
INSERT INTO patients (id, patient_id, name, gender, age, phone, address, department_id, admission_date, discharge_date, status, created_at) VALUES
(1, 'P000001', '张三', '男', 35, '13800138001', '北京市朝阳区建国路88号', 1, '2023-01-15', '2023-01-20', '已出院', NOW()),
(2, 'P000002', '李四', '女', 28, '13800138002', '北京市海淀区中关村大街1号', 3, '2023-01-16', '2023-01-22', '已出院', NOW()),
(3, 'P000003', '王五', '男', 45, '13800138003', '北京市西城区西单北大街88号', 2, '2023-01-17', NULL, '住院中', NOW()),
(4, 'P000004', '赵六', '女', 32, '13800138004', '北京市东城区王府井大街138号', 4, '2023-01-18', '2023-01-25', '已出院', NOW()),
(5, 'P000005', '钱七', '男', 55, '13800138005', '北京市丰台区西三环南路88号', 5, '2023-01-19', NULL, '住院中', NOW()),
(6, 'P000006', '孙八', '女', 67, '13800138006', '北京市石景山区石景山路88号', 1, '2023-02-01', '2023-02-08', '已出院', NOW()),
(7, 'P000007', '周九', '男', 23, '13800138007', '北京市昌平区回龙观西大街88号', 4, '2023-02-03', '2023-02-10', '已出院', NOW()),
(8, 'P000008', '吴十', '女', 41, '13800138008', '北京市大兴区黄村东大街88号', 3, '2023-02-05', NULL, '住院中', NOW()),
(9, 'P000009', '郑十一', '男', 29, '13800138009', '北京市顺义区顺平路88号', 2, '2023-02-07', '2023-02-15', '已出院', NOW()),
(10, 'P000010', '冯十二', '女', 58, '13800138010', '北京市通州区通州北街88号', 5, '2023-02-10', NULL, '住院中', NOW());

-- Insert more patients for performance testing
INSERT INTO patients (patient_id, name, gender, age, phone, address, department_id, admission_date, status, created_at)
SELECT 
    CONCAT('P', LPAD(ROW_NUMBER() OVER () + 10, 6, '0')),
    CONCAT('测试患者', ROW_NUMBER() OVER ()),
    CASE WHEN MOD(ROW_NUMBER() OVER (), 2) = 0 THEN '男' ELSE '女' END,
    20 + MOD(ROW_NUMBER() OVER (), 60),
    CONCAT('138', LPAD(MOD(ROW_NUMBER() OVER (), 100000000), 8, '0')),
    CONCAT('北京市测试区测试街', ROW_NUMBER() OVER (), '号'),
    1 + MOD(ROW_NUMBER() OVER (), 7),
    DATE_ADD('2023-01-01', INTERVAL MOD(ROW_NUMBER() OVER (), 365) DAY),
    CASE WHEN MOD(ROW_NUMBER() OVER (), 3) = 0 THEN '住院中' ELSE '已出院' END,
    NOW()
FROM 
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t1,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t2,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t3,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t4
LIMIT 1000;

-- Insert test revenue data
INSERT INTO revenue (id, department_id, amount, revenue_date, type, description, patient_id, created_at) VALUES
(1, 1, 15000.00, '2023-01-15', '门诊费', '内科门诊收入', 1, NOW()),
(2, 2, 25000.00, '2023-01-15', '手术费', '外科手术收入', 3, NOW()),
(3, 3, 18000.00, '2023-01-16', '检查费', '妇科检查收入', 2, NOW()),
(4, 4, 12000.00, '2023-01-16', '治疗费', '儿科治疗收入', 4, NOW()),
(5, 5, 30000.00, '2023-01-17', '手术费', '心脏手术收入', 5, NOW()),
(6, 1, 8000.00, '2023-01-18', '药费', '内科药品收入', 6, NOW()),
(7, 2, 45000.00, '2023-01-19', '手术费', '外科大手术收入', 9, NOW()),
(8, 3, 22000.00, '2023-01-20', '检查费', '妇科专项检查收入', 8, NOW()),
(9, 4, 6000.00, '2023-01-21', '疫苗费', '儿科疫苗收入', 7, NOW()),
(10, 5, 35000.00, '2023-01-22', '治疗费', '心血管治疗收入', 10, NOW());

-- Generate more revenue data for testing
INSERT INTO revenue (department_id, amount, revenue_date, type, description, created_at)
SELECT 
    1 + MOD(ROW_NUMBER() OVER (), 7),
    ROUND(5000 + RAND() * 50000, 2),
    DATE_ADD('2023-01-01', INTERVAL MOD(ROW_NUMBER() OVER (), 365) DAY),
    CASE MOD(ROW_NUMBER() OVER (), 5)
        WHEN 0 THEN '门诊费'
        WHEN 1 THEN '手术费'
        WHEN 2 THEN '检查费'
        WHEN 3 THEN '治疗费'
        ELSE '药费'
    END,
    CONCAT('测试收入记录', ROW_NUMBER() OVER ()),
    NOW()
FROM 
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t1,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t2,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t3
LIMIT 500;

-- Insert test report configurations
INSERT INTO report_configs (id, name, description, data_source_id, canvas_width, canvas_height, components_json, status, created_by, created_at, updated_at) VALUES
(1, '患者统计报表', '医院患者统计分析报表，包含患者数量、年龄分布、科室分布等统计信息', 1, 800, 600, 
'[
  {
    "id": "table1",
    "type": "table",
    "name": "患者列表",
    "position": {"x": 50, "y": 50, "width": 700, "height": 300},
    "dataBinding": {
      "sql": "SELECT p.patient_id, p.name, p.gender, p.age, d.name as department, p.admission_date, p.status FROM patients p LEFT JOIN departments d ON p.department_id = d.id WHERE p.admission_date BETWEEN ? AND ? ORDER BY p.admission_date DESC",
      "parameters": ["startDate", "endDate"]
    },
    "tableConfig": {
      "columns": [
        {"title": "患者编号", "dataIndex": "patient_id"},
        {"title": "姓名", "dataIndex": "name"},
        {"title": "性别", "dataIndex": "gender"},
        {"title": "年龄", "dataIndex": "age"},
        {"title": "科室", "dataIndex": "department"},
        {"title": "入院日期", "dataIndex": "admission_date"},
        {"title": "状态", "dataIndex": "status"}
      ]
    }
  },
  {
    "id": "chart1",
    "type": "pie-chart",
    "name": "科室患者分布",
    "position": {"x": 50, "y": 400, "width": 350, "height": 250},
    "dataBinding": {
      "sql": "SELECT d.name as department, COUNT(p.id) as count FROM patients p LEFT JOIN departments d ON p.department_id = d.id WHERE p.admission_date BETWEEN ? AND ? GROUP BY d.id, d.name",
      "parameters": ["startDate", "endDate"]
    },
    "chartConfig": {
      "title": "科室患者分布",
      "dataKey": "count",
      "nameKey": "department"
    }
  }
]', 
'ACTIVE', 1, NOW(), NOW()),

(2, '科室收入报表', '各科室收入统计报表，展示收入趋势和科室对比', 1, 1000, 800,
'[
  {
    "id": "chart1",
    "type": "bar-chart",
    "name": "科室收入统计",
    "position": {"x": 50, "y": 50, "width": 900, "height": 400},
    "dataBinding": {
      "sql": "SELECT d.name as department, SUM(r.amount) as total_revenue FROM revenue r LEFT JOIN departments d ON r.department_id = d.id WHERE r.revenue_date BETWEEN ? AND ? GROUP BY d.id, d.name ORDER BY total_revenue DESC",
      "parameters": ["startDate", "endDate"]
    },
    "chartConfig": {
      "title": "科室收入统计",
      "xAxisKey": "department",
      "yAxisKey": "total_revenue",
      "showLegend": true
    }
  },
  {
    "id": "table1",
    "type": "table",
    "name": "收入明细",
    "position": {"x": 50, "y": 500, "width": 900, "height": 250},
    "dataBinding": {
      "sql": "SELECT d.name as department, r.type, r.amount, r.revenue_date, r.description FROM revenue r LEFT JOIN departments d ON r.department_id = d.id WHERE r.revenue_date BETWEEN ? AND ? ORDER BY r.revenue_date DESC LIMIT 100",
      "parameters": ["startDate", "endDate"]
    },
    "tableConfig": {
      "columns": [
        {"title": "科室", "dataIndex": "department"},
        {"title": "类型", "dataIndex": "type"},
        {"title": "金额", "dataIndex": "amount"},
        {"title": "日期", "dataIndex": "revenue_date"},
        {"title": "描述", "dataIndex": "description"}
      ]
    }
  }
]',
'ACTIVE', 1, NOW(), NOW()),

(3, '医生工作量报表', '医生工作量统计，包含接诊患者数量、科室分布等', 1, 800, 600,
'[
  {
    "id": "chart1",
    "type": "line-chart",
    "name": "医生接诊趋势",
    "position": {"x": 50, "y": 50, "width": 700, "height": 300},
    "dataBinding": {
      "sql": "SELECT DATE(p.admission_date) as date, COUNT(p.id) as patient_count FROM patients p WHERE p.admission_date BETWEEN ? AND ? GROUP BY DATE(p.admission_date) ORDER BY date",
      "parameters": ["startDate", "endDate"]
    },
    "chartConfig": {
      "title": "每日接诊患者数量",
      "xAxisKey": "date",
      "yAxisKey": "patient_count",
      "smooth": true
    }
  },
  {
    "id": "table1",
    "type": "table",
    "name": "医生工作量统计",
    "position": {"x": 50, "y": 400, "width": 700, "height": 200},
    "dataBinding": {
      "sql": "SELECT d.name as doctor_name, dept.name as department, COUNT(p.id) as patient_count FROM doctors d LEFT JOIN departments dept ON d.department_id = dept.id LEFT JOIN patients p ON p.department_id = d.department_id AND p.admission_date BETWEEN ? AND ? GROUP BY d.id, d.name, dept.name ORDER BY patient_count DESC",
      "parameters": ["startDate", "endDate"]
    },
    "tableConfig": {
      "columns": [
        {"title": "医生姓名", "dataIndex": "doctor_name"},
        {"title": "科室", "dataIndex": "department"},
        {"title": "接诊患者数", "dataIndex": "patient_count"}
      ]
    }
  }
]',
'ACTIVE', 1, NOW(), NOW());

-- Insert test report shares
INSERT INTO report_shares (id, report_id, share_code, share_title, share_description, share_type, access_password, expire_time, max_access_count, current_access_count, allow_export, allowed_formats, is_active, created_by, created_at) VALUES
(1, 1, 'ABC123DEF', '患者统计报表分享', '分享给院长查看的患者统计报表', 'PUBLIC', NULL, DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, 0, true, '["PDF","EXCEL","CSV"]', true, 1, NOW()),
(2, 2, 'XYZ789GHI', '科室收入报表分享', '限制访问的科室收入报表', 'PASSWORD', 'share123', DATE_ADD(NOW(), INTERVAL 7 DAY), 50, 5, true, '["PDF","EXCEL"]', true, 1, NOW()),
(3, 3, 'JKL456MNO', '医生工作量私享', '仅限指定人员查看', 'PRIVATE', NULL, DATE_ADD(NOW(), INTERVAL 14 DAY), 10, 2, false, '[]', true, 1, NOW());

-- Update auto increment values
ALTER TABLE users AUTO_INCREMENT = 6;
ALTER TABLE data_sources AUTO_INCREMENT = 4;
ALTER TABLE departments AUTO_INCREMENT = 8;
ALTER TABLE doctors AUTO_INCREMENT = 8;
ALTER TABLE patients AUTO_INCREMENT = 1011;
ALTER TABLE revenue AUTO_INCREMENT = 511;
ALTER TABLE report_configs AUTO_INCREMENT = 4;
ALTER TABLE report_shares AUTO_INCREMENT = 4;

-- Create indexes for better performance in testing
CREATE INDEX idx_patients_admission_date ON patients(admission_date);
CREATE INDEX idx_patients_department ON patients(department_id);
CREATE INDEX idx_revenue_date ON revenue(revenue_date);
CREATE INDEX idx_revenue_department ON revenue(department_id);
CREATE INDEX idx_doctors_department ON doctors(department_id);

-- Commit all changes
COMMIT;