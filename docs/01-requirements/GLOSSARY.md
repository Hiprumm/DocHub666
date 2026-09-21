# DocHub —— 统一领域语言词汇表（GLOSSARY）

> 文档版本：v1.0
> 冻结依据：docs/01-requirements/PRD.md、docs/01-requirements/USER_STORIES.md、docs/01-requirements/用户旅程与原子故事分解.md
> 目标：保证后续 **MySQL 物理建模 / Java、TypeScript 后端实体 / Vue 3 前端组件** 的命名绝对统一，杜绝术语混乱与 AI 命名幻觉。
>
> ⚠️ 使用规则：
> - **以“统一英文标识符”为准**，任何层不得自行发明同义别名。
> - 数据库表名一律 **小写 + 下划线（snake_case）**，复数形式；Java Entity / TS 类型一律 **UpperCamelCase**；Vue 组件 **`.vue` 文件用 PascalCase**、`camelCase` 文件名也可（二者在本项目统一采用 PascalCase 组件文件）。
> - 枚举值数据库存 **字符串（snake_case）**；Java/TS 用 `UPPER_SNAKE_CASE` 常量。
> - 本文词汇一经冻结，新增术语必须先在此登记再使用。

---

## 命名约定速查（跨层统一映射）

| 层 | 约定 | 示例（以"部门空间"为例） |
| --- | --- | --- |
| 数据库表名 | snake_case 复数 | `department_space` |
| 数据库字段 | snake_case 单数 | `space_name` |
| Java Entity 类 | PascalCase | `DepartmentSpace` |
| Java Service / Mapper | 后缀约定 | `DepartmentSpaceService` / `DepartmentSpaceMapper` |
| TypeScript 类型/接口 | PascalCase | `DepartmentSpace` |
| TypeScript 状态/常量 | UPPER_SNAKE_CASE | `SPACE_STATUS_ACTIVE` |
| Vue 3 组件文件 | PascalCase | `DepartmentSpaceList.vue` |
| API 路由 | kebab-case / 资源复数 | `GET /department-spaces` |

---

## 1. 核心领域实体（Domain Entities）

| 中文领域概念 | 统一英文标识符 | 对应代码 / 数据库命名约定 | 业务含义与边界定义 |
| --- | --- | --- | --- |
| 用户 | **User** | DB `user_user`(表) / Java `User` / TS `User` / Vue `UserList.vue` | 平台登录主体（员工）。含基础身份与账号状态，不承载组织权限（权限见 Membership / Role）。 |
| 部门空间 | **DepartmentSpace** | DB `department_space` / Java `DepartmentSpace` / TS `DepartmentSpace` / Vue `DepartmentSpaceList.vue` | 文档归属与权限的顶层边界容器，四级结构分界的最小单位；承载配额、命名、目录与成员管理。 |
| 空间成员关系 | **SpaceMembership** | DB `space_membership` / Java `SpaceMembership` / TS `SpaceMembership` | 用户 × 部门空间的关联，绑定 Role（见枚举）；决定用户在该空间的角色权限。 |
| 目录 / 文件夹 | **Folder** | DB `folder` / Java `Folder` / TS `Folder` / Vue `FolderTree.vue` | 部门空间内的文档组织单元；是权限继承的来源与归档目标位置。 |
| 文档 | **Document** | DB `document` / Java `Document` / TS `Document` / Vue `DocumentList.vue` | 管理的核心对象，包含元数据与状态（见状态枚举）；内容数据随版本承载。 |
| 文档版本 | **DocumentVersion** | DB `document_version` / Java `DocumentVersion` / TS `DocumentVersion` | 文档的不可变快照，持有文件内容 / 存储指针、{版本号、修改人、修改时间}。同名上传产生新版本而非覆盖。 |
| 文件存储对象 | **FileObject** | DB `file_object` / Java `FileObject` / TS `FileObject` | 物理存储层对象（≤100MB，支持分片上传），被 DocumentVersion 引用；管理分片与断点续传。 |
| 角色定义 | **Role** | DB `role_def` / Java `Role(DEF)` / TS `RoleDef` / Vue `RoleSelect.vue` | 全局角色抽象模板（Viewer/Editor/Admin），见枚举；角色实例归属空间经 SpaceMembership 绑定。 |
| 合成任务 | **SynthesisTask** | DB `synthesis_task` / Java `SynthesisTask` / TS `SynthesisTask` / Vue `SynthesisProgress.vue` | AI 合成的一次异步执行单元；带状态（进行/完成/失败/终止），关联来源文档集与产物。 |
| 引用来源 | **Citation** | DB `citation` / Java `Citation` / TS `Citation` / Vue `CitationList.vue` | AI 合成观点 → 源文档片段的溯源映射；支撑溯源校验，可带有“无来源”标记状态。 |
| 审计日志条目 | **AuditLog** | DB `audit_log` / Java `AuditLog` / TS `AuditLog` / Vue `AuditLogList.vue` | 登录/访问/上传/编辑/回收/AI 入库等动作的系统留痕，字段{主体、动作、对象、时间、结果}。 |
| 访问记录 | **AccessLog** | DB `access_log` / Java `AccessLog` / TS `AccessLog` | 部门级文档访问行为明细（用户、时间、动作、文档），供部门管理员治理核验。 |
| 存储配额 | **QuotaConfig** | DB `quota_config` / Java `QuotaConfig` / TS `QuotaConfig` / Vue `QuotaSetting.vue` | 部门 / 全局的存储配额与阈值配置；遵循“部门之和≤全局上限”约束。 |
| 告警 | **AlertRecord** | DB `alert_record` / Java `AlertRecord` / TS `AlertRecord` / Vue `AlertList.vue` | 配额超阈 / 异常行为产生的通知记录；同级别同态不重复，支持标记已处理。 |
| 收藏 | **Favorite** | DB `favorite` / Java `Favorite` / TS `Favorite` / Vue `FavoriteList.vue` | 用户对文档的星标关系（集合）快捷入口；越权文档不写入。 |

---

## 2. 核心领域枚举（Domain Enums）

| 中文领域概念 | 统一英文标识符（枚举） | 对应代码 / 数据库命名约定 | 业务含义与边界定义 |
| --- | --- | --- | --- |
| 角色类型 | **RoleType** | DB 存 `space_role`(string) / Java `RoleType` / TS `RoleType` | 取值：`VIEWER`(查看者)、`EDITOR`(编辑者)、`ADMIN`(管理员)。决定空间内读写/管理能力。 |
| 文档状态 | **DocumentStatus** | DB `document_status` / Java `DocumentStatus` / TS `DocumentStatus` | `UPLOADED`(已上传)、`PENDING_REVIEW`(待审)、`CONFIRMED`(已入库)、`ARCHIVED`(已归档)、`DELETED`(回收站)；AI 生成中为任务态 `AI_GENERATING` 见 SynthesisTaskStatus。 |
| AI 合成任务状态 | **SynthesisTaskStatus** | DB `task_status` / Java `SynthesisTaskStatus` / TS `SynthesisTaskStatus` | `RUNNING`(运行中)、`COMPLETED`(完成)、`FAILED`(失败)、`CANCELLED`(已取消)；完成后产出进入 PENDING_REVIEW。 |
| 引用溯源状态 | **CitationStatus** | DB `citation_status` / Java `CitationStatus` / TS `CitationStatus` | `VALID`(可溯源)、`UNTRACEABLE`(无来源/需人工)、`SOURCE_INVALID`(源文档已失效)。 |
| 合成类型 | **SynthesisType** | DB `synthesis_type` / Java `SynthesisType` / TS `SynthesisType` | `WEEKLY_REPORT`(周报汇总)、`PROPOSAL_DRAFT`(方案初稿)、`POLICY_SUMMARY`(制度摘要) 等，可扩展。 |
| 删除类型 | **DeleteKind** | DB `delete_kind` / Java `DeleteKind` / TS `DeleteKind` | `SOFT`(软删除→回收站)、`HARD`(硬删除，清空回收站后不可恢复)。 |
| 会员参与度状态 | **MembershipStatus** | DB `membership_status` / Java `MembershipStatus` / TS `MembershipStatus` | 成员在某空间的状态：`ACTIVE`(有效)、`REVOKED`(已回收/停用)。 |
| 审计动作类型 | **AuditAction** | DB `audit_action` / Java `AuditAction` / TS `AuditAction` | `LOGIN`、`ACCESS`、`UPLOAD`、`EDIT`、`VERSION_RESTORE`、`DELETE`、`RECYCLE`、`AI_CONFIRM`、`AI_ARCHIVE`、`PERMISSION_CHANGE` 等。 |
| 告警级别 | **AlertLevel** | DB `alert_level` / Java `AlertLevel` / TS `AlertLevel` | `INFO`、`WARN`(≥80% 阈值)、`ERROR`(超限)。 |
| 操作结果 | **OperationResult** | DB `operation_result` / Java `OperationResult` / TS `OperationResult` | `SUCCESS`(200/201)、`FORBIDDEN`(403)、`BAD_REQUEST`(400)、`VALIDATION_ERROR`(422)。统一供自动化断言复用。 |

---

## 3. 业务操作动词（Domain Actions）

> 对应后端 Service 方法 / 前端操作 / 审计动作词，动词统一，禁止同义替换（如不得用 `createFolder` 与 `newDir` 混用）。

| 中文领域概念 | 统一英文标识符（动词） | 对应代码 / 命名约定 | 业务含义与边界定义 |
| --- | --- | --- | --- |
| 上传文档 | **uploadDocument** | Java `uploadDocument` / TS `uploadDocument` / 审计 `UPLOAD` | 校验配额+权限+格式后写入，同名触发版本递增。 |
| 查看 / 预览文档 | **previewDocument** | `previewDocument` / `DOCUMENT_PREVIEW` | 在线预览（服务端转码或降级下载）；越权返回 403。 |
| 检索文档 | **searchDocument** | `searchDocument` / `DOCUMENT_SEARCH` | 自然语言语义检索，结果服务端裁剪到有权集合。 |
| 发起合成 | **startSynthesis** | `startSynthesis` / `AI_SYNTHESIS_START` | 创建 SynthesisTask；无来源/全越权则终止。 |
| 校验引用溯源 | **verifyCitation** | `verifyCitation` / `CITATION_VERIFY` | 服务端强制：逐观点映射源文档，判定 CitationStatus。 |
| 人工确认入库 | **confirmDocument** | `confirmDocument` / `AI_CONFIRM` | 履行“溯源校验通过+显式确认”双门槛，`PENDING_REVIEW→CONFIRMED`。 |
| 归档文档 | **archiveDocument** | `archiveDocument` / `DOCUMENT_ARCHIVE` | 移动至归档目录并继承目标目录权限；无权限则 403。 |
| 还原版本 | **restoreVersion** | `restoreVersion` / `VERSION_RESTORE` | Editor/Admin 将文档还原到某历史版本；Viewer 仅预览。 |
| 移动 / 归类文档 | **moveDocument** | `moveDocument` / `DOCUMENT_MOVE` | 跨目录移动，冲突需确认；权限继承目标目录。 |
| 软删除 | **softDelete** | `softDelete` / `DOCUMENT_DELETE` | 移入回收站（DELETED 态），可恢复。 |
| 恢复回收站 | **restoreFromRecycle** | `restoreFromRecycle` / `RECYCLE_RESTORE` | `DELETED→UPLOADED`；硬删除后不可恢复。 |
| 硬删除 | **hardDelete** | `hardDelete` / `RECYCLE_CLEAR` | 清空回收站，不可恢复（幂等）。 |
| 分配角色 | **assignRole** | `assignRole` / `PERMISSION_ASSIGN` | 为成员绑定某空间角色；Editor 不可自升 Admin。 |
| 回收权限 | **revokePermission** | `revokePermission` / `PERMISSION_REVOKE` | 回收成员权限，即时生效，返回 403；守护“保留至少一名 Admin”。 |
| 设定配额 | **setQuota** | `setQuota` / `QUOTA_SET` | 配置部门/全局配额，校验“新额度≥当前用量”“部门之和≤全局上限”。 |
| 处置异常 | **handleAnomaly** | `handleAnomaly` / `ANOMALY_HANDLE` | 定位并阻断异常访问/操作（回收权限、拉黑）；幂等。 |
| 导出日志 | **exportAuditLog** | `exportAuditLog` / `AUDIT_EXPORT` | 导出审计日志用于合规留存；校验导出上限。 |

---

## 4. 命名一致性校验清单（使用前必读）

以下为跨层落地时的硬性约束，防止命名幻觉：

- 同一实体在 DB / Java / TS / Vue 中语义**必须**严格同义，禁止出现“同物异名”（如 `Space` vs `DepartmentSpace`）。
- 枚举的数据库字符串值与 Java/TS 常量名保持一致（同上速查表中约定），避免大小写/下划线换算歧义。
- API 资源路由以实体复数 kebab-case 为准，操作动词不写入路由（动词通过 HTTP method 表达）。
- 文件组件命名：列表组件 `XxxList.vue`、表单 `XxxForm.vue`、设置 `XxxSetting.vue`，与实体复数/单数保持一致。
- 凡新增领域词汇，先在本文件登记审批后再进入编码，避免扩散性术语漂移。