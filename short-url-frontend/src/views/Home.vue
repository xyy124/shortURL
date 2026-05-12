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
  if (!code || !auth.isLoggedIn) return ''
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
  <main class="page">
    <div class="hero">
      <h1 class="hero-title">短链接 · 让分享更简单</h1>
      <p class="hero-desc">将长链接转换为简短、易分享的短链接，支持自定义后缀和数据统计</p>
    </div>

    <div class="form-wrap card">
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
          <div class="custom-code-field" :class="{ 'is-disabled': !auth.isLoggedIn }">
            <span class="custom-code-prefix">{{ siteHost }}/</span>
            <input
              v-model="customCode"
              type="text"
              class="input"
              placeholder="my-link"
              :disabled="!auth.isLoggedIn"
            />
          </div>
          <p v-if="!auth.isLoggedIn" class="form-hint">登录后可使用自定义短链功能</p>
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
  </main>
</template>

<style scoped>
.hero {
  text-align: center;
  padding: var(--space-4xl) 0 var(--space-2xl);
}

.hero-title {
  font-size: var(--font-4xl);
  font-weight: 600;
  letter-spacing: -2.4px;
  color: var(--color-ink);
  margin-bottom: var(--space-md);
  line-height: var(--font-4xl-lh);
}

.hero-desc {
  color: var(--color-body);
  font-size: var(--font-lg);
  line-height: var(--font-lg-lh);
  max-width: 480px;
  margin: 0 auto;
}

.form-wrap {
  max-width: 560px;
  margin: 0 auto;
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
  color: var(--color-ink);
}

.form-optional {
  color: var(--color-mute);
  font-weight: 400;
  font-size: var(--font-xs);
}

.form-hint {
  font-size: var(--font-xs);
  color: var(--color-mute);
  margin-top: var(--space-xxs);
}

.form-error {
  font-size: var(--font-xs);
  color: var(--color-error);
  margin-top: var(--space-xxs);
}

.custom-code-field {
  display: flex;
  align-items: center;
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-sm);
  overflow: hidden;
  transition: border-color 0.15s;
}

.custom-code-field:focus-within {
  border-color: var(--color-link);
  box-shadow: 0 0 0 1px var(--color-link);
}

.custom-code-field.is-disabled {
  opacity: 0.5;
}

.custom-code-prefix {
  padding: 0 var(--space-sm);
  height: 40px;
  display: flex;
  align-items: center;
  background: var(--color-canvas-soft);
  color: var(--color-body);
  font-size: var(--font-sm);
  white-space: nowrap;
  border-right: 1px solid var(--color-hairline);
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
