-- 融媒体管理系统数据库初始化脚本
-- 创建数据库和表结构，插入初始数据

-- ====================================
-- 1. 创建数据库
-- ====================================
CREATE DATABASE IF NOT EXISTS photography_system 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE photography_system;

-- ====================================
-- 2. 创建表结构
-- ====================================

-- 部门表
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '部门名称',
    type VARCHAR(20) NOT NULL COMMENT '部门类型',
    description VARCHAR(500) COMMENT '部门描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    UNIQUE KEY uk_dept_name (name),
    INDEX idx_dept_type (type),
    INDEX idx_dept_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',

    avatar_url VARCHAR(500) COMMENT '头像URL',
    role VARCHAR(10) NOT NULL DEFAULT 'MEMBER' COMMENT '角色',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    department_id BIGINT COMMENT '部门ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_email (email),

    INDEX idx_user_role (role),
    INDEX idx_user_department (department_id),
    INDEX idx_user_deleted (deleted),
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 设备表
CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '设备名称',

    category VARCHAR(50) NOT NULL COMMENT '设备分类',
    serial_number VARCHAR(50) NOT NULL COMMENT '设备编号',
    description TEXT COMMENT '设备描述',

    image_urls TEXT COMMENT '图片URLs(JSON格式)',
    stock_quantity INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    available_quantity INT NOT NULL DEFAULT 0 COMMENT '可用数量',

    status VARCHAR(20) DEFAULT '正常' COMMENT '设备状态',
    specifications TEXT COMMENT '设备规格参数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    UNIQUE KEY uk_equipment_serial (serial_number),
    INDEX idx_equipment_category (category),
    INDEX idx_equipment_status (status),
    INDEX idx_equipment_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备表';

-- 借还记录表
CREATE TABLE IF NOT EXISTS borrow_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '借用用户ID',
    equipment_id BIGINT NOT NULL COMMENT '设备ID',
    quantity INT NOT NULL COMMENT '借用数量',
    expected_return_time DATETIME NOT NULL COMMENT '预计归还时间',
    actual_return_time DATETIME COMMENT '实际归还时间',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '借用状态',
    borrow_reason VARCHAR(500) COMMENT '借用原因',
    approval_notes VARCHAR(500) COMMENT '审批备注',
    approved_by BIGINT COMMENT '审批人ID',
    approval_time DATETIME COMMENT '审批时间',
    return_notes VARCHAR(500) COMMENT '归还备注',
    damage_description VARCHAR(500) COMMENT '损坏描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_borrow_user (user_id),
    INDEX idx_borrow_equipment (equipment_id),
    INDEX idx_borrow_status (status),
    INDEX idx_borrow_deleted (deleted),
    INDEX idx_borrow_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借还记录表';

-- 公告表
CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    published BOOLEAN DEFAULT FALSE COMMENT '是否发布',
    priority INT DEFAULT 0 COMMENT '优先级',
    view_count BIGINT DEFAULT 0 COMMENT '查看次数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_announcement_published (published),
    INDEX idx_announcement_priority (priority),
    INDEX idx_announcement_created_by (created_by),
    INDEX idx_announcement_deleted (deleted),
    INDEX idx_announcement_created_at (created_at),
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 晚自习打卡配置表
CREATE TABLE IF NOT EXISTS study_checkin_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    location VARCHAR(100) NOT NULL COMMENT '打卡地点',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    description VARCHAR(500) COMMENT '描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_checkin_config_location (location),
    INDEX idx_checkin_config_active (active),
    INDEX idx_checkin_config_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='晚自习打卡配置表';

-- 晚自习打卡记录表
CREATE TABLE IF NOT EXISTS study_checkins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '打卡用户ID',
    checkin_date DATE NOT NULL COMMENT '打卡日期',
    checkin_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
    location VARCHAR(100) NOT NULL COMMENT '打卡地点',
    device_info VARCHAR(200) COMMENT '设备信息',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    notes VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    UNIQUE KEY uk_checkin_user_date_device (user_id, checkin_date, device_info),
    INDEX idx_checkin_user_date (user_id, checkin_date),
    INDEX idx_checkin_location (location),
    INDEX idx_checkin_date (checkin_date),
    INDEX idx_checkin_deleted (deleted),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='晚自习打卡记录表';

-- 执勤排班表
CREATE TABLE IF NOT EXISTS duty_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '执勤用户ID',
    day_of_week INT NOT NULL COMMENT '星期几(1-7)',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    notes VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    UNIQUE KEY uk_duty_user_day_time (user_id, day_of_week, start_time),
    INDEX idx_duty_schedule_user (user_id),
    INDEX idx_duty_schedule_day (day_of_week),
    INDEX idx_duty_schedule_active (active),
    INDEX idx_duty_schedule_deleted (deleted),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='执勤排班表';

-- 执勤记录表
CREATE TABLE IF NOT EXISTS duty_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '执勤用户ID',
    duty_schedule_id BIGINT NOT NULL COMMENT '执勤排班ID',
    duty_date DATE NOT NULL COMMENT '执勤日期',
    checkin_time DATETIME COMMENT '签到时间',
    checkout_time DATETIME COMMENT '签退时间',
    actual_start_time DATETIME COMMENT '实际开始时间',
    actual_end_time DATETIME COMMENT '实际结束时间',
    status VARCHAR(20) DEFAULT '待执勤' COMMENT '执勤状态',
    notes VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_duty_record_user_date (user_id, duty_date),
    INDEX idx_duty_record_schedule (duty_schedule_id),
    INDEX idx_duty_record_date (duty_date),
    INDEX idx_duty_record_status (status),
    INDEX idx_duty_record_deleted (deleted),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (duty_schedule_id) REFERENCES duty_schedules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='执勤记录表';

-- 请假申请表
CREATE TABLE IF NOT EXISTS leave_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '申请用户ID',
    leave_type VARCHAR(50) NOT NULL COMMENT '请假类型',
    leave_date DATE NOT NULL COMMENT '请假日期',
    reason VARCHAR(500) NOT NULL COMMENT '请假原因',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '请假状态',
    approved_by BIGINT COMMENT '审批人ID',
    approval_time DATETIME COMMENT '审批时间',
    approval_notes VARCHAR(500) COMMENT '审批备注',
    reference_id BIGINT COMMENT '关联ID',
    reference_type VARCHAR(50) COMMENT '关联类型',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_leave_user_date (user_id, leave_date),
    INDEX idx_leave_type (leave_type),
    INDEX idx_leave_status (status),
    INDEX idx_leave_deleted (deleted),
    INDEX idx_leave_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假申请表';

-- ====================================
-- 3. 插入初始数据
-- ====================================

-- 插入默认部门
INSERT INTO departments (name, type, description, created_at, updated_at, deleted) VALUES
('摄影部', 'PHOTOGRAPHY', '负责活动摄影、图片拍摄等工作', NOW(), NOW(), FALSE),
('采编部', 'EDITING', '负责新闻采集、内容编辑等工作', NOW(), NOW(), FALSE),
('审核部', 'REVIEW', '负责内容审核、质量把控等工作', NOW(), NOW(), FALSE),
('宣传部', 'PUBLICITY', '负责对外宣传、推广等工作', NOW(), NOW(), FALSE)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入默认管理员用户
INSERT INTO users (username, password, real_name, email, role, enabled, created_at, updated_at, deleted) VALUES
('admin', '$2a$10$N8tQk2jKZb2oE8HFvDN4xuwt5M8kE3z0FQNZSvP3XzBjKJH3Lrz8i', '系统管理员', 'admin@photography.com', 'ADMIN', true, NOW(), NOW(), FALSE)
ON DUPLICATE KEY UPDATE password = VALUES(password);

-- 插入示例设备分类的设备
INSERT INTO equipment (name, category, serial_number, description, stock_quantity, available_quantity, specifications, created_at, updated_at, deleted) VALUES
('佳能单反相机', '相机', 'CAM001', '专业级全画幅单反相机，适合各类摄影活动', 2, 2, '3040万像素，61点自动对焦，4K视频录制', NOW(), NOW(), FALSE),
('索尼微单相机', '相机', 'CAM002', '高分辨率全画幅微单相机', 1, 1, '6100万像素，实时追踪对焦，8K视频录制', NOW(), NOW(), FALSE),
('尼康镜头', '镜头', 'LENS001', '专业标准变焦镜头', 3, 3, '24-70mm焦距，f/2.8恒定光圈，VR防抖', NOW(), NOW(), FALSE),
('曼富图三脚架', '三脚架', 'TRI001', '碳纤维专业三脚架', 5, 5, '碳纤维材质，承重12kg，4节调节', NOW(), NOW(), FALSE),
('神牛闪光灯', '闪光灯', 'FLASH001', '便携式影室闪光灯', 2, 2, '600Ws功率，TTL/HSS支持，2.4G无线', NOW(), NOW(), FALSE),
('大疆无人机', '无人机', 'DRONE001', '专业航拍无人机', 1, 1, '4/3 CMOS传感器，5.1K视频，46分钟续航', NOW(), NOW(), FALSE),
('索尼录音设备', '录音设备', 'AUDIO001', '便携式数字录音机', 2, 2, '32bit Float录制，双XLR输入，专业级音质', NOW(), NOW(), FALSE)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入默认打卡配置
INSERT INTO study_checkin_configs (location, start_time, end_time, active, description, created_at, updated_at, deleted) VALUES
('图书馆自习室', '19:00:00', '21:30:00', TRUE, '图书馆晚自习打卡地点', NOW(), NOW(), FALSE),
('教学楼A座', '19:00:00', '21:30:00', TRUE, '教学楼A座自习室', NOW(), NOW(), FALSE),
('教学楼B座', '19:00:00', '21:30:00', TRUE, '教学楼B座自习室', NOW(), NOW(), FALSE)
ON DUPLICATE KEY UPDATE location = VALUES(location);

-- 插入示例公告
INSERT INTO announcements (title, content, created_by, published, priority, view_count, created_at, updated_at, deleted) VALUES
('融媒体管理系统正式上线', '欢迎使用融媒体管理系统！本系统提供设备借还、打卡执勤、公告管理等功能。如有问题请联系管理员。', 1, true, 10, 0, NOW(), NOW(), FALSE),
('设备借用注意事项', '1. 借用设备前请仔细检查设备状态\n2. 使用过程中注意设备保护\n3. 按时归还设备\n4. 如有损坏请及时上报', 1, true, 5, 0, NOW(), NOW(), FALSE),
('晚自习打卡规定', '1. 每日19:00-21:30为打卡时间\n2. 每个设备只允许一个账号打卡\n3. 如需请假请提前申请\n4. 连续缺卡3次将通报批评', 1, true, 8, 0, NOW(), NOW(), FALSE)
ON DUPLICATE KEY UPDATE title = VALUES(title);

-- ====================================
-- 4. 创建视图和函数（可选）
-- ====================================

-- 删除已存在的视图（如果存在）
DROP VIEW IF EXISTS user_borrow_stats;
DROP VIEW IF EXISTS equipment_usage_stats;

-- 创建用户设备借用统计视图
CREATE OR REPLACE VIEW user_borrow_stats AS
SELECT 
    u.id as user_id,
    u.real_name,
    u.username,
    d.name as department_name,
    COUNT(br.id) as total_borrows,
    COUNT(CASE WHEN br.status = 'RETURNED' THEN 1 END) as returned_count,
    COUNT(CASE WHEN br.status = 'BORROWED' THEN 1 END) as current_borrows,
    COUNT(CASE WHEN br.status = 'OVERDUE' THEN 1 END) as overdue_count
FROM users u
LEFT JOIN departments d ON u.department_id = d.id
LEFT JOIN borrow_records br ON u.id = br.user_id AND br.deleted = false
WHERE u.deleted = false
GROUP BY u.id, u.real_name, u.username, d.name;

-- 创建设备使用统计视图
CREATE OR REPLACE VIEW equipment_usage_stats AS
SELECT 
    e.id as equipment_id,
    e.name,
    e.category,
    e.serial_number,
    COUNT(br.id) as total_borrows,
    COUNT(CASE WHEN br.status = 'BORROWED' THEN 1 END) as current_borrows,
    e.stock_quantity,
    e.available_quantity,
    ROUND((COUNT(br.id) / NULLIF(e.stock_quantity, 0)) * 100, 2) as usage_rate
FROM equipment e
LEFT JOIN borrow_records br ON e.id = br.equipment_id AND br.deleted = false
WHERE e.deleted = false
GROUP BY e.id, e.name, e.category, e.serial_number, e.stock_quantity, e.available_quantity;

-- ====================================
-- 5. 创建存储过程（可选）
-- ====================================

-- 删除已存在的存储过程（如果存在）
DROP PROCEDURE IF EXISTS GenerateDutyRecords;

DELIMITER //

-- 自动生成执勤记录的存储过程
CREATE PROCEDURE GenerateDutyRecords(
    IN start_date DATE,
    IN end_date DATE
)
BEGIN
    -- 声明所有变量
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_user_id BIGINT;
    DECLARE v_schedule_id BIGINT;
    DECLARE v_day_of_week INT;
    DECLARE v_current_date DATE;
    
    -- 声明游标
    DECLARE schedule_cursor CURSOR FOR 
        SELECT user_id, id, day_of_week 
        FROM duty_schedules 
        WHERE active = TRUE AND deleted = FALSE;
    
    -- 声明异常处理程序
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    SET v_current_date = start_date;
    
    WHILE v_current_date <= end_date DO
        SET done = FALSE;
        OPEN schedule_cursor;
        
        schedule_loop: LOOP
            FETCH schedule_cursor INTO v_user_id, v_schedule_id, v_day_of_week;
            IF done THEN
                LEAVE schedule_loop;
            END IF;
            
            -- 检查当前日期是否匹配排班的星期几
            IF DAYOFWEEK(v_current_date) = v_day_of_week + 1 THEN
                -- 检查是否已存在记录
                IF NOT EXISTS (
                    SELECT 1 FROM duty_records 
                    WHERE user_id = v_user_id 
                    AND duty_date = v_current_date 
                    AND deleted = FALSE
                ) THEN
                    INSERT INTO duty_records (user_id, duty_schedule_id, duty_date, status)
                    VALUES (v_user_id, v_schedule_id, v_current_date, '待执勤');
                END IF;
            END IF;
        END LOOP;
        
        CLOSE schedule_cursor;
        SET v_current_date = DATE_ADD(v_current_date, INTERVAL 1 DAY);
    END WHILE;
END //

DELIMITER ;

-- ====================================
-- 6. 创建触发器（可选）
-- ====================================

-- 删除已存在的触发器（如果存在）
DROP TRIGGER IF EXISTS tr_borrow_status_update;

DELIMITER //

-- 借用记录状态变更时自动更新设备库存
CREATE TRIGGER tr_borrow_status_update 
AFTER UPDATE ON borrow_records
FOR EACH ROW
BEGIN
    -- 当状态从APPROVED变为BORROWED时，减少可用库存
    IF OLD.status = 'APPROVED' AND NEW.status = 'BORROWED' THEN
        UPDATE equipment 
        SET available_quantity = available_quantity - NEW.quantity
        WHERE id = NEW.equipment_id;
    END IF;
    
    -- 当状态变为RETURNED时，增加可用库存
    IF OLD.status = 'BORROWED' AND NEW.status = 'RETURNED' THEN
        UPDATE equipment 
        SET available_quantity = available_quantity + NEW.quantity
        WHERE id = NEW.equipment_id;
    END IF;
END //

DELIMITER ;

-- ====================================
-- 7. 插入测试数据（可选）
-- ====================================

-- 可以根据需要添加更多测试数据
-- 例如：测试用户、测试借用记录等

COMMIT;

-- 显示初始化完成信息
SELECT 'Database initialization completed successfully!' as message;
SELECT 'Default admin user: admin / 123456' as login_info;
SELECT 'Please change the default password after first login!' as security_notice;
