package com.sunnao.spring.ddd.template.client.system.dict;

import com.sunnao.spring.ddd.template.client.system.dict.req.QueryDictDataListRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.QueryDictTypePageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.QueryDictDataListResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.QueryDictTypePageResponseDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.ApplicationQueryService;

/**
 * 字典查询应用服务接口（读模式）
 * 职责：定义字典相关的查询接口
 */
public interface DictQueryAppService extends ApplicationQueryService {

    /**
     * 分页查询字典类型列表（管理端）
     *
     * @param requestDTO 请求参数
     * @return 分页结果
     */
    ResultDO<QueryDictTypePageResponseDTO> queryDictTypePage(QueryDictTypePageRequestDTO requestDTO);

    /**
     * 按类型键查询启用状态的字典数据列表（所有登录用户，走 Redis 缓存）
     *
     * @param requestDTO 请求参数
     * @return 字典数据列表
     */
    ResultDO<QueryDictDataListResponseDTO> queryDictDataList(QueryDictDataListRequestDTO requestDTO);

    /**
     * 按类型键查询全部字典数据列表（管理端，含禁用项，不走缓存）
     *
     * @param requestDTO 请求参数
     * @return 字典数据列表
     */
    ResultDO<QueryDictDataListResponseDTO> queryAllDictDataList(QueryDictDataListRequestDTO requestDTO);
}
