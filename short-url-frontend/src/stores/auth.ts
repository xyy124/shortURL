import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserVO } from '@/api/types'
import * as authApi from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<UserVO | null>(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function saveAuth(t: string, u: UserVO) {
    token.value = t
    user.value = u
    localStorage.setItem('token', t)
    localStorage.setItem('user', JSON.stringify(u))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function login(username: string, password: string) {
    const res = await authApi.login(username, password)
    saveAuth(res.data.token, res.data.user)
  }

  async function register(username: string, password: string, nickname: string) {
    const res = await authApi.register(username, password, nickname)
    saveAuth(res.data.token, res.data.user)
  }

  return { token, user, isLoggedIn, isAdmin, login, register, logout }
})
