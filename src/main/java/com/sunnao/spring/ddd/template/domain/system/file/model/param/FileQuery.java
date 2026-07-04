package com.sunnao.spring.ddd.template.domain.system.file.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件查询条件
 */
@Getter
@Setter
@ToString
public class FileQuery extends BaseParam {

    /** 原始文件名（模糊匹配） */
    private String originalName;

    /** 上传人ID */
    private Long uploadBy;
}
