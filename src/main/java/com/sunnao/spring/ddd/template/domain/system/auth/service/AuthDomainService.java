package com.sunnao.spring.ddd.template.domain.system.auth.service;

import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.DomainService;
import com.sunnao.spring.ddd.template.domain.system.auth.model.param.LoginParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;

/**
 * 认证领域服务接口
 * <p>
 * 封装身份认证核心业务逻辑（凭证校验、账号状态校验），
 * 不感知会话/token 技术细节（由 application 层收敛 Sa-Token 调用）。
 */
public interface AuthDomainService extends DomainService {

    /**
     * 身份认证：校验邮箱与密码，并校验账号状态
     *
     * @param param 登录参数
     * @return 认证通过的用户聚合根
     */
    ResultDO<UserAggregate> authenticate(LoginParam param);
}
