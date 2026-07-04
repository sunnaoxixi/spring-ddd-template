package com.sunnao.spring.ddd.template.common.lock;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 分级锁工厂
 * <p>
 * 由各 Repository 实现类注入，buildLock 方法委托本工厂构建锁实例，
 * 领域服务只依赖 LevelLock 接口，无需感知锁的具体实现。
 * <p>
 * 通过配置 app.lock.type 切换实现：redis（默认，分布式）| jvm（单机）。
 */
@Component
public class LockFactory {

    /** 锁类型：redis | jvm */
    @Value("${app.lock.type:redis}")
    private String lockType;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 构建分级锁
     *
     * @param lockKey 锁标识
     * @return 锁实例
     */
    public LevelLock buildLock(String lockKey) {
        if ("jvm".equalsIgnoreCase(lockType)) {
            return new JvmLevelLock(lockKey);
        }
        return new RedisLevelLock(stringRedisTemplate, lockKey);
    }
}
