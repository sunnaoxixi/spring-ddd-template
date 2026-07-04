package com.sunnao.spring.ddd.template.domain.system.user.service;

import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.DomainService;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.ChangeUserStatusParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.CreateUserParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.DeleteUserParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.UpdateUserParam;

/**
 * 用户领域服务接口（写模式）
 * <p>
 * 封装用户领域核心业务逻辑，维护聚合根完整性与一致性。
 */
public interface UserDomainService extends DomainService {

    /**
     * 创建用户
     *
     * @param param 创建参数
     * @return 用户聚合根（含回填的用户ID）
     */
    ResultDO<UserAggregate> createUser(CreateUserParam param);

    /**
     * 修改用户资料
     *
     * @param param 修改参数
     * @return 操作结果
     */
    ResultDO<Void> updateUser(UpdateUserParam param);

    /**
     * 变更用户状态（启用/禁用）
     *
     * @param param 变更参数
     * @return 操作结果
     */
    ResultDO<Void> changeUserStatus(ChangeUserStatusParam param);

    /**
     * 删除用户（逻辑删除）
     *
     * @param param 删除参数
     * @return 操作结果
     */
    ResultDO<Void> deleteUser(DeleteUserParam param);
}
