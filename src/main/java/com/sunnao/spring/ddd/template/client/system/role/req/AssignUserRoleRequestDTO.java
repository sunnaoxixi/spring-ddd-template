package com.sunnao.spring.ddd.template.client.system.role.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 给用户授予角色请求DTO（全量覆盖）
 */
@Getter
@Setter
@ToString
public class AssignUserRoleRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID集合（全量覆盖，空集合表示清空角色）
     */
    private List<Long> roleIds;

    @Override
    public ResultDO<Void> check() {
        if (userId == null) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "用户ID不能为空");
        }
        if (roleIds == null) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "角色ID集合不能为空（清空角色请传空数组）");
        }
        if (roleIds.stream().anyMatch(id -> id == null)) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "角色ID不能包含空值");
        }
        return ResultDO.buildSuccessResult();
    }
}
