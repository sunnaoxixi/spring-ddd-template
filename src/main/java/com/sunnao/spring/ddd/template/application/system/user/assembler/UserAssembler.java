package com.sunnao.spring.ddd.template.application.system.user.assembler;

import com.sunnao.spring.ddd.template.client.system.user.model.UserDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.ChangeUserStatusRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.CreateUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.DeleteUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.QueryUserPageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.UpdateUserRequestDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.GetUserDetailResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.QueryUserPageResponseDTO;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.model.entity.UserEntity;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.ChangeUserStatusParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.CreateUserParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.DeleteUserParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.UpdateUserParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.UserQuery;
import com.sunnao.spring.ddd.template.model.system.user.UserRoleEnum;
import com.sunnao.spring.ddd.template.model.system.user.UserStatusEnum;

import java.util.Collections;
import java.util.List;

/**
 * 用户转换器
 * 负责 RequestDTO/ResponseDTO 与领域对象之间的转换
 */
public class UserAssembler {

    private UserAssembler() {
    }

    /**
     * 创建用户 RequestDTO 转领域 Param（操作人由应用层从当前用户上下文获取）
     */
    public static CreateUserParam toCreateParam(CreateUserRequestDTO requestDTO, Long operatorId) {
        CreateUserParam param = new CreateUserParam();
        param.setEmail(requestDTO.getEmail());
        param.setNickname(requestDTO.getNickname());
        param.setPassword(requestDTO.getPassword());
        param.setAvatar(requestDTO.getAvatar());
        // client 角色码 → model 枚举
        param.setRole(UserRoleEnum.getByCode(requestDTO.getRole()));
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 修改用户资料 RequestDTO 转领域 Param（操作人由应用层从当前用户上下文获取）
     */
    public static UpdateUserParam toUpdateParam(UpdateUserRequestDTO requestDTO, Long operatorId) {
        UpdateUserParam param = new UpdateUserParam();
        param.setUserId(requestDTO.getUserId());
        param.setNickname(requestDTO.getNickname());
        param.setAvatar(requestDTO.getAvatar());
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 变更用户状态 RequestDTO 转领域 Param（client 状态码 → model 枚举，操作人由应用层从当前用户上下文获取）
     */
    public static ChangeUserStatusParam toChangeStatusParam(ChangeUserStatusRequestDTO requestDTO, Long operatorId) {
        ChangeUserStatusParam param = new ChangeUserStatusParam();
        param.setUserId(requestDTO.getUserId());
        param.setTargetStatus(UserStatusEnum.getByCode(requestDTO.getStatus()));
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 删除用户 RequestDTO 转领域 Param（操作人由应用层从当前用户上下文获取）
     */
    public static DeleteUserParam toDeleteParam(DeleteUserRequestDTO requestDTO, Long operatorId) {
        DeleteUserParam param = new DeleteUserParam();
        param.setUserId(requestDTO.getUserId());
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 分页查询 RequestDTO 转领域查询条件
     */
    public static UserQuery toUserQuery(QueryUserPageRequestDTO requestDTO) {
        UserQuery query = new UserQuery();
        query.setEmail(requestDTO.getEmail());
        query.setNickname(requestDTO.getNickname());
        query.setStatus(UserStatusEnum.getByCode(requestDTO.getStatus()));
        return query;
    }

    /**
     * 聚合根转 UserDTO（不含密码，model 枚举 → client 状态码）
     */
    public static UserDTO toUserDTO(UserAggregate aggregate) {
        if (aggregate == null || aggregate.getUserEntity() == null) {
            return null;
        }
        UserEntity entity = aggregate.getUserEntity();
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setNickname(entity.getNickname());
        dto.setAvatar(entity.getAvatar());
        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus().getCode());
        }
        if (entity.getRole() != null) {
            dto.setRole(entity.getRole().getCode());
        }
        dto.setCreateAt(entity.getCreateAt());
        dto.setUpdateAt(entity.getUpdateAt());
        return dto;
    }

    /**
     * 聚合根转用户详情 ResponseDTO
     */
    public static GetUserDetailResponseDTO toGetUserDetailResponseDTO(UserAggregate aggregate) {
        GetUserDetailResponseDTO responseDTO = new GetUserDetailResponseDTO();
        responseDTO.setUser(toUserDTO(aggregate));
        return responseDTO;
    }

    /**
     * 聚合根列表转分页 ResponseDTO
     */
    public static QueryUserPageResponseDTO toQueryUserPageResponseDTO(long total, List<UserAggregate> aggregates) {
        QueryUserPageResponseDTO responseDTO = new QueryUserPageResponseDTO();
        responseDTO.setTotal(total);
        if (aggregates == null || aggregates.isEmpty()) {
            responseDTO.setUsers(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setUsers(aggregates.stream().map(UserAssembler::toUserDTO).toList());
        return responseDTO;
    }
}
