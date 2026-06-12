-- 安全的审核字段迁移脚本（自动检查字段是否已存在）
-- 执行日期: 2025-01-09
-- 说明：此脚本可以重复执行，不会报错

-- 使用存储过程来安全地添加字段
DELIMITER $$

-- 1. 添加 audit_status 字段
DROP PROCEDURE IF EXISTS add_audit_status_column$$
CREATE PROCEDURE add_audit_status_column()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'checkin_records'
        AND COLUMN_NAME = 'audit_status'
    ) THEN
        ALTER TABLE checkin_records
        ADD COLUMN audit_status VARCHAR(20) DEFAULT 'NOT_REQUIRED' 
        COMMENT '审核状态：NOT_REQUIRED=无需审核, PENDING=待审核, APPROVED=已通过, REJECTED=已拒绝';
        SELECT 'audit_status 字段添加成功' AS result;
    ELSE
        SELECT 'audit_status 字段已存在，跳过' AS result;
    END IF;
END$$

-- 2. 添加 audited_by 字段
DROP PROCEDURE IF EXISTS add_audited_by_column$$
CREATE PROCEDURE add_audited_by_column()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'checkin_records'
        AND COLUMN_NAME = 'audited_by'
    ) THEN
        ALTER TABLE checkin_records
        ADD COLUMN audited_by BIGINT COMMENT '审核人ID';
        SELECT 'audited_by 字段添加成功' AS result;
    ELSE
        SELECT 'audited_by 字段已存在，跳过' AS result;
    END IF;
END$$

-- 3. 添加 audit_time 字段
DROP PROCEDURE IF EXISTS add_audit_time_column$$
CREATE PROCEDURE add_audit_time_column()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'checkin_records'
        AND COLUMN_NAME = 'audit_time'
    ) THEN
        ALTER TABLE checkin_records
        ADD COLUMN audit_time DATETIME COMMENT '审核时间';
        SELECT 'audit_time 字段添加成功' AS result;
    ELSE
        SELECT 'audit_time 字段已存在，跳过' AS result;
    END IF;
END$$

-- 4. 添加 audit_notes 字段
DROP PROCEDURE IF EXISTS add_audit_notes_column$$
CREATE PROCEDURE add_audit_notes_column()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'checkin_records'
        AND COLUMN_NAME = 'audit_notes'
    ) THEN
        ALTER TABLE checkin_records
        ADD COLUMN audit_notes VARCHAR(500) COMMENT '审核备注';
        SELECT 'audit_notes 字段添加成功' AS result;
    ELSE
        SELECT 'audit_notes 字段已存在，跳过' AS result;
    END IF;
END$$

-- 5. 添加外键约束
DROP PROCEDURE IF EXISTS add_audit_foreign_key$$
CREATE PROCEDURE add_audit_foreign_key()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'checkin_records'
        AND CONSTRAINT_NAME = 'fk_checkin_records_audited_by'
    ) THEN
        ALTER TABLE checkin_records
        ADD CONSTRAINT fk_checkin_records_audited_by 
        FOREIGN KEY (audited_by) REFERENCES users(id) ON DELETE SET NULL;
        SELECT '外键约束添加成功' AS result;
    ELSE
        SELECT '外键约束已存在，跳过' AS result;
    END IF;
END$$

-- 6. 添加索引
DROP PROCEDURE IF EXISTS add_audit_status_index$$
CREATE PROCEDURE add_audit_status_index()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'checkin_records'
        AND INDEX_NAME = 'idx_checkin_records_audit_status'
    ) THEN
        CREATE INDEX idx_checkin_records_audit_status ON checkin_records(audit_status);
        SELECT '索引添加成功' AS result;
    ELSE
        SELECT '索引已存在，跳过' AS result;
    END IF;
END$$

DELIMITER ;

-- 执行所有存储过程
CALL add_audit_status_column();
CALL add_audited_by_column();
CALL add_audit_time_column();
CALL add_audit_notes_column();
CALL add_audit_foreign_key();
CALL add_audit_status_index();

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_audit_status_column;
DROP PROCEDURE IF EXISTS add_audited_by_column;
DROP PROCEDURE IF EXISTS add_audit_time_column;
DROP PROCEDURE IF EXISTS add_audit_notes_column;
DROP PROCEDURE IF EXISTS add_audit_foreign_key;
DROP PROCEDURE IF EXISTS add_audit_status_index;

-- 验证字段是否都已存在
SELECT 
    '验证结果' AS check_type,
    COLUMN_NAME, 
    COLUMN_TYPE, 
    COLUMN_DEFAULT, 
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'checkin_records'
  AND COLUMN_NAME IN ('audit_status', 'audited_by', 'audit_time', 'audit_notes')
ORDER BY ORDINAL_POSITION;

-- 验证外键约束
SELECT 
    '外键验证' AS check_type,
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'checkin_records'
  AND CONSTRAINT_NAME = 'fk_checkin_records_audited_by';

-- 验证索引
SELECT 
    '索引验证' AS check_type,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'checkin_records'
  AND INDEX_NAME = 'idx_checkin_records_audit_status';

-- 统计数据
SELECT 
    '数据统计' AS check_type,
    COUNT(*) AS total_records,
    SUM(CASE WHEN audit_status = 'NOT_REQUIRED' THEN 1 ELSE 0 END) AS not_required_count,
    SUM(CASE WHEN audit_status = 'PENDING' THEN 1 ELSE 0 END) AS pending_count,
    SUM(CASE WHEN audit_status = 'APPROVED' THEN 1 ELSE 0 END) AS approved_count,
    SUM(CASE WHEN audit_status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected_count
FROM checkin_records
WHERE deleted = 0;

SELECT '✅ 审核字段迁移脚本执行完成！' AS status;

