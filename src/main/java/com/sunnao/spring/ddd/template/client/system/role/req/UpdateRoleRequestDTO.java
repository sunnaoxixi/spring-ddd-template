package com.sunnao.spring.ddd.template.client.system.role.req;

import com.sunnao.spring.ddd.template.client.system.role.enums.RoleStatusEnum;
import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 修改角色请求DTO（roleKey 不可变更）
 */
@Getter
@Setter
@ToString
public class UpdateRoleRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    @Override
    public ResultDO<Void> check() {
        if (roleId == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "角色ID不能为空");
        }
        boolean roleNameBlank = roleName == null || roleName.isBlank();
        if (roleNameBlank && status == null && remark == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "角色名称、状态、备注不能同时为空");
        }
        if (status != null && RoleStatusEnum.getByCode(status) == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "状态取值不合法");
        }
        return ResultDO.buildSuccessResult();
    }
}
