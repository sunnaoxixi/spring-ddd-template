package com.sunnao.spring.ddd.template.domain.auth.service;

import cn.hutool.crypto.digest.BCrypt;
import com.sunnao.spring.ddd.template.common.exception.BizException;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.auth.model.param.LoginParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.repository.UserRepository;
import com.sunnao.spring.ddd.template.model.system.user.UserStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 认证领域服务实现
 * <p>
 * 认证流程：加载用户 → 校验密码 → 校验账号状态。
 * 用户不存在与密码错误统一返回相同提示，防止账号枚举。
 * 异常统一捕获并转换为 ResultDO，不向上层抛出。
 */
@Slf4j
@Service
public class AuthDomainServiceImpl implements AuthDomainService {

    @Resource
    private UserRepository userRepository;

    @Override
    public ResultDO<UserAggregate> authenticate(LoginParam param) {
        try {
            // 1. 按邮箱加载用户（不存在与密码错误统一提示，防账号枚举）
            UserAggregate aggregate = userRepository.queryByEmail(param.getEmail());
            if (aggregate == null || aggregate.getUserEntity() == null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.AUTH_FAIL);
            }

            // 2. 校验密码（BCrypt 明文比对密文）
            if (!BCrypt.checkpw(param.getPassword(), aggregate.getUserEntity().getPassword())) {
                return ResultDO.buildFailResult(ErrorCodeEnum.AUTH_FAIL);
            }

            // 3. 校验账号状态
            if (!UserStatusEnum.ENABLED.equals(aggregate.getUserEntity().getStatus())) {
                return ResultDO.buildFailResult(ErrorCodeEnum.USER_DISABLED);
            }

            return ResultDO.buildSuccessResult(aggregate);
        } catch (BizException e) {
            log.error("登录认证业务异常, email: {}", param.getEmail(), e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("登录认证系统异常, email: {}", param.getEmail(), e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }
}
