package com.sunnao.spring.ddd.template.client.system.file.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 上传文件请求DTO
 * <p>
 * Controller 从 MultipartFile 转换而来，MultipartFile 不越过 adaptor 层。
 */
@Getter
@Setter
@ToString(exclude = "content")
public class UploadFileRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件 MIME 类型
     */
    private String contentType;

    /**
     * 文件内容
     */
    private byte[] content;

    @Override
    public ResultDO<Void> check() {
        if (originalName == null || originalName.isBlank()) {
            return ResultDO.buildFailResult("PARAM_ERROR", "文件名不能为空");
        }
        if (originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
            return ResultDO.buildFailResult("PARAM_ERROR", "文件名不合法");
        }
        if (content == null || content.length == 0) {
            return ResultDO.buildFailResult("PARAM_ERROR", "文件内容不能为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
