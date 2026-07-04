package com.sunnao.spring.ddd.template.domain.system.user.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 删除用户参数（逻辑删除）
 */
@Getter
@Setter
@ToString
public class DeleteUserParam extends BaseParam {

    /** 用户ID */
    private Long userId;

    /** 操作人ID */
    private Long operatorId;
}
