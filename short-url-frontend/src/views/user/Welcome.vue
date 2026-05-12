<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()
</script>

<template>
  <div class="welcome">
    <h1 class="welcome-title">欢迎使用短链接服务</h1>
    <p class="welcome-desc">快速将长链接转换为短链接，方便分享与管理。</p>

    <div class="guide-card">
      <div class="guide-icon">📋</div>
      <div class="guide-text">
        <p class="guide-primary">点击左侧「长链接转短链接」开始使用</p>
        <p class="guide-secondary">未登录用户也可使用基础功能</p>
      </div>
    </div>

    <div v-if="!auth.isLoggedIn" class="features-preview">
      <p class="preview-label">登录后可解锁更多功能：</p>
      <div class="preview-tags">
        <span class="preview-tag">自定义后缀</span>
        <span class="preview-tag">短链还原</span>
        <span class="preview-tag">数据统计</span>
        <span class="preview-tag">批量生成</span>
      </div>
    </div>

    <div v-else class="features-preview">
      <p class="preview-label">您已登录，可使用全部功能：</p>
      <div class="preview-tags">
        <span class="preview-tag tag-active">自定义后缀</span>
        <span class="preview-tag tag-active">数据统计</span>
        <span class="preview-tag tag-active">批量生成</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.welcome {
  max-width: 640px;
  animation: fadeUp 0.3s ease-out;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.welcome-title {
  font-size: var(--font-3xl);
  font-weight: 700;
  letter-spacing: -1.2px;
  color: var(--text-primary);
  margin-bottom: var(--space-sm);
  line-height: 1.2;
}

.welcome-desc {
  font-size: var(--font-lg);
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: var(--space-2xl);
}

.guide-card {
  display: flex;
  align-items: flex-start;
  gap: var(--space-md);
  padding: var(--space-lg);
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  margin-bottom: var(--space-xl);
}

.guide-icon {
  font-size: 28px;
  flex-shrink: 0;
  line-height: 1;
}

.guide-primary {
  font-size: var(--font-base);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-xxs);
}

.guide-secondary {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.features-preview {
  margin-top: var(--space-lg);
}

.preview-label {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-sm);
}

.preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-xs);
}

.preview-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: var(--font-xs);
  background: var(--color-canvas-soft);
  color: var(--text-muted);
  border: 1px solid var(--border-light);
}

.tag-active {
  background: var(--accent-muted);
  color: #92400e;
  border-color: var(--accent-light);
}
</style>
