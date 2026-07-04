package com.sunnao.spring.ddd.template.domain.system.user.model.param;

import com.sunnao.spring.ddd.template.common.model.BaseParam;
import com.sunnao.spring.ddd.template.model.system.user.UserStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户查询条件
 */
@Getter
@Setter
@ToString
public class UserQuery extends BaseParam {

    /** 邮箱（精确匹配） */
    private String email;

    /** 昵称（模糊匹配） */
    private String nickname;

    /** 状态 */
    private UserStatusEnum status;
}
