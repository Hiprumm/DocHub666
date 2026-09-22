# M1 用户管理（登录）模块任务文档

> 模块代码前缀：`auth`/`sys`（基于现有 RBAC = sys_user/sys_role/sys_permission/sys_dept）
> 依据：AGENTS.md §1-§6、PRD §2 RBAC 矩阵、GLOSSARY 实体表、USER_STORIES 用户/权限故事
> 原则：本模块先做，为 M2 文档模块提供鉴权底座（AuthContext、权限切面、JWT）。

---

## 1. 模块目标与边界

| 项 | 说明 |
|----|------|
| 目标 | 提供身份认证（注册/登录/JWT）+ 用户档案 + RBAC 权限（角色、权限点、部门）底座 |
| 核心实体 | User、Role、Permission、UserRole、UserPermission、UserDept 关联 |
| 提供接口 | 鉴权切面、当前用户上下文（AuthContext）、操作审计日志写入钩子 |
| 不包含 | 文档业务（M2）、AI 合成（M2）、空间/目录（转交 M2 的空间概念，本模块仅部门）
| 共享给 M2 | `PermissionDeniedException`、`@RequirePermission` 切面、`getCurrentUserId()` |

### 命名映射（对齐 GLOSSARY）
| 概念 | DB 表 | Java | TS 类型 | Vue 组件 |
|------|-------|------|--------|---------|
| 用户 | `sys_user` | `User` | `User` | `UserList.vue` |
| 角色 | `sys_role` | `Role` | `RoleDef` | `RoleSelect.vue` |
| 权限点 | `sys_permission` | `Permission` | `Permission` | `PermissionTree.vue` |
| 部门 | `sys_dept` | `Dept` | `Dept` | `DeptTree.vue` |

---

## 2. 任务清单（周期1-2）

### 周期1：底座与登录认证（P0 底座，先完成）

#### T1-1 实体与建表（已落地 schema.sql）
- **功能**：确认 `sys_user / sys_role / sys_permission / sys_dept` 表结构与 JPA 实体一一对应，所有主表含 `version`（乐观锁）+ `is_deleted` 逻辑删除 + 审计字段。
- **涉及文件**：`backend/.../entity/*.java`、`sql/schema.sql`
- **Ac**：新用户可插入；重复 username 抛业务异常；`sys_user.dept_id` 单值（一用户一部门）
- **性能军规**：主表必带 `@Version`；关联用逻辑外键字段；`@Getter/@Setter` 而非 `@Data`

#### T1-2 全局基建核对
- **功能**：`Result`/`PageResult`/`GlobalExceptionHandler`（含 409 乐观锁）/`SnowflakeIdWorker` 就位。
- **Ac**：字段校验错误中文返回；乐观锁冲突 409；统一 `{code,message,data}`

#### T1-3 登录接口（POST /auth/login）
- **功能**：账号+密码登录 → 校验 BCrypt → 签发 JWT（含 userId/roleKey 声明）。
- **涉及文件**：`auth/controller/AuthController`、`auth/dto/LoginReq/LoginResp`、`auth/service`、`security/JwtUtil`
- **Ac**（Happy Path + Edge）：
  - 正确凭证→ 200 + token；错误密码→401；账号不存在→401（统一提示防撞库）；被停用(status=1)→403；锁定注入到 DTO `password` 用 `@NotBlank` 中文提示
- **性能军规**：登录查询 `findByUsernameAndStatus` 一次取单行；token 无状态不落库（不引入每请求膨胀查询）
- **需新增依赖**：`spring-boot-starter-security`、`jjwt`（注意与现有依赖不冲突）

#### T1-4 鉴权切面与当前用户上下文
- **功能**：`AuthContext`（ThreadLocal）+ JWT 解析 Filter + `@RequirePermission("xxx")` 注解切面，方法级权限校验。
- **Ac**：无 token→401；token 过期→401；越权→403（`PermissionDeniedException`）；`getCurrentUserId()` 可用
- **性能军规**：Filter 只解析 token 一次放入 ThreadLocal，**不每请求查库**；权限集合可缓存于 token 声明

#### T1-5 密码体系接入 BCrypt
- **功能**：注册/修改密码用 `BCryptPasswordEncoder`（取代现有明文 TODO）。
- **Ac**：存库为 BCrypt 哈希；密码字段永不进 VO

### 周期2：用户/RBAC/部门管理（P0-P1）

#### T1-6 用户管理 CRUD（POST/GET/PUT/DELETE /sys/user 分页）
- **功能**：用户新增/编辑/启停/分页查询（Specification 动态筛选：关键字/deptId/status）。
- **Ac**：编辑必须携带 version（乐观锁第一道防线）；删除为逻辑删除；分页禁止 `SELECT *`
- **性能军规**：多条件检索用 `JpaSpecificationExecutor`；更新 DTO 带 `@NotNull version`

#### T1-7 角色定义与权限点 CRUD
- **功能**：角色（RoleDef：VIEWER/EDITOR/ADMIN）+ 权限点（Permission：`doc:upload`、`doc:review`、`sys:user:manage`…）。
- **Ac**：role_key / perm_key 唯一；树形权限（parent+ancestors）；停用角色即时生效

#### T1-8 用户-角色绑定 + 角色-权限绑定
- **功能**：`assignRole(userId, roleIds)`、`assignPermissionsToRole(roleId, permIds)`（显式中间表）。
- **Ac**：分配幂等（先删后插）；并行分配不产生脏数据；`saveAll` 批量
- **性能军规**：`findAllById` 批量校验存在性 + `saveAll` 批量插入，**禁止循环单条 save**

#### T1-9 部门管理（sys_dept CRUD + 树）
- **功能**：部门新增/编辑/删除/树形查询；`parent_id`+`ancestors` 扁平树，防父级=自身、有子部门禁止删除。
- **Ac**：部门树接口不加载已删除；全表加载后再内存过滤为反模式
- **性能军规**：树接口用 `findAllByIsDeleted(0)` 数据库过滤

#### T1-10 操作审计日志（AUDIT_LOGIN/ACCESS 写入）
- **功能**：统一 `AuditLogService.record(actor, action, object, result, meta)`，登录/越权/改密写入。
- **Ac**：异步不阻塞业务主流程；日志含时间/主体/动作/结果

---

## 3. 模块测试与验证命令

```bash
cd backend && ./mvnw clean compile test
cd frontend && pnpm run typecheck && pnpm run lint
```

**端到端验证链路**：注册 → 登录拿 token → 携带 token 调 `/sys/user` 分页 → 改密 → 越权访问返回 403 → 审计日志有记录。

---

## 4. 未尽事项 / 风险
- 需引入 Spring Security + JWT（当前工程尚未含），注意与现有依赖冲突。
- `AuthContext`/权限切面与现有 `user/dto/LoginDtoReq/Resp` 旧脚手架对齐或替换。