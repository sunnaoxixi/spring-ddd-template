package com.sunnao.spring.ddd.template.client.system.role.req;

import com.sunnao.spring.ddd.template.client.system.role.enums.RoleStatusEnum;
import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 分页查询角色请求DTO
 */
@Getter
@Setter
@ToString
public class QueryRolePageRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码，从1开始
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    /**
     * 角色标识（精确匹配，可选）
     */
    private String roleKey;

    /**
     * 角色名称（模糊匹配，可选）
     */
    private String roleName;

    /**
     * 状态：1-启用，0-禁用（可选）
     */
    private Integer status;

    @Override
    public ResultDO<Void> check() {
        if (pageNum == null || pageNum < 1) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "页码必须大于等于1");
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "每页条数必须在1~100之间");
        }
        if (status != null && RoleStatusEnum.getByCode(status) == null) {
            return ResultDO.buildFailResult(ErrorCodeEnum.PARAM_ERROR, "状态取值不合法");
        }
        return ResultDO.buildSuccessResult();
    }
}
