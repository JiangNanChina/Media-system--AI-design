-- MySQL 8.0.41
-- Add the required class field without failing when Hibernate has already created it.

USE `photography_system`;

SELECT
    COUNT(*),
    MAX(`IS_NULLABLE`),
    MAX(`CHARACTER_MAXIMUM_LENGTH`),
    MAX(`COLLATION_NAME`)
INTO
    @class_name_exists,
    @class_name_is_nullable,
    @class_name_max_length,
    @class_name_collation
FROM `information_schema`.`COLUMNS`
WHERE `TABLE_SCHEMA` = DATABASE()
  AND `TABLE_NAME` = 'join_applications'
  AND `COLUMN_NAME` = 'class_name';

SET @add_class_name_sql = IF(
    @class_name_exists = 0,
    'ALTER TABLE `join_applications` ADD COLUMN `class_name` VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `enrollment_year`',
    'SELECT ''join_applications.class_name already exists'' AS `migration_status`'
);

PREPARE add_class_name_statement FROM @add_class_name_sql;
EXECUTE add_class_name_statement;
DEALLOCATE PREPARE add_class_name_statement;

-- Preserve legacy applications before enforcing the new required constraint.
UPDATE `join_applications`
SET `class_name` = ''
WHERE `class_name` IS NULL;

SET @normalize_class_name_sql = IF(
    @class_name_exists = 0
        OR @class_name_is_nullable <> 'NO'
        OR @class_name_max_length <> 80
        OR @class_name_collation <> 'utf8mb4_unicode_ci',
    'ALTER TABLE `join_applications` MODIFY COLUMN `class_name` VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `enrollment_year`',
    'SELECT ''join_applications.class_name schema is correct'' AS `migration_status`'
);

PREPARE normalize_class_name_statement FROM @normalize_class_name_sql;
EXECUTE normalize_class_name_statement;
DEALLOCATE PREPARE normalize_class_name_statement;
