import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import NotFound from '@/views/NotFound.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/views/user/Layout.vue'),
      children: [
        { path: '', name: 'Welcome', component: () => import('@/views/user/Welcome.vue') },
        { path: 'long-to-short', name: 'LongToShort', component: () => import('@/views/user/LongToShort.vue') },
        { path: 'short-to-long', name: 'ShortToLong', component: () => import('@/views/user/ShortToLong.vue') },
        { path: 'stats', name: 'UserStats', component: () => import('@/views/user/Stats.vue') },
        { path: 'batch', name: 'Batch', component: () => import('@/views/user/Batch.vue') },
        { path: 'qrcode', name: 'QRCode', component: () => import('@/views/user/QRCode.vue') },
      ],
    },
    { path: '/login', name: 'Login', component: Login },
    { path: '/register', name: 'Register', component: Register },
    {
      path: '/admin',
      component: () => import('@/views/admin/Layout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: { name: 'Dashboard' } },
        { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/admin/Dashboard.vue') },
        { path: 'urls', name: 'UrlList', component: () => import('@/views/admin/UrlList.vue') },
        { path: 'stats', name: 'AdminStats', component: () => import('@/views/admin/Stats.vue') },
      ],
    },
    { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFound },
  ],
})

router.beforeEach((to, _from) => {
  if (to.meta.requiresAuth) {
    const auth = useAuthStore()
    if (!auth.isLoggedIn) {
      return { name: 'Login', query: { redirect: to.fullPath } }
    }
  }
})

export default router
