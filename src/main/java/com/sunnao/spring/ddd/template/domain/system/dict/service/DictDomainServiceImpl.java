package com.sunnao.spring.ddd.template.domain.system.dict.service;

import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.common.exception.BizException;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictDataAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictTypeAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.*;
import com.sunnao.spring.ddd.template.domain.system.dict.repository.DictRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 字典领域服务实现（写模式）
 * <p>
 * 标准流程：获取锁 → 加载聚合根 → 执行聚合根业务方法 → 持久化 → 释放锁。
 * 异常统一捕获并转换为 ResultDO，不向上层抛出。
 * 缓存失效由仓储在写操作内完成，领域层不感知缓存技术细节。
 */
@Slf4j
@Service
public class DictDomainServiceImpl implements DictDomainService {

    @Resource
    private DictRepository dictRepository;

    @Override
    public ResultDO<DictTypeAggregate> createDictType(CreateDictTypeParam param) {
        // 1. 获取锁（按类型键防并发重复创建）
        LevelLock levelLock = dictRepository.buildLock("system:dict:type:create:" + param.getTypeKey());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.LOCK_FAIL);
        }
        try {
            // 2. 类型键唯一性校验
            DictTypeAggregate exist = dictRepository.queryTypeByKey(param.getTypeKey());
            if (exist != null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.TYPE_KEY_DUPLICATE);
            }

            // 3. 构建聚合根
            DictTypeAggregate aggregate = DictTypeAggregate.create(param);

            // 4. 持久化（仓储回填ID）
            dictRepository.save(aggregate);

            return ResultDO.buildSuccessResult(aggregate);
        } catch (BizException e) {
            log.error("创建字典类型业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("创建字典类型系统异常, param: {}", param, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<Void> updateDictType(UpdateDictTypeParam param) {
        // 1. 获取锁
        LevelLock levelLock = dictRepository.buildLock("system:dict:type:update:" + param.getTypeId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.LOCK_FAIL);
        }
        try {
            // 2. 加载聚合根
            DictTypeAggregate aggregate = dictRepository.query(param.getTypeId());
            if (aggregate == null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.DICT_TYPE_NOT_FOUND);
            }

            // 3. 执行业务逻辑（通过聚合根方法）
            aggregate.update(param);

            // 4. 持久化变更（仓储内失效缓存）
            dictRepository.save(aggregate);

            return ResultDO.buildSuccessResult();
        } catch (BizException e) {
            log.error("修改字典类型业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("修改字典类型系统异常, param: {}", param, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<Void> deleteDictType(DeleteDictTypeParam param) {
        // 1. 获取锁
        LevelLock levelLock = dictRepository.buildLock("system:dict:type:update:" + param.getTypeId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.LOCK_FAIL);
        }
        try {
            // 2. 加载聚合根，确认存在
            DictTypeAggregate aggregate = dictRepository.query(param.getTypeId());
            if (aggregate == null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.DICT_TYPE_NOT_FOUND);
            }

            // 3. 逻辑删除类型及其下数据（仓储内失效缓存）
            dictRepository.deleteType(param.getTypeId(),
                    aggregate.getDictTypeEntity().getTypeKey(), param.getOperatorId());

            return ResultDO.buildSuccessResult();
        } catch (BizException e) {
            log.error("删除字典类型业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("删除字典类型系统异常, param: {}", param, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<DictDataAggregate> createDictData(CreateDictDataParam param) {
        // 1. 获取锁（按类型键+字典值防并发重复创建）
        LevelLock levelLock = dictRepository.buildLock(
                "system:dict:data:create:" + param.getTypeKey() + ":" + param.getValue());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.LOCK_FAIL);
        }
        try {
            // 2. 归属类型存在性校验
            DictTypeAggregate type = dictRepository.queryTypeByKey(param.getTypeKey());
            if (type == null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.DICT_TYPE_NOT_FOUND);
            }

            // 3. 同类型下字典值唯一性校验
            DictDataAggregate exist = dictRepository.queryDataByTypeKeyAndValue(
                    param.getTypeKey(), param.getValue());
            if (exist != null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.DICT_VALUE_DUPLICATE);
            }

            // 4. 构建聚合根并持久化（仓储回填ID并失效缓存）
            DictDataAggregate aggregate = DictDataAggregate.create(param);
            dictRepository.saveData(aggregate);

            return ResultDO.buildSuccessResult(aggregate);
        } catch (BizException e) {
            log.error("创建字典数据业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("创建字典数据系统异常, param: {}", param, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<Void> updateDictData(UpdateDictDataParam param) {
        // 1. 获取锁
        LevelLock levelLock = dictRepository.buildLock("system:dict:data:update:" + param.getDataId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.LOCK_FAIL);
        }
        try {
            // 2. 加载聚合根
            DictDataAggregate aggregate = dictRepository.queryData(param.getDataId());
            if (aggregate == null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.DICT_DATA_NOT_FOUND);
            }

            // 3. 变更字典值时校验同类型下唯一性
            String typeKey = aggregate.getDictDataEntity().getTypeKey();
            if (StrUtil.isNotBlank(param.getValue())
                    && !param.getValue().equals(aggregate.getDictDataEntity().getValue())) {
                DictDataAggregate exist = dictRepository.queryDataByTypeKeyAndValue(typeKey, param.getValue());
                if (exist != null) {
                    return ResultDO.buildFailResult(ErrorCodeEnum.DICT_VALUE_DUPLICATE);
                }
            }

            // 4. 执行业务逻辑（通过聚合根方法）
            aggregate.update(param);

            // 5. 持久化变更（仓储内失效缓存）
            dictRepository.saveData(aggregate);

            return ResultDO.buildSuccessResult();
        } catch (BizException e) {
            log.error("修改字典数据业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("修改字典数据系统异常, param: {}", param, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<Void> deleteDictData(DeleteDictDataParam param) {
        // 1. 获取锁
        LevelLock levelLock = dictRepository.buildLock("system:dict:data:update:" + param.getDataId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.LOCK_FAIL);
        }
        try {
            // 2. 加载聚合根，确认存在
            DictDataAggregate aggregate = dictRepository.queryData(param.getDataId());
            if (aggregate == null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.DICT_DATA_NOT_FOUND);
            }

            // 3. 逻辑删除（仓储内失效缓存）
            dictRepository.deleteData(param.getDataId(),
                    aggregate.getDictDataEntity().getTypeKey(), param.getOperatorId());

            return ResultDO.buildSuccessResult();
        } catch (BizException e) {
            log.error("删除字典数据业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("删除字典数据系统异常, param: {}", param, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        } finally {
            levelLock.unlock();
        }
    }
}
