# DocHub 项目模块划分总纲（MODULES.md）

> 本文件是从需求冻结文档到"可分模块、可追踪、可开发"任务计划的**枢纽**。
> 依据 AGENTS.md 铁律与 docs/01-requirements 下已冻结的 PRD / USER_STORIES / GLOSSARY 拆分。
> 目标：把项目拆为两大模块，每模块内含周期化任务清单，供后续逐任务开发，**性能与并发质量优先**。

---

## 1. 模块划分原则

1. **边界清晰**：以"业务闭环 + 权限边界"为界，互不串扰，降低耦合。
2. **任务粒度可验收**：每条任务满足"可独立开发/自测/AC 可判定"，符合 DoD（零编译错、Performance & Concurrency 军规）。
3. **命名统一**：所有字段/实体/接口命名 100% 对齐 `docs/01-requirements/GLOSSARY.md`，禁止脑补字段。
4. **性能优先**：所有查询/更新遵循 AGENTS.md §6 性能与并发军规（Specification、@Version、批量、DTO 投影、防全表加载）。

---

## 2. 两大模块总览

| 模块 | 代码目录 | 领域 | 核心实体 | 主要职责 |
|------|---------|------|----------|---------|
| **M1 用户管理（登录）模块** | `backend/.../user` + `auth` | 身份与授权 | User、Role、Permission、UserRole、UserPermission、Dept | 注册/登录/鉴权/JWT、用户CRUD、权限分配、部门、RBAC 权限矩阵、审计登录 |
| **M2 文档管理（业务）模块** | `backend/.../doc` | 文档闭环 | DepartmentSpace、SpaceMembership、Document、DocumentVersion、FileObject、SynthesisTask、Citation | 上传/预览/检索/AI合成/引用溯源/人工确认/归档/回收站、配额、审计、环形权限 |

> 二者通过**共享 AuthContext（当前登录用户）**与**RBAC 权限点**正交，文档模块不再重复实现鉴权。

---

## 3. 与已冻结需求文档的映射

| 模块 | 承接的 PRD 章节 | 承接的 USER_STORIES 分组 | 承接的 GLOSSARY 实体 |
|------|---------------|--------------------------|---------------------|
| M1 | §2 RBAC 权限矩阵 | S1-S?（用户/权限/登录相关） | User、Role、Permission、Dept、RoleType |
| M2 | §3 生命周期状态机、M1-M7 功能模块 | S?（文档/检索/合成相关） | Document、DocumentVersion、FileObject、SynthesisTask、Citation、**AuditLog、AlarmRecord、QuotaConfig** |

---

## 4. 公共正交能力（两模块共享，见独立文档）

| 能力 | 归属 | 文档 |
|------|------|------|
| 统一鉴权、AuthContext、鉴权切面 | M1 提供，M2 复用 | `02-common/COMMON_PERMISSION_AUDIT.md` |
| 操作审计日志 AuditLog | 两模块 | 同上 |
| 全局返回结构/异常/分页 | 基建 | 同上 |
| 性能与并发军规 | 全项目 | AGENTS.md §6 |

---

## 5. 周期化任务看板（两份模块级任务文档）

- **M1 用户管理模块**：`docs/modules/M1-user/README.md`（周期1-2 任务清单）
- **M2 文档管理模块**：`docs/modules/M2-doc/README.md`（周期1-5 任务清单）

每份任务文档统一结构：
1. 模块目标与边界（对接 PRD/GLOSSARY）
2. 已建表/待建表清单（对齐 schema.sql）
3. 周期化任务（T-xx），每条含：功能点、涉及文件、验收点(Ac)、遵循的性能军规
4. 测试与验证命令

---

## 6. 开发优先级建议

1. **先 M1 后 M2**：M1 提供鉴权底座，M2 的二次鉴权/环形权限依赖它。
2. **M1 内**：表格落地 → 登录/JWT → 用户CRUD → 权限分配 → 部门。
3. **M2 内**：空间/目录 → 上传/版本 → 检索 → AI 合成 -> 确认归档 → 回收站 → 治理/配额 → 系统管理。

> 详细任务清单见 `docs/modules/M1-user/README.md` 与 `docs/modules/M2-doc/README.md`。