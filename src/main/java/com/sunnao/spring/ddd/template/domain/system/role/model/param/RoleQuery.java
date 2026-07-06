package com.sunnao.spring.ddd.template.domain.system.role.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import com.sunnao.spring.ddd.template.model.system.role.RoleStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 角色查询条件
 */
@Getter
@Setter
@ToString
public class RoleQuery extends BaseParam {

    /**
     * 角色标识（精确匹配）
     */
    private String roleKey;

    /**
     * 角色名称（模糊匹配）
     */
    private String roleName;

    /**
     * 状态
     */
    private RoleStatusEnum status;
}
