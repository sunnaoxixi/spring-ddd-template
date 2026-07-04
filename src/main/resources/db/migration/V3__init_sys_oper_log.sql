-- 系统操作日志表（PostgreSQL）
CREATE TABLE sys_oper_log
(
    id          BIGSERIAL PRIMARY KEY,
    trace_id    VARCHAR(64),
    operator_id BIGINT,
    module      VARCHAR(64)  NOT NULL,
    action      VARCHAR(128) NOT NULL,
    uri         VARCHAR(256),
    params      VARCHAR(2048),
    result_code VARCHAR(64),
    cost_ms     BIGINT       NOT NULL DEFAULT 0,
    ip          VARCHAR(64),
    create_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_oper_log IS '系统操作日志表';
COMMENT ON COLUMN sys_oper_log.id IS '主键ID';
COMMENT ON COLUMN sys_oper_log.trace_id IS '链路追踪ID（与日志 traceId 对应）';
COMMENT ON COLUMN sys_oper_log.operator_id IS '操作人ID（登录前操作如 login 可为空）';
COMMENT ON COLUMN sys_oper_log.module IS '业务模块（如 user/role/dict/file/auth）';
COMMENT ON COLUMN sys_oper_log.action IS '操作动作（如 创建用户）';
COMMENT ON COLUMN sys_oper_log.uri IS '请求 URI';
COMMENT ON COLUMN sys_oper_log.params IS '请求参数摘要（超长截断）';
COMMENT ON COLUMN sys_oper_log.result_code IS '结果码（SUCCESS 或错误码）';
COMMENT ON COLUMN sys_oper_log.cost_ms IS '耗时（毫秒）';
COMMENT ON COLUMN sys_oper_log.ip IS '客户端IP';
COMMENT ON COLUMN sys_oper_log.create_at IS '操作时间';

CREATE INDEX idx_sys_oper_log_create_at ON sys_oper_log (create_at DESC);
CREATE INDEX idx_sys_oper_log_operator ON sys_oper_log (operator_id);
CREATE INDEX idx_sys_oper_log_module ON sys_oper_log (module);
