<script setup lang="ts">
/**
 * BaseSelect —— 与 BaseInput 同视觉语言的选择框
 * 提供 label + select，覆盖 hover/focus-visible/disabled 三态。
 */

interface Option {
  value: number | string
  label: string
}

defineProps<{
  id: string
  label?: string
  modelValue: number | string | undefined
  placeholder?: string
  options: Option[]
  disabled?: boolean
}>()

defineEmits<{
  (e: 'update:modelValue', value: number | string | undefined): void
}>()
</script>

<template>
  <div class="base-select">
    <label v-if="label" :for="id" class="base-select__label">{{ label }}</label>
    <select
      :id="id"
      class="base-select__field"
      :value="options.some((o) => String(o.value) === String(modelValue)) ? modelValue : ''"
      :disabled="disabled"
      @change="
        $emit(
          'update:modelValue',
          ($event.target as HTMLSelectElement).value === ''
            ? undefined
            : Number.isNaN(Number(($event.target as HTMLSelectElement).value))
              ? ($event.target as HTMLSelectElement).value
              : Number(($event.target as HTMLSelectElement).value),
        )
      "
    >
      <option value="" class="base-select__placeholder-option">
        {{ placeholder ?? '请选择' }}
      </option>
      <option
        v-for="opt in options"
        :key="opt.value"
        :value="opt.value"
        class="base-select__option"
      >
        {{ opt.label }}
      </option>
    </select>
  </div>
</template>

<style scoped>
.base-select {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.base-select__label {
  font-family: var(--font-mono);
  font-size: var(--text-micro);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-text-secondary);
  font-weight: 500;
}
.base-select__field {
  width: 100%;
  height: 40px;
  padding: 0 var(--space-3);
  color: var(--color-text-secondary);
  font-size: var(--text-body);
  font-family: var(--font-sans);
  background-color: var(--color-surface-raised);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-xs);
  transition:
    border-color var(--dur-fast) var(--ease-out),
    box-shadow var(--dur-fast) var(--ease-out);
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none'%3E%3Cpath d='M6 9l6 6 6-6' stroke='%2385B3CB' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right var(--space-3) center;
  padding-right: var(--space-8);
  cursor: pointer;
}
.base-select__field::placeholder {
  color: var(--color-text-placeholder-alt);
}
.base-select__field:hover:not(:disabled) {
  border-color: var(--color-border-strong);
}
.base-select__field:focus-visible {
  outline: none;
  border-width: 1.5px;
  border-color: var(--color-border-default);
  box-shadow: var(--glow-input-focus);
}
.base-select__field:disabled {
  color: var(--color-text-disabled);
  background-color: var(--color-surface-card);
  border-color: var(--color-border-disabled);
  cursor: not-allowed;
}
.base-select__placeholder-option,
.base-select__option {
  background-color: var(--color-surface-card);
  color: var(--color-text-secondary);
}
</style>