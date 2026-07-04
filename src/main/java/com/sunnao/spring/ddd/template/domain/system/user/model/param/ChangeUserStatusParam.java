package com.sunnao.spring.ddd.template.domain.system.user.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import com.sunnao.spring.ddd.template.model.system.user.UserStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 变更用户状态参数
 */
@Getter
@Setter
@ToString
public class ChangeUserStatusParam extends BaseParam {

    /** 用户ID */
    private Long userId;

    /** 目标状态 */
    private UserStatusEnum targetStatus;

    /** 操作人ID */
    private Long operatorId;
}
