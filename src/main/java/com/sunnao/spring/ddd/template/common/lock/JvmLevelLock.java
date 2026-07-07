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
 * <p>
 * 注册表条目按引用计数管理：tryLock 时计数 +1，加锁失败或 unlock 时计数 -1，
 * 计数归零即移除，避免高基数 lockKey（含邮箱/路径等）导致注册表无界增长。
 */
public class JvmLevelLock implements LevelLock {

    private static final ConcurrentMap<String, LockEntry> LOCK_REGISTRY = new ConcurrentHashMap<>();

    /**
     * 锁标识
     */
    @Getter
    private final String lockKey;

    /**
     * 加锁成功后持有的注册表条目（未加锁/已释放时为 null）
     */
    private LockEntry entry;

    public JvmLevelLock(String lockKey) {
        this.lockKey = lockKey;
    }

    @Override
    public boolean tryLock() {
        // 先引用计数 +1 再尝试加锁，保证条目在使用期间不被并发移除
        LockEntry candidate = LOCK_REGISTRY.compute(lockKey, (key, existing) -> {
            LockEntry e = existing == null ? new LockEntry() : existing;
            e.refCount++;
            return e;
        });
        if (candidate.lock.tryLock()) {
            this.entry = candidate;
            return true;
        }
        releaseReference();
        return false;
    }

    /**
     * 释放锁（仅当前线程持有时生效）
     */
    @Override
    public void unlock() {
        if (entry != null && entry.lock.isHeldByCurrentThread()) {
            entry.lock.unlock();
            releaseReference();
            entry = null;
        }
    }

    /**
     * 引用计数 -1，归零时从注册表移除条目
     */
    private void releaseReference() {
        LOCK_REGISTRY.compute(lockKey, (key, existing) -> {
            if (existing == null) {
                return null;
            }
            existing.refCount--;
            return existing.refCount <= 0 ? null : existing;
        });
    }

    /**
     * 注册表条目：锁实例 + 引用计数（计数变更均在 compute 回调内执行，天然串行）
     */
    private static class LockEntry {

        private final ReentrantLock lock = new ReentrantLock();

        private int refCount;
    }
}
