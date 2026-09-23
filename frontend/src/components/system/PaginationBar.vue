<script setup lang="ts">
/**
 * PaginationBar —— 统一分页条（首/前/后/末 + 页码信息）。
 */
const props = defineProps<{
  pageNum: number
  pageSize: number
  total: number
}>()

const emit = defineEmits<{ (e: 'change', pageNum: number): void }>()

const totalPages = Math.max(Math.ceil(props.total / props.pageSize), 1)

function toPage(page: number) {
  if (page < 1 || page > totalPages || page === props.pageNum) return
  emit('change', page)
}
</script>

<template>
  <div class="pagination">
    <p class="pagination__info">
      共 <strong class="pagination__num">{{ total }}</strong> 条
      <span class="pagination__sep">·</span>
      第 <strong class="pagination__num">{{ pageNum }}</strong> /
      <strong class="pagination__num">{{ totalPages }}</strong> 页
    </p>
    <nav class="pagination__nav" aria-label="分页导航">
      <button
        class="pagination__btn"
        type="button"
        :disabled="pageNum <= 1"
        aria-label="上一页"
        @click="toPage(pageNum - 1)"
      >
        上一页
      </button>
      <button
        class="pagination__btn"
        type="button"
        :disabled="pageNum >= totalPages"
        aria-label="下一页"
        @click="toPage(pageNum + 1)"
      >
        下一页
      </button>
    </nav>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4) 0;
  flex-wrap: wrap;
}
.pagination__info {
  margin: 0;
  font-size: var(--text-caption);
  color: var(--color-text-muted);
  font-family: var(--font-mono);
}
.pagination__num {
  color: var(--color-text-secondary);
  font-weight: 600;
}
.pagination__sep {
  margin: 0 var(--space-1);
  color: var(--color-text-disabled);
}
.pagination__nav {
  display: flex;
  gap: var(--space-2);
}
.pagination__btn {
  height: 32px;
  padding: 0 var(--space-4);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background-color: var(--color-surface-raised);
  color: var(--color-text-primary);
  font-family: var(--font-sans);
  font-size: var(--text-caption);
  cursor: pointer;
  transition:
    background-color var(--dur-fast) var(--ease-out),
    border-color var(--dur-fast) var(--ease-out),
    color var(--dur-fast) var(--ease-out);
}
.pagination__btn:hover:not(:disabled) {
  background-color: var(--color-surface-hover);
  border-color: var(--color-border-default);
}
.pagination__btn:disabled {
  cursor: not-allowed;
  color: var(--color-text-disabled);
  background-color: var(--color-surface-card);
  border-color: var(--color-border-disabled);
}
</style>