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
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
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
      axisLine: { lineStyle: { color: '#E5E7EB' } },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#F3F4F6' } },
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
    <div v-else-if="!hasData" class="loading-wrap" style="color:var(--color-text-secondary)">暂无数据</div>
    <div v-else ref="chartRef" class="chart-wrap card" />
  </div>
</template>

<style scoped>
.stats-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
}

.stats-select {
  width: auto;
  min-width: 140px;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: var(--space-10);
}

.chart-wrap {
  width: 100%;
  height: 400px;
  padding: var(--space-4);
}
</style>
