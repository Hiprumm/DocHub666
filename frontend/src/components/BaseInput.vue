<script setup lang="ts">
/**
 * BaseInput —— 设计文档 §3.2 输入框规范
 * 覆盖 default/hover/focus-visible/disabled/error 五态，
 * 通过具名插槽注入“后缀图标”（如密码显示/隐藏切换）。
 */
defineProps<{
  id: string
  label?: string
  type?: 'text' | 'password' | 'email'
  placeholder?: string
  modelValue: string
  error?: string
  autocomplete?: string
  disabled?: boolean
}>()

defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()
</script>

<template>
  <div class="base-input" :class="{ 'has-error': !!error }">
    <label v-if="label" :for="id" class="base-input__label">{{ label }}</label>
    <div class="base-input__wrapper">
      <input
        :id="id"
        class="base-input__field"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :autocomplete="autocomplete"
        :disabled="disabled"
        :aria-invalid="!!error"
        :aria-describedby="error ? `${id}-error` : undefined"
        @input="
          $emit('update:modelValue', ($event.target as HTMLInputElement).value)
        "
      >
      <!-- 后缀具名插槽：用于密码显示/隐藏切换等 -->
      <span v-if="$slots.suffix" class="base-input__suffix">
        <slot name="suffix" />
      </span>
    </div>
    <!-- 错误提示：内联红字 + 箭头图标（双信号，满足色觉友好） -->
    <p
      v-if="error"
      :id="`${id}-error`"
      class="base-input__error"
      role="alert"
    >
      <span class="base-input__error-icon" aria-hidden="true">▲</span>
      {{ error }}
    </p>
  </div>
</template>

<style scoped>
.base-input {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.base-input__label {
  font-family: var(--font-mono);
  font-size: var(--text-micro);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-text-secondary); /* #AAD9F2 正文/标签 */
  font-weight: 500;
}
.base-input__wrapper {
  position: relative;
}
.base-input__field {
  width: 100%;
  height: 40px;
  padding: 0 var(--space-3);
  color: var(--color-text-secondary);
  font-size: var(--text-body);
  font-family: var(--font-sans);
  background-color: var(--color-surface-raised); /* #002A3D */
  border: 1px solid var(--color-border-default); /* #2E5A6F */
  border-radius: var(--radius-xs);
  transition:
    border-color var(--dur-fast) var(--ease-out),
    box-shadow var(--dur-fast) var(--ease-out);
}
.base-input__field::placeholder {
  color: var(--color-text-placeholder-alt); /* #85B3CB 占位 */
}
.base-input__field:hover:not(:disabled) {
  border-color: var(--color-border-strong);
}
/* 聚焦态：边框 1.5px #2E5A6F + 内阴影微光晕（150ms ease-out） */
.base-input__field:focus-visible {
  outline: none;
  border-width: 1.5px;
  border-color: var(--color-border-default);
  box-shadow: var(--glow-input-focus);
}
.base-input__field:disabled {
  color: var(--color-text-disabled);
  background-color: var(--color-surface-card);
  border-color: var(--color-border-disabled);
  cursor: not-allowed;
}
/* 错误态：红边框 + 红字提示 */
.base-input.has-error .base-input__field {
  border-color: var(--color-error);
  box-shadow: 0 0 0 3px var(--color-error-soft);
}
.base-input__error {
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-caption);
  color: var(--color-error);
}
.base-input__error-icon {
  font-size: 10px;
  line-height: 1;
}
/* 有后缀时给输入框预留右侧空间，避免文字被图标遮蔽 */
.base-input__wrapper:has(.base-input__suffix) .base-input__field {
  padding-right: 44px;
}
.base-input__suffix {
  position: absolute;
  right: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
}
</style>