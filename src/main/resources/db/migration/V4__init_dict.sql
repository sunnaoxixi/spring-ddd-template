-- 字典类型表
CREATE TABLE sys_dict_type
(
    id        BIGSERIAL PRIMARY KEY,
    type_key  VARCHAR(64)  NOT NULL,
    type_name VARCHAR(128) NOT NULL,
    status    SMALLINT     NOT NULL DEFAULT 1,
    remark    VARCHAR(256),
    create_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted   SMALLINT     NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_dict_type IS '字典类型表';
COMMENT ON COLUMN sys_dict_type.id IS '主键ID';
COMMENT ON COLUMN sys_dict_type.type_key IS '字典类型键（唯一标识）';
COMMENT ON COLUMN sys_dict_type.type_name IS '字典类型名称';
COMMENT ON COLUMN sys_dict_type.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_dict_type.remark IS '备注';
COMMENT ON COLUMN sys_dict_type.create_at IS '创建时间';
COMMENT ON COLUMN sys_dict_type.update_at IS '更新时间';
COMMENT ON COLUMN sys_dict_type.create_by IS '创建人ID';
COMMENT ON COLUMN sys_dict_type.update_by IS '更新人ID';
COMMENT ON COLUMN sys_dict_type.deleted IS '逻辑删除：0-正常，1-已删除';

CREATE UNIQUE INDEX uk_sys_dict_type_key ON sys_dict_type (type_key) WHERE deleted = 0;

-- 字典数据表
CREATE TABLE sys_dict_data
(
    id         BIGSERIAL PRIMARY KEY,
    type_key   VARCHAR(64)  NOT NULL,
    label      VARCHAR(128) NOT NULL,
    dict_value VARCHAR(128) NOT NULL,
    sort       INT          NOT NULL DEFAULT 0,
    status     SMALLINT     NOT NULL DEFAULT 1,
    remark     VARCHAR(256),
    create_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by  BIGINT,
    update_by  BIGINT,
    deleted    SMALLINT     NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_dict_data IS '字典数据表';
COMMENT ON COLUMN sys_dict_data.id IS '主键ID';
COMMENT ON COLUMN sys_dict_data.type_key IS '字典类型键';
COMMENT ON COLUMN sys_dict_data.label IS '字典标签';
COMMENT ON COLUMN sys_dict_data.dict_value IS '字典值';
COMMENT ON COLUMN sys_dict_data.sort IS '排序（升序）';
COMMENT ON COLUMN sys_dict_data.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN sys_dict_data.remark IS '备注';
COMMENT ON COLUMN sys_dict_data.create_at IS '创建时间';
COMMENT ON COLUMN sys_dict_data.update_at IS '更新时间';
COMMENT ON COLUMN sys_dict_data.create_by IS '创建人ID';
COMMENT ON COLUMN sys_dict_data.update_by IS '更新人ID';
COMMENT ON COLUMN sys_dict_data.deleted IS '逻辑删除：0-正常，1-已删除';

CREATE INDEX idx_sys_dict_data_type_key ON sys_dict_data (type_key) WHERE deleted = 0;
CREATE UNIQUE INDEX uk_sys_dict_data_type_value ON sys_dict_data (type_key, dict_value) WHERE deleted = 0;

-- 种子数据：用户状态字典
INSERT INTO sys_dict_type (type_key, type_name, status, remark)
VALUES ('user_status', '用户状态', 1, '系统用户启用/禁用状态');

INSERT INTO sys_dict_data (type_key, label, dict_value, sort, status)
VALUES ('user_status', '启用', '1', 1, 1),
       ('user_status', '禁用', '0', 2, 1);
