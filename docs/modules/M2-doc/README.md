# M2 文档管理（业务）模块任务文档

> 模块代码前缀：`doc`（空间/目录/文档/检索/合成） + `sys`（配额/审计/告警）
> 依据：AGENTS.md §1-§6、PRD §3 生命周期状态机 + §4 功能模块 + §5 NFR、GLOSSARY 实体/枚举/动词、USER_STORIES 文档故事
> 核心闭环：**上传/查看 → 检索 → AI 合成（溯源校验+人工确认双门槛）→ 入库归档 → 回收站治理**
> 底线：AI 合成结果**未通过引用溯源校验或未经人工确认不得入库**；环形权限二次鉴权在 Service 层强制。

---

## 1. 模块目标与边界

| 项 | 说明 |
|----|------|
| 目标 | 实现"文档管理 + 智能检索与合成"双引擎闭环 |
| 核心实体 | DepartmentSpace、SpaceMembership、Folder、Document、DocumentVersion、FileObject、SynthesisTask、Citation + evtl. Favorite |
| 核心状态 | `UPLOADED → AI_GENERATING → PENDING_REVIEW → CONFIRMED → ARCHIVED / DELETED` |
| 不包含 | 全文搜索引擎（Out of Scope）、在线多人协同、审批流、跨组织外分享（PRD §6） |
| 依赖 | M1 提供鉴权（`getCurrentUserId()`）/ `@RequirePermission` 切面 / AuditLog 写入 |
| 排水 | 检索为**语义检索**（向量/关键词加权），针对"有权文档集合"，越权检索不可见 |

### 命名映射（对齐 GLOSSARY）
| 概念 | DB 表 | Java | TS 类型 | Vue 组件 |
|------|-------|------|--------|---------|
| 部门空间 | `department_space` | `DepartmentSpace` | `DepartmentSpace` | `DepartmentSpaceList.vue` |
| 空间成员 | `space_membership` | `SpaceMembership` | `SpaceMembership` | — |
| 目录 | `folder` | `Folder` | `Folder` | `FolderTree.vue` |
| 文档 | `document` | `Document` | `Document` | `DocumentList.vue` |
| 版本 | `document_version` | `DocumentVersion` | `DocumentVersion` | — |
| 文件对象 | `file_object` | `FileObject` | `FileObject` | — |
| 合成任务 | `synthesis_task` | `SynthesisTask` | `SynthesisTask` | `SynthesisProgress.vue` |
| 引用 | `citation` | `Citation` | `Citation` | `CitationList.vue` |
| 配额 | `quota_config` | `QuotaConfig` | `QuotaConfig` | `QuotaSetting.vue` |
| 告警 | `alert_record` | `AlertRecord` | `AlertRecord` | `AlertList.vue` |
| 收藏 | `favorite` | `Favorite` | `Favorite` | `FavoriteList.vue` |

### 枚举（GLOSSARY 对齐，DB 存字符串 snake_case）
- **DocumentStatus**：`UPLOADED` / `PENDING_REVIEW` / `CONFIRMED` / `ARCHIVED` / `DELETED`
- **SynthesisTaskStatus**：`RUNNING` / `COMPLETED` / `FAILED` / `CANCELLED`
- **CitationStatus**：`VALID` / `UNTRACEABLE` / `SOURCE_INVALID`
- **SynthesisType**：`WEEKLY_REPORT` / `PROPOSAL_DRAFT` / `POLICY_SUMMARY`（可扩展）
- **DeleteKind**：`SOFT` / `HARD`
- **AuditAction**：`UPLOAD` / `ACCESS` / `AI_CONFIRM` / `AI_ARCHIVE` / `RECYCLE` / `PERMISSION_CHANGE` ...

---

## 2. 任务清单（周期1-5）

### 周期1：数据层 + 空间/目录（P0，先建数据底座）

#### T1-1 实体与建表（对齐 schema.sql）
- **功能**：`department_space / folder / document / document_version / file_object / space_membership` 六表 + 实体，逻辑外键关联，主表含 `@Version` + 逻辑删除 + 审计。
- **Ac**：`document` 状态字典 lock；`document_version` 持不可变快照 + {versionNo, modifierId, versionTime}；`folder` 持 `parentId+ancestors`（树）
- **性能军规**：大文本（如版本正文 or 内容指针——**正文不 DEMO，用指针+元数据**）不上列表 SELECT *；列表用 DTO 投影

#### T1-2 部门空间 CRUD + 配额初值
- **功能**：空间创建/编辑（空间名唯一）/列表；`QuotaConfig` 初始化默认额度。
- **Ac**：空间删除前校验无文档；配额"部门之和≤全局上限"用 `setQuota` 校验

#### T1-3 目录服务
- **功能**：`Folder` 树 + 在空间内建目录；目录移动重算 `ancestors`。
- **Ac**：父级=自身禁止；环路径（ancestors 含目标）拦截

### 周期2：上传/版本/权限继承（P0-P1，上传闭环）

> 空间权限模型：`Viewer`(只读) / `Editor`(读写/上传) / `Admin`(管理空间)。

#### T2-1 文档上传（uploadDocument）+ 分片文件（FileObject）
- **功能**：校验配额昵称 + 权限（Editor/Admin）+ 格式 + 单文件≤100MB + 分片上传/断点续传；同名文档**新建版本**而非覆盖。
- **Ac**：
  - 配额超限→403（`QUOTA_EXCEEDED`）；越权上传→403；格式不支持→400
  - 同名上传→`document_version.versionNo` 自增，原文档保留，返回新版本号
- **性能军规**：`findById` 校验同名一次；文件二进制写入 OSS/本地（不写 DB BLOB），分片批量落盘

#### T2-2 文档预览 / 下载（previewDocument）
- **功能**：在线预览或降级下载；记录 AccessLog；越权 403。
- **Ac**：Viewer 可预览不可编辑；越权（非空间成员/无权限）→403，**不泄露存在性**

#### T2-3 权限继承校验服务（PermissionResolverService）
- **功能**：任意文档/目录解析出"空间 × 角色"→ 判定操作是否合法（等价于 PRD 三次鉴权：接口级→文件级→转角继承）。
- **Ac**：移动/归档/恢复时校验目标目录权限；无权限 403
- **性能军规**：一次查询缓存"用户-空间-角色"映射，避免重复 findById 链

#### T2-4 版本管理（restoreVersion / 列表）
- **功能**：历史版本列表 + 还原 `restoreVersion(documentVersionId)`；Viewer 仅预览。
- **Ac**：Editor/Admin 可还原；还原为软操作（新增快照）不破坏历史

### 周期3：检索 + AI 合成（P1-P2，智能层闭环）

#### T3-1 语义检索（searchDocument）
- **功能**：关键词/向量检索（MVP 用关键词加权 + 可选向量），**裁剪到有权文档集合**。
- **Ac**：检索结果**不返回越权文档**（服务端裁剪）；检索≤2s；返回 {docId, snippet, score}
- **性能军规**：检索在**有权文档子集**上执行；索引只对 `CONFIRMED/ARCHIVED` 文档开放

#### T3-2 AI 合成任务创建（startSynthesis）
- **功能**：选择 SynthesisType + 来源文档集 → 创建 `SynthesisTask`(RUNNING)。
- **Ac**：来源含越权文档→终止并提示；无来源→400；合成经异步执行

#### T3-3 引用溯源生成（Citation）
- **功能**：合成结果的每观点映射 `Citation`（源文档片段），产出 `citation_status`。
- **Ac**：`VALID`(有源) / `UNTRACEABLE`(无源) 标注清晰；合成产物进入 `PENDING_REVIEW`

#### T3-4 引用溯源校验（verifyCitation）
- **功能**：服务端强制校验每个观点是否有可溯源来源；有 `UNTRACEABLE` 或 `SOURCE_INVALID` 观点 → **禁止入库**。
- **Ac**：任一观点无来源→校验失败，`confirmDocument` 被拦截；全部 VALID 才放行

#### T3-5 人工确认入库（confirmDocument）
- **功能**：管理员/作者显式确认后，`PENDING_REVIEW → CONFIRMED`（写入 AuditLog `AI_CONFIRM`）。
- **Ac**：**必满足**verifyCitation 通过 + 人工确认**双门槛**；未确认校验失败一律拒绝归档

#### T3-6 归档（archiveDocument）
- **功能**：`CONFIRMED → ARCHIVED`，移动到归档目录，继承目标权限。
- **Ac**：归档记录 `AI_ARCHIVE`/`DOCUMENT_ARCHIVE`；越权 403

### 周期4：系统管理 + 治理 + 回收站（P1-P2）

#### T4-1 回收站（softDelete / hardDelete / restoreFromRecycle）
- **功能**：软删→`DELETED`；清空→硬删（`RECYCLE_CLEAR`，幂等）；恢复→回 `UPLOADED`。
- **Ac**：软删可恢复；**硬删后恢复返回'已失效'**；清空为空时幂等成功

#### T4-2 治理操作（moveDocument / rename / 归类 / revokePermission）
- **功能**：跨目录移动（冲突确认）、重命名、批量归类、回收成员权限。
- **Ac**：Editor 不可自升 Admin；回收权限即时生效→403；**保护至少保留一名 Admin**

#### T4-3 存储配额与告警（setQuota / AlertRecord）
- **功能**：设置部门/全局配额；达 ≥80% 触发 `WARN`、超限 `ERROR` 告警；同级别同态不重复。
- **Ac**：`新额度 ≥ 当前用量`；部门之和 ≤ 全局上限；告警去重幂等

#### T4-4 审计与访问日志导出（AuditLog / AccessLog / exportAuditLog）
- **功能**：记录上传/预览/检索/AI入库/权限变更；管理员导出审计日志（限上限）。
- **Ac**：exportAuditLog 超上限被校验；含主体/动作/对象/时间/结果

#### T4-5 收藏（Favorite）
- **功能**：文档星标集合；**越权文档不写入**。
- **Ac**：无权访问的文档收藏被忽略；收藏列表只含有权文档

### 周期5：前端 + 收尾加固（P2）

#### T5-1 前端文档列表 / 上传 / 预览页
- 对齐 DESIGN_SYSTEM.md，对接 M2 REST；存储 token 于 Pinia + localStorage

#### T5-2 前端检索 + AI 合成进度（SynthesisProgress.vue）+ 引用卡（CitationList.vue）+ 确认入库 Modal
- 检索结果展示 score/snippet；合成显示进度，切换页面不中断；确认入库弹窗

#### T5-3 前端部门空间管理 / 配额设置页（QuotaSetting.vue）/ 告警列表（AlertList.vue）

#### T5-4 NFR 与性能加固
- 检索≤2s、首屏 LCP≤1.5s、单文件≤100MB 分片；Keyset/Cursor 分页替代深 offset；`@Modifying` 原子计数（若有点赞/命中）

---

## 3. 状态机验收底线（对接 PRD §3）

```
UPLOADED --(startSynthesis)--> AI_GENERATING --(done)--> PENDING_REVIEW
PENDING_REVIEW --(verifyCitation VALID + confirmDocument)--> CONFIRMED
CONFIRMED --(archiveDocument)--> ARCHIVED
{any above} --(softDelete)--> DELETED --(restoreFromRecycle)--> UPLOADED
DELETED --(hardDelete)--> [void]
```
> **硬约束**：`PENDING_REVIEW → CONFIRMED` 必须**同时满足** ① 引用溯源校验通过（无 UNTRACEABLE/SOURCE_INVALID）② 显式人工确认，缺一不可，否则 403/400 拒绝。

---

## 4. 测试与验证命令

```bash
cd backend && ./mvnw clean compile test
cd frontend && pnpm run typecheck && pnpm run lint
```

**核心闭环验证**：上传→预览→检索到→AI合成→校验引用→人工确认入库→归档→软删→恢复。每个环节越权访问均需返回 403。

---

## 5. 未尽事项 / 风险
- 语义检索 MVP 采用"关键词加权 + 简单向量"，未引入全文搜索引擎（Out of Scope，符合 PRD §6）。
- 文件存储需选定本地目录或 OSS 抽象，分片/断点上传 `FileObject` 承载。