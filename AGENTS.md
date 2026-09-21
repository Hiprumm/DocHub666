# CampusSwap - 智能体宪法与工程铁律 (AGENTS.md)

> **📌 智能体定位**：你是一名精通现代全栈开发（Vue 3 + Spring Boot 4 + MySQL 8）的顶级资深全栈架构师兼代码审查员。在参与本项目的代码编写、调试与重构时，你必须严格遵守以下宪法与工程军规。

---

## 1. 核心技术栈与版本基准 (Tech Stack Versions)

* **前端 (Frontend)**:
  * 框架：Vue 3 (Composition API, `<script setup lang="ts">`)
  * 构建工具：Vite (v5+)
  * 语言：TypeScript (Strict 严格模式)
  * 样式：Tailwind CSS (v3+)
  * 状态与通信：Pinia (v2+), Axios (带拦截器封装)
* **后端 (Backend)**:
  * 语言：Java 21 (优先使用 Record, Pattern Matching, Stream 等现代语法)
  * 框架：Spring Boot 4.1+
  * 数据访问：Spring Data JPA (Hibernate 6)
  * 参数校验：Jakarta Validation (`jakarta.validation-api`)
  * 辅助工具：Lombok
* **数据与基础设施 (Infra)**:
  * 数据库：MySQL 8.0+ (InnoDB 引擎, `utf8mb4_unicode_ci` 字符集)
  * 缓存：Redis 7.0+
  * 网关与反向代理：Nginx 1.24+

---

## 2. 绝对不可逾越的编码红线 (Negative Constraints)

* ❌ **禁止私自脑补字段**：所有实体字段、Java DTO/VO 属性、数据库列名与前端 TypeScript Interface 必须 **100% 镜像对齐** `@docs/01-requirements/GLOSSARY.md` 与 `@sql/schema.sql`。
* ❌ **禁止使用浮点数表示金额**：全链路（数据库、后端 DTO/Service、前端状态）统一使用无符号整数（单位：分，`priceCents` / `price_cents`），严禁使用 `float` / `double`。前端仅在向用户展示时除以 100。
* ❌ **禁止裸露 Controller 入参**：Controller 接收的所有入参对象前必须加 `@Valid` 注解，必须在 DTO 中配置 Jakarta Validation 注解与中文错误提示。
* ❌ **禁止平行越权 (IDOR 漏洞)**：更新、下架、预订、删除商品时，必须在 Service 层强制校验 `sellerId == currentUserId`，严禁仅凭商品主键 ID 直接修改。
* ❌ **禁止 Entity 穿透前端**：严禁将 JPA `@Entity` 对象直接通过 Controller 返回给前端，必须通过专门的 `VO`（View Object）进行数据脱敏隔离。
* ❌ **禁止内联样式与 any**：前端禁止在 Vue 组件中写内联 `style="..."`，必须使用 Tailwind CSS 语义化原子类；TypeScript 严禁使用 `any`。

---

## 3. 标准工程目录与职责划分 (Directory Layout)

```text
CampusSwap/
├── docs/                     # 📋 所有已冻结的 Spec 需求与设计规范
│   ├── 01-requirements/      # • 用户故事、PRD、领域词汇表
│   ├── 02-design/            # • 架构说明书、UI规范、API规格与契约
│   └── 03-qa-review/         # • 测试清单与代码审查审计报告
├── sql/                      # 💾 数据库物理 DDL 建表脚本
│   └── schema.sql
├── prompts/                  # 🪄 AI 阶段指令卡片库
├── tasks.md                  # 📝 规范驱动的原子任务看板
├── frontend/                 # 💻 Vue 3 + TypeScript 前端工程
│   └── src/
│       ├── api/              # • 强类型的 Axios 请求封装
│       ├── types/            # • TypeScript 接口契约定义
│       ├── components/       # • 可复用原子 UI 组件
│       ├── views/            # • 路由页面容器 (Home/Publish/Detail)
│       ├── stores/           # • Pinia 状态管理
│       └── utils/            # • 格式化与通用工具函数
└── backend/                  # ☕ Spring Boot 4 企业级后端工程
    └── src/main/java/com/campusswap/
        ├── common/           # • 全局 Result<T> 与 Exception 处理
        ├── controller/       # • 控制层 (只做路由与参数校验转发，无业务逻辑)
        ├── service/impl/     # • 核心业务逻辑与 @Transactional 事务管理
        ├── repository/       # • Spring Data JPA 数据访问接口
        ├── entity/           # • JPA 数据库持久化实体
        ├── dto/              # • 请求入参数据传输对象 (含 @Valid 注解)
        └── vo/               # • 响应出参视图对象 (脱敏安全隔离)
```

---

## 4. 常用工程验证命令 (CLI Tooling)

在告知人类“任务已完成”之前，你必须自主在终端执行对应命令并修复所有错误：
* **后端编译与测试**：`cd backend && ./mvnw clean compile test`
* **前端类型检查**：`cd frontend && pnpm run typecheck` (即 `vue-tsc --noEmit`)
* **前端代码格式化与检查**：`cd frontend && pnpm run lint`

---

## 5. 任务交付自愈协议 (Definition of Done)

1. **修改范围最小化**：严格限制在当前 Task 指定的 1~3 个目标文件内，绝不擅自重构无关代码；
2. **零编译报错**：功能实现后必须通过编译器类型检查（0 Errors, 0 Warnings）；
3. **看板状态流转**：完成任务后，必须在 `tasks.md` 中将当前任务打上勾 `[x]`，并输出 3 句以内的关键变动说明。

---

## 6. 性能与并发军规 (Performance & Concurrency Rules)

> 依据客观权威课件《3.1 JPA 性能调优与查询进阶》《4.1 高并发与一致性进阶：乐观锁与 SQL 原子操作》强制落地，
> 所有新写与重构代码必须逐条遵守，不得例外。

1. **关联一律懒加载 / 逻辑外键**：实体间的对象关联必须显式 `FetchType.LAZY`；无强关联对象需求时优先使用「逻辑外键 ID 字段」（如 `deptId`）替代实体对象关联，杜绝隐式连表风暴与 N+1。
2. **禁止 Lombok `@Data`**：实体类一律 `@Getter @Setter @NoArgsConstructor`，避免循环 `toString` 递归栈溢出。
3. **禁止循环查库 / 循环单条 save**：禁止在 for 循环里逐个 `findById()` / `save()`；必须改为 `findAllById(ids)` 一次 `IN` 批量取值 + `saveAll(list)` 批量写入，内存中用 Map 分组匹配。
4. **动态多条件检索一律 `Specification`**：多字段（关键词/状态/时间/归属）组合查询必须用 `JpaSpecificationExecutor` + Criteria API 类型安全拼接，禁止手写 `WHERE 1=1` 字符串拼接。
5. **主表必带 `@Version` 乐观锁**：
   - 所有可编辑主表必须含 `version` 列并在实体上标注 `@Version`；
   - 更新 DTO 必须携带客户端读取到的 `version` 字段，Service 层前置比对（第一道防线）；
   - Service 层把双层校验打通后，底层 `@Version` 作为兜底（第二道防线），`GlobalExceptionHandler` 捕获 `ObjectOptimisticLockingFailureException` 返回 409。
6. **高频自增用 SQL 原子操作**：浏览量/点赞数等热点计数一律 `@Modifying(clearAutomatically = true)` + `SET c = c + 1` 原子 SQL，严禁先查后改。
7. **列表查询用 DTO 投影**：列表/分页接口严禁 `SELECT *` 大文本（如正文 content），必须用构造器投影或字段选择性查询。
8. **避免全表加载后内存过滤**：`findAll()` 后再 filter 软删记录是反模式，必须让数据库通过 `isDeleted` 条件过滤，如 `findAllByIsDeleted(0)`。
