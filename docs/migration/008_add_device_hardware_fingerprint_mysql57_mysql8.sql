-- MySQL 5.7 / 8.x: add the stable hardware recovery fingerprint.
-- Back up the database and run this script before deploying the new backend.

SET NAMES utf8mb4;

SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_devices'
      AND COLUMN_NAME = 'hardware_fingerprint'
);
SET @add_column_sql = IF(
    @column_exists = 0,
    'ALTER TABLE user_devices ADD COLUMN hardware_fingerprint VARCHAR(128) NULL AFTER device_fingerprint',
    'SELECT 1'
);
PREPARE add_column_statement FROM @add_column_sql;
EXECUTE add_column_statement;
DEALLOCATE PREPARE add_column_statement;

SET @index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_devices'
      AND INDEX_NAME = 'idx_user_devices_hardware_fingerprint'
);
SET @add_index_sql = IF(
    @index_exists = 0,
    'ALTER TABLE user_devices ADD INDEX idx_user_devices_hardware_fingerprint (hardware_fingerprint)',
    'SELECT 1'
);
PREPARE add_index_statement FROM @add_index_sql;
EXECUTE add_index_statement;
DEALLOCATE PREPARE add_index_statement;
