package com.sunnao.spring.ddd.template.infrastructure.system.file.converter;

import com.sunnao.spring.ddd.template.domain.system.file.model.aggregate.FileAggregate;
import com.sunnao.spring.ddd.template.domain.system.file.model.entity.FileEntity;
import com.sunnao.spring.ddd.template.infrastructure.system.file.mysql.po.FilePO;
import com.sunnao.spring.ddd.template.model.system.file.FileStorageTypeEnum;

import java.util.Collections;
import java.util.List;

/**
 * 文件数据转换器
 * 职责：聚合根（内部 FileEntity）与 PO 之间的纯技术转换，无业务逻辑
 */
public class FileConverter {

    private FileConverter() {
    }

    /**
     * PO 转换为聚合根（内部构建 FileEntity）
     */
    public static FileAggregate toAggregate(FilePO po) {
        if (po == null) {
            return null;
        }
        FileEntity entity = new FileEntity();
        entity.setId(po.getId());
        entity.setOriginalName(po.getOriginalName());
        entity.setPath(po.getPath());
        entity.setSize(po.getSize());
        entity.setContentType(po.getContentType());
        // 枚举转换：数据库 String → 领域枚举
        entity.setStorageType(FileStorageTypeEnum.getByCode(po.getStorageType()));
        entity.setCreateAt(po.getCreateAt());
        entity.setUpdateAt(po.getUpdateAt());
        entity.setCreateBy(po.getCreateBy());
        entity.setUpdateBy(po.getUpdateBy());

        FileAggregate aggregate = new FileAggregate();
        aggregate.setFileEntity(entity);
        return aggregate;
    }

    /**
     * 聚合根转换为 PO（拆解内部 FileEntity）
     */
    public static FilePO toPO(FileAggregate aggregate) {
        if (aggregate == null || aggregate.getFileEntity() == null) {
            return null;
        }
        FileEntity entity = aggregate.getFileEntity();
        FilePO po = new FilePO();
        po.setId(entity.getId());
        po.setOriginalName(entity.getOriginalName());
        po.setPath(entity.getPath());
        po.setSize(entity.getSize());
        po.setContentType(entity.getContentType());
        // 枚举转换：领域枚举 → 数据库 String
        if (entity.getStorageType() != null) {
            po.setStorageType(entity.getStorageType().getCode());
        }
        po.setCreateAt(entity.getCreateAt());
        po.setUpdateAt(entity.getUpdateAt());
        po.setCreateBy(entity.getCreateBy());
        po.setUpdateBy(entity.getUpdateBy());
        return po;
    }

    /**
     * PO 列表转换为聚合根列表
     */
    public static List<FileAggregate> toAggregateList(List<FilePO> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream().map(FileConverter::toAggregate).toList();
    }
}
