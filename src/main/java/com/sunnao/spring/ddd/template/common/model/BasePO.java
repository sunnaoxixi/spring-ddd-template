package com.sunnao.spring.ddd.template.common.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 持久化对象基类（审计字段）
 * <p>
 * 所有 PO 继承本类后，MybatisFlexConfigure 注册的全局 Insert/Update 监听器
 * 会在写库时自动填充审计字段（时间取当前时间，操作人取 CurrentUserContext），
 * 已显式赋值的字段不会被覆盖。
 * <p>
 * 逻辑删除字段（deleted）不在本类中，需要逻辑删除的表在各自 PO 上声明。
 */
@Getter
@Setter
public abstract class BasePO {

    /** 创建时间 */
    private LocalDateTime createAt;

    /** 更新时间 */
    private LocalDateTime updateAt;

    /** 创建人ID */
    private Long createBy;

    /** 更新人ID */
    private Long updateBy;
}
