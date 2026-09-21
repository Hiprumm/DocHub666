<script setup lang="ts">
/**
 * BaseButton —— 设计文档 §3.1 按钮规范
 * 支持四种变体（primary/secondary/text/icon）与 default/hover/disabled/loading 四态。
 */
import { computed } from 'vue'

type Variant = 'primary' | 'secondary' | 'text' | 'icon'
type Size = 'sm' | 'md' | 'lg' | 'icon'

const props = withDefaults(
  defineProps<{
    variant?: Variant
    size?: Size
    type?: 'button' | 'submit'
    disabled?: boolean
    loading?: boolean
    ariaLabel?: string
  }>(),
  {
    variant: 'primary',
    size: 'md',
    type: 'button',
    disabled: false,
    loading: false,
    ariaLabel: undefined,
  },
)

const emit = defineEmits<{ (e: 'click', ev: MouseEvent): void }>()

// 加载态下视同禁用，避免重复提交
const isDisabled = computed(() => props.disabled || props.loading)

function handleClick(ev: MouseEvent) {
  if (isDisabled.value) return
  emit('click', ev)
}
</script>

<template>
  <button
    class="base-button"
    :class="[`is-${variant}`, `is-${size}`]"
    :type="type"
    :disabled="isDisabled"
    :aria-busy="loading"
    :aria-label="ariaLabel"
    @click="handleClick"
  >
    <span v-if="loading" class="base-button__spinner" aria-hidden="true" />
    <!-- 图标按钮隐藏默认占位，由插槽注入图标 -->
    <span v-if="variant !== 'icon'" class="base-button__content">
      <slot />
    </span>
    <span v-else class="base-button__icon">
      <slot />
    </span>
  </button>
</template>

<style scoped>
.base-button {
  /* 尺寸与内边距仅引用 Token */
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  font-family: var(--font-sans);
  font-size: var(--text-body);
  font-weight: 500;
  line-height: 1;
  cursor: pointer;
  padding: 0 var(--space-4);
  transition:
    background-color var(--dur-fast) var(--ease-out),
    border-color var(--dur-fast) var(--ease-out),
    color var(--dur-fast) var(--ease-out),
    transform var(--dur-fast) var(--ease-out);
}

.base-button.is-sm {
  height: 32px;
  padding: 0 var(--space-3);
}
.base-button.is-md {
  height: 36px;
}
.base-button.is-lg {
  height: 44px;
  padding: 0 var(--space-6);
}
.base-button.is-icon {
  width: 36px;
  height: 36px;
  padding: 0;
  border-radius: var(--radius-sm);
}
.base-button__content,
.base-button__icon {
  display: inline-flex;
  align-items: center;
}

/* ---------- 主按钮 Primary（登录页“去 AI 味”版式） ----------
 * 背景 #3F6B81 → hover #2E5A6F，文字 #D1FFFF（海洋调，低饱和）。
 * 值取自 11 色板 Token，未新增色值；hover 仅明度过渡 + 轻微上移，无放大/流光。
 */
.base-button.is-primary {
  background-color: var(--color-btn-primary-bg);
  color: var(--color-btn-primary-fg);
}
.base-button.is-primary:hover:not(:disabled) {
  background-color: var(--color-btn-primary-bg-hover);
  transform: translateY(-1px);
}
.base-button.is-primary:active:not(:disabled) {
  filter: brightness(1.08);
}

/* ---------- 次按钮 Secondary ---------- */
.base-button.is-secondary {
  background-color: var(--color-surface-raised);
  border-color: var(--color-border-strong);
  color: var(--color-text-primary);
}
.base-button.is-secondary:hover:not(:disabled) {
  background-color: var(--color-surface-hover);
}
.base-button.is-secondary:active:not(:disabled) {
  background-color: var(--color-surface-card);
}

/* ---------- 文字按钮 Text ---------- */
.base-button.is-text {
  background-color: transparent;
  color: var(--color-accent);
}
.base-button.is-text:hover:not(:disabled) {
  background-color: var(--color-accent-soft);
}
.base-button.is-text:active:not(:disabled) {
  /* active 语义同 hover，保持视觉连续 */
  background-color: var(--color-accent-soft);
}

/* ---------- 图标按钮 Icon ---------- */
.base-button.is-icon {
  background-color: transparent;
  color: var(--color-accent);
}
.base-button.is-icon:hover:not(:disabled) {
  background-color: var(--color-surface-hover);
}

/* ---------- 禁用态 disabled ---------- */
.base-button:disabled {
  cursor: not-allowed;
  background-color: var(--color-border-disabled);
  color: rgba(209, 255, 255, 0.4); /* 由 text-primary 40% 透明得来，避硬编码原色 */
}
.base-button.is-primary:disabled {
  /* fg(D1FFFF) 40% 透明，值仍来自 11 色板 */
  color: rgba(209, 255, 255, 0.4);
}
.base-button.is-text:disabled,
.base-button.is-icon:disabled {
  background-color: transparent;
  color: var(--color-text-disabled);
}
.base-button.is-secondary:disabled {
  background-color: var(--color-surface-card);
  border-color: var(--color-border-disabled);
}

/* ---------- 加载态 loading ---------- */
.base-button__spinner {
  width: 16px;
  height: 16px;
  border: 1.5px solid var(--color-btn-primary-fg);
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s var(--ease-in-out) infinite;
}
.base-button:disabled .base-button__spinner {
  border-color: var(--color-btn-primary-fg);
  border-top-color: transparent;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>