import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios'
import type { ApiResponse } from './types'

const request = axios.create({
  baseURL: '/',
  timeout: 15000,
})

request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('token')
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    const body = res.data as ApiResponse<unknown>
    if (body.code !== 200) {
      return Promise.reject(new Error(body.msg || '请求失败'))
    }
    return res.data
  },
  (err: AxiosError<ApiResponse<null>>) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    const msg = err.response?.data?.msg || err.message || '请求失败'
    return Promise.reject(new Error(msg))
  },
)

export default request
