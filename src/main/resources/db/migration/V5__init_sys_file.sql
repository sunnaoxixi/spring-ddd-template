-- 系统文件表（PostgreSQL）
CREATE TABLE sys_file
(
    id            BIGSERIAL PRIMARY KEY,
    original_name VARCHAR(256) NOT NULL,
    path          VARCHAR(512) NOT NULL,
    size          BIGINT       NOT NULL DEFAULT 0,
    content_type  VARCHAR(128),
    storage_type  VARCHAR(32)  NOT NULL DEFAULT 'local',
    create_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by     BIGINT,
    update_by     BIGINT,
    deleted       SMALLINT     NOT NULL DEFAULT 0
);

COMMENT
ON TABLE sys_file IS '系统文件表';
COMMENT
ON COLUMN sys_file.id IS '主键ID';
COMMENT
ON COLUMN sys_file.original_name IS '原始文件名';
COMMENT
ON COLUMN sys_file.path IS '存储路径（相对存储根目录）';
COMMENT
ON COLUMN sys_file.size IS '文件大小（字节）';
COMMENT
ON COLUMN sys_file.content_type IS '文件 MIME 类型';
COMMENT
ON COLUMN sys_file.storage_type IS '存储类型：local-本地磁盘（OSS 等留扩展）';
COMMENT
ON COLUMN sys_file.create_at IS '创建时间（上传时间）';
COMMENT
ON COLUMN sys_file.update_at IS '更新时间';
COMMENT
ON COLUMN sys_file.create_by IS '创建人ID（上传人）';
COMMENT
ON COLUMN sys_file.update_by IS '更新人ID';
COMMENT
ON COLUMN sys_file.deleted IS '逻辑删除：0-正常，1-已删除';

CREATE INDEX idx_sys_file_create_by ON sys_file (create_by) WHERE deleted = 0;
