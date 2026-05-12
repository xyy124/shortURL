<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'
</script>

<template>
  <FeaturePlaceholder
    title="短链接转长链接"
    icon="🔄"
    description="输入短链接，解析还原为原始长链接。"
    :features="['短链接还原查询', '历史记录查看']"
  />
</template>
