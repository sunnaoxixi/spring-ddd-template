package com.sunnao.spring.ddd.template.application.system.dict.assembler;

import com.sunnao.spring.ddd.template.client.system.dict.model.DictDataDTO;
import com.sunnao.spring.ddd.template.client.system.dict.model.DictTypeDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.CreateDictDataRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.CreateDictTypeRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.DeleteDictDataRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.DeleteDictTypeRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.QueryDictTypePageRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.UpdateDictDataRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.UpdateDictTypeRequestDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.QueryDictDataListResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.QueryDictTypePageResponseDTO;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictDataAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictTypeAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.entity.DictDataEntity;
import com.sunnao.spring.ddd.template.domain.system.dict.model.entity.DictTypeEntity;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.CreateDictDataParam;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.CreateDictTypeParam;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.DeleteDictDataParam;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.DeleteDictTypeParam;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.DictTypeQuery;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.UpdateDictDataParam;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.UpdateDictTypeParam;
import com.sunnao.spring.ddd.template.model.system.dict.DictStatusEnum;

import java.util.Collections;
import java.util.List;

/**
 * 字典转换器
 * 负责 RequestDTO/ResponseDTO 与领域对象之间的转换
 */
public class DictAssembler {

    private DictAssembler() {
    }

    /**
     * 创建字典类型 RequestDTO 转领域 Param（操作人由应用层从当前用户上下文获取）
     */
    public static CreateDictTypeParam toCreateTypeParam(CreateDictTypeRequestDTO requestDTO, Long operatorId) {
        CreateDictTypeParam param = new CreateDictTypeParam();
        param.setTypeKey(requestDTO.getTypeKey());
        param.setTypeName(requestDTO.getTypeName());
        param.setRemark(requestDTO.getRemark());
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 修改字典类型 RequestDTO 转领域 Param（client 状态码 → model 枚举）
     */
    public static UpdateDictTypeParam toUpdateTypeParam(UpdateDictTypeRequestDTO requestDTO, Long operatorId) {
        UpdateDictTypeParam param = new UpdateDictTypeParam();
        param.setTypeId(requestDTO.getTypeId());
        param.setTypeName(requestDTO.getTypeName());
        param.setStatus(DictStatusEnum.getByCode(requestDTO.getStatus()));
        param.setRemark(requestDTO.getRemark());
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 删除字典类型 RequestDTO 转领域 Param
     */
    public static DeleteDictTypeParam toDeleteTypeParam(DeleteDictTypeRequestDTO requestDTO, Long operatorId) {
        DeleteDictTypeParam param = new DeleteDictTypeParam();
        param.setTypeId(requestDTO.getTypeId());
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 创建字典数据 RequestDTO 转领域 Param
     */
    public static CreateDictDataParam toCreateDataParam(CreateDictDataRequestDTO requestDTO, Long operatorId) {
        CreateDictDataParam param = new CreateDictDataParam();
        param.setTypeKey(requestDTO.getTypeKey());
        param.setLabel(requestDTO.getLabel());
        param.setValue(requestDTO.getValue());
        param.setSort(requestDTO.getSort());
        param.setRemark(requestDTO.getRemark());
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 修改字典数据 RequestDTO 转领域 Param（client 状态码 → model 枚举）
     */
    public static UpdateDictDataParam toUpdateDataParam(UpdateDictDataRequestDTO requestDTO, Long operatorId) {
        UpdateDictDataParam param = new UpdateDictDataParam();
        param.setDataId(requestDTO.getDataId());
        param.setLabel(requestDTO.getLabel());
        param.setValue(requestDTO.getValue());
        param.setSort(requestDTO.getSort());
        param.setStatus(DictStatusEnum.getByCode(requestDTO.getStatus()));
        param.setRemark(requestDTO.getRemark());
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 删除字典数据 RequestDTO 转领域 Param
     */
    public static DeleteDictDataParam toDeleteDataParam(DeleteDictDataRequestDTO requestDTO, Long operatorId) {
        DeleteDictDataParam param = new DeleteDictDataParam();
        param.setDataId(requestDTO.getDataId());
        param.setOperatorId(operatorId);
        return param;
    }

    /**
     * 分页查询 RequestDTO 转领域查询条件
     */
    public static DictTypeQuery toDictTypeQuery(QueryDictTypePageRequestDTO requestDTO) {
        DictTypeQuery query = new DictTypeQuery();
        query.setTypeKey(requestDTO.getTypeKey());
        query.setTypeName(requestDTO.getTypeName());
        query.setStatus(DictStatusEnum.getByCode(requestDTO.getStatus()));
        return query;
    }

    /**
     * 字典类型聚合根转 DictTypeDTO（model 枚举 → client 状态码）
     */
    public static DictTypeDTO toDictTypeDTO(DictTypeAggregate aggregate) {
        if (aggregate == null || aggregate.getDictTypeEntity() == null) {
            return null;
        }
        DictTypeEntity entity = aggregate.getDictTypeEntity();
        DictTypeDTO dto = new DictTypeDTO();
        dto.setId(entity.getId());
        dto.setTypeKey(entity.getTypeKey());
        dto.setTypeName(entity.getTypeName());
        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus().getCode());
        }
        dto.setRemark(entity.getRemark());
        dto.setCreateAt(entity.getCreateAt());
        dto.setUpdateAt(entity.getUpdateAt());
        return dto;
    }

    /**
     * 字典数据聚合根转 DictDataDTO（model 枚举 → client 状态码）
     */
    public static DictDataDTO toDictDataDTO(DictDataAggregate aggregate) {
        if (aggregate == null || aggregate.getDictDataEntity() == null) {
            return null;
        }
        DictDataEntity entity = aggregate.getDictDataEntity();
        DictDataDTO dto = new DictDataDTO();
        dto.setId(entity.getId());
        dto.setTypeKey(entity.getTypeKey());
        dto.setLabel(entity.getLabel());
        dto.setValue(entity.getValue());
        dto.setSort(entity.getSort());
        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus().getCode());
        }
        dto.setRemark(entity.getRemark());
        dto.setCreateAt(entity.getCreateAt());
        dto.setUpdateAt(entity.getUpdateAt());
        return dto;
    }

    /**
     * 字典类型聚合根列表转分页 ResponseDTO
     */
    public static QueryDictTypePageResponseDTO toQueryDictTypePageResponseDTO(long total,
                                                                              List<DictTypeAggregate> aggregates) {
        QueryDictTypePageResponseDTO responseDTO = new QueryDictTypePageResponseDTO();
        responseDTO.setTotal(total);
        if (aggregates == null || aggregates.isEmpty()) {
            responseDTO.setTypes(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setTypes(aggregates.stream().map(DictAssembler::toDictTypeDTO).toList());
        return responseDTO;
    }

    /**
     * 字典数据聚合根列表转列表 ResponseDTO
     */
    public static QueryDictDataListResponseDTO toQueryDictDataListResponseDTO(String typeKey,
                                                                              List<DictDataAggregate> aggregates) {
        QueryDictDataListResponseDTO responseDTO = new QueryDictDataListResponseDTO();
        responseDTO.setTypeKey(typeKey);
        if (aggregates == null || aggregates.isEmpty()) {
            responseDTO.setDataList(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setDataList(aggregates.stream().map(DictAssembler::toDictDataDTO).toList());
        return responseDTO;
    }
}
