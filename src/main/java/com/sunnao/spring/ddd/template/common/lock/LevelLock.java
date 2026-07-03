package com.sunnao.spring.ddd.template.common.lock;

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 分级锁
 * <p>
 * 由 Repository 的 buildLock 方法构建，DomainService 在写模式标准流程中通过
 * tryLock / unlock 保证并发安全。
 * <p>
 * 注意：骨架默认提供基于 JVM 的单机实现，生产环境集群部署时应替换为
 * 分布式锁实现（如 Redis），替换时保持 tryLock / unlock 语义不变。
 */
public class LevelLock {

    private static final ConcurrentMap<String, ReentrantLock> LOCK_REGISTRY = new ConcurrentHashMap<>();

    /**
     * 锁标识
     */
    @Getter
    private final String lockKey;

    private final ReentrantLock lock;

    public LevelLock(String lockKey) {
        this.lockKey = lockKey;
        this.lock = LOCK_REGISTRY.computeIfAbsent(lockKey, key -> new ReentrantLock());
    }

    /**
     * 尝试获取锁（不阻塞）
     *
     * @return 是否获取成功
     */
    public boolean tryLock() {
        return lock.tryLock();
    }

    /**
     * 释放锁（仅当前线程持有时生效）
     */
    public void unlock() {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
