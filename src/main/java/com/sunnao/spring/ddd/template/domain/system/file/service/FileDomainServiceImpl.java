package com.sunnao.spring.ddd.template.domain.system.file.service;

import com.sunnao.spring.ddd.template.common.exception.BizException;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.domain.system.file.model.aggregate.FileAggregate;
import com.sunnao.spring.ddd.template.domain.system.file.model.param.CreateFileParam;
import com.sunnao.spring.ddd.template.domain.system.file.model.param.DeleteFileParam;
import com.sunnao.spring.ddd.template.domain.system.file.repository.FileRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 文件领域服务实现（写模式）
 * <p>
 * 标准流程：获取锁 → 加载/构建聚合根 → 执行聚合根业务方法 → 持久化 → 释放锁。
 * 异常统一捕获并转换为 ResultDO，不向上层抛出。
 */
@Slf4j
@Service
public class FileDomainServiceImpl implements FileDomainService {

    @Resource
    private FileRepository fileRepository;

    @Override
    public ResultDO<FileAggregate> createFile(CreateFileParam param) {
        // 1. 获取锁（按存储路径防重复登记，路径由存储层生成保证唯一）
        LevelLock levelLock = fileRepository.buildLock("system:file:create:" + param.getPath());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.LOCK_FAIL);
        }
        try {
            // 2. 构建聚合根
            FileAggregate aggregate = FileAggregate.create(param);

            // 3. 持久化（仓储回填ID）
            fileRepository.save(aggregate);

            return ResultDO.buildSuccessResult(aggregate);
        } catch (BizException e) {
            log.error("登记文件元数据业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("登记文件元数据系统异常, param: {}", param, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        } finally {
            levelLock.unlock();
        }
    }

    @Override
    public ResultDO<FileAggregate> deleteFile(DeleteFileParam param) {
        // 1. 获取锁
        LevelLock levelLock = fileRepository.buildLock("system:file:update:" + param.getFileId());
        if (!levelLock.tryLock()) {
            return ResultDO.buildFailResult(ErrorCodeEnum.LOCK_FAIL);
        }
        try {
            // 2. 加载聚合根，确认存在
            FileAggregate aggregate = fileRepository.query(param.getFileId());
            if (aggregate == null) {
                return ResultDO.buildFailResult(ErrorCodeEnum.FILE_NOT_FOUND);
            }

            // 3. 逻辑删除元数据
            fileRepository.delete(param.getFileId(), param.getOperatorId());

            // 4. 返回被删聚合根（供应用层清理物理文件）
            return ResultDO.buildSuccessResult(aggregate);
        } catch (BizException e) {
            log.error("删除文件业务异常, param: {}", param, e);
            return ResultDO.buildFailResult(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("删除文件系统异常, param: {}", param, e);
            return ResultDO.buildFailResult(ErrorCodeEnum.SYSTEM_ERROR);
        } finally {
            levelLock.unlock();
        }
    }
}
