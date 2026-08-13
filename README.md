# Photography Management System

## 项目简介

最后更新：2026-08-13 20:10:40 +08:00（Asia/Shanghai）

Photography Management System 是一套面向校园融媒体中心的综合管理平台，用于把公开展示、内容投稿、招新报名、学院信息、摄影器材、人员组织、值班考勤和日常审批集中到同一个系统中管理。系统采用前后端分离架构，后端基于 Spring Boot 3、Spring Security 和 Spring Data JPA 提供 RESTful API，前端基于 Vue 3、Vite、Element Plus 和 Pinia 构建管理端与游客端页面。

项目服务的核心对象包括游客、投稿人、入部申请人、部员、部长、主任、指导老师和系统超级管理员。游客可以浏览融媒体中心公开落地页、提交视频素材并填写入部申请，成员可以完成登录、借还设备、晚自习签到、办公执勤和请假申请，管理角色可以进行账号审核、学院与部门管理、投稿审核、入部申请审核、设备维护、公告发布、站点配置和数据导出。

系统重点关注校园场景下的安全与可维护性：注册账号默认进入审核流程，访问令牌短时有效，刷新令牌通过 HttpOnly Cookie 轮换，敏感配置支持加密保存，投稿视频与入部作品存放在私有目录，生产环境通过独立配置文件和环境变量控制数据库、JWT、维护通行和配置加密密钥。

## 本次更新（2026-08-13 20:10:40 +08:00）

- 重塑游客端落地页、登录、找回密码、加入我们和视频投稿页面，统一校园融媒体影像档案视觉语言并完善移动端导航。
- 新增全局路由切换加载画面，优化进出场动画并支持 `prefers-reduced-motion`，兼顾页面反馈与无障碍体验。
- 新增投稿文件上传进度、处理中和失败状态展示，补充上传进度工具函数、组件单测及 Playwright 端到端场景。
- 将公开页面的大体量样式拆分为独立样式模块，降低 Vue 单文件组件复杂度并提升后续维护效率。
- 新增 `/join-us` 入部申请页，支持姓名、QQ 邮箱、手机号、性别、学院、专业、入学年份、自我介绍和可选作品上传。
- 新增入部申请后台审核，支持查看详情、下载作品、同意进入面试、驳回申请、配置面试 QQ 群，并在通过后发送邮件通知。
- 新增学院管理模块，维护入部申请和外部借用场景可选择的学院信息。
- 优化维护模式页面体验、路由维护状态缓存、本地 `127.0.0.1` CORS 支持，以及借还和器材管理页面交互。
- 新增游客可访问的融媒体落地页，支持首屏媒体、校园特色、部门风采、投稿入口、抖音/微信入口等内容配置。
- 新增视频投稿流程，支持 QQ 邮箱验证码、MP4/MOV/WebM 上传、500MB 文件限制、私有目录保存、后台审核、下载和邮件通知。
- 重构认证与账号安全，加入待审核注册、账号启停、登录失败锁定、找回密码、验证码限流、15 分钟访问令牌和 7 天旋转刷新令牌。
- 引入 `SUPER_ADMIN`、`ADVISOR` 等更清晰的角色模型，新增超级管理员账号审核、部门分配和角色调整能力，并兼容旧 `ADMIN` 数据。
- 新增维护模式，支持公开状态查询、维护通行页、两小时通行 Cookie，以及超级管理员维护配置。
- 扩展借还业务，支持内部/外部借用人类型、外部借用人信息、归还图片和导出字段补充。
- 补充数据库升级脚本与升级说明，新增媒体系统结构迁移和可选历史业务数据清理脚本。
- 完善前端路由、请求层和会话恢复逻辑，新增落地页管理、投稿管理、注册审核、找回密码和维护页等页面。
- 新增 Vitest、Vue Test Utils 和 Playwright 配置，覆盖路由守卫、用户状态、公开页面、维护模式和核心前端流程。

## 功能概览

### 用户与权限

- 支持待审核注册、登录锁定、密码找回、15 分钟访问令牌和 7 天旋转刷新令牌。
- 访问令牌只保存在 Pinia 内存，刷新令牌使用哈希入库并通过 HttpOnly/Secure/SameSite Cookie 传递。
- 用户角色包含部员、部长、主任、指导老师和系统超级管理员，权限同时在前端路由与后端接口校验。
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

### 公开站点与视频投稿

- `/` 是游客可访问的校园融媒体落地页，可配置视频/图片首屏、校园特色、部门风采、投稿说明、抖音和微信入口。
- `/submission` 支持 QQ 邮箱验证和 MP4/MOV/WebM 私有视频投稿，单文件最大 500MB，并显示上传进度。
- 部长、主任和超级管理员可审核、下载投稿，投稿文件不会通过静态目录公开。
- 支持维护模式和两小时维护通行 Cookie，落地页在维护期间保持可访问。

### 招新与入部申请

- `/join-us` 支持游客提交入部申请，填写学院、专业、入学年份、自我介绍，并可上传图片或视频作品。
- 管理端支持入部申请分页、状态筛选、详情查看、作品下载、审核通过/驳回和面试 QQ 群配置。
- 申请通过后系统会发送面试通知邮件，并记录通知是否成功。
- 支持学院分页、搜索、下拉列表、新增、编辑和删除，供入部申请与外部借用场景复用。

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
| `DB_PASSWORD` | 仅开发配置为 `123456` | 生产数据库密码 |
| `JWT_SECRET` | 仅开发环境有本地值 | 访问令牌签名密钥，生产环境必填 |
| `MAINTENANCE_TOKEN_SECRET` | 仅开发环境有本地值 | 维护通行凭证签名密钥，生产环境必填 |
| `CONFIG_ENCRYPTION_KEY` | 仅开发环境有本地值 | QQ SMTP 授权码 AES-GCM 加密密钥，生产环境必填 |
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

- 游客通过 `/register` 完成 QQ 邮箱验证后提交部员账号申请。
- 新账号状态为 `PENDING`，超级管理员分配部门并审核通过后才能登录。
- 角色提升只能由超级管理员操作，前端不提供旧 `ADMIN` 身份。
- 登录后的管理首页为 `/dashboard`；访问令牌只存在当前页面内存中，页面重载时通过刷新 Cookie 恢复会话。

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

# 前端单元测试与覆盖率
npm run test:coverage

# 本地预览构建产物
npm run preview
```

## API 模块

由于后端配置了 `server.servlet.context-path: /api`，下列路径均以 `/api` 为实际前缀。

| 模块 | 路径前缀 | 说明 |
| --- | --- | --- |
| 认证 | `/auth` | 登录、注册、刷新、校验、邮箱验证码 |
| 账号审核 | `/accounts/admin` | 待审核账号、审核通过/驳回、角色调整 |
| 用户 | `/users` | 用户列表、资料、头像、密码、统计 |
| 部门 | `/departments` | 部门列表、搜索、详情、统计 |
| 部门成员 | `/department-members` | 部长与管理角色维护部门成员信息 |
| 学院 | `/colleges` | 学院分页、搜索、下拉、创建、更新和删除 |
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
| 公开落地页 | `/landing` | 游客落地页内容、后台内容项和媒体上传 |
| 视频投稿 | `/submissions` | 投稿邮箱验证码、视频提交、审核和下载 |
| 入部申请 | `/join-applications` | 游客申请、作品上传、审核、面试群配置和邮件通知 |
| 维护模式 | `/maintenance` | 维护状态、通行验证和后台维护配置 |
| 图片 | `/images` | 器材、头像、归还、站点图片访问 |
| Excel 导出 | `/export` | 通用导出与模板下载 |
| 高德地图 | `/amap` | 地理编码、搜索、路线、定位、距离和位置校验 |

## 前端页面

主要页面包括：

- `/`：游客落地页
- `/submission`：视频投稿
- `/join-us`：入部申请
- `/forgot-password`：找回密码
- `/maintenance`：维护通行验证
- `/login`、`/register`：统一认证页
- `/dashboard`：首页
- `/user/list`、`/user/review`：用户管理与注册审核
- `/department/list`、`/department/colleges`：部门与学院管理
- `/equipment/list`、`/equipment/categories`：器材与分类管理
- `/borrow/list`：借还记录
- `/announcement/list`、`/announcement/:id`：公告管理与详情
- `/checkin/main`、`/checkin/records`、`/checkin/qr-generator`、`/checkin/audit`、`/checkin/configuration`、`/checkin/attendance`：晚自习签到与考勤
- `/duty/checkin`、`/duty/list`、`/duty/records`、`/duty/statistics`：办公执勤
- `/leave/list`：请假管理
- `/devices/my`、`/devices/admin`、`/devices/site-config`、`/devices/landing-config`：设备绑定、站点配置与落地页配置
- `/submission-management`：视频投稿审核
- `/join-applications`：入部申请审核
- `/profile`：个人中心

## 文件上传与静态资源

本地上传路径默认在项目根目录 `uploads/` 下：

- `uploads/avatars/`：用户头像
- `uploads/equipment/`：器材图片
- `uploads/returns/`：归还图片
- `uploads/site/`：站点 Logo、背景图、落地页图片和公开展示媒体

私有上传路径默认在项目根目录 `private-uploads/` 下：

- `private-uploads/submissions/`：视频投稿原始文件，仅通过带权限的下载接口访问
- `private-uploads/join-applications/`：入部申请作品文件，仅通过带权限的下载接口访问

前端开发环境通过 Vite 代理访问 `/uploads`，后端实际使用 `/api` 上下文处理资源。

## 测试情况

后端 JUnit 5 测试覆盖应用启动、JWT、邮箱验证码、验证码限流、维护通行与锁定、请假联动和用户设备。前端使用 Vitest 与 Vue Test Utils，覆盖路由角色守卫、维护跳转、内存访问令牌和刷新会话；核心路由与会话模块执行 80% 行覆盖门槛。

```bash
./mvnw.cmd test
```

数据库升级和可选历史清理见 `docs/UPGRADE_MEDIA_SYSTEM.md`。历史清理脚本必须显式传入确认变量，应用启动不会自动删除业务数据。

## 部署建议

- 生产环境必须提供数据库、JWT、维护签名和配置加密环境变量，缺少任何密钥时应拒绝启动。
- 使用独立 MySQL 实例并关闭调试级 SQL 日志。
- 为上传目录配置持久化存储和备份策略。
- 配置可信 CORS 域名，避免使用过宽的跨域策略。
- 通过 Spring Boot Actuator 接入健康检查和监控。
- 前端执行 `npm run build` 后，将 `Photography-UI/dist` 部署到 Nginx 或静态资源服务器，并将 `/api` 反向代理到后端服务。
