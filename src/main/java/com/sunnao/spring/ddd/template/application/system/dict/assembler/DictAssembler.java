package com.sunnao.spring.ddd.template.application.system.dict.assembler;

import com.sunnao.spring.ddd.template.client.system.dict.model.DictDataDTO;
import com.sunnao.spring.ddd.template.client.system.dict.model.DictTypeDTO;
import com.sunnao.spring.ddd.template.client.system.dict.req.*;
import com.sunnao.spring.ddd.template.client.system.dict.res.QueryDictDataListResponseDTO;
import com.sunnao.spring.ddd.template.client.system.dict.res.QueryDictTypePageResponseDTO;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictDataAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.aggregate.DictTypeAggregate;
import com.sunnao.spring.ddd.template.domain.system.dict.model.entity.DictDataEntity;
import com.sunnao.spring.ddd.template.domain.system.dict.model.entity.DictTypeEntity;
import com.sunnao.spring.ddd.template.domain.system.dict.model.param.*;
import com.sunnao.spring.ddd.template.model.system.dict.DictStatusEnum;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

/**
 * 字典转换器
 * 负责 RequestDTO/ResponseDTO 与领域对象之间的转换
 */
@Mapper(componentModel = "spring")
public interface DictAssembler {

    /**
     * 创建字典类型 RequestDTO 转领域 Param（操作人由应用层从当前用户上下文获取）
     */
    @Mapping(target = "operatorId", expression = "java(operatorId)")
    CreateDictTypeParam toCreateTypeParam(CreateDictTypeRequestDTO requestDTO, @Context Long operatorId);

    /**
     * 修改字典类型 RequestDTO 转领域 Param（client 状态码 → model 枚举）
     */
    default UpdateDictTypeParam toUpdateTypeParam(UpdateDictTypeRequestDTO requestDTO, Long operatorId) {
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
    @Mapping(target = "operatorId", expression = "java(operatorId)")
    DeleteDictTypeParam toDeleteTypeParam(DeleteDictTypeRequestDTO requestDTO, @Context Long operatorId);

    /**
     * 创建字典数据 RequestDTO 转领域 Param
     */
    @Mapping(target = "operatorId", expression = "java(operatorId)")
    CreateDictDataParam toCreateDataParam(CreateDictDataRequestDTO requestDTO, @Context Long operatorId);

    /**
     * 修改字典数据 RequestDTO 转领域 Param（client 状态码 → model 枚举）
     */
    default UpdateDictDataParam toUpdateDataParam(UpdateDictDataRequestDTO requestDTO, Long operatorId) {
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
    @Mapping(target = "operatorId", expression = "java(operatorId)")
    DeleteDictDataParam toDeleteDataParam(DeleteDictDataRequestDTO requestDTO, @Context Long operatorId);

    /**
     * 分页查询 RequestDTO 转领域查询条件
     */
    default DictTypeQuery toDictTypeQuery(QueryDictTypePageRequestDTO requestDTO) {
        DictTypeQuery query = new DictTypeQuery();
        query.setTypeKey(requestDTO.getTypeKey());
        query.setTypeName(requestDTO.getTypeName());
        query.setStatus(DictStatusEnum.getByCode(requestDTO.getStatus()));
        return query;
    }

    /**
     * 字典类型聚合根转 DictTypeDTO（model 枚举 → client 状态码）
     */
    default DictTypeDTO toDictTypeDTO(DictTypeAggregate aggregate) {
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
    default DictDataDTO toDictDataDTO(DictDataAggregate aggregate) {
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
    default QueryDictTypePageResponseDTO toQueryDictTypePageResponseDTO(long total,
                                                                        List<DictTypeAggregate> aggregates) {
        QueryDictTypePageResponseDTO responseDTO = new QueryDictTypePageResponseDTO();
        responseDTO.setTotal(total);
        if (aggregates == null || aggregates.isEmpty()) {
            responseDTO.setTypes(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setTypes(aggregates.stream().map(this::toDictTypeDTO).toList());
        return responseDTO;
    }

    /**
     * 字典数据聚合根列表转列表 ResponseDTO
     */
    default QueryDictDataListResponseDTO toQueryDictDataListResponseDTO(String typeKey,
                                                                        List<DictDataAggregate> aggregates) {
        QueryDictDataListResponseDTO responseDTO = new QueryDictDataListResponseDTO();
        responseDTO.setTypeKey(typeKey);
        if (aggregates == null || aggregates.isEmpty()) {
            responseDTO.setDataList(Collections.emptyList());
            return responseDTO;
        }
        responseDTO.setDataList(aggregates.stream().map(this::toDictDataDTO).toList());
        return responseDTO;
    }

    // ========== 枚举转换辅助方法 ==========

    @Named("intToDictStatus")
    default DictStatusEnum intToDictStatus(Integer code) {
        return DictStatusEnum.getByCode(code);
    }

    @Named("dictStatusToInt")
    default Integer dictStatusToInt(DictStatusEnum status) {
        return status == null ? null : status.getCode();
    }
}
