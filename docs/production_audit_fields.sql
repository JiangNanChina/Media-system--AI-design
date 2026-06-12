-- 生产环境审核字段迁移脚本
-- 兼容 MySQL 5.7+ 所有版本
-- 执行日期: 2025-01-09

-- 选择数据库
USE photography_system;

-- 方案：直接添加字段，如果已存在会报错但不影响后续执行

-- 1. 添加 audit_status 字段
ALTER TABLE checkin_records
    ADD COLUMN audit_status VARCHAR(20) DEFAULT 'NOT_REQUIRED' 
        COMMENT '审核状态：NOT_REQUIRED=无需审核, PENDING=待审核, APPROVED=已通过, REJECTED=已拒绝';

-- 2. 添加 audited_by 字段
ALTER TABLE checkin_records
    ADD COLUMN audited_by BIGINT 
        COMMENT '审核人ID';

-- 3. 添加 audit_time 字段
ALTER TABLE checkin_records
    ADD COLUMN audit_time DATETIME 
        COMMENT '审核时间';

-- 4. 添加 audit_notes 字段
ALTER TABLE checkin_records
    ADD COLUMN audit_notes VARCHAR(500) 
        COMMENT '审核备注';

-- 5. 添加外键约束
ALTER TABLE checkin_records
    ADD CONSTRAINT fk_checkin_records_audited_by
        FOREIGN KEY (audited_by) REFERENCES users(id) ON DELETE SET NULL;

-- 6. 添加索引
CREATE INDEX idx_checkin_records_audit_status ON checkin_records(audit_status);

-- 验证字段
SELECT 
    COLUMN_NAME AS '字段名',
    COLUMN_TYPE AS '类型',
    IS_NULLABLE AS '可空',
    COLUMN_DEFAULT AS '默认值',
    COLUMN_COMMENT AS '说明'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'photography_system'
  AND TABLE_NAME = 'checkin_records'
  AND COLUMN_NAME IN ('audit_status', 'audited_by', 'audit_time', 'audit_notes')
ORDER BY ORDINAL_POSITION;

-- 验证外键
SELECT 
    CONSTRAINT_NAME AS '约束名',
    COLUMN_NAME AS '字段',
    REFERENCED_TABLE_NAME AS '引用表',
    REFERENCED_COLUMN_NAME AS '引用字段'
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'photography_system'
  AND TABLE_NAME = 'checkin_records'
  AND CONSTRAINT_NAME = 'fk_checkin_records_audited_by';

-- 验证索引
SELECT 
    INDEX_NAME AS '索引名',
    COLUMN_NAME AS '字段',
    SEQ_IN_INDEX AS '顺序'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'photography_system'
  AND TABLE_NAME = 'checkin_records'
  AND INDEX_NAME = 'idx_checkin_records_audit_status';

SELECT '✅ 审核字段迁移完成！' AS status;

