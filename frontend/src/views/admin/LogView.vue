<script setup lang="ts">
/**
 * LogView —— 操作日志（表格 + 分页 + 关键词/操作人/结果筛选，只读）。
 */
import { onMounted, ref } from 'vue'
import { logApi } from '@/api/system'
import type { SysLog } from '@/types/system'
import BaseButton from '@/components/BaseButton.vue'
import PaginationBar from '@/components/system/PaginationBar.vue'

const list = ref<SysLog[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(true)
const error = ref('')

const actorName = ref('')
const action = ref('')
const result = ref<string | undefined>(undefined)

const resultOptions = [
  { value: 'SUCCESS', label: '成功' },
  { value: 'FAIL', label: '失败' },
  { value: 'DENIED', label: '拒绝' },
]

async function load(page = pageNum.value) {
  pageNum.value = page
  loading.value = true
  error.value = ''
  try {
    const res = await logApi.page({
      pageNum: page,
      pageSize: pageSize.value,
      actorName: actorName.value.trim() || undefined,
      action: action.value.trim() || undefined,
      result: result.value,
    })
    list.value = res.records
    total.value = res.total
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载日志失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => load(1))
</script>

<template>
  <div class="page">
    <div class="page__bar">
      <div class="page__title">
        <h2 class="page__heading">操作日志</h2>
        <p class="page__desc">系统操作审计留痕，只读查询。</p>
      </div>
    </div>

    <div class="filters">
      <div class="filters__item">
        <label class="filters__label" for="log-actor">操作人</label>
        <input
          id="log-actor"
          v-model="actorName"
          class="filters__input"
          type="text"
          placeholder="操作人账号"
          @keyup.enter="load(1)"
        >
      </div>
      <div class="filters__item">
        <label class="filters__label" for="log-action">操作类型</label>
        <input
          id="log-action"
          v-model="action"
          class="filters__input"
          type="text"
          placeholder="如：CREATE"
          @keyup.enter="load(1)"
        >
      </div>
      <div class="filters__item">
        <label class="filters__label" for="log-result">结果</label>
        <select id="log-result" v-model="result" class="form__select">
          <option value="">全部</option>
          <option v-for="r in resultOptions" :key="r.value" :value="r.value">{{ r.label }}</option>
        </select>
      </div>
      <BaseButton variant="secondary" @click="load(1)">查询</BaseButton>
      <BaseButton variant="text" @click="actorName = ''; action = ''; result = undefined; load(1)">重置</BaseButton>
    </div>

    <p v-if="error" class="page__error" role="alert">{{ error }}</p>

    <div v-if="loading" class="page__state">
      <span class="page__spinner" aria-hidden="true" /> 加载中…
    </div>
    <div v-else-if="list.length === 0" class="page__state">暂无日志数据。</div>

    <div v-else class="card">
      <table class="table">
        <thead>
          <tr>
            <th class="table__th--sm">操作人</th>
            <th>动作</th>
            <th>目标类型</th>
            <th>目标ID</th>
            <th class="table__th--sm">结果</th>
            <th>详情</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in list" :key="log.id" class="table__row">
            <td><span class="mono">{{ log.actorName || '—' }}</span></td>
            <td><span class="mono">{{ log.action }}</span></td>
            <td>{{ log.targetType || '—' }}</td>
            <td><span class="mono">{{ log.targetId || '—' }}</span></td>
            <td>
              <span class="log-result" :class="`is-${String(log.result).toLowerCase()}`">{{ log.result }}</span>
            </td>
            <td class="table__detail">{{ log.detail || '—' }}</td>
            <td><span class="mono">{{ log.createTime }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>

    <PaginationBar
      v-if="list.length > 0"
      :page-num="pageNum"
      :page-size="pageSize"
      :total="total"
      @change="load"
    />
  </div>
</template>

<style scoped>
@import './_page.css';
.table__detail {
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.log-result {
  display: inline-block;
  padding: 2px var(--space-2);
  border-radius: 999px;
  font-size: var(--text-micro);
  font-weight: 600;
  font-family: var(--font-mono);
}
.log-result.is-success {
  color: var(--color-success);
  background-color: var(--color-success-soft);
}
.log-result.is-fail,
.log-result.is-error {
  color: var(--color-error);
  background-color: var(--color-error-soft);
}
.log-result.is-denied {
  color: var(--color-warning);
  background-color: var(--color-warning-soft);
}
</style>