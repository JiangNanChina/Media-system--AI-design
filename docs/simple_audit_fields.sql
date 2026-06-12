-- 简化版审核字段迁移脚本（直接添加，不检查）
-- 执行日期: 2025-01-09
-- 说明：如果字段已存在会报错，可以忽略

-- ⚠️ 重要：首先选择数据库
USE photography_system;

-- 添加审核字段
ALTER TABLE checkin_records
    ADD COLUMN IF NOT EXISTS audit_status VARCHAR(20) DEFAULT 'NOT_REQUIRED' 
        COMMENT '审核状态：NOT_REQUIRED=无需审核, PENDING=待审核, APPROVED=已通过, REJECTED=已拒绝',
    ADD COLUMN IF NOT EXISTS audited_by BIGINT 
        COMMENT '审核人ID',
    ADD COLUMN IF NOT EXISTS audit_time DATETIME 
        COMMENT '审核时间',
    ADD COLUMN IF NOT EXISTS audit_notes VARCHAR(500) 
        COMMENT '审核备注';

-- 添加外键约束（如果已存在会报错，可以忽略）
ALTER TABLE checkin_records
    ADD CONSTRAINT fk_checkin_records_audited_by
        FOREIGN KEY (audited_by) REFERENCES users(id) ON DELETE SET NULL;

-- 添加索引（如果已存在会报错，可以忽略）
CREATE INDEX idx_checkin_records_audit_status ON checkin_records(audit_status);

-- 验证结果
SELECT '✅ 字段添加完成，正在验证...' AS status;

-- 查看新增的字段
SELECT
    COLUMN_NAME AS '字段名',
    COLUMN_TYPE AS '类型',
    COLUMN_DEFAULT AS '默认值',
    COLUMN_COMMENT AS '说明'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'photography_system'
  AND TABLE_NAME = 'checkin_records'
  AND COLUMN_NAME IN ('audit_status', 'audited_by', 'audit_time', 'audit_notes');

SELECT '✅ 审核字段迁移完成！' AS result;

