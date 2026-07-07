package com.sunnao.spring.ddd.template.common.security;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 登录防爆破限制器
 * <p>
 * 基于 Redis 固定窗口计数：按"邮箱 + 客户端IP"维度统计凭证失败次数，
 * 达到上限后拒绝登录直至窗口过期（窗口自首次失败起算）；登录成功即清零。
 * Redis 异常时降级放行（fail-open），仅记录日志，不影响登录主流程。
 */
@Slf4j
@Component
public class LoginAttemptLimiter {

    /**
     * 计数 key 前缀，完整 key：auth:login:fail:{email}:{ip}
     */
    private static final String KEY_PREFIX = "auth:login:fail:";

    /**
     * 窗口内最大失败次数
     */
    @Value("${app.security.login-max-failures:5}")
    private int maxFailures;

    /**
     * 锁定窗口时长（分钟）
     */
    @Value("${app.security.login-lock-minutes:15}")
    private long lockMinutes;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 是否已被锁定（失败次数达到上限）
     *
     * @param email 登录邮箱
     * @param ip    客户端IP（可为 null）
     * @return true-已锁定拒绝登录
     */
    public boolean isBlocked(String email, String ip) {
        try {
            String count = stringRedisTemplate.opsForValue().get(buildKey(email, ip));
            return count != null && Long.parseLong(count) >= maxFailures;
        } catch (Exception e) {
            log.warn("读取登录失败计数异常，降级放行, email: {}", email, e);
            return false;
        }
    }

    /**
     * 记录一次凭证失败（首次失败时设置窗口过期时间）
     *
     * @param email 登录邮箱
     * @param ip    客户端IP（可为 null）
     */
    public void recordFailure(String email, String ip) {
        try {
            String key = buildKey(email, ip);
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                stringRedisTemplate.expire(key, Duration.ofMinutes(lockMinutes));
            }
        } catch (Exception e) {
            log.warn("记录登录失败计数异常, email: {}", email, e);
        }
    }

    /**
     * 登录成功后清零失败计数
     *
     * @param email 登录邮箱
     * @param ip    客户端IP（可为 null）
     */
    public void clear(String email, String ip) {
        try {
            stringRedisTemplate.delete(buildKey(email, ip));
        } catch (Exception e) {
            log.warn("清理登录失败计数异常, email: {}", email, e);
        }
    }

    private String buildKey(String email, String ip) {
        return KEY_PREFIX + email + ":" + (StrUtil.isBlank(ip) ? "unknown" : ip);
    }
}
