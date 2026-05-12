<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'
</script>

<template>
  <FeaturePlaceholder
    title="批量生成"
    icon="📦"
    description="批量导入长链接，一次性生成多个短链接。"
    :features="['批量导入（CSV/文本）', '批量导出', '生成结果预览']"
  />
</template>
