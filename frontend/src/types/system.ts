/**
 * 系统管理模块类型定义 —— 与后端 vo/* 契约 100% 对齐
 * 所有 id 均为 String（后端防精度丢失）。严禁 any。
 */

/** 后端统一分页结果（对应 common.PageResult<T>） */
export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

/** 关联分配请求体（对应 dto.AssignReq：ids 为 Long 集合） */
export interface AssignReq {
  ids: number[]
}

/* ============================================================
 * 部门
 * ============================================================ */

/** 部门视图（对应 SysDeptVo） */
export interface SysDept {
  id: string
  parentId: string
  ancestors: string
  deptName: string
  orderNum: number
  leaderUserId: string
  status: number
  version: string
  createTime: string
}

/** 部门新增/编辑表单（对应 SysDeptDto，version 乐观锁） */
export interface DeptForm {
  id?: string
  parentId?: string | number
  deptName: string
  orderNum?: number
  leaderUserId?: string
  status?: number
  version?: number
}

/* ============================================================
 * 岗位
 * ============================================================ */

/** 岗位视图（对应 SysPostVo） */
export interface SysPost {
  id: string
  postName: string
  postKey: string
  sort: number
  status: number
  remark: string
  version: string
  createTime: string
}

/** 岗位新增/编辑表单（对应 SysPostDto） */
export interface PostForm {
  id?: string
  postName: string
  postKey: string
  sort?: number
  status?: number
  remark?: string
  version?: number
}

/* ============================================================
 * 角色
 * ============================================================ */

/** 角色视图（对应 SysRoleVo） */
export interface SysRole {
  id: string
  roleName: string
  roleKey: string
  sort: number
  status: number
  remark: string
  version: string
  createTime: string
}

/** 角色新增/编辑表单（对应 SysRoleDto） */
export interface RoleForm {
  id?: string
  roleName: string
  roleKey: string
  sort?: number
  status?: number
  remark?: string
  version?: number
}

/* ============================================================
 * 权限
 * ============================================================ */

/** 权限视图（对应 SysPermissionVo） */
export interface SysPermission {
  id: string
  parentId: string
  ancestors: string
  permName: string
  permKey: string
  permType: number
  path: string
  method: string
  sort: number
  status: number
  version: string
  createTime: string
}

/** 权限新增/编辑表单（对应 SysPermissionDto） */
export interface PermForm {
  id?: string
  parentId?: string | number
  permName: string
  permKey: string
  permType: number
  path?: string
  method?: string
  sort?: number
  status?: number
  version?: number
}

/* ============================================================
 * 用户
 * ============================================================ */

/** 用户视图（对应 SysUserVo；password 已脱敏） */
export interface SysUser {
  id: string
  username: string
  realName: string
  email: string
  phone: string
  deptId: string
  deptIds: string[]
  postIds: string[]
  roleIds: string[]
  status: number
  version: string
  createTime: string
  updateTime: string
}

/** 用户新增表单（对应 SysUserCreateDto） */
export interface UserCreateForm {
  username: string
  password: string
  realName?: string
  email?: string
  phone?: string
  deptId?: string
  status?: number
  deptIds?: number[]
  postIds?: number[]
  roleIds?: number[]
}

/** 用户编辑表单（对应 SysUserUpdateDto） */
export interface UserUpdateForm {
  id: string
  password?: string
  version: number
  realName?: string
  email?: string
  phone?: string
  deptId?: string
  status?: number
}

/* ============================================================
 * 操作日志
 * ============================================================ */

/** 操作日志视图（对应 AuditLogVo） */
export interface SysLog {
  id: string
  actorId: string
  actorName: string
  action: string
  targetType: string
  targetId: string
  result: string
  detail: string
  createTime: string
}

/** 日志分页查询参数（对应 AuditLogQueryDto） */
export interface LogQuery {
  pageNum: number
  pageSize: number
  actorName?: string
  action?: string
  result?: string
}