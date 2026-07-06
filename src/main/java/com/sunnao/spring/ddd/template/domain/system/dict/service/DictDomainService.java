package com.sunnao.spring.ddd.template.domain.system.dict.service;

import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.DomainService;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictDataAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictTypeAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.*;

/**
 * 字典领域服务接口（写模式）
 * <p>
 * 封装字典类型与字典数据的核心业务逻辑，维护聚合根完整性与一致性。
 */
public interface DictDomainService extends DomainService {

    /**
     * 创建字典类型
     *
     * @param param 创建参数
     * @return 字典类型聚合根（含回填的ID）
     */
    ResultDO<DictTypeAggregate> createDictType(CreateDictTypeParam param);

    /**
     * 修改字典类型（名称/状态/备注）
     *
     * @param param 修改参数
     * @return 操作结果
     */
    ResultDO<Void> updateDictType(UpdateDictTypeParam param);

    /**
     * 删除字典类型（逻辑删除，同时删除其下字典数据）
     *
     * @param param 删除参数
     * @return 操作结果
     */
    ResultDO<Void> deleteDictType(DeleteDictTypeParam param);

    /**
     * 创建字典数据
     *
     * @param param 创建参数
     * @return 字典数据聚合根（含回填的ID）
     */
    ResultDO<DictDataAggregate> createDictData(CreateDictDataParam param);

    /**
     * 修改字典数据（标签/值/排序/状态/备注）
     *
     * @param param 修改参数
     * @return 操作结果
     */
    ResultDO<Void> updateDictData(UpdateDictDataParam param);

    /**
     * 删除字典数据（逻辑删除）
     *
     * @param param 删除参数
     * @return 操作结果
     */
    ResultDO<Void> deleteDictData(DeleteDictDataParam param);
}
