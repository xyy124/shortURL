<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const nickname = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref('')

async function handleRegister() {
  error.value = ''
  if (!username.value.trim() || !password.value) {
    error.value = '请填写完整信息'
    return
  }
  if (password.value !== confirmPassword.value) {
    error.value = '两次密码输入不一致'
    return
  }
  if (password.value.length < 6) {
    error.value = '密码长度不少于 6 位'
    return
  }
  loading.value = true
  try {
    await auth.register(username.value.trim(), password.value, nickname.value.trim() || username.value.trim())
    router.push('/admin/dashboard')
  } catch (e: any) {
    error.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="page">
    <div class="auth-card card">
      <h1 class="auth-title">注册</h1>
      <p class="auth-desc">创建账号以使用完整功能</p>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <input v-model="username" class="input" placeholder="请输入用户名" autocomplete="username" />
        </div>
        <div class="form-group">
          <label class="form-label">
            昵称
            <span class="form-optional">（选填）</span>
          </label>
          <input v-model="nickname" class="input" placeholder="选填" />
        </div>
        <div class="form-group">
          <label class="form-label">密码</label>
          <input v-model="password" type="password" class="input" placeholder="至少 6 位" autocomplete="new-password" />
        </div>
        <div class="form-group">
          <label class="form-label">确认密码</label>
          <input v-model="confirmPassword" type="password" class="input" placeholder="再次输入密码" autocomplete="new-password" />
        </div>
        <p v-if="error" class="alert alert-error">{{ error }}</p>
        <button class="btn btn-primary btn-submit" :disabled="loading">
          <span v-if="loading" class="spinner" />
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      <p class="auth-switch">已有账号？<router-link to="/login">立即登录</router-link></p>
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

.form-optional {
  color: var(--color-text-secondary);
  font-weight: 400;
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
