package com.sunnao.spring.ddd.template.application.system.dict.scenario;

import com.sunnao.spring.ddd.template.application.system.dict.assembler.DictAssembler;
import com.sunnao.spring.ddd.template.client.system.dict.DictAppService;
import com.sunnao.spring.ddd.template.client.system.dict.req.CreateDictDataRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.CreateDictTypeRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.DeleteDictDataRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.DeleteDictTypeRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.UpdateDictDataRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.UpdateDictTypeRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.CreateDictDataResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.CreateDictTypeResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.DeleteDictDataResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.DeleteDictTypeResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.UpdateDictDataResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.UpdateDictTypeResponseDTO;
import com.sunnao.spring.ddd.template.common.context.CurrentUserContext;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictDataAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictTypeAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.service.DictDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 字典应用服务实现（写模式）
 * 职责：场景编排，参数自校验 → DTO 转 Param → 调用领域服务 → 组装响应
 */
@Slf4j
@Service
public class DictAppServiceImpl implements DictAppService {

    @Resource
    private DictDomainService dictDomainService;

    @Override
    public ResultDO<CreateDictTypeResponseDTO> createDictType(CreateDictTypeRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务创建字典类型（操作人取自当前登录用户）
            ResultDO<DictTypeAggregate> domainResult = dictDomainService.createDictType(
                    DictAssembler.toCreateTypeParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            CreateDictTypeResponseDTO responseDTO = new CreateDictTypeResponseDTO();
            responseDTO.setTypeId(domainResult.getData().getDictTypeEntity().getId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("创建字典类型系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    @Override
    public ResultDO<UpdateDictTypeResponseDTO> updateDictType(UpdateDictTypeRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务修改字典类型（操作人取自当前登录用户）
            ResultDO<Void> domainResult = dictDomainService.updateDictType(
                    DictAssembler.toUpdateTypeParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            UpdateDictTypeResponseDTO responseDTO = new UpdateDictTypeResponseDTO();
            responseDTO.setTypeId(requestDTO.getTypeId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("修改字典类型系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    @Override
    public ResultDO<DeleteDictTypeResponseDTO> deleteDictType(DeleteDictTypeRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务删除字典类型（操作人取自当前登录用户）
            ResultDO<Void> domainResult = dictDomainService.deleteDictType(
                    DictAssembler.toDeleteTypeParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            DeleteDictTypeResponseDTO responseDTO = new DeleteDictTypeResponseDTO();
            responseDTO.setTypeId(requestDTO.getTypeId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("删除字典类型系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    @Override
    public ResultDO<CreateDictDataResponseDTO> createDictData(CreateDictDataRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务创建字典数据（操作人取自当前登录用户）
            ResultDO<DictDataAggregate> domainResult = dictDomainService.createDictData(
                    DictAssembler.toCreateDataParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            CreateDictDataResponseDTO responseDTO = new CreateDictDataResponseDTO();
            responseDTO.setDataId(domainResult.getData().getDictDataEntity().getId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("创建字典数据系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    @Override
    public ResultDO<UpdateDictDataResponseDTO> updateDictData(UpdateDictDataRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务修改字典数据（操作人取自当前登录用户）
            ResultDO<Void> domainResult = dictDomainService.updateDictData(
                    DictAssembler.toUpdateDataParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            UpdateDictDataResponseDTO responseDTO = new UpdateDictDataResponseDTO();
            responseDTO.setDataId(requestDTO.getDataId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("修改字典数据系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    @Override
    public ResultDO<DeleteDictDataResponseDTO> deleteDictData(DeleteDictDataRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务删除字典数据（操作人取自当前登录用户）
            ResultDO<Void> domainResult = dictDomainService.deleteDictData(
                    DictAssembler.toDeleteDataParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            DeleteDictDataResponseDTO responseDTO = new DeleteDictDataResponseDTO();
            responseDTO.setDataId(requestDTO.getDataId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("删除字典数据系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }
}
