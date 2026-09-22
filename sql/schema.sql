-- ============================================================
-- DocHub —— 数据库物理建模脚本 (schema.sql)
-- 兼容：MySQL 8.0+ / InnoDB / utf8mb4_unicode_ci
-- 命名口径：经典 sys_ 前缀 RBAC（对齐《JPA 企业级实体类全景设计》课件）
--
-- 设计规约（严格遵循 JPA 课件 + AGENTS.md 红线）：
--   1. 主键一律 BIGINT（雪花算法 Long），逻辑外键使用「字段 ID 关联」（无条件实体对象关联，杜绝 N+1 与循环引用）；
--   2. 树形结构采用「parent_id + ancestors 祖先链」扁平设计，禁止 parent/children 自关联；
--   3. 多对多关系一律显式中间表（sys_user_role / sys_user_permission / sys_role_permission），禁止物理级联删除；
--   4. 统一审计字段：create_by / create_time / update_by / update_time / is_deleted（逻辑删除）；
--   5. 枚举状态字段存 TINYINT 数值码，杜绝 ORDINAL 错位与字符串冗余；
--   6. 全部雪花 ID 在实体层序列化为 String，避免前端 JS 精度丢失（DDL 层以 BIGINT 存储）。
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------------
-- 1. 部门表 sys_dept（树形扁平设计）
--    “一个用户只能属于一个部门” —— 在 sys_user.dept_id 上以单值字段关联，
--    用户与部门为一对多（部门 : 用户 = 1 : N），无需用户-部门中间表。
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
    `id`            BIGINT       NOT NULL                COMMENT '部门ID（雪花算法）',
    `parent_id`     BIGINT       NOT NULL DEFAULT 0      COMMENT '父部门ID（0 表示根节点）',
    `ancestors`     VARCHAR(500) NOT NULL DEFAULT '0'    COMMENT '祖级树链路径（如 0,100,101）',
    `dept_name`     VARCHAR(50)  NOT NULL                COMMENT '部门名称',
    `order_num`     INT          NOT NULL DEFAULT 0      COMMENT '同级显示排序号',
    `leader_user_id` BIGINT      NULL                    COMMENT '部门负责人用户ID（弱关联 sys_user.id）',
    `status`        TINYINT      NOT NULL DEFAULT 0      COMMENT '部门状态（0:正常 1:停用）',
    `version`       INT          NOT NULL DEFAULT 0      COMMENT '乐观锁版本号（@Version）',
    `create_by`     BIGINT       NULL                    COMMENT '创建人用户ID',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     BIGINT       NULL                    COMMENT '更新人用户ID',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '逻辑删除标记（0:正常 1:已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_dept_parent` (`parent_id`),
    KEY `idx_dept_ancestors` (`ancestors`),
    KEY `idx_dept_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '系统组织部门表（树形扁平设计）';

-- ------------------------------------------------------------------
-- 2. 用户表 sys_user
--    dept_id 为单值逻辑外键（严格一对多，一名用户唯一部门）。
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`           BIGINT       NOT NULL                COMMENT '用户ID（雪花算法）',
    `username`     VARCHAR(50)  NOT NULL                COMMENT '登录账号（唯一）',
    `password`     VARCHAR(100) NOT NULL                COMMENT '密码哈希（BCrypt，60+ 位）',
    `real_name`    VARCHAR(50)  NULL                    COMMENT '真实姓名',
    `email`        VARCHAR(100) NULL                    COMMENT '工作邮箱',
    `phone`        VARCHAR(20)  NULL                    COMMENT '手机号',
    `dept_id`      BIGINT       NOT NULL                COMMENT '所属部门ID（逻辑外键 sys_dept.id，一对一）',
    `status`       TINYINT      NOT NULL DEFAULT 0      COMMENT '账号状态（0:正常 1:停用）',
    `version`      INT          NOT NULL DEFAULT 0      COMMENT '乐观锁版本号（@Version）',
    `create_by`    BIGINT       NULL                    COMMENT '创建人用户ID',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    BIGINT       NULL                    COMMENT '更新人用户ID',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`   TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '逻辑删除标记（0:正常 1:已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    KEY `idx_user_dept_status` (`dept_id`, `status`),
    KEY `idx_user_email` (`email`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '系统用户核心账号表';

-- ------------------------------------------------------------------
-- 3. 角色表 sys_role
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`           BIGINT       NOT NULL                COMMENT '角色ID（雪花算法）',
    `role_name`    VARCHAR(50)  NOT NULL                COMMENT '角色名称（展示用）',
    `role_key`     VARCHAR(50)  NOT NULL                COMMENT '角色标识（唯一，如 admin/viewer/editor）',
    `sort`         INT          NOT NULL DEFAULT 0      COMMENT '显示排序号',
    `status`       TINYINT      NOT NULL DEFAULT 0      COMMENT '角色状态（0:正常 1:停用）',
    `version`      INT          NOT NULL DEFAULT 0      COMMENT '乐观锁版本号（@Version）',
    `remark`       VARCHAR(255) NULL                    COMMENT '角色备注',
    `create_by`    BIGINT       NULL                    COMMENT '创建人用户ID',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    BIGINT       NULL                    COMMENT '更新人用户ID',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`   TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '逻辑删除标记（0:正常 1:已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '系统角色表';

-- ------------------------------------------------------------------
-- 4. 权限表 sys_permission（菜单/接口级原子权限点，传统 RBAC）
--    树形扁平设计：parent_id + ancestors，perm_type 区分模块/菜单/按钮/接口。
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id`           BIGINT       NOT NULL                COMMENT '权限ID（雪花算法）',
    `parent_id`    BIGINT       NOT NULL DEFAULT 0      COMMENT '父权限ID（0 表示根节点）',
    `ancestors`    VARCHAR(500) NOT NULL DEFAULT '0'    COMMENT '祖级树链路径（如 0,100,101）',
    `perm_name`    VARCHAR(50)  NOT NULL                COMMENT '权限名称（如 创建文档）',
    `perm_key`     VARCHAR(100) NOT NULL                COMMENT '权限标识（唯一，如 doc:create、system:user:manage）',
    `perm_type`    TINYINT      NOT NULL DEFAULT 1      COMMENT '权限类型（1:目录 2:菜单 3:按钮/接口）',
    `path`         VARCHAR(200) NULL                    COMMENT '前端路由或后端接口路径',
    `method`       VARCHAR(10)  NULL                    COMMENT 'HTTP 方法（接口级权限用，如 GET/POST）',
    `sort`         INT          NOT NULL DEFAULT 0      COMMENT '显示排序号',
    `status`       TINYINT      NOT NULL DEFAULT 0      COMMENT '权限状态（0:正常 1:停用）',
    `version`      INT          NOT NULL DEFAULT 0      COMMENT '乐观锁版本号（@Version）',
    `create_by`    BIGINT       NULL                    COMMENT '创建人用户ID',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    BIGINT       NULL                    COMMENT '更新人用户ID',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`   TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '逻辑删除标记（0:正常 1:已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_perm_key` (`perm_key`),
    KEY `idx_perm_parent` (`parent_id`),
    KEY `idx_perm_ancestors` (`ancestors`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '系统权限表（菜单/接口级原子权限点）';

-- ------------------------------------------------------------------
-- 5. 用户-角色关联表 sys_user_role（显式中间表，禁止 @ManyToMany）
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id`          BIGINT   NOT NULL                  COMMENT '关联ID（雪花算法）',
    `user_id`     BIGINT   NOT NULL                  COMMENT '用户ID（逻辑外键 sys_user.id）',
    `role_id`     BIGINT   NOT NULL                  COMMENT '角色ID（逻辑外键 sys_role.id）',
    `create_by`   BIGINT   NULL                      COMMENT '授权绑定人用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权绑定时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_ur_role_id` (`role_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户-角色关联表';

-- ------------------------------------------------------------------
-- 6. 用户-权限关联表 sys_user_permission（例外直挂，超出角色的额外授权）
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user_permission`;
CREATE TABLE `sys_user_permission` (
    `id`            BIGINT   NOT NULL                  COMMENT '关联ID（雪花算法）',
    `user_id`       BIGINT   NOT NULL                  COMMENT '用户ID（逻辑外键 sys_user.id）',
    `permission_id` BIGINT   NOT NULL                  COMMENT '权限ID（逻辑外键 sys_permission.id）',
    `create_by`     BIGINT   NULL                      COMMENT '授权绑定人用户ID',
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权绑定时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_permission` (`user_id`, `permission_id`),
    KEY `idx_up_perm_id` (`permission_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户-权限关联表（用户级例外授权）';

-- ------------------------------------------------------------------
-- 7. 角色-权限关联表 sys_role_permission（角色绑定权限集合）
--    说明：传统 RBAC 中“角色关联权限”需此中间表承载，角色由此获得菜单/接口权限。
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `id`            BIGINT   NOT NULL                  COMMENT '关联ID（雪花算法）',
    `role_id`       BIGINT   NOT NULL                  COMMENT '角色ID（逻辑外键 sys_role.id）',
    `permission_id` BIGINT   NOT NULL                  COMMENT '权限ID（逻辑外键 sys_permission.id）',
    `create_by`     BIGINT   NULL                      COMMENT '授权绑定人用户ID',
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权绑定时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_rp_perm_id` (`permission_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '角色-权限关联表';

-- ------------------------------------------------------------------
-- 8. 操作审计日志表 audit_log（append-only 追加式留痕，不做逻辑删除/乐观锁）
--    字段映射 GLOSSARY「AuditLog」：{主体、动作、对象、时间、结果} + 附加元数据。
--    由 AuditLogService.record(...) 异步写入，action 对齐 AuditAction 枚举词。
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log` (
    `id`            BIGINT       NOT NULL                COMMENT '日志ID（雪花算法）',
    `actor_id`      BIGINT       NULL                    COMMENT '操作主体用户ID（未登录/系统行为可空）',
    `actor_name`    VARCHAR(50)  NULL                    COMMENT '操作主体账号名（冗余存，仅供留痕展示）',
    `action`        VARCHAR(50)  NOT NULL                COMMENT '审计动作类型（AuditAction：LOGIN/ACCESS/UPDATE/DELETE/PERMISSION_CHANGE...）',
    `target_type`   VARCHAR(50)  NULL                    COMMENT '操作对象类型（如 USER / ROLE / PERMISSION / DOCUMENT）',
    `target_id`     BIGINT       NULL                    COMMENT '操作对象ID（逻辑外键对应的目标主键）',
    `result`        VARCHAR(20)  NOT NULL                COMMENT '操作结果（OperationResult：SUCCESS/FORBIDDEN/BAD_REQUEST/...）',
    `detail`        VARCHAR(500) NULL                    COMMENT '附加元数据/说明',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_audit_actor_time` (`actor_id`, `create_time`),
    KEY `idx_audit_action_time` (`action`, `create_time`),
    KEY `idx_audit_target` (`target_type`, `target_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '操作审计日志表（append-only）';

SET FOREIGN_KEY_CHECKS = 1;