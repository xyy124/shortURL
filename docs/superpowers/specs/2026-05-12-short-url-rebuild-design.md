# Short URL Rebuild — Architecture Design

> 简历项目：短链接服务重构
> 日期：2026-05-12
> 状态：设计稿 v1

---

## 1. 项目概述

将现有 Spring Boot 2.4 + Thymeleaf + MySQL + Redis 单体短链接服务，重构为前后端分离、多级缓存、支持用户体系的现代化短链接平台。

### 核心目标

- 前端 Vue 3 + Nginx 前后端分离
- Maven 多模块组织，展示工程化能力
- 多级缓存（Caffeine + Redis）展示缓存体系深度
- Spring Security + JWT 用户认证
- 自定义短链、统计看板等功能扩展
- Docker Compose 一键本地部署

### 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.4.x | Jakarta EE |
| Java | 17+ | Spring Boot 3 最低要求 |
| Maven | 3.9+ | 多模块构建 |
| MyBatis-Plus | 3.5.x | ORM，减少 XML |
| Spring Security | 6.x | 认证授权 |
| Caffeine | 3.x | 本地缓存 |
| Redisson | 3.x | 分布式锁 |
| MySQL | 8.0 | 持久化数据库 |
| Redis | 7.x | 分布式缓存 |
| Vue 3 + Vite | 最新 | TypeScript |
| Pinia | 2.x | 状态管理 |
| ECharts | 5.x | 统计图表 |
| Nginx | 1.25+ (alpine) | 反向代理 + 静态文件 |
| Docker Compose | V2 | 容器编排 |

---

## 2. 项目目录结构

```
ShortURL/
├── pom.xml                          # Maven 父 POM
├── docker-compose.yml               # 一键启动
├── .env                             # 环境变量
├── .gitignore
├── README.md
├── docs/
│   └── design.md                    # 前端设计规范参考
│
├── docker/
│   ├── backend/
│   │   └── Dockerfile               # 多阶段构建 (Maven + JRE)
│   └── mysql/
│       └── init/
│           └── init.sql             # 初始化 DDL
│
├── short-url-common/                # [jar] 公共基座
│   └── src/main/java/top/naccl/dwz/common/
│       ├── dto/                     # ApiResponse, PageResult, 请求/响应 DTO
│       ├── entity/                  # UrlMap, User, VisitLog, DailyStats
│       ├── enums/                   # ResultCode, RoleEnum, DeviceTypeEnum
│       ├── exception/               # ApiException, GlobalExceptionHandler
│       ├── constant/                # CacheConstants, ShortUrlConstants
│       └── util/                    # HashUtils, Base62Utils, UrlUtils
│
├── short-url-core/                  # [jar] 核心业务引擎
│   └── src/main/java/top/naccl/dwz/core/
│       ├── bloom/RedisBloomFilter           # Redis BitMap 分布式布隆过滤器
│       ├── cache/MultiLevelCacheService     # Caffeine(L1) + Redis(L2)
│       ├── shortcode/                       # ShortCodeGenerator 接口 + 实现
│       ├── service/UrlMappingService        # 核心映射接口
│       └── visit/VisitLogger                # @Async 异步访问日志
│
├── short-url-api/                   # [jar] 对外短链 API
│   └── src/main/java/top/naccl/dwz/api/
│       ├── controller/                     # RedirectController, ShortenController
│       ├── interceptor/RateLimitInterceptor
│       └── service/ShortenService
│
├── short-url-admin/                 # [jar] 管理后台 API
│   └── src/main/java/top/naccl/dwz/admin/
│       ├── controller/                     # AuthController, UrlManageController, StatsController
│       └── service/                        # AuthService, UrlManageService, StatsService
│
├── short-url-web/                   # [jar] 启动入口
│   └── src/main/java/top/naccl/dwz/web/
│       ├── ShortUrlApplication.java
│       ├── config/                         # CaffeineCacheConfig, RedisConfig, WebMvcConfig, AsyncConfig
│       ├── security/                       # JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig
│       └── mapper/                         # MyBatis-Plus Mapper + XML
│
└── frontend/                        # Vue 3 独立项目
    ├── package.json
    ├── vite.config.ts
    ├── Dockerfile                    # node build + nginx serve
    ├── nginx/nginx.conf
    └── src/
        ├── main.ts
        ├── App.vue
        ├── router/index.ts
        ├── stores/ (Pinia)
        ├── api/ (Axios 封装)
        ├── views/ (Home, Login, Register, Dashboard, UrlList, UrlDetail)
        ├── components/ (NavBar, ShortenForm, StatsChart)
        └── styles/
```

---

## 3. 模块依赖关系

```
short-url-web (可执行 JAR)
  ├── short-url-api
  │    └── short-url-core
  │         └── short-url-common
  └── short-url-admin
       └── short-url-core
            └── short-url-common
```

- `short-url-web` 作为启动入口，通过 `@SpringBootApplication(scanBasePackages = "top.naccl.dwz")` 跨模块扫描
- MyBatis-Plus Mapper 位于 `web` 模块，XML 通过 `classpath*:mapper/*.xml` 跨模块加载
- `short-url-core` 保持纯业务，不依赖 Web/Security

---

## 4. 短码生成算法

### 双轨策略

- **系统自动生成**：MurmurHash32 → base62 → 补齐 6 位
- **用户自定义**：正则校验 `/^[a-zA-Z][a-zA-Z0-9]{3,15}$/` + 存在性校验 + 敏感词过滤

### HashShortCodeGenerator 核心逻辑

- 输入原 URL，输出 6 位短码
- 如果布隆过滤器命中，用计数器作为盐值重哈希（最多 5 次）
- 不再修改原始 URL（旧方案在 URL 后追加 `*`）
- DB 唯一性兜底（布隆过滤器可能误判）

### 改进对照

| 方面 | 当前实现 | 重构方案 |
|------|---------|---------|
| 哈希冲突处理 | 在 URL 后追加 `*` | 计数器盐值，不影响原 URL |
| 冲突重试 | 递归无上限 | 最多 5 次，超限报错 |
| 布隆过滤器 | Hutool JVM 本地 | Redis BitMap 分布式 |
| 自定义短链 | 不支持 | 支持自定义 |
| 短码长度 | 不固定 | 固定 6 位 |

---

## 5. 多级缓存架构

### 三层防御体系

```
请求
  → ① Redis BloomFilter（前置：不存在的直接拒绝，防穿透）
    → ② Caffeine L1（本地堆内，10k上限，15min TTL）
      → ③ Redis L2（分布式，30min TTL + 随机0-300s偏移，防雪崩）
        → ④ Redisson 互斥锁（防击穿，只一个线程查 DB）
          → DB 查询
            → 回填 L1 + L2
            → 空值缓存 5min（防穿透）
```

### 关键策略

- **穿透防护**：布隆过滤器前置拦截 + 空值缓存
- **击穿防护**：Redisson 分布式锁 + double-check
- **雪崩防护**：过期时间 BASE_TTL + random(0, 300)
- **L1/L2 一致性**：写操作同时更新 L1+L2，Redis 作为权威缓存

---

## 6. 分布式布隆过滤器

基于 Redis BitMap 实现：

- bitSize = 2^24 ≈ 1677 万位（~2MB）
- 5 个哈希函数，种子 [1, 3, 5, 7, 11]
- 期望容量 100 万，误判率 < 1%
- 应用启动时从 DB 加载所有活动短码重建
- 定时任务每日凌晨全量重建

---

## 7. 数据模型

### url_map（增强版）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | PK 自增 |
| short_code | VARCHAR(10) | 短链码，唯一索引 |
| long_url | VARCHAR(2048) | 原始长链接 |
| user_id | BIGINT | 所属用户，NULL 为匿名 |
| title | VARCHAR(255) | 链接标题 |
| is_custom | TINYINT(1) | 是否自定义 |
| is_active | TINYINT(1) | 是否启用 |
| expire_time | DATETIME | 过期时间 |
| views | INT | 总访问次数 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### user

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | PK |
| username | VARCHAR(50) | 唯一 |
| password | VARCHAR(255) | BCrypt |
| nickname | VARCHAR(50) | 昵称 |
| email | VARCHAR(100) | 邮箱 |
| role | VARCHAR(20) | USER / ADMIN |
| is_active | TINYINT(1) | 激活状态 |
| create/update_time | DATETIME | 时间戳 |

### visit_log

访问明细：short_code, visit_time, ip, user_agent, referer, country, city, device_type, browser, os

### daily_stats

日汇总：short_code, stats_date, pv, uv, ip_count（按 code+date 唯一索引）

---

## 8. API 设计

### 公开端点

| 方法 | 路径 | 说明 | 限流 |
|------|------|------|------|
| GET | `/{shortCode}` | 302 重定向 | 无 |
| POST | `/api/v1/shorten` | 生成短链 | IP 5次/分钟 |
| POST | `/api/v1/auth/login` | 登录 | IP 10次/分钟 |
| POST | `/api/v1/auth/register` | 注册 | IP 3次/小时 |

### 管理端点（JWT 认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/urls` | 用户短链列表（分页） |
| POST | `/api/v1/admin/urls` | 创建短链 |
| POST | `/api/v1/admin/urls/custom` | 创建自定义短链 |
| DELETE | `/api/v1/admin/urls/{id}` | 删除 |
| PUT | `/api/v1/admin/urls/{id}` | 更新 |
| GET | `/api/v1/admin/stats/overview` | 概览统计 |
| GET | `/api/v1/admin/stats/daily` | 日趋势 |
| GET | `/api/v1/admin/stats/referer` | 来源排名 |
| GET | `/api/v1/admin/stats/device` | 设备分布 |

### 短链重定向与 SPA 路由冲突规避

Nginx 正则 `~ ^/[a-zA-Z0-9]{4,8}$` 优先匹配短码，SPA 路由使用连字符（如 `/url-list`、`/stats-detail`），互不干扰。

---

## 9. 安全设计

- Spring Security 6 配置，CSRF 禁用，无状态 Session
- JWT：HMAC-SHA256，24h 过期，Payload 含 userId/username/role
- `JwtAuthenticationFilter` 从 `Authorization: Bearer <token>` 提取并校验
- 密码 BCrypt 加密
- URL 权限规则：

```
/{shortCode}          → permitAll
/api/v1/shorten       → permitAll (匿名可用)
/api/v1/auth/**       → permitAll
/api/v1/admin/**      → authenticated
```

---

## 10. Docker Compose 部署

### 服务拓扑

```
Frontend (Nginx :8080)
  ├── /{shortCode} → proxy → Backend (:8060)
  ├── /api/*       → proxy → Backend (:8060)
  └── /*           → SPA index.html

Backend (:8060)
  ├── MySQL (:3307)
  └── Redis (:6379)
```

### 服务定义

- **mysql**: 8.0，端口 3307，挂载 init.sql + 持久化卷
- **redis**: 7-alpine，端口 6379，持久化卷
- **backend**: 多阶段 Dockerfile（Maven 构建 + JRE 运行），端口 8060，依赖 mysql+redis
- **frontend**: 多阶段 Dockerfile（node build + nginx alpine serve），端口 8080，依赖 backend

### Git 初始化

- `git init` 在项目根目录
- `.gitignore` 包含：target/, node_modules/, .env, *.log, .DS_Store, .idea/, *.iml
- README.md 包含：项目简介、技术栈、架构图、启动步骤

---

## 11. 数据流

### 短链生成

```
用户请求 → 限流拦截器 → 校验 URL
  → [自定义] 校验格式/存在性/敏感词
  → [自动]   MurmurHash + base62 + BloomFilter 防冲突
  → INSERT url_map → BloomFilter.add → Redis.set → 返回短码
```

### 短链访问

```
GET /{code} → Nginx → Backend
  → BloomFilter.mightContain (拦截不存在)
  → Caffeine.get (L1)
  → Redis.get (L2，回填 L1)
  → Redisson.lock → DB 查询 (互斥加载)
  → 302 Redirect
  → @Async 记录 visit_log + daily_stats
```

---

## 12. 实施阶段

| Phase | 内容 |
|-------|------|
| 0 | 项目初始化：Maven 多模块骨架、git init、.gitignore、README |
| 1 | short-url-common：DTO、实体、枚举、异常、工具类 |
| 2 | short-url-core：RedisBloomFilter、MultiLevelCacheService、ShortCodeGenerator |
| 3 | short-url-api：RedirectController、ShortenController |
| 4 | short-url-admin + web：SecurityConfig、JWT、AuthController |
| 5 | 数据模型扩展：MyBatis-Plus、init.sql |
| 6 | 统计模块：VisitLogger、StatsService、StatsController |
| 7 | Vue 3 前端：所有页面和组件 |
| 8 | Dockerfile + docker-compose.yml + Nginx 配置 |
| 9 | 联调测试 + 文档完善 + 首次 commit |
