package com.sunnao.spring.ddd.template.client.system.online;

import com.sunnao.spring.ddd.template.client.system.online.req.KickOnlineUserByTokenRequestDTO;
import com.sunnao.spring.ddd.template.client.system.online.req.KickOnlineUserByUserRequestDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.ApplicationCmdService;

/**
 * 在线用户应用服务接口（写模式）
 * 职责：定义强制下线（踢人）操作接口
 */
public interface OnlineAppService extends ApplicationCmdService {

    /**
     * 按会话 token 踢下线（仅影响单个会话）
     *
     * @param requestDTO 请求参数
     * @return 操作结果
     */
    ResultDO<Void> kickByToken(KickOnlineUserByTokenRequestDTO requestDTO);

    /**
     * 按用户踢下线该用户全部会话
     *
     * @param requestDTO 请求参数
     * @return 操作结果
     */
    ResultDO<Void> kickByUser(KickOnlineUserByUserRequestDTO requestDTO);
}
