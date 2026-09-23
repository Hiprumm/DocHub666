<script setup lang="ts">
/**
 * PostView —— 岗位管理（表格 + 分页 + 关键词筛选）。
 */
import { onMounted, ref } from 'vue'
import { postApi } from '@/api/system'
import type { PostForm, SysPost } from '@/types/system'
import BaseButton from '@/components/BaseButton.vue'
import BaseInput from '@/components/BaseInput.vue'
import BaseSelect from '@/components/system/BaseSelect.vue'
import StatusBadge from '@/components/system/StatusBadge.vue'
import ModalDrawer from '@/components/system/ModalDrawer.vue'
import ConfirmDialog from '@/components/system/ConfirmDialog.vue'
import PaginationBar from '@/components/system/PaginationBar.vue'

const list = ref<SysPost[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(true)
const error = ref('')
const keyword = ref('')

const formOpen = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const form = ref<PostForm>({
  postName: '',
  postKey: '',
  sort: 0,
  status: 0,
  remark: '',
})

const deleteOpen = ref(false)
const deleting = ref(false)
const deleteTarget = ref<SysPost | null>(null)

const statusOptions = [
  { value: 0, label: '启用' },
  { value: 1, label: '停用' },
]

async function load(page = pageNum.value) {
  pageNum.value = page
  loading.value = true
  error.value = ''
  try {
    const res = await postApi.list(page, pageSize.value, keyword.value.trim() || undefined)
    list.value = res.records
    total.value = res.total
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载岗位失败'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  form.value = { postName: '', postKey: '', sort: 0, status: 0, remark: '' }
  formOpen.value = true
}

function openEdit(post: SysPost) {
  isEdit.value = true
  form.value = {
    id: post.id,
    postName: post.postName,
    postKey: post.postKey,
    sort: post.sort ?? 0,
    status: post.status ?? 0,
    remark: post.remark ?? '',
    version: Number(post.version ?? 0),
  }
  formOpen.value = true
}

async function confirmSave() {
  if (!form.value.postName.trim() || !form.value.postKey.trim()) return
  submitting.value = true
  try {
    const payload: PostForm = {
      id: isEdit.value ? form.value.id : undefined,
      postName: form.value.postName.trim(),
      postKey: form.value.postKey.trim(),
      sort: form.value.sort ?? 0,
      status: form.value.status ?? 0,
      remark: form.value.remark,
      version: form.value.version,
    }
    if (isEdit.value) await postApi.update(payload)
    else await postApi.create(payload)
    formOpen.value = false
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  } finally {
    submitting.value = false
  }
}

function askDelete(post: SysPost) {
  deleteTarget.value = post
  deleteOpen.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await postApi.remove(Number(deleteTarget.value.id))
    deleteOpen.value = false
    deleteTarget.value = null
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败'
  } finally {
    deleting.value = false
  }
}

onMounted(() => load(1))
</script>

<template>
  <div class="page">
    <div class="page__bar">
      <div class="page__title">
        <h2 class="page__heading">岗位管理</h2>
        <p class="page__desc">维护岗位字典，用于用户关联归档。</p>
      </div>
      <BaseButton variant="primary" @click="openCreate">新增岗位</BaseButton>
    </div>

    <div class="filters">
      <div class="filters__item">
        <label class="filters__label" for="post-keyword">关键词</label>
        <input
          id="post-keyword"
          v-model="keyword"
          class="filters__input"
          type="text"
          placeholder="岗位名称 / 标识"
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
    <div v-else-if="list.length === 0" class="page__state">暂无岗位数据。</div>

    <div v-else class="card">
      <table class="table">
        <thead>
          <tr>
            <th>岗位名称</th>
            <th>岗位标识</th>
            <th class="table__th--sm">排序</th>
            <th class="table__th--sm">状态</th>
            <th>备注</th>
            <th class="table__th--actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="post in list" :key="post.id" class="table__row">
            <td>{{ post.postName }}</td>
            <td><span class="mono">{{ post.postKey }}</span></td>
            <td>{{ post.sort }}</td>
            <td><StatusBadge :status="post.status" /></td>
            <td>{{ post.remark || '—' }}</td>
            <td>
              <div class="table__actions">
                <BaseButton size="sm" variant="text" @click="openEdit(post)">编辑</BaseButton>
                <BaseButton size="sm" variant="text" @click="askDelete(post)">删除</BaseButton>
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
      :title="isEdit ? '编辑岗位' : '新增岗位'"
      :loading="submitting"
      @confirm="confirmSave"
    >
      <template #body>
        <div class="form">
          <BaseInput id="post-name" v-model="form.postName" label="岗位名称" placeholder="如：研发工程师" />
          <BaseInput id="post-key" v-model="form.postKey" label="岗位标识" placeholder="如：dev-engineer" />
          <BaseInput
            id="post-sort"
            label="显示顺序"
            type="text"
            placeholder="0"
            :model-value="String(form.sort ?? '')"
            @update:model-value="form.sort = Number($event) || 0"
          />
          <BaseSelect id="post-status" v-model="form.status" label="状态" :options="statusOptions" />
          <div class="form__field">
            <label class="form__label" for="post-remark">备注</label>
            <textarea id="post-remark" v-model="form.remark" class="form__textarea" placeholder="可选备注" />
          </div>
        </div>
      </template>
    </ModalDrawer>

    <ConfirmDialog
      v-model:open="deleteOpen"
      title="删除岗位"
      :message="`确定删除岗位「${deleteTarget?.postName ?? ''}」吗？该操作不可撤销。`"
      :loading="deleting"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
@import './_page.css';
</style>