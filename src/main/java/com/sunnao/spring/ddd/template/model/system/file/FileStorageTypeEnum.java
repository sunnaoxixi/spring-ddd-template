package com.sunnao.spring.ddd.template.model.system.file;

import lombok.Getter;

/**
 * 文件存储类型枚举
 * 共享模型：所有内部模块（domain/application/adaptor/infrastructure）都可以使用
 */
@Getter
public enum FileStorageTypeEnum {

    /**
     * 本地磁盘存储
     */
    LOCAL("local", "本地磁盘"),

    /**
     * S3 兼容对象存储（阿里云 OSS、腾讯云 COS、MinIO 等）
     */
    S3("s3", "S3 对象存储"),
    ;

    /**
     * 存储类型标识（数据库存储）
     */
    private final String code;

    private final String description;

    FileStorageTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据码值获取枚举
     *
     * @param code 码值
     * @return 枚举，未匹配返回 null
     */
    public static FileStorageTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (FileStorageTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
