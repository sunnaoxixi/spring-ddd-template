package com.sunnao.spring.ddd.template.infrastructure.system.role.repository;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.sunnao.spring.ddd.template.common.exception.RepositoryException;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.lock.LockFactory;
import com.sunnao.spring.ddd.template.common.model.PageQuery;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.domain.system.role.model.aggregate.RoleAggregate;
import com.sunnao.spring.ddd.template.domain.system.role.model.entity.PermissionEntity;
import com.sunnao.spring.ddd.template.domain.system.role.model.param.RoleQuery;
import com.sunnao.spring.ddd.template.domain.system.role.repository.RoleRepository;
import com.sunnao.spring.ddd.template.infrastructure.system.role.converter.RoleConverter;
import com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.mapper.PermissionMapper;
import com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.mapper.RoleMapper;
import com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.mapper.RolePermissionMapper;
import com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.mapper.UserRoleMapper;
import com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.po.PermissionPO;
import com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.po.RolePO;
import com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.po.RolePermissionPO;
import com.sunnao.spring.ddd.template.infrastructure.system.role.mysql.po.UserRolePO;
import com.sunnao.spring.ddd.template.model.system.role.RoleStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 角色仓储实现类
 * 职责：聚合根的持久化与查询，角色-权限/用户-角色关联维护，PO 与聚合根的纯技术转换，无业务逻辑
 */
@Slf4j
@Component
public class RoleRepositoryImpl implements RoleRepository {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private LockFactory lockFactory;

    @Override
    public RoleAggregate query(Long id) throws RepositoryException {
        try {
            RolePO po = roleMapper.selectOneById(id);
            RoleAggregate aggregate = RoleConverter.toAggregate(po);
            fillPermKeys(aggregate);
            return aggregate;
        } catch (Exception e) {
            log.error("查询角色失败, id: {}", id, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询角色数据异常", e);
        }
    }

    @Override
    public RoleAggregate query(RoleQuery query) throws RepositoryException {
        try {
            RolePO po = roleMapper.selectOneByQuery(buildWrapper(query));
            return RoleConverter.toAggregate(po);
        } catch (Exception e) {
            log.error("查询角色失败, query: {}", query, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询角色数据异常", e);
        }
    }

    @Override
    public Page<RoleAggregate> queryPage(PageQuery<RoleQuery> pageQuery) throws RepositoryException {
        try {
            int pageSize = pageQuery.getPageSize();
            int pageNumber = pageQuery.getStartIndex() / pageSize + 1;

            com.mybatisflex.core.paginate.Page<RolePO> poPage = roleMapper.paginate(
                    pageNumber, pageSize, buildWrapper(pageQuery.getQuery()));

            List<RoleAggregate> aggregates = RoleConverter.toAggregateList(poPage.getRecords());
            return new PageImpl<>(aggregates, PageRequest.of(pageNumber - 1, pageSize), poPage.getTotalRow());
        } catch (Exception e) {
            log.error("分页查询角色失败, pageQuery: {}", pageQuery.getQuery(), e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "分页查询角色数据异常", e);
        }
    }

    @Override
    public void save(RoleAggregate aggregate) throws RepositoryException {
        try {
            RolePO po = RoleConverter.toPO(aggregate);
            if (po == null) {
                throw new RepositoryException(ErrorCodeEnum.DATA_ERROR, "角色数据为空，无法保存");
            }

            // 审计字段（createAt/updateAt/createBy/updateBy）由全局监听器自动填充
            if (po.getId() == null) {
                // 新增：插入后回填ID到聚合根
                roleMapper.insertSelective(po);
                aggregate.getRoleEntity().setId(po.getId());
                aggregate.getRoleEntity().setCreateAt(po.getCreateAt());
            } else {
                // 更新：仅更新非空字段，创建信息不可变
                po.setCreateAt(null);
                po.setCreateBy(null);
                roleMapper.update(po);
            }
            aggregate.getRoleEntity().setUpdateAt(po.getUpdateAt());
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存角色失败, aggregate: {}", aggregate, e);
            throw new RepositoryException(ErrorCodeEnum.DB_SAVE_ERROR, "保存角色数据异常", e);
        }
    }

    @Override
    public RoleAggregate queryByRoleKey(String roleKey) throws RepositoryException {
        try {
            QueryWrapper wrapper = QueryWrapper.create().eq(RolePO::getRoleKey, roleKey);
            RolePO po = roleMapper.selectOneByQuery(wrapper);
            return RoleConverter.toAggregate(po);
        } catch (Exception e) {
            log.error("根据角色标识查询角色失败, roleKey: {}", roleKey, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询角色数据异常", e);
        }
    }

    @Override
    public List<RoleAggregate> queryByIds(List<Long> roleIds) throws RepositoryException {
        try {
            if (CollUtil.isEmpty(roleIds)) {
                return Collections.emptyList();
            }
            QueryWrapper wrapper = QueryWrapper.create().in(RolePO::getId, roleIds);
            return RoleConverter.toAggregateList(roleMapper.selectListByQuery(wrapper));
        } catch (Exception e) {
            log.error("批量查询角色失败, roleIds: {}", roleIds, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询角色数据异常", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long roleId, Long operatorId) throws RepositoryException {
        try {
            // 1. 记录删除操作人（更新时间由全局监听器自动填充）
            RolePO po = new RolePO();
            po.setId(roleId);
            po.setUpdateBy(operatorId);
            roleMapper.update(po);

            // 2. 逻辑删除角色（deleted 置为 1）
            roleMapper.deleteById(roleId);

            // 3. 清理角色-权限、用户-角色关联（硬删除）
            rolePermissionMapper.deleteByQuery(
                    QueryWrapper.create().eq(RolePermissionPO::getRoleId, roleId));
            userRoleMapper.deleteByQuery(
                    QueryWrapper.create().eq(UserRolePO::getRoleId, roleId));
        } catch (Exception e) {
            log.error("删除角色失败, roleId: {}", roleId, e);
            throw new RepositoryException(ErrorCodeEnum.DB_DELETE_ERROR, "删除角色数据异常", e);
        }
    }

    @Override
    public List<PermissionEntity> queryAllPermissions() throws RepositoryException {
        try {
            QueryWrapper wrapper = QueryWrapper.create().orderBy(PermissionPO::getId, true);
            return RoleConverter.toPermissionEntityList(permissionMapper.selectListByQuery(wrapper));
        } catch (Exception e) {
            log.error("查询全部权限点失败", e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询权限数据异常", e);
        }
    }

    @Override
    public List<PermissionEntity> queryPermissionsByIds(List<Long> permissionIds) throws RepositoryException {
        try {
            if (CollUtil.isEmpty(permissionIds)) {
                return Collections.emptyList();
            }
            QueryWrapper wrapper = QueryWrapper.create().in(PermissionPO::getId, permissionIds);
            return RoleConverter.toPermissionEntityList(permissionMapper.selectListByQuery(wrapper));
        } catch (Exception e) {
            log.error("批量查询权限点失败, permissionIds: {}", permissionIds, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询权限数据异常", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveRolePermissions(Long roleId, List<Long> permissionIds) throws RepositoryException {
        try {
            // 全量覆盖：先删后插
            rolePermissionMapper.deleteByQuery(
                    QueryWrapper.create().eq(RolePermissionPO::getRoleId, roleId));
            if (CollUtil.isEmpty(permissionIds)) {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            List<RolePermissionPO> poList = permissionIds.stream().map(permissionId -> {
                RolePermissionPO po = new RolePermissionPO();
                po.setRoleId(roleId);
                po.setPermissionId(permissionId);
                po.setCreateAt(now);
                return po;
            }).toList();
            rolePermissionMapper.insertBatch(poList);
        } catch (Exception e) {
            log.error("保存角色权限关联失败, roleId: {}, permissionIds: {}", roleId, permissionIds, e);
            throw new RepositoryException(ErrorCodeEnum.DB_SAVE_ERROR, "保存角色权限关联异常", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveUserRoles(Long userId, List<Long> roleIds) throws RepositoryException {
        try {
            // 全量覆盖：先删后插
            userRoleMapper.deleteByQuery(
                    QueryWrapper.create().eq(UserRolePO::getUserId, userId));
            if (CollUtil.isEmpty(roleIds)) {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            List<UserRolePO> poList = roleIds.stream().map(roleId -> {
                UserRolePO po = new UserRolePO();
                po.setUserId(userId);
                po.setRoleId(roleId);
                po.setCreateAt(now);
                return po;
            }).toList();
            userRoleMapper.insertBatch(poList);
        } catch (Exception e) {
            log.error("保存用户角色关联失败, userId: {}, roleIds: {}", userId, roleIds, e);
            throw new RepositoryException(ErrorCodeEnum.DB_SAVE_ERROR, "保存用户角色关联异常", e);
        }
    }

    @Override
    public List<String> queryRoleKeysByUserId(Long userId) throws RepositoryException {
        try {
            List<RolePO> roles = queryEnabledRolesByUserId(userId);
            return roles.stream().map(RolePO::getRoleKey).distinct().toList();
        } catch (Exception e) {
            log.error("查询用户角色标识失败, userId: {}", userId, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询用户角色数据异常", e);
        }
    }

    @Override
    public Map<Long, List<String>> queryRoleKeysByUserIds(List<Long> userIds) throws RepositoryException {
        try {
            if (CollUtil.isEmpty(userIds)) {
                return Collections.emptyMap();
            }
            // 1. 用户角色关联
            List<UserRolePO> userRoles = userRoleMapper.selectListByQuery(
                    QueryWrapper.create().in(UserRolePO::getUserId, userIds));
            if (userRoles.isEmpty()) {
                return Collections.emptyMap();
            }

            // 2. 启用状态的角色
            List<Long> roleIds = userRoles.stream().map(UserRolePO::getRoleId).distinct().toList();
            List<RolePO> roles = roleMapper.selectListByQuery(QueryWrapper.create()
                    .in(RolePO::getId, roleIds)
                    .eq(RolePO::getStatus, RoleStatusEnum.ENABLED.getCode()));
            Map<Long, String> roleKeyById = new HashMap<>();
            roles.forEach(role -> roleKeyById.put(role.getId(), role.getRoleKey()));

            // 3. 按用户分组
            Map<Long, List<String>> result = new HashMap<>();
            for (UserRolePO userRole : userRoles) {
                String roleKey = roleKeyById.get(userRole.getRoleId());
                if (roleKey != null) {
                    result.computeIfAbsent(userRole.getUserId(), k -> new ArrayList<>()).add(roleKey);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("批量查询用户角色标识失败, userIds: {}", userIds, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询用户角色数据异常", e);
        }
    }

    @Override
    public List<String> queryPermKeysByUserId(Long userId) throws RepositoryException {
        try {
            // 1. 用户的启用角色
            List<RolePO> roles = queryEnabledRolesByUserId(userId);
            if (roles.isEmpty()) {
                return Collections.emptyList();
            }
            List<Long> roleIds = roles.stream().map(RolePO::getId).toList();

            // 2. 角色关联的权限ID
            List<RolePermissionPO> rolePermissions = rolePermissionMapper.selectListByQuery(
                    QueryWrapper.create().in(RolePermissionPO::getRoleId, roleIds));
            if (rolePermissions.isEmpty()) {
                return Collections.emptyList();
            }
            List<Long> permissionIds = rolePermissions.stream()
                    .map(RolePermissionPO::getPermissionId).distinct().toList();

            // 3. 权限标识
            List<PermissionPO> permissions = permissionMapper.selectListByQuery(
                    QueryWrapper.create().in(PermissionPO::getId, permissionIds));
            return permissions.stream().map(PermissionPO::getPermKey).distinct().toList();
        } catch (Exception e) {
            log.error("查询用户权限标识失败, userId: {}", userId, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询用户权限数据异常", e);
        }
    }

    @Override
    public LevelLock buildLock(String lockKey) {
        return lockFactory.buildLock(lockKey);
    }

    /**
     * 查询用户关联的启用状态角色
     */
    private List<RolePO> queryEnabledRolesByUserId(Long userId) {
        List<UserRolePO> userRoles = userRoleMapper.selectListByQuery(
                QueryWrapper.create().eq(UserRolePO::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(UserRolePO::getRoleId).distinct().toList();
        return roleMapper.selectListByQuery(QueryWrapper.create()
                .in(RolePO::getId, roleIds)
                .eq(RolePO::getStatus, RoleStatusEnum.ENABLED.getCode()));
    }

    /**
     * 查询聚合根详情时填充权限 key 集合
     */
    private void fillPermKeys(RoleAggregate aggregate) {
        if (aggregate == null || aggregate.getRoleEntity() == null || aggregate.getRoleEntity().getId() == null) {
            return;
        }
        List<RolePermissionPO> rolePermissions = rolePermissionMapper.selectListByQuery(
                QueryWrapper.create().eq(RolePermissionPO::getRoleId, aggregate.getRoleEntity().getId()));
        if (rolePermissions.isEmpty()) {
            aggregate.setPermKeys(Collections.emptyList());
            return;
        }
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermissionPO::getPermissionId).distinct().toList();
        List<PermissionPO> permissions = permissionMapper.selectListByQuery(
                QueryWrapper.create().in(PermissionPO::getId, permissionIds));
        aggregate.setPermKeys(permissions.stream().map(PermissionPO::getPermKey).toList());
    }

    /**
     * 构建查询条件（纯技术转换）
     */
    private QueryWrapper buildWrapper(RoleQuery query) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query == null) {
            return wrapper;
        }
        if (StrUtil.isNotBlank(query.getRoleKey())) {
            wrapper.eq(RolePO::getRoleKey, query.getRoleKey());
        }
        if (StrUtil.isNotBlank(query.getRoleName())) {
            wrapper.like(RolePO::getRoleName, query.getRoleName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(RolePO::getStatus, query.getStatus().getCode());
        }
        wrapper.orderBy(RolePO::getId, false);
        return wrapper;
    }
}
