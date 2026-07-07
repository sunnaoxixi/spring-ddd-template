package com.sunnao.spring.ddd.template.infrastructure.system.log.repository;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.sunnao.spring.ddd.template.common.exception.RepositoryException;
import com.sunnao.spring.ddd.template.common.model.PageQuery;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.domain.system.log.model.aggregate.LoginLogAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.model.param.LoginLogQuery;
import com.sunnao.spring.ddd.template.domain.system.log.repository.LoginLogRepository;
import com.sunnao.spring.ddd.template.infrastructure.system.log.converter.LoginLogConverter;
import com.sunnao.spring.ddd.template.infrastructure.system.log.mysql.mapper.LoginLogMapper;
import com.sunnao.spring.ddd.template.infrastructure.system.log.mysql.po.LoginLogPO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 登录日志仓储实现类
 * 职责：登录日志的持久化与分页查询，PO 与聚合根的纯技术转换，无业务逻辑
 */
@Slf4j
@Component
public class LoginLogRepositoryImpl implements LoginLogRepository {

    @Resource
    private LoginLogMapper loginLogMapper;

    @Resource
    private LoginLogConverter loginLogConverter;

    @Override
    public void save(LoginLogAggregate aggregate) throws RepositoryException {
        try {
            LoginLogPO po = loginLogConverter.toPO(aggregate);
            if (po == null) {
                throw new RepositoryException(ErrorCodeEnum.DATA_ERROR, "登录日志数据为空，无法保存");
            }
            loginLogMapper.insertSelective(po);
            aggregate.getLoginLogEntity().setId(po.getId());
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存登录日志失败, aggregate: {}", aggregate, e);
            throw new RepositoryException(ErrorCodeEnum.DB_SAVE_ERROR, "保存登录日志异常", e);
        }
    }

    @Override
    public Page<LoginLogAggregate> queryPage(PageQuery<LoginLogQuery> pageQuery) throws RepositoryException {
        try {
            int pageSize = pageQuery.getPageSize();
            int pageNumber = pageQuery.getStartIndex() / pageSize + 1;

            com.mybatisflex.core.paginate.Page<LoginLogPO> poPage = loginLogMapper.paginate(
                    pageNumber, pageSize, buildWrapper(pageQuery.getQuery()));

            List<LoginLogAggregate> aggregates = loginLogConverter.toAggregateList(poPage.getRecords());
            return new PageImpl<>(aggregates, PageRequest.of(pageNumber - 1, pageSize), poPage.getTotalRow());
        } catch (Exception e) {
            log.error("分页查询登录日志失败, pageQuery: {}", pageQuery.getQuery(), e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "分页查询登录日志异常", e);
        }
    }

    /**
     * 构建查询条件（纯技术转换）
     */
    private QueryWrapper buildWrapper(LoginLogQuery query) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query == null) {
            wrapper.orderBy(LoginLogPO::getId, false);
            return wrapper;
        }
        if (StrUtil.isNotBlank(query.getEmail())) {
            wrapper.eq(LoginLogPO::getEmail, query.getEmail());
        }
        if (query.getUserId() != null) {
            wrapper.eq(LoginLogPO::getUserId, query.getUserId());
        }
        if (query.getSuccess() != null) {
            wrapper.eq(LoginLogPO::getSuccess, query.getSuccess());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(LoginLogPO::getCreateAt, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(LoginLogPO::getCreateAt, query.getEndTime());
        }
        wrapper.orderBy(LoginLogPO::getId, false);
        return wrapper;
    }
}
