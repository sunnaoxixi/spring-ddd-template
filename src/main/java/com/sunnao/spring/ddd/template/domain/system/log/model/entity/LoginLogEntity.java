package com.sunnao.spring.ddd.template.domain.system.log.model.entity;

import com.sunnao.spring.ddd.template.common.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录日志实体
 * <p>
 * 纯数据载体（读模式 + 异步落库），无业务逻辑，由 LoginLogAggregate 聚合根持有。
 * 日志只增不改，updateAt/createBy/updateBy 恒为空。
 */
@Getter
@Setter
public class LoginLogEntity extends BaseEntity {

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 用户ID（登录失败时可为空）
     */
    private Long userId;

    /**
     * 登录邮箱
     */
    private String email;

    /**
     * 是否登录成功
     */
    private Boolean success;

    /**
     * 结果码（SUCCESS 或错误码）
     */
    private String code;

    /**
     * 结果说明（失败原因）
     */
    private String msg;

    /**
     * 客户端IP
     */
    private String ip;

    /**
     * 客户端 User-Agent（超长截断）
     */
    private String userAgent;
}
