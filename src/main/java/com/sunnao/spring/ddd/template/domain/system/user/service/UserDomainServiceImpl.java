package com.sunnao.spring.ddd.template.domain.system.user.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.sunnao.spring.ddd.template.common.event.DomainEventPublisher;
import com.sunnao.spring.ddd.template.common.exception.BizException;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.role.model.aggregate.RoleAggregate;
import com.sunnao.spring.ddd.template.domain.system.role.repository.RoleRepository;
import com.sunnao.spring.ddd.template.domain.system.user.event.UserCreatedEvent;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.model.entity.UserEntity;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.ChangeUserStatusParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.CreateUserParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.DeleteUserParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.UpdateUserParam;
import com.sunnao.spring.ddd.template.domain.system.user.repository.UserRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户领域服务实现（写模式）
 * <p>
 * 标准流程：获取锁 → 加载聚合根 → 执行聚合根业务方法 → 持久化 → 释放锁。
 * 异常统一捕获并转换为 ResultDO，不向上层抛出。
 */
@Slf4j
@Service
public class UserDomainServiceImpl implements UserDomainService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RoleRepository roleRepository;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public ResultDO<UserAggregate> createUser(CreateUserParam param) {
        // 1. 获取锁（按邮箱防并发重复创建）
        LevelLock levelLock = userRepository.buildLock("system:user:create:" + param.getEmail());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.LOCK_FAIL);
        }
        try {
            // 2. 邮箱唯一性校验
            UserAggregate exist = userRepository.queryByEmail(param.getEmail());
            if (exist != null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.EMAIL_DUPLICATE);
            }

            // 3. 解析角色（未指定时默认授予 user 角色）
            List<RoleAggregate> roles = resolveRoles(param.getRoleIds());
            if (roles.isEmpty()) {
                return ResultDO.buildFailResult(ErrorCodeEnum.ROLE_NOT_FOUND, "存在无效的角色ID");
            }

            // 4. 密码加密后构建聚合根
            String encodedPassword = BCrypt.hashpw(param.getPassword());
            UserAggregate aggregate = UserAggregate.create(param, encodedPassword);

            // 5. 持久化 + 建立用户角色关联（同一事务，仓储回填ID）
            userRepository.saveWithRoles(aggregate,
                    roles.stream().map(role -> role.getRoleEntity().getId()).toList());
            UserEntity entity = aggregate.getUserEntity();
            entity.setRoles(roles.stream().map(role -> role.getRoleEntity().getRoleKey()).toList());

            // 6. 发布领域事件（异步消费，失败不影响主流程）
            domainEventPublisher.publish(new UserCreatedEvent(
                    entity.getId(), entity.getEmail(), entity.getNickname(), param.getOperatorId()));

            return ResultDO.buildSuccessResult(aggregate);
        } catch (BizException e) {
            log.error("创建用户业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建用户系统异常, param: {}", param, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<Void> updateUser(UpdateUserParam param) {
        // 1. 获取锁
        LevelLock levelLock = userRepository.buildLock("system:user:update:" + param.getUserId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.LOCK_FAIL);
        }
        try {
            // 2. 加载聚合根
            UserAggregate aggregate = userRepository.query(param.getUserId());
            if (aggregate == null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.USER_NOT_FOUND);
            }

            // 3. 执行业务逻辑（通过聚合根方法）
            aggregate.updateProfile(param);

            // 4. 持久化变更
            userRepository.save(aggregate);

            return ResultDO.buildSuccessResult();
        } catch (BizException e) {
            log.error("修改用户资料业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("修改用户资料系统异常, param: {}", param, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<Void> changeUserStatus(ChangeUserStatusParam param) {
        // 1. 获取锁
        LevelLock levelLock = userRepository.buildLock("system:user:update:" + param.getUserId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.LOCK_FAIL);
        }
        try {
            // 2. 加载聚合根
            UserAggregate aggregate = userRepository.query(param.getUserId());
            if (aggregate == null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.USER_NOT_FOUND);
            }

            // 3. 执行业务逻辑（通过聚合根方法）
            aggregate.changeStatus(param);

            // 4. 持久化变更
            userRepository.save(aggregate);

            return ResultDO.buildSuccessResult();
        } catch (BizException e) {
            log.error("变更用户状态业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("变更用户状态系统异常, param: {}", param, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<Void> deleteUser(DeleteUserParam param) {
        // 1. 获取锁
        LevelLock levelLock = userRepository.buildLock("system:user:update:" + param.getUserId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.LOCK_FAIL);
        }
        try {
            // 2. 加载聚合根，确认存在
            UserAggregate aggregate = userRepository.query(param.getUserId());
            if (aggregate == null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.USER_NOT_FOUND);
            }

            // 3. 逻辑删除 + 清理用户角色关联（同一事务）
            userRepository.deleteWithRoles(param.getUserId(), param.getOperatorId());

            return ResultDO.buildSuccessResult();
        } catch (BizException e) {
            log.error("删除用户业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除用户系统异常, param: {}", param, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        } finally {
            levelLock.unlock();
        }
    }

    /**
     * 解析创建用户的角色集合：未指定时默认授予 user 角色；
     * 指定时校验角色存在性，存在无效ID返回空列表由调用方转失败结果
     */
    private List<RoleAggregate> resolveRoles(List<Long> roleIds) throws BizException {
        if (CollUtil.isEmpty(roleIds)) {
            RoleAggregate defaultRole = roleRepository.queryByRoleKey("user");
            if (defaultRole == null) {
                throw new BizException(ErrorCodeEnum.ROLE_NOT_FOUND, "默认角色 user 不存在，请检查初始化数据");
            }
            return List.of(defaultRole);
        }
        List<Long> distinctIds = roleIds.stream().distinct().toList();
        List<RoleAggregate> roles = roleRepository.queryByIds(distinctIds);
        if (roles.size() != distinctIds.size()) {
            return List.of();
        }
        return roles;
    }
}
