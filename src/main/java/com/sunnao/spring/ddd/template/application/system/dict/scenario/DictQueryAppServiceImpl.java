package com.sunnao.spring.ddd.template.application.system.dict.scenario;

import com.sunnao.spring.ddd.template.application.system.dict.assembler.DictAssembler;
import com.sunnao.spring.ddd.template.client.system.dict.DictQueryAppService;
import com.sunnao.spring.ddd.template.client.system.dict.req.QueryDictDataListRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.QueryDictTypePageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.QueryDictDataListResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.QueryDictTypePageResponseDTO;
import com.sunnao.spring.ddd.template.common.model.PageQuery;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictDataAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictTypeAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.DictTypeQuery;
import com.sunnao.spring.ddd.template.domain.system.dict.repository.DictRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典查询应用服务实现（读模式）
 * 职责：领域内查询，通过 Repository 获取聚合根后经 Assembler 转换为 DTO；
 * 按 typeKey 查询启用数据走 Redis 缓存（缓存逻辑收敛在仓储实现）
 */
@Slf4j
@Service
public class DictQueryAppServiceImpl implements DictQueryAppService {

    @Resource
    private DictRepository dictRepository;

    @Override
    public ResultDO<QueryDictTypePageResponseDTO> queryDictTypePage(QueryDictTypePageRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 组装分页查询条件（pageNum 从1开始 → startIndex）
            PageQuery<DictTypeQuery> pageQuery = PageQuery.build(DictAssembler.toDictTypeQuery(requestDTO));
            pageQuery.setStartIndex((requestDTO.getPageNum() - 1) * requestDTO.getPageSize());
            pageQuery.setPageSize(requestDTO.getPageSize());

            // 3. 查询本领域字典类型分页数据
            Page<DictTypeAggregate> page = dictRepository.queryPage(pageQuery);

            // 4. 组装响应 DTO
            return ResultDO.buildSuccessResult(
                    DictAssembler.toQueryDictTypePageResponseDTO(page.getTotalElements(), page.getContent()));
        } catch (Exception e) {
            log.error("分页查询字典类型失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    @Override
    public ResultDO<QueryDictDataListResponseDTO> queryDictDataList(QueryDictDataListRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 查询启用状态字典数据（走 Redis 缓存）
            List<DictDataAggregate> aggregates =
                    dictRepository.queryEnabledDataByTypeKey(requestDTO.getTypeKey());

            // 3. 组装响应 DTO
            return ResultDO.buildSuccessResult(
                    DictAssembler.toQueryDictDataListResponseDTO(requestDTO.getTypeKey(), aggregates));
        } catch (Exception e) {
            log.error("按类型键查询字典数据失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    @Override
    public ResultDO<QueryDictDataListResponseDTO> queryAllDictDataList(QueryDictDataListRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 查询全部字典数据（管理端，含禁用项，不走缓存）
            List<DictDataAggregate> aggregates =
                    dictRepository.queryAllDataByTypeKey(requestDTO.getTypeKey());

            // 3. 组装响应 DTO
            return ResultDO.buildSuccessResult(
                    DictAssembler.toQueryDictDataListResponseDTO(requestDTO.getTypeKey(), aggregates));
        } catch (Exception e) {
            log.error("按类型键查询全部字典数据失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }
}
