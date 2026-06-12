# 📋 API调用规范

## 🎯 目标
统一前端API调用规范，避免重复前缀错误，提高代码质量和维护性。

## 📐 规范说明

### ✅ 正确的API调用方式

由于`axios`实例已配置`baseURL: '/api'`，所有API调用**不应**包含`/api`前缀：

```javascript
// ✅ 正确 - 不包含 /api 前缀
await request.get('/users')
await request.post('/users', userData)  
await request.put('/users/123', userData)
await request.delete('/users/123')

// ✅ 正确 - 复杂路径也不包含 /api 前缀
await request.get('/leave-requests/statistics')
await request.post('/leave-requests/submit', requestData)
await request.put('/duty/schedules/123', scheduleData)
```

### ❌ 错误的API调用方式

```javascript
// ❌ 错误 - 包含重复的 /api 前缀
await request.get('/api/users')           // 实际请求: /api/api/users
await request.post('/api/users', data)   // 实际请求: /api/api/users
await request.put('/api/users/123', data) // 实际请求: /api/api/users/123
```

## 🔧 axios配置说明

当前`axios`实例配置 (`Photography-UI/src/utils/request.js`):
```javascript
const request = axios.create({
  baseURL: '/api',  // 自动添加 /api 前缀
  timeout: 10000
})
```

## 📂 API路径分类

### 1. 用户管理 API
```javascript
// 用户相关
GET    /users              // 获取用户列表
POST   /users              // 创建用户
PUT    /users/{id}         // 更新用户
DELETE /users/{id}         // 删除用户（软删除）
DELETE /users/{id}/physical // 物理删除用户

// 认证相关
POST   /auth/login         // 用户登录
POST   /auth/register      // 用户注册
POST   /auth/logout        // 用户登出
```

### 2. 设备管理 API
```javascript
GET    /equipment          // 获取设备列表
POST   /equipment          // 创建设备
PUT    /equipment/{id}     // 更新设备
DELETE /equipment/{id}     // 删除设备

// 借还管理
GET    /borrows            // 获取借还记录
POST   /borrows/submit     // 提交借用申请
PUT    /borrows/{id}/return // 归还设备
```

### 3. 请假管理 API
```javascript
GET    /leave-requests               // 获取请假列表
POST   /leave-requests/submit        // 提交请假申请
PUT    /leave-requests/{id}/approve  // 审批请假
DELETE /leave-requests/{id}          // 删除请假（软删除）
DELETE /leave-requests/{id}/physical // 物理删除请假
```

### 4. 执勤管理 API
```javascript
GET    /duty/schedules      // 获取执勤排班
POST   /duty/schedules      // 创建执勤排班
PUT    /duty/schedules/{id} // 更新执勤排班
DELETE /duty/schedules/{id} // 删除执勤排班

GET    /duty/records        // 获取执勤记录
POST   /duty/checkin        // 执勤签到
PUT    /duty/checkout       // 执勤签退
```

### 5. 打卡管理 API
```javascript
GET    /checkin/configurations    // 获取打卡配置
POST   /checkin/configurations    // 创建打卡配置
PUT    /checkin/configurations/{id} // 更新打卡配置

GET    /checkin/records          // 获取打卡记录
POST   /checkin/submit           // 提交打卡
```

### 6. 公告管理 API
```javascript
GET    /announcements        // 获取公告列表
POST   /announcements        // 创建公告
PUT    /announcements/{id}   // 更新公告
DELETE /announcements/{id}   // 删除公告

// 公开接口（无需认证）
GET    /announcements/public // 获取公开公告
```

### 7. 部门管理 API
```javascript
GET    /departments          // 获取部门列表
GET    /departments/list     // 获取简单部门列表（注册时使用）
POST   /departments          // 创建部门
PUT    /departments/{id}     // 更新部门
DELETE /departments/{id}     // 删除部门
```

## 🔍 公开API列表

以下API无需Token验证，已在后端SecurityConfig中配置：
```javascript
// 认证相关
POST   /auth/login
POST   /auth/register

// 公开资源
GET    /departments/list              // 注册时获取部门
GET    /equipment-categories/active   // 获取激活的设备分类
GET    /announcements/public          // 公开公告

// 静态资源
GET    /uploads/**                    // 上传文件访问
```

## 🚨 常见错误案例

### 错误类型1: 重复前缀
```javascript
// ❌ 错误 - 导致 /api/api/users
await request.get('/api/users')

// ✅ 正确
await request.get('/users')
```

### 错误类型2: 不一致的路径风格
```javascript
// ❌ 不一致
await request.get('/api/leave-requests')  // 有前缀
await request.get('/duty/schedules')      // 无前缀

// ✅ 一致
await request.get('/leave-requests')
await request.get('/duty/schedules')
```

## 🛠️ 开发建议

### 1. 代码检查清单
- [ ] 所有API调用都不包含`/api`前缀
- [ ] 路径使用kebab-case（短横线命名）
- [ ] 资源使用复数形式（users, equipments, announcements）
- [ ] RESTful风格（GET获取, POST创建, PUT更新, DELETE删除）

### 2. 错误处理
```javascript
try {
  const response = await request.get('/users')
  // 处理成功响应
} catch (error) {
  if (error !== 'cancel') {
    console.error('API调用失败:', error)
    ElMessage.error('操作失败')
  }
}
```

### 3. 参数传递
```javascript
// GET请求 - 使用params
await request.get('/users', { 
  params: { page: 0, size: 10, keyword: 'search' } 
})

// POST/PUT请求 - 数据在请求体
await request.post('/users', userData)
await request.put('/users/123', userData)
```

## 📝 检查工具

可以使用以下命令检查项目中的API调用：

```bash
# 查找可能的重复前缀
grep -r "'/api/" Photography-UI/src/

# 查找所有API调用
grep -r "request\.\(get\|post\|put\|delete\)" Photography-UI/src/
```

## 🔄 版本历史

- **v1.0** (2024-12-01): 初始版本，建立基本规范
- **v1.1** (2024-12-01): 修复重复前缀问题，统一调用风格

---

**📢 重要提醒**: 
1. 所有新的API调用都必须遵循此规范
2. 发现不规范的调用请及时修复
3. 代码review时重点检查API路径
4. 如有疑问请参考此文档或咨询团队
