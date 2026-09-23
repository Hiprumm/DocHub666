<script setup lang="ts">
/**
 * DeptView —— 部门管理（树形表格 + 新增/编辑/删除）
 * 后端 tree() 返回展平列表，按祖先链深度缩进展示层级。
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { deptApi } from '@/api/system'
import type { DeptForm, SysDept } from '@/types/system'
import BaseButton from '@/components/BaseButton.vue'
import BaseInput from '@/components/BaseInput.vue'
import BaseSelect from '@/components/system/BaseSelect.vue'
import StatusBadge from '@/components/system/StatusBadge.vue'
import ModalDrawer from '@/components/system/ModalDrawer.vue'
import ConfirmDialog from '@/components/system/ConfirmDialog.vue'

const list = ref<SysDept[]>([])
const loading = ref(true)
const error = ref('')

const formOpen = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editingId = ref('')
const form = reactive<DeptForm>({
  deptName: '',
  parentId: undefined,
  orderNum: 0,
  leaderUserId: '',
  status: 0,
})

const deleteOpen = ref(false)
const deleting = ref(false)
const deleteTarget = ref<SysDept | null>(null)

async function load() {
  loading.value = true
  error.value = ''
  try {
    list.value = await deptApi.tree()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载部门失败'
  } finally {
    loading.value = false
  }
}

const parentOptions = computed(() =>
  list.value
    .filter((d) => d.id !== editingId.value)
    .map((d) => ({ value: d.id, label: d.deptName })),
)

function depthOf(dept: SysDept): number {
  if (!dept.ancestors) return 0
  const chain = dept.ancestors.split(',').filter((s) => s)
  return Math.max(chain.length - 1, 0)
}

const statusOptions = [
  { value: 0, label: '启用' },
  { value: 1, label: '停用' },
]

function openCreate(parent?: string) {
  isEdit.value = false
  editingId.value = ''
  Object.assign(form, { deptName: '', parentId: undefined, orderNum: 0, leaderUserId: '', status: 0 })
  if (parent !== undefined) form.parentId = parent
  formOpen.value = true
}

function openEdit(dept: SysDept) {
  isEdit.value = true
  editingId.value = dept.id
  Object.assign(form, {
    id: dept.id,
    parentId: dept.parentId || undefined,
    deptName: dept.deptName,
    orderNum: dept.orderNum ?? 0,
    leaderUserId: dept.leaderUserId || undefined,
    status: dept.status ?? 0,
    version: Number(dept.version ?? 0),
  })
  formOpen.value = true
}

async function confirmSave() {
  if (!form.deptName.trim()) return
  submitting.value = true
  try {
    const payload: DeptForm = {
      id: isEdit.value ? form.id : undefined,
      parentId: form.parentId !== undefined && form.parentId !== '' ? Number(form.parentId) : 0,
      deptName: form.deptName.trim(),
      orderNum: form.orderNum ?? 0,
      leaderUserId: form.leaderUserId || undefined,
      status: form.status ?? 0,
      version: form.version,
    }
    if (isEdit.value) {
      await deptApi.update(payload)
    } else {
      await deptApi.create(payload)
    }
    formOpen.value = false
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  } finally {
    submitting.value = false
  }
}

function askDelete(dept: SysDept) {
  deleteTarget.value = dept
  deleteOpen.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await deptApi.remove(Number(deleteTarget.value.id))
    deleteOpen.value = false
    deleteTarget.value = null
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败'
  } finally {
    deleting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="page__bar">
      <div class="page__title">
        <h2 class="page__heading">部门管理</h2>
        <p class="page__desc">维护组织架构树，支持层级新增与顺序编排。</p>
      </div>
      <BaseButton variant="primary" @click="openCreate()">新增部门</BaseButton>
    </div>

    <p v-if="error" class="page__error" role="alert">{{ error }}</p>

    <div v-if="loading" class="page__state">
      <span class="page__spinner" aria-hidden="true" /> 加载中…
    </div>
    <div v-else-if="list.length === 0" class="page__state">
      暂无部门数据，点击右上角「新增部门」创建。
    </div>

    <div v-else class="card">
      <table class="table">
        <thead>
          <tr>
            <th>部门名称</th>
            <th class="table__th--sm">顺序</th>
            <th class="table__th--sm">负责人</th>
            <th class="table__th--sm">状态</th>
            <th class="table__th--actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="dept in list" :key="dept.id" class="table__row">
            <td>
              <span class="table__tree" :style="{ paddingLeft: `${depthOf(dept) * 20 + 8}px` }">
                <span v-if="depthOf(dept) > 0" class="table__indent" aria-hidden="true">└</span>
                {{ dept.deptName }}
              </span>
            </td>
            <td>{{ dept.orderNum }}</td>
            <td>{{ dept.leaderUserId || '—' }}</td>
            <td><StatusBadge :status="dept.status" /></td>
            <td>
              <div class="table__actions">
                <BaseButton size="sm" variant="text" @click="openCreate(dept.id)">新增子级</BaseButton>
                <BaseButton size="sm" variant="text" @click="openEdit(dept)">编辑</BaseButton>
                <BaseButton size="sm" variant="text" @click="askDelete(dept)">删除</BaseButton>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <ModalDrawer
      v-model:open="formOpen"
      :title="isEdit ? '编辑部门' : '新增部门'"
      :loading="submitting"
      @confirm="confirmSave"
    >
      <template #body>
        <div class="form">
          <BaseInput id="dept-name" v-model="form.deptName" label="部门名称" placeholder="请输入部门名称" />
          <BaseSelect
            id="dept-parent"
            v-model="form.parentId"
            label="上级部门"
            :options="parentOptions"
            placeholder="顶级部门"
          />
          <BaseInput
            id="dept-order"
            label="显示顺序"
            type="text"
            placeholder="0"
            :model-value="String(form.orderNum ?? '')"
            @update:model-value="form.orderNum = Number($event) || 0"
          />
          <BaseInput
            id="dept-leader"
            label="负责人ID"
            type="text"
            placeholder="负责人用户ID"
            :model-value="form.leaderUserId ?? ''"
            @update:model-value="form.leaderUserId = $event"
          />
          <BaseSelect id="dept-status" v-model="form.status" label="状态" :options="statusOptions" />
        </div>
      </template>
    </ModalDrawer>

    <ConfirmDialog
      v-model:open="deleteOpen"
      title="删除部门"
      :message="`确定删除部门「${deleteTarget?.deptName ?? ''}」吗？该操作不可撤销。`"
      :loading="deleting"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
@import './_page.css';
.form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
</style>