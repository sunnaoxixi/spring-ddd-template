package com.sunnao.spring.ddd.template.client.system.user;

import com.sunnao.spring.ddd.template.client.system.user.req.GetUserDetailRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.QueryUserPageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.GetUserDetailResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.QueryUserPageResponseDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.ApplicationQueryService;

/**
 * 用户查询应用服务接口（读模式）
 * 职责：定义用户相关的查询操作接口
 */
public interface UserQueryAppService extends ApplicationQueryService {

    /**
     * 获取用户详情
     *
     * @param requestDTO 查询参数
     * @return 用户详情
     */
    ResultDO<GetUserDetailResponseDTO> getUserDetail(GetUserDetailRequestDTO requestDTO);

    /**
     * 分页查询用户列表
     *
     * @param requestDTO 查询参数
     * @return 分页结果
     */
    ResultDO<QueryUserPageResponseDTO> queryUserPage(QueryUserPageRequestDTO requestDTO);
}
