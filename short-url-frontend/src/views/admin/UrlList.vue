<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUrlList, deleteUrl, toggleUrlActive } from '@/api/urlManage'
import type { UrlMapRecord } from '@/api/types'

const records = ref<UrlMapRecord[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const res = await getUrlList({ page: page.value, size })
    records.value = res.data.records
    total.value = res.data.total
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function handleToggle(id: number) {
  try {
    await toggleUrlActive(id)
    await fetchData()
  } catch {
    // handled by interceptor
  }
}

async function handleDelete(id: number) {
  if (!confirm('确定删除该短链？')) return
  try {
    await deleteUrl(id)
    await fetchData()
  } catch {
    // handled by interceptor
  }
}

function formatDate(s: string | null) {
  if (!s) return '-'
  return s.substring(0, 19).replace('T', ' ')
}

const totalPages = () => Math.ceil(total.value / size)

onMounted(fetchData)
</script>

<template>
  <div class="page">
    <h1 class="page-title">短链管理</h1>
    <div class="card table-wrap">
      <table>
        <thead>
          <tr>
            <th>短码</th>
            <th>目标链接</th>
            <th>访问量</th>
            <th>类型</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="table-loading"><span class="spinner" /></td>
          </tr>
          <tr v-else-if="records.length === 0">
            <td colspan="7" class="table-empty">暂无数据</td>
          </tr>
          <tr v-for="r in records" :key="r.id">
            <td><code>{{ r.shortCode }}</code></td>
            <td class="td-long" :title="r.longUrl">{{ r.longUrl }}</td>
            <td>{{ r.views }}</td>
            <td>
              <span class="badge" :class="r.isCustom ? 'badge-warning' : 'badge-success'">
                {{ r.isCustom ? '自定义' : '自动' }}
              </span>
            </td>
            <td>
              <span class="badge" :class="r.isActive ? 'badge-success' : 'badge-danger'">
                {{ r.isActive ? '启用' : '禁用' }}
              </span>
            </td>
            <td>{{ formatDate(r.createTime) }}</td>
            <td class="td-actions">
              <button class="btn btn-ghost btn-sm" @click="handleToggle(r.id)">
                {{ r.isActive ? '禁用' : '启用' }}
              </button>
              <button class="btn btn-danger btn-sm" @click="handleDelete(r.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-if="totalPages() > 1" class="pagination">
      <button class="btn btn-ghost btn-sm" :disabled="page <= 1" @click="page--; fetchData()">上一页</button>
      <span class="page-info">{{ page }} / {{ totalPages() }}</span>
      <button class="btn btn-ghost btn-sm" :disabled="page >= totalPages()" @click="page++; fetchData()">下一页</button>
    </div>
  </div>
</template>

<style scoped>
.td-long {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.td-actions {
  display: flex;
  gap: var(--space-xs);
}

.btn-sm {
  padding: var(--space-xxs) var(--space-sm);
  font-size: var(--font-xs);
}

.table-loading,
.table-empty {
  text-align: center;
  padding: var(--space-2xl) !important;
  color: var(--color-body);
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-md);
  margin-top: var(--space-md);
}

.page-info {
  font-size: var(--font-sm);
  color: var(--color-body);
}
</style>
