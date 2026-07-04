package com.sunnao.spring.ddd.template.domain.system.file.model.entity;

import com.sunnao.spring.ddd.template.common.model.BaseEntity;
import com.sunnao.spring.ddd.template.model.system.file.FileStorageTypeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件实体
 * <p>
 * 承载文件元数据（物理内容由 FileStorage 管理），由 FileAggregate 聚合根持有，
 * 外部只能通过聚合根方法访问本实体。
 */
@Getter
@Setter
public class FileEntity extends BaseEntity {

    /** 原始文件名 */
    private String originalName;

    /** 存储路径（相对存储根目录） */
    private String path;

    /** 文件大小（字节） */
    private Long size;

    /** 文件 MIME 类型 */
    private String contentType;

    /** 存储类型 */
    private FileStorageTypeEnum storageType;
}
