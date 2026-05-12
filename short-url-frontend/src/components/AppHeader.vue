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
        <router-link to="/" class="nav-link">首页</router-link>
        <template v-if="auth.isLoggedIn">
          <router-link to="/admin/dashboard" class="nav-link">管理后台</router-link>
          <span class="nav-user">{{ auth.user?.nickname || auth.user?.username }}</span>
          <button class="btn btn-ghost" @click="handleLogout">退出</button>
        </template>
        <template v-else>
          <router-link to="/login" class="btn btn-ghost">登录</router-link>
          <router-link to="/register" class="btn btn-primary">注册</router-link>
        </template>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.header {
  height: var(--header-height);
  background: var(--color-card);
  border-bottom: 1px solid var(--color-border);
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
  padding: 0 var(--space-6);
}

.logo {
  font-size: var(--font-xl);
  font-weight: 700;
  color: var(--color-primary);
}

.logo:hover {
  color: var(--color-primary-hover);
}

.nav {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.nav-link {
  color: var(--color-text-secondary);
  font-size: var(--font-sm);
  font-weight: 500;
}

.nav-link:hover {
  color: var(--color-text);
}

.nav-link.router-link-active {
  color: var(--color-primary);
}

.nav-user {
  font-size: var(--font-sm);
  color: var(--color-text-secondary);
}
</style>
