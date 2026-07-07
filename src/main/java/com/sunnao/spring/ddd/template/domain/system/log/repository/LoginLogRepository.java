package com.sunnao.spring.ddd.template.domain.system.log.repository;

import com.sunnao.spring.ddd.template.common.exception.RepositoryException;
import com.sunnao.spring.ddd.template.common.model.PageQuery;
import com.sunnao.spring.ddd.template.common.model.Repository;
import com.sunnao.spring.ddd.template.domain.system.log.model.aggregate.LoginLogAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.model.param.LoginLogQuery;
import org.springframework.data.domain.Page;

/**
 * 登录日志仓储接口
 * <p>
 * 定义在 domain 层，实现在 infrastructure 层。
 * 日志只增不改：仅提供保存与分页查询，不提供更新/删除。
 */
public interface LoginLogRepository extends Repository {

    /**
     * 保存登录日志（插入后回填ID）
     *
     * @param aggregate 登录日志聚合根
     * @throws RepositoryException 异常
     */
    void save(LoginLogAggregate aggregate) throws RepositoryException;

    /**
     * 分页查询登录日志（按登录时间倒序）
     *
     * @param pageQuery 查询条件
     * @return 登录日志分页集合
     * @throws RepositoryException 异常
     */
    Page<LoginLogAggregate> queryPage(PageQuery<LoginLogQuery> pageQuery) throws RepositoryException;
}
