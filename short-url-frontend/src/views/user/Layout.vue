<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import UserSidebar from '@/components/UserSidebar.vue'

const auth = useAuthStore()
</script>

<template>
  <div class="user-layout">
    <UserSidebar />
    <div class="user-main">
      <div class="status-bar">
        <span class="status-dot" :class="{ online: auth.isLoggedIn }" />
        <span v-if="auth.isLoggedIn">欢迎回来，{{ auth.user?.nickname || auth.user?.username }}</span>
        <span v-else>当前状态：未登录</span>
      </div>
      <main class="user-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.user-layout {
  display: flex;
  min-height: 100vh;
}

.user-main {
  flex: 1;
  margin-left: var(--sidebar-width);
  display: flex;
  flex-direction: column;
}

.status-bar {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-sm) var(--space-xl);
  background: var(--card-bg);
  border-bottom: 1px solid var(--border-light);
  font-size: var(--font-sm);
  color: var(--text-secondary);
  position: sticky;
  top: 0;
  z-index: 20;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-mute);
  flex-shrink: 0;
}

.status-dot.online {
  background: var(--color-success);
}

.user-content {
  flex: 1;
  padding: var(--space-xl);
  background: var(--bg-main);
}
</style>
