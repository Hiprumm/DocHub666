# DocHub 环境变量配置说明（ENV_CONFIG）

> 本文件汇总 DocHub 后端全部通过环境变量（`${ENV:默认}`）注入的配置项。
> 设计原则：**敏感凭据一律环境变量化，禁止硬编码进源码或提交进 Git**
> （`application-local.yml` 等含真实凭据的文件已由 `.gitignore` 忽略）。

---

## 1. 环境变量总表

| 环境变量 | 必填 | 默认值 | 说明 | 使用位置 |
| --- | --- | --- | --- | --- |
| `SERVER_PORT` | 否 | `10086` | 应用服务端口 | `application-local.yml` |
| `DB_HOST` | 否 | `localhost` | MySQL 主机 | `application-dev.yml` / local |
| `DB_PORT` | 否 | `3306` | MySQL 端口 | `application-dev.yml` / local |
| `DB_NAME` | 否 | `dochub` | 数据库名 | `application-dev.yml` / local |
| `DB_USERNAME` | 是* | `root` | 数据库账号 | `application-dev.yml` / local |
| `DB_PASSWORD` | 是 | `123456`(dev) | 数据库密码 | `application-dev.yml` / local |
| `SHOW_SQL` | 否 | `true` | 是否打印 JPA SQL | `application-local.yml` |
| `DDL_AUTO` | 否 | `update` | Hibernate DDL 策略 | `application-local.yml` |
| `REDIS_HOST` | 否 | `localhost` | Redis 主机 | `application-dev.yml` / local |
| `REDIS_PORT` | 否 | `6379` | Redis 端口 | `application-dev.yml` / local |
| `REDIS_PASSWORD` | 否 | 空 | Redis 密码（无鉴权可留空） | `application-dev.yml` / local |
| `JWT_SECRET` | **prod 强制** | dev 有默认 | JWT 签名密钥（HS256，**≥32 字节**） | `application.yml` / prod |
| `APP_RSA_PRIVATE_KEY` | prod 建议 | 空 | RSA 私钥（PKCS#8 Base64，用于登录密码加密） | `application.yml` / prod |

> `*`：`DB_USERNAME/DB_PASSWORD` 在生产严格无默认回退，缺失将启动失败；dev 仅作本地兜底。

---

## 2. 敏感项重点说明

### 2.1 JWT 签名密钥（`JWT_SECRET`）
- 生产环境**必须显式通过环境变量注入，禁止回退默认值**（`application.yml` 的默认值仅限本地调试）。
- **长度必须 ≥ 32 字节**，否则 [JwtUtil.java](../backend/src/main/java/com/example/backend/security/JwtUtil.java) 启动即抛 `IllegalArgumentException`。
- 生成示例：
  ```powershell
  # Windows
  $env:JWT_SECRET = (ConvertTo-SecureString -AsPlainText (New-Guid).ToString() -Force | ForEach-Object { (New-Guid).ToString() })
  # 或直接手工指定 ≥32 字符随机串
  $env:JWT_SECRET = 'R3pL4c3-With-A-Long-Random-Secret-String-At-Least-32-Byte__'
  ```

### 2.2 RSA 私钥（`APP_RSA_PRIVATE_KEY`）
- 用于登录密码**公钥加密 → 后端私钥解密**（`RSA/ECB/OAEPWithSHA-256AndMGF1Padding`）。
- 值为 **PKCS#8 私钥的 Base64 体**（PEM 去掉头尾标签行后那一整段）。
- **留空则自动降级为明文传输**（能力降级，不影响登录主流程）。
- 生成一对密钥（PowerShell + .NET，生成 `JWT` 风格 PKCS8 Base64）：
  ```powershell
  $rsa = [System.Security.Cryptography.RSA]::Create(2048)
  $priv = $rsa.ExportPkcs8PrivateKey()
  $pub  = $rsa.ExportSubjectPublicKeyInfo()
  "私钥:" + [Convert]::ToBase64String($priv)   # 填入 APP_RSA_PRIVATE_KEY
  "公钥:" + [Convert]::ToBase64String($pub)    # 后端 /auth/public-key 自动派生下发，无需手动配置
  $rsa.Dispose()
  ```
  > 说明：后端 [RsaUtil.java](../backend/src/main/java/com/example/backend/security/RsaUtil.java) 启动时由私钥自动派生公钥下发，你不必单独配置公钥。
- 一旦更换私钥，此前加密在途的密码将无法解密（提示"密码格式异常"），属预期行为。

---

## 3. 生产环境（application-prod.yml）注入清单

生产 profile 下**所有敏感项均无默认回退**，缺失即启动失败：

| 环境变量 | 必填 | 说明 |
| --- | --- | --- |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | 是 | 生产数据库连接 |
| `DB_USERNAME` / `DB_PASSWORD` | 是 | 生产数据库凭据 |
| `REDIS_HOST`(及端口/密码) | 是 | 生产 Redis |
| `JWT_SECRET` | 是 | ≥32 字节签名密钥 |
| `APP_RSA_PRIVATE_KEY` | 建议 | PKCS#8 私钥，开启密码加密传输 |

部署注入示例（Linux systemd / Docker）：
```bash
export DB_HOST=10.0.0.5 DB_PORT=3306 DB_NAME=dochub
export DB_USERNAME=dochub_app DB_PASSWORD='<强随机密码>'
export REDIS_HOST=10.0.0.6 REDIS_PORT=6379
export JWT_SECRET='<≥32字节强随机串>'
export APP_RSA_PRIVATE_KEY='<PKCS8私钥Base64>'
java -jar backend.jar --spring.profiles.active=prod
```

---

## 4. 本地开发（application-local.example.yml）

本地通过 `--spring.profiles.active=dev,local` 叠加 local profile，建议复用它即可，
**不要改 production 文件**。将模板复制为 `application-local.yml` 并填入本机真实值：

```powershell
Copy-Item backend/src/main/resources/application-local.example.yml backend/src/main/resources/application-local.yml
$env:DB_PASSWORD = '你的数据库密码'
$env:JWT_SECRET = 'Local-Dev-Jwt-Secret-...'
.\mvnw -f backend spring-boot:run -Dspring-boot.run.profiles=dev,local
```

---

## 5. 常见故障

| 症状 | 排查 |
| --- | --- |
| 启动报 `JWT 签名密钥缺失或长度不足` | `JWT_SECRET` 未设或 < 32 字节 |
| 登录提示"密码格式异常" | `APP_RSA_PRIVATE_KEY` 与前端公钥不匹配，或私钥无效 |
| 登录明文可、加密方案失效 | `APP_RSA_PRIVATE_KEY` 为空（已降级明文） |
| 数据库连不上 | 核对 `DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD` |
| 防爆破/会话/踢人失效但能登录 | Redis 不可用，能力降级（直查库，不锁死用户） |

---

## 6. 敏感文件 Git 治理

- ✅ 应忽略：`application-local.yml`、`application-prod.yml`（含真实凭据，若提交则移除），`.env` 等
- ✅ 应共享：`application-local.example.yml`（占位符模板）、`application-dev.yml`（无敏感默认值）
- ⚠️ 严禁提交：任何含真实密钥/密码的配置文件、`.pem` 私钥文件