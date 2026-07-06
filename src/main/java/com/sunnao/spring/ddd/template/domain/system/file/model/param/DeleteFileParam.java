package com.sunnao.spring.ddd.template.domain.system.file.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 删除文件参数
 */
@Getter
@Setter
@ToString
public class DeleteFileParam extends BaseParam {

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 操作人ID
     */
    private Long operatorId;
}
