package com.sunnao.spring.ddd.template.infrastructure.system.file.repository;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.sunnao.spring.ddd.template.common.exception.RepositoryException;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.lock.LockFactory;
import com.sunnao.spring.ddd.template.common.model.PageQuery;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.domain.system.file.model.aggregate.FileAggregate;
import com.sunnao.spring.ddd.template.domain.system.file.model.param.FileQuery;
import com.sunnao.spring.ddd.template.domain.system.file.repository.FileRepository;
import com.sunnao.spring.ddd.template.infrastructure.system.file.converter.FileConverter;
import com.sunnao.spring.ddd.template.infrastructure.system.file.mysql.mapper.FileMapper;
import com.sunnao.spring.ddd.template.infrastructure.system.file.mysql.po.FilePO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文件仓储实现类
 * 职责：文件元数据的持久化与查询，PO 与聚合根的纯技术转换，无业务逻辑
 */
@Slf4j
@Component
public class FileRepositoryImpl implements FileRepository {

    @Resource
    private FileMapper fileMapper;

    @Resource
    private LockFactory lockFactory;

    @Override
    public FileAggregate query(Long id) throws RepositoryException {
        try {
            FilePO po = fileMapper.selectOneById(id);
            return FileConverter.toAggregate(po);
        } catch (Exception e) {
            log.error("查询文件失败, id: {}", id, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询文件数据异常", e);
        }
    }

    @Override
    public FileAggregate query(FileQuery query) throws RepositoryException {
        try {
            FilePO po = fileMapper.selectOneByQuery(buildWrapper(query));
            return FileConverter.toAggregate(po);
        } catch (Exception e) {
            log.error("查询文件失败, query: {}", query, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询文件数据异常", e);
        }
    }

    @Override
    public Page<FileAggregate> queryPage(PageQuery<FileQuery> pageQuery) throws RepositoryException {
        try {
            int pageSize = pageQuery.getPageSize();
            int pageNumber = pageQuery.getStartIndex() / pageSize + 1;

            com.mybatisflex.core.paginate.Page<FilePO> poPage = fileMapper.paginate(
                    pageNumber, pageSize, buildWrapper(pageQuery.getQuery()));

            List<FileAggregate> aggregates = FileConverter.toAggregateList(poPage.getRecords());
            return new PageImpl<>(aggregates, PageRequest.of(pageNumber - 1, pageSize), poPage.getTotalRow());
        } catch (Exception e) {
            log.error("分页查询文件失败, pageQuery: {}", pageQuery.getQuery(), e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "分页查询文件数据异常", e);
        }
    }

    @Override
    public void save(FileAggregate aggregate) throws RepositoryException {
        try {
            FilePO po = FileConverter.toPO(aggregate);
            if (po == null) {
                throw new RepositoryException(ErrorCodeEnum.DATA_ERROR, "文件数据为空，无法保存");
            }

            // 审计字段（createAt/updateAt/createBy/updateBy）由全局监听器自动填充
            if (po.getId() == null) {
                // 新增：插入后回填ID到聚合根
                fileMapper.insertSelective(po);
                aggregate.getFileEntity().setId(po.getId());
                aggregate.getFileEntity().setCreateAt(po.getCreateAt());
            } else {
                // 更新：仅更新非空字段，创建信息不可变
                po.setCreateAt(null);
                po.setCreateBy(null);
                fileMapper.update(po);
            }
            aggregate.getFileEntity().setUpdateAt(po.getUpdateAt());
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存文件失败, aggregate: {}", aggregate, e);
            throw new RepositoryException(ErrorCodeEnum.DB_SAVE_ERROR, "保存文件数据异常", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long fileId, Long operatorId) throws RepositoryException {
        try {
            // 1. 记录删除操作人（更新时间由全局监听器自动填充）
            FilePO po = new FilePO();
            po.setId(fileId);
            po.setUpdateBy(operatorId);
            fileMapper.update(po);

            // 2. 逻辑删除（deleted 置为 1）
            fileMapper.deleteById(fileId);
        } catch (Exception e) {
            log.error("删除文件失败, fileId: {}", fileId, e);
            throw new RepositoryException(ErrorCodeEnum.DB_DELETE_ERROR, "删除文件数据异常", e);
        }
    }

    @Override
    public LevelLock buildLock(String lockKey) {
        return lockFactory.buildLock(lockKey);
    }

    /**
     * 构建查询条件（纯技术转换）
     */
    private QueryWrapper buildWrapper(FileQuery query) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query == null) {
            return wrapper;
        }
        if (StrUtil.isNotBlank(query.getOriginalName())) {
            wrapper.like(FilePO::getOriginalName, query.getOriginalName());
        }
        if (query.getUploadBy() != null) {
            wrapper.eq(FilePO::getCreateBy, query.getUploadBy());
        }
        wrapper.orderBy(FilePO::getId, false);
        return wrapper;
    }
}
