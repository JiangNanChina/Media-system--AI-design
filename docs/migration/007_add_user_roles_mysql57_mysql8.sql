-- MySQL 5.7 / 8.x, repeatable migration for multi-role users.
-- Back up the database before running this file.

SET NAMES utf8mb4;

-- Keep the legacy effective-role column for older application versions and
-- clients. VARCHAR(20) is large enough for SUPER_ADMIN.
ALTER TABLE users
    MODIFY COLUMN role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '兼容字段：用户当前最高角色';

UPDATE users
SET role = 'SUPER_ADMIN'
WHERE role = 'ADMIN';

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    KEY idx_user_roles_role (role),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色集合';

-- Normalize a partially migrated deployment before backfilling missing rows.
DELETE legacy_role
FROM user_roles legacy_role
JOIN user_roles canonical_role
  ON canonical_role.user_id = legacy_role.user_id
 AND canonical_role.role = 'SUPER_ADMIN'
WHERE legacy_role.role = 'ADMIN';

UPDATE user_roles
SET role = 'SUPER_ADMIN'
WHERE role = 'ADMIN';

INSERT IGNORE INTO user_roles (user_id, role)
SELECT id, role
FROM users;
