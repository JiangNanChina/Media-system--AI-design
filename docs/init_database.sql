-- Photography 融媒体管理系统 MySQL 8.x 初始化脚本
-- 说明：脚本可重复执行，不会删除已有业务数据；已有 admin 会被重置为指定初始密码。

CREATE DATABASE IF NOT EXISTS photography_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE photography_system;

SET NAMES utf8mb4;
SET time_zone = '+08:00';

-- ============================================================
-- 基础组织与用户
-- ============================================================

CREATE TABLE IF NOT EXISTS departments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_departments_name_deleted (name, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门';

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt 密码哈希',
    real_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NULL,
    phone VARCHAR(20) NULL,
    avatar_url VARCHAR(500) NULL,
    role ENUM('MEMBER','MINISTER','DIRECTOR','ADVISOR','SUPER_ADMIN','ADMIN') NOT NULL DEFAULT 'MEMBER',
    enabled BIT(1) NOT NULL DEFAULT b'1',
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,
    token_version INT NOT NULL DEFAULT 0,
    department_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username_deleted (username, deleted),
    KEY idx_users_department_id (department_id),
    KEY idx_users_role (role),
    KEY idx_users_account_status (account_status),
    CONSTRAINT fk_users_department
        FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户';

-- ============================================================
-- 设备与借用
-- ============================================================

CREATE TABLE IF NOT EXISTS equipment_categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_active BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_equipment_categories_name_deleted (name, deleted),
    KEY idx_equipment_categories_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备分类';

CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category_id BIGINT NULL,
    category_name VARCHAR(50) NULL,
    category VARCHAR(50) NULL COMMENT '兼容旧分类字段',
    serial_number VARCHAR(50) NOT NULL,
    description VARCHAR(1000) NULL,
    image_url VARCHAR(500) NULL,
    image_urls VARCHAR(2000) NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    available_quantity INT NOT NULL DEFAULT 0,
    damaged_quantity INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NULL DEFAULT '正常',
    specifications VARCHAR(2000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_equipment_serial_deleted (serial_number, deleted),
    KEY idx_equipment_category_id (category_id),
    KEY idx_equipment_status (status),
    CONSTRAINT fk_equipment_category
        FOREIGN KEY (category_id) REFERENCES equipment_categories (id) ON DELETE SET NULL,
    CONSTRAINT chk_equipment_quantities
        CHECK (stock_quantity >= 0 AND available_quantity >= 0 AND damaged_quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备';

CREATE TABLE IF NOT EXISTS borrow_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    borrower_type ENUM('INTERNAL','EXTERNAL') NOT NULL DEFAULT 'INTERNAL',
    external_borrower_type ENUM('COLLEGE','DEPARTMENT','TEACHER') NULL,
    external_organization VARCHAR(150) NULL,
    external_contact_name VARCHAR(80) NULL,
    external_phone VARCHAR(20) NULL,
    external_email VARCHAR(120) NULL,
    user_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    expected_return_time DATETIME NOT NULL,
    actual_return_time DATETIME NULL,
    status ENUM('PENDING','APPROVED','REJECTED','BORROWED','RETURNED','OVERDUE') NOT NULL DEFAULT 'PENDING',
    borrow_reason VARCHAR(500) NULL,
    approval_notes VARCHAR(500) NULL,
    approved_by BIGINT NULL,
    approval_time DATETIME NULL,
    return_notes VARCHAR(500) NULL,
    damage_description VARCHAR(500) NULL,
    return_images VARCHAR(2000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_borrow_user_id (user_id),
    KEY idx_borrow_equipment_id (equipment_id),
    KEY idx_borrow_approved_by (approved_by),
    KEY idx_borrow_status (status),
    KEY idx_borrow_expected_return_time (expected_return_time),
    CONSTRAINT fk_borrow_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_borrow_equipment
        FOREIGN KEY (equipment_id) REFERENCES equipment (id),
    CONSTRAINT fk_borrow_approved_by
        FOREIGN KEY (approved_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_borrow_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备借用记录';

-- ============================================================
-- 公告与站点内容
-- ============================================================

CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    published BIT(1) NOT NULL DEFAULT b'0',
    type ENUM('SYSTEM','IMPORTANT','GENERAL','ACTIVITY') NOT NULL DEFAULT 'GENERAL',
    priority INT NOT NULL DEFAULT 0,
    view_count BIGINT NOT NULL DEFAULT 0,
    publish_time DATETIME NULL,
    archived BIT(1) NOT NULL DEFAULT b'0',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_announcements_author_id (author_id),
    KEY idx_announcements_published (published, archived),
    KEY idx_announcements_type_priority (type, priority),
    CONSTRAINT fk_announcements_author
        FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告';

CREATE TABLE IF NOT EXISTS site_configs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT NULL,
    description VARCHAR(500) NULL,
    config_type ENUM('TEXT','IMAGE','COLOR','NUMBER','BOOLEAN','JSON') NOT NULL,
    enabled BIT(1) NOT NULL DEFAULT b'1',
    sort_order INT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_site_configs_key_deleted (config_key, deleted),
    KEY idx_site_configs_type (config_type),
    KEY idx_site_configs_enabled_sort (enabled, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站点配置';

CREATE TABLE IF NOT EXISTS landing_content_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    section_type ENUM('CAMPUS_FEATURE','DEPARTMENT_SHOWCASE') NOT NULL,
    title VARCHAR(160) NOT NULL,
    summary TEXT NULL,
    media_url VARCHAR(500) NULL,
    link_url VARCHAR(500) NULL,
    published BIT(1) NOT NULL DEFAULT b'1',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_landing_section_sort (section_type, sort_order),
    KEY idx_landing_published (published, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首页内容';

CREATE TABLE IF NOT EXISTS media_submissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
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
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    reviewer_id BIGINT NULL,
    review_feedback VARCHAR(1000) NULL,
    reviewed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_media_submissions_number (submission_number),
    KEY idx_media_submissions_status (status),
    KEY idx_media_submissions_reviewer_id (reviewer_id),
    CONSTRAINT fk_media_submissions_reviewer
        FOREIGN KEY (reviewer_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='媒体投稿';

-- ============================================================
-- 打卡
-- ============================================================

CREATE TABLE IF NOT EXISTS checkin_configurations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    location_name VARCHAR(100) NOT NULL,
    location_address VARCHAR(200) NULL,
    location_description VARCHAR(500) NULL,
    longitude DOUBLE NULL,
    latitude DOUBLE NULL,
    session_name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    session_description VARCHAR(500) NULL,
    required_weekdays VARCHAR(20) NOT NULL DEFAULT '1,2,3,4',
    is_active BIT(1) NOT NULL DEFAULT b'1',
    sort_order INT NULL DEFAULT 0,
    early_checkin_minutes INT NULL DEFAULT 0,
    late_checkin_minutes INT NULL DEFAULT 0,
    qr_code VARCHAR(1000) NULL,
    wifi_ssid VARCHAR(100) NULL,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_checkin_config_active_sort (is_active, sort_order),
    KEY idx_checkin_config_created_by (created_by),
    CONSTRAINT fk_checkin_config_created_by
        FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡配置';

CREATE TABLE IF NOT EXISTS checkin_config_users (
    config_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (config_id, user_id),
    KEY idx_checkin_config_users_user_id (user_id),
    CONSTRAINT fk_checkin_config_users_config
        FOREIGN KEY (config_id) REFERENCES checkin_configurations (id) ON DELETE CASCADE,
    CONSTRAINT fk_checkin_config_users_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡配置用户关联';

CREATE TABLE IF NOT EXISTS checkin_device_usages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    configuration_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    usage_date DATE NOT NULL,
    device_fingerprint_hash VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_checkin_device_slot (configuration_id, usage_date, device_fingerprint_hash),
    KEY idx_checkin_device_usage_user_id (user_id),
    CONSTRAINT fk_checkin_device_usage_config
        FOREIGN KEY (configuration_id) REFERENCES checkin_configurations (id) ON DELETE CASCADE,
    CONSTRAINT fk_checkin_device_usage_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡设备使用记录';

CREATE TABLE IF NOT EXISTS checkin_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    configuration_id BIGINT NOT NULL,
    checkin_time DATETIME NOT NULL,
    checkout_time DATETIME NULL,
    checkin_latitude DECIMAL(10,7) NULL,
    checkin_longitude DECIMAL(11,7) NULL,
    checkout_latitude DECIMAL(10,7) NULL,
    checkout_longitude DECIMAL(11,7) NULL,
    checkin_address VARCHAR(500) NULL,
    checkout_address VARCHAR(500) NULL,
    status ENUM('NORMAL','LATE','EARLY_LEAVE','ABSENT','MAKEUP','LEAVE') NOT NULL DEFAULT 'NORMAL',
    is_late BIT(1) NOT NULL DEFAULT b'0',
    late_minutes INT NULL DEFAULT 0,
    duration_minutes INT NULL,
    checkin_method VARCHAR(20) NULL DEFAULT 'GPS',
    device_info VARCHAR(1000) NULL,
    checkin_photo VARCHAR(500) NULL,
    checkout_photo VARCHAR(500) NULL,
    notes VARCHAR(1000) NULL,
    ip_address VARCHAR(50) NULL,
    user_agent VARCHAR(500) NULL,
    audit_status ENUM('NOT_REQUIRED','PENDING','APPROVED','REJECTED') NULL DEFAULT 'NOT_REQUIRED',
    audited_by BIGINT NULL,
    audit_time DATETIME NULL,
    audit_notes VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_checkin_records_user_time (user_id, checkin_time),
    KEY idx_checkin_records_configuration_id (configuration_id),
    KEY idx_checkin_records_audit_status (audit_status),
    KEY idx_checkin_records_audited_by (audited_by),
    CONSTRAINT fk_checkin_records_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_checkin_records_configuration
        FOREIGN KEY (configuration_id) REFERENCES checkin_configurations (id),
    CONSTRAINT fk_checkin_records_audited_by
        FOREIGN KEY (audited_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡记录';

-- ============================================================
-- 执勤与请假
-- ============================================================

CREATE TABLE IF NOT EXISTS duty_schedules (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    day_of_week INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BIT(1) NOT NULL DEFAULT b'1',
    notes VARCHAR(500) NULL,
    early_checkin_minutes INT NOT NULL DEFAULT 30,
    late_checkin_minutes INT NOT NULL DEFAULT 15,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_duty_schedule_slot (user_id, day_of_week, start_time),
    CONSTRAINT fk_duty_schedules_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_duty_day_of_week CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_duty_time_range CHECK (start_time < end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='执勤排班';

CREATE TABLE IF NOT EXISTS duty_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    duty_schedule_id BIGINT NOT NULL,
    duty_date DATE NOT NULL,
    checkin_time DATETIME NULL,
    checkout_time DATETIME NULL,
    actual_start_time DATETIME NULL,
    actual_end_time DATETIME NULL,
    status VARCHAR(20) NULL DEFAULT '待执勤',
    notes VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_duty_records_user_date (user_id, duty_date),
    KEY idx_duty_records_schedule_id (duty_schedule_id),
    CONSTRAINT fk_duty_records_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_duty_records_schedule
        FOREIGN KEY (duty_schedule_id) REFERENCES duty_schedules (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='执勤记录';

CREATE TABLE IF NOT EXISTS duty_swap_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    requester_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    requester_schedule_id BIGINT NOT NULL,
    target_schedule_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason VARCHAR(500) NULL,
    response_reason VARCHAR(500) NULL,
    swap_date DATE NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_duty_swap_requester_id (requester_id),
    KEY idx_duty_swap_target_user_id (target_user_id),
    KEY idx_duty_swap_requester_schedule_id (requester_schedule_id),
    KEY idx_duty_swap_target_schedule_id (target_schedule_id),
    KEY idx_duty_swap_status (status),
    CONSTRAINT fk_duty_swap_requester
        FOREIGN KEY (requester_id) REFERENCES users (id),
    CONSTRAINT fk_duty_swap_target_user
        FOREIGN KEY (target_user_id) REFERENCES users (id),
    CONSTRAINT fk_duty_swap_requester_schedule
        FOREIGN KEY (requester_schedule_id) REFERENCES duty_schedules (id),
    CONSTRAINT fk_duty_swap_target_schedule
        FOREIGN KEY (target_schedule_id) REFERENCES duty_schedules (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='执勤调换申请';

CREATE TABLE IF NOT EXISTS leave_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    leave_type ENUM('DUTY_LEAVE','CHECKIN_LEAVE','OTHER') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    attachment_urls VARCHAR(2000) NULL,
    status ENUM('PENDING','APPROVED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    apply_time DATETIME NOT NULL,
    approve_time DATETIME NULL,
    approver_id BIGINT NULL,
    approve_notes VARCHAR(1000) NULL,
    days_count INT NULL,
    emergency BIT(1) NOT NULL DEFAULT b'0',
    contact_phone VARCHAR(20) NULL,
    contact_person VARCHAR(100) NULL,
    checkin_configuration_id BIGINT NULL,
    duty_schedule_ids VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_leave_requests_user_dates (user_id, start_date, end_date),
    KEY idx_leave_requests_approver_id (approver_id),
    KEY idx_leave_requests_status (status),
    CONSTRAINT fk_leave_requests_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_leave_requests_approver
        FOREIGN KEY (approver_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_leave_date_range CHECK (start_date <= end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假申请';

-- ============================================================
-- 安全、设备审计与令牌
-- ============================================================

CREATE TABLE IF NOT EXISTS user_devices (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    device_fingerprint VARCHAR(500) NOT NULL,
    device_name VARCHAR(200) NULL,
    device_type ENUM('MOBILE','TABLET','DESKTOP','UNKNOWN') NOT NULL,
    os_info VARCHAR(200) NULL,
    browser_info VARCHAR(200) NULL,
    screen_resolution VARCHAR(50) NULL,
    timezone VARCHAR(50) NULL,
    language VARCHAR(50) NULL,
    ip_address VARCHAR(45) NULL,
    is_active BIT(1) NOT NULL DEFAULT b'1',
    first_bound_at DATETIME NOT NULL,
    last_active_at DATETIME NULL,
    bind_status ENUM('ACTIVE','SUSPENDED','REVOKED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_user_devices_user_id (user_id),
    KEY idx_user_devices_fingerprint (device_fingerprint(191)),
    KEY idx_user_devices_active (is_active, bind_status),
    KEY idx_user_devices_last_active_at (last_active_at),
    CONSTRAINT fk_user_devices_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设备';

CREATE TABLE IF NOT EXISTS device_audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    device_id BIGINT NULL,
    device_fingerprint VARCHAR(255) NOT NULL,
    device_name VARCHAR(255) NULL,
    device_type ENUM('MOBILE','TABLET','DESKTOP','UNKNOWN') NOT NULL,
    action_type ENUM(
        'DEVICE_CREATED','DEVICE_ACTIVATED','DEVICE_DEACTIVATED','DEVICE_REACTIVATED',
        'DEVICE_DELETED','LOGIN_SUCCESS','LOGIN_FAILED','SUSPICIOUS_ACTIVITY'
    ) NOT NULL,
    action_description VARCHAR(500) NULL,
    ip_address VARCHAR(255) NULL,
    user_agent VARCHAR(1000) NULL,
    action_time DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_device_audit_user_id (user_id),
    KEY idx_device_audit_device_id (device_id),
    KEY idx_device_audit_action_time (action_time),
    KEY idx_device_audit_action_type (action_type),
    CONSTRAINT fk_device_audit_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备安全审计';

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    replaced_by_hash VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_hash (token_hash),
    KEY idx_refresh_tokens_user_id (user_id),
    KEY idx_refresh_tokens_expires_at (expires_at),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='刷新令牌';

CREATE TABLE IF NOT EXISTS email_verification_codes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(120) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    code_hash VARCHAR(120) NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_email_code_email_purpose (email, purpose),
    KEY idx_email_code_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证码';

CREATE TABLE IF NOT EXISTS email_notification_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    notification_type VARCHAR(50) NOT NULL,
    business_type VARCHAR(50) NOT NULL,
    business_id BIGINT NOT NULL,
    recipient_email VARCHAR(120) NOT NULL,
    recipient_name VARCHAR(100) NULL,
    period_key VARCHAR(100) NOT NULL,
    success BIT(1) NOT NULL DEFAULT b'0',
    error_message VARCHAR(1000) NULL,
    sent_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_email_notification_dedupe (notification_type, business_id, recipient_email, period_key),
    KEY idx_email_notification_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件通知日志';

-- ============================================================
-- 初始数据
-- ============================================================

START TRANSACTION;

INSERT INTO departments (name, description, deleted) VALUES
    ('摄影部', '负责活动摄影、图片拍摄等工作', b'0'),
    ('采编部', '负责新闻采集、内容编辑等工作', b'0'),
    ('审核部', '负责内容审核、质量把控等工作', b'0'),
    ('运营部', '负责平台运营、账号维护与内容分发等工作', b'0'),
    ('宣传部', '负责对外宣传、推广等工作', b'0')
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO equipment_categories (name, description, sort_order, is_active, deleted) VALUES
    ('相机', '各类相机设备，包括单反相机、微单相机等', 1, b'1', b'0'),
    ('镜头', '各类镜头，包括定焦镜头、变焦镜头等', 2, b'1', b'0'),
    ('三脚架', '各类三脚架和稳定器设备', 3, b'1', b'0'),
    ('闪光灯', '各类闪光灯和照明设备', 4, b'1', b'0'),
    ('录音设备', '各类录音和音频设备', 5, b'1', b'0'),
    ('无人机', '无人机和航拍设备', 6, b'1', b'0'),
    ('其他', '其他摄影相关设备', 7, b'1', b'0')
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    sort_order = VALUES(sort_order),
    is_active = b'1',
    updated_at = CURRENT_TIMESTAMP;

-- BCrypt cost=12；明文密码为 123456。
INSERT INTO users (
    username, password, real_name, email, role, enabled, account_status,
    failed_login_attempts, token_version, deleted
) VALUES (
    'admin', '$2a$12$WQ98HalbZ6r29jkn/EqBqurDWWNYpGSadE2qkuspT0cc0sdKFTBfS',
    '系统管理员', 'admin@photography.local', 'SUPER_ADMIN', b'1', 'ACTIVE', 0, 0, b'0'
)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    real_name = VALUES(real_name),
    role = 'SUPER_ADMIN',
    enabled = b'1',
    account_status = 'ACTIVE',
    failed_login_attempts = 0,
    locked_until = NULL,
    token_version = token_version + 1,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO site_configs (
    config_key, config_value, description, config_type, enabled, sort_order, deleted
) VALUES
    ('site.title', '融媒体管理系统', '网站标题', 'TEXT', b'1', 0, b'0'),
    ('site.subtitle', 'Photography System', '网站副标题', 'TEXT', b'1', 0, b'0'),
    ('login.title', '融媒体管理系统', '登录页面标题', 'TEXT', b'1', 0, b'0'),
    ('login.welcome', '欢迎回来，请登录您的账户', '登录页面欢迎语', 'TEXT', b'1', 0, b'0'),
    ('theme.primary_color', '#409EFF', '主题色', 'COLOR', b'1', 0, b'0'),
    ('mail.enabled', 'false', '是否启用QQ邮箱验证码与提醒', 'BOOLEAN', b'1', 1000, b'0'),
    ('mail.smtp_host', 'smtp.qq.com', 'QQ邮箱SMTP服务器', 'TEXT', b'1', 1001, b'0'),
    ('mail.smtp_port', '465', 'QQ邮箱SMTP端口', 'NUMBER', b'1', 1002, b'0'),
    ('mail.smtp_ssl_enabled', 'true', '是否启用SMTP SSL', 'BOOLEAN', b'1', 1003, b'0'),
    ('mail.qq_account', '', 'QQ邮箱账号', 'TEXT', b'1', 1004, b'0'),
    ('mail.qq_auth_code', '', 'QQ邮箱SMTP授权码', 'TEXT', b'1', 1005, b'0'),
    ('mail.sender_name', '融媒体管理系统', '邮件发件人名称', 'TEXT', b'1', 1006, b'0'),
    ('mail.reminder_advance_minutes', '30', '执勤和晚自习提醒提前分钟数', 'NUMBER', b'1', 1007, b'0'),
    ('mail.overdue_reminder_interval_hours', '24', '设备逾期归还提醒间隔小时数', 'NUMBER', b'1', 1008, b'0'),
    ('mail.log_retention_days', '30', '邮件发送日志与验证码记录保留天数', 'NUMBER', b'1', 1009, b'0'),
    ('mail.duty_reminder_enabled', 'true', '执勤提醒开关', 'BOOLEAN', b'1', 1010, b'0'),
    ('mail.checkin_reminder_enabled', 'true', '晚自习打卡提醒开关', 'BOOLEAN', b'1', 1011, b'0'),
    ('mail.leave_approval_reminder_enabled', 'true', '请假审批提醒开关', 'BOOLEAN', b'1', 1012, b'0'),
    ('mail.borrow_overdue_reminder_enabled', 'true', '设备逾期归还提醒开关', 'BOOLEAN', b'1', 1013, b'0'),
    ('maintenance.enabled', 'false', '维护模式开关', 'BOOLEAN', b'1', 1100, b'0'),
    ('maintenance.title', '系统维护中', '维护页标题', 'TEXT', b'1', 1101, b'0'),
    ('maintenance.message', '系统正在维护，请稍后再试。', '维护页说明', 'TEXT', b'1', 1102, b'0')
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    config_type = VALUES(config_type),
    enabled = b'1',
    sort_order = VALUES(sort_order),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO landing_content_items
    (section_type, title, summary, published, sort_order, deleted)
SELECT 'CAMPUS_FEATURE', '校园现场', '用影像与文字记录课堂、社团、赛事和校园公共生活。', b'1', 1, b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM landing_content_items
    WHERE section_type = 'CAMPUS_FEATURE' AND title = '校园现场' AND deleted = b'0'
);

INSERT INTO landing_content_items
    (section_type, title, summary, published, sort_order, deleted)
SELECT 'CAMPUS_FEATURE', '青年创作', '让学生创意获得专业支持与公开展示的机会。', b'1', 2, b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM landing_content_items
    WHERE section_type = 'CAMPUS_FEATURE' AND title = '青年创作' AND deleted = b'0'
);

INSERT INTO landing_content_items
    (section_type, title, summary, published, sort_order, deleted)
SELECT 'CAMPUS_FEATURE', '影像档案', '持续沉淀学校发展、人物故事与集体记忆。', b'1', 3, b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM landing_content_items
    WHERE section_type = 'CAMPUS_FEATURE' AND title = '影像档案' AND deleted = b'0'
);

INSERT INTO landing_content_items
    (section_type, title, summary, published, sort_order, deleted)
SELECT 'DEPARTMENT_SHOWCASE', '摄影部', '负责校园活动摄影、专题影像与视觉素材管理。', b'1', 1, b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM landing_content_items
    WHERE section_type = 'DEPARTMENT_SHOWCASE' AND title = '摄影部' AND deleted = b'0'
);

INSERT INTO landing_content_items
    (section_type, title, summary, published, sort_order, deleted)
SELECT 'DEPARTMENT_SHOWCASE', '审核部', '负责稿件审核、事实核验与内容质量把控。', b'1', 2, b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM landing_content_items
    WHERE section_type = 'DEPARTMENT_SHOWCASE' AND title = '审核部' AND deleted = b'0'
);

INSERT INTO landing_content_items
    (section_type, title, summary, published, sort_order, deleted)
SELECT 'DEPARTMENT_SHOWCASE', '运营部', '负责平台运营、内容分发与用户反馈。', b'1', 3, b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM landing_content_items
    WHERE section_type = 'DEPARTMENT_SHOWCASE' AND title = '运营部' AND deleted = b'0'
);

COMMIT;

SELECT
    '数据库初始化完成' AS message,
    'admin' AS admin_username,
    '123456' AS initial_password,
    '首次登录后请立即修改密码' AS security_notice;
