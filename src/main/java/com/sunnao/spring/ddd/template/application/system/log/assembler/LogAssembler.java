package com.sunnao.spring.ddd.template.application.system.log.assembler;

import com.sunnao.spring.ddd.template.client.system.log.res.LoginLogDTO;
import com.sunnao.spring.ddd.template.client.system.log.res.OperLogDTO;
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
import org.mapstruct.Mapper;

import java.util.Collections;
import java.util.List;

/**
 * 操作日志转换器
 * 负责 RequestDTO/ResponseDTO 与领域对象之间的转换
 */
@Mapper(componentModel = "spring")
public interface LogAssembler {

    /**
     * 分页查询 RequestDTO 转领域查询条件
     */
    OperLogQuery toOperLogQuery(QueryOperLogPageRequestDTO requestDTO);

    /**
     * 聚合根转 OperLogDTO
     */
    default OperLogDTO toOperLogDTO(OperLogAggregate aggregate) {
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
    default QueryOperLogPageResponseDTO toQueryOperLogPageResponseDTO(long total,
                                                                      List<OperLogAggregate> aggregates) {
        QueryOperLogPageResponseDTO responseDTO = new QueryOperLogPageResponseDTO();
        responseDTO.setTotal(total);
        if (aggregates == null || aggregates.isEmpty()) {
            responseDTO.setLogs(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setLogs(aggregates.stream().map(this::toOperLogDTO).toList());
        return responseDTO;
    }

    /**
     * 分页查询登录日志 RequestDTO 转领域查询条件
     */
    LoginLogQuery toLoginLogQuery(QueryLoginLogPageRequestDTO requestDTO);

    /**
     * 聚合根转 LoginLogDTO
     */
    default LoginLogDTO toLoginLogDTO(LoginLogAggregate aggregate) {
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
    default QueryLoginLogPageResponseDTO toQueryLoginLogPageResponseDTO(long total,
                                                                        List<LoginLogAggregate> aggregates) {
        QueryLoginLogPageResponseDTO responseDTO = new QueryLoginLogPageResponseDTO();
        responseDTO.setTotal(total);
        if (aggregates == null || aggregates.isEmpty()) {
            responseDTO.setLogs(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setLogs(aggregates.stream().map(this::toLoginLogDTO).toList());
        return responseDTO;
    }
}
