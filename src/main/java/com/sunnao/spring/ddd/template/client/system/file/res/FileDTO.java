package com.sunnao.spring.ddd.template.client.system.file.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 文件 DTO
 * <p>
 * 复用引用对象，被多个 ResponseDTO 引用，不含物理内容。
 */
@Getter
@Setter
@ToString
public class FileDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    private Long id;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 文件 MIME 类型
     */
    private String contentType;

    /**
     * 上传人ID
     */
    private Long uploadBy;

    /**
     * 上传时间
     */
    private LocalDateTime createAt;
}
