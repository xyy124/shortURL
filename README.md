# Short URL - 短链接服务

基于 Spring Boot 3 + Vue 3 的短链接生成与管理平台。

## 技术栈

**后端：** Spring Boot 3.4, MyBatis-Plus, Spring Security, Caffeine, Redis, Redisson
**前端：** Vue 3 + Vite + TypeScript, Pinia, Axios, ECharts
**部署：** Docker Compose (MySQL + Redis + Nginx)

## 功能

- 长链接转短链接（MurmurHash + base62）
- 多级缓存（Caffeine 本地 + Redis 分布式）
- 分布式布隆过滤器防缓存穿透
- 用户注册登录（JWT）
- 自定义短链
- 访问统计看板（PV / UV / 设备 / 来源）
- Docker Compose 一键启动

## 快速开始

### 前置要求

- JDK 17+
- Docker & Docker Compose

### 启动

```bash
# 构建并启动所有服务
docker compose up -d

# 访问
# 前端：http://localhost:8080
# API：http://localhost:8060
```

### 本地开发

```bash
# 后端启动
cd short-url-web
mvn spring-boot:run

# 前端启动
cd frontend
npm install
npm run dev
```

## 项目结构

```
short-url/
├── short-url-common/    # 公共 DTO、实体、工具类
├── short-url-core/      # 核心引擎（缓存、布隆过滤器、短码生成）
├── short-url-api/       # 对外 API（重定向、生成短链）
├── short-url-admin/     # 管理后台 API（用户、统计）
├── short-url-web/       # 启动入口 + Security 配置
└── frontend/            # Vue 3 前端
```

## 架构

```
请求 → RedisBloomFilter → Caffeine L1 → Redis L2 → DB
```
