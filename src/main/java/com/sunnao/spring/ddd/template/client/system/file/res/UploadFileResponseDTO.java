package com.sunnao.spring.ddd.template.client.system.file.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 上传文件响应DTO
 */
@Getter
@Setter
@ToString
public class UploadFileResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 文件ID */
    private Long fileId;

    /** 原始文件名 */
    private String originalName;

    /** 文件大小（字节） */
    private Long size;
}
