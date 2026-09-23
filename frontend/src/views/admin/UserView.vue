<script setup lang="ts">
/**
 * UserView —— 用户管理
 * 表格 + 分页 + 关键词/部门/岗位筛选；新增/编辑/删除；弹窗内分配部门/岗位/角色。
 */
import { onMounted, ref } from 'vue'
import { deptApi, postApi, relationApi, roleApi, userApi } from '@/api/system'
import type { SysDept, SysPost, SysRole, SysUser, UserCreateForm, UserUpdateForm } from '@/types/system'
import BaseButton from '@/components/BaseButton.vue'
import BaseInput from '@/components/BaseInput.vue'
import BaseSelect from '@/components/system/BaseSelect.vue'
import StatusBadge from '@/components/system/StatusBadge.vue'
import ModalDrawer from '@/components/system/ModalDrawer.vue'
import ConfirmDialog from '@/components/system/ConfirmDialog.vue'
import PaginationBar from '@/components/system/PaginationBar.vue'

const list = ref<SysUser[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(true)
const error = ref('')

const keyword = ref('')
const deptFilter = ref<string | undefined>()
const postFilter = ref<string | undefined>()

const deptOptions = ref<{ value: string; label: string }[]>([])
const postOptions = ref<{ value: string; label: string }[]>([])
const roleOptions = ref<{ value: string; label: string }[]>([])

const formOpen = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editingUser = ref<SysUser | null>(null)
const formUsername = ref('')
const formPassword = ref('')
const formRealName = ref('')
const formEmail = ref('')
const formPhone = ref('')
const formDeptId = ref<string | undefined>(undefined)
const formStatus = ref<number>(0)
const selectedDepts = ref<Set<string>>(new Set())
const selectedPosts = ref<Set<string>>(new Set())
const selectedRoles = ref<Set<string>>(new Set())

const deleteOpen = ref(false)
const deleting = ref(false)
const deleteTarget = ref<SysUser | null>(null)

const statusOptions = [
  { value: 0, label: '启用' },
  { value: 1, label: '停用' },
]

function toNumbers(set: Set<string>): number[] {
  return [...set].map((id) => Number(id))
}

async function loadMeta() {
  try {
    const [depts, roles, posts] = await Promise.all([
      deptApi.tree(),
      roleApi.list(1, 100),
      postApi.list(1, 100),
    ])
    deptOptions.value = depts.map((d: SysDept) => ({ value: d.id, label: d.deptName }))
    roleOptions.value = roles.records.map((r: SysRole) => ({ value: r.id, label: r.roleName }))
    postOptions.value = posts.records.map((p: SysPost) => ({ value: p.id, label: p.postName }))
  } catch {
    // 元数据加载失败不阻断主列表
  }
}

async function load(page = pageNum.value) {
  pageNum.value = page
  loading.value = true
  error.value = ''
  try {
    const res = await userApi.list({
      pageNum: page,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      deptId: deptFilter.value,
      postId: postFilter.value,
    })
    list.value = res.records
    total.value = res.total
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载用户失败'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  editingUser.value = null
  formUsername.value = ''
  formPassword.value = ''
  formRealName.value = ''
  formEmail.value = ''
  formPhone.value = ''
  formDeptId.value = undefined
  formStatus.value = 0
  selectedDepts.value = new Set()
  selectedPosts.value = new Set()
  selectedRoles.value = new Set()
  formOpen.value = true
}

function openEdit(user: SysUser) {
  isEdit.value = true
  editingUser.value = user
  formUsername.value = user.username
  formPassword.value = ''
  formRealName.value = user.realName ?? ''
  formEmail.value = user.email ?? ''
  formPhone.value = user.phone ?? ''
  formDeptId.value = user.deptId || undefined
  formStatus.value = user.status ?? 0
  selectedDepts.value = new Set(user.deptIds ?? [])
  selectedPosts.value = new Set(user.postIds ?? [])
  selectedRoles.value = new Set(user.roleIds ?? [])
  formOpen.value = true
}

async function confirmSave() {
  if (!formUsername.value.trim()) return
  if (!isEdit.value && formPassword.value.length < 8) return
  submitting.value = true
  try {
    if (isEdit.value && editingUser.value) {
      const base: UserUpdateForm = {
        id: editingUser.value.id,
        version: Number(editingUser.value.version ?? 0),
        realName: formRealName.value || undefined,
        email: formEmail.value || undefined,
        phone: formPhone.value || undefined,
        deptId: formDeptId.value,
        status: formStatus.value ?? 0,
      }
      if (formPassword.value) base.password = formPassword.value
      await userApi.update(base)
      const uid = Number(editingUser.value.id)
      await relationApi.assignUserDepts(uid, toNumbers(selectedDepts.value))
      await relationApi.assignUserPosts(uid, toNumbers(selectedPosts.value))
      await relationApi.assignUserRoles(uid, toNumbers(selectedRoles.value))
    } else {
      const payload: UserCreateForm = {
        username: formUsername.value.trim(),
        password: formPassword.value,
        realName: formRealName.value || undefined,
        email: formEmail.value || undefined,
        phone: formPhone.value || undefined,
        deptId: formDeptId.value,
        status: formStatus.value ?? 0,
        deptIds: toNumbers(selectedDepts.value),
        postIds: toNumbers(selectedPosts.value),
        roleIds: toNumbers(selectedRoles.value),
      }
      await userApi.create(payload)
    }
    formOpen.value = false
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  } finally {
    submitting.value = false
  }
}

function askDelete(user: SysUser) {
  deleteTarget.value = user
  deleteOpen.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await userApi.remove(Number(deleteTarget.value.id))
    deleteOpen.value = false
    deleteTarget.value = null
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败'
  } finally {
    deleting.value = false
  }
}

function toggle(set: Set<string>, id: string): Set<string> {
  const next = new Set(set)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  return next
}

onMounted(() => {
  void loadMeta()
  void load(1)
})
</script>

<template>
  <div class="page">
    <div class="page__bar">
      <div class="page__title">
        <h2 class="page__heading">用户管理</h2>
        <p class="page__desc">管理用户账号、组织归属、角色授权与状态。</p>
      </div>
      <BaseButton variant="primary" @click="openCreate">新增用户</BaseButton>
    </div>

    <div class="filters">
      <div class="filters__item">
        <label class="filters__label" for="user-keyword">关键词</label>
        <input
          id="user-keyword"
          v-model="keyword"
          class="filters__input"
          type="text"
          placeholder="账号 / 姓名 / 邮箱"
          @keyup.enter="load(1)"
        >
      </div>
      <div class="filters__item">
        <label class="filters__label" for="user-dept-filter">所属部门</label>
        <select id="user-dept-filter" v-model="deptFilter" class="form__select">
          <option value="">全部</option>
          <option v-for="d in deptOptions" :key="d.value" :value="d.value">{{ d.label }}</option>
        </select>
      </div>
      <div class="filters__item">
        <label class="filters__label" for="user-post-filter">所属岗位</label>
        <select id="user-post-filter" v-model="postFilter" class="form__select">
          <option value="">全部</option>
          <option v-for="p in postOptions" :key="p.value" :value="p.value">{{ p.label }}</option>
        </select>
      </div>
      <BaseButton variant="secondary" @click="load(1)">查询</BaseButton>
      <BaseButton
        variant="text"
        @click="keyword = ''; deptFilter = undefined; postFilter = undefined; load(1)"
      >
        重置
      </BaseButton>
    </div>

    <p v-if="error" class="page__error" role="alert">{{ error }}</p>

    <div v-if="loading" class="page__state">
      <span class="page__spinner" aria-hidden="true" /> 加载中…
    </div>
    <div v-else-if="list.length === 0" class="page__state">暂无用户数据。</div>

    <div v-else class="card">
      <table class="table">
        <thead>
          <tr>
            <th>账号</th>
            <th>姓名</th>
            <th>邮箱</th>
            <th>手机</th>
            <th>角色</th>
            <th class="table__th--sm">状态</th>
            <th class="table__th--actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in list" :key="user.id" class="table__row">
            <td><span class="mono">{{ user.username }}</span></td>
            <td>{{ user.realName || '—' }}</td>
            <td>{{ user.email || '—' }}</td>
            <td>{{ user.phone || '—' }}</td>
            <td>
              <span v-if="user.roleIds && user.roleIds.length > 0" class="mono">
                {{ roleOptions.filter((r) => user.roleIds.includes(r.value)).map((r) => r.label).join('、') || '—' }}
              </span>
              <span v-else>—</span>
            </td>
            <td><StatusBadge :status="user.status" /></td>
            <td>
              <div class="table__actions">
                <BaseButton size="sm" variant="text" @click="openEdit(user)">编辑</BaseButton>
                <BaseButton size="sm" variant="text" @click="askDelete(user)">删除</BaseButton>
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
      :title="isEdit ? '编辑用户' : '新增用户'"
      :loading="submitting"
      @confirm="confirmSave"
    >
      <template #body>
        <div class="form">
          <BaseInput id="user-username" v-model="formUsername" label="登录账号" placeholder="请输入登录账号" />
          <BaseInput
            id="user-pass"
            v-model="formPassword"
            label="密码"
            type="password"
            :placeholder="isEdit ? '留空则不修改密码' : '至少 8 位'"
          />
          <BaseInput id="user-realname" v-model="formRealName" label="姓名" placeholder="真实姓名" />
          <BaseInput id="user-email" v-model="formEmail" label="邮箱" type="email" placeholder="name@domain.com" />
          <BaseInput id="user-phone" v-model="formPhone" label="手机号" type="text" placeholder="11 位手机号" />
          <BaseSelect
            id="user-dept"
            v-model="formDeptId"
            label="主部门"
            :options="deptOptions"
            placeholder="选择主部门"
          />
          <BaseSelect id="user-status" v-model="formStatus" label="状态" :options="statusOptions" />

          <p class="section-label">关联分配</p>

          <div class="assign">
            <p class="assign__title">部门</p>
            <div class="assign__chips">
              <button
                v-for="d in deptOptions"
                :key="d.value"
                type="button"
                class="assign__chip"
                :class="{ 'is-on': selectedDepts.has(d.value) }"
                @click="selectedDepts = toggle(selectedDepts, d.value)"
              >
                {{ d.label }}
              </button>
            </div>

            <p class="assign__title">岗位</p>
            <div class="assign__chips">
              <button
                v-for="p in postOptions"
                :key="p.value"
                type="button"
                class="assign__chip"
                :class="{ 'is-on': selectedPosts.has(p.value) }"
                @click="selectedPosts = toggle(selectedPosts, p.value)"
              >
                {{ p.label }}
              </button>
            </div>

            <p class="assign__title">角色</p>
            <div class="assign__chips">
              <button
                v-for="r in roleOptions"
                :key="r.value"
                type="button"
                class="assign__chip"
                :class="{ 'is-on': selectedRoles.has(r.value) }"
                @click="selectedRoles = toggle(selectedRoles, r.value)"
              >
                {{ r.label }}
              </button>
            </div>
          </div>
        </div>
      </template>
    </ModalDrawer>

    <ConfirmDialog
      v-model:open="deleteOpen"
      title="删除用户"
      :message="`确定删除用户「${deleteTarget?.username ?? ''}」吗？该操作不可撤销。`"
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
.assign {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.assign__title {
  margin: var(--space-1) 0 0;
  font-family: var(--font-mono);
  font-size: var(--text-micro);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--color-text-tertiary);
}
.assign__chips {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}
.assign__chip {
  height: 30px;
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-default);
  border-radius: 999px;
  background-color: transparent;
  color: var(--color-text-tertiary);
  font-family: var(--font-sans);
  font-size: var(--text-caption);
  cursor: pointer;
  transition:
    background-color var(--dur-fast) var(--ease-out),
    border-color var(--dur-fast) var(--ease-out),
    color var(--dur-fast) var(--ease-out);
}
.assign__chip:hover {
  border-color: var(--color-border-strong);
  color: var(--color-text-secondary);
}
.assign__chip.is-on {
  background-color: var(--color-accent-soft);
  border-color: var(--color-accent-mark);
  color: var(--color-accent-mark);
}
</style>