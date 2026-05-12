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
  <header class="header">
    <div class="header-inner">
      <router-link to="/" class="logo">短链接</router-link>
      <nav class="nav">
        <template v-if="auth.isLoggedIn">
          <span class="nav-user">{{ auth.user?.nickname || auth.user?.username }}</span>
          <button class="btn btn-ghost btn-sm" @click="handleLogout">退出</button>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-link">登录</router-link>
          <router-link to="/register" class="btn btn-primary btn-sm">注册</router-link>
        </template>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.header {
  height: var(--header-height);
  background: var(--color-canvas);
  border-bottom: 1px solid var(--color-hairline);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-lg);
}

.logo {
  font-size: var(--font-xl);
  font-weight: 600;
  color: var(--color-ink);
  letter-spacing: -0.6px;
}

.logo:hover {
  opacity: 0.7;
}

.nav {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.nav-link {
  color: var(--color-body);
  font-size: var(--font-sm);
  font-weight: 500;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-full);
  transition: background 0.15s;
}

.nav-link:hover {
  background: var(--color-canvas-soft);
  color: var(--color-ink);
}

.nav-link.router-link-active {
  color: var(--color-ink);
}

.nav-user {
  font-size: var(--font-sm);
  color: var(--color-body);
}
</style>
