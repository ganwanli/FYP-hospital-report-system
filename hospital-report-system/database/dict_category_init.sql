-- 数据字典分类管理初始化脚本
-- 创建时间：2025-01-15

-- 1. 创建分类管理表（树形结构）
CREATE TABLE IF NOT EXISTS dict_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_code VARCHAR(50) NOT NULL COMMENT '分类编码',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父级分类ID，0表示顶级',
    category_level INT DEFAULT 1 COMMENT '分类层级，1为顶级',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    icon VARCHAR(50) DEFAULT NULL COMMENT '图标',
    description TEXT COMMENT '分类描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    
    INDEX idx_parent_id (parent_id),
    INDEX idx_category_code (category_code),
    INDEX idx_status (status),
    INDEX idx_level_sort (category_level, sort_order),
    UNIQUE KEY uk_category_code (category_code)
) COMMENT='数据字典分类表';

-- 2. 创建字段定义表（如果不存在）
CREATE TABLE IF NOT EXISTS dict_field (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字段ID',
    field_code VARCHAR(100) NOT NULL UNIQUE COMMENT '字段编码（唯一）',
    field_name VARCHAR(200) NOT NULL COMMENT '字段名称',
    field_name_en VARCHAR(200) COMMENT '英文名称',
    category_id BIGINT NOT NULL COMMENT '所属分类ID',
    
    -- 技术描述
    description TEXT COMMENT '字段描述/业务含义',
    data_type VARCHAR(50) COMMENT '数据类型（VARCHAR、INT、DECIMAL等）',
    data_length VARCHAR(20) COMMENT '数据长度（如：100 或 10,2）',
    
    -- 数据来源
    source_database VARCHAR(100) COMMENT '源数据库名',
    source_table VARCHAR(200) COMMENT '源数据表名',
    source_field VARCHAR(500) COMMENT '源字段名或表达式',
    filter_condition TEXT COMMENT '筛选条件',
    
    -- 计算逻辑
    calculation_sql TEXT COMMENT '完整的SQL查询语句',
    
    -- 业务属性
    update_frequency VARCHAR(50) COMMENT '更新频率（实时、每日、每月等）',
    data_owner VARCHAR(100) COMMENT '数据负责人',
    remark TEXT COMMENT '备注说明',
    
    -- 系统字段
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-停用',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    
    INDEX idx_category_id (category_id),
    INDEX idx_field_code (field_code),
    INDEX idx_field_name (field_name),
    INDEX idx_status (status),
    INDEX idx_data_owner (data_owner),
    
    FOREIGN KEY (category_id) REFERENCES dict_category(id) ON DELETE RESTRICT
) COMMENT='数据字典字段定义表';

-- 3. 插入初始分类数据
INSERT INTO dict_category (category_code, category_name, parent_id, category_level, sort_order, icon, description, status, create_by, update_by) VALUES
-- 顶级分类
('REPORT', '上报类', 0, 1, 1, 'upload', '用于政府部门上报的数据分类', 1, 'system', 'system'),
('INTERNAL', '内部运营', 0, 1, 2, 'setting', '医院内部运营管理相关数据分类', 1, 'system', 'system'),
('PERFORMANCE', '绩效考核', 0, 1, 3, 'trophy', '绩效考核相关数据分类', 1, 'system', 'system'),
('FINANCIAL', '财务管理', 0, 1, 4, 'dollar', '财务相关数据分类', 1, 'system', 'system'),
('CLINICAL', '临床业务', 0, 1, 5, 'heart', '临床业务相关数据分类', 1, 'system', 'system'),

-- 上报类子分类
('REPORT_MONTHLY', '月报数据', 1, 2, 1, 'calendar', '每月定期上报的数据', 1, 'system', 'system'),
('REPORT_QUARTERLY', '季报数据', 1, 2, 2, 'calendar', '每季度上报的数据', 1, 'system', 'system'),
('REPORT_ANNUAL', '年报数据', 1, 2, 3, 'calendar', '每年上报的数据', 1, 'system', 'system'),
('REPORT_SPECIAL', '专项上报', 1, 2, 4, 'file-text', '特殊项目或临时性上报数据', 1, 'system', 'system'),

-- 内部运营子分类
('INTERNAL_PATIENT', '患者管理', 2, 2, 1, 'user', '患者信息管理相关数据', 1, 'system', 'system'),
('INTERNAL_STAFF', '人员管理', 2, 2, 2, 'users', '医院人员管理相关数据', 1, 'system', 'system'),
('INTERNAL_RESOURCE', '资源管理', 2, 2, 3, 'database', '医院资源配置管理数据', 1, 'system', 'system'),
('INTERNAL_QUALITY', '质量管理', 2, 2, 4, 'shield', '医疗质量管理相关数据', 1, 'system', 'system'),

-- 绩效考核子分类
('PERF_DOCTOR', '医生绩效', 3, 2, 1, 'user-check', '医生绩效考核相关数据', 1, 'system', 'system'),
('PERF_NURSE', '护士绩效', 3, 2, 2, 'user-check', '护士绩效考核相关数据', 1, 'system', 'system'),
('PERF_DEPT', '科室绩效', 3, 2, 3, 'building', '科室绩效考核相关数据', 1, 'system', 'system'),
('PERF_HOSPITAL', '医院绩效', 3, 2, 4, 'hospital', '医院整体绩效考核数据', 1, 'system', 'system'),

-- 财务管理子分类
('FIN_REVENUE', '收入管理', 4, 2, 1, 'trending-up', '医院收入相关数据', 1, 'system', 'system'),
('FIN_COST', '成本管理', 4, 2, 2, 'trending-down', '医院成本相关数据', 1, 'system', 'system'),
('FIN_BUDGET', '预算管理', 4, 2, 3, 'pie-chart', '预算管理相关数据', 1, 'system', 'system'),
('FIN_ANALYSIS', '财务分析', 4, 2, 4, 'bar-chart', '财务分析相关数据', 1, 'system', 'system'),

-- 临床业务子分类
('CLINICAL_OUTPATIENT', '门诊业务', 5, 2, 1, 'clipboard', '门诊相关业务数据', 1, 'system', 'system'),
('CLINICAL_INPATIENT', '住院业务', 5, 2, 2, 'bed', '住院相关业务数据', 1, 'system', 'system'),
('CLINICAL_EMERGENCY', '急诊业务', 5, 2, 3, 'zap', '急诊相关业务数据', 1, 'system', 'system'),
('CLINICAL_SURGERY', '手术业务', 5, 2, 4, 'scissors', '手术相关业务数据', 1, 'system', 'system');

-- 4. 更新父级分类ID（使用实际的ID）
UPDATE dict_category SET parent_id = (SELECT id FROM (SELECT id FROM dict_category WHERE category_code = 'REPORT') AS t) WHERE category_code IN ('REPORT_MONTHLY', 'REPORT_QUARTERLY', 'REPORT_ANNUAL', 'REPORT_SPECIAL');
UPDATE dict_category SET parent_id = (SELECT id FROM (SELECT id FROM dict_category WHERE category_code = 'INTERNAL') AS t) WHERE category_code IN ('INTERNAL_PATIENT', 'INTERNAL_STAFF', 'INTERNAL_RESOURCE', 'INTERNAL_QUALITY');
UPDATE dict_category SET parent_id = (SELECT id FROM (SELECT id FROM dict_category WHERE category_code = 'PERFORMANCE') AS t) WHERE category_code IN ('PERF_DOCTOR', 'PERF_NURSE', 'PERF_DEPT', 'PERF_HOSPITAL');
UPDATE dict_category SET parent_id = (SELECT id FROM (SELECT id FROM dict_category WHERE category_code = 'FINANCIAL') AS t) WHERE category_code IN ('FIN_REVENUE', 'FIN_COST', 'FIN_BUDGET', 'FIN_ANALYSIS');
UPDATE dict_category SET parent_id = (SELECT id FROM (SELECT id FROM dict_category WHERE category_code = 'CLINICAL') AS t) WHERE category_code IN ('CLINICAL_OUTPATIENT', 'CLINICAL_INPATIENT', 'CLINICAL_EMERGENCY', 'CLINICAL_SURGERY');

-- 5. 插入一些示例字段数据
INSERT INTO dict_field (field_code, field_name, field_name_en, category_id, description, data_type, data_length, source_database, source_table, source_field, status, create_by, update_by) VALUES
('OUTPATIENT_COUNT', '门诊人次', 'Outpatient Count', (SELECT id FROM dict_category WHERE category_code = 'CLINICAL_OUTPATIENT'), '统计门诊患者总人次', 'INT', '11', 'hospital_db', 'outpatient_records', 'COUNT(*)', 1, 'system', 'system'),
('INPATIENT_COUNT', '住院人次', 'Inpatient Count', (SELECT id FROM dict_category WHERE category_code = 'CLINICAL_INPATIENT'), '统计住院患者总人次', 'INT', '11', 'hospital_db', 'inpatient_records', 'COUNT(*)', 1, 'system', 'system'),
('EMERGENCY_COUNT', '急诊人次', 'Emergency Count', (SELECT id FROM dict_category WHERE category_code = 'CLINICAL_EMERGENCY'), '统计急诊患者总人次', 'INT', '11', 'hospital_db', 'emergency_records', 'COUNT(*)', 1, 'system', 'system'),
('SURGERY_COUNT', '手术台次', 'Surgery Count', (SELECT id FROM dict_category WHERE category_code = 'CLINICAL_SURGERY'), '统计手术总台次', 'INT', '11', 'hospital_db', 'surgery_records', 'COUNT(*)', 1, 'system', 'system'),
('TOTAL_REVENUE', '总收入', 'Total Revenue', (SELECT id FROM dict_category WHERE category_code = 'FIN_REVENUE'), '医院总收入金额', 'DECIMAL', '15,2', 'hospital_db', 'financial_records', 'SUM(revenue)', 1, 'system', 'system');

-- 6. 创建视图用于方便查询分类树
CREATE OR REPLACE VIEW v_dict_category_tree AS
SELECT 
    dc.*,
    (SELECT category_name FROM dict_category WHERE id = dc.parent_id) as parent_name,
    (SELECT COUNT(*) FROM dict_category WHERE parent_id = dc.id AND status = 1) as children_count,
    (SELECT COUNT(*) FROM dict_field WHERE category_id = dc.id AND status = 1) as field_count,
    CASE 
        WHEN dc.parent_id = 0 THEN dc.category_name
        ELSE CONCAT(
            (SELECT GROUP_CONCAT(category_name ORDER BY category_level SEPARATOR ' / ') 
             FROM dict_category 
             WHERE id IN (
                 WITH RECURSIVE category_path AS (
                     SELECT id, category_name, parent_id, category_level, 1 as path_level
                     FROM dict_category 
                     WHERE id = dc.id
                     UNION ALL
                     SELECT p.id, p.category_name, p.parent_id, p.category_level, cp.path_level + 1
                     FROM dict_category p
                     INNER JOIN category_path cp ON p.id = cp.parent_id
                 )
                 SELECT id FROM category_path WHERE parent_id != 0
             )
            )
        )
    END as category_path
FROM dict_category dc
WHERE dc.status = 1
ORDER BY dc.category_level, dc.sort_order, dc.create_time;

-- 7. 创建存储过程用于获取分类树
DELIMITER //
CREATE PROCEDURE GetCategoryTree(IN p_parent_id BIGINT)
BEGIN
    WITH RECURSIVE category_tree AS (
        SELECT 
            id, category_code, category_name, parent_id, category_level, 
            sort_order, icon, description, status, create_time, update_time,
            create_by, update_by, 0 as depth
        FROM dict_category 
        WHERE parent_id = p_parent_id AND status = 1
        
        UNION ALL
        
        SELECT 
            dc.id, dc.category_code, dc.category_name, dc.parent_id, dc.category_level,
            dc.sort_order, dc.icon, dc.description, dc.status, dc.create_time, dc.update_time,
            dc.create_by, dc.update_by, ct.depth + 1
        FROM dict_category dc
        INNER JOIN category_tree ct ON dc.parent_id = ct.id
        WHERE dc.status = 1
    )
    SELECT 
        ct.*,
        (SELECT COUNT(*) FROM dict_category WHERE parent_id = ct.id AND status = 1) as has_children,
        (SELECT COUNT(*) FROM dict_field WHERE category_id = ct.id AND status = 1) as field_count
    FROM category_tree ct
    ORDER BY ct.category_level, ct.sort_order, ct.create_time;
END //
DELIMITER ;

-- 8. 创建触发器维护数据一致性
DELIMITER //
CREATE TRIGGER tr_dict_category_before_delete
BEFORE DELETE ON dict_category
FOR EACH ROW
BEGIN
    DECLARE child_count INT DEFAULT 0;
    DECLARE field_count INT DEFAULT 0;
    
    -- 检查是否有子分类
    SELECT COUNT(*) INTO child_count FROM dict_category WHERE parent_id = OLD.id AND status = 1;
    IF child_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '该分类下存在子分类，不能删除';
    END IF;
    
    -- 检查是否有关联字段
    SELECT COUNT(*) INTO field_count FROM dict_field WHERE category_id = OLD.id AND status = 1;
    IF field_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '该分类下存在关联字段，不能删除';
    END IF;
END //
DELIMITER ;

-- 9. 插入完成提示
SELECT '数据字典分类管理初始化完成' as message;
SELECT COUNT(*) as category_count FROM dict_category WHERE status = 1;
SELECT COUNT(*) as field_count FROM dict_field WHERE status = 1;
