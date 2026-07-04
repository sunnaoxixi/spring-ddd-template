package com.sunnao.spring.ddd.template.domain.system.log.repository;

import com.sunnao.spring.ddd.template.common.exception.RepositoryException;
import com.sunnao.spring.ddd.template.common.model.PageQuery;
import com.sunnao.spring.ddd.template.common.model.Repository;
import com.sunnao.spring.ddd.template.domain.system.log.model.aggregate.OperLogAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.model.param.OperLogQuery;
import org.springframework.data.domain.Page;

/**
 * 操作日志仓储接口
 * <p>
 * 定义在 domain 层，实现在 infrastructure 层。
 * 日志只增不改：仅提供保存与分页查询，不提供更新/删除。
 */
public interface OperLogRepository extends Repository {

    /**
     * 保存操作日志（插入后回填ID）
     *
     * @param aggregate 操作日志聚合根
     * @throws RepositoryException 异常
     */
    void save(OperLogAggregate aggregate) throws RepositoryException;

    /**
     * 分页查询操作日志（按操作时间倒序）
     *
     * @param pageQuery 查询条件
     * @return 操作日志分页集合
     * @throws RepositoryException 异常
     */
    Page<OperLogAggregate> queryPage(PageQuery<OperLogQuery> pageQuery) throws RepositoryException;
}
