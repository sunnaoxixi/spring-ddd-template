package com.sunnao.spring.ddd.template.infrastructure.system.dict.repository;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.sunnao.spring.ddd.template.common.exception.RepositoryException;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.lock.LockFactory;
import com.sunnao.spring.ddd.template.common.model.PageQuery;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictDataAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictTypeAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.DictTypeQuery;
import com.sunnao.spring.ddd.template.domain.system.dict.repository.DictRepository;
import com.sunnao.spring.ddd.template.infrastructure.system.dict.converter.DictConverter;
import com.sunnao.spring.ddd.template.infrastructure.system.dict.mysql.mapper.DictDataMapper;
import com.sunnao.spring.ddd.template.infrastructure.system.dict.mysql.mapper.DictTypeMapper;
import com.sunnao.spring.ddd.template.infrastructure.system.dict.mysql.po.DictDataPO;
import com.sunnao.spring.ddd.template.infrastructure.system.dict.mysql.po.DictTypePO;
import com.sunnao.spring.ddd.template.model.system.dict.DictStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * 字典仓储实现类
 * 职责：字典类型/数据的持久化与查询，按 typeKey 的启用数据列表走 Redis 缓存
 * （写操作失效对应 key，缓存读写失败降级为直查数据库），无业务逻辑
 */
@Slf4j
@Component
public class DictRepositoryImpl implements DictRepository {

    /**
     * 缓存 key 前缀，完整 key：dict:data:{typeKey}
     */
    private static final String CACHE_KEY_PREFIX = "dict:data:";

    /**
     * 缓存过期时间（写操作会主动失效，过期兜底防脏数据长存）
     */
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    @Resource
    private DictTypeMapper dictTypeMapper;

    @Resource
    private DictConverter dictConverter;

    @Resource
    private DictDataMapper dictDataMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private LockFactory lockFactory;

    @Override
    public DictTypeAggregate query(Long id) throws RepositoryException {
        try {
            DictTypePO po = dictTypeMapper.selectOneById(id);
            return dictConverter.toTypeAggregate(po);
        } catch (Exception e) {
            log.error("查询字典类型失败, id: {}", id, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询字典类型数据异常", e);
        }
    }

    @Override
    public DictTypeAggregate query(DictTypeQuery query) throws RepositoryException {
        try {
            DictTypePO po = dictTypeMapper.selectOneByQuery(buildTypeWrapper(query));
            return dictConverter.toTypeAggregate(po);
        } catch (Exception e) {
            log.error("查询字典类型失败, query: {}", query, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询字典类型数据异常", e);
        }
    }

    @Override
    public Page<DictTypeAggregate> queryPage(PageQuery<DictTypeQuery> pageQuery) throws RepositoryException {
        try {
            int pageSize = pageQuery.getPageSize();
            int pageNumber = pageQuery.getStartIndex() / pageSize + 1;

            com.mybatisflex.core.paginate.Page<DictTypePO> poPage = dictTypeMapper.paginate(
                    pageNumber, pageSize, buildTypeWrapper(pageQuery.getQuery()));

            List<DictTypeAggregate> aggregates = dictConverter.toTypeAggregateList(poPage.getRecords());
            return new PageImpl<>(aggregates, PageRequest.of(pageNumber - 1, pageSize), poPage.getTotalRow());
        } catch (Exception e) {
            log.error("分页查询字典类型失败, pageQuery: {}", pageQuery.getQuery(), e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "分页查询字典类型数据异常", e);
        }
    }

    @Override
    public void save(DictTypeAggregate aggregate) throws RepositoryException {
        try {
            DictTypePO po = dictConverter.toTypePO(aggregate);
            if (po == null) {
                throw new RepositoryException(ErrorCodeEnum.DATA_ERROR, "字典类型数据为空，无法保存");
            }

            // 审计字段（createAt/updateAt/createBy/updateBy）由全局监听器自动填充
            if (po.getId() == null) {
                // 新增：插入后回填ID到聚合根
                dictTypeMapper.insertSelective(po);
                aggregate.getDictTypeEntity().setId(po.getId());
                aggregate.getDictTypeEntity().setCreateAt(po.getCreateAt());
            } else {
                // 更新：仅更新非空字段，创建信息不可变
                po.setCreateAt(null);
                po.setCreateBy(null);
                dictTypeMapper.update(po);
                // 类型状态变更影响读侧展示，失效对应缓存
                evictCache(aggregate.getDictTypeEntity().getTypeKey());
            }
            aggregate.getDictTypeEntity().setUpdateAt(po.getUpdateAt());
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存字典类型失败, aggregate: {}", aggregate, e);
            throw new RepositoryException(ErrorCodeEnum.DB_SAVE_ERROR, "保存字典类型数据异常", e);
        }
    }

    @Override
    public DictTypeAggregate queryTypeByKey(String typeKey) throws RepositoryException {
        try {
            QueryWrapper wrapper = QueryWrapper.create().eq(DictTypePO::getTypeKey, typeKey);
            DictTypePO po = dictTypeMapper.selectOneByQuery(wrapper);
            return dictConverter.toTypeAggregate(po);
        } catch (Exception e) {
            log.error("根据类型键查询字典类型失败, typeKey: {}", typeKey, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询字典类型数据异常", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteType(Long typeId, String typeKey, Long operatorId) throws RepositoryException {
        try {
            // 1. 记录删除操作人（更新时间由全局监听器自动填充）
            DictTypePO po = new DictTypePO();
            po.setId(typeId);
            po.setUpdateBy(operatorId);
            dictTypeMapper.update(po);

            // 2. 逻辑删除类型（deleted 置为 1）
            dictTypeMapper.deleteById(typeId);

            // 3. 逻辑删除该类型下所有字典数据
            dictDataMapper.deleteByQuery(QueryWrapper.create().eq(DictDataPO::getTypeKey, typeKey));

            // 4. 事务提交后失效缓存（避免提交前并发读回填旧数据）
            evictCacheAfterCommit(typeKey);
        } catch (Exception e) {
            log.error("删除字典类型失败, typeId: {}, typeKey: {}", typeId, typeKey, e);
            throw new RepositoryException(ErrorCodeEnum.DB_DELETE_ERROR, "删除字典类型数据异常", e);
        }
    }

    @Override
    public DictDataAggregate queryData(Long dataId) throws RepositoryException {
        try {
            DictDataPO po = dictDataMapper.selectOneById(dataId);
            return dictConverter.toDataAggregate(po);
        } catch (Exception e) {
            log.error("查询字典数据失败, dataId: {}", dataId, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询字典数据异常", e);
        }
    }

    @Override
    public DictDataAggregate queryDataByTypeKeyAndValue(String typeKey, String value) throws RepositoryException {
        try {
            QueryWrapper wrapper = QueryWrapper.create()
                    .eq(DictDataPO::getTypeKey, typeKey)
                    .eq(DictDataPO::getDictValue, value);
            DictDataPO po = dictDataMapper.selectOneByQuery(wrapper);
            return dictConverter.toDataAggregate(po);
        } catch (Exception e) {
            log.error("根据类型键与字典值查询字典数据失败, typeKey: {}, value: {}", typeKey, value, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询字典数据异常", e);
        }
    }

    @Override
    public void saveData(DictDataAggregate aggregate) throws RepositoryException {
        try {
            DictDataPO po = dictConverter.toDataPO(aggregate);
            if (po == null) {
                throw new RepositoryException(ErrorCodeEnum.DATA_ERROR, "字典数据为空，无法保存");
            }

            // 审计字段（createAt/updateAt/createBy/updateBy）由全局监听器自动填充
            if (po.getId() == null) {
                // 新增：插入后回填ID到聚合根
                dictDataMapper.insertSelective(po);
                aggregate.getDictDataEntity().setId(po.getId());
                aggregate.getDictDataEntity().setCreateAt(po.getCreateAt());
            } else {
                // 更新：仅更新非空字段，创建信息不可变
                po.setCreateAt(null);
                po.setCreateBy(null);
                dictDataMapper.update(po);
            }
            aggregate.getDictDataEntity().setUpdateAt(po.getUpdateAt());

            // 写后失效对应类型缓存
            evictCache(aggregate.getDictDataEntity().getTypeKey());
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存字典数据失败, aggregate: {}", aggregate, e);
            throw new RepositoryException(ErrorCodeEnum.DB_SAVE_ERROR, "保存字典数据异常", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteData(Long dataId, String typeKey, Long operatorId) throws RepositoryException {
        try {
            // 1. 记录删除操作人（更新时间由全局监听器自动填充）
            DictDataPO po = new DictDataPO();
            po.setId(dataId);
            po.setUpdateBy(operatorId);
            dictDataMapper.update(po);

            // 2. 逻辑删除（deleted 置为 1）
            dictDataMapper.deleteById(dataId);

            // 3. 事务提交后失效缓存（避免提交前并发读回填旧数据）
            evictCacheAfterCommit(typeKey);
        } catch (Exception e) {
            log.error("删除字典数据失败, dataId: {}, typeKey: {}", dataId, typeKey, e);
            throw new RepositoryException(ErrorCodeEnum.DB_DELETE_ERROR, "删除字典数据异常", e);
        }
    }

    @Override
    public List<DictDataAggregate> queryEnabledDataByTypeKey(String typeKey) throws RepositoryException {
        // 1. 优先读缓存（缓存异常降级为直查数据库）
        String cacheKey = CACHE_KEY_PREFIX + typeKey;
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StrUtil.isNotBlank(cached)) {
                return dictConverter.toDataAggregateList(JSONUtil.toList(cached, DictDataPO.class));
            }
        } catch (Exception e) {
            log.warn("读取字典缓存失败，降级直查数据库, typeKey: {}", typeKey, e);
        }

        // 2. 回源数据库（类型必须启用，数据仅启用状态，按 sort 升序；类型不存在或已禁用时读侧视为无数据）
        List<DictDataPO> poList;
        try {
            DictTypePO typePO = dictTypeMapper.selectOneByQuery(QueryWrapper.create()
                    .eq(DictTypePO::getTypeKey, typeKey)
                    .eq(DictTypePO::getStatus, DictStatusEnum.ENABLED.getCode()));
            if (typePO == null) {
                poList = Collections.emptyList();
            } else {
                QueryWrapper wrapper = QueryWrapper.create()
                        .eq(DictDataPO::getTypeKey, typeKey)
                        .eq(DictDataPO::getStatus, DictStatusEnum.ENABLED.getCode())
                        .orderBy(DictDataPO::getSort, true)
                        .orderBy(DictDataPO::getId, true);
                poList = dictDataMapper.selectListByQuery(wrapper);
            }
        } catch (Exception e) {
            log.error("按类型键查询字典数据失败, typeKey: {}", typeKey, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询字典数据异常", e);
        }

        // 3. 写缓存（失败不影响主流程）
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(poList), CACHE_TTL);
        } catch (Exception e) {
            log.warn("写入字典缓存失败, typeKey: {}", typeKey, e);
        }
        return dictConverter.toDataAggregateList(poList);
    }

    @Override
    public List<DictDataAggregate> queryAllDataByTypeKey(String typeKey) throws RepositoryException {
        try {
            QueryWrapper wrapper = QueryWrapper.create()
                    .eq(DictDataPO::getTypeKey, typeKey)
                    .orderBy(DictDataPO::getSort, true)
                    .orderBy(DictDataPO::getId, true);
            return dictConverter.toDataAggregateList(dictDataMapper.selectListByQuery(wrapper));
        } catch (Exception e) {
            log.error("按类型键查询全部字典数据失败, typeKey: {}", typeKey, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询字典数据异常", e);
        }
    }

    @Override
    public LevelLock buildLock(String lockKey) {
        return lockFactory.buildLock(lockKey);
    }

    /**
     * 在当前事务提交后失效缓存；无活动事务时立即失效。
     * 若在事务提交前失效，并发读可能在“失效 → 提交”窗口内回源旧数据并重新写入缓存
     */
    private void evictCacheAfterCommit(String typeKey) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evictCache(typeKey);
                }
            });
            return;
        }
        evictCache(typeKey);
    }

    /**
     * 失效指定类型的字典数据缓存（失败仅记录日志，TTL 兜底）
     */
    private void evictCache(String typeKey) {
        if (StrUtil.isBlank(typeKey)) {
            return;
        }
        try {
            stringRedisTemplate.delete(CACHE_KEY_PREFIX + typeKey);
        } catch (Exception e) {
            log.warn("失效字典缓存失败, typeKey: {}", typeKey, e);
        }
    }

    /**
     * 构建字典类型查询条件（纯技术转换）
     */
    private QueryWrapper buildTypeWrapper(DictTypeQuery query) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query == null) {
            return wrapper;
        }
        if (StrUtil.isNotBlank(query.getTypeKey())) {
            wrapper.eq(DictTypePO::getTypeKey, query.getTypeKey());
        }
        if (StrUtil.isNotBlank(query.getTypeName())) {
            wrapper.like(DictTypePO::getTypeName, query.getTypeName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(DictTypePO::getStatus, query.getStatus().getCode());
        }
        wrapper.orderBy(DictTypePO::getId, false);
        return wrapper;
    }
}
