import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '@/utils/storage'

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
      // 原型预览路由：暖棕纸质风登录页 Demo（不参与正式登录流程，仅方向评审）
      path: '/paper-login',
      name: 'paper-login-demo',
      component: () => import('@/prototypes/PaperLoginDemo.vue'),
      meta: { title: '登录·纸质原型', allowAnonymous: true },
    },
    {
      path: '/dashboard',
      component: () => import('@/components/system/AdminLayout.vue'),
      redirect: { name: 'system-home' },
      children: [
        {
          path: '',
          name: 'system-home',
          component: () => import('@/views/admin/DashboardHome.vue'),
          meta: { title: '仪表盘' },
        },
        {
          path: 'user',
          name: 'system-user',
          component: () => import('@/views/admin/UserView.vue'),
          meta: { title: '用户管理' },
        },
        {
          path: 'dept',
          name: 'system-dept',
          component: () => import('@/views/admin/DeptView.vue'),
          meta: { title: '部门管理' },
        },
        {
          path: 'role',
          name: 'system-role',
          component: () => import('@/views/admin/RoleView.vue'),
          meta: { title: '角色管理' },
        },
        {
          path: 'permission',
          name: 'system-permission',
          component: () => import('@/views/admin/PermissionView.vue'),
          meta: { title: '权限管理' },
        },
        {
          path: 'post',
          name: 'system-post',
          component: () => import('@/views/admin/PostView.vue'),
          meta: { title: '岗位管理' },
        },
        {
          path: 'log',
          name: 'system-log',
          component: () => import('@/views/admin/LogView.vue'),
          meta: { title: '操作日志' },
        },
      ],
    },
    // 兜底重定向回登录
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.afterEach((to) => {
  const title = (to.meta.title as string) ?? ''
  document.title = title ? `${title} · DocHub` : 'DocHub'
})

/**
 * 登录守卫：
 * - 已登录访问登录页 → 重定向仪表盘；
 * - 未登录访问受保护页 → 重定向登录页。
 */
router.beforeEach((to) => {
  const authed = isLoggedIn()
  // 原型预览路由无条件放行，供方向评审
  if (to.meta.allowAnonymous) {
    return true
  }
  if (to.name === 'login' && authed) {
    return { name: 'dashboard' }
  }
  if (to.name !== 'login' && !authed) {
    return { name: 'login' }
  }
  return true
})

export default router
