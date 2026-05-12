<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { createShortUrl, createCustomShortUrl } from '@/api/shorten'
import ResultCard from '@/components/ResultCard.vue'

const auth = useAuthStore()
const siteHost = computed(() => window.location.host)

const longUrl = ref('')
const customCode = ref('')
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
      <h1 class="hero-title">短链接生成器</h1>
      <p class="hero-desc">将长链接转换为简短、易分享的短链接，支持自定义后缀和数据统计</p>
    </div>

    <div class="form-wrap card">
      <template v-if="!result">
        <div class="form-group">
          <label class="form-label">长链接</label>
          <input
            v-model="longUrl"
            type="url"
            class="input"
            placeholder="请输入长链接，如 https://example.com/very/long/url"
            @keyup.enter="handleSubmit"
          />
        </div>
        <div class="form-group">
          <label class="form-label">
            自定义后缀
            <span class="form-optional">（选填，仅登录用户可用）</span>
          </label>
          <div class="custom-code-wrap">
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
        </div>
        <p v-if="error" class="alert alert-error">{{ error }}</p>
        <button class="btn btn-primary btn-submit" :disabled="loading" @click="handleSubmit">
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
  padding: var(--space-10) 0 var(--space-8);
}

.hero-title {
  font-size: var(--font-3xl);
  font-weight: 800;
  color: var(--color-text);
  margin-bottom: var(--space-3);
}

.hero-desc {
  color: var(--color-text-secondary);
  font-size: var(--font-base);
  max-width: 480px;
  margin: 0 auto;
}

.form-wrap {
  max-width: 560px;
  margin: 0 auto;
  padding: var(--space-6);
  position: relative;
}

.form-group {
  margin-bottom: var(--space-5);
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

.form-hint {
  font-size: var(--font-xs);
  color: var(--color-text-secondary);
  margin-top: var(--space-1);
}

.custom-code-wrap {
  display: flex;
  align-items: center;
  gap: 0;
}

.custom-code-prefix {
  padding: var(--space-2) var(--space-3);
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-right: none;
  border-radius: var(--radius-sm) 0 0 var(--radius-sm);
  font-size: var(--font-sm);
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.custom-code-wrap .input {
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
}

.btn-submit {
  width: 100%;
  padding: var(--space-3);
  font-size: var(--font-base);
  margin-top: var(--space-4);
}

.copy-toast {
  position: absolute;
  bottom: var(--space-3);
  left: 50%;
  transform: translateX(-50%);
  font-size: var(--font-sm);
  color: var(--color-success);
}
</style>
