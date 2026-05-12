<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { getDailyStats } from '@/api/stats'
import type { DailyStatsItem } from '@/api/types'
import * as echarts from 'echarts'

const days = ref(7)
const loading = ref(false)
const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

async function fetchData() {
  loading.value = true
  hasData.value = false
  try {
    const res = await getDailyStats({ days: days.value })
    loading.value = false
    await nextTick()
    renderChart(res.data)
  } catch (e) {
    console.error('获取统计数据失败', e)
    loading.value = false
  }
}

const hasData = ref(false)

function renderChart(data: DailyStatsItem[]) {
  hasData.value = data.length > 0
  if (!chartRef.value || !hasData.value) return
  if (chart) chart.dispose()
  chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['PV', 'UV', 'IP'],
      bottom: 0,
    },
    grid: { left: 50, right: 20, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: data.map((d) => d.statsDate.substring(5)),
      axisLine: { lineStyle: { color: '#ebebeb' } },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0' } },
    },
    series: [
      {
        name: 'PV',
        type: 'line',
        smooth: true,
        data: data.map((d) => d.pv),
        itemStyle: { color: '#4F46E5' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(79,70,229,0.2)' },
          { offset: 1, color: 'rgba(79,70,229,0)' },
        ])},
      },
      {
        name: 'UV',
        type: 'line',
        smooth: true,
        data: data.map((d) => d.uv),
        itemStyle: { color: '#10B981' },
      },
      {
        name: 'IP',
        type: 'line',
        smooth: true,
        data: data.map((d) => d.ipCount),
        itemStyle: { color: '#F59E0B' },
      },
    ],
  })
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', () => chart?.resize())
})
onUnmounted(() => {
  chart?.dispose()
})
watch(days, fetchData)
</script>

<template>
  <div class="page">
    <div class="stats-header">
      <h1 class="page-title" style="margin-bottom: 0">数据统计</h1>
      <select v-model.number="days" class="input stats-select">
        <option :value="7">最近 7 天</option>
        <option :value="14">最近 14 天</option>
        <option :value="30">最近 30 天</option>
      </select>
    </div>
    <div v-if="loading" class="loading-wrap"><span class="spinner" /></div>
    <div v-else ref="chartRef" class="chart-wrap card">
      <div v-if="!hasData" class="empty-state">暂无数据</div>
    </div>
    <div class="stats-legend">
      <div class="legend-item">
        <span class="legend-dot" style="background: #4F46E5" />
        <span><strong>PV</strong>（Page View）- 页面浏览次数，每访问一次短链计数+1</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot" style="background: #10B981" />
        <span><strong>UV</strong>（Unique Visitor）- 独立访客数，按 IP 去重统计</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot" style="background: #F59E0B" />
        <span><strong>IP</strong> - 独立 IP 数，按来源 IP 去重统计</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stats-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-lg);
}

.stats-select {
  width: auto;
  min-width: 140px;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: var(--space-2xl);
}

.chart-wrap {
  position: relative;
  width: 100%;
  height: 400px;
  padding: var(--space-md);
}

.empty-state {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-body);
  font-size: var(--font-sm);
}

.stats-legend {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-md) var(--space-lg);
  margin-top: var(--space-md);
  padding: var(--space-md);
  background: var(--color-canvas-soft);
  border-radius: var(--radius-sm);
  font-size: var(--font-sm);
  color: var(--color-body);
  line-height: var(--font-sm-lh);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-item strong {
  color: var(--color-ink);
}
</style>
