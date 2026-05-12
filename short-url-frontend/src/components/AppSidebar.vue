<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function handleLogout() {
  auth.logout()
  router.push('/')
}
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-user">
      <div class="sidebar-avatar">{{ (auth.user?.nickname || auth.user?.username)?.[0] }}</div>
      <div class="sidebar-user-info">
        <span class="sidebar-user-name">{{ auth.user?.nickname || auth.user?.username }}</span>
        <span class="sidebar-user-role">{{ auth.user?.role === 'ADMIN' ? '管理员' : '用户' }}</span>
      </div>
    </div>
    <nav class="sidebar-nav">
      <router-link to="/admin/dashboard" class="sidebar-link">
        <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/></svg>
        概览
      </router-link>
      <router-link to="/admin/urls" class="sidebar-link">
        <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/></svg>
        短链管理
      </router-link>
      <router-link to="/admin/stats" class="sidebar-link">
        <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>
        数据统计
      </router-link>
    </nav>
    <div class="sidebar-footer">
      <button class="sidebar-logout" @click="handleLogout">
        <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/></svg>
        退出登录
      </button>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  height: calc(100vh - var(--header-height));
  position: fixed;
  top: var(--header-height);
  left: 0;
  background: var(--color-canvas);
  border-right: 1px solid var(--color-hairline);
  display: flex;
  flex-direction: column;
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-lg) var(--space-md);
  border-bottom: 1px solid var(--color-hairline);
}

.sidebar-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  background: var(--color-primary);
  color: var(--color-on-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-sm);
  font-weight: 600;
  flex-shrink: 0;
}

.sidebar-user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.sidebar-user-name {
  font-size: var(--font-sm);
  font-weight: 500;
  color: var(--color-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-user-role {
  font-size: var(--font-xs);
  color: var(--color-mute);
}

.sidebar-nav {
  flex: 1;
  padding: var(--space-sm);
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sidebar-link {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-sm);
  color: var(--color-body);
  font-size: var(--font-sm);
  font-weight: 500;
  transition: background 0.15s;
}

.sidebar-link:hover {
  background: var(--color-canvas-soft);
  color: var(--color-ink);
}

.sidebar-link.router-link-active {
  background: var(--color-canvas-soft-2);
  color: var(--color-ink);
}

.sidebar-footer {
  padding: var(--space-sm);
  border-top: 1px solid var(--color-hairline);
}

.sidebar-logout {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  width: 100%;
  padding: var(--space-xs) var(--space-sm);
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-body);
  font-size: var(--font-sm);
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}

.sidebar-logout:hover {
  background: var(--color-error-soft);
  color: var(--color-error);
}

.icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}
</style>
