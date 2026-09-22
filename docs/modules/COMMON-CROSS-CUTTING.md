# 公共正交能力约定（Common Cross-Cutting）

> 供 M1（用户/登录）、M2（文档业务）共享，避免重复实现。依据 AGENTS.md §2/§6 与现有基建。

---

## 1. 统一鉴权栈（M1 提供，全项目复用）

| 能力 | 说明 | 位置 |
|------|------|------|
| `AuthContext` | ThreadLocal 持有当前 userId / roleKey / 空间角色缓存，RequestFilter 注入一次 | `common/` |
| `@RequirePermission("permKey")` | 方法级权限切面，校验当前用户权限点，失败抛 `PermissionDeniedException`(403) | `common/` |
| `getCurrentUserId()` | 获取当前登录用户 ID（JWT 声明，不限库） | `common/` |
| `BCryptPasswordEncoder` | 密码哈希（M1 T1-5） | `auth/` |
| `JwtUtil` | 签发/解析 token | `auth/` |

> 铁律：Filter **只解析一次 token** 放入 ThreadLocal，绝不每请求查库；权限集合可带在 token 声明缓存。

## 2. 统一返回与异常

- 响应一律 `Result<T>{code,message,data}`；分页 `PageResult{list,total,pageNum,pageSize}`。
- `GlobalExceptionHandler` 兜底：`BusinessException`→业务失败、`MethodArgumentNotValidException`→校验中文提示、`ObjectOptimisticLockingFailureException`→409。
- HTTP 状态码语义：401 未登录 / 403 越权（`PermissionDeniedException`）/ 400 参数 / 409 版本冲突。

## 3. 操作审计（AuditLog）

```java
auditLogService.record(getCurrentUserId(), AuditAction.UPLOAD, targetId, OperationResult.SUCCESS, meta);
```
- 动作枚举见 GLOSSARY `AuditAction`（LOGIN/ACCESS/UPLOAD/EDIT/AI_CONFIRM/AI_ARCHIVE/RECYCLE/PERMISSION_CHANGE...）。
- 写入**异步**（`@Async`或消息），不阻塞业务主流程；含主体/动作/对象/时间/结果。

## 4. Spring 性能与并发军规（全项目必须遵守，AGENTS.md §6）

1. **懒加载 / 逻辑外键**：关联用 ID 字段（`deptId`/`spaceId`），不建仓耗对象关联，杜绝 N+1。
2. **禁止 `@Data`**：实体用 `@Getter @Setter @NoArgsConstructor`，防 toString 递归栈溢出。
3. **禁止循环查/存**：批量用 `findAllById` + `saveAll`，内存 Map 分组匹配。
4. **动态检索用 `Specification`**（`JpaSpecificationExecutor`），禁手写 WHERE 1=1 拼接。
5. **主表必带 `@Version`**：更新 DTO 带 version，Service 前置比对(第一道防线)+底层乐观锁兜底(第二道)+409。
6. **热点计数用 `@Modifying` 原子 SQL**（`SET c=c+1`，`clearAutomatically=true`），禁先查后改。
7. **列表/分页用 DTO 投影**，禁 SELECT * 大文本。
8. **禁 findAll 后内存过滤软删**：用 `findAllByIsDeleted(0)`。

## 5. 跨模块边界红线

- M2 的环形权限二次鉴权在 **Service 层强制**，不依赖前端；接口层 `@RequirePermission` 只做第一道。
- 任何"资源是否存在"对越权者**不泄露存在性**（同 401/403 统一提示）。
- 新增领域词汇必须先登记 `docs/01-requirements/GLOSSARY.md` 再编码，禁止扩散命名幻觉。