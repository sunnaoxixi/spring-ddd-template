package com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate;

import cn.hutool.core.util.StrUtil;
import com.sunnao.spring.ddd.template.common.exception.AggregateException;
import com.sunnao.spring.ddd.template.common.model.BaseAggregate;
import com.sunnao.spring.ddd.template.common.result.ErrorCodeEnum;
import com.sunnao.spring.ddd.template.domain.system.dict.model.entity.DictTypeEntity;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.CreateDictTypeParam;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.UpdateDictTypeParam;
import com.sunnao.spring.ddd.template.model.system.dict.DictStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

/**
 * 字典类型聚合根
 * <p>
 * 不直接持有字典类型属性，仅包含 DictTypeEntity 实体；
 * 外部通过聚合根的业务方法访问和变更内部实体，不通过 getter 直接修改实体。
 */
@Getter
@Setter
public class DictTypeAggregate extends BaseAggregate {

    private static final Pattern TYPE_KEY_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{1,63}$");

    /**
     * 字典类型实体
     */
    private DictTypeEntity dictTypeEntity;

    /**
     * 创建字典类型聚合根
     *
     * @param param 创建参数
     * @return 字典类型聚合根
     * @throws AggregateException 校验失败
     */
    public static DictTypeAggregate create(CreateDictTypeParam param) throws AggregateException {
        if (param == null) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "创建参数不能为空");
        }
        if (StrUtil.isBlank(param.getTypeKey())) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "字典类型键不能为空");
        }
        if (!TYPE_KEY_PATTERN.matcher(param.getTypeKey()).matches()) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "字典类型键须以小写字母开头，仅含小写字母/数字/下划线，长度2~64");
        }
        if (StrUtil.isBlank(param.getTypeName())) {
            throw new AggregateException(ErrorCodeEnum.PARAM_ERROR, "字典类型名称不能为空");
        }

        DictTypeEntity entity = new DictTypeEntity();
        entity.setTypeKey(param.getTypeKey());
        entity.setTypeName(param.getTypeName());
        entity.setStatus(DictStatusEnum.ENABLED);
        entity.setRemark(param.getRemark());
        entity.setCreateBy(param.getOperatorId());
        entity.setUpdateBy(param.getOperatorId());

        DictTypeAggregate aggregate = new DictTypeAggregate();
        aggregate.setDictTypeEntity(entity);
        return aggregate;
    }

    /**
     * 修改字典类型（名称/状态/备注）
     *
     * @param param 修改参数
     * @throws AggregateException 校验失败
     */
    public void update(UpdateDictTypeParam param) throws AggregateException {
        requireEntity();
        this.dictTypeEntity.update(param);
    }

    private void requireEntity() throws AggregateException {
        if (this.dictTypeEntity == null) {
            throw new AggregateException(ErrorCodeEnum.DATA_ERROR, "字典类型实体不存在");
        }
    }
}
