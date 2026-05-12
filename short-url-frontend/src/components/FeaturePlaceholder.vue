<script setup lang="ts">
defineProps<{
  title: string
  icon: string
  description?: string
  features?: string[]
}>()
</script>

<template>
  <div class="placeholder-page">
    <h1 class="placeholder-title">{{ icon }} {{ title }}</h1>
    <div class="placeholder-card">
      <div class="placeholder-icon">🚧</div>
      <p class="placeholder-main">功能开发中</p>
      <p class="placeholder-sub">敬请期待...</p>
      <p v-if="description" class="placeholder-desc">{{ description }}</p>
      <div v-if="features?.length" class="placeholder-features">
        <p class="placeholder-features-label">预计提供：</p>
        <ul>
          <li v-for="f in features" :key="f">{{ f }}</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.placeholder-page {
  max-width: 560px;
  animation: fadeUp 0.3s ease-out;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.placeholder-title {
  font-size: var(--font-2xl);
  font-weight: 700;
  letter-spacing: -0.8px;
  color: var(--text-primary);
  margin-bottom: var(--space-lg);
}

.placeholder-card {
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--space-2xl);
  text-align: center;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: var(--space-md);
}

.placeholder-main {
  font-size: var(--font-xl);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-xxs);
}

.placeholder-sub {
  font-size: var(--font-sm);
  color: var(--text-muted);
  margin-bottom: var(--space-lg);
}

.placeholder-desc {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-lg);
  line-height: 1.5;
}

.placeholder-features {
  text-align: left;
  background: var(--bg-main);
  border-radius: var(--radius-md);
  padding: var(--space-md) var(--space-lg);
}

.placeholder-features-label {
  font-size: var(--font-sm);
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: var(--space-xs);
}

.placeholder-features ul {
  list-style: none;
  padding: 0;
}

.placeholder-features li {
  position: relative;
  padding-left: var(--space-md);
  font-size: var(--font-sm);
  color: var(--text-secondary);
  line-height: 1.8;
}

.placeholder-features li::before {
  content: '·';
  position: absolute;
  left: 0;
  font-weight: 700;
  color: var(--accent);
}
</style>
