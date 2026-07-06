package com.sunnao.spring.ddd.template.domain.system.dict.model.entity;

import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.common.exception.AggregateException;
import com.sunnao.spring.ddd.template.common.model.BaseEntity;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.UpdateDictDataParam;
import com.sunnao.spring.ddd.template.model.system.dict.DictStatusEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 字典数据实体
 * <p>
 * 承载字典数据项属性与变更逻辑，由 DictDataAggregate 聚合根持有，
 * 外部只能通过聚合根方法访问本实体。
 */
@Getter
@Setter
public class DictDataEntity extends BaseEntity {

    /**
     * 字典类型键（归属类型，创建后不可变更）
     */
    private String typeKey;

    /**
     * 字典标签
     */
    private String label;

    /**
     * 字典值
     */
    private String value;

    /**
     * 排序（升序）
     */
    private Integer sort;

    /**
     * 状态
     */
    private DictStatusEnum status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 修改字典数据（标签/值/排序/状态/备注，typeKey 不可变更）
     *
     * @param param 修改参数
     * @throws AggregateException 校验失败
     */
    public void update(UpdateDictDataParam param) throws AggregateException {
        if (param == null) {
            throw new AggregateException("PARAM_ERROR", "修改参数不能为空");
        }
        if (StrUtil.isBlank(param.getLabel()) && StrUtil.isBlank(param.getValue())
                && param.getSort() == null && param.getStatus() == null && param.getRemark() == null) {
            throw new AggregateException("PARAM_ERROR", "修改内容不能全部为空");
        }
        if (StrUtil.isNotBlank(param.getLabel())) {
            this.label = param.getLabel();
        }
        if (StrUtil.isNotBlank(param.getValue())) {
            this.value = param.getValue();
        }
        if (param.getSort() != null) {
            this.sort = param.getSort();
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
