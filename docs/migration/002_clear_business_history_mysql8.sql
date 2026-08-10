-- DESTRUCTIVE and intentionally separate from the schema migration.
-- Run only after database/uploads backups and with this session variable:
-- mysql --init-command="SET @CONFIRM_MEDIA_HISTORY_CLEANUP='YES'" photography_system < 002_clear_business_history_mysql8.sql

DELIMITER $$
DROP PROCEDURE IF EXISTS confirm_media_history_cleanup$$
CREATE PROCEDURE confirm_media_history_cleanup()
BEGIN
    IF COALESCE(@CONFIRM_MEDIA_HISTORY_CLEANUP, 'NO') <> 'YES' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cleanup refused: set @CONFIRM_MEDIA_HISTORY_CLEANUP=YES';
    END IF;
END$$
DELIMITER ;

CALL confirm_media_history_cleanup();
DROP PROCEDURE confirm_media_history_cleanup;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE refresh_tokens;
TRUNCATE TABLE checkin_device_usages;
TRUNCATE TABLE checkin_records;
TRUNCATE TABLE leave_requests;
TRUNCATE TABLE duty_swap_requests;
TRUNCATE TABLE duty_records;
TRUNCATE TABLE duty_schedules;
TRUNCATE TABLE checkin_configurations;
TRUNCATE TABLE borrow_records;
TRUNCATE TABLE device_audit_logs;
TRUNCATE TABLE user_devices;
SET FOREIGN_KEY_CHECKS = 1;

UPDATE users
SET failed_login_attempts = 0, locked_until = NULL, token_version = token_version + 1;
