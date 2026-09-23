<script setup lang="ts">
/**
 * DashboardHome —— 仪表盘首页（系统管理模块概览卡片）。
 */
const cards = [
  { title: '用户管理', desc: '账号、组织关系与状态维护', to: '/dashboard/user' },
  { title: '部门管理', desc: '组织架构树形维护', to: '/dashboard/dept' },
  { title: '角色管理', desc: '角色定义与权限分配', to: '/dashboard/role' },
  { title: '权限管理', desc: '权限点树形维护', to: '/dashboard/permission' },
  { title: '岗位管理', desc: '岗位字典维护', to: '/dashboard/post' },
  { title: '操作日志', desc: '系统操作审计留痕', to: '/dashboard/log' },
]
</script>

<template>
  <div class="dash-home">
    <p class="dash-home__lead">欢迎使用 DocHub 系统管理。请从下方模块进入管理操作。</p>
    <div class="dash-home__grid">
      <RouterLink
        v-for="card in cards"
        :key="card.to"
        :to="card.to"
        class="dash-card"
      >
        <h2 class="dash-card__title">{{ card.title }}</h2>
        <p class="dash-card__desc">{{ card.desc }}</p>
        <span class="dash-card__arrow" aria-hidden="true">→</span>
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.dash-home {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  max-width: 960px;
}
.dash-home__lead {
  margin: 0;
  font-size: var(--text-body);
  color: var(--color-text-tertiary);
}
.dash-home__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: var(--space-4);
}
.dash-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-6);
  background-color: var(--color-surface-card);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  color: var(--color-text-secondary);
  text-decoration: none;
  transition:
    border-color var(--dur-fast) var(--ease-out),
    background-color var(--dur-fast) var(--ease-out),
    transform var(--dur-fast) var(--ease-out);
}
.dash-card:hover {
  border-color: var(--color-border-strong);
  background-color: var(--color-surface-raised);
  color: var(--color-text-primary);
  text-decoration: none;
  transform: translateY(-1px);
}
.dash-card__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: var(--text-h2);
  font-weight: 600;
  color: var(--color-accent-mark);
}
.dash-card__desc {
  margin: 0;
  font-size: var(--text-body);
  line-height: 1.6;
}
.dash-card__arrow {
  position: absolute;
  right: var(--space-4);
  top: var(--space-4);
  color: var(--color-text-muted);
  font-size: var(--text-h2);
}
</style>