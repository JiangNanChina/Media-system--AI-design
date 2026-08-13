-- Add configurable retention for QQ mail logs and verification code records.

INSERT INTO site_configs (config_key, config_value, description, config_type, enabled, sort_order, deleted, created_at, updated_at)
VALUES ('mail.log_retention_days', '30', '邮件发送日志与验证码记录保留天数', 'NUMBER', 1, 1009, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    config_type = VALUES(config_type),
    enabled = VALUES(enabled),
    sort_order = VALUES(sort_order),
    deleted = 0,
    updated_at = NOW();
