<script setup lang="ts">
import AppSidebar from '@/components/AppSidebar.vue'
</script>

<template>
  <div class="admin-layout">
    <AppSidebar />
    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: calc(100vh - var(--header-height));
}

.admin-main {
  flex: 1;
  margin-left: var(--sidebar-width);
  padding: var(--space-6);
}
</style>
