<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'
</script>

<template>
  <FeaturePlaceholder
    title="二维码生成"
    icon="📱"
    description="为短链接生成二维码，扫码即可访问。"
    :features="['二维码图片生成', '尺寸 / 颜色自定义', 'PNG 下载']"
  />
</template>
