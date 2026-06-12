-- 融媒体管理系统 - 简化版数据库初始化脚本
-- 仅包含基本表结构和必需的初始数据

-- 创建数据库
CREATE DATABASE IF NOT EXISTS photography_system 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE photography_system;

-- 1. 部门表
CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

-- 2. 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),

    avatar_url VARCHAR(500),
    role VARCHAR(10) DEFAULT 'MEMBER',
    enabled BOOLEAN DEFAULT TRUE,
    department_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- 3. 设备表
CREATE TABLE equipment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,

    category VARCHAR(50) NOT NULL,
    serial_number VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,

    image_urls TEXT,
    stock_quantity INT DEFAULT 0,
    available_quantity INT DEFAULT 0,

    status VARCHAR(20) DEFAULT '正常',
    specifications TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

-- 4. 借还记录表
CREATE TABLE borrow_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    expected_return_time DATETIME NOT NULL,
    actual_return_time DATETIME,
    status VARCHAR(20) DEFAULT 'PENDING',
    borrow_reason VARCHAR(500),
    approval_notes VARCHAR(500),
    approved_by BIGINT,
    approval_time DATETIME,
    return_notes VARCHAR(500),
    damage_description VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- 5. 公告表
CREATE TABLE announcements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    created_by BIGINT NOT NULL,
    published BOOLEAN DEFAULT FALSE,
    priority INT DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 6. 晚自习打卡配置表
CREATE TABLE study_checkin_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    location VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    description VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

-- 7. 晚自习打卡记录表
CREATE TABLE study_checkins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    checkin_date DATE NOT NULL,
    checkin_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    location VARCHAR(100) NOT NULL,
    device_info VARCHAR(200),
    ip_address VARCHAR(45),
    notes VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 8. 执勤排班表
CREATE TABLE duty_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    day_of_week INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    notes VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 9. 执勤记录表
CREATE TABLE duty_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    duty_schedule_id BIGINT NOT NULL,
    duty_date DATE NOT NULL,
    checkin_time DATETIME,
    checkout_time DATETIME,
    actual_start_time DATETIME,
    actual_end_time DATETIME,
    status VARCHAR(20) DEFAULT '待执勤',
    notes VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (duty_schedule_id) REFERENCES duty_schedules(id)
);

-- 10. 请假申请表
CREATE TABLE leave_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    leave_date DATE NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    approved_by BIGINT,
    approval_time DATETIME,
    approval_notes VARCHAR(500),
    reference_id BIGINT,
    reference_type VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- 插入基础数据
-- 默认部门
INSERT INTO departments (name, type, description, created_at, updated_at, deleted) VALUES
('摄影部', 'PHOTOGRAPHY', '负责活动摄影、图片拍摄等工作', NOW(), NOW(), FALSE),
('采编部', 'EDITING', '负责新闻采集、内容编辑等工作', NOW(), NOW(), FALSE),
('审核部', 'REVIEW', '负责内容审核、质量把控等工作', NOW(), NOW(), FALSE),
('宣传部', 'PUBLICITY', '负责对外宣传、推广等工作', NOW(), NOW(), FALSE);

-- 默认管理员 (密码: 123456)
INSERT INTO users (username, password, real_name, email, role, created_at, updated_at, deleted) VALUES
('admin', '$2a$10$N8tQk2jKZb2oE8HFvDN4xuwt5M8kE3z0FQNZSvP3XzBjKJH3Lrz8i', '系统管理员', 'admin@photography.com', 'ADMIN', NOW(), NOW(), FALSE);

-- 默认打卡配置
INSERT INTO study_checkin_configs (location, start_time, end_time, active, description, created_at, updated_at, deleted) VALUES
('图书馆自习室', '19:00:00', '21:30:00', TRUE, '图书馆晚自习打卡地点', NOW(), NOW(), FALSE),
('教学楼A座', '19:00:00', '21:30:00', TRUE, '教学楼A座自习室', NOW(), NOW(), FALSE);

-- 示例设备
INSERT INTO equipment (name, category, serial_number, stock_quantity, available_quantity) VALUES
('佳能单反相机', '相机', 'CAM001', 2, 2),
('索尼微单相机', '相机', 'CAM002', 1, 1),
('尼康镜头', '镜头', 'LENS001', 3, 3),
('曼富图三脚架', '三脚架', 'TRI001', 5, 5);

-- 欢迎公告
INSERT INTO announcements (title, content, created_by, published, priority, view_count, created_at, updated_at, deleted) VALUES
('欢迎使用融媒体管理系统', '系统已成功部署，默认管理员账户为 admin/123456，请及时修改密码！', 1, true, 10, 0, NOW(), NOW(), FALSE);

SELECT 'Database initialized successfully!' as message;
SELECT 'Login: admin / 123456' as admin_info;
