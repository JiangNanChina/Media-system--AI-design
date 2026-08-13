-- Upgrade a legacy photography_system dump to the schema required by the
-- current Spring Boot project. This script is intended to run once after
-- importing photography_system_2026-08-11_04-00-02_mysql_data.sql.
--
-- Compatible with MySQL 5.7 used by the current Baota deployment.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- Users and security migration
-- ============================================================

ALTER TABLE `users`
    MODIFY COLUMN `role`
        ENUM('MEMBER','MINISTER','DIRECTOR','ADVISOR','SUPER_ADMIN','ADMIN')
        COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MEMBER',
    ADD COLUMN `account_status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER `enabled`,
    ADD COLUMN `failed_login_attempts` INT NOT NULL DEFAULT 0 AFTER `account_status`,
    ADD COLUMN `locked_until` DATETIME NULL AFTER `failed_login_attempts`,
    ADD COLUMN `token_version` INT NOT NULL DEFAULT 0 AFTER `locked_until`;

UPDATE `users`
SET `role` = 'SUPER_ADMIN'
WHERE `role` = 'ADMIN';

UPDATE `users`
SET `account_status` = CASE WHEN `enabled` = 1 THEN 'ACTIVE' ELSE 'DISABLED' END,
    `failed_login_attempts` = COALESCE(`failed_login_attempts`, 0),
    `token_version` = COALESCE(`token_version`, 0);

-- ============================================================
-- Borrowing, announcements, leave requests, and equipment
-- ============================================================

UPDATE `borrow_records`
SET `status` = 'REJECTED'
WHERE `status` = 'CANCELLED';

ALTER TABLE `borrow_records`
    ADD COLUMN `borrower_type` ENUM('INTERNAL','EXTERNAL') NOT NULL DEFAULT 'INTERNAL' AFTER `id`,
    ADD COLUMN `external_borrower_type` ENUM('COLLEGE','DEPARTMENT','TEACHER') NULL AFTER `borrower_type`,
    ADD COLUMN `external_organization` VARCHAR(150) COLLATE utf8mb4_unicode_ci NULL AFTER `external_borrower_type`,
    ADD COLUMN `external_contact_name` VARCHAR(80) COLLATE utf8mb4_unicode_ci NULL AFTER `external_organization`,
    ADD COLUMN `external_phone` VARCHAR(20) COLLATE utf8mb4_unicode_ci NULL AFTER `external_contact_name`,
    ADD COLUMN `external_email` VARCHAR(120) COLLATE utf8mb4_unicode_ci NULL AFTER `external_phone`,
    MODIFY COLUMN `status`
        ENUM('PENDING','APPROVED','REJECTED','BORROWED','RETURNED','OVERDUE')
        COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
    MODIFY COLUMN `return_images` VARCHAR(2000) COLLATE utf8mb4_unicode_ci NULL;

ALTER TABLE `announcements`
    MODIFY COLUMN `type`
        ENUM('SYSTEM','IMPORTANT','GENERAL','ACTIVITY','URGENT')
        COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GENERAL';

UPDATE `announcements`
SET `type` = 'IMPORTANT'
WHERE `type` = 'URGENT';

ALTER TABLE `announcements`
    MODIFY COLUMN `type`
        ENUM('SYSTEM','IMPORTANT','GENERAL','ACTIVITY')
        COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GENERAL',
    MODIFY COLUMN `priority` INT NOT NULL DEFAULT 0,
    MODIFY COLUMN `view_count` BIGINT NOT NULL DEFAULT 0;

ALTER TABLE `leave_requests`
    MODIFY COLUMN `status`
        ENUM('PENDING','APPROVED','REJECTED','CANCELLED')
        COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
    MODIFY COLUMN `attachment_urls` VARCHAR(2000) COLLATE utf8mb4_unicode_ci NULL;

ALTER TABLE `equipment`
    MODIFY COLUMN `description` VARCHAR(1000) COLLATE utf8mb4_unicode_ci NULL,
    MODIFY COLUMN `damaged_quantity` INT NOT NULL DEFAULT 0 COMMENT '损坏数量',
    MODIFY COLUMN `status` VARCHAR(20) COLLATE utf8mb4_unicode_ci NULL DEFAULT '正常',
    MODIFY COLUMN `specifications` VARCHAR(2000) COLLATE utf8mb4_unicode_ci NULL;

ALTER TABLE `equipment_categories`
    MODIFY COLUMN `description` VARCHAR(255) COLLATE utf8mb4_unicode_ci NULL,
    MODIFY COLUMN `is_active` BIT(1) NOT NULL DEFAULT b'1';

ALTER TABLE `checkin_configurations`
    MODIFY COLUMN `required_weekdays` VARCHAR(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1,2,3,4',
    MODIFY COLUMN `sort_order` INT NULL DEFAULT 0,
    MODIFY COLUMN `early_checkin_minutes` INT NULL DEFAULT 0,
    MODIFY COLUMN `late_checkin_minutes` INT NULL DEFAULT 0;

ALTER TABLE `checkin_records`
    MODIFY COLUMN `audit_status`
        ENUM('NOT_REQUIRED','PENDING','APPROVED','REJECTED')
        COLLATE utf8mb4_unicode_ci NULL DEFAULT 'NOT_REQUIRED';

-- ============================================================
-- New tables used by the current project
-- ============================================================

CREATE TABLE IF NOT EXISTS `landing_content_items` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `section_type` ENUM('CAMPUS_FEATURE','DEPARTMENT_SHOWCASE') COLLATE utf8mb4_unicode_ci NOT NULL,
    `title` VARCHAR(160) COLLATE utf8mb4_unicode_ci NOT NULL,
    `summary` TEXT COLLATE utf8mb4_unicode_ci NULL,
    `media_url` VARCHAR(500) COLLATE utf8mb4_unicode_ci NULL,
    `link_url` VARCHAR(500) COLLATE utf8mb4_unicode_ci NULL,
    `published` BIT(1) NOT NULL DEFAULT b'1',
    `sort_order` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    KEY `idx_landing_section` (`section_type`),
    KEY `idx_landing_published` (`published`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首页内容项';

CREATE TABLE IF NOT EXISTS `media_submissions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `submission_number` VARCHAR(40) COLLATE utf8mb4_unicode_ci NOT NULL,
    `title` VARCHAR(160) COLLATE utf8mb4_unicode_ci NOT NULL,
    `description` TEXT COLLATE utf8mb4_unicode_ci NULL,
    `submitter_name` VARCHAR(80) COLLATE utf8mb4_unicode_ci NOT NULL,
    `phone` VARCHAR(20) COLLATE utf8mb4_unicode_ci NOT NULL,
    `qq_email` VARCHAR(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    `organization` VARCHAR(160) COLLATE utf8mb4_unicode_ci NULL,
    `original_filename` VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    `stored_filename` VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    `mime_type` VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    `file_size` BIGINT NOT NULL,
    `status` ENUM('PENDING','APPROVED','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
    `reviewer_id` BIGINT NULL,
    `review_feedback` VARCHAR(1000) COLLATE utf8mb4_unicode_ci NULL,
    `reviewed_at` DATETIME NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_submission_number` (`submission_number`),
    KEY `idx_submission_status` (`status`),
    KEY `fk_media_submissions_reviewer` (`reviewer_id`),
    CONSTRAINT `fk_media_submissions_reviewer`
        FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='融媒体投稿';

CREATE TABLE IF NOT EXISTS `checkin_device_usages` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `configuration_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `usage_date` DATE NOT NULL,
    `device_fingerprint_hash` VARCHAR(64) COLLATE utf8mb4_unicode_ci NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_checkin_device_slot` (`configuration_id`, `usage_date`, `device_fingerprint_hash`),
    KEY `idx_checkin_device_user_id` (`user_id`),
    CONSTRAINT `fk_checkin_device_usage_configuration`
        FOREIGN KEY (`configuration_id`) REFERENCES `checkin_configurations` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_checkin_device_usage_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡设备使用记录';

CREATE TABLE IF NOT EXISTS `refresh_tokens` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `token_hash` VARCHAR(64) COLLATE utf8mb4_unicode_ci NOT NULL,
    `expires_at` DATETIME NOT NULL,
    `revoked_at` DATETIME NULL,
    `replaced_by_hash` VARCHAR(64) COLLATE utf8mb4_unicode_ci NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_refresh_token_hash` (`token_hash`),
    KEY `idx_refresh_user_id` (`user_id`),
    CONSTRAINT `fk_refresh_tokens_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='刷新令牌';

CREATE TABLE IF NOT EXISTS `colleges` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(160) COLLATE utf8mb4_unicode_ci NOT NULL,
    `description` VARCHAR(500) COLLATE utf8mb4_unicode_ci NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_colleges_name_deleted` (`name`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学院';

CREATE TABLE IF NOT EXISTS `join_applications` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `application_number` VARCHAR(40) COLLATE utf8mb4_unicode_ci NOT NULL,
    `real_name` VARCHAR(80) COLLATE utf8mb4_unicode_ci NOT NULL,
    `qq_email` VARCHAR(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    `phone` VARCHAR(20) COLLATE utf8mb4_unicode_ci NOT NULL,
    `gender` ENUM('MALE','FEMALE','OTHER') COLLATE utf8mb4_unicode_ci NOT NULL,
    `college` VARCHAR(160) COLLATE utf8mb4_unicode_ci NOT NULL,
    `major` VARCHAR(160) COLLATE utf8mb4_unicode_ci NOT NULL,
    `enrollment_year` INT NOT NULL,
    `self_introduction` TEXT COLLATE utf8mb4_unicode_ci NOT NULL,
    `work_original_filename` VARCHAR(255) COLLATE utf8mb4_unicode_ci NULL,
    `work_stored_filename` VARCHAR(255) COLLATE utf8mb4_unicode_ci NULL,
    `work_mime_type` VARCHAR(100) COLLATE utf8mb4_unicode_ci NULL,
    `work_file_size` BIGINT NULL,
    `status` ENUM('PENDING','INTERVIEW','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
    `reviewer_id` BIGINT NULL,
    `review_feedback` VARCHAR(1000) COLLATE utf8mb4_unicode_ci NULL,
    `reviewed_at` DATETIME NULL,
    `interview_qq_group` VARCHAR(50) COLLATE utf8mb4_unicode_ci NULL,
    `notification_sent` BIT(1) NOT NULL DEFAULT b'0',
    `notified_at` DATETIME NULL,
    `notification_error` VARCHAR(1000) COLLATE utf8mb4_unicode_ci NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_join_application_number` (`application_number`),
    KEY `idx_join_application_status` (`status`),
    KEY `idx_join_application_email` (`qq_email`),
    KEY `fk_join_applications_reviewer` (`reviewer_id`),
    CONSTRAINT `fk_join_applications_reviewer`
        FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入社申请';

-- ============================================================
-- Default data for new features. Existing user content is kept.
-- ============================================================

INSERT INTO `site_configs`
    (`config_key`, `config_value`, `description`, `config_type`, `enabled`, `sort_order`, `deleted`)
VALUES
    ('maintenance.enabled', 'false', '维护模式开关', 'BOOLEAN', 1, 1100, 0),
    ('maintenance.title', '系统维护中', '维护页标题', 'TEXT', 1, 1101, 0),
    ('maintenance.message', '系统正在维护，请稍后再试。', '维护页说明', 'TEXT', 1, 1102, 0)
ON DUPLICATE KEY UPDATE
    `description` = VALUES(`description`),
    `config_type` = VALUES(`config_type`),
    `enabled` = 1,
    `sort_order` = VALUES(`sort_order`),
    `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `landing_content_items`
    (`section_type`, `title`, `summary`, `published`, `sort_order`, `deleted`)
SELECT 'CAMPUS_FEATURE', '校园现场', '用影像与文字记录课堂、社团、赛事和校园公共生活。', b'1', 1, b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `landing_content_items`
    WHERE `section_type` = 'CAMPUS_FEATURE' AND `title` = '校园现场' AND `deleted` = b'0'
);

INSERT INTO `landing_content_items`
    (`section_type`, `title`, `summary`, `published`, `sort_order`, `deleted`)
SELECT 'CAMPUS_FEATURE', '青年创作', '让学生创意获得专业支持与公开展示的机会。', b'1', 2, b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `landing_content_items`
    WHERE `section_type` = 'CAMPUS_FEATURE' AND `title` = '青年创作' AND `deleted` = b'0'
);

INSERT INTO `landing_content_items`
    (`section_type`, `title`, `summary`, `published`, `sort_order`, `deleted`)
SELECT 'CAMPUS_FEATURE', '影像档案', '持续沉淀学校发展、人物故事与集体记忆。', b'1', 3, b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `landing_content_items`
    WHERE `section_type` = 'CAMPUS_FEATURE' AND `title` = '影像档案' AND `deleted` = b'0'
);

INSERT INTO `landing_content_items`
    (`section_type`, `title`, `summary`, `published`, `sort_order`, `deleted`)
SELECT 'DEPARTMENT_SHOWCASE', '摄影部', '负责校园活动摄影、专题影像与视觉素材管理。', b'1', 1, b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `landing_content_items`
    WHERE `section_type` = 'DEPARTMENT_SHOWCASE' AND `title` = '摄影部' AND `deleted` = b'0'
);

INSERT INTO `landing_content_items`
    (`section_type`, `title`, `summary`, `published`, `sort_order`, `deleted`)
SELECT 'DEPARTMENT_SHOWCASE', '审核部', '负责稿件审核、事实核验与内容质量把控。', b'1', 2, b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `landing_content_items`
    WHERE `section_type` = 'DEPARTMENT_SHOWCASE' AND `title` = '审核部' AND `deleted` = b'0'
);

INSERT INTO `landing_content_items`
    (`section_type`, `title`, `summary`, `published`, `sort_order`, `deleted`)
SELECT 'DEPARTMENT_SHOWCASE', '运营部', '负责平台运营、内容分发与用户反馈。', b'1', 3, b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `landing_content_items`
    WHERE `section_type` = 'DEPARTMENT_SHOWCASE' AND `title` = '运营部' AND `deleted` = b'0'
);

SET FOREIGN_KEY_CHECKS = 1;

