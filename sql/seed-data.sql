-- ============================================================
-- DocHub —— 系统管理模块种子数据 (seed-data.sql)
-- 对齐：sql/schema.sql（列名/类型/默认值 100% 一致）
-- 兼容：MySQL 8.0+ / InnoDB / utf8mb4_unicode_ci
--
-- 用法：
--   mysql -u<user> -p<db> < seed-data.sql
--
-- 默认超管账号（明文 / 哈希一致）：
--   username : admin
--   password : 12345678  （BCrypt $2a$10$ 前缀，可被项目 BCryptPasswordEncoder.matches 校验）
--
-- 幂等说明：
--   schema.sql 每个表都是逻辑删除（is_deleted），主键为手工固定 BIGINT。
--   本脚本默认「首次执行一次性插入」；若需可重复执行，请先手动取消下面
--   DELETE 段的注释（按依赖倒序清空 关联表 → 业务表 后再跑，绝不使用 DROP）。
--
-- SVN/雪花策略：seed 全部使用可读固定 ID（1001/2001/3001/4001/5001...），
-- 便于理解与旁路执行，不与应用运行时雪花算法冲突。
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------------
-- 【幂等】可选清空段（默认注释，重复执行脚本时取消注释启用）
-- ------------------------------------------------------------------
-- DELETE FROM sys_role_permission;
-- DELETE FROM sys_user_role;
-- DELETE FROM sys_user_dept;
-- DELETE FROM sys_user_post;
-- DELETE FROM sys_user_permission;
-- DELETE FROM sys_permission;
-- DELETE FROM sys_role;
-- DELETE FROM sys_user;
-- DELETE FROM sys_post;
-- DELETE FROM sys_dept;

-- ------------------------------------------------------------------
-- 1. 根部门 sys_dept（parent_id=0 / ancestors='0' 表示根节点）
-- ------------------------------------------------------------------
INSERT INTO `sys_dept`
    (`id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader_user_id`, `status`, `version`, `create_by`)
VALUES
    (1001, 0, '0', '总部', 1, 3001, 0, 0, 3001);

-- ------------------------------------------------------------------
-- 2. 岗位 sys_post（post_key 唯一）
-- ------------------------------------------------------------------
INSERT INTO `sys_post`
    (`id`, `post_name`, `post_key`, `sort`, `status`, `version`, `remark`, `create_by`)
VALUES
    (2001, '系统管理员', 'admin',  1, 0, 0, '超管岗位，拥有全部系统配置',       3001),
    (2002, '普通成员',   'member', 2, 0, 0, '默认成员岗位，只读基础数据',       3001);

-- ------------------------------------------------------------------
-- 3. 超管用户 sys_user（password 为 BCrypt 哈希，明文 12345678）
--    dept_id 单值主部门=1001；完整多部门关联见 sys_user_dept
-- ------------------------------------------------------------------
INSERT INTO `sys_user`
    (`id`, `username`, `password`, `real_name`, `email`, `phone`, `dept_id`, `status`, `version`, `create_by`)
VALUES
    (3001, 'admin',
     '$2a$10$0sf3lj5wl0chEnNCqk7NCeZQQpfX7ljqPYcaP8e6YL.LTlAuQc2eq',
     '系统管理员', 'admin@docHub.local', '13800000000', 1001, 0, 0, 3001);

-- ------------------------------------------------------------------
-- 4. 超管角色 sys_role（role_key=admin 唯一）
-- ------------------------------------------------------------------
INSERT INTO `sys_role`
    (`id`, `role_name`, `role_key`, `sort`, `status`, `version`, `remark`, `create_by`)
VALUES
    (4001, '超级管理员', 'admin', 1, 0, 0, '超管角色，绑定全部 system:* 权限点', 3001);

-- ------------------------------------------------------------------
-- 5. 权限点 sys_permission（perm_key 与 Controller @RequirePermission 注解对齐）
--    覆盖列：system:user:* / role:* / dept:* / perm:* / post:* / log:*
--    perm_type=3 接口级；查询类又可复用于前端菜单（path/method 留空即可）
-- ------------------------------------------------------------------
INSERT INTO `sys_permission`
    (`id`, `parent_id`, `ancestors`, `perm_name`, `perm_key`, `perm_type`, `sort`, `status`, `version`, `create_by`)
VALUES
    -- 用户管理 user
    (5001, 0, '0', '用户-查询', 'system:user:query',  3, 1, 0, 0, 3001),
    (5002, 0, '0', '用户-新增', 'system:user:add',    3, 2, 0, 0, 3001),
    (5003, 0, '0', '用户-修改', 'system:user:update', 3, 3, 0, 0, 3001),
    (5004, 0, '0', '用户-删除', 'system:user:delete', 3, 4, 0, 0, 3001),
    -- 角色管理 role
    (5005, 0, '0', '角色-查询', 'system:role:query',  3, 5, 0, 0, 3001),
    (5006, 0, '0', '角色-新增', 'system:role:add',    3, 6, 0, 0, 3001),
    (5007, 0, '0', '角色-修改', 'system:role:update', 3, 7, 0, 0, 3001),
    (5008, 0, '0', '角色-删除', 'system:role:delete', 3, 8, 0, 0, 3001),
    -- 部门管理 dept
    (5009, 0, '0', '部门-查询', 'system:dept:query',  3, 9,  0, 0, 3001),
    (5010, 0, '0', '部门-新增', 'system:dept:add',    3, 10, 0, 0, 3001),
    (5011, 0, '0', '部门-修改', 'system:dept:update', 3, 11, 0, 0, 3001),
    (5012, 0, '0', '部门-删除', 'system:dept:delete', 3, 12, 0, 0, 3001),
    -- 权限管理 perm
    (5013, 0, '0', '权限-查询', 'system:perm:query',  3, 13, 0, 0, 3001),
    (5014, 0, '0', '权限-新增', 'system:perm:add',    3, 14, 0, 0, 3001),
    (5015, 0, '0', '权限-修改', 'system:perm:update', 3, 15, 0, 0, 3001),
    (5016, 0, '0', '权限-删除', 'system:perm:delete', 3, 16, 0, 0, 3001),
    -- 岗位管理 post
    (5017, 0, '0', '岗位-查询', 'system:post:query',  3, 17, 0, 0, 3001),
    (5018, 0, '0', '岗位-新增', 'system:post:add',    3, 18, 0, 0, 3001),
    (5019, 0, '0', '岗位-修改', 'system:post:update', 3, 19, 0, 0, 3001),
    (5020, 0, '0', '岗位-删除', 'system:post:delete', 3, 20, 0, 0, 3001),
    -- 操作日志 log（只读，无写操作）
    (5021, 0, '0', '日志-查询', 'system:log:query',    3, 21, 0, 0, 3001),
    (5022, 0, '0', '日志-新增', 'system:log:add',      3, 22, 0, 0, 3001),
    (5023, 0, '0', '日志-修改', 'system:log:update',   3, 23, 0, 0, 3001),
    (5024, 0, '0', '日志-删除', 'system:log:delete',   3, 24, 0, 0, 3001);

-- ------------------------------------------------------------------
-- 6. 关联绑定（多对多中间表）
-- ------------------------------------------------------------------
-- 用户-部门：admin(3001) → 总部(1001)
INSERT INTO `sys_user_dept` (`id`, `user_id`, `dept_id`, `create_by`)
VALUES (8001, 3001, 1001, 3001);

-- 用户-岗位：admin(3001) → 系统管理员(2001)
INSERT INTO `sys_user_post` (`id`, `user_id`, `post_id`, `create_by`)
VALUES (9001, 3001, 2001, 3001);

-- 用户-角色：admin(3001) → 超级管理员(4001)
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_by`)
VALUES (6001, 3001, 4001, 3001);

-- 角色-权限：超级管理员(4001) → 全部 24 个 system:* 权限点(5001~5024)
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_by`)
VALUES
    (7001,  4001, 5001, 3001),
    (7002,  4001, 5002, 3001),
    (7003,  4001, 5003, 3001),
    (7004,  4001, 5004, 3001),
    (7005,  4001, 5005, 3001),
    (7006,  4001, 5006, 3001),
    (7007,  4001, 5007, 3001),
    (7008,  4001, 5008, 3001),
    (7009,  4001, 5009, 3001),
    (7010,  4001, 5010, 3001),
    (7011,  4001, 5011, 3001),
    (7012,  4001, 5012, 3001),
    (7013,  4001, 5013, 3001),
    (7014,  4001, 5014, 3001),
    (7015,  4001, 5015, 3001),
    (7016,  4001, 5016, 3001),
    (7017,  4001, 5017, 3001),
    (7018,  4001, 5018, 3001),
    (7019,  4001, 5019, 3001),
    (7020,  4001, 5020, 3001),
    (7021,  4001, 5021, 3001),
    (7022,  4001, 5022, 3001),
    (7023,  4001, 5023, 3001),
    (7024,  4001, 5024, 3001);

SET FOREIGN_KEY_CHECKS = 1;