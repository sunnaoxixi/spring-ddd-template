package com.sunnao.spring.ddd.template.client.system.file.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 下载文件请求DTO
 */
@Getter
@Setter
@ToString
public class DownloadFileRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    private Long fileId;

    @Override
    public ResultDO<Void> check() {
        if (fileId == null) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "文件ID不能为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
