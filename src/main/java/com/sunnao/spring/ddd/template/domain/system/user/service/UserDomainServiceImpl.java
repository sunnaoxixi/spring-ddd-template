package com.sunnao.spring.ddd.template.domain.system.user.service;

import cn.hutool.crypto.digest.BCrypt;
import com.sunnao.spring.ddd.template.common.event.DomainEventPublisher;
import com.sunnao.spring.ddd.template.common.exception.BizException;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
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
    private DomainEventPublisher domainEventPublisher;

    @Override
    public ResultDO<UserAggregate> createUser(CreateUserParam param) {
        // 1. 获取锁（按邮箱防并发重复创建）
        LevelLock levelLock = userRepository.buildLock("system:user:create:" + param.getEmail());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult("LOCK_FAIL", "获取锁失败，请稍后重试");
        }
        try {
            // 2. 邮箱唯一性校验
            UserAggregate exist = userRepository.queryByEmail(param.getEmail());
            if (exist != null) {
                return ResultDO.buildFailResult("EMAIL_DUPLICATE", "邮箱已被注册");
            }

            // 3. 密码加密后构建聚合根
            String encodedPassword = BCrypt.hashpw(param.getPassword());
            UserAggregate aggregate = UserAggregate.create(param, encodedPassword);

            // 4. 持久化（仓储回填ID）
            userRepository.save(aggregate);

            // 5. 发布领域事件（异步消费，失败不影响主流程）
            UserEntity entity = aggregate.getUserEntity();
            domainEventPublisher.publish(new UserCreatedEvent(
                    entity.getId(), entity.getEmail(), entity.getNickname(), param.getOperatorId()));

            return ResultDO.buildSuccessResult(aggregate);
        } catch (BizException e) {
            log.error("创建用户业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("创建用户系统异常, param: {}", param, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<Void> updateUser(UpdateUserParam param) {
        // 1. 获取锁
        LevelLock levelLock = userRepository.buildLock("system:user:update:" + param.getUserId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult("LOCK_FAIL", "获取锁失败，请稍后重试");
        }
        try {
            // 2. 加载聚合根
            UserAggregate aggregate = userRepository.query(param.getUserId());
            if (aggregate == null) {
                return ResultDO.buildFailResult("USER_NOT_FOUND", "用户不存在");
            }

            // 3. 执行业务逻辑（通过聚合根方法）
            aggregate.updateProfile(param);

            // 4. 持久化变更
            userRepository.save(aggregate);

            return ResultDO.buildSuccessResult();
        } catch (BizException e) {
            log.error("修改用户资料业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("修改用户资料系统异常, param: {}", param, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<Void> changeUserStatus(ChangeUserStatusParam param) {
        // 1. 获取锁
        LevelLock levelLock = userRepository.buildLock("system:user:update:" + param.getUserId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult("LOCK_FAIL", "获取锁失败，请稍后重试");
        }
        try {
            // 2. 加载聚合根
            UserAggregate aggregate = userRepository.query(param.getUserId());
            if (aggregate == null) {
                return ResultDO.buildFailResult("USER_NOT_FOUND", "用户不存在");
            }

            // 3. 执行业务逻辑（通过聚合根方法）
            aggregate.changeStatus(param);

            // 4. 持久化变更
            userRepository.save(aggregate);

            return ResultDO.buildSuccessResult();
        } catch (BizException e) {
            log.error("变更用户状态业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("变更用户状态系统异常, param: {}", param, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<Void> deleteUser(DeleteUserParam param) {
        // 1. 获取锁
        LevelLock levelLock = userRepository.buildLock("system:user:update:" + param.getUserId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult("LOCK_FAIL", "获取锁失败，请稍后重试");
        }
        try {
            // 2. 加载聚合根，确认存在
            UserAggregate aggregate = userRepository.query(param.getUserId());
            if (aggregate == null) {
                return ResultDO.buildFailResult("USER_NOT_FOUND", "用户不存在");
            }

            // 3. 逻辑删除
            userRepository.delete(param.getUserId(), param.getOperatorId());

            return ResultDO.buildSuccessResult();
        } catch (BizException e) {
            log.error("删除用户业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("删除用户系统异常, param: {}", param, e);
            return ResultDO.buildFailResult("SYSTEM_ERROR", "系统异常");
        } finally {
            levelLock.unlock();
        }
    }
}
