import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 配置：别名 @ 指向 src，便于跨目录引用 Token 与通用组件
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    open: true,
    proxy: {
      // 后端 context-path 为 /backend，端口 10086（见 backend application-dev.yml）
      '/backend': {
        target: 'http://localhost:10086',
        changeOrigin: true,
      },
    },
  },
})