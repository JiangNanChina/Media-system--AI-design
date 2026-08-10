# 校融媒体系统升级说明

## 升级前

1. 停止后端和定时任务，确认没有正在进行的上传或借还操作。
2. 备份 MySQL：`mysqldump --single-transaction --routines photography_system > photography_system_before_media.sql`。
3. 完整备份 `uploads/` 和 `private-uploads/`，并校验备份可读取。
4. 准备 `DB_PASSWORD`、至少 64 字节的 `JWT_SECRET`、独立的 `MAINTENANCE_TOKEN_SECRET`，以及 32 字节 Base64 格式的 `CONFIG_ENCRYPTION_KEY`。

## 执行迁移

先执行可重复的结构迁移：

```bash
mysql photography_system < docs/migration/001_media_system_schema_mysql8.sql
```

确认业务历史允许清空后，再显式执行清理脚本：

```bash
mysql --init-command="SET @CONFIRM_MEDIA_HISTORY_CLEANUP='YES'" photography_system < docs/migration/002_clear_business_history_mysql8.sql
```

清理脚本保留用户、部门、设备分类、设备、公告、头像、设备图片、Logo、背景和非敏感站点配置。它会清空借还、请假、签到配置与记录、执勤排班与记录、设备绑定和相关审计数据。脚本未收到确认变量时会直接失败。

## 首次启动

1. 使用生产配置启动后端。启动迁移器会把遗留明文密码原地升级为 BCrypt strength 12，并只记录迁移数量。
2. 检查日志中没有密码、SMTP 授权码或令牌内容。
3. 由超级管理员重新填写 QQ SMTP 授权码；旧明文授权码不会迁移。
4. 配置落地页校园视频/图片、校园特色、部门风采、抖音与微信链接。
5. 验证超级管理员、主任、部长、指导老师和部员的权限矩阵后再开放访问。

## 回滚

停止新版本，恢复升级前数据库以及 `uploads/`、`private-uploads/` 的同一时间点备份，然后部署原版本。不要只回滚数据库或文件系统其中之一。

