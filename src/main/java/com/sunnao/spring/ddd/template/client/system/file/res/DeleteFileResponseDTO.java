package com.sunnao.spring.ddd.template.client.system.file.res;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 删除文件响应DTO
 */
@Getter
@Setter
@ToString
public class DeleteFileResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 文件ID */
    private Long fileId;
}
