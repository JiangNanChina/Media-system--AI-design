# 图片路径重复问题修复部署指南

## 📋 问题描述

**症状：**
- 设备图片上传后无法加载显示
- 图片路径出现多次重复，如：`/www/photography/uploads/uploads/uploads/equipment/xxx.jpg`
- 正确路径应该是：`/www/photography/uploads/equipment/xxx.jpg`

**根本原因：**
1. 配置文件中的路径配置不当，导致路径拼接时产生重复
2. `FileUploadConfig.java` 缺少路径规范化逻辑

## 🔧 修复内容

### 1. 配置文件优化
- 修改 `config/application-prod.yml`
- 添加配置说明注释，明确路径拼接规则

### 2. 代码增强
- 增强 `FileUploadConfig.java` 的路径处理逻辑
- 新增 `normalizePathToPreventDuplication()` 方法防止路径重复
- 新增详细的初始化日志输出

### 3. 数据库修复脚本
- 创建 `scripts/fix_duplicate_image_paths.sql`
- 修复现有数据库中的错误路径

## 🚀 部署步骤

### 步骤 1：备份数据

**在服务器上执行：**
```bash
# 1.1 备份数据库
cd /www/photography
mysqldump -u photography_system -p photography_system > backup_$(date +%Y%m%d_%H%M%S).sql

# 1.2 备份旧的 jar 包
cp photography-1.0.0.jar photography-1.0.0.jar.backup_$(date +%Y%m%d_%H%M%S)

# 1.3 备份配置文件
cp config/application-prod.yml config/application-prod.yml.backup_$(date +%Y%m%d_%H%M%S)
```

### 步骤 2：修复数据库中的错误路径

**登录数据库：**
```bash
mysql -u photography_system -p photography_system
```

**执行修复脚本：**
```sql
-- 方式1：直接在 MySQL 客户端中执行
source /www/photography/scripts/fix_duplicate_image_paths.sql;

-- 或者方式2：从本地上传后执行
-- 先将 scripts/fix_duplicate_image_paths.sql 上传到服务器
```

**或者从本地执行：**
```bash
# 在本地上传脚本到服务器
scp scripts/fix_duplicate_image_paths.sql root@你的服务器IP:/www/photography/scripts/

# SSH 到服务器执行
ssh root@你的服务器IP
cd /www/photography
mysql -u photography_system -p photography_system < scripts/fix_duplicate_image_paths.sql
```

**验证修复结果：**
```sql
-- 检查设备图片路径
SELECT id, name, image_url FROM equipment WHERE image_url IS NOT NULL LIMIT 10;

-- 应该看到类似这样的路径：
-- /uploads/equipment/abc123.jpg
-- /uploads/equipment/def456.png

-- 不应该有这样的路径：
-- /www/photography/uploads/uploads/uploads/equipment/xxx.jpg  ❌
-- /uploads/uploads/equipment/xxx.jpg  ❌
```

### 步骤 3：本地编译新版本

**在开发机器上（Windows）：**
```powershell
# 3.1 切换到项目目录
cd C:\Users\江楠\IdeaProjects\Photography

# 3.2 清理并编译
.\mvnw.cmd clean package -DskipTests

# 3.3 验证编译结果
# 确认 target\photography-1.0.0.jar 文件存在
dir target\photography-1.0.0.jar
```

### 步骤 4：上传到服务器

**使用 SCP 或 SFTP 上传文件：**

#### 方式A：使用 SCP（PowerShell）
```powershell
# 上传 jar 包
scp target\photography-1.0.0.jar root@你的服务器IP:/www/photography/

# 上传配置文件
scp config\application-prod.yml root@你的服务器IP:/www/photography/config/

# 上传数据库修复脚本（如果还没上传）
scp scripts\fix_duplicate_image_paths.sql root@你的服务器IP:/www/photography/scripts/
```

#### 方式B：使用宝塔面板
1. 登录宝塔面板
2. 进入文件管理
3. 导航到 `/www/photography/`
4. 上传 `photography-1.0.0.jar`
5. 导航到 `/www/photography/config/`
6. 上传 `application-prod.yml`

### 步骤 5：在服务器上部署

**SSH 连接到服务器：**
```bash
ssh root@你的服务器IP
cd /www/photography
```

**停止旧应用：**

#### 方式A：如果使用 PM2
```bash
pm2 stop photography
# 或
pm2 delete photography
```

#### 方式B：如果使用 systemd
```bash
systemctl stop photography
```

#### 方式C：手动停止
```bash
# 查找进程
ps aux | grep photography-1.0.0.jar

# 停止进程（替换 <PID> 为实际进程ID）
kill <PID>

# 或强制停止
pkill -f photography-1.0.0.jar
```

**启动新应用：**

#### 方式A：使用 PM2（推荐）
```bash
# 如果有 PM2 配置文件
pm2 start /www/photography/scripts/pm2.config.js

# 或直接启动
pm2 start java --name photography -- \
  -jar \
  -Dspring.profiles.active=prod \
  -Dfile.encoding=UTF-8 \
  /www/photography/photography-1.0.0.jar

# 保存 PM2 配置
pm2 save

# 设置开机自启
pm2 startup
```

#### 方式B：使用 nohup
```bash
nohup java -jar \
  -Dspring.profiles.active=prod \
  -Dfile.encoding=UTF-8 \
  photography-1.0.0.jar \
  > logs/app.log 2>&1 &
```

#### 方式C：使用 systemd
```bash
systemctl start photography
systemctl enable photography
```

### 步骤 6：验证部署

**6.1 检查应用启动日志：**
```bash
# PM2
pm2 logs photography --lines 100

# 或查看日志文件
tail -100 /www/photography/logs/application.log

# 查找路径配置日志
tail -200 /www/photography/logs/application.log | grep -E "解析后的|静态资源"
```

**预期日志输出：**
```
===== 文件上传路径配置初始化 =====
原始配置 - uploadPath: /www/photography/uploads/
原始配置 - avatarPath: avatars/
原始配置 - equipmentPath: equipment/
原始配置 - returnPath: returns/
===== 解析后的路径 =====
解析后的上传路径: /www/photography/uploads
解析后的头像路径: /www/photography/uploads/avatars
解析后的设备路径: /www/photography/uploads/equipment
解析后的归还图片路径: /www/photography/uploads/returns
================================
静态资源映射路径: file:/www/photography/uploads/
```

**⚠️ 注意：如果看到路径重复，说明配置有问题！**

**6.2 检查应用是否正常运行：**
```bash
# 检查端口是否监听
netstat -tlnp | grep 8080

# 或使用 ss 命令
ss -tlnp | grep 8080

# 检查进程
ps aux | grep photography
```

**6.3 测试 API：**
```bash
# 测试健康检查（如果有）
curl http://localhost:8080/actuator/health

# 或测试登录接口
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
```

**6.4 测试图片访问：**
```bash
# 检查 uploads 目录
ls -lh /www/photography/uploads/equipment/ | head -10

# 测试某个图片文件（替换为实际文件名）
curl -I http://你的域名/uploads/equipment/某个文件.jpg

# 应该返回 200 OK
```

### 步骤 7：前端验证

**7.1 清除浏览器缓存**
- 按 `Ctrl + Shift + Delete` 或 `Cmd + Shift + Delete`
- 清除缓存和 Cookie

**7.2 访问设备管理页面**
```
https://你的域名/equipment
```

**7.3 测试上传新图片**
1. 点击"添加设备"或编辑现有设备
2. 上传一张图片
3. 保存
4. 刷新页面，检查图片是否正常显示

**7.4 检查浏览器控制台**
- 打开浏览器开发者工具 (F12)
- 切换到 Network 标签
- 刷新页面
- 查看图片请求，确保：
  - 图片 URL 格式为：`/uploads/equipment/xxx.jpg`
  - HTTP 状态码为 200
  - 没有 404 错误

## 📊 监控与日志

### 查看实时日志
```bash
# PM2
pm2 logs photography --lines 200

# 或直接查看日志文件
tail -f /www/photography/logs/application.log
```

### 查看错误日志
```bash
# 查找错误
grep -i error /www/photography/logs/application.log | tail -50

# 查找图片路径相关错误
grep -i "uploads" /www/photography/logs/application.log | tail -50
```

## 🔍 故障排查

### 问题1：图片仍然无法显示

**检查路径格式：**
```sql
-- 查看数据库中的路径
SELECT id, name, image_url FROM equipment WHERE image_url IS NOT NULL LIMIT 10;
```

**如果路径仍然有问题，重新执行修复脚本：**
```bash
mysql -u photography_system -p photography_system < scripts/fix_duplicate_image_paths.sql
```

### 问题2：上传新图片后路径仍然重复

**检查配置文件：**
```bash
cat /www/photography/config/application-prod.yml | grep -A 5 "file:"
```

**应该看到：**
```yaml
file:
  upload:
    path: /www/photography/uploads/
    avatar-path: avatars/
    equipment-path: equipment/
    return-path: returns/
```

**如果配置错误，修正后重启应用**

### 问题3：启动日志显示路径重复

**检查 jar 包版本：**
```bash
# 确认使用的是新编译的版本
ls -lh /www/photography/photography-1.0.0.jar

# 查看文件修改时间，应该是最近的
```

**如果是旧版本，重新上传新版本**

### 问题4：Nginx 返回 404

**检查 Nginx 配置：**
```bash
cat /etc/nginx/conf.d/photography.conf
# 或
cat /www/server/panel/vhost/nginx/你的域名.conf
```

**应该包含静态资源代理：**
```nginx
location /uploads/ {
    alias /www/photography/uploads/;
    expires 30d;
    add_header Cache-Control "public";
    access_log off;
}
```

**重载 Nginx：**
```bash
nginx -t
nginx -s reload
# 或使用宝塔
bt reload nginx
```

### 问题5：文件权限问题

**检查并修复权限：**
```bash
# 检查当前权限
ls -la /www/photography/uploads/

# 修复权限
chown -R www:www /www/photography/uploads/
find /www/photography/uploads -type d -exec chmod 755 {} \;
find /www/photography/uploads -type f -exec chmod 644 {} \;

# 或者如果使用其他用户运行 Java
chown -R <运行用户>:<运行组> /www/photography/uploads/
```

## ✅ 验证清单

部署完成后，请逐项检查：

- [ ] **数据库修复完成**
  - [ ] 执行了修复脚本
  - [ ] 验证路径格式正确（`/uploads/equipment/xxx.jpg`）
  - [ ] 没有路径重复（不含 `/uploads/uploads/`）

- [ ] **应用部署成功**
  - [ ] jar 包成功上传
  - [ ] 配置文件正确
  - [ ] 应用正常启动

- [ ] **日志验证通过**
  - [ ] 启动日志显示正确路径
  - [ ] 没有错误日志
  - [ ] 路径配置无重复

- [ ] **功能测试通过**
  - [ ] 旧图片正常显示
  - [ ] 可以上传新图片
  - [ ] 新上传的图片正常显示
  - [ ] 浏览器无 404 错误

- [ ] **性能检查**
  - [ ] 应用响应正常
  - [ ] CPU 和内存占用正常
  - [ ] 没有频繁重启

## 📞 技术支持

如果遇到问题，请收集以下信息：

1. **应用启动日志：**
   ```bash
   tail -200 /www/photography/logs/application.log
   ```

2. **数据库路径样例：**
   ```sql
   SELECT id, name, image_url FROM equipment WHERE image_url IS NOT NULL LIMIT 5;
   ```

3. **Nginx 访问日志：**
   ```bash
   tail -100 /var/log/nginx/access.log | grep uploads
   ```

4. **Nginx 错误日志：**
   ```bash
   tail -100 /var/log/nginx/error.log
   ```

5. **文件系统检查：**
   ```bash
   ls -lh /www/photography/uploads/equipment/ | head -10
   ```

## 📝 相关文件

- `config/application-prod.yml` - 生产环境配置文件
- `src/main/java/com/example/photography/config/FileUploadConfig.java` - 文件上传配置类
- `scripts/fix_duplicate_image_paths.sql` - 数据库路径修复脚本
- `scripts/baota-deploy.sh` - 宝塔部署脚本（可选）

## 🎯 预期效果

修复完成后：

1. **数据库中的路径格式：**
   ```
   /uploads/equipment/abc-123-def.jpg
   /uploads/avatars/user-avatar.png
   /uploads/returns/return-image.jpg
   ```

2. **文件系统中的实际位置：**
   ```
   /www/photography/uploads/equipment/abc-123-def.jpg
   /www/photography/uploads/avatars/user-avatar.png
   /www/photography/uploads/returns/return-image.jpg
   ```

3. **前端访问的 URL：**
   ```
   https://你的域名/uploads/equipment/abc-123-def.jpg
   https://你的域名/uploads/avatars/user-avatar.png
   https://你的域名/uploads/returns/return-image.jpg
   ```

4. **HTTP 响应：**
   - 状态码：200 OK
   - Content-Type: image/jpeg 或 image/png
   - 图片正常显示

---

**最后更新：** 2025-10-14  
**版本：** 1.0

