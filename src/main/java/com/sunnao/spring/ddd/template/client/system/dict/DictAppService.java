package com.sunnao.spring.ddd.template.client.system.dict;

import com.sunnao.spring.ddd.template.client.system.dict.req.*;
import com.sunnao.spring.ddd.template.client.system.dict.res.*;
import com.sunnao.spring.ddd.template.common.result.ResultDO;
import com.sunnao.spring.ddd.template.common.service.ApplicationCmdService;

/**
 * 字典应用服务接口（写模式）
 * 职责：定义字典类型与字典数据的写操作接口
 */
public interface DictAppService extends ApplicationCmdService {

    /**
     * 创建字典类型
     *
     * @param requestDTO 请求参数
     * @return 创建结果
     */
    ResultDO<CreateDictTypeResponseDTO> createDictType(CreateDictTypeRequestDTO requestDTO);

    /**
     * 修改字典类型（名称/状态/备注）
     *
     * @param requestDTO 请求参数
     * @return 修改结果
     */
    ResultDO<UpdateDictTypeResponseDTO> updateDictType(UpdateDictTypeRequestDTO requestDTO);

    /**
     * 删除字典类型（逻辑删除，同时删除其下字典数据）
     *
     * @param requestDTO 请求参数
     * @return 删除结果
     */
    ResultDO<DeleteDictTypeResponseDTO> deleteDictType(DeleteDictTypeRequestDTO requestDTO);

    /**
     * 创建字典数据
     *
     * @param requestDTO 请求参数
     * @return 创建结果
     */
    ResultDO<CreateDictDataResponseDTO> createDictData(CreateDictDataRequestDTO requestDTO);

    /**
     * 修改字典数据（标签/值/排序/状态/备注）
     *
     * @param requestDTO 请求参数
     * @return 修改结果
     */
    ResultDO<UpdateDictDataResponseDTO> updateDictData(UpdateDictDataRequestDTO requestDTO);

    /**
     * 删除字典数据（逻辑删除）
     *
     * @param requestDTO 请求参数
     * @return 删除结果
     */
    ResultDO<DeleteDictDataResponseDTO> deleteDictData(DeleteDictDataRequestDTO requestDTO);
}
