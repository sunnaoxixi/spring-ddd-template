package com.sunnao.spring.ddd.template.domain.system.log.model.entity;

import com.sunnao.spring.ddd.template.common.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 操作日志实体
 * <p>
 * 纯数据载体（读模式 + 异步落库），无业务逻辑，由 OperLogAggregate 聚合根持有。
 * 日志只增不改，updateAt/createBy/updateBy 恒为空。
 */
@Getter
@Setter
public class OperLogEntity extends BaseEntity {

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 操作人ID（登录前操作如 login 可为空）
     */
    private Long operatorId;

    /**
     * 业务模块
     */
    private String module;

    /**
     * 操作动作
     */
    private String action;

    /**
     * 请求 URI
     */
    private String uri;

    /**
     * 请求参数摘要（超长截断）
     */
    private String params;

    /**
     * 结果码（SUCCESS 或错误码）
     */
    private String resultCode;

    /**
     * 耗时（毫秒）
     */
    private Long costMs;

    /**
     * 客户端IP
     */
    private String ip;
}
