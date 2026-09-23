<script setup lang="ts">
/**
 * ModalDrawer —— 通用弹窗容器（表单抽屉），承载 title + body 插槽 + footer。
 * 支持 Esc / 遮罩点击关闭、打开时焦点落在标题、prefers-reduced-motion 降级。
 */
import { onMounted, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    open: boolean
    title: string
    okText?: string
    cancelText?: string
    loading?: boolean
    width?: number
  }>(),
  {
    okText: '保存',
    cancelText: '取消',
    loading: false,
    width: 520,
  },
)

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

const titleRef = ref<HTMLElement | null>(null)

watch(
  () => props.open,
  (open) => {
    if (open) {
      requestAnimationFrame(() => titleRef.value?.focus())
    }
  },
)

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') close()
}

function close() {
  emit('update:open', false)
  emit('cancel')
}

function onMaskClick() {
  close()
}

onMounted(() => {
  document.addEventListener('keydown', onKeydown)
})
</script>

<template>
  <Teleport to="body">
    <Transition name="sys-modal">
      <div
        v-if="open"
        class="modal-overlay"
        @click.self="onMaskClick"
      >
        <div
          class="modal-panel"
          role="dialog"
          aria-modal="true"
          :aria-label="title"
          :style="{ width: `${width}px` }"
        >
          <header class="modal-panel__head">
            <h2 ref="titleRef" class="modal-panel__title" tabindex="-1">{{ title }}</h2>
            <button
              class="modal-panel__close"
              type="button"
              aria-label="关闭"
              @click="close"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
              </svg>
            </button>
          </header>

          <div class="modal-panel__body">
            <slot name="body" />
          </div>

          <footer class="modal-panel__foot">
            <button
              class="modal-panel__btn modal-panel__btn--cancel"
              type="button"
              @click="close"
            >
              {{ cancelText }}
            </button>
            <button
              class="modal-panel__btn modal-panel__btn--ok"
              type="button"
              :disabled="loading"
              @click="$emit('confirm')"
            >
              <span v-if="loading" class="modal-panel__spinner" aria-hidden="true" />
              {{ loading ? '保存中…' : okText }}
            </button>
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
  background-color: var(--color-surface-overlay);
}
.modal-panel {
  max-width: 100%;
  max-height: 86vh;
  display: flex;
  flex-direction: column;
  background-color: var(--color-surface-card);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-modal);
  overflow: hidden;
}
.modal-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--color-border-default);
}
.modal-panel__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: var(--text-h2);
  font-weight: 600;
  color: var(--color-text-primary);
}
.modal-panel__title:focus {
  outline: none;
}
.modal-panel__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: var(--radius-xs);
  background: transparent;
  color: var(--color-text-muted);
  cursor: pointer;
  transition:
    color var(--dur-fast) var(--ease-out),
    background-color var(--dur-fast) var(--ease-out);
}
.modal-panel__close:hover {
  color: var(--color-accent-mark);
  background-color: var(--color-surface-raised);
}
.modal-panel__body {
  padding: var(--space-6);
  overflow-y: auto;
}
.modal-panel__foot {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--color-border-default);
}
.modal-panel__btn {
  height: 36px;
  padding: 0 var(--space-4);
  border-radius: var(--radius-sm);
  font-family: var(--font-sans);
  font-size: var(--text-body);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  transition:
    background-color var(--dur-fast) var(--ease-out),
    border-color var(--dur-fast) var(--ease-out),
    color var(--dur-fast) var(--ease-out);
}
.modal-panel__btn--cancel {
  background-color: var(--color-surface-raised);
  border: 1px solid var(--color-border-strong);
  color: var(--color-text-primary);
}
.modal-panel__btn--cancel:hover {
  background-color: var(--color-surface-hover);
}
.modal-panel__btn--ok {
  background-color: var(--color-btn-primary-bg);
  border: 1px solid transparent;
  color: var(--color-btn-primary-fg);
}
.modal-panel__btn--ok:hover:not(:disabled) {
  background-color: var(--color-btn-primary-bg-hover);
}
.modal-panel__btn--ok:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}
.modal-panel__spinner {
  width: 14px;
  height: 14px;
  border: 1.5px solid var(--color-btn-primary-fg);
  border-top-color: transparent;
  border-radius: 50%;
  animation: sys-spin 0.8s var(--ease-in-out) infinite;
}
@keyframes sys-spin {
  to {
    transform: rotate(360deg);
  }
}

@media (prefers-reduced-motion: no-preference) {
  .sys-modal-enter-active,
  .sys-modal-leave-active {
    transition: opacity var(--dur-base) var(--ease-out);
  }
  .sys-modal-enter-active .modal-panel,
  .sys-modal-leave-active .modal-panel {
    transition: transform var(--dur-base) var(--ease-out);
  }
  .sys-modal-enter-from,
  .sys-modal-leave-to {
    opacity: 0;
  }
  .sys-modal-enter-from .modal-panel,
  .sys-modal-leave-to .modal-panel {
    transform: translateY(12px);
  }
}
</style>