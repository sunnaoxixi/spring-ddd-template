package com.sunnao.spring.ddd.template.client.system.file.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 下载文件响应DTO（含物理内容）
 */
@Getter
@Setter
@ToString(exclude = "content")
public class DownloadFileResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 文件ID */
    private Long fileId;

    /** 原始文件名 */
    private String originalName;

    /** 文件 MIME 类型 */
    private String contentType;

    /** 文件大小（字节） */
    private Long size;

    /** 文件内容 */
    private byte[] content;
}
