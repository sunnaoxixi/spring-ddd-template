package com.sunnao.spring.ddd.template.client.system.role.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.regex.Pattern;

/**
 * 创建角色请求DTO
 */
@Getter
@Setter
@ToString
public class CreateRoleRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Pattern ROLE_KEY_PATTERN = Pattern.compile("^[a-z][a-z0-9_-]{1,63}$");

    /**
     * 角色标识（创建后不可变更）
     */
    private String roleKey;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 备注
     */
    private String remark;

    @Override
    public ResultDO<Void> check() {
        if (roleKey == null || roleKey.isBlank()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "角色标识不能为空");
        }
        if (!ROLE_KEY_PATTERN.matcher(roleKey).matches()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "角色标识须以小写字母开头，仅含小写字母/数字/下划线/中划线，长度2~64");
        }
        if (roleName == null || roleName.isBlank()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "角色名称不能为空");
        }
        return ResultDO.buildSuccessResult();
    }
}
