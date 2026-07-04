package com.sunnao.spring.ddd.template.application.system.user.scenario;

import com.sunnao.spring.ddd.template.application.system.user.assembler.UserAssembler;
import com.sunnao.spring.ddd.template.client.system.user.UserAppService;
import com.sunnao.spring.ddd.template.client.system.user.req.ChangeUserStatusRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.CreateUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.DeleteUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.UpdateUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.ChangeUserStatusResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.CreateUserResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.DeleteUserResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.UpdateUserResponseDTO;
import com.sunnao.spring.ddd.template.common.context.CurrentUserContext;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.service.UserDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户应用服务实现（写模式）
 * 职责：场景编排，参数自校验 → DTO 转 Param → 调用领域服务 → 组装响应
 */
@Slf4j
@Service
public class UserAppServiceImpl implements UserAppService {

    @Resource
    private UserDomainService userDomainService;

    @Override
    public ResultDO<CreateUserResponseDTO> createUser(CreateUserRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务创建用户（操作人取自当前登录用户）
            ResultDO<UserAggregate> domainResult = userDomainService.createUser(
                    UserAssembler.toCreateParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            CreateUserResponseDTO responseDTO = new CreateUserResponseDTO();
            responseDTO.setUserId(domainResult.getData().getUserEntity().getId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("创建用户系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    @Override
    public ResultDO<UpdateUserResponseDTO> updateUser(UpdateUserRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务修改资料（操作人取自当前登录用户）
            ResultDO<Void> domainResult = userDomainService.updateUser(
                    UserAssembler.toUpdateParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            UpdateUserResponseDTO responseDTO = new UpdateUserResponseDTO();
            responseDTO.setUserId(requestDTO.getUserId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("修改用户资料系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    @Override
    public ResultDO<ChangeUserStatusResponseDTO> changeUserStatus(ChangeUserStatusRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务变更状态（操作人取自当前登录用户）
            ResultDO<Void> domainResult = userDomainService.changeUserStatus(
                    UserAssembler.toChangeStatusParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            ChangeUserStatusResponseDTO responseDTO = new ChangeUserStatusResponseDTO();
            responseDTO.setUserId(requestDTO.getUserId());
            responseDTO.setStatus(requestDTO.getStatus());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("变更用户状态系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    @Override
    public ResultDO<DeleteUserResponseDTO> deleteUser(DeleteUserRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务逻辑删除（操作人取自当前登录用户）
            ResultDO<Void> domainResult = userDomainService.deleteUser(
                    UserAssembler.toDeleteParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            DeleteUserResponseDTO responseDTO = new DeleteUserResponseDTO();
            responseDTO.setUserId(requestDTO.getUserId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("删除用户系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }
}
