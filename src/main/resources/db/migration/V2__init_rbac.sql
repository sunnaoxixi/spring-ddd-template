-- RBAC：角色 / 权限 / 角色权限关联 / 用户角色关联（PostgreSQL）

-- 角色表
CREATE TABLE sys_role
(
    id        BIGSERIAL PRIMARY KEY,
    role_key  VARCHAR(64)  NOT NULL,
    role_name VARCHAR(128) NOT NULL,
    status    SMALLINT     NOT NULL DEFAULT 1,
    remark    VARCHAR(256),
    create_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted   SMALLINT     NOT NULL DEFAULT 0
);

COMMENT
ON TABLE sys_role IS '系统角色表';
COMMENT
ON COLUMN sys_role.id IS '主键ID';
COMMENT
ON COLUMN sys_role.role_key IS '角色标识（Sa-Token 鉴权使用，唯一）';
COMMENT
ON COLUMN sys_role.role_name IS '角色名称';
COMMENT
ON COLUMN sys_role.status IS '状态：1-启用，0-禁用';
COMMENT
ON COLUMN sys_role.remark IS '备注';
COMMENT
ON COLUMN sys_role.create_at IS '创建时间';
COMMENT
ON COLUMN sys_role.update_at IS '更新时间';
COMMENT
ON COLUMN sys_role.create_by IS '创建人ID';
COMMENT
ON COLUMN sys_role.update_by IS '更新人ID';
COMMENT
ON COLUMN sys_role.deleted IS '逻辑删除：0-正常，1-已删除';

CREATE UNIQUE INDEX uk_sys_role_key ON sys_role (role_key) WHERE deleted = 0;

-- 权限表
CREATE TABLE sys_permission
(
    id        BIGSERIAL PRIMARY KEY,
    perm_key  VARCHAR(128) NOT NULL,
    perm_name VARCHAR(128) NOT NULL,
    remark    VARCHAR(256),
    create_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted   SMALLINT     NOT NULL DEFAULT 0
);

COMMENT
ON TABLE sys_permission IS '系统权限表';
COMMENT
ON COLUMN sys_permission.id IS '主键ID';
COMMENT
ON COLUMN sys_permission.perm_key IS '权限标识（Sa-Token 鉴权使用，唯一）';
COMMENT
ON COLUMN sys_permission.perm_name IS '权限名称';
COMMENT
ON COLUMN sys_permission.remark IS '备注';
COMMENT
ON COLUMN sys_permission.create_at IS '创建时间';
COMMENT
ON COLUMN sys_permission.update_at IS '更新时间';
COMMENT
ON COLUMN sys_permission.create_by IS '创建人ID';
COMMENT
ON COLUMN sys_permission.update_by IS '更新人ID';
COMMENT
ON COLUMN sys_permission.deleted IS '逻辑删除：0-正常，1-已删除';

CREATE UNIQUE INDEX uk_sys_permission_key ON sys_permission (perm_key) WHERE deleted = 0;

-- 角色-权限关联表（硬删除，无审计）
CREATE TABLE sys_role_permission
(
    id            BIGSERIAL PRIMARY KEY,
    role_id       BIGINT    NOT NULL,
    permission_id BIGINT    NOT NULL,
    create_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT
ON TABLE sys_role_permission IS '角色-权限关联表';
COMMENT
ON COLUMN sys_role_permission.role_id IS '角色ID';
COMMENT
ON COLUMN sys_role_permission.permission_id IS '权限ID';

CREATE UNIQUE INDEX uk_sys_role_permission ON sys_role_permission (role_id, permission_id);

-- 用户-角色关联表（硬删除，无审计）
CREATE TABLE sys_user_role
(
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT    NOT NULL,
    role_id   BIGINT    NOT NULL,
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT
ON TABLE sys_user_role IS '用户-角色关联表';
COMMENT
ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT
ON COLUMN sys_user_role.role_id IS '角色ID';

CREATE UNIQUE INDEX uk_sys_user_role ON sys_user_role (user_id, role_id);
CREATE INDEX idx_sys_user_role_user ON sys_user_role (user_id);

-- 种子角色：admin / user
INSERT INTO sys_role (role_key, role_name, status, remark)
VALUES ('admin', '管理员', 1, '系统管理员，拥有全部权限'),
       ('user', '普通用户', 1, '默认角色，基础权限');

-- 种子权限点（按模块 read/write 粒度）
INSERT INTO sys_permission (perm_key, perm_name)
VALUES ('system:user:read', '用户查询'),
       ('system:user:write', '用户管理'),
       ('system:role:read', '角色查询'),
       ('system:role:write', '角色管理'),
       ('system:dict:read', '字典查询'),
       ('system:dict:write', '字典管理'),
       ('system:file:read', '文件查询/下载'),
       ('system:file:write', '文件上传/删除'),
       ('system:log:read', '操作日志查询');

-- admin 角色拥有全部权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
         CROSS JOIN sys_permission p
WHERE r.role_key = 'admin';

-- user 角色基础权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
         JOIN sys_permission p ON p.perm_key IN ('system:dict:read', 'system:file:read', 'system:file:write')
WHERE r.role_key = 'user';

-- 存量用户角色迁移：sys_user.role（1-管理员，0-普通用户）→ sys_user_role
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
         JOIN sys_role r ON r.role_key = CASE WHEN u.role = 1 THEN 'admin' ELSE 'user' END;

-- 迁移完成后删除 sys_user.role 列
ALTER TABLE sys_user
DROP
COLUMN role;
