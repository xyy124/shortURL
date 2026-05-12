<template>
  <main class="page not-found">
    <h1 class="nf-code">404</h1>
    <p class="nf-desc">页面不存在</p>
    <router-link to="/" class="btn btn-primary">返回首页</router-link>
  </main>
</template>

<style scoped>
.not-found {
  text-align: center;
  padding-top: var(--space-10);
}

.nf-code {
  font-size: 96px;
  font-weight: 800;
  color: var(--color-primary);
  margin-bottom: var(--space-2);
}

.nf-desc {
  font-size: var(--font-lg);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-6);
}
</style>
