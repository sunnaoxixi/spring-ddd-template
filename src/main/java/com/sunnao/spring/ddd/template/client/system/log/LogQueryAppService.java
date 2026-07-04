package com.sunnao.spring.ddd.template.client.system.log;

import com.sunnao.spring.ddd.template.client.system.log.req.QueryOperLogPageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.log.res.QueryOperLogPageResponseDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.ApplicationQueryService;

/**
 * 操作日志查询应用服务接口（读模式）
 * 职责：定义操作日志分页查询接口；日志写入由切面事件异步完成，不对外提供写接口
 */
public interface LogQueryAppService extends ApplicationQueryService {

    /**
     * 分页查询操作日志（按操作时间倒序）
     *
     * @param requestDTO 请求参数
     * @return 分页结果
     */
    ResultDO<QueryOperLogPageResponseDTO> queryOperLogPage(QueryOperLogPageRequestDTO requestDTO);
}
