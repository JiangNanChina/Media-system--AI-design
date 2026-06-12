-- 添加管理员审核相关字段到 checkin_records 表
-- 执行日期: 2025-01-09

-- 1. 添加审核状态字段
ALTER TABLE checkin_records
ADD COLUMN audit_status VARCHAR(20) DEFAULT 'NOT_REQUIRED' COMMENT '审核状态：NOT_REQUIRED=无需审核, PENDING=待审核, APPROVED=已通过, REJECTED=已拒绝';

-- 2. 添加审核人ID字段
ALTER TABLE checkin_records
ADD COLUMN audited_by BIGINT COMMENT '审核人ID',
ADD CONSTRAINT fk_checkin_records_audited_by FOREIGN KEY (audited_by) REFERENCES users(id) ON DELETE SET NULL;

-- 3. 添加审核时间字段
ALTER TABLE checkin_records
ADD COLUMN audit_time DATETIME COMMENT '审核时间';

-- 4. 添加审核备注字段
ALTER TABLE checkin_records
ADD COLUMN audit_notes VARCHAR(500) COMMENT '审核备注';

-- 5. 为审核状态字段添加索引（提高查询效率）
CREATE INDEX idx_checkin_records_audit_status ON checkin_records(audit_status);

-- 验证字段是否添加成功
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'checkin_records'
  AND COLUMN_NAME IN ('audit_status', 'audited_by', 'audit_time', 'audit_notes');

-- 查询待审核记录数量（测试）
SELECT COUNT(*) AS pending_count
FROM checkin_records
WHERE audit_status = 'PENDING' AND deleted = 0;

-- 查看所有审核状态的分布（测试）
SELECT audit_status, COUNT(*) AS count
FROM checkin_records
WHERE deleted = 0
GROUP BY audit_status;

