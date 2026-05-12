<script setup lang="ts">
defineProps<{
  shortUrl: string
  longUrl: string
}>()

const emit = defineEmits<{
  copy: []
  reset: []
}>()

async function handleCopy() {
  try {
    await navigator.clipboard.writeText(shortUrl)
    emit('copy')
  } catch {
    // fallback
    const ta = document.createElement('textarea')
    ta.value = shortUrl
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
    emit('copy')
  }
}
</script>

<template>
  <div class="result-card card">
    <div class="result-label">生成成功！</div>
    <div class="result-url-wrap">
      <a :href="shortUrl" target="_blank" class="result-url">{{ shortUrl }}</a>
      <button class="btn btn-primary" @click="handleCopy">复制</button>
    </div>
    <div class="result-long">
      <span class="result-long-label">原始链接：</span>
      <span class="result-long-text">{{ longUrl }}</span>
    </div>
    <button class="btn btn-ghost result-back" @click="emit('reset')">继续生成</button>
  </div>
</template>

<style scoped>
.result-card {
  padding: var(--space-6);
  text-align: center;
}

.result-label {
  font-size: var(--font-lg);
  font-weight: 600;
  color: var(--color-success);
  margin-bottom: var(--space-4);
}

.result-url-wrap {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  justify-content: center;
  margin-bottom: var(--space-3);
}

.result-url {
  font-size: var(--font-xl);
  font-weight: 600;
  color: var(--color-primary);
}

.result-long {
  font-size: var(--font-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-4);
  word-break: break-all;
}

.result-long-label {
  flex-shrink: 0;
}

.result-back {
  margin-top: var(--space-2);
}
</style>
