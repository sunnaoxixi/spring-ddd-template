package com.sunnao.spring.ddd.template.infrastructure.system.user.converter;

import com.sunnao.spring.ddd.template.domain.system.user.model.aggregate.UserAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.model.entity.UserEntity;
import com.sunnao.spring.ddd.template.infrastructure.system.user.mysql.po.UserPO;
import com.sunnao.spring.ddd.template.model.system.user.UserStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

/**
 * 用户数据转换器
 * 职责：聚合根（内部 UserEntity）与 PO 之间的纯技术转换，无业务逻辑
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    /**
     * PO 转换为 UserEntity（枚举转换：数据库 Integer → 领域枚举）
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "intToUserStatus")
    @Mapping(target = "roles", ignore = true)
    UserEntity toEntity(UserPO po);

    /**
     * UserEntity 转换为 PO（枚举转换：领域枚举 → 数据库 Integer）
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "userStatusToInt")
    @Mapping(target = "deleted", ignore = true)
    UserPO toUserPO(UserEntity entity);

    /**
     * PO 转换为聚合根（内部构建 UserEntity）
     */
    default UserAggregate toAggregate(UserPO po) {
        if (po == null) {
            return null;
        }
        UserAggregate aggregate = new UserAggregate();
        aggregate.setUserEntity(toEntity(po));
        return aggregate;
    }

    /**
     * 聚合根转换为 PO（拆解内部 UserEntity）
     */
    default UserPO toPO(UserAggregate aggregate) {
        if (aggregate == null || aggregate.getUserEntity() == null) {
            return null;
        }
        return toUserPO(aggregate.getUserEntity());
    }

    /**
     * PO 列表转换为聚合根列表
     */
    default List<UserAggregate> toAggregateList(List<UserPO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(this::toAggregate).toList();
    }

    // ========== 枚举转换辅助方法 ==========

    /**
     * 数据库 Integer → 领域枚举（roles 归属 role 领域，由应用层按需填充）
     */
    @Named("intToUserStatus")
    default UserStatusEnum intToUserStatus(Integer code) {
        return UserStatusEnum.getByCode(code);
    }

    /**
     * 领域枚举 → 数据库 Integer
     */
    @Named("userStatusToInt")
    default Integer userStatusToInt(UserStatusEnum status) {
        return status == null ? null : status.getCode();
    }
}
