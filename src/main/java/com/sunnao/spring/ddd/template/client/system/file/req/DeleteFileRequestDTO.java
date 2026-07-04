package com.sunnao.spring.ddd.template.client.system.file.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 删除文件请求DTO（逻辑删除元数据 + 清理物理文件）
 */
@Getter
@Setter
@ToString
public class DeleteFileRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 文件ID */
    private Long fileId;

    @Override
    public ResultDO<Void> check() {
        if (fileId == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "文件ID不能为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
