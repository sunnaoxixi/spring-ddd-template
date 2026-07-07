package com.sunnao.spring.ddd.template.domain.system.log.model.aggregate;

import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.common.exception.AggregateException;
import com.sunnao.spring.ddd.template.common.model.BaseAggregate;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.domain.system.log.event.LoginLogEvent;
import com.sunnao.spring.ddd.template.domain.system.log.model.entity.LoginLogEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录日志聚合根
 * <p>
 * 日志只增不改，聚合根仅作为数据载体（异步落库 + 读模式查询）。
 */
@Getter
@Setter
public class LoginLogAggregate extends BaseAggregate {

    /**
     * 登录日志实体
     */
    private LoginLogEntity loginLogEntity;

    /**
     * 从登录日志事件构建聚合根
     *
     * @param event 登录日志事件
     * @return 登录日志聚合根
     * @throws AggregateException 校验失败
     */
    public static LoginLogAggregate create(LoginLogEvent event) throws AggregateException {
        if (event == null) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "登录日志事件不能为空");
        }
        if (StrUtil.isBlank(event.getEmail())) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "登录邮箱不能为空");
        }

        LoginLogEntity entity = new LoginLogEntity();
        entity.setTraceId(event.getTraceId());
        entity.setUserId(event.getOperatorId());
        entity.setEmail(event.getEmail());
        entity.setSuccess(event.isSuccess());
        entity.setCode(event.getCode());
        entity.setMsg(event.getMsg());
        entity.setIp(event.getIp());
        entity.setUserAgent(event.getUserAgent());
        entity.setCreateAt(event.getOccurredAt());

        LoginLogAggregate aggregate = new LoginLogAggregate();
        aggregate.setLoginLogEntity(entity);
        return aggregate;
    }
}
