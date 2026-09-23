# DocHub · 智能企业文档管理平台

> 让每一份文档，都有归处、有脉络、可被唤醒。

DocHub 是一个前后端分离的企业级文档管理平台，提供部门空间沉淀、检索与内容合成能力。前端基于 Vue 3 + TypeScript + Vite，后端基于 Spring Boot 4 + JPA + MySQL + Redis，并内置了完整的认证安全体系与 RBAC 权限控制。

---

## ✨ 核心特性

- **双 Token 认证**：短效 Access Token + 长效 Refresh Token，Refresh 自动轮换，Redis 会话管理，支持踢人下线
- **登录防爆破**：按账号/IP 失败计数锁定，IP 级固定窗口限流，可选图形验证码
- **RSA 密码加密传输**：登录密码经 Web Crypto 公钥加密，私钥由环境变量注入；未配置时自动降级明文
- **RBAC 权限控制**：`@RequirePermission` 注解 + AOP 切面，Spring Security + BCrypt，权限变更即时生效
- **Redis 降级策略**：Redis 不可用时自动降级为直查库，不阻塞核心流程
- **前后端契约对齐**：TS Interface 与 Java DTO/VO 镜像对齐，强类型贯穿全链路

---

## 🧱 技术栈

| 层 | 技术 |
| --- | --- |
| 前端 | Vue 3 (Composition API, `<script setup lang="ts">`) · Vite 6 · TypeScript (Strict) · Tailwind CSS · Vue Router · Pinia · Axios |
| 后端 | Java 17 · Spring Boot 4.1.1 · Spring Data JPA · Spring Security · Redis · jjwt 0.11.5 · Lombok |
| 数据 | MySQL 8.0+ (InnoDB, `utf8mb4_unicode_ci`) · Redis 7.0+ |

---

## 📁 工程结构

```text
WenDang/
├── docs/                 # 文档：需求、设计、QA 审查
│   ├── 01-requirements/  # 用户故事、PRD、领域词汇表
│   ├── 02-design/        # 架构、UI 规范、API 契约、环境变量说明
│   └── 03-qa-review/     # 测试清单与代码审查报告
├── sql/
│   └── schema.sql        # 数据库物理 DDL
├── prompts/              # AI 阶段指令卡片库
├── tasks.md              # 规范驱动的原子任务看板
├── frontend/             # Vue 3 + TS 前端工程
│   └── src/
│       ├── api/          # 强类型 Axios 请求封装
│       ├── types/        # TS 接口契约定义
│       ├── components/   # 可复用原子 UI 组件
│       ├── views/        # 路由页面 (Login/Dashboard/...)
│       ├── stores/       # Pinia 状态管理
│       └── utils/        # 格式化与通用工具
└── backend/              # Spring Boot 后端工程
    └── src/main/java/com/example/backend/
        ├── common/       # 全局 Result<T> 与异常处理
        ├── controller/   # 控制层（路由与参数校验）
        ├── service/impl/ # 核心业务与事务
        ├── repository/   # Spring Data JPA
        ├── entity/       # JPA 持久化实体
        ├── dto/          # 请求入参（@Valid 校验）
        ├── vo/           # 响应出参（数据脱敏）
        ├── auth/         # 认证：登录/刷新/登出/验证码
        └── security/     # JWT、会话、防爆破、RSA、权限切面
```

---

## 🚀 快速启动

### 前置条件
- JDK 17+ · Maven 3.8+（或使用 `./mvnw`）
- Node.js 18+ · pnpm
- MySQL 8.0+ · Redis 7.0+（Redis 可选，缺省时后端自动降级）

### 1. 准备数据库

执行建表脚本：

```bash
mysql -uroot -p < sql/schema.sql
```

### 2. 配置后端

本地开发基于 `application-local.example.yml` 模板创建 `application-local.yml`（已 gitignore，不会提交）：

```bash
cd backend
cp src/main/resources/application-local.example.yml src/main/resources/application-local.yml
```

视本机环境调整 `application-local.yml` 中的数据库账号密码，必要时覆盖环境变量。

### 3. 启动后端

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,local
# 或
mvn spring-boot:run -Dspring-boot.run.profiles=dev,local
```

后端默认端口 `10086`，接口前缀 `/backend`。

### 4. 启动前端

```bash
cd frontend
pnpm install
pnpm dev
```

前端默认端口 `5173`，已通过 Vite 代理将 `/backend` 转发至后端 `:10086`。

访问 **http://localhost:5173/** 即可。

---

## 🔐 环境变量

| 变量 | 必要值 / 作用 | 必填 |
| --- | --- | --- |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | MySQL 连接（dev 默认 `localhost:3306/dochub`） | 生产必填 |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号（dev 默认 `root/123456`） | 生产必填 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Redis 连接 | 生产必填 |
| `JWT_SECRET` | JWT 签名密钥，**需 ≥32 字节** | 生产必填 |
| `APP_RSA_PRIVATE_KEY` | RSA PKCS#8 私钥（Base64），用于登录密码加密；留空则降级明文 | 启用加密时必填 |

> 生产（`application-prod.yml`）无任何明文回退，`JWT_SECRET`、数据库凭据必须通过环境变量注入，否则启动失败。
> 详细说明与密钥生成方法见 [docs/02-design/ENV_CONFIG.md](docs/02-design/ENV_CONFIG.md)。

---

## 🛠️ 常用命令

```bash
# 后端编译与测试
cd backend && ./mvnw clean compile test

# 前端类型检查
cd frontend && npx vue-tsc -b

# 前端代码检查与修复
cd frontend && pnpm run lint
```

---

## 📖 文档索引

- 需求与领域词汇表：`docs/01-requirements/`
- 架构/UI/API 契约：`docs/02-design/`
- 环境变量配置说明：[docs/02-design/ENV_CONFIG.md](docs/02-design/ENV_CONFIG.md)
- 测试与代码审查：`docs/03-qa-review/`
- 数据库 DDL：[sql/schema.sql](sql/schema.sql)
- 任务看板：[tasks.md](tasks.md)

---

## 📄 License

内部项目。