-- 生产环境安全迁移脚本（带字段检查）
-- 兼容 MySQL 5.7+ 所有版本
-- 可以重复执行，不会报错
-- 执行日期: 2025-01-09

USE photography_system;

-- 设置变量检查字段是否存在
SET @audit_status_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'photography_system' 
      AND TABLE_NAME = 'checkin_records' 
      AND COLUMN_NAME = 'audit_status'
);

SET @audited_by_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'photography_system' 
      AND TABLE_NAME = 'checkin_records' 
      AND COLUMN_NAME = 'audited_by'
);

SET @audit_time_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'photography_system' 
      AND TABLE_NAME = 'checkin_records' 
      AND COLUMN_NAME = 'audit_time'
);

SET @audit_notes_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'photography_system' 
      AND TABLE_NAME = 'checkin_records' 
      AND COLUMN_NAME = 'audit_notes'
);

-- 1. 添加 audit_status 字段
SET @sql1 = IF(@audit_status_exists = 0,
    'ALTER TABLE checkin_records ADD COLUMN audit_status VARCHAR(20) DEFAULT ''NOT_REQUIRED'' COMMENT ''审核状态：NOT_REQUIRED=无需审核, PENDING=待审核, APPROVED=已通过, REJECTED=已拒绝''',
    'SELECT ''⚠️ audit_status 字段已存在，跳过'' AS msg'
);
PREPARE stmt1 FROM @sql1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- 2. 添加 audited_by 字段
SET @sql2 = IF(@audited_by_exists = 0,
    'ALTER TABLE checkin_records ADD COLUMN audited_by BIGINT COMMENT ''审核人ID''',
    'SELECT ''⚠️ audited_by 字段已存在，跳过'' AS msg'
);
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- 3. 添加 audit_time 字段
SET @sql3 = IF(@audit_time_exists = 0,
    'ALTER TABLE checkin_records ADD COLUMN audit_time DATETIME COMMENT ''审核时间''',
    'SELECT ''⚠️ audit_time 字段已存在，跳过'' AS msg'
);
PREPARE stmt3 FROM @sql3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- 4. 添加 audit_notes 字段
SET @sql4 = IF(@audit_notes_exists = 0,
    'ALTER TABLE checkin_records ADD COLUMN audit_notes VARCHAR(500) COMMENT ''审核备注''',
    'SELECT ''⚠️ audit_notes 字段已存在，跳过'' AS msg'
);
PREPARE stmt4 FROM @sql4;
EXECUTE stmt4;
DEALLOCATE PREPARE stmt4;

-- 5. 添加外键约束（检查是否存在）
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'photography_system' 
      AND TABLE_NAME = 'checkin_records' 
      AND CONSTRAINT_NAME = 'fk_checkin_records_audited_by'
);

SET @sql5 = IF(@fk_exists = 0,
    'ALTER TABLE checkin_records ADD CONSTRAINT fk_checkin_records_audited_by FOREIGN KEY (audited_by) REFERENCES users(id) ON DELETE SET NULL',
    'SELECT ''⚠️ 外键约束已存在，跳过'' AS msg'
);
PREPARE stmt5 FROM @sql5;
EXECUTE stmt5;
DEALLOCATE PREPARE stmt5;

-- 6. 添加索引（检查是否存在）
SET @idx_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = 'photography_system' 
      AND TABLE_NAME = 'checkin_records' 
      AND INDEX_NAME = 'idx_checkin_records_audit_status'
);

SET @sql6 = IF(@idx_exists = 0,
    'CREATE INDEX idx_checkin_records_audit_status ON checkin_records(audit_status)',
    'SELECT ''⚠️ 索引已存在，跳过'' AS msg'
);
PREPARE stmt6 FROM @sql6;
EXECUTE stmt6;
DEALLOCATE PREPARE stmt6;

-- 验证结果
SELECT '✅ 开始验证...' AS status;

-- 验证字段
SELECT 
    '✅ 字段验证' AS check_type,
    COLUMN_NAME AS field_name,
    COLUMN_TYPE AS field_type,
    COLUMN_DEFAULT AS default_value,
    COLUMN_COMMENT AS comment_text
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'photography_system'
  AND TABLE_NAME = 'checkin_records'
  AND COLUMN_NAME IN ('audit_status', 'audited_by', 'audit_time', 'audit_notes')
ORDER BY ORDINAL_POSITION;

-- 验证外键
SELECT 
    '✅ 外键验证' AS check_type,
    CONSTRAINT_NAME AS constraint_name,
    COLUMN_NAME AS column_name,
    REFERENCED_TABLE_NAME AS ref_table,
    REFERENCED_COLUMN_NAME AS ref_column
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'photography_system'
  AND TABLE_NAME = 'checkin_records'
  AND CONSTRAINT_NAME = 'fk_checkin_records_audited_by';

-- 验证索引
SELECT 
    '✅ 索引验证' AS check_type,
    INDEX_NAME AS index_name,
    COLUMN_NAME AS column_name,
    SEQ_IN_INDEX AS sequence
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'photography_system'
  AND TABLE_NAME = 'checkin_records'
  AND INDEX_NAME = 'idx_checkin_records_audit_status';

SELECT '✅ 审核字段迁移脚本执行完成！' AS final_status;

