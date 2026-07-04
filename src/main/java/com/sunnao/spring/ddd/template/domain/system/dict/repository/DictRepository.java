package com.sunnao.spring.ddd.template.domain.system.dict.repository;

import com.sunnao.spring.ddd.template.common.exception.RepositoryException;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.model.AggregateRepository;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictDataAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictTypeAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.DictTypeQuery;

import java.util.List;

/**
 * 字典仓储接口
 * <p>
 * 定义在 domain 层，实现在 infrastructure 层。
 * 以字典类型为主聚合（继承基类 CRUD），字典数据提供独立的读写方法；
 * 按 typeKey 查询字典数据走 Redis 缓存，写操作负责失效对应缓存 key。
 */
public interface DictRepository extends AggregateRepository<DictTypeAggregate, DictTypeQuery> {

    /**
     * 根据类型键查询字典类型（用于唯一性校验）
     *
     * @param typeKey 字典类型键
     * @return 字典类型聚合根，不存在返回 null
     * @throws RepositoryException 异常
     */
    DictTypeAggregate queryTypeByKey(String typeKey) throws RepositoryException;

    /**
     * 逻辑删除字典类型（同时逻辑删除其下所有字典数据，并失效缓存）
     *
     * @param typeId     字典类型ID
     * @param typeKey    字典类型键
     * @param operatorId 操作人ID
     * @throws RepositoryException 异常
     */
    void deleteType(Long typeId, String typeKey, Long operatorId) throws RepositoryException;

    /**
     * 根据ID查询字典数据
     *
     * @param dataId 字典数据ID
     * @return 字典数据聚合根，不存在返回 null
     * @throws RepositoryException 异常
     */
    DictDataAggregate queryData(Long dataId) throws RepositoryException;

    /**
     * 根据类型键与字典值查询字典数据（用于同类型下值唯一性校验）
     *
     * @param typeKey 字典类型键
     * @param value   字典值
     * @return 字典数据聚合根，不存在返回 null
     * @throws RepositoryException 异常
     */
    DictDataAggregate queryDataByTypeKeyAndValue(String typeKey, String value) throws RepositoryException;

    /**
     * 保存字典数据（新增回填ID，更新仅更新非空字段；写后失效对应类型缓存）
     *
     * @param aggregate 字典数据聚合根
     * @throws RepositoryException 异常
     */
    void saveData(DictDataAggregate aggregate) throws RepositoryException;

    /**
     * 逻辑删除字典数据（并失效对应类型缓存）
     *
     * @param dataId     字典数据ID
     * @param typeKey    字典类型键
     * @param operatorId 操作人ID
     * @throws RepositoryException 异常
     */
    void deleteData(Long dataId, String typeKey, Long operatorId) throws RepositoryException;

    /**
     * 按类型键查询启用状态的字典数据列表（按 sort 升序，优先读 Redis 缓存，未命中回源并写缓存）
     *
     * @param typeKey 字典类型键
     * @return 字典数据聚合根列表
     * @throws RepositoryException 异常
     */
    List<DictDataAggregate> queryEnabledDataByTypeKey(String typeKey) throws RepositoryException;

    /**
     * 按类型键查询全部字典数据列表（管理端使用，不走缓存，按 sort 升序）
     *
     * @param typeKey 字典类型键
     * @return 字典数据聚合根列表
     * @throws RepositoryException 异常
     */
    List<DictDataAggregate> queryAllDataByTypeKey(String typeKey) throws RepositoryException;

    /**
     * 构建分布式锁
     *
     * @param lockKey 锁标识
     * @return 锁对象
     */
    LevelLock buildLock(String lockKey);
}
