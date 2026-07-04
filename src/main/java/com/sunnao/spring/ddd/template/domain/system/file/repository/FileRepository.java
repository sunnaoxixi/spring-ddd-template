package com.sunnao.spring.ddd.template.domain.system.file.repository;

import com.sunnao.spring.ddd.template.common.exception.RepositoryException;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.model.AggregateRepository;
import com.sunnao.spring.ddd.template.domain.system.file.model.aggregate.FileAggregate;
import com.sunnao.spring.ddd.template.domain.system.file.model.param.FileQuery;

/**
 * 文件仓储接口
 * <p>
 * 定义在 domain 层，实现在 infrastructure 层。
 * 只负责文件元数据的持久化；物理文件由应用层通过 FileStorage 抽象管理。
 */
public interface FileRepository extends AggregateRepository<FileAggregate, FileQuery> {

    /**
     * 逻辑删除文件元数据
     *
     * @param fileId     文件ID
     * @param operatorId 操作人ID
     * @throws RepositoryException 异常
     */
    void delete(Long fileId, Long operatorId) throws RepositoryException;

    /**
     * 构建分布式锁
     *
     * @param lockKey 锁标识
     * @return 锁对象
     */
    LevelLock buildLock(String lockKey);
}
