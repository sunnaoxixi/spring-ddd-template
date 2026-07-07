package com.sunnao.spring.ddd.template.application.system.user.scenario;

import cn.dev33.satoken.stp.StpUtil;
import com.sunnao.spring.ddd.template.application.system.user.assembler.UserAssembler;
import com.sunnao.spring.ddd.template.client.system.user.UserAppService;
import com.sunnao.spring.ddd.template.client.system.user.enums.UserStatusEnum;
import com.sunnao.spring.ddd.template.client.system.user.req.ChangeUserStatusRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.CreateUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.DeleteUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.UpdateUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.ChangeUserStatusResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.CreateUserResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.DeleteUserResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.UpdateUserResponseDTO;
import com.sunnao.spring.ddd.template.common.context.CurrentUserContext;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.service.UserDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户应用服务实现（写模式）
 * 职责：场景编排，参数自校验 → DTO 转 Param → 调用领域服务 → 组装响应。
 * 禁用/删除用户成功后强制下线其全部会话（Sa-Token 调用收敛在应用层）。
 */
@Slf4j
@Service
public class UserAppServiceImpl implements UserAppService {

    @Resource
    private UserDomainService userDomainService;

    @Resource
    private UserAssembler userAssembler;

    @Override
    public ResultDO<CreateUserResponseDTO> createUser(CreateUserRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务创建用户
            ResultDO<UserAggregate> domainResult = userDomainService.createUser(userAssembler.toCreateParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            CreateUserResponseDTO responseDTO = new CreateUserResponseDTO();
            responseDTO.setUserId(domainResult.getData().getUserEntity().getId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("创建用户系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
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
                    userAssembler.toUpdateParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            UpdateUserResponseDTO responseDTO = new UpdateUserResponseDTO();
            responseDTO.setUserId(requestDTO.getUserId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("修改用户资料系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
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
                    userAssembler.toChangeStatusParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 禁用成功后强制下线该用户全部会话，防止旧 token 继续访问
            if (UserStatusEnum.DISABLED.getCode().equals(requestDTO.getStatus())) {
                kickoutQuietly(requestDTO.getUserId());
            }

            // 4. 组装响应
            ChangeUserStatusResponseDTO responseDTO = new ChangeUserStatusResponseDTO();
            responseDTO.setUserId(requestDTO.getUserId());
            responseDTO.setStatus(requestDTO.getStatus());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("变更用户状态系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
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
                    userAssembler.toDeleteParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 删除成功后强制下线该用户全部会话，防止旧 token 继续访问
            kickoutQuietly(requestDTO.getUserId());

            // 4. 组装响应
            DeleteUserResponseDTO responseDTO = new DeleteUserResponseDTO();
            responseDTO.setUserId(requestDTO.getUserId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("删除用户系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 强制下线用户全部会话（禁用/删除后调用）；
     * 库内变更已提交，踢人失败仅记录日志不影响主流程（会话最长存活至 token 过期）
     */
    private void kickoutQuietly(Long userId) {
        try {
            StpUtil.kickout(userId);
        } catch (Exception e) {
            log.error("强制下线用户会话失败, userId: {}", userId, e);
        }
    }
}
