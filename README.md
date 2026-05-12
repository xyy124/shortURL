# Short URL - 短链接服务

基于 **Spring Boot 3 + Vue 3** 构建的高性能短链接生成与管理平台，支持多级缓存、分布式布隆过滤器、访问统计和 Docker Compose 一键部署。

## 功能特性

- **短链生成** — MurmurHash32 + base62 生成 6 位短码，hash 冲突时使用计数器加盐重试，重试耗尽后由数据库唯一索引兜底
- **自定义短链** — 登录用户可自定义短链码
- **多级缓存** — Caffeine（本地）→ Redis（分布式）→ DB 三级缓存，穿透时使用 Redisson 分布式锁防止缓存雪崩
- **布隆过滤器** — 基于 Redis BitMap 的分布式布隆过滤器（2^24 bits，5 个哈希函数），拦截不存在 key 的缓存穿透
- **缓存空值** — 对不存在但被查询的 key 缓存空值（含 TTL），进一步减少 DB 压力
- **访问统计** — PV / UV / 独立 IP 统计，ECharts 折线图展示
- **User-Agent 解析** — 记录访客设备类型、浏览器、操作系统
- **JWT 鉴权** — Spring Security 6 + HMAC-SHA256 JWT，登录/注册/管理后台
- **频率限制** — 基于 Redis 的 IP 级限流（5 次 / 10 秒）
- **302 重定向** — 短链访问实时 302 跳转至目标长链接
- **Docker Compose** — MySQL + Redis + 后端 + Nginx 一键启动

## 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17+（推荐 JDK 22） |
| 框架 | Spring Boot | 3.4.5 |
| ORM | MyBatis-Plus | 3.5.9 |
| 安全 | Spring Security 6 + jjwt | 0.12.6 |
| 本地缓存 | Caffeine | (Spring Boot 内建) |
| 分布式缓存 | Redis + Redisson | 3.41.0 |
| 工具库 | Hutool | 5.8.34 |
| 构建 | Maven | 3.9+ |
| 前端 | Vue 3 + TypeScript + Vite 5 | |
| 状态管理 | Pinia | |
| 图表 | ECharts + vue-echarts | |
| 容器化 | Docker Compose | |

## 架构

### 三级缓存

```
                          ┌──────────┐
                          │  客户端   │
                          └────┬─────┘
                               │ GET /{shortCode}
                               ▼
                    ┌──────────────────┐
                    │  RedisBloomFilter │  ◄── 拦截不存在 key
                    │  (2^24 bits,5hash)│      直接返回 404
                    └────────┬─────────┘
                             │ key 可能存在
                             ▼
                    ┌──────────────────┐
                    │  Caffeine L1     │  ◄── 本地缓存 (10k, 15min)
                    │  (进程内)         │      命中直接返回
                    └────────┬─────────┘
                             │ miss
                             ▼
                    ┌──────────────────┐
                    │  Redis L2        │  ◄── 分布式缓存 (30min±5min)
                    │  (分布式)         │      命中回填 L1
                    └────────┬─────────┘
                             │ miss
                             ▼
                    ┌──────────────────┐
                    │  Redisson Lock   │  ◄── 分布式锁防止缓存雪崩
                    │  (锁键: lock:url:)│      获得锁者查 DB
                    └────────┬─────────┘
                             │ 获得锁
                             ▼
                    ┌──────────────────┐
                    │  MySQL           │
                    │  (持久层)         │
                    └──────────────────┘
```

### 短码生成流程

```
Long URL
    │
    ▼
MurmurHash32 (Hutool)
    │
    ▼
base62 编码 → 取前 6 位
    │
    ▼
布隆过滤器检查是否已存在 ── 不存在 ──▶ 写入 DB + 布隆过滤器 + 缓存
    │
 存在 (可能误判)
    │
    ▼
DB 查重 ── 不存在 ──▶ 写入 DB + 布隆过滤器 + 缓存
    │
 存在 (真冲突)
    │
    ▼
计数器加盐 (counter++) → 重新 hash → base62 → 检查
    │
 重试耗尽
    │
    ▼
写入 DB（唯一索引兜底）→ 成功则回填缓存
```

### 项目结构

```
short-url/
├── short-url-common/          # 公共模块
│   ├── constant/              # 缓存 key、错误码常量
│   ├── dto/                   # 请求/响应 DTO
│   ├── entity/                # 数据库实体
│   ├── enums/                 # 枚举
│   ├── exception/             # 统一异常
│   └── util/                  # 工具类（URL 校验等）
│
├── short-url-core/            # 核心引擎
│   ├── bloom/                 # Redis 布隆过滤器
│   ├── cache/                 # 多级缓存实现
│   ├── mapper/                # MyBatis-Plus Mapper
│   ├── service/               # 短链生成 + 统计服务
│   └── interceptor/           # 限流拦截器
│
├── short-url-api/             # 对外 API
│   ├── controller/
│   │   ├── ShortenController  # POST /api/v1/shorten
│   │   └── RedirectController  # GET /{shortCode}
│   └── filter/                # 请求日志过滤器
│
├── short-url-admin/           # 管理后台 API
│   ├── config/                # Security 配置
│   ├── controller/            # URL 管理 + 统计 + 认证
│   ├── filter/                # JWT 认证过滤器
│   └── service/               # 后台管理服务
│
├── short-url-web/             # 启动入口
│   └── ShortUrlApplication.java
│
├── short-url-frontend/        # Vue 3 前端
│   ├── src/
│   │   ├── api/               # Axios API 层
│   │   ├── components/        # 公共组件（Header/Sidebar/ResultCard）
│   │   ├── views/             # 页面（Home/Login/Register/Admin）
│   │   ├── stores/            # Pinia 状态管理
│   │   ├── router/            # 路由配置
│   │   └── styles/            # 全局样式
│   ├── Dockerfile
│   └── nginx.conf
│
├── docker/
│   ├── backend/Dockerfile     # 后端容器化
│   └── mysql/init/init.sql    # 数据库初始化
│
├── docker-compose.yml         # 一键部署
└── .env.example               # 环境变量模板
```

## 数据库设计

### url_map — 短链映射表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| short_code | VARCHAR(10) | 短链码（唯一索引） |
| long_url | VARCHAR(2048) | 原始长链接 |
| user_id | BIGINT | 所属用户（null 为匿名） |
| title | VARCHAR(255) | 链接标题 |
| is_custom | TINYINT(1) | 是否自定义短链 |
| is_active | TINYINT(1) | 是否启用 |
| expire_time | DATETIME | 过期时间 |
| views | INT | 累计访问次数 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### visit_log — 访问日志表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| short_code | VARCHAR(10) | 短链码 |
| visit_time | DATETIME | 访问时间 |
| ip | VARCHAR(50) | 访客 IP |
| user_agent | VARCHAR(500) | User-Agent |
| referer | VARCHAR(500) | 来源 |
| device_type | VARCHAR(10) | 设备类型 (PC/MOBILE/TABLET) |
| browser | VARCHAR(50) | 浏览器 |
| os | VARCHAR(50) | 操作系统 |

### daily_stats — 每日统计表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| short_code | VARCHAR(10) | 短链码 |
| stats_date | DATE | 统计日期 |
| pv | INT | 页面访问量 |
| uv | INT | 独立访客 |
| ip_count | INT | 独立 IP 数 |

### user — 用户表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(255) | BCrypt 加密密码 |
| nickname | VARCHAR(50) | 昵称 |
| email | VARCHAR(100) | 邮箱 |
| role | VARCHAR(20) | 角色（USER/ADMIN） |
| is_active | TINYINT(1) | 是否激活 |
| create_time | DATETIME | 注册时间 |

## API 文档

### 公开接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/v1/shorten` | 生成短链（匿名可用，支持自定义需登录） | 可选 |
| GET | `/{shortCode}` | 302 重定向到目标 URL | 否 |

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/auth/register` | 用户注册 |
| POST | `/api/v1/auth/login` | 用户登录，返回 JWT |

### 管理接口（需 JWT）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/urls` | 分页获取我的短链列表 |
| POST | `/api/v1/admin/urls` | 创建自定义短链 |
| DELETE | `/api/v1/admin/urls/{id}` | 软删除短链（置 `is_active=false`，保留统计数据） |
| PUT | `/api/v1/admin/urls/{id}/toggle` | 启用/禁用短链 |
| GET | `/api/v1/admin/urls/all` | 获取所有短链（管理员） |
| GET | `/api/v1/admin/stats/overview` | 总览统计数据 |
| GET | `/api/v1/admin/stats/daily` | 每日趋势数据 |

### 请求/响应示例

**生成短链：**

```bash
curl -X POST http://localhost:8060/api/v1/shorten \
  -H "Content-Type: application/json" \
  -d '{"longUrl": "https://example.com/very/long/url/that/needs/shortening"}'
```

响应：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "shortCode": "aB3xK9",
    "shortUrl": "http://localhost:8060/aB3xK9",
    "longUrl": "https://example.com/very/long/url/that/needs/shortening"
  }
}
```

**自定义短链（需登录）：**

```bash
curl -X POST http://localhost:8060/api/v1/admin/urls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{"longUrl": "https://example.com", "customCode": "myLink"}'
```

## 快速开始

### Docker Compose 部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/xyy124/shortURL.git
cd short-url

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 修改密码等配置

# 3. 启动所有服务
docker compose up -d

# 4. 访问
# 前端页面：http://localhost:8080
# API 服务：http://localhost:8060
```

### 本地开发

#### 前置要求

- JDK 17+（推荐 JDK 22，暂不兼容 JDK 25）
- Maven 3.9+
- MySQL 8.0+
- Redis 7+
- Node.js 20+

#### 后端

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE short_url DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p short_url < docker/mysql/init/init.sql

# 2. 编译
mvn clean package -DskipTests

# 3. 运行
cd short-url-web
mvn spring-boot:run
```

#### 前端

```bash
cd short-url-frontend
npm install
npm run dev
```

前端开发服务器默认运行在 `http://localhost:3000`，已配置 API 代理到 `http://localhost:8060`。

## 配置说明

### 环境变量（.env）

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 | `root123` |
| `MYSQL_DATABASE` | 数据库名 | `short_url` |
| `REDIS_PASSWORD` | Redis 密码 | `redis123` |
| `JWT_SECRET` | JWT 签名密钥（base64, ≥256位） | - |

### 限流配置

在 `RateLimitInterceptor.java` 中配置：每个 IP 每 10 秒最多 5 次短链生成请求。

## 开发计划 / 待实现

- [x] 短链生成与重定向
- [x] 多级缓存
- [x] 布隆过滤器
- [x] JWT 认证
- [x] 管理后台
- [x] 访问统计
- [x] 限流
- [x] 前端页面
- [x] Docker Compose 部署
- [ ] 链接分组管理
- [ ] 批量导入/导出
- [ ] 二维码生成
- [ ] OAuth2 登录（GitHub/Google）

## License

MIT
