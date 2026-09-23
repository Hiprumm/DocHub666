# DocHub 大数据量性能演进路线图 (PERF_ROADMAP)

> 适用范围：d:\WorkSpace\Java\WenDang（DocHub 文档管理平台）
> 口径基准：对照课件 4.1 / 5.1 / 6.1 / 7.1 / 7.2 / 7.3 与本项目实际实现
> 更新日期：2026-09-23

---

## 1. 目标与原则

本路线图面向 **数据量大** 的分阶段性能改造，回答"哪一层会先被撑爆、按什么顺序加固"。
排序逻辑：**数据库 QPS/存储 → 主从同步与内存 → 分布式一致性与水平扩展**。

### 强制前提（任何改动必须遵守）
- 一律走 `var(--token-*)` 无关（后端无此约束），后端约束见 AGENTS.md：**禁循环查库、禁 findAll 内存过滤、列表用 DTO 投影、Specification 拼条件、主表 `@Version`、高频计数 SQL 原子操作**。
- 金额一律整数分、列表投影选择性查询 —— 均为既有铁律，本次改造不破坏。
- 改动最小化，不提前引入无真实载体的抽象。

---

## 2. 现状对标（2026-09-23 实测）

### 已达成 ✅
| 能力 | 落点 |
|---|---|
| JWT 双 token + Redis 会话管理（主动登出/多端互踢） | `security/JwtUtil` `security/SessionManager` |
| JWT 过滤器 + AuthContext 注入 | `security/JwtAuthFilter` |
| RBAC：用户/角色/权限并集缓存 Redis `auth:perm:{userId}`（30min TTL，Redis 降级直查库） | `security/PermissionAspect` |
| 乐观锁 `@Version`（BaseEntity）+ DTO 前置比对 + 409 兜底 | `entity/BaseEntity` `common/GlobalExceptionHandler` |
| 列表页 Specification+JPA 分页，无 findAll 内存过滤（SysUser 实测） | `service/impl/SysUserServiceImpl#page` |
| 关联集合批量 IN 查询（禁循环单查） | `SysUserServiceImpl.fillRelations` |
| 登录防爆破 + 验证码 + RSA | `security/LoginBruteforceGuard` 等 |

### 未达成（待补）⭐⭐
| # | 能力 | 现状 | 优先级 |
|---|---|---|---|
| 1 | **业务数据 Cache-Aside**（先查缓存→未命中回源→写后删+TTL） | Redis 仅用于认证/权限缓存，**无业务数据缓存** | 🔴 P0 |
| 2 | **高频计数 SQL 原子操作**（`SET c=c+1`） | 无浏览量/热度实体载体 | 🔴 P0（待载体） |
| 3 | 缓存三兄弟防护（穿透/雪崩/击穿） | 未实现 | 🟠 P1 |
| 4 | 深度分页优化（游标/keyset） | 当前 offset 分页，大数据量后尾页变慢 | 🟠 P1 |
| 5 | DTO 投影全面落地 | 部分实体存在大文本字段风险待查 | 🟡 P2 |
| 6 | 双级缓存 JetCache(Caffeine+Redis)+Pub/Sub | 未引入 | 🟡 P2（多实例才需） |
| 7 | 读写分离 / 分库分表 / Redis 集群 | 未做，纯架构话题 | 🟡 P2 |

### 关键事实
本仓库当前业务实体为**系统管理模块**（SysUser/SysRole/SysDept/SysLog），**尚无文档内容实体**（无 DocDocument、无浏览量/热度计数字段）。故：
- Cache-Aside、原子计数、内容缓存等改造 **目前没有真实数据载体**；
- 唯一可立即落地的 P0/P1 项，是围绕 **已存在的列表/分页** 与 **通用缓存工具** 展开的基建。

---

## 3. 分阶段实施清单

### 🔴 P0 —— 数据量上来之前必须做

**3.1 业务数据 Cache-Aside 通用基建（可立即落地，无业务载体依赖）**
- 新增通用缓存访问封装 `CacheAsideService`（`config/redis` 或 `common`）：
  - `get(k, type, loader, ttl)`：缓存命中→回源→回填；
  - `invalidate(k)`：写后删缓存；
  - 统一 key 前缀，避免与 `auth:token:*`/`auth:perm:*` 冲突；
  - Redis 不可用时透明降级为直接查库（与 PermissionAspect 一致）。
- 说明：真实业务数据缓存待文档实体出现后，用该工具接入即可。

**3.2 高频计数原子化（预留 API，待实体载体）**
- 提供 Repo 原子递增模式样板（`@Modifying + @Query SET c = c + 1`），文档实体出现后直接套用；
- 明确禁止对热点字段"先查后改"。

### 🟠 P1 —— 中等规模（十万~百万行）时落地

**3.3 深度分页优化**
- 翻页接口（用户/日志等高频表）在数据量大后从 `offset` 分页平滑迁移至 **游标分页**（`WHERE id < lastId`）；
- 保留首屏 offset，深层页切 keyset。

**3.4 缓存三兄弟防护**
- 缓存穿透：空值短 TTL 缓存 / 布隆过滤器；
- 缓存雪崩：TTL 加随机能乱；
- 缓存击穿：加锁回源（可复用 CacheAside 的水库 singleflight）。

**3.5 索引与慢查询治理**
- 审核 schema.sql 所有表 KEY/INDEX，补齐外键/`isDeleted`/`createTime` 常用查询组合索引；
- 禁止对大文本字段 `SELECT *`、禁止 `LIKE %xx%` 无索引高频查询。

### 🟡 P2 —— 千万级+ / 多实例时才需要

**3.6 双级缓存 JetCache (Caffeine + Redis)**
- 触发条件：**多实例部署 + 单实例 Redis 命中也要跨网络 + 热点极高**；
- 落地：pom 引入 jetcache，L1 Caffeine + L2 Redis，`@Cached(cacheType=BOTH)`，`@CacheInvalidate` 双清，L2 淘汰 + Pub/Sub 广播 L1 失效；
- 明确：**单实例阶段引入属于过度设计，暂缓**。

**3.7 读写分离 / 分库分表 / Redis 集群**
- 纯架构话题，数据量与连接数双双告警时才进入，超出课件范围，仅记录。

---

## 4. 验收标准（Definition of Done）
- 每完成一项：后端 `mvnw clean compile` 通过，前端 `vue-tsc --noEmit` 通过（如涉及）；
- tasks.md 对应项打勾 `[x]`，输出 ≤3 句关键变动；
- 不引入无真实载体的抽象、不重构无关代码。

## 5. 与既有铁律的衔接
本路线图严格承接 AGENTS.md 第 6 节（性能与并发军规）：投影、Specification、`@Version`、原子 SQL、Redis 降级等均为既有约定的落地与查漏，非新增另行体系。