package com.sunnao.spring.ddd.template.domain.system.user.model.aggregate;

import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.common.exception.AggregateException;
import com.sunnao.spring.ddd.template.common.model.BaseAggregate;
import com.sunnao.spring.ddd.template.domain.system.user.model.entity.UserEntity;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.ChangeUserStatusParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.CreateUserParam;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.UpdateUserParam;
import com.sunnao.spring.ddd.template.model.system.user.UserStatusEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户聚合根
 * <p>
 * 不直接持有用户属性，仅包含 UserEntity 实体；
 * 外部通过聚合根的业务方法访问和变更内部实体，不通过 getter 直接修改实体。
 */
@Getter
@Setter
public class UserAggregate extends BaseAggregate {

    /** 用户实体 */
    private UserEntity userEntity;

    /**
     * 创建用户聚合根
     *
     * @param param           创建参数
     * @param encodedPassword 加密后的密码
     * @return 用户聚合根
     * @throws AggregateException 校验失败
     */
    public static UserAggregate create(CreateUserParam param, String encodedPassword) throws AggregateException {
        if (param == null) {
            throw new AggregateException("PARAM_ERROR", "创建参数不能为空");
        }
        if (StrUtil.isBlank(param.getEmail())) {
            throw new AggregateException("PARAM_ERROR", "邮箱不能为空");
        }
        if (StrUtil.isBlank(param.getNickname())) {
            throw new AggregateException("PARAM_ERROR", "昵称不能为空");
        }
        if (StrUtil.isBlank(encodedPassword)) {
            throw new AggregateException("PARAM_ERROR", "密码不能为空");
        }

        UserEntity entity = new UserEntity();
        entity.setEmail(param.getEmail());
        entity.setNickname(param.getNickname());
        entity.setPassword(encodedPassword);
        entity.setAvatar(param.getAvatar());
        entity.setStatus(UserStatusEnum.ENABLED);
        entity.setCreateBy(param.getOperatorId());
        entity.setUpdateBy(param.getOperatorId());

        UserAggregate aggregate = new UserAggregate();
        aggregate.setUserEntity(entity);
        return aggregate;
    }

    /**
     * 修改用户资料（昵称/头像）
     *
     * @param param 修改参数
     * @throws AggregateException 校验失败
     */
    public void updateProfile(UpdateUserParam param) throws AggregateException {
        requireEntity();
        this.userEntity.updateProfile(param);
    }

    /**
     * 变更用户状态（启用/禁用）
     *
     * @param param 变更参数
     * @throws AggregateException 校验失败或状态流转不合法
     */
    public void changeStatus(ChangeUserStatusParam param) throws AggregateException {
        requireEntity();
        if (param == null || param.getTargetStatus() == null) {
            throw new AggregateException("PARAM_ERROR", "目标状态不能为空");
        }
        if (UserStatusEnum.ENABLED.equals(param.getTargetStatus())) {
            this.userEntity.enable(param.getOperatorId());
        } else {
            this.userEntity.disable(param.getOperatorId());
        }
    }

    /**
     * 重置密码
     *
     * @param encodedPassword 加密后的新密码
     * @param operatorId      操作人ID
     * @throws AggregateException 校验失败
     */
    public void resetPassword(String encodedPassword, Long operatorId) throws AggregateException {
        requireEntity();
        this.userEntity.resetPassword(encodedPassword, operatorId);
    }

    private void requireEntity() throws AggregateException {
        if (this.userEntity == null) {
            throw new AggregateException("DATA_ERROR", "用户实体不存在");
        }
    }
}
