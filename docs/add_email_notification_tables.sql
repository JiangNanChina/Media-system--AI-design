-- QQ email verification and reminder support.
-- Run this on environments where Hibernate ddl-auto is not allowed to update schema.

CREATE TABLE IF NOT EXISTS email_verification_codes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    email VARCHAR(120) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    code_hash VARCHAR(120) NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_email_code_email_purpose (email, purpose),
    INDEX idx_email_code_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS email_notification_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    notification_type VARCHAR(50) NOT NULL,
    business_type VARCHAR(50) NOT NULL,
    business_id BIGINT NOT NULL,
    recipient_email VARCHAR(120) NOT NULL,
    recipient_name VARCHAR(100) NULL,
    period_key VARCHAR(100) NOT NULL,
    success TINYINT(1) NOT NULL DEFAULT 0,
    error_message VARCHAR(1000) NULL,
    sent_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_email_notification_dedupe (notification_type, business_id, recipient_email, period_key),
    INDEX idx_email_notification_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO site_configs (config_key, config_value, description, config_type, enabled, sort_order, deleted, created_at, updated_at)
VALUES
('mail.enabled', 'false', '是否启用QQ邮箱验证码与提醒', 'BOOLEAN', 1, 1000, 0, NOW(), NOW()),
('mail.smtp_host', 'smtp.qq.com', 'QQ邮箱SMTP服务器', 'TEXT', 1, 1001, 0, NOW(), NOW()),
('mail.smtp_port', '465', 'QQ邮箱SMTP端口', 'NUMBER', 1, 1002, 0, NOW(), NOW()),
('mail.smtp_ssl_enabled', 'true', '是否启用SMTP SSL', 'BOOLEAN', 1, 1003, 0, NOW(), NOW()),
('mail.qq_account', '', 'QQ邮箱账号', 'TEXT', 1, 1004, 0, NOW(), NOW()),
('mail.qq_auth_code', '', 'QQ邮箱SMTP授权码', 'TEXT', 1, 1005, 0, NOW(), NOW()),
('mail.sender_name', '融媒体管理系统', '邮件发件人名称', 'TEXT', 1, 1006, 0, NOW(), NOW()),
('mail.reminder_advance_minutes', '30', '执勤和晚自习提醒提前分钟数', 'NUMBER', 1, 1007, 0, NOW(), NOW()),
('mail.overdue_reminder_interval_hours', '24', '设备逾期归还提醒间隔小时数', 'NUMBER', 1, 1008, 0, NOW(), NOW()),
('mail.log_retention_days', '30', '邮件发送日志与验证码记录保留天数', 'NUMBER', 1, 1009, 0, NOW(), NOW()),
('mail.duty_reminder_enabled', 'true', '执勤提醒开关', 'BOOLEAN', 1, 1010, 0, NOW(), NOW()),
('mail.checkin_reminder_enabled', 'true', '晚自习打卡提醒开关', 'BOOLEAN', 1, 1011, 0, NOW(), NOW()),
('mail.leave_approval_reminder_enabled', 'true', '请假审批提醒开关', 'BOOLEAN', 1, 1012, 0, NOW(), NOW()),
('mail.borrow_overdue_reminder_enabled', 'true', '设备逾期归还提醒开关', 'BOOLEAN', 1, 1013, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
description = VALUES(description),
config_type = VALUES(config_type),
enabled = VALUES(enabled),
sort_order = VALUES(sort_order),
deleted = 0,
updated_at = NOW();
