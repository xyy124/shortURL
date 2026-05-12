<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOverview } from '@/api/stats'
import type { StatsOverview } from '@/api/types'

const overview = ref<StatsOverview | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getOverview()
    overview.value = res.data
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})

const cards = [
  { label: '总短链数', key: 'totalUrls' as const, color: '#4F46E5' },
  { label: '总访问量', key: 'totalViews' as const, color: '#10B981' },
  { label: '今日新增', key: 'todayNewUrls' as const, color: '#F59E0B' },
  { label: '今日访问', key: 'todayViews' as const, color: '#EF4444' },
]
</script>

<template>
  <div class="page">
    <h1 class="page-title">概览</h1>
    <div v-if="loading" class="loading-wrap"><span class="spinner" /></div>
    <div v-else class="stats-grid">
      <div v-for="card in cards" :key="card.key" class="stat-card card">
        <div class="stat-dot" :style="{ background: card.color }" />
        <div class="stat-info">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ overview?.[card.key] ?? '-' }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.loading-wrap {
  display: flex;
  justify-content: center;
  padding: var(--space-2xl);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--space-lg);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-lg);
}

.stat-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.stat-label {
  font-size: var(--font-sm);
  color: var(--color-body);
}

.stat-value {
  font-size: var(--font-2xl);
  font-weight: 700;
}
</style>
