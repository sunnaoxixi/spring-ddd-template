package com.sunnao.spring.ddd.template.domain.system.role.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 删除角色参数
 */
@Getter
@Setter
@ToString
public class DeleteRoleParam extends BaseParam {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 操作人ID
     */
    private Long operatorId;
}
