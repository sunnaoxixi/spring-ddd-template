package com.sunnao.spring.ddd.template.domain.system.user.model.entity;

import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.common.exception.AggregateException;
import com.sunnao.spring.ddd.template.common.model.BaseEntity;
import com.sunnao.spring.ddd.template.domain.system.user.model.param.UpdateUserParam;
import com.sunnao.spring.ddd.template.model.system.user.UserStatusEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户实体
 * <p>
 * 承载用户属性与状态变更逻辑，由 UserAggregate 聚合根持有，
 * 外部只能通过聚合根方法访问本实体。
 */
@Getter
@Setter
public class UserEntity extends BaseEntity {

    /** 邮箱 */
    private String email;

    /** 昵称 */
    private String nickname;

    /** 密码（加密后） */
    private String password;

    /** 状态 */
    private UserStatusEnum status;

    /** 头像URL */
    private String avatar;

    /**
     * 修改用户资料（昵称/头像）
     *
     * @param param 修改参数
     * @throws AggregateException 校验失败
     */
    public void updateProfile(UpdateUserParam param) throws AggregateException {
        if (param == null) {
            throw new AggregateException("PARAM_ERROR", "修改参数不能为空");
        }
        if (StrUtil.isBlank(param.getNickname()) && StrUtil.isBlank(param.getAvatar())) {
            throw new AggregateException("PARAM_ERROR", "昵称与头像不能同时为空");
        }
        if (StrUtil.isNotBlank(param.getNickname())) {
            this.nickname = param.getNickname();
        }
        if (StrUtil.isNotBlank(param.getAvatar())) {
            this.avatar = param.getAvatar();
        }
        this.setUpdateBy(param.getOperatorId());
    }

    /**
     * 启用用户
     *
     * @param operatorId 操作人ID
     * @throws AggregateException 状态流转不合法
     */
    public void enable(Long operatorId) throws AggregateException {
        if (UserStatusEnum.ENABLED.equals(this.status)) {
            throw new AggregateException("STATUS_INVALID", "用户已是启用状态");
        }
        this.status = UserStatusEnum.ENABLED;
        this.setUpdateBy(operatorId);
    }

    /**
     * 禁用用户
     *
     * @param operatorId 操作人ID
     * @throws AggregateException 状态流转不合法
     */
    public void disable(Long operatorId) throws AggregateException {
        if (UserStatusEnum.DISABLED.equals(this.status)) {
            throw new AggregateException("STATUS_INVALID", "用户已是禁用状态");
        }
        this.status = UserStatusEnum.DISABLED;
        this.setUpdateBy(operatorId);
    }

    /**
     * 重置密码
     *
     * @param encodedPassword 加密后的新密码
     * @param operatorId      操作人ID
     * @throws AggregateException 校验失败
     */
    public void resetPassword(String encodedPassword, Long operatorId) throws AggregateException {
        if (StrUtil.isBlank(encodedPassword)) {
            throw new AggregateException("PARAM_ERROR", "密码不能为空");
        }
        this.password = encodedPassword;
        this.setUpdateBy(operatorId);
    }
}
