<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { createShortUrl, createCustomShortUrl } from '@/api/shorten'
import ResultCard from '@/components/ResultCard.vue'

const auth = useAuthStore()
const siteHost = computed(() => window.location.host)

const longUrl = ref('')
const customCode = ref('')

const customCodeError = computed(() => {
  const code = customCode.value.trim()
  if (!code) return ''
  if (!auth.isLoggedIn) return ''
  if (!/^[a-zA-Z]/.test(code)) return '必须以字母开头'
  if (!/^[a-zA-Z][a-zA-Z0-9]{3,15}$/.test(code)) return '4-16位字母数字组合'
  return ''
})

const loading = ref(false)
const error = ref('')
const result = ref<{ shortUrl: string; longUrl: string } | null>(null)
const copied = ref(false)

async function handleSubmit() {
  error.value = ''
  copied.value = false

  const url = longUrl.value.trim()
  if (!url) {
    error.value = '请输入链接地址'
    return
  }

  if (customCodeError.value) return

  loading.value = true
  try {
    const custom = customCode.value.trim()
    if (custom && !auth.isLoggedIn) {
      error.value = '请登录后使用自定义后缀功能'
      return
    }
    const res = custom
      ? await createCustomShortUrl(url, custom)
      : await createShortUrl(url)
    result.value = res.data
  } catch (e: any) {
    error.value = e.message || '生成失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function handleCopy() {
  copied.value = true
  setTimeout(() => (copied.value = false), 2000)
}

function handleReset() {
  result.value = null
  longUrl.value = ''
  customCode.value = ''
}
</script>

<template>
  <div class="l2s-page">
    <h1 class="l2s-title">长链接转短链接</h1>
    <p class="l2s-desc">输入长链接，快速生成短链接。</p>

    <div class="l2s-card">
      <template v-if="!result">
        <div class="form-group">
          <label class="form-label">长链接</label>
          <input
            v-model="longUrl"
            type="url"
            class="input input-lg"
            placeholder="请输入长链接，如 https://example.com/very/long/url"
            @keyup.enter="handleSubmit"
          />
        </div>
        <div class="form-group">
          <label class="form-label">
            自定义后缀
            <span class="form-optional">选填</span>
          </label>
          <div class="custom-code-field">
            <span class="custom-code-prefix">{{ siteHost }}/</span>
            <input
              v-model="customCode"
              type="text"
              class="input"
              placeholder="my-link"
            />
          </div>
          <p v-if="!auth.isLoggedIn && customCode.trim()" class="form-hint hint-login">
            登录后可使用自定义后缀
          </p>
          <p v-else-if="customCode.trim() && customCodeError" class="form-error">{{ customCodeError }}</p>
          <p v-else class="form-hint">以字母开头，4-16位字母数字</p>
        </div>
        <p v-if="error" class="alert alert-error">{{ error }}</p>
        <button class="btn btn-primary btn-pill btn-submit" :disabled="loading" @click="handleSubmit">
          <span v-if="loading" class="spinner" />
          {{ loading ? '生成中...' : '生成短链接' }}
        </button>
      </template>

      <ResultCard
        v-else
        :short-url="result.shortUrl"
        :long-url="result.longUrl"
        @copy="handleCopy"
        @reset="handleReset"
      />
      <p v-if="copied" class="copy-toast">已复制到剪贴板</p>
    </div>
  </div>
</template>

<style scoped>
.l2s-page {
  max-width: 560px;
  animation: fadeUp 0.3s ease-out;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.l2s-title {
  font-size: var(--font-2xl);
  font-weight: 700;
  letter-spacing: -0.8px;
  color: var(--text-primary);
  margin-bottom: var(--space-xs);
}

.l2s-desc {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-lg);
}

.l2s-card {
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--space-xl) var(--space-lg);
  position: relative;
}

.form-group {
  margin-bottom: var(--space-lg);
}

.form-label {
  display: block;
  font-size: var(--font-sm);
  font-weight: 500;
  margin-bottom: var(--space-xs);
  color: var(--text-primary);
}

.form-optional {
  color: var(--text-muted);
  font-weight: 400;
  font-size: var(--font-xs);
}

.form-hint {
  font-size: var(--font-xs);
  color: var(--text-muted);
  margin-top: var(--space-xxs);
}

.hint-login {
  color: var(--accent);
}

.form-error {
  font-size: var(--font-xs);
  color: var(--color-error);
  margin-top: var(--space-xxs);
}

.input-lg {
  height: 44px;
  font-size: var(--font-base);
}

.custom-code-field {
  display: flex;
  align-items: center;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  overflow: hidden;
  transition: border-color 0.15s;
}

.custom-code-field:focus-within {
  border-color: var(--color-link);
  box-shadow: 0 0 0 1px var(--color-link);
}

.custom-code-prefix {
  padding: 0 var(--space-sm);
  height: 40px;
  display: flex;
  align-items: center;
  background: var(--bg-main);
  color: var(--text-secondary);
  font-size: var(--font-sm);
  white-space: nowrap;
  border-right: 1px solid var(--border-light);
  flex-shrink: 0;
}

.custom-code-field .input {
  border: none;
  border-radius: 0;
  height: 40px;
}

.custom-code-field .input:focus {
  box-shadow: none;
}

.btn-submit {
  width: 100%;
  margin-top: var(--space-lg);
}

.copy-toast {
  position: absolute;
  bottom: var(--space-sm);
  left: 50%;
  transform: translateX(-50%);
  font-size: var(--font-sm);
  color: var(--color-success);
}
</style>
