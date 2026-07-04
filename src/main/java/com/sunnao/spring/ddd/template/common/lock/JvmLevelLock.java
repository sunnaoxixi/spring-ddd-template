package com.sunnao.spring.ddd.template.common.lock;

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 分级锁 JVM 实现（单机）
 * <p>
 * 基于进程内 ReentrantLock，仅适用于单实例部署；
 * 集群部署请使用 RedisLevelLock（app.lock.type: redis，默认值）。
 */
public class JvmLevelLock implements LevelLock {

    private static final ConcurrentMap<String, ReentrantLock> LOCK_REGISTRY = new ConcurrentHashMap<>();

    /**
     * 锁标识
     */
    @Getter
    private final String lockKey;

    private final ReentrantLock lock;

    public JvmLevelLock(String lockKey) {
        this.lockKey = lockKey;
        this.lock = LOCK_REGISTRY.computeIfAbsent(lockKey, key -> new ReentrantLock());
    }

    @Override
    public boolean tryLock() {
        return lock.tryLock();
    }

    /**
     * 释放锁（仅当前线程持有时生效）
     */
    @Override
    public void unlock() {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
