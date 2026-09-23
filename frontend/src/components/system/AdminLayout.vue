<script setup lang="ts">
/**
 * AdminLayout —— 系统管理 App Shell（侧边导航 + 顶栏 + 内容区）。
 * 暖棕「纸质笔记本」主题：侧栏为书脊深棕，内容区为浅纸；
 * 侧边高亮当前激活项。
 */
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { logoutApi } from '@/api/auth'
import { getAuth, clearAuth } from '@/utils/storage'

const route = useRoute()
const router = useRouter()
const loggingOut = ref(false)
const auth = getAuth()

const navItems = [
  { to: '/dashboard', label: '仪表盘', icon: 'dash' },
  { to: '/dashboard/user', label: '用户管理', icon: 'user' },
  { to: '/dashboard/dept', label: '部门管理', icon: 'dept' },
  { to: '/dashboard/role', label: '角色管理', icon: 'role' },
  { to: '/dashboard/permission', label: '权限管理', icon: 'perm' },
  { to: '/dashboard/post', label: '岗位管理', icon: 'post' },
  { to: '/dashboard/log', label: '操作日志', icon: 'log' },
]

const currentLabel = computed(() => {
  const match = navItems.find((n) => n.to === route.path)
  return match ? match.label : '仪表盘'
})
const displayName = computed(() => auth?.realName || auth?.username || '用户')

async function handleLogout() {
  loggingOut.value = true
  try {
    await logoutApi()
  } catch {
    // 忽略后端异常，继续本地登出
  } finally {
    clearAuth()
    loggingOut.value = false
    void router.replace('/')
  }
}
</script>

<template>
  <div class="admin-shell">
    <!-- 侧边导航 -->
    <aside class="admin-side" aria-label="系统管理导航">
      <div class="admin-side__brand">
        <span class="admin-side__logo" aria-hidden="true">
          <svg width="26" height="26" viewBox="0 0 32 32" fill="none">
            <rect width="32" height="32" rx="6" stroke="var(--color-accent-mark)" stroke-width="1" />
            <path d="M9 8h9l5 5v11H9z" stroke="var(--color-accent-mark)" stroke-width="1.4" stroke-linejoin="round" />
            <path d="M18 8v5h5" stroke="var(--color-accent-mark)" stroke-width="1.4" stroke-linejoin="round" />
            <path d="M13 19h7M13 22h5" stroke="var(--color-accent-mark)" stroke-width="1.2" stroke-linecap="round" />
          </svg>
        </span>
        <span class="admin-side__name">DocHub</span>
      </div>

      <nav class="admin-nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="admin-nav__item"
          :class="{ 'is-active': route.path === item.to }"
        >
          <span class="admin-nav__icon" aria-hidden="true">
            <svg v-if="item.icon === 'dash'" width="16" height="16" viewBox="0 0 24 24" fill="none">
              <rect x="3" y="3" width="7" height="7" rx="1" stroke="currentColor" stroke-width="1.5" />
              <rect x="14" y="3" width="7" height="7" rx="1" stroke="currentColor" stroke-width="1.5" />
              <rect x="3" y="14" width="7" height="7" rx="1" stroke="currentColor" stroke-width="1.5" />
              <rect x="14" y="14" width="7" height="7" rx="1" stroke="currentColor" stroke-width="1.5" />
            </svg>
            <svg v-else-if="item.icon === 'user'" width="16" height="16" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="8" r="3.4" stroke="currentColor" stroke-width="1.5" />
              <path d="M5 20c.6-3.4 3.3-5 7-5s6.4 1.6 7 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
            </svg>
            <svg v-else-if="item.icon === 'dept'" width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M12 3v5m0 5v5M4 13h3v3H4zM17 8h3v3h-3z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round" />
              <circle cx="12" cy="3" r="1" fill="currentColor" /><circle cx="12" cy="13" r="1" fill="currentColor" />
            </svg>
            <svg v-else-if="item.icon === 'role'" width="16" height="16" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="9" r="5.5" stroke="currentColor" stroke-width="1.5" />
              <path d="M12 14.5V21m-3-2h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
            </svg>
            <svg v-else-if="item.icon === 'perm'" width="16" height="16" viewBox="0 0 24 24" fill="none">
              <rect x="4" y="4" width="9" height="9" rx="1.5" stroke="currentColor" stroke-width="1.5" />
              <rect x="15" y="11" width="5" height="9" rx="1.5" stroke="currentColor" stroke-width="1.5" />
              <rect x="4" y="15" width="5" height="5" rx="1.5" stroke="currentColor" stroke-width="1.5" />
            </svg>
            <svg v-else-if="item.icon === 'post'" width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M6 6h12M6 12h12M6 18h7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
            </svg>
            <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="5" r="1.2" fill="currentColor" />
              <path d="M12 8v5l3 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
            </svg>
          </span>
          <span class="admin-nav__label">{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>

    <!-- 主区 -->
    <div class="admin-main">
      <header class="admin-topbar">
        <h1 class="admin-topbar__title">{{ currentLabel }}</h1>
        <div class="admin-user">
          <span class="admin-user__avatar" aria-hidden="true">
            {{ displayName.charAt(0).toUpperCase() }}
          </span>
          <span class="admin-user__name">{{ displayName }}</span>
          <button
            class="admin-user__logout"
            type="button"
            :disabled="loggingOut"
            @click="handleLogout"
          >
            {{ loggingOut ? '退出中…' : '返回登录' }}
          </button>
        </div>
      </header>

      <main class="admin-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-shell {
  display: flex;
  min-height: 100vh;
  background-color: var(--color-bg-base);
  color: var(--color-text-primary);
}

/* ---------- 侧边导航 ---------- */
.admin-side {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
  width: 232px;
  flex-shrink: 0;
  padding: var(--space-6) var(--space-4);
  background-color: var(--color-accent-mark); /* 书脊深棕 */
  border-right: 1px solid rgba(240, 232, 221, 0.12);
}
.admin-side__brand {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 0 var(--space-2);
}
.admin-side__logo {
  color: var(--color-text-on-accent);
}
.admin-side__name {
  font-family: var(--font-display);
  color: var(--color-text-on-accent);
  font-size: var(--text-h2);
  font-weight: 600;
}
.admin-nav {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}
.admin-nav__item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  height: 40px;
  padding: 0 var(--space-3);
  border-radius: var(--radius-sm);
  color: rgba(240, 232, 221, 0.68);
  text-decoration: none;
  transition:
    color var(--dur-fast) var(--ease-out),
    background-color var(--dur-fast) var(--ease-out);
}
.admin-nav__item:hover {
  color: var(--color-text-on-accent);
  background-color: rgba(240, 232, 221, 0.1);
  text-decoration: none;
}
.admin-nav__item.is-active {
  color: var(--color-accent-mark);
  background-color: var(--color-text-on-accent);
  box-shadow: var(--shadow-card);
}
.admin-nav__icon {
  display: inline-flex;
  flex-shrink: 0;
}
.admin-nav__label {
  font-size: var(--text-body);
}

/* ---------- 主区 ---------- */
.admin-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.admin-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  height: 64px;
  padding: 0 var(--space-8);
  background-color: var(--color-bg-base);
  border-bottom: 1px solid var(--color-border-default);
  flex-shrink: 0;
}
.admin-topbar__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: var(--text-title);
  font-weight: 600;
  color: var(--color-text-secondary);
}
.admin-user {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}
.admin-user__avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background-color: var(--color-surface-raised);
  border: 1px solid var(--color-border-strong);
  color: var(--color-accent-mark);
  font-size: var(--text-caption);
  font-weight: 600;
}
.admin-user__name {
  font-size: var(--text-body);
  color: var(--color-text-secondary);
}
.admin-user__logout {
  height: 32px;
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background-color: transparent;
  color: var(--color-text-muted);
  font-family: var(--font-sans);
  font-size: var(--text-caption);
  cursor: pointer;
  transition:
    color var(--dur-fast) var(--ease-out),
    background-color var(--dur-fast) var(--ease-out);
}
.admin-user__logout:hover:not(:disabled) {
  color: var(--color-accent-mark);
  background-color: var(--color-surface-raised);
}
.admin-user__logout:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.admin-content {
  flex: 1;
  padding: var(--space-6) var(--space-8) var(--space-8);
  overflow-x: hidden;
}

@media (max-width: 768px) {
  .admin-side {
    width: 72px;
    padding: var(--space-4) var(--space-2);
  }
  .admin-side__name {
    display: none;
  }
  .admin-nav__label {
    display: none;
  }
  .admin-content {
    padding: var(--space-4);
  }
  .admin-topbar {
    padding: 0 var(--space-4);
  }
}
</style>