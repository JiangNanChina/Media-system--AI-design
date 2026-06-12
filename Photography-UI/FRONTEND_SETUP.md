# 融媒体管理系统前端设置指南

## 🚀 项目概述

这是一个基于 Vue 3 + Element Plus 的现代化融媒体管理系统前端项目。

### 技术栈
- **框架**: Vue 3 (Composition API)
- **UI库**: Element Plus
- **构建工具**: Vite
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **HTTP客户端**: Axios
- **工具库**: @vueuse/core

## 📁 项目结构

```
Photography-UI/
├── src/
│   ├── components/          # 公共组件
│   │   ├── Layout.vue       # 主布局组件
│   │   ├── AnnouncementDialog.vue  # 公告弹窗
│   │   └── ComingSoon.vue   # 开发中占位组件
│   ├── views/               # 页面组件
│   │   ├── Login.vue        # 登录页面
│   │   ├── Dashboard.vue    # 仪表板
│   │   ├── Profile.vue      # 个人中心
│   │   ├── UserManagement.vue      # 用户管理
│   │   ├── EquipmentManagement.vue # 设备管理
│   │   ├── BorrowManagement.vue    # 借还管理
│   │   ├── AnnouncementManagement.vue # 公告管理
│   │   ├── StudyCheckin.vue        # 晚自习打卡
│   │   ├── DutyManagement.vue      # 办公执勤
│   │   ├── LeaveManagement.vue     # 请假管理
│   │   └── NotFound.vue            # 404页面
│   ├── stores/              # 状态管理
│   │   └── user.js          # 用户状态
│   ├── router/              # 路由配置
│   │   └── index.js         # 路由定义
│   ├── utils/               # 工具函数
│   │   └── request.js       # HTTP请求封装
│   ├── App.vue              # 根组件
│   ├── main.js              # 入口文件
│   └── style.css            # 全局样式
├── public/                  # 静态资源
├── index.html               # HTML模板
├── vite.config.js           # Vite配置
└── package.json             # 项目依赖
```

## 🛠️ 开发环境设置

### 1. 安装依赖
```bash
cd Photography-UI
npm install
```

### 2. 启动开发服务器
```bash
npm run dev
```

### 3. 构建生产版本
```bash
npm run build
```

### 4. 预览生产版本
```bash
npm run preview
```

## 🔧 配置说明

### API 配置
- 后端 API 地址: `http://localhost:8080/api`
- 开发环境代理已配置，支持跨域请求

### 路由配置
- 使用 Vue Router 4 的 History 模式
- 包含路由守卫，自动处理登录验证和权限控制
- 支持懒加载，优化首屏加载性能

### 状态管理
- 使用 Pinia 管理全局状态
- 用户信息和登录状态持久化到 localStorage

### HTTP 请求
- 使用 Axios 封装，自动处理：
  - JWT Token 注入
  - 错误拦截和消息提示
  - 响应数据统一处理

## 🎨 UI 特性

### 设计风格
- 现代化扁平设计
- 响应式布局，支持移动端
- 优雅的动画过渡效果
- 统一的色彩体系

### 组件特性
- Element Plus 组件库
- 自定义主题配色
- 图标系统集成
- 国际化支持（中文）

### 页面布局
- 侧边栏导航
- 顶部工具栏
- 面包屑导航
- 公告通知系统

## 🔐 权限控制

### 角色系统
- **管理员**: 拥有所有功能权限
- **成员**: 基础功能权限

### 路由权限
- 自动根据用户角色显示/隐藏菜单
- 页面级权限控制
- 未授权访问自动重定向

## 📱 响应式设计

### 断点设置
- xs: < 768px (手机)
- sm: 768px - 992px (平板)
- md: 992px - 1200px (小桌面)
- lg: > 1200px (大桌面)

### 适配策略
- 移动端侧边栏自动折叠
- 表格组件支持横向滚动
- 按钮和表单控件适配触摸操作

## 🚦 开发状态

### ✅ 已完成功能
- [x] 项目基础架构
- [x] 用户认证系统
- [x] 路由和权限控制
- [x] 主布局组件
- [x] 登录页面
- [x] 仪表板页面
- [x] 个人中心页面
- [x] 公告系统
- [x] 响应式设计

### 🔄 开发中功能
- [ ] 用户管理页面
- [ ] 设备管理页面
- [ ] 借还管理页面
- [ ] 晚自习打卡页面
- [ ] 办公执勤页面
- [ ] 请假管理页面

## 🎯 下一步计划

1. **完善核心功能页面**
   - 用户管理 (CRUD + 权限分配)
   - 设备管理 (CRUD + 图片上传)
   - 借还管理 (申请 + 审核 + 归还)

2. **增强用户体验**
   - 数据表格优化
   - 表单验证增强
   - 加载状态优化

3. **功能扩展**
   - 数据导出功能
   - 批量操作支持
   - 高级搜索筛选

## 🤝 开发规范

### 代码风格
- 使用 Vue 3 Composition API
- 组件命名采用 PascalCase
- 文件命名采用 kebab-case
- 变量命名采用 camelCase

### 组件规范
- 单文件组件 (.vue)
- 逻辑复用使用 Composables
- 响应式数据使用 ref/reactive
- 事件处理使用 emit

### 提交规范
- feat: 新功能
- fix: 修复问题
- docs: 文档更新
- style: 样式调整
- refactor: 代码重构

## 📞 技术支持

如有问题，请参考：
1. Vue 3 官方文档
2. Element Plus 组件文档
3. Vite 构建工具文档
4. 项目内代码注释和示例
