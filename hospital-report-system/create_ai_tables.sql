-- ===================================
-- AI Assistant Tables Creation Script
-- ===================================
-- Run this script in your MySQL database to create the AI assistant tables

-- 1. AI对话表 (AI Conversations)
CREATE TABLE ai_conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(255) NOT NULL COMMENT '对话标题',
    datasource_id BIGINT COMMENT '关联数据源ID',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, ARCHIVED, DELETED',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_datasource_id (datasource_id),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time)
) COMMENT 'AI对话表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. AI消息表 (AI Messages)
CREATE TABLE ai_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL COMMENT '对话ID',
    message_type VARCHAR(20) NOT NULL COMMENT '消息类型：USER, ASSISTANT, SYSTEM',
    content LONGTEXT NOT NULL COMMENT '消息内容',
    metadata JSON COMMENT '元数据（SQL、分析结果等）',
    token_count INT DEFAULT 0 COMMENT 'Token消耗量',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_message_type (message_type),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (conversation_id) REFERENCES ai_conversation(id) ON DELETE CASCADE
) COMMENT 'AI消息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. SQL分析记录表 (SQL Analysis Logs)
CREATE TABLE sql_analysis_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    conversation_id BIGINT COMMENT '关联对话ID',
    sql_content LONGTEXT NOT NULL COMMENT 'SQL内容',
    analysis_type VARCHAR(50) NOT NULL COMMENT '分析类型：SQL_EXPLAIN, SQL_OPTIMIZE, PERFORMANCE_ANALYZE',
    analysis_result JSON COMMENT '技术分析结果',
    ai_suggestions LONGTEXT COMMENT 'AI建议内容',
    execution_time BIGINT COMMENT '分析执行时间(ms)',
    datasource_id BIGINT COMMENT '数据源ID',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '分析状态：SUCCESS, FAILED, PENDING',
    error_message TEXT COMMENT '错误信息',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_analysis_type (analysis_type),
    INDEX idx_datasource_id (datasource_id),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (conversation_id) REFERENCES ai_conversation(id) ON DELETE SET NULL
) COMMENT 'SQL分析记录表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. AI使用统计表 (AI Usage Statistics)
CREATE TABLE ai_usage_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    stat_date DATE NOT NULL COMMENT '统计日期', 
    total_conversations INT DEFAULT 0 COMMENT '总对话数',
    total_messages INT DEFAULT 0 COMMENT '总消息数',
    total_tokens_used INT DEFAULT 0 COMMENT '总Token使用量',
    sql_analysis_count INT DEFAULT 0 COMMENT 'SQL分析次数',
    database_analysis_count INT DEFAULT 0 COMMENT '数据库分析次数',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_date (user_id, stat_date),
    INDEX idx_user_id (user_id),
    INDEX idx_stat_date (stat_date)
) COMMENT 'AI使用统计表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================
-- 测试数据 (Optional - for testing)
-- ===================================

-- 插入测试对话 (需要先确保有用户数据)
-- INSERT INTO ai_conversation (user_id, title, status) VALUES (1, '测试对话', 'ACTIVE');

-- 插入测试消息
-- INSERT INTO ai_message (conversation_id, message_type, content) 
-- VALUES (1, 'SYSTEM', '欢迎使用AI助手！我可以帮助您分析数据库结构和优化SQL语句。');

-- ===================================
-- 验证表创建成功
-- ===================================

-- 检查表是否创建成功
SHOW TABLES LIKE 'ai_%';

-- 查看表结构
DESC ai_conversation;
DESC ai_message;
DESC sql_analysis_log;
DESC ai_usage_stats;

-- 检查外键约束
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = DATABASE() 
AND REFERENCED_TABLE_NAME IS NOT NULL
AND TABLE_NAME LIKE 'ai_%';