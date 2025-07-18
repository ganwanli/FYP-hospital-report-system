-- =====================================================
-- 创建SQL模板版本表 - 简化版本
-- 如果完整版本执行失败，请使用此版本
-- =====================================================

-- 删除表（如果存在）
DROP TABLE IF EXISTS sql_template_version;

-- 创建SQL模板版本表
CREATE TABLE sql_template_version (
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
    approved_time DATETIME COMMENT '审批时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL模板版本表';

-- 创建索引
CREATE INDEX idx_template_id ON sql_template_version (template_id);
CREATE INDEX idx_version_number ON sql_template_version (version_number);
CREATE INDEX idx_is_current ON sql_template_version (is_current);
CREATE INDEX idx_created_by ON sql_template_version (created_by);
CREATE INDEX idx_created_time ON sql_template_version (created_time);
CREATE INDEX idx_template_hash ON sql_template_version (template_hash);
CREATE INDEX idx_validation_status ON sql_template_version (validation_status);
CREATE INDEX idx_approval_status ON sql_template_version (approval_status);

-- 验证表创建
SELECT 'sql_template_version table created successfully' as status;

-- 查看表结构
DESCRIBE sql_template_version;
