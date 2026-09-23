# DocHub 开发任务看板（tasks.md）

> 依据 AGENTS.md §1-§6 与 docs/modules 模块任务文档拆分，按 **M1 → M2** 执行。
> DoD：零编译错 + 性能/并发军规 + 完成后勾选 `[x]` 并输出 ≤3 句关键变动。
> 验证命令：后端 `cd backend && ./mvnw clean compile test`；前端 `pnpm run typecheck && pnpm run lint`。

---

## M1 用户管理（登录）模块

### 周期1：底座与登录认证
- [x] **T1-1** 实体与建表（sys_user/role/permission/dept 对齐 schema.sql，含 @Version + 逻辑删除 + 审计）
- [x] **T1-2** 全局基建核对（Result/PageResult/GlobalExceptionHandler 409/SnowflakeIdWorker）
- [x] **T1-3** 登录接口（POST /auth/login，BCrypt 校验 + JWT 签发）
- [x] **T1-4** 鉴权切面与当前用户上下文（AuthContext/Filter/@RequirePermission）
- [x] **T1-5** 密码体系接入 BCrypt（取代明文 TODO）

### 周期2：用户/RBAC/部门管理
- [x] **T1-6** 用户管理 CRUD（Specification 分页筛选，更新带 version 乐观锁）
- [x] **T1-7** 角色定义与权限点 CRUD（role_key/perm_key 唯一，树形权限）
- [x] **T1-8** 用户-角色绑定 + 角色-权限绑定（显式中间表，saveAll 批量，幂等）
- [x] **T1-9** 部门管理（树形 parent_id+ancestors，防父=自身，有子禁删）
- [x] **T1-10** 操作审计日志（AUDIT_LOGIN/ACCESS 异步写入）

## M2 文档管理（业务）模块

### 周期1：数据层 + 空间/目录
- [ ] **T1-1** 实体与建表（department_space/folder/document/document_version/file_object/space_membership）
- [ ] **T1-2** 部门空间 CRUD + 配额初值（QuotaConfig）
- [ ] **T1-3** 目录服务（树 + 建目录，移动重算 ancestors）

### 周期2：上传/版本/权限继承
- [ ] **T2-1** 文档上传（uploadDocument）+ 分片文件（FileObject，≤100MB，同名新版本）
- [ ] **T2-2** 文档预览/下载（previewDocument，Viewer 只读，越权 403）
- [ ] **T2-3** 权限继承校验服务（PermissionResolverService，移动/归档 403）
- [ ] **T2-4** 版本管理（restoreVersion / 历史列表）
- [ ] **T2-5** 前端部门空间管理、文档治理操作界面

### 周期3：检索 + AI 合成（智能层闭环）
- [ ] **T3-1** 语义检索（searchDocument，有权文档子集裁剪，≤2s）
- [ ] **T3-2** AI 合成任务创建（startSynthesis，SynthesisType + 来源集，异步）
- [ ] **T3-3** 引用溯源生成（Citation，观点→源文档片段映射）
- [ ] **T3-4** 引用溯源校验（verifyCitation，无来源/SOURCE_INVALID 禁止入库）
- [ ] **T3-5** 人工确认入库（confirmDocument，verifyCitation 通过 + 显式确认双门槛）
- [ ] **T3-6** 归档（archiveDocument，CONFIRMED→ARCHIVED，继承目标权限）

### 周期4：系统管理 + 治理 + 回收站
- [ ] **T4-1** 回收站（softDelete/hardDelete/restoreFromRecycle，硬删后不可恢复）
- [ ] **T4-2** 治理操作（moveDocument/rename/归类/revokePermission，Editor 不可自升 Admin）
- [ ] **T4-3** 存储配额与告警（setQuota，≥80% WARN / 超限 ERROR，去重幂等）
- [ ] **T4-4** 审计与访问日志导出（AuditLog/AccessLog/exportAuditLog 限上限）
- [ ] **T4-5** 收藏（Favorite，越权文档不写入）

### 周期5：前端 + 收尾加固
- [ ] **T5-1** 前端文档列表/上传/预览页
- [ ] **T5-2** 前端检索 + AI 合成进度 + 引用卡 + 确认入库 Modal
- [ ] **T5-3** 前端部门空间管理/配额设置页/告警列表
- [ ] **T5-4** NFR 与性能加固（检索≤2s、LCP≤1.5s、分片、Keyset/Cursor 分页、原子计数）

---

## M3 认证安全升级（参考 demo2/backend(2) 设计）

- [x] **AS-1** 双 token + 会话管理（JwtUtil 拆 Access/Refresh、SessionManager、AuthContext 扩展、/auth/refresh 轮换、/auth/logout）
- [x] **AS-2** 前端 401 自动刷新重放（http.ts single-flight 刷新队列 + storage 双 token）
- [x] **AS-3** 登录防爆破（LoginBruteforceGuard：5 次锁 10min + IP 60s/20 限流）
- [x] **AS-4** 图形验证码（CaptchaService 预留开关 + /auth/captcha）
- [x] **AS-5** RSA 密码加密传输（RsaUtil 私钥解密 + /auth/public-key + 前端 Web Crypto）
- [x] **AS-6** 薄弱点加固（Token 脱敏、密钥环境变量化、白名单复核、登出使 Refresh 失效）

---

## 硬约束（任何任务完成后复核）
- [ ] 命名 100% 对齐 `docs/01-requirements/GLOSSARY.md`，未脑补字段
- [ ] 主表含 `@Version`，更新 DTO 带 version + Service 前置比对 + 409 兜底
- [ ] 动态检索用 `Specification`，无 WHERE 1=1 拼接；批量用 findAllById + saveAll
- [ ] 列表/分页用 DTO 投影，无 SELECT *；无 findById 循环；findAll 后过滤软删已消除
- [ ] AI 合成结果：未通过引用溯源校验或未人工确认，禁止入库归档
- [ ] 环形权限二次鉴权在 Service 层强制，越权返回 403 且不泄露存在性