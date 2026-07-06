package com.sunnao.spring.ddd.template.domain.system.role.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 给用户授予角色参数（全量覆盖）
 */
@Getter
@Setter
@ToString
public class AssignUserRoleParam extends BaseParam {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID集合（全量覆盖，空集合表示清空角色）
     */
    private List<Long> roleIds;

    /**
     * 操作人ID
     */
    private Long operatorId;
}
