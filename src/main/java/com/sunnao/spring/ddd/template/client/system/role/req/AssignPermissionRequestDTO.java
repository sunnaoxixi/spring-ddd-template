package com.sunnao.spring.ddd.template.client.system.role.req;

import com.sunnao.spring.ddd.template.common.model.BaseDto;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 给角色分配权限请求DTO（全量覆盖）
 */
@Getter
@Setter
@ToString
public class AssignPermissionRequestDTO extends BaseDto {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限ID集合（全量覆盖，空集合表示清空权限）
     */
    private List<Long> permissionIds;

    @Override
    public ResultDO<Void> check() {
        if (roleId == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "角色ID不能为空");
        }
        if (permissionIds == null) {
            return ResultDO.buildFailResult("PARAM_ERROR", "权限ID集合不能为空（清空权限请传空数组）");
        }
        if (permissionIds.stream().anyMatch(id -> id == null)) {
            return ResultDO.buildFailResult("PARAM_ERROR", "权限ID不能包含空值");
        }
        return ResultDO.buildSuccessResult();
    }
}
