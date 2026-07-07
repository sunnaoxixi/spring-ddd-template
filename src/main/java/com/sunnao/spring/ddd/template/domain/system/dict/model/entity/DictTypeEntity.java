package com.sunnao.spring.ddd.template.domain.system.dict.model.entity;

import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.common.exception.AggregateException;
import com.sunnao.spring.ddd.template.common.model.BaseEntity;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.UpdateDictTypeParam;
import com.sunnao.spring.ddd.template.model.system.dict.DictStatusEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 字典类型实体
 * <p>
 * 承载字典类型属性与变更逻辑，由 DictTypeAggregate 聚合根持有，
 * 外部只能通过聚合根方法访问本实体。
 */
@Getter
@Setter
public class DictTypeEntity extends BaseEntity {

    /**
     * 字典类型键（唯一标识，创建后不可变更）
     */
    private String typeKey;

    /**
     * 字典类型名称
     */
    private String typeName;

    /**
     * 状态
     */
    private DictStatusEnum status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 修改字典类型（名称/状态/备注，typeKey 不可变更）
     *
     * @param param 修改参数
     * @throws AggregateException 校验失败
     */
    public void update(UpdateDictTypeParam param) throws AggregateException {
        if (param == null) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "修改参数不能为空");
        }
        if (StrUtil.isBlank(param.getTypeName()) && param.getStatus() == null && param.getRemark() == null) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "类型名称、状态、备注不能同时为空");
        }
        if (StrUtil.isNotBlank(param.getTypeName())) {
            this.typeName = param.getTypeName();
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
