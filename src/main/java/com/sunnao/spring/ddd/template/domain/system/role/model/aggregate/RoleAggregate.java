package com.sunnao.spring.ddd.template.domain.system.role.model.aggregate;

import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.common.exception.AggregateException;
import com.sunnao.spring.ddd.template.common.model.BaseAggregate;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.domain.system.role.model.entity.RoleEntity;
import com.sunnao.spring.ddd.template.domain.system.role.model.param.CreateRoleParam;
import com.sunnao.spring.ddd.template.domain.system.role.model.param.UpdateRoleParam;
import com.sunnao.spring.ddd.template.domain.system.role.model.value.PermissionKeysValue;
import com.sunnao.spring.ddd.template.model.system.role.RoleStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

/**
 * 角色聚合根
 * <p>
 * 包含角色实体与该角色拥有的权限 key 集合；
 * 外部通过聚合根的业务方法访问和变更内部实体，不通过 getter 直接修改实体。
 */
@Getter
@Setter
public class RoleAggregate extends BaseAggregate {

    private static final Pattern ROLE_KEY_PATTERN = Pattern.compile("^[a-z][a-z0-9_-]{1,63}$");

    /**
     * 角色实体
     */
    private RoleEntity roleEntity;

    /**
     * 权限标识集合值对象
     */
    private PermissionKeysValue permissionKeys;

    /**
     * 创建角色聚合根
     *
     * @param param 创建参数
     * @return 角色聚合根
     * @throws AggregateException 校验失败
     */
    public static RoleAggregate create(CreateRoleParam param) throws AggregateException {
        if (param == null) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "创建参数不能为空");
        }
        if (StrUtil.isBlank(param.getRoleKey())) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "角色标识不能为空");
        }
        if (!ROLE_KEY_PATTERN.matcher(param.getRoleKey()).matches()) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "角色标识须以小写字母开头，仅含小写字母/数字/下划线/中划线，长度2~64");
        }
        if (StrUtil.isBlank(param.getRoleName())) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "角色名称不能为空");
        }

        RoleEntity entity = new RoleEntity();
        entity.setRoleKey(param.getRoleKey());
        entity.setRoleName(param.getRoleName());
        entity.setStatus(RoleStatusEnum.ENABLED);
        entity.setRemark(param.getRemark());
        entity.setCreateBy(param.getOperatorId());
        entity.setUpdateBy(param.getOperatorId());

        RoleAggregate aggregate = new RoleAggregate();
        aggregate.setRoleEntity(entity);
        return aggregate;
    }

    /**
     * 修改角色（名称/状态/备注）
     *
     * @param param 修改参数
     * @throws AggregateException 校验失败或状态流转不合法
     */
    public void update(UpdateRoleParam param) throws AggregateException {
        requireEntity();
        this.roleEntity.update(param);
    }

    /**
     * 删除前校验（内置角色不允许删除）
     *
     * @throws AggregateException 校验失败
     */
    public void checkDeletable() throws AggregateException {
        requireEntity();
        if (this.roleEntity.isBuiltIn()) {
            throw new AggregateException(ErrorCodeEnum.ROLE_BUILT_IN, "内置角色不允许删除");
        }
    }

    private void requireEntity() throws AggregateException {
        if (this.roleEntity == null) {
            throw new AggregateException(ErrorCodeEnum.DATA_ERROR, "角色实体不存在");
        }
    }
}
