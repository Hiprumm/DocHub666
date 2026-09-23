<script setup lang="ts">
/**
 * PermissionView —— 权限管理（树形表格 + 新增/编辑/删除）。
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { permissionApi } from '@/api/system'
import type { PermForm, SysPermission } from '@/types/system'
import BaseButton from '@/components/BaseButton.vue'
import BaseInput from '@/components/BaseInput.vue'
import BaseSelect from '@/components/system/BaseSelect.vue'
import StatusBadge from '@/components/system/StatusBadge.vue'
import ModalDrawer from '@/components/system/ModalDrawer.vue'
import ConfirmDialog from '@/components/system/ConfirmDialog.vue'

const list = ref<SysPermission[]>([])
const loading = ref(true)
const error = ref('')

const formOpen = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editingId = ref('')
const form = reactive<PermForm>({
  permName: '',
  permKey: '',
  permType: 1,
  parentId: undefined,
  path: '',
  method: '',
  sort: 0,
  status: 0,
})

const deleteOpen = ref(false)
const deleting = ref(false)
const deleteTarget = ref<SysPermission | null>(null)

async function load() {
  loading.value = true
  error.value = ''
  try {
    list.value = await permissionApi.tree()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载权限失败'
  } finally {
    loading.value = false
  }
}

const parentOptions = computed(() =>
  list.value
    .filter((p) => p.id !== editingId.value)
    .map((p) => ({ value: p.id, label: p.permName })),
)

const typeOptions = [
  { value: 1, label: '目录' },
  { value: 2, label: '菜单' },
  { value: 3, label: '按钮' },
]

const statusOptions = [
  { value: 0, label: '启用' },
  { value: 1, label: '停用' },
]

function depthOf(p: SysPermission): number {
  if (!p.ancestors) return 0
  const chain = p.ancestors.split(',').filter((s) => s)
  return Math.max(chain.length - 1, 0)
}

function openCreate(parent?: string) {
  isEdit.value = false
  editingId.value = ''
  Object.assign(form, { permName: '', permKey: '', permType: 1, parentId: undefined, path: '', method: '', sort: 0, status: 0 })
  if (parent !== undefined) form.parentId = parent
  formOpen.value = true
}

function openEdit(perm: SysPermission) {
  isEdit.value = true
  editingId.value = perm.id
  Object.assign(form, {
    id: perm.id,
    parentId: perm.parentId || undefined,
    permName: perm.permName,
    permKey: perm.permKey,
    permType: perm.permType ?? 1,
    path: perm.path ?? '',
    method: perm.method ?? '',
    sort: perm.sort ?? 0,
    status: perm.status ?? 0,
    version: Number(perm.version ?? 0),
  })
  formOpen.value = true
}

async function confirmSave() {
  if (!form.permName.trim() || !form.permKey.trim()) return
  submitting.value = true
  try {
    const payload: PermForm = {
      id: isEdit.value ? form.id : undefined,
      parentId: form.parentId !== undefined && form.parentId !== '' ? Number(form.parentId) : 0,
      permName: form.permName.trim(),
      permKey: form.permKey.trim(),
      permType: form.permType ?? 1,
      path: form.path,
      method: form.method,
      sort: form.sort ?? 0,
      status: form.status ?? 0,
      version: form.version,
    }
    if (isEdit.value) await permissionApi.update(payload)
    else await permissionApi.create(payload)
    formOpen.value = false
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  } finally {
    submitting.value = false
  }
}

function askDelete(perm: SysPermission) {
  deleteTarget.value = perm
  deleteOpen.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await permissionApi.remove(Number(deleteTarget.value.id))
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
        <h2 class="page__heading">权限管理</h2>
        <p class="page__desc">维护权限点树，供角色配置访问边界。</p>
      </div>
      <BaseButton variant="primary" @click="openCreate()">新增权限</BaseButton>
    </div>

    <p v-if="error" class="page__error" role="alert">{{ error }}</p>

    <div v-if="loading" class="page__state">
      <span class="page__spinner" aria-hidden="true" /> 加载中…
    </div>
    <div v-else-if="list.length === 0" class="page__state">
      暂无权限数据，点击右上角「新增权限」创建。
    </div>

    <div v-else class="card">
      <table class="table">
        <thead>
          <tr>
            <th>权限名称</th>
            <th>权限标识</th>
            <th class="table__th--sm">类型</th>
            <th>路径 / 方法</th>
            <th class="table__th--sm">状态</th>
            <th class="table__th--actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="perm in list" :key="perm.id" class="table__row">
            <td>
              <span class="table__tree" :style="{ paddingLeft: `${depthOf(perm) * 20 + 8}px` }">
                <span v-if="depthOf(perm) > 0" class="table__indent" aria-hidden="true">└</span>
                {{ perm.permName }}
              </span>
            </td>
            <td><span class="mono">{{ perm.permKey }}</span></td>
            <td>{{ typeOptions.find((t) => t.value === perm.permType)?.label ?? perm.permType }}</td>
            <td>
              <span class="mono">{{ perm.path || '—' }}<template v-if="perm.method"> · {{ perm.method }}</template></span>
            </td>
            <td><StatusBadge :status="perm.status" /></td>
            <td>
              <div class="table__actions">
                <BaseButton size="sm" variant="text" @click="openCreate(perm.id)">新增子级</BaseButton>
                <BaseButton size="sm" variant="text" @click="openEdit(perm)">编辑</BaseButton>
                <BaseButton size="sm" variant="text" @click="askDelete(perm)">删除</BaseButton>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <ModalDrawer
      v-model:open="formOpen"
      :title="isEdit ? '编辑权限' : '新增权限'"
      :loading="submitting"
      @confirm="confirmSave"
    >
      <template #body>
        <div class="form">
          <BaseInput id="perm-name" v-model="form.permName" label="权限名称" placeholder="请输入权限名称" />
          <BaseInput id="perm-key" v-model="form.permKey" label="权限标识" placeholder="如：system:user:query" />
          <BaseSelect
            id="perm-parent"
            v-model="form.parentId"
            label="上级权限"
            :options="parentOptions"
            placeholder="顶级权限"
          />
          <BaseSelect id="perm-type" v-model="form.permType" label="权限类型" :options="typeOptions" />
          <BaseInput
            id="perm-path"
            label="路径"
            type="text"
            placeholder="如：/sys/user/list"
            :model-value="form.path ?? ''"
            @update:model-value="form.path = $event"
          />
          <BaseInput
            id="perm-method"
            label="HTTP 方法"
            type="text"
            placeholder="如：GET"
            :model-value="form.method ?? ''"
            @update:model-value="form.method = $event"
          />
          <BaseInput
            id="perm-sort"
            label="显示顺序"
            type="text"
            placeholder="0"
            :model-value="String(form.sort ?? '')"
            @update:model-value="form.sort = Number($event) || 0"
          />
          <BaseSelect id="perm-status" v-model="form.status" label="状态" :options="statusOptions" />
        </div>
      </template>
    </ModalDrawer>

    <ConfirmDialog
      v-model:open="deleteOpen"
      title="删除权限"
      :message="`确定删除权限「${deleteTarget?.permName ?? ''}」吗？该操作不可撤销。`"
      :loading="deleting"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
@import './_page.css';
.form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}
@media (max-width: 640px) {
  .form {
    grid-template-columns: 1fr;
  }
}
</style>