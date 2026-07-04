package com.sunnao.spring.ddd.template.client.system.role;

import com.sunnao.spring.ddd.template.client.system.role.req.AssignPermissionRequestDTO;
import com.sunnao.spring.ddd.template.client.system.role.req.AssignUserRoleRequestDTO;
import com.sunnao.spring.ddd.template.client.system.role.req.CreateRoleRequestDTO;
import com.sunnao.spring.ddd.template.client.system.role.req.DeleteRoleRequestDTO;
import com.sunnao.spring.ddd.template.client.system.role.req.UpdateRoleRequestDTO;
import com.sunnao.spring.ddd.template.client.system.role.res.AssignPermissionResponseDTO;
import com.sunnao.spring.ddd.template.client.system.role.res.AssignUserRoleResponseDTO;
import com.sunnao.spring.ddd.template.client.system.role.res.CreateRoleResponseDTO;
import com.sunnao.spring.ddd.template.client.system.role.res.DeleteRoleResponseDTO;
import com.sunnao.spring.ddd.template.client.system.role.res.UpdateRoleResponseDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.ApplicationCmdService;

/**
 * 角色应用服务接口（写模式）
 * 职责：定义角色相关的写操作接口
 */
public interface RoleAppService extends ApplicationCmdService {

    /**
     * 创建角色
     *
     * @param requestDTO 请求参数
     * @return 创建结果
     */
    ResultDO<CreateRoleResponseDTO> createRole(CreateRoleRequestDTO requestDTO);

    /**
     * 修改角色（名称/状态/备注）
     *
     * @param requestDTO 请求参数
     * @return 修改结果
     */
    ResultDO<UpdateRoleResponseDTO> updateRole(UpdateRoleRequestDTO requestDTO);

    /**
     * 删除角色（逻辑删除）
     *
     * @param requestDTO 请求参数
     * @return 删除结果
     */
    ResultDO<DeleteRoleResponseDTO> deleteRole(DeleteRoleRequestDTO requestDTO);

    /**
     * 给角色分配权限（全量覆盖）
     *
     * @param requestDTO 请求参数
     * @return 分配结果
     */
    ResultDO<AssignPermissionResponseDTO> assignPermissions(AssignPermissionRequestDTO requestDTO);

    /**
     * 给用户授予角色（全量覆盖）
     *
     * @param requestDTO 请求参数
     * @return 授予结果
     */
    ResultDO<AssignUserRoleResponseDTO> assignUserRoles(AssignUserRoleRequestDTO requestDTO);
}
