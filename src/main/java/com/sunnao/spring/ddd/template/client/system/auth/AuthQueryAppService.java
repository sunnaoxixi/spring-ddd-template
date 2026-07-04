package com.sunnao.spring.ddd.template.client.system.auth;

import com.sunnao.spring.ddd.template.client.system.auth.res.GetLoginUserResponseDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.ApplicationQueryService;

/**
 * 认证查询应用服务接口（读模式）
 * 职责：定义登录态相关的查询操作接口
 */
public interface AuthQueryAppService extends ApplicationQueryService {

    /**
     * 获取当前登录用户信息
     *
     * @return 当前登录用户信息
     */
    ResultDO<GetLoginUserResponseDTO> getLoginUserInfo();
}
