-- Fix startup failure:
--   Data truncated for column 'role'
--
-- Older deployments created users.role as ENUM without SUPER_ADMIN or as
-- VARCHAR(10). The current application writes SUPER_ADMIN, so widen the
-- column before starting the new JAR.

ALTER TABLE users
    MODIFY COLUMN role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '用户角色';

UPDATE users
SET role = 'SUPER_ADMIN'
WHERE role = 'ADMIN';

