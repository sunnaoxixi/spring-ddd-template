package com.sunnao.spring.ddd.template.domain.system.role.repository;

import com.sunnao.spring.ddd.template.common.exception.RepositoryException;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.model.AggregateRepository;
import com.sunnao.spring.ddd.template.domain.system.role.model.aggregate.RoleAggregate;
import com.sunnao.spring.ddd.template.domain.system.role.model.entity.PermissionEntity;
import com.sunnao.spring.ddd.template.domain.system.role.model.param.RoleQuery;

import java.util.List;
import java.util.Map;

/**
 * 角色仓储接口
 * <p>
 * 定义在 domain 层，实现在 infrastructure 层。
 * 除聚合根 CRUD 外，提供权限点查询、角色-权限/用户-角色关联维护，
 * 以及按用户维度的角色/权限查询（供 Sa-Token StpInterfaceImpl 使用）。
 */
public interface RoleRepository extends AggregateRepository<RoleAggregate, RoleQuery> {

    /**
     * 根据角色标识查询角色（用于唯一性校验）
     *
     * @param roleKey 角色标识
     * @return 角色聚合根，不存在返回 null
     * @throws RepositoryException 异常
     */
    RoleAggregate queryByRoleKey(String roleKey) throws RepositoryException;

    /**
     * 根据角色ID集合批量查询角色
     *
     * @param roleIds 角色ID集合
     * @return 角色聚合根列表
     * @throws RepositoryException 异常
     */
    List<RoleAggregate> queryByIds(List<Long> roleIds) throws RepositoryException;

    /**
     * 逻辑删除角色（同时清理角色-权限、用户-角色关联）
     *
     * @param roleId     角色ID
     * @param operatorId 操作人ID
     * @throws RepositoryException 异常
     */
    void delete(Long roleId, Long operatorId) throws RepositoryException;

    /**
     * 查询全部权限点
     *
     * @return 权限实体列表
     * @throws RepositoryException 异常
     */
    List<PermissionEntity> queryAllPermissions() throws RepositoryException;

    /**
     * 根据权限ID集合批量查询权限点（用于分配权限时校验存在性）
     *
     * @param permissionIds 权限ID集合
     * @return 权限实体列表
     * @throws RepositoryException 异常
     */
    List<PermissionEntity> queryPermissionsByIds(List<Long> permissionIds) throws RepositoryException;

    /**
     * 保存角色的权限关联（全量覆盖：先删后插）
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID集合（空集合表示清空权限）
     * @throws RepositoryException 异常
     */
    void saveRolePermissions(Long roleId, List<Long> permissionIds) throws RepositoryException;

    /**
     * 保存用户的角色关联（全量覆盖：先删后插）
     *
     * @param userId  用户ID
     * @param roleIds 角色ID集合（空集合表示清空角色）
     * @throws RepositoryException 异常
     */
    void saveUserRoles(Long userId, List<Long> roleIds) throws RepositoryException;

    /**
     * 查询用户拥有的角色标识集合（仅启用状态的角色，供 Sa-Token 鉴权）
     *
     * @param userId 用户ID
     * @return 角色标识列表
     * @throws RepositoryException 异常
     */
    List<String> queryRoleKeysByUserId(Long userId) throws RepositoryException;

    /**
     * 批量查询用户拥有的角色标识集合（仅启用状态的角色，供用户列表展示）
     *
     * @param userIds 用户ID集合
     * @return userId → 角色标识列表，无角色的用户不在 Map 中
     * @throws RepositoryException 异常
     */
    Map<Long, List<String>> queryRoleKeysByUserIds(List<Long> userIds) throws RepositoryException;

    /**
     * 查询用户拥有的权限标识集合（经启用状态的角色关联，供 Sa-Token 鉴权）
     *
     * @param userId 用户ID
     * @return 权限标识列表
     * @throws RepositoryException 异常
     */
    List<String> queryPermKeysByUserId(Long userId) throws RepositoryException;

    /**
     * 构建分布式锁
     *
     * @param lockKey 锁标识
     * @return 锁对象
     */
    LevelLock buildLock(String lockKey);
}
