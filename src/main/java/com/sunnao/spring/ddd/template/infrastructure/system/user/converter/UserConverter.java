package com.sunnao.spring.ddd.template.infrastructure.system.user.converter;

import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.model.entity.UserEntity;
import com.sunnao.spring.ddd.template.infrastructure.system.user.mysql.po.UserPO;
import com.sunnao.spring.ddd.template.model.system.user.UserStatusEnum;

import java.util.Collections;
import java.util.List;

/**
 * 用户数据转换器
 * 职责：聚合根（内部 UserEntity）与 PO 之间的纯技术转换，无业务逻辑
 */
public class UserConverter {

    private UserConverter() {
    }

    /**
     * PO 转换为聚合根（内部构建 UserEntity）
     */
    public static UserAggregate toAggregate(UserPO po) {
        if (po == null) {
            return null;
        }
        UserEntity entity = new UserEntity();
        entity.setId(po.getId());
        entity.setEmail(po.getEmail());
        entity.setNickname(po.getNickname());
        entity.setPassword(po.getPassword());
        entity.setAvatar(po.getAvatar());
        // 枚举转换：数据库 Integer → 领域枚举（roles 归属 role 领域，由应用层按需填充）
        entity.setStatus(UserStatusEnum.getByCode(po.getStatus()));
        entity.setCreateAt(po.getCreateAt());
        entity.setUpdateAt(po.getUpdateAt());
        entity.setCreateBy(po.getCreateBy());
        entity.setUpdateBy(po.getUpdateBy());

        UserAggregate aggregate = new UserAggregate();
        aggregate.setUserEntity(entity);
        return aggregate;
    }

    /**
     * 聚合根转换为 PO（拆解内部 UserEntity）
     */
    public static UserPO toPO(UserAggregate aggregate) {
        if (aggregate == null || aggregate.getUserEntity() == null) {
            return null;
        }
        UserEntity entity = aggregate.getUserEntity();
        UserPO po = new UserPO();
        po.setId(entity.getId());
        po.setEmail(entity.getEmail());
        po.setNickname(entity.getNickname());
        po.setPassword(entity.getPassword());
        po.setAvatar(entity.getAvatar());
        // 枚举转换：领域枚举 → 数据库 Integer
        if (entity.getStatus() != null) {
            po.setStatus(entity.getStatus().getCode());
        }
        po.setCreateAt(entity.getCreateAt());
        po.setUpdateAt(entity.getUpdateAt());
        po.setCreateBy(entity.getCreateBy());
        po.setUpdateBy(entity.getUpdateBy());
        return po;
    }

    /**
     * PO 列表转换为聚合根列表
     */
    public static List<UserAggregate> toAggregateList(List<UserPO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(UserConverter::toAggregate).toList();
    }
}
