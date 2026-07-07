package com.sunnao.spring.ddd.template.domain.system.user.repository;

import com.sunnao.spring.ddd.template.common.exception.RepositoryException;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.model.AggregateRepository;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.UserQuery;

import java.util.List;

/**
 * 用户仓储接口
 * <p>
 * 定义在 domain 层，实现在 infrastructure 层。
 * 继承基类的 query(Long) / query(UserQuery) / queryPage(PageQuery) / save 能力。
 * 用户的角色关联数据归属 role 领域（RoleRepository）；
 * saveWithRoles / deleteWithRoles 为跨仓储事务性组合方法（实现内委托 RoleRepository 并保证同一事务）。
 */
public interface UserRepository extends AggregateRepository<UserAggregate, UserQuery> {

    /**
     * 根据邮箱查询用户（用于邮箱唯一性校验）
     *
     * @param email 邮箱
     * @return 用户聚合根，不存在返回 null
     * @throws RepositoryException 异常
     */
    UserAggregate queryByEmail(String email) throws RepositoryException;

    /**
     * 保存用户并全量覆盖用户角色关联（同一事务，任一失败整体回滚）
     *
     * @param aggregate 用户聚合根（新增时回填ID）
     * @param roleIds   角色ID集合
     * @throws RepositoryException 异常
     */
    void saveWithRoles(UserAggregate aggregate, List<Long> roleIds) throws RepositoryException;

    /**
     * 逻辑删除用户
     *
     * @param userId     用户ID
     * @param operatorId 操作人ID
     * @throws RepositoryException 异常
     */
    void delete(Long userId, Long operatorId) throws RepositoryException;

    /**
     * 逻辑删除用户并清理其角色关联（同一事务，任一失败整体回滚）
     *
     * @param userId     用户ID
     * @param operatorId 操作人ID
     * @throws RepositoryException 异常
     */
    void deleteWithRoles(Long userId, Long operatorId) throws RepositoryException;

    /**
     * 构建分布式锁
     *
     * @param lockKey 锁标识
     * @return 锁对象
     */
    LevelLock buildLock(String lockKey);
}
