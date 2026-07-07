package com.sunnao.spring.ddd.template.infrastructure.system.user.repository;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.sunnao.spring.ddd.template.common.exception.RepositoryException;
import com.sunnao.spring.ddd.template.common.lock.LevelLock;
import com.sunnao.spring.ddd.template.common.lock.LockFactory;
import com.sunnao.spring.ddd.template.common.model.PageQuery;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.model.entity.UserEntity;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.UserQuery;
import com.sunnao.spring.ddd.template.domain.system.user.repository.UserRepository;
import com.sunnao.spring.ddd.template.infrastructure.system.user.converter.UserConverter;
import com.sunnao.spring.ddd.template.infrastructure.system.user.mysql.mapper.UserMapper;
import com.sunnao.spring.ddd.template.infrastructure.system.user.mysql.po.UserPO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户仓储实现类
 * 职责：聚合根的持久化与查询，PO 与聚合根的纯技术转换，无业务逻辑
 */
@Slf4j
@Component
public class UserRepositoryImpl implements UserRepository {

    @Resource
    private UserMapper userMapper;

    @Resource
    private LockFactory lockFactory;

    @Override
    public UserAggregate query(Long id) throws RepositoryException {
        try {
            UserPO po = userMapper.selectOneById(id);
            return UserConverter.toAggregate(po);
        } catch (Exception e) {
            log.error("查询用户失败, id: {}", id, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询用户数据异常", e);
        }
    }

    @Override
    public UserAggregate query(UserQuery query) throws RepositoryException {
        try {
            UserPO po = userMapper.selectOneByQuery(buildWrapper(query));
            return UserConverter.toAggregate(po);
        } catch (Exception e) {
            log.error("查询用户失败, query: {}", query, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询用户数据异常", e);
        }
    }

    @Override
    public Page<UserAggregate> queryPage(PageQuery<UserQuery> pageQuery) throws RepositoryException {
        try {
            int pageSize = pageQuery.getPageSize();
            int pageNumber = pageQuery.getStartIndex() / pageSize + 1;

            com.mybatisflex.core.paginate.Page<UserPO> poPage = userMapper.paginate(
                    pageNumber, pageSize, buildWrapper(pageQuery.getQuery()));

            List<UserAggregate> aggregates = UserConverter.toAggregateList(poPage.getRecords());
            return new PageImpl<>(aggregates, PageRequest.of(pageNumber - 1, pageSize), poPage.getTotalRow());
        } catch (Exception e) {
            log.error("分页查询用户失败, pageQuery: {}", pageQuery.getQuery(), e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "分页查询用户数据异常", e);
        }
    }

    @Override
    public void save(UserAggregate aggregate) throws RepositoryException {
        try {
            UserPO po = UserConverter.toPO(aggregate);
            if (po == null) {
                throw new RepositoryException(ErrorCodeEnum.DATA_ERROR, "用户数据为空，无法保存");
            }

            // 审计字段（createAt/updateAt/createBy/updateBy）由全局监听器自动填充
            UserEntity entity = aggregate.getUserEntity();
            if (po.getId() == null) {
                // 新增：插入后回填ID到聚合根
                userMapper.insertSelective(po);
                entity.setId(po.getId());
                entity.setCreateAt(po.getCreateAt());
            } else {
                // 更新：仅更新非空字段，创建信息不可变
                po.setCreateAt(null);
                po.setCreateBy(null);
                userMapper.update(po);
            }
            entity.setUpdateAt(po.getUpdateAt());
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存用户失败, aggregate: {}", aggregate, e);
            throw new RepositoryException(ErrorCodeEnum.DB_SAVE_ERROR, "保存用户数据异常", e);
        }
    }

    @Override
    public UserAggregate queryByEmail(String email) throws RepositoryException {
        try {
            QueryWrapper wrapper = QueryWrapper.create().eq(UserPO::getEmail, email);
            UserPO po = userMapper.selectOneByQuery(wrapper);
            return UserConverter.toAggregate(po);
        } catch (Exception e) {
            log.error("根据邮箱查询用户失败, email: {}", email, e);
            throw new RepositoryException(ErrorCodeEnum.DB_QUERY_ERROR, "查询用户数据异常", e);
        }
    }

    @Override
    public void delete(Long userId, Long operatorId) throws RepositoryException {
        try {
            // 1. 记录删除操作人（更新时间由全局监听器自动填充）
            UserPO po = new UserPO();
            po.setId(userId);
            po.setUpdateBy(operatorId);
            userMapper.update(po);

            // 2. 逻辑删除（deleted 置为 1）
            userMapper.deleteById(userId);
        } catch (Exception e) {
            log.error("删除用户失败, userId: {}", userId, e);
            throw new RepositoryException(ErrorCodeEnum.DB_DELETE_ERROR, "删除用户数据异常", e);
        }
    }

    @Override
    public LevelLock buildLock(String lockKey) {
        return lockFactory.buildLock(lockKey);
    }

    /**
     * 构建查询条件（纯技术转换）
     */
    private QueryWrapper buildWrapper(UserQuery query) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (query == null) {
            return wrapper;
        }
        if (StrUtil.isNotBlank(query.getEmail())) {
            wrapper.eq(UserPO::getEmail, query.getEmail());
        }
        if (StrUtil.isNotBlank(query.getNickname())) {
            wrapper.like(UserPO::getNickname, query.getNickname());
        }
        if (query.getStatus() != null) {
            wrapper.eq(UserPO::getStatus, query.getStatus().getCode());
        }
        wrapper.orderBy(UserPO::getId, false);
        return wrapper;
    }
}
