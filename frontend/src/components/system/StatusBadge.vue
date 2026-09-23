<script setup lang="ts">
/**
 * StatusBadge —— 启用/停用状态徽标（双信号：语义色 + 文字）。
 * status: 0 启用 / 1 停用（对齐后端 status 枚举）。
 */
const props = defineProps<{
  status: number
}>()

const enabled = props.status === 0
</script>

<template>
  <span class="status-badge" :class="enabled ? 'is-enabled' : 'is-disabled'">
    <span class="status-badge__dot" aria-hidden="true" />
    {{ enabled ? '启用' : '停用' }}
  </span>
</template>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: 2px var(--space-2);
  border-radius: 999px;
  font-size: var(--text-micro);
  font-weight: 600;
  line-height: 1.4;
}
.status-badge__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
.status-badge.is-enabled {
  color: var(--color-success);
  background-color: var(--color-success-soft);
}
.status-badge.is-enabled .status-badge__dot {
  background-color: var(--color-success);
}
.status-badge.is-disabled {
  color: var(--color-warning);
  background-color: var(--color-warning-soft);
}
.status-badge.is-disabled .status-badge__dot {
  background-color: var(--color-warning);
}
</style>