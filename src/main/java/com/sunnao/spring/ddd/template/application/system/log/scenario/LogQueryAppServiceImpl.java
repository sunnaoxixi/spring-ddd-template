package com.sunnao.spring.ddd.template.application.system.log.scenario;

import com.sunnao.spring.ddd.template.application.system.log.assembler.LogAssembler;
import com.sunnao.spring.ddd.template.client.system.log.LogQueryAppService;
import com.sunnao.spring.ddd.template.client.system.log.req.QueryLoginLogPageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.log.req.QueryOperLogPageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.log.res.QueryLoginLogPageResponseDTO;
import com.sunnao.spring.ddd.template.client.system.log.res.QueryOperLogPageResponseDTO;
import com.sunnao.spring.ddd.template.common.model.PageQuery;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.log.model.aggregate.LoginLogAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.model.aggregate.OperLogAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.model.param.LoginLogQuery;
import com.sunnao.spring.ddd.template.domain.system.log.model.param.OperLogQuery;
import com.sunnao.spring.ddd.template.domain.system.log.repository.LoginLogRepository;
import com.sunnao.spring.ddd.template.domain.system.log.repository.OperLogRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * 日志查询应用服务实现（读模式，含操作日志与登录日志）
 * 职责：领域内查询，通过 Repository 获取聚合根后经 Assembler 转换为 DTO
 */
@Slf4j
@Service
public class LogQueryAppServiceImpl implements LogQueryAppService {

    @Resource
    private OperLogRepository operLogRepository;

    @Resource
    private LogAssembler logAssembler;

    @Resource
    private LoginLogRepository loginLogRepository;

    @Override
    public ResultDO<QueryOperLogPageResponseDTO> queryOperLogPage(QueryOperLogPageRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 组装分页查询条件（pageNum 从1开始 → startIndex）
            PageQuery<OperLogQuery> pageQuery = PageQuery.build(logAssembler.toOperLogQuery(requestDTO));
            pageQuery.setStartIndex((requestDTO.getPageNum() - 1) * requestDTO.getPageSize());
            pageQuery.setPageSize(requestDTO.getPageSize());

            // 3. 查询本领域操作日志分页数据
            Page<OperLogAggregate> page = operLogRepository.queryPage(pageQuery);

            // 4. 组装响应 DTO
            return ResultDO.buildSuccessResult(
                    logAssembler.toQueryOperLogPageResponseDTO(page.getTotalElements(), page.getContent()));
        } catch (Exception e) {
            log.error("分页查询操作日志失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }

    @Override
    public ResultDO<QueryLoginLogPageResponseDTO> queryLoginLogPage(QueryLoginLogPageRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 组装分页查询条件（pageNum 从1开始 → startIndex）
            PageQuery<LoginLogQuery> pageQuery = PageQuery.build(logAssembler.toLoginLogQuery(requestDTO));
            pageQuery.setStartIndex((requestDTO.getPageNum() - 1) * requestDTO.getPageSize());
            pageQuery.setPageSize(requestDTO.getPageSize());

            // 3. 查询本领域登录日志分页数据
            Page<LoginLogAggregate> page = loginLogRepository.queryPage(pageQuery);

            // 4. 组装响应 DTO
            return ResultDO.buildSuccessResult(
                    logAssembler.toQueryLoginLogPageResponseDTO(page.getTotalElements(), page.getContent()));
        } catch (Exception e) {
            log.error("分页查询登录日志失败, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }
}
