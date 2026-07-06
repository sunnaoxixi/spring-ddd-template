package com.sunnao.spring.ddd.template.domain.system.role.service;

import cn.hutool.core.collection.CollUtil;
import com.sunnao.spring.ddd.template.common.exception.BizException;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.role.model.aggregate.RoleAggregate;
import com.sunnao.spring.ddd.template.domain.system.role.model.entity.PermissionEntity;
import com.sunnao.spring.ddd.template.domain.system.role.model.param.*;
import com.sunnao.spring.ddd.template.domain.system.role.repository.RoleRepository;
import com.sunnao.spring.ddd.template.domain.system.user.repository.UserRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色领域服务实现（写模式）
 * <p>
 * 标准流程：获取锁 → 加载聚合根 → 执行聚合根业务方法 → 持久化 → 释放锁。
 * 异常统一捕获并转换为 ResultDO，不向上层抛出。
 */
@Slf4j
@Service
public class RoleDomainServiceImpl implements RoleDomainService {

    @Resource
    private RoleRepository roleRepository;

    @Resource
    private UserRepository userRepository;

    @Override
    public ResultDO<RoleAggregate> createRole(CreateRoleParam param) {
        // 1. 获取锁（按角色标识防并发重复创建）
        LevelLock levelLock = roleRepository.buildLock("system:role:create:" + param.getRoleKey());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult("LOCK_FAIL", "获取锁失败，请稍后重试");
        }
        try {
            // 2. 角色标识唯一性校验
            RoleAggregate exist = roleRepository.queryByRoleKey(param.getRoleKey());
            if (exist != null) {
                return ResultDO.buildFailResult("ROLE_KEY_DUPLICATE", "角色标识已存在");
            }

            // 3. 构建聚合根
            RoleAggregate aggregate = RoleAggregate.create(param);

            // 4. 持久化（仓储回填ID）
            roleRepository.save(aggregate);

            return ResultDO.buildSuccessResult(aggregate);
        } catch (BizException e) {
            log.error("创建角色业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("创建角色系统异常, param: {}", param, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<Void> updateRole(UpdateRoleParam param) {
        // 1. 获取锁
        LevelLock levelLock = roleRepository.buildLock("system:role:update:" + param.getRoleId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult("LOCK_FAIL", "获取锁失败，请稍后重试");
        }
        try {
            // 2. 加载聚合根
            RoleAggregate aggregate = roleRepository.query(param.getRoleId());
            if (aggregate == null) {
                return ResultDO.buildFailResult("ROLE_NOT_FOUND", "角色不存在");
            }

            // 3. 执行业务逻辑（通过聚合根方法）
            aggregate.update(param);

            // 4. 持久化变更
            roleRepository.save(aggregate);

            return ResultDO.buildSuccessResult();
        } catch (BizException e) {
            log.error("修改角色业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("修改角色系统异常, param: {}", param, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<Void> deleteRole(DeleteRoleParam param) {
        // 1. 获取锁
        LevelLock levelLock = roleRepository.buildLock("system:role:update:" + param.getRoleId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult("LOCK_FAIL", "获取锁失败，请稍后重试");
        }
        try {
            // 2. 加载聚合根，确认存在
            RoleAggregate aggregate = roleRepository.query(param.getRoleId());
            if (aggregate == null) {
                return ResultDO.buildFailResult("ROLE_NOT_FOUND", "角色不存在");
            }

            // 3. 删除前校验（内置角色不可删除）
            aggregate.checkDeletable();

            // 4. 逻辑删除（同时清理关联）
            roleRepository.delete(param.getRoleId(), param.getOperatorId());

            return ResultDO.buildSuccessResult();
        } catch (BizException e) {
            log.error("删除角色业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("删除角色系统异常, param: {}", param, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<Void> assignPermissions(AssignPermissionParam param) {
        // 1. 获取锁
        LevelLock levelLock = roleRepository.buildLock("system:role:update:" + param.getRoleId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult("LOCK_FAIL", "获取锁失败，请稍后重试");
        }
        try {
            // 2. 加载聚合根，确认存在
            RoleAggregate aggregate = roleRepository.query(param.getRoleId());
            if (aggregate == null) {
                return ResultDO.buildFailResult("ROLE_NOT_FOUND", "角色不存在");
            }

            // 3. 校验权限点存在性
            List<Long> permissionIds = param.getPermissionIds() == null
                    ? List.of() : param.getPermissionIds().stream().distinct().toList();
            if (CollUtil.isNotEmpty(permissionIds)) {
                List<PermissionEntity> permissions = roleRepository.queryPermissionsByIds(permissionIds);
                if (permissions.size() != permissionIds.size()) {
                    return ResultDO.buildFailResult("PERMISSION_NOT_FOUND", "存在无效的权限ID");
                }
            }

            // 4. 全量覆盖角色权限关联
            roleRepository.saveRolePermissions(param.getRoleId(), permissionIds);

            return ResultDO.buildSuccessResult();
        } catch (BizException e) {
            log.error("分配权限业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("分配权限系统异常, param: {}", param, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<Void> assignUserRoles(AssignUserRoleParam param) {
        // 1. 获取锁（按用户维度）
        LevelLock levelLock = roleRepository.buildLock("system:role:assign-user:" + param.getUserId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult("LOCK_FAIL", "获取锁失败，请稍后重试");
        }
        try {
            // 2. 校验用户存在
            if (userRepository.query(param.getUserId()) == null) {
                return ResultDO.buildFailResult("USER_NOT_FOUND", "用户不存在");
            }

            // 3. 校验角色存在性
            List<Long> roleIds = param.getRoleIds() == null
                    ? List.of() : param.getRoleIds().stream().distinct().toList();
            if (CollUtil.isNotEmpty(roleIds)) {
                List<RoleAggregate> roles = roleRepository.queryByIds(roleIds);
                if (roles.size() != roleIds.size()) {
                    return ResultDO.buildFailResult("ROLE_NOT_FOUND", "存在无效的角色ID");
                }
            }

            // 4. 全量覆盖用户角色关联
            roleRepository.saveUserRoles(param.getUserId(), roleIds);

            return ResultDO.buildSuccessResult();
        } catch (BizException e) {
            log.error("给用户授角色业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("给用户授角色系统异常, param: {}", param, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        } finally {
            levelLock.unlock();
        }
    }
}
