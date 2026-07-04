-- 系统管理-用户表（PostgreSQL）
CREATE TABLE IF NOT EXISTS sys_user
(
    id        BIGSERIAL PRIMARY KEY,
    email     VARCHAR(128) NOT NULL,
    nickname  VARCHAR(64)  NOT NULL,
    password  VARCHAR(128) NOT NULL,
    status    SMALLINT     NOT NULL DEFAULT 1,
    role      SMALLINT     NOT NULL DEFAULT 0,
    avatar    VARCHAR(512),
    create_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted   SMALLINT     NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_user IS '系统用户表';
COMMENT ON COLUMN sys_user.id IS '主键ID';
COMMENT ON COLUMN sys_user.email IS '邮箱';
COMMENT ON COLUMN sys_user.nickname IS '昵称';
COMMENT ON COLUMN sys_user.password IS '密码（BCrypt 加密）';
COMMENT ON COLUMN sys_user.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_user.role IS '角色：1-管理员，0-普通用户';
COMMENT ON COLUMN sys_user.avatar IS '头像URL';
COMMENT ON COLUMN sys_user.create_at IS '创建时间';
COMMENT ON COLUMN sys_user.update_at IS '更新时间';
COMMENT ON COLUMN sys_user.create_by IS '创建人ID';
COMMENT ON COLUMN sys_user.update_by IS '更新人ID';
COMMENT ON COLUMN sys_user.deleted IS '逻辑删除：0-正常，1-已删除';

-- 邮箱唯一索引（仅对未删除记录生效）
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_email ON sys_user (email) WHERE deleted = 0;

-- 已建库升级：补充角色字段
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS role SMALLINT NOT NULL DEFAULT 0;

-- 种子管理员（邮箱 admin@example.com，初始密码 admin123456，首次登录后请修改）
INSERT INTO sys_user (email, nickname, password, status, role)
SELECT 'admin@example.com', '系统管理员', '$2a$10$eQ.ZNV/FNWv58.HQsE./g.UYIurknDSvBvOe5v6K7cOwO/OF9vDam', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE email = 'admin@example.com' AND deleted = 0);
