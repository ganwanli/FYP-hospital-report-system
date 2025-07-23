-- 为 sql_template 表添加 datasource_id 字段
-- 执行时间: 2024-01-XX

-- 检查字段是否已存在，如果不存在则添加
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sql_template'
    AND COLUMN_NAME = 'datasource_id'
);

-- 如果字段不存在，则添加字段
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE sql_template ADD COLUMN datasource_id BIGINT NULL COMMENT "关联的数据源ID" AFTER tags',
    'SELECT "Column datasource_id already exists" as message'
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
    'ALTER TABLE sql_template ADD CONSTRAINT fk_sql_template_datasource FOREIGN KEY (datasource_id) REFERENCES datasource(id) ON DELETE SET NULL ON UPDATE CASCADE',
    'SELECT "Datasource table does not exist or foreign key already exists" as message'
);

PREPARE fk_stmt FROM @fk_sql;
EXECUTE fk_stmt;
DEALLOCATE PREPARE fk_stmt;

-- 添加索引以提高查询性能
SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sql_template'
    AND INDEX_NAME = 'idx_sql_template_datasource_id'
);

SET @index_sql = IF(@index_exists = 0 AND @column_exists = 0,
    'CREATE INDEX idx_sql_template_datasource_id ON sql_template(datasource_id)',
    'SELECT "Index already exists" as message'
);

PREPARE index_stmt FROM @index_sql;
EXECUTE index_stmt;
DEALLOCATE PREPARE index_stmt;

-- 验证字段添加结果
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'sql_template'
AND COLUMN_NAME = 'datasource_id';
