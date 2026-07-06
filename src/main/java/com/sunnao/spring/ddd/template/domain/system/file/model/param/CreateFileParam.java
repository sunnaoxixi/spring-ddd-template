package com.sunnao.spring.ddd.template.domain.system.file.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 创建文件参数（登记文件元数据，物理文件已由应用层存储）
 */
@Getter
@Setter
@ToString
public class CreateFileParam extends BaseParam {

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 存储路径（相对存储根目录）
     */
    private String path;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 文件 MIME 类型
     */
    private String contentType;

    /**
     * 存储类型标识（如 local）
     */
    private String storageType;

    /**
     * 操作人ID（上传人）
     */
    private Long operatorId;
}
