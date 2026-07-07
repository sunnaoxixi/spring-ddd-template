package com.sunnao.spring.ddd.template.application.system.role.assembler;

import com.sunnao.spring.ddd.template.client.system.role.model.PermissionDTO;
import com.sunnao.spring.ddd.template.client.system.role.model.RoleDTO;
import com.sunnao.spring.ddd.template.client.system.role.req.*;
import com.sunnao.spring.ddd.template.client.system.role.res.GetRoleDetailResponseDTO;
import com.sunnao.spring.ddd.template.client.system.role.res.QueryPermissionListResponseDTO;
import com.sunnao.spring.ddd.template.client.system.role.res.QueryRolePageResponseDTO;
import com.sunnao.spring.ddd.template.domain.system.role.model.aggregate.RoleAggregate;
import com.sunnao.spring.ddd.template.domain.system.role.model.entity.PermissionEntity;
import com.sunnao.spring.ddd.template.domain.system.role.model.entity.RoleEntity;
import com.sunnao.spring.ddd.template.domain.system.role.model.param.*;
import com.sunnao.spring.ddd.template.model.system.role.RoleStatusEnum;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

/**
 * 角色转换器
 * 负责 RequestDTO/ResponseDTO 与领域对象之间的转换
 */
@Mapper(componentModel = "spring")
public interface RoleAssembler {

    /**
     * 创建角色 RequestDTO 转领域 Param（操作人由应用层从当前用户上下文获取）
     */
    @Mapping(target = "operatorId", expression = "java(operatorId)")
    CreateRoleParam toCreateParam(CreateRoleRequestDTO requestDTO, @Context Long operatorId);

    /**
     * 修改角色 RequestDTO 转领域 Param（client 状态码 → model 枚举）
     */
    default UpdateRoleParam toUpdateParam(UpdateRoleRequestDTO requestDTO, Long operatorId) {
        UpdateRoleParam param = new UpdateRoleParam();
        param.setRoleId(requestDTO.getRoleId());
        param.setRoleName(requestDTO.getRoleName());
        param.setStatus(RoleStatusEnum.getByCode(requestDTO.getStatus()));
        param.setRemark(requestDTO.getRemark());
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 删除角色 RequestDTO 转领域 Param
     */
    @Mapping(target = "operatorId", expression = "java(operatorId)")
    DeleteRoleParam toDeleteParam(DeleteRoleRequestDTO requestDTO, @Context Long operatorId);

    /**
     * 分配权限 RequestDTO 转领域 Param
     */
    @Mapping(target = "operatorId", expression = "java(operatorId)")
    AssignPermissionParam toAssignPermissionParam(AssignPermissionRequestDTO requestDTO, @Context Long operatorId);

    /**
     * 给用户授角色 RequestDTO 转领域 Param
     */
    @Mapping(target = "operatorId", expression = "java(operatorId)")
    AssignUserRoleParam toAssignUserRoleParam(AssignUserRoleRequestDTO requestDTO, @Context Long operatorId);

    /**
     * 分页查询 RequestDTO 转领域查询条件
     */
    default RoleQuery toRoleQuery(QueryRolePageRequestDTO requestDTO) {
        RoleQuery query = new RoleQuery();
        query.setRoleKey(requestDTO.getRoleKey());
        query.setRoleName(requestDTO.getRoleName());
        query.setStatus(RoleStatusEnum.getByCode(requestDTO.getStatus()));
        return query;
    }

    /**
     * 聚合根转 RoleDTO（model 枚举 → client 状态码）
     */
    default RoleDTO toRoleDTO(RoleAggregate aggregate) {
        if (aggregate == null || aggregate.getRoleEntity() == null) {
            return null;
        }
        RoleEntity entity = aggregate.getRoleEntity();
        RoleDTO dto = new RoleDTO();
        dto.setId(entity.getId());
        dto.setRoleKey(entity.getRoleKey());
        dto.setRoleName(entity.getRoleName());
        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus().getCode());
        }
        dto.setRemark(entity.getRemark());
        dto.setCreateAt(entity.getCreateAt());
        dto.setUpdateAt(entity.getUpdateAt());
        return dto;
    }

    /**
     * 聚合根转角色详情 ResponseDTO（含权限 key 集合）
     */
    default GetRoleDetailResponseDTO toGetRoleDetailResponseDTO(RoleAggregate aggregate) {
        GetRoleDetailResponseDTO responseDTO = new GetRoleDetailResponseDTO();
        responseDTO.setRole(toRoleDTO(aggregate));
        responseDTO.setPermKeys(aggregate.getPermKeys() == null
                ? Collections.emptyList() : aggregate.getPermKeys());
        return responseDTO;
    }

    /**
     * 聚合根列表转分页 ResponseDTO
     */
    default QueryRolePageResponseDTO toQueryRolePageResponseDTO(long total, List<RoleAggregate> aggregates) {
        QueryRolePageResponseDTO responseDTO = new QueryRolePageResponseDTO();
        responseDTO.setTotal(total);
        if (aggregates == null || aggregates.isEmpty()) {
            responseDTO.setRoles(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setRoles(aggregates.stream().map(this::toRoleDTO).toList());
        return responseDTO;
    }

    /**
     * 权限实体列表转 ResponseDTO
     */
    default QueryPermissionListResponseDTO toQueryPermissionListResponseDTO(List<PermissionEntity> entities) {
        QueryPermissionListResponseDTO responseDTO = new QueryPermissionListResponseDTO();
        if (entities == null || entities.isEmpty()) {
            responseDTO.setPermissions(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setPermissions(entities.stream().map(this::toPermissionDTO).toList());
        return responseDTO;
    }

    /**
     * 权限实体转 PermissionDTO
     */
    PermissionDTO toPermissionDTO(PermissionEntity entity);

    // ========== 枚举转换辅助方法 ==========

    @Named("intToRoleStatus")
    default RoleStatusEnum intToRoleStatus(Integer code) {
        return RoleStatusEnum.getByCode(code);
    }

    @Named("roleStatusToInt")
    default Integer roleStatusToInt(RoleStatusEnum status) {
        return status == null ? null : status.getCode();
    }
}
