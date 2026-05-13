<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const toast = ref('')
let toastTimer: number | undefined

interface MenuItem {
  path: string
  label: string
  icon: string
  requiresAuth: boolean
  isDivider?: boolean
}

const menuItems: MenuItem[] = [
  { path: '/long-to-short', label: '长链接转短链接', icon: '🔗', requiresAuth: false },
  { path: '/short-to-long', label: '短链接转长链接', icon: '🔄', requiresAuth: true },
  { path: '', label: '', icon: '', requiresAuth: false, isDivider: true },
  { path: '/stats', label: '访问统计', icon: '📊', requiresAuth: true },
  { path: '/batch', label: '批量生成', icon: '📦', requiresAuth: true },
  { path: '/qrcode', label: '二维码生成', icon: '📱', requiresAuth: true },
]

function handleNav(item: MenuItem) {
  if (item.isDivider) return
  if (item.requiresAuth && !auth.isLoggedIn) {
    showToast('请登录后使用此功能')
    return
  }
  router.push(item.path)
}

function showToast(msg: string) {
  toast.value = msg
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => { toast.value = '' }, 2500)
}

onUnmounted(() => {
  if (toastTimer) clearTimeout(toastTimer)
})

function handleLogout() {
  auth.logout()
  router.push('/')
}

function isActive(item: MenuItem): boolean {
  if (item.isDivider) return false
  return route.path === item.path
}

function isDisabled(item: MenuItem): boolean {
  if (item.isDivider) return true
  return item.requiresAuth && !auth.isLoggedIn
}
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-brand">
      <span class="sidebar-logo">短链接</span>
      <span class="sidebar-brand-desc">URL Shortener</span>
    </div>

    <div class="sidebar-user">
      <div class="user-avatar">{{ auth.isLoggedIn ? (auth.user?.nickname?.[0] || auth.user?.username?.[0] || '?') : '?' }}</div>
      <div class="user-info">
        <span v-if="auth.isLoggedIn" class="user-name">{{ auth.user?.nickname || auth.user?.username }}</span>
        <span v-else class="user-name">未登录</span>
        <span class="user-status">{{ auth.isLoggedIn ? '已登录' : '未登录' }}</span>
      </div>
    </div>

    <nav class="sidebar-nav" aria-label="主导航">
      <template v-for="(item, index) in menuItems" :key="item.isDivider ? 'divider-' + index : item.path">
        <div v-if="item.isDivider" class="sidebar-divider" />
        <button
          v-else
          class="sidebar-item"
          :class="{ active: isActive(item), disabled: isDisabled(item) }"
          :disabled="isDisabled(item)"
          @click="handleNav(item)"
        >
          <span class="sidebar-item-icon">{{ item.icon }}</span>
          <span class="sidebar-item-label">{{ item.label }}</span>
          <span v-if="isDisabled(item)" class="sidebar-item-lock">🔒</span>
        </button>
      </template>
    </nav>

    <div class="sidebar-footer">
      <button v-if="auth.isLoggedIn" class="sidebar-logout" @click="handleLogout">
        退出登录
      </button>
      <router-link v-else to="/login" class="sidebar-login-btn">登录 / 注册</router-link>
    </div>

    <Teleport to="body">
      <div v-if="toast" class="toast" role="alert">{{ toast }}</div>
    </Teleport>
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: var(--sidebar-width);
  height: 100vh;
  background: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  z-index: 101;
  overflow-y: auto;
}

.sidebar-brand {
  padding: var(--space-lg) var(--space-md) var(--space-sm);
  border-bottom: 1px solid var(--sidebar-divider);
}

.sidebar-logo {
  font-family: var(--font-logo);
  font-size: var(--font-xl);
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: -0.5px;
  display: block;
}

.sidebar-brand-desc {
  font-size: 11px;
  color: var(--sidebar-text);
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-md);
  border-bottom: 1px solid var(--sidebar-divider);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--accent-muted);
  color: var(--accent-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-sm);
  font-weight: 600;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  font-size: var(--font-sm);
  font-weight: 500;
  color: #f1f5f9;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-status {
  font-size: 11px;
  color: var(--sidebar-text);
}

.sidebar-nav {
  flex: 1;
  padding: var(--space-sm);
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sidebar-divider {
  height: 1px;
  background: var(--sidebar-divider);
  margin: var(--space-xs) var(--space-sm);
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: 10px var(--space-sm);
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--sidebar-text);
  font-size: var(--font-sm);
  cursor: pointer;
  transition: all 0.15s;
  text-align: left;
  width: 100%;
  position: relative;
}

.sidebar-item:hover:not(.disabled) {
  background: var(--sidebar-hover);
  color: #e2e8f0;
}

.sidebar-item.active {
  background: var(--sidebar-active);
  color: var(--sidebar-text-active);
}

.sidebar-item.active::before {
  content: '';
  position: absolute;
  left: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  border-radius: 0 3px 3px 0;
  background: var(--accent);
}

.sidebar-item:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  pointer-events: none;
}

.sidebar-item-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
  flex-shrink: 0;
}

.sidebar-item-label {
  flex: 1;
}

.sidebar-item-lock {
  font-size: 10px;
  opacity: 0.7;
}

.sidebar-footer {
  padding: var(--space-md);
  border-top: 1px solid var(--sidebar-divider);
}

.sidebar-logout {
  width: 100%;
  padding: 8px;
  border: 1px solid var(--sidebar-divider);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--sidebar-text);
  font-size: var(--font-sm);
  cursor: pointer;
  transition: all 0.15s;
  text-align: center;
}

.sidebar-logout:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.3);
}

.sidebar-login-btn {
  display: block;
  text-align: center;
  padding: 8px;
  border-radius: var(--radius-sm);
  background: var(--accent);
  color: #0f172a;
  font-size: var(--font-sm);
  font-weight: 600;
  text-decoration: none;
  transition: opacity 0.15s;
}

.sidebar-login-btn:hover {
  opacity: 0.9;
  color: #0f172a;
}

.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: #1e293b;
  color: #f8fafc;
  padding: 10px 20px;
  border-radius: var(--radius-sm);
  font-size: var(--font-sm);
  box-shadow: 0 4px 16px rgba(0,0,0,0.2);
  animation: toastIn 0.25s ease-out;
  z-index: 9999;
}

@keyframes toastIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}
</style>
