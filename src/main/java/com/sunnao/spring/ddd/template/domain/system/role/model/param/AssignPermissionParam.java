package com.sunnao.spring.ddd.template.domain.system.role.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 给角色分配权限参数（全量覆盖）
 */
@Getter
@Setter
@ToString
public class AssignPermissionParam extends BaseParam {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限ID集合（全量覆盖，空集合表示清空权限）
     */
    private List<Long> permissionIds;

    /**
     * 操作人ID
     */
    private Long operatorId;
}
