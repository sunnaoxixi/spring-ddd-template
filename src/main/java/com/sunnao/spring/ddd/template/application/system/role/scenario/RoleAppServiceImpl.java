package com.sunnao.spring.ddd.template.application.system.role.scenario;

import com.sunnao.spring.ddd.template.application.system.role.assembler.RoleAssembler;
import com.sunnao.spring.ddd.template.client.system.role.RoleAppService;
import com.sunnao.spring.ddd.template.client.system.role.req.*;
import com.sunnao.spring.ddd.template.client.system.role.res.*;
import com.sunnao.spring.ddd.template.common.context.CurrentUserContext;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.role.model.aggregate.RoleAggregate;
import com.sunnao.spring.ddd.template.domain.system.role.service.RoleDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 角色应用服务实现（写模式）
 * 职责：场景编排，参数自校验 → DTO 转 Param → 调用领域服务 → 组装响应
 */
@Slf4j
@Service
public class RoleAppServiceImpl implements RoleAppService {

    @Resource
    private RoleDomainService roleDomainService;

    @Override
    public ResultDO<CreateRoleResponseDTO> createRole(CreateRoleRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务创建角色（操作人取自当前登录用户）
            ResultDO<RoleAggregate> domainResult = roleDomainService.createRole(
                    RoleAssembler.toCreateParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            CreateRoleResponseDTO responseDTO = new CreateRoleResponseDTO();
            responseDTO.setRoleId(domainResult.getData().getRoleEntity().getId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("创建角色系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    @Override
    public ResultDO<UpdateRoleResponseDTO> updateRole(UpdateRoleRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务修改角色（操作人取自当前登录用户）
            ResultDO<Void> domainResult = roleDomainService.updateRole(
                    RoleAssembler.toUpdateParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            UpdateRoleResponseDTO responseDTO = new UpdateRoleResponseDTO();
            responseDTO.setRoleId(requestDTO.getRoleId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("修改角色系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    @Override
    public ResultDO<DeleteRoleResponseDTO> deleteRole(DeleteRoleRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务删除角色（操作人取自当前登录用户）
            ResultDO<Void> domainResult = roleDomainService.deleteRole(
                    RoleAssembler.toDeleteParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            DeleteRoleResponseDTO responseDTO = new DeleteRoleResponseDTO();
            responseDTO.setRoleId(requestDTO.getRoleId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("删除角色系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    @Override
    public ResultDO<AssignPermissionResponseDTO> assignPermissions(AssignPermissionRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务分配权限（操作人取自当前登录用户）
            ResultDO<Void> domainResult = roleDomainService.assignPermissions(
                    RoleAssembler.toAssignPermissionParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            AssignPermissionResponseDTO responseDTO = new AssignPermissionResponseDTO();
            responseDTO.setRoleId(requestDTO.getRoleId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("分配权限系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }

    @Override
    public ResultDO<AssignUserRoleResponseDTO> assignUserRoles(AssignUserRoleRequestDTO requestDTO) {
        try {
            // 1. 参数自校验
            ResultDO<Void> checkResult = requestDTO.check();
            if (!checkResult.isSuccess()) {
                return ResultDO.buildFailResult(checkResult.getCode(), checkResult.getMsg());
            }

            // 2. 调用领域服务给用户授角色（操作人取自当前登录用户）
            ResultDO<Void> domainResult = roleDomainService.assignUserRoles(
                    RoleAssembler.toAssignUserRoleParam(requestDTO, CurrentUserContext.getUserId()));
            if (!domainResult.isSuccess()) {
                return ResultDO.buildFailResult(domainResult.getCode(), domainResult.getMsg());
            }

            // 3. 组装响应
            AssignUserRoleResponseDTO responseDTO = new AssignUserRoleResponseDTO();
            responseDTO.setUserId(requestDTO.getUserId());
            return ResultDO.buildSuccessResult(responseDTO);
        } catch (Exception e) {
            log.error("给用户授角色系统异常, requestDTO: {}", requestDTO, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        }
    }
}
