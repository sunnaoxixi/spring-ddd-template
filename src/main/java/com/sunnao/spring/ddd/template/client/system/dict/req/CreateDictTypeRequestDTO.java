package com.sunnao.spring.ddd.template.client.system.dict.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.regex.Pattern;

/**
 * 创建字典类型请求DTO
 */
@Getter
@Setter
@ToString
public class CreateDictTypeRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Pattern TYPE_KEY_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{1,63}$");

    /**
     * 字典类型键（创建后不可变更）
     */
    private String typeKey;

    /**
     * 字典类型名称
     */
    private String typeName;

    /**
     * 备注
     */
    private String remark;

    @Override
    public ResultDO<Void> check() {
        if (typeKey == null || typeKey.isBlank()) {
            return ResultDO.buildFailResult("PARAM_ERROR", "字典类型键不能为空");
        }
        if (!TYPE_KEY_PATTERN.matcher(typeKey).matches()) {
            return ResultDO.buildFailResult("PARAM_ERROR", "字典类型键须以小写字母开头，仅含小写字母/数字/下划线，长度2~64");
        }
        if (typeName == null || typeName.isBlank()) {
            return ResultDO.buildFailResult("PARAM_ERROR", "字典类型名称不能为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
