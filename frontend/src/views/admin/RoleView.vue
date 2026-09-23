<script setup lang="ts">
/**
 * RoleView —— 角色管理（表格 + 分页 + 关键词筛选 + 权限配置）。
 */
import { onMounted, ref } from 'vue'
import { permissionApi, relationApi, roleApi } from '@/api/system'
import type { RoleForm, SysPermission, SysRole } from '@/types/system'
import BaseButton from '@/components/BaseButton.vue'
import BaseInput from '@/components/BaseInput.vue'
import BaseSelect from '@/components/system/BaseSelect.vue'
import StatusBadge from '@/components/system/StatusBadge.vue'
import ModalDrawer from '@/components/system/ModalDrawer.vue'
import ConfirmDialog from '@/components/system/ConfirmDialog.vue'
import PaginationBar from '@/components/system/PaginationBar.vue'

const list = ref<SysRole[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(true)
const error = ref('')
const keyword = ref('')

const formOpen = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const form = ref<RoleForm>({
  roleName: '',
  roleKey: '',
  sort: 0,
  status: 0,
  remark: '',
})

const deleteOpen = ref(false)
const deleting = ref(false)
const deleteTarget = ref<SysRole | null>(null)

/* ---------- 权限配置 ---------- */
const permOpen = ref(false)
const permLoading = ref(false)
const permSaving = ref(false)
const permRole = ref<SysRole | null>(null)
const permTree = ref<SysPermission[]>([])
const selectedPerms = ref<Set<string>>(new Set())

const statusOptions = [
  { value: 0, label: '启用' },
  { value: 1, label: '停用' },
]

async function load(page = pageNum.value) {
  pageNum.value = page
  loading.value = true
  error.value = ''
  try {
    const res = await roleApi.list(page, pageSize.value, keyword.value.trim() || undefined)
    list.value = res.records
    total.value = res.total
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载角色失败'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  form.value = { roleName: '', roleKey: '', sort: 0, status: 0, remark: '' }
  formOpen.value = true
}

function openEdit(role: SysRole) {
  isEdit.value = true
  form.value = {
    id: role.id,
    roleName: role.roleName,
    roleKey: role.roleKey,
    sort: role.sort ?? 0,
    status: role.status ?? 0,
    remark: role.remark ?? '',
    version: Number(role.version ?? 0),
  }
  formOpen.value = true
}

async function confirmSave() {
  if (!form.value.roleName.trim() || !form.value.roleKey.trim()) return
  submitting.value = true
  try {
    const payload: RoleForm = {
      id: isEdit.value ? form.value.id : undefined,
      roleName: form.value.roleName.trim(),
      roleKey: form.value.roleKey.trim(),
      sort: form.value.sort ?? 0,
      status: form.value.status ?? 0,
      remark: form.value.remark,
      version: form.value.version,
    }
    if (isEdit.value) await roleApi.update(payload)
    else await roleApi.create(payload)
    formOpen.value = false
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  } finally {
    submitting.value = false
  }
}

function askDelete(role: SysRole) {
  deleteTarget.value = role
  deleteOpen.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await roleApi.remove(Number(deleteTarget.value.id))
    deleteOpen.value = false
    deleteTarget.value = null
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败'
  } finally {
    deleting.value = false
  }
}

function depthOf(p: SysPermission): number {
  if (!p.ancestors) return 0
  const chain = p.ancestors.split(',').filter((s) => s)
  return Math.max(chain.length - 1, 0)
}

async function openPermAssign(role: SysRole) {
  permRole.value = role
  permOpen.value = true
  permLoading.value = true
  permError.value = ''
  try {
    const [tree, checked] = await Promise.all([
      permissionApi.tree(),
      relationApi.listRolePermissions(Number(role.id)),
    ])
    permTree.value = tree
    selectedPerms.value = new Set(checked.map((id) => String(id)))
  } catch (e) {
    permError.value = e instanceof Error ? e.message : '加载权限失败'
  } finally {
    permLoading.value = false
  }
}

function togglePerm(id: string) {
  const next = new Set(selectedPerms.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  selectedPerms.value = next
}

async function savePerms() {
  if (!permRole.value) return
  permSaving.value = true
  try {
    await relationApi.assignRolePermissions(
      Number(permRole.value.id),
      [...selectedPerms.value].map((id) => Number(id)),
    )
    permOpen.value = false
  } catch (e) {
    permError.value = e instanceof Error ? e.message : '保存权限失败'
  } finally {
    permSaving.value = false
  }
}

const permError = ref('')

onMounted(() => load(1))
</script>

<template>
  <div class="page">
    <div class="page__bar">
      <div class="page__title">
        <h2 class="page__heading">角色管理</h2>
        <p class="page__desc">角色定义与权限分配，控制用户的系统访问边界。</p>
      </div>
      <BaseButton variant="primary" @click="openCreate">新增角色</BaseButton>
    </div>

    <div class="filters">
      <div class="filters__item">
        <label class="filters__label" for="role-keyword">关键词</label>
        <input
          id="role-keyword"
          v-model="keyword"
          class="filters__input"
          type="text"
          placeholder="角色名称 / 标识"
          @keyup.enter="load(1)"
        >
      </div>
      <BaseButton variant="secondary" @click="load(1)">查询</BaseButton>
      <BaseButton variant="text" @click="keyword = ''; load(1)">重置</BaseButton>
    </div>

    <p v-if="error" class="page__error" role="alert">{{ error }}</p>

    <div v-if="loading" class="page__state">
      <span class="page__spinner" aria-hidden="true" /> 加载中…
    </div>
    <div v-else-if="list.length === 0" class="page__state">暂无角色数据。</div>

    <div v-else class="card">
      <table class="table">
        <thead>
          <tr>
            <th>角色名称</th>
            <th>角色标识</th>
            <th class="table__th--sm">排序</th>
            <th class="table__th--sm">状态</th>
            <th>备注</th>
            <th class="table__th--actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="role in list" :key="role.id" class="table__row">
            <td>{{ role.roleName }}</td>
            <td><span class="mono">{{ role.roleKey }}</span></td>
            <td>{{ role.sort }}</td>
            <td><StatusBadge :status="role.status" /></td>
            <td>{{ role.remark || '—' }}</td>
            <td>
              <div class="table__actions">
                <BaseButton size="sm" variant="text" @click="openPermAssign(role)">配置权限</BaseButton>
                <BaseButton size="sm" variant="text" @click="openEdit(role)">编辑</BaseButton>
                <BaseButton size="sm" variant="text" @click="askDelete(role)">删除</BaseButton>
              </div>
            </td>
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

    <ModalDrawer
      v-model:open="formOpen"
      :title="isEdit ? '编辑角色' : '新增角色'"
      :loading="submitting"
      @confirm="confirmSave"
    >
      <template #body>
        <div class="form">
          <BaseInput id="role-name" v-model="form.roleName" label="角色名称" placeholder="如：系统管理员" />
          <BaseInput id="role-key" v-model="form.roleKey" label="角色标识" placeholder="如：ROLE_ADMIN" />
          <BaseInput
            id="role-sort"
            label="显示顺序"
            type="text"
            placeholder="0"
            :model-value="String(form.sort ?? '')"
            @update:model-value="form.sort = Number($event) || 0"
          />
          <BaseSelect id="role-status" v-model="form.status" label="状态" :options="statusOptions" />
          <div class="form__field">
            <label class="form__label" for="role-remark">备注</label>
            <textarea id="role-remark" v-model="form.remark" class="form__textarea" placeholder="可选备注" />
          </div>
        </div>
      </template>
    </ModalDrawer>

    <ModalDrawer
      v-model:open="permOpen"
      :title="`配置权限 · ${permRole?.roleName ?? ''}`"
      :ok-text="'保存权限'"
      :loading="permSaving"
      @confirm="savePerms"
    >
      <template #body>
        <p v-if="permError" class="page__error" role="alert">{{ permError }}</p>
        <div v-if="permLoading" class="page__state">
          <span class="page__spinner" aria-hidden="true" /> 加载权限中…
        </div>
        <div v-else class="perm-list">
          <label
            v-for="perm in permTree"
            :key="perm.id"
            class="perm-list__item"
            :style="{ paddingLeft: `${depthOf(perm) * 20 + 12}px` }"
          >
            <input
              class="perm-list__box"
              type="checkbox"
              :checked="selectedPerms.has(perm.id)"
              @change="togglePerm(perm.id)"
            >
            <span class="perm-list__label">
              {{ perm.permName }}
              <span class="mono">· {{ perm.permKey }}</span>
            </span>
          </label>
        </div>
      </template>
    </ModalDrawer>

    <ConfirmDialog
      v-model:open="deleteOpen"
      title="删除角色"
      :message="`确定删除角色「${deleteTarget?.roleName ?? ''}」吗？该操作不可撤销。`"
      :loading="deleting"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
@import './_page.css';
.perm-list {
  display: flex;
  flex-direction: column;
  max-height: 52vh;
  overflow-y: auto;
}
.perm-list__item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: background-color var(--dur-fast) var(--ease-out);
}
.perm-list__item:hover {
  background-color: var(--color-surface-raised);
}
.perm-list__box {
  appearance: none;
  width: 15px;
  height: 15px;
  margin: 0;
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-xs);
  background-color: transparent;
  cursor: pointer;
  position: relative;
  flex-shrink: 0;
  transition: background-color var(--dur-fast) var(--ease-out);
}
.perm-list__box:checked {
  background-color: var(--color-accent-mark);
  border-color: var(--color-accent-mark);
}
.perm-list__box:checked::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 1px;
  width: 4px;
  height: 8px;
  border: solid var(--color-bg-base);
  border-width: 0 1.6px 1.6px 0;
  transform: rotate(45deg);
}
.perm-list__label {
  font-size: var(--text-body);
}
</style>