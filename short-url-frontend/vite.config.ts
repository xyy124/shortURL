import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8060',
        changeOrigin: true,
      },
      // 短链接重定向：6 位 Base62 短码（须含大写字母或数字，避免匹配前端路由如 /stats）
      '^/(?=.*[A-Z0-9])[a-zA-Z0-9]{6}$': {
        target: 'http://localhost:8060',
        changeOrigin: true,
      },
    },
  },
})
