<script setup lang="ts">
/**
 * ConfirmDialog —— 删除确认弹窗。危险操作使用 error 语义色强调。零常态动效。
 */
import { watch, ref } from 'vue'

const props = withDefaults(
  defineProps<{
    open: boolean
    title?: string
    message: string
    confirmText?: string
    cancelText?: string
    loading?: boolean
  }>(),
  {
    title: '确认操作',
    confirmText: '删除',
    cancelText: '取消',
    loading: false,
  },
)

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

const focusRef = ref<HTMLButtonElement | null>(null)

watch(
  () => props.open,
  (open) => {
    if (open) {
      requestAnimationFrame(() => focusRef.value?.focus())
    }
  },
)

function close() {
  emit('update:open', false)
  emit('cancel')
}
</script>

<template>
  <Teleport to="body">
    <Transition name="sys-modal">
      <div v-if="open" class="confirm-overlay" @click.self="close">
        <div
          class="confirm-panel"
          role="alertdialog"
          aria-modal="true"
          :aria-label="title"
        >
          <div class="confirm-panel__icon" aria-hidden="true">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
              <path
                d="M12 9v4m0 4h.01M10.3 4.1 2.7 17a2 2 0 0 0 1.7 3h15.2a2 2 0 0 0 1.7-3L13.7 4.1a2 2 0 0 0-3.4 0Z"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </div>
          <h2 class="confirm-panel__title" tabindex="-1">{{ title }}</h2>
          <p class="confirm-panel__message">{{ message }}</p>
          <div class="confirm-panel__actions">
            <button
              class="confirm-panel__btn confirm-panel__btn--cancel"
              type="button"
              @click="close"
            >
              {{ cancelText }}
            </button>
            <button
              ref="focusRef"
              class="confirm-panel__btn confirm-panel__btn--danger"
              type="button"
              :disabled="loading"
              @click="$emit('confirm')"
            >
              {{ loading ? '处理中…' : confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  z-index: 120;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
  background-color: var(--color-surface-overlay);
}
.confirm-panel {
  width: 400px;
  max-width: 100%;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: var(--space-3);
  padding: var(--space-6);
  background-color: var(--color-surface-card);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-modal);
}
.confirm-panel__icon {
  display: inline-flex;
  width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background-color: var(--color-error-soft);
  color: var(--color-error);
}
.confirm-panel__title {
  margin: 0;
  font-family: var(--font-display);
  font-size: var(--text-h2);
  font-weight: 600;
  color: var(--color-text-primary);
}
.confirm-panel__title:focus {
  outline: none;
}
.confirm-panel__message {
  margin: 0;
  font-size: var(--text-body);
  line-height: 1.6;
  color: var(--color-text-secondary);
}
.confirm-panel__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-2);
}
.confirm-panel__btn {
  height: 36px;
  padding: 0 var(--space-4);
  border-radius: var(--radius-sm);
  font-family: var(--font-sans);
  font-size: var(--text-body);
  cursor: pointer;
  transition:
    background-color var(--dur-fast) var(--ease-out),
    border-color var(--dur-fast) var(--ease-out),
    color var(--dur-fast) var(--ease-out);
}
.confirm-panel__btn--cancel {
  background-color: var(--color-surface-raised);
  border: 1px solid var(--color-border-strong);
  color: var(--color-text-primary);
}
.confirm-panel__btn--cancel:hover {
  background-color: var(--color-surface-hover);
}
.confirm-panel__btn--danger {
  background-color: var(--color-error-soft);
  border: 1px solid var(--color-error);
  color: var(--color-error);
}
.confirm-panel__btn--danger:hover:not(:disabled) {
  background-color: var(--color-error);
  color: var(--color-text-on-accent);
}
.confirm-panel__btn--danger:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

@media (prefers-reduced-motion: no-preference) {
  .sys-modal-enter-active,
  .sys-modal-leave-active {
    transition: opacity var(--dur-base) var(--ease-out);
  }
  .sys-modal-enter-active .confirm-panel,
  .sys-modal-leave-active .confirm-panel {
    transition: transform var(--dur-base) var(--ease-out);
  }
  .sys-modal-enter-from,
  .sys-modal-leave-to {
    opacity: 0;
  }
  .sys-modal-enter-from .confirm-panel,
  .sys-modal-leave-to .confirm-panel {
    transform: translateY(12px);
  }
}
</style>