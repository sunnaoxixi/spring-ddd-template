package com.sunnao.spring.ddd.template.domain.system.log.model.aggregate;

import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.common.exception.AggregateException;
import com.sunnao.spring.ddd.template.common.model.BaseAggregate;
import com.sunnao.spring.ddd.template.domain.system.log.event.OperLogEvent;
import com.sunnao.spring.ddd.template.domain.system.log.model.entity.OperLogEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 操作日志聚合根
 * <p>
 * 日志只增不改，聚合根仅作为数据载体（异步落库 + 读模式查询）。
 */
@Getter
@Setter
public class OperLogAggregate extends BaseAggregate {

    /** 操作日志实体 */
    private OperLogEntity operLogEntity;

    /**
     * 从操作日志事件构建聚合根
     *
     * @param event 操作日志事件
     * @return 操作日志聚合根
     * @throws AggregateException 校验失败
     */
    public static OperLogAggregate create(OperLogEvent event) throws AggregateException {
        if (event == null) {
            throw new AggregateException("PARAM_ERROR", "操作日志事件不能为空");
        }
        if (StrUtil.isBlank(event.getModule()) || StrUtil.isBlank(event.getAction())) {
            throw new AggregateException("PARAM_ERROR", "业务模块与操作动作不能为空");
        }

        OperLogEntity entity = new OperLogEntity();
        entity.setTraceId(event.getTraceId());
        entity.setOperatorId(event.getOperatorId());
        entity.setModule(event.getModule());
        entity.setAction(event.getAction());
        entity.setUri(event.getUri());
        entity.setParams(event.getParams());
        entity.setResultCode(event.getResultCode());
        entity.setCostMs(event.getCostMs() != null ? event.getCostMs() : 0L);
        entity.setIp(event.getIp());
        entity.setCreateAt(event.getOccurredAt());

        OperLogAggregate aggregate = new OperLogAggregate();
        aggregate.setOperLogEntity(entity);
        return aggregate;
    }
}
