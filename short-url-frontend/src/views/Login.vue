<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  error.value = ''
  if (!username.value.trim() || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  try {
    await auth.login(username.value.trim(), password.value)
    const redirect = (route.query.redirect as string) || '/admin/dashboard'
    router.push(redirect)
  } catch (e: any) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="page">
    <div class="auth-card card">
      <h1 class="auth-title">登录</h1>
      <p class="auth-desc">登录后管理短链接和查看统计数据</p>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <input v-model="username" class="input" placeholder="请输入用户名" autocomplete="username" />
        </div>
        <div class="form-group">
          <label class="form-label">密码</label>
          <input v-model="password" type="password" class="input" placeholder="请输入密码" autocomplete="current-password" />
        </div>
        <p v-if="error" class="alert alert-error">{{ error }}</p>
        <button class="btn btn-primary btn-submit" :disabled="loading">
          <span v-if="loading" class="spinner" />
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <p class="auth-switch">还没有账号？<router-link to="/register">立即注册</router-link></p>
    </div>
  </main>
</template>

<style scoped>
.auth-card {
  max-width: 400px;
  margin: var(--space-10) auto;
  padding: var(--space-8);
}

.auth-title {
  font-size: var(--font-2xl);
  font-weight: 700;
  text-align: center;
  margin-bottom: var(--space-2);
}

.auth-desc {
  text-align: center;
  color: var(--color-text-secondary);
  font-size: var(--font-sm);
  margin-bottom: var(--space-6);
}

.form-group {
  margin-bottom: var(--space-4);
}

.form-label {
  display: block;
  font-size: var(--font-sm);
  font-weight: 500;
  margin-bottom: var(--space-2);
  color: var(--color-text);
}

.btn-submit {
  width: 100%;
  padding: var(--space-3);
  font-size: var(--font-base);
  margin-top: var(--space-4);
}

.auth-switch {
  text-align: center;
  margin-top: var(--space-5);
  font-size: var(--font-sm);
  color: var(--color-text-secondary);
}
</style>
