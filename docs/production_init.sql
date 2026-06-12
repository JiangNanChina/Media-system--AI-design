-- 融媒体管理系统 - 生产环境数据库初始化脚本
-- 包含完整的表结构、索引、约束和基础数据

-- ====================================
-- 数据库创建
-- ====================================
CREATE DATABASE IF NOT EXISTS photography_system 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE photography_system;

-- ====================================
-- 表结构创建
-- ====================================

-- 部门表
DROP TABLE IF EXISTS departments;
CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    name VARCHAR(100) NOT NULL COMMENT '部门名称',
    type VARCHAR(20) NOT NULL COMMENT '部门类型：PHOTOGRAPHY,EDITING,REVIEW,PUBLICITY,CUSTOM',
    description VARCHAR(500) COMMENT '部门描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标志',
    
    UNIQUE INDEX uk_dept_name (name),
    INDEX idx_dept_type (type),
    INDEX idx_dept_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门信息表';

-- 用户表
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱地址',
    phone VARCHAR(20) COMMENT '手机号码',

    avatar_url VARCHAR(500) COMMENT '头像URL',
    role VARCHAR(10) NOT NULL DEFAULT 'MEMBER' COMMENT '用户角色：ADMIN,MEMBER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账户是否启用',
    department_id BIGINT COMMENT '所属部门ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标志',
    
    UNIQUE INDEX uk_user_username (username),
    UNIQUE INDEX uk_user_email (email),

    INDEX idx_user_role (role),
    INDEX idx_user_department (department_id),
    INDEX idx_user_enabled (enabled),
    INDEX idx_user_deleted (deleted),
    
    CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 设备表
DROP TABLE IF EXISTS equipment;
CREATE TABLE equipment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    name VARCHAR(100) NOT NULL COMMENT '设备名称',

    category VARCHAR(50) NOT NULL COMMENT '设备分类',
    serial_number VARCHAR(50) NOT NULL COMMENT '设备序列号',
    description TEXT COMMENT '设备描述',

    image_urls TEXT COMMENT '设备图片URLs(JSON格式)',
    stock_quantity INT NOT NULL DEFAULT 0 COMMENT '库存总数量',
    available_quantity INT NOT NULL DEFAULT 0 COMMENT '可借用数量',

    status VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '设备状态：正常,维修中,报废',
    specifications TEXT COMMENT '设备技术规格',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标志',
    
    UNIQUE INDEX uk_equipment_serial (serial_number),
    INDEX idx_equipment_category (category),
    INDEX idx_equipment_status (status),
    INDEX idx_equipment_available (available_quantity),
    INDEX idx_equipment_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备信息表';

-- 借还记录表
DROP TABLE IF EXISTS borrow_records;
CREATE TABLE borrow_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '借还记录ID',
    user_id BIGINT NOT NULL COMMENT '借用用户ID',
    equipment_id BIGINT NOT NULL COMMENT '设备ID',
    quantity INT NOT NULL COMMENT '借用数量',
    expected_return_time DATETIME NOT NULL COMMENT '预计归还时间',
    actual_return_time DATETIME COMMENT '实际归还时间',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING,APPROVED,REJECTED,BORROWED,RETURNED,OVERDUE',
    borrow_reason VARCHAR(500) COMMENT '借用原因',
    approval_notes VARCHAR(500) COMMENT '审批备注',
    approved_by BIGINT COMMENT '审批人ID',
    approval_time DATETIME COMMENT '审批时间',
    return_notes VARCHAR(500) COMMENT '归还备注',
    damage_description VARCHAR(500) COMMENT '损坏描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标志',
    
    INDEX idx_borrow_user (user_id),
    INDEX idx_borrow_equipment (equipment_id),
    INDEX idx_borrow_status (status),
    INDEX idx_borrow_expected_return (expected_return_time),
    INDEX idx_borrow_created_at (created_at),
    INDEX idx_borrow_deleted (deleted),
    
    CONSTRAINT fk_borrow_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_borrow_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE,
    CONSTRAINT fk_borrow_approver FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备借还记录表';

-- 公告表
DROP TABLE IF EXISTS announcements;
CREATE TABLE announcements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '公告ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    published BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否发布',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级(数字越大优先级越高)',
    view_count BIGINT NOT NULL DEFAULT 0 COMMENT '查看次数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标志',
    
    INDEX idx_announcement_published (published),
    INDEX idx_announcement_priority (priority),
    INDEX idx_announcement_created_by (created_by),
    INDEX idx_announcement_created_at (created_at),
    INDEX idx_announcement_deleted (deleted),
    
    CONSTRAINT fk_announcement_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告信息表';

-- 晚自习打卡配置表
DROP TABLE IF EXISTS study_checkin_configs;
CREATE TABLE study_checkin_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    location VARCHAR(100) NOT NULL COMMENT '打卡地点',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    description VARCHAR(500) COMMENT '配置描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标志',
    
    INDEX idx_checkin_config_location (location),
    INDEX idx_checkin_config_active (active),
    INDEX idx_checkin_config_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='晚自习打卡配置表';

-- 晚自习打卡记录表
DROP TABLE IF EXISTS study_checkins;
CREATE TABLE study_checkins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '打卡记录ID',
    user_id BIGINT NOT NULL COMMENT '打卡用户ID',
    checkin_date DATE NOT NULL COMMENT '打卡日期',
    checkin_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
    location VARCHAR(100) NOT NULL COMMENT '打卡地点',
    device_info VARCHAR(200) COMMENT '设备信息(用于限制)',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    notes VARCHAR(500) COMMENT '备注信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标志',
    
    UNIQUE INDEX uk_checkin_user_date_device (user_id, checkin_date, device_info),
    INDEX idx_checkin_user_date (user_id, checkin_date),
    INDEX idx_checkin_location (location),
    INDEX idx_checkin_date (checkin_date),
    INDEX idx_checkin_deleted (deleted),
    
    CONSTRAINT fk_checkin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='晚自习打卡记录表';

-- 执勤排班表
DROP TABLE IF EXISTS duty_schedules;
CREATE TABLE duty_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '排班ID',
    user_id BIGINT NOT NULL COMMENT '执勤用户ID',
    day_of_week INT NOT NULL COMMENT '星期几(1=周一,7=周日)',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    notes VARCHAR(500) COMMENT '备注信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标志',
    
    UNIQUE INDEX uk_duty_user_day_time (user_id, day_of_week, start_time),
    INDEX idx_duty_schedule_user (user_id),
    INDEX idx_duty_schedule_day (day_of_week),
    INDEX idx_duty_schedule_active (active),
    INDEX idx_duty_schedule_deleted (deleted),
    
    CONSTRAINT fk_duty_schedule_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_duty_day_of_week CHECK (day_of_week BETWEEN 1 AND 7)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='办公室执勤排班表';

-- 执勤记录表
DROP TABLE IF EXISTS duty_records;
CREATE TABLE duty_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '执勤记录ID',
    user_id BIGINT NOT NULL COMMENT '执勤用户ID',
    duty_schedule_id BIGINT NOT NULL COMMENT '执勤排班ID',
    duty_date DATE NOT NULL COMMENT '执勤日期',
    checkin_time DATETIME COMMENT '签到时间',
    checkout_time DATETIME COMMENT '签退时间',
    actual_start_time DATETIME COMMENT '实际开始时间',
    actual_end_time DATETIME COMMENT '实际结束时间',
    status VARCHAR(20) NOT NULL DEFAULT '待执勤' COMMENT '执勤状态：待执勤,执勤中,已完成,缺勤,请假',
    notes VARCHAR(500) COMMENT '备注信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标志',
    
    INDEX idx_duty_record_user_date (user_id, duty_date),
    INDEX idx_duty_record_schedule (duty_schedule_id),
    INDEX idx_duty_record_date (duty_date),
    INDEX idx_duty_record_status (status),
    INDEX idx_duty_record_deleted (deleted),
    
    CONSTRAINT fk_duty_record_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_duty_record_schedule FOREIGN KEY (duty_schedule_id) REFERENCES duty_schedules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='办公室执勤记录表';

-- 请假申请表
DROP TABLE IF EXISTS leave_requests;
CREATE TABLE leave_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '请假申请ID',
    user_id BIGINT NOT NULL COMMENT '申请用户ID',
    leave_type VARCHAR(50) NOT NULL COMMENT '请假类型：晚自习请假,办公室执勤请假',
    leave_date DATE NOT NULL COMMENT '请假日期',
    reason VARCHAR(500) NOT NULL COMMENT '请假原因',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '申请状态：PENDING,APPROVED,REJECTED',
    approved_by BIGINT COMMENT '审批人ID',
    approval_time DATETIME COMMENT '审批时间',
    approval_notes VARCHAR(500) COMMENT '审批备注',
    reference_id BIGINT COMMENT '关联对象ID',
    reference_type VARCHAR(50) COMMENT '关联对象类型：DUTY_SCHEDULE,STUDY_CHECKIN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标志',
    
    INDEX idx_leave_user_date (user_id, leave_date),
    INDEX idx_leave_type (leave_type),
    INDEX idx_leave_status (status),
    INDEX idx_leave_reference (reference_id, reference_type),
    INDEX idx_leave_created_at (created_at),
    INDEX idx_leave_deleted (deleted),
    
    CONSTRAINT fk_leave_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_approver FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假申请表';

-- ====================================
-- 初始数据插入
-- ====================================

-- 插入默认部门
INSERT INTO departments (name, type, description) VALUES
('摄影部', 'PHOTOGRAPHY', '负责活动摄影、图片拍摄、影像记录等专业摄影工作'),
('采编部', 'EDITING', '负责新闻采集、内容编辑、稿件审查等编辑工作'),
('审核部', 'REVIEW', '负责内容审核、质量把控、流程监督等审核工作'),
('宣传部', 'PUBLICITY', '负责对外宣传、活动推广、媒体联络等宣传工作');

-- 插入默认管理员用户 (密码: 123456，使用BCrypt加密)
INSERT INTO users (username, password, real_name, email, role, enabled) VALUES
('admin', '$2a$10$N8tQk2jKZb2oE8HFvDN4xuwt5M8kE3z0FQNZSvP3XzBjKJH3Lrz8i', '系统管理员', 'admin@photography.com', 'ADMIN', true);

-- 插入默认晚自习打卡配置
INSERT INTO study_checkin_configs (location, start_time, end_time, active, description, created_at, updated_at, deleted) VALUES
('图书馆自习室', '19:00:00', '21:30:00', TRUE, '图书馆主要晚自习区域，环境安静适合学习', NOW(), NOW(), FALSE),
('教学楼A座自习室', '19:00:00', '21:30:00', TRUE, '教学楼A座专用自习室，设施完善', NOW(), NOW(), FALSE),
('教学楼B座自习室', '19:00:00', '21:30:00', TRUE, '教学楼B座专用自习室，空间宽敞', NOW(), NOW(), FALSE);

-- 插入示例设备
INSERT INTO equipment (name, category, serial_number, description, stock_quantity, available_quantity, specifications) VALUES
('佳能单反相机', '相机', 'CAM001', '专业级全画幅单反相机，适合各类摄影活动和专业拍摄', 2, 2, '3040万像素CMOS传感器，61点自动对焦系统，4K视频录制'),
('索尼微单相机', '相机', 'CAM002', '高分辨率全画幅微单相机，轻便高画质', 1, 1, '6100万像素传感器，实时追踪对焦，8K视频录制'),
('尼康专业镜头', '镜头', 'LENS001', '专业标准变焦镜头，适用于多种拍摄场景', 3, 3, '24-70mm焦距范围，f/2.8恒定光圈，VR光学防抖'),
('曼富图碳纤维三脚架', '三脚架', 'TRI001', '轻量化碳纤维专业三脚架，稳定性强', 5, 5, '碳纤维材质，最大承重12kg，4节腿管调节'),
('神牛影室闪光灯', '闪光灯', 'FLASH001', '便携式专业影室闪光灯，功率强劲', 2, 2, '600Ws输出功率，TTL/HSS支持，2.4G无线引闪'),
('大疆航拍无人机', '无人机', 'DRONE001', '专业航拍无人机，画质优秀续航持久', 1, 1, '4/3 CMOS传感器，5.1K视频录制，最长46分钟续航'),
('索尼数字录音设备', '录音设备', 'AUDIO001', '便携式专业数字录音机，音质清晰', 2, 2, '32bit Float录制，双XLR输入，专业级音频处理');

-- 插入系统欢迎公告
INSERT INTO announcements (title, content, created_by, published, priority, view_count, created_at, updated_at, deleted) VALUES
('融媒体管理系统正式启用通知', 
'各位老师和同学们好！\n\n融媒体管理系统现已正式上线，主要功能包括：\n1. 设备借还管理 - 便捷的器材借用和归还流程\n2. 晚自习打卡 - 规范的考勤管理\n3. 办公室执勤 - 完善的值班安排\n4. 公告发布 - 及时的信息通知\n\n请大家按照相关规定使用系统，如有问题请联系管理员。\n\n祝大家使用愉快！', 
1, true, 10, 0, NOW(), NOW(), FALSE),

('设备借用管理规定', 
'为规范设备使用，特制定以下规定：\n\n1. 借用流程：\n   - 在线提交借用申请\n   - 等待管理员审核\n   - 审核通过后方可借用\n\n2. 使用规范：\n   - 爱护设备，按规范操作\n   - 禁止私自拆解或维修\n   - 及时报告设备故障\n\n3. 归还要求：\n   - 按约定时间归还\n   - 归还时检查设备状态\n   - 如有损坏及时说明\n\n请大家严格遵守相关规定！', 
1, true, 8, 0, NOW(), NOW(), FALSE),

('晚自习打卡注意事项', 
'晚自习打卡相关规定：\n\n1. 打卡时间：每日19:00-21:30\n2. 打卡地点：图书馆自习室、教学楼A/B座\n3. 特殊规定：\n   - 每个设备每日只允许一个账号打卡\n   - 如需请假请提前在系统中申请\n   - 连续缺卡将影响考勤记录\n\n4. 技术支持：\n   - 如遇打卡异常请及时联系管理员\n   - 系统会记录IP地址和设备信息\n\n感谢大家的配合！', 
1, true, 7, 0, NOW(), NOW(), FALSE);

-- 完成提示
SELECT '==================================' as '';
SELECT '数据库初始化完成！' as message;
SELECT '==================================' as '';
SELECT '默认管理员账户信息：' as '';
SELECT '用户名: admin' as username;
SELECT '密码: 123456' as password;
SELECT '==================================' as '';
SELECT '请及时修改默认密码以确保系统安全！' as security_warning;
SELECT '==================================' as '';

COMMIT;
