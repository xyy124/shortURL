# 用户仪表盘界面设计

## 概述

将普通用户的前端体验从单一表单页升级为「管理后台式」左右布局，包含功能菜单导航和动态内容区域。未登录和已登录用户共用同一界面，功能权限通过菜单显式区分。

## 路由结构

```
/ → UserLayout（左右结构父布局）
  / → Welcome 欢迎页
  /long-to-short → 长链接转短链接（已实现）
  /short-to-long → 短链接转长链接（占位）
  /stats → 访问统计（占位）
  /batch → 批量生成（占位）
  /qrcode → 二维码生成（占位）

/login → Login.vue（不变）
/register → Register.vue（不变）
/admin/* → 管理后台（不变）
```

## 布局

左右结构：左侧固定宽度侧边栏（约 240px）+ 右侧自适应内容区。

### 左侧菜单 UserSidebar

- Logo/品牌区
- 登录状态显示（"未登录" / "欢迎回来，用户名"）
- 功能菜单列表，按顺序：
  1. 长链接转短链接（始终可用，✅ 已实现）
  2. 短链接转长链接（需登录，🚧 待开发）
  3. 访问统计（需登录，🚧 待开发）
  4. 批量生成（需登录，🚧 待开发）
  5. 二维码生成（需登录，🚧 待开发）
- 底部：帮助与反馈 / 退出登录（已登录时显示）

未登录时，受限菜单项置灰（opacity + cursor: not-allowed），点击弹出 toast 提示"请登录后使用此功能"。

### 右侧内容区

内容区顶部固定显示登录状态条：
- 未登录：「🟢 当前状态：未登录」
- 已登录：「🟢 欢迎回来，{nickname}」

#### 欢迎页（/）
- 大标题「欢迎使用短链接服务」
- 引导卡片，提示用户点击左侧菜单开始
- 展示功能预览（登录后可解锁的功能列表）

#### 长链接转短链接（/long-to-short）
- 迁移现有 Home.vue 的功能界面
- 保留 URL 输入 + 自定义后缀 + 生成按钮 + ResultCard
- 自定义后缀输入框始终可见（生成时若未登录且输入了自定义后缀，提示登录）

#### 占位页通用模板
- 页面标题
- 功能占位卡片（🚧 图标 + "功能开发中" + 功能预告描述）

## 美学方向：「现代工具」风格

### 色彩系统

```
--sidebar-bg:       rgba(15, 23, 42, 0.95)   深灰蓝侧边栏
--sidebar-border:   rgba(255, 255, 255, 0.06)
--sidebar-hover:    rgba(255, 255, 255, 0.08)
--sidebar-active:   rgba(245, 158, 11, 0.15)
--accent:           #f59e0b                   琥珀色主色
--accent-light:     #fbbf24
--bg-main:          #f8fafc                   内容区底色
--card-bg:          #ffffff                   卡片背景
--text-primary:     #1e293b                   主文字
--text-secondary:   #64748b                   辅助文字
--text-muted:       #94a3b8
--border-light:     #e2e8f0
```

### 版式

- 中文：系统字体栈（`PingFang SC, Microsoft YaHei, sans-serif`）
- 品牌 Logo：`Plus Jakarta Sans`（英文部分）
- 短链码/URL 展示：`JetBrains Mono` 等宽字体

### 动效

- 菜单项 hover：背景渐入 (150ms) + 左侧 2px 琥珀色边框指示条滑入
- 内容切换：fade + translateY(4px → 0)，200ms ease-out
- Toast 通知：从顶部 slide-down，2.5s 自动消失
- 欢迎页加载：内容 stagger 出现，animation-delay 递增 80ms

## 权限控制

| 功能 | 未登录 | 已登录 |
|------|--------|--------|
| 长链接转短链接 | ✅ 可用 | ✅ 可用 |
| 自定义后缀 | ❌ 需登录 | ✅ 可用 |
| 短链接转长链接 | ❌ 置灰提示 | 🚧 占位 |
| 访问统计 | ❌ 置灰提示 | 🚧 占位 |
| 批量生成 | ❌ 置灰提示 | 🚧 占位 |
| 二维码生成 | ❌ 置灰提示 | 🚧 占位 |

## 文件变更清单

### 新建文件

| 文件 | 说明 |
|------|------|
| `src/views/user/Layout.vue` | 用户主布局（左右结构） |
| `src/components/UserSidebar.vue` | 用户侧边栏菜单 |
| `src/views/user/Welcome.vue` | 欢迎页 |
| `src/views/user/LongToShort.vue` | 长链接转短链接功能页 |
| `src/views/user/ShortToLong.vue` | 短链接转长链接占位页 |
| `src/views/user/FeaturePlaceholder.vue` | 通用占位页组件 |
| `src/components/StatusToast.vue` | Toast 提示组件 |

### 修改文件

| 文件 | 改动 |
|------|------|
| `src/router/index.ts` | 新增用户路由层级，调整根路由 |
| `src/views/Login.vue` | 登录后根据 role 区分重定向 |
| `src/components/AppHeader.vue` | 简化导航（去除登录/注册，保留 Logo） |
| `src/styles/main.css` | 新增 CSS 变量 |

### 删除文件

- `src/views/Home.vue`（内容迁移至 LongToShort.vue 后移除）

## 不涉及

- `/admin/*` 管理后台不变
- 后端 API 不变
- 认证逻辑不变
