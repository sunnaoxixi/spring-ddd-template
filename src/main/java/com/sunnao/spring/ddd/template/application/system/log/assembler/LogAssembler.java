package com.sunnao.spring.ddd.template.application.system.log.assembler;

import com.sunnao.spring.ddd.template.client.system.log.model.LoginLogDTO;
import com.sunnao.spring.ddd.template.client.system.log.model.OperLogDTO;
import com.sunnao.spring.ddd.template.client.system.log.req.QueryLoginLogPageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.log.req.QueryOperLogPageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.log.res.QueryLoginLogPageResponseDTO;
import com.sunnao.spring.ddd.template.client.system.log.res.QueryOperLogPageResponseDTO;
import com.sunnao.spring.ddd.template.domain.system.log.model.aggregate.LoginLogAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.model.aggregate.OperLogAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.model.entity.LoginLogEntity;
import com.sunnao.spring.ddd.template.domain.system.log.model.entity.OperLogEntity;
import com.sunnao.spring.ddd.template.domain.system.log.model.param.LoginLogQuery;
import com.sunnao.spring.ddd.template.domain.system.log.model.param.OperLogQuery;

import java.util.Collections;
import java.util.List;

/**
 * 操作日志转换器
 * 负责 RequestDTO/ResponseDTO 与领域对象之间的转换
 */
public class LogAssembler {

    private LogAssembler() {
    }

    /**
     * 分页查询 RequestDTO 转领域查询条件
     */
    public static OperLogQuery toOperLogQuery(QueryOperLogPageRequestDTO requestDTO) {
        OperLogQuery query = new OperLogQuery();
        query.setModule(requestDTO.getModule());
        query.setOperatorId(requestDTO.getOperatorId());
        query.setStartTime(requestDTO.getStartTime());
        query.setEndTime(requestDTO.getEndTime());
        return query;
    }

    /**
     * 聚合根转 OperLogDTO
     */
    public static OperLogDTO toOperLogDTO(OperLogAggregate aggregate) {
        if (aggregate == null || aggregate.getOperLogEntity() == null) {
            return null;
        }
        OperLogEntity entity = aggregate.getOperLogEntity();
        OperLogDTO dto = new OperLogDTO();
        dto.setId(entity.getId());
        dto.setTraceId(entity.getTraceId());
        dto.setOperatorId(entity.getOperatorId());
        dto.setModule(entity.getModule());
        dto.setAction(entity.getAction());
        dto.setUri(entity.getUri());
        dto.setParams(entity.getParams());
        dto.setResultCode(entity.getResultCode());
        dto.setCostMs(entity.getCostMs());
        dto.setIp(entity.getIp());
        dto.setCreateAt(entity.getCreateAt());
        return dto;
    }

    /**
     * 聚合根列表转分页 ResponseDTO
     */
    public static QueryOperLogPageResponseDTO toQueryOperLogPageResponseDTO(long total,
                                                                            List<OperLogAggregate> aggregates) {
        QueryOperLogPageResponseDTO responseDTO = new QueryOperLogPageResponseDTO();
        responseDTO.setTotal(total);
        if (aggregates == null || aggregates.isEmpty()) {
            responseDTO.setLogs(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setLogs(aggregates.stream().map(LogAssembler::toOperLogDTO).toList());
        return responseDTO;
    }

    /**
     * 分页查询登录日志 RequestDTO 转领域查询条件
     */
    public static LoginLogQuery toLoginLogQuery(QueryLoginLogPageRequestDTO requestDTO) {
        LoginLogQuery query = new LoginLogQuery();
        query.setEmail(requestDTO.getEmail());
        query.setUserId(requestDTO.getUserId());
        query.setSuccess(requestDTO.getSuccess());
        query.setStartTime(requestDTO.getStartTime());
        query.setEndTime(requestDTO.getEndTime());
        return query;
    }

    /**
     * 聚合根转 LoginLogDTO
     */
    public static LoginLogDTO toLoginLogDTO(LoginLogAggregate aggregate) {
        if (aggregate == null || aggregate.getLoginLogEntity() == null) {
            return null;
        }
        LoginLogEntity entity = aggregate.getLoginLogEntity();
        LoginLogDTO dto = new LoginLogDTO();
        dto.setId(entity.getId());
        dto.setTraceId(entity.getTraceId());
        dto.setUserId(entity.getUserId());
        dto.setEmail(entity.getEmail());
        dto.setSuccess(entity.getSuccess());
        dto.setCode(entity.getCode());
        dto.setMsg(entity.getMsg());
        dto.setIp(entity.getIp());
        dto.setUserAgent(entity.getUserAgent());
        dto.setCreateAt(entity.getCreateAt());
        return dto;
    }

    /**
     * 登录日志聚合根列表转分页 ResponseDTO
     */
    public static QueryLoginLogPageResponseDTO toQueryLoginLogPageResponseDTO(long total,
                                                                              List<LoginLogAggregate> aggregates) {
        QueryLoginLogPageResponseDTO responseDTO = new QueryLoginLogPageResponseDTO();
        responseDTO.setTotal(total);
        if (aggregates == null || aggregates.isEmpty()) {
            responseDTO.setLogs(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setLogs(aggregates.stream().map(LogAssembler::toLoginLogDTO).toList());
        return responseDTO;
    }
}
