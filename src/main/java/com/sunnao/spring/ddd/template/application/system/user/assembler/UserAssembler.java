package com.sunnao.spring.ddd.template.application.system.user.assembler;

import com.sunnao.spring.ddd.template.client.system.user.res.UserDTO;
import com.sunnao.spring.ddd.template.client.system.user.req.*;
import com.sunnao.spring.ddd.template.client.system.user.res.GetUserDetailResponseDTO;
import com.sunnao.spring.ddd.template.client.system.user.res.QueryUserPageResponseDTO;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.model.entity.UserEntity;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.*;
import com.sunnao.spring.ddd.template.model.system.user.UserStatusEnum;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

/**
 * 用户转换器
 * 负责 RequestDTO/ResponseDTO 与领域对象之间的转换
 */
@Mapper(componentModel = "spring")
public interface UserAssembler {

    /**
     * 创建用户 RequestDTO 转领域 Param（操作人由应用层从当前用户上下文获取）
     */
    @Mapping(target = "operatorId", expression = "java(operatorId)")
    CreateUserParam toCreateParam(CreateUserRequestDTO requestDTO, @Context Long operatorId);

    /**
     * 修改用户资料 RequestDTO 转领域 Param（操作人由应用层从当前用户上下文获取）
     */
    @Mapping(target = "operatorId", expression = "java(operatorId)")
    UpdateUserParam toUpdateParam(UpdateUserRequestDTO requestDTO, @Context Long operatorId);

    /**
     * 变更用户状态 RequestDTO 转领域 Param（client 状态码 → model 枚举，操作人由应用层从当前用户上下文获取）
     */
    default ChangeUserStatusParam toChangeStatusParam(ChangeUserStatusRequestDTO requestDTO, Long operatorId) {
        ChangeUserStatusParam param = new ChangeUserStatusParam();
        param.setUserId(requestDTO.getUserId());
        param.setTargetStatus(UserStatusEnum.getByCode(requestDTO.getStatus()));
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 删除用户 RequestDTO 转领域 Param（操作人由应用层从当前用户上下文获取）
     */
    @Mapping(target = "operatorId", expression = "java(operatorId)")
    DeleteUserParam toDeleteParam(DeleteUserRequestDTO requestDTO, @Context Long operatorId);

    /**
     * 分页查询 RequestDTO 转领域查询条件
     */
    default UserQuery toUserQuery(QueryUserPageRequestDTO requestDTO) {
        UserQuery query = new UserQuery();
        query.setEmail(requestDTO.getEmail());
        query.setNickname(requestDTO.getNickname());
        query.setStatus(UserStatusEnum.getByCode(requestDTO.getStatus()));
        return query;
    }

    /**
     * 聚合根转 UserDTO（不含密码，model 枚举 → client 状态码）
     */
    default UserDTO toUserDTO(UserAggregate aggregate) {
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
        dto.setRoles(entity.getRoles() == null ? Collections.emptyList() : entity.getRoles());
        dto.setCreateAt(entity.getCreateAt());
        dto.setUpdateAt(entity.getUpdateAt());
        return dto;
    }

    /**
     * 聚合根转用户详情 ResponseDTO
     */
    default GetUserDetailResponseDTO toGetUserDetailResponseDTO(UserAggregate aggregate) {
        GetUserDetailResponseDTO responseDTO = new GetUserDetailResponseDTO();
        responseDTO.setUser(toUserDTO(aggregate));
        return responseDTO;
    }

    /**
     * 聚合根列表转分页 ResponseDTO
     */
    default QueryUserPageResponseDTO toQueryUserPageResponseDTO(long total, List<UserAggregate> aggregates) {
        QueryUserPageResponseDTO responseDTO = new QueryUserPageResponseDTO();
        responseDTO.setTotal(total);
        if (aggregates == null || aggregates.isEmpty()) {
            responseDTO.setUsers(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setUsers(aggregates.stream().map(this::toUserDTO).toList());
        return responseDTO;
    }

    // ========== 枚举转换辅助方法 ==========

    @Named("intToUserStatus")
    default UserStatusEnum intToUserStatus(Integer code) {
        return UserStatusEnum.getByCode(code);
    }

    @Named("userStatusToInt")
    default Integer userStatusToInt(UserStatusEnum status) {
        return status == null ? null : status.getCode();
    }
}
