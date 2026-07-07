package com.sunnao.spring.ddd.template.infrastructure.system.log.converter;

import com.sunnao.spring.ddd.template.domain.system.log.model.aggregate.LoginLogAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.model.entity.LoginLogEntity;
import com.sunnao.spring.ddd.template.infrastructure.system.log.mysql.po.LoginLogPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;

/**
 * 登录日志数据转换器
 * 职责：聚合根（内部 LoginLogEntity）与 PO 之间的纯技术转换，无业务逻辑
 */
@Mapper(componentModel = "spring")
public interface LoginLogConverter {

    /**
     * PO 转换为 LoginLogEntity（纯字段映射，无枚举转换）
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    LoginLogEntity toEntity(LoginLogPO po);

    /**
     * LoginLogEntity 转换为 PO
     */
    LoginLogPO toLoginLogPO(LoginLogEntity entity);

    /**
     * PO 转换为聚合根（内部构建 LoginLogEntity）
     */
    default LoginLogAggregate toAggregate(LoginLogPO po) {
        if (po == null) {
            return null;
        }
        LoginLogAggregate aggregate = new LoginLogAggregate();
        aggregate.setLoginLogEntity(toEntity(po));
        return aggregate;
    }

    /**
     * 聚合根转换为 PO（拆解内部 LoginLogEntity）
     */
    default LoginLogPO toPO(LoginLogAggregate aggregate) {
        if (aggregate == null || aggregate.getLoginLogEntity() == null) {
            return null;
        }
        return toLoginLogPO(aggregate.getLoginLogEntity());
    }

    /**
     * PO 列表转换为聚合根列表
     */
    default List<LoginLogAggregate> toAggregateList(List<LoginLogPO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(this::toAggregate).toList();
    }
}
