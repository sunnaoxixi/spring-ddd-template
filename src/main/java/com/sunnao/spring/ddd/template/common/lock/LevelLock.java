package com.sunnao.spring.ddd.template.common.lock;

/**
 * 分级锁接口
 * <p>
 * 由 LockFactory 构建（Repository 实现侧注入工厂并实现 buildLock），
 * DomainService 在写模式标准流程中通过 tryLock / unlock 保证并发安全。
 * <p>
 * 实现：JvmLevelLock（单机）、RedisLevelLock（分布式，默认），
 * 通过配置 app.lock.type: jvm|redis 切换，两者保持相同语义。
 */
public interface LevelLock {

    /**
     * 尝试获取锁（不阻塞）
     *
     * @return 是否获取成功
     */
    boolean tryLock();

    /**
     * 释放锁（仅当前持有者释放生效）
     */
    void unlock();

    /**
     * 获取锁标识
     *
     * @return 锁标识
     */
    String getLockKey();
}
