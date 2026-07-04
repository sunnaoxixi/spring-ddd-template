package com.sunnao.spring.ddd.template.client.system.role;

import com.sunnao.spring.ddd.template.client.system.role.req.GetRoleDetailRequestDTO;
import com.sunnao.spring.ddd.template.client.system.role.req.QueryRolePageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.role.res.GetRoleDetailResponseDTO;
import com.sunnao.spring.ddd.template.client.system.role.res.QueryPermissionListResponseDTO;
import com.sunnao.spring.ddd.template.client.system.role.res.QueryRolePageResponseDTO;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.ApplicationQueryService;

/**
 * 角色查询应用服务接口（读模式）
 * 职责：定义角色相关的查询接口
 */
public interface RoleQueryAppService extends ApplicationQueryService {

    /**
     * 获取角色详情（含权限 key 集合）
     *
     * @param requestDTO 请求参数
     * @return 角色详情
     */
    ResultDO<GetRoleDetailResponseDTO> getRoleDetail(GetRoleDetailRequestDTO requestDTO);

    /**
     * 分页查询角色列表
     *
     * @param requestDTO 请求参数
     * @return 分页结果
     */
    ResultDO<QueryRolePageResponseDTO> queryRolePage(QueryRolePageRequestDTO requestDTO);

    /**
     * 查询全部权限点（供分配权限时选择）
     *
     * @return 权限点列表
     */
    ResultDO<QueryPermissionListResponseDTO> queryPermissionList();
}
