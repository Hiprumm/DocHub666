import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由表：
 * - '/'      登录页（首个落地页面）
 * - '/dashboard' 占位路由，登录成功后跳转
 */
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { title: '仪表盘' },
    },
    // 兜底重定向回登录
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.afterEach((to) => {
  const title = (to.meta.title as string) ?? ''
  document.title = title ? `${title} · DocHub` : 'DocHub'
})

export default router