package com.sunnao.spring.ddd.template.domain.system.user.repository;

import com.sunnao.spring.ddd.template.common.exception.RepositoryException;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.model.AggregateRepository;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.UserQuery;

/**
 * 用户仓储接口
 * <p>
 * 定义在 domain 层，实现在 infrastructure 层。
 * 继承基类的 query(Long) / query(UserQuery) / queryPage(PageQuery) / save 能力。
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
     * 逻辑删除用户
     *
     * @param userId     用户ID
     * @param operatorId 操作人ID
     * @throws RepositoryException 异常
     */
    void delete(Long userId, Long operatorId) throws RepositoryException;

    /**
     * 构建分布式锁
     *
     * @param lockKey 锁标识
     * @return 锁对象
     */
    LevelLock buildLock(String lockKey);
}
