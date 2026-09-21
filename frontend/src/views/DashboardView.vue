<script setup lang="ts">
/**
 * DashboardView —— 占位页面
 * 登录成功后跳转目标。视觉遵循设计系统 §4.3 空状态规范。
 */
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <main class="dashboard-placeholder">
    <section class="empty-state" aria-labelledby="empty-title">
      <div class="empty-state__icon" aria-hidden="true">
        <svg width="48" height="48" viewBox="0 0 32 32" fill="none">
          <rect width="32" height="32" rx="8" fill="var(--color-surface-card)" stroke="var(--color-border-default)" />
          <path d="M9 8h9l5 5v11H9z" stroke="var(--color-text-tertiary)" stroke-width="2" stroke-linejoin="round" />
          <path d="M18 8v5h5" stroke="var(--color-text-tertiary)" stroke-width="2" stroke-linejoin="round" />
        </svg>
      </div>
      <h1 id="empty-title" class="empty-state__title">欢迎使用 DocHub</h1>
      <p class="empty-state__desc">登录成功。此处为仪表盘占位页，后续将承载文档管理功能。</p>
      <button class="empty-state__action" @click="void router.push('/')">返回登录</button>
    </section>
  </main>
</template>

<style scoped>
.dashboard-placeholder {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-bg-base);
  padding: var(--space-6);
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-3);
  max-width: 400px;
}
.empty-state__title {
  margin: 0;
  font-size: var(--text-h1);
  color: var(--color-text-primary);
}
.empty-state__desc {
  margin: 0;
  font-size: var(--text-body);
  color: var(--color-text-tertiary);
}
.empty-state__action {
  margin-top: var(--space-2);
  height: 36px;
  padding: 0 var(--space-4);
  border: none;
  border-radius: var(--radius-sm);
  background-color: var(--color-accent);
  color: var(--color-text-on-accent);
  font-family: var(--font-sans);
  font-size: var(--text-body);
  cursor: pointer;
  transition:
    background-color var(--dur-fast) var(--ease-out),
    box-shadow var(--dur-fast) var(--ease-out);
}
.empty-state__action:hover {
  background-color: var(--color-accent-hover);
  box-shadow: var(--glow-accent);
}
</style>