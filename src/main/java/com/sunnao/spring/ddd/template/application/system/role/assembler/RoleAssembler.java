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

import java.util.Collections;
import java.util.List;

/**
 * 角色转换器
 * 负责 RequestDTO/ResponseDTO 与领域对象之间的转换
 */
public class RoleAssembler {

    private RoleAssembler() {
    }

    /**
     * 创建角色 RequestDTO 转领域 Param（操作人由应用层从当前用户上下文获取）
     */
    public static CreateRoleParam toCreateParam(CreateRoleRequestDTO requestDTO, Long operatorId) {
        CreateRoleParam param = new CreateRoleParam();
        param.setRoleKey(requestDTO.getRoleKey());
        param.setRoleName(requestDTO.getRoleName());
        param.setRemark(requestDTO.getRemark());
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 修改角色 RequestDTO 转领域 Param（client 状态码 → model 枚举）
     */
    public static UpdateRoleParam toUpdateParam(UpdateRoleRequestDTO requestDTO, Long operatorId) {
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
    public static DeleteRoleParam toDeleteParam(DeleteRoleRequestDTO requestDTO, Long operatorId) {
        DeleteRoleParam param = new DeleteRoleParam();
        param.setRoleId(requestDTO.getRoleId());
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 分配权限 RequestDTO 转领域 Param
     */
    public static AssignPermissionParam toAssignPermissionParam(AssignPermissionRequestDTO requestDTO,
                                                                Long operatorId) {
        AssignPermissionParam param = new AssignPermissionParam();
        param.setRoleId(requestDTO.getRoleId());
        param.setPermissionIds(requestDTO.getPermissionIds());
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 给用户授角色 RequestDTO 转领域 Param
     */
    public static AssignUserRoleParam toAssignUserRoleParam(AssignUserRoleRequestDTO requestDTO, Long operatorId) {
        AssignUserRoleParam param = new AssignUserRoleParam();
        param.setUserId(requestDTO.getUserId());
        param.setRoleIds(requestDTO.getRoleIds());
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 分页查询 RequestDTO 转领域查询条件
     */
    public static RoleQuery toRoleQuery(QueryRolePageRequestDTO requestDTO) {
        RoleQuery query = new RoleQuery();
        query.setRoleKey(requestDTO.getRoleKey());
        query.setRoleName(requestDTO.getRoleName());
        query.setStatus(RoleStatusEnum.getByCode(requestDTO.getStatus()));
        return query;
    }

    /**
     * 聚合根转 RoleDTO（model 枚举 → client 状态码）
     */
    public static RoleDTO toRoleDTO(RoleAggregate aggregate) {
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
    public static GetRoleDetailResponseDTO toGetRoleDetailResponseDTO(RoleAggregate aggregate) {
        GetRoleDetailResponseDTO responseDTO = new GetRoleDetailResponseDTO();
        responseDTO.setRole(toRoleDTO(aggregate));
        responseDTO.setPermKeys(aggregate.getPermKeys() == null
                ? Collections.emptyList() : aggregate.getPermKeys());
        return responseDTO;
    }

    /**
     * 聚合根列表转分页 ResponseDTO
     */
    public static QueryRolePageResponseDTO toQueryRolePageResponseDTO(long total, List<RoleAggregate> aggregates) {
        QueryRolePageResponseDTO responseDTO = new QueryRolePageResponseDTO();
        responseDTO.setTotal(total);
        if (aggregates == null || aggregates.isEmpty()) {
            responseDTO.setRoles(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setRoles(aggregates.stream().map(RoleAssembler::toRoleDTO).toList());
        return responseDTO;
    }

    /**
     * 权限实体列表转 ResponseDTO
     */
    public static QueryPermissionListResponseDTO toQueryPermissionListResponseDTO(List<PermissionEntity> entities) {
        QueryPermissionListResponseDTO responseDTO = new QueryPermissionListResponseDTO();
        if (entities == null || entities.isEmpty()) {
            responseDTO.setPermissions(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setPermissions(entities.stream().map(RoleAssembler::toPermissionDTO).toList());
        return responseDTO;
    }

    /**
     * 权限实体转 PermissionDTO
     */
    public static PermissionDTO toPermissionDTO(PermissionEntity entity) {
        if (entity == null) {
            return null;
        }
        PermissionDTO dto = new PermissionDTO();
        dto.setId(entity.getId());
        dto.setPermKey(entity.getPermKey());
        dto.setPermName(entity.getPermName());
        dto.setRemark(entity.getRemark());
        return dto;
    }
}
