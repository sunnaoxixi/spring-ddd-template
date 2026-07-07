package com.sunnao.spring.ddd.template.infrastructure.system.log.repository;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.sunnao.spring.ddd.template.common.exception.RepositoryException;
import com.sunnao.spring.ddd.template.common.model.PageQuery;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.domain.system.log.model.aggregate.OperLogAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.model.param.OperLogQuery;
import com.sunnao.spring.ddd.template.domain.system.log.repository.OperLogRepository;
import com.sunnao.spring.ddd.template.infrastructure.system.log.converter.OperLogConverter;
import com.sunnao.spring.ddd.template.infrastructure.system.log.mysql.mapper.OperLogMapper;
import com.sunnao.spring.ddd.template.infrastructure.system.log.mysql.po.OperLogPO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 操作日志仓储实现类
 * 职责：操作日志的持久化与分页查询，PO 与聚合根的纯技术转换，无业务逻辑
 */
@Slf4j
@Component
public class OperLogRepositoryImpl implements OperLogRepository {

    @Resource
    private OperLogMapper operLogMapper;

    @Resource
    private OperLogConverter operLogConverter;

    @Override
    public void save(OperLogAggregate aggregate) throws RepositoryException {
        try {
            OperLogPO po = operLogConverter.toPO(aggregate);
            if (po == null) {
                throw new RepositoryException(ErrorCodeEnum.DATA_ERROR, "操作日志数据为空，无法保存");
            }
            operLogMapper.insertSelective(po);
            aggregate.getOperLogEntity().setId(po.getId());
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存操作日志失败, aggregate: {}", aggregate, e);
            throw new RepositoryException(ErrorCodeEnum.DB_SAVE_ERROR, "保存操作日志异常", e);
        }
    }

    @Override
    public Page<OperLogAggregate> queryPage(PageQuery<OperLogQuery> pageQuery) throws RepositoryException {
        try {
            int pageSize = pageQuery.getPageSize();
            int pageNumber = pageQuery.getStartIndex() / pageSize + 1;

            com.mybatisflex.core.paginate.Page<OperLogPO> poPage = operLogMapper.paginate(
                    pageNumber, pageSize, buildWrapper(pageQuery.getQuery()));

            List<OperLogAggregate> aggregates = operLogConverter.toAggregateList(poPage.getRecords());
            return new PageImpl<>(aggregates, PageRequest.of(pageNumber - 1, pageSize), poPage.getTotalRow());
        } catch (Exception e) {
            log.error("分页查询操作日志失败, pageQuery: {}", pageQuery.getQuery(), e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "分页查询操作日志异常", e);
        }
    }

    /**
     * 构建查询条件（纯技术转换）
     */
    private QueryWrapper buildWrapper(OperLogQuery query) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query == null) {
            wrapper.orderBy(OperLogPO::getId, false);
            return wrapper;
        }
        if (StrUtil.isNotBlank(query.getModule())) {
            wrapper.eq(OperLogPO::getModule, query.getModule());
        }
        if (query.getOperatorId() != null) {
            wrapper.eq(OperLogPO::getOperatorId, query.getOperatorId());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(OperLogPO::getCreateAt, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(OperLogPO::getCreateAt, query.getEndTime());
        }
        wrapper.orderBy(OperLogPO::getId, false);
        return wrapper;
    }
}
