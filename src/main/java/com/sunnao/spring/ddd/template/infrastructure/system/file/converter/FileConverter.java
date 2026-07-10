package com.sunnao.spring.ddd.template.infrastructure.system.file.converter;

import com.sunnao.spring.ddd.template.domain.system.file.model.aggregate.FileAggregate;
import com.sunnao.spring.ddd.template.domain.system.file.model.entity.FileEntity;
import com.sunnao.spring.ddd.template.infrastructure.system.file.mysql.po.FilePO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;

/**
 * 文件数据转换器
 * 职责：聚合根（内部 FileEntity）与 PO 之间的纯技术转换，无业务逻辑
 */
@Mapper(componentModel = "spring")
public interface FileConverter {

    /**
     * PO 转换为 FileEntity
     */
    FileEntity toEntity(FilePO po);

    /**
     * FileEntity 转换为 PO
     */
    @Mapping(target = "deleted", ignore = true)
    FilePO toFilePO(FileEntity entity);

    /**
     * PO 转换为聚合根（内部构建 FileEntity）
     */
    default FileAggregate toAggregate(FilePO po) {
        if (po == null) {
            return null;
        }
        FileAggregate aggregate = new FileAggregate();
        aggregate.setFileEntity(toEntity(po));
        return aggregate;
    }

    /**
     * 聚合根转换为 PO（拆解内部 FileEntity）
     */
    default FilePO toPO(FileAggregate aggregate) {
        if (aggregate == null || aggregate.getFileEntity() == null) {
            return null;
        }
        return toFilePO(aggregate.getFileEntity());
    }

    /**
     * PO 列表转换为聚合根列表
     */
    default List<FileAggregate> toAggregateList(List<FilePO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(this::toAggregate).toList();
    }
}
