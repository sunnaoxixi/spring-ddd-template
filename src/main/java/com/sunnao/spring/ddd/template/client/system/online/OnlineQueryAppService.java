package com.sunnao.spring.ddd.template.client.system.online;

import com.sunnao.spring.ddd.template.client.system.online.req.QueryOnlineUserPageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.online.res.QueryOnlineUserPageResponseDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.ApplicationQueryService;

/**
 * 在线用户查询应用服务接口（读模式）
 * 职责：定义在线会话分页查询接口；数据源为 Sa-Token Redis 会话，非数据库
 */
public interface OnlineQueryAppService extends ApplicationQueryService {

    /**
     * 分页查询在线用户（会话）
     *
     * @param requestDTO 请求参数
     * @return 分页结果
     */
    ResultDO<QueryOnlineUserPageResponseDTO> queryOnlineUserPage(QueryOnlineUserPageRequestDTO requestDTO);
}
