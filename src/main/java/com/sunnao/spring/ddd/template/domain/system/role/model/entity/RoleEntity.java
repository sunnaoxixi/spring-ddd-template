package com.sunnao.spring.ddd.template.domain.system.role.model.entity;

import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.common.exception.AggregateException;
import com.sunnao.spring.ddd.template.common.model.BaseEntity;
import com.sunnao.spring.ddd.template.domain.system.role.model.param.UpdateRoleParam;
import com.sunnao.spring.ddd.template.model.system.role.RoleStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * 角色实体
 * <p>
 * 承载角色属性与状态变更逻辑，由 RoleAggregate 聚合根持有，
 * 外部只能通过聚合根方法访问本实体。
 */
@Getter
@Setter
public class RoleEntity extends BaseEntity {

    /** 内置角色标识（不可删除，admin 不可禁用） */
    public static final Set<String> BUILT_IN_ROLE_KEYS = Set.of("admin", "user");

    /** 角色标识（Sa-Token 鉴权使用，创建后不可变更） */
    private String roleKey;

    /** 角色名称 */
    private String roleName;

    /** 状态 */
    private RoleStatusEnum status;

    /** 备注 */
    private String remark;

    /**
     * 是否内置角色
     */
    public boolean isBuiltIn() {
        return this.roleKey != null && BUILT_IN_ROLE_KEYS.contains(this.roleKey);
    }

    /**
     * 修改角色（名称/状态/备注，roleKey 不可变更）
     *
     * @param param 修改参数
     * @throws AggregateException 校验失败或状态流转不合法
     */
    public void update(UpdateRoleParam param) throws AggregateException {
        if (param == null) {
            throw new AggregateException("PARAM_ERROR", "修改参数不能为空");
        }
        if (StrUtil.isBlank(param.getRoleName()) && param.getStatus() == null && param.getRemark() == null) {
            throw new AggregateException("PARAM_ERROR", "角色名称、状态、备注不能同时为空");
        }
        if (RoleStatusEnum.DISABLED.equals(param.getStatus()) && "admin".equals(this.roleKey)) {
            throw new AggregateException("ROLE_BUILT_IN", "内置管理员角色不允许禁用");
        }
        if (StrUtil.isNotBlank(param.getRoleName())) {
            this.roleName = param.getRoleName();
        }
        if (param.getStatus() != null) {
            this.status = param.getStatus();
        }
        if (param.getRemark() != null) {
            this.remark = param.getRemark();
        }
        this.setUpdateBy(param.getOperatorId());
    }
}
