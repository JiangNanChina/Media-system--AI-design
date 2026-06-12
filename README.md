# Photography Management System

一个前后端分离的融媒体/摄影设备管理系统，后端基于 Spring Boot 3，前端基于 Vue 3、Vite、Element Plus 和 Pinia。系统围绕摄影器材资产、借还审批、晚自习签到、办公执勤、请假审批、公告发布、用户与部门管理等日常管理场景构建。

## 功能概览

### 用户与权限

- 支持登录、注册、Token 刷新、当前用户信息查询和 Token 校验。
- 使用 Spring Security + JWT 做认证授权，前端通过路由守卫控制登录态和管理员页面访问。
- 用户角色包含普通成员与管理员，管理员可维护用户、部门、设备分类、站点配置等后台数据。
- 支持用户头像上传、个人资料维护、修改密码、重置密码、启停账号。

### 部门与组织

- 支持部门列表、分页查询、搜索、详情、创建和统计。
- 内置部门初始化入口，便于快速建立基础组织结构。

### 摄影器材管理

- 支持器材分页、搜索、分类筛选、可借列表、详情、创建、图片上传、库存调整和状态统计。
- 支持器材分类管理，启动时会初始化默认分类：相机、镜头、三脚架、闪光灯、录音设备、无人机、其他。
- 支持器材图片、归还图片、头像等文件访问与维护。

### 借还审批

- 用户可提交器材借用申请、查看我的借还记录和当前借用。
- 管理端可查看全部记录、待审批、逾期记录、按状态/用户/器材筛选记录。
- 支持归还图片上传、借还统计、记录导出和软删除记录查看。

### 晚自习签到与考勤

- 支持签到、签退、今日状态、用户记录、全部记录、近期记录和统计。
- 支持签到配置管理，包括地点、场次、有效范围、可用配置、附近配置等。
- 支持二维码生成、签到审核、审核通过/拒绝、待审核数量统计。
- 支持按日期汇总、用户汇总、明细查询和 Excel 导出。

### 办公执勤

- 支持执勤排班管理、启停排班、按用户/星期/当前时段查询。
- 支持执勤签到、签退、今日记录、我的记录、全部记录、统计和导出。
- 支持调班申请、审批与个人调班记录。

### 请假管理

- 支持请假提交、审批、取消、详情、我的申请、待审批、紧急请假和请假校验。
- 支持请假附件上传、全局统计、类型统计、趋势统计和导出。
- 可关联签到配置与执勤排班，用于判断请假影响范围。

### 公告与站点配置

- 支持公告公开列表、分页、登录弹窗公告、最新公告、详情、搜索、我的公告、浏览量修复和统计。
- 管理员可创建和维护公告。
- 支持站点标题、Logo、背景图、邮件配置等站点配置，应用启动时自动初始化默认站点配置。

### 设备绑定与审计

- 支持查看我的登录设备/绑定设备。
- 管理端可查看设备列表、重置用户设备、统计、清理无效设备，并记录设备审计日志。

### 地图与定位

- 后端集成高德地图 Web API，提供地理编码、逆地理编码、地点搜索、周边搜索、驾车路线、IP 定位、静态地图、距离计算和签到位置校验。
- 前端包含地图选择、地图展示、定位辅助与签到位置相关工具。

### 导出与文档

- 使用 Apache POI 导出签到记录、借还记录、设备列表、用户列表、请假记录、执勤记录和签到统计。
- 使用 Springdoc OpenAPI 生成接口文档。

## 技术栈

### 后端

- Java 17
- Spring Boot 3.2.0
- Spring Web / Spring Data JPA / Spring Security / Validation
- Spring Mail / AOP / Actuator / WebFlux
- MySQL
- JWT：jjwt 0.12.3
- Springdoc OpenAPI
- Apache POI
- Lombok
- JUnit 5、Spring Security Test、H2 测试数据库

### 前端

- Vue 3.5
- Vite 7
- Vue Router 4
- Pinia
- Element Plus
- Axios
- Sass
- qrcode、jsqr
- @vueuse/core

## 项目结构

```text
.
├── pom.xml                         # Spring Boot 后端 Maven 配置
├── src
│   ├── main
│   │   ├── java/com/example/photography
│   │   │   ├── config              # 安全、跨域、上传、JPA、初始化等配置
│   │   │   ├── controller          # REST API 控制器
│   │   │   ├── dto                 # 请求/响应 DTO
│   │   │   ├── exception           # 全局异常处理
│   │   │   ├── model               # JPA 实体与枚举
│   │   │   ├── repository          # Spring Data JPA 仓库
│   │   │   ├── security            # JWT 过滤器
│   │   │   ├── service             # 业务接口与实现
│   │   │   └── util / utils        # JWT、文件、图片、坐标等工具
│   │   └── resources
│   │       └── application.yml     # 后端主配置
│   └── test                        # 后端测试
├── Photography-UI
│   ├── package.json                # 前端依赖与脚本
│   ├── vite.config.js              # Vite、代理和构建配置
│   └── src
│       ├── components              # 通用组件
│       ├── composables             # 组合式逻辑
│       ├── router                  # 路由与权限守卫
│       ├── stores                  # Pinia 状态
│       ├── utils                   # 请求、定位、图片、通知等工具
│       └── views                   # 页面视图
├── uploads                         # 本地上传文件目录
├── docs                            # 项目文档
├── scripts                         # 辅助脚本
└── www                             # Web 相关资源
```

## 环境要求

- JDK 17+
- Maven 3.8+，或使用项目内 Maven Wrapper
- Node.js 18+，建议使用 LTS 版本
- MySQL 8.x

## 后端配置

后端配置位于 `src/main/resources/application.yml`，也支持通过环境变量覆盖关键配置。

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/photography_system...` | MySQL 连接地址 |
| `DB_USERNAME` | `root` | 数据库用户名 |
| `DB_PASSWORD` | 空 | 数据库密码 |
| `JWT_SECRET` | `change-me-to-a-strong-512-bit-secret-before-deploy` | JWT 密钥，生产环境必须替换 |
| `ADMIN_SECRET_KEY` | `change-me-before-deploy` | 管理员注册密钥，生产环境必须替换 |
| `AMAP_WEB_API_KEY` | 空 | 高德 Web 服务 API Key |
| `AMAP_JS_API_KEY` | 空 | 高德 JS API Key |

默认后端服务端口为 `8080`，统一接口前缀为 `/api`。

## 前端配置

前端配置位于 `Photography-UI/.env` 或参考 `Photography-UI/env.example` 创建本地环境文件。

常用变量：

```env
VITE_APP_TITLE=融媒体设备管理系统
VITE_APP_BASE_API=/api
VITE_BMAP_API_KEY=YOUR_BMAP_API_KEY
```

开发服务器默认端口为 `3000`，`vite.config.js` 已将 `/api` 代理到 `http://localhost:8080`，并将 `/uploads` 代理到后端上传资源路径。

## 快速启动

### 1. 准备数据库

创建 MySQL 数据库：

```sql
CREATE DATABASE photography_system
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

根据本地 MySQL 账号设置环境变量，或直接修改 `application.yml` 中的数据库配置。

### 2. 启动后端

Windows:

```bash
./mvnw.cmd spring-boot:run
```

macOS / Linux:

```bash
./mvnw spring-boot:run
```

后端启动后访问：

- API 根路径：`http://localhost:8080/api`
- Swagger UI：`http://localhost:8080/api/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/api/v3/api-docs`

### 3. 启动前端

```bash
cd Photography-UI
npm install
npm run dev
```

前端默认访问地址：

```text
http://localhost:3000
```

### 4. 注册与登录

- 普通用户可通过 `/register` 注册。
- 管理员注册需要 `ADMIN_SECRET_KEY`。
- 登录成功后前端会将 JWT 和用户信息保存到 `localStorage`，路由守卫会在 Token 过期前自动拦截并跳转登录页。

## 常用脚本

### 后端

```bash
# 运行测试
./mvnw.cmd test

# 清理并测试
./mvnw.cmd clean test

# 打包
./mvnw.cmd clean package
```

### 前端

```bash
cd Photography-UI

# 开发
npm run dev

# 生产构建
npm run build

# 本地预览构建产物
npm run preview
```

## API 模块

由于后端配置了 `server.servlet.context-path: /api`，下列路径均以 `/api` 为实际前缀。

| 模块 | 路径前缀 | 说明 |
| --- | --- | --- |
| 认证 | `/auth` | 登录、注册、刷新、校验、邮箱验证码 |
| 用户 | `/users` | 用户列表、资料、头像、密码、统计 |
| 部门 | `/departments` | 部门列表、搜索、详情、统计 |
| 器材 | `/equipment` | 器材列表、搜索、库存、图片、统计 |
| 器材分类 | `/equipment-categories` | 分类列表、启停、统计 |
| 借还 | `/borrows` | 借用申请、审批、归还、统计、导出 |
| 公告 | `/announcements` | 公告公开查询、后台管理、统计 |
| 晚自习签到 | `/checkin` | 签到签退、记录、统计、审核 |
| 签到配置 | `/checkin/configurations` | 签到地点、时段、有效范围配置 |
| 每日考勤 | `/daily-checkin` | 日汇总、用户汇总、明细、导出 |
| 考勤统计 | `/attendance` | 考勤统计和区间统计 |
| 执勤 | `/duty` | 排班、签到、记录、统计、调班 |
| 请假 | `/leave-requests` | 请假提交、审批、取消、统计、导出 |
| 设备绑定 | `/devices` | 我的设备、后台设备列表、清理和统计 |
| 站点配置 | `/site-config` | 公开站点配置、后台配置、图片上传、邮件测试 |
| 图片 | `/images` | 器材、头像、归还、站点图片访问 |
| Excel 导出 | `/export` | 通用导出与模板下载 |
| 高德地图 | `/amap` | 地理编码、搜索、路线、定位、距离和位置校验 |

## 前端页面

主要页面包括：

- `/login`、`/register`：统一认证页
- `/dashboard`：首页
- `/user/list`：用户管理
- `/department/list`：部门管理
- `/equipment/list`、`/equipment/categories`：器材与分类管理
- `/borrow/list`：借还记录
- `/announcement/list`、`/announcement/:id`：公告管理与详情
- `/checkin/main`、`/checkin/records`、`/checkin/qr-generator`、`/checkin/audit`、`/checkin/configuration`、`/checkin/attendance`：晚自习签到与考勤
- `/duty/checkin`、`/duty/list`、`/duty/records`、`/duty/statistics`：办公执勤
- `/leave/list`：请假管理
- `/devices/my`、`/devices/admin`、`/devices/site-config`：设备绑定与站点配置
- `/profile`：个人中心

## 文件上传与静态资源

本地上传路径默认在项目根目录 `uploads/` 下：

- `uploads/avatars/`：用户头像
- `uploads/equipment/`：器材图片
- `uploads/returns/`：归还图片

前端开发环境通过 Vite 代理访问 `/uploads`，后端实际使用 `/api` 上下文处理资源。

## 测试情况

后端已有 JUnit 5 测试，覆盖应用启动、AOP 依赖、JWT 工具、邮件配置、邮箱验证码、请假服务、用户设备服务等。

```bash
./mvnw.cmd test
```

前端当前 `package.json` 未配置测试脚本。如需补充前端单元测试，建议引入 Vitest 与 Vue Test Utils，优先覆盖登录、路由守卫、请求封装、核心表单和关键管理页面。

## 部署建议

- 生产环境必须替换 `JWT_SECRET` 和 `ADMIN_SECRET_KEY`。
- 使用独立 MySQL 实例并关闭调试级 SQL 日志。
- 为上传目录配置持久化存储和备份策略。
- 配置可信 CORS 域名，避免使用过宽的跨域策略。
- 通过 Spring Boot Actuator 接入健康检查和监控。
- 前端执行 `npm run build` 后，将 `Photography-UI/dist` 部署到 Nginx 或静态资源服务器，并将 `/api` 反向代理到后端服务。

