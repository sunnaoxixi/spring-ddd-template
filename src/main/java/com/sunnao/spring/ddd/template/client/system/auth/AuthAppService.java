package com.sunnao.spring.ddd.template.client.system.auth;

import com.sunnao.spring.ddd.template.client.system.auth.req.LoginRequestDTO;
import com.sunnao.spring.ddd.template.client.system.auth.res.LoginResponseDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.ApplicationCmdService;

/**
 * 认证应用服务接口（写模式）
 * 职责：定义登录、登出等会话变更操作接口
 */
public interface AuthAppService extends ApplicationCmdService {

    /**
     * 登录：认证通过后签发 token
     *
     * @param requestDTO 登录参数
     * @return token 与用户基础信息
     */
    ResultDO<LoginResponseDTO> login(LoginRequestDTO requestDTO);

    /**
     * 登出：注销当前会话
     *
     * @return 操作结果
     */
    ResultDO<Void> logout();
}
