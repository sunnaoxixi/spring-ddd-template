package com.sunnao.spring.ddd.template.domain.system.role.service;

import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.DomainService;
import com.sunnao.spring.ddd.template.domain.system.role.model.aggregate.RoleAggregate;
import com.sunnao.spring.ddd.template.domain.system.role.model.param.*;

/**
 * 角色领域服务接口（写模式）
 * <p>
 * 封装角色领域核心业务逻辑：角色 CRUD、给用户授角色。
 */
public interface RoleDomainService extends DomainService {

    /**
     * 创建角色
     *
     * @param param 创建参数
     * @return 角色聚合根（含回填的角色ID）
     */
    ResultDO<RoleAggregate> createRole(CreateRoleParam param);

    /**
     * 修改角色（名称/状态/备注）
     *
     * @param param 修改参数
     * @return 操作结果
     */
    ResultDO<Void> updateRole(UpdateRoleParam param);

    /**
     * 删除角色（逻辑删除，内置角色不可删除）
     *
     * @param param 删除参数
     * @return 操作结果
     */
    ResultDO<Void> deleteRole(DeleteRoleParam param);

    /**
     * 给用户授予角色（全量覆盖）
     *
     * @param param 授予参数
     * @return 操作结果
     */
    ResultDO<Void> assignUserRoles(AssignUserRoleParam param);
}
