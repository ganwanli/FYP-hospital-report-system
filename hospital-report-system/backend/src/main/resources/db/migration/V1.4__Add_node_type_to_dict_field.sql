-- 添加node_type字段到dict_field表
-- 创建时间：2025-01-20

-- 添加node_type字段
ALTER TABLE dict_field 
ADD COLUMN node_type VARCHAR(20) DEFAULT 'field' COMMENT '节点类型（field/category）' 
AFTER field_type;

-- 更新现有数据，设置默认值为'field'
UPDATE dict_field SET node_type = 'field' WHERE node_type IS NULL;

-- 添加索引
CREATE INDEX idx_node_type ON dict_field (node_type);
