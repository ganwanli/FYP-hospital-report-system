-- 为 sql_template_version 表添加 datasource_id 字段
-- 执行时间: 2024-01-XX

-- 检查字段是否已存在，如果不存在则添加
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sql_template_version'
    AND COLUMN_NAME = 'datasource_id'
);

-- 如果字段不存在，则添加字段
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE sql_template_version ADD COLUMN datasource_id BIGINT NULL COMMENT "关联的数据源ID" AFTER modification_note',
    'SELECT "Column datasource_id already exists in sql_template_version" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加外键约束（如果数据源表存在）
SET @datasource_table_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'datasource'
);

-- 添加外键约束的SQL
SET @fk_sql = IF(@datasource_table_exists > 0 AND @column_exists = 0,
    'ALTER TABLE sql_template_version ADD CONSTRAINT fk_sql_template_version_datasource FOREIGN KEY (datasource_id) REFERENCES datasource(id) ON DELETE SET NULL ON UPDATE CASCADE',
    'SELECT "Datasource table does not exist or foreign key already exists for sql_template_version" as message'
);

PREPARE fk_stmt FROM @fk_sql;
EXECUTE fk_stmt;
DEALLOCATE PREPARE fk_stmt;

-- 添加索引以提高查询性能
SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sql_template_version'
    AND INDEX_NAME = 'idx_sql_template_version_datasource_id'
);

SET @index_sql = IF(@index_exists = 0 AND @column_exists = 0,
    'CREATE INDEX idx_sql_template_version_datasource_id ON sql_template_version(datasource_id)',
    'SELECT "Index already exists for sql_template_version" as message'
);

PREPARE index_stmt FROM @index_sql;
EXECUTE index_stmt;
DEALLOCATE PREPARE index_stmt;

-- 从主模板表同步数据源ID到版本表（可选的数据迁移）
SET @sync_sql = IF(@column_exists = 0,
    'UPDATE sql_template_version v 
     INNER JOIN sql_template t ON v.template_id = t.template_id 
     SET v.datasource_id = t.datasource_id 
     WHERE v.datasource_id IS NULL AND t.datasource_id IS NOT NULL',
    'SELECT "Data sync skipped - column already exists" as message'
);

PREPARE sync_stmt FROM @sync_sql;
EXECUTE sync_stmt;
DEALLOCATE PREPARE sync_stmt;

-- 验证字段添加结果
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'sql_template_version'
AND COLUMN_NAME = 'datasource_id';

-- 验证数据同步结果
SELECT 
    COUNT(*) as total_versions,
    COUNT(datasource_id) as versions_with_datasource,
    COUNT(*) - COUNT(datasource_id) as versions_without_datasource
FROM sql_template_version;
