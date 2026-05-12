<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'
</script>

<template>
  <FeaturePlaceholder
    title="访问统计"
    icon="📊"
    description="查看短链接的访问数据，包括 PV、UV、独立 IP 等。"
    :features="['PV / UV 统计', '趋势折线图', '设备 / 浏览器分布']"
  />
</template>
