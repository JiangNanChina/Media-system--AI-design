# 融媒体管理系统 - 数据库初始化指南

本目录包含了融媒体管理系统的数据库初始化脚本，请根据需要选择合适的脚本执行。

## 📁 脚本文件说明

### 1. `production_init.sql` - 生产环境推荐 ⭐
- **适用场景**: 生产环境、正式部署
- **特点**: 
  - 完整的表结构定义
  - 详细的字段注释
  - 完善的索引和约束
  - 基础示例数据
  - 安全的外键约束
- **推荐指数**: ⭐⭐⭐⭐⭐

### 2. `simple_init.sql` - 快速开始
- **适用场景**: 开发测试、快速体验
- **特点**:
  - 简化的表结构
  - 基本的索引
  - 必要的初始数据
  - 快速执行
- **推荐指数**: ⭐⭐⭐⭐

### 3. `init_database.sql` - 功能完整
- **适用场景**: 完整功能展示、高级用户
- **特点**:
  - 包含视图和存储过程
  - 数据库触发器
  - 高级统计功能
  - 完整的示例数据
- **推荐指数**: ⭐⭐⭐

## 🚀 快速部署步骤

### 步骤1: 准备数据库环境
```bash
# 确保MySQL服务运行
sudo systemctl start mysql

# 登录MySQL
mysql -u root -p
```

### 步骤2: 执行初始化脚本
```sql
-- 推荐使用生产环境脚本
source /path/to/docs/production_init.sql;

-- 或者使用简化版本
source /path/to/docs/simple_init.sql;
```

### 步骤3: 验证安装
```sql
-- 检查数据库
SHOW DATABASES LIKE 'photography_system';

-- 检查表结构
USE photography_system;
SHOW TABLES;

-- 验证数据
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM departments;
```

## 🔧 配置应用连接

修改 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/photography_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 👤 默认账户信息

### 管理员账户
- **用户名**: `admin`
- **密码**: `123456`
- **角色**: 管理员
- **邮箱**: `admin@photography.com`

> ⚠️ **安全提示**: 首次登录后请立即修改默认密码！

## 🏢 预设部门

系统会自动创建以下部门：
1. **摄影部** (PHOTOGRAPHY) - 负责活动摄影、图片拍摄等工作
2. **采编部** (EDITING) - 负责新闻采集、内容编辑等工作  
3. **审核部** (REVIEW) - 负责内容审核、质量把控等工作
4. **宣传部** (PUBLICITY) - 负责对外宣传、推广等工作

## 📷 示例设备

脚本会预设以下设备类别的示例数据：
- **相机**: 佳能5D4、索尼α7RV
- **镜头**: 尼康24-70mm
- **三脚架**: 曼富图碳纤维
- **闪光灯**: 神牛AD600Pro
- **无人机**: 大疆Mavic 3
- **录音设备**: 索尼PCM-D100

## 🕐 打卡配置

默认晚自习打卡地点：
- **图书馆自习室**: 19:00-21:30
- **教学楼A座**: 19:00-21:30  
- **教学楼B座**: 19:00-21:30

## 🗂️ 数据库结构概览

### 核心表结构
```
departments          (部门表)
├── users            (用户表)
│   ├── borrow_records       (借还记录)
│   ├── study_checkins       (晚自习打卡)
│   ├── duty_records         (执勤记录)  
│   ├── leave_requests       (请假申请)
│   └── announcements        (公告 - 创建人)
├── equipment        (设备表)
└── study_checkin_configs    (打卡配置)
└── duty_schedules          (执勤排班)
```

### 表关系说明
- `users` ↔ `departments`: 多对一 (用户属于部门)
- `users` ↔ `borrow_records`: 一对多 (用户可有多个借还记录)
- `equipment` ↔ `borrow_records`: 一对多 (设备可有多个借还记录)
- `users` ↔ `study_checkins`: 一对多 (用户可有多个打卡记录)
- `users` ↔ `duty_schedules`: 一对多 (用户可有多个执勤排班)

## 🔍 常用查询

### 查看用户信息
```sql
SELECT u.username, u.real_name, d.name as department 
FROM users u 
LEFT JOIN departments d ON u.department_id = d.id 
WHERE u.deleted = false;
```

### 查看设备库存
```sql
SELECT name, category, serial_number, stock_quantity, available_quantity 
FROM equipment 
WHERE deleted = false;
```

### 查看今日打卡情况
```sql
SELECT u.real_name, sc.location, sc.checkin_time 
FROM study_checkins sc 
JOIN users u ON sc.user_id = u.id 
WHERE sc.checkin_date = CURDATE();
```

## 🛠️ 故障排除

### 常见问题

#### 1. 字符集问题
```sql
-- 检查数据库字符集
SHOW CREATE DATABASE photography_system;

-- 如果字符集不正确，重新创建
DROP DATABASE photography_system;
CREATE DATABASE photography_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2. 外键约束错误
```sql
-- 检查外键
SELECT * FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE REFERENCED_TABLE_SCHEMA = 'photography_system';

-- 禁用外键检查 (临时)
SET FOREIGN_KEY_CHECKS = 0;
-- 执行操作
SET FOREIGN_KEY_CHECKS = 1;
```

#### 3. 权限问题
```sql
-- 创建专用数据库用户
CREATE USER 'photography_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON photography_system.* TO 'photography_user'@'localhost';
FLUSH PRIVILEGES;
```

## 📊 性能优化建议

### 1. 索引优化
主要表都已创建必要索引，如需自定义查询可添加：

```sql
-- 为常用查询添加复合索引
CREATE INDEX idx_borrow_user_status_date ON borrow_records(user_id, status, created_at);
CREATE INDEX idx_checkin_location_date ON study_checkins(location, checkin_date);
```

### 2. 定期维护
```sql
-- 分析表统计信息
ANALYZE TABLE users, equipment, borrow_records;

-- 优化表
OPTIMIZE TABLE users, equipment, borrow_records;
```

## 🔄 数据备份

### 备份命令
```bash
# 完整备份
mysqldump -u root -p photography_system > photography_backup_$(date +%Y%m%d).sql

# 仅备份结构
mysqldump -u root -p --no-data photography_system > photography_structure.sql

# 仅备份数据
mysqldump -u root -p --no-create-info photography_system > photography_data.sql
```

### 恢复命令
```bash
# 恢复数据库
mysql -u root -p photography_system < photography_backup_20240101.sql
```

## 📞 技术支持

如遇到问题：
1. 检查MySQL版本是否兼容 (推荐8.0+)
2. 确认字符集设置正确
3. 验证用户权限充足
4. 查看MySQL错误日志

---

**祝您使用愉快！** 🎉
