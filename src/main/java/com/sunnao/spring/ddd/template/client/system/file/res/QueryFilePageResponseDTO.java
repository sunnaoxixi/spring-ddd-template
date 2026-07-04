package com.sunnao.spring.ddd.template.client.system.file.res;

import com.sunnao.spring.ddd.template.client.system.file.model.FileDTO;
import com.sunnao.spring.ddd.template.common.model.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 分页查询文件响应DTO
 */
@Getter
@Setter
@ToString
public class QueryFilePageResponseDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 总条数 */
    private Long total;

    /** 文件列表 */
    private List<FileDTO> files;
}
