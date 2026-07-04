package com.sunnao.spring.ddd.template.common.lock;

import cn.hutool.core.util.IdUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;

/**
 * 分级锁 Redis 实现（分布式，默认）
 * <p>
 * 加锁：SET key token NX PX（token 为随机值，默认 30 秒过期防止死锁）；
 * 释放：Lua 脚本校验 token 后删除，保证只释放自己持有的锁。
 * <p>
 * 注意：不支持重入与自动续期，持锁操作耗时应远小于过期时间。
 */
@Slf4j
public class RedisLevelLock implements LevelLock {

    /** 锁默认过期时间 */
    private static final Duration DEFAULT_EXPIRE = Duration.ofSeconds(30);

    /** 释放锁 Lua 脚本：token 匹配才删除，避免误删他人持有的锁 */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redisTemplate;

    /**
     * 锁标识
     */
    @Getter
    private final String lockKey;

    /** 持有者随机凭证 */
    private final String token;

    public RedisLevelLock(StringRedisTemplate redisTemplate, String lockKey) {
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
        this.token = IdUtil.fastSimpleUUID();
    }

    @Override
    public boolean tryLock() {
        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, token, DEFAULT_EXPIRE);
            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            log.error("Redis 加锁异常, lockKey: {}", lockKey, e);
            return false;
        }
    }

    @Override
    public void unlock() {
        try {
            redisTemplate.execute(UNLOCK_SCRIPT, List.of(lockKey), token);
        } catch (Exception e) {
            // 释放失败不影响主流程，锁到期后自动过期
            log.error("Redis 释放锁异常, lockKey: {}", lockKey, e);
        }
    }
}
