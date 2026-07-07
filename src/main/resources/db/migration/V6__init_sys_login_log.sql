-- 系统登录日志表（PostgreSQL）
CREATE TABLE sys_login_log
(
    id         BIGSERIAL PRIMARY KEY,
    trace_id   VARCHAR(64),
    user_id    BIGINT,
    email      VARCHAR(128) NOT NULL,
    success    BOOLEAN      NOT NULL,
    code       VARCHAR(64),
    msg        VARCHAR(256),
    ip         VARCHAR(64),
    user_agent VARCHAR(512),
    create_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT
ON TABLE sys_login_log IS '系统登录日志表';
COMMENT
ON COLUMN sys_login_log.id IS '主键ID';
COMMENT
ON COLUMN sys_login_log.trace_id IS '链路追踪ID（与日志 traceId 对应）';
COMMENT
ON COLUMN sys_login_log.user_id IS '用户ID（登录失败时可为空）';
COMMENT
ON COLUMN sys_login_log.email IS '登录邮箱';
COMMENT
ON COLUMN sys_login_log.success IS '是否登录成功';
COMMENT
ON COLUMN sys_login_log.code IS '结果码（SUCCESS 或错误码）';
COMMENT
ON COLUMN sys_login_log.msg IS '结果说明（失败原因）';
COMMENT
ON COLUMN sys_login_log.ip IS '客户端IP';
COMMENT
ON COLUMN sys_login_log.user_agent IS '客户端 User-Agent（超长截断）';
COMMENT
ON COLUMN sys_login_log.create_at IS '登录时间';

CREATE INDEX idx_sys_login_log_create_at ON sys_login_log (create_at DESC);
CREATE INDEX idx_sys_login_log_user ON sys_login_log (user_id);
CREATE INDEX idx_sys_login_log_email ON sys_login_log (email);
