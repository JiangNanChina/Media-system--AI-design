# 登录问题排查指南

## 问题描述
根据错误日志 `localhost-1756724186860.log`，前端在尝试登录时收到了"用户名或密码错误"的响应。

## 排查步骤

### 1. 检查后端服务状态

首先确认后端 Spring Boot 服务是否正常运行：

```bash
# 检查后端服务是否启动
curl http://localhost:8080/api/health
```

如果返回错误，说明后端服务没有启动，请：
1. 进入后端项目目录
2. 运行 `./mvnw spring-boot:run` 或通过 IDE 启动

### 2. 使用登录测试工具

我已经为您创建了一个专门的登录测试工具，可以帮助诊断问题：

1. 启动前端项目：
   ```bash
   cd Photography-UI
   npm run dev
   ```

2. 在浏览器中访问：
   ```
   http://localhost:3000/login-test
   ```

3. 使用测试工具进行以下检查：
   - **连接测试**：点击"测试后端连接"按钮
   - **预设账户测试**：使用默认的管理员或用户账户测试登录
   - **自定义测试**：输入自己的用户名密码进行测试

### 3. 检查默认用户账户

根据数据库初始化脚本，应该有以下默认账户：

**管理员账户：**
- 用户名：`admin`
- 密码：`123456`

**普通用户账户：**
- 用户名：`user1`
- 密码：`password123`

### 4. 检查数据库连接

确认数据库服务正常运行并且包含初始化数据：

```sql
-- 检查用户表是否有数据
SELECT id, username, real_name, role, enabled FROM users;

-- 检查密码是否正确加密
SELECT username, password FROM users WHERE username = 'admin';
```

### 5. 检查后端日志

查看后端控制台输出，寻找以下信息：
- 数据库连接状态
- 登录请求日志
- 密码验证过程
- 任何异常信息

### 6. 常见问题和解决方案

#### 问题1：后端服务未启动
**症状**：连接测试失败，显示网络错误
**解决方案**：
```bash
cd /path/to/backend/project
./mvnw spring-boot:run
```

#### 问题2：数据库连接失败
**症状**：后端启动时报数据库连接错误
**解决方案**：
1. 检查 MySQL 服务是否启动
2. 确认 `application.yml` 中的数据库配置正确
3. 确认数据库用户权限

#### 问题3：数据库未初始化
**症状**：连接正常但用户不存在
**解决方案**：
1. 运行数据库初始化脚本 `docs/init_database.sql`
2. 或运行简化版本 `docs/simple_init.sql`

#### 问题4：密码加密问题
**症状**：用户存在但密码验证失败
**解决方案**：
确认后端使用 BCrypt 加密，初始化脚本中的密码应该是加密后的。

#### 问题5：CORS 问题
**症状**：浏览器控制台显示 CORS 错误
**解决方案**：
确认后端 `SecurityConfig.java` 中已正确配置 CORS。

### 7. API 端点测试

可以使用 Postman 或 curl 直接测试登录 API：

```bash
# 测试登录 API
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

期望返回：
```json
{
  "success": true,
  "data": {
    "token": "eyJ...",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "role": "ADMIN"
    }
  }
}
```

### 8. 前端配置检查

确认前端配置正确：

1. **API 基础 URL**：检查 `src/utils/request.js` 中的 `baseURL`
2. **代理配置**：检查 `vite.config.js` 中的代理设置
3. **端口配置**：确认前后端端口不冲突

### 9. 如果问题仍然存在

请提供以下信息：
1. 后端控制台完整日志
2. 浏览器开发者工具中的网络请求详情
3. 数据库中 users 表的数据
4. 登录测试工具的测试结果

## 快速修复建议

1. **重启所有服务**：
   ```bash
   # 重启数据库
   sudo service mysql restart
   
   # 重启后端（如果正在运行，先停止再启动）
   ./mvnw spring-boot:run
   
   # 重启前端
   npm run dev
   ```

2. **重新初始化数据库**：
   ```bash
   mysql -u root -p photography_db < docs/simple_init.sql
   ```

3. **清除浏览器缓存**：
   - 清除 localStorage
   - 刷新页面
   - 重新尝试登录

通过以上步骤，应该能够定位和解决登录问题。如果问题依然存在，请使用登录测试工具获取详细的错误信息。
