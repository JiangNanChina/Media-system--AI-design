# 📘 开发指南

## 🎯 项目概述

本项目是一个摄影体管理系统，采用前后端分离架构：
- **后端**: Spring Boot + Spring Security + JPA
- **前端**: Vue 3 + Element Plus + Vite
- **数据库**: MySQL
- **认证**: JWT Token

## 🚀 快速开始

### 前端开发
```bash
cd Photography-UI
npm install
npm run dev
```

### 后端开发
```bash
./mvnw spring-boot:run
```

## 📐 API调用规范

### ⚠️ 重要规则
**所有API调用都不应包含`/api`前缀！**

### ✅ 正确示例
```javascript
// ✅ 正确 - 不包含 /api 前缀
await request.get('/users')
await request.post('/equipment', data)
await request.put('/leave-requests/123/approve', data)
await request.delete('/duty/schedules/456')
```

### ❌ 错误示例
```javascript
// ❌ 错误 - 包含重复的 /api 前缀
await request.get('/api/users')           // 实际请求: /api/api/users
await request.post('/api/equipment', data) // 实际请求: /api/api/equipment
```

### 📋 完整规范
详细的API调用规范请参考: [API_STANDARDS.md](./API_STANDARDS.md)

## 🔧 开发工具

### API检查脚本
```bash
# 检查项目中的API调用规范
bash scripts/api-check.sh

# 或使用Node.js版本（如果可用）
node scripts/check-api-calls.js
```

### ESLint规则
项目包含自定义ESLint规则来检测重复的API前缀，配置文件: `.eslintrc-api-check.js`

## 🎨 代码规范

### Vue组件
```vue
<template>
  <!-- 使用Element Plus组件 -->
  <el-button @click="handleClick">按钮</el-button>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// 响应式数据
const loading = ref(false)

// API调用
const handleClick = async () => {
  try {
    loading.value = true
    await request.post('/users', userData)
    ElMessage.success('操作成功')
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}
</script>
```

### API调用模式
```javascript
// 标准错误处理
try {
  const response = await request.get('/endpoint')
  // 处理成功响应
} catch (error) {
  if (error !== 'cancel') {
    console.error('API调用失败:', error)
    ElMessage.error('操作失败')
  }
}
```

## 🗂️ 项目结构

```
Photography-UI/
├── src/
│   ├── components/     # 通用组件
│   ├── views/         # 页面组件
│   ├── stores/        # Pinia状态管理
│   ├── router/        # 路由配置
│   ├── utils/         # 工具函数
│   └── assets/        # 静态资源
├── scripts/           # 开发脚本
├── API_STANDARDS.md   # API调用规范
└── DEVELOPMENT.md     # 开发指南
```

## 🔍 常见问题

### Q: API调用返回500错误
A: 检查是否有重复的`/api`前缀，使用检查脚本：`bash scripts/api-check.sh`

### Q: 如何添加新的API调用
A: 
1. 确保不包含`/api`前缀
2. 使用RESTful风格的URL
3. 添加适当的错误处理

### Q: 如何处理认证
A: 
- Token会自动添加到请求头
- 登录/注册等公开API会自动跳过Token验证
- Token过期会自动跳转到登录页

## 📦 依赖管理

### 前端主要依赖
- Vue 3
- Element Plus
- Vue Router
- Pinia
- Axios
- Vite

### 开发依赖
- ESLint
- Prettier
- Vite插件

## 🚀 部署指南

### 前端部署
```bash
npm run build
# 将 dist/ 目录部署到Web服务器
```

### 后端部署
```bash
./mvnw clean package
# 运行生成的JAR文件
java -jar target/photography-0.0.1-SNAPSHOT.jar
```

## 📝 更新日志

### v1.1.0 (2024-12-01)
- ✅ 统一API调用规范
- ✅ 修复重复API前缀问题
- ✅ 添加API检查工具
- ✅ 完善开发文档

### v1.0.0 (2024-11-01)
- 🎉 初始版本发布
- ✅ 基础功能实现

---

## 📞 支持

如有开发问题或建议，请：
1. 查看 [API_STANDARDS.md](./API_STANDARDS.md)
2. 运行检查脚本排查问题
3. 联系团队成员

**记住**: 所有API调用都不要包含`/api`前缀！ 🎯
