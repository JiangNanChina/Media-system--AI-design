-- 修复设备表 damaged_quantity 字段默认值
-- 执行日期: 2025-01-17
-- 说明: 为 damaged_quantity 字段添加默认值 0，解决添加新设备时的 NOT NULL 约束错误

USE photography_system;

-- 1. 先更新现有的 NULL 值为 0（如果有的话）
UPDATE equipment 
SET damaged_quantity = 0 
WHERE damaged_quantity IS NULL;

-- 2. 修改字段，添加默认值
ALTER TABLE equipment 
MODIFY COLUMN damaged_quantity INT NOT NULL DEFAULT 0 COMMENT '损坏数量';

-- 3. 验证修改
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'photography_system' 
  AND TABLE_NAME = 'equipment' 
  AND COLUMN_NAME = 'damaged_quantity';

-- 预期结果:
-- COLUMN_NAME: damaged_quantity
-- COLUMN_TYPE: int
-- IS_NULLABLE: NO
-- COLUMN_DEFAULT: 0
-- COLUMN_COMMENT: 损坏数量

