/**
 * 系统管理模块 API 封装 —— 复用 http.ts 的 request helper（自动带 token、解包 Result<T>）。
 * 后端所有接口带 @RequirePermission，前端只负责路由调用。
 */
import { request } from '@/api/http'
import type {
  AssignReq,
  DeptForm,
  LogQuery,
  PageResult,
  PermForm,
  PostForm,
  RoleForm,
  SysDept,
  SysLog,
  SysPermission,
  SysPost,
  SysRole,
  SysUser,
  UserCreateForm,
  UserUpdateForm,
} from '@/types/system'

/* ============================================================
 * 部门 /sys/dept
 * ============================================================ */
export const deptApi = {
  tree: () => request<SysDept[]>({ url: '/sys/dept/tree', method: 'get' }),
  children: (parentId: number) =>
    request<SysDept[]>({ url: '/sys/dept/children', method: 'get', params: { parentId } }),
  get: (id: number) => request<SysDept>({ url: `/sys/dept/${id}`, method: 'get' }),
  create: (data: DeptForm) => request<SysDept>({ url: '/sys/dept', method: 'post', data }),
  update: (data: DeptForm) => request<SysDept>({ url: '/sys/dept', method: 'put', data }),
  remove: (id: number) => request<void>({ url: `/sys/dept/${id}`, method: 'delete' }),
}

/* ============================================================
 * 岗位 /sys/post
 * ============================================================ */
export const postApi = {
  list: (pageNum: number, pageSize: number, keyword?: string) =>
    request<PageResult<SysPost>>({
      url: '/sys/post/list',
      method: 'get',
      params: { pageNum, pageSize, keyword: keyword || undefined },
    }),
  get: (id: number) => request<SysPost>({ url: `/sys/post/${id}`, method: 'get' }),
  create: (data: PostForm) => request<SysPost>({ url: '/sys/post', method: 'post', data }),
  update: (data: PostForm) => request<SysPost>({ url: '/sys/post', method: 'put', data }),
  remove: (id: number) => request<void>({ url: `/sys/post/${id}`, method: 'delete' }),
}

/* ============================================================
 * 角色 /sys/role
 * ============================================================ */
export const roleApi = {
  list: (pageNum: number, pageSize: number, keyword?: string) =>
    request<PageResult<SysRole>>({
      url: '/sys/role/list',
      method: 'get',
      params: { pageNum, pageSize, keyword: keyword || undefined },
    }),
  get: (id: number) => request<SysRole>({ url: `/sys/role/${id}`, method: 'get' }),
  create: (data: RoleForm) => request<SysRole>({ url: '/sys/role', method: 'post', data }),
  update: (data: RoleForm) => request<SysRole>({ url: '/sys/role', method: 'put', data }),
  remove: (id: number) => request<void>({ url: `/sys/role/${id}`, method: 'delete' }),
}

/* ============================================================
 * 权限 /sys/permission
 * ============================================================ */
export const permissionApi = {
  tree: () => request<SysPermission[]>({ url: '/sys/permission/tree', method: 'get' }),
  children: (parentId: number) =>
    request<SysPermission[]>({ url: '/sys/permission/children', method: 'get', params: { parentId } }),
  get: (id: number) => request<SysPermission>({ url: `/sys/permission/${id}`, method: 'get' }),
  create: (data: PermForm) => request<SysPermission>({ url: '/sys/permission', method: 'post', data }),
  update: (data: PermForm) => request<SysPermission>({ url: '/sys/permission', method: 'put', data }),
  remove: (id: number) => request<void>({ url: `/sys/permission/${id}`, method: 'delete' }),
}

/* ============================================================
 * 用户 /sys/user
 * ============================================================ */
export interface UserListParams {
  pageNum: number
  pageSize: number
  keyword?: string
  deptId?: string
  postId?: string
}

export const userApi = {
  list: (params: UserListParams) =>
    request<PageResult<SysUser>>({
      url: '/sys/user/list',
      method: 'get',
      params: {
        pageNum: params.pageNum,
        pageSize: params.pageSize,
        keyword: params.keyword || undefined,
        deptId: params.deptId || undefined,
        postId: params.postId || undefined,
      },
    }),
  get: (id: number) => request<SysUser>({ url: `/sys/user/${id}`, method: 'get' }),
  create: (data: UserCreateForm) => request<SysUser>({ url: '/sys/user', method: 'post', data }),
  update: (data: UserUpdateForm) => request<SysUser>({ url: '/sys/user', method: 'put', data }),
  remove: (id: number) => request<void>({ url: `/sys/user/${id}`, method: 'delete' }),
}

/* ============================================================
 * 关联 /sys/relation
 * ============================================================ */
export const relationApi = {
  listUserDepts: (userId: number) =>
    request<number[]>({ url: `/sys/relation/user/${userId}/depts`, method: 'get' }),
  listUserPosts: (userId: number) =>
    request<number[]>({ url: `/sys/relation/user/${userId}/posts`, method: 'get' }),
  listUserRoles: (userId: number) =>
    request<number[]>({ url: `/sys/relation/user/${userId}/roles`, method: 'get' }),
  listRolePermissions: (roleId: number) =>
    request<number[]>({ url: `/sys/relation/role/${roleId}/permissions`, method: 'get' }),
  assignUserDepts: (userId: number, ids: number[]) =>
    request<void>({ url: `/sys/relation/user/${userId}/depts`, method: 'put', data: { ids } as AssignReq }),
  assignUserPosts: (userId: number, ids: number[]) =>
    request<void>({ url: `/sys/relation/user/${userId}/posts`, method: 'put', data: { ids } as AssignReq }),
  assignUserRoles: (userId: number, ids: number[]) =>
    request<void>({ url: `/sys/relation/user/${userId}/roles`, method: 'put', data: { ids } as AssignReq }),
  assignRolePermissions: (roleId: number, ids: number[]) =>
    request<void>({ url: `/sys/relation/role/${roleId}/permissions`, method: 'put', data: { ids } as AssignReq }),
}

/* ============================================================
 * 操作日志 /sys/audit-log
 * ============================================================ */
export const logApi = {
  page: (query: LogQuery) =>
    request<PageResult<SysLog>>({
      url: '/sys/audit-log/page',
      method: 'get',
      params: {
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        actorName: query.actorName || undefined,
        action: query.action || undefined,
        result: query.result || undefined,
      },
    }),
}