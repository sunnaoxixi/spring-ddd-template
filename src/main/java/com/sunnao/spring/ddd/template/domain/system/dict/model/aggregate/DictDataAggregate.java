package com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate;

import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.common.exception.AggregateException;
import com.sunnao.spring.ddd.template.common.model.BaseAggregate;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.domain.system.dict.model.entity.DictDataEntity;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.CreateDictDataParam;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.UpdateDictDataParam;
import com.sunnao.spring.ddd.template.model.system.dict.DictStatusEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 字典数据聚合根
 * <p>
 * 不直接持有字典数据属性，仅包含 DictDataEntity 实体；
 * 外部通过聚合根的业务方法访问和变更内部实体，不通过 getter 直接修改实体。
 */
@Getter
@Setter
public class DictDataAggregate extends BaseAggregate {

    /**
     * 字典数据实体
     */
    private DictDataEntity dictDataEntity;

    /**
     * 创建字典数据聚合根
     *
     * @param param 创建参数
     * @return 字典数据聚合根
     * @throws AggregateException 校验失败
     */
    public static DictDataAggregate create(CreateDictDataParam param) throws AggregateException {
        if (param == null) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "创建参数不能为空");
        }
        if (StrUtil.isBlank(param.getTypeKey())) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "字典类型键不能为空");
        }
        if (StrUtil.isBlank(param.getLabel())) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "字典标签不能为空");
        }
        if (StrUtil.isBlank(param.getValue())) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "字典值不能为空");
        }

        DictDataEntity entity = new DictDataEntity();
        entity.setTypeKey(param.getTypeKey());
        entity.setLabel(param.getLabel());
        entity.setValue(param.getValue());
        entity.setSort(param.getSort() != null ? param.getSort() : 0);
        entity.setStatus(DictStatusEnum.ENABLED);
        entity.setRemark(param.getRemark());
        entity.setCreateBy(param.getOperatorId());
        entity.setUpdateBy(param.getOperatorId());

        DictDataAggregate aggregate = new DictDataAggregate();
        aggregate.setDictDataEntity(entity);
        return aggregate;
    }

    /**
     * 修改字典数据（标签/值/排序/状态/备注）
     *
     * @param param 修改参数
     * @throws AggregateException 校验失败
     */
    public void update(UpdateDictDataParam param) throws AggregateException {
        requireEntity();
        this.dictDataEntity.update(param);
    }

    private void requireEntity() throws AggregateException {
        if (this.dictDataEntity == null) {
            throw new AggregateException(ErrorCodeEnum.DATA_ERROR, "字典数据实体不存在");
        }
    }
}
