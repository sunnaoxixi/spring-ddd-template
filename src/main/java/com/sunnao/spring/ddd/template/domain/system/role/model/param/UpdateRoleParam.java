package com.sunnao.spring.ddd.template.domain.system.role.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import com.sunnao.spring.ddd.template.model.system.role.RoleStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 修改角色参数（roleKey 创建后不可变更）
 */
@Getter
@Setter
@ToString
public class UpdateRoleParam extends BaseParam {

    /** 角色ID */
    private Long roleId;

    /** 角色名称 */
    private String roleName;

    /** 状态 */
    private RoleStatusEnum status;

    /** 备注 */
    private String remark;

    /** 操作人ID */
    private Long operatorId;
}
