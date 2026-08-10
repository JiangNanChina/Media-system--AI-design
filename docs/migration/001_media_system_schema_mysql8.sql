-- MySQL 8.x, repeatable schema/data migration for the campus media system.
-- Back up the database and both upload directories before running this file.

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS failed_login_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS locked_until DATETIME NULL,
    ADD COLUMN IF NOT EXISTS token_version INT NOT NULL DEFAULT 0;

UPDATE users SET role = 'SUPER_ADMIN' WHERE role = 'ADMIN';
UPDATE users SET account_status = 'ACTIVE' WHERE account_status IS NULL OR account_status = '';

ALTER TABLE borrow_records
    ADD COLUMN IF NOT EXISTS borrower_type VARCHAR(20) NOT NULL DEFAULT 'INTERNAL',
    ADD COLUMN IF NOT EXISTS external_borrower_type VARCHAR(20) NULL,
    ADD COLUMN IF NOT EXISTS external_organization VARCHAR(150) NULL,
    ADD COLUMN IF NOT EXISTS external_contact_name VARCHAR(80) NULL,
    ADD COLUMN IF NOT EXISTS external_phone VARCHAR(20) NULL,
    ADD COLUMN IF NOT EXISTS external_email VARCHAR(120) NULL;

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    replaced_by_hash VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT NOT NULL DEFAULT 0,
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS landing_content_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    section_type VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    summary TEXT NULL,
    media_url VARCHAR(500) NULL,
    link_url VARCHAR(500) NULL,
    published BIT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS media_submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_number VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    description TEXT NULL,
    submitter_name VARCHAR(80) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    qq_email VARCHAR(120) NOT NULL,
    organization VARCHAR(160) NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewer_id BIGINT NULL,
    review_feedback VARCHAR(1000) NULL,
    reviewed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT NOT NULL DEFAULT 0,
    CONSTRAINT uk_submission_number UNIQUE (submission_number),
    KEY idx_submission_status (status),
    CONSTRAINT fk_submission_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS checkin_device_usages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    configuration_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    usage_date DATE NOT NULL,
    device_fingerprint_hash VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT NOT NULL DEFAULT 0,
    CONSTRAINT uk_checkin_device_slot UNIQUE (configuration_id, usage_date, device_fingerprint_hash),
    CONSTRAINT fk_usage_configuration FOREIGN KEY (configuration_id) REFERENCES checkin_configurations(id),
    CONSTRAINT fk_usage_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO departments (name, description, created_at, updated_at, deleted)
SELECT '摄影部', '摄影摄像与器材管理', NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = '摄影部' AND deleted = 0);
INSERT INTO departments (name, description, created_at, updated_at, deleted)
SELECT '审核部', '内容审核与质量管理', NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = '审核部' AND deleted = 0);
INSERT INTO departments (name, description, created_at, updated_at, deleted)
SELECT '运营部', '平台运营与内容发布', NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = '运营部' AND deleted = 0);

-- Legacy plaintext SMTP authorization codes are intentionally discarded.
UPDATE site_configs
SET config_value = '', updated_at = NOW()
WHERE config_key = 'mail.qq_auth_code';
UPDATE site_configs
SET config_value = 'false', updated_at = NOW()
WHERE config_key = 'mail.enabled';

